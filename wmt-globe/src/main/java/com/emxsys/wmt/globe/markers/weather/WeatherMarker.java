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
package com.emxsys.wmt.globe.markers.weather;

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.gis.api.marker.Marker;
import com.emxsys.wmt.globe.markers.BasicMarker;
import com.emxsys.wmt.globe.markers.BasicMarkerBuilder;
import com.emxsys.wmt.globe.markers.BasicMarkerWriter;
import com.emxsys.wmt.globe.util.Positions;
import com.emxsys.wmt.util.HttpUtil;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.BalloonAttributes;
import gov.nasa.worldwind.render.BasicBalloonAttributes;
import gov.nasa.worldwind.render.GlobeBrowserBalloon;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Size;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.w3c.dom.Document;
import visad.Field;

/**
 * This class extends BasicMarker to provide Weather data and symbols.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @see BasicMarker
 * @see Marker
 */
@Messages(
        {
            "CTL_WeatherDialogTitle=Edit Weather Marker",
            "ERR_ViewerNotFound=WorldWindPanel not found."
        })
public class WeatherMarker extends BasicMarker {

    private final Field field;
    private final String moreInfoLink;
    private GlobeBrowserBalloon balloon;
    private static final Logger logger = Logger.getLogger(WeatherMarker.class.getName());

    WeatherMarker() {
        this("Wx Marker", GeoCoord3D.INVALID_POSITION, null, null);
    }

    public WeatherMarker(String name, Coord3D location) {
        this(name, location, null, null);
    }

    public WeatherMarker(String name, Coord3D location, Field field, URL moreInfo) {
        super(location);
        setName(name);
        this.field = field;
        this.moreInfoLink = null;//moreInfo.toString();

        // Get the implementation from the super class' lookup
        PointPlacemark placemark = getLookup().lookup(PointPlacemark.class);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);  // CLAMP_TO_GROUND, RELATIVE_TO_GROUND or ABSOLUTE

        PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
        attrs.setLabelColor("ffffffff");
        attrs.setLineColor("ff0000ff");
        attrs.setUsePointAsDefaultImage(true);
        attrs.setScale(5d);
        placemark.setAttributes(attrs);
        overrideDefaultAttributes();
    }

    private void overrideDefaultAttributes() {
        Preferences pref = NbPreferences.forModule(getClass());
        double scale = pref.getDouble("weather_marker.scale", 1.0);
        double imageOffsetX = pref.getDouble("weather_marker.image_offset_x", 0.5);
        double imageOffsetY = pref.getDouble("weather_marker.image_offset_y", 0.0);
        double labelOffsetX = pref.getDouble("weather_marker.label_offset_x", 0.9);
        double labelOffsetY = pref.getDouble("weather_marker.label_offset_y", 0.6);

        PointPlacemark placemark = getLookup().lookup(PointPlacemark.class);
        PointPlacemarkAttributes attributes = placemark.getAttributes();
        attributes.setScale(scale);
        attributes.setImageOffset(new Offset(imageOffsetX, imageOffsetY, AVKey.FRACTION, AVKey.FRACTION));
        attributes.setLabelOffset(new Offset(labelOffsetX, labelOffsetY, AVKey.FRACTION, AVKey.FRACTION));
    }

    @Override
    public void edit() {
        if (balloon == null) {
            makeBrowserBalloon();
        }
        if (balloon != null) {
            balloon.setVisible(true);
        }
    }

    protected void makeBrowserBalloon() {

        String htmlString = HttpUtil.callWebService(moreInfoLink);
        if (htmlString == null) {
            return;
        }
        this.balloon = new GlobeBrowserBalloon(htmlString, Positions.fromCoord3D(getPosition()));

        // Size the balloon to provide enough space for its content.
        BalloonAttributes attrs = new BasicBalloonAttributes();
        attrs.setSize(new Size(Size.NATIVE_DIMENSION, 0d, null, Size.NATIVE_DIMENSION, 0d, null));
        balloon.setAttributes(attrs);

        // Add the renderable to the Marker
        getInstanceContent().add(balloon);
    }

    /**
     * Gets the class responsible for creating WeatherMarkers from XML Elements.
     *
     * @return WeatherMarker.Builder.class.
     */
    @Override
    public Class<WeatherMarker.Builder> getFactoryClass() {
        return WeatherMarker.Builder.class;
    }

    /**
     * Gets a factory for WeatherMarker objects.
     *
     * @return a WeatherMarkerFactory instance
     * @see WeatherMarkerFactory
     */
    @Deprecated
    public static Builder getFactory() {
        return new WeatherMarker.Builder();

    }

    /**
     * Builder for creating WeatherMarkers. DataObjects will query for Marker.Builder with the
 ATTR_PROVIDER class to find the appropriate XML encoder/decoder.

 Note: Ensure this is a 'static' inner class so that it can be instantiated by the service
 provider framework.
     *
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    public static class Builder extends BasicMarkerBuilder {

        // See package-info.java for the declaration of the IcsMarkerTemplate
        private static final String TEMPLATE_CONFIG_FILE = "Templates/Marker/IcsMarkerTemplate.xml";
        private static DataObject template;
        private static final Logger logger = Logger.getLogger(Builder.class.getName());

        public Builder() {
        }

        public Builder(String name, Coord3D coord) {
            super(coord);
            name(name);
        }

        public Builder(Document sourceDoc) {
            super(sourceDoc);
        }

        @Override
        public Marker build() {
            if (getDocument() == null) {
                return new WeatherMarker(super.name, super.coord);
            } else {               
                return initializeFromXml(new WeatherMarker());
            }
        }

        @Override
        protected BasicMarker initializeFromXml(BasicMarker marker) {
            return super.initializeFromXml(marker); //To change body of generated methods, choose Tools | Templates.
        }
  
    }

    /**
     * Writer class.
     */
    public static class Writer extends BasicMarkerWriter {

        // See package-info.java for the declaration of the WeatherMarkerTemplate
        private static final Logger logger = Logger.getLogger(Writer.class.getName());
        private static final String TEMPLATE_CONFIG_FILE = "Templates/Marker/WeatherMarkerTemplate.xml";
        private static DataObject template;

        /**
         * Called by super.createDataObject().
         * @return
         */
        @Override
        protected DataObject getTemplate() {
            if (template == null) {
                try {
                    template = DataObject.find(FileUtil.getConfigFile(TEMPLATE_CONFIG_FILE));
                } catch (DataObjectNotFoundException ex) {
                    logger.log(Level.SEVERE, "getTemplate() cannot find: {0}", TEMPLATE_CONFIG_FILE);
                }
            }
            return template;
        }

    }
}
