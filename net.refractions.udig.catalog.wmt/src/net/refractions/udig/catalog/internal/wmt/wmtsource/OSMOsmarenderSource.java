package net.refractions.udig.catalog.internal.wmt.wmtsource;

public class OSMOsmarenderSource extends OSMSource {
    public OSMOsmarenderSource() {
        System.out.println("OSMOsmarenderSource");
        setName("Osmarender"); //$NON-NLS-1$
    }

    @Override
    public String getBaseUrl() {
        return null;
    }

}
