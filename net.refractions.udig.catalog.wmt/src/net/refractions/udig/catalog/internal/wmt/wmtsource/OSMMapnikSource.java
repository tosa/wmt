package net.refractions.udig.catalog.internal.wmt.wmtsource;

public class OSMMapnikSource extends OSMSource {
    public OSMMapnikSource() {
        System.out.println("OSMMapnikSource");
        setName("Mapnik"); //$NON-NLS-1$
    }

    @Override
    public String getBaseUrl() {
        return "mapnik";
    }
    

}
