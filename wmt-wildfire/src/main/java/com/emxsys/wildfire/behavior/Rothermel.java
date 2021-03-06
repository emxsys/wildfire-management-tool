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

import com.emxsys.util.MathUtil;
import static java.lang.Math.*;
import java.util.logging.Logger;

/**
 * The Rothermel, et al, fire spread model developed for modeling wildland fire behavior.
 *
 * References:
 * <ul>
 * <li><a name="bib_1000"></a>Albini, F.A., 1976, Estimating Wildfire Behavior and Effects, General
 * Technical Report INT-30, USDA Forest Service, Intermountain Forest and Range Experiment Station
 *
 * <li><a name="bib_1001"></a>Anderson, H.A., 1983, Predicting Wind-driven Wild Land Fire Size and
 * Shape, Research Paper INT-305, USDA Forest Service, Intermountain Forest and Range Experiment
 * Station
 *
 * <li><a name="bib_1002"></a>Anderson, K., 2009, A Comparison of Hourly Fire Fuel Moisture Code
 * Calculations within Canada, Canadian Forest Service
 *
 * <li><a name="bib_1003"></a>Rothermel, R.C., 1972, A mathematical model for predicting fire spread
 * in wildland fuels, General Technical Report INT-115, USDA Forest Service, Intermountain Forest
 * and Range Experiment Station
 *
 * <li><a name="bib_1004"></a>Rothermel et al, 1986, Modeling Moisture Content of Fine Dead Wildland
 * Fuels: Input to the BEHAVE Fire Prediction System, Research Paper, INT-359, USDA Forest Service,
 * Intermountain Research Station
 *
 * <li><a name="bib_1005"></a>Van Wagner, C.E., 1977, A Method of Computing Fine Fuel Moisture
 * Content Throughout the Diurnal Cycle, Information Report PS-X-69, Petawawa Forest Experiment
 * Station, Canadian Forest Service
 * </ul>
 * Other Sources:
 * <ul>
 * <li>BehavePlus5, xfblib.cpp, Copyright Collin D. Bevins.
 * <li>Firelib v1.04, firelib.c, Copyright Collin D. Bevins.
 * </ul>
 *
 * @author Bruce Schubert
 */
public class Rothermel {

    private static final Logger logger = Logger.getLogger(Rothermel.class.getName());
    private static final double HALF_PI = Math.PI / 2.;

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
    public static double midFlameWindAdjustmentFactor(double fuelDepth) {
        double waf = 1.0;
        if (fuelDepth > 0) {
            // From BehavePlus5, xfblib.cpp by Collin D. Bevins
            waf = 1.83 / log((20. + 0.36 * fuelDepth) / (0.13 * fuelDepth));
        }
        return min(max(waf, 0), 1);
    }

    /**
     * Computes the mid-flame wind speed from 20 foot wind speeds. Used to compute rate of spread.
     *
     * @param wndSpd20Ft Wind speed 20 feet above the vegetation [MPH]
     * @param fuelDepth Vegetation height [feet]
     * @return Wind speed at vegetation height
     */
    public static double calcWindSpeedMidFlame(double wndSpd20Ft, double fuelDepth) {
        return wndSpd20Ft * midFlameWindAdjustmentFactor(fuelDepth);
    }

    /**
     * Computes the wind speed at the fuel level from 20 foot wind speeds. Used to compute wind
     * cooling effect on fuel temperatures.
     *
     * @param wndSpd20Ft Wind speed 20 feet above the vegetation [MPH]
     * @param fuelDepth Vegetation height [feet]
     * @return Wind speed at vegetation height
     */
    static public double calcWindSpeedNearFuel(double wndSpd20Ft, double fuelDepth) {
        // Equation #36
        // The ratio of windspeed at vegetation height to that at
        // 20 feet above the vegitation is given by:
        //  U_h' / U_20+h' = 1 / ln((20 + 0.36 * h') / 0.13 * h')
        //      where:
        //          h' = vegitation height [feet]
        if (fuelDepth == 0) {
            fuelDepth = 0.1;
        }
        double U_h = (1.0 / log((20 + 0.36 * fuelDepth) / (0.13 * fuelDepth))) * wndSpd20Ft;
        return U_h;
    }

