package net.refractions.udig.catalog.internal.wmt.tile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.geotools.geometry.jts.ReferencedEnvelope;

public class Tile {
    private URL url;
    private String id;
    private ReferencedEnvelope extent;
    private BufferedImage bufferedImage; //imageObject of the downloaded/cached tile
    
    public Tile(URL url, ReferencedEnvelope extent, String id) {
        this.extent = extent;
        this.url = url;
        this.id = id;
    }
    
    public URL getUrl(){
        return url;
    }
    
    public ReferencedEnvelope getExtent() {
        return extent;
    }
    
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
    
    public String getId() {
        return id;
    }
    
    /**
     * 
     * @see net.refractions.udig.catalog.wmsc.server.WMSTile#loadTile(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void download() {
        BufferedImage bufImage = null;
        InputStream inputStream = null;
        try {

            // todo: check if file exists via http-return-code?
//            HttpURLConnection connection = null;
//            connection = (HttpURLConnection) getUrl().openConnection();
//
//            inputStream = connection.getInputStream();

            bufImage = ImageIO.read(getUrl()); //(inputStream);

            if (bufImage != null) {
                bufferedImage = bufImage;
                                
                System.out.println(bufferedImage.getWidth() + " - " + bufferedImage.getHeight());
            } else {
                // create an error buffered image
                System.out.println("download failed: " + url.toString());
            }
        } catch (Exception e1) {
            System.out.println("download failed: " + url.toString());
        } catch (Throwable t) {
            System.out.println("download failed: " + url.toString());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.out.println("failed to close input stream!!!"); //$NON-NLS-1$
                }
            }
        }
    }
    
}
