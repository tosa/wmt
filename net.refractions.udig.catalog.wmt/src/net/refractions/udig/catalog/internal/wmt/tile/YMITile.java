package net.refractions.udig.catalog.internal.wmt.tile;

import java.net.URL;
import java.text.DecimalFormat;

import net.refractions.udig.catalog.internal.wmt.Trace;
import net.refractions.udig.catalog.internal.wmt.WMTPlugin;
import net.refractions.udig.catalog.internal.wmt.tile.YMITile.YMITileName.YMIZoomLevel;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.YMISource;
import net.refractions.udig.core.internal.CorePlugin;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;


public class YMITile extends WMTTile{
    private YMITileName tileName;
    private YMISource ymiSource;
    
    public YMITile(int x, int y, YMIZoomLevel zoomLevel, YMISource ymiSource) {
        this(new YMITileName(x, y, zoomLevel, ymiSource), ymiSource);
    }
    
    public YMITile(YMITileName tileName, YMISource ymiSource){
        super(YMITile.getExtentFromTileName(tileName), tileName);
        
        this.tileName = tileName;
        this.ymiSource = ymiSource;
    }
    //region Get extent from tile-name        
    /**
     * Returns the bounding box of a tile by the given tile name.
     * 
     * The lower left corner of tile 0/0 is at -90,-180.
     * 
     * see:
     * http://developer.mapquest.com/content/documentation/ApiDocumentation/53/JavaScript/JS_DeveloperGuide_v5.3.0.1.htm#styler-id1.17
     * 
     * @param tileName
     * @return BoundingBox for a tile
     */
    public static ReferencedEnvelope getExtentFromTileName(YMITileName tileName) {
        ReferencedEnvelope extent = new ReferencedEnvelope(
                tile2lon(tileName.getX(), tileName.zoomLevel.getZoomLevel()), 
                tile2lon(tileName.getX() + 1, tileName.zoomLevel.getZoomLevel()), 
                tile2lat(tileName.getY(), tileName.zoomLevel.getZoomLevel()), 
                tile2lat(tileName.getY() + 1, tileName.zoomLevel.getZoomLevel()), 
                DefaultGeographicCRS.WGS84);
        
        return extent;
    }
    
    private static double tile2lon(double x, int z) {        
        return (x / YMITileName.YMIZoomLevel.maxTileNumbers[z] * 360.0) - 180;
    }

    private static double tile2lat(double y, int z) {
        double tileCount = YMITileName.YMIZoomLevel.maxTileNumbers[z];
        double n = Math.PI - ((2.0 * Math.PI * y) / tileCount);        
        return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
    }
    //endregion
    
    @Override
    public YMITile getLowerNeighbour() {
        return new YMITile(tileName.getLowerNeighbour(), ymiSource);
    }

    @Override
    public YMITile getRightNeighbour() {
        return new YMITile(tileName.getRightNeighbour(), ymiSource);
    }


    public static class YMITileFactory extends WMTTileFactory {

        //region Get tile from coordinate
        /**
         * Finds out the tile which contains the coordinate at a given zoom level.
         * 
         * see:
         * http://developer.mapquest.com/content/documentation/ApiDocumentation/53/JavaScript/JS_DeveloperGuide_v5.3.0.1.htm#styler-id1.17
         * 
         * @param lat y
         * @param lon x
         * @param zoomLevel
         * @param wmtSource
         * @return
         */
        public YMITile getTileFromCoordinate(double lat, double lon, 
                WMTZoomLevel zoomLevel, WMTSource wmtSource) {
            // normalize latitude and longitude
            lat = WMTTileFactory.normalizeDegreeValue(lat, 90);
            lon = WMTTileFactory.normalizeDegreeValue(lon, 180);
            
            /**
             * Because the latitude is only valid in 
             * 85.0511 °N to 85.0511 °S (http://wiki.openstreetmap.org/wiki/Tilenames#X_and_Y),
             * we have to correct if necessary.
             */
            lat = WMTTileFactory.moveInRange(lat, -85.0511, 85.0511);
            
            double tileCount = YMITileName.YMIZoomLevel.maxTileNumbers[zoomLevel.getZoomLevel()];
            int xTile = (int) Math.floor((lon + 180) / 360 * tileCount);
            int yTile = (int) Math.floor(
                    (1 - Math.log(Math.tan(lat * Math.PI / 180) + 1
                    / Math.cos(lat * Math.PI / 180))
                    / Math.PI)
                    / 2 * tileCount
                );
            
            WMTPlugin.debug("[YMITile.getTileFromCoordinate] " + zoomLevel.getZoomLevel() + //$NON-NLS-1$
                    "/" + xTile +  "/" + yTile + " lon: " + lon + " lat: " + lat, Trace.OSM);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            
            return new YMITile(xTile, yTile, (YMIZoomLevel) zoomLevel, (YMISource) wmtSource);
        }
        

