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
 *     - Neither the name of Bruce Schubert,  nor the names of its 
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

import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.TerrainTuple;
import com.emxsys.solar.internal.SolarData;
import com.emxsys.solar.spi.SunlightProviderFactory;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import visad.Real;
import visad.RealTuple;

/**
 *
 * @author Bruce Schubert
 */
public class BasicSunlightTest {

    private BasicSunlight instance;
    private ZonedDateTime date;
    private GeoCoord3D observer;

    public BasicSunlightTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        date = ZonedDateTime.of(2014, 05, 29, 15, 00, 00, 00, ZoneId.of("-8"));
        observer = GeoCoord3D.fromDegreesAndMeters(34.25, -119.2, 60);
        instance = SunlightProviderFactory.getInstance().getSunlight(date, observer);
        /*
         Date,Time,Topocentric zenith angle,Top. azimuth angle (eastward from N),Local sunrise time,Local sun transit time,Local sunset time,Observer hour angle,Topocentric sun declination,Topocentric local hour angle,Top. elevation angle (uncorrected),Sunrise hour angle,Sunset hour angle,Sun transit altitude
         5/29/2014,0:00:00,124.114195,1.638156,4.782459,11.903856,19.019525,181.458900,21.618566,181.458845,-34.114195,-106.760688,106.737361,77.446386
         5/29/2014,1:00:00,121.973057,18.086403,4.782459,11.903856,19.019525,196.457511,21.625029,196.456904,-31.973057,-106.760688,106.737361,77.446386
         5/29/2014,2:00:00,116.615006,32.858815,4.782459,11.903856,19.019525,211.456119,21.631529,211.455000,-26.615006,-106.760688,106.737361,77.446386
         5/29/2014,3:00:00,108.772834,45.364473,4.782459,11.903856,19.019525,226.454725,21.638060,226.453169,-18.772834,-106.760688,106.737361,77.446386
         5/29/2014,4:00:00,99.182900,55.799431,4.782459,11.903856,19.019525,241.453326,21.644616,241.451442,-9.182900,-106.760688,106.737361,77.446386
         5/29/2014,5:00:00,88.108612,64.678451,4.782459,11.903856,19.019525,256.451925,21.651184,256.449839,1.584219,-106.760688,106.737361,77.446386
         5/29/2014,6:00:00,76.803184,72.562027,4.782459,11.903856,19.019525,271.450521,21.657754,271.448375,13.128032,-106.760688,106.737361,77.446386
         5/29/2014,7:00:00,64.797131,80.006649,4.782459,11.903856,19.019525,286.449113,21.664312,286.447055,25.167747,-106.760688,106.737361,77.446386
         5/29/2014,8:00:00,52.491706,87.661366,4.782459,11.903856,19.019525,301.447702,21.670844,301.445871,37.486633,-106.760688,106.737361,77.446386
         5/29/2014,9:00:00,40.117892,96.565812,4.782459,11.903856,19.019525,316.446288,21.677340,316.444809,49.868073,-106.760688,106.737361,77.446386
         5/29/2014,10:00:00,28.032867,109.115071,4.782459,11.903856,19.019525,331.444871,21.683788,331.443845,61.958264,-106.760688,106.737361,77.446386
         5/29/2014,11:00:00,17.312508,132.967747,4.782459,11.903856,19.019525,346.443451,21.690182,346.442948,72.682309,-106.760688,106.737361,77.446386
         5/29/2014,12:00:00,12.613735,186.145145,4.782459,11.903856,19.019525,1.442028,21.696516,1.442082,77.382552,-106.760688,106.737361,77.446386
         5/29/2014,13:00:00,19.135239,233.320758,4.782459,11.903856,19.019525,16.440601,21.702788,16.441209,70.858989,-106.760688,106.737361,77.446386
         5/29/2014,14:00:00,30.290704,253.846182,4.782459,11.903856,19.019525,31.439172,21.709002,31.440291,59.699565,-106.760688,106.737361,77.446386
         5/29/2014,15:00:00,42.471631,265.372919,4.782459,11.903856,19.019525,46.437739,21.715162,46.439295,47.513124,-106.760688,106.737361,77.446386
         5/29/2014,16:00:00,54.846901,273.921964,4.782459,11.903856,19.019525,61.436303,21.721276,61.438189,35.129505,-106.760688,106.737361,77.446386
         5/29/2014,17:00:00,67.104656,281.479193,4.782459,11.903856,19.019525,76.434864,21.727355,76.436951,22.856290,-106.760688,106.737361,77.446386
         5/29/2014,18:00:00,79.013984,288.968163,4.782459,11.903856,19.019525,91.433423,21.733411,91.435569,10.903991,-106.760688,106.737361,77.446386
         5/29/2014,19:00:00,89.948310,297.005411,4.782459,11.903856,19.019525,106.431978,21.739457,106.434037,-0.502155,-106.760688,106.737361,77.446386
         5/29/2014,20:00:00,101.062185,306.143531,4.782459,11.903856,19.019525,121.430530,21.745507,121.432361,-11.062185,-106.760688,106.737361,77.446386
         5/29/2014,21:00:00,110.346044,316.940562,4.782459,11.903856,19.019525,136.429079,21.751572,136.430558,-20.346044,-106.760688,106.737361,77.446386
         5/29/2014,22:00:00,117.748595,329.874142,4.782459,11.903856,19.019525,151.427624,21.757662,151.428651,-27.748595,-106.760688,106.737361,77.446386
         5/29/2014,23:00:00,122.517191,345.019905,4.782459,11.903856,19.019525,166.426167,21.763786,166.426671,-32.517191,-106.760688,106.737361,77.446386    }
        
        
         5/29/2014,  Date,
         15:00:00,   Time,
         42.471631,  Topocentric zenith angle,
         265.372919, Top. azimuth angle (eastward from N),
         4.782459,   Local sunrise time,
         11.903856,  Local sun transit time,
         19.019525,  Local sunset time,
         46.437739,  Observer hour angle,
         21.715162,  Topocentric sun declination,
         46.439295,  Topocentric local hour angle,
         47.513124,  Top. elevation angle (uncorrected),
         47.525909,  Top. elevation angle (corrected),
         -106.760688,Sunrise hour angle,
         106.737361, Sunset hour angle,
         77.446386,  Sun transit altitude

         5/29/2014,15:00:00,42.474091,265.372919,4.782459,11.903856,19.019525,46.437739,21.715162,46.439295,47.513124,47.525909,-106.760688,106.737361,77.446386

         */
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of fromRealTuple method, of class BasicSunlight.
     */
    @Test
    public void testFromRealTuple() {
        System.out.println("fromRealTuple");
        RealTuple sunightTuple = new RealTuple(SolarType.SUNLIGHT);
        BasicSunlight result = BasicSunlight.fromRealTuple(sunightTuple);
    }

