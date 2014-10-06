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
package com.emxsys.wildfire.api;

import com.emxsys.visad.FireUnit;
import com.emxsys.visad.GeneralUnit;
import java.util.HashMap;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import visad.CommonUnit;
import visad.Unit;

/**
 * Wildfire preferences.
 *
 * @author Bruce Schubert
 */
public class WildfirePreferences {

    // Preference key and uoms
    public static final String PREF_BYRAMS_INTENSITY_UOM = "wildfire.byrams.uom";
    public static final String PREF_HEAT_RELEASE_UOM = "wildfire.heat.uom";
    public static final String PREF_FLAME_LENGTH_UOM = "wildfire.fln.uom";
    public static final String PREF_FUEL_TEMP_UOM = "wildfire.fuel_temp.uom";
    public static final String PREF_ROS_UOM = "wildfire.ros.uom";
    public static final String UOM_CHAINS = "chains";
    public static final String UOM_MPH = "mph";
    public static final String UOM_MPS = "mps";
    public static final String UOM_KPH = "kph";
    public static final String UOM_FAHRENHEIT = "fahrenheit";
    public static final String UOM_CELSIUS = "celsius";
    public static final String UOM_FOOT = "foot";
    public static final String UOM_METER = "meter";
    public static final String UOM_KJ_PER_M2 = "kJ/m^2";
    public static final String UOM_BTU_PER_FT2 = "btu/ft^2";
    public static final String UOM_KW_PER_METER = "kW/m";
    public static final String UOM_BTU_PER_FOOT_PER_SEC = "btu/ft/s";

    // Defaults
    static final String DEFAULT_BYRAMS_INTENSITY_UOM = UOM_BTU_PER_FOOT_PER_SEC;
    static final String DEFAULT_HEAT_RELEASE_UOM = UOM_BTU_PER_FT2;
    static final String DEFAULT_FUEL_TEMP_UOM = UOM_FAHRENHEIT;
    static final String DEFAULT_FLAME_LENGTH_UOM = UOM_FOOT;
    static final String DEFAULT_ROS_UOM = UOM_CHAINS;

    // Implementation
    private static final HashMap<String, Unit> rosUnits = new HashMap<>();
    private static final HashMap<String, Unit> flnUnits = new HashMap<>();
    private static final HashMap<String, Unit> heatUnits = new HashMap<>();
    private static final HashMap<String, Unit> fliUnits = new HashMap<>();
    private static final Preferences prefs = NbPreferences.forModule(WildfirePreferences.class);

    static {
        rosUnits.put(UOM_CHAINS, FireUnit.chain_hour);
        rosUnits.put(UOM_MPH, GeneralUnit.mph);
        rosUnits.put(UOM_MPS, CommonUnit.meterPerSecond);
        rosUnits.put(UOM_KPH, GeneralUnit.kph);

        flnUnits.put(UOM_FOOT, GeneralUnit.foot);
        flnUnits.put(UOM_METER, CommonUnit.meter);

        heatUnits.put(UOM_KJ_PER_M2, FireUnit.kJ_m2);
        heatUnits.put(UOM_BTU_PER_FT2, FireUnit.Btu_ft2);

        fliUnits.put(UOM_KW_PER_METER, FireUnit.kW_m);
        fliUnits.put(UOM_BTU_PER_FOOT_PER_SEC, FireUnit.Btu_ft_s);
    }

