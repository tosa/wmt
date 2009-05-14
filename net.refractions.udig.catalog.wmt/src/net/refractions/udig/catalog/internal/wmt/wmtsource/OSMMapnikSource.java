package net.refractions.udig.catalog.internal.wmt.wmtsource;

public class OSMMapnikSource extends OSMSource {
    public static String NAME = "Mapnik"; //$NON-NLS-1$
    
    public OSMMapnikSource() {
        System.out.println("OSMMapnikSource");
        setName(NAME); 
    }

    @Override
    public String getBaseUrl() {
        return "mapnik";
    }
    

}
