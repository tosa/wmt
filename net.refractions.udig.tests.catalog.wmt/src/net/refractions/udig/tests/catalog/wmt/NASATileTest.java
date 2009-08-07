package net.refractions.udig.tests.catalog.wmt;

import junit.framework.TestCase;
import net.refractions.udig.catalog.internal.wmt.tile.NASATile;
import net.refractions.udig.catalog.internal.wmt.tile.NASATile.NASATileName;
import net.refractions.udig.catalog.internal.wmt.tile.NASATile.NASATileName.NASAZoomLevel;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile.WMTTileFactory;
import net.refractions.udig.catalog.internal.wmt.wmtsource.NASASource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSourceFactory;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;


public class NASATileTest extends TestCase {
    
    private NASASource source;
    private NASASource sourceUSA;
    
    @Before
    public void setUp() throws Exception {
        
        String resourceId1 = "Global Mosaic, pan sharpened visual";        
        source = (NASASource) WMTSourceFactory.createSource(null, WMTSource.getRelatedServiceUrl(NASASource.class), resourceId1, true);
        
        String resourceId2 =  "Continental US Elevation";        
        sourceUSA = (NASASource) WMTSourceFactory.createSource(null, WMTSource.getRelatedServiceUrl(NASASource.class), resourceId2, true); 
    }

    @Test
    public void testZoomLevel() {
        
        NASAZoomLevel zoomLevel = source.getZoomLevel(1);


        ReferencedEnvelope boundsFirstTile = new ReferencedEnvelope(-180, -52, -38, 90, DefaultGeographicCRS.WGS84);
        assertEquals("http://wms.jpl.nasa.gov/wms.cgi?request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,-38,-52,90", 
                zoomLevel.getTileUrl(boundsFirstTile));
        
        ReferencedEnvelope bounds2ndTile = new ReferencedEnvelope(-52, 76, -38, 90, DefaultGeographicCRS.WGS84);
        assertEquals("http://wms.jpl.nasa.gov/wms.cgi?request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-52,-38,76,90", 
                zoomLevel.getTileUrl(bounds2ndTile));
        

        ReferencedEnvelope bounds3rdTile = new ReferencedEnvelope(-52, 76, -166, -38, DefaultGeographicCRS.WGS84);
        assertEquals("http://wms.jpl.nasa.gov/wms.cgi?request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-52,-166,76,-38", 
                zoomLevel.getTileUrl(bounds3rdTile));
        
        assertEquals(3, zoomLevel.calculateMaxTilePerRowNumber(0));
    }
    
    public void testGetTileFromCoordinate() {
        
        NASAZoomLevel zoomLevel = source.getZoomLevel(1);
        WMTTileFactory tileFactory = source.getTileFactory();
        
        NASATile tile = (NASATile) tileFactory.getTileFromCoordinate(90, -180, zoomLevel, source);        
        assertEquals("Global Mosaic, pan sharpened visual_1_0_0", tile.getId());
        
        NASATile tile2 = (NASATile) tileFactory.getTileFromCoordinate(-90, -180, zoomLevel, source);        
        assertEquals("Global Mosaic, pan sharpened visual_1_0_1", tile2.getId());
        
        NASATile tile3 = (NASATile) tileFactory.getTileFromCoordinate(-90, -51, zoomLevel, source);        
        assertEquals("Global Mosaic, pan sharpened visual_1_1_1", tile3.getId());
    }
    
    public void testGetTileFromCoordinateUSA() {
        
        NASAZoomLevel zoomLevel = sourceUSA.getZoomLevel(3);
        WMTTileFactory tileFactory = sourceUSA.getTileFactory();
        
        NASATile tile = (NASATile) tileFactory.getTileFromCoordinate(50, -125, zoomLevel, sourceUSA);        
        assertEquals("Continental US Elevation_3_0_0", tile.getId());
        
        NASATile tile2 = (NASATile) tileFactory.getTileFromCoordinate(39, -125, zoomLevel, sourceUSA);        
        assertEquals("Continental US Elevation_3_0_1", tile2.getId());
        
        NASATile tile3 = (NASATile) tileFactory.getTileFromCoordinate(39, -114, zoomLevel, sourceUSA);        
        assertEquals("Continental US Elevation_3_1_1", tile3.getId());
    }
    
    public void testGetExtentFromTileName() {
        NASAZoomLevel zoomLevel = source.getZoomLevel(1);
        
        NASATileName tileName1 = new NASATileName(0, 0, zoomLevel, source);
        
        assertEquals("ReferencedEnvelope[-180.0 : -52.0, -38.0 : 90.0]", 
                NASATile.getExtentFromTileName(tileName1).toString());
        
        assertEquals("ReferencedEnvelope[-180.0 : -52.0, -166.0 : -38.0]", 
                NASATile.getExtentFromTileName(tileName1.getLowerNeighbour()).toString());
        
        assertEquals("ReferencedEnvelope[-52.0 : 76.0, -38.0 : 90.0]", 
                NASATile.getExtentFromTileName(tileName1.getRightNeighbour()).toString());
    }
    
    public void testGetExtentFromTileNameUSA() {
        NASAZoomLevel zoomLevel = sourceUSA.getZoomLevel(3);
        
        NASATileName tileName1 = new NASATileName(0, 0, zoomLevel, sourceUSA);
        
        equals(NASATile.getExtentFromTileName(tileName1), 
                -125.0, -114.33333333, 39.33333333, 50.0); 

        equals(NASATile.getExtentFromTileName(tileName1.getLowerNeighbour()), 
                -125.0, -114.33333333, 28.66666666, 39.33333333); 

        equals(NASATile.getExtentFromTileName(tileName1.getRightNeighbour()), 
                -114.33333333,-103.66666666, 39.33333333, 50.0);         
    }
    
    private void equals(ReferencedEnvelope env, double xmin, double xmax, double ymin, double ymax) {
        double delta = 0.0000001;
        
        assertEquals(xmax, env.getMaxX(), delta);
        assertEquals(xmin, env.getMinX(), delta);
        assertEquals(ymax, env.getMaxY(), delta);
        assertEquals(ymin, env.getMinY(), delta);
    }
    
}
