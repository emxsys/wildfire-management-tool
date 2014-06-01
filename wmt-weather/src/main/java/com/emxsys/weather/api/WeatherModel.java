/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com> 
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
package com.emxsys.weather.api;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.visad.Times;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.util.logging.Logger;
import visad.Data;
import visad.DateTime;
import visad.FieldImpl;
import visad.RealTuple;
import visad.RealTupleType;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class WeatherModel {

    private FieldImpl weather;
    private static final Logger logger = Logger.getLogger(WeatherModel.class.getName());

    /**
     * Math type: ( time -> ( (lat, lon ) -> ( temperature, humidity, ... ) ) )
     *
     * @return hourly weather
     */
    public final FieldImpl getWeatherData() {
        initializeWeather();
        return this.weather;
    }
    
    public WeatherTuple getWeather(ZonedDateTime time, Coord2D coord) {
        if (time == null) {
            throw new IllegalArgumentException("time");
        } else if (coord == null || coord.isMissing()) {
            throw new IllegalArgumentException("coord");
        }
        try {
            initializeWeather();
            DateTime dateTime = Times.fromZonedDateTime(time);
            RealTuple location = new RealTuple(RealTupleType.LatitudeLongitudeTuple, new double[]{
                coord.getLatitudeDegrees(), coord.getLongitudeDegrees()
            });
            FieldImpl spatialField = (FieldImpl) weather.evaluate(dateTime, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            RealTuple tuple = (RealTuple) spatialField.evaluate(location, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            return tuple.isMissing() ? WeatherTuple.INVALID_TUPLE : WeatherTuple.fromRealTuple(tuple);
        } catch (VisADException | RemoteException ex) {
            logger.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    public WeatherTuple getWeatherAt(int temporalIndex, int spatialIndex) {
        try {
            initializeWeather();
            FieldImpl field = (FieldImpl) this.weather.getSample(temporalIndex);
            RealTuple tuple = (RealTuple) field.getSample(spatialIndex);
            return tuple.isMissing() ? WeatherTuple.INVALID_TUPLE : WeatherTuple.fromRealTuple(tuple);
        } catch (VisADException | RemoteException ex) {
            logger.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    protected void initializeWeather() {
        if (this.weather == null) {
            this.weather = createWeather();
        }
    }

    protected abstract FieldImpl createWeather();
}
