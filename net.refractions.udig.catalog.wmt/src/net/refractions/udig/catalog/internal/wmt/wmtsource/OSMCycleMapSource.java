package net.refractions.udig.catalog.internal.wmt.wmtsource;

public class OSMCycleMapSource extends OSMSource {
    public OSMCycleMapSource() {
        System.out.println("OSMCycleMapSource");
        setName("Cycle Map"); //$NON-NLS-1$
    }

    @Override
    public String getBaseUrl() {
        return null;
    }

}
