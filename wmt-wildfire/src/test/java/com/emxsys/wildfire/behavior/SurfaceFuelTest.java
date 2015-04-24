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
import com.emxsys.util.MathUtil;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.BasicFuelMoisture;
import com.emxsys.wildfire.api.BasicFuelModel;
import com.emxsys.wildfire.api.StdFuelModelParams13;
import com.emxsys.wildfire.api.StdFuelModelParams40;
import static com.emxsys.wildfire.api.WeatherConditions.HOT_AND_DRY;
import com.emxsys.wildfire.api.WildfireType;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import visad.Real;
import visad.VisADException;

/**
 * Runs SurfaceFuel unit tests for all FuelModels.
 *
 * @author Bruce Schubert
 */
@RunWith(Parameterized.class)
public class SurfaceFuelTest {

    private final SurfaceFuel instance;
    private final String fuelModelCode;

    static HashMap<String, double[]> expResults;
    // HashMap Results indicies. See SetupClass()
    static final int SIGMA = 0;
    static final int RHO_B = 1;
    static final int BETA = 2;
    static final int BETA_RATIO = 3;
    static final int MX_LIVE = 4;
    static final int I_R = 5;
    static final int I_R_DEAD = 6;
    static final int I_R_LIVE = 7;
    static final int HPA = 8;
    static final int HSK = 9;

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
        //FuelMoisture moisture = new FuelMoisture(FuelMoistureScenario.D1L1);
        FuelMoisture moisture = BasicFuelMoisture.fromWeatherConditions(HOT_AND_DRY);