    /**
     * Calculates the fire ellipse eccentricity from the effective wind speed.
     *
     * Anderson 1983: eq. (4)
     *
     * <pre>
     * Consider using Anderson 1983: eq. (17)
     *      l/w = 0.936 EXP(0.1l47U) + 0.461 EXP(-0.0692U)
     *  where U = midflame miles per hour.
     * </pre>
     * @param effectiveWind The effective wind speed of the combined wind and slope. [mph]
     * @return The eccentricity of the ellipse
     */
    public static double eccentricity(double effectiveWind) {
        double eccentricity = 0;
        if (effectiveWind > 0) {
            // From FireLib 1.04, firelib.c by Collin D. Bevins
            // a1 = major axis of semiellipse at the rear of the fire,
            //    = 1. + 0.25 * effectiveWindSpd / 88.0);
            double lbRatio = 1. + 0.002840909 * effectiveWind;
            if (lbRatio > 1.00001) {
                eccentricity = sqrt(pow(lbRatio, 2) - 1.0) / lbRatio;
            }
        }
        return eccentricity;
    }

    /**
     * Compute difference between fuel temperature and the air temperature due to solar heating and
     * wind cooling effects.
     *
     * Rothermel 1986: eq. (1)
     *
     * @param I radiation intensity [cal/c2 * min]
     * @param T_a temperature of air [fahrenheit]
     * @param U_h wind velocity at fuel level [mph]
     *
     * @return T_f temperature of fuel [fahrenheit]
     */
    static public double calcFuelTemp(double I, double T_a, double U_h) {
        // Rothermel et al, 1986, page 9
        // Equation #1
        // The difference in temperature between the air and fuel is assumed
        // to be directly proportional to the incident radiation intensity, I,
        // and inversely proportional to the wind velocity, U, and two constants
        // attributed to fuel conditions:
        //  T_f - T_a == I / (0.015 * U_h + 0.026)
        // where:
        //  T_f  =   temperature of fuel [farenheit]
        //  T_a  =   temperature of air  [farenheit]
        //  I    =   radiation intencity [cal/c2 * min]
        //  U_h  =   wind velocity at fuel level [mph]

        double T_f = (I / (0.015 * U_h + 0.026)) + T_a;
        return T_f;
    }

    /**
     * Compute the relative humidity for the air immediately adjacent to the fuel.
     *
     * Rothermel 1986: eq. (2)
     *
     * @param H_a relative humidity of the air [percent]
     * @param T_f fuel temperature [Fahrenheit]
     * @param T_a air temperature [Fahrenheit]
     * @return H_f - relative humidity of the air next to the fuel [percent]
     */
    static public double calcRelativeHumidityNearFuel(double H_a, double T_f, double T_a) {
        // Rothermel et al, 1986, page 9
        // Equation #2
        // Correction for relative humidity as a function of the fuel temperature
        // and air temperature:
        //  H_f = H_a * exp(-0.033(T_f - T_a))
        double H_f = H_a * exp(-0.033 * (T_f - T_a));
        return H_f;
    }

    /**
     * Computes the solar irradiance.
     *
     * Rothermel 1986: eq. (3)
     *
     * @param I_a irradiance at the forest floor perpendicular to the solar ray [cal/cm2*min]
     * @param r2 The earth-sun (center of mass) distance squared
     * @param A solar elevation angle to the sun [radians]
     *
     * @return I - incident radiation on the forest floor [cal/cm2*min]
     */
    static public double calcSolarIrradianceOnHorzSurface(double I_a, double r2, double A) {
        // Rothermel et al, 1986, page 9
        // Equation #3
        // I = (I_a / r2) * sin A
        if (A <= 0) {
            return 0;
        }
        double I = (I_a / r2) * sin(A);
        return I;
    }

    /**
     * Computes the optical air mass, i.e., the ratio of the optical path length of radiation
     * through the atmosphere at angle A, to the path length toward the zenith at sea level.
     *
     * Rothermel 1986: eq. (16)
     *
     * @param A the solar altitude angle [radians]
     * @param E the elevation at angle A [feet]
     * @return M the optical air mass ratio
     */
    static public double calcOpticalAirMass(double A, double E) {
        // Equation #16
        // M = (absolute_pressure / sea_level_pressure) 
        // csc A = exp(-0.0000448E) csc A
        double M = 0;
        if (A > 0) {
            M = exp(-0.0000448 * E) * 1.0 / sin(A);
        }
        return M;
    }

