/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of Bruce Schubert,  nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
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
package com.emxsys.wmt.cps;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coords;
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.weather.api.WeatherModel;
import com.emxsys.weather.api.BasicWeather;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import static com.emxsys.weather.api.WeatherType.FIRE_WEATHER;
import com.emxsys.weather.api.services.WeatherForecaster;
import com.emxsys.weather.api.services.WeatherObserver;
import java.awt.EventQueue;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import visad.Data;
import visad.Field;
import visad.FlatField;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 * This class manages the WeatherModel(s) used in the Model and Controller.
 *
 * @author Bruce Schubert
 */
public class WeatherManager {

    /**
     * Gets the WeatherManager singleton instance.
     *
     * @return The singleton.
     */
    public static WeatherManager getInstance() {
        return WeatherManagerHolder.INSTANCE;
    }

    // The weather providers
    private WeatherForecaster forecaster;
    private WeatherObserver observer;
    private DiurnalWeatherProvider defaultProvider;

    // The local data cache
    private final Map<Long, FlatField> observationCache = new HashMap<>();
    private final Map<Long, FlatField> forecastCache = new HashMap<>();

    // The spatial and temporal domains
    private ZonedDateTime hour;
    private Duration historyPeriod = Duration.ofHours(24);
    private Duration forecastPeriod = Duration.ofHours(72);
    private Duration interval = Duration.ofHours(1);

    private final AtomicReference<TemporalDomain> forecastTimeframe = new AtomicReference<>();
    private final AtomicReference<TemporalDomain> observationTimeframe = new AtomicReference<>();
    private final AtomicReference<SpatialDomain> spatialDomain = new AtomicReference<>();

    // The weather models for the domain
    private WeatherModel weatherForecast;
    private WeatherModel weatherObservations;

    // Process/thread resources
    private final RequestProcessor executor = new RequestProcessor(WeatherManager.class);
    private Task refreshTask;

    // WeatherModel change notification support
    private ChangeSupport changeSupport = new ChangeSupport(this);

    // Logging support
    private static final Logger logger = Logger.getLogger(WeatherManager.class.getName());

    /**
     * Private constructor. Use WeatherManager.getInstance() to get the singleton.
     */
    private WeatherManager() {
        defaultProvider = DiurnalWeatherProvider.fromWeatherPreferences();
        updateTime(null);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /**
     * Automatically update the weather forecast when the system time advances past the
     * top-of-the-hour.
     */
    public void updateTime(ZonedDateTime ignored) {
        // real-time observations and forecast are relative to the current clock time, not application time.
        ZonedDateTime currentHour = ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS);
        if (hour == null || !(hour.equals(currentHour))) {
            hour = currentHour;

            ZonedDateTime start = hour.minus(historyPeriod);
            ZonedDateTime end = hour.plus(forecastPeriod);

            observationTimeframe.set(TemporalDomain.from(start, hour, interval));
            forecastTimeframe.set(TemporalDomain.from(hour, end, interval));

            refreshModels();
        }
    }

    /**
     * Update the weather's spatial domain when the project's sector/area-of-interest changes
     */
    public synchronized void updateSpatialDomain(Coord2D coord) {
        // Ensure the current area of interest is meets the minumum spatial domain requirements
        SpatialDomain domain = spatialDomain.get();
        if (domain == null || !domain.contains(coord)) {

            // Create a one-degree sector around the position of interest.
            // TODO: Make the area of interest size a configurable CPS Option.
            double lat = coord.getLatitudeDegrees();
            double lon = coord.getLongitudeDegrees();
            GeoCoord2D sw = GeoCoord2D.fromDegrees(lat - .1, lon - .1);
            GeoCoord2D ne = GeoCoord2D.fromDegrees(lat + .1, lon + .1);

            // Set the domain to the sector's extents
            // TODO: Make the number of grid points a configurable CPS Option
            spatialDomain.set(SpatialDomain.from(sw, ne, 10, 10));

            refreshModels();
        }
    }

