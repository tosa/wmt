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
import java.io.IOException;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.internal.wmt.WMTGeoResource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.render.impl.RendererImpl;
import net.refractions.udig.project.render.IMultiLayerRenderer;
import net.refractions.udig.project.render.RenderException;
import net.refractions.udig.render.wmt.basic.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

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
        try {
            WMTSource wmtSource;
            wmtSource = resource.resolve(WMTSource.class, null);
            text = wmtSource.getName();
        } catch (Exception e) {
            // TODO Handle IOException
            text = null;
        }
        if (text == null) text = "failed";
        
        destination.setColor(Color.BLACK);
        destination.drawString(text, 12, 12);
        
    }

    public void refreshImage() throws RenderException {
        System.out.println("refreshImage");
    }

    

}
