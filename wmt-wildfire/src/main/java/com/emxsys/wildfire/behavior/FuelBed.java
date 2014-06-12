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

import com.emxsys.visad.FireUnit;
import com.emxsys.visad.Reals;
import static com.emxsys.visad.Reals.convertTo;
import com.emxsys.visad.Tuples;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.WildfireType;
import static com.emxsys.wildfire.api.WildfireType.BETA;
import static com.emxsys.wildfire.api.WildfireType.BETA_OPT;
import static com.emxsys.wildfire.api.WildfireType.BETA_RATIO;
import static com.emxsys.wildfire.api.WildfireType.ETA_M;
import static com.emxsys.wildfire.api.WildfireType.ETA_S;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_LOAD_DEAD_100H;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_LOAD_DEAD_10H;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_LOAD_DEAD_1H;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_LOAD_DEAD_HERB;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_LOAD_LIVE_HERB;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_LOAD_LIVE_WOODY;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_SAV_DEAD_100H;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_SAV_DEAD_10H;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_SAV_DEAD_1H;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_SAV_DEAD_HERB;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_SAV_LIVE_HERB;
import static com.emxsys.wildfire.api.WildfireType.FUELBED_SAV_LIVE_WOODY;
import static com.emxsys.wildfire.api.WildfireType.FUEL_BED;
import static com.emxsys.wildfire.api.WildfireType.FUEL_BED_DEPTH;
import static com.emxsys.wildfire.api.WildfireType.FUEL_LOAD;
import static com.emxsys.wildfire.api.WildfireType.GAMMA;
import static com.emxsys.wildfire.api.WildfireType.HEAT_CONTENT_US;
import static com.emxsys.wildfire.api.WildfireType.I_R;
import static com.emxsys.wildfire.api.WildfireType.MX_DEAD;
import static com.emxsys.wildfire.api.WildfireType.MX_LIVE;
import static com.emxsys.wildfire.api.WildfireType.RHO_B;
import static com.emxsys.wildfire.api.WildfireType.RHO_P;
import static com.emxsys.wildfire.api.WildfireType.SIGMA;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class FuelBed extends RealTuple {

    private static final Logger logger = Logger.getLogger(FuelBed.class.getName());
    public static final int LOAD_DEAD_1H_INDEX = Tuples.getIndex(FUELBED_LOAD_DEAD_1H, FUEL_BED);
    public static final int LOAD_DEAD_10H_INDEX = Tuples.getIndex(FUELBED_LOAD_DEAD_10H, FUEL_BED);
    public static final int LOAD_DEAD_100H_INDEX = Tuples.getIndex(FUELBED_LOAD_DEAD_100H, FUEL_BED);
    public static final int LOAD_DEAD_HERB_INDEX = Tuples.getIndex(FUELBED_LOAD_DEAD_HERB, FUEL_BED);
    public static final int LOAD_LIVE_HERB_INDEX = Tuples.getIndex(FUELBED_LOAD_LIVE_HERB, FUEL_BED);
    public static final int LOAD_LIVE_WOODY_INDEX = Tuples.getIndex(FUELBED_LOAD_LIVE_WOODY, FUEL_BED);
    public static final int SAV_DEAD_1H_INDEX = Tuples.getIndex(FUELBED_SAV_DEAD_1H, FUEL_BED);
    public static final int SAV_DEAD_10H_INDEX = Tuples.getIndex(FUELBED_SAV_DEAD_10H, FUEL_BED);
    public static final int SAV_DEAD_100H_INDEX = Tuples.getIndex(FUELBED_SAV_DEAD_100H, FUEL_BED);
    public static final int SAV_DEAD_HERB_INDEX = Tuples.getIndex(FUELBED_SAV_DEAD_HERB, FUEL_BED);
    public static final int SAV_LIVE_HERB_INDEX = Tuples.getIndex(FUELBED_SAV_LIVE_HERB, FUEL_BED);
    public static final int SAV_LIVE_WOODY_INDEX = Tuples.getIndex(FUELBED_SAV_LIVE_WOODY, FUEL_BED);
    public static final int FUEL_BED_DEPTH_INDEX = Tuples.getIndex(FUEL_BED_DEPTH, FUEL_BED);
    public static final int MX_DEAD_INDEX = Tuples.getIndex(MX_DEAD, FUEL_BED);

    // In all fire behavior simulation systems that use the Rothermel model, total mineral content
    // is 5.55 percent, effective (silica-free) mineral content is 1.00 percent, and oven-dry fuel
    // particle density is 513 kg/m3 (32 lb/ft3).
    /** Total effective (silica-free) mineral content: 1%. Rothermel 1972 eq.(63) */
    static final double s_e = 1.0;
    /** Total mineral content: 5.5% - Albini's constant. The non-combustible fuel component. */
    static final double s_t = 5.5;
    /** Oven-dry fuel-particle density: 32 [lbs/ft3] - Albini's constant */
    static final double rho_p = 32.0;

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
    boolean nonBurnable = false;

    // Intermediate values
    double W_prime;                 // computed in Mx_live
    double wn_dead, wn_live;        // computed in eta_M
    double Mf_dead_t, Mf_live_t;        // computed in eta_M
    double eta_M_dead, eta_M_live;  // computed in eta_M for I_r
    double I_r_dead, I_r_live;      // computed in I_r

    FuelModel fuelModel;
    FuelMoisture fuelMoisture;
    Real characteristicSAV;
    Real meanBulkDensity;
    Real liveMx;
    Real moistureDamping;
    Real meanPackingRatio;
    Real optimalPackingRatio;
    Real reactionVelocity;
    Real reactionIntensity;

    public static FuelBed from(FuelModel model, FuelMoisture moisture) {
        try {
            // Transfer cured herbaceous fuel into the dead herbaceous fuel load
            double curing = model.isDynamic() ? calcHerbaceousCuring(moisture) : 0;
            double liveHerbLoad = model.getLiveHerbFuelLoad().getValue(FireUnit.lb_ft2);
            double deadHerbLoad = 0;
            if (liveHerbLoad > 0) {
                deadHerbLoad = liveHerbLoad * curing;
                liveHerbLoad -= deadHerbLoad;
            }
            // Build a tuple with the proper UOMs
            RealTuple tuple = new RealTuple(new Real[]{
                convertTo(FUELBED_LOAD_DEAD_HERB, new Real(FUEL_LOAD, deadHerbLoad)),
                convertTo(FUELBED_LOAD_DEAD_1H, model.getDead1HrFuelLoad()),
                convertTo(FUELBED_LOAD_DEAD_10H, model.getDead10HrFuelLoad()),
                convertTo(FUELBED_LOAD_DEAD_100H, model.getDead100HrFuelLoad()),
                convertTo(FUELBED_LOAD_LIVE_HERB, new Real(FUEL_LOAD, liveHerbLoad)),
                convertTo(FUELBED_LOAD_LIVE_WOODY, model.getLiveWoodyFuelLoad()),
                convertTo(FUELBED_SAV_DEAD_HERB, model.getLiveHerbSAVRatio()), // dead SAV same as live SAV
                convertTo(FUELBED_SAV_DEAD_1H, model.getDead1HrSAVRatio()),
                convertTo(FUELBED_SAV_DEAD_10H, model.getDead10HrSAVRatio()),
                convertTo(FUELBED_SAV_DEAD_100H, model.getDead100HrSAVRatio()),
                convertTo(FUELBED_SAV_LIVE_HERB, model.getLiveHerbSAVRatio()),
                convertTo(FUELBED_SAV_LIVE_WOODY, model.getLiveWoodySAVRatio()),
                convertTo(FUEL_BED_DEPTH, model.getFuelBedDepth()),
                convertTo(MX_DEAD, model.getMoistureOfExtinction())});

            // Conditional logging
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(tuple.longString());
            }
            return new FuelBed(model, moisture, tuple);

        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, "Error in fuel bed", ex);
            Exceptions.printStackTrace(ex);
            return new FuelBed();
        }

    }

    /**
     * Construct a new FuelCharacter object with "missing" values.
     */
    public FuelBed() {
        super(WildfireType.FUEL_BED);
    }

    /**
     * Constructs an instance with from a RealTuple.
     *
     * @param fuelMoistureTuple Fuel moisture values.
     */
    private FuelBed(FuelModel model, FuelMoisture moisture, RealTuple tuple) throws VisADException, RemoteException {
        super(WildfireType.FUEL_BED, tuple.getRealComponents(), null);
        this.fuelModel = model;
        this.fuelMoisture = moisture;

        double[] values = getValues();

        // Compute adjusted fine fuels (mix dead herbaceous with dead 1hr)
        double w0_d = values[LOAD_DEAD_1H_INDEX];
        double w0_h = values[LOAD_DEAD_HERB_INDEX];
        double sv_d = values[SAV_DEAD_1H_INDEX];
        double sv_h = values[SAV_DEAD_HERB_INDEX];
        double deadFineFuelLoad = w0_d + w0_h;
        double deadFineSAV = ((sv_d * sv_d * w0_d) + (sv_h * sv_h * w0_h))
                / (sv_d * w0_d + sv_h * w0_h);

        // Populate arrays used in formulas that sum fuel particle components
        this.sv = new double[]{
            deadFineSAV,
            values[SAV_DEAD_10H_INDEX],
            values[SAV_DEAD_100H_INDEX],
            values[SAV_LIVE_HERB_INDEX],
            values[SAV_LIVE_WOODY_INDEX]};
        this.sv_dead = new double[]{
            deadFineSAV,
            values[SAV_DEAD_10H_INDEX],
            values[SAV_DEAD_100H_INDEX]};
        this.sv_live = new double[]{
            values[SAV_LIVE_HERB_INDEX],
            values[SAV_LIVE_WOODY_INDEX]};

        this.w0 = new double[]{
            deadFineFuelLoad,
            values[LOAD_DEAD_10H_INDEX],
            values[LOAD_DEAD_100H_INDEX],
            values[LOAD_LIVE_HERB_INDEX],
            values[LOAD_LIVE_WOODY_INDEX]};
        this.w0_dead = new double[]{
            deadFineFuelLoad,
            values[LOAD_DEAD_10H_INDEX],
            values[LOAD_DEAD_100H_INDEX]};
        this.w0_live = new double[]{
            values[LOAD_LIVE_HERB_INDEX],
            values[LOAD_LIVE_WOODY_INDEX]};

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

        if (w0_total == 0 || sv_total == 0) {
            nonBurnable = true;
        }
    }

    /**
     * Computes the cured portion of live herbaceous fuels.
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

    /**
     * Gets the mean bulk density (fuel-bed weight per unit volume).
     *
     * @return rho_b [lbs/ft3]
     */
    public Real getMeanBulkDensity() {
        if (nonBurnable) {
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
    public Real getMeanPackingRatio() {
        if (nonBurnable) {
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
    public Real getOptimalPackingRatio() {
        if (nonBurnable) {
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
    public Real getRelativePackingRatio() {
        if (nonBurnable) {
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
    public Real getCharacteristicSAV() {
        if (nonBurnable) {
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
    public Real getLiveMoistureOfExt() {
        if (nonBurnable) {
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
    public Real getMoistureDamping() {
        if (nonBurnable) {
            return new Real(0);
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
    public Real getLowHeatContent() {
        return convertTo(HEAT_CONTENT_US, this.fuelModel.getLowHeatContent());
    }

    /**
     * Gets the potential reaction velocity [1/min].
     *
     * @return gamma [1/min]
     */
    public Real getReactionVelocity() {
        if (nonBurnable) {
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
    public Real getReactionIntensity() {
        if (nonBurnable) {
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
    public Real getFlameResidenceTime() {
        double tau = Rothermel.flameResidenceTime(getCharacteristicSAV().getValue());
        return new Real(tau);
    }

    /**
     * Gets the heat release per unit area.
     *
     * @return hpa [Btu/ft2]
     */
    public Real getHeatRelease() {
        if (nonBurnable) {
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
    public Real getPropagatingFluxRatio() {
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
     */
    public Real getHeatSink() {
        if (nonBurnable) {
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
     * Gets the dead herbaceous fuel loading.
     */
    public Real getDeadHerbFuelLoad() {
        try {
            return (Real) getComponent(LOAD_DEAD_HERB_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the 1 hour dead fuel loading.
     */
    public Real getDead1HrFuelLoad() {
        try {
            return (Real) getComponent(LOAD_DEAD_1H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the 10 hour dead fuel loading.
     */
    public Real getDead10HrFuelLoad() {
        try {
            return (Real) getComponent(LOAD_DEAD_10H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the 100 hour dead fuel loading.
     */
    public Real getDead100HrFuelLoad() {
        try {
            return (Real) getComponent(LOAD_DEAD_100H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the adjusted live herbaceous fuel loading.
     */
    public Real getLiveHerbFuelLoad() {
        try {
            return (Real) getComponent(LOAD_LIVE_HERB_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the live woody fuel loading.
     */
    public Real getLiveWoodyFuelLoad() {
        try {
            return (Real) getComponent(LOAD_LIVE_WOODY_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the dead herbaceous fuel surface-area-to-volume ratio.
     */
    public Real getDeadHerbSAVRatio() {
        try {
            return (Real) getComponent(SAV_DEAD_HERB_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the 1 hour dead fuel surface-area-to-volume ratio.
     */
    public Real getDead1HrSAVRatio() {
        try {
            return (Real) getComponent(SAV_DEAD_1H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the 10 hour dead fuel surface-area-to-volume ratio.
     */
    public Real getDead10HrSAVRatio() {
        try {
            return (Real) getComponent(SAV_DEAD_10H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the 100 hour dead fuel surface-area-to-volume ratio.
     */
    public Real getDead100HrSAVRatio() {
        try {
            return (Real) getComponent(SAV_DEAD_100H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the live herbaceous fuel surface-area-to-volume ratio.
     */
    public Real getLiveHerbSAVRatio() {
        try {
            return (Real) getComponent(SAV_LIVE_HERB_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the live woody fuel loading surface-area-to-volume ratio.
     */
    public Real getLiveWoodySAVRatio() {
        try {
            return (Real) getComponent(SAV_LIVE_WOODY_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the fuel bed depth.
     */
    public Real getFuelBedDepth() {
        try {
            return (Real) getComponent(FUEL_BED_DEPTH_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the fuel model used to construct the fuel bed.
     */
    public FuelModel getFuelModel() {
        return this.fuelModel;
    }
    
    public boolean getIsBurnable() {
        return !this.nonBurnable;
    }

    /**
     * Gets the dead moisture of extinction percent. Fuel moisture content is expressed as the mass
     * of water as a percentage of oven-dry mass.
     *
     * This value is used as a way to predict the effect of moisture content on fire behavior
     * (through the moisture damping coefficient). The greater the difference between actual
     * moisture content and the moisture of extinction, the smaller the moisture damping coefficient
     * and therefore the greater the spread rate.
     */
    public Real getDeadMoistureOfExt() {
        return Reals.convertTo(MX_DEAD, fuelModel.getMoistureOfExtinction());
    }

    public Real getDeadHerbFuelMoisture() {
        return fuelMoisture.getDead1HrFuelMoisture();   // dead herbaceous same as dead 1 hour.
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

    public Real getLiveHerbFuelMoisture() {
        return fuelMoisture.getLiveHerbFuelMoisture();
    }

    public Real getLiveWoodyFuelMoisture() {
        return fuelMoisture.getLiveWoodyFuelMoisture();
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
}
