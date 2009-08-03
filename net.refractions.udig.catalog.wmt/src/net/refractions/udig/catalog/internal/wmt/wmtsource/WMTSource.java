package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.internal.wmt.WMTService;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile.WMTTileFactory;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile.WMTZoomLevel;
import net.refractions.udig.catalog.internal.wmt.ui.properties.WMTLayerProperties;
import net.refractions.udig.catalog.wmsc.server.Tile;
import net.refractions.udig.core.internal.CorePlugin;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.ObjectCache;
import org.geotools.util.ObjectCaches;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 *
 * @author to.srwn
 * @since 1.1.0
 */
public abstract class WMTSource {
    // todo: move into properties
    public static int SCALE_FACTOR = 50;
    
    
    private String name;
    
    /** 
     * This WeakHashMap acts as a memory cache.
     * Because we are using SoftReference, we won't run
     * out of Memory, the GC will free space.
     **/
    private ObjectCache tiles = ObjectCaches.create("soft", 50); //$NON-NLS-1$
    private List<String> tempTileList = new ArrayList<String>(); // just for testing (i will take this out later)
    
    private WMTService wmtService;
    
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
    
    public String getFileFormat() {
        return "png"; //$NON-NLS-1$
    }
    
    public ReferencedEnvelope getBounds() {
        return new ReferencedEnvelope(-180, 180, -85.051, 85.0511, DefaultGeographicCRS.WGS84);        
    }
    
    //region CRS
    public static final CoordinateReferenceSystem CRS_EPSG_900913;    
    static {
        CoordinateReferenceSystem crs = null;
        
        try {
            crs = CRS.decode("EPSG:900913"); //$NON-NLS-1$
        } catch (Exception exc1) {
            String wkt =
                "PROJCS[\"Google Mercator\","+
               "GEOGCS[\"WGS 84\","+
                "    DATUM[\"World Geodetic System 1984\"," +
                "        SPHEROID[\"WGS 84\",6378137.0,298.257223563," +
                "            AUTHORITY[\"EPSG\",\"7030\"]]," +
                "        AUTHORITY[\"EPSG\",\"6326\"]]," +
                "    PRIMEM[\"Greenwich\",0.0," +
                "        AUTHORITY[\"EPSG\",\"8901\"]]," +
                "    UNIT[\"degree\",0.017453292519943295]," +
                "    AXIS[\"Geodetic latitude\",NORTH]," +
                "    AXIS[\"Geodetic longitude\",EAST]," +
                "    AUTHORITY[\"EPSG\",\"4326\"]]," +
                "PROJECTION[\"Mercator_1SP\"]," +
                "PARAMETER[\"semi_minor\",6378137.0]," +
                "PARAMETER[\"latitude_of_origin\",0.0]," +
                "PARAMETER[\"central_meridian\",0.0]," +
                "PARAMETER[\"scale_factor\",1.0],"+
                "PARAMETER[\"false_easting\",0.0]," +
                "PARAMETER[\"false_northing\",0.0]," +
                "UNIT[\"m\",1.0]," +
                "AXIS[\"Easting\",EAST]," +
                "AXIS[\"Northing\",NORTH]," +
                "AUTHORITY[\"EPSG\",\"900913\"]]";
            
            try {
                crs = CRS.parseWKT(wkt);
            } catch (Exception exc2) {
                crs = DefaultGeographicCRS.WGS84;
            }
        }
        
        CRS_EPSG_900913 = crs;
    }
    
    /**
     * The projection the tiles are drawn in.
     *
     * @return
     */
    public CoordinateReferenceSystem getProjectedTileCrs() {
        return WMTSource.CRS_EPSG_900913;
    }
    
    /**
     * The CRS that is used when the extent is cut in tiles.
     *
     * @return
     */
    public CoordinateReferenceSystem getTileCrs() {
        return DefaultGeographicCRS.WGS84;
    }
    //endregion

    //region Methods to access the tile-list (cache)
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
    
    //region Methods related to the service    
    public WMTService getWmtService() {
        return wmtService;
    }

    public void setWmtService(WMTService wmtService) {
        this.wmtService = wmtService;
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
    //endregion
    
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
    }
    
    /**
     * Returns the zoom-level that should be used to fetch the tiles.
     *
     * @param scale
     * @param scaleFactor
     * @param useRecommended always use the calculated zoom-level, do not use the one the user selected
     * @return
     */
    public int getZoomLevelToUse(double scale, int scaleFactor, boolean useRecommended,
            WMTLayerProperties layerProperties) {
        if (useRecommended) {
            return getZoomLevelFromMapScale(scale, scaleFactor);            
        }
        
        // try to load the property values
        boolean selectionAutomatic = true;
        int zoomLevel = -1;
        
        if (layerProperties.load()) {            
            selectionAutomatic = layerProperties.getSelectionAutomatic();
            zoomLevel = layerProperties.getZoomLevel();
        } else {
            selectionAutomatic = true;
        }
        
        // check if the zoom-level is valid
        if (!selectionAutomatic && 
                ((zoomLevel >= getMinZoomLevel()) && (zoomLevel <= getMaxZoomLevel()))) {
            // the zoom-level from the properties is valid, so let's take it
            return zoomLevel;
        } else {
            // No valid property values or automatic selection of the zoom-level
            return getZoomLevelFromMapScale(scale, scaleFactor);
        }
    }
    
