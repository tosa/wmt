package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.util.Map;

import net.refractions.udig.catalog.internal.wmt.tile.WMTTile.WMTTileFactory;
import net.refractions.udig.catalog.internal.wmt.ui.properties.WMTLayerProperties;
import net.refractions.udig.catalog.wmsc.server.Tile;

import org.geotools.geometry.jts.ReferencedEnvelope;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class VESource extends WMTSource {
    public static String NAME = "Microsoft Virtual Earth"; //$NON-NLS-1$
    
    protected VESource() {
        System.out.println("VESource");
        setName(NAME);
    }

    /**
     * see: 
     * http://blogs.esri.com/Support/blogs/mappingcenter/archive/2009/03/19/How-can-you-tell-what-map-scales-are-shown-for-online-maps_3F00_.aspx
     */
    @Override
    public double[] getScaleList() {
        throw new NotImplementedException();
    }

    @Override
    public WMTTileFactory getTileFactory() {
        return null;
    }

}
