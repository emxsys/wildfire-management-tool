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

import com.emxsys.visad.Reals;
import com.emxsys.wildfire.behave.Behave;
import java.util.*;
import visad.Real;

/**
 * Class responsible for transforming fuel model inputs into SI units used by the Behave class. It
 * uses a builder pattern to create objects from either standard fuel model enums (see
 * StdFuelModeParams13 and StdFuelModelParams40) or via explicit parameters in the Builder's build
 * method (for custom models).
 *
 * @see StdFuelModelParams13
 * @see StdFuelModelParams40
 * @see Behave
 *
 * @author Bruce D. Schubert
 */
public class StdFuelModel implements FuelModel {

    /**
     * A non-burnable fuel model representing an INVALID model; Fuel model number: -1
     */
    public static final FuelModel INVALID;
    /**
     * The original 13 fuel models.
     */
    public static final String FUEL_MODEL_GROUP_ORIGINAL_13 = "Original 13";
    /**
     * The standard 40 fuel models.
     */
    public static final String FUEL_MODEL_GROUP_STANDARD_40 = "Standard 40";
    /**
     * Unburnable fuel models common to the original 13 and standard 40, such as urban, water, etc.
     */
    public static final String FUEL_MODEL_GROUP_UNBURNABLE = "Unburnable";
    /**
     * Custom fuel models.
     */
    public static final String FUEL_MODEL_GROUP_CUSTOM = "Custom";
    /**
     * Collection reuse of previously created FuelModel instances
     */
    private static Map<Integer, FuelModel> fuelModels;
    private static Set<String> fuelModelGroups;
    private int modelNo;
    private String modelCode;
    private String modelName;
    private String modelGroup;
    private boolean dynamic;
    private Real dead1HrFuelLoad;
    private Real dead10HrFuelLoad;
    private Real dead100HrFuelLoad;
    private Real liveHerbFuelLoad;
    private Real liveWoodyFuelLoad;
    private Real dead1HrSAVRatio;
    private Real dead10HrSAVRatio;
    private Real dead100HrSAVRatio;
    private Real liveHerbSAVRatio;
    private Real liveWoodySAVRatio;
    private Real fuelBedDepth;
    private Real moistureOfExtinction;
    private Real lowHeatContent;

    static {
        fuelModels = new HashMap<>();
        fuelModelGroups = new HashSet<>();
        INVALID = new StdFuelModel.Builder(-1, "INVALID", "Invalid Fuel Model",
                Builder.FUEL_LOAD_ZERO,
                Builder.SAV_RATIO_ZERO,
                Builder.FUEL_DEPTH_ZERO,
                Builder.EXT_MOISTURE_ZERO).build();
    }

    /**
     * Returns a FuelModel object matching the fuel model number.
     *
     * @param fuelModelNo an integer matching either a previously created custom fuel model, or one
     * of the original '13' or standard '40' fuel model codes.
     * @return null if the FuelModel doesn't exist or cannot be created.
     */
    public static FuelModel getFuelModel(int fuelModelNo) {
        // First, look for an existing fuel model instance...
        FuelModel fm = fuelModels.get(fuelModelNo);
        if (fm == null) {
            // ... attempt to create a fuel model from one of the fuel model enums
            final String fmt = "FBFM%02d";
            String name = String.format(fmt, fuelModelNo);
            try {
                if (fuelModelNo < 100) {
                    StdFuelModelParams13 params = StdFuelModelParams13.valueOf(name);
                    fm = new Builder(params).build();
                } else {
                    StdFuelModelParams40 params = StdFuelModelParams40.valueOf(name);
                    fm = new Builder(params).build();
                }
            } catch (IllegalArgumentException e) {
                // fm remains null if not a valid enum
            }
        }
        return fm;
    }

    public static final class Builder {

