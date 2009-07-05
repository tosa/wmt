package net.refractions.udig.tests.catalog.wmt;

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
}
