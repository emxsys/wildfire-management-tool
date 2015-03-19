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
package com.emxsys.wmt.cps.options;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * CPS Options
 *
 * @author Bruce Schubert
 */
public class CpsOptions {

    public static final String TERRESTRAL_SHADING_ENABLED = "wmt.cps.terrestrial.shading.enabled";
    public static final boolean DEFAULT_TERRESTRAL_SHADING = true;
    // Unit of Measure property key and values
    public static final String UOM_KEY = "wmt.cps.uom";
    public static final String UOM_SI = "si";
    public static final String UOM_METRIC = "metric";
    public static final String UOM_US = "us";
    public static final String DEFAULT_UOM = UOM_US;
    // Air Temp Preferences property keys
    public static final String TEMP_SUNRISE_KEY = "wmt.cps.tempAtSunrise";
    public static final String TEMP_1200_KEY = "wmt.cps.tempAtNoon";
    public static final String TEMP_1400_KEY = "wmt.cps.tempAt1400";
    public static final String TEMP_SUNSET_KEY = "wmt.cps.tempAtSunset";
    // Humidity Preferences property keys
    public static final String RH_SUNRISE_KEY = "wmt.cps.rhAtSunrise";
    public static final String RH_1200_KEY = "wmt.cps.rhAtNoon";
    public static final String RH_1400_KEY = "wmt.cps.rhAt1400";
    public static final String RH_SUNSET_KEY = "wmt.cps.rhAtSunset";
    // Defaults for rh controls
    public static final int DEFAULT_RH_SUNRISE = 20;
    public static final int DEFAULT_RH_1200 = 7;
    public static final int DEFAULT_RH_1400 = 6;
    public static final int DEFAULT_RH_SUNSET = 8;
    // Defaults for temp controls (F)
    public static final int DEFAULT_TEMP_SUNRISE = 65;
    public static final int DEFAULT_TEMP_1200 = 80;
    public static final int DEFAULT_TEMP_1400 = 82;
    public static final int DEFAULT_TEMP_SUNSET = 75;

    private static Preferences prefs = NbPreferences.forModule(CpsOptions.class);

    public static String getUom() {
        return prefs.get(UOM_KEY, DEFAULT_UOM);
    }

}
