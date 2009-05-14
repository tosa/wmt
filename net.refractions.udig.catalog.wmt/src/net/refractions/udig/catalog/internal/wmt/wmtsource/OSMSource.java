package net.refractions.udig.catalog.internal.wmt.wmtsource;

public abstract class OSMSource extends WMTSource {
    public static String NAME = "OpenStreetMap"; //$NON-NLS-1$

    public OSMSource() {
        System.out.println("OSMSource");
        setName(NAME); 
    }
    
    public abstract String getBaseUrl();

}
