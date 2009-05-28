package net.refractions.udig.tests.catalog.wmt;

import org.geotools.geometry.jts.ReferencedEnvelope;

import net.refractions.udig.catalog.internal.wmt.tile.OSMTile;
import net.refractions.udig.catalog.internal.wmt.tile.OSMTile.OSMTileName;
import net.refractions.udig.catalog.internal.wmt.tile.OSMTile.OSMTileName.OSMZoomLevel;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMMapnikSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
import junit.framework.TestCase;


public class OSMTileTest extends TestCase{

    public void testGetTileFromCoordinate() {
        OSMSource osmSource = new OSMMapnikSource();
        
        OSMTile tile = OSMTile.getTileFromCoordinate(49.38052, 6.55268, new OSMZoomLevel(6), osmSource);
        
        assertEquals("http://tile.openstreetmap.org/6/33/21.png", tile.getUrl().toString()); //$NON-NLS-1$
    }
    
    public void testGetTileFromCoordinateLatitude() {
        OSMSource osmSource = new OSMMapnikSource();
        
        OSMTile tile = OSMTile.getTileFromCoordinate(85.33499182341461, -103.68507972493057, new OSMZoomLevel(0), osmSource);
        
        assertEquals("http://tile.openstreetmap.org/0/0/0.png", tile.getUrl().toString()); //$NON-NLS-1$
    }
    
    public void testGetExtentFromTileName() {
        OSMTileName tileName = new OSMTileName(33, 21, new OSMZoomLevel(6));
        
        ReferencedEnvelope extent = OSMTile.getExtentFromTileName(tileName);
        
        assertEquals(5.625, extent.getMinX(), 0.01);
        assertEquals(11.25, extent.getMaxX(), 0.01);
        assertEquals(48.92249, extent.getMinY(), 0.01);
        assertEquals(52.48278, extent.getMaxY(), 0.01);
        
        System.out.println("Min-X: " + extent.getMinX()); //$NON-NLS-1$
        System.out.println("Max-X: " + extent.getMaxX()); //$NON-NLS-1$
        System.out.println("Min-Y: " + extent.getMinY()); //$NON-NLS-1$
        System.out.println("Max-Y: " + extent.getMaxY()); //$NON-NLS-1$        
    }
    
    public void testNeighbourCalculation() {
        OSMZoomLevel zoomLevel = new OSMZoomLevel(1);        
        OSMTileName tileName = new OSMTileName(1, 0, zoomLevel);
        
        assertEquals(new OSMTileName(0, 0, zoomLevel), tileName.getRightNeighbour());  
        assertEquals(new OSMTileName(1, 1, zoomLevel), tileName.getUpperNeighbour());     
    }
    
    public void testNeighbourCalculation2() {
        OSMZoomLevel zoomLevel = new OSMZoomLevel(2);        
        OSMTileName tileName = new OSMTileName(2, 2, zoomLevel);
        
        assertEquals(new OSMTileName(3, 2, zoomLevel), tileName.getRightNeighbour());     
    }
    
    public void testGetExtentFromTileName2() {
        // actually not a test, just printing out the extent of the whole map
        OSMTileName tileName = new OSMTileName(0, 0, new OSMZoomLevel(0));
        ReferencedEnvelope extent = OSMTile.getExtentFromTileName(tileName);
        
        System.out.println("Min-X: " + extent.getMinX()); //$NON-NLS-1$
        System.out.println("Max-X: " + extent.getMaxX()); //$NON-NLS-1$
        System.out.println("Min-Y: " + extent.getMinY()); //$NON-NLS-1$
        System.out.println("Max-Y: " + extent.getMaxY()); //$NON-NLS-1$     
        
        assertEquals(true, true);
    }
}
