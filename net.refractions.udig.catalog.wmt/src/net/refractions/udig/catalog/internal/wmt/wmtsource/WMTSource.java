package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;

import net.refractions.udig.catalog.internal.wmt.WMTService;
import net.refractions.udig.catalog.internal.wmt.tile.Tile;
import net.refractions.udig.core.internal.CorePlugin;

/**
 *
 * @author to.srwn
 * @since 1.1.0
 */
public abstract class WMTSource {
    private String name;
    
    public WMTSource() {
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
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
     * @param scale The current map scale.
     * @return Zoom-level
     */
    public int getZoomLevelFromMapScale(double scale) {
        double[] scaleList = getScaleList();
        
        assert(scaleList != null && scaleList.length > 0);
        
        // Start with the most detailed zoom-level and search the best-fitting one
        int zoomLevel = scaleList.length - 1;
        for (int i = scaleList.length-2; i >= 0; i--) {
            if (Double.isNaN(scaleList[i])) break;
            
            if (scale > scaleList[i+1]) {
                zoomLevel = i;
            }
            
        }
        
        return zoomLevel;
    }
    //endregion
    
    //region Tiles-Cutting    
    /**
     * Cuts extent into tiles.
     * 
     * @param extent The extent which should be cut.
     * @param scale The map scale.
     * @return The list of found tiles.
     */
    public abstract List<Tile> cutExtentIntoTiles(ReferencedEnvelope extent, double scale);
    //endregion
    
    @Override
    public String toString() {
        return getName();
    }
    
       
    

}
