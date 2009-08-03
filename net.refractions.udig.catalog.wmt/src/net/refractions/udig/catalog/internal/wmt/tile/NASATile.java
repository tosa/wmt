package net.refractions.udig.catalog.internal.wmt.tile;

import java.awt.Dimension;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import net.refractions.udig.catalog.internal.wmt.tile.NASATile.NASATileName.NASAZoomLevel;
import net.refractions.udig.catalog.internal.wmt.wmtsource.NASASource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.core.internal.CorePlugin;
import net.refractions.udig.project.internal.render.impl.ScaleUtils;

import org.eclipse.swt.widgets.Display;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

public class NASATile extends WMTTile {

    private NASATileName tileName;
    private NASASource nasaSource;
    
    public NASATile(int x, int y, NASAZoomLevel zoomLevel, NASASource nasaSource) {
        this(new NASATileName(x, y, zoomLevel, nasaSource), nasaSource);
    }
    
    public NASATile(NASATileName tileName, NASASource nasaSource){
        super(NASATile.getExtentFromTileName(tileName), tileName);
        
        this.tileName = tileName;
        this.nasaSource = nasaSource;
    }
    //region Get extent from tile-name        
    /**
     * Returns the bounding box of a tile by the given tile name.
     * 
     * The upper left corner of tile 0/0 is at 90,-180.
     *  
     * @param tileName
     * @return BoundingBox for a tile
     */
    public static ReferencedEnvelope getExtentFromTileName(NASATileName tileName) {
        ReferencedEnvelope extent = new ReferencedEnvelope(
                tile2lon(tileName.getX(), tileName), 
                tile2lon(tileName.getX() + 1, tileName), 
                tile2lat(tileName.getY(), tileName), 
                tile2lat(tileName.getY() + 1, tileName), 
                DefaultGeographicCRS.WGS84);
        
        return extent;
    }
    
    public static double tile2lat(int row, NASATileName tileName) {       
        return 90 - row * tileName.getHeightInWorldUnits();     
    }
    
    public static double tile2lon(int col, NASATileName tileName) { 
        return -180 + col * tileName.getWidthInWorldUnits();            
    }

    //endregion
    
    @Override
    public NASATile getLowerNeighbour() {
        return new NASATile(tileName.getLowerNeighbour(), nasaSource);
    }

    @Override
    public NASATile getRightNeighbour() {
        return new NASATile(tileName.getRightNeighbour(), nasaSource);
    }

    public static class NASATileFactory extends WMTTileFactory {

        //region Get tile from coordinate
        /**
         * Finds out the tile which contains the coordinate at a given zoom level.
         * 
         * @param lat y
         * @param lon x
         * @param zoomLevel
         * @param wmtSource
         * @return
         */
        public NASATile getTileFromCoordinate(double lat, double lon, 
                WMTZoomLevel zoomLevel, WMTSource wmtSource) {
            // normalize latitude and longitude
            lat = WMTTileFactory.normalizeDegreeValue(lat, 90);
            lon = WMTTileFactory.normalizeDegreeValue(lon, 180);

            NASAZoomLevel nasaZoomLevel = (NASAZoomLevel) zoomLevel;
            
            int row = (int) Math.abs((lat - 90)  / nasaZoomLevel.getHeightInWorldUnits());
            int col = (int) Math.abs((lon + 180) / nasaZoomLevel.getWidthInWorldUnits());
            
            return new NASATile(col, row, nasaZoomLevel, (NASASource) wmtSource);
        }
        //endregion

        public WMTZoomLevel getZoomLevel(int zoomLevel, WMTSource wmtSource) {
            NASASource nasaSource = (NASASource) wmtSource;
            
            return nasaSource.getZoomLevel(zoomLevel);
        }
        
    }
    
    /**
     * A small helper class which stores the tile-name.
     * 
     * @author to.srwn
     * @since 1.1.0
     */
    public static class NASATileName extends WMTTileName{
        private NASAZoomLevel zoomLevel;
        private NASASource nasaSource;
                
        public NASATileName(int x, int y, NASAZoomLevel zoomLevel, NASASource source) {
            super(zoomLevel, x, y, source);
            this.zoomLevel = zoomLevel;
            this.nasaSource = source;
        }
        
        /**
         * 
         * 
         * @return
         */
        public URL getTileUrl() {
            try {
                String tileUrl = zoomLevel.getTileUrl(getExtentFromTileName(this));
                
                return new URL(null, tileUrl, CorePlugin.RELAXED_HANDLER);  
                
            } catch (Exception e) {
                // todo: error-handling
                e.printStackTrace();
            }
            
            return null;
        }
       

        
        public NASATileName getRightNeighbour() {
            return new NASATileName( 
                        WMTTileName.arithmeticMod((getX()+1), zoomLevel.getMaxTilePerRowNumber()),
                        getY(),
                        zoomLevel,
                        nasaSource);
        }
        
