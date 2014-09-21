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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import static java.lang.Math.round;
import java.util.HashMap;
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
    static final String DEFAULT_AIR_TEMP_UOM = UOM_FAHRENHEIT;
    static final String DEFAULT_WIND_SPD_UOM = UOM_MPH;
    // Air Temp Preferences property keys
    public static final String PREF_AIR_TEMP_SUNRISE = "weather.tempAtSunrise";
    public static final String PREF_AIR_TEMP_1200 = "weather.tempAtNoon";
    public static final String PREF_AIR_TEMP_1400 = "weather.tempAt1400";
    public static final String PREF_AIR_TEMP_SUNSET = "weather.tempAtSunset";
    // Defaults for temp controls (F)
    static final int DEFAULT_AIR_TEMP_SUNRISE = 65;
    static final int DEFAULT_AIR_TEMP_1200 = 80;
    static final int DEFAULT_AIR_TEMP_1400 = 82;
    static final int DEFAULT_AIR_TEMP_SUNSET = 75;
    // Humidity Preferences property keys
    public static final String PREF_RH_SUNRISE = "weather.rhAtSunrise";
    public static final String PREF_RH_1200 = "weather.rhAtNoon";
    public static final String PREF_RH_1400 = "weather.rhAt1400";
    public static final String PREF_RH_SUNSET = "weather.rhAtSunset";
    // Defaults for rh controls (%)
    static final int DEFAULT_RH_SUNRISE = 20;
    static final int DEFAULT_RH_1200 = 7;
    static final int DEFAULT_RH_1400 = 6;
    static final int DEFAULT_RH_SUNSET = 8;

    private static final HashMap<String, Unit> tempUnits = new HashMap<>();
    private static final HashMap<String, Unit> windUnits = new HashMap<>();

    static {
        tempUnits.put(UOM_FAHRENHEIT, GeneralUnit.degF);
        tempUnits.put(UOM_CELSIUS, GeneralUnit.degC);

        windUnits.put(UOM_MPH, GeneralUnit.mph);
        windUnits.put(UOM_KPH, GeneralUnit.kph);
        windUnits.put(UOM_KTS, GeneralUnit.knot);
        windUnits.put(UOM_MPS, CommonUnit.meterPerSecond);
    }

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(WeatherOptions.class);

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

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public static Unit getAirTempUnit() {
        String uom = prefs.get(PREF_AIR_TEMP_UOM, DEFAULT_AIR_TEMP_UOM);
        return tempUnits.get(uom);
    }

    public static void setAirTempUnit(Unit unit) {
        String uom;
        if (unit.equals(GeneralUnit.degF)) {
            uom = UOM_FAHRENHEIT;
        } else if (unit.equals(GeneralUnit.degC)) {
            uom = UOM_CELSIUS;
        } else {
            throw new IllegalArgumentException("Invalid air temp unit: " + unit);
        }

        Unit oldUnit = getAirTempUnit();
        prefs.put(PREF_AIR_TEMP_UOM, uom);
        pcs.firePropertyChange(PREF_AIR_TEMP_UOM, oldUnit, getAirTempUnit());

    }


    public static Unit getWindSpeedUnit() {
        String uom = prefs.get(PREF_WIND_SPD_UOM, DEFAULT_WIND_SPD_UOM);
        return windUnits.get(uom);
    }
    
    public static void setWindSpeedUnit(Unit unit) {
        String uom;
        if (unit.equals(GeneralUnit.mph)) {
            uom = UOM_MPH;
        } else if (unit.equals(GeneralUnit.kph)) {
            uom = UOM_KPH;
        } else if (unit.equals(GeneralUnit.knot)) {
            uom = UOM_KTS;
        } else if (unit.equals(CommonUnit.meterPerSecond)) {
            uom = UOM_MPS;
        } else {
            throw new IllegalArgumentException("Invalid wind speed unit: " + unit);
        }

        Unit oldUnit = getWindSpeedUnit();
        prefs.put(PREF_WIND_SPD_UOM, uom);
        pcs.firePropertyChange(PREF_WIND_SPD_UOM, oldUnit, getWindSpeedUnit());

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
        return getAirTempUnit().equals(degF) ? airTempF : Reals.convertTo(WeatherType.AIR_TEMP_C, airTempF);
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
