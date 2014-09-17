/*
 * Copyright (c) 2014, Bruce Schubert 
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
package com.emxsys.weather.options;

import com.emxsys.visad.GeneralUnit;
import static com.emxsys.visad.GeneralUnit.degF;
import com.emxsys.visad.Reals;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.api.WeatherType;
import static java.lang.Math.round;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import visad.CommonUnit;
import visad.Real;
import visad.Unit;

/**
 * Weather Options
 *
 * @author Bruce Schubert
 */
public class WeatherOptions {

    private static final Preferences prefs = NbPreferences.forModule(WeatherOptions.class);

    // Unit of Measure property key and values
    public static final String PREF_AIR_TEMP_UOM = "weather.airtemp.uom";
    public static final String PREF_WIND_SPD_UOM = "weather.windspd.uom";
    public static final String UOM_FAHRENHEIT = "fahrenheit";
    public static final String UOM_CELSIUS = "celsius";
    public static final String UOM_MPH = "mph";
    public static final String UOM_KPH = "kph";
    public static final String UOM_KTS = "kts";
    public static final String UOM_MPS = "mps";
    public static final String DEFAULT_AIR_TEMP_UOM = UOM_FAHRENHEIT;
    public static final String DEFAULT_WIND_SPD_UOM = UOM_MPH;
    // Air Temp Preferences property keys
    public static final String PREF_AIR_TEMP_SUNRISE = "weather.tempAtSunrise";
    public static final String PREF_AIR_TEMP_1200 = "weather.tempAtNoon";
    public static final String PREF_AIR_TEMP_1400 = "weather.tempAt1400";
    public static final String PREF_AIR_TEMP_SUNSET = "weather.tempAtSunset";
    // Defaults for temp controls (F)
    public static final int DEFAULT_AIR_TEMP_SUNRISE = 65;
    public static final int DEFAULT_AIR_TEMP_1200 = 80;
    public static final int DEFAULT_AIR_TEMP_1400 = 82;
    public static final int DEFAULT_AIR_TEMP_SUNSET = 75;
    // Humidity Preferences property keys
    public static final String PREF_RH_SUNRISE = "weather.rhAtSunrise";
    public static final String PREF_RH_1200 = "weather.rhAtNoon";
    public static final String PREF_RH_1400 = "weather.rhAt1400";
    public static final String PREF_RH_SUNSET = "weather.rhAtSunset";
    // Defaults for rh controls (%)
    public static final int DEFAULT_RH_SUNRISE = 20;
    public static final int DEFAULT_RH_1200 = 7;
    public static final int DEFAULT_RH_1400 = 6;
    public static final int DEFAULT_RH_SUNSET = 8;

    public static DiurnalWeatherProvider newDiurnalWeatherProvider() {
        DiurnalWeatherProvider provider = new DiurnalWeatherProvider();
        provider.initializeAirTemperatures(
                getAirTempPreference(PREF_AIR_TEMP_SUNRISE),
                getAirTempPreference(PREF_AIR_TEMP_1200),
                getAirTempPreference(PREF_AIR_TEMP_1400),
                getAirTempPreference(PREF_AIR_TEMP_SUNSET));
        
        provider.initializeRelativeHumidities(
                getRelHumidityPreference(PREF_RH_SUNRISE),
                getRelHumidityPreference(PREF_RH_1200),
                getRelHumidityPreference(PREF_RH_1400),
                getRelHumidityPreference(PREF_RH_SUNSET));
        return provider;
    }

    public static Unit getAirTempUom() {
        String uom = prefs.get(PREF_AIR_TEMP_UOM, DEFAULT_AIR_TEMP_UOM);
        switch (uom) {
            case UOM_FAHRENHEIT:
                return GeneralUnit.degF;
            case UOM_CELSIUS:
                return GeneralUnit.degC;
            default:
                throw new IllegalStateException("Unhandled Air Temp UOM: " + uom);
        }
    }

    public static Unit getWindSpeedUom() {
        String uom = prefs.get(PREF_WIND_SPD_UOM, DEFAULT_WIND_SPD_UOM);
        switch (uom) {
            case UOM_MPH:
                return GeneralUnit.mph;
            case UOM_KPH:
                return GeneralUnit.kph;
            case UOM_KTS:
                return GeneralUnit.knot;
            case UOM_MPS:
                return CommonUnit.meterPerSecond;
            default:
                throw new IllegalStateException("Unhandled Wind Speed UOM: " + uom);
        }
    }

    public static Real getAirTempPreference(String key) {
        int defValue;
        switch (key) {
            case PREF_AIR_TEMP_1200:
                defValue = DEFAULT_AIR_TEMP_1200;
                break;
            case PREF_AIR_TEMP_1400:
                defValue = DEFAULT_AIR_TEMP_1400;
                break;
            case PREF_AIR_TEMP_SUNRISE:
                defValue = DEFAULT_AIR_TEMP_SUNRISE;
                break;
            case PREF_AIR_TEMP_SUNSET:
                defValue = DEFAULT_AIR_TEMP_SUNSET;
                break;
            default:
                throw new IllegalArgumentException("Illegal Air Temperature key: " + key);
        }
        Real airTempF = new Real(WeatherType.AIR_TEMP_F, prefs.getInt(key, defValue));
        return getAirTempUom().equals(degF) ? airTempF : Reals.convertTo(WeatherType.AIR_TEMP_C, airTempF);
    }

    public static void setAirTempPreference(String key, Real value) {
        // Store the temp in Fahreheit
        Real airTempF = Reals.convertTo(WeatherType.AIR_TEMP_F, value);
        prefs.putInt(key, (int) round(airTempF.getValue()));
    }

    public static Real getRelHumidityPreference(String key) {
        int value;
        switch (key) {
            case PREF_RH_1200:
                value = prefs.getInt(key, DEFAULT_RH_1200);
                break;
            case PREF_RH_1400:
                value = prefs.getInt(key, DEFAULT_RH_1400);
                break;
            case PREF_RH_SUNRISE:
                value = prefs.getInt(key, DEFAULT_RH_SUNRISE);
                break;
            case PREF_RH_SUNSET:
                value = prefs.getInt(key, DEFAULT_RH_SUNSET);
                break;
            default:
                throw new IllegalArgumentException("Illegal RH key: " + key);
        }
        return new Real(WeatherType.REL_HUMIDITY, value);
    }

    public static void setRelHumidityPreference(String key, int value) {
        prefs.putInt(key, value);
    }

}
