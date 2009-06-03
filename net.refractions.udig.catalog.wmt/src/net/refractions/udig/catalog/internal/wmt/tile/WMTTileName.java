package net.refractions.udig.catalog.internal.wmt.tile;

import java.net.URL;

import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;

public abstract class WMTTileName {
    public static final String ID_DIVIDER = "_"; //$NON-NLS-1$
    
    private int zoomLevel;
    private int x;
    private int y;
    private WMTSource source;
    
    public WMTTileName(int zoomLevel, int x, int y, WMTSource source) {
        this.zoomLevel = zoomLevel;
        this.x = x;
        this.y = y;
        this.source = source;
    }
    
    public int getZoomLevel() {
        return zoomLevel;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public WMTSource getSource() {
        return source;
    }
    
    public String getId() {
        return source.getName() + ID_DIVIDER + 
                getZoomLevel() + ID_DIVIDER + 
                getX() + ID_DIVIDER + 
                getY();
    }
    
    public abstract URL getTileUrl();
       
}
