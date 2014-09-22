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
public class WildfireOptions {

    // Preference key and uoms
    public static final String PREF_ROS_UOM = "wildfire.ros.uom";
    public static final String UOM_CHAINS = "chains";
    public static final String UOM_MPH = "mph";
    public static final String UOM_MPS = "mps";
    public static final String UOM_KPH = "kph";

    // Defaults
    static final String DEFAULT_ROS_UOM = UOM_CHAINS;

    // Implementation
    private static final HashMap<String, Unit> rosUnits = new HashMap<>();
    private static final Preferences prefs = NbPreferences.forModule(WildfireOptions.class);

    static {
        rosUnits.put(UOM_CHAINS, FireUnit.chain_hour);
        rosUnits.put(UOM_MPH, GeneralUnit.mph);
        rosUnits.put(UOM_MPS, CommonUnit.meterPerSecond);
        rosUnits.put(UOM_KPH, GeneralUnit.kph);
    }

    /**
     * Adds a PreferenceChangeListener to the underlying Wildfire module preferences file.
     * @param listener 
     */
    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.removePreferenceChangeListener(listener);
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
        String uom = prefs.get(PREF_ROS_UOM, DEFAULT_ROS_UOM);
        return rateOfSpreadUomToUnit(uom);
    }
    
    /**
     * Sets the Rate of Spread UOM preference.
     * @param uom One of: UOM_CHAINS, UOM_MPH, UOM_KPH, UOM_MPS.
     */
    public static void setRateOfSpread(String uom) {
        if (rosUnits.get(uom)==null) {
            throw new IllegalArgumentException("Invalid ROS UOM: " + uom);
        }
        prefs.put(PREF_ROS_UOM, uom);
    }

    public static Unit rateOfSpreadUomToUnit(String uom) {
        return rosUnits.get(uom);
    }

}
