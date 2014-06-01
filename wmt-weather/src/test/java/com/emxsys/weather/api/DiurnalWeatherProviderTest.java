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
package com.emxsys.weather.api;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.visad.Times;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNoException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import visad.Field;
import visad.Real;

/**
 *
 * @author Bruce Schubert
 */
public class DiurnalWeatherProviderTest {

    private DiurnalWeatherProvider instance;

    public DiurnalWeatherProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        instance = new DiurnalWeatherProvider(ZonedDateTime.now(), GeoCoord3D.fromDegrees(34.25, -119.2));
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getName method, of class DiurnalWeatherProvider.
     */
    @Ignore
    @Test
    public void testGetName() {
        System.out.println("getName");
        String result = instance.getName();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of initializeAirTemperatures method, of class DiurnalWeatherProvider.
     */
    @Test
    public void testInitializeAirTemperatures() {
        System.out.println("initializeAirTemperatures");
        Real tempAtSunrise = new Real(WeatherType.AIR_TEMP_F, 60);
        Real tempAtNoon = new Real(WeatherType.AIR_TEMP_F, 75);
        Real tempAt1400 = new Real(WeatherType.AIR_TEMP_F, 80);
        Real tempAtSunset = new Real(WeatherType.AIR_TEMP_C, 10);
        try {
            instance.initializeAirTemperatures(tempAtSunrise, tempAtNoon, tempAt1400, tempAtSunset);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test of initializeRelativeHumidities method, of class DiurnalWeatherProvider.
     */
    @Test
    public void testInitializeRelativeHumidities() {
        System.out.println("initializeRelativeHumidities");
        Real rhAtSunrise = new Real(WeatherType.REL_HUMIDITY, 90);
        Real rhAtNoon = new Real(WeatherType.REL_HUMIDITY, 25);
        Real rhAt1400 = new Real(WeatherType.REL_HUMIDITY, 20);
        Real rhAtSunset = new Real(WeatherType.REL_HUMIDITY, 40);
        try {
            instance.initializeRelativeHumidities(rhAtSunrise, rhAtNoon, rhAt1400, rhAtSunset);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test of initializeWindSpeeds method, of class DiurnalWeatherProvider.
     */
    @Test
    public void testInitializeWindSpeeds() {
        System.out.println("initializeWindSpeeds");
        TreeMap<LocalTime, Real> windSpeeds = new TreeMap<>();
        try {
            windSpeeds.put(LocalTime.of(6, 00), new Real(WeatherType.WIND_SPEED_MPH, 5));
            windSpeeds.put(LocalTime.of(9, 00), new Real(WeatherType.WIND_SPEED_KPH, 10));
            windSpeeds.put(LocalTime.of(12, 00), new Real(WeatherType.WIND_SPEED_SI, 20));
            instance.initializeWindSpeeds(windSpeeds);
        } catch (Exception e) {
            fail(e.toString());
        }
        try {
            windSpeeds.put(LocalTime.of(16, 00), new Real(WeatherType.WIND_SPEED_SI, -5));
            instance.initializeWindSpeeds(windSpeeds);
            fail("Did not catch exception!");
        } catch (Exception e) {
            //System.out.println(e);
        }
    }

    /**
     * Test of initializeWindDirections method, of class DiurnalWeatherProvider.
     */
    @Test
    public void testInitializeWindDirections() {
        System.out.println("initializeWindDirections");
        TreeMap<LocalTime, Real> windDirections = new TreeMap<>();
        windDirections.put(LocalTime.of(6, 00), new Real(WeatherType.WIND_DIR, 5));
        windDirections.put(LocalTime.of(9, 00), new Real(WeatherType.WIND_DIR, 10));
        windDirections.put(LocalTime.of(12, 00), new Real(WeatherType.WIND_DIR, 20));
        windDirections.put(LocalTime.of(16, 00), new Real(WeatherType.WIND_DIR, -5));
        try {
            instance.initializeWindDirections(windDirections);
        } catch (Exception e) {
            assumeNoException(e);
        }
    }

    /**
     * Test of initializeCloudCovers method, of class DiurnalWeatherProvider.
     */
    @Test
    public void testInitializeCloudCovers() {
        System.out.println("initializeCloudCovers");
            TreeMap<LocalTime, Real> cloudCovers = new TreeMap<>();
        try {
            cloudCovers.put(LocalTime.of(6, 00), new Real(WeatherType.CLOUD_COVER, 5));
            instance.initializeCloudCovers(cloudCovers);
        } catch (Exception e) {
            assumeNoException(e);
        }

        try {
            cloudCovers.put(LocalTime.of(9, 00), new Real(WeatherType.CLOUD_COVER, -1));
            instance.initializeCloudCovers(cloudCovers);
            fail("Did not catch exception");
        } catch (Exception e) {
            //System.out.println(e);
        }
    }

    /**
     * Test of getHourlyWeather method, of class DiurnalWeatherProvider.
     */
    @Test
    public void testGetDailyWeather() {
        System.out.println("getDailyWeather");
        try {
            Real tempAtSunrise = new Real(WeatherType.AIR_TEMP_C, 0);
            Real tempAtNoon = new Real(WeatherType.AIR_TEMP_C, 75);
            Real tempAt1400 = new Real(WeatherType.AIR_TEMP_C, 100);
            Real tempAtSunset = new Real(WeatherType.AIR_TEMP_C, 50);
            instance.initializeAirTemperatures(tempAtSunrise, tempAtNoon, tempAt1400, tempAtSunset);

            Real rhAtSunrise = new Real(WeatherType.REL_HUMIDITY, 100);
            Real rhAtNoon = new Real(WeatherType.REL_HUMIDITY, 25);
            Real rhAt1400 = new Real(WeatherType.REL_HUMIDITY, 0);
            Real rhAtSunset = new Real(WeatherType.REL_HUMIDITY, 75);
            instance.initializeRelativeHumidities(rhAtSunrise, rhAtNoon, rhAt1400, rhAtSunset);

            TreeMap<LocalTime, Real> windSpeeds = new TreeMap<>();
            windSpeeds.put(LocalTime.of(6, 00), new Real(WeatherType.WIND_SPEED_SI, 5));
            windSpeeds.put(LocalTime.of(9, 45), new Real(WeatherType.WIND_SPEED_SI, 10));
            windSpeeds.put(LocalTime.of(12, 15), new Real(WeatherType.WIND_SPEED_SI, 20));
            windSpeeds.put(LocalTime.of(16, 30), new Real(WeatherType.WIND_SPEED_SI, 0));
            instance.initializeWindSpeeds(windSpeeds);

            TreeMap<LocalTime, Real> windDirections = new TreeMap<>();
            windDirections.put(LocalTime.of(6, 00), new Real(WeatherType.WIND_DIR, 45));
            windDirections.put(LocalTime.of(9, 00), new Real(WeatherType.WIND_DIR, 90));
            windDirections.put(LocalTime.of(12, 00), new Real(WeatherType.WIND_DIR, 180));
            windDirections.put(LocalTime.of(16, 00), new Real(WeatherType.WIND_DIR, -90));
            instance.initializeWindDirections(windDirections);

            TreeMap<LocalTime, Real> cloudCovers = new TreeMap<>();
            cloudCovers.put(LocalTime.of(6, 00), new Real(WeatherType.CLOUD_COVER, 5));
            cloudCovers.put(LocalTime.of(12, 00), new Real(WeatherType.CLOUD_COVER, 100));
            instance.initializeCloudCovers(cloudCovers);

            TemporalDomain domain = new TemporalDomain(ZonedDateTime.now(), 24);
            Field result = instance.getHourlyWeather(domain);

            assertNotNull(result);
            System.out.println(" > Daily Weather: " + result.longString());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test of getCurrentWeather method, of class DiurnalWeatherProvider.
     */
    @Ignore
    @Test
    public void testGetCurrentWeather() {
        System.out.println("getCurrentWeather");
        Coord2D coord = null;
        Real radius_ignored = null;
        Duration age_ignored = null;
        Field expResult = null;
        Field result = instance.getCurrentWeather(coord, radius_ignored, age_ignored);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
