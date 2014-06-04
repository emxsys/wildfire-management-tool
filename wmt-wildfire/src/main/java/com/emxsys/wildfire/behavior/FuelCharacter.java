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
import com.emxsys.visad.GeneralUnit;
import static com.emxsys.visad.Reals.convertTo;
import com.emxsys.visad.Tuples;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.WildfireType;
import static com.emxsys.wildfire.api.WildfireType.BETA;
import static com.emxsys.wildfire.api.WildfireType.BETA_OPT;
import static com.emxsys.wildfire.api.WildfireType.BETA_RATIO;
import static com.emxsys.wildfire.api.WildfireType.FUEL_BED_DEPTH;
import static com.emxsys.wildfire.api.WildfireType.FUEL_CHARACTERISTICS;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_100H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_10H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_1H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_HERB;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_WOODY;
import static com.emxsys.wildfire.api.WildfireType.LOAD_DEAD_100H;
import static com.emxsys.wildfire.api.WildfireType.LOAD_DEAD_10H;
import static com.emxsys.wildfire.api.WildfireType.LOAD_DEAD_1H;
import static com.emxsys.wildfire.api.WildfireType.LOAD_LIVE_HERB;
import static com.emxsys.wildfire.api.WildfireType.LOAD_LIVE_WOODY;
import static com.emxsys.wildfire.api.WildfireType.MX_DEAD;
import static com.emxsys.wildfire.api.WildfireType.MX_LIVE;
import static com.emxsys.wildfire.api.WildfireType.RHO_B;
import static com.emxsys.wildfire.api.WildfireType.SAV_DEAD_100H;
import static com.emxsys.wildfire.api.WildfireType.SAV_DEAD_10H;
import static com.emxsys.wildfire.api.WildfireType.SAV_DEAD_1H;
import static com.emxsys.wildfire.api.WildfireType.SAV_LIVE_HERB;
import static com.emxsys.wildfire.api.WildfireType.SAV_LIVE_WOODY;
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
public class FuelCharacter extends RealTuple {

    private static final Logger logger = Logger.getLogger(FuelCharacter.class.getName());
    public static final int LOAD_DEAD_1H_INDEX = Tuples.getIndex(LOAD_DEAD_1H, FUEL_CHARACTERISTICS);
    public static final int LOAD_DEAD_10H_INDEX = Tuples.getIndex(LOAD_DEAD_10H, FUEL_CHARACTERISTICS);
    public static final int LOAD_DEAD_100H_INDEX = Tuples.getIndex(LOAD_DEAD_100H, FUEL_CHARACTERISTICS);
    public static final int LOAD_LIVE_HERB_INDEX = Tuples.getIndex(LOAD_LIVE_HERB, FUEL_CHARACTERISTICS);
    public static final int LOAD_LIVE_WOODY_INDEX = Tuples.getIndex(LOAD_LIVE_WOODY, FUEL_CHARACTERISTICS);
    public static final int SAV_DEAD_1H_INDEX = Tuples.getIndex(SAV_DEAD_1H, FUEL_CHARACTERISTICS);
    public static final int SAV_DEAD_10H_INDEX = Tuples.getIndex(SAV_DEAD_10H, FUEL_CHARACTERISTICS);
    public static final int SAV_DEAD_100H_INDEX = Tuples.getIndex(SAV_DEAD_100H, FUEL_CHARACTERISTICS);
    public static final int SAV_LIVE_HERB_INDEX = Tuples.getIndex(SAV_LIVE_HERB, FUEL_CHARACTERISTICS);
    public static final int SAV_LIVE_WOODY_INDEX = Tuples.getIndex(SAV_LIVE_WOODY, FUEL_CHARACTERISTICS);
    public static final int FUEL_MOISTURE_1H_INDEX = Tuples.getIndex(FUEL_MOISTURE_1H, FUEL_CHARACTERISTICS);
    public static final int FUEL_MOISTURE_10H_INDEX = Tuples.getIndex(FUEL_MOISTURE_10H, FUEL_CHARACTERISTICS);
    public static final int FUEL_MOISTURE_100H_INDEX = Tuples.getIndex(FUEL_MOISTURE_100H, FUEL_CHARACTERISTICS);
    public static final int FUEL_MOISTURE_HERB_INDEX = Tuples.getIndex(FUEL_MOISTURE_HERB, FUEL_CHARACTERISTICS);
    public static final int FUEL_MOISTURE_WOODY_INDEX = Tuples.getIndex(FUEL_MOISTURE_WOODY, FUEL_CHARACTERISTICS);
    public static final int FUEL_BED_DEPTH_INDEX = Tuples.getIndex(FUEL_BED_DEPTH, FUEL_CHARACTERISTICS);
    public static final int MX_DEAD_INDEX = Tuples.getIndex(MX_DEAD, FUEL_CHARACTERISTICS);

