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

import static java.lang.Math.*;
import java.util.logging.Logger;

/**
 * The Rothermel fire spread model developed for BEHAVE.
 * <ul>
 * <li style="bullet"><a name="bib_1000"></a>Albini, F.A., 1976, Estimating Wildfire Behavior and
 * Effects, General Technical Report INT-30, USDA Forest Service, Intermountain Forest and Range
 * Experiment Station
 *
 * <li><a name="bib_1010"></a>Rothermel, R.C., 1972, A mathematical model for predicting fire spread
 * in wildland fuels, General Technical Report INT-115, USDA Forest Service, Intermountain Forest
 * and Range Experiment Station
 * </ul>
 *
 * @author Bruce Schubert
 */
public class Rothermel {

    private static final Logger logger = Logger.getLogger(Rothermel.class.getName());

    /**
     * Calculates the mean bulk density (fuel-bed weight per unit volume): rho_b.
     *
     * @param w0 An array of fuel particle loading values [lb/ft2].
     * @param height The fuel bed height [ft].
     *
     * @return rho_b [lbs/ft3]
     */
    public static double meanBulkDensity(double[] w0, double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("height must be > 0.");
        }
        double w0_t = 0;
        for (double w : w0) {
            w0_t += w;
        }
        double rho_b = w0_t / height;
        return rho_b;
    }

    /**
     * Calculates the mean packing ratio for the fuel: beta.
     *
     * The compactness of the fuel bed is defined by the packing ratio, which is defined as the
     * fraction of the fuel array volume that is occupied by the fuel. Rothermel 1972: eq. (74)
     *
     * @param rho_b The mean bulk density of the fuel bed [lbs/ft3].
     * @param rho_p The oven-dry fuel-particle density [lbs/ft3].
     *
     * @return beta [dimensionless]
     */
    public static double meanPackingRatio(double rho_b, double rho_p) {
        if (rho_p <= 0) {
            throw new IllegalArgumentException("rho_p must be > 0.");
        }
        double beta = rho_b / rho_p;
        if ((beta > 0.12) || (beta < 0)) {
            throw new IllegalStateException(
                    "Mean packing ration [beta] out of limits [0,0.12]: " + beta);
        }
        return beta;
    }

    /**
     * Computes the optimal packing ratio for the fuel: beta_opt.
     *
     * Optimum packing ratio is a term used in the Rothermel's (1972) surface fire spread model
     * indicating the packing ratio that optimizes the reaction velocity term of the spread model.
     * Optimum packing ratio is a function of the fineness of fuel particles, which is measured by
     * the characteristic surface-area-to-volume ratio of the fuelbed. Optimum packing ratio does
     * not optimize fire behavior (rate of spread or fireline intensity). Fire Science Glossary
     * [electronic]. http://www.firewords.net
     *
     * @param sigma The characteristic SAV ratio for the fuel complex [ft2/ft3].
     *
     * @return beta_opt [dimensionless]
     */
    public static double optimalPackingRatio(double sigma) {
        double beta_opt = 3.348 * pow(sigma, -0.8189);
        return beta_opt;
    }

    /**
     * Computes the characteristic surface-area-to-volume ratio for the fuel complex: sigma.
     *
     * In Rothermel's (1972) surface fire spread model, characteristic surface-area-to-volume (SAV)
     * ratio constitutes the fuelbed-average SAV weighted by particle surface area. Surface-area
     * weighting emphasizes fine fuel because finer fuel particles have larger SAV ratios. Fire
     * Science Glossary [electronic]. http://www.firewords.net
     *
     * Rothermel 1972: eq. (71) and (72).
     *
     * @param sv An array of fuel particle SAV ratio values [ft2/ft3].
     * @param w0 An array of fuel particle loading values [lbs/ft2].
     *
     * @return sigma [ft2/ft3]
     */
    public static double characteristicSAV(double[] sv, double[] w0) {
        double sw_t = 0.;     // sw = (sv * w)
        double s2w_t = 0.;    // s2w = (sv^2 * w)
        for (int i = 0; i < sv.length; i++) {
            sw_t += sv[i] * w0[i];
            s2w_t += sv[i] * sv[i] * w0[i];
        }
        if (sw_t <= 0) {
            throw new IllegalArgumentException("w0 total loading must be > 0.");
        }
        double sigma = s2w_t / sw_t;
        return sigma;
    }

    /**
     * Calculates the potential reaction velocity: gamma.
     *
     * Rothermel 1972: eq. (68),(70) and Albini 1976: pg. 88
     *
     * @param sigma The characteristic SAV ratio [ft2/ft3].
     * @param beta_ratio The relative packing ratio [beta/beta_opt].
     * @return gamma [1/min]
     */
    public static double reactionVelocity(double sigma, double beta_ratio) {
        double sigma15 = Math.pow(sigma, 1.5);
        double A = 133. / Math.pow(sigma, 0.7913);    // Albini 
        double gamma_max = sigma15 / (495. + 0.0594 * sigma15);
        double gamma = gamma_max * pow(beta_ratio, A) * exp(A * (1. - beta_ratio));
        return gamma;
    }

    /**
     * Calculates the reaction intensity: I_r.
     *
     * The rate of heat release, per unit area of the flaming fire front, expressed as heat
     * energy/area/time, such as Btu/square foot/minute, or Kcal/square meter/second.
     *
     * Rothermel 1972: eq. (58), (59) thru (60)
     *
     * @param gamma The potential reaction velocity.
     * @param heat The low heat content [Btu/lb]
     * @param eta_M The moisture damping coefficient.
     * @param eta_s The mineral damping coefficient.
     *
     * @return I_r [BTU/ft2/min].
     */
    public static double reactionIntensity(double gamma, double heat, double eta_M, double eta_s) {
        double I_r = gamma * heat * eta_M * eta_s;
        return I_r;
    }

    /**
     * Calculates the flame residence time: tau.
     *
     * Albini (1976): p.91
     *
     * @param sigma The characteristic SAV ratio [ft2/ft3].
     * @return tau [min].
     */
    public static double flameResidenceTime(double sigma) {
        if (sigma <= 0) {
            throw new IllegalArgumentException("sigma must be > 0.");
        }
        double tau = 384. / sigma;
        return tau;
    }

    /**
     * Calculates the heat release per unit area: hpa.
     *
     * @param I_r The reaction intensity [Btu/ft2/min].
     * @param tau The flame residence time [min].
     *
     * @return hpa [Btu/ft2]
     */
    public static double heatRelease(double I_r, double tau) {
        double hpa = I_r * tau;
        return hpa;
    }

    /**
     * Gets the propagating flux ratio: xi.
     *
     * The no-wind propagating flux ratio is a function of the mean packing ratio (beta) and the
     * characteristic SAV ratio (sigma).
     *
     * Rothermel 1972: eq. (42)(76)
     *
     * @param sigma The characteristic SAV ratio [ft2/ft3].
     * @param beta The mean packing ratio [-]
     *
     * @return xi
     */
    public static double propagatingFluxRatio(double sigma, double beta) {
        if (sigma <= 0) {
            throw new IllegalArgumentException("sigma must be > 0.");
        }
        double xi = exp((0.792 + 0.681 * sqrt(sigma)) * (beta + 0.1)) / (192 + 0.2595 * sigma);
        return xi;
    }

    /**
     * Calculates the effective heating number: epsilon.
     *
     * Rothermel 1972: eq. (14) and (77).
     *
     * @param sv The SAV ratio value for an individual particle [ft2/ft3].
     *
     * @return epsilon.
     */
    public static double effectiveHeatingNumber(double sv) {
        double epsilon = 0;
        if (sv > 0) {
            epsilon = exp(-138. / sv);
        }
        return epsilon;
    }

    /**
     * Calculates the heat of preignition: Q_ig.
     *
     * Rothermel 1972: eq. (12) and (78).
     *
     * @param Mf The fuel moisture value for an individual fuel particle [%].
     *
     * @return Q_ig.
     */
    public static double heatOfPreignition(double Mf) {
        double Q_ig = 250 + 1116 * (Mf * 0.01); // Mf = [fraction]
        return Q_ig;
    }

    /**
     * Calculates the heat sink term: hsk.
     *
     * Rothermel 1972: eq. (77).
     *
     * @param preignitionHeat An array of heat of preignition values for individual particles
     * (Q_ig).
     * @param effectiveHeating An array of effective heating number values for individual particles
     * (epsilon).
     * @param sw An array of (sv * w0) weighting values for individual fuel particles (sw).
     * @param density The mean bulk density for the fuel complex (rho_b).
     *
     * @return hsk [Btu/ft3]
     */
    public static double heatSink(double[] preignitionHeat, double[] effectiveHeating, double[] sw, double density) {
        double Qig_t = 0;   // sum[i=1,n][Qig_i]
        double sw_t = 0;    // sum[i=1,n][sw_i]
        for (int i = 0; i < sw.length; i++) {
            Qig_t += preignitionHeat[i] * effectiveHeating[i] * sw[i];
            sw_t += sw[i];
        }
        double hsk = density * (Qig_t / sw_t);
        return hsk;
    }

    /**
     * Calculates the wind factor: phi_w.
     *
     * Rothermel 1972: eq. (47) and (79),(82),(83),(84)
     *
     * @param midFlameWindSpd The wind speed at mid-flame height [ft/min].
     * @param sigma The characteristic SAV ratio for the fuelbed [ft2/ft3].
     * @param beta_ratio The relative packing ratio [beta/beta_opt].
     *
     * @return phi_w
     */
    public static double windFactor(double midFlameWindSpd, double sigma, double beta_ratio) {
        double C = windParameterC(sigma);
        double B = windParameterB(sigma);
        double E = windParameterE(sigma);
        double phi_w = windFactor(midFlameWindSpd, C, B, E, beta_ratio);
        return phi_w;
    }

    /**
     * Calculates the wind multiplier for the rate of spread: phi_w.
     *
     * Rothermel 1972: eq. (47)
     *
     * @param midFlameWindSpd The wind speed at mid-flame height [ft/min].
     * @param C Result from Rothermel 1972: eq. (48).
     * @param B Result from Rothermel 1972: eq. (49).
     * @param E Result from Rothermel 1972: eq. (50).
     * @param beta_ratio The relative packing ratio [beta/beta_opt].
     *
     * @return phi_w
     */
    public static double windFactor(double midFlameWindSpd, double C, double B, double E, double beta_ratio) {
        double phi_w = C * pow(midFlameWindSpd, B) * pow(beta_ratio, -E);
        return phi_w;
    }

    /**
     * Calculates the wind parameter C.
     *
     * Rothermel 1972: eq. (48)
     *
     * @param sigma The characteristic SAV ratio for the fuelbed [ft2/ft3].
     *
     * @return C
     */
    public static double windParameterC(double sigma) {
        double C = 7.47 * exp(-0.133 * pow(sigma, 0.55));
        return C;
    }

    /**
     * Calculates the wind parameter B.
     *
     * Rothermel 1972: eq. (49)
     *
     * @param sigma The characteristic SAV ratio for the fuelbed [ft2/ft3].
     *
     * @return B
     */
    public static double windParameterB(double sigma) {
        double B = 0.02526 * pow(sigma, 0.54);
        return B;
    }

    /**
     * Calculates the wind parameter E.
     *
     * Rothermel 1972: eq. (50)
     *
     * @param sigma The characteristic SAV ratio for the fuelbed [ft2/ft3].
     *
     * @return E
     */
    public static double windParameterE(double sigma) {
        double E = 0.715 * exp(-0.000359 * sigma);
        return E;
    }

    /**
     * Calculates the slope multiplier for the rate of spread: phi_s.
     *
     * Rothermel 1972: eq. (51) and (78)
     *
     * @param slopeDegrees The steepness of the slope [degrees].
     * @param beta The mean packing ratio.
     *
     * @return phi_s
     */
    public static double slopeFactor(double slopeDegrees, double beta) {
        double phi = toRadians(slopeDegrees);
        double tan_phi = tan(phi);
        double phi_s = 5.275 * pow(beta, -0.3) * pow(tan_phi, 2);
        return phi_s;
    }

    /**
     * Calculates the effective wind speed from the combined wind and slope factors: efw.
     *
     * Rothermel 1972: eq. (87)
     *
     * @param phiEw The combined wind and slope factors [phiW + phiS].
     * @param beta_ratio beta/beta_opt.
     * @param sigma The characteristic SAV ratio [ft2/ft3].
     * @return efw [ft/min]
     */
    public static double effectiveWindSpeed(double phiEw, double beta_ratio, double sigma) {
        double C = windParameterC(sigma);
        double B = windParameterB(sigma);
        double E = windParameterE(sigma);
        double efw = effectiveWindSpeed(phiEw, C, B, E, beta_ratio);
        return efw;
    }

    /**
     * Calculates the effective wind speed from the combined wind and slope factors: efw.
     *
     * Rothermel 1972: eq. (87)
     *
     * @param phiEw The combined wind and slope factors [phiW + phiS].
     * @param C Result from Rothermel 1972: eq. (48).
     * @param B Result from Rothermel 1972: eq. (49).
     * @param E Result from Rothermel 1972: eq. (50).
     * @param beta_ratio
     * @return efw [ft/min]
     */
    public static double effectiveWindSpeed(double phiEw, double C, double B, double E, double beta_ratio) {
        // Effective windspeed: actually this is only the inverse function of phi_w
        double efw = (pow(phiEw / (C * pow(beta_ratio, -E)), 1 / B));
        return efw;
    }

    /**
     * Calculates the rate of spread with wind and/or slope: ros.
     *
     * Rothermel 1972: eq. (52) - heat source / heat sink
     *
     * @param reactionIntensity The fire reaction intensity (I_r) [BTU/ft2/min].
     * @param propogatingFlux The fire propagating flux (xi) [fraction].
     * @param windFactor The wind coefficient (phi_w).
     * @param slopeFactor The slope coefficient (phi_s).
     * @param heatSink The total heat sink (hsk) [Btu/ft3].
     *
     * @return ros [ft/min]
     */
    public static double rateOfSpread(double reactionIntensity, double propogatingFlux,
                                      double windFactor, double slopeFactor, double heatSink) {
        if (heatSink <= 0) {
            throw new IllegalArgumentException("hsk must be > 0.");
        }
        double ros = (reactionIntensity * propogatingFlux * (1 + windFactor + slopeFactor)) / heatSink;
        return ros;
    }

    /**
     * Calculates the rate of spread without wind and slope: ros.
     *
     * Rothermel 1972: eq. (52) - heat source / heat sink
     *
     * @param reactionIntensity The fire reaction intensity (I_r) [BTU/ft2/min].
     * @param propogatingFlux The fire propagating flux (xi) [fraction].
     * @param heatSink The total heat sink (hsk) [Btu/ft3].
     *
     * @return ros [ft/min]
     */
    public static double rateOfSpreadNoWindNoSlope(double reactionIntensity, double propogatingFlux, double heatSink) {
        if (heatSink <= 0) {
            throw new IllegalArgumentException("hsk must be > 0.");
        }
        double ros = (reactionIntensity * propogatingFlux) / heatSink;
        return ros;
    }

    /**
     * Calculates the flame zone depth: fzd.
     *
     * The depth, or front-to-back distance, of the actively flaming zone of a free spreading fire
     * can be determined from the rate of spread and the particle-residence time. Albini 1976: pg.
     * 86
     *
     * @param rateOfSpread The fire rate of spread (ros) [ft/min].
     * @param flameResidenceTime The fuelbed's flame residence time [min].
     *
     * @return fzd [ft]
     */
    public static double flameZoneDepth(double rateOfSpread, double flameResidenceTime) {
        double fzd = rateOfSpread * flameResidenceTime;
        return fzd;
    }

    /**
     * Calculates Byram's fireline intensity: I.
     *
     * Byram's intensity, I, is the rate of heat release per unit of fire edge. The reaction
     * intensity, I_r, provided by Rothermel's spread model is the rate of energy release per unit
     * area in the actively flaming zone.
     *
     * Albini 1976: eq. (16), pg. 86
     *
     * @param flameZoneDepth The depth of the actively flaming zone [ft].
     * @param reactionIntensity The fuelbed's fire reaction intensity (I_r) [Btu/ft2/min].
     *
     * @return I [Btu/ft/s]
     */
    public static double firelineIntensity(double flameZoneDepth, double reactionIntensity) {
        double I = reactionIntensity * flameZoneDepth / 60.;
        return I;
    }

    /**
     * Calculates flame length: L.
     *
     * Albini 1976: eq. (17) pg. 86
     *
     * @param firelineIntensity Byram's fireline intensity (I) [Btu/ft/s].
     *
     * @return L [ft]
     */
    public static double flameLength(double firelineIntensity) {
        double L = 0.45 * pow(firelineIntensity, 0.46);
        return L;
    }

    /**
     * Calculates the wind adjustment factor for scaling wind speed from 20-ft to midflame height.
     *
     * Wind adjustment factor is calculated as an average from the top of the fuel bed to twice the
     * fuel bed depth, using Albini and Baughman (1979) equation 9 (page 5).
     *
     * @param fuelDepth Fuel bed depth (height) [ft].
     * @return Wind adjustment factor, waf [0..1]
     */
    public static double windAdjustmentFactor(double fuelDepth) {
        double waf = 1.0;
        if (fuelDepth > 0) {
            // From BehavePlus5, xfblib.cpp by Collin D. Bevins
            waf = 1.83 / log((20. + 0.36 * fuelDepth) / (0.13 * fuelDepth));
        }
        return min(max(waf, 0), 1);
    }

    /**
     * Calculates the fire ellipse parameters from the effective wind speed.
     *
     * @param effectiveWind
     * @return The eccentricity of the ellipse
     */
    public static double eccentricity(double effectiveWind) {
        double eccentricity = 0;
        if (effectiveWind > 0) {
            // From FireLib 1.04, firelib.c by Collin D. Bevins
            // = 1. + 0.25 * effectiveWindSpd / 88.0);
            double lwRatio = 1. + 0.002840909 * effectiveWind;
            if (lwRatio > 1.00001) {
                eccentricity = sqrt(pow(lwRatio, 2) - 1.0) / lwRatio;
            }
        }
        return eccentricity;
    }
}