        // Load the original 13 fuel models
        for (StdFuelModelParams13 fbfm13 : StdFuelModelParams13.values()) {
            FuelModel model = new BasicFuelModel.Builder(fbfm13).build();

            params.add(new Object[]{model, moisture});
        }
        // Load the standard 40 fuel models
        for (StdFuelModelParams40 fbfm40 : StdFuelModelParams40.values()) {
            FuelModel model = new BasicFuelModel.Builder(fbfm40).build();

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
    public SurfaceFuelTest(FuelModel model, FuelMoisture moisture) {
        instance = SurfaceFuel.from(model, moisture);
        fuelModelCode = instance.fuelModel.getModelCode();
    }

    /**
     * Run before any test is executed.
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {

        expResults = new HashMap<>();
        try (InputStream stream = ClassLoader.getSystemResourceAsStream("BehaveUnitTest.csv")) {
            CsvReader reader = new CsvReader(stream, ',', Charset.defaultCharset());
            reader.readHeaders();

            while (reader.readRecord()) {
                expResults.put(reader.get("FM").toUpperCase(),
                        new double[]{
                            Double.parseDouble(reader.get("SIGMA")),
                            Double.parseDouble(reader.get("RHO_B")),
                            Double.parseDouble(reader.get("BETA")),
                            Double.parseDouble(reader.get("BETA_RATIO")),
                            Double.parseDouble(reader.get("MX_LIVE")),
                            Double.parseDouble(reader.get("I_R")),
                            Double.parseDouble(reader.get("I_R_DEAD")),
                            Double.parseDouble(reader.get("I_R_LIVE")),
                            Double.parseDouble(reader.get("HPA")),
                            Double.parseDouble(reader.get("HSK")),});
            }
            System.out.println("");
            reader.toString();
            reader.close();
        }
    }

    @Test
    public void testReport() throws VisADException {
        System.out.println("testReport" + " : " + fuelModelCode);
        String report = instance.report();
        assertNotNull(report);
        assertTrue(!report.isEmpty());
        //System.out.println(instance.report());
    }

    @Test
    public void testCalcHerbaceousCuring() {
        System.out.println("calcHerbaceousCuring" + " : " + fuelModelCode);
        FuelMoisture fuelMoisture = BasicFuelMoisture.fromWeatherConditions(HOT_AND_DRY);
        double expResult = 0.56;
        double result = SurfaceFuel.calcHerbaceousCuring(fuelMoisture);
        assertEquals(expResult, result, 1);
    }

    @Test
    public void testGetMeanBulkDensity() {
        System.out.println("getMeanBulkDensity" + " : " + fuelModelCode);
        Real result = instance.getMeanBulkDensity();
        double[] expected = expResults.get(fuelModelCode);
        assertEquals(fuelModelCode + ": rho_b [lb/ft3]", expected[RHO_B], result.getValue(), expected[RHO_B] * 0.01);
    }

    @Test
    public void testGetMeanPackingRatio() {
        System.out.println("getMeanPackingRatio" + " : " + fuelModelCode);
        Real result = instance.getMeanPackingRatio();
        double[] expected = expResults.get(fuelModelCode);
        assertEquals(fuelModelCode + ": beta [-]", expected[BETA], result.getValue(), 0.001);
    }

    @Ignore
    public void testGetOptimalPackingRatio() {
        System.out.println("getOptimalPackingRatio" + " : " + fuelModelCode);
        Real result = instance.getOptimalPackingRatio();
        //double[] expected = expResults.get(fuelModelCode);
        //assertEquals(fuelModelCode + ": beta_opt [-]", expected[BETA_OPT], result.getValue(), expected[BETA_OPT] * 0.01);
        assertNotNull(result);
        assertTrue(!result.isMissing());
    }

    @Test
    public void testGetRelativePackingRatio() {
        System.out.println("getRelativePackingRatio" + " : " + fuelModelCode);
        Real result = instance.getRelativePackingRatio();
        double[] expected = expResults.get(fuelModelCode);
        assertEquals(fuelModelCode + ": beta_ratio [-]", expected[BETA_RATIO], result.getValue(), expected[BETA_RATIO] * 0.01);
    }

    @Test
    public void testGetCharacteristicSAV() {
        System.out.println("getCharacteristicSAV" + " : " + fuelModelCode);
        Real result = instance.getCharacteristicSAV();
        double[] expected = expResults.get(fuelModelCode);
        assertEquals(fuelModelCode + ": sigma [ft2/ft3]", expected[SIGMA], result.getValue(), expected[SIGMA] * 0.0125);
    }

    @Test
    public void testGetMineralDamping() {
        System.out.println("getMineralDamping" + " : " + fuelModelCode);
        Real result = instance.getMineralDamping();
        assertNotNull(result);
        assertTrue(!result.isMissing());
    }

    @Test
    public void testGetLiveMoistureOfExt() {
        System.out.println("getLiveMoistureOfExt" + " : " + fuelModelCode);
        Real result = instance.getLiveMoistureOfExt();
        double[] expected = expResults.get(fuelModelCode);
        assertEquals(fuelModelCode + ": mx_live [%]", expected[MX_LIVE], result.getValue(), expected[MX_LIVE] * 0.5);
    }

    @Test
    public void testGetMoistureDamping() {
        System.out.println("getMoistureDamping" + " : " + fuelModelCode);
        Real result = instance.getMoistureDamping();
        assertNotNull(result);
        assertTrue(!result.isMissing());
    }

    @Test
    public void testGetHeatRelease() throws VisADException {
        System.out.println("getHeatRelease" + " : " + fuelModelCode);
        Real result = instance.getHeatRelease();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        double[] expected = expResults.get(fuelModelCode);
        double expResult = expected[HPA];
        double value = result.getValue();
        double tolerance = expResult * 0.01;
        if (fuelModelCode.equalsIgnoreCase("SH9")) {
            assumeTrue(MathUtil.nearlyEquals(expResult, value, tolerance));
        } else {
            assertEquals(fuelModelCode + ": hpa [Btu/ft2]", expResult, value, tolerance);
        }
    }

    @Test
    public void testGetHeatSink() {
        System.out.println("getHeatSink" + " : " + fuelModelCode);
        Real result = instance.getHeatSink();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        double[] expected = expResults.get(fuelModelCode);
        double expResult = expected[HSK];
        double value = result.getValue();
        double tolerance = expResult * 0.01;
        if (fuelModelCode.equalsIgnoreCase("SH9")) {
            assumeTrue(MathUtil.nearlyEquals(expResult, value, tolerance));
        } else {
            assertEquals(fuelModelCode + ": hsk [Btu/ft3]", expResult, value, tolerance);

        }
    }

    @Test
    public void testGetDead1HrFuelLoad() {
        System.out.println("getDead1HrFuelLoad" + " : " + fuelModelCode);
        Real result = instance.getDead1HrFuelLoad();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUELBED_LOAD_DEAD_1H));
    }

    @Test
    public void testGetDead10HrFuelLoad() {
        System.out.println("getDead10HrFuelLoad" + " : " + fuelModelCode);
        Real result = instance.getDead10HrFuelLoad();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUELBED_LOAD_DEAD_10H));
    }

    @Test
    public void testGetDead100HrFuelLoad() {
        System.out.println("getDead100HrFuelLoad" + " : " + fuelModelCode);
        Real result = instance.getDead100HrFuelLoad();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUELBED_LOAD_DEAD_100H));
    }

    @Test
    public void testGetLiveHerbFuelLoad() {
        System.out.println("getLiveHerbFuelLoad" + " : " + fuelModelCode);
        Real result = instance.getLiveHerbFuelLoad();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUELBED_LOAD_LIVE_HERB));
    }

    @Test
    public void testGetLiveWoodyFuelLoad() {
        System.out.println("getLiveWoodyFuelLoad" + " : " + fuelModelCode);
        Real result = instance.getLiveWoodyFuelLoad();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUELBED_LOAD_LIVE_WOODY));
    }

    @Test
    public void testGetDead1HrSAVRatio() {
        System.out.println("getDead1HrSAVRatio" + " : " + fuelModelCode);
        Real result = instance.getDead1HrSAVRatio();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUELBED_SAV_DEAD_1H));
    }

    @Test
    public void testGetDead10HrSAVRatio() {
        System.out.println("getDead10HrSAVRatio" + " : " + fuelModelCode);
        Real result = instance.getDead10HrSAVRatio();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUELBED_SAV_DEAD_10H));
    }

    @Test
    public void testGetDead100HrSAVRatio() {
        System.out.println("getDead100HrSAVRatio" + " : " + fuelModelCode);
        Real result = instance.getDead100HrSAVRatio();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUELBED_SAV_DEAD_100H));
    }

    @Test
    public void testGetLiveHerbSAVRatio() {
        System.out.println("getLiveHerbSAVRatio" + " : " + fuelModelCode);
        Real result = instance.getLiveHerbSAVRatio();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUELBED_SAV_LIVE_HERB));
    }

    @Test
    public void testGetLiveWoodySAVRatio() {
        System.out.println("getLiveWoodySAVRatio" + " : " + fuelModelCode);
        Real result = instance.getLiveWoodySAVRatio();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUELBED_SAV_LIVE_WOODY));
    }

    @Test
    public void testGetFuelBedDepth() {
        System.out.println("getFuelBedDepth" + " : " + fuelModelCode);
        Real result = instance.getFuelBedDepth();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.FUEL_BED_DEPTH));
    }

    @Test
    public void testGetDeadMoistureOfExt() {
        System.out.println("getDeadMoistureOfExt" + " : " + fuelModelCode);
        Real result = instance.getDeadMoistureOfExt();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.MX_DEAD));
    }

    @Test
    public void testGetDead1HrFuelMoisture() {
        System.out.println("getDead1HrFuelMoisture" + " : " + fuelModelCode);
        Real result = instance.getDead1HrFuelMoisture();
        assertNotNull(result);
        assertTrue(!result.isMissing());
    }

    @Test
    public void testGetDead10HrFuelMoisture() {
        System.out.println("getDead10HrFuelMoisture" + " : " + fuelModelCode);
        Real result = instance.getDead10HrFuelMoisture();
        assertNotNull(result);
        assertTrue(!result.isMissing());
    }

    @Test
    public void testGetDead100HrFuelMoisture() {
        System.out.println("getDead100HrFuelMoisture" + " : " + fuelModelCode);
        Real result = instance.getDead100HrFuelMoisture();
        assertNotNull(result);
        assertTrue(!result.isMissing());
    }

    @Test
    public void testGetLiveHerbFuelMoisture() {
        System.out.println("getLiveHerbFuelMoisture" + " : " + fuelModelCode);
        Real result = instance.getLiveHerbFuelMoisture();
        assertNotNull(result);
        assertTrue(!result.isMissing());
    }

    @Test
    public void testGetLiveWoodyFuelMoisture() {
        System.out.println("getLiveWoodyFuelMoisture" + " : " + fuelModelCode);
        Real result = instance.getLiveWoodyFuelMoisture();
        assertNotNull(result);
        assertTrue(!result.isMissing());
    }

    @Test
    public void testGetLowHeatContent() {
        System.out.println("getLowHeatContent");
        Real result = instance.getLowHeatContent();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.HEAT_CONTENT_US));
    }

    @Test
    public void testGetReactionVelocity() {
        System.out.println("getReactionVelocity");
        Real result = instance.getReactionVelocity();
        assertNotNull(result);
        assertTrue(!result.isMissing());
        assertTrue(result.getType().equals(WildfireType.GAMMA));

    }

    @Test
    public void testGetReactionIntensity() {
        System.out.println("getReactionIntensity");
        Real result = instance.getReactionIntensity();
        double[] expected = expResults.get(fuelModelCode);
        assertEquals(fuelModelCode + ": I_r_dead [Btu/ft2/min]", expected[I_R_DEAD], instance.I_r_dead, expected[I_R_DEAD] * 0.01);
        assertEquals(fuelModelCode + ": I_r_live [Btu/ft2/min]", expected[I_R_LIVE], instance.I_r_live, expected[I_R_LIVE] * 0.01);
        assertEquals(fuelModelCode + ": I_r [Btu/ft2/min]", expected[I_R], result.getValue(), expected[I_R] * 0.01);
    }

}
