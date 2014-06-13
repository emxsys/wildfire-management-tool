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

import com.emxsys.gis.api.TerrainTuple;
import com.emxsys.weather.api.WeatherTuple;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wildfire.api.StdFuelModelParams13;
import com.emxsys.wildfire.api.StdFuelModelParams40;
import com.emxsys.wildfire.api.WeatherConditions;
import java.rmi.RemoteException;
import java.util.ArrayList;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import visad.Real;
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
        ArrayList<FuelModel> models = new ArrayList<>();
        ArrayList<FuelMoisture> moistures = new ArrayList<>();

        // Load the original 13 fuel models
        for (StdFuelModelParams13 fbfm13 : StdFuelModelParams13.values()) {
            models.add(new StdFuelModel.Builder(fbfm13).build());
        }
        // Load the standard 40 fuel models
        for (StdFuelModelParams40 fbfm40 : StdFuelModelParams40.values()) {
            models.add(new StdFuelModel.Builder(fbfm40).build());
        }
        // Common moisture scenario used in test
        for (WeatherConditions conditions : WeatherConditions.values()) {
            moistures.add(FuelMoistureTuple.fromWeatherConditions(conditions));
        }

        WeatherTuple weather = WeatherTuple.fromReals(
                new Real(WeatherType.AIR_TEMP_F, 70),
                new Real(WeatherType.REL_HUMIDITY, 20),
                new Real(WeatherType.WIND_SPEED_MPH, 5),
                new Real(WeatherType.WIND_DIR, 215),
                new Real(WeatherType.CLOUD_COVER, 0));
        TerrainTuple terrain = new TerrainTuple(180, 16.7, 0);

        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();            
            for (FuelModel fuelModel : models) {
                for (FuelMoisture fuelMoisture : moistures) {
                    FireReaction result = instance.computeFireBehavior(
                            fuelModel, fuelMoisture, weather, terrain);
                    assertNotNull(result);
                    result.getRateOfSpread();   // force the calculations
                    //System.out.println(fuelModel.getModelCode() + ": " + result);
                }
            }
            long end = System.currentTimeMillis();          
            System.out.printf(">>> Pass %d completed in %dms%n", i+1, end - start);
        }
    }
}
