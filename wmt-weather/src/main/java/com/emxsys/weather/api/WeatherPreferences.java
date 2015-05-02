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
package com.emxsys.weather.api;

import com.emxsys.visad.GeneralUnit;
import static com.emxsys.visad.GeneralUnit.degF;
import com.emxsys.visad.Reals;
import com.emxsys.weather.api.WeatherType;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_KTS;
import static java.lang.Math.round;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import visad.CommonUnit;
import visad.Real;
import visad.RealType;
import visad.Unit;
import visad.VisADException;

/**
 * Weather Options
 *
 * @author Bruce Schubert
 */
public class WeatherPreferences {

    private static final Preferences prefs = NbPreferences.forModule(WeatherPreferences.class);

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
    static final int DEFAULT_RH_SUNRISE = 60;
    static final int DEFAULT_RH_1200 = 25;
    static final int DEFAULT_RH_1400 = 20;
    static final int DEFAULT_RH_SUNSET = 40;
    // Winds Preferences property keys
    public static final String PREF_WIND_SPEEDS = "weather.wind.speeds";
    public static final String PREF_WIND_DIRECTIONS = "weather.wind.directions";
    public static final String PREF_CLOUD_COVERS = "weather.cloudCover";

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

    /**
     * Adds a PreferenceChangeListener to the underlying WildfireOptions preferences file.
     * @param listener
     */
    public static void addPreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.addPreferenceChangeListener(listener);
    }

    public static void removePreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.removePreferenceChangeListener(listener);
    }

    /**
     *
     * @return The unit of measure preference for air temperature.
     */
    public static String getAirTempUom() {
        return prefs.get(PREF_AIR_TEMP_UOM, DEFAULT_AIR_TEMP_UOM);
    }

    /**
     *
     * @return The current Unit for air temperature.
     */
    public static Unit getAirTempUnit() {
        return airTempUomToUnit(getAirTempUom());
    }

    /**
     * Sets the air temperature UOM preference from a VisAD Unit.
     *
     * @param unit Air Temperature Unit
     */
    public static void setAirTempUnit(Unit unit) {
        if (unit.equals(GeneralUnit.degF)) {
            setAirTempUom(UOM_FAHRENHEIT);
        } else if (unit.equals(GeneralUnit.degC)) {
            setAirTempUom(WeatherPreferences.UOM_CELSIUS);
        } else {
            throw new IllegalArgumentException("Invalid air temp UOM: " + unit.toString());
        }
    }

    /**
     * Sets the air temperature unit of measure preference.
     * @param uom One of: UOM_FAHRENHEIT, UOM_CELSIUS.
     */
    public static void setAirTempUom(String uom) {
        if (tempUnits.get(uom) == null) {
            throw new IllegalArgumentException("Invalid air temp UOM: " + uom);
        }
        prefs.put(PREF_AIR_TEMP_UOM, uom);
    }

    /**
     *
     * @return The unit of measure preference for air temperature wind speed.
     */
    public static String getWindSpeedUom() {
        return prefs.get(PREF_WIND_SPD_UOM, DEFAULT_WIND_SPD_UOM);
    }

    /**
     * Sets the air temperature unit of measure preference.
     * @param uom One of: UOM_MPH, UOM_KPH, UOM_KTS, UOM_MPS.
     */
    public static void setWindSpeedUom(String uom) {
        if (windUnits.get(uom) == null) {
            throw new IllegalArgumentException("Invalid wind speed UOM: " + uom);
        }
        prefs.put(PREF_WIND_SPD_UOM, uom);
    }

    /**
     *
     * @return The current Unit for wind speed.
     */
    public static Unit getWindSpeedUnit() {
        return windSpeedUomToUnit(getWindSpeedUom());
    }

    /**
     * Sets the wind speed UOM from a VisAD Unit.
     * @param unit Wind Speed Unit
     */
    public static void setWindSpeedUnit(Unit unit) {
        if (unit.equals(GeneralUnit.mph)) {
            setWindSpeedUom(UOM_MPH);
        } else if (unit.equals(GeneralUnit.kph)) {
            setWindSpeedUom(UOM_KPH);
        } else if (unit.equals(GeneralUnit.knot)) {
            setWindSpeedUom(UOM_KTS);
        } else if (unit.equals(CommonUnit.meterPerSecond)) {
            setWindSpeedUom(UOM_MPS);
        } else {
            throw new IllegalArgumentException("Invalid wind speed UOM: " + unit.toString());
        }
    }

    public static Real getAirTempValue(String key) {
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

    public static void setAirTempValue(String key, Real value) {
        // Store the temp in Fahreheit
        Real airTempF = Reals.convertTo(WeatherType.AIR_TEMP_F, value);
        prefs.putInt(key, (int) round(airTempF.getValue()));
    }

    public static Real getRelHumidityValue(String key) {
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

    public static void setRelHumidityValue(String key, int value) {
        prefs.putInt(key, value);
    }

    public static Unit airTempUomToUnit(String uom) {
        Unit unit = tempUnits.get(uom);
        if (unit == null) {
            throw new IllegalArgumentException("Invalid air temperature uom: " + uom);
        }
        return unit;
    }

    public static Unit windSpeedUomToUnit(String uom) {
        Unit unit = windUnits.get(uom);
        if (unit == null) {
            throw new IllegalArgumentException("Invalid wind speed uom: " + uom);
        }
        return unit;
    }

    public static TreeMap<LocalTime, Real> getDiurnalWindSpeeds() {
        String winds = prefs.get(PREF_WIND_SPEEDS, "");
        if (!winds.isEmpty()) {
            return stringToTreeMap(winds, WeatherType.WIND_SPEED_KTS);
        } else {
            return new TreeMap<>();
        }
    }

    public static void setDiurnalWindSpeeds(TreeMap<LocalTime, Real> speeds) {
        prefs.put(PREF_WIND_SPEEDS, treeMapToString(speeds, GeneralUnit.knot));
    }

    public static TreeMap<LocalTime, Real> getDiurnalWindDirs() {
        String winds = prefs.get(PREF_WIND_DIRECTIONS, "");
        if (!winds.isEmpty()) {
            return stringToTreeMap(winds, WeatherType.WIND_DIR);
        } else {
            return new TreeMap<>();
        }
    }

    public static void setDiurnalWindDirections(TreeMap<LocalTime, Real> dirs) {
        prefs.put(PREF_WIND_DIRECTIONS, treeMapToString(dirs, CommonUnit.degree));
    }

    public static TreeMap<LocalTime, Real> getDiurnalClouds() {
        String winds = prefs.get(PREF_CLOUD_COVERS, "");
        if (!winds.isEmpty()) {
            return stringToTreeMap(winds, WeatherType.CLOUD_COVER);
        } else {
            return new TreeMap<>();
        }
    }

    public static void setDiurnalClouds(TreeMap<LocalTime, Real> speeds) {
        prefs.put(PREF_CLOUD_COVERS, treeMapToString(speeds, GeneralUnit.percent));
    }

    private static String treeMapToString(TreeMap<LocalTime, Real> map, Unit uom) {
        // TODO: Populate diurnal wind speeds
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<LocalTime, Real>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            try {
                Entry<LocalTime, Real> entry = iterator.next();
                sb.append(entry.getKey().toString());
                sb.append("=");
                sb.append(entry.getValue().getValue(uom));
                sb.append(",");
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return sb.toString();
    }

    private static TreeMap<LocalTime, Real> stringToTreeMap(String string, RealType type) {
        TreeMap<LocalTime, Real> map = new TreeMap<>();
        if (!string.isEmpty()) {
            String[] pairs = string.split(",");
            for (String pair : pairs) {
                String[] values = pair.split("=");
                LocalTime time = LocalTime.parse(values[0]);
                Double val = Double.valueOf(values[1]);
                Real real = new Real(type, val);
                map.put(time, real);
            }
        }
        return map;
    }
}
