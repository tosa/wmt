package net.refractions.udig.catalog.internal.wmt;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMMapnikSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;

public class WMTServiceExtension implements ServiceExtension {
    
	public static final String KEY = "net.refractions.udig.catalog.internal.wmt.url"; //$NON-NLS-1$
	
    /**
     * TODO summary sentence for createService ...
     * 
     * @see net.refractions.udig.catalog.ServiceExtension#createService(java.net.URL, java.util.Map)
     * @param id
     * @param params
     * @return
     */
    public WMTService createService( URL id, Map<String,Serializable> params ) {
        System.out.println("WMTServiceExtension.createService");//$NON-NLS-1$
        if (params == null)
            return null;
        
        if( params.containsKey(KEY)){
            System.out.println((URL) params.get(WMTServiceExtension.KEY));
            return new WMTService(params);
        }
        
        return null;
    }
    
    /**
     * Get service from WMTSource-Class
     * 
     * Usage:
     * IService = serviceExtension.createService(OSMMapnikSource.class);
     * 
     * @param sourceClass
     * @return
     */
    public WMTService createService(Class<? extends WMTSource> sourceClass) {
        URL url = OSMSource.getRelatedServiceUrl(sourceClass);        
        WMTService service = createService(null, createParams(url));
        
        return service;        
    }

    /**
     * TODO summary sentence for createParams ...
     * 
     * @see net.refractions.udig.catalog.ServiceExtension#createParams(java.net.URL)
     * @param url
     * @return
     */
    public Map<String,Serializable> createParams( URL url ) {
        System.out.println("createParams");//$NON-NLS-1$
        //todo: check if the class exists?
        if( url != null && url.toExternalForm().startsWith( WMTService.SERVICE_URL.toExternalForm())){
            Map<String,Serializable> map = new HashMap<String,Serializable>();
            map.put(KEY, url);
            
            return map;            
        }
        
        return null;
    }    
}