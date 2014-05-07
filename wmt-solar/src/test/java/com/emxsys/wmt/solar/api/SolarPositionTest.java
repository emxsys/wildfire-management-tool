/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
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
package com.emxsys.wmt.solar.api;

import com.emxsys.wmt.gis.api.GeoCoord3D;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import visad.RealTuple;

/**
 *
 * @author Bruce Schubert
 */
public class SolarPositionTest {

    public SolarPositionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of SolarPosition constructor
     */
    @Test
    public void testSolarPosition() {
        System.out.println("SolarPosition constructor");
        //    spa.year          = 2003;
        //    spa.month         = 10;
        //    spa.day           = 17;
        //    spa.hour          = 12;
        //    spa.minute        = 30;
        //    spa.second        = 30;
        //    spa.timezone      = -7.0;
        //    spa.delta_ut1     = 0;
        //    spa.delta_t       = 67;
        //    spa.longitude     = -105.1786;
        //    spa.latitude      = 39.742476;
        //    spa.elevation     = 1830.14;
        //    spa.pressure      = 820;
        //    spa.temperature   = 11;
        //    spa.slope         = 30;
        //    spa.azm_rotation  = -10;
        //    spa.atmos_refract = 0.5667;
        ZonedDateTime date = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 1, ZoneId.of("-7"));
        GeoCoord3D observer = GeoCoord3D.fromDegreesAndMeters(39.742476, -105.1786, 0/*1830.14*/);
        SolarPosition spa = new SolarPosition(date, observer);
        System.out.println(spa.toString());
        /////////////////////////////////////////////
        // The output of this test should be:
        //
        //Julian Day:    2452930.312847
        //L:             2.401826e+01 degrees
        //B:             -1.011219e-04 degrees
        //R:             0.996542 AU
        //H:             11.105902 degrees
        //Delta Psi:     -3.998404e-03 degrees
        //Delta Epsilon: 1.666568e-03 degrees
        //Epsilon:       23.440465 degrees
        //Zenith:        50.111622 degrees
        //Azimuth:       194.340241 degrees
        //Incidence:     25.187000 degrees
        //Sunrise:       06:12:43 Local Time
        //Sunset:        17:20:19 Local Time
        //
        /////////////////////////////////////////////     
        //display the results inside the SPA structure
        System.out.print(String.format("Julian Day:    %.6f\n", spa.jd));
        System.out.print(String.format("L:             %.6e degrees\n", spa.l));
        System.out.print(String.format("B:             %.6e degrees\n", spa.b));
        System.out.print(String.format("R:             %.6f AU\n", spa.r));
        System.out.print(String.format("H:             %.6f degrees\n", spa.h));
        System.out.print(String.format("Delta Psi:     %.6e degrees\n", spa.del_psi));
        System.out.print(String.format("Delta Epsilon: %.6e degrees\n", spa.del_epsilon));
        System.out.print(String.format("Epsilon:       %.6f degrees\n", spa.epsilon));
        System.out.print(String.format("Zenith:        %.6f degrees\n", spa.zenith));
        System.out.print(String.format("Azimuth:       %.6f degrees\n", spa.azimuth));
        System.out.print(String.format("Incidence:     %.6f degrees\n", spa.incidence));

        double min = 60.0 * (spa.sunrise - (int) (spa.sunrise));
        double sec = 60.0 * (min - (int) min);
        System.out.print(String.format("Sunrise:       %02d:%02d:%02d Local Time\n", (int) (spa.sunrise), (int) min, (int) sec));

        min = 60.0 * (spa.sunset - (int) (spa.sunset));
        sec = 60.0 * (min - (int) min);
        System.out.print(String.format("Sunset:        %02d:%02d:%02d Local Time\n", (int) (spa.sunset), (int) min, (int) sec));

