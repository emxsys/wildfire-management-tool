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

import com.emxsys.visad.RealXmlAdapter;
import com.emxsys.visad.Reals;
import java.util.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import visad.Real;

/**
 * Class responsible for transforming fuel model inputs into SI units used by the Behave class. It
 * uses a builder pattern to create objects from either standard fuel model enums (see
 * StdFuelModeParams13 and StdFuelModelParams40) or via explicit parameters in the Builder's build
 * method (for custom models).
 *
 * @see StdFuelModelParams13
 * @see StdFuelModelParams40
 *
 * @author Bruce D. Schubert
 */
@XmlRootElement(name = "fuelModel")
@XmlType(propOrder
        = {"modelNo", "modelCode", "modelName", "modelGroup", "dynamic",
           "dead1HrFuelLoad", "dead10HrFuelLoad", "dead100HrFuelLoad", "liveHerbFuelLoad", "liveWoodyFuelLoad",
           "dead1HrSAVRatio", "dead10HrSAVRatio", "dead100HrSAVRatio", "liveHerbSAVRatio", "liveWoodySAVRatio",
           "fuelBedDepth", "moistureOfExtinction", "lowHeatContent", "burnable"
        })
public class BasicFuelModel implements FuelModel {

    /**
     * A non-burnable fuel model representing an INVALID_FUEL_MODEL model; Fuel model number: -1
     */
    public static final BasicFuelModel INVALID_FUEL_MODEL;
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
    private static Map<Integer, BasicFuelModel> fuelModels;
    private static Set<String> fuelModelGroups;