    /**
     * Computes irradiance at forest floor perpendicular to solar ray (1 [cal/cm2*min] = 697.8
     * [watts/m2])
     *
     * @param M is the optical air mass ratio
     * @param S_c cloud cover [percent]
     * @param p is the transparency coefficient
     * @return attenuated irradiance [cal/cm2*min]
     */
    static public double calcAttenuatedIrradiance(double M, double S_c, double p) {
        // I_a = I_M * tau_n
        //  where:
        //      tau_n is net transmittance of clouds and trees
        //      I_M is the direct solar irradiance including atmospheric attenuation
        // I_M = I_o * pM
        //  where:
        //      I_o is incident intensity or solar constant 1.98 cal/cm2*min
        //      p is the transparency coefficient
        //      M is the optical air mass, the ratio of the
        //
        if (M <= 0) {
            return 0;
        }
        final double I_o = 1.98;        // solar constant [cal/cm2*min]
        double tau_c = 1 - (S_c / 100.0); // cloud shade transmittance
        double tau_t = 1;               // tree shade transmittance (default 1 for now)
        double tau_n = tau_t * tau_c;
        double I_M = I_o * pow(p, M);   // incident radiation attenuated by atmosphere
        double I_a = I_M * tau_n;       // irradiance at forest floor perpendicular to solar ray
        return I_a;
    }

    /**
     * Computes the irradiance on a slope (neglecting the small variation in r).
     *
     * Rothermel 1986, eq. (9),(10) and (11), adjusted for Z relative to North instead of East
     *
     * @param alpha slope angle from horizontal at slope azimuth [radians]
     * @param beta aspect of the slope [radians]
     * @param A solar altitude (elevation) angle [radians]
     * @param Z solar azimuth angle (true) [radians]
     * @param I_a attenuated irradiance [cal/cm2*min]
     * @return incident radiation intensity [cal/cm2*min]
     */
    static public double calcIrradianceOnASlope(double alpha, double beta,
                                                double A, double Z,
                                                double I_a) {
        // Rothermel et al, 1986, page 11
        // Equation #9, 10 and 11
        //  I = Ia * sin zeta
        // where:
        //  alpha   = slope angle from horizontal at slope azimuth
        //  psi     = slope angle at solar azimuth Z
        //      zeta replaces A in equation #3, the solar angle to the slope in
        //      the plane normal to the slope
        //  sin zeta = sin(A - psi) * cos(alpha) / cos(psi)
        //  tan psi  = tan alpha * sin(z - beta)
        // where:
        //      (A - psi) is the solar angle to the slope in local vertical plane
        //      and psi is the slope angle at the solar azimuth, z,
        //

        // Precondition: Sun above the horizon
        if (A <= 0) {
            return 0;
        }
        // Precondition: Must have sunlight (not total shade)
        if (I_a <= 0) {
            return 0;
        }

        // Adjusted original algorithm from East azimuth to North azimuth with 1/2 PI.
        double tanPsi = tan(alpha) * sin(Z - beta - HALF_PI);
        double psi = atan(tanPsi);
        double sinZeta = sin(A - psi) * cos(alpha) / cos(psi);

        // I is the incident radiation intensity
        double I = I_a * sinZeta;

        // Post condition: I >= 0
        return (I > 0) ? I : 0;
    }

    /*
     * Original algorithm with East Azimuth (e.g., East = 0 degrees, South = 90 degrees).
     * @deprecated
     */
    @Deprecated
    static double calcIrradianceOnASlopeWithEastAz(double alpha, double beta, double A, double Z, double I_a) {
        // Rothermel et al, 1986, page 11
        // Equation #9, 10 and 11
        //  I = Ia * sin zeta
        // where:
        //  alpha   = slope angle from horizontal at slope azimuth
        //  psi     = slope angle at solar azimuth Z
        //      zeta replaces A in equation #3, the solar angle to the slope in
        //      the plane normal to the slope
        //  sin zeta = sin(A - psi) * cos(alpha) / cos(psi)
        //  tan psi  = tan alpha * sin(z - beta)
        // where:
        //      (A - psi) is the solar angle to the slope in local vertical plane
        //      and psi is the slope angle at the solar azimuth, z,
        //

        // Precondition: Sun above the horizon
        if (A <= 0) {
            return 0;
        }

        double tanPsi = tan(alpha) * sin(Z - beta);
        double psi = atan(tanPsi);
        double sinZeta = sin(A - psi) * cos(alpha) / cos(psi);

        // I is the incident radiation intensity
        double I = I_a * sinZeta;

        // Post condition: I >= 0
        return (I > 0) ? I : 0;
    }