        //endregion

        public WMTZoomLevel getZoomLevel(int zoomLevel, WMTSource wmtSource) {
            return new YMITileName.YMIZoomLevel(zoomLevel);
        }
        
    }
    
    /**
     * A small helper class which stores the tile-name.
     * 
     * @author to.srwn
     * @since 1.1.0
     */
    public static class YMITileName extends WMTTileName{
        private YMIZoomLevel zoomLevel;
        private YMISource ymiSource;
        
        private static DecimalFormat formatter = new DecimalFormat("##0.#################"); //$NON-NLS-1$
                
        public YMITileName(int x, int y, YMIZoomLevel zoomLevel, YMISource source) {
            super(zoomLevel, x, y, source);
            this.zoomLevel = zoomLevel;
            this.ymiSource = source;
        }
        
        /**
         * Asks the MapQuest API to generate the map-image url.
         * 
         * @return
         */
        public URL getTileUrl() {
            try {
                int zoom = zoomLevel.getZoomLevel();
                
                // the map is generated by the center coordinate
                double lon = getCenterLon(zoom);
                double lat = getCenterLat(zoom);
                
                String mapImageUrl = "http://local.yahooapis.com/MapsService/V1/mapImage?appid=YD-4g6HBf0_JX0yq2IsdnV1Ne9JTpKxQ3Miew--" + 
                    "&latitude=" + formatDouble(lat) + 
                    "&longitude=" + formatDouble(lon) + 
                    "&zoom=" + (12 - zoom) + 
                    "&image_height=" + YMISource.TILESIZE + "&image_width=" + YMISource.TILESIZE + "&image_type=png&output=xml";
                
                // get the image url from the request
                
                return new URL(null, mapImageUrl, CorePlugin.RELAXED_HANDLER);  
                
            } catch (Exception e) {
                WMTPlugin.log("[YMITile] Could not create the url for tile (Zoom: " + zoomLevel.getZoomLevel() + //$NON-NLS-1$
                        ", X: " + getX() + ", " + getY(), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            return null;
        }
       
        private static String formatDouble(double value) {
            String formattedString = formatter.format(value).replace(',', '.');
            
            if (formattedString.equals("0")) { //$NON-NLS-1$
                // Yahoo does not understand "0" or "0.0" as coordinate..
                return "0.0000000000000000000001"; //$NON-NLS-1$
            } else {
                return formattedString;
            }
        }
        
        public double getCenterLon(int z) {
            return YMITile.tile2lon(getX() + 0.5, z);
        }
       
        public double getCenterLat(int scale) {
            return YMITile.tile2lat(getY() + 0.5, scale);
        }
        
        public YMITileName getRightNeighbour() {
            return new YMITileName( 
                        WMTTileName.arithmeticMod((getX()+1), zoomLevel.getMaxTilePerRowNumber()),
                        getY(),
                        zoomLevel,
                        ymiSource);
        }
        
        public YMITileName getLowerNeighbour() {
            return new YMITileName( 
                        getX(),
                        WMTTileName.arithmeticMod((getY()+1), zoomLevel.getMaxTilePerColNumber()),
                        zoomLevel,
                        ymiSource);
        }
        
        public String toString() {
            return zoomLevel.getZoomLevel() + "/" + getX() + "/" + getY(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof YMITileName)) return false;
            
            YMITileName other = (YMITileName) obj;
            
            return (getX() == other.getX()) && (getY() == other.getY()) && zoomLevel.equals(other.zoomLevel);
        }

        /**
         * Small helper class which wraps the zoom-level and
         * the maximum tile-number for x and y in this zoom-level. 
         * 
         * 
         * @author to.srwn
         * @since 1.1.0
         */
        public static class YMIZoomLevel extends WMTZoomLevel{
           
            public YMIZoomLevel(int zoomLevel) {
                super(zoomLevel);
            }
            
            /**
             * Maximum tile-number for each each zoom-level
             * 
             * (similar to the MapQuest AJAX API)
             */
            public static final int[] maxTileNumbers = new int[] {
                42,
                84,
                167,
                333,
                666,
                1332,
                2664,
                5327,
                10654,
                21308,
                42615,
                85230
            };
            
            /**
             * The maximum tile-number:
             * 
             * For example at zoom-level 0, the tilenames are in the following range:
             * 0..1
             */
            @Override
            public int calculateMaxTilePerColNumber(int zoomLevel) {
                return YMIZoomLevel.maxTileNumbers[zoomLevel];  
            }

            @Override
            public int calculateMaxTilePerRowNumber(int zoomLevel) {
                return calculateMaxTilePerColNumber(zoomLevel);
            }   
        }
        
    }
}
