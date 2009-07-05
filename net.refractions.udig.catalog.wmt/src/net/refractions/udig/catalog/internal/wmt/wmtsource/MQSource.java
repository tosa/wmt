package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.util.Map;

import net.refractions.udig.catalog.internal.wmt.tile.MQTile;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile.WMTTileFactory;

public class MQSource extends WMTSource {
    public static String NAME = "MapQuest Maps"; //$NON-NLS-1$
    private static WMTTileFactory tileFactory = new MQTile.MQTileFactory();

    public MQSource() {
        System.out.println(NAME);
        setName(NAME); 
    }
    
    public static final double PIXELSPERLATDEGREE = 315552459.66191697;
    public static final double PIXELSPERLNGDEGREE = 250344597.90989706;
    public static int TILESIZE = 256;
    
    @Override
    public String getFileFormat() {
        return "gif"; //$NON-NLS-1$
    }
    
    /*
     * MapQuest scales
     * we are using the same scales that MaqQuest is using for their tiles
     * (even if we could request whatever scale we want, but so we 
     * can also use the same tiling scheme) 
     * 
     * see also:
     * http://developer.mapquest.com/content/documentation/ApiDocumentation/53/JavaScript/JS_DeveloperGuide_v5.3.0.1.htm#styler-id1.17
     */
    public static double[] scaleList = {
        88011773,
        29337258,
        9779086,
        3520471,
        1504475,
        701289,
        324767,
        154950,
        74999,
        36000,
        18000,
        9000,
        4700,
        2500,
        1500,
        1000
    };

    
    @Override
    public double[] getScaleList() {
        return MQSource.scaleList;
    }
    @Override
    public WMTTileFactory getTileFactory() {
        return tileFactory;
    }

}
