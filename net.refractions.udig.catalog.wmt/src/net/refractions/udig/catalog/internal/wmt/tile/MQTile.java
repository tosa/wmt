package net.refractions.udig.catalog.internal.wmt.tile;

import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.catalog.internal.wmt.tile.MQTile.MQTileName.MQZoomLevel;
import net.refractions.udig.catalog.internal.wmt.wmtsource.MQSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.core.internal.CorePlugin;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

public class MQTile extends WMTTile {
    private MQTileName tileName;
    private MQSource mqSource;
    
    public MQTile(int x, int y, MQZoomLevel zoomLevel, MQSource mqSource) {
        this(new MQTileName(x, y, zoomLevel, mqSource), mqSource);
    }
    
    public MQTile(MQTileName tileName, MQSource mqSource){
        super(MQTile.getExtentFromTileName(tileName), tileName);
        
        this.tileName = tileName;
        this.mqSource = mqSource;
    }
    //region Get extent from tile-name        
    /**
     * Returns the bounding box of a tile by the given tile name.
     * 
     * see:
     * http://developer.mapquest.com/content/documentation/ApiDocumentation/53/JavaScript/JS_DeveloperGuide_v5.3.0.1.htm#styler-id1.17mods
     * 
     * @param tileName
     * @return BoundingBox for a tile
     */
    public static ReferencedEnvelope getExtentFromTileName(MQTileName tileName) {
        int scale = (int) MQSource.scaleList[tileName.zoomLevel.getZoomLevel()];
        
        // todo: not correct for y = 0
        ReferencedEnvelope extent = new ReferencedEnvelope(
                tile2lon(tileName.getX(), scale), 
                tile2lon(tileName.getX() + 1, scale), 
                tile2lat(tileName.getY() + 1, scale), 
                tile2lat(tileName.getY(), scale), 
                DefaultGeographicCRS.WGS84);
        
        return extent;
    }
    
    private static double tile2lat(int row, int scale) {
        return pixel2lat(MQSource.TILESIZE * row - 1, scale);
    }
    
    private static double tile2lon(int col, int scale) {
        return pixel2lon(MQSource.TILESIZE * col - 1, scale);
    }
    
    private static double pixel2lat(int y, int scale) {
        return (y / (MQSource.PIXELSPERLATDEGREE / scale)) - 90;
    }
    
    private static double pixel2lon(int x, int scale) {
        return (x / (MQSource.PIXELSPERLNGDEGREE / scale)) - 180;
    }
    //endregion
    
    @Override
    public WMTTile getLowerNeighbour() {
        return new MQTile(tileName.getLowerNeighbour(), mqSource);
    }

    @Override
    public WMTTile getRightNeighbour() {
        return new MQTile(tileName.getRightNeighbour(), mqSource);
    }


    public static class MQTileFactory extends WMTTileFactory {

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
        public MQTile getTileFromCoordinate(double lat, double lon, 
                WMTZoomLevel zoomLevel, WMTSource wmtSource) {
            // normalize latitude and longitude
            lat = WMTTileFactory.normalizeDegreeValue(lat, 90);
            lon = WMTTileFactory.normalizeDegreeValue(lon, 180);
            
            double y = (lat + 90.0) * (MQSource.PIXELSPERLATDEGREE / 
                    MQSource.scaleList[zoomLevel.getZoomLevel()]);
            int row = (int) (y / MQSource.TILESIZE);
            
            double x = (lon + 180.0) * (MQSource.PIXELSPERLNGDEGREE / 
                    MQSource.scaleList[zoomLevel.getZoomLevel()]);
            int col = (int) (x / MQSource.TILESIZE);
            
            return new MQTile(col, row, (MQZoomLevel) zoomLevel, (MQSource) wmtSource);
        }
        

        //endregion

        public WMTZoomLevel getZoomLevel( int zoomLevel ) {
            return new MQTileName.MQZoomLevel(zoomLevel);
        }
        
    }
    
    /**
     * A small helper class which stores the tile-name.
     * 
     * @author to.srwn
     * @since 1.1.0
     */
    public static class MQTileName extends WMTTileName{
        private MQZoomLevel zoomLevel;
        private MQSource mqSource;
                
        public MQTileName(int x, int y, MQZoomLevel zoomLevel, MQSource source) {
            super(zoomLevel, x, y, source);
            this.zoomLevel = zoomLevel;
            this.mqSource = source;
        }
        
        /**
         * Generates the url for a tile:
         * "The ROW is represented in URI space as two numerics: the first is the floor 
         * of the ROW divided by 1000, the second is the remainder.
         * The COL is treated similarly. For example, an aerial tile at SCALE=1000, 
         * ROW=142685 and COL=97653, is represented in the URL as /sat/Scale1000/142/685/97/653.jpg."
         * 
         * see http://developer.mapquest.com/content/documentation/ApiDocumentation/53/JavaScript/JS_DeveloperGuide_v5.3.0.1.htm#styler-id1.17mods
         *
         * @param osmSource
         * @return
         */
        public URL getTileUrl() {
            try {
                int rowOne = getY() / 1000;
                int rowTwo = getY() % 1000;
                
                int colOne = getX() / 1000;
                int colTwo = getX() % 1000;
                
                return new URL(null,
                        "http://tile23.mqcdn.com/map/Scale" +  //$NON-NLS-1$
                        ((int) MQSource.scaleList[zoomLevel.getZoomLevel()]) + "/" +  //$NON-NLS-1$
                        rowOne + "/" + //$NON-NLS-1$
                        rowTwo + "/" + //$NON-NLS-1$
                        colOne + "/" + //$NON-NLS-1$
                        colTwo + ".gif", //$NON-NLS-1$
                        CorePlugin.RELAXED_HANDLER); 
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            
            return null;
        }
       
        public MQTileName getRightNeighbour() {
            return new MQTileName( 
                        WMTTileName.arithmeticMod((getX()+1), zoomLevel.getMaxTileNumber()),
                        getY(),
                        zoomLevel,
                        mqSource);
        }
        
        public MQTileName getLowerNeighbour() {
            return new MQTileName( 
                        getX(),
                        WMTTileName.arithmeticMod((getY()-1), zoomLevel.getMaxTileNumber()),
                        zoomLevel,
                        mqSource);
        }
        
        public String toString() {
            return zoomLevel.getZoomLevel() + "/" + getX() + "/" + getY(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MQTileName)) return false;
            
            MQTileName other = (MQTileName) obj;
            
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
        public static class MQZoomLevel extends WMTZoomLevel{
           
            public MQZoomLevel(int zoomLevel) {
                super(zoomLevel);
            }
            
            /**
             * Maximum tile-number for each each zoom-level
             * 
             * (from the MapQuest AJAX API)
             */
            public static final int[] maxTileNumbers = new int[] {
                4,12,36,100,234,502,1084,2272,4694,9778,19558,39116,74900,140818,234698,352047                
            };
            
            /**
             * The maximum tile-number:
             * 
             * For example at zoom-level 0, the tilenames are in the following range:
             * 0..3
             */
            public int calculateMaxTileNumber(int zoomLevel) { 
                return MQZoomLevel.maxTileNumbers[zoomLevel];  
            }   
        }
        
    }
    
}
