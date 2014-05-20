/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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

import visad.Real;
import visad.RealType;

/**
 * Original 13 Fire Behavior Fuel Models as codefied by Albini and Anderson.
 *
 * Implemented using an enum with data and behavior. See "Effective Java,
 * Second Edition", Item #30.
 *
 * <ul>
 * <li style="bullet"><a name="bib_1000"></a>Albini, F.A., 1976,
 * Estimating Wildfire Behaviour and Effects,
 * General Technical Report INT-30,
 * USDA Forest Service, Intermountain Forest and Range Experiment Station
 * </ul>
 * See Table 7. Pg 92
 * Albini used a constant 10-hr SAV ratio of 109 1/ft, and a constant
 * 100-hr SAV ratio of 30 1/ft.
 *
 * <ul>
 * <li style="bullet"><a name="bib_1000"></a> Anderson, Hal E., 1982, 
 * Aids to Determining Fuel Models For Estimating Fire Behavior,
 * General Technical Report INT-122,
 * USDA Forest Service, Intermountain Forest and Range Experiment Station
 * </ul>
 *
 * @author Bruce Schubert
 * @version $Revision: 26 $
 */
public enum StdFuelModelParams13 {

    /** Constructor params:
     * 
     * modelNo, dead1HrFuelLoad, dead10HrFuelLoad, dead100HrFuelLoad,
     * liveFuelLoad, dead1HrSAVRatio, liveFuelSAVRatio,
     * fuelBeddepth, moistureOfextinction, modelName
     */
    // Grass and grass-dominated
    /**
     * Short Grass (1 foot)
     */
    FBFM01(1, 0.74, 0.00, 0.00, 0.00, 3500, 0, 1.0, 12, "Short Grass (1 foot)"),
    /**
     * Timber (grass and understory)
     */
    FBFM02(2, 2.00, 1.00, 0.50, 0.50, 3000, 1500, 1.0, 15, "Timber (grass and understory)"),
    /**
     * Tall grass (2.5 feet)
     */
    FBFM03(3, 3.01, 0.00, 0.00, 0.00, 1500, 0, 2.5, 25, "Tall grass (2.5 feet)"),
    // Chaparral and shrub fields
    /**
     * Chaparral (6 feet)
     */
    FBFM04(4, 5.01, 4.01, 2.00, 5.01, 2000, 1500, 6.0, 20, "Chaparral (6 feet)"),
    /**
     * Brush (2 feet)
     */
    FBFM05(5, 1.00, 0.50, 0.00, 2.00, 2000, 1500, 2.0, 20, "Brush (2 feet)"),
    /**
     * Dormant brush, hardwood slash
     */
    FBFM06(6, 1.50, 2.50, 2.00, 0.00, 1750, 0, 2.5, 25, "Dormant brush, hardwood slash"),
    /**
     * Southern rough
     */
    FBFM07(7, 1.13, 1.87, 1.50, 0.37, 1750, 1550, 2.5, 40, "Southern rough"),
    // Timber litter
    /**
     * Closed timber litter
     */
    FBFM08(8, 1.50, 1.00, 2.50, 0.0, 2000, 0, 0.2, 30, "Closed timber litter"),
    /**
     * Hardwood litter
     */
    FBFM09(9, 2.92, 0.41, 0.15, 0.0, 2500, 0, 0.2, 25, "Hardwood litter"),
    /**
     * Timber (litter and understory)
     */
    FBFM10(10, 3.01, 2.00, 5.01, 2.0, 2000, 1500, 1.0, 25, "Timber (litter and understory)"),
    // Slash
    /**
     * Light logging slash
     */
    FBFM11(11, 1.50, 4.51, 5.51, 0.0, 1500, 0, 1.0, 15, "Light logging slash"),
    /**
     * Medium logging slash
     */
    FBFM12(12, 4.01, 14.03, 16.53, 0.0, 1500, 0, 2.3, 20, "Medium logging slash"),
    /**
     * Heavy logging slash
     */
    FBFM13(13, 7.01, 23.04, 28.05, 0.0, 1500, 0, 3.0, 25, "Heavy logging slash"),
    // Non-burnable (not part of original thirteen, but appears in LANDFIRE data)
    /**
     * Urban
     */
    FBFM91(91, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, 0, "Urban"),
    /**
     * Snow/Ice
     */
    FBFM92(92, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, 0, "Snow/Ice"),
    /**
     * Agriculture
     */
    FBFM93(93, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, 0, "Agriculture"),
    /**
     * Water
     */
    FBFM98(98, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, 0, "Water"),
    /**
     * Barren
     */
    FBFM99(99, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, 0, "Barren");

