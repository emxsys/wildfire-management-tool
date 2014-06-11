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
 * The Rothermel fire spread model.
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
     * Calculates the potential reaction velocity [1/min].
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
     * Calculates the reaction intensity [BTU/ft2/min].
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
     * @return I_r.
     */
    public static double reactionIntensity(double gamma, double heat, double eta_M, double eta_s) {
        double I_r = gamma * heat * eta_M * eta_s;
        return I_r;
    }

    /**
     * Calculates the flame residence time: tau [min].
     *
     * Albini (1976): p.91
     *
     * @param sigma The characteristic SAV ratio [ft2/ft3].
     * @return tau [min].
     */
    public static double flameResidenceTime(double sigma) {
        double tau = 384. / sigma;
        return tau;
    }

    /**
     * Calculates the heat release per unit area [Btu/ft2].
     *
     * @param I_r The reaction intensity [Btu/ft2/min].
     * @param tau The flame residence time [min].
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
     * @return xi
     */
    public static double propagatingFluxRatio(double sigma, double beta) {
        double xi = exp((0.792 + 0.681 * sqrt(sigma)) * (beta + 0.1)) / (192 + 0.2595 * sigma);
        return xi;
    }

    /**
     * Calculates the effective heating number: epsilon.
     *
     * Rothermel 1972: eq. (77).
     *
     * @param sv The SAV ratio for an individual particle [ft2/ft3].
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
     * Rothermel 1972: eq. (78).
     *
     * @param Mf The fuel moisture for an individual fuel particle [%].
     * @retrn Q_ig.
     */
    public static double heatOfPreignition(double Mf) {
        double Q_ig = 250 + 1116 * (Mf * 0.01); // Mf = [fraction]
        return Q_ig;
    }

    /**
     * Calculates the heat sink term [Btu/ft3].
     *
     * Rothermel 1972: eq. (77).
     *
     * @param Q_ig An array of heat of preignition values for individual particles.
     * @param epsilon An array of effective heating number values for individual particles.
     * @param sw An array of (sv * w0) weighting values for individual fuel particles.
     * @param rho_b The mean bulk density for the fuel complex.
     *
     * @return hsk [Btu/ft3]
     */
    public static double heatSink(double[] Q_ig, double[] epsilon, double[] sw, double rho_b) {
        double Qig_t = 0;
        double sw_t = 0;
        for (int i = 0; i < sw.length; i++) {
            Qig_t += Q_ig[i] * epsilon[i] * sw[i];
            sw_t += sw[i];
        }
        double hsk = rho_b * (Qig_t / sw_t);
        return hsk;
    }
}
