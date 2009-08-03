package net.refractions.udig.tests.catalog.wmt;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;

import net.refractions.udig.catalog.internal.wmt.tile.NASATile.NASATileName.NASAZoomLevel;
import junit.framework.TestCase;


public class NASATileTest extends TestCase {
    @Test
    public void testZoomLevel() {
        String requestTag = "request=GetMap&layers=global_mosaic&srs=EPSG:4326&width=512&height=512&bbox=-180,-38,-52,90&format=image/jpeg&version=1.1.1&styles=visual";
        ReferencedEnvelope bounds = new ReferencedEnvelope(-180, 180, -90, 90, DefaultGeographicCRS.WGS84);
        
        NASAZoomLevel zoomLevel = new NASAZoomLevel(requestTag, bounds);
        
        ReferencedEnvelope boundsFirstTile = new ReferencedEnvelope(-180, -52, -38, 90, DefaultGeographicCRS.WGS84);
        assertEquals("http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers=global_mosaic&srs=EPSG:4326&width=512&height=512&bbox=-180,-38,-52,90&format=image/jpeg&version=1.1.1&styles=visual", 
                zoomLevel.getTileUrl(boundsFirstTile));
        

        ReferencedEnvelope bounds2ndTile = new ReferencedEnvelope(-52, 76, -38, 90, DefaultGeographicCRS.WGS84);
        assertEquals("http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers=global_mosaic&srs=EPSG:4326&width=512&height=512&bbox=-52,-38,76,90&format=image/jpeg&version=1.1.1&styles=visual", 
                zoomLevel.getTileUrl(bounds2ndTile));
        

        ReferencedEnvelope bounds3rdTile = new ReferencedEnvelope(-52, 76, -166, -38, DefaultGeographicCRS.WGS84);
        assertEquals("http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers=global_mosaic&srs=EPSG:4326&width=512&height=512&bbox=-52,-166,76,-38&format=image/jpeg&version=1.1.1&styles=visual", 
                zoomLevel.getTileUrl(bounds3rdTile));
        
        assertEquals(3, zoomLevel.calculateMaxTilePerRowNumber(0));
    }
    
}
