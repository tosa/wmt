package net.refractions.udig.catalog.internal.wmt.wmtsource;

public abstract class OSMSource extends WMTSource {

    public OSMSource() {
        System.out.println("OSMSource");
        setName("OSMSource"); //$NON-NLS-1$
    }
    
    public abstract String getBaseUrl();

}
