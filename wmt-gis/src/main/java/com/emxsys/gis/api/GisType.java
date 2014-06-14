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
package com.emxsys.gis.api;

import com.emxsys.visad.Reals;
import com.emxsys.visad.Units;
import visad.CommonUnit;
import visad.RealTupleType;
import visad.RealType;

/**
 * This utility class provides easy access to common Real types used in the GIS API.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class GisType {

    /** Angle in degrees - RealType: <b>angle:deg</b>. */
    public static final RealType ANGLE;
    /** Distance in meters - RealType: <b>distance:m</b>. */
    public static final RealType DISTANCE;
    /** Area in hectares (1 ha = 10,000 m2, 100m x100m) - RealType: <b>area:ha</b>. */
    public static final RealType AREA;
    /** Latitude in degrees - RealType: <b>latitude:deg</b>. */
    public static final RealType LAT;
    /** Longitude in degrees - RealType: <b>longitude:deg</b>. */
    public static final RealType LON;
    /** Altitude in meters: RealType: <b>altitude:m</b>. */
    public static final RealType ALT;
    /** Latitude/Longitude tuple: RealTupleType: <b>[LAT,LON]</b>. */
    public final static RealTupleType LATLON;
    /** Latitude/Longitude/Altitude tuple: RealTupleType: <b>[LAT,LON,ALT]</b>. */
    public final static RealTupleType LATLONALT;
    /** Terrain aspect in degrees: - RealType: <b>aspect:deg</b>. */
    public static final RealType ASPECT;
    /** Terrain slope in degrees: - RealType: <b>slope:deg</b>. */
    public static final RealType SLOPE;
    /** Terrain elevation in meters: - RealType: <b>elevation:m</b>. */
    public static final RealType ELEVATION;
    /** Terrain aspect,slope,elevation tuple: RealTupleType: <b>[ASPECT,SLOPE,ELEVATION]</b>. */
    public final static RealTupleType TERRAIN;

    /**
     * RealType initializer
     */
    static {
        // Angle types
        ANGLE = RealType.getRealType("angle:deg", CommonUnit.degree, null); 

        // Distance types
        DISTANCE = RealType.getRealType("distance:m", CommonUnit.meter, null);

        // Area types
        AREA = RealType.getRealType("area:ha", Units.getUnit("hectare"), null); // 10000 m2 (100m x 100m)

        // Geographic coordinate types
        LAT = RealType.Latitude;    // aliases for system intrisic types
        LON = RealType.Longitude;
        ALT = RealType.Altitude;
//        LAT = RealType.getRealType("latitude:deg", CommonUnit.degree, null);
//        LON = RealType.getRealType("longitude:deg", CommonUnit.degree, null);
//        ALT = RealType.getRealType("altitude:m", CommonUnit.meter, null);

        // Terrain types
        ASPECT = RealType.getRealType("aspect:deg", CommonUnit.degree, null);
        SLOPE = RealType.getRealType("slope:deg", CommonUnit.degree, null);
        ELEVATION = RealType.getRealType("elevation:m", CommonUnit.meter, null);

        // Tuple types
        // Create RealTupleTypes from arrays (the Reals utility class will catch exceptions)        
        LATLON = Reals.newRealTupleType(new RealType[]{
            LAT, LON
        });
        LATLONALT = Reals.newRealTupleType(new RealType[]{
            LAT, LON, ALT
        });
        TERRAIN = Reals.newRealTupleType(
                new RealType[]{
                    ASPECT, SLOPE, ELEVATION
                });
    }

    private GisType() {
    }
}