        // -------------------
        // Required parameters
        // -------------------
        private Real dead1HrFuelLoad;
        private Real dead10HrFuelLoad;
        private Real dead100HrFuelLoad;
        private Real liveHerbFuelLoad;
        private Real liveWoodyFuelLoad;
        private Real fuelBedDepth;
        private Real moistureOfExtinction;
        // -------------------
        // Optional parameters
        // -------------------
        private int modelNo = 0;
        private String modelCode;
        private String modelName;
        private String modelGroup;
        private boolean dynamic = false;
        private Real dead1HrSAVRatio;
        private Real dead10HrSAVRatio;
        private Real dead100HrSAVRatio;
        private Real liveHerbSAVRatio;
        private Real liveWoodySAVRatio;
        private Real heatContent;
        // Defaults and constants used by Albini (1976)
        static final Real SAV_RATIO_10HR_US = new Real(WildfireType.SAV_RATIO_US, 109.0);
        static final Real SAV_RATIO_100HR_US = new Real(WildfireType.SAV_RATIO_US, 30.0);
        static final Real HEAT_CONTENT_US = new Real(WildfireType.HEAT_CONTENT_US, 8000.0);
        static final Real SAV_RATIO_ZERO = new Real(WildfireType.SAV_RATIO_SI, 0.0);
        static final Real FUEL_LOAD_ZERO = new Real(WildfireType.FUEL_LOAD_SI, 0.0);
        static final Real FUEL_DEPTH_ZERO = new Real(WildfireType.FUEL_DEPTH_SI, 0.0);
        static final Real EXT_MOISTURE_ZERO = new Real(WildfireType.MOISTURE_OF_EXTINCTION, 0.0);

        /**
         * Builder constructor for custom fuel models using just the required Fuel Model parameters.
         *
         * @param fuelModelNo fuel model number [common id]
         * @param modelCode fuel model code
         * @param modelName fuel model name
         * @param dead1HrFuelLoad 1-hour dead fuel loading [kg/m2]
         * @param dead1HrSAVRatio 1-hour dead fuel surface-area-to-volumne ratio [1/m]
         * @param fuelBedDepth fuel bed depth [m]
         * @param moistureOfExtinction Moisture of extinction for dead fuels [percent]
         */
        public Builder(int fuelModelNo, String modelCode, String modelName,
                       Real dead1HrFuelLoad, Real dead1HrSAVRatio,
                       Real fuelBedDepth, Real moistureOfExtinction) {
            // Required params
            this.modelNo = fuelModelNo;
            this.modelCode = modelCode;
            this.modelName = modelName;
            this.modelGroup = FUEL_MODEL_GROUP_CUSTOM;
            this.setDead1HrFuelLoad(dead1HrFuelLoad);
            this.setDead1HrSAVRatio(dead1HrSAVRatio);
            this.setFuelBedDepth(fuelBedDepth);
            this.setMoistureOfExtinction(moistureOfExtinction);
            // Optional params
            this.setDead10HrFuelLoad(FUEL_LOAD_ZERO);
            this.setDead100HrFuelLoad(FUEL_LOAD_ZERO);
            this.setLiveHerbFuelLoad(FUEL_LOAD_ZERO);
            this.setLiveWoodyFuelLoad(FUEL_LOAD_ZERO);
            this.setDead10HrSAVRatio(SAV_RATIO_10HR_US);
            this.setDead100HrSAVRatio(SAV_RATIO_100HR_US);
            this.setLiveHerbSAVRatio(SAV_RATIO_ZERO);
            this.setLiveWoodySAVRatio(SAV_RATIO_ZERO);
            this.setHeatContent(HEAT_CONTENT_US);
        }

        /**
         * Fuel model builder from the one of original '13' fuel models
         *
         * @param fbfm - "fire behavior fuel model" enum representing a LANDFIRE code
         */
        public Builder(StdFuelModelParams13 fbfm) {
            // Note: The 13 standard fuel models don't have separate live
            // herbaceus and live woody fuel components, just live fuel.
            // Upon examination of all of the nffl property input files created
            // by the Behave class author Andreas Bachmann, we see the live fuel
            // components are mapped to live woody inputs.
            // (see com.emxsys.behave.data for examples)
            this.setDead1HrFuelLoad(fbfm.getDead1HrFuelLoad());
            this.setDead10HrFuelLoad(fbfm.getDead10HrFuelLoad());
            this.setDead100HrFuelLoad(fbfm.getDead100HrFuelLoad());
            this.setLiveWoodyFuelLoad(fbfm.getLiveFuelLoad());
            this.setDead1HrSAVRatio(fbfm.getDead1HrSAVRatio());
            this.setDead10HrSAVRatio(fbfm.getDead10HrFuelLoad().getValue() > 0 ? SAV_RATIO_10HR_US : SAV_RATIO_ZERO);
            this.setDead100HrSAVRatio(fbfm.getDead100HrFuelLoad().getValue() > 0 ? SAV_RATIO_100HR_US : SAV_RATIO_ZERO);
            this.setLiveWoodySAVRatio(fbfm.getLiveFuelSAVRatio());
            this.setFuelBedDepth(fbfm.getFuelBedDepth());
            this.setMoistureOfExtinction(fbfm.getMoistureOfExtinction());
            this.setHeatContent(fbfm.getLowHeatContent());
            this.modelNo = fbfm.getModelNo();
            this.modelGroup = fbfm.getModelNo() > 90 && fbfm.getModelNo() < 100 ? FUEL_MODEL_GROUP_UNBURNABLE : FUEL_MODEL_GROUP_ORIGINAL_13;
            this.setModelCode("#" + fbfm.getModelNo());
            this.setModelName(fbfm.getModelName());
            this.setLiveHerbFuelLoad(FUEL_LOAD_ZERO);
            this.setLiveHerbSAVRatio(SAV_RATIO_ZERO);
        }