    public synchronized void refreshModels() {
        // Prerequisites
        if (spatialDomain.get() == null || forecastTimeframe.get() == null) {
            return;
        }

        // Perform task in a worker thread.
        if (refreshTask == null) {

            // Create the runnable task
            refreshTask = executor.create(() -> {

                // Create a 'cancellable' progress bar
                ProgressHandle progressBar = ProgressHandleFactory.createHandle("Downloading weather", refreshTask);
                try {
                    // Update weather forecast
                    progressBar.start();
                    if (forecaster != null) {
                        System.out.println("Getting Forecast for:");
                        System.out.println(forecastTimeframe.get().toString());
                        progressBar.progress("Downloading weather forecast");

                        weatherForecast = forecaster.getForecast(spatialDomain.get(), forecastTimeframe.get());
                        // Remove any stale forecasts
                        //forecastCache.clear();
                    }

                    // Update weather history
                    if (observer != null) {
                        progressBar.progress("Downloading weather observations");
                        weatherObservations = observer.getObservations(spatialDomain.get(), observationTimeframe.get());
                        // No need to flush cache...observations do not change over time.
                        //observationCache.clear();
                    }
                    // Notify the Controller of a change in weather
                    EventQueue.invokeLater(() -> {
                        changeSupport.fireChange();
                    });

                } finally {
                    progressBar.finish();
                }
            }, true);
        }

        // Coellese refresh requests.  Each refresh request (re)postpones the task.
        // The refresh will occur when the system is motionless for the specified delay time.
        int msDelay = 1000; // TODO: Make the delay time a configurable CpsOption.
        refreshTask.schedule(msDelay);
    }

    public BasicWeather getCurrentWeatherAt(Coord2D coord) {
        // Prerequistes
//        WeatherModel model;
//        Map<Long, FlatField> cache;
//        if (historyDomain.contains(time)) {
//            model = weatherObservations;
//            cache = historyCache;
//        } else if (forecastDomain.contains(time)) {
//            // Ensure the model contains the time
//            model = weatherForecast;
//            cache = forecastCache;
//        } else {
//            return BasicWeather.INVALID_WEATHER;
//        }
//        System.out.println(model);
//
//        if (!spatialDomain.contains(coord)) {
//            return BasicWeather.INVALID_WEATHER;
//        }
//        // Get the hourly floor and cache key for the hour-by-hour weather
//        ZonedDateTime timeHour = time.truncatedTo(ChronoUnit.HOURS);
//        Long key = timeHour.toEpochSecond();
//
//        // First, get the spatial wx field for the hour
//        FlatField wxField = cache.get(key);
//        if (wxField == null) {
//            wxField = model.getSpatialWeatherAt(timeHour);
//            if (wxField == null) {
//                cache.put(key, wxField);
//            }
//        }
//        // Now get and return the weather tuple at the coordinate
//        if (wxField != null) {
//            try {
//                RealTuple tuple = (RealTuple) wxField.evaluate(
//                        Coords.toLatLonTuple(coord),
//                        Data.WEIGHTED_AVERAGE,
//                        Data.NO_ERRORS);
//                if (tuple != null) {
//                    // Transform RealTuple to BasicWeather (assures proper units after resample)
//                    return BasicWeather.fromRealTuple(tuple);
//                }
//            } catch (VisADException | RemoteException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
        // Return a safe value if no weather available
        return BasicWeather.INVALID_WEATHER;

    }

