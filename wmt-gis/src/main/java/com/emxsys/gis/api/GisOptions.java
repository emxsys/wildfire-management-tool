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
package com.emxsys.gis.api;

import com.emxsys.visad.GeneralUnit;
import java.util.HashMap;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import visad.CommonUnit;
import visad.Unit;

/**
 * GIS preferences.
 *
 * @author Bruce Schubert
 */
public class GisOptions {

    // Preference key and uoms
    public static final String PREF_AREA_UOM = "gis.area.uom";
    public static final String PREF_DISTANCE_UOM = "gis.distance.uom";
    public static final String PREF_ELEVATION_UOM = "gis.elevation.uom";
    // Distance and elevation
    public static final String UOM_CHAINS = "chains";
    public static final String UOM_FEET = "feet";
    public static final String UOM_KILOMETERS = "kilometers";
    public static final String UOM_METERS = "meters";
    public static final String UOM_MILES = "miles";
    // Area
    public static final String UOM_ACRES = "acres";
    public static final String UOM_HECTARES = "hectares";

    // Defaults
    static final String DEFAULT_AREA_UOM = UOM_ACRES;
    static final String DEFAULT_DISTANCE_UOM = UOM_MILES;
    static final String DEFAULT_ELEVATION_UOM = UOM_FEET;

    // Implementation
    private static final HashMap<String, Unit> areaUnits = new HashMap<>();
    private static final HashMap<String, Unit> distUnits = new HashMap<>();
    private static final HashMap<String, Unit> elevUnits = new HashMap<>();
    private static final Preferences prefs = NbPreferences.forModule(GisOptions.class);

    static {
        // Build a map of permissible area UOMs
        areaUnits.put(UOM_ACRES, GeneralUnit.acre);
        areaUnits.put(UOM_HECTARES, GeneralUnit.hectare);

        // Build a map of permissible distance UOMs
        distUnits.put(UOM_CHAINS, GeneralUnit.chain);
        distUnits.put(UOM_FEET, GeneralUnit.foot);
        distUnits.put(UOM_KILOMETERS, GeneralUnit.kilometer);
        distUnits.put(UOM_METERS, CommonUnit.meter);
        distUnits.put(UOM_MILES, GeneralUnit.mile);

        // Build a map of permissible elevation UOMs
        elevUnits.put(UOM_FEET, GeneralUnit.foot);
        elevUnits.put(UOM_METERS, CommonUnit.meter);
    }

    /**
     * Adds a PreferenceChangeListener to the underlying WildfireOptions preferences file.
     * @param listener
     */
    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.removePreferenceChangeListener(listener);
    }

    /**
     * @return The current unit of measure for area.
     */
    public static String getAreaUom() {
        return prefs.get(PREF_AREA_UOM, DEFAULT_AREA_UOM);
    }

    /**
     * @return The preferred Unit for area.
     */
    public static Unit getAreaUnit() {
        return areaUomToUnit(getAreaUom());
    }

    /**
     * Sets the area unit of measure preference.
     * @param uom One of: UOM_ACRES, UOM_HECTARES.
     */
    public static void setAreaUom(String uom) {
        if (areaUnits.get(uom) == null) {
            throw new IllegalArgumentException("Invalid area UOM: " + uom);
        }
        prefs.put(PREF_AREA_UOM, uom);
    }

    /**
     * @return The preferred unit of measure for distance.
     */
    public static String getDistanceUom() {
        return prefs.get(PREF_DISTANCE_UOM, DEFAULT_DISTANCE_UOM);
    }

    /**
     * @return The preferred Unit for distance.
     */
    public static Unit getDistanceUnit() {
        return distanceUomToUnit(getDistanceUom());
    }

    /**
     * Sets the distance unit of measure preference.
     * @param uom One of: UOM_CHAINS, UOM_FEET, UOM_KILOMETERS, UOM_METERS, UOM_MILES.
     */
    public static void setDistanceUom(String uom) {
        if (distUnits.get(uom) == null) {
            throw new IllegalArgumentException("Invalid distance UOM: " + uom);
        }
        prefs.put(PREF_DISTANCE_UOM, uom);
    }

    /**
     * @return The preferred unit of measure for elevation.
     */
    public static String getElevationUom() {
        return prefs.get(PREF_ELEVATION_UOM, DEFAULT_ELEVATION_UOM);
    }

    /**
     * @return The preferred Unit for elevation.
     */
    public static Unit getElevationUnit() {
        return elevationUomToUnit(getElevationUom());
    }

    /**
     * Sets the elevation unit of measure preference.
     * @param uom One of: UOM_CHAINS, UOM_FEET, UOM_KILOMETERS, UOM_METERS, UOM_MILES.
     */
    public static void setElevationUom(String uom) {
        if (distUnits.get(uom) == null) {
            throw new IllegalArgumentException("Invalid elevation UOM: " + uom);
        }
        prefs.put(PREF_ELEVATION_UOM, uom);
    }

    public static Unit areaUomToUnit(String uom) {
        Unit unit = areaUnits.get(uom);
        if (unit == null) {
            throw new IllegalArgumentException("Invalid area uom: " + uom);
        }
        return unit;
    }

    public static Unit distanceUomToUnit(String uom) {
        Unit unit = distUnits.get(uom);
        if (unit == null) {
            throw new IllegalArgumentException("Invalid distance uom: " + uom);
        }
        return unit;
    }

    public static Unit elevationUomToUnit(String uom) {
        Unit unit = elevUnits.get(uom);
        if (unit == null) {
            throw new IllegalArgumentException("Invalid elevation uom: " + uom);
        }
        return unit;
    }

}
