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

import com.emxsys.wmt.gis.api.Coord3D;
import java.util.Date;
import visad.Real;

/**
 * A SunlightProvider instance provides solar angles and times.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public interface SunlightProvider {

    /**
     * Gets the coordinate where the position of the sun is overhead at the given date and time.
     *
     * @param utcTime UTC time used to get sun's position
     * @return the position of the sun
     */
    Coord3D getSunPosition(Date utcTime);

    /**
     * Constructs a new Sunlight object represent the sun angles for a given date/time.
     *
     * @param utcTime a UTC date/time
     * @return a Sunlight object for the given date/time
     */
    Sunlight getSunlight(Date utcTime);

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
