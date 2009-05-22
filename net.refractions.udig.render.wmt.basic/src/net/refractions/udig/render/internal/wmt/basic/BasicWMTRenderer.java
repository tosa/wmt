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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.refractions.udig.catalog.IGeoResource;
//import net.refractions.udig.catalog.internal.wms.WmsPlugin;
import net.refractions.udig.catalog.internal.wmt.WMTGeoResource;
import net.refractions.udig.catalog.internal.wmt.tile.Tile;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.render.impl.RendererImpl;
import net.refractions.udig.project.render.IMultiLayerRenderer;
import net.refractions.udig.project.render.RenderException;
import net.refractions.udig.render.wmt.basic.internal.Messages;

import org.apache.commons.httpclient.HttpConnection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.StyleBuilder;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * The basic renderer for a WMS Layer
 * <p>
 * </p>
 */
public class BasicWMTRenderer extends RendererImpl implements IMultiLayerRenderer {


    /**
     * Construct a new BasicWMSRenderer
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
        
        ILayer layer = getContext().getLayer();
        IGeoResource resource = layer.findGeoResource(WMTSource.class);
        
        if (resource == null) return;
        
        String text;
        WMTSource wmtSource = null;
        try {            
            wmtSource = resource.resolve(WMTSource.class, null);
            text = wmtSource.getName();
        } catch (Exception e) {
            // TODO Handle IOException
            text = null;
        }
        if (text == null) text = "failed";
        
        destination.setColor(Color.BLACK);
        destination.drawString(text, 12, 12);
        
        if (wmtSource == null) return;
        
        
        // Get map extent, which should be drawn 
        //todo: difference between getRenderBounds() and context.getViewportModel().getBounds() ??
        //ReferencedEnvelope mapExtent = getRenderBounds();
        ReferencedEnvelope mapExtent = context.getViewportModel().getBounds();
        
        // Get map and layer CRS
        CoordinateReferenceSystem mapCRS = mapExtent.getCoordinateReferenceSystem();
        CoordinateReferenceSystem layerCRS = layer.getCRS(); // should be WGS_84
        
                
        ReferencedEnvelope mapExtentProjected;
        /*
         * Compare CoordinateReferenceSystem:
         * http://docs.codehaus.org/display/GEOTDOC/05+Use+of+Equals+with+CoordinateReferenceSystem+and+Datum    
         */        
        if(mapCRS.getName().equals(layerCRS.getName())) {
            // no need to reproject
            mapExtentProjected = mapExtent;
        } else {         
            // Reproject map extent
            try {                
                mapExtentProjected = mapExtent.transform(layerCRS, true);
            } catch (Exception e) {
                // map extent can not be reprojected, cancel rendering
                throw new RenderException("reprojecting error");
                //todo: nice error message
            }            
        }
        
        // Scale
        double scale = getContext().getViewportModel().getScaleDenominator();
        System.out.println("Scale: " +  scale + " -  zoom-level: " + wmtSource.getZoomLevelFromMapScale(scale));
        
        //Find tiles
        List<Tile> tileList = wmtSource.cutExtentIntoTiles(mapExtentProjected, scale);
        
        // Download and display tiles
        StyleBuilder styleBuilder = new StyleBuilder();
        RasterSymbolizer style = styleBuilder.createRasterSymbolizer();
        for(Tile tile : tileList) {
            // todo: cache
            tile.download();
                        
            try {
                renderTile(destination, tile, style);
            } catch (Exception e) {
                System.out.println("renderTile failed");
            }
        }
    }
    
    /**
     * 
     * @see net.refractions.udig.render.internal.wmsc.basic#renderTile(Graphics2D graphics, Tile tile, CoordinateReferenceSystem crs, RasterSymbolizer style)
     * @param graphics
     * @param tile
     * @param style
     * @throws FactoryException
     * @throws TransformException
     */
    private void renderTile(Graphics2D graphics, Tile tile, RasterSymbolizer style) throws FactoryException, TransformException {
        
        if (tile == null || tile.getBufferedImage() == null) {
            return;
        }
        
        // create a gridcoverage from the tile image        
        GridCoverageFactory factory = new GridCoverageFactory();
        GridCoverage2D coverage = (GridCoverage2D) factory.create("GridCoverage", tile.getBufferedImage(), tile.getExtent()); //$NON-NLS-1$        
        Envelope2D coveragebounds = coverage.getEnvelope2D();

        // bounds of tile
        Envelope bnds = new Envelope(coveragebounds.getMinX(), coveragebounds.getMaxX(), coveragebounds.getMinY(),
                coveragebounds.getMaxY());

        //convert bounds to necessary viewport projection
        if (!coverage.getCoordinateReferenceSystem().equals(getContext().getCRS())){
            MathTransform transform = CRS.findMathTransform(coverage.getCoordinateReferenceSystem(), getContext().getCRS());
            bnds = JTS.transform(bnds, transform);
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

    
    public void refreshImage() throws RenderException {
        System.out.println("refreshImage");
    }
}
