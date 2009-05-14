package net.refractions.udig.catalog.internal.wmt.wmtsource;

public class OSMCycleMapSource extends OSMSource {
    public static String NAME = "Cycle Map"; //$NON-NLS-1$
    
    public OSMCycleMapSource() {
        System.out.println("OSMCycleMapSource");
        setName(NAME); 
    }

    @Override
    public String getBaseUrl() {
        return null;
    }

}
