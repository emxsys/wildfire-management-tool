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

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.marker.Marker;
import com.emxsys.time.api.TimeEvent;
import com.emxsys.time.api.TimeListener;
import com.emxsys.time.api.TimeProvider;
import com.emxsys.time.spi.DefaultTimeProvider;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.weather.api.WeatherForecaster;
import com.emxsys.weather.api.WeatherModel;
import com.emxsys.weather.api.WeatherProvider;
import com.emxsys.weather.api.WeatherTuple;
import com.emxsys.wmt.globe.markers.AbstractMarkerBuilder;
import com.emxsys.wmt.globe.markers.AbstractMarkerWriter;
import static com.emxsys.wmt.globe.markers.AbstractMarkerWriter.MKR_PREFIX;
import com.emxsys.wmt.globe.markers.BasicMarker;
import com.emxsys.wmt.globe.markers.MarkerSupport;
import com.emxsys.wmt.globe.util.Positions;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.PreRenderable;
import java.awt.Image;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicReference;
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
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import visad.DateTime;
import visad.Field;
import visad.RealTuple;
import visad.VisADException;

/**
 * The WeatherMarker class extends BasicMarker to provide weather data and symbols.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @see BasicMarker
 */
@Messages({
    "CTL_WeatherDialogTitle=Edit Weather Marker",})
public class WeatherMarker extends BasicMarker {

    private static final Logger logger = Logger.getLogger(WeatherMarker.class.getName());
    private WeatherModel forecast;
    private Image image;
    private Placemark placemark;
    private WeatherProvider wxProvider;
    private WeatherBalloon balloon;
    private Loader loader = new Loader(this);
    private Updater updater = new Updater(this);
    private PreRenderable preRenderDelegate = new PreRenderable() {
        @Override
        public void preRender(DrawContext dc) {
//            if (dc != null && placemark.hasKey("DISPLAY_DATE") && dc.hasKey("DISPLAY_DATEINTERVAL")) {
//                Date displayDate = (Date) placemark.getValue("DISPLAY_DATE");
//                DateInterval displayDateInterval = (DateInterval) dc.getValue("DISPLAY_DATEINTERVAL");
//
//                if (displayDate != null && displayDateInterval != null) {
//                    //does this displayDate exist within the displayDateInterval?
//                    long displayDateMillis = displayDate.getTime();
//                    //return (displayDateMillis >= displayDateInterval.getStartMillis() && displayDateMillis <= displayDateInterval.getEndMillis()) ? true : false;
//                }
//            }
        }
    };

    /**
     * An Executor for processing TimeEvents in a sliding task.
     */
    private static class Updater implements TimeListener, Runnable {

        private static final RequestProcessor processor = new RequestProcessor(WeatherMarker.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<TimeEvent> lastEvent = new AtomicReference<>(new TimeEvent(this, null, null));
        private final WeatherMarker marker;

        Updater(WeatherMarker marker) {
            this.marker = marker;
        }

        @Override
        public void updateTime(TimeEvent evt) {
            // Sliding task: coallese the update events into fixed intervals
            this.lastEvent.set(evt);
            if (this.updatingTask.isFinished()) {
                this.updatingTask.schedule(1000);
            }
        }

        @Override
        public void run() {
            TimeEvent event = this.lastEvent.get();
            if (event == null) {
                return;
            }
            if (marker.forecast == null) {
                return;
            }
            WeatherTuple wx = marker.forecast.getWeather(event.getNewTime(), marker.getPosition());
            marker.placemark.setLabelText(wx.isMissing() ? "missing" : String.format("T: %1$.0f, RH: %2$.0f", 
                    wx.getAirTemperature().getValue(), wx.getRelativeHumidity().getValue()));
        }
    }

    /**
     * An Executor for loading weather forecasts.
     */
    private static class Loader implements Runnable {

        private static final RequestProcessor processor = new RequestProcessor(WeatherMarker.class);
        private final RequestProcessor.Task loadingTask = processor.create(this, true); // true = initiallyFinished
        private final WeatherMarker marker;

        Loader(WeatherMarker marker) {
            this.marker = marker;
        }

        public void loadWeatherForecast() {
            processor.post(this);
        }

