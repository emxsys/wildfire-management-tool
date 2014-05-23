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

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.marker.Marker;
import com.emxsys.wmt.globe.markers.BasicMarker;
import com.emxsys.wmt.globe.markers.AbstractMarkerBuilder;
import com.emxsys.wmt.globe.markers.AbstractMarkerWriter;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.Document;

/**
 * Pushpin implements the Emxsys {@link Marker} GIS interface, which is backed by a WorldWind
 * {@link PointPlacemark} implementation.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class Pushpin extends BasicMarker {

    private static final Logger logger = Logger.getLogger(Pushpin.class.getName());

    Pushpin() {
        this("Pushpin", GeoCoord3D.INVALID_COORD);
    }

    public Pushpin(String name, Coord3D location) {
        super(location);
        setName(name);
        // Add persistance capability
        getInstanceContent().add(new Writer(this));        
    }

    @Override
    public void edit() {
        // Create the editor panel wrapped in a standard dialog...
        PushpinEditor.edit(this, false);
    }

    /**
     * Gets the class responsible for creating Pushpins from XML Elements.
     *
     * @return Builder.class.
     */
    @Override
    public Class<Builder> getFactoryClass() {
        return Builder.class;
    }

    /**
     * The Pushpin.Builder is responsible for building a new Pushpin instance from either the given
     * parameters or from the contents of an XML file.
     *
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    public static class Builder extends AbstractMarkerBuilder {

        public Builder() {
        }

        public Builder(Document document) {
            super(document);
        }

        @Override
        public Marker build() {
            BasicMarker marker = new Pushpin();
            if (getDocument() != null) {
                marker = initializeFromXml(marker);
            }
            return initializeFromParameters(marker);
        }

        @Override
        protected BasicMarker initializeFromXml(BasicMarker marker) {
            return super.initializeFromXml(marker);
        }

    }

    /**
     * The IcsMarker.Writer is responsible for writing a marker to a persistent store.
     *
     * @author Bruce Schubert
     */
    public static class Writer extends AbstractMarkerWriter {

        // See package-info.java for the declaration of the PushpinMarkerTemplate
        private static final String TEMPLATE_CONFIG_FILE = "Templates/Marker/PushpinMarkerTemplate.xml";
        private static DataObject template;
        private static final Logger logger = Logger.getLogger(Writer.class.getName());

        public Writer(BasicMarker marker) {
            super(marker);
        }

        
        /**
         * Called by super.createDataObject().
         * @return A template file used for new Pushpins.
         */
        @Override
        protected DataObject getTemplate() {
            if (template == null) {
                try {
                    template = DataObject.find(FileUtil.getConfigFile(TEMPLATE_CONFIG_FILE));
                } catch (DataObjectNotFoundException ex) {
                    logger.log(Level.SEVERE, "Pushpin.Writer.getTemplate() cannot find: {0}", TEMPLATE_CONFIG_FILE);
                }
            }
            return template;
        }
    }
}
