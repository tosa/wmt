package net.refractions.udig.catalog.internal.wmt.wmtsource;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.refractions.udig.catalog.internal.wmt.tile.MQTile;
import com.mapquest.apiwrapper.MQAPIWrapper;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile.WMTTileFactory;

public class MQSource extends WMTSource {
    public static String NAME = "MapQuest Maps"; //$NON-NLS-1$
    
    private static WMTTileFactory tileFactory = new MQTile.MQTileFactory();
    private static MQAPIWrapper apiWrapper = null;
    
    protected MQSource() {
        System.out.println(NAME);
        setName(NAME); 
    }
    

    //region CRS
    public static final CoordinateReferenceSystem CRS_MQ_PROJECTION;    
    static {
        CoordinateReferenceSystem crs = null;
        
        String wkt =
                "PROJCS[\"MapQuest Projection\"," +
                "   GEOGCS[\"GCS_ADG_1984\"," +
                "       DATUM[\"D_Australian_1984\"," +
                "           SPHEROID[\"Australian\",6378137.0,298.25]" +
                "       ]," +
                "       PRIMEM[\"Greenwich\",0.0]," +
                "       UNIT[\"Degree\",0.0174532925199433]" +
                "   ]," +
                "PROJECTION[\"Equidistant_Cylindrical\"]," +
                "PARAMETER[\"False_Easting\",0.0]," +
                "PARAMETER[\"False_Northing\",0.0]," +
                "PARAMETER[\"Central_Meridian\",0.0]," +
                "PARAMETER[\"Standard_Parallel_1\",37.5]," +
                "UNIT[\"Meter\",1.0]" +
                "]";
//        String wkt =
//        "PROJCS[\"WGS84 / EquiRectangular\", " +
//        "      GEOGCS[\"WGS 84\", " +
//        "          DATUM[\"WGS_1984\", " +
//        "              SPHEROID[\"WGS_1984\", 6378156.743315, 1.0]" +
//        "          ], " +
//        "          PRIMEM[\"Greenwich\", 0.0], " +
//        "          UNIT[\"degree\", 0.017453292519943295], " +
//        "          AXIS[\"Longitude\", EAST], " +
//        "          AXIS[\"Latitude\", NORTH]" +
//        "      ], " +
//        "      PROJECTION[\"Equirectangular\"], " +
//        "      PARAMETER[\"latitude_of_origin\", 37.5],   " +            
//        "      PARAMETER[\"central_meridian\", 0.0], " +
//        "      PARAMETER[\"false_easting\", 0.0], " +
//        "      PARAMETER[\"false_northing\", 0.0], " +
//        "      UNIT[\"m\", 1.0], " +
//        "      AXIS[\"x\", EAST], " +
//        "      AXIS[\"y\", NORTH] " +
//        "  ] ";


        
            try {
                crs = CRS.parseWKT(wkt);
            } catch (Exception exc2) {
                System.out.println("-------------------------: " + exc2.getMessage());
                crs = DefaultGeographicCRS.WGS84;
            }
        
        CRS_MQ_PROJECTION = crs;
    }
    
    public CoordinateReferenceSystem getProjectedTileCrs() {
        return CRS_MQ_PROJECTION;
    }
    //endregion
    
    public ReferencedEnvelope getBounds() {
        return new ReferencedEnvelope(-180, 180, -90, 88.5, DefaultGeographicCRS.WGS84);        
    }
    
    
    public static final double PIXELSPERLATDEGREE = 315552459.66191697;
    public static final double PIXELSPERLNGDEGREE = 250344597.90989706;
    
    public static final int TILESIZE_WIDTH = 512;
    public static final int TILESIZE_HEIGHT = 320;
    
    @Override
    public String getFileFormat() {
        return "gif"; //$NON-NLS-1$
    }
    
    /*
     * MapQuest scales
     * we are using the same scales that MaqQuest is using for their tiles
     * (even if we could request whatever scale we want, but so we 
     * can use a similar tiling scheme) 
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
    
    public MQAPIWrapper getApiWrapper() {
        if (apiWrapper == null){
            synchronized (this) {
                if (apiWrapper == null){
                    apiWrapper = new MQAPIWrapper();
                }
            }
        }
        
        return apiWrapper;
    }

}