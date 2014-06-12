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

import com.csvreader.CsvReader;
import com.emxsys.gis.api.Terrain;
import com.emxsys.gis.api.TerrainTuple;
import com.emxsys.visad.FireUnit;
import com.emxsys.weather.api.Weather;
import static com.emxsys.weather.api.WeatherType.WIND_DIR;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_MPH;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wildfire.api.StdFuelModelParams13;
import com.emxsys.wildfire.api.StdFuelModelParams40;
import static com.emxsys.wildfire.api.WeatherConditions.HOT_AND_DRY;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import visad.Real;
import visad.VisADException;

/**
 * Runs FireReaction unit tests for all FuelModels.
 *
 * @author Bruce Schubert
 */
@RunWith(Parameterized.class)
public class FireReactionTest {

    private final FireReaction instance;
    private final FuelBed fuelbed;
    private final String fuelModelCode;

    static HashMap<String, double[]> expResults;
    // HashMap Results indicies. See SetupClass()
    static final int ROS = 0;

    /*
     * Test data generator.
    
     * This method is called the the JUnit parameterized test runner and
     * returns a Collection of Arrays.  For each Array in the Collection,
     * each array element corresponds to a parameter in the constructor.
     */
    @Parameters
    public static Collection<Object[]> generateParams() {
        // Constructor parameters
        List<Object[]> params = new ArrayList<>();

        // Common moisture scenario used in test
        FuelMoisture moisture = FuelMoistureTuple.fromWeatherConditions(HOT_AND_DRY);

        // Load the original 13 fuel models
        for (StdFuelModelParams13 fbfm13 : StdFuelModelParams13.values()) {
            FuelModel model = new StdFuelModel.Builder(fbfm13).build();
            params.add(new Object[]{model, moisture});
        }
        // Load the standard 40 fuel models
        for (StdFuelModelParams40 fbfm40 : StdFuelModelParams40.values()) {
            FuelModel model = new StdFuelModel.Builder(fbfm40).build();
            params.add(new Object[]{model, moisture});
        }
        return params;
    }

    /**
     * Parameterized Constructor. The JUnit test runner will instantiate this class once for every
     * element in the Collection returned by the method annotated with {@code @Parameters}. See
     * generateParams().
     * @param model
     * @param moisture
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public FireReactionTest(FuelModel model, FuelMoisture moisture) {
        fuelbed = FuelBed.from(model, moisture);
        fuelModelCode = model.getModelCode();
        Terrain terrain = new TerrainTuple(0, 17.458, 0);
        instance = new FireReaction(fuelbed, terrain, new Real(WIND_SPEED_MPH, 5), new Real(WIND_DIR, 225));
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        expResults = new HashMap<>();
        try (InputStream stream = ClassLoader.getSystemResourceAsStream("BehaveUnitTest.csv")) {
            CsvReader reader = new CsvReader(stream, ',', Charset.defaultCharset());
            reader.readHeaders();

            while (reader.readRecord()) {
                expResults.put(reader.get("FM").toUpperCase(),
                        new double[]{
                            Double.parseDouble(reader.get("ROS")),});
            }
            System.out.println("");
            reader.toString();
            reader.close();
        }
    }

    @Ignore
    @Test
    public void testFrom() {
        System.out.println("from");
        FuelBed fuelBed = null;
        Terrain terrain = null;
        Weather weather = null;
        FireReaction expResult = null;
        FireReaction result = FireReaction.from(fuelBed, terrain, weather);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Ignore
    @Test
    public void testGetRateOfSpreadNoWindNoSlope() {
        System.out.println("getRateOfSpreadNoWindNoSlope");
        FireReaction instance = null;
        Real expResult = null;
        Real result = instance.getRateOfSpreadNoWindNoSlope();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetRateOfSpread() throws VisADException {
        System.out.println("getRateOfSpread : " + fuelModelCode);
        Real result = instance.getRateOfSpread();
        double[] expected = expResults.get(fuelModelCode);
        assertEquals(fuelModelCode + ": ros [ch/hr]", expected[ROS], result.getValue(FireUnit.chain_hour), expected[ROS] * 0.01);
    }

}
