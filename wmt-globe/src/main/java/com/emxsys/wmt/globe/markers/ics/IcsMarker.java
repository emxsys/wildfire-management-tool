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

import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.marker.Marker;
import com.emxsys.wmt.globe.markers.BasicMarker;
import com.emxsys.wmt.globe.markers.MarkerSupport;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class extends BasicMarker to provide a set of ICS symbols. It includes a service provider
 * for creating IcsMarker instances from XML Elements, plus an editor for editing the marker
 * attributes.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @see BasicMarker
 * @see MarkerFactory
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
        this("Marker", GeoCoord3D.INVALID_POSITION);
    }

    public IcsMarker(String name, Coord3D location) {
        super(location);
        setName(name);
        PointPlacemark placemark = getLookup().lookup(PointPlacemark.class);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);  // CLAMP_TO_GROUND, RELATIVE_TO_GROUND or ABSOLUTE
        overrideDefaultAttributes();
    }

    private void overrideDefaultAttributes() {
        Preferences pref = NbPreferences.forModule(getClass());
        double scale = pref.getDouble("ics_marker.scale", 1.0);
        double imageOffsetX = pref.getDouble("ics_marker.image_offset_x", 0.5);
        double imageOffsetY = pref.getDouble("ics_marker.image_offset_y", 0.0);
        double labelOffsetX = pref.getDouble("ics_marker.label_offset_x", 0.9);
        double labelOffsetY = pref.getDouble("ics_marker.label_offset_y", 0.6);

        PointPlacemark placemark = getLookup().lookup(PointPlacemark.class);
        PointPlacemarkAttributes attributes = placemark.getAttributes();
        attributes.setScale(scale);
        attributes.setImageOffset(new Offset(imageOffsetX, imageOffsetY, AVKey.FRACTION, AVKey.FRACTION));
        attributes.setLabelOffset(new Offset(labelOffsetX, labelOffsetY, AVKey.FRACTION, AVKey.FRACTION));
    }

    @Override
    public void edit() {
        // Create the editor panel wrapped in a standard dialog...
        IcsMarkerEditor.edit(this, false);
    }

    /**
     * Gets the class responsible for creating Pushpins from XML Elements.
     *
     * @return IcsMarkerFactory.class.
     */
    @Override
    public Class<IcsMarkerFactory> getFactoryClass() {
        return IcsMarkerFactory.class;
    }

    /**
     * Gets a factory for IcsMarker objects.
     *
     * @return a IcsMarkerFactory instance
     * @see IcsMarkerFactory
     */
    public static Factory getFactory() {
        return new IcsMarkerFactory();
    }

    /**
     * Factory for creating IcsMarkers. DataObjects will query for Marker.Factory with the
     * ATTR_PROVIDER class to find the appropriate XML encoder/decoder.
     *
     * Note: Ensure this is a 'static' inner class so that it can be instantiated by the service
     * provider framework.
     *
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    @ServiceProvider(service = Marker.Factory.class)
    public static class IcsMarkerFactory extends BasicMarker.MarkerFactory {

        // See package-info.java for the declaration of the IcsMarkerTemplate
        private static final String TEMPLATE_CONFIG_FILE = "Templates/Marker/IcsMarkerTemplate.xml";
        private static DataObject template;
        private static final Logger logger = Logger.getLogger(IcsMarkerFactory.class.getName());

        public IcsMarkerFactory() {
            try {
                template = DataObject.find(FileUtil.getConfigFile(TEMPLATE_CONFIG_FILE));
            } catch (DataObjectNotFoundException ex) {
                logger.severe(ex.toString());
            }
        }

        /**
         *
         * @return A new IcsMarker instance.
         */
        @Override
        public Marker newMarker() {
            return new IcsMarker();
        }

        /**
         * Creates a DataObject in the supplied folder using the supplied IcsMarker instance for
         * DataObject contents.
         *
         * @param marker assigned to the DataObject
         * @param folder where the DataObject will be created, uses the current project if null
         * @return a BasicMarkerDataObject
         */
        @Override
        public DataObject createDataObject(Marker marker, FileObject folder) {
            if (marker instanceof IcsMarker) {
                return MarkerSupport.createBasicMarkerDataObject((IcsMarker) marker, folder, template);
            } else {
                throw new IllegalArgumentException("createBasicMarkerDataObject: marker argument must be a IcsMarker, not a " + marker.getClass().getName());
            }
        }
    }
}
