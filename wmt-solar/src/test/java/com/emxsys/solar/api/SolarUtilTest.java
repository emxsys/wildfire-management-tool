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
package com.emxsys.solar.api;

import com.emxsys.solar.api.SolarUtil;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord2D;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author bruce
 */
public class SolarUtilTest {

    ZonedDateTime vernalEquinoxUTC = ZonedDateTime.of(2014, 03, 21, 12, 0, 0, 0, ZoneId.of("Z"));
    ZonedDateTime vernalEquinoxPST = ZonedDateTime.of(2014, 03, 21, 12, 0, 0, 0, ZoneId.of("GMT-8"));

    public SolarUtilTest() {
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
     * Test of calcJulianDate method, of class SolarUtil.
     */
    @Test
    public void testCalcJulianDate() {
        System.out.println("calcJulianDate");
        // Expected results from http://aa.usno.navy.mil/data/docs/JulianDate.php

        //  The Julian date for CE 2014 March 21 12:00:00.0 UT is JD 2456738.000000
        ZonedDateTime datetime = ZonedDateTime.of(2014, 03, 21, 12, 0, 0, 0, ZoneId.of("Z"));
        double expResult = 2456738.000000;
        assertEquals(expResult, SolarUtil.calcJulianDate(datetime), .000001);
        assertEquals(expResult, SolarUtil.calcJulianDate(Date.from(datetime.toInstant())), .000001);

        //The Julian date for CE  2014 March 21 00:00:00.0 UT is JD 2456737.500000
        datetime = ZonedDateTime.of(2014, 03, 21, 0, 0, 0, 0, ZoneId.of("Z"));
        expResult = 2456737.500000;
        assertEquals(expResult, SolarUtil.calcJulianDate(datetime), .000001);
        assertEquals(expResult, SolarUtil.calcJulianDate(Date.from(datetime.toInstant())), .000001);

        // The Julian date for CE  2014 March 21 18:30:45.0 UT is JD 2456738.271354
        datetime = ZonedDateTime.of(2014, 03, 21, 18, 30, 45, 0, ZoneId.of("Z"));
        expResult = 2456738.271354;
        assertEquals(expResult, SolarUtil.calcJulianDate(datetime), .000001);
        assertEquals(expResult, SolarUtil.calcJulianDate(Date.from(datetime.toInstant())), .000001);

        // The Julian date for CE  1985 February 17 06:00:00.0 UT is JD 2446113.750000
        datetime = ZonedDateTime.of(1985, 02, 17, 06, 0, 0, 0, ZoneId.of("Z"));
        expResult = 2446113.750000;
        assertEquals(expResult, SolarUtil.calcJulianDate(datetime), .000001);
        assertEquals(expResult, SolarUtil.calcJulianDate(Date.from(datetime.toInstant())), .000001);
    }

    /**
     * Test of getSunPosition method, of class SolarUtil.
     */
    @Test
    public void testGetSunPosition() throws VisADException, RemoteException {
        System.out.println("getSunPosition");

        Coord3D sunPosition = SolarUtil.getSunPosition(vernalEquinoxUTC);
        System.out.println(" Sun Position @ " + vernalEquinoxUTC + ": " + sunPosition.longString());

        ZonedDateTime datetime = ZonedDateTime.of(1980, 07, 27, 0, 0, 0, 0, ZoneId.of("Z"));
        sunPosition = SolarUtil.getSunPosition(datetime);
        System.out.println(" Sun Position @ " + datetime + ": " + sunPosition.longString());

        datetime = ZonedDateTime.now();
        sunPosition = SolarUtil.getSunPosition(datetime);
        System.out.println(" Sun Position @ " + datetime + ": " + sunPosition.longString());
    }

    /**
     * Test of getAzimuthAltitude method, of class SolarUtil.
     */
    @Test
    public void testGetAzimuthAltitude() throws VisADException, RemoteException {
        System.out.println("getAzimuthAltitude");
        Coord2D observer = GeoCoord2D.fromDegrees(0, 0);
        ZonedDateTime time = vernalEquinoxUTC;
        RealTuple horizonCoords = SolarUtil.getAzimuthAltitude(observer, time);
        System.out.println(" Horizon Coordinates @ " + time + ": " + horizonCoords.longString());

        observer = GeoCoord2D.fromDegrees(34.25, -119.2);
        time = ZonedDateTime.now();
        horizonCoords = SolarUtil.getAzimuthAltitude(observer, time);
        System.out.println(" Horizon Coordinates from " + observer + " @ " + time + ": " + horizonCoords.longString());

    }

    /**
     * Test of calcHourAngle method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testCalcHourAngle() {
        System.out.println("calcHourAngle");
        ZonedDateTime time = ZonedDateTime.now();
        Real expResult = null;
        Real result = SolarUtil.calcHourAngle(time);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAltitudeAngle method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testGetAltitudeAngle() {
        System.out.println("getAltitudeAngle");
        Real latitude = null;
        Real declination = null;
        Real solarHour = null;
        Real expResult = null;
        Real result = SolarUtil.getAltitudeAngle(latitude, declination, solarHour);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calcAltitudeAngle method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testCalcAltitudeAngle() {
        System.out.println("calcAltitudeAngle");
        double phi = 0.0;
        double sigma = 0.0;
        double H = 0.0;
        double expResult = 0.0;
        double result = SolarUtil.calcAltitudeAngle(phi, sigma, H);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calcAzimuthAngle method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testCalcAzimuthAngle_4args_1() {
        System.out.println("calcAzimuthAngle");
        Real latitude = null;
        Real declination = null;
        Real altitude = null;
        Real solarHour = null;
        Real expResult = null;
        Real result = SolarUtil.calcAzimuthAngle(latitude, declination, altitude, solarHour);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calcAzimuthAngle method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testCalcAzimuthAngle_4args_2() {
        System.out.println("calcAzimuthAngle");
        double phi = 0.0;
        double sigma = 0.0;
        double a = 0.0;
        double H = 0.0;
        double expResult = 0.0;
        double result = SolarUtil.calcAzimuthAngle(phi, sigma, a, H);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calcSubsolarPointAppox method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testSubsolarPoint_ZonedDateTime() {
        System.out.println("subsolarPoint");
        ZonedDateTime time = null;
        Coord2D expResult = null;
        Coord2D result = SolarUtil.calcSubsolarPointAppox(time);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calcSubsolarPointAppox method, of class SolarUtil.
     */
    @Test
    public void testSubsolarPoint() throws VisADException, RemoteException {
        System.out.println("calcSunPosition");
        ZonedDateTime time = ZonedDateTime.of(1980, 07, 27, 0, 0, 0, 0, ZoneId.of("Z"));
        for (int i = 0; i < 24; i++) {
            ZonedDateTime t = time.plusHours(i);
            double[] latLon = SolarUtil.calcSubsolarPoint(SolarUtil.calcJulianDate(t));
            System.out.println("calcSubsolarPoint(" + t + ") :" + Math.toDegrees(latLon[0]) + "," + Math.toDegrees(latLon[1]));
//            RealTuple tuple = SolarUtil.getRightAscentionDeclination(t);
//            System.out.println(" getRightAscentionDeclination(" + t + ") = "+tuple);
        }
    }

    /**
     * Test of DirectNormalSolarFlux method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testDirectNormalSolarFlux() {
        System.out.println("DirectNormalSolarFlux");
        Date date = null;
        double solarAltitudeAngleDegrees = 0.0;
        double expResult = 0.0;
        double result = SolarUtil.DirectNormalSolarFlux(date, solarAltitudeAngleDegrees);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of IncidenceAngleDegrees method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testIncidenceAngleDegrees() {
        System.out.println("IncidenceAngleDegrees");
        double surfaceTiltAngleDegrees = 0.0;
        double surfaceAzimuthAngleDegrees = 0.0;
        double solarAzimuthAngleDegrees = 0.0;
        double solarAltitudeAngleDegrees = 0.0;
        double expResult = 0.0;
        double result = SolarUtil.IncidenceAngleDegrees(surfaceTiltAngleDegrees, surfaceAzimuthAngleDegrees, solarAzimuthAngleDegrees, solarAltitudeAngleDegrees);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of DeclinationDegrees method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testDeclinationDegrees() {
        System.out.println("DeclinationDegrees");
        int dayOfYear = 0;
        double expResult = 0.0;
        double result = SolarUtil.DeclinationDegrees(dayOfYear);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of SolarHourDegrees method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testSolarHourDegrees() {
        System.out.println("SolarHourDegrees");
        double localSolarTime = 0.0;
        double expResult = 0.0;
        double result = SolarUtil.SolarHourDegrees(localSolarTime);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of LocalSolarTimeFromClockTime method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testLocalSolarTimeFromClockTime() {
        System.out.println("LocalSolarTimeFromClockTime");
        Date clockTime = null;
        double lonTzStdMer = 0.0;
        double lonActualLoc = 0.0;
        int dstMins = 0;
        double expResult = 0.0;
        double result = SolarUtil.LocalSolarTimeFromClockTime(clockTime, lonTzStdMer, lonActualLoc, dstMins);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of EquationOfTimeInHours method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testEquationOfTimeInHours() {
        System.out.println("EquationOfTimeInHours");
        int dayOfYear = 0;
        double expResult = 0.0;
        double result = SolarUtil.EquationOfTimeInHours(dayOfYear);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of ZenithAngleDegrees method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testZenithAngleDegrees() {
        System.out.println("ZenithAngleDegrees");
        double latitudeDegrees = 0.0;
        double solarHourDegrees = 0.0;
        double declinationDegrees = 0.0;
        double expResult = 0.0;
        double result = SolarUtil.ZenithAngleDegrees(latitudeDegrees, solarHourDegrees, declinationDegrees);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of AzimuthAngleDegrees method, of class SolarUtil.
     */
    @Ignore
    @Test
    public void testAzimuthAngleDegrees() {
        System.out.println("AzimuthAngleDegrees");
        double altitudeAngleDegrees = 0.0;
        double latitudeDegrees = 0.0;
        double solarHourDegrees = 0.0;
        double declinationDegrees = 0.0;
        double expResult = 0.0;
        double result = SolarUtil.AzimuthAngleDegrees(altitudeAngleDegrees, latitudeDegrees, solarHourDegrees, declinationDegrees);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
