package net.refractions.udig.tests.catalog.wmt;

import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMMapnikSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMOsmarenderSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
import junit.framework.TestCase;

public class OSMSourceTest extends TestCase{

    public void testZoomLevelMapping() {
        OSMSource source = new OSMMapnikSource();
        
        assertEquals(18, source.getZoomLevelFromMapScale(0));
        assertEquals(18, source.getZoomLevelFromMapScale(1600));
        
        assertEquals(17, source.getZoomLevelFromMapScale(2000));
        
        assertEquals(3, source.getZoomLevelFromMapScale(30000000));

        assertEquals(2, source.getZoomLevelFromMapScale(130000000));   
        
        // Osmarender Tests
        OSMSource sourceOsmarender = new OSMOsmarenderSource();
        assertEquals(17, sourceOsmarender.getZoomLevelFromMapScale(1000));
        assertEquals(2, sourceOsmarender.getZoomLevelFromMapScale(130000000));  
    }
    
    
}