        assertEquals("JD", 2452930.312847, spa.jd, .000001);
        assertEquals("L", 2.401826e+01, spa.l, .0001);
        assertEquals("B", -1.011219e-04, spa.b, .00000001);
        assertEquals("R", 0.996542, spa.r, .000001);
        assertEquals("H", 11.105902, spa.h, .000001);
        assertEquals("Delta Psi", -3.998404e-03, spa.del_psi, .00000001);
        assertEquals("Delta Epsilon", 1.666568e-03, spa.del_epsilon, .0000001);
        assertEquals("Epsilon", 23.440465, spa.epsilon, .000001);
        assertEquals("Zenith", 50.111622, spa.zenith, .000001);
        assertEquals("Azimuth", 194.340241, spa.azimuth, .000001);
        assertEquals("Incidence", 25.187000, spa.incidence, .000001);
    }

    /**
     * Test of getPosition method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testGetData() {
        System.out.println("getData");
        SolarPosition instance = null;
        RealTuple expResult = null;
        RealTuple result = instance.getData();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of limit_degrees method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testLimit_degrees() {
        System.out.println("limit_degrees");
        double degrees = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.limit_degrees(degrees);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of limit_degrees180pm method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testLimit_degrees180pm() {
        System.out.println("limit_degrees180pm");
        double degrees = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.limit_degrees180pm(degrees);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of limit_degrees180 method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testLimit_degrees180() {
        System.out.println("limit_degrees180");
        double degrees = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.limit_degrees180(degrees);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of limit_zero2one method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testLimit_zero2one() {
        System.out.println("limit_zero2one");
        double value = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.limit_zero2one(value);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of limit_minutes method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testLimit_minutes() {
        System.out.println("limit_minutes");
        double minutes = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.limit_minutes(minutes);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of dayfrac_to_local_hr method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testDayfrac_to_local_hr() {
        System.out.println("dayfrac_to_local_hr");
        double dayfrac = 0.0;
        double timezone = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.dayfrac_to_local_hr(dayfrac, timezone);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of third_order_polynomial method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testThird_order_polynomial() {
        System.out.println("third_order_polynomial");
        double a = 0.0;
        double b = 0.0;
        double c = 0.0;
        double d = 0.0;
        double x = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.third_order_polynomial(a, b, c, d, x);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of julian_day method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testJulian_day() {
        System.out.println("julian_day");
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
        double tz = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.julian_day(year, month, day, hour, minute, second, tz);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of julian_century method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testJulian_century() {
        System.out.println("julian_century");
        double jd = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.julian_century(jd);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of julian_ephemeris_day method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testJulian_ephemeris_day() {
        System.out.println("julian_ephemeris_day");
        double jd = 0.0;
        double delta_t = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.julian_ephemeris_day(jd, delta_t);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of julian_ephemeris_century method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testJulian_ephemeris_century() {
        System.out.println("julian_ephemeris_century");
        double jde = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.julian_ephemeris_century(jde);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of julian_ephemeris_millennium method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testJulian_ephemeris_millennium() {
        System.out.println("julian_ephemeris_millennium");
        double jce = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.julian_ephemeris_millennium(jce);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of earth_periodic_term_summation method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testEarth_periodic_term_summation() {
        System.out.println("earth_periodic_term_summation");
        double[][] terms = null;
        int count = 0;
        double jme = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.earth_periodic_term_summation(terms, count, jme);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of earth_values method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testEarth_values() {
        System.out.println("earth_values");
        double[] term_sum = null;
        int count = 0;
        double jme = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.earth_values(term_sum, count, jme);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of earth_heliocentric_longitude method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testEarth_heliocentric_longitude() {
        System.out.println("earth_heliocentric_longitude");
        double jme = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.earth_heliocentric_longitude(jme);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of earth_heliocentric_latitude method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testEarth_heliocentric_latitude() {
        System.out.println("earth_heliocentric_latitude");
        double jme = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.earth_heliocentric_latitude(jme);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of earth_radius_vector method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testEarth_radius_vector() {
        System.out.println("earth_radius_vector");
        double jme = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.earth_radius_vector(jme);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of geocentric_longitude method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testGeocentric_longitude() {
        System.out.println("geocentric_longitude");
        double l = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.geocentric_longitude(l);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of geocentric_latitude method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testGeocentric_latitude() {
        System.out.println("geocentric_latitude");
        double b = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.geocentric_latitude(b);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mean_elongation_moon_sun method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testMean_elongation_moon_sun() {
        System.out.println("mean_elongation_moon_sun");
        double jce = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.mean_elongation_moon_sun(jce);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mean_anomaly_sun method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testMean_anomaly_sun() {
        System.out.println("mean_anomaly_sun");
        double jce = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.mean_anomaly_sun(jce);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mean_anomaly_moon method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testMean_anomaly_moon() {
        System.out.println("mean_anomaly_moon");
        double jce = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.mean_anomaly_moon(jce);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of argument_latitude_moon method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testArgument_latitude_moon() {
        System.out.println("argument_latitude_moon");
        double jce = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.argument_latitude_moon(jce);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of ascending_longitude_moon method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testAscending_longitude_moon() {
        System.out.println("ascending_longitude_moon");
        double jce = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.ascending_longitude_moon(jce);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of xy_term_summation method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testXy_term_summation() {
        System.out.println("xy_term_summation");
        int i = 0;
        double[] x = null;
        double expResult = 0.0;
        double result = SolarPosition.xy_term_summation(i, x);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nutation_longitude_and_obliquity method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testNutation_longitude_and_obliquity() {
        System.out.println("nutation_longitude_and_obliquity");
        double jce = 0.0;
        double[] x = null;
        double[] expResult = null;
        double[] result = SolarPosition.nutation_longitude_and_obliquity(jce, x);
        //assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of ecliptic_mean_obliquity method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testEcliptic_mean_obliquity() {
        System.out.println("ecliptic_mean_obliquity");
        double jme = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.ecliptic_mean_obliquity(jme);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of ecliptic_true_obliquity method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testEcliptic_true_obliquity() {
        System.out.println("ecliptic_true_obliquity");
        double delta_epsilon = 0.0;
        double epsilon0 = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.ecliptic_true_obliquity(delta_epsilon, epsilon0);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of aberration_correction method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testAberration_correction() {
        System.out.println("aberration_correction");
        double r = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.aberration_correction(r);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of apparent_sun_longitude method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testApparent_sun_longitude() {
        System.out.println("apparent_sun_longitude");
        double theta = 0.0;
        double delta_psi = 0.0;
        double delta_tau = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.apparent_sun_longitude(theta, delta_psi, delta_tau);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of greenwich_mean_sidereal_time method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testGreenwich_mean_sidereal_time() {
        System.out.println("greenwich_mean_sidereal_time");
        double jd = 0.0;
        double jc = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.greenwich_mean_sidereal_time(jd, jc);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of greenwich_sidereal_time method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testGreenwich_sidereal_time() {
        System.out.println("greenwich_sidereal_time");
        double nu0 = 0.0;
        double delta_psi = 0.0;
        double epsilon = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.greenwich_sidereal_time(nu0, delta_psi, epsilon);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of geocentric_sun_right_ascension method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testGeocentric_sun_right_ascension() {
        System.out.println("geocentric_sun_right_ascension");
        double lamda = 0.0;
        double epsilon = 0.0;
        double beta = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.geocentric_sun_right_ascension(lamda, epsilon, beta);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of geocentric_sun_declination method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testGeocentric_sun_declination() {
        System.out.println("geocentric_sun_declination");
        double beta = 0.0;
        double epsilon = 0.0;
        double lamda = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.geocentric_sun_declination(beta, epsilon, lamda);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of observer_hour_angle method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testObserver_hour_angle() {
        System.out.println("observer_hour_angle");
        double nu = 0.0;
        double longitude = 0.0;
        double alpha_deg = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.observer_hour_angle(nu, longitude, alpha_deg);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sun_equatorial_horizontal_parallax method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testSun_equatorial_horizontal_parallax() {
        System.out.println("sun_equatorial_horizontal_parallax");
        double r = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.sun_equatorial_horizontal_parallax(r);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sun_right_ascension_parallax_and_topocentric_dec method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testSun_right_ascension_parallax_and_topocentric_dec() {
        System.out.println("sun_right_ascension_parallax_and_topocentric_dec");
        double latitude = 0.0;
        double elevation = 0.0;
        double xi = 0.0;
        double h = 0.0;
        double delta = 0.0;
        double[] expResult = null;
        double[] result = SolarPosition.sun_right_ascension_parallax_and_topocentric_dec(latitude, elevation, xi, h, delta);
        //assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of topocentric_sun_right_ascension method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testTopocentric_sun_right_ascension() {
        System.out.println("topocentric_sun_right_ascension");
        double alpha_deg = 0.0;
        double delta_alpha = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.topocentric_sun_right_ascension(alpha_deg, delta_alpha);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of topocentric_local_hour_angle method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testTopocentric_local_hour_angle() {
        System.out.println("topocentric_local_hour_angle");
        double h = 0.0;
        double delta_alpha = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.topocentric_local_hour_angle(h, delta_alpha);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of topocentric_elevation_angle method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testTopocentric_elevation_angle() {
        System.out.println("topocentric_elevation_angle");
        double latitude = 0.0;
        double delta_prime = 0.0;
        double h_prime = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.topocentric_elevation_angle(latitude, delta_prime, h_prime);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of atmospheric_refraction_correction method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testAtmospheric_refraction_correction() {
        System.out.println("atmospheric_refraction_correction");
        double pressure = 0.0;
        double temperature = 0.0;
        double atmos_refract = 0.0;
        double e0 = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.atmospheric_refraction_correction(pressure, temperature, atmos_refract, e0);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of topocentric_elevation_angle_corrected method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testTopocentric_elevation_angle_corrected() {
        System.out.println("topocentric_elevation_angle_corrected");
        double e0 = 0.0;
        double delta_e = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.topocentric_elevation_angle_corrected(e0, delta_e);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of topocentric_zenith_angle method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testTopocentric_zenith_angle() {
        System.out.println("topocentric_zenith_angle");
        double e = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.topocentric_zenith_angle(e);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of topocentric_azimuth_angle_neg180_180 method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testTopocentric_azimuth_angle_neg180_180() {
        System.out.println("topocentric_azimuth_angle_neg180_180");
        double h_prime = 0.0;
        double latitude = 0.0;
        double delta_prime = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.topocentric_azimuth_angle_neg180_180(h_prime, latitude, delta_prime);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of topocentric_azimuth_angle_zero_360 method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testTopocentric_azimuth_angle_zero_360() {
        System.out.println("topocentric_azimuth_angle_zero_360");
        double azimuth180 = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.topocentric_azimuth_angle_zero_360(azimuth180);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of surface_incidence_angle method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testSurface_incidence_angle() {
        System.out.println("surface_incidence_angle");
        double zenith = 0.0;
        double azimuth180 = 0.0;
        double azm_rotation = 0.0;
        double slope = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.surface_incidence_angle(zenith, azimuth180, azm_rotation, slope);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sun_mean_longitude method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testSun_mean_longitude() {
        System.out.println("sun_mean_longitude");
        double jme = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.sun_mean_longitude(jme);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of eot method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testEot() {
        System.out.println("eot");
        double m = 0.0;
        double alpha = 0.0;
        double del_psi = 0.0;
        double epsilon = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.eot(m, alpha, del_psi, epsilon);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of approx_sun_transit_time method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testApprox_sun_transit_time() {
        System.out.println("approx_sun_transit_time");
        double alpha_zero = 0.0;
        double longitude = 0.0;
        double nu = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.approx_sun_transit_time(alpha_zero, longitude, nu);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sun_hour_angle_at_rise_set method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testSun_hour_angle_at_rise_set() {
        System.out.println("sun_hour_angle_at_rise_set");
        double latitude = 0.0;
        double delta_zero = 0.0;
        double h0_prime = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.sun_hour_angle_at_rise_set(latitude, delta_zero, h0_prime);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of approx_sun_rise_and_set method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testApprox_sun_rise_and_set() {
        System.out.println("approx_sun_rise_and_set");
        double[] m_rts = null;
        double h0 = 0.0;
        SolarPosition.approx_sun_rise_and_set(m_rts, h0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rts_alpha_delta_prime method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testRts_alpha_delta_prime() {
        System.out.println("rts_alpha_delta_prime");
        double[] ad = null;
        double n = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.rts_alpha_delta_prime(ad, n);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rts_sun_altitude method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testRts_sun_altitude() {
        System.out.println("rts_sun_altitude");
        double latitude = 0.0;
        double delta_prime = 0.0;
        double h_prime = 0.0;
        double expResult = 0.0;
        double result = SolarPosition.rts_sun_altitude(latitude, delta_prime, h_prime);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sun_rise_and_set method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testSun_rise_and_set() {
        System.out.println("sun_rise_and_set");
        double[] m_rts = null;
        double[] h_rts = null;
        double[] delta_prime = null;
        double latitude = 0.0;
        double[] h_prime = null;
        double h0_prime = 0.0;
        int sun = 0;
        double expResult = 0.0;
        double result = SolarPosition.sun_rise_and_set(m_rts, h_rts, delta_prime, latitude, h_prime, h0_prime, sun);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculate_geocentric_sun_right_ascension_and_declination method, of class
     * SolarPosition.
     */
    @Ignore
    @Test
    public void testCalculate_geocentric_sun_right_ascension_and_declination() {
        System.out.println("calculate_geocentric_sun_right_ascension_and_declination");
        SolarPosition spa = null;
        SolarPosition.calculate_geocentric_sun_right_ascension_and_declination(spa);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculate_eot_and_sun_rise_transit_set method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testCalculate_eot_and_sun_rise_transit_set() {
        System.out.println("calculate_eot_and_sun_rise_transit_set");
        SolarPosition spa = null;
        SolarPosition.calculate_eot_and_sun_rise_transit_set(spa);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of spa_calculate method, of class SolarPosition.
     */
    @Ignore
    @Test
    public void testSpa_calculate() {
        System.out.println("spa_calculate");
        SolarPosition spa = null;
        SolarPosition.spa_calculate(spa);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
