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
package com.emxsys.util;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class AngleUtil {

    private AngleUtil() {
    }

    /**
     * Computes the shortest distance between two angles.
     *
     * @param angle1Degrees
     * @param angle2Degrees
     * @return The angular distance in degrees.
     */
    public static double angularDistanceBetween(double angle1Degrees, double angle2Degrees) {
        double differenceDegrees = angle1Degrees - angle2Degrees;
        while (differenceDegrees < -180) {
            differenceDegrees += 360;
        }
        while (differenceDegrees > 180) {
            differenceDegrees -= 360;
        }
        return Math.abs(differenceDegrees);
    }

    public static boolean crossesMeridian(double angle1Degrees, double angle2Degrees) {
        // An angular chord crosses a merdian (prime merdian or date line) if end pos have different
        // longitude signs and are more than 180 degrees longitude apart
        return (Math.signum(angle1Degrees) != Math.signum(angle2Degrees))
                && (Math.abs(angle1Degrees - angle2Degrees) > 180);
    }

    /**
     * Normalizes an angle to greater than or equal to zero and less than 360.
     *
     * @param degrees a positive or negative angle in degrees
     * @return an angle between 0 and 360.
     */
    public static double normalize360(double degrees) {
        while (degrees < 0.0) {
            degrees += 360;
        }
        while (degrees >= 360.0) {
            degrees -= 360;
        }
        return degrees;
    }

    /**
     * Returns one of the eight cardinal points for the angle.
     *
     * @param degrees
     * @return "N", "NE", "E", "SE", "S", "SW", "W" or "NW"
     */
    public static String degreesToCardinalPoint8(double degrees) {
        final String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        degrees = normalize360(degrees);
        return directions[(int) Math.round(degrees / 45)];
    }

    /**
     * Returns one of the 16 cardinal points for the angle.
     *
     * @param degrees
     * @return "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W",
     * "WNW", "NW" or "NNW"
     */
    public static String degreesToCardinalPoint16(double degrees) {
        final String directions[]
                = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N"};
        degrees = normalize360(degrees);
        return directions[(int) Math.round(degrees / 22.5)];
    }
}