    @Override
    public String toString() {
        return modelNo + " - " + modelName;
    }

    /** 1 hour dead fuel loading
     * @return [tons/acre]*/
    public Real getDead1HrFuelLoad() {
        return dead1HrFuelLoad;
    }

    /** 10 hour dead fuel loading
     * @return [tons/acre]*/
    public Real getDead10HrFuelLoad() {
        return dead10HrFuelLoad;
    }

    /** 100 hour dead fuel loading
     * @return [tons/acre]*/
    public Real getDead100HrFuelLoad() {
        return dead100HrFuelLoad;
    }

    /** 1 hour dead fuel SAV Ratio
     * @return [1/ft]*/
    public Real getDead1HrSAVRatio() {
        return dead1HrSAVRatio;
    }

    /** Moisture of extinction for dead fuels
     * @return [percent] */
    public Real getMoistureOfExtinction() {
        return extinctionMoisture;
    }

    /** Fuel bed depth
     * @return [feet] */
    public Real getFuelBedDepth() {
        return fuelBedDepth;
    }

    /** Live fuel loading
     * @return [tons/acre]*/
    public Real getLiveFuelLoad() {
        return liveFuelLoad;
    }

    /** Live fuel SAV Ratio
     * @return [1/ft]*/
    public Real getLiveFuelSAVRatio() {
        return liveFuelSAVRatio;
    }

    /** Low heat content - heat of combustion
     * @return [Btu/lt]*/
    public Real getLowHeatContent() {
        return lowHeatContent;
    }

    /** Fuel model number */
    public int getModelNo() {
        return modelNo;
    }

    /** Fuel type complex */
    public String getModelName() {
        return modelName;
    }
    private final Real dead1HrFuelLoad;
    private final Real dead10HrFuelLoad;
    private final Real dead100HrFuelLoad;
    private final Real liveFuelLoad;
    private final Real dead1HrSAVRatio;
    private final Real liveFuelSAVRatio;
    private final Real fuelBedDepth;
    private final Real extinctionMoisture;
    private final Real lowHeatContent;
    private final int modelNo;
    private final String modelName;

    StdFuelModelParams13(int modelNo,
            double dead1HrFuelLoad, double dead10HrFuelLoad, double dead100HrFuelLoad,
            double liveFuelLoad, double dead1HrSAVRatio, double liveFuelSAVRatio,
            double fuelDepth, int extinctionMoisture, String modelName) {

        this.modelNo = modelNo;
        this.dead1HrFuelLoad = new Real(WildfireType.FUEL_LOAD_US, dead1HrFuelLoad);
        this.dead10HrFuelLoad = new Real(WildfireType.FUEL_LOAD_US, dead10HrFuelLoad);
        this.dead100HrFuelLoad = new Real(WildfireType.FUEL_LOAD_US, dead100HrFuelLoad);
        this.liveFuelLoad = new Real(WildfireType.FUEL_LOAD_US, liveFuelLoad);
        this.dead1HrSAVRatio = new Real(WildfireType.SAV_RATIO_US, dead1HrSAVRatio);
        this.liveFuelSAVRatio = new Real(WildfireType.SAV_RATIO_US, liveFuelSAVRatio);
        this.fuelBedDepth = new Real(WildfireType.FUEL_DEPTH_US, fuelDepth);
        this.extinctionMoisture = new Real(WildfireType.MOISTURE_OF_EXTINCTION, extinctionMoisture);
        this.lowHeatContent = new Real(WildfireType.HEAT_US, 8000.0);   // Abini (1976)
        this.modelName = modelName;
    }
}
