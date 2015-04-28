/*
 * Copyright (c) 2014, bruce 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of bruce,  nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
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
package com.emxsys.solar.internal;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.Terrain;
import com.emxsys.gis.api.BasicTerrain;
import com.emxsys.visad.GeneralUnit;
import java.time.ZonedDateTime;
import org.openide.util.Exceptions;
import visad.CommonUnit;
import visad.Real;
import visad.VisADException;

public class SolarData implements Cloneable {

    //-----------------INPUT VALUES--------------------
    /** 4-digit year, valid range: -2000 to 6000 */
    int year;
    /** 2-digit month, valid range: 1 to 12 */
    int month;
    /** 2-digit day, valid range: 1 to 31 */
    int day;
    /** Observer local hour, valid range: 0 to 24 */
    int hour;
    /** Observer local minute, valid range: 0 to 59 */
    int minute;
    /** Observer local second, valid range: 0 to 59 */
    int second;
    /** Observer time zone (negative west of Greenwich) */
    double timezone;
    /** Difference between earth rotation time and terrestrial time. It is derived from observation
     * only and is reported in this bulletin: http://maia.usno.navy.mil/ser7/ser7.dat, where delta_t
     * = 32.184 + (TAI-UTC) + DUT1. Valid range: -8000 to 8000 seconds */
    double delta_t = 67;    // Beginning 1 July 2012: TAI-UTC = 35.000 000 seconds  
    /** Observer longitude (negative west of Greenwich). Valid range: -180 to 180 degrees */
    double longitude;
    /** Observer latitude (negative south of equator). Valid range: -90 to 90 degrees */
    double latitude;
    /** Observer elevation [meters]. Valid range: -6500000 or higher meters */
    double elevation;
    /** Annual average local pressure [millibars]. Valid range: 0 to 5000 millibars */
    double pressure;   // 29.92 hg standard pressure - aviation
    /** Annual average local temperature [degrees Celsius]. Valid range: -273 to 6000 degrees
     * Celsius */
    double temperature;    // 59 F standard pressure - aviation
    /** Surface slope (measured from the horizontal plane). Valid range: -360 to 360 degrees */
    double slope;
    /** Surface azimuth rotation (measured from south to projection of surface normal on horizontal
     * plane, negative west) */
    double azm_rotation;
    /** Atmospheric refraction at sunrise and sunset (0.5667 deg is typical). Valid range: -5 to 5
     * degrees */
    double atmos_refract = 0.5667;
    /** Switch to choose functions for desired output (from enumeration) */
    int function;
    //-----------------Intermediate OUTPUT VALUES--------------------
    /** Julian day */
    double jd;
    /** Julian century */
    double jc;
    /** Julian ephemeris day */
    double jde;
    /** Julian ephemeris century */
    double jce;
    /** Julian ephemeris millennium */
    double jme;
    /** earth heliocentric longitude [degrees] */
    double l;
    /** earth heliocentric latitude [degrees] */
    double b;
    /** earth radius vector [Astronomical Units, AU] */
    double r;
    /** geocentric longitude [degrees] */
    double theta;
    /** geocentric latitude [degrees] */
    double beta;
    /** mean elongation (moon-sun) [degrees] */
    double x0;
    /** mean anomaly (sun) [degrees] */
    double x1;
    /** mean anomaly (moon) [degrees] */
    double x2;
    /** argument latitude (moon) [degrees] */
    double x3;
    /** ascending longitude (moon) [degrees] */
    double x4;
    /** nutation longitude [degrees] */
    double del_psi;
    /** nutation obliquity [degrees] */
    double del_epsilon;
    /** ecliptic mean obliquity [arc seconds] */
    double epsilon0;
    /** ecliptic true obliquity [degrees] */
    double epsilon;
    /** aberration correction [degrees] */
    double del_tau;
    /** apparent sun longitude [degrees] */
    double lamda;
    /** Greenwich mean sidereal time [degrees] */
    double nu0;
    /** Greenwich sidereal time [degrees] */
    double nu;
    /** geocentric sun right ascension [degrees] */
    double alpha;
    /** geocentric sun declination [degrees] */
    double delta;
    /** observer hour angle [degrees] */
    double h;
    /** sun equatorial horizontal parallax [degrees] */
    double xi;
    /** sun right ascension parallax [degrees] */
    double del_alpha;
    /** topocentric sun declination [degrees] */
    double delta_prime;
    /** topocentric sun right ascension [degrees] */
    double alpha_prime;
    /** topocentric local hour angle [degrees] */
    double h_prime;
    /** topocentric elevation angle (uncorrected) [degrees] */
    double e0;
    /** atmospheric refraction correction [degrees] */
    double del_e;
    /** topocentric elevation angle (corrected) [degrees] */
    double e;
    /** equation of time [minutes] */
    double eot;
    /** sunrise hour angle [degrees] */
    double srha;
    /** sunset hour angle [degrees] */
    double ssha;
    /** sun transit altitude [degrees] */
    double sta;

    //---------------------Final OUTPUT VALUES------------------------
    /** topocentric zenith angle [degrees] */
    double zenith;
    /** topocentric azimuth angle (westward from south) [-180 to 180 degrees] */
    double azimuth180;
    /** topocentric azimuth angle (eastward from north) [ 0 to 360 degrees] */
    double azimuth;
    /** surface incidence angle [degrees] */
    double incidence;
    /** local sun transit time (or solar noon) [fractional hour] */
    double suntransit;
    /** local sunrise time (+/- 30 seconds) [fractional hour] */
    double sunrise;
    /** local sunset time (+/- 30 seconds) [fractional hour] */
    double sunset;

    public SolarData(ZonedDateTime time, Coord3D observer) {
        this(time, observer, new BasicTerrain(0, 0, 0));
    }

    public SolarData(ZonedDateTime time, Coord3D observer, Terrain terrain) {
        this(time, observer, terrain, new Real(15), new Real(1013.25));
    }

    public SolarData(ZonedDateTime time, Coord3D observer, Terrain terrain, Real temperature, Real pressure) {
        try {
            this.year = time.getYear();
            this.month = time.getMonthValue();
            this.day = time.getDayOfMonth();
            this.hour = time.getHour();
            this.minute = time.getMinute();
            this.second = time.getSecond();
            this.timezone = time.getOffset().getTotalSeconds() / 3600.;
            this.latitude = observer.getLatitudeDegrees();
            this.longitude = observer.getLongitudeDegrees();
            this.elevation = observer.getAltitudeMeters();
            this.slope = terrain.getSlopeDegrees();
            this.azm_rotation = terrain.getAspectDegrees();
            this.temperature = temperature.getValue(GeneralUnit.degC);
            this.pressure = pressure.getValue(CommonUnit.promiscuous);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public double getTimezone() {
        return timezone;
    }

    public void setTimezone(double timezone) {
        this.timezone = timezone;
    }

    public double getDelta_t() {
        return delta_t;
    }

    public void setDelta_t(double delta_t) {
        this.delta_t = delta_t;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the observer elevation.
     * @return elevation
     */
    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public double getAzmRotation() {
        return azm_rotation;
    }

    public void setAzmRotation(double azm_rotation) {
        this.azm_rotation = azm_rotation;
    }

    /** Atmospheric refraction at sunrise and sunset (0.5667 deg is typical). Valid range: -5 to 5
     * degrees */
    public double getAtmosphericRefraction() {
        return atmos_refract;
    }

    public void setAtmosphericRefraction(double atmos_refract) {
        this.atmos_refract = atmos_refract;
    }

    /**
     * Gets the Julian day.
     * @return jd
     */
    public double getJulianDay() {
        return jd;
    }

    /**
     * Gets the Julian century.
     * @return jc
     */
    public double getJulianCentury() {
        return jc;
    }

    /**
     * Gets the Julian ephemeris day.
     * @return jde
     */
    public double getJulianEphemerisDay() {
        return jde;
    }

    /**
     * Gets the Julian ephemeris century.
     * @return jce
     */
    public double getJulianEphemerisCentury() {
        return jce;
    }

    /**
     * Gets the Julian ephemeris millennium.
     * @return jme
     */
    public double getJulianEphemerisMillennium() {
        return jme;
    }

    /**
     * Gets the earth heliocentric longitude on the celestial sphere [degrees]. “Heliocentric” means
     * that the Earth position is calculated with respect to the center of the sun.
     *
     * @return l
     */
    public double getEarthHeliocentricLongitude() {
        return l;
    }

    /**
     * Gets the earth heliocentric latitude on the celestial sphere [degrees]. “Heliocentric” means
     * that the Earth position is calculated with respect to the center of the sun.
     *
     * @return b
     */
    public double getEarthHeliocentricLatitude() {
        return b;
    }

    /**
     * Gets the earth radius vector [Astronomical Units, AU].
     *
     * @return r
     */
    public double getEarthRadiusVector() {
        return r;
    }

    /**
     * Gets the geocentric longitude on the celestial sphere [degrees]. “Geocentric” means that the
     * sun position is calculated with respect to the Earth center.
     *
     * @return theta
     */
    public double getGeocentricLongitude() {
        return theta;
    }

    /**
     * Gets the geocentric latitude on the celestial sphere [degrees]. “Geocentric” means that the
     * sun position is calculated with respect to the Earth center.
     *
     * @return beta
     */
    public double getGeocentricLatitude() {
        return beta;
    }

    public double getX0() {
        return x0;
    }

    public double getX1() {
        return x1;
    }

    public double getX2() {
        return x2;
    }

    public double getX3() {
        return x3;
    }

    public double getX4() {
        return x4;
    }

    public double getDel_psi() {
        return del_psi;
    }

    public double getDel_epsilon() {
        return del_epsilon;
    }

    /** ecliptic mean obliquity [arc seconds] */
    public double getEclipticMeanObliquity() {
        return epsilon0;
    }

    /** ecliptic true obliquity [degrees] */
    public double getEclipticTrueObliquity() {
        return epsilon;
    }

    /** aberration correction [degrees] */
    public double getAberrationCorrection() {
        return del_tau;
    }

    /** apparent sun longitude on the celestial sphere [degrees]. */
    public double getApparentSunLongitude() {
        return lamda;
    }

    /** Greenwich mean sidereal time [degrees] */
    public double getGreenwichMeanSiderealTime() {
        return nu0;
    }

    /** Greenwich sidereal time [degrees] */
    public double getGreenwichSiderealTime() {
        return nu;
    }

    /**
     * Geocentric sun right ascension on the celestial sphere [degrees]. “Geocentric” means that the
     * sun position is calculated with respect to the Earth center.
     */
    public double getGeocentricSunRightAscension() {
        return alpha;
    }

    /**
     * Geocentric sun declination on the celestial sphere [degrees]. “Geocentric” means that the sun
     * position is calculated with respect to the Earth center.
     */
    public double getGeocentricSunDeclination() {
        return delta;
    }

    /**
     * Gets the observer hour angle [degrees]
     *
     * @return h
     */
    public double getObserverHourAngle() {
        return h;
    }

    public double getXi() {
        return xi;
    }

    /** sun right ascension parallax [degrees] */
    public double getSunRightAscensionParallax() {
        return del_alpha;
    }

    /**
     * Gets the topocentric sun declination [degrees]. “Topocentric” means that the sun position is
     * calculated with respect to the observer local position at the Earth surface.
     *
     * @return delta_prime
     */
    public double getTopocentricSunDeclination() {
        return delta_prime;
    }

    /**
     * Gets the topocentric sun right ascension [degrees]. “Topocentric” means that the sun position
     * is calculated with respect to the observer local position at the Earth surface.
     *
     * @return alpha_prime
     */
    public double getTopocentricSunRightAscension() {
        return alpha_prime;
    }

    /**
     * Gets the topocentric local hour angle [degrees]. “Topocentric” means that the sun position is
     * calculated with respect to the observer local position at the Earth surface.
     *
     * @return h_prime
     */
    public double getTopocentricLocalHourAngle() {
        return h_prime;
    }

    /**
     * Gets the topocentric elevation angle (uncorrected) [degrees]. “Topocentric” means that the
     * sun position is calculated with respect to the observer local position at the Earth surface.
     *
     * @return e0
     */
    public double getTopocentricElevationAngle() {
        return e0;
    }

    /** atmospheric refraction correction [degrees] */
    public double getAtmosphericRefractionCorrection() {
        return del_e;
    }

    /**
     * Gets the topocentric elevation angle (corrected for atmospheric refraction) [degrees].
     * “Topocentric” means that the sun position is calculated with respect to the observer local
     * position at the Earth surface.
     *
     * @return e
     */
    public double getTopocentricElevationAngleCorrected() {
        return e;
    }

    /** equation of time [minutes] */
    public double getEquationOfTime() {
        return eot;
    }

    /**
     * Gets the sunrise hour angle [degrees].
     *
     * @return srha
     */
    public double getSunriseHourAngle() {
        return srha;
    }

    /**
     * Gets the sunset hour angle [degrees].
     *
     * @return ssha
     */
    public double getSunsetHourAngle() {
        return ssha;
    }

    /**
     * Gets the sun transit altitude [degrees] .
     */
    public double getSunTransitAltitude() {
        return sta;
    }

    /**
     * Gets the topocentric zenith angle [degrees]. This is the angle between the observer's zenith
     * and the sun. “Topocentric” means that the sun position is calculated with respect to the
     * observer local position at the Earth surface.
     *
     * @return zenith
     */
    public double getZenith() {
        return zenith;
    }

    /**
     * Gets the topocentric azimuth angle (westward from south) [-180 to 180 degrees]. “Topocentric”
     * means that the sun position is calculated with respect to the observer local position at the
     * Earth surface.
     *
     * @return azimuth180
     */
    public double getAzimuth180() {
        return azimuth180;
    }

    /**
     * Gets the topocentric azimuth angle (eastward from north) [ 0 to 360 degrees]. “Topocentric”
     * means that the sun position is calculated with respect to the observer local position at the
     * Earth surface.
     *
     * @return azimuth
     */
    public double getAzimuth() {
        return azimuth;
    }

    /** surface incidence angle [degrees] */
    public double getIncidence() {
        return incidence;
    }

    /**
     * Gets the local sun transit time (or solar noon) [fractional hour].
     *
     * @return suntransit
     */
    public double getSunTransit() {
        return suntransit;
    }

    /**
     * Gets the local sunrise time (+/- 30 seconds) [fractional hour].
     *
     * @return sunrise
     */
    public double getSunrise() {
        return sunrise;
    }

    /**
     * Gets the local sunset time (+/- 30 seconds) [fractional hour].
     *
     * return sunset
     */
    public double getSunset() {
        return sunset;
    }

    @Override
    public String toString() {
        return "SolarPosition{" + "jd=" + jd + ", l=" + l + ", b=" + b + ", r=" + r + ", del_psi=" + del_psi + ", del_epsilon=" + del_epsilon + ", epsilon=" + epsilon + ", h=" + h + ", zenith=" + zenith + ", azimuth=" + azimuth + ", incidence=" + incidence + ", sunrise=" + sunrise + ", sunset=" + sunset + '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
