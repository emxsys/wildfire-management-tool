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

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import java.time.ZonedDateTime;
import java.util.Date;
import visad.Real;
import visad.RealTuple;

/**
 * A SunlightProvider instance provides solar angles and times.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public interface SunlightProvider {

    /**
     * Gets the coordinate where the position of the sun is overhead at the given date and time. The
     * latitude is the same as declination, the longitude is the right ascension converted to a
     * terrestrial coordinate.
     *
     * @param time The time used to get sun's position.
     * @return The position of the sun as a latitude/longitude.
     */
    Coord3D getSunPosition(ZonedDateTime time);

    /**
     * Gets the subsolar point coordinates of the sun at the given date and time, and the 
     * horizonal coordinates relative to the observer.
     *
     * @param time The time used to get sun's position.
     * @param observer The coordinates of the observer.
     * @return A SolarType.SUN_POSITION tuple.
     */
    RealTuple getSunPosition(ZonedDateTime time, Coord3D observer);

    /**
     * Constructs a new Sunlight instance represent the sun angles for a given date/time and
     * location.
     *
     * @param time The time for the sunlight.
     * @param coord The location for the sunlight.
     * @return A Sunlight object.
     */
    Sunlight getSunlight(ZonedDateTime time, Coord2D coord);

    /**
     * Constructs a new SunlightHours object for a given longitude and date.
     *
     * @param latitude angular degrees north/south
     * @param utcTime a UTC date/time
     * @return a SunlightHours object for the given date and latitude
     */
    SunlightHours getSunlightHours(Real latitude, Date utcTime);

    /**
     * Constructs a new SunlightHours object for a given longitude and date.
     *
     * @param longitude angular degrees west/east
     * @param utcTime a UTC date/time
     * @return The solar hour for the given utcTime as a SolarType.SOLAR_HOUR
     */
    Real getSolarTime(Real longitude, Date utcTime);

}