    // In all fire behavior simulation systems that use the Rothermel model, total mineral content
    // is 5.55 percent, effective (silica-free) mineral content is 1.00 percent, and oven-dry fuel
    // particle density is 513 kg/m3 (32 lb/ft3).
    /** Total effective (silica-free) mineral content */
    static final double s_e = 1.0;
    /** Total mineral content [%] - Albini's constant */
    static final double s_t = 5.5;
    /** Ovendry fuel-particle density[lbs/ft3] - Albini's constant */
    static final double rho_p = 32.0;

    /** Total fuel load */
    double w0;              // used by meanBulkDensity
    /** Auxillary */
    double sw_d, sw_l;      // used by mxLive and moistureDamping
    double sw_t;            // used by characteristicSAV
    /** s2w = (sv^2 * w0) */
    double s2w_t;           // used by characteristicSAV
    double swm_d, swm_l;    // used by moistureDamping
    /** Net fuel load */
    double wn_d, wn_l, dead, live;      // used by moistureDamping
    /** Ratio dead/live fine fuel */
    double sumhd, sumhl, sumhdm;        // used by moistureDamping
    double W_prime, Mf_dead;            // used by mxLive

    double Mx_dead;
    double depth;

    boolean nonBurnable = false;

    FuelModel fuelModel;
    Real characteristicSAV;
    Real meanBulkDensity;
    Real liveMx;
    Real moistureDamping;
    Real meanPackingRatio;
    Real optimalPackingRatio;