        /**
         * Fuel model builder from the one of new fuel models defined by Scott and Burgan.
         *
         * @param fbfm "fire behavior fuel model" enum representing a LANDFIRE code
         */
        public Builder(StdFuelModelParams40 fbfm) {
            this.setDead1HrFuelLoad(fbfm.getDead1HrFuelLoad());
            this.setDead10HrFuelLoad(fbfm.getDead10HrFuelLoad());
            this.setDead100HrFuelLoad(fbfm.getDead100HrFuelLoad());
            this.setLiveHerbFuelLoad(fbfm.getLiveHerbFuelLoad());
            this.setLiveWoodyFuelLoad(fbfm.getLiveWoodyFuelLoad());
            this.setDynamic(fbfm.isDynamic());
            this.setDead1HrSAVRatio(fbfm.getDead1HrSAVRatio());
            this.setLiveHerbSAVRatio(fbfm.getLiveHerbSAVRatio());
            this.setLiveWoodySAVRatio(fbfm.getLiveWoodySAVRatio());
            this.setMoistureOfExtinction(fbfm.getMoistureOfExtinction());
            this.setFuelBedDepth(fbfm.getFuelBedDepth());
            this.setHeatContent(fbfm.getHeatContent());
            this.modelNo = fbfm.getModelNo();
            this.modelGroup = fbfm.getModelNo() > 90 && fbfm.getModelNo() < 100 ? FUEL_MODEL_GROUP_UNBURNABLE : FUEL_MODEL_GROUP_STANDARD_40;
            this.setModelCode(fbfm.getModelCode());
            this.setModelName(fbfm.getModelName());
            this.setDead10HrSAVRatio(fbfm.getDead10HrFuelLoad().getValue() > 0 ? SAV_RATIO_10HR_US : SAV_RATIO_ZERO);
            this.setDead100HrSAVRatio(fbfm.getDead100HrFuelLoad().getValue() > 0 ? SAV_RATIO_100HR_US : SAV_RATIO_ZERO);
        }

        /**
         * @param fuelLoad 1 hour dead fuel loading [kg/m2]
         */
        public void setDead1HrFuelLoad(Real fuelLoad) {
            this.dead1HrFuelLoad = Reals.convertTo(WildfireType.FUEL_LOAD_SI, fuelLoad);
        }

        /**
         * @param fuelLoad 10 hour dead fuel loading [kg/m2]
         */
        public void setDead10HrFuelLoad(Real fuelLoad) {
            this.dead10HrFuelLoad = Reals.convertTo(WildfireType.FUEL_LOAD_SI, fuelLoad);
        }

        /**
         * @param fuelLoad 100 hour dead fuel loading [kg/m2]
         */
        public void setDead100HrFuelLoad(Real fuelLoad) {
            this.dead100HrFuelLoad = Reals.convertTo(WildfireType.FUEL_LOAD_SI, fuelLoad);
        }

        /**
         * @param fuelLoad Live herbatious fuel loading [kg/m2]
         */
        public void setLiveHerbFuelLoad(Real fuelLoad) {
            this.liveHerbFuelLoad = Reals.convertTo(WildfireType.FUEL_LOAD_SI, fuelLoad);
        }

        /**
         * @param fuelLoad Live woody fuel loading [kg/m2]
         */
        public void setLiveWoodyFuelLoad(Real fuelLoad) {
            this.liveWoodyFuelLoad = Reals.convertTo(WildfireType.FUEL_LOAD_SI, fuelLoad);
        }

        /**
         * @param savRatio Dead 1Hr fuel surface-area-to-volumne ratio [1/m]
         */
        public void setDead1HrSAVRatio(Real savRatio) {
            this.dead1HrSAVRatio = Reals.convertTo(WildfireType.SAV_RATIO_SI, savRatio);
        }

        /**
         * @param savRatio Dead 1Hr fuel surface-area-to-volumne ratio [1/m]
         */
        public void setDead10HrSAVRatio(Real savRatio) {
            this.dead10HrSAVRatio = Reals.convertTo(WildfireType.SAV_RATIO_SI, savRatio);
        }