    /**
     * Test of getDeclination method, of class BasicSunlight.
     */
    @Test
    public void testGetDeclination() {
        //5/29/2014,15:00:00,42.474091,265.372919,4.782459,11.903856,19.019525,46.437739,21.715162,46.439295,47.513124,-106.760688,106.737361,77.446386
        System.out.println("getDeclination");
        Real result = instance.getDeclination();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("declination", 21.715162, result.getValue(), .001);
    }

    /**
     * Test of getSubsolarLatitude method, of class BasicSunlight.
     */
    @Test
    public void testGetSubsolarLatitude() {
        System.out.println("getSubsolarLatitude");
        Real result = instance.getSubsolarLatitude();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("subsolar latitude", 21.715162, result.getValue(), .001);
    }

    /**
     * Test of getSubsolarLongitude method, of class BasicSunlight.
     */
    @Test
    public void testGetSubsolarLongitude() {
        System.out.println("getSubsolarLongitude");
        Real result = instance.getSubsolarLongitude();
        System.out.println(observer + " @ " + date + ": " +result);
    }

    /**
     * Test of getAzimuthAngle method, of class BasicSunlight.
     */
    @Test
    public void testGetAzimuthAngle() {
        System.out.println("getAzimuthAngle");
        Real result = instance.getAzimuthAngle();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("azimuth", 265.372919, result.getValue(), .001);
    }

    /**
     * Test of getZenithAngle method, of class BasicSunlight.
     */
    @Test
    public void testGetZenithAngle() {
        System.out.println("getZenithAngle");
        Real result = instance.getZenithAngle();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("zenith", 42.471631, result.getValue(), .001);
    }

    /**
     * Test of getAltitudeAngle method, of class BasicSunlight.
     */
    @Test
    public void testGetAltitudeAngle() {
        System.out.println("getAltitudeAngle");
        Real result = instance.getAltitudeAngle();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("altitude angle (corrected)", 47.525909, result.getValue(), .003);
    }

    /**
     * Test of getLocalHourAngle method, of class BasicSunlight.
     */
    @Test
    public void testGetLocalHourAngle() {
        System.out.println("getLocalHourAngle");
        Real result = instance.getLocalHourAngle();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("local hour angle", 46.439295, result.getValue(), .001);
    }

    /**
     * Test of getSunriseHourAngle method, of class BasicSunlight.
     */
    @Test
    public void testGetSunriseHourAngle() {
        System.out.println("getSunriseHourAngle");
        Real result = instance.getSunriseHourAngle();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("sunrise hour angle", -106.760688, result.getValue(), .001);
    }

    /**
     * Test of getSunsetHourAngle method, of class BasicSunlight.
     */
    @Test
    public void testGetSunsetHourAngle() {
        System.out.println("getSunsetHourAngle");
        Real result = instance.getSunsetHourAngle();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("sunset hour angle", 106.737361, result.getValue(), .001);
    }

    /**
     * Test of getSunriseHour method, of class BasicSunlight.
     */
    @Test
    public void testGetSunriseHour() {
        System.out.println("getSunriseHour");
        Real result = instance.getSunriseHour();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("sunrise hour std time", 4.782459, result.getValue(), .001);
    }

    /**
     * Test of getSunsetHour method, of class BasicSunlight.
     */
    @Test
    public void testGetSunsetHour() {
        System.out.println("getSunsetHour");
        Real result = instance.getSunsetHour();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("sunset hour std time", 19.019525, result.getValue(), .001);
    }

    /**
     * Test of getSunTransitHour method, of class BasicSunlight.
     */
    @Test
    public void testGetSunTransitHour() {
        System.out.println("getSunTransitHour");
        Real result = instance.getSunTransitHour();
        System.out.println(observer + " @ " + date + ": " +result);
        assertEquals("sun transit hour std time", 11.903856, result.getValue(), .001);
    }

}