    /**
     * Computes the Canadian Standard Daily Fine Fuel Moisture Code (FFMC) and converts it to a
     * percentage. Calculates the fuel moisture percentage for early afternoon from noon-time
     * weather conditions or forecasts. <br/>
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations located on page 47. <br/>
     *
     * Note: FFMC: Low = 0 - 72; Moderate = 73 - 77; High = 78 - 82; Extreme > 82
     *
     * @param m_0 initial fuel moisture at noon [percent]
     * @param T_f air temp immediately adjacent to fuel [Fahrenheit]
     * @param H_f relative humidity immediately adjacent to fuel [percent]
     * @param W 20 foot wind speed [MPH]
     * @param R rainfall amount [inches]
     * @return Fuel moisture percent derived from computed FFMC code [percent]
     */
    static public double calcCanadianStandardDailyFineFuelMoisture(double m_0, double T_f,
                                                                   double H_f, double W, double R) {

        // f_0 - initial moisture converted to a FFMC
        double f_0 = 101d - m_0;

        // Equation #1 - adust the initial fuel moisture code (f0) for rain
        double f_R;     // f_0 modified for rain
        // if rain > 0.02"
        if (R > 0.02) {
            double R_A = min(R, 1.5);
            double F;
            if (R_A <= 0.055) { // [inches]
                F = -56.0 - 55.6 * log(R_A + 0.04);
            } else if (R_A <= 0.225) { // [inches]
                F = -1.0 - 18.2 * log(R_A - 0.04);
            } else {
                F = 14 - 8.25 * log(R_A - 0.075);
            }
            f_R = max(0, (F * f_0 / 100) + 1 - 8.73 * exp(-0.1117 * f_0));
        } else {
            // little or no rain
            f_R = f_0;
        }

        // Equation #2
        // m_R - initial fuel moisture [percent] adjusted for rain
        double m_R = 101 - f_R;

        // Equation #3 - equilibrium drying curve
        double E_d = (0.942 * pow(H_f, 0.679)) + 11 * exp((H_f / 10) - 10);

        // Equation #4 - equilibrium wetting curve
        double E_w = (0.597 * pow(H_f, 0.768)) + 14 * exp((H_f / 8) - 12.5);

        // m - fine fuel moisture adjusted for humidity and wind
        double m;
        if (MathUtil.nearlyEquals(m_R, E_d)) {
            m = m_R;
        } // Wetting
        else if (m_R < E_d) {
            // fuel moisture is below the drying curve so a wetting trend is in effect
            // Equation #5
            m = E_w + (m_R - E_w) / 1.9953;   //-- original
            //m = E_W + (E_W - m_R) / 1.9953;     // corrected based on Anderson 2009 87-10 
        } // Drying
        else {
            // fuel moisture is above the drying curve so a drying trend is in effect

            // Here we constrain 20' wind to between 1 and 14 mph
            W = min(max(1.0, W), 14.0);
            // Equations #6 and #7
            double x = 0.424 * (1 - pow(H_f / 100, 1.7)) + 0.088 * pow(W, 0.5) * (1 - pow(H_f / 100, 8));
            m = E_d + (m_R - E_d) / pow(10, x);
        }
        // compute fine fuel moisture delta for temperature
        double delta = 0;
        if (f_0 < 99.0) {
            delta = max(-16.0, (T_f - 70d) * (0.63 - 0.0065 * f_R));
        }
        // final FFMC code constrained to between 0 and 99
        double f = max(0, min(99d, 101d - m + delta));

        // FFMC code converted to fuel moisture percentage
        return 101d - f;
    }

