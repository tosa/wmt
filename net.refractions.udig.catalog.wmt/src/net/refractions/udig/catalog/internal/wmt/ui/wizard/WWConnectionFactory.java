package net.refractions.udig.catalog.internal.wmt.ui.wizard;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.wmt.wmtsource.ww.LayerSet;
import net.refractions.udig.catalog.internal.wmt.ww.WWGeoResource;
import net.refractions.udig.catalog.internal.wmt.ww.WWService;
import net.refractions.udig.catalog.internal.wmt.ww.WWServiceExtension;
import net.refractions.udig.catalog.ui.UDIGConnectionFactory;

import org.eclipse.core.runtime.NullProgressMonitor;

public class WWConnectionFactory extends UDIGConnectionFactory {

	public boolean canProcess(Object context) {
		if( context instanceof IResolve ){
           IResolve resolve = (IResolve) context;
           return resolve.canResolve( LayerSet.class );
       }
       return toCapabilitiesURL(context) != null;        
	}
	
	public Map<String, Serializable> createConnectionParameters(Object context) {
		  if( context instanceof IResolve  ){
	            Map params = createParams( (IResolve) context );
	            if( !params.isEmpty() ) return params;            
	        } 
	        URL url = toCapabilitiesURL( context );
	        if( url == null ){
	            // so we are not sure it is a wms url
	            // lets guess
	            url = CatalogPlugin.locateURL(context);
	        }
	        if( url != null ) {
	            // well we have a url - lets try it!            
	            List<IResolve> list = CatalogPlugin.getDefault().getLocalCatalog().find( url, null );
	            for( IResolve resolve : list ){
	                Map params = createParams( resolve );
	                if( !params.isEmpty() ) return params; // we got the goods!
	            }
	            return createParams( url );            
	        }        
	        return Collections.EMPTY_MAP;
	}

	static public Map<String,Serializable> createParams( IResolve handle ){
        if( handle instanceof WWService) {
            // got a hit!
            WWService service = (WWService) handle;
            return service.getConnectionParams();
        }
        else if (handle instanceof WWGeoResource ){
            WWGeoResource geoResource = (WWGeoResource) handle;
            WWService service;
            try {
                service = (WWService) geoResource.service( new NullProgressMonitor());
                return service.getConnectionParams();
            } catch (IOException e) {
                checkedURL( geoResource.getIdentifier() );
            }                    
        }
        else if( handle.canResolve( LayerSet.class )){
            // must be some kind of handle from a search!
            return createParams( handle.getIdentifier() );
        }
        return Collections.EMPTY_MAP;
    }
	
	/** 'Create' params given the provided url, no magic occurs */
    static public Map<String,Serializable> createParams( URL url ){
        WWServiceExtension factory = new WWServiceExtension();
        Map params = factory.createParams( url );
        if( params != null) return params;
        
        Map<String,Serializable> params2 = new HashMap<String,Serializable>();
        params2.put(WWService.WW_URL_KEY,url);
        return params2;
    }

    
	 /**
     * Convert "data" to a wms capabilities url
     * <p>
     * Candidates for conversion are:
     * <ul>
     * <li>URL - from browser DnD
     * <li>URL#layer - from browser DnD
     * <li>WMSService - from catalog DnD
     * <li>WMSGeoResource - from catalog DnD
     * <li>IService - from search DnD
     * </ul>
     * </p>
     * <p>
     * No external processing should be required here, it is enough to guess and let
     * the ServiceFactory try a real connect.
     * </p>
     * @param data IService, URL, or something else
     * @return URL considered a possibility for a WMS Capabilities, or null
     */
    static URL toCapabilitiesURL( Object data ) {
        if( data instanceof IResolve ){
            return toCapabilitiesURL( (IResolve) data );
        }
        else if( data instanceof URL ){
            return toCapabilitiesURL( (URL) data );
        }
        else if( CatalogPlugin.locateURL(data) != null ){
            return toCapabilitiesURL( CatalogPlugin.locateURL(data) );
        }
        else {
            return null; // no idea what this should be
        }
    }

    static URL toCapabilitiesURL( IResolve resolve ){
        if( resolve instanceof IService ){
            return toCapabilitiesURL( (IService) resolve );
        }
        return toCapabilitiesURL( resolve.getIdentifier() );        
    }

    static URL toCapabilitiesURL( IService resolve ){
        if( resolve instanceof WWService ){
            return toCapabilitiesURL( (WWService) resolve );
        }
        return toCapabilitiesURL( resolve.getIdentifier() );        
    }

    /** No further QA checks needed - we know this one works */
    static URL toCapabilitiesURL( WWService service ){
        return service.getIdentifier();                
    }

    /** Quick sanity check to see if url is a WMS url */
    static URL toCapabilitiesURL( URL url ){
        if (url == null) return null;
    
        String path = url.getPath() == null ? null : url.getPath().toLowerCase();
        String query = url.getQuery() == null ? null : url.getQuery().toLowerCase();
        String protocol = url.getProtocol() == null ? null : url.getProtocol().toLowerCase();
    
        if (!"http".equals(protocol) //$NON-NLS-1$
                && !"https".equals(protocol)) { //$NON-NLS-1$ 
            return null;
        }
        return null;
    }
    
    /** Check that any trailing #layer is removed from the url */
    static public URL checkedURL( URL url ){
        String check = url.toExternalForm();

        int hash = check.indexOf('#');
        if ( hash == -1 ){
            return url;            
        }
        try {
            return new URL( check.substring(0, hash ));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
	public URL createConnectionURL(Object context) {
	    if( context instanceof URL ){
	        return (URL) context;
	    }
		return null;
	}

}
