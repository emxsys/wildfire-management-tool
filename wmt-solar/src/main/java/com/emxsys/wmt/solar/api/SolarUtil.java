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

import com.emxsys.wmt.gis.GeoCoord3D;
import static java.lang.Math.*;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SolarUtil
{
    private static final Logger logger = Logger.getLogger(SolarUtil.class.getName());


    /**
     * Computes the position of the sun at the given time relative to the earth lat/lon/altitude
     * coordinate system.
     *
     * @param datetime
     * @return a Coord3D representing the sun's position as lat and lon with elevation set to 1 A.U.
     * @author heidtmare
     */
    static public GeoCoord3D getSunPosition(Date datetime)
    {
        final double ASTRONOMICAL_UNIT_METERS = 149597870700.0;

        Calendar time = Calendar.getInstance();
        time.setTime(datetime);
        double[] ll = subsolarPoint(time);
        GeoCoord3D sunPosition = GeoCoord3D.fromRadiansAndMeters(ll[0], ll[1], ASTRONOMICAL_UNIT_METERS);
        logger.log(Level.FINE, "SUN Position: {0}", sunPosition.toString());

        return sunPosition;
    }


    /**
     * Calculate the LatLon of sun at given time. Latitude is equivalent to declination. Longitude
     * is equivalent to right ascension.
     *
     * Posted on WorldWind Java Development forum by heidtmare, "This implementation of
     * subsolarPoint is from the old sunlight package."
     *
     * @author heidtmare
     * @param time
     * @return latitude and longitude of sun on the celestial sphere
     */
    static double[] subsolarPoint(Calendar time)
    {
        // Main variables
        double elapsedJulianDays;
        double decimalHours;
        double eclipticLongitude;
        double eclipticObliquity;
        double rightAscension, declination;
        // Calculate difference in days between the current Julian Day
        // and JD 2451545.0, which is noon 1 January 2000 Universal Time
        {
            // Calculate time of the day in UT decimal hours
            decimalHours = time.get(Calendar.HOUR_OF_DAY)
                + (time.get(Calendar.MINUTE) + time.get(Calendar.SECOND) / 60.0)
                / 60.0;
            // Calculate current Julian Day
            long aux1 = (time.get(Calendar.MONTH) - 14) / 12;
            long aux2 = (1461 * (time.get(Calendar.YEAR) + 4800 + aux1)) / 4
                + (367 * (time.get(Calendar.MONTH) - 2 - 12 * aux1)) / 12
                - (3 * ((time.get(Calendar.YEAR) + 4900 + aux1) / 100)) / 4
                + time.get(Calendar.DAY_OF_MONTH) - 32075;
            double julianDate = (double) (aux2) - 0.5 + decimalHours / 24.0;
            // Calculate difference between current Julian Day and JD 2451545.0
            elapsedJulianDays = julianDate - 2451545.0;
        }
        // Calculate ecliptic coordinates (ecliptic longitude and obliquity of the
        // ecliptic in radians but without limiting the angle to be less than 2*Pi
        // (i.e., the result may be greater than 2*Pi)
        {
            double omega = 2.1429 - 0.0010394594 * elapsedJulianDays;
            double meanLongitude = 4.8950630 + 0.017202791698 * elapsedJulianDays; // Radians
            double meanAnomaly = 6.2400600 + 0.0172019699 * elapsedJulianDays;
            eclipticLongitude = meanLongitude + 0.03341607
                * Math.sin(meanAnomaly) + 0.00034894
                * Math.sin(2 * meanAnomaly) - 0.0001134 - 0.0000203
                * Math.sin(omega);
            eclipticObliquity = 0.4090928 - 6.2140e-9 * elapsedJulianDays
                + 0.0000396 * Math.cos(omega);
        }
        // Calculate celestial coordinates ( right ascension and declination ) in radians
        // but without limiting the angle to be less than 2*Pi (i.e., the result may be
        // greater than 2*Pi)
        {
            double sinEclipticLongitude = Math.sin(eclipticLongitude);
            double dY = Math.cos(eclipticObliquity) * sinEclipticLongitude;
            double dX = Math.cos(eclipticLongitude);
            rightAscension = Math.atan2(dY, dX);
            if (rightAscension < 0.0)
            {
                rightAscension = rightAscension + Math.PI * 2.0;
            }
            declination = Math.asin(Math.sin(eclipticObliquity) * sinEclipticLongitude);
        }

        double greenwichMeanSiderealTime = 6.6974243242 + 0.0657098283 * elapsedJulianDays + decimalHours;
        double longitude = rightAscension - Math.toRadians(greenwichMeanSiderealTime * 15.0);

        //longitude += Math.PI;//This was putting the sun on the wrong side of the earth!!

        while (declination > Math.PI / 2.0)
        {
            declination -= Math.PI;
        }
        while (declination <= -Math.PI / 2.0)
        {
            declination += Math.PI;
        }
        while (longitude > Math.PI)
        {
            longitude -= Math.PI * 2.0;
        }
        while (longitude <= -Math.PI)
        {
            longitude += Math.PI * 2.0;
        }

        return new double[]
        {
            declination, longitude
        };
    }


    /**
     * Idn = A * exp( -B / sin(beta)
     *
     * @param date
     * @param solarAltitudeAngleDegrees
     * @return
     */
    public static double DirectNormalSolarFlux(Date date, double solarAltitudeAngleDegrees)
    {

        if (solarAltitudeAngleDegrees <= 0)
        {
            return 0;
        }

        // The apparent direct normal solar flux at the outer edge of the earth's atmosphere
        // on the 21st day of each month
        final double[] SOLAR_FLUX =
        {
            1230, 1215, 1186, 1136, 1104, 1088, 1085, 1107, 1151, 1192, 1221, 1233
        };

        // The apparent atmospheric extinction coefficient on the 21st day of each month
        final double[] EXTINCTION =
        {
            0.142, 0.144, 0.145, 0.180, 0.196, 0.205, 0.207, 0.201, 0.177, 0.160, 0.149, 0.142
        };

        final int EPOCH_DATE = 21;

        // Coefficients
        double A = 0.0;
        double B = 0.0;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int dom = cal.get(Calendar.DAY_OF_MONTH);
        int curMonth = cal.get(Calendar.MONTH); // zero based array index

        if (dom == EPOCH_DATE)
        {
            A = SOLAR_FLUX[curMonth];
            B = EXTINCTION[curMonth];
        }
        else
        {
            // Interpolate between two monthly coeffecients in our table of which
            // the values arebased on the 21st day of each month.  We assume 30 
            // days per month for the interpolation - crude but simple.
            Calendar otherCal = (Calendar) cal.clone();
            otherCal.add(Calendar.MONTH, dom < EPOCH_DATE ? -1 : +1);
            int otherMonth = otherCal.get(Calendar.MONTH);  // zero based array index

            // Establish the begin and end values
            double A1 = SOLAR_FLUX[dom < EPOCH_DATE ? otherMonth : curMonth];
            double A2 = SOLAR_FLUX[dom < EPOCH_DATE ? curMonth : otherMonth];
            double B1 = EXTINCTION[dom < EPOCH_DATE ? otherMonth : curMonth];
            double B2 = EXTINCTION[dom < EPOCH_DATE ? curMonth : otherMonth];

            // Interpolate
            int dayRange = (dom < EPOCH_DATE ? otherCal : cal).getActualMaximum(Calendar.DATE);
            int epochToEom = dayRange - EPOCH_DATE;
            int numDays = dom < EPOCH_DATE ? dom + epochToEom : dom - EPOCH_DATE;
            double frac = numDays / (double) dayRange;
            A = A1 + ((A2 - A1) * frac);
            B = B1 + ((B2 - B1) * frac);
        }

        double beta = toRadians(solarAltitudeAngleDegrees);
        double directNormalFlux = A * exp(-B / sin(beta));

        return directNormalFlux;
    }


    public static double IncidenceAngleDegrees(
        double surfaceTiltAngleDegrees, double surfaceAzimuthAngleDegrees,
        double solarAzimuthAngleDegrees, double solarAltitudeAngleDegrees)
    {
        double sigma = toRadians(surfaceTiltAngleDegrees);
        double psi = toRadians(surfaceAzimuthAngleDegrees);
        double beta = toRadians(solarAltitudeAngleDegrees);
        double phi = toRadians(solarAzimuthAngleDegrees);

        double gamma = abs(phi - psi); // surface-solar azimuth angle
        double cosTheta = 0;                // angle between surface normal and solar rays

        if (surfaceTiltAngleDegrees == 90.0)
        {
            // The surface is vertical
            cosTheta = cos(beta) * cos(gamma);
        }
        else if (surfaceTiltAngleDegrees == 0.0)
        {
            // The surface is horizontal (incidence angle is equal to zenith angle)
            cosTheta = sin(beta);
        }
        else
        {
            // The surface is tilted
            cosTheta = cos(beta) * cos(gamma) * sin(sigma)
                + sin(beta) * cos(sigma);
        }
        return toDegrees(acos(cosTheta));
    }


    /**
     * The declination angle thoughout the year can be well approximated by a sine function.
     *
     * @returns the sun's declination angle (in radians)
     * @param dayOfYear Day of the year (1-365).
     */
    public static double DeclinationDegrees(int dayOfYear)
    {
        // Source: University of Minissota - Department of Mechanical Engineering
        // from ME 4131 THERMAL ENVIRONMENTAL ENGINEERING LABORATORY MANUAL
        // http://www.me.umn.edu/courses/me4131/HTMLPages/LabManual.htm
        // http://www.me.umn.edu/courses/me4131/LabManual/AppDSolarRadiation.pdf
        double dec = 23.45 * sin(toRadians((360 / 365.0) * (284 + dayOfYear)));
        return dec;
    }


    public static double SolarHourDegrees(double localSolarTime)
    {
        double lst = localSolarTime % 24;
        double solarHour = 15 * (lst - 12.0);
        return solarHour;
    }


    /**
     * Local Solar Time is based on the apparent solar day, which is the interval between two
     * successive returns of the Sun to the local meridian. Note that the solar day starts at noon,
     * so apparent solar time 00:00 means noon and 12:00 means midnight. Solar time can be measured
     * by a sundial.
     *
     * @param clockTime
     * @param lonTzStdMer is the longitude of time zone's meridian
     * @param lonActualLoc is the longitude of the actual location
     * @param dstMins
     * @return solar hour
     */
    public static double LocalSolarTimeFromClockTime(Date clockTime,
        double lonTzStdMer, double lonActualLoc, int dstMins)
    {

        Calendar cal = Calendar.getInstance();
        cal.setTime(clockTime);
        double LST; // Local Solar Time (hrs)
        double CT;  // Clock Time (hrs)  e.g. 3:45pm = 15.75
        double E;   // Equation of Time (hrs)
        CT = cal.get(Calendar.HOUR_OF_DAY) + (cal.get(Calendar.MINUTE) / 60.0);
        E = EquationOfTimeInHours(cal.get(Calendar.DAY_OF_YEAR));
        LST = CT + (1 / 15.0) * (lonTzStdMer - lonActualLoc) + E - (dstMins / 60.0);
        return LST;
    }


    /**
     * The equation of time is the difference between apparent solar time and mean solar time, both
     * taken at a given place (or at another place with the same geographical longitude) at the same
     * real instant of time. E.T. = apparent − mean. Positive means: Sun runs fast and culminates
     * earlier, or the sundial is ahead of mean time.
     *
     * @param dayOfYear
     * @return (apparent - mean) in hours
     */
    public static double EquationOfTimeInHours(int dayOfYear)
    {
        double B = toRadians((360 * (dayOfYear - 81)) / 364.0);
        double E = (0.165 * sin(2 * B)) - (0.126 * cos(B)) - (0.025 * sin(B));
        return E;
    }


    public static double ZenithAngleDegrees(
        double latitudeDegrees, double solarHourDegrees, double declinationDegrees)
    {
        double l = toRadians(latitudeDegrees);
        double h = toRadians(solarHourDegrees);
        double d = toRadians(declinationDegrees);

        double t = sin(l) * sin(d);
        double u = (cos(l) * cos(h) * cos(d));
        double zenith = acos(t + u);
        return toDegrees(zenith);
    }


    public static double AltitudeAngleDegrees(
        double latitudeDegrees, double solarHourDegrees, double declinationDegrees)
    {
        double l = toRadians(latitudeDegrees);
        double h = toRadians(solarHourDegrees);
        double d = toRadians(declinationDegrees);

        double t = sin(l) * sin(d);
        double u = cos(l) * cos(h) * cos(d);
        double altitude = asin(t + u);
        return toDegrees(altitude);
    }


    /**
     *
     * @param altitudeAngleDegrees
     * @param latitudeDegrees
     * @param solarHourDegrees
     * @param declinationDegrees
     * @return the azimuth angle measured from the south
     */
    public static double AzimuthAngleDegrees(double altitudeAngleDegrees,
        double latitudeDegrees, double solarHourDegrees, double declinationDegrees)
    {
        double a = toRadians(altitudeAngleDegrees);
        double l = toRadians(latitudeDegrees);
        double h = toRadians(solarHourDegrees);
        double d = toRadians(declinationDegrees);

        double t = sin(d) * cos(l);
        double u = cos(d) * sin(l) * cos(h);
        double ts = -cos(d) * sin(h);

        double sin1 = 0;
        double cos2 = 0;
        double azimuth = 0;
        // Prevent divide by zero
        if (a < PI / 2.0)
        {
            sin1 = ts / cos(a);
            cos2 = (t - u / cos(a));
        }
        // Range checking
        if (sin1 > 1.0)
        {
            sin1 = 1.0;
        }
        if (sin1 < -1.0)
        {
            sin1 = -1.0;
        }
        if (cos2 > 1.0)
        {
            cos2 = 1.0;
        }
        if (cos2 < -1.0)
        {
            cos2 = -1.0;
        }
        // Check the quadrants
        if (sin1 < -0.99999)
        {
            azimuth = asin(sin1);
        }
        else if (sin1 > 0 && cos2 < 0)
        {
            if (sin1 >= 1.0)
            {
                azimuth = -(PI / 2.0);
            }
            else
            {
                azimuth = PI / 2.0 + (PI / 2.0 - asin(sin1));
            }
        }
        else if (sin1 < 0 && cos2 < 0)
        {
            if (sin1 <= -1.0)
            {
                azimuth = PI / 2.0;
            }
            else
            {
                azimuth = -(PI / 2.0) - (PI / 2.0 + asin(sin1));
            }
        }
        else
        {
            azimuth = asin(sin1);
        }
        // switching to degrees
        azimuth = toDegrees(azimuth);
        // reorient the measurement to the south to match me.umn.edu algorithms
        azimuth += azimuth > 0 ? -180.0 : 180.0;
        return azimuth;

        //azimuth = acos((t - u) / cos(a));
        //return toDegrees(azimuth);
    }
}