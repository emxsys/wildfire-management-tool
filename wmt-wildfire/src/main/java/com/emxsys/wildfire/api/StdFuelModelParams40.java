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

/**
 * New Standard 40 Fire Behavior Fuel Models as defined by Scott and Burgan
 *
 * Implemented using an enum with data and behavior. See "Effective Java,
 * Second Edition", Item #30.
 *
 * <ul>
 * <li style="bullet"><a name="bib_1000"></a>
 * Scott and Burgan, 2005, Standard Fire Behavior Fuel Models: A Comprehensive
 * Set for Use with Rotherme's Surface Fire Spread Model,
 * General Technical Report RMRS-GTR-153, USDA Forest Service,
 * Rocky Mountain Research Station
 * </ul>
 *
 * @author Bruce Schubert
 * @version $Revision: 9 $
 */
public enum StdFuelModelParams40 {

    /**
     * Grass Fuel Type Model (GR)
     * GR1 - Short, Sparse Dry Climate Grass
     */
    FBFM101(101, "GR1", 0.1, 0.0, 0.0, 0.3, 0.0, true, 2200, 2000, 0, 0.4, 15, 8000, "Short, Sparse Dry Climate Grass"),
    /**
     * Grass Fuel Type Model (GR)
     * GR2 - Low Load, Dry Climate Grass
     */
    FBFM102(102, "GR2", 0.1, 0.0, 0.0, 1.0, 0.0, true, 2000, 1800, 0, 1.0, 15, 8000, "Low Load, Dry Climate Grass"),
    /**
     * Grass Fuel Type Model (GR)
     * GR3 -Low Load, Very Coarse, Humid Climate Grass
     */
    FBFM103(103, "GR3", 0.1, 0.4, 0.0, 1.5, 0.0, true, 1500, 1300, 0, 2.0, 30, 8000, "Low Load, Very Coarse, Humid Climate Grass"),
    /**
     * Grass Fuel Type Model (GR)
     * GR4 - Moderate Load, Dry Climate Grass
     */
    FBFM104(104, "GR4", 0.25, 0.0, 0.0, 1.9, 0.0, true, 2000, 1800, 0, 2.0, 15, 8000, "Moderate Load, Dry Climate Grass"),
    /**
     * Grass Fuel Type Model (GR)
     * GR5 - Moderate Load, Dry Climate Grass
     */
    FBFM105(105, "GR5", 0.4, 0.0, 0.0, 2.5, 0.0, true, 1800, 1600, 0, 1.5, 40, 8000, "Low Load, Humid Climate Grass"),
    /**
     * Grass Fuel Type Model (GR)
     * GR6 - Moderate Load, Humid Climate Grass
     */
    FBFM106(106, "GR6", 0.1, 0.0, 0.0, 3.4, 0.0, true, 2200, 2000, 0, 1.5, 40, 9000, "Moderate Load, Humid Climate Grass"), // note BTU heat content (typo?)
    /**
     * Grass Fuel Type Model (GR)
     * GR7 - High Load, Dry Climate Grass
     */
    FBFM107(107, "GR7", 1.0, 0.0, 0.0, 5.4, 0.0, true, 2000, 1800, 0, 3.0, 15, 8000, "High Load, Dry Climate Grass"),
    /**
     * Grass Fuel Type Model (GR)
     * GR8 - High Load, Very Coarse, Humid Climate Grass
     */
    FBFM108(108, "GR8", 0.5, 1.0, 0.0, 7.3, 0.0, true, 1500, 1300, 0, 4.0, 30, 8000, "High Load, Very Coarse, Humid Climate Grass"),
    /**
     * Grass Fuel Type Model (GR)
     * GR9 - Very High Load, Humid Climate Grass
     */
    FBFM109(109, "GR9", 1.0, 1.0, 0.0, 9.0, 0.0, true, 1800, 1600, 0, 5.0, 40, 8000, "Very High Load, Humid Climate Grass"),
    /**
     * Grass-Shrub Fuel Type Model (GS)
     * GS1 - Low Load, Dry Climate Grass-Shrub
     */
    FBFM121(121, "GS1", 0.2, 0.0, 0.0, 0.5, 0.65, true, 2000, 1800, 1800, 0.9, 15, 8000, "Low Load, Dry Climate Grass-Shrub"),
    /**
     * Grass-Shrub Fuel Type Model (GS)
     * GS2 - Moderate Load, Dry Climate Grass-Shrub
     */
    FBFM122(122, "GS2", 0.5, 0.5, 0.0, 0.6, 1.0, true, 2000, 1800, 1800, 1.5, 15, 8000, "Moderate Load, Dry Climate Grass-Shrub"),
    /**
     * Grass-Shrub Fuel Type Model (GS)
     * GS3 - Moderate Load, Humid Climate Grass-Shrub
     */
    FBFM123(123, "GS3", 0.3, 0.25, 0.0, 1.45, 1.25, true, 1800, 1600, 1600, 1.8, 40, 8000, "Moderate Load, Humid Climate Grass-Shrub"),
    /**
     * Grass-Shrub Fuel Type Model (GS)
     * GS4 - High Load, Humid Climate Grass-Shrub
     */
    FBFM124(124, "GS4", 1.9, 0.3, 0.1, 3.4, 7.1, true, 1800, 1600, 1600, 2.1, 40, 8000, "High Load, Humid Climate Grass-Shrub"),
    /**
     * Shrub Fuel Type Model (SH)
     * SH1 - Low Load Dry Climate Shrub
     */
    FBFM141(141, "SH1", 0.25, 0.25, 0.0, 0.15, 1.3, true, 2000, 1800, 1600, 1.0, 15, 8000, "Low Load Dry Climate Shrub"),
    /**
     * Shrub Fuel Type Model (SH)
     * SH2 - Moderate Load Dry Climate Shrub
     */
    FBFM142(142, "SH2", 1.35, 2.4, 0.75, 0.0, 3.85, false, 2000, 0, 1600, 1.0, 15, 8000, "Moderate Load Dry Climate Shrub"),
    /**
     * Shrub Fuel Type Model (SH)
     * SH3 - Moderate Load, Humid Climate Shrub
     */
    FBFM143(143, "SH3", 0.45, 3.0, 0.0, 0.0, 6.2, false, 1600, 0, 1400, 2.4, 40, 8000, "Moderate Load, Humid Climate Shrub"),
    /**
     * Shrub Fuel Type Model (SH)
     * SH4 - Low Load, Humid Climate Timber-Shrub
     */
    FBFM144(144, "SH4", 0.85, 1.15, 0.2, 0.0, 2.55, false, 2000, 1800, 1600, 3.0, 30, 8000, "Low Load, Humid Climate Timber-Shrub"),
    /**
     * Shrub Fuel Type Model (SH)
     * SH5 - High Load, Dry Climate Shrub
     */
    FBFM145(145, "SH5", 3.6, 2.1, 0.0, 0.0, 2.9, false, 750, 0, 1600, 6.0, 15, 8000, "High Load, Dry Climate Shrub"),
    /**
     * Shrub Fuel Type Model (SH)
     * SH6 - Low Load, Humid Climate Shrub
     */
    FBFM146(146, "SH6", 2.9, 1.45, 0.0, 0.0, 1.4, false, 750, 0, 1600, 2.0, 30, 8000, "Low Load, Humid Climate Shrub"),
    /**
     * Shrub Fuel Type Model (SH)
     * SH7 - Very High Load, Dry Climate Shrub
     */
    FBFM147(147, "SH7", 3.5, 5.3, 2.2, 0.0, 3.4, false, 750, 0, 1600, 6.0, 15, 8000, "Very High Load, Dry Climate Shrub"),
    /**
     * Shrub Fuel Type Model (SH)
     * SH8 - High Load, Humid Climate Shrub
     */
    FBFM148(148, "SH8", 2.05, 3.4, 0.85, 0.0, 4.35, false, 750, 0, 1600, 3.0, 40, 8000, "High Load, Humid Climate Shrub"),
    /**
     * Shrub Fuel Type Model (SH)
     * SH9 - Very High Load, Humid Climate Shrub
     */
    FBFM149(149, "SH9", 4.5, 2.45, 0.0, 1.55, 7.0, true, 750, 1800, 1500, 4.4, 40, 8000, "Very High Load, Humid Climate Shrub"),
    /**
     * Timber-Understory Fuel Type Model (TU)
     * TU1 - Low Load Dry Climate Timber-Grass-Shrub
     */
    FBFM161(161, "TU1", 0.2, 0.9, 1.5, 0.2, 0.9, true, 2000, 1800, 1600, 0.6, 20, 8000, "Low Load Dry Climate Timber-Grass-Shrub"),
    /**
     * Timber-Understory Fuel Type Model (TU)
     * TU2 - Moderate Load, Humid Climate Timber-Shrub
     */
    FBFM162(162, "TU2", 0.95, 1.8, 1.25, 0.0, 0.2, false, 2000, 0, 1600, 1.0, 30, 8000, "Moderate Load, Humid Climate Timber-Shrub"),
    /**
     * Timber-Understory Fuel Type Model (TU)
     * TU3 - Moderate Load, Humid Climate Timber-Grass-Shrub
     */
    FBFM163(163, "TU3", 1.1, 0.15, 0.25, 0.65, 1.1, true, 1800, 1600, 1400, 1.3, 30, 8000, "Moderate Load, Humid Climate Timber-Grass-Shrub"),
    /**
     * Timber-Understory Fuel Type Model (TU)
     * TU4 - Dwarf Conifer With Understory
     */
    FBFM164(164, "TU4", 4.5, 0.0, 0.0, 0.0, 2.0, false, 2300, 0, 2000, 0.5, 12, 8000, "Dwarf Conifer With Understory"),
    /**
     * Timber-Understory Fuel Type Model (TU)
     * TU5 - Very High Load, Dry Climate Timber-Shrub
     */
    FBFM165(165, "TU5", 4.0, 4.0, 3.0, 0.0, 3.0, false, 1500, 0, 750, 1.0, 25, 8000, "Very High Load, Dry Climate Timber-Shrub"),
    /**
     * Timber Litter Fuel Type Model (TL)
     * TL1 - Low Load Compact Conifer Litter
     */
    FBFM181(181, "TL1", 1.0, 2.2, 3.6, 0.0, 0.0, false, 2000, 0, 0, 0.2, 30, 8000, "Low Load Compact Conifer Litter"),
    /**
     * Timber Litter Fuel Type Model (TL)
     * TL2 - Low Load Broadleaf Litter
     */
    FBFM182(182, "TL2", 1.4, 2.3, 2.2, 0.0, 0.0, false, 2000, 0, 0, 0.2, 25, 8000, "Low Load Broadleaf Litter"),
    /**
     * Timber Litter Fuel Type Model (TL)
     * TL3 - Moderate Load Conifer Litter
     */
    FBFM183(183, "TL3", 0.5, 2.2, 2.8, 0.0, 0.0, false, 2000, 0, 0, 0.3, 20, 8000, "Moderate Load Conifer Litter"),
    /**
     * Timber Litter Fuel Type Model (TL)
     * TL4 - Small Downed Logs
     */
    FBFM184(184, "TL4", 0.5, 1.5, 4.2, 0.0, 0.0, false, 2000, 0, 0, 0.4, 25, 8000, "Small Downed Logs"),
    /**
     * Timber Litter Fuel Type Model (TL)
     * TL5 - High Load Conifer Litter
     */
    FBFM185(185, "TL5", 1.15, 2.5, 4.4, 0.0, 0.0, false, 2000, 0, 1600, 0.6, 25, 8000, "High Load Conifer Litter"),
    /**
     * Timber Litter Fuel Type Model (TL)
     * TL6 - Moderate Load Broadleaf Litter
     */
    FBFM186(186, "TL6", 2.4, 1.2, 1.2, 0.0, 0.0, false, 2000, 0, 0, 0.3, 25, 8000, "Moderate Load Broadleaf Litter"),
    /**
     * Timber Litter Fuel Type Model (TL)
     * TL7 - Large Downed Logs
     */
    FBFM187(187, "TL7", 0.3, 1.4, 8.1, 0.0, 0.0, false, 2000, 0, 0, 0.4, 25, 8000, "Large Downed Logs"),
    /**
     * Timber Litter Fuel Type Model (TL)
     * TL8 - Long-Needle Litter
     */
    FBFM188(188, "TL8", 5.8, 1.4, 1.1, 0.0, 0.0, false, 1800, 0, 0, 0.3, 35, 8000, "Long-Needle Litter"),
    /**
     * Timber Litter Fuel Type Model (TL)
     * TL9 - Very High Load Broadleaf Litter
     */
    FBFM189(189, "TL9", 6.65, 3.3, 4.15, 0.0, 0.0, false, 1800, 0, 1600, 0.6, 35, 8000, "Very High Load Broadleaf Litter"),
    /**
     * Slash-Blowdown Fuel Type Model (SB)
     * SB1 - Low Load Activity Fuel
     */
    FBFM201(201, "SB1", 1.5, 3.0, 11.0, 0.0, 0.0, false, 2000, 0, 0, 1.0, 25, 8000, "Low Load Activity Fuel"),
    /**
     * Slash-Blowdown Fuel Type Model (SB)
     * SB2 - Moderate Load Activity Fuel or Low Load Blowdown
     */
    FBFM202(202, "SB2", 4.5, 4.25, 4.0, 0.0, 0.0, false, 2000, 0, 0, 1.0, 25, 8000, "Moderate Load Activity Fuel or Low Load Blowdown"),
    /**
     * Slash-Blowdown Fuel Type Model (SB)
     * SB3 - High Load Activity Fuel or Moderate Load Blowdown
     */
    FBFM203(203, "SB3", 5.5, 2.75, 3.0, 0.0, 0.0, false, 2000, 0, 0, 1.2, 25, 8000, "High Load Activity Fuel or Moderate Load Blowdown"),
    /**
     * Slash-Blowdown Fuel Type Model (SB)
     * SB3 - High Load Blowdown
     */
    FBFM204(204, "SB4", 5.25, 3.5, 5.25, 0.0, 0.0, false, 2000, 0, 0, 2.7, 25, 8000, "High Load Blowdown"),
    /**
     * Nonburnable Fuel Type Model (NB)
     * NB1 - Urban/Developed
     */
    FBFM91(91, "NB1", 0.0, 0.0, 0.0, 0.0, 0.0, false, 0, 0, 0, 0.0, 0, 0, "Urban/Developed"),
    /**
     * Nonburnable Fuel Type Model (NB)
     * NB2 - Snow/Ice
     */
    FBFM92(92, "NB2", 0.0, 0.0, 0.0, 0.0, 0.0, false, 0, 0, 0, 0.0, 0, 0, "Snow/Ice"),
    /**
     * Nonburnable Fuel Type Model (NB)
     * NB3 - Agriculture
     */
    FBFM93(93, "NB3", 0.0, 0.0, 0.0, 0.0, 0.0, false, 0, 0, 0, 0.0, 0, 0, "Agriculture"),
    /**
     * Nonburnable Fuel Type Model (NB)
     * NB8 - Open Water
     */
    FBFM98(98, "NB8", 0.0, 0.0, 0.0, 0.0, 0.0, false, 0, 0, 0, 0.0, 0, 0, "Open Water"),
    /**
     * Nonburnable Fuel Type Model (NB)
     * NB9 - Bare Bround
     */
    FBFM99(99, "NB9", 0.0, 0.0, 0.0, 0.0, 0.0, false, 0, 0, 0, 0.0, 0, 0, "Bare Bround");

