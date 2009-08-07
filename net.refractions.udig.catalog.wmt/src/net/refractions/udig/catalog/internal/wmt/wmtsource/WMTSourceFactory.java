package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.net.URL;

import net.refractions.udig.catalog.internal.wmt.WMTService;

public class WMTSourceFactory {
    
    // todo: make every WMTSource class singleton, so that the cache is reused!
    public static WMTSource createSource(WMTService service, URL url, 
            String resourceId) throws Throwable {
        WMTSource source;
                
        String className = getClassFromUrl(url);
        source = (WMTSource) Class.forName(className).newInstance();
        
        source.init(resourceId);
        source.setWmtService(service);

        return source;
    }
    
    /**
     * Strip out the start of the url:
     * 
     * wmt:///localhost/wmt/net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource
     *  -->
     *  net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource 
     *
     * @param url
     * @return
     */
    public static String getClassFromUrl(URL url) {
        return url.toString().replace(WMTService.ID, ""); //$NON-NLS-1$
    }
    
    public static WMTSource createSource(WMTService service, URL url, 
            String resourceId, boolean noException) {
        WMTSource source;
        
        try{
            source = createSource(service, url, resourceId);
        } catch (Throwable exc) {
            source = null;
        }
        
        return source;
    }
}