        public NASATileName getLowerNeighbour() {
            return new NASATileName( 
                        getX(),
                        WMTTileName.arithmeticMod((getY()+1), zoomLevel.getMaxTilePerColNumber()),
                        zoomLevel,
                        nasaSource);
        }
        
        public double getWidthInWorldUnits() {
            return zoomLevel.getWidthInWorldUnits();
        }
        
        public double getHeightInWorldUnits() {
            return zoomLevel.getHeightInWorldUnits();
        }
        
        public String toString() {
            return zoomLevel.getZoomLevel() + "/" + getX() + "/" + getY(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NASATileName)) return false;
            
            NASATileName other = (NASATileName) obj;
            
            return (getX() == other.getX()) && (getY() == other.getY()) && zoomLevel.equals(other.zoomLevel);
        }

        /**
         * Small helper class which wraps the zoom-level and
         * the maximum tile-number for x and y in this zoom-level. 
         * 
         * 
         * @author to.srwn
         * @since 1.1.0
         */
        public static class NASAZoomLevel extends WMTZoomLevel implements Comparable<NASAZoomLevel>{
                private ReferencedEnvelope boundsOfFirstTile;
                private ReferencedEnvelope tiledGroupBounds;
                private String requestUrlPrefix;
                private String requestUrlSuffix;
                private DecimalFormat boundsFormatter;
                private double scale;
                
                private String baseUrl = "http://onearth.jpl.nasa.gov/wms.cgi?"; //todo: get this from somewhere
                
