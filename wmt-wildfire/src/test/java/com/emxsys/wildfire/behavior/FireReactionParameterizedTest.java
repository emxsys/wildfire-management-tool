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
import com.emxsys.util.MathUtil;
import com.emxsys.visad.FireUnit;
import static com.emxsys.weather.api.WeatherType.WIND_DIR;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_MPH;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wildfire.api.StdFuelModelParams13;
import com.emxsys.wildfire.api.StdFuelModelParams40;
import static com.emxsys.wildfire.api.WeatherConditions.HOT_AND_DRY;
import static com.emxsys.wildfire.api.WildfireType.ASPECT;
import static com.emxsys.wildfire.api.WildfireType.SLOPE;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import org.junit.BeforeClass;
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
public class FireReactionParameterizedTest {

    private final FireReaction instance;
    private final Fuelbed fuelbed;
    private final String fuelModelCode;

    static HashMap<String, double[]> expResults;
    // HashMap Results indicies. See SetupClass()
    static final int ROS = 0;
    static final int SPD_DIR = 1;
    static final int EFF_WND_SPD = 2;
    static final int FLI = 3;
    static final int FL = 4;

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
     *
     * Fuel Moisture 1-h Moisture % 6 10-h Moisture % 7 100-h Moisture % 8 Live Herbaceous Moisture
     * % 70 Live Woody Moisture % 70 Weather Midflame Wind Speed mi/h 5 Direction of Wind Vector
     * (from upslope) deg 35 Terrain Slope Steepness % 30
     *
     * @param model
     * @param moisture
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public FireReactionParameterizedTest(FuelModel model, FuelMoisture moisture) {
        fuelbed = Fuelbed.from(model, moisture);
        fuelModelCode = model.getModelCode();
        Terrain terrain = new TerrainTuple(180, 16.7, 0);
        instance = new FireReaction(fuelbed,
                new Real(WIND_SPEED_MPH, 5),
                new Real(WIND_DIR, 215),
                terrain.getAspect(),
                terrain.getSlope());
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        expResults = new HashMap<>();
        try (InputStream stream = ClassLoader.getSystemResourceAsStream("BehaveUnitTest.csv")) {
            CsvReader reader = new CsvReader(stream, ',', Charset.defaultCharset());
            reader.readHeaders();

            while (reader.readRecord()) {
                expResults.put(reader.get("FM").toUpperCase(),
                        new double[]{Double.parseDouble(reader.get("ROS")),
                                     Double.parseDouble(reader.get("SPD_DIR")),
                                     Double.parseDouble(reader.get("EFF_WND_SPD")),
                                     Double.parseDouble(reader.get("FLI")),
                                     Double.parseDouble(reader.get("FL")),});
            }
            System.out.println("");
            reader.toString();
            reader.close();
        }
    }

    @Test
    public void testGetRateOfSpreadMax() throws VisADException {
        System.out.println("getRateOfSpreadMax : " + fuelModelCode);
        Real result = instance.getRateOfSpreadMax();
        double[] expected = expResults.get(fuelModelCode);
        double expResult = result.getValue(FireUnit.chain_hour);
        expResult = Math.round(expResult * 10) / 10.;
        double tolerance = 0.25;
        if (fuelModelCode.equalsIgnoreCase("SH9")) {
            assumeTrue(MathUtil.nearlyEquals(expected[ROS], expResult, tolerance));
        } else {
            assertEquals(fuelModelCode + ": ros [ch/hr]", expected[ROS], expResult, tolerance);
        }
    }

    @Test
    public void testGetRateOfSpreadBacking() throws VisADException {
        System.out.println("getRateOfSpreadBacking : " + fuelModelCode);
        Real resultMax = instance.getRateOfSpreadMax();
        Real result = instance.getRateOfSpreadBacking();
        assertNotNull(result);
        if (fuelbed.isBurnable()) {
            assertTrue(result.getValue() < resultMax.getValue());
        }
    }

    @Test
    public void testGetRateOfSpreadAzimuth() throws VisADException, RemoteException {
        System.out.println("getRateOfSpreadBacking : " + fuelModelCode);
        Real resultMax = instance.getRateOfSpreadMax();
        Real resultDir = instance.getDirectionMaxSpread();
        if (fuelbed.isBurnable()) {
//            System.out.print(resultDir.longString());
//            System.out.println(resultMax.longString());
            for (int i = 0; i < 360; i++) {
                Real azimuth = new Real(i);
                Real result = instance.getRateOfSpreadAtAzimuth(azimuth);
                assertNotNull(result);
                assertTrue(resultMax.getValue() > result.getValue());
//                if (i % 15 == 0) {
//                    System.out.print(azimuth.longString());
//                    System.out.println(result.longString());
//                }
            }
        }
    }

    @Test
    public void testGetDirectionMaxSpread() throws VisADException {
        System.out.println("getDirectionMaxSpread : " + fuelModelCode);
        Real result = instance.getDirectionMaxSpread();
        double[] expected = expResults.get(fuelModelCode);
        double expResult = result.getValue();
        double tolerance = 0.5; // degree
        assertEquals(fuelModelCode + ": spread dir [degrees]", expected[SPD_DIR], expResult, tolerance);
    }

    @Test
    public void testGetEffectiveWindSpeed() throws VisADException {
        System.out.println("getEffectiveWindSpeed : " + fuelModelCode);
        Real result = instance.getEffectiveWindSpeed();
        double[] expected = expResults.get(fuelModelCode);
        double expResult = result.getValue();
        double tolerance = 0.5; // mph
        assertEquals(fuelModelCode + ": eff wind speed [mph]", expected[EFF_WND_SPD], expResult, tolerance);
    }

    @Test
    public void testGetFirelineIntensity() throws VisADException {
        System.out.println("getFirelineIntensity : " + fuelModelCode);
        Real result = instance.getFirelineIntensity();
        double[] expected = expResults.get(fuelModelCode);
        double expResult = Math.round(result.getValue());
        double tolerance = expected[FLI] * 0.025; // 2.5%
        if (fuelModelCode.equalsIgnoreCase("SH9")) {
            assumeTrue(MathUtil.nearlyEquals(expected[FLI], expResult, tolerance));
        } else {
            assertEquals(fuelModelCode + ": fli [Btu/ft/s]", expected[FLI], expResult, tolerance);
        }
    }

    @Test
    public void testGetFlameLength() throws VisADException {
        System.out.println("getFlameLength : " + fuelModelCode);
        Real result = instance.getFlameLength();
        double[] expected = expResults.get(fuelModelCode);
        double expResult = result.getValue();
        expResult = Math.round(expResult * 10) / 10.; // round to 1/10.
        double tolerance = 0.25;    // 3"
        if (fuelModelCode.equalsIgnoreCase("SH9")) {
            assumeTrue(MathUtil.nearlyEquals(expected[FL], expResult, tolerance));
        } else {
            assertEquals(fuelModelCode + ": fl [ft]", expected[FL], expResult, tolerance);
        }
    }

}
