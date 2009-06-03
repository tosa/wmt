package net.refractions.udig.catalog.internal.wmt.tile;

import java.util.List;
import java.util.Map;

import org.geotools.data.ows.CRSEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.wmsc.server.Tile;
import net.refractions.udig.catalog.wmsc.server.TiledWebMapServer;
import net.refractions.udig.catalog.wmsc.server.WMSTileSet;

public class WMTTileSetWrapper extends WMSTileSet {
    private WMTSource wmtSource;
    
    
    public WMTTileSetWrapper(WMTSource wmtSource) {
        this.wmtSource = wmtSource;
    }

    @Override
    public String createQueryString(Envelope tile) {
        return super.createQueryString(tile);
    }

    @Override
    public ReferencedEnvelope getBounds() {
        return super.getBounds();
    }

    @Override
    public List<Envelope> getBoundsListForZoom( Envelope bounds, double zoom ) {
        return super.getBoundsListForZoom(bounds, zoom);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return super.getCoordinateReferenceSystem();
    }

    @Override
    public String getEPSGCode() {
        return super.getEPSGCode();
    }

    @Override
    public String getFormat() {
        return super.getFormat();
    }

    @Override
    public int getHeight() {
        return wmtSource.getTileHeight();
    }

    @Override
    public int getId() {
        return super.getId();
    }

    @Override
    public String getLayers() {
        return "";
    }

    @Override
    public int getNumLevels() {
        return 0;
    }

    @Override
    public double[] getResolutions() {
        return null;
    }

    @Override
    public TiledWebMapServer getServer() {
        return null;
    }

    @Override
    public String getStyles() {
        return "";
    }

    @Override
    public long getTileCount(Envelope bounds, double zoom) {
        return super.getTileCount(bounds, zoom);
    }

    @Override
    public Map<String, Tile> getTilesFromViewportScale( Envelope bounds, double viewportScale ) {
        return super.getTilesFromViewportScale(bounds, viewportScale);
    }

    @Override
    public Map<String, Tile> getTilesFromZoom( Envelope bounds, double zoom ) {
        return super.getTilesFromZoom(bounds, zoom);
    }

    @Override
    public int getWidth() {
        return wmtSource.getTileWidth();
    }

    @Override
    public void setBoundingBox( CRSEnvelope bbox ) {
        super.setBoundingBox(bbox);
    }

    @Override
    public void setCoorindateReferenceSystem( String epsg ) {
        super.setCoorindateReferenceSystem(epsg);
    }

    @Override
    public void setFormat( String format ) {
        super.setFormat(format);
    }

    @Override
    public void setHeight( int height ) {
        super.setHeight(height);
    }

    @Override
    public void setLayers( String layers ) {
        super.setLayers(layers);
    }

    @Override
    public void setResolutions( String res ) {
        super.setResolutions(res);
    }

    @Override
    public void setServer( TiledWebMapServer server ) {
        super.setServer(server);
    }

    @Override
    public void setStyles( String styles ) {
        super.setStyles(styles);
    }

    @Override
    public void setWidth( int width ) {
        super.setWidth(width);
    }
    

}
