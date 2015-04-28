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

import com.emxsys.wildfire.api.FuelMoistureXmlAdapter;
import com.emxsys.wildfire.api.FuelModelXmlAdapter;
import com.emxsys.gis.api.Terrain;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.visad.FireUnit;
import static com.emxsys.visad.GeneralUnit.*;
import com.emxsys.visad.RealXmlAdapter;
import com.emxsys.visad.Reals;
import static com.emxsys.visad.Reals.convertTo;
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.BasicFuelModel;
import com.emxsys.wildfire.api.BasicFuelMoisture;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.WildfireType;
import static com.emxsys.wildfire.api.WildfireType.*;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.Exceptions;
import static visad.CommonUnit.radian;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 * The SurfaceFuel class represents a fuel complex, including the fuel moisture, fuel loading and
 * surface area to volume ratios, reaction intensities and moistures of extinction.
 *
 * @author Bruce Schubert
 */
@XmlRootElement(name = "surfacefuel")
@XmlType(propOrder
        = {
            "fuelModel",
            "fuelMoisture",
            "fuelTemperature",
            "meanBulkDensity",
            "fuelParticleDensity",
            "meanPackingRatio",
            "optimalPackingRatio",
            "relativePackingRatio",
            "characteristicSAV",
            "liveMoistureOfExt",
            "mineralDamping",
            "moistureDamping",
            "lowHeatContent",
            "reactionVelocity",
            "reactionIntensity",
            "flameResidenceTime",
            "heatRelease",
            "propagatingFluxRatio",
            "heatSink",
            "burnable",})
public class SurfaceFuel {

    public static SurfaceFuel from(FuelModel model, FuelMoisture moisture) {
        return from(model, moisture, new Real(WildfireType.FUEL_TEMP_F));
    }