    /**
     * Computes the Canadian Hourly Fine Fuel Moisture percentage.
     *
     * Note: The calcCanadianStandardDailyFineFuelMoisure provides the first m_0. Subsequently, the
     * previous hour's m becomes m_0.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations located on page 47.
     *
     * @param m_0 previous hour's fuel moisture [percent]
     * @param H relative humidity [percent]
     * @param T_c air temperature [Celsius]
     * @param W_k 20' wind speed [KPH]
     *
     * @return fuel moisture [percent]
     */
    static public double calcCanadianHourlyFineFuelMoisture(double m_0, double H, double T_c, double W_k) {
        // Equation #1 [not used/applicatble] converts previous hours FFMC to fuel moisture percent
        // m_0 = previous hour's fuel moisture;

        // constrain wind to 22.5 kph
        W_k = min(max(0, W_k), 22.5);

        double m = 0;

        // Equation #2a compute equilibruim moisture curve (EMC) for drying
        double E_d = 0.942 * pow(H, 0.679) + 11 * exp((H - 100) / 10)
                + 0.18 * (21.1 - T_c) * (1 - exp(-0.115 * H));
        // Equation #2b compute equilibruim moisture curve (EMC) for wetting
        double E_w = 0.618 * pow(H, 0.753) + 10 * exp((H - 100) / 10)
                + 0.18 * (21.1 - T_c) * (1 - exp(-0.115 * H));

        if (m_0 > E_d) {
            // Drying...
            // Equations #3a and #3b compute log drying rate for hourly computation, log base 10
            double k_a = 0.424 * (1 - pow(H / 100, 1.7)) + 0.0694 * pow(W_k, 0.5) * (1 - pow(H / 100, 8));
            double k_d = k_a * 0.0579 * exp(0.0365 * T_c);
            // Equation #5a computes final fuel moisture percent
            m = E_d + (m_0 - E_d) * exp(-2.303 * k_d);

        } else if (m_0 < E_w) {
            // Wetting...

            // Rothermel (4a) and (4b) compute log wetting rate for hourly computation, log base 10
            double k_b = 0.424 * (1 - (pow((100 - H) / 100, 1.7)))
                    + 0.0694 * pow(W_k, 0.5) * (1 - (pow((100 - H) / 100, 8)));

            double k_w = k_b * 0.0579 * exp(0.0365 * T_c);

            // Equation #5b computes final fuel moisture percent
            m = E_w - (E_w - m_0) * exp(-2.303 * k_w);    // Rothemel pg 48

        } else {
            m = m_0;
        }
        return m;
    }

    /**
     * Computes the fine dead fuel moisture using the EMC from Canadian Standard Daily Fine Fuel
     * Moisture Code formula. The return value assumes instantaneous drying and wetting. <br/>
     *
     * Anderson, K., 2009, "A Comparison of Hourly Fire Fuel Moisture Code Calculations within
     * Canada", Canadian Forest Service
     *
     * @param m_0 Initial fuel moisture used to determine drying or wetting phase. [percent]
     * @param T_c Air temperature immediately adjacent to fuel [Celsius]
     * @param H Relative humidity immediately adjacent to fuel [percent]
     * @return Dead fine fuel moisture [percent]
     */
    static public double calcFineDeadFuelMoisture(double m_0, double T_c, double H) {

        // Van Wagner Eq. #2a (87-8a) equilibruim moisture curve (EMC) for drying
        double E_d = 0.942 * pow(H, 0.679) + 11 * exp((H - 100) / 10)
                + 0.18 * (21.1 - T_c) * (1 - exp(-0.115 * H));

        // Van Wagner Eq. #2b (87-8b) equilibruim moisture curve (EMC) for wetting
        double E_w = 0.618 * pow(H, 0.753) + 10 * exp((H - 100) / 10)
                + 0.18 * (21.1 - T_c) * (1 - exp(-0.115 * H));

        // m - fine fuel moisture 
        double m;
        if (m_0 > E_d) {
            // Instantaneous Drying
            m = E_d;
        } else if (m_0 < E_w) {
            // Instantaneous Wetting
            m = E_w;
        } else {
            // No change
            m = m_0;
        }
        return max(m, 0);
    }

}
