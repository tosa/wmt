package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.internal.wmt.tile.OSMTile;
import net.refractions.udig.catalog.internal.wmt.tile.OSMTile.OSMTileName.OSMZoomLevel;
import net.refractions.udig.catalog.internal.wmt.ui.properties.WMTLayerProperties;
import net.refractions.udig.catalog.wmsc.server.Tile;

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
     * http://blogs.esri.com/Support/blogs/mappingcenter/archive/2009/03/19/How-can-you-tell-what-map-scales-are-shown-for-online-maps_3F00_.aspx
     * </pre>
     */
    public static double[] scaleList = {
        Double.NaN,
        Double.NaN,    
        147914381,
        73957190,
        36978595,
        18489297,
        9244648,
        4622324,
        2311162,
        1155581,
        577790,
        288895,
        144447,
        72223,
        36111,
        18055,
        9027,
        4513,
        2256
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
    
    //todo: check if tile is rendered from OSM before adding to list (otherwise take the next zoom level!)
    /**
     * OSM implementation of cutting the tiles.
     * 
     * @see WMTSource.cutExtentIntoTiles(ReferencedEnvelope extent, double scale)
     * @param extent The extent which should be cut.
     * @param scale The map scale.
     * @param scaleFactor The scale-factor (0-100): scale up or down?
     * @return The list of found tiles.
     */
    public Map<String, Tile> cutExtentIntoTiles(ReferencedEnvelope extent, double scale, 
            int scaleFactor, boolean recommendedZoomLevel, WMTLayerProperties layerProperties) {
        OSMZoomLevel zoomLevel = new OSMZoomLevel(getZoomLevelToUse(scale, 
                scaleFactor, recommendedZoomLevel, layerProperties));
        long maxNumberOfTiles = ((long) zoomLevel.getMaxTileNumber()) * ((long) zoomLevel.getMaxTileNumber());
                
        Map<String, Tile> tileList = new HashMap<String, Tile>();
        
        System.out.println("MinX: " + extent.getMinX() + " MaxX: " + extent.getMaxX());
        System.out.println("MinY: " + extent.getMinY() + " MaxY: " + extent.getMaxY());
        
        // Let's get the first tile which covers the upper-left corner
        OSMTile firstTile = OSMTile.getTileFromCoordinate(extent.getMaxY(), extent.getMinX(), zoomLevel, this);
        tileList.put(firstTile.getId(), addTileToList(firstTile));
        
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
                    tileList.put(rightNeighbour.getId(), addTileToList(rightNeighbour));
                    
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
                tileList.put(lowerNeighbour.getId(), addTileToList(lowerNeighbour));
                
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
