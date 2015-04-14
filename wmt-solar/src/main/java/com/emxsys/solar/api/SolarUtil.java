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
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoCoord3D;
import static com.emxsys.util.AngleUtil.*;
import static java.lang.Math.*;
import java.rmi.RemoteException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.JulianFields;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import static visad.CommonUnit.*;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

@Deprecated
public class SolarUtil {

    private static final Logger logger = Logger.getLogger(SolarUtil.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    /**
     * Computes the position of the sun at the given time relative to the earth lat/lon/altitude
     * coordinate system.
     *
     * @param time The time for which to obtain the sun position (will be converted to UTC).
     * @return A Coord3D representing the sun's position as lat and lon with elevation set to 1 A.U.
     * @author Bruce Schubert
     */
    public static Coord3D getSunPosition(ZonedDateTime time) {
        final double ASTRONOMICAL_UNIT_METERS = 149597870700.0;

        // Get the right ascension and declination (lat/lon)
        double[] latLon = calcSubsolarPoint(calcJulianDate(time));
        GeoCoord3D sunPosition = GeoCoord3D.fromDegreesAndMeters(
                toDegrees(latLon[0]), toDegrees(latLon[1]), ASTRONOMICAL_UNIT_METERS);
//        Coord2D point = calcSubsolarPointAppox(time);
//        GeoCoord3D sunPosition = GeoCoord3D.fromDegreesAndMeters(point.getLatitudeDegrees(), point.getLongitudeDegrees(), ASTRONOMICAL_UNIT_METERS);
        return sunPosition;
    }

    /**
     *
     *
     * @author Bruce Schubert
     * @param time UTC time
     * @return latitude and longitude of sun on the celestial sphere
     * @throws visad.VisADException
     * @throws java.rmi.RemoteException
     */
    static public RealTuple getRightAscentionDeclination(ZonedDateTime time) throws VisADException, RemoteException {

        double epsilon_g = 279.403303;  // eclipticLongitudeAtEpoch
        double omega_g = 282.768422;    // eclipticLongitudeOfPerigee
        double e = 0.016713;            // eccentricityOfOrbit

        // Calculate difference in days between the current Julian Day
        // and JD 2447891.5, which is 1 January 1990 Universal Time
        double D = calcJulianDate(time) - 2447891.5;
        double N = normalize360((360. / 365.242191) * D);
        double meanSun = N + epsilon_g - omega_g;
        double E_c = (360. / PI) * e * sin(toRadians(meanSun));
        double eclipticLongitude = normalize360(N + E_c + epsilon_g);
        double eclipticLatitude = 0;

        // Ecliptic to equatorial coordinate conversion
        return getEquatorialCoordinates(eclipticLatitude, toRadians(eclipticLongitude));
    }

    public static RealTuple getEquatorialCoordinates(double eclipticLatitude, double eclipticLongitude) throws VisADException, RemoteException {
        // Ecliptic to equatorial coordinate conversion
        double eclipticObliquity = toRadians(23.441884); // eclipticObliquity
        double delta = toDegrees(asin(sin(eclipticLatitude) * cos(eclipticObliquity)
                + cos(eclipticLatitude) * sin(eclipticObliquity) * sin(eclipticLongitude)));

        double y = sin(eclipticLongitude) * cos(eclipticObliquity) - tan(eclipticLatitude) * sin(eclipticObliquity);
        double x = cos(eclipticLongitude);
        double alpha = toDegrees(atan2(y, x)); // right ascension

        Real ascension = new Real(SolarType.RIGHT_ASCENSION, alpha);
        Real declination = new Real(SolarType.DECLINATION, delta);

        return new RealTuple(SolarType.EQUATORIAL_COORDINATES, new Real[]{ascension, declination}, null);
    }

    /**
     * Gets the solar azimuth and altitude angles from the observer's position to the sun.
     *
     * @param observer The observer's coordinates.
     * @param time
     * @return The horizon coordinates (azimuth and altitude) to the sun.
     */
    public static RealTuple getAzimuthAltitude(Coord2D observer, ZonedDateTime time) {
        throw new UnsupportedOperationException("Not working correctly!!");
//        try {
//            if (observer.isMissing()) {
//                throw new IllegalArgumentException("getAzimuthAltitude(" + observer + ")");
//            }
//            RealTuple rightAscentionDeclination = getRightAscentionDeclination(time);
//            double phi = observer.getLatitude().getValue(radian);
//            double sigma = rightAscentionDeclination.getRealComponents()[1].getValue(radian);
//            double H = calcHourAngle(time).getValue(radian);
//            double a = calcAltitudeAngle(phi, sigma, H);
//            double A = calcAzimuthAngle(phi, sigma, a, H);
//
//            Real azimuth = new Real(SolarType.AZIMUTH_ANGLE, toDegrees(A));
//            Real altitude = new Real(SolarType.ALTITUDE_ANGLE, toDegrees(a));
//
//            return new RealTuple(SolarType.HORIZON_COORDINATES, new Real[]{azimuth, altitude}, null);
//
//        } catch (VisADException | RemoteException | IllegalArgumentException ex) {
//            logger.log(Level.SEVERE, ex.toString());
//        }
//        return new RealTuple(SolarType.HORIZON_COORDINATES);
    }

    /**
     * Computes the Julian date from the given date/time.
     *
     * @param datetime The date and time.
     * @return The Julian date.
     */
    public static double calcJulianDate(ZonedDateTime datetime) {
        final long SECONDS_IN_FULL_DAY = 86400;
        final long SECONDS_IN_HALF_DAY = 43200;
        ZonedDateTime utc = datetime.withZoneSameInstant(ZoneId.of("Z"));
        // Add the offset from noon to the Julian date.
        return utc.getLong(JulianFields.JULIAN_DAY)
                + ((double) (utc.getLong(ChronoField.SECOND_OF_DAY) - SECONDS_IN_HALF_DAY)) / SECONDS_IN_FULL_DAY;
    }

    /**
     * Computes the Julian date from the given date/time.
     *
     * From "Practical Astronomy with Your Calculator" by Peter Duffet-Smith
     * @param utc The date and time to convert.
     * @return The Julian date.
     */
    public static double calcJulianDate(Date utc) {
        final long SECONDS_IN_FULL_DAY = 86400;
        final long SECONDS_IN_HALF_DAY = 43200;
        // Create Gregorian start date used for comparison
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(1592, 10, 15);
        Date start = cal.getTime();

        // Convert incoming date to UTC
        cal.setTime(utc);

        long y = cal.get(Calendar.YEAR);
        long m = cal.get(Calendar.MONTH) + 1;
        long d = cal.get(Calendar.DAY_OF_MONTH);
        if (m <= 2) {
            y -= 1;
            m += 12;
        }
        long A = y / 100;
        long B = utc.after(start) ? 2 - A + (A / 4) : 0;
        long C = (y < 0) ? (long) ((365.25 * y) - 0.75) : (long) (365.25 * y);
        long D = (long) (30.6001 * (m + 1));
        double offsetFromNoon = ((double) (cal.get(Calendar.HOUR_OF_DAY) * 60 * 60
                + cal.get(Calendar.MINUTE) * 60
                + cal.get(Calendar.SECOND) - SECONDS_IN_HALF_DAY)) / SECONDS_IN_FULL_DAY;
        return B + C + D + d + 1720995 + offsetFromNoon;

    }

    /**
     * Computes the hour angle (H) from UTC.
     * @param time The time (will be converted to UTC).
     * @return The computed hour-angle (H).
     */
    public static Real calcHourAngle(ZonedDateTime time) {
        LocalTime utc = time.withZoneSameInstant(ZoneId.of("Z")).toLocalTime();
        double t = utc.getHour() + ((utc.getMinute() + (utc.getSecond() / 60.0)) / 60.0);
        double H = 15 * t;  // 15 degrees per hour
        return new Real(SolarType.HOUR_ANGLE, H);
    }

    /**
     * Calculates altitude angle (a), from equatorial coordinates.
     *
     * @param latitude Observer's geographic latitude.
     * @param declination Earth's declination angle.
     * @param solarHour The solar hour-angle.
     * @return The computed altitude angle (a).
     */
    public static Real getAltitudeAngle(Real latitude, Real declination, Real solarHour) {
        try {
            if (latitude.isMissing() || declination.isMissing() || solarHour.isMissing()) {
                throw new IllegalArgumentException(latitude.longString() + ", " + declination.longString() + ", " + solarHour.longString());
            }
            double phi = latitude.getValue(radian);
            double sigma = declination.getValue(radian);
            double H = solarHour.getValue(radian);

            double a = calcAltitudeAngle(phi, sigma, H);
            return new Real(SolarType.ALTITUDE_ANGLE, toDegrees(a));

        } catch (VisADException | RemoteException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    /**
     * Calculates the solar altitude angle (a), from equatorial coordinates.
     *
     * See: "Practical Astronomy with you Calculator", #25.
     *
     * @param phi Observer's geographic latitude [radians]
     * @param sigma Earth's declination angle. [radians]
     * @param H Solar hour-angle [radians]
     *
     * @return The computed altitude angle (a).
     */
    public static double calcAltitudeAngle(double phi, double sigma, double H) {
        double t = sin(phi) * sin(sigma);
        double u = cos(phi) * cos(H) * cos(sigma);
        double a = asin(t + u);
        return a;
    }

    /**
     * Calculates the solar azimuth angle (A), from equatorial coordinates.
     * @param latitude Observer's geographic latitude.
     * @param declination Earth's declination angle.
     * @param altitude Sun's altitude angle.
     * @param solarHour
     * @return The computed Azimuth angle (A).
     */
    public static Real calcAzimuthAngle(Real latitude, Real declination, Real altitude, Real solarHour) {
        try {
            if (latitude.isMissing() || declination.isMissing() || altitude.isMissing()) {
                throw new IllegalArgumentException(latitude.longString() + ", " + declination.longString() + ", " + altitude.longString());
            }
            double phi = latitude.getValue(radian);
            double sigma = declination.getValue(radian);
            double a = altitude.getValue(radian);
            double H = solarHour.getValue(radian);

            double A = calcAzimuthAngle(phi, sigma, a, H);
            return new Real(SolarType.AZIMUTH_ANGLE, toDegrees(A));

        } catch (VisADException | RemoteException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    /**
     * Calculates Azimuth angle (A), from equatorial coordinates.
     *
     * See: "Practical Astronomy with you Calculator", #25.
     *
     * @param phi Observer's geographic latitude [radians]
     * @param sigma Earth's declination angle. [radians]
     * @param a Sun's altitude angle [radians]
     * @param H Sun's hour-angle [radians]
     *
     * @return The computed azimuth angle (Z).
     */
    public static double calcAzimuthAngle(double phi, double sigma, double a, double H) {
        double t = sin(sigma) - sin(phi) * sin(a);
        double u = cos(phi) * cos(a);
        double A = acos(t / u);
        if (signum(sin(H)) == -1) {
            return A;
        } else {
            return (2 * PI) - A;
        }
    }

    /**
     *
     * Calculate the LatLon of sun at given time. Latitude is equivalent to declination. Longitude
     * is equivalent to right ascension. The subsolar point on a planet is where its sun is
     * perceived to be directly overhead (in zenith), that is where the sun's rays are hitting the
     * planet exactly perpendicular to its surface.
     *
     * See: http://en.wikipedia.org/wiki/Position_of_the_Sun
     *
     * @author Bruce Schubert
     * @param time UTC time
     * @return latitude and longitude of sun on the celestial sphere
     */
    static public Coord2D calcSubsolarPointAppox(ZonedDateTime time) {
        // Main variables
        double elapsedJulianDays;
        double eclipticLongitude;
        double eclipticObliquity;
        double rightAscension, declination;
        // Calculate difference in days between the current Julian Day
        // and JD 2451545.0, which is noon 1 January 2000 Universal Time
        {
            elapsedJulianDays = calcJulianDate(time) - 2451545.0;
        }
        // Calculate ecliptic coordinates (ecliptic longitude and obliquity of the
        // ecliptic in radians but without limiting the angle to be less than 2*Pi
        // (i.e., the result may be greater than 2*Pi)
        {
            // The mean longitude of the Sun, corrected for the aberration of light, is:
            double meanLongitude = toRadians(normalize360(280.460 + 0.9856474 * elapsedJulianDays));
            double meanAnomaly = toRadians(normalize360(357.528 + 0.9856003 * elapsedJulianDays));
            eclipticLongitude = meanLongitude
                    + toRadians(1.915) * sin(meanAnomaly)
                    + toRadians(0.020) * sin(2 * meanAnomaly);
            eclipticObliquity = toRadians(23.439 - 0.0000004 * elapsedJulianDays);
        }
        // Calculate celestial coordinates ( right ascension and declination ) in radians
        {
//            double sinEclipticLongitude = sin(eclipticLongitude);
//            double dY = cos(eclipticObliquity) * sinEclipticLongitude;
//            double dX = cos(eclipticLongitude);
//            rightAscension = atan2(dY, dX);
            rightAscension = atan(cos(eclipticObliquity) * tan(eclipticLongitude));
            if (rightAscension < 0.0) {
                rightAscension = rightAscension + Math.PI * 2.0;
            }
            declination = asin(sin(eclipticObliquity) * sin(eclipticLongitude));
        }
        double longitude = rightAscension;

        while (declination > Math.PI / 2.0) {
            declination -= Math.PI;
        }
        while (declination <= -Math.PI / 2.0) {
            declination += Math.PI;
        }
        while (longitude > Math.PI) {
            longitude -= Math.PI * 2.0;
        }
        while (longitude <= -Math.PI) {
            longitude += Math.PI * 2.0;
        }
        return GeoCoord2D.fromRadians(declination, longitude);
    }

    /**
     * Calculate the LatLon of sun at given time. Latitude is equivalent to declination. Longitude
     * is equivalent to right ascension. The subsolar point on a planet is where its sun is
     * perceived to be directly overhead (in zenith), that is where the sun's rays are hitting the
     * planet exactly perpendicular to its surface.
     *
     * Posted on WorldWind Java Development forum by heidtmare, "This implementation of
     * calcSubsolarPointAppox is from the old sunlight package."
     *
     * Original c++ source here: http://www.psa.es/sdg/archive/SunPos.cpp
     *
     * @author heidtmare
     * @param time UTC time
     * @return latitude and longitude of sun on the celestial sphere
     */
    static double[] calcSubsolarPoint(double julianDate) {
        // Main variables
        double elapsedJulianDays;
        double eclipticLongitude;
        double eclipticObliquity;
        double rightAscension, declination;
        // Calculate difference in days between the current Julian Day
        // and JD 2451545.0, which is noon 1 January 2000 Universal Time
        {
            elapsedJulianDays = julianDate - 2451545.0;
        }
        // Calculate ecliptic coordinates (ecliptic longitude and obliquity of the
        // ecliptic in radians but without limiting the angle to be less than 2*Pi
        // (i.e., the result may be greater than 2*Pi)
        {
            double omega = 2.1429 - 0.0010394594 * elapsedJulianDays;
            double meanLongitude = 4.8950630 + 0.017202791698 * elapsedJulianDays; // Radians
            double meanAnomaly = 6.2400600 + 0.0172019699 * elapsedJulianDays;
            eclipticLongitude = meanLongitude
                    + 0.03341607 * Math.sin(meanAnomaly)
                    + 0.00034894 * Math.sin(2 * meanAnomaly)
                    - 0.0001134 - 0.0000203 * Math.sin(omega);
            eclipticObliquity = 0.4090928
                    - 6.2140e-9 * elapsedJulianDays
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
            if (rightAscension < 0.0) {
                rightAscension = rightAscension + Math.PI * 2.0;
            }
            declination = Math.asin(Math.sin(eclipticObliquity) * sinEclipticLongitude);
        }
        double greenwichMeanSiderealTime = (18.697374558 + 24.06570982441908 * elapsedJulianDays) % 24;
        double longitude = rightAscension - toRadians(greenwichMeanSiderealTime * 15.0);

        while (declination > Math.PI / 2.0) {
            declination -= Math.PI;
        }
        while (declination <= -Math.PI / 2.0) {
            declination += Math.PI;
        }
        while (longitude > Math.PI) {
            longitude -= Math.PI * 2.0;
        }
        while (longitude <= -Math.PI) {
            longitude += Math.PI * 2.0;
        }

        return new double[]{
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
    public static double DirectNormalSolarFlux(Date date, double solarAltitudeAngleDegrees) {

        if (solarAltitudeAngleDegrees <= 0) {
            return 0;
        }

        // The apparent direct normal solar flux at the outer edge of the earth's atmosphere
        // on the 21st day of each month
        final double[] SOLAR_FLUX = {
            1230, 1215, 1186, 1136, 1104, 1088, 1085, 1107, 1151, 1192, 1221, 1233
        };

        // The apparent atmospheric extinction coefficient on the 21st day of each month
        final double[] EXTINCTION = {
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

        if (dom == EPOCH_DATE) {
            A = SOLAR_FLUX[curMonth];
            B = EXTINCTION[curMonth];
        } else {
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
            double solarAzimuthAngleDegrees, double solarAltitudeAngleDegrees) {
        double sigma = toRadians(surfaceTiltAngleDegrees);
        double psi = toRadians(surfaceAzimuthAngleDegrees);
        double beta = toRadians(solarAltitudeAngleDegrees);
        double phi = toRadians(solarAzimuthAngleDegrees);

        double gamma = abs(phi - psi); // surface-solar azimuth angle
        double cosTheta = 0;                // angle between surface normal and solar rays

        if (surfaceTiltAngleDegrees == 90.0) {
            // The surface is vertical
            cosTheta = cos(beta) * cos(gamma);
        } else if (surfaceTiltAngleDegrees == 0.0) {
            // The surface is horizontal (incidence angle is equal to zenith angle)
            cosTheta = sin(beta);
        } else {
            // The surface is tilted
            cosTheta = cos(beta) * cos(gamma) * sin(sigma)
                    + sin(beta) * cos(sigma);
        }
        return toDegrees(acos(cosTheta));
    }

    /**
     * The declination angle throughout the year can be well approximated by a sine function.
     *
     * @param dayOfYear Day of the year (1-365).
     * @return the sun's declination angle (in radians)
     */
    public static double DeclinationDegrees(int dayOfYear) {
        // Source: University of Minissota - Department of Mechanical Engineering
        // from ME 4131 THERMAL ENVIRONMENTAL ENGINEERING LABORATORY MANUAL
        // http://www.me.umn.edu/courses/me4131/HTMLPages/LabManual.htm
        // http://www.me.umn.edu/courses/me4131/LabManual/AppDSolarRadiation.pdf
        double dec = 23.45 * sin(toRadians((360 / 365.0) * (284 + dayOfYear)));
        return dec;
    }

    public static double SolarHourDegrees(double localSolarTime) {
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
                                                     double lonTzStdMer, double lonActualLoc, int dstMins) {

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
    public static double EquationOfTimeInHours(int dayOfYear) {
        double B = toRadians((360 * (dayOfYear - 81)) / 364.0);
        double E = (0.165 * sin(2 * B)) - (0.126 * cos(B)) - (0.025 * sin(B));
        return E;
    }

    public static double ZenithAngleDegrees(
            double latitudeDegrees, double solarHourDegrees, double declinationDegrees) {
        double l = toRadians(latitudeDegrees);
        double h = toRadians(solarHourDegrees);
        double d = toRadians(declinationDegrees);

        double t = sin(l) * sin(d);
        double u = (cos(l) * cos(h) * cos(d));
        double zenith = acos(t + u);
        return toDegrees(zenith);
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
                                             double latitudeDegrees, double solarHourDegrees, double declinationDegrees) {
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
        if (a < PI / 2.0) {
            sin1 = ts / cos(a);
            cos2 = (t - u / cos(a));
        }
        // Range checking
        if (sin1 > 1.0) {
            sin1 = 1.0;
        }
        if (sin1 < -1.0) {
            sin1 = -1.0;
        }
        if (cos2 > 1.0) {
            cos2 = 1.0;
        }
        if (cos2 < -1.0) {
            cos2 = -1.0;
        }
        // Check the quadrants
        if (sin1 < -0.99999) {
            azimuth = asin(sin1);
        } else if (sin1 > 0 && cos2 < 0) {
            if (sin1 >= 1.0) {
                azimuth = -(PI / 2.0);
            } else {
                azimuth = PI / 2.0 + (PI / 2.0 - asin(sin1));
            }
        } else if (sin1 < 0 && cos2 < 0) {
            if (sin1 <= -1.0) {
                azimuth = PI / 2.0;
            } else {
                azimuth = -(PI / 2.0) - (PI / 2.0 + asin(sin1));
            }
        } else {
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