                private static final char BOUNDS_SEPERATOR = ',';
                private static final String doublePatternString = "([+-]?\\d*\\.?\\d*)(?![-+0-9\\.])"; //$NON-NLS-1$
                private static final String bboxPatternString = "(bbox)(=)" + doublePatternString + //$NON-NLS-1$
                    "(,)" + doublePatternString + //$NON-NLS-1$
                    "(,)" + doublePatternString + //$NON-NLS-1$
                    "(,)" + doublePatternString; //$NON-NLS-1$
                private static final Pattern bboxPattern = Pattern.compile(
                        bboxPatternString, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                
                
            public NASAZoomLevel(String tilePattern, ReferencedEnvelope tiledGroupBounds) {
                super(0);
                
                this.tiledGroupBounds = tiledGroupBounds;
                
                String rawRequest = getRawRequestStringFromTilePattern(tilePattern);
                parseBboxFromRequest(rawRequest);
                findOutScale();
            }
            
            /**
             * Constructs the URL with which a tile for the corresponding 
             * bounds can be fetched.
             * 
             * @param bounds The Extent of the tile
             */
            public String getTileUrl(ReferencedEnvelope bounds) {
                StringBuffer bbox = new StringBuffer();
                
                bbox.append(baseUrl);
                bbox.append(requestUrlPrefix);
                bbox.append("bbox="); //$NON-NLS-1$
                bbox.append(getFormattedCoordinate(bounds.getMinX()));
                bbox.append(BOUNDS_SEPERATOR);
                bbox.append(getFormattedCoordinate(bounds.getMinY()));
                bbox.append(BOUNDS_SEPERATOR);
                bbox.append(getFormattedCoordinate(bounds.getMaxX()));
                bbox.append(BOUNDS_SEPERATOR);
                bbox.append(getFormattedCoordinate(bounds.getMaxY()));
                bbox.append(requestUrlSuffix);
                
                return bbox.toString();
            }
            
            private String getFormattedCoordinate(double value) {
                return boundsFormatter.format(value).replace(',', '.');
            }
            
            /**
             * The <TilePattern> tag may look like this:
             * request=GetMap&layers=global_mosaic&srs=EPSG:4326&format=image/jpeg&styles=visual&width=512&height=512&bbox=-180,-38,-52,90 
             * request=GetMap&layers=global_mosaic&srs=EPSG:4326&width=512&height=512&bbox=-180,-38,-52,90&format=image/jpeg&version=1.1.1&styles=visual
             * 
             * We just want the first request.
             * 
             * @param tilePattern
             * @return
             */
            private String getRawRequestStringFromTilePattern(String tilePattern) {
               if (tilePattern.contains(" ")) { //$NON-NLS-1$
                   return tilePattern.substring(0, tilePattern.indexOf(' ') );
               }
               
               return tilePattern;
            }
            
            /**
             * Parses the BBox of the first tile from the tilepattern, and also 
             * gets the part of the url before the bbox (requestUrlPrefix)
             * and after the bbox (requestUrlSuffix).
             * 
             * For example:
             * rawRequest = request=GetMap&layers=global_mosaic&srs=EPSG:4326&width=512&height=512&bbox=-180,-38,-52,90&format=image/jpeg&version=1.1.1&styles=visual
             * 
             * -->
             * 
             * bbox = -180,-38,-52,90
             * requestUrlPrefix = request=GetMap&layers=global_mosaic&srs=EPSG:4326&width=512&height=512&
             * requestUrlSuffix = &format=image/jpeg&version=1.1.1&styles=visual
             *
             * @param rawRequest
             */
            private void parseBboxFromRequest(String rawRequest) {
                Matcher m = bboxPattern.matcher(rawRequest);
                
                if (m.find())
                {
                    String xMinText = m.group(3);
                    String yMinText = m.group(5);
                    String xMaxText = m.group(7);
                    String yMaxText = m.group(9);
                    
                    setBoundsFormatter(xMinText, yMinText, xMaxText, yMaxText);
                    
                    // Set the url prefix and suffix
                    requestUrlPrefix = getRequestPart(rawRequest, 0, m.start());
                    requestUrlSuffix = getRequestPart(rawRequest, m.end(), rawRequest.length());
                    
                    // build bbox
                    try {
                        double xMin = Double.parseDouble(xMinText);
                        double yMin = Double.parseDouble(yMinText);
                        double xMax = Double.parseDouble(xMaxText);
                        double yMax = Double.parseDouble(yMaxText);
                        
                        boundsOfFirstTile = new ReferencedEnvelope(
                                xMin,
                                xMax,
                                yMin,
                                yMax,
                                DefaultGeographicCRS.WGS84 // todo: get this from somewhere
                        );
                        
                        return;
                    } catch(NumberFormatException exc) {}
                }
                
                boundsOfFirstTile = null;
            }
            
            private String getRequestPart(String rawRequest, int start, int end) {
                if (start < 0 || end < 0 || start > end) {
                    return ""; //$NON-NLS-1$
                } else {
                    return rawRequest.substring(start, end);
                }
            }
            
            /**
             * So that requests to the NASA WMS server can be recognized as a
             * tile request, the request must follow a special tilepattern (see
             * http://onearth.jpl.nasa.gov/tiled.html ).
             * 
             * Therefore to avoid request like "1.0000001,0.9999999" we round
             * to a specific accuracy by using DecimalFormat. 
             *
             * @param xMinText
             * @param yMinText
             * @param xMaxText
             * @param yMaxText
             */
            private void setBoundsFormatter(String xMinText, String yMinText, String xMaxText, String yMaxText) {
                int maxCountOfDecimalPlaces = getMax(
                        getDecimalPlacesCount(xMinText),
                        getDecimalPlacesCount(yMinText),
                        getDecimalPlacesCount(xMaxText),
                        getDecimalPlacesCount(yMaxText));
                
                String format = "##0"; //$NON-NLS-1$
                
                if (maxCountOfDecimalPlaces > 0) {
                    format += "."; //$NON-NLS-1$
                    
                    for (int i = 0; i < maxCountOfDecimalPlaces; i++) {
                        format += "#"; //$NON-NLS-1$
                    }
                }
                    
                boundsFormatter = new DecimalFormat(format);
            }
            
            /**
             * Counts the number of decimal places.
             *
             * @param number
             * @return
             */
            private int getDecimalPlacesCount(String number) {
                if (number.contains(".")) { //$NON-NLS-1$
                    int posOfDot = number.indexOf("."); //$NON-NLS-1$
                    
                    return (number.length() - 1) - posOfDot;
                } else {
                    return 0;
                }
            }
            
            private int getMax(int a, int b, int c, int d) {
                return Math.max(a, 
                            Math.max(b, 
                                Math.max(c, d)));        
            }
            
            /**
             * Calculates the corresponding scale from the bounds of a tile
             * and the size of that tile in pixels.
             */
            private void findOutScale() {
                if (boundsOfFirstTile == null) {
                    scale = Double.NaN;
                } else {
                    scale = ScaleUtils.calculateScaleDenominator(boundsOfFirstTile, 
                                new Dimension(512, 512), // todo: get from somewhere
                                Display.getDefault().getDPI().x);
                }
            }
            
    
            @Override
            public int calculateMaxTilePerColNumber(int zoomLevel) {
                if (boundsOfFirstTile == null) return 0;

                return (int) Math.ceil(tiledGroupBounds.getHeight() / boundsOfFirstTile.getHeight());
            }

            @Override
            public int calculateMaxTilePerRowNumber(int zoomLevel) {
                if (boundsOfFirstTile == null) return 0;
                
                return (int) Math.ceil(tiledGroupBounds.getWidth() / boundsOfFirstTile.getWidth());
            } 

            public double getScale() {
                return scale;
            }
            
            public double getWidthInWorldUnits() {
                return boundsOfFirstTile.getWidth();
            }
            
            public double getHeightInWorldUnits() {
                return boundsOfFirstTile.getHeight();
            }
            
            public int compareTo(NASAZoomLevel other) {
                return Double.compare(scale, other.scale);
            }
        }
        
    }
}
