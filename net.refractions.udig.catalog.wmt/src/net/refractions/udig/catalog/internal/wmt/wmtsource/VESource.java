package net.refractions.udig.catalog.internal.wmt.wmtsource;

public class VESource extends WMTSource {
    public static String NAME = "Microsoft Virtual Earth"; //$NON-NLS-1$
    
    public VESource() {
        System.out.println("VESource");
        setName(NAME);
    }

}
