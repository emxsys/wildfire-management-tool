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

import visad.Data;
import visad.Real;

/**
 * A Sunlight instance contains key solar values pertaining to a date and an earth location.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public interface Sunlight extends Data {

    /**
     * Declination is the earth's tilt angle relative to the sun at a given date and time. It is
     * also the latitude where the sun is overhead.
     * @return The declination angle [degrees].
     */
    Real getDeclination();

    /**
     * Longitude is earth longitude for where the sun is overhead at a given date and time.  This is
     * roughly equivalent to the right ascension, but on the terrestrial sphere.
     * @return [degrees]
     */
    Real getLongitude();

    /**
     * Gets the solar altitude angle--how high is the sun from the horizon.
     * @return The solar altitude angle (A). [degrees]
     */
    Real getSolarAltitudeAngle();

    /**
     * Gets the solar azimuth angle--where is sun relative to North.
     * @return The solar azimuth angle (Z). [degrees]
     */
    Real getSolarAzimuthAngle();

    /**
     * Gets the time of sunrise in solar hours, where 0.00 is noon, 0600 is -6.0, and 1800 is +6.0.
     * @return Sunrise solar hour relative to solar noon. [hours]
     */
    Real getSunriseHour();

    /**
     * Gets the time of sunset in solar hours, where noon is 0.00, 0600 is -6.0, and 1800 is +6.0.
     * @return Sunset solar hour relative to solar noon. [hours]
     */
    Real getSunsetHour();

}
