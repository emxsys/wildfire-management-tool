/*
 * Copyright (c) 2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.markers.pushpins;

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.marker.Marker;
import com.emxsys.wmt.globe.markers.BasicMarker;
import com.emxsys.wmt.globe.markers.MarkerSupport;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.lookup.ServiceProvider;

/**
 * Pushpin implements the Emxsys {@link Marker} GIS interface, which is backed by a WorldWind
 * {@link PointPlacemark} implementation.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class Pushpin extends BasicMarker {

    private static final Logger logger = Logger.getLogger(Pushpin.class.getName());

    Pushpin() {
        super();
    }

    public Pushpin(String name, Coord3D location) {
        super(location);
        setName(name);
    }

    @Override
    public void edit() {
        // Create the editor panel wrapped in a standard dialog...
        PushpinEditor.edit(this, false);
    }

    /**
     * Gets the class responsible for creating Pushpins from XML Elements.
     *
     * @return PushpinFactory.class.
     */
    @Override
    public Class<PushpinFactory> getFactoryClass() {
        return PushpinFactory.class;
    }

    /**
     * Gets a factory for Pushpin objects.
     *
     * @return a PushpinFactory instance
     * @see PushpinFactory
     */
    public static Factory getFactory() {
        return new PushpinFactory();
    }

    /**
     * Note to self: Ensure this is a 'static' inner class so that it can be instantiated by the
     * service provider framework. DataObjects will query for Marker.Factory with the ATTR_PROVIDER
     * class to find the appropriate XML encoder/decoder. Called by Pushpin.newInstance.
     *
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    @ServiceProvider(service = Marker.Factory.class)
    public static class PushpinFactory implements Marker.Factory {

        private static final String TEMPLATE_CONFIG_FILE = "Templates/Marker/PushpinMarkerTemplate.xml";
        private static DataObject template;
        private static final Logger logger = Logger.getLogger(PushpinFactory.class.getName());

        public PushpinFactory() {
            try {
                template = DataObject.find(FileUtil.getConfigFile(TEMPLATE_CONFIG_FILE));
            } catch (DataObjectNotFoundException ex) {
                logger.log(Level.SEVERE, "PushpinFactory() could not find template: {0}", TEMPLATE_CONFIG_FILE);
            }
        }

        /**
         *
         * @return a new Pushpin instance.
         */
        @Override
        public Marker newMarker() {
            return new Pushpin();
        }

        /**
         * Creates a DataObject that represents the supplied Marker.
         *
         * @param marker to be assigned to the DataObject
         * @param folder where to create the DataObject, uses the current project if null
         * @return a BasicMarkerDataObject
         */
        @Override
        public DataObject createDataObject(Marker marker, FileObject folder) {
            if (marker instanceof Pushpin) {
                return MarkerSupport.createBasicMarkerDataObject((Pushpin) marker, folder, template);
            } else {
                throw new IllegalArgumentException("createBasicMarkerDataObject: marker argument must be a Pushpin, not a " + marker.getClass().getName());
            }
        }
    }
}
