package net.refractions.udig.catalog.internal.wmt.tile;

import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import net.refractions.udig.catalog.internal.wmt.tile.OSMTile.OSMTileName.OSMZoomLevel;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
import net.refractions.udig.core.internal.CorePlugin;

/**
 * 
 * TODO Purpose of 
 * <p>
 *
 * </p>
 * @author to.srwn
 * @since 1.1.0
 */
public class OSMTile extends Tile {
    private OSMTileName tileName;
    private OSMSource osmSource;
    
    public OSMTile(int x, int y, OSMZoomLevel zoomLevel, OSMSource osmSource) {
        this(new OSMTileName(x, y, zoomLevel), osmSource);
    }
    
    public OSMTile(OSMTileName tileName, OSMSource osmSource){
        super(tileName.getTileUrl(osmSource), OSMTile.getExtentFromTileName(tileName), tileName.toString());
        
        this.tileName = tileName;
        this.osmSource = osmSource;
    }
    
    public OSMTile getLeftNeighbour() {
        return new OSMTile(tileName.getLeftNeighbour(), osmSource);
    }
    
    public OSMTile getRightNeighbour() {
        return new OSMTile(tileName.getRightNeighbour(), osmSource);
    }
    
    public OSMTile getUpperNeighbour() {
        return new OSMTile(tileName.getUpperNeighbour(), osmSource);
    }
    
    public OSMTile getLowerNeighbour() {
        return new OSMTile(tileName.getLowerNeighbour(), osmSource);
    }
    
    
    
    @Override
    public boolean equals( Object obj ) {
        if (!(obj instanceof OSMTile)) return false;
        
        OSMTile other = (OSMTile) obj;
        
        return tileName.equals(other.tileName);
    }

    //region Get tile from coordinate
    /**
     * Finds out the tile which contains the coordinate at a given zoom level.
     * 
     * see:
     * http://wiki.openstreetmap.org/wiki/Tilenames#Java
     * 
     * @param lat y
     * @param lon x
     * @param zoomLevel
     * @param osmSource
     * @return
     */
    public static OSMTile getTileFromCoordinate(double lat, double lon, OSMZoomLevel zoomLevel, OSMSource osmSource) {
        // normalize latitude and longitude
        lat = normalizeDegreeValue(lat, 90);
        lon = normalizeDegreeValue(lon, 180);
        
        /**
         * Because the latitude is only valid in 
         * 85.0511 °N to 85.0511 °S (http://wiki.openstreetmap.org/wiki/Tilenames#X_and_Y),
         * we have to correct if necessary.
         */
        if (lat > 85.0511) {
            lat = 85.0511;
        } else if(lat < -85.0511) {
            lat = -85.0511;            
        }
        
        int xTile = (int) Math.floor((lon + 180) / 360 * (1 << zoomLevel.getZoomLevel()));
        int yTile = (int) Math.floor(
                (1 - Math.log(Math.tan(lat * Math.PI / 180) + 1
                / Math.cos(lat * Math.PI / 180))
                / Math.PI)
                / 2 * (1 << zoomLevel.getZoomLevel())
            );
        System.out.println("getTileFromCoordinate " + zoomLevel.zoomLevel + "/" + xTile +  "/" + 
                yTile + " lon: " + lon + " lat: " + lat );
        
        return new OSMTile(xTile, yTile, zoomLevel, osmSource);
    }
    
    /**
     * uDig may produce numbers like -210° for the longitude, but we need
     * a number in the range -180 to 180, so instead of -210 we want 150.
     * 
     * @param value the number to normalize (e.g. -210)
     * @param maxValue the maximum value (e.g. 180 -> the range is: -180..180)
     * @return a number between (-maxvalue) and maxvalue
     */
    private static double normalizeDegreeValue(double value, int maxValue) {
        int range = 2 * maxValue;
        value = (value + maxValue) % range;
        
        if (value < 0) {
            value += range;
        }
        
        return (value-maxValue);
    }
    //endregion
    
    //region Get extent from tile-name        
    /**
     * Returns the bounding box of a tile by the given tile name.
     * 
     * see:
     * http://wiki.openstreetmap.org/wiki/Tilenames#compute_bounding_box_for_tile_number
     * 
     * @param tileName
     * @return BoundingBox for a tile
     */
    public static ReferencedEnvelope getExtentFromTileName(OSMTileName tileName) {
        ReferencedEnvelope extent = new ReferencedEnvelope(
                tile2lon(tileName.x, tileName.zoomLevel.getZoomLevel()), 
                tile2lon(tileName.x + 1, tileName.zoomLevel.getZoomLevel()), 
                tile2lat(tileName.y, tileName.zoomLevel.getZoomLevel()), 
                tile2lat(tileName.y + 1, tileName.zoomLevel.getZoomLevel()), 
                DefaultGeographicCRS.WGS84);
        
        return extent;
    }
    
