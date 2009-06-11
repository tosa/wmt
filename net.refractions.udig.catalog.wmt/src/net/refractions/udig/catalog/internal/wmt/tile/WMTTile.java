package net.refractions.udig.catalog.internal.wmt.tile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import javax.imageio.ImageIO;

import net.refractions.udig.catalog.internal.wmt.WMTPlugin;
import net.refractions.udig.catalog.wmsc.server.Tile;
import net.refractions.udig.catalog.wmsc.server.TileSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.geometry.jts.ReferencedEnvelope;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.vividsolutions.jts.geom.Envelope;

public abstract class WMTTile implements Tile{
    private final static boolean testing = false;  // for testing output
    
    public static final int INERROR = 1;
    public static final int OK = 0;
    
    private WMTTileName tileName;
    private ReferencedEnvelope extent;
    private BufferedImage image; //imageObject of the downloaded/cached tile
    private Object imageLock = new Object();
    private int state;
    
    public WMTTile(ReferencedEnvelope extent, WMTTileName tileName) {
        this.extent = extent;
        this.tileName = tileName;
    }
    
    public URL getUrl(){
        return tileName.getTileUrl();
    }
    
    public ReferencedEnvelope getExtent() {
        return extent;
    }
    
    public BufferedImage getBufferedImage() {
        return image;
    }
    
    public String getId() {
        return tileName.getId();
    }
    
    public String getReleatedSourceName() {
        return tileName.getSource().getName();
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
//             if (connection.getResponseCode() == 200)
            
            bufImage = ImageIO.read(getUrl()); //(inputStream);

            if (bufImage != null) {
                image = bufImage;
                                
                System.out.println(image.getWidth() + " - " + image.getHeight());
            } else {
                // create an error buffered image
                System.out.println("download failed: " + getUrl().toString());
            }
        } catch (Exception e1) {
            System.out.println("download failed: " + getUrl().toString());
        } catch (Throwable t) {
            System.out.println("download failed: " + getUrl().toString());
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

    public Envelope getBounds() {
        return extent;
    }

    public String getPosition() {
        return getId();
    }

    public double getScale() {
        return tileName.getZoomLevel();
    }

    public Object getTileLock() {
        return imageLock;
    }

    public TileSet getTileSet() {
        return null;
    }

    /* (non-Javadoc)
     * @see net.refractions.udig.catalog.wmsc.server.Tile#getTileState()
     */
    public int getTileState(){
        return this.state;
    }
    
    /* (non-Javadoc)
     * @see net.refractions.udig.catalog.wmsc.server.Tile#setTileState(int)
     */
    public void setTileState(int state){
        this.state = state;
    }

    public boolean loadTile(IProgressMonitor monitor) {
        if (tileName == null) {
            WMTPlugin.log("error, no tilename", null); //$NON-NLS-1$
            return false;
        }
        
        // get a lock for this tile and only fetch a new image if
        // it does not have one set already when the lock is obtained.
        // TODO:  add support for re-fetching expired images too
        Object lock = getTileLock();
        BufferedImage bufImage = null;
        if (testing) {
            System.out.println("getting lock: "+getId()); //$NON-NLS-1$
        }
        synchronized (lock) {
            if (testing) {
                System.out.println("got lock: "+getId()); //$NON-NLS-1$
            }
            if ((getBufferedImage() != null && getTileState() != WMTTile.INERROR) || monitor.isCanceled()) {
                // tile image already set
                monitor.setCanceled(true);
                if (testing) {
                    System.out.println("REQUEST CANCELLED - REMOVING lock: "+getId()); //$NON-NLS-1$
                }                    
                return true;
            }
           try {
                // simulate latency if testing
                if (testing) {
                    Random rand = new Random(); 
                    long delay = rand.nextInt(5000); // delay 1-5 secs
                    System.out.println("request delaying for: "+delay); //$NON-NLS-1$
                    Thread.sleep(delay);  // simulate latency
                }
                WMTPlugin.log("WMT GetTile: "+ getUrl(), null);  //$NON-NLS-1$
                bufImage = ImageIO.read(getUrl());
                if (bufImage != null) {
                    setBufferedImageInternal(bufImage);
                    setTileState(WMTTile.OK);
                }else{
                    // create an error buffered image
                    setBufferedImageInternal(createErrorImage());
                    setTileState(WMTTile.INERROR);
                }
            } catch (Exception e1) {
                // create an error buffered image
                setBufferedImageInternal(createErrorImage());
                setTileState(WMTTile.INERROR);
                WMTPlugin.log("error loading tile, placeholder created:", e1); //$NON-NLS-1$
            } catch( Throwable t){
                // create an error buffered image
                setBufferedImageInternal(createErrorImage());
                setTileState(WMTTile.INERROR);
                WMTPlugin.log("error loading tile, placeholder created:", t); //$NON-NLS-1$
            } finally {
                // nothing?
            }
        } // end synchronized block
        
        if (testing) {
            System.out.println("REMOVING lock: "+getId()); //$NON-NLS-1$
        }
        
        // if we successfully set the buffered image to something, return true
        if (getBufferedImage() != null) {
            return true;
        }
        System.out.println("// if we get here, something prevented us from setting an image");
        // if we get here, something prevented us from setting an image
        return false;
    }    

    private BufferedImage createErrorImage() {
        BufferedImage bf = new BufferedImage(tileName.getSource().getTileWidth(), tileName.getSource().getTileHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bf.createGraphics();
        g.setColor(Color.RED);
        g.drawLine(0, 0, tileName.getSource().getTileWidth(), tileName.getSource().getTileHeight());
        g.drawLine(0, tileName.getSource().getTileHeight(), tileName.getSource().getTileWidth(), 0);
        return bf;
    }
    
    public void setBufferedImage(BufferedImage im) {
        Object lock = getTileLock();
        synchronized (lock) {       
            setBufferedImageInternal(im);
            if (getBufferedImage() != null) {
                setTileState(WMTTile.OK);
            }
            else {
                setTileState(WMTTile.INERROR);
            }
        }
    }

    /**
     * Set the buffered image without locking.
     */
    private void setBufferedImageInternal(BufferedImage im) {
        this.image = im;
    }
    
    public void setPosition(String pos)  {
        throw new NotImplementedException();
    }


    public int compareTo(Tile other) {
        // id contains scale and bounds so compare with that
        return getId().compareTo( other.getId() );
    }
    
    public boolean equals(Object arg0) {
        if (arg0 instanceof Tile) {
            Tile tile = (Tile) arg0;
            // id contains scale and bounds so compare with that
            if (getId().equals(tile.getId())) {
                return true;
            }
            else {
                return false;
            }
        }
        return super.equals(arg0);
    }
    
}