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

import com.emxsys.weather.api.Weather;
import static com.emxsys.visad.Reals.*;
import static com.emxsys.weather.api.WeatherType.*;
import java.rmi.RemoteException;
import visad.Data;
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

    /** An tuple with "missing" components */
    public static final WeatherTuple INVALID_TUPLE = new WeatherTuple();
    private Real airTemperature;
    private Real relativeHumidity;
    private Real windSpeed;
    private Real windDirection;
    private Real cloudCover;
    private Data[] components;

    /**
     * Constructs and instance with missing values.
     */
    public WeatherTuple() {
        this(new Real(AIR_TEMP_C),
             new Real(REL_HUMIDITY),
             new Real(WIND_SPEED_SI),
             new Real(WIND_DIR),
             new Real(CLOUD_COVER));
    }

    /**
     * Construct a WeatherTuple object from doubles.
     *
     * @param airTemperature [celsius]
     * @param relativeHumidity [percent]
     * @param windSpeed [m/s]
     * @param windDirection [degrees]
     * @param cloudCover [percent]
     */
    public WeatherTuple(double airTemperature, double relativeHumidity,
                        double windSpeed, double windDirection,
                        double cloudCover) throws VisADException {
        this(
                new Real(AIR_TEMP_C, airTemperature),
                new Real(REL_HUMIDITY, relativeHumidity),
                new Real(WIND_SPEED_SI, windSpeed),
                new Real(WIND_DIR, windDirection),
                new Real(CLOUD_COVER, cloudCover));
    }

    /**
     * Construct a WeatherTuple object from Reals. Guarantees that input parameter values are
     * converted to the member's specified RealTypes.
     */
    public WeatherTuple(Real[] realArray) {
        this(realArray[0], realArray[1], realArray[2], realArray[3], realArray[4]);
    }

    /**
     * Construct a WeatherTuple object from Reals. Guarantees that input parameter values are
     * converted to the member's specified RealTypes.
     *
     * @param airTemperature must be compatible with WildfireType.AIR_TEMP_C
     * @param relativeHumidity must be compatible with WildfireType.REL_HUMIDITY
     * @param windSpeed must be compatible with WildfireType.WIND_SPEED_SI
     * @param windDirection must be compatible with WildfireType.WIND_DIR
     * @param cloudCover must be compatible with WildfireType.CLOUD_COVER
     */
    public WeatherTuple(Real airTemperature, Real relativeHumidity,
                        Real windSpeed, Real windDirection, Real cloudCover) {
        super(FIRE_WEATHER);
        this.airTemperature = convertTo(AIR_TEMP_C, airTemperature);
        this.relativeHumidity = convertTo(REL_HUMIDITY, relativeHumidity);
        this.windSpeed = convertTo(WIND_SPEED_SI, windSpeed);
        this.windDirection = convertTo(WIND_DIR, windDirection);
        this.cloudCover = convertTo(CLOUD_COVER, cloudCover);
    }

    /**
     * Air temperature
     * @return [degC]
     */
    @Override
    public visad.Real getAirTemperature() {
        return this.airTemperature;
    }

    /**
     * Relative humidity
     * @return [percent]
     */
    @Override
    public Real getRelativeHumidity() {
        return this.relativeHumidity;
    }

    /**
     * Wind Speed
     * @return [m/s]
     */
    @Override
    public visad.Real getWindSpeed() {
        return this.windSpeed;
    }

    /**
     * Wind direction
     * @return [deg]
     */
    @Override
    public visad.Real getWindDirection() {
        return this.windDirection;
    }

    /**
     * Cloud cover
     * @return [percent]
     */
    @Override
    public Real getCloudCover() {
        return this.cloudCover;
    }

    /**
     * is missing any data elements
     * @return is missing
     */
    @Override
    public boolean isMissing() {
        return airTemperature.isMissing() || relativeHumidity.isMissing()
                || windSpeed.isMissing() || windDirection.isMissing()
                || cloudCover.isMissing();
    }

    /**
     * Get the i'th component.
     *
     * @param i Which one
     * @return The component
     *
     * @throws RemoteException On badness
     * @throws VisADException On badness
     */
    @Override
    public Data getComponent(int i) throws VisADException, RemoteException {
        switch (i) {
            case 0:
                return airTemperature;
            case 1:
                return relativeHumidity;
            case 2:
                return windSpeed;
            case 3:
                return windDirection;
            case 4:
                return cloudCover;
            default:
                throw new IllegalArgumentException("Wrong component number:" + i);
        }
    }

    /**
     * Create, if needed, and return the component array.
     *
     * @param copy Ignored.
     * @return components
     */
    @Override
    public Data[] getComponents(boolean copy) {
        //Create the array and populate it if needed
        if (components == null) {
            Data[] tmp = new Data[getDimension()];
            tmp[0] = airTemperature;
            tmp[1] = relativeHumidity;
            tmp[2] = windSpeed;
            tmp[3] = windDirection;
            tmp[4] = cloudCover;
            components = tmp;
        }
        return components;
    }

    /**
     * Indicates if this Tuple is identical to an object.
     *
     * @param obj The object.
     * @return <code>true</code> if and only if the object is a Tuple and both Tuple-s have
     * identical component sequences.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WeatherTuple)) {
            return false;
        }
        WeatherTuple that = (WeatherTuple) obj;

        return this.airTemperature.equals(that.airTemperature)
                && this.relativeHumidity.equals(that.relativeHumidity)
                && this.windSpeed.equals(that.windSpeed)
                && this.windDirection.equals(that.windDirection)
                && this.cloudCover.equals(that.cloudCover);
    }

    /**
     * Returns the hash code of this object.
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        return windSpeed.hashCode()
                & windDirection.hashCode()
                ^ airTemperature.hashCode()
                | (relativeHumidity.hashCode() & cloudCover.hashCode());
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
//
    /*
     * Computes the diurnal air temperature within the daily cycle beginning
     * with the day's 1400 temperature and ending with n
     * private void createDiurnalTemperatureCycleData(TimeSeries series, Hour period, CycleParams cycle) {
     //        while (true) {
     //            if (period.getHour() >= 14 || period.getHour() <= 12) {
     //                double T_a = calcDiurnalValueForTimeOfDay(period.getHour(),
     //                        cycle.timeOfSunrise, cycle.timeOfSunset,
     //                        cycle.tempAt1400, cycle.tempAtSunset, cycle.tempAtSunrise, cycle.tempAtNoon);
     //                series.add(period, T_a);
     //                period = (Hour) period.next();
     //            } else {
     //                break;
     //            }
     //        }
     //    }
     //
     //    private void createDiurnalHumidityCycleData(TimeSeries series, Hour period, CycleParams cycle) {
     //        while (true) {
     //            if (period.getHour() >= 14 || period.getHour() <= 12) {
     //                double T_a = calcHumidityForTimeOfDay(period.getHour(),
     //                        cycle.timeOfSunrise, cycle.timeOfSunset,
     //                        cycle.humidityAt1400, cycle.humidityAtSunset, cycle.humidityAtSunrise, cycle.humidityAtNoon);
     //                series.add(period, T_a);
     //                period = (Hour) period.next();
     //            } else {
     //                break;
     //            }
     //        }
     //    }
     ext day's 1200 temperature
     * @param localTime     time of day for desired temp
     * @param localTimeOfSunrise   time of sunrise
     * @param localTimeOfSunset   time of sunset
     * @param H_14  temperature at 1400
     * @param H_s   temperature at sunset
     * @param H_r   temperature at sunrise
     * @param H_12  temperature at noon
     * @return      temperature at t
     */
}
