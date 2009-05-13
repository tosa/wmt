package net.refractions.udig.catalog.internal.wmt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;

import net.refractions.udig.core.internal.CorePlugin;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;


public class WMTGeoResource extends IGeoResource {

    private WMTSource source;
    private WMTService wmtService;
    
    public WMTGeoResource( WMTService service ) {
        System.out.println("WMTGeoResource");
        this.service = service;
        this.wmtService = service;
        
        source = service.getSource();
    }

    public WMTSource getSource(){
        return source;
    }
    
    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public <T> boolean canResolve( Class<T> adaptee ) {
        System.out.println("WMTGeoResource.canresolve");
        return adaptee != null
                && (adaptee.isAssignableFrom(WMTSource.class)
                        || super.canResolve(adaptee));
    }

    /*
     * @see net.refractions.udig.catalog.IGeoResource#resolve(java.lang.Class,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {

        System.out.println("WMTGeoResource.resolve");
        if (monitor == null)
            monitor = new NullProgressMonitor();

        if (adaptee == null) {
            throw new NullPointerException("No adaptor specified" ); //$NON-NLS-1$
        }        
        if (adaptee.isAssignableFrom(WMTSource.class)) {
            return adaptee.cast(wmtService.getSource());
        }

        return super.resolve(adaptee, monitor);
    }


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
        return service.getStatus();
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return service.getMessage();
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getIdentifier()
     */
    public URL getIdentifier() {
        try {
            return new URL(null,
                    service.getIdentifier().toString() + "#WMTGeoResource" , CorePlugin.RELAXED_HANDLER); //$NON-NLS-1$
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return service.getIdentifier();
    }

}
