package net.refractions.udig.catalog.internal.wmt.tile;

import java.net.URL;

import org.geotools.geometry.jts.ReferencedEnvelope;

public class Tile {
    private URL url;
    private String id;
    private ReferencedEnvelope extent;
    // + imageObject of the downloaded/cached tile
    
    public Tile(URL url, ReferencedEnvelope extent) {
        this.extent = extent;
        this.url = url;
    }
    
    public URL getUrl(){
        return url;
    }
    
    public ReferencedEnvelope getExtent() {
        return extent;
    }
}
