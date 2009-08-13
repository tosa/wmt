package net.refractions.udig.catalog.internal.wmt.ui.wizard;


import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import net.refractions.udig.catalog.ui.UDIGConnectionFactory;


public class WMTConnectionFactory extends UDIGConnectionFactory {

    public boolean canProcess(Object context) {
        return false;
    }

    public Map<String, Serializable> createConnectionParameters(Object context) {
        System.out.println("createConnectionParameters");
        return null;
    }

    /**
     * This method is only called (never called?), when a GeoResource for a NASA layer is 
     * added, so we can always return the Url for a NASASource service.
     */
    public URL createConnectionURL(Object context) {
        System.out.println("WMTConnectionFactory.createConnectionURL ");
        
        return null;
//        return WMTSource.getRelatedServiceUrl(NASASource.class);
    }
}
