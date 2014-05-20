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
package com.emxsys.wmt.weather.mesowest;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GisType;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.visad.Reals;
import java.rmi.RemoteException;
import java.util.Date;
import java.time.Duration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import visad.Data;
import visad.Field;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class MesoWestWeatherProviderTest {

    public MesoWestWeatherProviderTest() {
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
     * Test of getInstance method, of class MesoWestWeatherProvider.
     */
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        // 1st instance tests lookup
        MesoWestWeatherProvider result = MesoWestWeatherProvider.getInstance();
        assertNotNull(result);
        // 2nd call of constructor should throw exeception
        exception.expect(IllegalStateException.class);
        MesoWestWeatherProvider instance = new MesoWestWeatherProvider();
    }

    /**
     * Test of getWeather method, of class MesoWestWeatherProvider.
     */
    @Test
    public void testGetWeather() {
        System.out.println("getWeather");
        Date utcTime = null;
        Coord2D coord = null;
        MesoWestWeatherProvider instance = MesoWestWeatherProvider.getInstance();

    }

    /**
     * Test of getLatestWeather method, of class MesoWestWeatherProvider.
     */
    @Test
    public void testGetLatestWeather() throws VisADException, RemoteException {
        System.out.println("getLatestWeather");
        Coord2D coord = GeoCoord2D.fromDegrees(34.25, -119.2);
        Real radius = Reals.newDistance(52800, GeneralUnit.foot);
        MesoWestWeatherProvider instance = MesoWestWeatherProvider.getInstance();
        
        Field result = null;
        try {
            result = instance.getLatestWeather(coord, radius);
        } catch (Exception e) {
            System.out.println(e.toString());
            Assume.assumeNoException(e);
        }
        assertNotNull(result);
        System.out.println(result.toString());

        float[][] samples = result.getDomainSet().getSamples(false);
        double[][] values = result.getValues(false);
        for (int i = 0; i < samples[0].length; i++) {
            String text = String.format("(%1$f,%2$f) -> (%3$.1f, %4$.1f, %5$.1f, %6$.1f)",
                    samples[0][i], samples[1][i],
                    values[0][i], values[1][i], values[2][i], values[3][i]);
            System.out.println(text);
        }
        RealTuple domainTuple = new RealTuple(GisType.LATLON, new double[]{34.26, -119.1});
        Data data = result.evaluate(domainTuple);
        System.out.println("Sampled: "+ domainTuple.toString() + " -> " + data.toString());
    }

    /**
     * Test of getLatestWeather method, of class MesoWestWeatherProvider.
     */
    @Test
    public void testGetLatestAgedWeather() throws VisADException, RemoteException {
        System.out.println("getLatestAgedWeather");
        Coord2D coord = GeoCoord2D.fromDegrees(34.25, -119.2);
        Real radius = Reals.newDistance(52800, GeneralUnit.foot);
        Duration age = Duration.ofMinutes(60);
        MesoWestWeatherProvider instance = MesoWestWeatherProvider.getInstance();
        Field result = null;
        try {
            result = instance.getLatestWeather(coord, radius, age);
        } catch (Exception e) {
            System.out.println(e.toString());
            Assume.assumeNoException(e);
        }
        assertNotNull(result);
        System.out.println(result.toString());

        float[][] samples = result.getDomainSet().getSamples(false);
        double[][] values = result.getValues(false);
        for (int i = 0; i < samples[0].length; i++) {
            String text = String.format("(%1$f,%2$f) -> (%3$.1f, %4$.1f, %5$.1f, %6$.1f)",
                    samples[0][i], samples[1][i],
                    values[0][i], values[1][i], values[2][i], values[3][i]);
            System.out.println(text);
        }

        // Sample the data (need at least three points to define a triangle
        if (result.getLength() > 2) {
            RealTuple domainTuple = new RealTuple(GisType.LATLON, new double[]{34.26, -119.1});
            Data data = result.evaluate(domainTuple);
            System.out.println(domainTuple.toString() + " -> " + data.toString());
        }
    }
}
