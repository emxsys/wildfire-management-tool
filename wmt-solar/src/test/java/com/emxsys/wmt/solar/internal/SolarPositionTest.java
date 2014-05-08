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
package com.emxsys.wmt.solar.internal;

import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.gis.api.Terrain;
import com.emxsys.wmt.gis.api.TerrainTuple;
import com.emxsys.wmt.solar.internal.SolarPositionAlgorithms;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import visad.Real;
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
     * Test of SolarPositionAlgorithms constructor
     */
    @Test
    public void testSolarPosition() {
        System.out.println("SolarPositionCalculator");
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
        
        SolarData spa = new SolarData(date, observer, new TerrainTuple(-10,30,0), new Real(11), new Real(820));
        SolarPositionAlgorithms.spa_calculate(spa);

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
        //display the results inside the SolarPositionAlgorithms structure
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

        assertEquals("JD", 2452930.312847, spa.getJulianDay(), .000001);
        assertEquals("L", 2.401826e+01, spa.getEarthHeliocentricLongitude(), .0001);
        assertEquals("B", -1.011219e-04, spa.getEarthHeliocentricLatitude(), .00000001);
        assertEquals("R", 0.996542, spa.getEarthRadiusVector(), .000001);
        assertEquals("H", 11.105902, spa.getObserverHourAngle(), .000001);
        assertEquals("Delta Psi", -3.998404e-03, spa.del_psi, .00000001);
        assertEquals("Delta Epsilon", 1.666568e-03, spa.del_epsilon, .0000001);
        assertEquals("Epsilon", 23.440465, spa.getEclipticTrueObliquity(), .000001);
        assertEquals("Zenith", 50.111622, spa.getZenith(), .000001);
        assertEquals("Azimuth", 194.340241, spa.getAzimuth(), .000001);
        assertEquals("Incidence", 25.187000, spa.getIncidence(), .000001);
    }


}
