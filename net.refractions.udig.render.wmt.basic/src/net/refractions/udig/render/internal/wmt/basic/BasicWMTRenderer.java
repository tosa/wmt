/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.render.internal.wmt.basic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.internal.PreferenceConstants;
import net.refractions.udig.catalog.internal.wms.WmsPlugin;
import net.refractions.udig.catalog.internal.wmt.WMTPlugin;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTileImageReadWriter;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTileSetWrapper;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.wmsc.server.Tile;
import net.refractions.udig.catalog.wmsc.server.TileListener;
import net.refractions.udig.catalog.wmsc.server.TileRange;
import net.refractions.udig.catalog.wmsc.server.TileRangeInMemory;
//import net.refractions.udig.catalog.wmsc.server.TileRangeOnDisk;
import net.refractions.udig.catalog.internal.wmt.tile.TileRangeOnDisk;
import net.refractions.udig.catalog.wmsc.server.TileSet;
import net.refractions.udig.catalog.wmsc.server.TileWorkerQueue;
import net.refractions.udig.catalog.wmsc.server.WMSTile;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.render.impl.RendererImpl;
import net.refractions.udig.project.render.IMultiLayerRenderer;
import net.refractions.udig.project.render.RenderException;
import net.refractions.udig.render.internal.wmsc.basic.WMSCTileCaching;
import net.refractions.udig.render.wmt.basic.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.StyleBuilder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * The basic renderer for a WMT Layer
 * <p>
 * </p>
 */
public class BasicWMTRenderer extends RendererImpl implements IMultiLayerRenderer {
    //todo: move to settings
    private static int WARNING_TOO_MANY_TILES = 50;
    
    private static StyleBuilder styleBuilder = new StyleBuilder();

    private TileListenerImpl listener = new TileListenerImpl();
    
    private final static boolean testing = false;  // for debugging
    private static final boolean TESTING = WMTPlugin.getDefault().isDebugging();
    
    private static int staticid = 0; // for debugging
    
    /**
     * Static thread pools that will be reused for each renderer that gets created
     */
    private static TileWorkerQueue requestTileWorkQueue = new TileWorkerQueue();
    private static TileWorkerQueue writeTileWorkQueue = new TileWorkerQueue();
    
    /**
     * Use a blocking queue to keep track of and notice when tiles are ready to draw
     */
    private BlockingQueue<Tile> tilesToDraw_queue = new PriorityBlockingQueue<Tile>();
    
    private CoordinateReferenceSystem mapCRS;
    private CoordinateReferenceSystem layerCRS;
    
    /**
     * Construct a new BasicWMTRenderer
     */
    public BasicWMTRenderer() {
        System.out.println("BasicWMTRenderer is called!");
     
    }

    @Override
    public void render( Graphics2D destination, IProgressMonitor monitor ) throws RenderException {
        render(destination, getContext().getImageBounds(), monitor);
    }

    @Override
    public void render( IProgressMonitor monitor ) throws RenderException {
        Graphics2D graphics = (Graphics2D) getContext().getImage().getGraphics();
        render(graphics, getRenderBounds(), monitor);
    }