    /** 1 hour dead fuel loading
     * @return [tons/acre] */
    public Real getDead1HrFuelLoad() {
        return dead1HrFuelLoad;
    }

    /** 10 hour dead fuel loading
     * @return [tons/acre] */
    public Real getDead10HrFuelLoad() {
        return dead10HrFuelLoad;
    }

    /** 100 hour dead fuel loading
     * @return [tons/acre] */
    public Real getDead100HrFuelLoad() {
        return dead100HrFuelLoad;
    }

    /** 1 hour dead fuel Surface-area-to-volumne ratio
     * @return [1/ft] */
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

    /** Heat content
     * @return [BTU/lb]*/
    public Real getHeatContent() {
        return heatContent;
    }

    /** Fuel model type - static or dynamic
     * @return true   if dynamic */
    public boolean isDynamic() {
        return isDynamic;
    }

    /** Live herbatious fuel loading
     * @return [tons/acre] */
    public Real getLiveHerbFuelLoad() {
        return liveHerbFuelLoad;
    }

    /** Live herbatious surface area to volume ratio
     * @return [percent]
     */
    public Real getLiveHerbSAVRatio() {
        return liveHerbSAVRatio;
    }

    /** Live woody fuel loading
     * @return [tons/acre] */
    public Real getLiveWoodyFuelLoad() {
        return liveWoodyFuelLoad;
    }

