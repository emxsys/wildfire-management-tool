/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.layers;

import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * This utility class provides a factory for creating WorldWind layers from external configuration
 * files.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: LayerFactory.java 623 2013-05-09 23:04:08Z bdschubert $
 */
public final class LayerFactory {

    public static final String LAYER_INSTANCE_FILE_KEY = "com.emxsys.worldwind.layers.instanceFile";
    public static final String ACTUATE_ATTR = "actuate";
    public static final String CONFIG_ATTR = "config";
    public static final String DISPLAY_NAME_ATTR = "displayName";
    public static final String INSTANCE_CLASS_ATTR = "instanceClass";
    public static final String NAME_ATTR = "name";
    public static final String OPACITY_ATTR = "opacity";
    public static final String EXPIRATION_AGE_ATTR = "expirationAgeSeconds";
    public static final String REFRESH_INTERVAL_ATTR = "refreshIntervalSeconds";
    /** A WW factory for creating layers from an external source */
    private static final BasicLayerFactory factory = new BasicLayerFactory();
    private static final Logger logger = Logger.getLogger(LayerFactory.class.getName());

    static {
        logger.setLevel(null/*Level.ALL*/);
    }

    /**
     * Hidden constructor
     */
    private LayerFactory() {
    }

    /**
     * Factory method referenced in XML layer instanceCreate that creates a WorldWind layer from an
     * external WorldWind configuration file that is specified in the the FileObject parameter's
     * "config" attribute.
     *
     * The WorldWind configuration files are typically found in or below the application's
     * modules/ext folder. Your module can supply configuration files by placing them into the
     * project's release/modules/ext folder hierarchy.
     * 
     * @param instanceFile
     * @return 
     */
    public static Layer createLayer(FileObject instanceFile) {
        // Determine whether we're initializing from a config file or a class
        boolean useExternalConfig = instanceFile.getAttribute(CONFIG_ATTR) != null;
        Layer layer = useExternalConfig
                ? createLayerFromExternalConfig(instanceFile)
                : createLayerFromClass(instanceFile);

        // Update the layer properties
        if (layer != null) {
            updateLayerFromFileAttributes(layer, instanceFile);
        }
        return layer;
    }

    /**
     * Sets Layer properties from attributes in the instance file.
     *
     * @param layer The layer to update.
     * @param instanceFile The layer .instance file.
     */
    public static void updateLayerFromFileAttributes(Layer layer, FileObject instanceFile) {
        // Set the title to the localized name if available
        String displayName = (String) instanceFile.getAttribute(DISPLAY_NAME_ATTR);
        layer.setName(displayName == null ? (String) instanceFile.getAttribute(NAME_ATTR) : displayName);

        // Set the layer's enabled state - the default is enabled
        String actuate = (String) instanceFile.getAttribute(ACTUATE_ATTR);
        layer.setEnabled((actuate == null) ? true : actuate.contentEquals("onLoad"));

        // Set the layer's opacity state - the default is enabled
        Double opacity = (Double) instanceFile.getAttribute(OPACITY_ATTR);
        layer.setOpacity((opacity == null) ? 1.0 : opacity);

        // Set the layer's expiration time
        Integer refreshIntervalSecs = (Integer) instanceFile.getAttribute(REFRESH_INTERVAL_ATTR);
        Integer expirationAgeSecs = (Integer) instanceFile.getAttribute(EXPIRATION_AGE_ATTR);
        if (refreshIntervalSecs != null) {
            if (expirationAgeSecs == null) {
                expirationAgeSecs = 1;
            }
            RefreshService refreshService = new RefreshService(layer, expirationAgeSecs, refreshIntervalSecs);
            layer.setValue("RefreshService", refreshService);
        }
        // Store the FileObject in the layer so additional attributes can be queried later,
        // e.g., see WorldWindLayerAdapter.
        layer.setValue(LAYER_INSTANCE_FILE_KEY, instanceFile);
    }

    /**
     * Creates a layer via the factory and the external source defined in the instance file's
     * "config" attribute.
     *
     * @param instanceFile
     * @return
     * @throws IllegalStateException
     */
    private static Layer createLayerFromExternalConfig(FileObject instanceFile) throws
            IllegalStateException {
        // Return value.
        Layer layer = null;

        // Get the external WorldWind (WMS) layer configuration file.
        URL url = (URL) instanceFile.getAttribute(CONFIG_ATTR);
        if (url == null) {
            throw new IllegalStateException("No config attribute in " + FileUtil.getFileDisplayName(instanceFile));
        }

        // The nbinst protocol implies the external file is stored in the application's modules/ext 
        // folder hierarchy.
        if (url.getProtocol().contains("nbinst")) {
            // Use URLMapper to decode nbinst protocal into an absolute path
            FileObject cfgFile = URLMapper.findFileObject(url);
            if (cfgFile != null) {
                File diskFile = FileUtil.toFile(cfgFile);
                layer = (Layer) factory.createFromConfigSource(diskFile, null);
                logger.config(diskFile.toString());
            } else {
                logger.log(Level.WARNING, "No FileObject for {0}. ", url);
            }
        } // Otherwise, create the layer directly from the url, which may point to a file inside the 
        // worldwind.jar
        else {
            layer = (Layer) factory.createFromConfigSource(url, null);
            logger.config(url.toString());
        }
        return layer;
    }

    /**
     * Creates a layer from the instanceClass attribute.
     *
     * @param instanceFile instance file containing an instanceClass attribute.
     * @return the object returned by the Class.newInstance() method.
     */
    private static Layer createLayerFromClass(FileObject instanceFile) {
        String instanceClass = (String) instanceFile.getAttribute(INSTANCE_CLASS_ATTR);
        if (instanceClass == null) {
            throw new IllegalStateException("FileObject " + instanceFile.getName() + " instanceClass attribute is empty.");
        }
        try {
            Class<?> layerClass = Class.forName(instanceClass);
            if (!Layer.class.isAssignableFrom(layerClass)) {
                throw new IllegalStateException(layerClass.getName() + " is not compatible with Layer.class.");
            }
            Layer layer = (Layer) layerClass.newInstance();
            logger.config(instanceClass);

            return layer;
        } catch (ClassNotFoundException | IllegalStateException | InstantiationException | IllegalAccessException ex) {
            logger.severe(ex.getMessage());
            throw new IllegalStateException(ex);
        }
    }

    /**
     * A RefreshService. An instance of this class can be added to a Layer object to force its map
     * data to be periodically refreshed.
     */
    static class RefreshService {

        private final Timer timer = new Timer();

        /**
         * RefreshService constructor.
         * @param layer to refresh
         * @param expirationAge The expiration age of a layer tile, in seconds after initialization.
         * @param refreshPeriod The amount of time to wait before checking for expiration.
         */
        RefreshService(final Layer layer, final long expirationAge, final long refreshPeriod) {

            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // If greater than zero, the layer ignores and eliminates any previously cached 
                    // data older than the time specified, and requests new information from the data source.
                    layer.setExpiryTime(System.currentTimeMillis() + (expirationAge * 1000));
                    System.out.println(" Refreshing " + layer.getName() + ", expiration time: " + LocalDateTime.ofEpochSecond(layer.getExpiryTime(), 0, ZoneOffset.UTC));
                }
            }, 0, refreshPeriod * 1000);
        }
    }
}
