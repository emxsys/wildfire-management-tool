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
package com.emxsys.wmt.solar.internal;



/**
 *
 * @author Bruce Schubert
 */
public class SolarCalculator implements Cloneable {

    public SolarCalculator() {
    }

     public static double dayfrac_to_local_hr(double dayfrac, double timezone) {
        return SolarPositionAlgorithms.dayfrac_to_local_hr(dayfrac, timezone);
    }

    public static double third_order_polynomial(double a, double b, double c, double d, double x) {
        return SolarPositionAlgorithms.third_order_polynomial(a, b, c, d, x);
    }

    public static double julian_day(int year, int month, int day, int hour, int minute, int second, double tz) {
        return SolarPositionAlgorithms.julian_day(year, month, day, hour, minute, second, tz);
    }

    public static double julian_century(double jd) {
        return SolarPositionAlgorithms.julian_century(jd);
    }

    public static double julian_ephemeris_day(double jd, double delta_t) {
        return SolarPositionAlgorithms.julian_ephemeris_day(jd, delta_t);
    }

    public static double julian_ephemeris_century(double jde) {
        return SolarPositionAlgorithms.julian_ephemeris_century(jde);
    }

    public static double julian_ephemeris_millennium(double jce) {
        return SolarPositionAlgorithms.julian_ephemeris_millennium(jce);
    }

    public static double earth_periodic_term_summation(double[][] terms, int count, double jme) {
        return SolarPositionAlgorithms.earth_periodic_term_summation(terms, count, jme);
    }

    public static double earth_values(double[] term_sum, int count, double jme) {
        return SolarPositionAlgorithms.earth_values(term_sum, count, jme);
    }

    public static double earth_heliocentric_longitude(double jme) {
        return SolarPositionAlgorithms.earth_heliocentric_longitude(jme);
    }

    public static double earth_heliocentric_latitude(double jme) {
        return SolarPositionAlgorithms.earth_heliocentric_latitude(jme);
    }

    public static double earth_radius_vector(double jme) {
        return SolarPositionAlgorithms.earth_radius_vector(jme);
    }

    public static double geocentric_longitude(double l) {
        return SolarPositionAlgorithms.geocentric_longitude(l);
    }

    public static double geocentric_latitude(double b) {
        return SolarPositionAlgorithms.geocentric_latitude(b);
    }

    public static double mean_elongation_moon_sun(double jce) {
        return SolarPositionAlgorithms.mean_elongation_moon_sun(jce);
    }

    public static double mean_anomaly_sun(double jce) {
        return SolarPositionAlgorithms.mean_anomaly_sun(jce);
    }

    public static double mean_anomaly_moon(double jce) {
        return SolarPositionAlgorithms.mean_anomaly_moon(jce);
    }

    public static double argument_latitude_moon(double jce) {
        return SolarPositionAlgorithms.argument_latitude_moon(jce);
    }

    public static double ascending_longitude_moon(double jce) {
        return SolarPositionAlgorithms.ascending_longitude_moon(jce);
    }

    public static double xy_term_summation(int i, double[] x) {
        return SolarPositionAlgorithms.xy_term_summation(i, x);
    }

    /**
     *
     * @param jce Julian ephemeris millennium
     * @param x Array: {mean_elongation_moon_sun, mean_anomaly_sun, mean_anomaly_moon,
     * argument_latitude_moon, ascending_longitude_moon }
     * @return Array: {nutation longitude, nutation obliquity} [degrees]
     */
    public static double[] nutation_longitude_and_obliquity(double jce, double[] x) {
        return SolarPositionAlgorithms.nutation_longitude_and_obliquity(jce, x);
    }

    public static double ecliptic_mean_obliquity(double jme) {
        return SolarPositionAlgorithms.ecliptic_mean_obliquity(jme);
    }

    public static double ecliptic_true_obliquity(double delta_epsilon, double epsilon0) {
        return SolarPositionAlgorithms.ecliptic_true_obliquity(delta_epsilon, epsilon0);
    }

    public static double aberration_correction(double r) {
        return SolarPositionAlgorithms.aberration_correction(r);
    }

    public static double apparent_sun_longitude(double theta, double delta_psi, double delta_tau) {
        return SolarPositionAlgorithms.apparent_sun_longitude(theta, delta_psi, delta_tau);
    }

    public static double greenwich_mean_sidereal_time(double jd, double jc) {
        return SolarPositionAlgorithms.greenwich_mean_sidereal_time(jd, jc);
    }

