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
import java.awt.Image;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
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
@Messages({
    "CTL_WeatherDialogTitle=Edit Weather Marker",})
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

        // Replace the base class implementation
        overrideDefaultAttributes(placemark);
    }

    private void overrideDefaultAttributes(PointPlacemark placemark) {
        Preferences pref = NbPreferences.forModule(getClass());
        double scale = pref.getDouble("weather_marker.scale", 10.0);
        double imageOffsetX = pref.getDouble("weather_marker.image_offset_x", 0.0);
        double imageOffsetY = pref.getDouble("weather_marker.image_offset_y", 0.0);
        double labelOffsetX = pref.getDouble("weather_marker.label_offset_x", 0.9);
        double labelOffsetY = pref.getDouble("weather_marker.label_offset_y", 0.6);

        PointPlacemarkAttributes attrs = new PointPlacemarkAttributes(placemark.getAttributes());
        attrs.setLabelColor("ffffffff");
        attrs.setLineColor("ff0000ff");
        attrs.setScale(scale);
        attrs.setImageOffset(new Offset(imageOffsetX, imageOffsetY, AVKey.FRACTION, AVKey.FRACTION));
        attrs.setLabelOffset(new Offset(labelOffsetX, labelOffsetY, AVKey.FRACTION, AVKey.FRACTION));
        // We'll use a point for the WW symbol.
        attrs.setUsePointAsDefaultImage(true);

        placemark.setAttributes(attrs);
    }

    /**
     * @return An image for a Weather Marker Node tin the Projects window.
     */
    @Override
    public Image getImage() {
        URL imgUrl = getClass().getResource("sun_clouds.png");
        ImageIcon icon = new ImageIcon(imgUrl);
        return ImageUtilities.icon2Image(icon);
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
     * Builder for creating WeatherMarkers.
     *
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    public static class Builder extends BasicMarkerBuilder {

        public Builder(Document document) {
            super(document);
        }

        public Builder() {
        }

        @Override
        public Marker build() {
            BasicMarker marker = new WeatherMarker();
            if (getDocument() != null) {
                marker = initializeFromXml(marker);
            }
            return initializeFromParameters(marker);
        }

        @Override
        protected BasicMarker initializeFromXml(BasicMarker marker) {
            return super.initializeFromXml(marker);
        }

        @Override
        protected BasicMarker initializeFromParameters(BasicMarker marker) {
            return super.initializeFromParameters(marker);
        }

    }

    /**
     * Writer class for writing WeatherMarkers to persistent storage.
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
