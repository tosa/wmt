package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.internal.wmt.tile.OSMTile;
import net.refractions.udig.catalog.internal.wmt.tile.Tile;
import net.refractions.udig.catalog.internal.wmt.tile.OSMTile.OSMTileName.OSMZoomLevel;

import org.geotools.geometry.jts.ReferencedEnvelope;

import com.vividsolutions.jts.geom.Envelope;

public abstract class OSMSource extends WMTSource {
    public static String NAME = "OpenStreetMap"; //$NON-NLS-1$

    public OSMSource() {
        System.out.println(NAME);
        setName(NAME); 
    }
    
    public abstract String getBaseUrl();
    
    //region Zoom-level
    /**
     * A list that represents a mapping between OSM zoom-levels and map scale.
     * <pre>
     * see: 
     * http://wiki.openstreetmap.org/wiki/FAQ#What_is_the_map_scale_for_a_particular_zoom_level_of_the_map.3F
     * 
     * Zoom level    Scale as representative fraction    Meters per pixel
     * 18  1 : 1,693   0.597164
     * 17  1 : 3,385   1.194329
     * 16  1 : 6,771   2.388657
     * 15  1 : 14,000  4.777314
     * 14  1 : 27,000  9.554629
     * 13  1 : 54,000  19.109257
     * 12  1 : 108,000     38.218514
     * 11  1 : 217,000     76.437028
     * 10  1 : 433,000     152.874057
     * 9   1 : 867,000     305.748113
     * 8   1 : 2 million   611.496226
     * 7   1 : 3 million   1222.992453
     * 6   1 : 7 million   2445.984905
     * 5   1 : 14 million  4891.969810
     * 4   1 : 28 million  9783.939621
     * 3   1 : 55 million  19567.879241
     * 2   1 : 111 million     39135.758482  
     * </pre>
     */
    private static double[] scaleList = {
        Double.NaN,
        Double.NaN,
        111000000,
        55000000,
        28000000,
        14000000,
        7000000,
        3000000,
        2000000,
        867000,
        433000,
        217000,
        108000,
        54000,
        27000,
        14000,
        6771,
        3385,
        1693                
    };
    
    /**
     * Returns the mapping list
     *
     * @return mapping between OSM zoom-levels and map scale
     */
    @Override
    public double[] getScaleList() { 
        return OSMSource.scaleList;
    }
    //endregion

    //region Tiles-Cutting
    /**
     * OSM implementation of cutting the tiles.
     * 
     * @see WMTSource.cutExtentIntoTiles(ReferencedEnvelope extent, double scale)
     * @param extent The extent which should be cut.
     * @param scale The map scale.
     * @return The list of found tiles.
     */
    public  List<Tile> cutExtentIntoTiles(ReferencedEnvelope extent, double scale) {
        OSMZoomLevel zoomLevel = new OSMZoomLevel(getZoomLevelFromMapScale(scale));
        int maxNumberOfTiles = zoomLevel.getMaxTileNumber() * zoomLevel.getMaxTileNumber();
                
        List<Tile> tileList = new ArrayList<Tile>();
        
        System.out.println("MinX: " + extent.getMinX() + "MaxX: " + extent.getMaxX());
        System.out.println("MinY: " + extent.getMinY() + "MaxY: " + extent.getMaxY());
        
        // Let's get the first tile which covers the upper-left corner
        OSMTile firstTile = OSMTile.getTileFromCoordinate(extent.getMaxY(), extent.getMinX(), zoomLevel, this);
        tileList.add(firstTile);
        
        OSMTile firstTileOfRow = null;
        OSMTile movingTile = firstTileOfRow = firstTile;
        // Loop column
        do {
            // Loop row
            do {
                // get the next tile right of this one
                OSMTile rightNeighbour = movingTile.getRightNeighbour();
                
                // Check if the new tile is still part of the extent and
                // that we don't have the first tile again
                if (extent.intersects((Envelope) rightNeighbour.getExtent())
                        && !firstTileOfRow.equals(rightNeighbour)) {
                    tileList.add(rightNeighbour);
                    
                    System.out.println("adding tile(r) " + rightNeighbour.getId());
                    
                    movingTile = rightNeighbour;
                } else {
                    break;
                }
            } while(tileList.size() <= maxNumberOfTiles);

            // get the next tile under the first one of the row
            OSMTile lowerNeighbour = firstTileOfRow.getLowerNeighbour();
            
            // Check if the new tile is still part of the extent
            if (extent.intersects((Envelope) lowerNeighbour.getExtent())
                    && !firstTile.equals(lowerNeighbour)) {
                tileList.add(lowerNeighbour);
                
                System.out.println("adding tile(l) " + lowerNeighbour.getId());
                
                firstTileOfRow = movingTile = lowerNeighbour;
            } else {
                break;
            }            
        } while(tileList.size() <= maxNumberOfTiles);
        
        return tileList;
    }
    //endregion
}