    public static FuelCharacter from(FuelModel model, FuelMoisture moisture) {
        try {
            // Transfer cured herbaceous fuel into the dead herbaceous fuel load
            double curing = model.isDynamic() ? calcHerbaceousCuring(moisture) : 0;
            double dead1HrLoad = model.getDead1HrFuelLoad().getValue(FireUnit.tons_acre);
            double dead1HrSAV = model.getDead1HrSAVRatio().getValue(FireUnit.ft2_ft3);
            double liveHerbSAV = model.getLiveHerbSAVRatio().getValue(FireUnit.ft2_ft3);
            double liveHerbLoad = model.getLiveHerbFuelLoad().getValue(FireUnit.tons_acre);
            if (dead1HrLoad > 0) {
                double deadHerbLoad = liveHerbLoad * curing;
                liveHerbLoad -= deadHerbLoad;
                // Compute a new dead 1hr surface area-to-volume ratio
                // with the cured live herbaceous fuel mixed in ...
                dead1HrSAV = ((dead1HrSAV * dead1HrSAV * dead1HrLoad) + (liveHerbSAV * liveHerbSAV * deadHerbLoad))
                        / ((dead1HrSAV * dead1HrLoad) + (liveHerbSAV * deadHerbLoad));
                // ... and add the dead herbaceous fuel to dead 1hr fuel
                dead1HrLoad += deadHerbLoad;
            }
            // Build a tuple with the proper UOMs
            RealTuple tuple = new RealTuple(new Real[]{
                new Real(LOAD_DEAD_1H, dead1HrLoad),
                convertTo(LOAD_DEAD_10H, model.getDead10HrFuelLoad()),
                convertTo(LOAD_DEAD_100H, model.getDead100HrFuelLoad()),
                new Real(LOAD_LIVE_HERB, liveHerbLoad),
                convertTo(LOAD_LIVE_WOODY, model.getLiveWoodyFuelLoad()),
                new Real(SAV_DEAD_1H, dead1HrSAV),
                convertTo(SAV_DEAD_10H, model.getDead10HrSAVRatio()),
                convertTo(SAV_DEAD_100H, model.getDead100HrSAVRatio()),
                new Real(SAV_LIVE_HERB, liveHerbSAV),
                convertTo(SAV_LIVE_WOODY, model.getLiveWoodySAVRatio()),
                convertTo(FUEL_BED_DEPTH, model.getFuelBedDepth()),
                convertTo(MX_DEAD, model.getMoistureOfExtinction()),
                moisture.getDead1HrFuelMoisture(),
                moisture.getDead10HrFuelMoisture(),
                moisture.getDead100HrFuelMoisture(),
                moisture.getLiveHerbFuelMoisture(),
                moisture.getLiveWoodyFuelMoisture()});

            // Conditional logging
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(tuple.longString());
            }
            return new FuelCharacter(model, tuple);

        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, "Error in fuel characteristics", ex);
            Exceptions.printStackTrace(ex);
            return new FuelCharacter();
        }

    }

    /**
     * Construct a new FuelCharacter object with "missing" values.
     */
    public FuelCharacter() {
        super(WildfireType.FUEL_CHARACTERISTICS);
    }

    /**
     * Constructs an instance with from a RealTuple.
     *
     * @param fuelMoistureTuple Fuel moisture values.
     */
    private FuelCharacter(FuelModel model, RealTuple tuple) throws VisADException, RemoteException {
        super(WildfireType.FUEL_CHARACTERISTICS, tuple.getRealComponents(), null);
        this.fuelModel = model;

        double[] values = getValues();
        double sv_d1 = values[SAV_DEAD_1H_INDEX];
        double sv_d2 = values[SAV_DEAD_10H_INDEX];
        double sv_d3 = values[SAV_DEAD_100H_INDEX];
        double sv_lh = values[SAV_LIVE_HERB_INDEX];
        double sv_lw = values[SAV_LIVE_WOODY_INDEX];
        double w0_d1 = values[LOAD_DEAD_1H_INDEX];
        double w0_d2 = values[LOAD_DEAD_10H_INDEX];
        double w0_d3 = values[LOAD_DEAD_100H_INDEX];
        double w0_lh = values[LOAD_LIVE_HERB_INDEX];
        double w0_lw = values[LOAD_LIVE_WOODY_INDEX];
        double m_d1 = values[FUEL_MOISTURE_1H_INDEX];
        double m_d2 = values[FUEL_MOISTURE_10H_INDEX];
        double m_d3 = values[FUEL_MOISTURE_100H_INDEX];
        double m_lh = values[FUEL_MOISTURE_HERB_INDEX];
        double m_lw = values[FUEL_MOISTURE_WOODY_INDEX];

        Mx_dead = values[MX_DEAD_INDEX];
        depth = values[FUEL_BED_DEPTH_INDEX];

        // Total weight [tons/acre]
        w0 = (w0_d1 + w0_d2 + w0_d3 + w0_lh + w0_lw);
        if (w0 == 0) {
            nonBurnable = true;
            return;
        }

        // Auxillary vars
        double sw_d1 = sv_d1 * w0_d1;
        double sw_d2 = sv_d2 * w0_d2;
        double sw_d3 = sv_d3 * w0_d3;
        double sw_lh = sv_lh * w0_lh;
        double sw_lw = sv_lw * w0_lw;
        sw_d = sw_d1 + sw_d2 + sw_d3;
        sw_l = sw_lh + sw_lw;
        sw_t = sw_d + sw_l;
        if (sw_t <= 0.0) {
            throw new IllegalStateException("Surface-to-volume-ratio not defined!");
        }
        // s2w = (sv^2 * w0)
        double s2w_d = (sw_d1 * sv_d1) + (sw_d2 * sv_d2) + (sw_d3 * sv_d3);
        double s2w_l = (sw_lh * sv_lh) + (sw_lw * sv_lw);
        s2w_t = s2w_d + s2w_l;
        // sw2 = (sv * w0^2)
        double sw2_d = (sw_d1 * w0_d1) + (sw_d2 * w0_d2) + (sw_d3 * w0_d3);
        double sw2_l = (sw_lh * w0_lh) + (sw_lw * w0_lw);
        double sw2_t = sw2_d + sw2_l;
        // swm = (sv * w0 * m)
        swm_d = (sw_d1 * m_d1) + (sw_d2 * m_d2) + (sw_d3 * m_d3);
        swm_l = (sw_lh * m_lh) + (sw_lw * m_lw);

//        // Net fuel loading
//        double wn_d1 = w0_d1 * (1 - s_t / 100);
//        double wn_d2 = w0_d2 * (1 - s_t / 100);
//        double wn_d3 = w0_d3 * (1 - s_t / 100);
//        double wn_lh = w0_lh * (1 - s_t / 100);
//        double wn_lw = w0_lw * (1 - s_t / 100);
//        // Rothermel 53. mean total surface area
//        double A_d1 = sw_d1 / rho_p;
//        double A_d2 = sw_d2 / rho_p;
//        double A_d3 = sw_d3 / rho_p;
//        double A_lh = sw_lh / rho_p;
//        double A_lw = sw_lw / rho_p;
//        // Rotherml 54 & 55
//        double A_d = A_d1 + A_d2 + A_d3;
//        double A_l = A_lh + A_lw;
//        double A_t = A_d + A_l;
//        // Rothermel 56. weighing parameters used in the net fuel loading.
//        double f_d1 = A_d1 / A_d;
//        double f_d2 = A_d2 / A_d;
//        double f_d3 = A_d3 / A_d;
//        double f_lh = A_lh / A_l;
//        double f_lw = A_lw / A_l;
        // Rothermel (59)
        if (sw_d > 0) {
            wn_d = ((1 - s_t / 100) * sw2_d) / sw_d;
            //dead = f_d1 * wn_d1 + f_d2 * wn_d2 + f_d3 * wn_d3;
        }
        if (sw_l > 0) {
            wn_l = (1 - s_t / 100) * sw2_l / sw_l;
            //live = f_lh * wn_lh + f_lw * wn_lw;
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
     * @return rho_b [lbs/ft3]
     */
    public Real getMeanBulkDensity() {
        if (nonBurnable) {
            return new Real(RHO_B, 0);
        } else if (this.meanBulkDensity == null) {
            try {
                // Convert from tons/acre to lbs/ft2
                double load = new Real(WildfireType.FUEL_LOAD_US, this.w0).getValue(FireUnit.lb_ft2);
                double height = getFuelBedDepth().getValue(GeneralUnit.foot);
                double rho_b = load / height;

                this.meanBulkDensity = new Real(RHO_B, rho_b);
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
                throw new IllegalStateException(ex);
            }
        }
        return this.meanBulkDensity;
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
            double rho_b = getMeanBulkDensity().getValue();
            double beta = rho_b / rho_p;
            // Rothermel 1972, p.18-19, values are between 0 and 0.12
            if ((beta > 0.12) || (beta < 0)) {
                throw new IllegalStateException(
                        "Mean packing ration [beta] out of limits [0,0.12]: " + beta);
            }
            this.meanPackingRatio = new Real(BETA, beta);
        }
        return this.meanPackingRatio;
    }

    /**
     * Gets the optimal packing ratio for the fuel.
     *
     * Optimum packing ratio is a term used in the Rothermel's (1972) surface fire spread model
     * indicating the packing ratio that optimizes the reaction velocity term of the spread model.
     * Optimum packing ratio is a function of the fineness of fuel particles, which is measured by
     * the characteristic surface-area-to-volume ratio of the fuelbed. Optimum packing ratio does
     * not optimize fire behavior (rate of spread or fireline intensity). Fire Science Glossary
     * [electronic]. http://www.firewords.net
     *
     * @return beta_opt [dimensionless]
     */
    public Real getOptimalPackingRatio() {
        if (nonBurnable) {
            return new Real(BETA_OPT, 0);
        }
        double sigma = getCharacteristicSAV().getValue();
        double beta_opt = 3.348 * Math.pow(sigma, -0.8189);
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
     * In Rothermel's (1972) surface fire spread model, characteristic surface-area-to-volume (SAV)
     * ratio constitutes the fuelbed-average SAV weighted by particle surface area. Surface-area
     * weighting emphasizes fine fuel because finer fuel particles have larger SAV ratios. Fire
     * Science Glossary [electronic]. http://www.firewords.net
     *
     * @return sigma [surface-area/volume]
     */
    public Real getCharacteristicSAV() {
        if (nonBurnable) {
            return new Real(SIGMA, 0);
        } else if (this.characteristicSAV == null) {
            // Rothermel 1972: eq. (71) and (72)
            double sigma = this.s2w_t / this.sw_t;
            this.characteristicSAV = new Real(SIGMA, sigma);
        }
        return characteristicSAV;
    }

    /**
     * Gets the mineral damping coefficient
     * @return eta_s
     */
    public Real getMineralDamping() {
        // Rothermel 1972: eq. (62)
        // s_e = silica-free ash content of fuel) - Albini's constant
        double eta_s = 0.174 * Math.pow(s_e / 100., -0.19);
        return new Real(eta_s);

    }

    /**
     * Gets coefficient weighting factor for live moisture of extinction.
     *
     * Using Albini (1976): page 89
     */
    public Real getLiveMoistureContentOfExt() {
        if (nonBurnable) {
            return new Real(MX_LIVE, 0);
        } else if (this.liveMx == null) {
            // Albini (1976): page 89:
            //   => (Mx)_living = 2.9W'(1-(M'_f)_dead/(Mx)_dead) - 0.226 (min = Mx_dead)
            // Calc "fine" fuel loadings, dead/live:
            double sav1Hr = getDead1HrSAVRatio().getValue();
            double sav10Hr = getDead10HrSAVRatio().getValue();
            double sav100Hr = getDead100HrSAVRatio().getValue();
            double savHerb = getLiveHerbSAVRatio().getValue();
            double savWoody = getLiveWoodySAVRatio().getValue();
            double w0_d1 = getDead1HrFuelLoad().getValue();
            double w0_d2 = getDead10HrFuelLoad().getValue();
            double w0_d3 = getDead100HrFuelLoad().getValue();
            double w0_lh = getLiveHerbFuelLoad().getValue();
            double w0_lw = getLiveWoodyFuelLoad().getValue();
            double m_d1 = getDead1HrFuelMoisture().getValue();
            double m_d2 = getDead10HrFuelMoisture().getValue();
            double m_d3 = getDead100HrFuelMoisture().getValue();
            double hn_d1 = 0.;
            double hn_d2 = 0.;
            double hn_d3 = 0.;
            double hn_lh = 0.;
            double hn_lw = 0.;
            //   W' = SUM(w0_d*exp(-138/sv_d*)/SUM(w0_l*exp(-500/sv_l*)
            if (sav1Hr > 0.) {
                hn_d1 = w0_d1 * Math.exp(-138 / sav1Hr);
            }
            if (sav10Hr > 0.) {
                hn_d2 = w0_d2 * Math.exp(-138 / sav10Hr);
            }
            if (sav100Hr > 0.) {
                hn_d3 = w0_d3 * Math.exp(-138 / sav100Hr);
            }
            if (savHerb > 0.) {
                hn_lh = w0_lh * Math.exp(-500 / savHerb);
            }
            if (savWoody > 0.) {
                hn_lw = w0_lw * Math.exp(-500 / savWoody);
            }
            // W' ratio of "fine" fuel loading, dead/living
            double sumLive = hn_lh + hn_lw;
            double sumDead = hn_d1 + hn_d2 + hn_d3;
            W_prime = sumLive > 0 ? (sumDead / sumLive) : 0;

            // Moisture content of "fine" dead fuel
            double deadMoisture = (hn_d1 * m_d1) + (hn_d2 * m_d2) + (hn_d3 * m_d3);
            Mf_dead = sumDead > 0 ? deadMoisture / sumDead : 0;

            double Mx_live = Math.max((2.9 * W_prime * (1 - Mf_dead / Mx_dead) - 0.226) * 100, Mx_dead);

            this.liveMx = new Real(MX_LIVE, Mx_live);
        }
        return this.liveMx;
    }

    /**
     * Calculates the moisture damping coefficients for dead and live fuel.
     *
     * The moisture damping coefficient accounts for the decrease in caused by the combustion of
     * fuels that initially contained moisture. Rothermel (1972): eq. (65) & (66).
     *
     * @return eta_M
     */
    public Real getMoistureDamping() {
        if (nonBurnable) {
            return new Real(0);
        } else if (this.moistureDamping == null) {
            // Moisture ratios
            double Mx_live = getLiveMoistureContentOfExt().getValue();
            double rm_l = 0.;
            double rm_d = 0.;
            if (this.sw_l > 0) {
                rm_l = this.swm_l / (this.sw_l * Mx_live);
            }
            if (this.sw_d > 0) {
                rm_d = this.swm_d / (this.sw_d * this.Mx_dead);
            }
            double eta_Md = 1 - 2.59 * (rm_d) + 5.11 * Math.pow(rm_d, 2) - 3.52 * Math.pow(rm_d, 3);
            double eta_Ml = 1 - 2.59 * (rm_l) + 5.11 * Math.pow(rm_l, 2) - 3.52 * Math.pow(rm_l, 3);
            // check for eta_* lower than 0;
            if (eta_Md < 0) {
                eta_Md = 0.;
            }
            if (eta_Ml < 0) {
                eta_Ml = 0.;
            }
            double eta_M = (this.wn_d * eta_Md) + (this.wn_l * eta_Ml);
            this.moistureDamping = new Real(eta_M);
        }
        return this.moistureDamping;
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
     * Gets the dead moisture of extinction percent.
     */
    public Real getDeadMoistureOfExt() {
        try {
            return (Real) getComponent(MX_DEAD_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Real getDead1HrFuelMoisture() {
        try {
            return (Real) getComponent(FUEL_MOISTURE_1H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Real getDead10HrFuelMoisture() {
        try {
            return (Real) getComponent(FUEL_MOISTURE_10H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Real getDead100HrFuelMoisture() {
        try {
            return (Real) getComponent(FUEL_MOISTURE_100H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Real getLiveHerbFuelMoisture() {
        try {
            return (Real) getComponent(FUEL_MOISTURE_HERB_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Real getLiveWoodyFuelMoisture() {
        try {
            return (Real) getComponent(FUEL_MOISTURE_WOODY_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public String report() {
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
//        appendLine(sb, "  Heat content                 heat     [kJ/kg] ="
//                + lPad(df2.format(behave.heat), 10));
        appendLine(sb, "  Particle density             rho_p   [lb/ft3] ="
                + lPad(df2.format(rho_p), 10));
        appendLine(sb, "  Fuel bed depth               depth       [ft] ="
                + lPad(df2.format(depth), 10));
        appendLine(sb, "  Moisture of extinction       Mx_dead      [%] ="
                + lPad(df2.format(getDeadMoistureOfExt().getValue()), 10));
//        appendLine(sb, "  Fuel load transferred        curing       [%] ="
//                + lPad(df2.format(behave.curing * 100), 10));
        appendLine(sb, " ");

        // static fuel properties
        appendLine(sb, "  Size |  Fuel Load | Surface-to-Volume- | Moisture");
        appendLine(sb, "       |[tons/acre] | Ratio    [ft2/ft3] |      [%]");
        appendLine(sb, " ---------------------------------------------------");
        appendLine(sb, "    d1 |"
                + lPad(df3.format(getDead1HrFuelLoad().getValue()), 10) + "  |"
                + lPad(df3.format(getDead1HrSAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getDead1HrFuelMoisture().getValue()), 8));
        appendLine(sb, "    d2 |"
                + lPad(df3.format(getDead10HrFuelLoad().getValue()), 10) + "  |"
                + lPad(df3.format(getDead10HrSAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getDead10HrFuelMoisture().getValue()), 8));
        appendLine(sb, "    d3 |"
                + lPad(df3.format(getDead100HrFuelLoad().getValue()), 10) + "  |"
                + lPad(df3.format(getDead100HrSAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getDead100HrFuelMoisture().getValue()), 8));
        appendLine(sb, "    lh |"
                + lPad(df3.format(getLiveHerbFuelLoad().getValue()), 10) + "  |"
                + lPad(df3.format(getLiveHerbSAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getLiveHerbFuelMoisture().getValue()), 8));
        appendLine(sb, "    lw |"
                + lPad(df3.format(getLiveWoodyFuelLoad().getValue()), 10) + "  |"
                + lPad(df3.format(getLiveWoodySAVRatio().getValue()), 18) + "  |"
                + lPad(df1.format(getLiveWoodyFuelMoisture().getValue()), 8));
        appendLine(sb, " ");

        appendLine(sb, "  => Live moisture of extintion Mx_live     [%] ="
                + lPad(df2.format(getLiveMoistureContentOfExt().getValue()), 10));
        appendLine(sb, "  => Characteristic sv-ratio    sigma [ft2/ft3] ="
                + lPad(df2.format(getCharacteristicSAV().getValue()), 10));
        appendLine(sb, "  => Mean bulk density          rho_b  [lb/ft3] ="
                + lPad(df4.format(getMeanBulkDensity().getValue()), 12));
        appendLine(sb, "  => Mean packing ratio         beta        [-] ="
                + lPad(df5.format(getMeanPackingRatio().getValue()), 13));
        appendLine(sb, "  => Relative packing ratio     beta_ratio  [-] ="
                + lPad(df5.format(getRelativePackingRatio().getValue()), 13));
        appendLine(sb, " ");
        appendLine(sb, "  => Net fuel load live               wn_l  [-] ="
                + lPad(df5.format(wn_l), 13));
        appendLine(sb, "  => Net fuel load dead               wn_d  [-] ="
                + lPad(df5.format(wn_d), 13));
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
