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

import com.emxsys.visad.GeneralUnit;
import com.emxsys.visad.Reals;
import com.emxsys.visad.Tuples;
import visad.CommonUnit;
import visad.RealTupleType;
import visad.RealType;

/**
 * WeatherType is a utility class used to obtain VisAD based weather types.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class WeatherType {

    // Fire weather types
    /** Wind speed in meters per second. */
    public static final RealType WIND_SPEED_SI;
    /** Wind speed in miles per hour. */
    public static final RealType WIND_SPEED_MPH;
    /** Wind speed in kilometers per hour. */
    public static final RealType WIND_SPEED_KPH;
    /** Wind speed in knots. */
    public static final RealType WIND_SPEED_KTS;
    /** Wind direction "to" in compass direction (degrees). */
    public static final RealType WIND_DIR;
    /** Air temperature in Kelvin (base unit) */
    public static final RealType AIR_TEMP;
    /** Air temperature in Celsius */
    public static final RealType AIR_TEMP_C;
    /** Air temperature in Fahrenheit */
    public static final RealType AIR_TEMP_F;
    /** Relative humidity percentage (0=dry, 100=wet). */
    public static final RealType REL_HUMIDITY;
    /** Cloud cover percentage (0=clear, 100=cloudy). */
    public static final RealType CLOUD_COVER;
    /** Rainfall (inches). */
    public static final RealType RAINFALL_INCH;
    /** Fire Weather: AIR_TEMP_C, REL_HUMIDITY, WIND_SPEED_SI, WIND_DIR, CLOUD_COVER */
    public final static RealTupleType FIRE_WEATHER;

    static {
        WIND_SPEED_SI = RealType.getRealType("wind_speed:m/s", CommonUnit.meterPerSecond, null);
        WIND_SPEED_MPH = RealType.getRealType("wind_speed:mph", GeneralUnit.mph, null);
        WIND_SPEED_KPH = RealType.getRealType("wind_speed:kph", GeneralUnit.kph, null);
        WIND_SPEED_KTS = RealType.getRealType("wind_speed:kts", GeneralUnit.knot, null);
        WIND_DIR = RealType.getRealType("wind_dir:deg", CommonUnit.degree, null);
        AIR_TEMP = RealType.getRealType("air_temp", GeneralUnit.degK, null);
        AIR_TEMP_C = RealType.getRealType("air_temp:C", GeneralUnit.degC, null);
        AIR_TEMP_F = RealType.getRealType("air_temp:F", GeneralUnit.degF, null);
        REL_HUMIDITY = RealType.getRealType("rel_humidity:%", GeneralUnit.percent, null);
        CLOUD_COVER = RealType.getRealType("cloud_cover:%", GeneralUnit.percent, null);
        RAINFALL_INCH = RealType.getRealType("rainfall:in", GeneralUnit.inch, null);

        FIRE_WEATHER = Reals.newRealTupleType(
                new RealType[]{
                    AIR_TEMP_F, REL_HUMIDITY, WIND_SPEED_KTS, WIND_DIR, CLOUD_COVER
                });

    }
    public static final int AIR_TEMP_INDEX = Tuples.getIndex(AIR_TEMP_F, FIRE_WEATHER);
    public static final int REL_HUMIDITY_INDEX = Tuples.getIndex(REL_HUMIDITY, FIRE_WEATHER);
    public static final int WIND_SPEED_INDEX = Tuples.getIndex(WIND_SPEED_KTS, FIRE_WEATHER);
    public static final int WIND_DIR_INDEX = Tuples.getIndex(WIND_DIR, FIRE_WEATHER);
    public static final int CLOUD_COVER_INDEX = Tuples.getIndex(CLOUD_COVER, FIRE_WEATHER);
}