        /**
         * @param savRatio Dead 1Hr fuel surface-area-to-volumne ratio [1/m]
         */
        public void setDead100HrSAVRatio(Real savRatio) {
            this.dead100HrSAVRatio = Reals.convertTo(WildfireType.SAV_RATIO_SI, savRatio);
        }

        /**
         * @param savRatio Live herbatious fuel surface-area-to-volumne ratio [1/m]
         */
        public void setLiveHerbSAVRatio(Real savRatio) {
            this.liveHerbSAVRatio = Reals.convertTo(WildfireType.SAV_RATIO_SI, savRatio);
        }

        /**
         * @param savRatio Live woody fuel loading surface-area-to-volumne ratio [1/m]
         */
        public void setLiveWoodySAVRatio(Real savRatio) {
            this.liveWoodySAVRatio = Reals.convertTo(WildfireType.SAV_RATIO_SI, savRatio);
        }

        /**
         * @param depth fuel bed depth [m]
         */
        public void setFuelBedDepth(Real depth) {
            this.fuelBedDepth = Reals.convertTo(WildfireType.FUEL_DEPTH_SI, depth);
        }

        /**
         * @param percent moisture of extinction [%]
         */
        public void setMoistureOfExtinction(Real percent) {
            this.moistureOfExtinction = Reals.convertTo(WildfireType.MOISTURE_OF_EXTINCTION, percent);
        }

        /**
         * @param heat low heat content [kJ/kg]
         */
        public void setHeatContent(Real heat) {
            this.heatContent = Reals.convertTo(WildfireType.HEAT_CONTENT_SI, heat);
        }

        /**
         * @param isDynamic Fuel model type
         */
        public void setDynamic(boolean isDynamic) {
            this.dynamic = isDynamic;
        }

        /**
         * @param modelCode Fuel model code
         */
        public void setModelCode(String modelCode) {
            this.modelCode = modelCode;
        }

        /**
         * @param modelName fuel type complex
         */
        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        /**
         * Build a Fuel Model
         *
         * @return a fuel model ready for use
         * @throws IllegalStateException
         */
        public StdFuelModel build() {
            return new StdFuelModel(this);
        }
    }

    /**
     * Private constructor called by Builder
     *
     * @param builder
     */
    @SuppressWarnings("LeakingThisInConstructor")
    private StdFuelModel(Builder builder) {
        // Init members
        this.dead100HrFuelLoad = builder.dead100HrFuelLoad;
        this.dead10HrFuelLoad = builder.dead10HrFuelLoad;
        this.dead1HrFuelLoad = builder.dead1HrFuelLoad;
        this.liveHerbFuelLoad = builder.liveHerbFuelLoad;
        this.liveWoodyFuelLoad = builder.liveWoodyFuelLoad;
        this.dynamic = builder.dynamic;
        this.dead1HrSAVRatio = builder.dead1HrSAVRatio;
        this.dead10HrSAVRatio = builder.dead10HrSAVRatio;
        this.dead100HrSAVRatio = builder.dead100HrSAVRatio;
        this.liveHerbSAVRatio = builder.liveHerbSAVRatio;
        this.liveWoodySAVRatio = builder.liveWoodySAVRatio;
        this.fuelBedDepth = builder.fuelBedDepth;
        this.moistureOfExtinction = builder.moistureOfExtinction;
        this.lowHeatContent = builder.heatContent;
        this.modelNo = builder.modelNo;
        this.modelCode = builder.modelCode;
        this.modelName = builder.modelName;
        this.modelGroup = builder.modelGroup;
        // Store this object in the collection of available models...
        fuelModels.put(modelNo, this);
        // ... and update the list of available model groups
        fuelModelGroups.add(modelGroup);
    }

    /**
     * fuel model number
     */
    @Override
    public int getModelNo() {
        return this.modelNo;
    }

    /**
     * fuel model code
     */
    @Override
    public String getModelCode() {
        return this.modelCode;
    }

    /**
     * fuel type complex
     */
    @Override
    public String getModelName() {
        return this.modelName;
    }

    /**
     * fuel type group
     */
    @Override
    public String getModelGroup() {
        return this.modelGroup;
    }

    /**
     * 1 hour dead fuel loading [kg/m2]
     */
    @Override
    public Real getDead1HrFuelLoad() {
        return this.dead1HrFuelLoad;
    }

