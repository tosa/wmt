package net.refractions.udig.tests.catalog.wmt;

import org.geotools.geometry.jts.ReferencedEnvelope;

import net.refractions.udig.catalog.internal.wmt.tile.MQTile;
import net.refractions.udig.catalog.internal.wmt.tile.MQTile.MQTileName.MQZoomLevel;
import net.refractions.udig.catalog.internal.wmt.tile.MQTile.MQTileName;
import net.refractions.udig.catalog.internal.wmt.wmtsource.MQSource;
import junit.framework.TestCase;


public class MQTileTest extends TestCase {
    public void testNeighbourCalculation() {
        //MQSource source = new MQSource();
        
        //MQTile tile = new MQTile(0, 1, new MQZoomLevel(0), null);
        MQTileName tileName = new MQTileName(0, 1, new MQZoomLevel(0), null);
        
        assertEquals(new MQTileName(0, 0, new MQZoomLevel(0), null), tileName.getLowerNeighbour());  
    }
    
    public void testHighestRow() {
        MQTile tile = new MQTile(0, 2, new MQZoomLevel(0), null); 
        System.out.println(tile.getUrl());
        printExtent(tile.getExtent());
    }
    
    public void testLowestRow() {
        MQTile tile = new MQTile(0, 0, new MQZoomLevel(0), null);
        System.out.println(tile.getUrl());
        printExtent(tile.getExtent());
        
        MQTile tile2 = tile.getRightNeighbour();
        System.out.println(tile2.getUrl());
        printExtent(tile2.getExtent());
        
        MQTile tile3 = tile2.getRightNeighbour();
        System.out.println(tile3.getUrl());
        printExtent(tile3.getExtent());
        
        MQTile tile4 = tile3.getRightNeighbour();
        System.out.println(tile4.getUrl());
        printExtent(tile4.getExtent());
    }
    
    private void printExtent(ReferencedEnvelope env) {
        System.out.println("minY: " + env.getMinY());
        System.out.println("maxY: " + env.getMaxY());
        System.out.println("minX: " + env.getMinX());
        System.out.println("maxX: " + env.getMaxX());
        
    }
    
    public void testToRemove() {
        System.out.println(MQTile.pixel2lat(640, 88011773) + " , " + 
                MQTile.pixel2lon(1024, 88011773));
    }
}
