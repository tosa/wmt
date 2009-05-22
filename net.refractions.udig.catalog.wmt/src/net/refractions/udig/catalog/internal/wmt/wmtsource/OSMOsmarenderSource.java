package net.refractions.udig.catalog.internal.wmt.wmtsource;

public class OSMOsmarenderSource extends OSMSource {
    public static String NAME = "Osmarender"; //$NON-NLS-1$
    
    public OSMOsmarenderSource() {
        System.out.println(NAME);
        setName(NAME); 
    }

    @Override
    public String getBaseUrl() {
        return "http://tah.openstreetmap.org/Tiles/tile/"; //$NON-NLS-1$
    }

}