    /**
     * 10 hour dead fuel loading [kg/m2]
     */
    @Override
    public Real getDead10HrFuelLoad() {
        return this.dead10HrFuelLoad;
    }

    /**
     * 100 hour dead fuel loading [kg/m2]
     */
    @Override
    public Real getDead100HrFuelLoad() {
        return this.dead100HrFuelLoad;
    }

    /**
     * Live herbatious fuel loading [kg/m2]
     */
    @Override
    public Real getLiveHerbFuelLoad() {
        return this.liveHerbFuelLoad;
    }

    /**
     * Live woody fuel loading [kg/m2]
     */
    @Override
    public Real getLiveWoodyFuelLoad() {
        return this.liveWoodyFuelLoad;
    }

    /**
     * Fuel model type
     */
    @Override
    public boolean isDynamic() {
        return this.dynamic;
    }

    /**
     * The burnable state
     */
    @Override
    public boolean isBurnable() {
        return ((this.dead1HrSAVRatio.getValue()
                + this.dead10HrSAVRatio.getValue()
                + this.dead100HrSAVRatio.getValue()
                + this.liveHerbSAVRatio.getValue()
                + this.liveWoodySAVRatio.getValue()) == 0)
                || ((this.dead1HrFuelLoad.getValue()
                + this.dead10HrFuelLoad.getValue()
                + this.dead100HrFuelLoad.getValue()
                + this.liveHerbFuelLoad.getValue()
                + this.liveWoodyFuelLoad.getValue()) == 0);
    }

    /**
     * 1 hour dead fuel surface-area-to-volumne ratio [1/m]
     */
    @Override
    public Real getDead1HrSAVRatio() {
        return this.dead1HrSAVRatio;
    }

    /**
     * 10 hour dead fuel surface-area-to-volumne ratio [1/m]
     */
    @Override
    public Real getDead10HrSAVRatio() {
        return this.dead10HrSAVRatio;
    }

    /**
     * 100 hour dead fuel surface-area-to-volumne ratio [1/m]
     */
    @Override
    public Real getDead100HrSAVRatio() {
        return this.dead100HrSAVRatio;
    }

    /**
     * Live herbatious fuel surface-area-to-volumne ratio [1/m]
     */
    @Override
    public Real getLiveHerbSAVRatio() {
        return this.liveHerbSAVRatio;
    }

    /**
     * Live woody fuel loading surface-area-to-volumne ratio [1/m]
     */
    @Override
    public Real getLiveWoodySAVRatio() {
        return this.liveWoodySAVRatio;
    }

    /**
     * Fuel bed depth [meters]
     */
    @Override
    public Real getFuelBedDepth() {
        return this.fuelBedDepth;
    }

    /**
     * Moisture of extinction for dead fuels [percent]. This is the value of fuel moisture content
     * for which a fire would not spread.
     */
    @Override
    public Real getMoistureOfExtinction() {
        return this.moistureOfExtinction;
    }

    /**
     * Heat content [kJ/kg]
     */
    @Override
    public Real getLowHeatContent() {
        return this.lowHeatContent;
    }

    @Override
    public String toString() {
        return "[" + this.modelNo + "] " + this.modelCode + " - " + this.modelName;
    }

    public String toLongString() {
        return "[" + this.modelNo + "] " + this.modelCode + " - " + this.modelName
                + "\n 1Hr Load:   " + this.getDead1HrFuelLoad().toValueString()
                + "\n 10Hr Load:  " + this.getDead10HrFuelLoad().toValueString()
                + "\n 100Hr Load: " + this.getDead100HrFuelLoad().toValueString()
                + "\n Herb Load:  " + this.getLiveHerbFuelLoad().toValueString()
                + "\n Woody Load: " + this.getLiveWoodyFuelLoad().toValueString()
                + "\n 1Hr SAV:    " + this.getDead1HrSAVRatio().toValueString()
                + "\n 10Hr SAV:   " + this.getDead10HrSAVRatio().toValueString()
                + "\n 100Hr SAV:  " + this.getDead100HrSAVRatio().toValueString()
                + "\n Herb SAV:   " + this.getLiveHerbSAVRatio().toValueString()
                + "\n Woody SAV:  " + this.getLiveWoodySAVRatio().toValueString()
                + "\n Extinction: " + this.getMoistureOfExtinction().toValueString()
                + "\n Fuel Bed Depth:   " + this.getFuelBedDepth().toValueString()
                + "\n Low Heat Content: " + this.getLowHeatContent().toValueString();

    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + this.modelNo;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StdFuelModel other = (StdFuelModel) obj;
        return this.modelNo == other.modelNo;
    }

}