    /**
     * Adds a PreferenceChangeListener to the underlying Wildfire module preferences file.
     * @param listener
     */
    public static void addPreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.addPreferenceChangeListener(listener);
    }

    public static void removePreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.removePreferenceChangeListener(listener);
    }

    /**
     * @return The current unit of measure for fuel temperature.
     */
    public static String getFuelTemperatureUom() {
        return prefs.get(PREF_FUEL_TEMP_UOM, DEFAULT_FUEL_TEMP_UOM);
    }

    /**
     * @return The current Unit for fuel temperature
     */
    public static Unit getFuelTemperatureUnit() {
        String uom = getFuelTemperatureUom();
        switch (uom) {
            case UOM_FAHRENHEIT:
                return GeneralUnit.degF;
            case UOM_CELSIUS:
                return GeneralUnit.degC;
            default:
                throw new IllegalStateException("Unhandled fuel temp uom: " + uom);
        }
    }

    /**
     * @return The current unit of measure for rate of spread.
     */
    public static String getRateOfSpreadUom() {
        return prefs.get(PREF_ROS_UOM, DEFAULT_ROS_UOM);
    }

    /**
     * @return The current Unit for rate of spread.
     */
    public static Unit getRateOfSpreadUnit() {
        return rateOfSpreadUomToUnit(getRateOfSpreadUom());
    }

    /**
     * Typically used by preference change listeners.
     * @param uom
     * @return
     */
    public static Unit rateOfSpreadUomToUnit(String uom) {
        return rosUnits.get(uom);
    }

    /**
     * Sets the Rate of Spread UOM preference.
     * @param uom One of: UOM_CHAINS, UOM_MPH, UOM_KPH, UOM_MPS.
     */
    public static void setRateOfSpread(String uom) {
        if (rosUnits.get(uom) == null) {
            throw new IllegalArgumentException("Invalid ROS UOM: " + uom);
        }
        prefs.put(PREF_ROS_UOM, uom);
    }

    /**
     * @return The current unit of measure for flame length.
     */
    public static String getFlameLengthUom() {
        return prefs.get(PREF_FLAME_LENGTH_UOM, DEFAULT_FLAME_LENGTH_UOM);
    }

    /**
     * @return The current Unit for flame length.
     */
    public static Unit getFlameLengthUnit() {
        return flameLengthUomToUnit(getFlameLengthUom());
    }

    /**
     * Sets the Flame Length UOM preference.
     * @param uom One of: UOM_FOOT or UOM_METER
     */
    public static void setFlameLengthUom(String uom) {
        if (flnUnits.get(uom) == null) {
            throw new IllegalArgumentException("Invalid Flame Length UOM: " + uom);
        }
        prefs.put(PREF_FLAME_LENGTH_UOM, uom);
    }

    /**
     * Typically used by preference change listeners.
     * @param uom 
     * @return
     */
    public static Unit flameLengthUomToUnit(String uom) {
        return flnUnits.get(uom);
    }

    /**
     * @return The current unit of measure for heat release.
     */
    public static String getHeatReleaseUom() {
        return prefs.get(PREF_HEAT_RELEASE_UOM, DEFAULT_HEAT_RELEASE_UOM);
    }

    /**
     * @return The current Unit for heat release.
     */
    public static Unit getHeatReleaseUnit() {
        return heatReleaseUomToUnit(getHeatReleaseUom());
    }

    /**
     * Sets the Heat Release UOM preference.
     * @param uom One of: UOM_KJ_PER_M2 or UOM_BTU_PER_FT2
     */
    public static void setHeatReleaseUom(String uom) {
        if (heatUnits.get(uom) == null) {
            throw new IllegalArgumentException("Invalid Heat Release UOM: " + uom);
        }
        prefs.put(PREF_HEAT_RELEASE_UOM, uom);
    }

    /**
     * Typically used by preference change listeners.
     * @param uom
     * @return
     */
    public static Unit heatReleaseUomToUnit(String uom) {
        return heatUnits.get(uom);
    }

    /**
     * @return The current unit of measure for Byram's intensity.
     */
    public static String getByramsIntensityUom() {
        return prefs.get(PREF_BYRAMS_INTENSITY_UOM, DEFAULT_BYRAMS_INTENSITY_UOM);
    }

    /**
     * @return The current Unit for Byram's intensity.
     */
    public static Unit getByramsIntensityUnit() {
        return byramsIntensityUomToUnit(getByramsIntensityUom());
    }

    /**
     * Sets the Byram's Intensity UOM preference.
     * @param uom One of: UOM_KW_PER_METER or UOM_BTU_PER_FOOT_PER_SEC
     */
    public static void setByramsIntensityUom(String uom) {
        if (fliUnits.get(uom) == null) {
            throw new IllegalArgumentException("Invalid Byrams Intensity UOM: " + uom);
        }
        prefs.put(PREF_BYRAMS_INTENSITY_UOM, uom);
    }

    /**
     * Typically used by preference change listeners.
     * @param uom
     * @return
     */
    public static Unit byramsIntensityUomToUnit(String uom) {
        return fliUnits.get(uom);
    }

}
