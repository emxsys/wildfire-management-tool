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
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.weather.api.WeatherModel;
import com.emxsys.weather.api.WeatherTuple;
import com.emxsys.weather.api.services.WeatherForecaster;
import com.emxsys.weather.api.services.WeatherObserver;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import visad.Data;
import visad.FlatField;
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
    private WeatherForecaster forecaster;
    private WeatherObserver observer;
    private final Map<Long, FlatField> observationCache = new HashMap<>();
    private final Map<Long, FlatField> forecastCache = new HashMap<>();

    private ZonedDateTime hour;
    private Duration historyPeriod = Duration.ofHours(24);
    private Duration forecastPeriod = Duration.ofHours(72);
    private final AtomicReference<TemporalDomain> forecastTimeframe = new AtomicReference<>();
    private final AtomicReference<TemporalDomain> observationTimeframe = new AtomicReference<>();
    private final AtomicReference<SpatialDomain> spatialDomain = new AtomicReference<>();
    private WeatherModel weatherForecast;
    private WeatherModel weatherObservations;

    private final RequestProcessor executor = new RequestProcessor(WeatherManager.class);
    private Task refreshTask;

    /**
     * Private constructor. Use WeatherManager.getInstance() to get the singleton.
     */
    private WeatherManager() {
        updateTime(null);
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

            observationTimeframe.set(TemporalDomain.from(start, hour));
            forecastTimeframe.set(TemporalDomain.from(hour, end));

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

    public WeatherTuple getCurrentWeatherAt(Coord2D coord) {
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
//            return WeatherTuple.INVALID_TUPLE;
//        }
//        System.out.println(model);
//
//        if (!spatialDomain.contains(coord)) {
//            return WeatherTuple.INVALID_TUPLE;
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
//                    // Transform RealTuple to WeatherTuple (assures proper units after resample)
//                    return WeatherTuple.fromRealTuple(tuple);
//                }
//            } catch (VisADException | RemoteException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
        // Return a safe value if no weather available
        return WeatherTuple.INVALID_TUPLE;

    }

    public WeatherTuple getWeatherAt(Coord2D coord, ZonedDateTime time) {
        WeatherModel model = null;
        Map<Long, FlatField> cache = null;

        // Temporal prerequistes: ensure a model exists for the temporal domain
        TemporalDomain obsTime = observationTimeframe.get();
        TemporalDomain fcstTime = forecastTimeframe.get();
        if (obsTime != null && obsTime.contains(time)) {
            if (weatherObservations != null) {
                model = weatherObservations;
                cache = observationCache;
            }
        } else if (fcstTime != null && fcstTime.contains(time)) {
            // Ensure the model contains the time
            if (weatherForecast != null) {
                model = weatherForecast;
                cache = forecastCache;
            }
        }
        if (model == null || cache == null) {
            return WeatherTuple.INVALID_TUPLE;
        }
        //System.out.println(model);

        // Spatial prerequisite: ensure the spatial domain is valid
        SpatialDomain area = spatialDomain.get();
        if (area == null || !area.contains(coord)) {
            return WeatherTuple.INVALID_TUPLE;
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
                RealTuple tuple = (RealTuple) wxField.evaluate(
                        Coords.toLatLonTuple(coord),
                        Data.WEIGHTED_AVERAGE,
                        Data.NO_ERRORS);
                if (tuple != null) {
                    // Transform RealTuple to WeatherTuple (assures proper units after resample)
                    return WeatherTuple.fromRealTuple(tuple);
                }
            } catch (VisADException | RemoteException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        // Return a safe value if no weather available
        return WeatherTuple.INVALID_TUPLE;
    }

    public void setForecaster(WeatherForecaster forecaster) {
        this.forecaster = forecaster;
    }

    public void setObserver(WeatherObserver observer) {
        this.observer = observer;
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
