package net.refractions.udig.catalog.internal.wmt.wmtsource;

public class OSMOsmarenderSource extends OSMSource {
    public static String NAME = "Osmarender"; //$NON-NLS-1$
    
    public OSMOsmarenderSource() {
        System.out.println("OSMOsmarenderSource");
        setName(NAME); 
    }

    @Override
    public String getBaseUrl() {
        return null;
    }

}
