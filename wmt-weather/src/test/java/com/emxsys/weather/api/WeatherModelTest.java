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
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.SpatialField;
import com.emxsys.visad.TemporalDomain;
import static com.emxsys.weather.api.WeatherType.FIRE_WEATHER;
import java.lang.instrument.Instrumentation;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import visad.FieldImpl;
import visad.FlatField;
import visad.RealTuple;
import visad.Tuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class WeatherModelTest {

    private WeatherModel instance;

    public WeatherModelTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @Before
    public void setUp() throws VisADException, RemoteException {
        System.out.println("from(TemporalDomain,SpatialFieldArr)");
        TemporalDomain timeDomain = new TemporalDomain(ZonedDateTime.now(), 12);
        SpatialDomain spatialDomain = new SpatialDomain(
                GeoCoord2D.fromDegrees(34.0, -120.0),
                GeoCoord2D.fromDegrees(35.0, -119.0), 5, 5);
        final int numTimes = timeDomain.getTemporalDomainSetLength();
        final int numLatLons = spatialDomain.getSpatialDomainSetLength();

        SpatialField[] fields = new SpatialField[numTimes];
        for (int t = 0; t < numTimes; t++) {
            double[][] rangeSamples = new double[FIRE_WEATHER.getDimension()][numLatLons];
            for (int xy = 0; xy < numLatLons; xy++) {
                rangeSamples[0][xy] = 1. + xy + t;    // air temp
                rangeSamples[1][xy] = 2. + xy + t;    // rh
                rangeSamples[2][xy] = 3. + xy + t;    // wnd spd
                rangeSamples[3][xy] = 4. + xy + t;    // wnd dir
                rangeSamples[4][xy] = 5. + xy + t;    // cloud cover
            }
            fields[t] = SpatialField.from(spatialDomain, FIRE_WEATHER, rangeSamples);
        }
        instance = WeatherModel.from(timeDomain, fields);

    }

    @Test
    public void testConstructor() throws VisADException, RemoteException {
        System.out.println("constructor");

        TemporalDomain timeDomain = new TemporalDomain(ZonedDateTime.now(), 12);
        SpatialDomain spatialDomain = new SpatialDomain(
                GeoCoord2D.fromDegrees(34.0, -120.0),
                GeoCoord2D.fromDegrees(35.0, -119.0), 5, 5);
        FlatField spatialField = spatialDomain.createSimpleSpatialField(FIRE_WEATHER);
        FieldImpl timeSpaceField = timeDomain.createTemporalField(spatialField.getType());
        final int numLatLons = spatialDomain.getSpatialDomainSetLength();
        final int numTimes = timeDomain.getTemporalDomainSetLength();
        for (int t = 0; t < numTimes; t++) {
            double[][] rangeSamples = new double[FIRE_WEATHER.getDimension()][numLatLons];
            for (int xy = 0; xy < numLatLons; xy++) {
                rangeSamples[0][xy] = 1. + xy * (t + 1);    // air temp
                rangeSamples[1][xy] = 2. + xy * (t + 1);    // rh
                rangeSamples[2][xy] = 3. + xy * (t + 1);    // wnd spd
                rangeSamples[3][xy] = 4. + xy * (t + 1);    // wnd dir
                rangeSamples[4][xy] = 5. + xy * (t + 1);    // cloud cover
            }
            spatialField.setSamples(rangeSamples);
            timeSpaceField.setSample(t, spatialField);
        }
        WeatherModel result = new WeatherModel(timeSpaceField);
        assertNotNull(result);
        //System.out.println(result);
    }

    @Test
    public void testFrom_TemporalDomain_SpatialFieldArr() {
        System.out.println("from(TemporalDomain,SpatialFieldArr)");
        TemporalDomain timeDomain = new TemporalDomain(ZonedDateTime.now(), 12);
        SpatialDomain spatialDomain = new SpatialDomain(
                GeoCoord2D.fromDegrees(34.0, -120.0),
                GeoCoord2D.fromDegrees(35.0, -119.0), 5, 5);
        final int numTimes = timeDomain.getTemporalDomainSetLength();
        final int numLatLons = spatialDomain.getSpatialDomainSetLength();

        SpatialField[] fields = new SpatialField[numTimes];
        for (int t = 0; t < numTimes; t++) {
            double[][] rangeSamples = new double[FIRE_WEATHER.getDimension()][numLatLons];
            for (int xy = 0; xy < numLatLons; xy++) {
                rangeSamples[0][xy] = 1. + xy * (t + 1);    // air temp
                rangeSamples[1][xy] = 2. + xy * (t + 1);    // rh
                rangeSamples[2][xy] = 3. + xy * (t + 1);    // wnd spd
                rangeSamples[3][xy] = 4. + xy * (t + 1);    // wnd dir
                rangeSamples[4][xy] = 5. + xy * (t + 1);    // cloud cover
            }
            fields[t] = SpatialField.from(spatialDomain, FIRE_WEATHER, rangeSamples);
        }
        WeatherModel result = WeatherModel.from(timeDomain, fields);
    }

    @Test
    public void testFrom_3args() throws VisADException, RemoteException {
        System.out.println("from(time,coord,tuple)");
        ZonedDateTime time = ZonedDateTime.now();
        Coord2D coord = GeoCoord2D.fromDegrees(34.25, -119.2);
        WeatherTuple wxTuple = WeatherTuple.fromRealTuple(new RealTuple(FIRE_WEATHER, new double[]{29, 10, 5, 235, 10}));

        WeatherModel result = WeatherModel.from(time, coord, wxTuple);
        assertNotNull(result);
        //System.out.println(result);
    }

    @Test
    public void testGetField() throws VisADException, RemoteException {
        System.out.println("getField");
        ZonedDateTime time = ZonedDateTime.now();
        Coord2D coord = GeoCoord2D.fromDegrees(34.25, -119.2);
        WeatherTuple wxTuple = WeatherTuple.fromRealTuple(new RealTuple(FIRE_WEATHER, new double[]{29, 10, 5, 235, 10}));
        WeatherModel instance = WeatherModel.from(time, coord, wxTuple);
        FieldImpl result = instance.getField();
        assertNotNull(result);
        //System.out.println(result);
    }

    @Test
    public void testGetWeather() {
        System.out.println("getWeather");
        System.out.println(instance);
        ZonedDateTime time = ZonedDateTime.now();

        // Outside temporal domain
        WeatherTuple result = instance.getWeather(time.minusHours(1), GeoCoord2D.fromDegrees(34.0, -120.0));
        assertNotNull("result not null", result);
        assertTrue("result isMissing", result.isMissing());
        
        // Outside temporal domain
        result = instance.getWeather(time.plusHours(12), GeoCoord2D.fromDegrees(34.0, -120.0));
        assertNotNull("result not null", result);
        assertTrue("result isMissing", result.isMissing());
        
        // Outside spatial domain, but close to the edge
        result = instance.getWeather(time, GeoCoord2D.fromDegrees(33.9, -120.1));
        assertNotNull("result not null", result);
        assertTrue("result not isMissing", !result.isMissing());
        assertEquals("air temp", 1.0, result.getAirTemperature().getValue(), .0001);

        // On a corner
        result = instance.getWeather(time, GeoCoord2D.fromDegrees(34.0, -120.0));
        assertNotNull("result not null", result);
        assertTrue("result not isMissing", !result.isMissing());
        assertEquals("air temp", 1.0, result.getAirTemperature().getValue(), .0001);
        
        // On opposite corner
        result = instance.getWeather(time, GeoCoord2D.fromDegrees(35.0, -119.0));
        assertEquals("air temp", 25.0, result.getAirTemperature().getValue(), .0001);

        // Centered
        result = instance.getWeather(time, GeoCoord2D.fromDegrees(34.5, -119.5));
        assertEquals("air temp", 13, result.getAirTemperature().getValue(), .0001);

        // On SW corner, 1 hour later
        result = instance.getWeather(time.plusHours(1), GeoCoord2D.fromDegrees(34.0, -120.0));
        assertEquals("air temp", 2.0, result.getAirTemperature().getValue(), .0001);

        // On SW corner, 1/2 hour later
        result = instance.getWeather(time.plusMinutes(30), GeoCoord2D.fromDegrees(34.0, -120.));
        assertEquals("air temp", 1.5, result.getAirTemperature().getValue(), .0001);
        
        // At center, 90 minutes later
        result = instance.getWeather(time.plusMinutes(90), GeoCoord2D.fromDegrees(34.5, -119.5));
        assertEquals("air temp", 14.5, result.getAirTemperature().getValue(), .0001);
        
        // Between temporal samples and spatial samples.
        result = instance.getWeather(time.plusMinutes(90), GeoCoord2D.fromDegrees(34.625, -119.5));
        assertEquals("air temp", 15., result.getAirTemperature().getValue(), .0001);        
        System.out.println(result);
    }

    @Ignore
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        WeatherModel instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Ignore
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        WeatherModel instance = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Ignore
    @Test
    public void testToString() {
        System.out.println("toString");
        WeatherModel instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