    /**
     * Returns the lowest zoom-level number from the scaleList.
     *
     * @param scaleList
     * @return
     */
    public int getMinZoomLevel() {
        double[] scaleList = getScaleList();
        int minZoomLevel = 0;
        
        while (Double.isNaN(scaleList[minZoomLevel]) && (minZoomLevel < scaleList.length)) {
            minZoomLevel++;
        }
        
        return minZoomLevel;       
    }
    
    /**
     * Returns the highest zoom-level number from the scaleList.
     *
     * @param scaleList
     * @return
     */
    public int getMaxZoomLevel() {
        double[] scaleList = getScaleList();
        int maxZoomLevel = scaleList.length - 1;
        
        while (Double.isNaN(scaleList[maxZoomLevel]) && (maxZoomLevel >= 0)) {
            maxZoomLevel--;
        }
        
        return maxZoomLevel;
    }
    //endregion
    
    //region Tiles-Cutting  
    /**
     * Returns the TileFactory which is used to call the 
     * method getTileFromCoordinate().
     */
    public abstract WMTTileFactory getTileFactory();
   
    /**
     * The method which finds all tiles that are within the given extent,
     * used for all different map services.
     * 
     * @see WMTSource.cutExtentIntoTiles(ReferencedEnvelope extent, double scale)
     * @param extent The extent which should be cut.
     * @param scale The map scale.
     * @param scaleFactor The scale-factor (0-100): scale up or down?
     * @param recommendedZoomLevel Always use the calculated zoom-level, do not use the one the user selected
     * @return The list of found tiles.
     */
    public Map<String, Tile> cutExtentIntoTiles(ReferencedEnvelope extent, double scale, 
            int scaleFactor, boolean recommendedZoomLevel, WMTLayerProperties layerProperties) {
        extent = normalizeExtent(extent);
        
        WMTTileFactory tileFactory = getTileFactory();
                
        WMTZoomLevel zoomLevel = tileFactory.getZoomLevel(getZoomLevelToUse(scale, 
                scaleFactor, recommendedZoomLevel, layerProperties), this);
        long maxNumberOfTiles = zoomLevel.getMaxTileNumber();
                
        Map<String, Tile> tileList = new HashMap<String, Tile>();
        
        System.out.println("MinX: " + extent.getMinX() + " MaxX: " + extent.getMaxX());
        System.out.println("MinY: " + extent.getMinY() + " MaxY: " + extent.getMaxY());
        
        // Let's get the first tile which covers the upper-left corner
        WMTTile firstTile = tileFactory.getTileFromCoordinate(
                extent.getMaxY(), extent.getMinX(), zoomLevel, this);
        tileList.put(firstTile.getId(), addTileToList(firstTile));
        
        WMTTile firstTileOfRow = null;
        WMTTile movingTile = firstTileOfRow = firstTile;
        // Loop column
        do {
            // Loop row
            do {
                // get the next tile right of this one
                WMTTile rightNeighbour = movingTile.getRightNeighbour();
                
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
            WMTTile lowerNeighbour = firstTileOfRow.getLowerNeighbour();
            
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
    
    /**
     * The extent from the viewport may look like this:
     * MaxY: 110° (=-70°)   MinY: -110°
     * MaxX: 180°           MinX: -180°
     * 
     * But cutExtentIntoTiles(..) requires an extent that looks like this:
     * MaxY: 85° (or 90°)   MinY: -85° (or -90°)
     * MaxX: 180°           MinX: -180°
     * 
     * @param envelope
     * @return
     */
    private ReferencedEnvelope normalizeExtent(ReferencedEnvelope envelope) {
        ReferencedEnvelope bounds = getBounds();
        
        if (    envelope.getMaxY() > bounds.getMaxY() ||
                envelope.getMinY() < bounds.getMinY() ||
                envelope.getMaxX() > bounds.getMaxX() ||
                envelope.getMinX() < bounds.getMinX()   ) {
            
            
            double maxY = (envelope.getMaxY() > bounds.getMaxY()) ? bounds.getMaxY() : envelope.getMaxY();
            double minY = (envelope.getMinY() < bounds.getMinY()) ? bounds.getMinY() : envelope.getMinY(); 
            double maxX = (envelope.getMaxX() > bounds.getMaxX()) ? bounds.getMaxX() : envelope.getMaxX();
            double minX = (envelope.getMinX() < bounds.getMinX()) ? bounds.getMinX() : envelope.getMinX(); 
            
            ReferencedEnvelope newEnvelope = new ReferencedEnvelope(minX, maxX, minY, maxY, 
                    envelope.getCoordinateReferenceSystem());
            
            return newEnvelope;
        }
        
        return envelope;
    }
    //endregion
    
    @Override
    public String toString() {
        return getName();
    }
}
