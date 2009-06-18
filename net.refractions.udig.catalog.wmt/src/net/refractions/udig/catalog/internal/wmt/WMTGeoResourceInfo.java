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

import net.refractions.udig.catalog.IGeoResourceInfo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


class WMTGeoResourceInfo extends IGeoResourceInfo {
    /** WMTResourceInfo resource field */
    private final WMTGeoResource resource;
    
    WMTGeoResourceInfo(WMTGeoResource resource, IProgressMonitor monitor ) throws IOException {
        this.resource = resource;
        
        this.title = this.resource.getTitle();
        
        //todo: set bounds so that the whole map is shown
        this.bounds = new ReferencedEnvelope(-180, 180, -90, 90, DefaultGeographicCRS.WGS84);
                  
    }
    @Override
    public CoordinateReferenceSystem getCRS() {
        return DefaultGeographicCRS.WGS84;
    }
    
    
   
}