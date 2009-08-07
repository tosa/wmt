package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.refractions.udig.catalog.internal.wmt.tile.NASATile;
import net.refractions.udig.catalog.internal.wmt.tile.NASATile.NASATileName.NASAZoomLevel;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile.WMTTileFactory;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Element;

/**
 * This class represents one TiledGroup
 * see http://onearth.jpl.nasa.gov/wms.cgi?request=GetTileService
 * and http://onearth.jpl.nasa.gov/tiled.html
 * 
 * @author to.srwn
 * @since 1.1.0
 */
public class NASASource extends WMTSource {
//    public static final String KEY_TILEGROUP_NAME = "NASA_TILEGROUP"; //$NON-NLS-1$
    
    private static WMTTileFactory tileFactory = new NASATile.NASATileFactory();
    
    private ReferencedEnvelope bounds;
    /* The prefix for the request: http://wms.jpl.nasa.gov/wms.cgi? */
    private String baseUrl;

    private List<NASAZoomLevel> zoomLevels;    
    private double[] scales;
    
    private int tileWidth;
    private int tileHeight;
    private String tileFormat;
    
    private static final String tileWidthPatternString = "(width=)(\\d*)(&)"; //$NON-NLS-1$
    private static final String tileHeightPatternString = "(height=)(\\d*)(&)"; //$NON-NLS-1$
    private static final String tileFormatPatternString = "(format=image/)([a-zA-Z]+)(&)"; //$NON-NLS-1$
    private static final Pattern tileWidthPattern = Pattern.compile(
            tileWidthPatternString, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern tileHeightPattern = Pattern.compile(
            tileHeightPatternString, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern tileFormatPattern = Pattern.compile(
            tileFormatPatternString, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
   
    protected NASASource() {}

    /**
     * This method parses all needed information: bounds, zoom-levels, 
     * tile-size and tile-format.
     */
    @Override
    protected void init(String resourceId) throws Exception {
        NASASourceManager sourceManager = NASASourceManager.getInstance();
        
        Element tiledGroup = sourceManager.getTiledGroup(resourceId);
        
        this.baseUrl = sourceManager.getBaseUrl();       
        setName(tiledGroup.getChildText("Name")); //$NON-NLS-1$
        setBounds(tiledGroup.getChild("LatLonBoundingBox")); //$NON-NLS-1$
                
        String firstTilePattern = tiledGroup.getChild("TilePattern").getValue(); //$NON-NLS-1$
        setTileSize(firstTilePattern);
        setTileFormat(firstTilePattern);
        
        setZoomLevels(tiledGroup.getChildren("TilePattern"), baseUrl); //$NON-NLS-1$        
    }

    //region Scales/Zoom-Levels
    /**
     * Retrieves zoom-levels from the TilePatterns of a TiledGroup
     *
     * @param tilePatterns
     * @param baseUrl
     */
    private void setZoomLevels(List<?> tilePatterns, String baseUrl) {
        zoomLevels = new ArrayList<NASAZoomLevel>();
        
        for(Object element : tilePatterns) {
            if (element instanceof Element) {
                Element tilePattern = (Element) element;
                String tilePatternText = tilePattern.getValue();
                NASAZoomLevel zoomLevel = new NASAZoomLevel(tilePatternText, this);
                
                System.out.println("Zoom-Level " + zoomLevel.getScale() + " " + zoomLevel.getWidthInWorldUnits());
                
                zoomLevels.add(zoomLevel);
            }
        }
        
        generateScaleList();       
    }
    
    /**
     * Generates the scale-list from the given zoom-level-list.
     */
    private void generateScaleList() {
        // first: sort the zoom-level list, so that low scales (high numbers)
        // are at top of the list
        Collections.sort(zoomLevels);
        Collections.reverse(zoomLevels);
        
        // generate scale list  
        scales = new double[zoomLevels.size()];
        for (int i = 0; i < zoomLevels.size(); i++) {
            zoomLevels.get(i).setZoomLevel(i);
            
            scales[i] = zoomLevels.get(i).getScale();
        }     
    }
    //endregion
    
    //region TiledGroup-bounds
    private void setBounds(Element latLonBoundingBox) {
        try {
            double minX = latLonBoundingBox.getAttribute("minx").getDoubleValue(); //$NON-NLS-1$
            double maxX = latLonBoundingBox.getAttribute("maxx").getDoubleValue(); //$NON-NLS-1$
            double minY = latLonBoundingBox.getAttribute("miny").getDoubleValue(); //$NON-NLS-1$
            double maxY = latLonBoundingBox.getAttribute("maxy").getDoubleValue(); //$NON-NLS-1$
            
            bounds = new ReferencedEnvelope(minX, maxX, minY, maxY, DefaultGeographicCRS.WGS84);            
        } catch(Exception exc) {
            bounds = new ReferencedEnvelope(-180, 180, -90, 90, DefaultGeographicCRS.WGS84);
        }
        System.out.println(bounds);
    }
    //endregion
    
    //region Tile-Size and Tile-Format
    /**
     * Gets the tile-size from the first tile-pattern.
     * 
     *
     * @param tilePattern
     */
    private void setTileSize(String tilePattern) {
        Matcher mWidth = tileWidthPattern.matcher(tilePattern);
        Matcher mHeight = tileHeightPattern.matcher(tilePattern);
        
        if (mWidth.find() && mHeight.find()) {
            String widthText = mWidth.group(2);
            String heightText = mHeight.group(2);
            
            try{
                int width = Integer.parseInt(widthText);
                int height = Integer.parseInt(heightText);
                
                tileWidth = width;
                tileHeight = height;
                
                return;
            } catch(Exception exc) {}
        }
        
        tileWidth = 512;
        tileHeight = 512;
    }
    
    /**
     * Get the image format from the first tile-pattern.
     *
     * @param tilePattern
     */
    private void setTileFormat(String tilePattern) {
        Matcher matcher = tileFormatPattern.matcher(tilePattern);
        
        String format = "jpeg"; //$NON-NLS-1$
        if (matcher.find()) {
            format = matcher.group(2);
        }
        
        tileFormat = format;
    }
    //endregion
    
    @Override
    public String getFileFormat() {
        return tileFormat; 
    }

    @Override
    public double[] getScaleList() {
        return scales;
    }
    
    public NASAZoomLevel getZoomLevel(int index) {
        return zoomLevels.get(index);
    }

    @Override
    public ReferencedEnvelope getBounds() {
        return bounds;
    }
    
    @Override
    public WMTTileFactory getTileFactory() {
        return tileFactory;
    }

    @Override
    public int getTileHeight() {
        return tileHeight;
    }

    @Override
    public int getTileWidth() {
        return tileWidth;
    }

    public String getBaseUrl(){
        return baseUrl;
    }
}
