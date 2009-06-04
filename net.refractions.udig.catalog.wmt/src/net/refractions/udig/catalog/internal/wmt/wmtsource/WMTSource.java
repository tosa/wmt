package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ObjectCache;
import org.geotools.util.ObjectCaches;

import net.refractions.udig.catalog.internal.wmt.WMTService;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile;
import net.refractions.udig.catalog.wmsc.server.Tile;
import net.refractions.udig.core.internal.CorePlugin;

/**
 *
 * @author to.srwn
 * @since 1.1.0
 */
public abstract class WMTSource {
    private String name;
    
    /** 
     * This WeakHashMap acts as a memory cache.
     * Because we are using SoftReference, we won't run
     * out of Memory, the GC will free space.
     **/
    private ObjectCache tiles = ObjectCaches.create("soft", 50); //$NON-NLS-1$
    private List<String> tempTileList = new ArrayList<String>();
    
    public WMTSource() {
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public int getTileWidth() {
        return 256;
    }
    
    public int getTileHeight() {
        return 256;
    }
    
    //region Methods to access the tile-list
    public boolean listContainsTile(String tileId) {
        System.out.println("ListContainsTile: " + (tiles.peek(tileId) == null) + " - " + (tiles.get(tileId) == null));
        return !(tiles.peek(tileId) == null || tiles.get(tileId) == null);
    }
    
    public WMTTile addTileToList(WMTTile tile) {
        System.out.println(" --------------------------- " + 
                tempTileList.contains(tile.getId()) + " - " + listContainsTile(tile.getId())
                + " --- " + tiles.getKeys().size());
        
        if(!tempTileList.contains(tile.getId()))
            tempTileList.add(tile.getId());
        
        if (listContainsTile(tile.getId())){
            System.out.println(tile.getId() + " already in Cache");
            return getTileFromList(tile.getId());
        } else {
            System.out.println(tile.getId() + " was not in Cache");
            tiles.put(tile.getId(), tile);
            return tile;            
        }
    }
    
    public WMTTile getTileFromList(String tileId) {
        return (WMTTile) tiles.get(tileId);
    }
    //endregion
    
    /**
     * Returns the catalog url for a given class.
     * <pre>
     * For example:
     * getRelatedServiceUrl(OSMMapnikSource.class) returns:
     * wmt://localhost/wmt/net.refractions.udig.catalog.internal.wmt.wmtsource.OSMMapnikSource
     * </pre>
     *
     * @param sourceClass
     * @return catalog url
     */
    public static URL getRelatedServiceUrl(Class<? extends WMTSource> sourceClass) {
        URL url;
        
        try {
            url = new URL(null, WMTService.SERVICE_URL.toString() + sourceClass.getName(), CorePlugin.RELAXED_HANDLER);
        }
        catch(MalformedURLException exc) {
            url = null;
        }        
        
        return url; 
    }
    
    //region Zoom-level
    /**
     * Returns a list that represents a mapping between zoom-levels and map scale.
     * 
     * Array index: zoom-level
     * Value at index: map scale
     * 
     * High zoom-level -> more detailed map
     * Low zoom-level -> less detailed map
     * 
     * @return mapping between zoom-levels and map scale
     */
    public abstract double[] getScaleList();
    
    /**
     * Translates the map scale into a zoom-level for the map services.
     *
     * The scale-factor (0-100) decides whether the tiles will be
     * scaled down (100) or scaled up (0).
     *
     * @param scale The current map scale.
     * @param scaleFactor Scale-factor (0-100)
     * @return Zoom-level
     */
    public int getZoomLevelFromMapScale(double scale, int scaleFactor) {
        double[] scaleList = getScaleList();
        
        assert(scaleList != null && scaleList.length > 0);
        
        // Start with the most detailed zoom-level and search the best-fitting one
        int zoomLevel = scaleList.length - 1;
        for (int i = scaleList.length-2; i >= 0; i--) {
            if (Double.isNaN(scaleList[i])) break;
            if (scale < scaleList[i]) break;
            
            zoomLevel = i;
            if (scale > scaleList[i+1]) {
                zoomLevel = i;
            }
        }
        
       // return zoomLevel;
        // Now apply the scale-factor
        if (zoomLevel == 0) {
            return zoomLevel;
        } else {
            int upperScaleIndex = zoomLevel - 1;
            int lowerScaleIndex = zoomLevel;
            
            double deltaScale = scaleList[upperScaleIndex] - scaleList[lowerScaleIndex];
            double rangeScale = (scaleFactor / 100d) * deltaScale;
            double limitScale = scaleList[lowerScaleIndex] + rangeScale;
            if (scale > limitScale) {
                return upperScaleIndex;
            } else {
                return lowerScaleIndex;
            }
        }
        
        // Scale-Up
//        for (int i = scaleList.length-2; i >= 0; i--) {
//            if (Double.isNaN(scaleList[i])) break;
//            
//            if (scale > scaleList[i+1]) {
//                zoomLevel = i;
//            }            
//        }
        
        // Scale-Down
//        for (int i = scaleList.length-2; i >= 0; i--) {
//            if (Double.isNaN(scaleList[i])) break;
//            if (scale < scaleList[i]) break;
//            
//            zoomLevel = i;
////            if (scale > scaleList[i+1]) {
////                zoomLevel = i;
////            }
//            
//        }
        
        //return zoomLevel;
    }
    //endregion
    
    //region Tiles-Cutting    
    /**
     * Cuts extent into tiles.
     * 
     * @param extent The extent which should be cut.
     * @param scale The map scale.
     * @param scaleFactor The scale-factor (0-100): scale up or down?
     * @return The list of found tiles.
     */
    public abstract Map<String, Tile> cutExtentIntoTiles(ReferencedEnvelope extent, double scale, int scaleFactor);
    //endregion
    
    @Override
    public String toString() {
        return getName();
    }
    
       
    

}
