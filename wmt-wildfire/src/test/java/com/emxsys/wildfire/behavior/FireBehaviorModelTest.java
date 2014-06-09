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

package com.emxsys.wildfire.behavior;

import com.emxsys.gis.api.Terrain;
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.FireEnvironment;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import com.emxsys.wildfire.api.StdFuelModel;
import static com.emxsys.wildfire.api.StdFuelModelParams13.FBFM04;
import static com.emxsys.wildfire.api.WeatherConditions.HOT_AND_DRY;
import java.rmi.RemoteException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class FireBehaviorModelTest {
    
    public FireBehaviorModelTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testComputeFireBehavior() throws VisADException, RemoteException {
        System.out.println("computeFireBehavior");
        FireBehaviorModel instance = new FireBehaviorModel();
        FuelModel fuelModel = new StdFuelModel.Builder(FBFM04).build();
        FuelMoisture fuelMoisture = FuelMoistureTuple.fromWeatherConditions(HOT_AND_DRY);
        
        FuelBed fuelCharacter = Rothermel.getFuelCharacter(fuelModel, fuelMoisture);
        assertNotNull(fuelCharacter);
        assertTrue(!fuelCharacter.isMissing());
        System.out.println(fuelCharacter.longString());
        
        RealTuple fuelCombustion = Rothermel.getFuelCombustible(fuelCharacter);
        assertNotNull(fuelCombustion);
        assertTrue(!fuelCombustion.isMissing());
        System.out.println(fuelCombustion.longString());
        
        
//        Weather weather = null;
//        Terrain terrain = null;
//        FireEnvironment expResult = null;
//        FireEnvironment result = instance.computeFireBehavior(fuelModel, fuelMoisture, weather, terrain);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
}
