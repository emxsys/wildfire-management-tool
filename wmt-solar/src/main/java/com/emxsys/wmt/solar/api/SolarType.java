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
package com.emxsys.wmt.solar.api;

import com.emxsys.wmt.visad.GeneralUnit;
import com.emxsys.wmt.visad.Reals;
import visad.CommonUnit;
import visad.RealTupleType;
import visad.RealType;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: SolarType.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public class SolarType {

    // Time
    public static final RealType TIME;
    public static final RealType SOLAR_HOUR;
    public static final RealType SUNRISE_HOUR;
    public static final RealType SUNSET_HOUR;
    public static final RealTupleType SUNSET_SUNRISE;
    public static final RealType LATITUDE;
    public static final RealType DECLINATION;
    public final static RealTupleType SOLAR_DATA;

    static {
        TIME = RealType.getRealType("local_time:sec", CommonUnit.secondsSinceTheEpoch, null);
        SOLAR_HOUR = RealType.getRealType("solar_time:hour", GeneralUnit.hour, null);
        SUNRISE_HOUR = RealType.getRealType("sunrise:hour", GeneralUnit.hour, null);
        SUNSET_HOUR = RealType.getRealType("sunset:hour", GeneralUnit.hour, null);
        SUNSET_SUNRISE = Reals.newRealTupleType(
                new RealType[]{
                    SOLAR_HOUR, SOLAR_HOUR
                });
        LATITUDE = RealType.getRealType("latitude:deg", CommonUnit.degree, null);
        DECLINATION = RealType.getRealType("declination:deg", CommonUnit.degree, null);
        SOLAR_DATA = Reals.newRealTupleType(
                new RealType[]{
                    LATITUDE, DECLINATION, TIME, TIME
                });

    }

}
