package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.util.Map;

import net.refractions.udig.catalog.internal.wmt.tile.YMITile;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile.WMTTileFactory;
import net.refractions.udig.catalog.internal.wmt.ui.properties.WMTLayerProperties;
import net.refractions.udig.catalog.wmsc.server.Tile;

import org.geotools.geometry.jts.ReferencedEnvelope;

public class YMISource extends WMTSource {
    public static String NAME = "Yahoo! Map Image"; //$NON-NLS-1$
    
    private static WMTTileFactory tileFactory = new YMITile.YMITileFactory();
    
    public static final int TILESIZE = 500;

    protected YMISource() {
        setName(NAME); 
    }
    
    @Override
    public WMTTileFactory getTileFactory() {
        return tileFactory;
    }
            
    @Override
    public int getTileHeight() {
        return TILESIZE;
    }

    @Override
    public int getTileWidth() {
        return TILESIZE;
    }

    @Override
    public Map<String, Tile> cutExtentIntoTiles( ReferencedEnvelope extent, double scale,
            int scaleFactor, boolean recommendedZoomLevel, WMTLayerProperties layerProperties ) {
        return null;
    }
    /*
     * Yahoo Map Image zoom-levels are in range [1,12],
     * see also:
     * http://code.davidjanes.com/blog/2008/11/08/switching-between-mapping-apis-and-universal-map-levels/
     */
    public static double[] scaleList = {
        Double.NaN,
        Double.NaN,    
        Double.NaN,
        Double.NaN,
        Double.NaN,
        Double.NaN,
        9244648,
        4622324,
        2311162,
        1155581,
        577790,
        288895,
        144447,
        72223,
        36111,
        18055,
        9027,
        4513
    };

    @Override
    public double[] getScaleList() { 
        return YMISource.scaleList;
    }

}
