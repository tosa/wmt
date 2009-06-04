package net.refractions.udig.tests.catalog.wmt;

import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMMapnikSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMOsmarenderSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
import junit.framework.TestCase;

public class OSMSourceTest extends TestCase{

    public void testZoomLevelMapping() {
        OSMSource source = new OSMMapnikSource();

        System.out.println(source.getZoomLevelFromMapScale(1700, 50));
        
        // Tests for scale-factor = 50
        int scaleFactorMiddle = 50;
           
        assertEquals(18, source.getZoomLevelFromMapScale(0, scaleFactorMiddle));
        assertEquals(18, source.getZoomLevelFromMapScale(1600, scaleFactorMiddle));
        assertEquals(18, source.getZoomLevelFromMapScale(1700, scaleFactorMiddle));

        assertEquals(17, source.getZoomLevelFromMapScale(3000, scaleFactorMiddle));        
        assertEquals(17, source.getZoomLevelFromMapScale(3400, scaleFactorMiddle));
        
        assertEquals(16, source.getZoomLevelFromMapScale(6000, scaleFactorMiddle));
        
        assertEquals(3, source.getZoomLevelFromMapScale(60000000, scaleFactorMiddle));

        assertEquals(2, source.getZoomLevelFromMapScale(100000000, scaleFactorMiddle)); 
        assertEquals(2, source.getZoomLevelFromMapScale(120000000, scaleFactorMiddle));   
        
        // Osmarender Tests
        OSMSource sourceOsmarender = new OSMOsmarenderSource();
        assertEquals(17, sourceOsmarender.getZoomLevelFromMapScale(1000, scaleFactorMiddle));
        assertEquals(2, sourceOsmarender.getZoomLevelFromMapScale(130000000, scaleFactorMiddle));  
        
        
        // Tests for scale-factor = 0 (always scale up tiles)
        int scaleFactorUp = 0;
        
        assertEquals(18, source.getZoomLevelFromMapScale(0, scaleFactorUp));
        assertEquals(18, source.getZoomLevelFromMapScale(1600, scaleFactorUp));
        assertEquals(17, source.getZoomLevelFromMapScale(1700, scaleFactorUp));

        assertEquals(17, source.getZoomLevelFromMapScale(3000, scaleFactorUp));        
        assertEquals(16, source.getZoomLevelFromMapScale(3400, scaleFactorUp));
        
        assertEquals(16, source.getZoomLevelFromMapScale(6000, scaleFactorUp));
        
        assertEquals(2, source.getZoomLevelFromMapScale(60000000, scaleFactorUp));

        assertEquals(2, source.getZoomLevelFromMapScale(100000000, scaleFactorUp)); 
        assertEquals(2, source.getZoomLevelFromMapScale(120000000, scaleFactorUp));   
        
        // Osmarender Tests
        assertEquals(17, sourceOsmarender.getZoomLevelFromMapScale(1000, scaleFactorUp));
        assertEquals(2, sourceOsmarender.getZoomLevelFromMapScale(130000000, scaleFactorUp));  
        
        
        // Tests for scale-factor = 100 (always scale down tiles)
        int scaleFactorDown = 100;
        
        assertEquals(18, source.getZoomLevelFromMapScale(0, scaleFactorDown));
        assertEquals(18, source.getZoomLevelFromMapScale(1600, scaleFactorDown));
        assertEquals(18, source.getZoomLevelFromMapScale(1700, scaleFactorDown));

        assertEquals(18, source.getZoomLevelFromMapScale(3000, scaleFactorDown));        
        assertEquals(17, source.getZoomLevelFromMapScale(3400, scaleFactorDown));
        
        assertEquals(17, source.getZoomLevelFromMapScale(6000, scaleFactorDown));
        
        assertEquals(3, source.getZoomLevelFromMapScale(60000000, scaleFactorDown));

        assertEquals(3, source.getZoomLevelFromMapScale(100000000, scaleFactorDown)); 
        assertEquals(2, source.getZoomLevelFromMapScale(120000000, scaleFactorDown));   
        
        // Osmarender Tests
        assertEquals(17, sourceOsmarender.getZoomLevelFromMapScale(1000, scaleFactorDown));
        assertEquals(2, sourceOsmarender.getZoomLevelFromMapScale(130000000, scaleFactorDown));  
        
    }
    
    
}