    public static double greenwich_sidereal_time(double nu0, double delta_psi, double epsilon) {
        return SolarPositionAlgorithms.greenwich_sidereal_time(nu0, delta_psi, epsilon);
    }

    public static double geocentric_sun_right_ascension(double lamda, double epsilon, double beta) {
        return SolarPositionAlgorithms.geocentric_sun_right_ascension(lamda, epsilon, beta);
    }

    public static double geocentric_sun_declination(double beta, double epsilon, double lamda) {
        return SolarPositionAlgorithms.geocentric_sun_declination(beta, epsilon, lamda);
    }

    public static double observer_hour_angle(double nu, double longitude, double alpha_deg) {
        return SolarPositionAlgorithms.observer_hour_angle(nu, longitude, alpha_deg);
    }

    public static double sun_equatorial_horizontal_parallax(double r) {
        return SolarPositionAlgorithms.sun_equatorial_horizontal_parallax(r);
    }

    public static double[] sun_right_ascension_parallax_and_topocentric_dec(double latitude, double elevation, double xi, double h, double delta) {
        return SolarPositionAlgorithms.sun_right_ascension_parallax_and_topocentric_dec(latitude, elevation, xi, h, delta);
    }

    public static double topocentric_sun_right_ascension(double alpha_deg, double delta_alpha) {
        return SolarPositionAlgorithms.topocentric_sun_right_ascension(alpha_deg, delta_alpha);
    }

    public static double topocentric_local_hour_angle(double h, double delta_alpha) {
        return SolarPositionAlgorithms.topocentric_local_hour_angle(h, delta_alpha);
    }

    public static double topocentric_elevation_angle(double latitude, double delta_prime, double h_prime) {
        return SolarPositionAlgorithms.topocentric_elevation_angle(latitude, delta_prime, h_prime);
    }

    public static double atmospheric_refraction_correction(double pressure, double temperature, double atmos_refract, double e0) {
        return SolarPositionAlgorithms.atmospheric_refraction_correction(pressure, temperature, atmos_refract, e0);
    }

    public static double topocentric_elevation_angle_corrected(double e0, double delta_e) {
        return SolarPositionAlgorithms.topocentric_elevation_angle_corrected(e0, delta_e);
    }

    public static double topocentric_zenith_angle(double e) {
        return SolarPositionAlgorithms.topocentric_zenith_angle(e);
    }

    public static double topocentric_azimuth_angle_neg180_180(double h_prime, double latitude, double delta_prime) {
        return SolarPositionAlgorithms.topocentric_azimuth_angle_neg180_180(h_prime, latitude, delta_prime);
    }

    public static double topocentric_azimuth_angle_zero_360(double azimuth180) {
        return SolarPositionAlgorithms.topocentric_azimuth_angle_zero_360(azimuth180);
    }

    public static double surface_incidence_angle(double zenith, double azimuth180, double azm_rotation, double slope) {
        return SolarPositionAlgorithms.surface_incidence_angle(zenith, azimuth180, azm_rotation, slope);
    }

    public static double sun_mean_longitude(double jme) {
        return SolarPositionAlgorithms.sun_mean_longitude(jme);
    }

    public static double eot(double m, double alpha, double del_psi, double epsilon) {
        return SolarPositionAlgorithms.eot(m, alpha, del_psi, epsilon);
    }

    public static double approx_sun_transit_time(double alpha_zero, double longitude, double nu) {
        return SolarPositionAlgorithms.approx_sun_transit_time(alpha_zero, longitude, nu);
    }

    public static double sun_hour_angle_at_rise_set(double latitude, double delta_zero, double h0_prime) {
        return SolarPositionAlgorithms.sun_hour_angle_at_rise_set(latitude, delta_zero, h0_prime);
    }

    public static void approx_sun_rise_and_set(double[] m_rts, double h0) {
        SolarPositionAlgorithms.approx_sun_rise_and_set(m_rts, h0);
    }

    public static double rts_alpha_delta_prime(double[] ad, double n) {
        return SolarPositionAlgorithms.rts_alpha_delta_prime(ad, n);
    }

    public static double rts_sun_altitude(double latitude, double delta_prime, double h_prime) {
        return SolarPositionAlgorithms.rts_sun_altitude(latitude, delta_prime, h_prime);
    }

    public static double sun_rise_and_set(double[] m_rts, double[] h_rts, double[] delta_prime, double latitude, double[] h_prime, double h0_prime, int sun) {
        return SolarPositionAlgorithms.sun_rise_and_set(m_rts, h_rts, delta_prime, latitude, h_prime, h0_prime, sun);
    }

}
