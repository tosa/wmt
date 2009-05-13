/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2008, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.catalog.internal.wmt;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.ui.CatalogUIPlugin;
import net.refractions.udig.catalog.ui.ISharedImages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

class WMTGeoResourceInfo extends IGeoResourceInfo {
    /** WMSResourceInfo resource field */
    private final WMTGeoResource resource;
    @SuppressWarnings("unchecked")
    
    WMTGeoResourceInfo(WMTGeoResource resource, IProgressMonitor monitor ) throws IOException {
        this.resource = resource;
        
        this.title = "WMT";
     
          
    }
    @Override
    public CoordinateReferenceSystem getCRS() {
        return DefaultGeographicCRS.WGS84;
    }
    
    
   
}