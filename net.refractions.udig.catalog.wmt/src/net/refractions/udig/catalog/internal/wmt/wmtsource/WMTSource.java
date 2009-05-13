package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.net.MalformedURLException;
import java.net.URL;
import net.refractions.udig.catalog.internal.wmt.WMTService;
import net.refractions.udig.core.internal.CorePlugin;

public abstract class WMTSource {
    private String name;
    
    public WMTSource() {
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public static URL getRelatedServiceUrl(Class<? extends WMTSource> sourceClass)
    {
        URL url;
        
        try {
            url = new URL(null, WMTService.SERVICE_URL.toString() + sourceClass.getName(), CorePlugin.RELAXED_HANDLER);
        }
        catch(MalformedURLException exc) {
            url = null;
        }        
        
        return url; 
    }

    @Override
    public String toString() {
        return getName();
    }
    
    

}
