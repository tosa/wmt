package net.refractions.udig.tests.catalog.wmt;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import net.refractions.udig.catalog.internal.wmt.tile.OSMTile;
import net.refractions.udig.catalog.internal.wmt.tile.OSMTile.OSMTileName;
import net.refractions.udig.catalog.internal.wmt.tile.OSMTile.OSMTileName.OSMZoomLevel;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMMapnikSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
import junit.framework.TestCase;


public class OSMTileTest extends TestCase{

    public void testGetTileFromCoordinate() {
        OSMSource osmSource = new OSMMapnikSource();
        
        OSMTile tile = (OSMTile) osmSource.getTileFactory().getTileFromCoordinate(49.38052, 6.55268, new OSMZoomLevel(6), osmSource);
        
        assertEquals("http://tile.openstreetmap.org/6/33/21.png", tile.getUrl().toString()); //$NON-NLS-1$
    }
    
    public void testGetTileFromCoordinateLatitude() {
        OSMSource osmSource = new OSMMapnikSource();
        
        OSMTile tile = (OSMTile) osmSource.getTileFactory().getTileFromCoordinate(85.33499182341461, -103.68507972493057, new OSMZoomLevel(0), osmSource);
        
        assertEquals("http://tile.openstreetmap.org/0/0/0.png", tile.getUrl().toString()); //$NON-NLS-1$
    }
    
    public void testGetExtentFromTileName() {
        OSMTileName tileName = new OSMTileName(33, 21, new OSMZoomLevel(6), null);
        
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
        OSMTileName tileName = new OSMTileName(1, 0, zoomLevel, null);
        
        assertEquals(new OSMTileName(0, 0, zoomLevel, null), tileName.getRightNeighbour());  
        //assertEquals(new OSMTileName(1, 1, zoomLevel, null), tileName.getUpperNeighbour());     
    }
    
    public void testNeighbourCalculation2() {
        OSMZoomLevel zoomLevel = new OSMZoomLevel(2);        
        OSMTileName tileName = new OSMTileName(2, 2, zoomLevel, null);
        
        assertEquals(new OSMTileName(3, 2, zoomLevel, null), tileName.getRightNeighbour());     
    }
    
    public void testGetExtentFromTileName2() {
        // actually not a test, just printing out the extent of the whole map
        OSMTileName tileName = new OSMTileName(0, 0, new OSMZoomLevel(0), null);
        ReferencedEnvelope extent = OSMTile.getExtentFromTileName(tileName);
        
        System.out.println("Min-X: " + extent.getMinX()); //$NON-NLS-1$
        System.out.println("Max-X: " + extent.getMaxX()); //$NON-NLS-1$
        System.out.println("Min-Y: " + extent.getMinY()); //$NON-NLS-1$
        System.out.println("Max-Y: " + extent.getMaxY()); //$NON-NLS-1$     
        
        assertEquals(true, true);
    }
    
    public void testYahooTry() {
        OSMSource osmSource = new OSMMapnikSource();

        OSMTile tile = (OSMTile) osmSource.getTileFactory().getTileFromCoordinate(84, -170, new OSMZoomLevel(3), osmSource);
        //OSMTile tile = OSMTile.getTileFromCoordinate(49.56474, 6.36812220, new OSMZoomLevel(17), osmSource);
        Coordinate coord = tile.getCenter();

        System.out.println("&latitude=" + coord.y +
                "&longitude=" + coord.x);
        
        printCorrectMedian(tile.getExtent(), osmSource);
        
        System.out.println("-latitude=" + tile.getExtent().getMedian(1) +
                "&longitude=" + tile.getExtent().getMedian(0));
        
        OSMTile tile2 = tile.getRightNeighbour();
        coord = tile2.getCenter();

        System.out.println("&latitude=" + coord.y +
                "&longitude=" + coord.x);
//        printCorrectMedian(tile2.getExtent(), osmSource);
//        System.out.println("-latitude=" + tile2.getExtent().getMedian(1) +
//                "&longitude=" + tile2.getExtent().getMedian(0));
        
        OSMTile tile3 = tile.getLowerNeighbour();
        coord = tile3.getCenter();

        System.out.println("&latitude=" + coord.y +
                "&longitude=" + coord.x);
//        printCorrectMedian(tile3.getExtent(), osmSource);
//        System.out.println("-latitude=" + tile3.getExtent().getMedian(1) +
//                "&longitude=" + tile3.getExtent().getMedian(0));
        
        //printCorrectMedian(tile.getExtent(), osmSource);
    }
    
    private void printCorrectMedian(ReferencedEnvelope envelope, OSMSource source) {
        CoordinateReferenceSystem mercator = source.getProjectedTileCrs();
        
        try {
            MathTransform transform = CRS.findMathTransform(envelope.getCoordinateReferenceSystem(), 
                    mercator);
            ReferencedEnvelope envMercator = 
                new ReferencedEnvelope(JTS.transform(envelope, transform), mercator);
            
            Coordinate median = new Coordinate(envMercator.getMedian(0), envMercator.getMedian(1));
            

            MathTransform transformBack = CRS.findMathTransform(mercator, 
                    envelope.getCoordinateReferenceSystem());
            
            Coordinate mediaWgs = new Coordinate();
            mediaWgs = JTS.transform(median, mediaWgs, transformBack);
            
            System.out.println("&latitude=" + mediaWgs.y +
                    "&longitude=" + mediaWgs.x);
            
            
        } catch (Exception e) {
            // TODO Handle FactoryException
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
        
        
    }
}
