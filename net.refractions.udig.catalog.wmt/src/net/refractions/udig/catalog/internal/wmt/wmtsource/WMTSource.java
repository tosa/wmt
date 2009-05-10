package net.refractions.udig.catalog.internal.wmt.wmtsource;

public abstract class WMTSource {
    private String text;
    
    public WMTSource() {
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
}