    /** Live woody surface area to volume ratio
     * @return [percent] */
    public Real getLiveWoodySAVRatio() {
        return liveWoodySAVRatio;
    }

    /** fuel model code */
    public String getModelCode() {
        return modelCode;
    }

    /**
     * Fuel model complex name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Fuel model number compatible with LANDFIRE data
     */
    public int getModelNo() {
        return modelNo;
    }

    /**
     * Model # - CODE - Model name
     */
    @Override
    public String toString() {
        return modelNo + " - " + modelCode + " - " + modelName;
    }

    private final int modelNo;
    private final String modelCode;
    private final Real dead1HrFuelLoad;
    private final Real dead10HrFuelLoad;
    private final Real dead100HrFuelLoad;
    private final Real liveHerbFuelLoad;
    private final Real liveWoodyFuelLoad;
    private final boolean isDynamic;
    private final Real dead1HrSAVRatio;
    private final Real liveHerbSAVRatio;
    private final Real liveWoodySAVRatio;
    private final Real fuelBedDepth;
    private final Real extinctionMoisture;
    private final Real heatContent;
    private final String modelName;

    StdFuelModelParams40(int modelNo, String modelCode,
            double dead1HrFuelLoad, double dead10HrFuelLoad, double dead100HrFuelLoad,
            double liveHerbFuelLoad, double liveWoodyFuelLoad, boolean isDynamic,
            double dead1HrSAVRatio, double liveHerbSAVRatio, double liveWoodySAVRatio,
            double fuelBedDepth, double extinctionMoisture, double heatContent, String modelName) {

        this.dead1HrFuelLoad = new Real(WildfireType.FUEL_LOAD_US, dead1HrFuelLoad);
        this.dead10HrFuelLoad = new Real(WildfireType.FUEL_LOAD_US, dead10HrFuelLoad);
        this.dead100HrFuelLoad = new Real(WildfireType.FUEL_LOAD_US, dead100HrFuelLoad);
        this.liveHerbFuelLoad = new Real(WildfireType.FUEL_LOAD_US, liveHerbFuelLoad);
        this.liveWoodyFuelLoad = new Real(WildfireType.FUEL_LOAD_US, liveWoodyFuelLoad);
        this.dead1HrSAVRatio = new Real(WildfireType.SAV_RATIO_US, dead1HrSAVRatio);
        this.liveHerbSAVRatio = new Real(WildfireType.SAV_RATIO_US, liveHerbSAVRatio);
        this.liveWoodySAVRatio = new Real(WildfireType.SAV_RATIO_US, liveWoodySAVRatio);
        this.fuelBedDepth = new Real(WildfireType.FUEL_DEPTH_US, fuelBedDepth);
        this.extinctionMoisture = new Real(WildfireType.MOISTURE_OF_EXTINCTION, extinctionMoisture);
        this.heatContent = new Real(WildfireType.HEAT_CONTENT_US, heatContent);
        this.modelNo = modelNo;
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.isDynamic = isDynamic;
    }
}
