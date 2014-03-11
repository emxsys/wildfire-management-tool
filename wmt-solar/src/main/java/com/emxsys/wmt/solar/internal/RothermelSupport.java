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
package com.emxsys.wmt.solar.internal;

import com.emxsys.wmt.visad.Times;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.CommonUnit;
import visad.DateTime;
import visad.Real;
import visad.RealType;
import static java.lang.Math.*;
import visad.VisADException;

/**
 * A utility class for computing solar angles using Rothermel formulas.
 * 
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class RothermelSupport {

    private RothermelSupport() {
    }

    /**
     * Computes the solar declination angle for a given day of the year.
     *
     * Solar Declination angle is the angle between a plane perpendicular to incoming solar
     * radiation and the rotational axis of the earth. It varies form +23.5 degress on June 21/22
     * and -23.5 degrees on December 21/22.
     *
     * @see "Rothermel et al, 1986, page 11"
     *
     * @param date the date used to compute the angle
     * @return the solar declination angle [degrees]
     */
    static public Real calcSolarDeclinationAngle(Date date) {
        // Rothermel et al, 1986, page 11
        // Equation #8
        // 0.9863 = 360 degrees /365 days
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        long julianDay = calendar.get(Calendar.DAY_OF_YEAR);

        double delta = 23.5 * sin(toRadians(0.9863) * (284 + julianDay));
        return new Real(RealType.Declination, delta); // [degrees]
    }

    /**
     * Computes the time of sunrise and sunset for latitudes less than 66.5 Local Standard Time
     * (LST).
     *
     * @see "Rothermel et al, 1986, page 48"
     * @return two element array of times (solar noon == 12 LST)
     */
    static public DateTime[] calcSunriseSunset(Real latitude, Real declination, Date date) {
        double sunriseHour = calcSunriseHour(latitude, declination);
        double sunsetHour = 24 - sunriseHour;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int julianDay = calendar.get(Calendar.DAY_OF_YEAR);
        DateTime[] array;
        try {
            array = new DateTime[]{
                new DateTime(year, julianDay, sunriseHour * 3600),
                new DateTime(year, julianDay, sunsetHour * 3600)
            };
            return array;
        } catch (VisADException ex) {
            IllegalStateException ise = new IllegalStateException("Sunrise: " + sunriseHour + ", Sunset: " + sunsetHour, ex);
            Exceptions.printStackTrace(ise);
            throw ise;
        }

    }

    /**
     * Computes the time of sunrise for latitudes less than 66.5
     *
     * @see "Rothermel et al, 1986, page 48"
     * @return time of sunrise relative to solar noon
     */
    static public double calcSunriseHour(Real latitude, Real declination) {
        // Based on Equation #1
        // sin h = (sin A - sin phi * sin delta) / (cos phi * cos delta);
        //  The text includes additional conditions for testing
        //  for perpetual day or perpetual night... not implemented.
        try {
            double phi = latitude.getValue(CommonUnit.radian);
            double delta = declination.getValue(CommonUnit.radian);
            if (abs(phi) > toDegrees(66.5)) {
                throw new IllegalArgumentException("latitude must be < 66.5 degrees");
            }
            // TODO: Add tests for perpetual day/night (return 0 for no sunrise/sunset)

            // This equation computes the hour angle when the solar altitude angle = 0
            double h = asin(0 - sin(phi) * sin(delta)) / (cos(phi) * cos(delta));
            // 15 degrees of rotation per hour; relative to 0600hrs
            double t = (toDegrees(h) / 15) + 6;
            return t;

        } catch (IllegalArgumentException | VisADException ex) {
            Logger.getLogger(RothermelSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0.0;
    }

    /**
     * Computes the time of sunset for latitudes less than 66.5
     *
     * @see "Rothermel et al, 1986, page 48"
     * @return time of sunset relative to solar noon
     */
    static public double calcSunsetHour(Real latitude, Real declination) {
        double t_r = calcSunriseHour(latitude, declination);
        double t_s = 24 - t_r;
        return t_s;
    }

    /**
     * Computes hour angle from local 6am
     * @param timeProjection local time in 24hr format
     * @return hour angle 0600=0 deg; 1200=90 deg; 1800=180 deg; 0000=270[radians]
     */
    static public Real calcLocalHourAngle(Real time) {
        double t = Times.toClockTime(time);
        // deg per hour = (360.0 / 24.0) = 15
        double h = 15 * ((t >= 6.0 ? t : t + 24) - 6.0);
        try {
            return new Real(RealType.Generic, toRadians(h), CommonUnit.radian);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Computes the solar altitude angle A, i.e., how high is the sun from the horizon.
     * @param h hour angle from the local 6am [(360/24)*(t - 6.0)]
     * @param phi latitude [radians]
     * @param delta solar declination [radians]
     * @return solar altitude angle [radians]
     */
    static public double calcSolarAltitudeAngle(double h, double phi, double delta) {
        // Rothermel et al, 1986, page 10
        // Equation #4
        // The solar altitude angle A, is given by
        //  sin A = sin h * cos delta * cos phi + sin delta * sin phi
        // where:
        //  A =   solar altitude angle
        double sinA = (sin(h) * cos(delta) * cos(phi)) + (sin(delta) * sin(phi));
        return asin(sinA);
    }

    /**
     * Computes the solar azimuth angle, Z, i.e., where is sun relative to East. At 0600 local time
     * the solar azimuth is 0 degrees; at 1800 it is 180 degrees.
     * @param h hour angle from the local 6am [(360/24)*(t - 6.0)] [radians]
     * @param phi latitude [radians]
     * @param delta solar declination [radians]
     * @param A solar altitude angle [radians]
     * @return solar azimuth angle 0 <= Z <= 2PI relative to East [radians]
     */
    static public double calcSolarAzimuthAngle(double h, double phi, double delta, double A) {
        // Rothermel et al, 1986, page 11
        // Equation #5 and #6. The solar azimuth angle Z, is given by simple
        // ratios of equations 5 and 6.

        // Eq #5
        double tanZ = ((sin(h) * cos(delta) * sin(phi)) - (sin(delta) * cos(phi)))
                / (cos(h) * cos(delta));
        // Eq #6
        double cosZ = cos(h) * (cos(delta) / cos(A));

        // Compare cos and tan values to determine which inverse function to use
        // and which  quadrant of the circle to assign the value to (relative
        // to East). But first, invert the tan value when in the southern hemisphere to
        // sync it up with the cos value which is not influenced by the sign of phi --
        // A angle used in cosZ is is always positive at noon regardless of hemisphere.
        double Z = 0;
        if (phi < 0) {
            tanZ = -tanZ;
        }
        if (tanZ >= 0 && cosZ >= 0) {
            Z = atan(tanZ);         // late morning (east to south)
        } else if (tanZ < 0 && cosZ < 0) {
            Z = acos(cosZ);         // early afternnon (south to west)
        } else if (tanZ >= 0 && cosZ < 0) {
            Z = atan(tanZ) + PI;    // night (west to north)
        } else {
            Z = 2 * PI - acos(cosZ);// early morning (north to east)
        }
        return Z;
    }

    /**
     * Computes hour angle from local 6am
     * @param timeProjection local time in 24hr format
     * @return hour angle 0600=0 deg; 1200=90 deg; 1800=180 deg; 0000=270[radians]
     */
    static public double calcLocalHourAngle(double t) {
        // deg per hour = (360.0 / 24.0) = 15
        double h = 15 * ((t >= 6.0 ? t : t + 24) - 6.0);
        return toRadians(h);
    }
}
