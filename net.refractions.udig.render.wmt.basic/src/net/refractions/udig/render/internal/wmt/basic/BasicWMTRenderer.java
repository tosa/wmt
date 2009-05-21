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
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
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
        
        //region tryz
        DirectPosition dpUpper = mapExtentProjected.getUpperCorner();  
        DirectPosition dpLower = mapExtentProjected.getLowerCorner();
        
        java.awt.Point p1 = getContext().worldToPixel(new Coordinate(dpUpper.getCoordinate()[0], dpUpper.getCoordinate()[1]));
        java.awt.Point p2 = getContext().worldToPixel(new Coordinate(dpLower.getCoordinate()[0], dpLower.getCoordinate()[1]));
        
        destination.drawLine(p1.x, p1.y, p2.x, p2.y);
        destination.fillOval(p1.x-50, p1.y+50, 10, 10);
        //endregion
        
        // Scale
        double scale = getContext().getViewportModel().getScaleDenominator();
        System.out.println("Scale: " +  scale + " -  zoom-level: " + wmtSource.getZoomLevelFromMapScale(scale));
        
        //Find tiles
        List<Tile> tileList = wmtSource.cutExtentIntoTiles(mapExtentProjected, scale);
        
        System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
"<kml xmlns=\"http://www.opengis.net/kml/2.2\">" + 
"<Folder>" + 
"<name>Ground Overlays</name>");
        for(Tile tile : tileList) {
            System.out.println("<GroundOverlay>" + 
"            <name></name>" + 
"            <Icon>" + 
"                <href>" + tile.getUrl().toString() + "</href>" + 
"                <viewBoundScale>0,75</viewBoundScale>" + 
"            </Icon>" + 
"            <LatLonBox>" + 
"                <north>" + tile.getExtent().getMaxY() + "</north>" + 
"                <south>" + tile.getExtent().getMinY() + "</south>" + 
"                <east>" + tile.getExtent().getMaxX() + "</east>" + 
"                <west>" + tile.getExtent().getMinX() + "</west>" + 
"            </LatLonBox>" + 
"        </GroundOverlay>");
            
            
            /*//stupid try..
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) tile.getUrl().openConnection();
                
                System.out.println( "HTTP "+connection.getResponseCode()+":" +tile.getUrl() );                 
                if( connection.getResponseCode() == 204 ){
                    connection.disconnect();
                    return; // we must be "No data for this region.";                            
                }
                ImageInputStream imageStream = ImageIO.createImageInputStream(connection.getInputStream());
                
                //ImageInputStream imageStream = Accessor.openImageInput( tile.getUrl());
                
                ImageReader imagePNG = ImageIO.getImageReadersByFormatName("png").next();
                imagePNG.setInput( imageStream );
                
                BufferedImage image = imagePNG.read(0);
                                
                
                java.awt.Point p = getContext().worldToPixel(new Coordinate(tile.getExtent().getMaxY(), tile.getExtent().getMinX()));
                            
                //destination.drawImage(image, null, p.x, p.y);
                destination.drawImage(image, new AffineTransform(1f, 0f, 0f, 1f, p.x, p.y), null);
                
            } catch (IOException e) {
                // TODO Handle IOException
                System.out.println("anzeigen klappt nicht");
            }  
            */
            
        }
        System.out.println("</Folder></kml>");
    }

   

    
    public void refreshImage() throws RenderException {
        System.out.println("refreshImage");
    }

    

}
