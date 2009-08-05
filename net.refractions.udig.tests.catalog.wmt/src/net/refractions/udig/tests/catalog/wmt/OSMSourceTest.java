package net.refractions.udig.tests.catalog.wmt;

import java.util.Map;

import junit.framework.TestCase;
import net.refractions.udig.catalog.internal.wmt.ui.properties.WMTLayerProperties;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMMapnikSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMOsmarenderSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSourceFactory;
import net.refractions.udig.catalog.wmsc.server.Tile;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

public class OSMSourceTest extends TestCase{
    
    private OSMSource source = (OSMMapnikSource) WMTSourceFactory.createSource(null, WMTSource.getRelatedServiceUrl(OSMMapnikSource.class), null, true);
    private OSMSource sourceOsmarender = (OSMOsmarenderSource) WMTSourceFactory.createSource(null, WMTSource.getRelatedServiceUrl(OSMOsmarenderSource.class), null, true);
        
    public void testZoomLevelMappingScaleFactor50() {        
        // Tests for scale-factor = 50
        int scaleFactorMiddle = 50;
           
        assertEquals(18, source.getZoomLevelFromMapScale(0, scaleFactorMiddle));
        assertEquals(18, source.getZoomLevelFromMapScale(2250, scaleFactorMiddle));
        assertEquals(18, source.getZoomLevelFromMapScale(2260, scaleFactorMiddle));

        assertEquals(17, source.getZoomLevelFromMapScale(4500, scaleFactorMiddle));        
        assertEquals(17, source.getZoomLevelFromMapScale(4550, scaleFactorMiddle));
        
        assertEquals(16, source.getZoomLevelFromMapScale(9000, scaleFactorMiddle));
        
        assertEquals(3, source.getZoomLevelFromMapScale(73000000, scaleFactorMiddle));

        assertEquals(2, source.getZoomLevelFromMapScale(120000000, scaleFactorMiddle)); 
        assertEquals(2, source.getZoomLevelFromMapScale(150000000, scaleFactorMiddle));   
        
        // Osmarender Tests
        assertEquals(17, sourceOsmarender.getZoomLevelFromMapScale(1000, scaleFactorMiddle));
        assertEquals(2, sourceOsmarender.getZoomLevelFromMapScale(150000000, scaleFactorMiddle));  
    }
    
    public void testZoomLevelMappingScaleFactor0() {    
        // Tests for scale-factor = 0 (always scale up tiles)
        int scaleFactorUp = 0;
        
        assertEquals(18, source.getZoomLevelFromMapScale(0, scaleFactorUp));
        assertEquals(18, source.getZoomLevelFromMapScale(2250, scaleFactorUp));
        assertEquals(17, source.getZoomLevelFromMapScale(2260, scaleFactorUp));

        assertEquals(17, source.getZoomLevelFromMapScale(4500, scaleFactorUp));        
        assertEquals(16, source.getZoomLevelFromMapScale(4550, scaleFactorUp));
        
        assertEquals(16, source.getZoomLevelFromMapScale(8000, scaleFactorUp));
        
        assertEquals(2, source.getZoomLevelFromMapScale(80000000, scaleFactorUp));

        assertEquals(2, source.getZoomLevelFromMapScale(100000000, scaleFactorUp)); 
        assertEquals(2, source.getZoomLevelFromMapScale(120000000, scaleFactorUp));   
        
        // Osmarender Tests
        assertEquals(17, sourceOsmarender.getZoomLevelFromMapScale(1000, scaleFactorUp));
        assertEquals(2, sourceOsmarender.getZoomLevelFromMapScale(130000000, scaleFactorUp));   
    }
    
    public void testZoomLevelMappingScaleFactor100() {
        
        // Tests for scale-factor = 100 (always scale down tiles)
        int scaleFactorDown = 100;
        
        assertEquals(18, source.getZoomLevelFromMapScale(0, scaleFactorDown));
        assertEquals(18, source.getZoomLevelFromMapScale(2550, scaleFactorDown));
        assertEquals(18, source.getZoomLevelFromMapScale(2560, scaleFactorDown));

        assertEquals(18, source.getZoomLevelFromMapScale(4500, scaleFactorDown));        
        assertEquals(17, source.getZoomLevelFromMapScale(4550, scaleFactorDown));
        
        assertEquals(17, source.getZoomLevelFromMapScale(6000, scaleFactorDown));
        
        assertEquals(3, source.getZoomLevelFromMapScale(74000000, scaleFactorDown));

        assertEquals(3, source.getZoomLevelFromMapScale(140000000, scaleFactorDown)); 
        assertEquals(2, source.getZoomLevelFromMapScale(150000000, scaleFactorDown));   
        
        // Osmarender Tests
        assertEquals(17, sourceOsmarender.getZoomLevelFromMapScale(1000, scaleFactorDown));
        assertEquals(2, sourceOsmarender.getZoomLevelFromMapScale(150000000, scaleFactorDown));  
        
    }
    
    public void testCutExtentInTiles() {
        WMTLayerProperties layerProp = new WMTLayerProperties(null);
        
        ReferencedEnvelope env = new ReferencedEnvelope(-200, 220, -90, 95, DefaultGeographicCRS.WGS84);
        
        Map<String, Tile> tiles = source.cutExtentIntoTiles(env, 150000000, 50, true, layerProp);
        
        assertEquals(16, tiles.size());
    }
    
}
