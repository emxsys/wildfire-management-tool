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

import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_100H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_10H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_1H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_HERB;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_WOODY;
import static com.emxsys.wildfire.api.WildfireType.FUEL_TEMP_C;
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
public class FuelConditionTupleTest {

    static BasicFuelCondition instance;
    static BasicFuelMoisture FM;
    static Real FM1 = new Real(FUEL_MOISTURE_1H, 1);
    static Real FM10 = new Real(FUEL_MOISTURE_10H, 2);
    static Real FM100 = new Real(FUEL_MOISTURE_100H, 3);
    static Real FMHERB = new Real(FUEL_MOISTURE_HERB, 4);
    static Real FMWOODY = new Real(FUEL_MOISTURE_WOODY, 5);
    static Real FUELTEMP = new Real(FUEL_TEMP_C, 10);

    public FuelConditionTupleTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        FM = BasicFuelMoisture.fromReals(FM1, FM10, FM100, FMHERB, FMWOODY);
        instance = BasicFuelCondition.fromReals(FM, FUELTEMP);
    }

    @Before
    public void setUp() {
    }

    /**
     * Test of fromRealTuple method, of class BasicFuelCondition.
     */
    @Test
    public void testFromRealTuple() throws VisADException, RemoteException {
        System.out.println("fromRealTuple");
        RealTuple fuelCondition = new RealTuple(new Real[]{
            FM1, FM10, FM100, FMHERB, FMWOODY, FUELTEMP,
        });
        BasicFuelCondition result = BasicFuelCondition.fromRealTuple(fuelCondition);
        assertEquals(instance, result);
    }

    /**
     * Test of fromReals method, of class BasicFuelCondition.
     */
    @Test
    public void testFromReals() {
        System.out.println("fromReals");
        BasicFuelCondition result = BasicFuelCondition.fromReals(FM, FUELTEMP);
        assertEquals(instance, result);
    }

    /**
     * Test of getFuelMoisture method, of class BasicFuelCondition.
     */
    @Test
    public void testGetFuelMoisture() {
        System.out.println("getFuelMoisture");
        BasicFuelMoisture result = instance.getFuelMoisture();
        assertEquals(FM, result);
    }

    /**
     * Test of getDead1HrFuelMoisture method, of class BasicFuelCondition.
     */
    @Test
    public void testGetDead1HrFuelMoisture() {
        System.out.println("getDead1HrFuelMoisture");
        Real result = instance.getDead1HrFuelMoisture();
        assertEquals(FM1, result);
    }

    /**
     * Test of getDead10HrFuelMoisture method, of class BasicFuelCondition.
     */
    @Test
    public void testGetDead10HrFuelMoisture() {
        System.out.println("getDead10HrFuelMoisture");
        Real result = instance.getDead10HrFuelMoisture();
        assertEquals(FM10, result);
    }

    /**
     * Test of getDead100HrFuelMoisture method, of class BasicFuelCondition.
     */
    @Test
    public void testGetDead100HrFuelMoisture() {
        System.out.println("getDead100HrFuelMoisture");
        Real result = instance.getDead100HrFuelMoisture();
        assertEquals(FM100, result);
    }

    /**
     * Test of getLiveHerbFuelMoisture method, of class BasicFuelCondition.
     */
    @Test
    public void testGetLiveHerbFuelMoisture() {
        System.out.println("getLiveHerbFuelMoisture");
        Real result = instance.getLiveHerbFuelMoisture();
        assertEquals(FMHERB, result);
    }

    /**
     * Test of getLiveWoodyFuelMoisture method, of class BasicFuelCondition.
     */
    @Test
    public void testGetLiveWoodyFuelMoisture() {
        System.out.println("getLiveWoodyFuelMoisture");
        Real result = instance.getLiveWoodyFuelMoisture();
        assertEquals(FMWOODY, result);
    }

    /**
     * Test of getFuelTemperature method, of class BasicFuelCondition.
     */
    @Test
    public void testGetFuelTemperature() {
        System.out.println("getFuelTemperature");
        Real result = instance.getFuelTemperature();
        assertEquals(FUELTEMP, result);
    }


}