    /**
     * Gets the weather at a specific location and time. If the location and time are outside of
     * the spatial or temporal domains, then the weather is determined by the WeatherPreferences.
     *
     * @param coord The location.
     * @param time The date/time.
     * @return A valid BasicWeather instance.
     */
    public BasicWeather getWeatherAt(Coord2D coord, ZonedDateTime time) {

        // Determine which model to use
        WeatherModel model = null;
        Map<Long, FlatField> cache = null;

        // Temporal prerequistes: ensure a model exists for the temporal domain
        TemporalDomain obsTimeFrame = observationTimeframe.get();
        TemporalDomain fcstTimeFrame = forecastTimeframe.get();
        if (obsTimeFrame != null && obsTimeFrame.contains(time)) {
            if (weatherObservations != null) {
                model = weatherObservations;
                cache = observationCache;
            }
        } else if (fcstTimeFrame != null && fcstTimeFrame.contains(time)) {
            // Ensure the model contains the time
            if (weatherForecast != null) {
                model = weatherForecast;
                cache = forecastCache;
            }
        }
        // TODO: Create a hybrid model to bridge the gap from the last observation to the first forecast.            
        if (model == null || cache == null) {
            // Use a diurnal model from the preferences for the times outside the temporal domains.
            return defaultProvider.getWeather(time, GeoCoord3D.fromCoord(coord));
        }
        // Spatial prerequisite: ensure the spatial domain is valid
        SpatialDomain area = spatialDomain.get();
        if (area == null || !area.contains(coord)) {
            // Use a diurnal model from the preferences for the locations outside the spatial domain.
            return defaultProvider.getWeather(time, GeoCoord3D.fromCoord(coord));
        }

        // Get the hourly floor and cache key for the hour-by-hour weather
        ZonedDateTime timeHour = time.truncatedTo(ChronoUnit.HOURS);
        Long key = timeHour.toEpochSecond();

        // First, get the spatial wx field for the hour
        FlatField wxField = cache.get(key);
        if (wxField == null) {
            wxField = model.getSpatialWeatherAt(timeHour);
            if (wxField == null) {
                cache.put(key, wxField);
            }
        }
        // Now get and return the weather tuple at the coordinate
        if (wxField != null) {
            try {
                // TODO: Extract each wx component out as a single field
                // FIRE_WEATHER: AIR_TEMP_F, REL_HUMIDITY, WIND_SPEED_KTS, WIND_DIR, CLOUD_COVER                
                Field airTemp = wxField.extract(0);
                Field relHum = wxField.extract(1);
                Field windSpd = wxField.extract(2);
                Field windDir = wxField.extract(3);
                Field cloudCvr = wxField.extract(4);

                Real temperature = (Real) airTemp.evaluate(
                        Coords.toLatLonTuple(coord),
                        Data.WEIGHTED_AVERAGE,
                        Data.DEPENDENT //Data.NO_ERRORS 
                );
                Real humidity = (Real) relHum.evaluate(
                        Coords.toLatLonTuple(coord),
                        Data.WEIGHTED_AVERAGE,
                        Data.DEPENDENT //Data.NO_ERRORS 
                );
                Real direction = (Real) windDir.evaluate(
                        Coords.toLatLonTuple(coord),
                        Data.NEAREST_NEIGHBOR,
                        Data.DEPENDENT //Data.NO_ERRORS 
                );

                RealTuple tuple = (RealTuple) wxField.evaluate(
                        Coords.toLatLonTuple(coord),
                        Data.WEIGHTED_AVERAGE,
                        Data.DEPENDENT //Data.NO_ERRORS 
                );
                if (tuple != null) {
                    // Transform RealTuple to BasicWeather (assures proper units after resample)
                    return BasicWeather.fromRealTuple(tuple);

//                    Real[] reals = tuple.getRealComponents();
//                    return BasicWeather.fromReals(
//                            reals[0].isMissing() ? 
//                            tuple.getRealComponents()null, null, null, null, null)
                }
            } catch (VisADException | RemoteException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        // Return a safe value if no weather available
        return BasicWeather.INVALID_WEATHER;
    }

    /**
     * Sets the weather forecasting service, e.g., NWS.
     *
     * @param forecaster A weather provider that provides forecasts.
     */
    public void setForecaster(WeatherForecaster forecaster) {
        this.forecaster = forecaster;
        refreshModels();
    }

    /**
     * Sets the weather observer service.
     *
     * @param observer A weather provider the provides historical observations.
     */
    public void setObserver(WeatherObserver observer) {
        this.observer = observer;
        refreshModels();
    }

    public WeatherModel getWeatherForecast() {
        return weatherForecast;
    }

    public WeatherModel getWeatherHistory() {
        return weatherObservations;

    }

    /**
     * Singleton implementation.
     */
    private static class WeatherManagerHolder {

        private static final WeatherManager INSTANCE = new WeatherManager();
    }
}
