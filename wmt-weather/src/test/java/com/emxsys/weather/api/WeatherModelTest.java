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
import com.emxsys.gis.api.Coords;
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
import visad.FunctionType;
import visad.Irregular2DSet;
import visad.RealTuple;
import visad.RealTupleType;
import static visad.RealTupleType.LatitudeLongitudeTuple;
import visad.Tuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class WeatherModelTest {

    private WeatherModel spatialInstance;
    private WeatherModel irregularInstance;
    private WeatherModel temporalInstance;

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
        final int numTimes = timeDomain.getDomainSetLength();
        final int numLatLons = spatialDomain.getDomainSetLength();

        // Create a spatialField for each time
        SpatialField[] spatialFields = new SpatialField[numTimes];
        for (int t = 0; t < numTimes; t++) {
            double[][] rangeSamples = new double[FIRE_WEATHER.getDimension()][numLatLons];
            for (int xy = 0; xy < numLatLons; xy++) {
                rangeSamples[0][xy] = 1. + xy + t;    // air temp
                rangeSamples[1][xy] = 2. + xy + t;    // rh
                rangeSamples[2][xy] = 3. + xy + t;    // wnd spd
                rangeSamples[3][xy] = 4. + xy + t;    // wnd dir
                rangeSamples[4][xy] = 5. + xy + t;    // cloud cover
            }
            spatialFields[t] = SpatialField.from(spatialDomain, FIRE_WEATHER, rangeSamples);
        }
        temporalInstance = WeatherModel.from(timeDomain, spatialFields);

        // Create a temporalField for each lat/lon
        FlatField[] temporalFields = new FlatField[numLatLons];
        for (int xy = 0; xy < numLatLons; xy++) {
            double[][] rangeSamples = new double[FIRE_WEATHER.getDimension()][numTimes];
            for (int t = 0; t < numTimes; t++) {
                rangeSamples[0][t] = 1. + xy + t;    // air temp
                rangeSamples[1][t] = 2. + xy + t;    // rh
                rangeSamples[2][t] = 3. + xy + t;    // wnd spd
                rangeSamples[3][t] = 4. + xy + t;    // wnd dir
                rangeSamples[4][t] = 5. + xy + t;    // cloud cover
            }
            FlatField field = timeDomain.createSimpleTemporalField(FIRE_WEATHER);
            field.setSamples(rangeSamples);
            temporalFields[xy] = field;
        }
        spatialInstance = WeatherModel.from(spatialDomain, temporalFields);

        FieldImpl spatialField = new FieldImpl(
                new FunctionType(LatitudeLongitudeTuple, new FunctionType(RealTupleType.Time1DTuple, FIRE_WEATHER)),
                new Irregular2DSet(LatitudeLongitudeTuple, spatialDomain.getDomainSet().getSamples(true)));
        spatialField.setSamples(temporalFields, true);
        irregularInstance = new WeatherModel(spatialField);
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
        final int numLatLons = spatialDomain.getDomainSetLength();
        final int numTimes = timeDomain.getDomainSetLength();
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
        final int numTimes = timeDomain.getDomainSetLength();
        final int numLatLons = spatialDomain.getDomainSetLength();

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
    public void testGetLatestWeather() throws VisADException, RemoteException {
        System.out.println("getLatestWeather");

        FlatField temporalResult = temporalInstance.getLatestWeather();
        assertNotNull(temporalResult);

        //System.out.println(temporalResult);
        RealTuple tuple = (RealTuple) temporalResult.getSample(temporalResult.getLength() - 1);
        assertEquals(40, tuple.getValues()[4], .01);
        tuple = (RealTuple) temporalResult.getSample(0);
        assertEquals(12, tuple.getValues()[0], .01);

        FlatField spatialResult = spatialInstance.getLatestWeather();
        assertNotNull(spatialResult);

        //System.out.println(spatialResult);
        tuple = (RealTuple) temporalResult.getSample(temporalResult.getLength() - 1);
        assertEquals(40, tuple.getValues()[4], .01);
        tuple = (RealTuple) temporalResult.getSample(0);
        assertEquals(12, tuple.getValues()[0], .01);
    }

    @Test
    public void testGetSpatialWeatherAt() throws VisADException, RemoteException {
        System.out.println("getSpatialWeatherAt");
        //System.out.println(irregularInstance);

        ZonedDateTime time = ZonedDateTime.now();
        Coord2D coord = GeoCoord2D.fromDegrees(34.5, -119.5);

        // Outside temporal domain
        FlatField result = irregularInstance.getSpatialWeatherAt(time.plusHours(1));
        assertNotNull("result not null", result);
        assertTrue("result !isMissing", !result.isMissing());
        System.out.println("getSpatialWeatherAt( " + time + " ):");
        System.out.println(result);

        RealTuple tuple = (RealTuple) result.evaluate(Coords.toLatLonTuple(coord));
        System.out.println("getSpatialWeatherAt( " + time + " ).evaluate( " + coord + " ): ");
        System.out.println(tuple);
    }

    @Test
    public void testGetWeather() {
        System.out.println("getWeather");
        //System.out.println(temporalInstance);
        ZonedDateTime time = ZonedDateTime.now();

        // Outside temporal domain
        WeatherTuple result = temporalInstance.getWeather(time.minusHours(1), GeoCoord2D.fromDegrees(34.0, -120.0));
        assertNotNull("result not null", result);
        assertTrue("result isMissing", result.isMissing());

        // Outside temporal domain
        result = temporalInstance.getWeather(time.plusHours(12), GeoCoord2D.fromDegrees(34.0, -120.0));
        assertNotNull("result not null", result);
        assertTrue("result isMissing", result.isMissing());

        // Outside spatial domain, but close to the edge
        result = temporalInstance.getWeather(time, GeoCoord2D.fromDegrees(33.9, -120.1));
        assertNotNull("result not null", result);
        assertTrue("result not isMissing", !result.isMissing());
        assertEquals("air temp", 1.0, result.getAirTemperature().getValue(), .0001);

        // On a corner
        result = temporalInstance.getWeather(time, GeoCoord2D.fromDegrees(34.0, -120.0));
        assertNotNull("result not null", result);
        assertTrue("result not isMissing", !result.isMissing());
        assertEquals("air temp", 1.0, result.getAirTemperature().getValue(), .0001);

        // On opposite corner
        result = temporalInstance.getWeather(time, GeoCoord2D.fromDegrees(35.0, -119.0));
        assertEquals("air temp", 25.0, result.getAirTemperature().getValue(), .0001);

        // Centered
        result = temporalInstance.getWeather(time, GeoCoord2D.fromDegrees(34.5, -119.5));
        assertEquals("air temp", 13, result.getAirTemperature().getValue(), .0001);

        // On SW corner, 1 hour later
        result = temporalInstance.getWeather(time.plusHours(1), GeoCoord2D.fromDegrees(34.0, -120.0));
        assertEquals("air temp", 2.0, result.getAirTemperature().getValue(), .0001);

        // On SW corner, 1/2 hour later
        result = temporalInstance.getWeather(time.plusMinutes(30), GeoCoord2D.fromDegrees(34.0, -120.));
        assertEquals("air temp", 1.5, result.getAirTemperature().getValue(), .0001);

        // At center, 90 minutes later
        result = temporalInstance.getWeather(time.plusMinutes(90), GeoCoord2D.fromDegrees(34.5, -119.5));
        assertEquals("air temp", 14.5, result.getAirTemperature().getValue(), .0001);

        // Between temporal samples and spatial samples.
        result = temporalInstance.getWeather(time.plusMinutes(90), GeoCoord2D.fromDegrees(34.625, -119.5));
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

    @Test
    public void testToString() {
        System.out.println("toString");
        WeatherModel instance = irregularInstance;
        System.out.println(instance);
    }

}