    /**
     * Creates a SurfaceFuel object. If the FuelModal is dynamic, then the herbaceous fuels are
     * cured based on the FuelMoisture and the cured fuels are transfered to the dead 1 hour
     * category.
     *
     * @param model Either a static or dynamic fuel model.
     * @param moisture
     * @param fuelTemp
     * @return A new SurfaceFuel object.
     */
    public static SurfaceFuel from(FuelModel model, FuelMoisture moisture, Real fuelTemp) {
        try {

            return new SurfaceFuel(model, moisture, fuelTemp);

        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, "Error in fuel bed", ex);
            Exceptions.printStackTrace(ex);
            return new SurfaceFuel();
        }

    }

    /**
     * Computes the fuel temperature from the given environmental parameters.
     *
     * @param fuelModel The fuel model containing the vegetation height.
     * @param sun The current sunlight prevailing upon the fuel.
     * @param wx The current weather acting on the fuel (wind, air temperature and sky cover).
     * @param terrain The slope, aspect and elevation at the fuel.
     * @param shaded Set true if the fuel is currently shaded (by terrain or night).
     *
     * @return The fuel temperature [Fahrenheit]
     *
     * @see Rothermel
     */
    public static Real computeFuelTemperature(FuelModel fuelModel,
                                              Sunlight sun, Weather wx,
                                              Terrain terrain, boolean shaded) {
        try {
            // Vegetation height [feet]
            double h_v = fuelModel.getFuelBedDepth().getValue(foot);

            // Weather Values
            double W = wx.getWindSpeed().getValue(mph); // 20' wind speed
            double S_c = shaded ? 100. : wx.getCloudCover().getValue(); // [percent]
            double T_a = wx.getAirTemperature().getValue(degF);

            // Atmospheric transparency
            // p    Qualitative description
            // 0.8  exceptionally clear atmosphere
            // 0.75 average clear forest atmosphere
            // 0.7  moderate forest (blue) haze
            // 0.6  dense haze
            double p = 0.75;

            // Terrain Values
            double E = terrain.getElevationMeters();
            double slope = terrain.getSlope().getValue(radian);
            double aspect = terrain.getAspect().getValue(radian);

            // Calculate solar irradiance
            double A = sun.getAltitudeAngle().getValue(radian);
            double Z = sun.getAzimuthAngle().getValue(radian);
            double M = Rothermel.calcOpticalAirMass(A, E);
            double I_a = Rothermel.calcAttenuatedIrradiance(M, S_c, p);
            double I = Rothermel.calcIrradianceOnASlope(slope, aspect, A, Z, I_a);

            // Calculate fuel temperature and humidity immediatly adjacent to fuel
            double U_h = Rothermel.calcWindSpeedNearFuel(W, h_v);
            double T_f = Rothermel.calcFuelTemp(I, T_a, U_h); // fahrenheit

            return new Real(WildfireType.FUEL_TEMP_F, T_f);

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Computes the fine fuel moisture from the given environmental parameters. Dead 1-hour fuel
     * moisture is computed using an "instantaneous" wetting or drying computation, versus the
     * traditional 1-hour time lag formula. This approach is computationally more performant.
     *
     * Per K. Anderson, "This approach produces diurnal variations closer to expected values and
     * when used in fire-growth modeling, over-predictions are reduced by 30%."
     * <p>
     * References:
     * <ul>
     * <li><a name="bib_1002"></a>Anderson, K., 2009, A Comparison of Hourly Fire Fuel Moisture Code
     * Calculations within Canada, Canadian Forest Service
     * </ul>
     *
     * @param fuelTemperature Fuel surface temperature.
     * @param airTemperature General air temperature.
     * @param relHumidity General relative humidity.
     * @param initialDead1HrFuelMoisture Previous hour's fuel moisture - determines a wetting or
     * drying trend.
     *
     * @return The fine fuel moisture (dead 1 hour fuel moisture) [%]
     *
     * @see Rothermel
     */
    public static Real computeFineFuelMoisture(Real fuelTemperature,
                                               Real airTemperature,
                                               Real relHumidity,
                                               Real initialDead1HrFuelMoisture) {
        try {
            // Weather inputs
            double Ta_f = airTemperature.getValue(degF);
            double Ha = relHumidity.getValue();    // %

            // Calculate humidity immediatly adjacent to fuel
            double Tf_f = fuelTemperature.getValue(degF); // fahrenheit
            double Hf = Rothermel.calcRelativeHumidityNearFuel(Ha, Tf_f, Ta_f); // humidity at fuel

            // Compute fine dead fuel moisture... requires metric values;
            // temp and humidity have been adjusted for solar preheating.
            double Tf_c = fuelTemperature.getValue(degC); // celsius
            double m_0 = initialDead1HrFuelMoisture.getValue();
            double m = Rothermel.calcFineDeadFuelMoisture(m_0, Tf_c, Hf);   // instantaneous wetting/drying

            // Round the fuel moisture to reduce the number entries in cache
            return new Real(FUEL_MOISTURE_1H, m);//MathUtil.round(m, m < 2 ? 1 : 2));

        } catch (VisADException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Computes the cured portion of live herbaceous fuels.
     *
     * @param fuelMoisture Provides the live fuel moisture.
     * @return The cured percentage [%].
     */
    public static double calcHerbaceousCuring(FuelMoisture fuelMoisture) {
        double herbMoisture = fuelMoisture.getLiveHerbFuelMoisture().getValue();
        if (herbMoisture == 0) {
            return 0;
        }
        double curing;
        if (herbMoisture >= 120.) {
            curing = 0.0;   // fully green
        } else if (herbMoisture <= 30) {
            curing = 1.0;   // fully cured
        } else {
            // interpolate between 30 and 120 percents
            curing = 1.0 - ((herbMoisture - 30.) / 90.);
        }
        return curing;
    }

    // In all fire behavior simulation systems that use the Rothermel model, total mineral content
    // is 5.55 percent, effective (silica-free) mineral content is 1.00 percent, and oven-dry fuel
    // particle density is 513 kg/m3 (32 lb/ft3).
    /** Total effective (silica-free) mineral content: 1%. Rothermel 1972 eq.(63) */
    static final double s_e = 1.0;
    /** Total mineral content: 5.5% - Albini's constant. The non-combustible fuel component. */
    static final double s_t = 5.5;
    /** Oven-dry fuel-particle density: 32 [lbs/ft3] - Albini's constant */
    static final double rho_p = 32.0;

    /**
     * The data implementation tuple of type WildfireType.FUEL_BED.
     */
    //private RealTuple tuple;
    // Inputs parameters
    FuelModel fuelModel;
    FuelMoisture fuelMoisture;
    Real fuelTemperature;

    // Input values
    double[] sv;        // [ft2/ft3]      
    double[] sv_dead;   // [ft2/ft3]      
    double[] sv_live;   // [ft2/ft3]
    double[] w0;        // [lb/ft2]
    double[] w0_dead;   // [lb/ft2]
    double[] w0_live;   // [lb/ft2]
    double[] Mf;        // [%]
    double[] M_dead;
    double[] M_live;

    // Precomputed values from inputs
    double w0_total;            // total loading [lb/ft2]
    double w0_total_dead;       // total dead loading [lb/ft2]
    double w0_total_live;       // total live loading [lb/ft2]
    double sv_total;            // total SAV [ft2/ft3]
    double sv_total_dead;       // total dead SAV [ft2/ft3]
    double sv_total_live;       // total live SAV [ft2/ft3]
    boolean burnable = false;
    boolean initialized = false;    // synchronizer

    // Intermediate values
    double W_prime;                 // computed in Mx_live
    double wn_dead, wn_live;        // computed in eta_M
    double Mf_dead_t, Mf_live_t;        // computed in eta_M
    double eta_M_dead, eta_M_live;  // computed in eta_M for I_r
    double I_r_dead, I_r_live;      // computed in I_r

    // Outputs
    Real curedDeadHerbFuelLoad;
    Real curedLiveHerbFuelLoad;
    Real characteristicSAV;
    Real meanBulkDensity;
    Real liveMx;
    Real moistureDamping;
    Real meanPackingRatio;
    Real optimalPackingRatio;
    Real reactionVelocity;
    Real reactionIntensity;

    private static final Logger logger = Logger.getLogger(SurfaceFuel.class.getName());

    /**
     * Construct a new SurfaceFuel object with "missing" values.
     */
    public SurfaceFuel() {
        //this.tuple = new RealTuple(WildfireType.FUEL_BED);
        this.fuelModel = BasicFuelModel.INVALID_FUEL_MODEL;
        this.fuelMoisture = BasicFuelMoisture.INVALID_FUEL_MOISTURE;
        this.burnable = false;
        this.initialized = false;
    }

    /**
     * Private constructor creates an instance initialized with the given values.
     */
    private SurfaceFuel(FuelModel model, FuelMoisture moisture, Real fuelTemp)
            throws VisADException, RemoteException {
        this.fuelModel = model;
        this.fuelMoisture = moisture;
        this.fuelTemperature = Reals.convertTo(FUEL_TEMP_F, fuelTemp);
        //this.tuple = new RealTuple(WildfireType.FUEL_BED, tuple.getRealComponents(), null);
        initialize();
    }

    /**
     * Initializer from input parameters.
     */
    private void initialize() {

        try {
            // Prerequisites
            if (this.fuelModel.equals(BasicFuelModel.INVALID_FUEL_MODEL)
                    || this.fuelMoisture.equals(BasicFuelMoisture.INVALID_FUEL_MOISTURE)) {
                this.initialized = false;
                return;
            }
            // Transfer cured herbaceous fuel into the dead herbaceous fuel load
            double curing = fuelModel.isDynamic() ? calcHerbaceousCuring(fuelMoisture) : 0;
            double liveHerbLoad = fuelModel.getLiveHerbFuelLoad().getValue(FireUnit.lb_ft2);
            double deadHerbLoad = 0;
            if (liveHerbLoad > 0) {
                deadHerbLoad = liveHerbLoad * curing;
                liveHerbLoad -= deadHerbLoad;
            }
            curedDeadHerbFuelLoad = new Real(FUEL_LOAD, deadHerbLoad);
            curedLiveHerbFuelLoad = new Real(FUEL_LOAD, liveHerbLoad);

            // Compute adjusted fine fuels (mix dead herbaceous with dead 1hr)
            double w0_d = getDead1HrFuelLoad().getValue();  //values[LOAD_DEAD_1H_INDEX];
            double w0_h = getDeadHerbFuelLoad().getValue(); //values[LOAD_DEAD_HERB_INDEX];
            double sv_d = getDead1HrSAVRatio().getValue();  //values[SAV_DEAD_1H_INDEX];
            double sv_h = getDeadHerbSAVRatio().getValue(); //values[SAV_DEAD_HERB_INDEX];
            double deadFineFuelLoad = w0_d + w0_h;
            double deadFineSAV = ((sv_d * sv_d * w0_d) + (sv_h * sv_h * w0_h))
                    / (sv_d * w0_d + sv_h * w0_h);

            // Populate arrays used in formulas that sum fuel particle components
            this.sv = new double[]{
                deadFineSAV,
                getDead10HrSAVRatio().getValue(), //values[SAV_DEAD_10H_INDEX],
                getDead100HrSAVRatio().getValue(), //values[SAV_DEAD_100H_INDEX],
                getLiveHerbSAVRatio().getValue(), //values[SAV_LIVE_HERB_INDEX],
                getLiveWoodySAVRatio().getValue(), //values[SAV_LIVE_WOODY_INDEX]
            };
            this.sv_dead = new double[]{
                deadFineSAV,
                getDead10HrSAVRatio().getValue(), //values[SAV_DEAD_10H_INDEX],
                getDead100HrSAVRatio().getValue(), //values[SAV_DEAD_100H_INDEX]
            };
            this.sv_live = new double[]{
                getLiveHerbSAVRatio().getValue(), //values[SAV_LIVE_HERB_INDEX],
                getLiveWoodySAVRatio().getValue(), //values[SAV_LIVE_WOODY_INDEX]
            };

            this.w0 = new double[]{
                deadFineFuelLoad,
                getDead10HrFuelLoad().getValue(), //values[LOAD_DEAD_10H_INDEX],
                getDead100HrFuelLoad().getValue(), //values[LOAD_DEAD_100H_INDEX],
                getLiveHerbFuelLoad().getValue(), //values[LOAD_LIVE_HERB_INDEX],
                getLiveWoodyFuelLoad().getValue(), //values[LOAD_LIVE_WOODY_INDEX]
            };
            this.w0_dead = new double[]{
                deadFineFuelLoad,
                getDead10HrFuelLoad().getValue(), //values[LOAD_DEAD_10H_INDEX],
                getDead100HrFuelLoad().getValue(), //values[LOAD_DEAD_100H_INDEX],
            };
            this.w0_live = new double[]{
                getLiveHerbFuelLoad().getValue(), //values[LOAD_LIVE_HERB_INDEX],
                getLiveWoodyFuelLoad().getValue(), //values[LOAD_LIVE_WOODY_INDEX]
            };

            this.Mf = new double[]{
                fuelMoisture.getDead1HrFuelMoisture().getValue(), // dead herbaceous
                fuelMoisture.getDead10HrFuelMoisture().getValue(),
                fuelMoisture.getDead100HrFuelMoisture().getValue(),
                fuelMoisture.getLiveHerbFuelMoisture().getValue(),
                fuelMoisture.getLiveWoodyFuelMoisture().getValue()};
            this.M_dead = new double[]{
                fuelMoisture.getDead1HrFuelMoisture().getValue(), // dead herbaceous
                fuelMoisture.getDead10HrFuelMoisture().getValue(),
                fuelMoisture.getDead100HrFuelMoisture().getValue()};
            this.M_live = new double[]{
                fuelMoisture.getLiveHerbFuelMoisture().getValue(),
                fuelMoisture.getLiveWoodyFuelMoisture().getValue()};

            // Precompute total weight [lb/ft2] and total SAV [ft2/ft3]
            for (int i = 0; i < w0_dead.length; i++) {
                w0_total += w0_dead[i];
                sv_total += sv_dead[i];
                w0_total_dead += w0_dead[i];
                sv_total_dead += sv_dead[i];
            }
            for (int i = 0; i < w0_live.length; i++) {
                w0_total += w0_live[i];
                sv_total += sv_live[i];
                w0_total_live += w0_live[i];
                sv_total_live += sv_live[i];
            }

            burnable = (w0_total > 0 && sv_total > 0) && fuelModel.isBurnable();
            initialized = true;
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            initialized = false;
        }
    }

    /**
     * Gets the WildfireType.FUEL_BED implementation tuple.
     * @return A WildfireType.FUEL_BED type RealTuple.
     */
    public RealTuple getTuple() {
        try {
            // Build a tuple with the proper UOMs
            RealTuple tuple = new RealTuple(FUEL_BED, new Real[]{
                getDeadHerbFuelLoad(),
                getDead1HrFuelLoad(),
                getDead10HrFuelLoad(),
                getDead100HrFuelLoad(),
                getLiveHerbFuelLoad(),
                getLiveWoodyFuelLoad(),
                getDeadHerbSAVRatio(), // dead SAV same as live SAV
                getDead1HrSAVRatio(),
                getDead10HrSAVRatio(),
                getDead100HrSAVRatio(),
                getLiveHerbSAVRatio(),
                getLiveWoodySAVRatio(),
                getFuelBedDepth(),
                getMoistureOfExtinction()
            }, null);
            return tuple;
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            return new RealTuple(FUEL_BED);
        }
    }

    @XmlElement
    @XmlJavaTypeAdapter(FuelModelXmlAdapter.class)
    public FuelModel getFuelModel() {
        return fuelModel;
    }

    public void setFuelModel(FuelModel model) {
        synchronized (this) {
            this.fuelModel = model;
            initialize();
        }

    }

    @XmlElement
    @XmlJavaTypeAdapter(FuelMoistureXmlAdapter.class)
    public FuelMoisture getFuelMoisture() {
        return fuelMoisture;
    }

    public void setFuelMoisture(FuelMoisture fuelMoisture) {
        synchronized (this) {
            this.fuelMoisture = fuelMoisture;
            initialize();
        }
    }

    /**
     * Gets the mean bulk density (fuel-bed weight per unit volume).
     *
     * @return rho_b [lbs/ft3]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getMeanBulkDensity() {
        if (!isValid()) {
            return new Real(RHO_B, 0);
        }
        if (this.meanBulkDensity == null) {
            double rho_b = Rothermel.meanBulkDensity(
                    w0, getFuelBedDepth().getValue());
            this.meanBulkDensity = new Real(RHO_B, rho_b);
        }
        return this.meanBulkDensity;
    }

    /**
     * Gets the oven-dry fuel-particle density.
     *
     * @return rho_p [lbs/ft3]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getFuelParticleDensity() {
        return new Real(RHO_P, 32); // Albini's constant.
    }

    /**
     * Gets the mean packing ratio for the fuel.
     *
     * The compactness of the fuel bed is defined by the packing ratio, which is defined as the
     * fraction of the fuel array volume that is occupied by the fuel. Rothermel 1972: eq. (74)
     *
     * @return beta [dimensionless]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getMeanPackingRatio() {
        if (!isValid()) {
            return new Real(BETA, 0);
        } else if (this.meanPackingRatio == null) {
            double beta = Rothermel.meanPackingRatio(
                    getMeanBulkDensity().getValue(),
                    getFuelParticleDensity().getValue());
            this.meanPackingRatio = new Real(BETA, beta);
        }
        return this.meanPackingRatio;
    }

    /**
     * Gets the optimal packing ratio for the fuel: beta_opt.
     *
     * @return beta_opt [dimensionless]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getOptimalPackingRatio() {
        if (!isValid()) {
            return new Real(BETA_OPT, 0);
        }
        double beta_opt = Rothermel.optimalPackingRatio(
                getCharacteristicSAV().getValue());
        return new Real(BETA_OPT, beta_opt);
    }

    /**
     * Gets the relative packing ratio for the fuel (beta/beta_opt)
     *
     * @return beta_ratio [dimensionless]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getRelativePackingRatio() {
        if (!isValid()) {
            return new Real(BETA_RATIO, 0);
        }
        double beta = getMeanPackingRatio().getValue();
        double beta_opt = getOptimalPackingRatio().getValue();
        double beta_ratio = beta / beta_opt;
        return new Real(BETA_RATIO, beta_ratio);
    }

    /**
     * Gets the characteristic surface-area-to-volume ratio for the fuel complex.
     *
     * @return sigma [ft2/ft3]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getCharacteristicSAV() {
        if (!isValid()) {
            return new Real(SIGMA, 0);
        }
        if (this.characteristicSAV == null) {
            double sigma = Rothermel.characteristicSAV(sv, w0);
            this.characteristicSAV = new Real(SIGMA, sigma);
        }
        return characteristicSAV;
    }

    /**
     * Gets the live moisture of extinction [%]. Using Albini (1976): page 89
     * @return Mx_live
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getLiveMoistureOfExt() {
        if (!isValid()) {
            return new Real(MX_LIVE, 0);
        }
        if (this.liveMx == null) {
            double sumDead = 0;
            double sumLive = 0;
            double sumDeadMoisture = 0;
            double sumLiveMoisture = 0;

            // W' = SUM(w0_d*exp(-138/sv_d*)/SUM(w0_l*exp(-500/sv_l*)
            for (int i = 0; i < sv_dead.length; i++) {
                if (sv_dead[i] > 0) {
                    double weighting = w0_dead[i] * Math.exp(-138 / sv_dead[i]);
                    sumDead += weighting;
                    sumDeadMoisture += M_dead[i] * weighting;
                }
            }
            for (int i = 0; i < sv_live.length; i++) {
                if (sv_live[i] > 0) {
                    double weighting = w0_live[i] * Math.exp(-500 / sv_live[i]);
                    sumLive += weighting;
                    sumLiveMoisture += M_live[i] * weighting;
                }
            }
            W_prime = (sumLive > 0) ? (sumDead / sumLive) : 0;

            // Albini (1976): page 89:
            //  (Mx)_living = 2.9W'(1-(M'_f)_dead/(Mx)_dead) - 0.226 (min = Mx_dead)
            Mf_dead_t = (sumDead > 0) ? (sumDeadMoisture / sumDead) : 0;
            Mf_live_t = (sumLive > 0) ? (sumLiveMoisture / sumLive) : 0;
            double Mx_dead = fuelModel.getMoistureOfExtinction().getValue();
            double Mx_live = Math.max(Mx_dead,
                    (2.9 * W_prime * (1 - Mf_dead_t / Mx_dead) - 0.226) * 100);

            this.liveMx = new Real(MX_LIVE, Mx_live);
        }
        return this.liveMx;
    }

    /**
     * Gets the mineral damping coefficient.
     * @return eta_s
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getMineralDamping() {
        // Rothermel 1972: eq. (62)
        // s_e = silica-free ash content of fuel - Albini's constant
        double eta_s = 0.174 * Math.pow(s_e / 100., -0.19);
        return new Real(ETA_S, eta_s);

    }

    /**
     * Calculates the moisture damping coefficients for dead and live fuel.
     *
     * The moisture damping coefficient accounts for the decrease in caused by the combustion of
     * fuels that initially contained moisture. The greater the difference between the actual
     * moisture content and the moisture of extinction, the smaller the moisture damping coefficient
     * and therefore the greater the spread rate.
     *
     * @return eta_M
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getMoistureDamping() {
        if (!isValid()) {
            return new Real(ETA_M, 0);
        }
        if (this.moistureDamping == null) {
            // Intermediates
            double swm_dead = 0; // swm = (sv_total * w0_total * m)
            double swm_live = 0;
            double sw_d = 0;
            double sw_l = 0;
            double sw2_d = 0;

            for (int i = 0; i < w0_dead.length; i++) {
                // XXX Special case for Fuel Model SH9 net fine dead fuel loading.
                if (fuelModel.getModelCode().equalsIgnoreCase("SH9") && i == 0) {
                    // This dynamic fuel model uses a different algorithm for 
                    // mixing in the dead herbaceous with the dead 1hr fuels.
                    double w0_d = getDead1HrFuelLoad().getValue();
                    double w0_h = getDeadHerbFuelLoad().getValue();
                    double sv_d = getDead1HrSAVRatio().getValue();
                    double sv_h = getDeadHerbSAVRatio().getValue();
                    double M_d = getDead1HrFuelMoisture().getValue();
                    sw_d += (sv_d * w0_d) + (sv_h * w0_h);
                    sw2_d += (sv_d * w0_d * w0_d) + (sv_h * w0_h * w0_h);
                    swm_dead += (sv_d * w0_d * M_d) + (sv_h * w0_h * M_d);
                } else {
                    // Using fine dead fuel loading adjustment performed in constructor
                    double s = sv_dead[i];
                    double w = w0_dead[i];
                    double m = M_dead[i];
                    sw_d += s * w;
                    sw2_d += s * w * w;
                    swm_dead += s * w * m;
                }
            }
            for (int i = 0; i < w0_live.length; i++) {
                double s = sv_live[i];
                double w = w0_live[i];
                double m = M_live[i];
                sw_l += s * w;
                swm_live += s * w * m;
            }
            // Net fuel loading: The dry-weight loading of any particular fuel element, w0, 
            // includes the non-combustible mineral fraction, s_t. The loading of combustible 
            // fuel is w0(l - s_t). Albini 1976: pg. (88)
            // XXX - Two different algorithms for live and dead are needed to match the BehavePlus outputs! 
            // XXX - Why? Could it be because Mx_live and Mx_dead are different somehow? Computed differently?
            if (sw_d > 0) {
                wn_dead = ((1 - s_t / 100) * sw2_d) / sw_d;
                //wn_dead = (1 - s_t / 100) * w0_total_dead;    -- doesn't work correctly
            }
            if (sw_l > 0) {
                wn_live = (1 - s_t / 100) * w0_total_live;
            }

            // Moisture ratios: (Mf/Mx)
            // Rothermel 1972: eq. (29) and (65) & (66). 
            double Mx_live = getLiveMoistureOfExt().getValue();
            double Mx_dead = getDeadMoistureOfExt().getValue();
            double ratio_dead = 0;
            if (sw_d > 0) {
                ratio_dead = swm_dead / (sw_d * Mx_dead);
            }
            double ratio_live = 0;
            if (sw_l > 0) {
                ratio_live = swm_live / (sw_l * Mx_live);
            }

            // Moisture coefficients: 1 - 2.59 * (Mf/Mx) + 5.11 * (Mf/Mx)^2 - 3.52 * (Mf/Mx)^3
            eta_M_dead = wn_dead * Math.max(0,
                    1 - 2.59 * ratio_dead
                    + 5.11 * Math.pow(ratio_dead, 2)
                    - 3.52 * Math.pow(ratio_dead, 3));
            eta_M_live = wn_live * Math.max(0,
                    1 - 2.59 * ratio_live
                    + 5.11 * Math.pow(ratio_live, 2)
                    - 3.52 * Math.pow(ratio_live, 3));

            double eta_M = (eta_M_dead) + (eta_M_live);

            this.moistureDamping = new Real(ETA_M, eta_M);
        }
        return this.moistureDamping;
    }

    /**
     * Gets the fuel's low heat content (Albini's constant for forest fuels) [BTU/lb].
     *
     * @return 8,000 BTU/lb
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getLowHeatContent() {
        return convertTo(HEAT_CONTENT_US, this.fuelModel.getLowHeatContent());
    }

    /**
     * Gets the potential reaction velocity [1/min].
     *
     * @return gamma [1/min]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getReactionVelocity() {
        if (!isValid()) {
            return new Real(GAMMA, 0);
        }
        if (this.reactionVelocity == null) {
            double gamma = Rothermel.reactionVelocity(
                    getCharacteristicSAV().getValue(),
                    getRelativePackingRatio().getValue());
            this.reactionVelocity = new Real(GAMMA, gamma);
        }
        return reactionVelocity;
    }

    /**
     * Gets the reaction intensity [BTU/ft2/min]. The rate of heat release, per unit area of the
     * flaming fire front, expressed as heat energy/area/time, such as Btu/square foot/minute, or
     * Kcal/square meter/second.
     *
     * @return I_r.
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getReactionIntensity() {
        if (!isValid()) {
            return new Real(I_R, 0);
        }
        if (reactionIntensity == null) {
            // Rothermel 1972: eq. (58), (59) thru (60)
            double gamma = getReactionVelocity().getValue();
            double heat = getLowHeatContent().getValue();
            double eta_M = getMoistureDamping().getValue();
            double eta_s = getMineralDamping().getValue();
            //double I_r = gamma * heat * eta_M * eta_s;
            I_r_dead = Rothermel.reactionIntensity(gamma, heat, eta_M_dead, eta_s);
            I_r_live = Rothermel.reactionIntensity(gamma, heat, eta_M_live, eta_s);
            double I_r = I_r_dead + I_r_live;

            reactionIntensity = new Real(I_R, I_r);
        }
        return reactionIntensity;
    }

    /**
     * Gets the flame residence time.
     *
     * @return tau [min]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getFlameResidenceTime() {
        if (!isValid()) {
            return new Real(0);
        }
        double tau = Rothermel.flameResidenceTime(getCharacteristicSAV().getValue());
        return new Real(tau);
    }

    /**
     * Gets the heat release per unit area.
     *
     * @return hpa [Btu/ft2]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getHeatRelease() {
        if (!isValid()) {
            return new Real(0);
        }
        double hpa = Rothermel.heatRelease(
                getReactionIntensity().getValue(),
                getFlameResidenceTime().getValue());
        return new Real(hpa);
    }

    /**
     * Gets the propagating flux ratio.
     *
     * The no-wind propagating flux ratio is a function of the mean packing ratio (beta) and the
     * characteristic SAV ratio (sigma).
     *
     * @return xi
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getPropagatingFluxRatio() {
        if (!isValid()) {
            return new Real(0);
        }
        double xi = Rothermel.propagatingFluxRatio(
                getCharacteristicSAV().getValue(),
                getMeanPackingRatio().getValue());
        return new Real(xi);
    }

    /**
     * Gets the heat sink term.
     *
     * Rothermel (1972) atypically used the term heat sink to represent the heat per unit fuel-bed
     * volume required for ignition.
     *
     * Typically, the heat required for ignition is represented by its contributing components: the
     * effective mass (mass density of fuel to be heated to ignition) and the heat required to raise
     * the temperature of that mass to ignition. See http://www.firewords.net
     *
     * Rothermel (1972): eq. (77) + (78)
     * @return [Btu/ft3]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getHeatSink() {
        if (!isValid()) {
            return new Real(0);
        }
        // Effective heating number for each particle: epsilon 
        double[] epsilon = new double[sv.length];
        for (int i = 0; i < epsilon.length; i++) {
            epsilon[i] = Rothermel.effectiveHeatingNumber(sv[i]);
        }

        // Heat of Preignition for each particle: Q_ig
        double[] Q_ig = new double[Mf.length];
        for (int i = 0; i < Q_ig.length; i++) {
            Q_ig[i] = Rothermel.heatOfPreignition(Mf[i]);
        }

        // sv * w0 weighting for each particle: sw
        double[] sw = new double[sv.length];
        for (int i = 0; i < sw.length; i++) {
            sw[i] = sv[i] * w0[i];
        }

        double rho_b = getMeanBulkDensity().getValue();

        // Heat Sink
        double hsk = Rothermel.heatSink(Q_ig, epsilon, sw, rho_b);

        return new Real(hsk);
    }

    /**
     * Gets the 1 hour dead fuel loading.
     * @return [ton/acre]
     */
    public Real getDead1HrFuelLoad() {
        return convertTo(FUELBED_LOAD_DEAD_1H, fuelModel.getDead1HrFuelLoad());
    }

    /**
     * Gets the 10 hour dead fuel loading.
     * @return [ton/acre]
     */
    public Real getDead10HrFuelLoad() {
        return convertTo(FUELBED_LOAD_DEAD_10H, fuelModel.getDead10HrFuelLoad());
    }

    /**
     * Gets the 100 hour dead fuel loading.
     * @return [ton/acre]
     */
    public Real getDead100HrFuelLoad() {
        return convertTo(FUELBED_LOAD_DEAD_100H, fuelModel.getDead100HrFuelLoad());
    }

    /**
     * Gets the cured dead herbaceous fuel loading.
     * @return [ton/acre]
     */
    public Real getDeadHerbFuelLoad() {
        return convertTo(FUELBED_LOAD_DEAD_HERB, this.curedDeadHerbFuelLoad);
    }

    /**
     * Gets the cured live herbaceous fuel loading.
     * @return [ton/acre]
     */
    public Real getLiveHerbFuelLoad() {
        return convertTo(FUELBED_LOAD_LIVE_HERB, this.curedLiveHerbFuelLoad);
    }

    /**
     * Gets the live woody fuel loading.
     * @return [ton/acre]
     */
    public Real getLiveWoodyFuelLoad() {
        return convertTo(FUELBED_LOAD_LIVE_WOODY, fuelModel.getLiveWoodyFuelLoad());
    }

    /**
     * Gets the 1 hour dead fuel surface-area-to-volume ratio.
     * @return [ft2/ft3]
     */
    public Real getDead1HrSAVRatio() {
        return convertTo(FUELBED_SAV_DEAD_1H, fuelModel.getDead1HrSAVRatio());
    }

    /**
     * Gets the 10 hour dead fuel surface-area-to-volume ratio.
     * @return [ft2/ft3]
     */
    public Real getDead10HrSAVRatio() {
        return convertTo(FUELBED_SAV_DEAD_10H, fuelModel.getDead10HrSAVRatio());
    }

    /**
     * Gets the 100 hour dead fuel surface-area-to-volume ratio.
     * @return [ft2/ft3]
     */
    public Real getDead100HrSAVRatio() {
        return convertTo(FUELBED_SAV_DEAD_100H, fuelModel.getDead100HrSAVRatio());
    }

    /**
     * Gets the dead herbaceous fuel surface-area-to-volume ratio.
     * @return [ft2/ft3]
     */
    public Real getDeadHerbSAVRatio() {
        return convertTo(FUELBED_SAV_DEAD_HERB, fuelModel.getLiveHerbSAVRatio()); // dead SAV ratio is same as live SAV ratio
    }

    /**
     * Gets the live herbaceous fuel surface-area-to-volume ratio.
     * @return [ft2/ft3]
     */
    public Real getLiveHerbSAVRatio() {
        return convertTo(FUELBED_SAV_LIVE_HERB, fuelModel.getLiveHerbSAVRatio());
    }

    /**
     * Gets the live woody fuel loading surface-area-to-volume ratio.
     * @return [ft2/ft3]
     */
    public Real getLiveWoodySAVRatio() {
        return convertTo(FUELBED_SAV_LIVE_WOODY, fuelModel.getLiveWoodySAVRatio());
    }

    /**
     * Gets the fuel bed depth.
     * @return [foot]
     */
    public Real getFuelBedDepth() {
        return convertTo(FUEL_BED_DEPTH, fuelModel.getFuelBedDepth());
    }

    /**
     * Gets the moisture of extinction.
     * @return [percent]
     */
    public Real getMoistureOfExtinction() {
        return convertTo(MX_DEAD, fuelModel.getMoistureOfExtinction());
    }

    public Real getDead1HrFuelMoisture() {
        return fuelMoisture.getDead1HrFuelMoisture();
    }

    public Real getDead10HrFuelMoisture() {
        return fuelMoisture.getDead10HrFuelMoisture();
    }

    public Real getDead100HrFuelMoisture() {
        return fuelMoisture.getDead100HrFuelMoisture();
    }

    public Real getDeadHerbFuelMoisture() {
        // dead herbaceous fuel moisture is same as dead 1 hour.
        return fuelMoisture.getDead1HrFuelMoisture();
    }

    public Real getLiveHerbFuelMoisture() {
        return fuelMoisture.getLiveHerbFuelMoisture();
    }

    public Real getLiveWoodyFuelMoisture() {
        return fuelMoisture.getLiveWoodyFuelMoisture();
    }

    /**
     * Gets the dead moisture of extinction percent. Fuel moisture content is expressed as the mass
     * of water as a percentage of oven-dry mass.
     *
     * This value is used as a way to predict the effect of moisture content on fire behavior
     * (through the moisture damping coefficient). The greater the difference between actual
     * moisture content and the moisture of extinction, the smaller the moisture damping coefficient
     * and therefore the greater the spread rate.
     * @return [percent]
     */
    public Real getDeadMoistureOfExt() {
        return convertTo(MX_DEAD, fuelModel.getMoistureOfExtinction());
    }

    /**
     * Gets the burnable state of this fuel.
     * @return True if the fuel model is a burnable type (e.g., not water, urban, etc.)
     */
    @XmlElement
    public boolean isBurnable() {
        return this.burnable;
    }

    /**
     * Gets the initialized state, set by <code>initialized()</code>.
     * @return True if this instance has been initialized.
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    private boolean isValid() {
        return isInitialized() && isBurnable();
    }

    /**
     * Gets the fuel temperature supplied in the constructor.
     * @return The fuel temperature in Fahrenheit; may contain a missing value.
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getFuelTemperature() {
        return fuelTemperature;
    }

    @Override
    public String toString() {
        try {
            return report();
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            return getTuple().toString();
        }
    }

    public String report() throws VisADException {
        // This extremely nice output was copied from the BehaveTest.java
        // class by Andreas Bachmann.

        // Build a StringBuilder(non-thread Safe) or StringBuffer for our output
        StringBuilder sb = new StringBuilder();

        // print out inputs and outputs...
        DecimalFormat df1 = new DecimalFormat("#,##0.0");
        DecimalFormat df2 = new DecimalFormat("#,##0.00");
        DecimalFormat df3 = new DecimalFormat("#,##0.000");
        DecimalFormat df4 = new DecimalFormat("#,##0.0000");
        DecimalFormat df5 = new DecimalFormat("#,##0.00000");

        appendLine(sb, "Fuel Model: " + fuelModel + " " + (fuelModel.isDynamic() ? " (D)" : " (S)"));
        appendLine(sb, " ");
        appendLine(sb, "  Effective mineral content    s_e          [%] ="
                + lPad(df1.format(s_e), 10));
        appendLine(sb, "  Total mineral content        s_t          [%] ="
                + lPad(df1.format(s_t), 10));
        appendLine(sb, "  Heat content                 heat    [Btu/lb] ="
                + lPad(df2.format(getLowHeatContent().getValue()), 10));
        appendLine(sb, "  Particle density             rho_p   [lb/ft3] ="
                + lPad(df2.format(rho_p), 10));
        appendLine(sb, "  Fuel bed depth               depth       [ft] ="
                + lPad(df2.format(getFuelBedDepth().getValue()), 10));
        appendLine(sb, "  Moisture of extinction       Mx_dead      [%] ="
                + lPad(df2.format(getDeadMoistureOfExt().getValue()), 10));
//        appendLine(sb, "  Fuel load transferred        curing       [%] ="
//                + lPad(df2.format(behave.curing * 100), 10));
        appendLine(sb, " ");

        // static fuel properties
        appendLine(sb, "  Size |  Fuel Load | Surface-to-Volume- | Moisture");
        appendLine(sb, "       |[tons/acre] | Ratio    [ft2/ft3] |      [%]");
        appendLine(sb, " ---------------------------------------------------");
        appendLine(sb, "    dh |"
                + lPad(df3.format(getDeadHerbFuelLoad().getValue(FireUnit.tons_acre)), 10) + "  |"
                + lPad(df3.format(getDeadHerbSAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getDeadHerbFuelMoisture().getValue()), 8));
        appendLine(sb, "    d1 |"
                + lPad(df3.format(getDead1HrFuelLoad().getValue(FireUnit.tons_acre)), 10) + "  |"
                + lPad(df3.format(getDead1HrSAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getDead1HrFuelMoisture().getValue()), 8));
        appendLine(sb, "    d2 |"
                + lPad(df3.format(getDead10HrFuelLoad().getValue(FireUnit.tons_acre)), 10) + "  |"
                + lPad(df3.format(getDead10HrSAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getDead10HrFuelMoisture().getValue()), 8));
        appendLine(sb, "    d3 |"
                + lPad(df3.format(getDead100HrFuelLoad().getValue(FireUnit.tons_acre)), 10) + "  |"
                + lPad(df3.format(getDead100HrSAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getDead100HrFuelMoisture().getValue()), 8));
        appendLine(sb, "    lh |"
                + lPad(df3.format(getLiveHerbFuelLoad().getValue(FireUnit.tons_acre)), 10) + "  |"
                + lPad(df3.format(getLiveHerbSAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getLiveHerbFuelMoisture().getValue()), 8));
        appendLine(sb, "    lw |"
                + lPad(df3.format(getLiveWoodyFuelLoad().getValue(FireUnit.tons_acre)), 10) + "  |"
                + lPad(df3.format(getLiveWoodySAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getLiveWoodyFuelMoisture().getValue()), 8));
        appendLine(sb, " ");

        appendLine(sb, "  => Percent dead fuel                      [%] ="
                + lPad(df2.format(w0_total_dead / w0_total * 100), 10));
        appendLine(sb, "  => Percent live fuel                      [%] ="
                + lPad(df2.format(w0_total_live / w0_total * 100), 10));
        appendLine(sb, " ");

        appendLine(sb, "  => Live moisture of extintion Mx_live     [%] ="
                + lPad(df2.format(getLiveMoistureOfExt().getValue()), 10));
        appendLine(sb, "  => Characteristic sv-ratio    sigma [ft2/ft3] ="
                + lPad(df2.format(getCharacteristicSAV().getValue()), 10));
        appendLine(sb, "  => Mean bulk density          rho_b  [lb/ft3] ="
                + lPad(df4.format(getMeanBulkDensity().getValue()), 12));
        appendLine(sb, "  => Mean packing ratio         beta        [-] ="
                + lPad(df5.format(getMeanPackingRatio().getValue()), 13));
        appendLine(sb, "  => Relative packing ratio     beta_ratio  [-] ="
                + lPad(df5.format(getRelativePackingRatio().getValue()), 13));
        appendLine(sb, " ");

        appendLine(sb, "  => Mineral damping coeff.     eta_S       [-] ="
                + lPad(df5.format(getMineralDamping().getValue()), 13));
        appendLine(sb, "  => Moisture damping coeff.    eta_M       [-] ="
                + lPad(df5.format(getMoistureDamping().getValue()), 13));
        appendLine(sb, "  => Moisture damping - dead    eta_M_dead  [-] ="
                + lPad(df5.format(eta_M_dead), 13));
        appendLine(sb, "  => Moisture damping - live    eta_M_live  [-] ="
                + lPad(df5.format(eta_M_live), 13));
        appendLine(sb, "  => Dry net fuel loading       wn_d   [lb/ft2] ="
                + lPad(df5.format(wn_dead), 13) + " : "
                + lPad(df5.format(new Real(FUEL_LOAD, wn_dead).getValue(FireUnit.kg_m2)), 7) + " [kg/m2]");
        appendLine(sb, "  => Live net fuel loading      wn_l   [lb/ft2] ="
                + lPad(df5.format(wn_live), 13) + " : "
                + lPad(df5.format(new Real(FUEL_LOAD, wn_live).getValue(FireUnit.kg_m2)), 7) + " [kg/m2]");
        appendLine(sb, " ");

        appendLine(sb, "  => Reaction intensity         I_r [Btu/f2/mn] ="
                + lPad(df2.format(getReactionIntensity().getValue()), 10) + "    : "
                + lPad(df2.format(getReactionIntensity().getValue(FireUnit.kW_m2)), 7) + " [kW/m2]");
        appendLine(sb, "  => Reaction intensity - dead  I_r [Btu/f2/mn] ="
                + lPad(df2.format(I_r_dead), 10));
        appendLine(sb, "  => Reaction intensity - live  I_r [Btu/f2/mn] ="
                + lPad(df2.format(I_r_live), 10));
        appendLine(sb, " ");

        return sb.toString();

    }

    private static void appendLine(StringBuilder sb, String s) {
        sb.append(s);
        sb.append('\n');
    }

    private static String lPad(String t, int outputWidth) {
        while (t.length() < outputWidth) {
            t = " " + t;
        }
        return t;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.fuelModel);
        hash = 53 * hash + Objects.hashCode(this.fuelMoisture);
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
        final SurfaceFuel other = (SurfaceFuel) obj;
        if (!Objects.equals(this.fuelModel, other.fuelModel)) {
            return false;
        }
        return Objects.equals(this.fuelMoisture, other.fuelMoisture);
    }

}