        @Override
        public void run() {
            WeatherForecaster forecaster = marker.getProvider().getLookup().lookup(WeatherForecaster.class);
            if (forecaster == null) {
                logger.warning("No point forecaster available.");
                return;
            }
            marker.forecast = forecaster.getForecast(SpatialDomain.from(marker.getPosition()), null);
        }
    }

    WeatherMarker() {
        this("Wx Marker", GeoCoord3D.INVALID_COORD, null, null);
    }

    public WeatherMarker(String name, Coord3D location) {
        this(name, location, null, null);

    }

    public WeatherMarker(String name, Coord3D location, WeatherModel model, URL moreInfo) {
        super(location);
        setName(name);
        this.forecast = model;
        this.placemark = getLookup().lookup(Placemark.class);

        // Initialize renderables
        initializePlacemark();
        initializeBrowserBalloon();

        // Listen to the application time
        TimeProvider tp = DefaultTimeProvider.getInstance();
        tp.addTimeListener(WeakListeners.create(TimeListener.class, updater, tp));

        // Add persistance capability
        getInstanceContent().add(new Writer(this));
    }

    private void initializePlacemark() {
        placemark.setPreRenderableDelegate(preRenderDelegate);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);  // CLAMP_TO_GROUND, RELATIVE_TO_GROUND or ABSOLUTE

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
        return wxProvider;
    }

    public void setWeatherProvider(WeatherProvider provider) {
        this.wxProvider = provider;
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
    private void initializeBrowserBalloon() {
        try {
            // Remove existing balloon
            if (balloon != null) {
                getInstanceContent().remove(balloon);
            }
            // Create new balloon
            balloon = new WeatherBalloon(Positions.fromCoord3D(getPosition()), wxProvider);

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
     * Builder class used for creating WeatherMarkers.
     *
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    public static class Builder extends AbstractMarkerBuilder {

        private WeatherProvider provider;

        public Builder(Document document) {
            super(document);
        }

        public Builder() {
        }

        public WeatherProvider getWeatherProvider() {
            return provider;
        }

        public Builder weatherProvider(WeatherProvider provider) {
            this.provider = provider;
            return this;
        }

        @Override
        public Marker build() {
            WeatherMarker marker = new WeatherMarker();
            if (getDocument() != null) {
                marker = initializeWxFromXml(marker);
            }
            marker = initializeWxFromParameters(marker);
            marker.loader.loadWeatherForecast();
            return marker;
        }

        /**
         * Initializes the supplied marker with the WeatherProvider property from the XML.
         * @param marker The WeatherMarker to be initialized.
         * @return The updated WeatherMarker.
         */
        protected WeatherMarker initializeWxFromXml(WeatherMarker marker) {
            // Let the base class do all the heavy lifting
            super.initializeFromXml(marker);

            // Now set the marker's WeatherProvider from the XML
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(MarkerSupport.getNamespaceContext());
            try {
                // Get the WeatherProvider class name defined in the document [0..1]
                String providerClass = xpath.evaluate("//" + MKR_PREFIX + ":" + "WeatherProvider", getDocument());
                if (providerClass == null || providerClass.isEmpty()) {
                    throw new RuntimeException("WeatherProvider is not defined in XML.");
                }
                // Look for the WeatherProvider instance on the global lookup.
                Lookup.Result<WeatherProvider> result = Lookup.getDefault().lookupResult(WeatherProvider.class);
                for (Lookup.Item<WeatherProvider> item : result.allItems()) {
                    if (item.getType().getName().equals(providerClass)) {
                        // Found it! Set the marker's wx provider.
                        marker.setWeatherProvider(item.getInstance());
                        return marker;
                    }
                }
                logger.log(Level.WARNING, "Could not find WeatherProvider for {0}", marker.getName());
            } catch (XPathExpressionException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalStateException ex) {
                logger.warning(ex.toString());
            }
            return marker;
        }

        protected WeatherMarker initializeWxFromParameters(WeatherMarker marker) {
            super.initializeFromParameters(marker);
            if (provider != null) {
                if (getDocument() != null) {
                    logger.log(Level.INFO, "Overriding the WeatherProvider set by XML document for {0}.", marker.getName());
                }
                marker.setWeatherProvider(provider);
            }
            return marker;
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
