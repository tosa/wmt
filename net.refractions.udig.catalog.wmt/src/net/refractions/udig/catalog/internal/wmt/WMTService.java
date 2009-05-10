package net.refractions.udig.catalog.internal.wmt;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.ITransientResolve;
import net.refractions.udig.core.internal.CorePlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.wmt.internal.Messages;

/**
 * Service implementation for map graphics
 */
public class WMTService extends IService {
    public static String ID = "wmt://localhost/wmt/"; //$NON-NLS-1$
        
    /** Dummy url for a MapGraphic */
    public final static URL SERVICE_URL;
    static {
        URL tmp;
        try {
            tmp = new URL(null, ID , CorePlugin.RELAXED_HANDLER);
        } catch (MalformedURLException e) {
            tmp=null;
            e.printStackTrace();
        }
        System.out.println("Static-Block: Service_Url set to " + tmp);
        SERVICE_URL=tmp;
    }

    /** MapGraphic resource children * */
    private volatile List<IGeoResource> members;
    
    private Map<String, Serializable> params;
    private URL url;
    
    /* Class which represents the WMT service */
    private WMTSource source;
    
    private Throwable msg;
    
    /**
     * Construct <code>MapGraphicService</code>.
     */
    public WMTService(Map<String, Serializable> params) {
        System.out.println("WMTService");
        if (params != null)
        {
            this.params = params;
            this.url = (URL) params.get(WMTServiceExtension.KEY);
        }
    }

    public WMTSource getSource(){
        if (source == null) {
            synchronized (this) {
                if (source == null) {
                    try {
                        /*
                         * Strip out the start of the url:
                         * 
                         * wmt:///localhost/wmt/net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource
                         * -->
                         * net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource
                         */
                        String className = url.toString().replace(ID, ""); //$NON-NLS-1$
                        source = (WMTSource) Class.forName(className).newInstance();
                    } catch(Throwable t) {
                        msg = t;
                        source = null;
                    }
                    
                }
            }
        }
        return source;
    }
    
    
    /*
     * @see net.refractions.udig.catalog.IService#resolve(java.lang.Class,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        System.out.println("WMTService.resolve");
        
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
    
    @Override
	protected
    synchronized IServiceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        if (info == null){
            synchronized (this) {
                if (info == null){
                    info = new WMTServiceInfo(this, monitor);
                }
            }
        }
        return info;
    }
    /*
     * @see net.refractions.udig.catalog.IService#members(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public List<IGeoResource> resources( IProgressMonitor monitor ) throws IOException {

        if (members == null) {
            synchronized (this) {
                if (members == null) {
                    members = Collections.singletonList((IGeoResource) new WMTGeoResource(this));
                    
                }
            }
        }
        

        return members;
    }

    /*
     * @see net.refractions.udig.catalog.IService#getConnectionParams()
     */
    @Override
    public Map<String, Serializable> getConnectionParams() {
        return params;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public <T> boolean canResolve( Class<T> adaptee ) {
        System.out.println("WMTService.canResolve");
        
        return adaptee != null
                && (adaptee.isAssignableFrom(WMTSource.class)
                        || super.canResolve(adaptee));
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getStatus()
     */
    public Status getStatus() {
        if (msg == null)
        {
            return Status.CONNECTED;
        } else {
            return Status.BROKEN;
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
        return url;
    }

}