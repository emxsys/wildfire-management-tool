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
     * Gets the declination: the earth's tilt angle relative to the sun at a given date and time. It
     * is also the latitude where the sun is directly overhead (subsolar point).
     * @return The declination angle [degrees].
     */
    Real getDeclination();

    /**
     * Gets the subsolar latitude: the earth latitude for where the sun is overhead at a given date
     * and time. This is identical to the declination.
     * @return [degrees]
     */
    Real getSubsolarLatitude();

    /**
     * Gets the subsolar longitude: the earth longitude for where the sun is overhead at a given
     * date and time.
     * @return [degrees]
     */
    Real getSubsolarLongitude();

    /**
     * Gets the solar azimuth angle: where the sun is relative to North.
     * @return The solar azimuth angle. [degrees]
     */
    Real getAzimuthAngle();

    /**
     * Gets the solar zenith angle: where is sun relative to the observer's zenith.
     * @return The solar zenith angle. [degrees]
     */
    Real getZenithAngle();

    /**
     * Gets the solar altitude angle: how high the sun is from the horizon.
     * @return The solar altitude angle (possibly corrected for atmospheric refraction). [degrees]
     */
    Real getAltitudeAngle();

    /**
     * Gets the local hour angle relative to the observer.
     * @return Hour angle between the sun and the observer. [degrees]
     */
    Real getLocalHourAngle();

    /**
     * Gets the solar hour angle for sunrise.
     * @return Sunrise hour angle from solar noon. [degrees]
     */
    Real getSunriseHourAngle();

    /**
     * Gets the solar hour angle for sunset.
     * @return Sunset hour angle from solar noon. [degrees]
     */
    Real getSunsetHourAngle();

    /**
     * Gets the local time of sunrise.
     * @return Sunrise time in the local time zone. [hours]
     */
    Real getSunriseHour();

    /**
     * Gets the local time of sunset.
     * @return Sunset time in the local time zone. [hours]
     */
    Real getSunsetHour();

    /**
     * Gets the local sun transit time (or solar noon).
     * @return The time of solar noon in local time zone. [hours]
     */
    Real getSunTransitHour();

    /**
     * Gets the time zone offset.
     * @return The offset from UTC to local time. [hours]
     */
    Real getZoneOffsetHour();

}
