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
package com.emxsys.wmt.globe.markers.ics;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.marker.Marker;
import com.emxsys.wmt.globe.markers.BasicMarker;
import com.emxsys.wmt.globe.markers.AbstractMarkerBuilder;
import com.emxsys.wmt.globe.markers.AbstractMarkerWriter;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.w3c.dom.Document;

/**
 * This class extends BasicMarker to provide a set of ICS symbols. It includes a service provider
 * for creating IcsMarker instances from XML Elements, plus an editor for editing the marker
 * attributes.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @see BasicMarker
 * @see Builder
 * @see Marker
 */
@Messages(
        {
            "CTL_PushpinDialogTitle=Edit Marker",
            "ERR_ViewerNotFound=WorldWindPanel not found."
        })
public class IcsMarker extends BasicMarker {

    private static final Logger logger = Logger.getLogger(IcsMarker.class.getName());

    IcsMarker() {
        this("Marker", GeoCoord3D.INVALID_COORD);
    }

    public IcsMarker(String name, Coord3D location) {
        super(location);
        setName(name);
        PointPlacemark placemark = getLookup().lookup(PointPlacemark.class);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);  // CLAMP_TO_GROUND, RELATIVE_TO_GROUND or ABSOLUTE
        overrideDefaultAttributes();
        // Add persistance capability
        getInstanceContent().add(new Writer(this));        
    }

    private void overrideDefaultAttributes() {
        Preferences pref = NbPreferences.forModule(getClass());
        double scale = pref.getDouble("ics_marker.scale", 1.0);
        double imageOffsetX = pref.getDouble("ics_marker.image_offset_x", 0.5);
        double imageOffsetY = pref.getDouble("ics_marker.image_offset_y", 0.0);
        double labelOffsetX = pref.getDouble("ics_marker.label_offset_x", 0.9);
        double labelOffsetY = pref.getDouble("ics_marker.label_offset_y", 0.6);

        PointPlacemark placemark = getLookup().lookup(PointPlacemark.class);
        PointPlacemarkAttributes attrs = new PointPlacemarkAttributes(placemark.getAttributes());
        attrs.setUsePointAsDefaultImage(false);
        attrs.setScale(scale);
        attrs.setImageOffset(new Offset(imageOffsetX, imageOffsetY, AVKey.FRACTION, AVKey.FRACTION));
        attrs.setLabelOffset(new Offset(labelOffsetX, labelOffsetY, AVKey.FRACTION, AVKey.FRACTION));
        placemark.setAttributes(attrs);
        
    }

    @Override
    public void edit() {
        // Create the editor panel wrapped in a standard dialog...
        IcsMarkerEditor.edit(this, false);
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
     * Builder for creating IcsMarkers. DataObjects will query for Marker.Builder with the
     * ATTR_PROVIDER class to find the appropriate XML encoder/decoder.
     *
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    public static class Builder extends AbstractMarkerBuilder {

        public Builder() {
        }

        public Builder(Document doc) {
            super(doc);
        }

        @Override
        public Marker build() {
            BasicMarker marker = new IcsMarker();
            if (getDocument() != null) {
                marker = initializeFromXml(marker);
            }
            return initializeFromParameters(marker);
        }

        @Override
        protected BasicMarker initializeFromXml(BasicMarker marker) {
            // Let the base class handle the heavy lifting.
            return super.initializeFromXml(marker); 
        }

    }

    /**
     * The IcsMarker.Writer is responsible for writing a marker to a persistent store.
     *
     * @author Bruce Schubert
     */
    public static class Writer extends AbstractMarkerWriter {

        // See package-info.java for the declaration of the IcsMarkerTemplate
        private static final String TEMPLATE_CONFIG_FILE = "Templates/Marker/IcsMarkerTemplate.xml";
        private static DataObject template;
        private static final Logger logger = Logger.getLogger(Writer.class.getName());

        public Writer(BasicMarker marker) {
            super(marker);
        }

        
        /**
         * Called by super.createDataObject().
         * @return A template file used for writing new IcsMarkers.
         */
        @Override
        protected DataObject getTemplate() {
            if (template == null) {
                try {
                    template = DataObject.find(FileUtil.getConfigFile(TEMPLATE_CONFIG_FILE));
                } catch (DataObjectNotFoundException ex) {
                    logger.log(Level.SEVERE, "IcsMarker.Writer.getTemplate() cannot find: {0}", TEMPLATE_CONFIG_FILE);
                }
            }
            return template;
        }

    }
}
