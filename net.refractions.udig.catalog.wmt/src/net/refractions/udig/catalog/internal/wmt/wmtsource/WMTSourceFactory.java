package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import net.refractions.udig.catalog.internal.wmt.WMTService;

public class WMTSourceFactory {
    
    // todo: make every WMTSource class singleton, so that the cache is reused!
    public static WMTSource createSource(WMTService service, URL url, 
            Map<String, Serializable> params) throws Throwable {
        WMTSource source;
                
        /*
         * Strip out the start of the url:
         * 
         * wmt:///localhost/wmt/net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource
         * -->
         * net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource
         */
        String className = url.toString().replace(WMTService.ID, ""); //$NON-NLS-1$
        source = (WMTSource) Class.forName(className).newInstance();
        
        source.init(params);
        source.setWmtService(service);

        return source;
    }
    
    public static WMTSource createSource(WMTService service, URL url, 
            Map<String, Serializable> params, boolean noException) {
        WMTSource source;
        
        try{
            source = createSource(service, url, params);
        } catch (Throwable exc) {
            source = null;
        }
        
        return source;
    }
}