    public synchronized void render( Graphics2D destination, Envelope bounds2,
            IProgressMonitor monitor ) throws RenderException {

        System.out.println("render");
        if (monitor == null){
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask("Render WMT", 100); //$NON-NLS-1$
        setState(STARTING);
                
        ILayer layer = getContext().getLayer();
        // assume everything will work fine
        layer.setStatus(ILayer.DONE);
        
        IGeoResource resource = layer.findGeoResource(WMTSource.class);
        
        if (resource == null) return;
        
        try {
            WMTSource wmtSource = null;
            try {            
                wmtSource = resource.resolve(WMTSource.class, null);
            } catch (Exception e) {
                // TODO Handle IOException
                wmtSource = null;
            }
            
            // todo: error.. etc
            if (wmtSource == null) return;
            
            
            // Get map extent, which should be drawn 
            //todo: difference between getRenderBounds() and context.getViewportModel().getBounds() 
            // and getContext().getImageBounds()??
            ReferencedEnvelope mapExtent = getRenderBounds();
            if (mapExtent == null){
                //mapExtent = getContext().getImageBounds();
                mapExtent = context.getViewportModel().getBounds();
            }
            
            // Get map and layer CRS
            mapCRS = mapExtent.getCoordinateReferenceSystem();
            layerCRS = layer.getCRS(); // should be WGS_84 // can the user change the layer CRS? he can
            
                    
            ReferencedEnvelope mapExtentProjected;
            MathTransform transformLayerToMap = null;
            /*
             * Compare CoordinateReferenceSystem:
             * http://docs.codehaus.org/display/GEOTDOC/05+Use+of+Equals+with+CoordinateReferenceSystem+and+Datum    
             */        
            //if(mapCRS.getName().equals(layerCRS.getName())) {
            if(mapCRS.equals(layerCRS)) {
                // no need to reproject
                mapExtentProjected = mapExtent;
            } else {         
                // Reproject map extent
                try {   
                    // Let's find a transformation to reproject the map extent
                    MathTransform transformMapToLayer = CRS.findMathTransform(mapCRS, layerCRS);
                    // .. and also one for back-projecting the tile extents 
                    transformLayerToMap = CRS.findMathTransform(layerCRS, mapCRS);
                    
                    mapExtentProjected = new ReferencedEnvelope(JTS.transform(mapExtent, transformMapToLayer), layerCRS);
                    
                    //mapExtentProjected = mapExtent.transform(layerCRS, true); // or use JTS.transform?? see BasicWMSCRenderer
                } catch (Exception e) {
                    // map extent can not be reprojected, cancel rendering
                    throw new RenderException("reprojecting error");
                    //todo: nice error message
                }            
            }
            
            // Scale
            double scale = getContext().getViewportModel().getScaleDenominator();
            System.out.println("Scale: " +  scale + " -  zoom-level: " + wmtSource.getZoomLevelFromMapScale(scale, WMTSource.SCALE_FACTOR) + 
                    " " + wmtSource.getZoomLevelToUse(scale, WMTSource.SCALE_FACTOR, false));
            
            //Find tiles
            Map<String, Tile> tileList = wmtSource.cutExtentIntoTiles(mapExtentProjected, scale, WMTSource.SCALE_FACTOR, false);
            
            // check if these are to many tiles
            int tilesCount = tileList.size();
            if (tilesCount > WARNING_TOO_MANY_TILES) {
                // too many tiles, let's use the recommended zoom-level
                tileList.clear();
                tileList = wmtSource.cutExtentIntoTiles(mapExtentProjected, scale, WMTSource.SCALE_FACTOR, true);                
                
                // show a warning about this
                layer.setStatus(ILayer.WARNING);
                layer.setStatusMessage(Messages.WARNING_TOO_MANY_TILES);
                
                System.out.println("tilesCount > WARNING_MANY_TILES");
            }
            
            // Download and display tiles
            
            // look up the preference for caching tiles on-disk or in 
            // memory and use the proper tilerange for that.
            TileRange range = null;
            
            TileSet tileset = new WMTTileSetWrapper(wmtSource);
            com.vividsolutions.jts.geom.Envelope bnds = mapExtentProjected;//new com.vividsolutions.jts.geom.Envelope(mapExtentProjected.getMinX(), mapExtentProjected.getMaxX(), mapExtentProjected.getMinY(), mapExtentProjected.getMaxY());
                    
            String value = CatalogPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_WMSCTILE_CACHING);
            if (value.equals(WMSCTileCaching.ONDISK.toString())) {
                String dir = CatalogPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_WMSCTILE_DISKDIR);
                WMTTileImageReadWriter tileReadWriter = new WMTTileImageReadWriter(dir);
                
                range = new TileRangeOnDisk(null, tileset, bnds, tileList, 
                        requestTileWorkQueue, writeTileWorkQueue, tileReadWriter);
            }
            else {
                range = new TileRangeInMemory(null, tileset, bnds, tileList, requestTileWorkQueue);
            }
            
            // create an empty raster symbolizer for rendering 
            RasterSymbolizer style = styleBuilder.createRasterSymbolizer(); 
            
         // setup how much each tile is worth for the monitor work %
            int tileCount = range.getTileCount();
            int tileWorth = (tileCount/100) * tileCount;
            
            int thisid = 0;
            if (testing) {
                staticid++;
                thisid = staticid;
            }
            
            // first render any tiles that are ready and render non-ready tiles with blank images 
            Map<String, Tile> tiles = range.getTiles();
            Set<String> notRenderedTiles = new HashSet<String>();
            Set<String> renderedTiles = new HashSet<String>();            
          
            for (String key : tiles.keySet()) {
                if (monitor.isCanceled()) {
                    setState(CANCELLED);   
                    if (testing) {
                        System.out.println("monitor CANCELED!!!: "+thisid); //$NON-NLS-1$
                    }
                    return;
                }
                Tile tile = tiles.get(key);
                if (tile != null && tile.getBufferedImage() != null && tile.getTileState() != WMSTile.INERROR) {
                    renderTile(destination, (WMTTile) tile, transformLayerToMap, style);
                    renderedTiles.add(key);
                    monitor.worked(tileWorth);  // inc the monitor work by 1 tile
                }
                else {
                    // set the tile blank (removing any previous content) and add it
                    // to be drawn later
                    renderBlankTile(destination, (WMTTile) tile, transformLayerToMap);
                    notRenderedTiles.add(key);
                }
            }      
            setState(RENDERING);
            
            // if the tilerange is not already completed, then load
            // the missing tiles
            if (!notRenderedTiles.isEmpty()) {
                if (monitor.isCanceled()) {
                    setState(CANCELLED);   
                    if (testing) {
                        System.out.println("monitor CANCELED!!!: "+thisid); //$NON-NLS-1$
                    }
                    return;
                }                
                
                // set the listener on the tile range
                range.addListener(listener);
                
                // load the missing tiles by sending requests for them
                range.loadTiles(monitor);
                
                // block until all the missing tiles have come through (and draw them
                // as they are added to the blocking queue
                while (!notRenderedTiles.isEmpty()) {
                    // check that the rendering is not canceled
                    if (monitor.isCanceled()) {
                        setState(CANCELLED);       
                        if (testing) {
                            System.out.println("monitor CANCELED!!!: "+thisid); //$NON-NLS-1$
                        }
                        tilesToDraw_queue.clear();
                        return;
                    } 
                    
                    if (testing) {
                        System.out.println("BLOCKED: "+thisid); //$NON-NLS-1$
                        System.out.println("waiting on: " + notRenderedTiles.size()+" tiles"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    
                    Tile tile = null;
                    try {
                        tile = (Tile) tilesToDraw_queue.take();  // blocks until a tile is ready to take
                        if (testing) {
                            System.out.println("removed from queue: "+tile.getId()); //$NON-NLS-1$
                        }
                    } catch (InterruptedException ex) {
                        if (testing) {
                            System.out.println("InterruptedException trying to take: "+ex); //$NON-NLS-1$
                        }
                    }
                    
                    if (testing) {
                        System.out.println("UNBLOCKED!!!: "+thisid); //$NON-NLS-1$
                    }
                    
                    // check that the rendering is not canceled again after block
                    if (monitor.isCanceled()) {
                        setState(CANCELLED);     
                        if (testing) {
                            System.out.println("monitor CANCELED!!!: "+thisid); //$NON-NLS-1$
                        }
                        
                        tilesToDraw_queue.clear();
                        return;
                    }
                    
                    // check that the tile's bounds are within the current
                    // context's bounds (if it's not, don't bother drawing it) and also
                    // only draw tiles that haven't already been drawn (panning fast
                    // can result in listeners being notified the same tile is ready multiple
                    // times but we don't want to draw it more than once per render cycle)
                    //ReferencedEnvelope viewbounds = getContext().getViewportModel().getBounds();
                    ReferencedEnvelope viewbounds = getContext().getImageBounds();
                    if (tile != null && tile.getBufferedImage() != null &&
                            viewbounds != null && 
                            mapExtentProjected.intersects(tile.getBounds()) &&
                            // todo: the following is from WMSCRenderer:
                            // it assumes that the map and the tile have the same CRS
                            // is that always true? (possible bug?)
                            //viewbounds.intersects(tile.getBounds()) && 
                            !renderedTiles.contains(tile.getId())) {
    
                        renderTile(destination, (WMTTile) tile, transformLayerToMap, style);
                        renderedTiles.add(tile.getId());
                        monitor.worked(tileWorth);  // inc the monitor work by 1 tile
                        setState(RENDERING); // tell renderer new data is ready                
                    }
                    
                    // remove the tile from the not rendered list regardless
                    // of whether it was actually drawn (this is to prevent
                    // this render cycle from blocking endlessly waiting for tiles
                    // that either didn't return or had some error)
                    notRenderedTiles.remove(tile.getId());
                }
            }
            
            if (testing) {
                System.out.println("DONE!!!: "+thisid); //$NON-NLS-1$
            }
        
    }catch (Exception ex){
        WMTPlugin.log("Error rendering WMT.", ex); //$NON-NLS-1$
    }
    
    monitor.done();
    setState(DONE);   
        
        
        
//        for(WMTTile tile : tileList) {
//            // todo: cache
//            if (tile.getBufferedImage() == null)
//                tile.download();
//                        
//            try {
//                renderTile(destination, tile, style);
//            } catch (Exception e) {
//                System.out.println("renderTile failed");
//            }
//        }
    }
    
    /**
     * 
     * @see net.refractions.udig.render.internal.wmsc.basic#renderTile(Graphics2D graphics, WMTTile tile, CoordinateReferenceSystem crs, RasterSymbolizer style)
     * @param graphics
     * @param tile
     * @param style
     * @throws FactoryException
     * @throws TransformException
     */
    private void renderTile(Graphics2D graphics, WMTTile tile, MathTransform transform, RasterSymbolizer style) throws FactoryException, TransformException {
        
        if (tile == null || tile.getBufferedImage() == null) {
            return;
        }
        
        // create a gridcoverage from the tile image        
        GridCoverageFactory factory = new GridCoverageFactory();
        
        //CoordinateReferenceSystem mercator1 = CRS.decode("EPSG:3785");
        //CoordinateReferenceSystem mercator2 = CRS.decode("EPSG:900913");
        
        String wkt =
            "PROJCS[\"Google Mercator\","+
    "GEOGCS[\"WGS 84\","+
     "   DATUM[\"World Geodetic System 1984\","+
      "      SPHEROID[\"WGS 84\",6378137.0,298.257223563,"+
       "         AUTHORITY[\"EPSG\",\"7030\"]],"+
       "     AUTHORITY[\"EPSG\",\"6326\"]],"+
        "PRIMEM[\"Greenwich\",0.0,"+
         "   AUTHORITY[\"EPSG\",\"8901\"]],"+
        "UNIT[\"degree\",0.017453292519943295],"+
        "AXIS[\"Geodetic latitude\",NORTH],"+
        "AXIS[\"Geodetic longitude\",EAST],"+
        "AUTHORITY[\"EPSG\",\"4326\"]],"+
    "PROJECTION[\"Mercator_1SP\"],"+
    "PARAMETER[\"semi_minor\",6378137.0],"+
    "PARAMETER[\"latitude_of_origin\",0.0],"+
    "PARAMETER[\"central_meridian\",0.0],"+
    "PARAMETER[\"scale_factor\",1.0],"+
    "PARAMETER[\"false_easting\",0.0],"+
    "PARAMETER[\"false_northing\",0.0],"+
    "UNIT[\"m\",1.0],"+
    "AXIS[\"Easting\",EAST],"+
    "AXIS[\"Northing\",NORTH],"+
    "AUTHORITY[\"EPSG\",\"900913\"]]";
        
        // EPSG:3785 does not work properly
//        String wkt =
//        "PROJCS[\"Popular Visualisation CRS / Mercator\","+
//         "      GEOGCS[\"Popular Visualisation CRS\","+
//          "         DATUM[\"Popular_Visualisation_Datum\","+
//           "            SPHEROID[\"Popular Visualisation Sphere\",6378137,0,"+
//            "               AUTHORITY[\"EPSG\",\"7059\"]],"+
//             "          TOWGS84[0,0,0,0,0,0,0],"+
//              "         AUTHORITY[\"EPSG\",\"6055\"]],"+
//               "    PRIMEM[\"Greenwich\",0,"+
//                "       AUTHORITY[\"EPSG\",\"8901\"]],"+
//                 "  UNIT[\"degree\",0.01745329251994328,"+
//                  "     AUTHORITY[\"EPSG\",\"9122\"]],"+
//                   "AUTHORITY[\"EPSG\",\"4055\"]],"+
//               "UNIT[\"metre\",1,"+
//                "   AUTHORITY[\"EPSG\",\"9001\"]],"+
//               "PROJECTION[\"Mercator_1SP\"],"+
//               "PARAMETER[\"central_meridian\",0],"+
//               "PARAMETER[\"scale_factor\",1],"+
//               "PARAMETER[\"false_easting\",0],"+
//               "PARAMETER[\"false_northing\",0],"+
//               "AUTHORITY[\"EPSG\",\"3785\"],"+
//               "AXIS[\"X\",EAST],"+
//               "AXIS[\"Y\",NORTH]]";
        
          CoordinateReferenceSystem googleCRS = CRS.parseWKT(wkt);
        //CoordinateReferenceSystem googleCRS = CRS.decode("EPSG:3395");
          
        MathTransform transformWGSToMercator = CRS.findMathTransform(tile.getExtent().getCoordinateReferenceSystem(), googleCRS);
        MathTransform transformMercatorToMap = CRS.findMathTransform(googleCRS, mapCRS);
        
        
         Envelope tileBndsMercator = JTS.transform(tile.getExtent(), transformWGSToMercator);
         ReferencedEnvelope refEnv = new ReferencedEnvelope(tileBndsMercator, googleCRS); 
         // convert tileBounds to GoogleCRS
        
         
         GridCoverage2D coverage = (GridCoverage2D) factory.create("GridCoverage", tile.getBufferedImage(), refEnv); //$NON-NLS-1$        
         
        //GridCoverage2D coverage = (GridCoverage2D) factory.create("GridCoverage", tile.getBufferedImage(), tile.getExtent()); //$NON-NLS-1$        
        Envelope2D coveragebounds = coverage.getEnvelope2D();

        // bounds of tile
        Envelope bnds = new Envelope(coveragebounds.getMinX(), coveragebounds.getMaxX(), coveragebounds.getMinY(),
                coveragebounds.getMaxY());
        
        
        //convert bounds to necessary viewport projection
//        if (!layerCRS.equals(mapCRS)){
//            //MathTransform transform = CRS.findMathTransform(coverage.getCoordinateReferenceSystem(), getContext().getCRS());
//            bnds = JTS.transform(bnds, transform);
//        }
        if (!googleCRS.equals(mapCRS)){
            //MathTransform transform = CRS.findMathTransform(coverage.getCoordinateReferenceSystem(), getContext().getCRS());
            bnds = JTS.transform(bnds, transformMercatorToMap);
        }
        
        //determine screen coordinates of tiles
        Point upperLeft = getContext().worldToPixel(new Coordinate(bnds.getMinX(), bnds.getMinY()));
        Point bottomRight = getContext().worldToPixel(new Coordinate(bnds.getMaxX(), bnds.getMaxY()));
        Rectangle tileSize = new Rectangle(upperLeft);
        tileSize.add(bottomRight);
        
        
        //render
        try{
            GridCoverageRenderer paint = new GridCoverageRenderer(getContext().getCRS(), bnds, tileSize);
           
            paint.paint(graphics, coverage, style);
           
            boolean TESTING = true;
            if( TESTING ){
                /* for testing draw border around tiles */
                graphics.setColor(Color.BLACK);
                graphics.drawLine((int)tileSize.getMinX(), (int)tileSize.getMinY(), (int)tileSize.getMinX(), (int)tileSize.getMaxY());
                graphics.drawLine((int)tileSize.getMinX(), (int)tileSize.getMinY(), (int)tileSize.getMaxX(), (int)tileSize.getMinY());
                graphics.drawLine((int)tileSize.getMaxX(), (int)tileSize.getMinY(), (int)tileSize.getMaxX(), (int)tileSize.getMaxY());
                graphics.drawLine((int)tileSize.getMinX(), (int)tileSize.getMaxY(), (int)tileSize.getMaxX(), (int)tileSize.getMaxY());
                graphics.drawString(tile.getId(), ((int)tileSize.getMaxX()-113), 
                        ((int)tileSize.getMaxY()-113));
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("Error Rendering tile. Painting Tile:" + (coverage != null ? coverage.getName() : "")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /**
     * Clears the area of the tile on the graphics
     * 
     * @param graphics graphics to draw onto
     * @param style raster symbolizer 
     * @throws FactoryException 
     * @throws TransformException 
     */
    private void renderBlankTile(Graphics2D graphics, WMTTile tile, MathTransform transform) throws FactoryException, TransformException {
        
        if (tile == null) {
            return;
        }
        
        // get the bounds of the tile
        Envelope bnds = tile.getBounds();

        // convert bounds to necessary viewport projection
        if (!layerCRS.equals(mapCRS)) {
            //MathTransform transform = CRS.findMathTransform(tile.getExtent().getCoordinateReferenceSystem(), getContext().getCRS());
            bnds = JTS.transform(bnds, transform);
        }
        
        // determine screen coordinates of tiles
        Point upperLeft = getContext().worldToPixel(new Coordinate(bnds.getMinX(), bnds.getMinY()));
        Point bottomRight = getContext().worldToPixel(new Coordinate(bnds.getMaxX(), bnds.getMaxY()));
        Rectangle tileSize = new Rectangle(upperLeft);
        tileSize.add(bottomRight);

        // render
        try {
            graphics.setBackground(new Color(255, 255, 255, 0));  // set the tile transparent for now
            graphics.clearRect(tileSize.x, tileSize.y, tileSize.width, tileSize.height);

            if( TESTING ){
                /* for testing draw border around tiles */
                graphics.setColor(Color.BLACK);
                graphics.drawLine((int)tileSize.getMinX(), (int)tileSize.getMinY(), (int)tileSize.getMinX(), (int)tileSize.getMaxY());
                graphics.drawLine((int)tileSize.getMinX(), (int)tileSize.getMinY(), (int)tileSize.getMaxX(), (int)tileSize.getMinY());
                graphics.drawLine((int)tileSize.getMaxX(), (int)tileSize.getMinY(), (int)tileSize.getMaxX(), (int)tileSize.getMaxY());
                graphics.drawLine((int)tileSize.getMinX(), (int)tileSize.getMaxY(), (int)tileSize.getMaxX(), (int)tileSize.getMaxY());
            }
        } catch (Throwable t) {
            t.printStackTrace();
            WmsPlugin.log("Error Rendering Blank tile. Painting Tile", t); //$NON-NLS-1$
        }
    } 
    
    /**
     * TileListener implementation for rendering new tiles that are ready
     * 
     * todo: this is a plain copy of the BasiWMSCRenderer implementation!!
     * 
     * 
     * @author GDavis
     * @since 1.1.0
     */
    protected class TileListenerImpl implements TileListener {

        public TileListenerImpl() {
            
        }
        public void notifyTileReady( net.refractions.udig.catalog.wmsc.server.Tile tile ) {
            // set the area that needs updating
            //setRenderBounds(tile.getBounds());
            
            // if the rendering is already in a rendering state, queue this tile
            // to draw and tell the renderer more data is ready, 
            // otherwise create a new rendering thread (which will check the tiles afresh)
            int currentState = getState();
            if ( (currentState == RENDERING || currentState == STARTING) ) {
                // queue the tile to draw
                try {
                    tilesToDraw_queue.put(tile);
                    if (testing) {
                        System.out.println("added to queue: "+tile.getId()); //$NON-NLS-1$
                    }
                } catch (InterruptedException e) {
                    WMTPlugin.log("Error while added tile to queue.", e); //$NON-NLS-1$
                }
            }
            else {  
                if (testing) {
                    System.out.println("RENDER_REQUEST: "+tile.getId()); //$NON-NLS-1$
                }
                setState(RENDER_REQUEST);   // start a new rendering thread
               
            }
        }        
    }
    
    public void refreshImage() throws RenderException {
        System.out.println("refreshImage");
    }
}
