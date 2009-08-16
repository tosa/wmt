package net.refractions.udig.tests.catalog.wmt;


import net.refractions.udig.catalog.internal.wmt.tile.YMITile;
import net.refractions.udig.catalog.internal.wmt.tile.YMITile.YMITileName;
import net.refractions.udig.catalog.internal.wmt.tile.YMITile.YMITileName.YMIZoomLevel;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSourceFactory;
import net.refractions.udig.catalog.internal.wmt.wmtsource.YMISource;
import junit.framework.TestCase;

public class YMITileTest extends TestCase{

    public void testGetTileFromCoordinate() {
        YMISource source = (YMISource) WMTSourceFactory.createSource(null, WMTSource.getRelatedServiceUrl(YMISource.class), null, true);
        
//        System.out.println(YMITile.getExtentFromTileName(new YMITileName(0, 0, new YMIZoomLevel(11), source)));
        YMITile tile = new YMITile(0, 0, new YMIZoomLevel(0), source);
        System.out.println(tile.getBounds());
        System.out.println(tile.getUrl());
        tile = new YMITile(51, 51, new YMIZoomLevel(0), source);
        System.out.println(tile.getBounds());
        System.out.println(tile.getUrl());
        
        YMITile tile1 = (YMITile) source.getTileFactory().getTileFromCoordinate(49.38052, 6.55268, new YMIZoomLevel(0), source);
        YMITile tile2 = tile1.getRightNeighbour();
        YMITile tile3 = tile1.getLowerNeighbour();
        
        System.out.println(tile1.getBounds());
        System.out.println(tile1.getUrl());
        System.out.println(tile2.getBounds());
        System.out.println(tile2.getUrl());
        System.out.println(tile3.getBounds());
        System.out.println(tile3.getUrl());
        
//        assertEquals("http://tile.openstreetmap.org/6/33/21.png", tile.getUrl().toString()); //$NON-NLS-1$
    }

}
