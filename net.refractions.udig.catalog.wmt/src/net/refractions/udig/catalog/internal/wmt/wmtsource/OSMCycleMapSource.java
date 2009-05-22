package net.refractions.udig.catalog.internal.wmt.wmtsource;

public class OSMCycleMapSource extends OSMSource {
    public static String NAME = "Cycle Map"; //$NON-NLS-1$
    
    public OSMCycleMapSource() {
        System.out.println(NAME);
        setName(NAME); 
    }

    @Override
    public String getBaseUrl() {
        return "http://andy.sandbox.cloudmade.com/tiles/cycle/"; //$NON-NLS-1$
    }

}