    static {
        fuelModels = new HashMap<>();
        fuelModelGroups = new HashSet<>();
        INVALID_FUEL_MODEL = new BasicFuelModel.Builder(-1, "INVALID", "Invalid Fuel Model",
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
    public static BasicFuelModel from(int fuelModelNo) {
        // First, look for an existing fuel model instance...
        BasicFuelModel fm = fuelModels.get(fuelModelNo);
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

    /**
     * A factory method used by Jersey/JAXB @FormParam.
     * @param fuelModelNoOrCode A model no (int) or model code (String).
     * @return A BasicFuelModel corresponding to the model no or model code; may return null.
     */
    public static BasicFuelModel fromString(String fuelModelNoOrCode) {
        return from(Integer.parseInt(fuelModelNoOrCode));
    }

    public static BasicFuelModel from(StdFuelModelParams13 params) {
        // First, look for an existing fuel model instance...
        BasicFuelModel fm = fuelModels.get(params.getModelNo());
        if (fm == null) {
            fm = new Builder(params).build();
        }
        return fm;
    }

    public static BasicFuelModel from(StdFuelModelParams40 params) {
        // First, look for an existing fuel model instance...
        BasicFuelModel fm = fuelModels.get(params.getModelNo());
        if (fm == null) {
            fm = new Builder(params).build();
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
        public BasicFuelModel build() {
            return new BasicFuelModel(this);
        }
    }

    ////////////////////
    // FuelModel members
    ////////////////////
    
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

    /**
     * Default constructor required for JavaBean support; creates an INVALID_FUEL_MODEL FuelModel.
     */
    public BasicFuelModel() {
        new Builder(-1, "INVALID", "Invalid Fuel Model",
                Builder.FUEL_LOAD_ZERO,
                Builder.SAV_RATIO_ZERO,
                Builder.FUEL_DEPTH_ZERO,
                Builder.EXT_MOISTURE_ZERO).build();
    }

    /**
     * Copy constructor performs a shallow copy of the immutable members.
     */
    public BasicFuelModel(FuelModel copy) {
        this.dead100HrFuelLoad = copy.getDead100HrFuelLoad();
        this.dead10HrFuelLoad = copy.getDead10HrFuelLoad();
        this.dead1HrFuelLoad = copy.getDead1HrFuelLoad();
        this.liveHerbFuelLoad = copy.getLiveHerbFuelLoad();
        this.liveWoodyFuelLoad = copy.getLiveWoodyFuelLoad();
        this.dynamic = copy.isDynamic();
        this.dead1HrSAVRatio = copy.getDead1HrSAVRatio();
        this.dead10HrSAVRatio = copy.getDead10HrSAVRatio();
        this.dead100HrSAVRatio = copy.getDead100HrSAVRatio();
        this.liveHerbSAVRatio = copy.getLiveHerbSAVRatio();
        this.liveWoodySAVRatio = copy.getLiveWoodySAVRatio();
        this.fuelBedDepth = copy.getFuelBedDepth();
        this.moistureOfExtinction = copy.getMoistureOfExtinction();
        this.lowHeatContent = copy.getLowHeatContent();
        this.modelNo = copy.getModelNo();
        this.modelCode = copy.getModelCode();
        this.modelName = copy.getModelName();
        this.modelGroup = copy.getModelGroup();
    }

    /**
     * Private constructor called by Builder
     *
     * @param builder
     */
    @SuppressWarnings("LeakingThisInConstructor")
    private BasicFuelModel(Builder builder) {
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
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    public int getModelNo() {
        return this.modelNo;
    }

    public void setModelNo(int modelNo) {
        this.modelNo = modelNo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    public String getModelCode() {
        return this.modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    public String getModelName() {
        return this.modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    public String getModelGroup() {
        return this.modelGroup;
    }

    public void setModelGroup(String modelGroup) {
        this.modelGroup = modelGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getDead1HrFuelLoad() {
        return this.dead1HrFuelLoad;
    }

    public void setDead1HrFuelLoad(Real dead1HrFuelLoad) {
        this.dead1HrFuelLoad = dead1HrFuelLoad;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getDead10HrFuelLoad() {
        return this.dead10HrFuelLoad;
    }

    public void setDead10HrFuelLoad(Real dead10HrFuelLoad) {
        this.dead10HrFuelLoad = dead10HrFuelLoad;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getDead100HrFuelLoad() {
        return this.dead100HrFuelLoad;
    }

    public void setDead100HrFuelLoad(Real dead100HrFuelLoad) {
        this.dead100HrFuelLoad = dead100HrFuelLoad;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getLiveHerbFuelLoad() {
        return this.liveHerbFuelLoad;
    }

    public void setLiveHerbFuelLoad(Real liveHerbFuelLoad) {
        this.liveHerbFuelLoad = liveHerbFuelLoad;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getLiveWoodyFuelLoad() {
        return this.liveWoodyFuelLoad;
    }

    public void setLiveWoodyFuelLoad(Real liveWoodyFuelLoad) {
        this.liveWoodyFuelLoad = liveWoodyFuelLoad;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    public boolean isDynamic() {
        return this.dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    public boolean isBurnable() {
        return (this.dead1HrSAVRatio.getValue() * this.dead1HrFuelLoad.getValue()
                + this.dead10HrSAVRatio.getValue() * this.dead10HrFuelLoad.getValue()
                + this.dead100HrSAVRatio.getValue() * this.dead100HrFuelLoad.getValue()
                + this.liveHerbSAVRatio.getValue() * this.liveHerbFuelLoad.getValue()
                + this.liveWoodySAVRatio.getValue() * this.liveWoodyFuelLoad.getValue()) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getDead1HrSAVRatio() {
        return this.dead1HrSAVRatio;
    }

    public void setDead1HrSAVRatio(Real dead1HrSAVRatio) {
        this.dead1HrSAVRatio = dead1HrSAVRatio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getDead10HrSAVRatio() {
        return this.dead10HrSAVRatio;
    }

    public void setDead10HrSAVRatio(Real dead10HrSAVRatio) {
        this.dead10HrSAVRatio = dead10HrSAVRatio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getDead100HrSAVRatio() {
        return this.dead100HrSAVRatio;
    }

    public void setDead100HrSAVRatio(Real dead100HrSAVRatio) {
        this.dead100HrSAVRatio = dead100HrSAVRatio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getLiveHerbSAVRatio() {
        return this.liveHerbSAVRatio;
    }

    public void setLiveHerbSAVRatio(Real liveHerbSAVRatio) {
        this.liveHerbSAVRatio = liveHerbSAVRatio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getLiveWoodySAVRatio() {
        return this.liveWoodySAVRatio;
    }

    public void setLiveWoodySAVRatio(Real liveWoodySAVRatio) {
        this.liveWoodySAVRatio = liveWoodySAVRatio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getFuelBedDepth() {
        return this.fuelBedDepth;
    }

    public void setFuelBedDepth(Real fuelBedDepth) {
        this.fuelBedDepth = fuelBedDepth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getMoistureOfExtinction() {
        return this.moistureOfExtinction;
    }

    public void setMoistureOfExtinction(Real moistureOfExtinction) {
        this.moistureOfExtinction = moistureOfExtinction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getLowHeatContent() {
        return this.lowHeatContent;
    }

    public void setLowHeatContent(Real lowHeatContent) {
        this.lowHeatContent = lowHeatContent;
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
        final BasicFuelModel other = (BasicFuelModel) obj;
        return this.modelNo == other.modelNo;
    }

}
