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
package com.emxsys.wildfire.api;

import static com.emxsys.wildfire.api.WeatherConditions.HOT_AND_DRY;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_100H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_10H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_1H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_HERB;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_WOODY;
import java.rmi.RemoteException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class FuelMoistureTupleTest {

    static FuelMoistureTuple instance;
    static Real FM1 = new Real(FUEL_MOISTURE_1H, 1);
    static Real FM10 = new Real(FUEL_MOISTURE_10H, 2);
    static Real FM100 = new Real(FUEL_MOISTURE_100H, 3);
    static Real FMHERB = new Real(FUEL_MOISTURE_HERB, 4);
    static Real FMWOODY = new Real(FUEL_MOISTURE_WOODY, 5);

    public FuelMoistureTupleTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        instance = FuelMoistureTuple.fromReals(FM1, FM10, FM100, FMHERB, FMWOODY);
    }

    @Before
    public void setUp() {
    }

    /**
     * Test of fromReals method, of class FuelMoistureTuple.
     */
    @Test
    public void testFromReals() {
        System.out.println("fromReals");
        FuelMoistureTuple result = FuelMoistureTuple.fromReals(FM1, FM10, FM100, FMHERB, FMWOODY);
        assertEquals(instance, result);
    }

    /**
     * Test of fromRealTuple method, of class FuelMoistureTuple.
     */
    @Test
    public void testFromRealTuple() throws VisADException, RemoteException {
        System.out.println("fromRealTuple");
        RealTuple realTuple = new RealTuple(new Real[]{FM1, FM10, FM100, FMHERB, FMWOODY});
        FuelMoistureTuple result = FuelMoistureTuple.fromRealTuple(realTuple);
        assertEquals(instance, result);
    }

    /**
     * Test of fromDoubles method, of class FuelMoistureTuple.
     */
    @Test
    public void testFromDoubles() {
        System.out.println("fromDoubles");
        double dead1HrFuelMoisture = 1.0;
        double dead10HrFuelMoisture = 2.0;
        double dead100HrFuelMoisture = 3.0;
        double liveHerbFuelMoisture = 4.0;
        double liveWoodyFuelMoisture = 5.0;
        FuelMoistureTuple result = FuelMoistureTuple.fromDoubles(dead1HrFuelMoisture, dead10HrFuelMoisture, dead100HrFuelMoisture, liveHerbFuelMoisture, liveWoodyFuelMoisture);
        assertEquals(instance, result);
    }

    /**
     * Test of fromWeatherConditions method, of class FuelMoistureTuple.
     */
    @Test
    public void testFromWeatherConditions() {
        System.out.println("fromWeatherConditions");
        WeatherConditions previousWeeksWx = HOT_AND_DRY;
        FuelMoistureTuple expResult = FuelMoistureTuple.fromDoubles(6, 7, 8, 70, 70);
        FuelMoistureTuple result = FuelMoistureTuple.fromWeatherConditions(previousWeeksWx);
        assertEquals(expResult, result);
    }

    /**
     * Test of getDead1HrFuelMoisture method, of class FuelMoistureTuple.
     */
    @Test
    public void testGetDead1HrFuelMoisture() {
        System.out.println("getDead1HrFuelMoisture");
        Real result = instance.getDead1HrFuelMoisture();
        assertEquals(FM1, result);
    }

    /**
     * Test of getDead10HrFuelMoisture method, of class FuelMoistureTuple.
     */
    @Test
    public void testGetDead10HrFuelMoisture() {
        System.out.println("getDead10HrFuelMoisture");
        Real result = instance.getDead10HrFuelMoisture();
        assertEquals(FM10, result);
    }

    /**
     * Test of getDead100HrFuelMoisture method, of class FuelMoistureTuple.
     */
    @Test
    public void testGetDead100HrFuelMoisture() {
        System.out.println("getDead100HrFuelMoisture");
        Real result = instance.getDead100HrFuelMoisture();
        assertEquals(FM100, result);
    }

    /**
     * Test of getLiveHerbFuelMoisture method, of class FuelMoistureTuple.
     */
    @Test
    public void testGetLiveHerbFuelMoisture() {
        System.out.println("getLiveHerbFuelMoisture");
        Real result = instance.getLiveHerbFuelMoisture();
        assertEquals(FMHERB, result);
    }

    /**
     * Test of getLiveWoodyFuelMoisture method, of class FuelMoistureTuple.
     */
    @Test
    public void testGetLiveWoodyFuelMoisture() {
        System.out.println("getLiveWoodyFuelMoisture");
        Real result = instance.getLiveWoodyFuelMoisture();
        assertEquals(FMWOODY, result);
    }

}
