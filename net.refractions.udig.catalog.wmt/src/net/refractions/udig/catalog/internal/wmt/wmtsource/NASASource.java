package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import net.refractions.udig.catalog.internal.wmt.tile.NASATile;
import net.refractions.udig.catalog.internal.wmt.tile.NASATile.NASATileName.NASAZoomLevel;
import net.refractions.udig.catalog.internal.wmt.tile.WMTTile.WMTTileFactory;

public class NASASource extends WMTSource {
    public static String NAME = "Global Mosaic, pan sharpened visual"; //$NON-NLS-1$
    
    private static WMTTileFactory tileFactory = new NASATile.NASATileFactory();
    
    private List<NASAZoomLevel> zoomLevels;
    // todo: get them from somewhere
    private ReferencedEnvelope bounds = new ReferencedEnvelope(-180, 180, -90, 90, DefaultGeographicCRS.WGS84);
    private double[] scales;
    
    public NASASource() {
        System.out.println(NAME);
        setName(NAME);
       
        // create the list of available zoom-levels (Tilepatterns)
        // later this will be parsed from a xml-file
        
        zoomLevels = new ArrayList<NASAZoomLevel>();
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,-38,-52,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,26,-116,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,58,-148,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,74,-164,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,82,-172,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,86,-176,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,88,-178,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,89,-179,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,89.5,-179.5,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,89.75,-179.75,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,89.875,-179.875,90", bounds));
        zoomLevels.add(new NASAZoomLevel("request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,89.9375,-179.9375,90", bounds));
        
        Collections.sort(zoomLevels);
        Collections.reverse(zoomLevels);
        
        // generate scale list  
        scales = new double[zoomLevels.size()];
        for (int i = 0; i < zoomLevels.size(); i++) {
            zoomLevels.get(i).setZoomLevel(i);
            
            scales[i] = zoomLevels.get(i).getScale();
        }
    }
    
    @Override
    public String getFileFormat() {
        return "jpeg"; //$NON-NLS-1$
    }

    @Override
    public double[] getScaleList() {
        return scales;
    }
    
    public NASAZoomLevel getZoomLevel(int index) {
        return zoomLevels.get(index);
    }
    
    @Override
    public WMTTileFactory getTileFactory() {
        return tileFactory;
    }

    // todo: get this from somewhere
    @Override
    public int getTileHeight() {
        return super.getTileHeight();
    }

    // todo: get this from somewhere
    @Override
    public int getTileWidth() {
        return super.getTileWidth();
    }

}
