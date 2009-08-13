package net.refractions.udig.catalog.internal.wmt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSourceFactory;
import net.refractions.udig.core.internal.CorePlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;


public class WMTGeoResource extends IGeoResource {
    
    public static final String DEFAULT_ID = "blank"; //$NON-NLS-1$

    private WMTService wmtService;
    private String resourceId;
    
    private WMTSource source;
    
    private Throwable msg;
    
    public WMTGeoResource(WMTService service, String resourceId) {
        System.out.println("WMTGeoResource");
        this.service = service;
        this.wmtService = service;
        
        if (resourceId.equals(WMTGeoResource.DEFAULT_ID)) {
            // is this a OSMCloudMadeSource?
            String styleId = getStyleIdFromUrl(wmtService.getIdentifier());
            
            // if yes, let's use the style-id as resource-id
            if(styleId != null) {
                this.resourceId = styleId;
            } else {
                this.resourceId = resourceId; 
            }
        } else {
            this.resourceId = resourceId;            
        }
        
        this.source = null;
    }
    
    /**
     * Gets the style-id from an url:
     * wmt://localhost/wmt/net.refractions.udig.catalog.internal.wmt.wmtsource.OSMCloudMadeSource/3
     *  -->
     *  3 
     *
     * @param url
     * @return
     */
    private String getStyleIdFromUrl(URL url) {
        String className = WMTSourceFactory.getClassFromUrl(url);
        String styleId = url.toString().replace(WMTService.ID, "").replace(className, "");  //$NON-NLS-1$ //$NON-NLS-2$ 
        
        if (!styleId.isEmpty()) {
            return styleId.replace("/", "");//$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return null;
        }
    }

    /**
     * Returns the WMTSource object connected to this GeoResource
     *
     * @return
     */
    public WMTSource getSource(){
        if (source == null) {
            synchronized (this) {
                if (source == null) {
                    try {                        
                        source = WMTSourceFactory.createSource(wmtService, 
                                wmtService.getIdentifier(), resourceId);       
                    } catch(Throwable t) {
                        source = null;
                        msg = t;
                    }
                }
            }
        }
        
        return source;
    }
    
    public String getTitle() {
        return getSource().getName();   
    }
    
    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public <T> boolean canResolve( Class<T> adaptee ) {
        return adaptee != null
                && (adaptee.isAssignableFrom(WMTSource.class)
                        || super.canResolve(adaptee));
    }

    /*
     * @see net.refractions.udig.catalog.IGeoResource#resolve(java.lang.Class,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (monitor == null)
            monitor = new NullProgressMonitor();

        if (adaptee == null) {
            throw new NullPointerException("No adaptor specified" ); //$NON-NLS-1$
        }        
        if (adaptee.isAssignableFrom(WMTSource.class)) {
            return adaptee.cast(getSource());
        }

        return super.resolve(adaptee, monitor);
    }


    /*
     * @see net.refractions.udig.catalog.IGeoResourceInfo#createInfo(java.lang.Class,
     *      org.eclipse.core.runtime.IProgressMonitor
     */
    protected IGeoResourceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        if (info == null){
            synchronized (this) {
                if (info == null){
                    info = new WMTGeoResourceInfo(this, monitor);
                }
            }
        }
        return info;
    }


    /*
     * @see net.refractions.udig.catalog.IResolve#getStatus()
     */
    public Status getStatus() {
        if (msg != null) {
            return Status.BROKEN;
        } else if (source == null){
            return Status.NOTCONNECTED;
        } else {
            return Status.CONNECTED;
        }
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return msg;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getIdentifier()
     */
    public URL getIdentifier() {
        try {
            return new URL(null,
                    service.getIdentifier().toString() + "#" + resourceId, CorePlugin.RELAXED_HANDLER); //$NON-NLS-1$
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return service.getIdentifier();
    }

}
