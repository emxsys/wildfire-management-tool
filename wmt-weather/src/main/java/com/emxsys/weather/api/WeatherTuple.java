/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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

import static com.emxsys.visad.Reals.*;
import com.emxsys.visad.Tuples;
import static com.emxsys.weather.api.WeatherType.*;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 * WeatherTuple is a concrete implementation of the Weather interface.
 *
 * The weather data model in VisAD can be represented by a FunctionType:
 *
 * (lat,lon,time) -> (air_temp, relative_humidity, wind_speed, wind_dir, cloud_cover)
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class WeatherTuple extends RealTuple implements Weather {

    /** A tuple with "missing" components */
    public static final WeatherTuple INVALID_TUPLE = new WeatherTuple();
    public static final int AIR_TEMP_INDEX = Tuples.getIndex(AIR_TEMP_C, FIRE_WEATHER);
    public static final int REL_HUMIDITY_INDEX = Tuples.getIndex(REL_HUMIDITY, FIRE_WEATHER);
    public static final int WIND_SPEED_INDEX = Tuples.getIndex(WIND_SPEED_SI, FIRE_WEATHER);
    public static final int WIND_DIR_INDEX = Tuples.getIndex(WIND_DIR, FIRE_WEATHER);
    public static final int CLOUD_COVER_INDEX = Tuples.getIndex(CLOUD_COVER, FIRE_WEATHER);
    //public static final int RAINFALL_INDEX = Tuples.getIndex(RAINFALL_INCH, FIRE_WEATHER);
    private static final Logger logger = Logger.getLogger(WeatherTuple.class.getName());

    /**
     * Creates a WeatherTuple from a RealTuple of type FIRE_WEATHER.
     * @param fireWeather A WeatherType.FIRE_WEATHER RealTuple.
     * @return A new WeatherTuple.
     */
    public static WeatherTuple fromRealTuple(RealTuple fireWeather) {
        if (!fireWeather.getType().equals(WeatherType.FIRE_WEATHER)) {
            throw new IllegalArgumentException("Incompatible MathType: " + fireWeather.getType());
        } else if (fireWeather.isMissing()) {
            return INVALID_TUPLE;
        }
        try {
            return new WeatherTuple(fireWeather);
        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, "Cannot create WeatherTuple.", ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Construct a WeatherTuple object from Reals. Guarantees that input parameter values are
     * converted to the RealTupleType's specified RealTypes.
     *
     * @param airTemperature must be compatible with WildfireType.AIR_TEMP_C
     * @param relativeHumidity must be compatible with WildfireType.REL_HUMIDITY
     * @param windSpeed must be compatible with WildfireType.WIND_SPEED_SI
     * @param windDirection must be compatible with WildfireType.WIND_DIR
     * @param cloudCover must be compatible with WildfireType.CLOUD_COVER
     * @return A new WeatherTuple.
     */
    public static WeatherTuple fromReals(Real airTemperature, Real relativeHumidity,
                                         Real windSpeed, Real windDirection, Real cloudCover) {
        try {
            Real[] reals = new Real[]{
                convertTo(AIR_TEMP_C, airTemperature),
                convertTo(REL_HUMIDITY, relativeHumidity),
                convertTo(WIND_SPEED_SI, windSpeed),
                convertTo(WIND_DIR, windDirection),
                convertTo(CLOUD_COVER, cloudCover)
            };
            return new WeatherTuple(new RealTuple(reals));

        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, "Cannot create WeatherTuple.", ex);
            throw new IllegalStateException(ex);
        }

    }

    /**
     * Constructs and instance with missing values.
     * @param Weather
     */
    WeatherTuple(RealTuple WeatherTuple) throws VisADException, RemoteException {
        super(WeatherType.FIRE_WEATHER, WeatherTuple.getRealComponents(), null);
    }

    /**
     * Constructs and instance with missing values.
     */
    public WeatherTuple() {
        super(WeatherType.FIRE_WEATHER);
    }

    /**
     * Air temperature
     * @return [degC]
     */
    @Override
    public visad.Real getAirTemperature() {
        try {
            return (Real) getComponent(AIR_TEMP_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Relative humidity
     * @return [percent]
     */
    @Override
    public Real getRelativeHumidity() {
        try {
            return (Real) getComponent(REL_HUMIDITY_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Wind Speed
     * @return [m/s]
     */
    @Override
    public visad.Real getWindSpeed() {
        try {
            return (Real) getComponent(WIND_SPEED_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Wind direction
     * @return [deg]
     */
    @Override
    public visad.Real getWindDirection() {
        try {
            return (Real) getComponent(WIND_DIR_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Cloud cover
     * @return [percent]
     */
    @Override
    public Real getCloudCover() {
        try {
            return (Real) getComponent(CLOUD_COVER_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }



    /**
     * to string
     * @return string of me
     */
    @Override
    public String toString() {
        return "Air: " + getAirTemperature().toValueString()
                + ", RH: " + getRelativeHumidity().toValueString()
                + ", Spd: " + getWindSpeed().toValueString()
                + ", Dir: " + getWindDirection().toValueString()
                + ", Sky: " + getCloudCover().toValueString();
    }
}
