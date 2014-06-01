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
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
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
package com.emxsys.weather.api;

import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.SpatioTemporalDomain;
import com.emxsys.visad.TemporalDomain;
import static com.emxsys.weather.api.WeatherType.FIRE_WEATHER;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.util.logging.Logger;
import visad.FieldImpl;
import visad.FlatField;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class DiurnalWeatherModel extends WeatherModel {

    private final TemporalDomain temporalDomain;
    private final SpatialDomain spatialDomain;
    private final SpotWeatherObserver spotWeatherObserver;
    private static final Logger logger = Logger.getLogger(DiurnalWeatherModel.class.getName());

    public DiurnalWeatherModel(SpatioTemporalDomain domain, WeatherProvider weatherProvider, boolean immediate) {
        if (domain==null) {
            throw new IllegalArgumentException("SpatioTemporalDomain is null.");
        } else if (weatherProvider == null) {
            throw new IllegalArgumentException("WeatherProvider is null.");
        }
        this.temporalDomain = domain.getTemporalDomain();
        this.spatialDomain = domain.getSpatialDomain();
        this.spotWeatherObserver = weatherProvider.getCapability(SpotWeatherObserver.class);
        if (this.spotWeatherObserver==null) {
            throw new IllegalArgumentException("WeatherProvider does not support SpotWeatherObserver.");            
        }
        if (immediate) {
            initializeWeather();
        }
    }

    @Override
    protected FieldImpl createWeather() {
        try {
            // Create the function: (time -> ((lat,lon) -> (FIRE_WEATHER)))
            FlatField spatialField = spatialDomain.createSimpleSpatialField(FIRE_WEATHER);
            FieldImpl spatioTemporalField = temporalDomain.createTemporalField(spatialField.getType());

            final int numLatLons = spatialDomain.getDomainSet().getLength();
            final int numTimes = temporalDomain.getDomainSet().getLength();
            double[][] wxSamples = new double[FIRE_WEATHER.getDimension()][numLatLons];

            // Loop through the temporal domain
            for (int i = 0; i < numTimes; i++) {
                ZonedDateTime time = temporalDomain.getZonedDateTimeAt(i);

                // Loop through the spatial domain
                for (int xy = 0; xy < numLatLons; xy++) {
                    // Compute weather data at the time and place
                    GeoCoord2D coord = GeoCoord2D.fromLatLonPoint(this.spatialDomain.getLatLonPointAt(xy));
                    WeatherTuple weather = spotWeatherObserver.getWeather(time, coord);

                    // Copy weather values into the spatial function's samples
                    double[] values = weather.getValues();
                    for (int dim = 0; dim < FIRE_WEATHER.getDimension(); dim++) {
                        wxSamples[dim][xy] = values[dim];
                    }
                }
                // Populate the spatio-temporal function's samples with the spatial function data
                spatialField.setSamples(wxSamples);
                spatioTemporalField.setSample(i, spatialField);
            }
            return spatioTemporalField;

        } catch (IllegalStateException | VisADException | RemoteException ex) {
            logger.severe(ex.toString());
        }
        return null;
    }

}
