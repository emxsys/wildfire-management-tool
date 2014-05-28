/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.solar.api;

import com.emxsys.visad.GeneralUnit;
import com.emxsys.visad.Reals;
import visad.CommonUnit;
import visad.RealTupleType;
import visad.RealType;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: SolarType.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public class SolarType {

    /** Time - UTC */
    public static final RealType TIME;
    /** Sunrise - UTC */
    public static final RealType SUNRISE_TIME;
    /** Sunset - UTC */
    public static final RealType SUNSET_TIME;
    /** Sunrise - local time [hours] */
    public static final RealType SUNRISE_HOUR;
    /** Sunset - local time [hours] */
    public static final RealType SUNSET_HOUR;
    /** Sunrise - angular offset from solar noon [degrees] */
    public static final RealType SUNRISE_HOUR_ANGLE;
    /** Sunset - angular offset from solar noon [degrees] */
    public static final RealType SUNSET_HOUR_ANGLE;
    public static final RealType RIGHT_ASCENSION;
    public static final RealType DECLINATION;
    /** Hour Angle - angular offset from solar noon */
    public static final RealType HOUR_ANGLE;
    public static final RealType LATITUDE;
    public static final RealType SUBSOLAR_LATITUDE;
    public static final RealType SUBSOLAR_LONGITUDE;
    public static final RealType LONGITUDE;
    public static final RealType ALTITUDE_ANGLE;
    public static final RealType AZIMUTH_ANGLE;
    public static final RealType ZENITH_ANGLE;
    public static final RealTupleType HORIZON_COORDINATES;
    public static final RealTupleType EQUATORIAL_COORDINATES;
    public static final RealTupleType SUBSOLAR_POINT;
    public static final RealTupleType SUN_POSITION;
    public final static RealTupleType SUNLIGHT;

    static {
        TIME = RealType.getRealType("local_time:sec", CommonUnit.secondsSinceTheEpoch, null);
        SUNRISE_TIME = RealType.getRealType("sunrise:utc", CommonUnit.secondsSinceTheEpoch, null);
        SUNSET_TIME = RealType.getRealType("sunset:utc", CommonUnit.secondsSinceTheEpoch, null);
        SUNRISE_HOUR = RealType.getRealType("sunrise:hour", GeneralUnit.hour, null);
        SUNSET_HOUR = RealType.getRealType("sunset:hour", GeneralUnit.hour, null);
        SUNRISE_HOUR_ANGLE = RealType.getRealType("sunrise_hour:deg", CommonUnit.degree, null);
        SUNSET_HOUR_ANGLE = RealType.getRealType("sunset_hour:deg", CommonUnit.degree, null);
        RIGHT_ASCENSION = RealType.getRealType("right_ascention:deg", CommonUnit.degree, null);
        DECLINATION = RealType.getRealType("declination:deg", CommonUnit.degree, null);
        HOUR_ANGLE = RealType.getRealType("hour:deg", CommonUnit.degree, null);
        LATITUDE = RealType.getRealType("latitude:deg", CommonUnit.degree, null);
        LONGITUDE = RealType.getRealType("longitude:deg", CommonUnit.degree, null);
        SUBSOLAR_LATITUDE = RealType.getRealType("subsolar_lat:deg", CommonUnit.degree, null);
        SUBSOLAR_LONGITUDE = RealType.getRealType("subsolar_lon:deg", CommonUnit.degree, null);
        ALTITUDE_ANGLE = RealType.getRealType("altitude:deg", CommonUnit.degree, null);
        AZIMUTH_ANGLE = RealType.getRealType("azimuth:deg", CommonUnit.degree, null);
        ZENITH_ANGLE = RealType.getRealType("zenith:deg", CommonUnit.degree, null);
        HORIZON_COORDINATES = Reals.newRealTupleType(
                new RealType[]{
                    AZIMUTH_ANGLE, ALTITUDE_ANGLE
                });
        EQUATORIAL_COORDINATES = Reals.newRealTupleType(
                new RealType[]{
                    RIGHT_ASCENSION, DECLINATION
                });
        SUBSOLAR_POINT = Reals.newRealTupleType(
                new RealType[]{
                    SUBSOLAR_LATITUDE, SUBSOLAR_LONGITUDE
                });
        SUN_POSITION = Reals.newRealTupleType(
                new RealType[]{
                    SUBSOLAR_LATITUDE, SUBSOLAR_LONGITUDE, AZIMUTH_ANGLE, ZENITH_ANGLE,
                });
        SUNLIGHT = Reals.newRealTupleType(
                new RealType[]{
                    SUBSOLAR_LATITUDE, SUBSOLAR_LONGITUDE, AZIMUTH_ANGLE, ZENITH_ANGLE, SUNRISE_HOUR, SUNSET_HOUR, SUNRISE_HOUR_ANGLE, SUNSET_HOUR_ANGLE, 
                });

    }

}
