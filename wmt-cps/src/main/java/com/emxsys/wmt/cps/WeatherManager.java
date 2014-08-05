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
import com.emxsys.visad.Times;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.api.SimpleWeatherProvider;
import com.emxsys.weather.api.WeatherModel;
import com.emxsys.weather.api.WeatherTuple;
import com.emxsys.weather.api.services.WeatherForecaster;
import com.emxsys.weather.api.services.WeatherRecorder;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.Exceptions;
import visad.Data;
import visad.DateTime;
import visad.FlatField;
import visad.RealTuple;
import visad.Set;
import visad.VisADException;

/**
 * This class manages the WeatherModel(s) used in the Model and Controller.
 *
 * @author Bruce Schubert
 */
public class WeatherManager {

    private WeatherForecaster forecaster;
    private WeatherRecorder recorder;
    private Map<Long, FlatField> historyCache = new HashMap<>();
    private Map<Long, FlatField> forecastCache = new HashMap<>();

    private ZonedDateTime hour;
    private Duration historyPeriod = Duration.ofHours(24);
    private Duration forecastPeriod = Duration.ofHours(72);
    private TemporalDomain temporalDomain;
    private TemporalDomain forecastDomain;
    private TemporalDomain historyDomain;
    private SpatialDomain spatialDomain;
    private WeatherModel weatherForecast;
    private WeatherModel weatherHistory;

    private final SimpleWeatherProvider simpleWeather = new SimpleWeatherProvider();
    private final DiurnalWeatherProvider diurnalWeather = new DiurnalWeatherProvider();

    // Update the weather when the system time advances past the top-of-the-hour
    public void updateTime(ZonedDateTime time) {
        ZonedDateTime currentHour = ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS);
        if (hour == null || !(hour.equals(currentHour))) {
            hour = currentHour;

            ZonedDateTime start = hour.minus(historyPeriod);
            ZonedDateTime end = hour.plus(forecastPeriod);

            historyDomain = TemporalDomain.from(start, hour);
            forecastDomain = TemporalDomain.from(hour, end);

            refreshModels();
        }
    }

    // Update the weather's spatial domain when the project's sector/area-of-interest changes
    public void updateCoord(Coord2D coord) {
        // Ensure the area of interest is meets the minumum spatial domain requirements
        if (spatialDomain == null || !spatialDomain.contains(coord)) {
            double lat = coord.getLatitudeDegrees();
            double lon = coord.getLongitudeDegrees();

            GeoCoord2D sw = GeoCoord2D.fromDegrees(lat - .5, lon - .5);
            GeoCoord2D ne = GeoCoord2D.fromDegrees(lat + .5, lon + .5);

            spatialDomain = SpatialDomain.from(sw, ne);

            refreshModels();
        }
    }

    private void refreshModels() {
        // Prerequisites
        if (spatialDomain == null || forecastDomain == null) {
            return;
        }
        // Update weather forecast
        if (forecaster != null) {
            weatherForecast = forecaster.getForecast(spatialDomain, forecastDomain);
            forecastCache.clear();
        }
        // Update weather history
        if (recorder != null) {
            weatherHistory = recorder.getRecordedConditions(spatialDomain, historyDomain);
            // No need to full cache...observations do not change over time.
        }
    }

    public WeatherTuple getWeatherAt(Coord2D coord, ZonedDateTime time) {
        // Prerequistes
        WeatherModel model;
        Map<Long, FlatField> cache;
        if (historyDomain.contains(time)) {
            model = weatherHistory;
            cache = historyCache;
        } else if (forecastDomain.contains(time)) {
            // Ensure the model contains the time
            model = weatherForecast;
            cache = forecastCache;
        } else {
            return WeatherTuple.INVALID_TUPLE;
        }
        System.out.println(model);

        if (!spatialDomain.contains(coord)) {
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

    public void setRecorder(WeatherRecorder recorder) {
        this.recorder = recorder;
    }

    public WeatherModel getWeatherForecast() {
        return weatherForecast;
    }

    public WeatherModel getWeatherHistory() {
        return weatherHistory;
    }
}
