/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.emxsys.wmt.landfire.layers;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.capabilities.QueryableByPoint;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.query.BasicQueryResult;
import com.emxsys.gis.api.query.QueryResult;
import com.emxsys.gis.api.viewer.Viewers;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.w3c.dom.Element;


/**
 * LandfireTiledImageLayer abstract class.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class LandfireTiledImageLayer extends WMSTiledImageLayer implements GisLayer,
                                                                                    QueryableByPoint {
    /** The lookup stores capabilities and meta-data. */
    protected Lookup lookup;
    protected InstanceContent content = new InstanceContent();

    /** The active flag determines whether the layer undergoes a color key lookup */
    private boolean active = false;

    /** The colorMap associates a pixel color with an object */
    private HashMap<Color, Object> colorMap = new HashMap<>();

    /** lastPosition is used to determine if the position of interest has moved. */
    private Position lastPosition;

    /** Decoder that decodes a color into a code. May be replaced by sub-classes */
    protected ColorDecoder decoder = new ColorDecoder();

    /** Decoder object that runs in a worker thread. */
    protected ColorDecoder threadedDecoder = new ColorDecoder();

    /** Queue of positions to be decoded. */
    private BlockingDeque<DecoderArgs> deque = new LinkedBlockingDeque<>();
    private EventListenerList listenerList = new EventListenerList();

    private static final Logger LOG = Logger.getLogger(LandfireTiledImageLayer.class.getName());

    /**
     * Constructor called by the {@code LandfireTiledImageLayerFactory} which processes a WorldWind
     * xml configuration file (not to be confused with a NetBeans XML layer file).
     *
     * @param domElement
     * @param params
     */
    public LandfireTiledImageLayer(Element domElement, AVList params) {
        super(domElement, params);
    }

    /**
     * Gets a lookup containing this layer's implementation.
     *
     * @return a lookup containing the implementation object(s).
     */
    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            // Add the GisLayer implemenation to the lookup
            this.content.add(this);
            this.lookup = new AbstractLookup(content);
        }
        return lookup;
    }

    /**
     * Whenever this layer is rendered, we will sample the pixel color at the center and update our
     * lookup with the value under the cross hairs.
     *
     * @param dc
     */
    @Override
    public void render(DrawContext dc) {
        super.render(dc);
        if (active) {
            lookupColorAtCenter(dc);
        }
    }

    /**
     * Controls the display of the map layer in a viewer.
     *
     * @param enabled the new state.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        LandfireTiledImageLayer.this.firePropertyChange(AVKey.LAYER, null, this);
        Viewers.refreshViewersContaining(this);
    }

    /**
     * Initialize the pixel key/value color map from a CSV file.
     *
     * @param csvUrl CSV file.
     */
    protected abstract void initColorMap(URL csvUrl);

    /**
     * Add a key/value to the color map.
     *
     * @param key pixel color
     * @param value the object
     */
    protected void addColorEntry(Color key, Object value) {
        // update the color-to-value map
        this.colorMap.put(key, value);
    }

    /**
     * @return the key/value color map.
     */
    protected HashMap<Color, Object> getColorMap() {
        return colorMap;
    }

    /**
     * Called in the construction process, this method initializes the layer properties from the XML
     * layer file.
     *
     * @param fo a file object in the WorldWind/Layers folder.
     */
    protected void updateLayerProperties(FileObject fo) {
        // Set the title to the localized name if available
        String displayName = (String) fo.getAttribute("displayName");
        this.setName(displayName == null ? (String) fo.getAttribute("name") : displayName);

        // Set the layer's active state - which controls whether the color
        // key/value lookup is performed
        Boolean activate = (Boolean) fo.getAttribute("active");
        if (activate == null) {
            active = false;
        } else {
            active = activate;
        }

        // Set the layer's enabled state - the default is enabled
        String actuate = (String) fo.getAttribute("actuate");
        if (actuate == null) {
            setEnabled(true); //
        } else {
            setEnabled(actuate.contentEquals("onLoad"));
        }

        // Set the layer's opacity state - the default is enabled
        Double opacity = (Double) fo.getAttribute("opacity");
        if (null == opacity) {
            setOpacity(1.0);
        } else {
            setOpacity(opacity);
        }
    }

    /**
     * Decodes the color at the lat/lon.
     *
     * @param point represents the lat/lon.
     * @return an object that represents the color at the point.
     */
    @Override
    public QueryResult<?> getObjectsAtLatLon(Coord2D point) {
        DecoderArgs args = new DecoderArgs(Position.fromDegrees(
            point.getLatitudeDegrees(),
            point.getLongitudeDegrees()));
        return new BasicQueryResult<>(this.decoder.lookupObjectAtPosition(args));
    }

    /**
     * Determine the pixel color of this layer at the viewport center. Called by render() when the
     * location changes.
     *
     * @param dc supplied by the render method.
     */
    private void lookupColorAtCenter(DrawContext dc) {
        lookupColorAtPosition(dc.getViewportCenterPosition());
    }

    /**
     * Determine the pixel color of this layer at the supplied position. The result is posted into
     * the instance content and a property change event is fired.
     *
     * @param globe
     * @param position where the pixel color should be sampled.
     */
    private void lookupColorAtPosition(Position position) {

        // Preempt processing the same position over and over
        if (position == null || position.equals(lastPosition)) {
            return;
        }
        // Rememeber the last position between calls
        lastPosition = position;

        // Push this position into the processing deque
        deque.add(new DecoderArgs(position));

        // Start the processing if required.
        if (!threadedDecoder.isAlive()) {
            threadedDecoder.start();
        }
    }


    /**
     * POD for packaging arguments for the worker thread.
     */
    class DecoderArgs {
        Position position;
        boolean shouldExit;

        /**
         * Decode the pixel color at the position.
         *
         * @param position to be evaluated
         */
        DecoderArgs(Position position) {
            this.shouldExit = false;
            this.position = position;
        }

        /**
         * EXIT Condition
         */
        DecoderArgs() {
            this.shouldExit = true;
            this.position = null;
        }
    }
    final DecoderArgs EXIT_CONDITION = new DecoderArgs();


    /**
     * Color lookup object
     *
     *
     *
     */
    protected class ColorDecoder extends Thread {
        private boolean done = false;
        private Sector tileSector;
        private BufferedImage tileImage;
        private Color lastColor;
        private Object lastColorKeyValue;
        private final LinkedList<DecoderArgs> list = new LinkedList<>();

        ColorDecoder() {
            super("Landfire:ColorDecoder");
        }

        Object lookupObjectAtPosition(DecoderArgs args) {
            try {
                updateSectorAndImageFromTile(args);
            }
            catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            Color color = decoder.getColorFromTileAtPosition(args.position);
            if (color != null) {
                return lookupColor(color);
            }
            return null;
        }

        /**
         * Processes the requests for retrieving a color key's value at from the queued positions.
         */
        @Override
        public void run() {
            while (!this.done) {
                try {
                    // Empty the deque into our container and process
                    deque.drainTo(list);
                    consumePositions(list);

                    // Deque is empty: wait for a new entry and process on arrival
                    list.add(deque.takeLast());
                    consumePositions(list);

                }
                catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, "thread interrupted: {0}", ex.toString());
                    this.done = true;
                }
            }
        }

        /**
         * Processes the most recently posted request; ignores and clears earlier enter entries.
         *
         * @param list the positions to be processed.
         */
        private void consumePositions(LinkedList<DecoderArgs> list) {
            if (list.isEmpty()) {
                return;
            }
            // Test for exit condition, established by a poison entry in the list
            for (DecoderArgs args : list) {
                if (args.shouldExit) {
                    // Set flag to exit run loop
                    this.done = true;
                    return;
                }
            }
            // Process the most recent entry (the last one)
            processPosition(list.getLast());
            list.clear();
        }

        /**
         * Process the decode the color at the supplied position and notify listeners of changes.
         *
         * @param args arguments associated with the position.
         */
        private void processPosition(DecoderArgs args) {
            if (args == null) {
                return;
            }

            try {
                updateSectorAndImageFromTile(args);

                // Sample the color
                if (this.tileImage != null) {
                    Color color = getColorFromTileAtPosition(args.position);
                    if (color == null) {
                        return;
                    }
                    if (this.lastColor != null && color.equals(this.lastColor)) {
                        return;
                    }

                    // There was a change!
                    Object colorKeyValue = lookupColor(color);
                    if (colorKeyValue != null) {
                        // Update the dynamic lookup content so that it contains
                        // the value associated with the new color key
                        // TODO: Should access to content be synchronized?
                        if (this.lastColorKeyValue != null) {
                            content.remove(this.lastColorKeyValue);
                        }
                        content.add(colorKeyValue);

                        // Notify listeners of new colorkey value
                        PropertyChangeEvent pce
                            = new PropertyChangeEvent(LandfireTiledImageLayer.this, "DATAVALUE", lastColorKeyValue, colorKeyValue);
                        firePropertyChangeEvent(pce);

                        this.lastColorKeyValue = colorKeyValue;
                        this.lastColor = color;
                    }

                }
            }
            catch (Exception ex) {
                LOG.log(Level.SEVERE, "processPosition() failed! The {0} layer is not available. {1}",
                    new Object[]{
                        LandfireTiledImageLayer.this.getName(), ex.getMessage()
                    });
            }
        }

        /**
         * Update the sector and image properties to from the texture tile at supplied position.
         *
         * @param args the position and associated arguments.
         * @throws Exception
         */
        private void updateSectorAndImageFromTile(DecoderArgs args) throws Exception {
            // Get a new image if we don't have one cached 
            if (this.tileSector == null || !this.tileSector.contains(args.position)) {
                if (getLevels().getLastLevel().getFormatSuffix().endsWith("dds")) {
                    throw new RuntimeException("The local file cache contains .dds files which cannot be converted to images. "
                        + "See the map's config file's FormatSuffix settings.");
                }
                TextureTile tile = getTileContainingLatLon(args.position);
                this.tileSector = tile.getSector();
                try {

                    this.tileImage = getImage(tile, "image/png", 20000);
                }
                catch (Exception e) {
                    this.tileImage = null;
                    throw e;
                }

                if (tileImage == null) {
                    System.out.println("LANDFIRE image is null!");
                }
            }
        }

        /**
         * Get pixel color from the tile image at the supplied position.
         *
         * @param position the position to lookup.
         */
        private Color getColorFromTileAtPosition(Position position) {
            // Interpolate lat/lon within sector
            double x0 = this.tileSector.getMinLongitude().degrees;
            double xf = position.getLongitude().degrees - x0;
            double xn = xf / this.tileSector.getDeltaLonDegrees();

            double y0 = this.tileSector.getMinLatitude().degrees;
            double yf = position.getLatitude().degrees - y0;
            double yn = yf / this.tileSector.getDeltaLatDegrees();

            // Sample the pixel color at the lat/lon offset
            int x = (int) Math.floor((this.tileImage.getWidth() - 1) * xn);
            int y = (int) Math.floor((this.tileImage.getHeight() - 1) * yn);
            y = (this.tileImage.getHeight() - 1) - y;
            int argb = this.tileImage.getRGB(x, y);

            return new Color(argb);
        }

        /**
         * Lookup the color value - examine nearby colors for a fuzzy lookup.
         *
         * @param color the color to lookup in the color map
         * @returns the Object represented by the color
         */
        protected Object lookupColor(Color color) {
            // Lookup the the original color
            Object value = getColorMap().get(color);
            if (value != null) {
                return value;
            }

            // Fuzzy lookup: Loop thru adjacent colors
            for (int r = -2; r <= 2; r++) {
                for (int g = -2; g <= 2; g++) {
                    for (int b = -2; b <= 2; b++) {
                        int red = Math.min(Math.max(color.getRed() + r, 0), 255);
                        int grn = Math.min(Math.max(color.getGreen() + g, 0), 255);
                        int blu = Math.min(Math.max(color.getBlue() + b, 0), 255);
                        Color fuzzyColor = new Color(red, grn, blu);

                        // Lookup the colorkey value (e.g. fuel model, vegetation height, etc.)
                        Object obj = getColorMap().get(fuzzyColor);
                        if (obj != null) {
                            // Add the original color to the color map to speed up future lookups
                            getColorMap().put(color, obj);

                            return obj;
                        }
                    }
                }
            }
            // We didn't break out of the loop: the color is not in the color table
            LOG.log(Level.WARNING, "Image Color =={0} Value Not Found!", color.toString());
            getColorMap().put(color, null);
            return null;
        }
    }

    /**
     * Gets the texture tile that contains the supplied latLon
     *
     * @param latLon the position
     * @return the texture tile containing the lat/lon.
     */
    private TextureTile getTileContainingLatLon(LatLon latLon) {
        int targetLevelNum = getLevels().getLastLevel().getLevelNumber();
        TileKey key = new TileKey(latLon.latitude, latLon.longitude, getLevels(), targetLevelNum);
        Sector tileSector = this.levels.computeSectorForKey(key);
        return new TextureTile(tileSector, getLevels().getLastLevel(), key.getRow(), key.getColumn());
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class, listener);
        super.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerList.remove(PropertyChangeListener.class, listener);
        super.removePropertyChangeListener(listener);
    }

    public void firePropertyChangeEvent(PropertyChangeEvent event) {
        PropertyChangeListener[] listeners = listenerList.getListeners(PropertyChangeListener.class);
        for (PropertyChangeListener eventListener : listeners) {
            eventListener.propertyChange(event);
        }
        super.firePropertyChange(event);
    }
}