    private static double tile2lon(int x, int z) {        
        return (x / Math.pow(2.0, z) * 360.0) - 180;
    }

    private static double tile2lat(int y, int z) {
        double n = Math.PI - ((2.0 * Math.PI * y) / Math.pow(2.0, z));        
        return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
    }
    //endregion
    
    /**
     * A small helper class which stores the tile-name.
     * 
     * @author to.srwn
     * @since 1.1.0
     */
    public static class OSMTileName {
        private OSMZoomLevel zoomLevel;
        private int x;
        private int y;
                
        public OSMTileName(int x, int y, OSMZoomLevel zoomLevel) {
            this.zoomLevel = zoomLevel;
            this.x = x;
            this.y = y;
        }
        
        /**
         * Generates the url for a tile:
         * <pre>
         * For example:
         * http://tile.openstreetmap.org/{zoom-level}/{x}/{y}.png
         * </pre>
         *
         * @param osmSource
         * @return
         */
        public URL getTileUrl(OSMSource osmSource) {
            try {
                return new URL(null,
                        osmSource.getBaseUrl() + 
                        zoomLevel.zoomLevel + "/" +  //$NON-NLS-1$
                        x + "/" + //$NON-NLS-1$
                        y + ".png", //$NON-NLS-1$
                        CorePlugin.RELAXED_HANDLER); 
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            
            return null;
        }
        
        public OSMTileName getLeftNeighbour() {
            return new OSMTileName( 
                        OSMTileName.arithmeticMod((x-1), zoomLevel.getMaxTileNumber()),
                        y,
                        zoomLevel);
        }
        
        public OSMTileName getRightNeighbour() {
            return new OSMTileName( 
                        OSMTileName.arithmeticMod((x+1), zoomLevel.getMaxTileNumber()),
                        y,
                        zoomLevel);
        }
        
        public OSMTileName getUpperNeighbour() {
            return new OSMTileName( 
                        x,
                        OSMTileName.arithmeticMod((y-1), zoomLevel.getMaxTileNumber()),
                        zoomLevel);
        }
        
        public OSMTileName getLowerNeighbour() {
            return new OSMTileName( 
                        x,
                        OSMTileName.arithmeticMod((y+1), zoomLevel.getMaxTileNumber()),
                        zoomLevel);
        }
        
        /**
         * Arithmetic implementation of modulo,
         * as the Java implementation of modulo can return negative values.
         * <pre>
         * arithmeticMod(-1, 8) = 7
         * </pre>
         *
         * @param a
         * @param b
         * @return the positive remainder
         */
        public static int arithmeticMod(int a, int b) {
            return (a > 0) ? a % b : a % b + b;
        }
        
        public String toString() {
            return zoomLevel.zoomLevel + "/" + x + "/" + y; //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof OSMTileName)) return false;
            
            OSMTileName other = (OSMTileName) obj;
            
            return (x ==other.x) && (y == other.y) && zoomLevel.equals(other.zoomLevel);
        }



        /**
         * Small helper class which wraps the zoom-level and
         * the maximum tile-number for x and y in this zoom-level. 
         * 
         * http://tile.openstreetmap.org/{zoom-level}/{x}/{y}.png
         * 
         * @author to.srwn
         * @since 1.1.0
         */
        public static class OSMZoomLevel {
            private int zoomLevel;
            private int maxTileNumber;
            
            public OSMZoomLevel(int zoomLevel) {
                this.zoomLevel = zoomLevel;                
                
                /**
                 * The maximum tile-number:
                 * 
                 * For example at zoom-level 2, the tilenames are in the following range:
                 * 2/0/0 - 2/3/3
                 * 
                 * (zoom-level/x/y): zoom-level/2^(zoom-level)-1/2^(zoom-level)-1)
                 */
                maxTileNumber = (1 << zoomLevel); // 2 ^ (zoomLevel)                
            }
            
            public int getZoomLevel() {
                return zoomLevel;
            }
            
            public int getMaxTileNumber() {
                return maxTileNumber;
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof OSMZoomLevel)) return false;
                
                OSMZoomLevel other = (OSMZoomLevel) obj;
                
                return zoomLevel == other.zoomLevel;
            }
            
            
        }
        
    }

}
