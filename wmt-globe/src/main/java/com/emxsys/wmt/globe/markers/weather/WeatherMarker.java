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
import com.emxsys.wmt.globe.markers.AbstractMarkerBuilder;
import com.emxsys.wmt.globe.markers.AbstractMarkerWriter;
import static com.emxsys.wmt.globe.markers.AbstractMarkerWriter.MKR_PREFIX;
import com.emxsys.wmt.globe.markers.BasicMarker;
import com.emxsys.wmt.globe.markers.MarkerSupport;
import com.emxsys.wmt.globe.util.Positions;
import com.emxsys.wmt.weather.api.WeatherProvider;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Image;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import visad.Field;

/**
 * The WeatherMarker class extends BasicMarker to provide weather data and symbols.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @see BasicMarker
 */
@Messages({
    "CTL_WeatherDialogTitle=Edit Weather Marker",})
public class WeatherMarker extends BasicMarker {

    private final Field field;
    private WeatherProvider provider;
    private WeatherBalloon balloon;
    private Image image;
    private static final Logger logger = Logger.getLogger(WeatherMarker.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

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

        // Add persistance capability
        getInstanceContent().add(new Writer(this));

        // Get the implementation from the super class' lookup
        PointPlacemark placemark = getLookup().lookup(PointPlacemark.class);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);  // CLAMP_TO_GROUND, RELATIVE_TO_GROUND or ABSOLUTE

        // Replace the base class implementation
        overrideDefaultAttributes(placemark);

        // Must allocate all the renderables now, before the Marker is assigned to a Renderer.
        makeBrowserBalloon();
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
        // We'll use a point instead of an icon for the symbol.
        attrs.setUsePointAsDefaultImage(true);

        placemark.setAttributes(attrs);
    }

    public WeatherProvider getProvider() {
        return provider;
    }

    public void setProvider(WeatherProvider provider) {
        this.provider = provider;
        this.balloon.setProvider(provider);
    }

    /**
     * @return An image for a Weather Marker Node tin the Projects window.
     */
    @Override
    public Image getImage() {
        if (image == null) {
            URL imgUrl = getClass().getResource("sun_clouds.png");
            ImageIcon icon = new ImageIcon(imgUrl);
            image = ImageUtilities.icon2Image(icon);
        }
        return image;
    }

    @Override
    public void setPosition(Coord3D coord) {
        super.setPosition(coord);
        WeatherBalloon balloon = getLookup().lookup(WeatherBalloon.class);
        if (balloon != null && balloon.isVisible()) {
            balloon.setVisible(false);
        }
    }

    @Override
    public void edit() {

    }
    // TODO: implmement timer based weather query

    // TODO: implement wind barb symbol
    private void makeBrowserBalloon() {
        try {
            // Remove existing balloon
            if (balloon != null) {
                getInstanceContent().remove(balloon);
            }
            // Create new balloon
            balloon = new WeatherBalloon(Positions.fromCoord3D(getPosition()), provider);
            
            // Attach balloon to this marker
            if (balloon != null) {
                PointPlacemark placemark = getLookup().lookup(PointPlacemark.class);
                // The BalloonController looks for this value when an object is clicked.            
                placemark.setValue(AVKey.BALLOON, balloon);
                // The MarkerLayer will look for Renderables in the lookup
                getInstanceContent().add(balloon);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "makeBrowserBalloon() failed :{0}", e.getMessage());
        }
    }

    /**
     * Gets the class responsible for creating WeatherMarkers from XML Elements.
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
    public static class Builder extends AbstractMarkerBuilder {

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

        /**
         * Initializes the supplied marker with the WeatherProvider property from the XML.
         * @param marker The WeatherMarker to be initialized.
         * @return The updated WeatherMarker.
         */
        @Override
        protected BasicMarker initializeFromXml(BasicMarker marker) {
            // Let the base class do all the heavy lifting
            WeatherMarker wxMarker = (WeatherMarker) super.initializeFromXml(marker);

            // Now set the marker's WeatherProvider from the XML
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(MarkerSupport.getNamespaceContext());
            try {
                // Get all WeatherProviders in the document [0..1]
                String providerClass = xpath.evaluate("//"+MKR_PREFIX + ":" + "WeatherProvider", getDocument());
                if (providerClass == null || providerClass.isEmpty()) {
                    throw new RuntimeException("WeatherProvider is not defined in XML.");
                }
                // Look for the provider on the global lookup.
                Class<?> clazz = Class.forName(providerClass);
                Collection<? extends WeatherProvider> allProviders = Lookup.getDefault().lookupAll(WeatherProvider.class);
                for (WeatherProvider provider : allProviders) {
                    if (clazz.isInstance(provider)) {
                        // Found it! Set the marker's Wx Provider
                        wxMarker.setProvider(provider);
                        return wxMarker;
                    }
                }
                logger.log(Level.WARNING, "Could not find WeatherProvider for {0}", wxMarker.getName());
            } catch (XPathExpressionException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotFoundException ex) {
                logger.severe(ex.toString());
            } catch (IllegalStateException ex) {
                logger.warning(ex.toString());
            }
            return wxMarker;
        }

        @Override
        protected BasicMarker initializeFromParameters(BasicMarker marker) {
            return super.initializeFromParameters(marker);
        }

    }

    /**
     * Writer class for writing WeatherMarkers to persistent storage.
     */
    public static class Writer extends AbstractMarkerWriter {

        // See package-info.java for the declaration of the WeatherMarkerTemplate
        private static final Logger logger = Logger.getLogger(Writer.class.getName());
        private static final String TEMPLATE_CONFIG_FILE = "Templates/Marker/WeatherMarkerTemplate.xml";
        private static DataObject template;

        public Writer(BasicMarker marker) {
            super(marker);
        }

        /**
         * Adds the WeatherProvider node to the Marker node.
         * @return The updated Document.
         */
        @Override
        protected Document writeDocument() {
            Document doc = super.writeDocument();
            NodeList markers = doc.getElementsByTagNameNS(BASIC_MARKER_NS_URI, TAG_MARKER);
            if (markers.getLength() == 1) {
                Element mkr = (Element) markers.item(0);
                Element provider = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + "WeatherProvider");
                provider.appendChild(doc.createTextNode(((WeatherMarker) super.getMarker()).getProvider().getClass().getName()));
                mkr.appendChild(provider);
            }
            return doc;
        }

        /**
         * Called by super.createDataObject().
         * @return A DataObject for WeatherMarkerTemplate.xml
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
