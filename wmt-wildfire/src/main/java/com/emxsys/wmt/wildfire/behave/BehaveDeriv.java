/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
 * - Neither the name of the Emxsys company nor the names of its 
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
package com.emxsys.behave;

import com.emxsys.behave.Behave;
import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

/**
 * Calculates the Derivations of Fire behavior according to the Rothermel
 *  model.
 * @author andreas bachmann
 * @version 1.0, April 2001
 */
class BehaveDeriv extends Behave {

    /**
     * get all the basic BEHAVE - Calculations done by Behave-Class
     */
    // instance vars...
    public boolean isCalculated = false;
    // the correlation matrix
    public double[][] corr = new double[17][17];
    // aditional parameters that come from the derivatives....
    double hsk_dw0_d1, hsk_dw0_d2, hsk_dw0_d3, hsk_dw0_lh, hsk_dw0_lw = 0.;
    double hsk_dm_d1, hsk_dm_d2, hsk_dm_d3, hsk_dm_lh, hsk_dm_lw = 0.;
    double hsk_dsv_d1, hsk_ddepth = 0.;
    double ros_dw0_d1, ros_dw0_d2, ros_dw0_d3, ros_dw0_lh, ros_dw0_lw = 0.;
    double ros_dsv_d1, ros_ddepth = 0.;
    double ros_dm_d1, ros_dm_d2, ros_dm_d3, ros_dm_lh, ros_dm_lw = 0.;
    double ros_dslp, ros_dasp, ros_dwsp, ros_dwdr = 0.;
    double ros_dmx = 0.;
    // end of calculations variables
    /**
     * input variables
     */
    public double[] stdv = new double[17];
    // out Stdv;
    public double rosStdv = Double.NaN;
    public double rosVar = Double.NaN;
    public double efwStdv = Double.NaN;
    public double efwVar = Double.NaN;
    public double sdrStdv = Double.NaN;
    public double sdrVar = Double.NaN;
    // array for derivatives...
    public double[] ros_d = new double[17];
    public double[] efw_d = new double[17];
    public double[] sdr_d = new double[17];

    /**
     * Constructor
     */
    public BehaveDeriv() {
        init();
    }

    /**
     * Init()
     *
     * initialize various arrays...
     */
    private void init() {
        int i, j;
        // initialize stdv vector
        for (i = 0; i < 17; i++) {
            stdv[i] = 0.;
            ros_d[i] = 0.;            // rate of spread derivatives
            efw_d[i] = 0.;            // effective windspeed derivatives
            sdr_d[i] = 0.;            // spread direction derivatives
        }
        // initialize default correlation matrix...
        for (i = 0; i < 17; i++) {
            for (j = 0; j < 17; j++) {
                if (i == j) {
                    corr[i][j] = 1.;
                } else {
                    corr[i][j] = 0.;
                }
            }
        }
    }

    /**************************************************************************
     *
     * Instance Methods
     *
     **************************************************************************/
    public void setCorrelationMatrix(double[][] corr) {
        this.corr = corr;
    }

    // the input properties will store the Standard deviations...
    public void setParameterStdv(String key, double value) {
        // evaluate String...
        if (key.equalsIgnoreCase("w0_d1")) {
            stdv[0] = value;
        } else if (key.equalsIgnoreCase("w0_d2")) {
            stdv[1] = value;
        } else if (key.equalsIgnoreCase("w0_d3")) {
            stdv[2] = value;
        } else if (key.equalsIgnoreCase("w0_lh")) {
            stdv[3] = value;
        } else if (key.equalsIgnoreCase("w0_lw")) {
            stdv[4] = value;
        } else if (key.equalsIgnoreCase("m_d1")) {
            stdv[5] = value;
        } else if (key.equalsIgnoreCase("m_d2")) {
            stdv[6] = value;
        } else if (key.equalsIgnoreCase("m_d3")) {
            stdv[7] = value;
        } else if (key.equalsIgnoreCase("m_lh")) {
            stdv[8] = value;
        } else if (key.equalsIgnoreCase("m_lw")) {
            stdv[9] = value;
        } else if (key.equalsIgnoreCase("sv_d1")) {
            stdv[10] = value;
        } else if (key.equalsIgnoreCase("depth")) {
            stdv[11] = value;
        } else if (key.equalsIgnoreCase("mx")) {
            stdv[12] = value;
        } else if (key.equalsIgnoreCase("wsp")) {
            stdv[13] = value;
        } else if (key.equalsIgnoreCase("wdr")) {
            stdv[14] = Math.toRadians(value);
        } else if (key.equalsIgnoreCase("slp")) {
            stdv[15] = Math.toRadians(value);
        } else if (key.equalsIgnoreCase("asp")) {
            stdv[16] = Math.toRadians(value);
        }
    }

    /**
     * wrapper for getting the value of some variables...
     */
    public double getValue(String key) {
        int i;
        double res = Double.NaN;
        String subkey = null;
        int l = key.length();
        boolean keyFound = false;

        // just an input value
        // HashMap might return null!
        if (varHashMap.containsKey(key)) {
            res = (varHashMap.get(key)).doubleValue();
            keyFound = true;
        }
        if (resHashMap.containsKey(key)) {
            res = (resHashMap.get(key)).doubleValue();
            keyFound = true;
        }
        //
        if (key.endsWith("Var")) {  // ros, sdr, efw
            subkey = key.substring(0, l - 3);
            if (subkey.equals("ros")) {
                res = rosVar;
                keyFound = true;
            } else if (subkey.equals("sdr")) {
                res = sdrVar;
                keyFound = true;
            } else if (subkey.equals("efw")) {
                res = efwVar;
                keyFound = true;
            }
        }

        if (key.endsWith("Stdv")) {  // ros, sdr, efw
            subkey = key.substring(0, l - 4);
            if (subkey.equals("ros")) {
                res = rosStdv;
                keyFound = true;
            } else if (subkey.equals("sdr")) {
                res = sdrStdv;
                keyFound = true;
            } else if (subkey.equals("efw")) {
                res = efwStdv;
                keyFound = true;
            }
        }

        if (key.endsWith("Stdv")) {  // input parameters (not all!)
            subkey = key.substring(0, l - 4);
            i = v.indexOf(subkey);
            if (i >= 0) {
                res = stdv[i];
                keyFound = true;
            }
        }

        if (key.endsWith("PDros")) {  // Partial Derivative of ROS
            subkey = key.substring(0, l - 5);
            i = v.indexOf(subkey);
            if (i >= 0) {
                res = ros_d[i];
                keyFound = true;
            }
        }

        if (key.endsWith("PDefw")) {  // Partial Derivative of EFW
            subkey = key.substring(0, l - 5);
            i = v.indexOf(subkey);
            if (i >= 0) {
                res = efw_d[i];
                keyFound = true;
            }
        }

        if (key.endsWith("PDsdr")) {  // Partial Derivative of SDR
            subkey = key.substring(0, l - 5);
            i = v.indexOf(subkey);
            if (i >= 0) {
                res = sdr_d[i];
                keyFound = true;
            }
        }

        if (key.equals("fuelModel")) {
            res = (double) fuelModel;
            keyFound = true;
        }
        //
        return keyFound ? res : Double.NaN;
    }

    public void calcVariances() {
        int i, j;
        double sdrVar_r = 0.;     // in Radians
        if (isCalculated) {
            rosVar = 0.;
            efwVar = 0.;
            sdrVar = 0.;
            //
            for (i = 0; i < 17; i++) {
                for (j = 0; j < 17; j++) {
                    rosVar += corr[i][j] * stdv[i] * stdv[j] * ros_d[i] * ros_d[j];
                    efwVar += corr[i][j] * stdv[i] * stdv[j] * efw_d[i] * efw_d[j];
                    sdrVar_r += corr[i][j] * stdv[i] * stdv[j] * sdr_d[i] * sdr_d[j];
                }
            }
            // set standard deviation...
            rosStdv = Math.sqrt(rosVar);
            efwStdv = Math.sqrt(efwVar);
            //
            // 27.9.2000
            // conversion from var_rad -> var_deg: var_rad * (180/pi)^2 !!
            sdrVar = Math.toDegrees(sdrVar_r) * 180 / Math.PI;
            sdrStdv = Math.toDegrees(Math.sqrt(sdrVar_r));
        }
    }

    // wrapper
    public void calcDerivs() {
        if (!super.isCalculated) {
            super.calc();
        }
        if (super.canDerive) {
            calcPartialDerivatives();
        }
    }

    protected void calcPartialDerivatives() {
        /**
         *  characteristic surface to volume ratio...
         */
        double sigma_dw0_d1 = 0.;
        double sigma_dw0_d2 = 0.;
        double sigma_dw0_d3 = 0.;
        double sigma_dw0_lh = 0.;
        double sigma_dw0_lw = 0.;
        double sigma_dsv_d1 = 0.;

        if (sw_t > 0) {
            // derivatives
            sigma_dw0_d1 = (sv_d1 / sw_t) * (sv_d1 - s2w_t / sw_t);
            sigma_dw0_d2 = (sv_d2 / sw_t) * (sv_d2 - s2w_t / sw_t);
            sigma_dw0_d3 = (sv_d3 / sw_t) * (sv_d3 - s2w_t / sw_t);
            sigma_dw0_lh = (sv_lh / sw_t) * (sv_lh - s2w_t / sw_t);
            sigma_dw0_lw = (sv_lw / sw_t) * (sv_lw - s2w_t / sw_t);
            sigma_dsv_d1 = (w0_d1 / sw_t) * (2 * sv_d1 - s2w_t / sw_t);
        }

        /**
         *   mean bulk density
         */
        double rho_b_dw0_d1, rho_b_dw0_d2, rho_b_dw0_d3 = 0.;
        double rho_b_dw0_lh, rho_b_dw0_lw = 0.;
        double rho_b_ddepth = 0.;

        rho_b_dw0_d1 = 1 / depth;
        rho_b_dw0_d2 = 1 / depth;
        rho_b_dw0_d3 = 1 / depth;
        rho_b_dw0_lh = 1 / depth;
        rho_b_dw0_lw = 1 / depth;
        rho_b_ddepth = -1 * rho_b / depth;

        /**
         *   packing ratios
         */
        // mean packing ratio
        double beta_dw0_d1, beta_dw0_d2, beta_dw0_d3, beta_dw0_lh, beta_dw0_lw = 0.;
        double beta_ddepth = 0.;

        beta_dw0_d1 = rho_b_dw0_d1 / rho_p;
        beta_dw0_d2 = rho_b_dw0_d2 / rho_p;
        beta_dw0_d3 = rho_b_dw0_d3 / rho_p;
        beta_dw0_lh = rho_b_dw0_lh / rho_p;
        beta_dw0_lw = rho_b_dw0_lw / rho_p;
        beta_ddepth = rho_b_ddepth / rho_p;

        // optimal packing ratio
        double beta_opt_dw0_d1, beta_opt_dw0_d2, beta_opt_dw0_d3 = 0.;
        double beta_opt_dw0_lh, beta_opt_dw0_lw, beta_opt_dsv_d1 = 0.;

        beta_opt_dw0_d1 = -7.25324297 * Math.pow(sigma, -1.8189) * sigma_dw0_d1;
        beta_opt_dw0_d2 = -7.25324297 * Math.pow(sigma, -1.8189) * sigma_dw0_d2;
        beta_opt_dw0_d3 = -7.25324297 * Math.pow(sigma, -1.8189) * sigma_dw0_d3;
        beta_opt_dw0_lh = -7.25324297 * Math.pow(sigma, -1.8189) * sigma_dw0_lh;
        beta_opt_dw0_lw = -7.25324297 * Math.pow(sigma, -1.8189) * sigma_dw0_lw;
        beta_opt_dsv_d1 = -7.25324297 * Math.pow(sigma, -1.8189) * sigma_dsv_d1;

        // ratio mean / optimal packing
        double beta_ratio_dw0_d1, beta_ratio_dw0_d2, beta_ratio_dw0_d3 = 0.;
        double beta_ratio_dw0_lh, beta_ratio_dw0_lw, beta_ratio_dsv_d1 = 0.;
        double beta_ratio_ddepth = 0.;

        beta_ratio_dw0_d1 = (beta_dw0_d1 * beta_opt - beta * beta_opt_dw0_d1) /
                Math.pow(beta_opt, 2);
        beta_ratio_dw0_d2 = (beta_dw0_d2 * beta_opt - beta * beta_opt_dw0_d2) /
                Math.pow(beta_opt, 2);
        beta_ratio_dw0_d3 = (beta_dw0_d3 * beta_opt - beta * beta_opt_dw0_d3) /
                Math.pow(beta_opt, 2);
        beta_ratio_dw0_lh = (beta_dw0_lh * beta_opt - beta * beta_opt_dw0_lh) /
                Math.pow(beta_opt, 2);
        beta_ratio_dw0_lw = (beta_dw0_lw * beta_opt - beta * beta_opt_dw0_lw) /
                Math.pow(beta_opt, 2);
        //
        beta_ratio_dsv_d1 = (-beta * beta_opt_dsv_d1) / Math.pow(beta_opt, 2);
        beta_ratio_ddepth = beta_ddepth / beta_opt;
        //

        /**
         * Net fuel loading
         */
        // wn_d'
        double wn_d_dw0_d1 = 0.;
        double wn_d_dw0_d2 = 0.;
        double wn_d_dw0_d3 = 0.;
        double wn_d_dsv_d1 = 0.;
        double wn_l_dw0_lh = 0.;
        double wn_l_dw0_lw = 0.;

        if (sw_d > 0) {
            wn_d_dw0_d1 = ((1 - s_t / 100) * sv_d1 / sw_d) * (2 * w0_d1 - sw2_d / sw_d);
            wn_d_dw0_d2 = ((1 - s_t / 100) * sv_d2 / sw_d) * (2 * w0_d2 - sw2_d / sw_d);
            wn_d_dw0_d3 = ((1 - s_t / 100) * sv_d3 / sw_d) * (2 * w0_d3 - sw2_d / sw_d);
            wn_d_dsv_d1 = ((1 - s_t / 100) * w0_d1 / sw_d) * (w0_d1 - sw2_d / sw_d);
        }

        // wn_l'
        if (sw_l > 0) {
            wn_l_dw0_lh = ((1 - s_t / 100) * sv_lh / sw_l) * (2 * w0_lh - sw2_l / sw_l);
            wn_l_dw0_lw = ((1 - s_t / 100) * sv_lw / sw_l) * (2 * w0_lw - sw2_l / sw_l);
        }

        /**
         * mineral damping coefficient
         */
        // W': ratio of "fine" fuel loadings, dead/living
        double W_dw0_d1 = 0.;
        double W_dw0_d2 = 0.;
        double W_dw0_d3 = 0.;
        double W_dw0_lh = 0.;
        double W_dw0_lw = 0.;
        double W_dsv_d1 = 0.;

        if (sumhl > 0) {
            W_dw0_d1 = 0.20482 * Math.exp(-452.76 / sv_d1) / sumhl;
            W_dw0_d2 = 0.20482 * Math.exp(-452.76 / sv_d2) / sumhl;
            W_dw0_d3 = 0.20482 * Math.exp(-452.76 / sv_d3) / sumhl;
            W_dw0_lh = -0.20482 * W * Math.exp(-1640.42 / sv_lh) / sumhl;
            W_dw0_lw = -0.20482 * W * Math.exp(-1640.42 / sv_lw) / sumhl;
            W_dsv_d1 = 452.76 * hn_d1 / (Math.pow(sv_d1, 2) * sumhl);
        }

        // Mf_dead': Moisture content of "fine" dead fuel
        double Mf_dead_dw0_d1 = 0.;
        double Mf_dead_dw0_d2 = 0.;
        double Mf_dead_dw0_d3 = 0.;
        double Mf_dead_dm_d1 = 0.;
        double Mf_dead_dm_d2 = 0.;
        double Mf_dead_dm_d3 = 0.;
        double Mf_dead_dsv_d1 = 0.;

        if (sumhd > 0) {
            Mf_dead_dw0_d1 = (Math.exp(-452.76 / sv_d1) / sumhd) * (m_d1 - (sumhdm / sumhd));
            Mf_dead_dw0_d2 = (Math.exp(-452.76 / sv_d2) / sumhd) * (m_d2 - (sumhdm / sumhd));
            Mf_dead_dw0_d3 = (Math.exp(-452.76 / sv_d3) / sumhd) * (m_d3 - (sumhdm / sumhd));

            Mf_dead_dm_d1 = hn_d1 / sumhd;
            Mf_dead_dm_d2 = hn_d2 / sumhd;
            Mf_dead_dm_d3 = hn_d3 / sumhd;

            Mf_dead_dsv_d1 = (452.76 * hn_d1 / (Math.pow(sv_d1, 2) * sumhd)) *
                    (m_d1 - (sumhdm / sumhd));
        }

        // Mx_live': Moisture of extinction of living fuel
        double Mx_live_dw0_d1 = 0.;
        double Mx_live_dw0_d2 = 0.;
        double Mx_live_dw0_d3 = 0.;
        double Mx_live_dw0_lh = 0.;
        double Mx_live_dw0_lw = 0.;
        double Mx_live_dsv_d1 = 0.;
        double Mx_live_dm_d1 = 0.;
        double Mx_live_dm_d2 = 0.;
        double Mx_live_dm_d3 = 0.;
        double Mx_live_dmx = 0.;

        Mx_live_dw0_d1 = 2.9 * W_dw0_d1 * (1 - Mf_dead / mx) - 2.9 * W * Mf_dead_dw0_d1 / mx;
        Mx_live_dw0_d2 = 2.9 * W_dw0_d2 * (1 - Mf_dead / mx) - 2.9 * W * Mf_dead_dw0_d2 / mx;
        Mx_live_dw0_d3 = 2.9 * W_dw0_d3 * (1 - Mf_dead / mx) - 2.9 * W * Mf_dead_dw0_d3 / mx;
        Mx_live_dsv_d1 = 2.9 * W_dsv_d1 * (1 - Mf_dead / mx) - 2.9 * W * Mf_dead_dsv_d1 / mx;

        if (sumhl > 0) {
            Mx_live_dm_d1 = -2.9 * hn_d1 / (sumhl * Mx_live);
            Mx_live_dm_d2 = -2.9 * hn_d2 / (sumhl * Mx_live);
            Mx_live_dm_d3 = -2.9 * hn_d3 / (sumhl * Mx_live);
        }

        Mx_live_dw0_lh = 2.9 * W_dw0_lh * (1 - Mf_dead / mx);
        Mx_live_dw0_lw = 2.9 * W_dw0_lw * (1 - Mf_dead / mx);

        Mx_live_dmx = 2.9 * W * Mf_dead / Math.pow(mx, 2);

        // moisture ratios
        double rm_d_dw0_d1 = 0.;
        double rm_d_dw0_d2 = 0.;
        double rm_d_dw0_d3 = 0.;
        double rm_d_dm_d1 = 0.;
        double rm_d_dm_d2 = 0.;
        double rm_d_dm_d3 = 0.;
        double rm_d_dsv_d1 = 0.;
        double rm_d_dmx = 0.;
        if (sw_d > 0) {
            // rm_d'
            rm_d_dw0_d1 = (sv_d1 / (sw_d * mx)) * (m_d1 - (swm_d / sw_d));
            rm_d_dw0_d2 = (sv_d2 / (sw_d * mx)) * (m_d2 - (swm_d / sw_d));
            rm_d_dw0_d3 = (sv_d3 / (sw_d * mx)) * (m_d3 - (swm_d / sw_d));
            rm_d_dsv_d1 = (w0_d1 / (sw_d * mx)) * (m_d1 - (swm_d / sw_d));

            rm_d_dm_d1 = sw_d1 / (sw_d * mx);
            rm_d_dm_d2 = sw_d2 / (sw_d * mx);
            rm_d_dm_d3 = sw_d3 / (sw_d * mx);

            rm_d_dmx = -1 * rm_d / mx;
        }

        double rm_l_dw0_d1 = 0.;
        double rm_l_dw0_d2 = 0.;
        double rm_l_dw0_d3 = 0.;
        double rm_l_dw0_lh = 0.;
        double rm_l_dw0_lw = 0.;
        double rm_l_dm_d1 = 0.;
        double rm_l_dm_d2 = 0.;
        double rm_l_dm_d3 = 0.;
        double rm_l_dm_lh = 0.;
        double rm_l_dm_lw = 0.;
        double rm_l_dsv_d1 = 0.;
        double rm_l_dmx = 0.;
        if ((sw_l > 0) && (Mx_live > 0)) {
            // rm_l'
            rm_l_dw0_lh = (sv_lh * m_lh - (swm_l * sv_lh / sw_l) -
                    (swm_l * Mx_live_dw0_lh / Mx_live)) / (sw_l * Mx_live);
            rm_l_dw0_lw = (sv_lw * m_lw - (swm_l * sv_lw / sw_l) -
                    (swm_l * Mx_live_dw0_lw / Mx_live)) / (sw_l * Mx_live);

            rm_l_dw0_d1 = -1 * rm_l * Mx_live_dw0_d1 / Mx_live;
            rm_l_dw0_d2 = -1 * rm_l * Mx_live_dw0_d2 / Mx_live;
            rm_l_dw0_d3 = -1 * rm_l * Mx_live_dw0_d3 / Mx_live;
            rm_l_dm_d1 = -1 * rm_l * Mx_live_dm_d1 / Mx_live;
            rm_l_dm_d2 = -1 * rm_l * Mx_live_dm_d2 / Mx_live;
            rm_l_dm_d3 = -1 * rm_l * Mx_live_dm_d3 / Mx_live;
            rm_l_dsv_d1 = -1 * rm_l * Mx_live_dsv_d1 / Mx_live;
            rm_l_dmx = -1 * rm_l * Mx_live_dmx / Mx_live;

            rm_l_dm_lh = sw_lh / (sw_l * Mx_live);
            rm_l_dm_lw = sw_lw / (sw_l * Mx_live);
        }

        // eta_Md': moisture damping coefficient dead fuels
        //
        double eta_Md_dw0_d1, eta_Md_dw0_d2, eta_Md_dw0_d3 = 0.;
        double eta_Md_dm_d1, eta_Md_dm_d2, eta_Md_dm_d3 = 0.;
        double eta_Md_dsv_d1, eta_Md_dmx = 0.;

        double et_dh = -2.59 + 10.22 * rm_d - 10.561 * Math.pow(rm_d, 2);
        eta_Md_dw0_d1 = rm_d_dw0_d1 * et_dh;
        eta_Md_dw0_d2 = rm_d_dw0_d2 * et_dh;
        eta_Md_dw0_d3 = rm_d_dw0_d3 * et_dh;
        eta_Md_dm_d1 = rm_d_dm_d1 * et_dh;
        eta_Md_dm_d2 = rm_d_dm_d2 * et_dh;
        eta_Md_dm_d3 = rm_d_dm_d3 * et_dh;
        eta_Md_dsv_d1 = rm_d_dsv_d1 * et_dh;
        eta_Md_dmx = rm_d_dmx * et_dh;

        // eta_Ml': moisture damping coefficient living fuels
        double eta_Ml_dw0_d1, eta_Ml_dw0_d2, eta_Ml_dw0_d3 = 0.;
        double eta_Ml_dw0_lh, eta_Ml_dw0_lw = 0.;
        double eta_Ml_dm_d1, eta_Ml_dm_d2, eta_Ml_dm_d3;
        double eta_Ml_dm_lh, eta_Ml_dm_lw = 0.;
        double eta_Ml_dsv_d1, eta_Ml_dmx = 0.;

        double et_lh = -2.59 + 10.22 * rm_l - 10.561 * Math.pow(rm_l, 2);
        eta_Ml_dw0_d1 = rm_l_dw0_d1 * et_lh;
        eta_Ml_dw0_d2 = rm_l_dw0_d2 * et_lh;
        eta_Ml_dw0_d3 = rm_l_dw0_d3 * et_lh;
        eta_Ml_dw0_lh = rm_l_dw0_lh * et_lh;
        eta_Ml_dw0_lw = rm_l_dw0_lw * et_lh;
        eta_Ml_dm_d1 = rm_l_dm_d1 * et_lh;
        eta_Ml_dm_d2 = rm_l_dm_d2 * et_lh;
        eta_Ml_dm_d3 = rm_l_dm_d3 * et_lh;
        eta_Ml_dm_lh = rm_l_dm_lh * et_lh;
        eta_Ml_dm_lw = rm_l_dm_lw * et_lh;
        eta_Ml_dsv_d1 = rm_l_dsv_d1 * et_lh;
        eta_Ml_dmx = rm_l_dmx * et_lh;

        //
        // eta_M': total moisture damping coefficient
        double eta_M_dw0_d1, eta_M_dw0_d2, eta_M_dw0_d3 = 0.;
        double eta_M_dw0_lh, eta_M_dw0_lw = 0.;
        double eta_M_dm_d1, eta_M_dm_d2, eta_M_dm_d3 = 0.;
        double eta_M_dm_lh, eta_M_dm_lw = 0.;
        double eta_M_dsv_d1, eta_M_dmx = 0.;
        eta_M_dw0_d1 = wn_d_dw0_d1 * eta_Md + wn_d * eta_Md_dw0_d1 +
                wn_l * eta_Ml_dw0_d1;
        eta_M_dw0_d2 = wn_d_dw0_d2 * eta_Md + wn_d * eta_Md_dw0_d2 +
                wn_l * eta_Ml_dw0_d1;
        eta_M_dw0_d3 = wn_d_dw0_d3 * eta_Md + wn_d * eta_Md_dw0_d3 +
                wn_l * eta_Ml_dw0_d1;
        eta_M_dsv_d1 = wn_d_dsv_d1 * eta_Md + wn_d * eta_Md_dsv_d1 +
                wn_l * eta_Ml_dsv_d1;

        eta_M_dw0_lh = wn_l_dw0_lh * eta_Ml + wn_l * eta_Ml_dw0_lh;
        eta_M_dw0_lw = wn_l_dw0_lw * eta_Ml + wn_l * eta_Ml_dw0_lw;

        eta_M_dm_d1 = wn_d * eta_Md_dm_d1 + wn_l * eta_Ml_dm_d1;
        eta_M_dm_d2 = wn_d * eta_Md_dm_d2 + wn_l * eta_Ml_dm_d2;
        eta_M_dm_d3 = wn_d * eta_Md_dm_d3 + wn_l * eta_Ml_dm_d3;
        eta_M_dmx = wn_d * eta_Md_dmx + wn_l * eta_Ml_dmx;

        eta_M_dm_lh = wn_l * eta_Ml_dm_lh;
        eta_M_dm_lw = wn_l * eta_Ml_dm_lw;

        /**
         * xi: propagating flux ratio
         */
        // xi'
        double xiz, xin;
        xiz = Math.exp((0.792 + 0.37597 * Math.sqrt(sigma)) * (beta + 0.1));
        xin = 192 + 0.0791 * sigma;
        //
        double xi_dw0_d1, xi_dw0_d2, xi_dw0_d3, xi_dw0_lh, xi_dw0_lw = 0.;
        double xi_ddepth, xi_dsv_d1 = 0.;
        double xiz_d, xin_d;
        xiz_d = xiz *
                ((0.187985 * sigma_dw0_d1 * (beta + 0.1) / Math.sqrt(sigma)) +
                beta_dw0_d1 * (0.792 + 0.376 * Math.sqrt(sigma)));
        xin_d = 0.0791 * sigma_dw0_d1;
        xi_dw0_d1 = (xiz_d * xin - xiz * xin_d) / Math.pow(xin, 2);

        xiz_d = xiz *
                ((0.187985 * sigma_dw0_d2 * (beta + 0.1) / Math.sqrt(sigma)) +
                beta_dw0_d2 * (0.792 + 0.376 * Math.sqrt(sigma)));
        xin_d = 0.0791 * sigma_dw0_d2;
        xi_dw0_d2 = (xiz_d * xin - xiz * xin_d) / Math.pow(xin, 2);

        xiz_d = xiz *
                ((0.187985 * sigma_dw0_d3 * (beta + 0.1) / Math.sqrt(sigma)) +
                beta_dw0_d3 * (0.792 + 0.376 * Math.sqrt(sigma)));
        xin_d = 0.0791 * sigma_dw0_d3;
        xi_dw0_d3 = (xiz_d * xin - xiz * xin_d) / Math.pow(xin, 2);

        xiz_d = xiz *
                ((0.187985 * sigma_dw0_lh * (beta + 0.1) / Math.sqrt(sigma)) +
                beta_dw0_lh * (0.792 + 0.376 * Math.sqrt(sigma)));
        xin_d = 0.0791 * sigma_dw0_lh;
        xi_dw0_lh = (xiz_d * xin - xiz * xin_d) / Math.pow(xin, 2);

        xiz_d = xiz *
                ((0.187985 * sigma_dw0_lw * (beta + 0.1) / Math.sqrt(sigma)) +
                beta_dw0_lw * (0.792 + 0.376 * Math.sqrt(sigma)));
        xin_d = 0.0791 * sigma_dw0_lw;
        xi_dw0_lw = (xiz_d * xin - xiz * xin_d) / Math.pow(xin, 2);

        xiz_d = xiz * (0.792 + 0.376 * Math.sqrt(sigma)) * beta_ddepth;
        xi_ddepth = xiz_d / xin;

        xiz_d = xiz * 0.187985 * sigma_dsv_d1 * (beta + 0.1) / Math.sqrt(sigma);
        xin_d = 0.0791 * sigma_dsv_d1;
        xi_dsv_d1 = (xiz_d * xin - xiz * xin_d) / Math.pow(xin, 2);
        ;

        /**
         * Gamma: reaction velocity
         */
        // A'
        double A_dw0_d1, A_dw0_d2, A_dw0_d3, A_dw0_lh, A_dw0_lw, A_dsv_d1 = 0.;

        A_dw0_d1 = -269.461389 * sigma_dw0_d1 / Math.pow(sigma, 1.7913);
        A_dw0_d2 = -269.461389 * sigma_dw0_d2 / Math.pow(sigma, 1.7913);
        A_dw0_d3 = -269.461389 * sigma_dw0_d3 / Math.pow(sigma, 1.7913);
        A_dw0_lh = -269.461389 * sigma_dw0_lh / Math.pow(sigma, 1.7913);
        A_dw0_lw = -269.461389 * sigma_dw0_lw / Math.pow(sigma, 1.7913);
        A_dsv_d1 = -269.461389 * sigma_dsv_d1 / Math.pow(sigma, 1.7913);

        // gamma_max: maximum reaction velocity
        double gamma_max_dw0_d1, gamma_max_dw0_d2, gamma_max_dw0_d3 = 0.;
        double gamma_max_dw0_lh, gamma_max_dw0_lw, gamma_max_dsv_d1 = 0.;

        double gamma_h = Math.sqrt(sigma) /
                Math.pow(29700 + 0.5997 * Math.pow(sigma, 1.5), 2);
        gamma_max_dw0_d1 = -7496.874 * sigma_dw0_d1 * gamma_h;
        gamma_max_dw0_d2 = -7496.874 * sigma_dw0_d2 * gamma_h;
        gamma_max_dw0_d3 = -7496.874 * sigma_dw0_d3 * gamma_h;
        gamma_max_dw0_lh = -7496.874 * sigma_dw0_lh * gamma_h;
        gamma_max_dw0_lw = -7496.874 * sigma_dw0_lw * gamma_h;
        gamma_max_dsv_d1 = -7496.874 * sigma_dsv_d1 * gamma_h;

        // gamma': potential reaction velocity
        double gamma_dw0_d1, gamma_dw0_d2, gamma_dw0_d3, gamma_dw0_lh = 0.;
        double gamma_dw0_lw, gamma_dsv_d1, gamma_ddepth = 0.;
        double g2;
        double g1 = Math.log(beta_ratio) + 1 - beta_ratio;
        double g3 = Math.pow(beta_ratio, A) * Math.exp(A * (1 - beta_ratio));

        g2 = (1 / beta_ratio - 1) * beta_ratio_dw0_d1;
        gamma_dw0_d1 = g3 * (gamma_max_dw0_d1 + gamma_max * (A_dw0_d1 * g1 - A * g2));

        g2 = (1 / beta_ratio - 1) * beta_ratio_dw0_d2;
        gamma_dw0_d2 = g3 * (gamma_max_dw0_d2 + gamma_max * (A_dw0_d2 * g1 - A * g2));

        g2 = (1 / beta_ratio - 1) * beta_ratio_dw0_d3;
        gamma_dw0_d3 = g3 * (gamma_max_dw0_d3 + gamma_max * (A_dw0_d3 * g1 - A * g2));

        g2 = (1 / beta_ratio - 1) * beta_ratio_dw0_lh;
        gamma_dw0_lh = g3 * (gamma_max_dw0_lh + gamma_max * (A_dw0_lh * g1 - A * g2));

        g2 = (1 / beta_ratio - 1) * beta_ratio_dw0_lw;
        gamma_dw0_lw = g3 * (gamma_max_dw0_lw + gamma_max * (A_dw0_lw * g1 - A * g2));

        g2 = (1 / beta_ratio - 1) * beta_ratio_dsv_d1;
        gamma_dsv_d1 = g3 * (gamma_max_dsv_d1 + gamma_max * (A_dsv_d1 * g1 - A * g2));

        gamma_ddepth = gamma_max * g3 * A * beta_ratio_ddepth * (1 / beta_ratio - 1);

        // I_r': reaction intensity
        double I_r_dw0_d1, I_r_dw0_d2, I_r_dw0_d3, I_r_dw0_lh, I_r_dw0_lw = 0.;
        double I_r_dsv_d1, I_r_ddepth, I_r_dmx = 0.;
        double I_r_dm_d1, I_r_dm_d2, I_r_dm_d3, I_r_dm_lh, I_r_dm_lw = 0.;

        I_r_dw0_d1 = heat * eta_s * ((gamma_dw0_d1 * eta_M) + (gamma * eta_M_dw0_d1));
        I_r_dw0_d2 = heat * eta_s * ((gamma_dw0_d2 * eta_M) + (gamma * eta_M_dw0_d2));
        I_r_dw0_d3 = heat * eta_s * ((gamma_dw0_d3 * eta_M) + (gamma * eta_M_dw0_d3));
        I_r_dw0_lh = heat * eta_s * ((gamma_dw0_lh * eta_M) + (gamma * eta_M_dw0_lh));
        I_r_dw0_lw = heat * eta_s * ((gamma_dw0_lw * eta_M) + (gamma * eta_M_dw0_lw));
        I_r_dsv_d1 = heat * eta_s * ((gamma_dsv_d1 * eta_M) + (gamma * eta_M_dsv_d1));

        I_r_ddepth = heat * eta_s * (gamma_ddepth * eta_M);

        I_r_dm_d1 = heat * eta_s * (gamma * eta_M_dm_d1);
        I_r_dm_d2 = heat * eta_s * (gamma * eta_M_dm_d2);
        I_r_dm_d3 = heat * eta_s * (gamma * eta_M_dm_d3);
        I_r_dm_lh = heat * eta_s * (gamma * eta_M_dm_lh);
        I_r_dm_lw = heat * eta_s * (gamma * eta_M_dm_lw);
        I_r_dmx = heat * eta_s * (gamma * eta_M_dmx);

        // phi': slope factor
        double phi_s_dw0_d1, phi_s_dw0_d2, phi_s_dw0_d3, phi_s_dw0_lh = 0.;
        double phi_s_dw0_lw, phi_s_ddepth, phi_s_dslp = 0.;
        double phi_h = -1.5825 * Math.pow(beta, -1.3) * Math.pow(tan_slp, 2);
        phi_s_dw0_d1 = phi_h * beta_dw0_d1;
        phi_s_dw0_d2 = phi_h * beta_dw0_d2;
        phi_s_dw0_d3 = phi_h * beta_dw0_d3;
        phi_s_dw0_lh = phi_h * beta_dw0_lh;
        phi_s_dw0_lw = phi_h * beta_dw0_lw;
        phi_s_ddepth = phi_h * beta_ddepth;

        if (slp > 0) {
            phi_s_dslp = 2 * phi_s * (1 + Math.pow(tan_slp, 2)) / tan_slp;
        }

        // wind factor
        // B'
        double B_dw0_d1, B_dw0_d2, B_dw0_d3, B_dw0_lh, B_dw0_lw, B_dsv_d1 = 0.;
        double B_dh = 0.00718092 / Math.pow(sigma, 0.46);
        B_dw0_d1 = sigma_dw0_d1 * B_dh;
        B_dw0_d2 = sigma_dw0_d2 * B_dh;
        B_dw0_d3 = sigma_dw0_d3 * B_dh;
        B_dw0_lh = sigma_dw0_lh * B_dh;
        B_dw0_lw = sigma_dw0_lw * B_dh;
        B_dsv_d1 = sigma_dsv_d1 * B_dh;
        // C'
        double C_dw0_d1, C_dw0_d2, C_dw0_d3, C_dw0_lh, C_dw0_lw, C_dsv_d1 = 0.;
        double C_dh = -0.284267115 * Math.exp(-0.06919 * Math.pow(sigma, 0.55)) /
                Math.pow(sigma, 0.45);
        C_dw0_d1 = sigma_dw0_d1 * C_dh;
        C_dw0_d2 = sigma_dw0_d2 * C_dh;
        C_dw0_d3 = sigma_dw0_d3 * C_dh;
        C_dw0_lh = sigma_dw0_lh * C_dh;
        C_dw0_lw = sigma_dw0_lw * C_dh;
        C_dsv_d1 = sigma_dsv_d1 * C_dh;
        // E'
        double E_dw0_d1, E_dw0_d2, E_dw0_d3, E_dw0_lh, E_dw0_lw, E_dsv_d1 = 0.;
        double E_dh = -0.000078221 * Math.exp(-0.0001094 * sigma);
        E_dw0_d1 = sigma_dw0_d1 * E_dh;
        E_dw0_d2 = sigma_dw0_d2 * E_dh;
        E_dw0_d3 = sigma_dw0_d3 * E_dh;
        E_dw0_lh = sigma_dw0_lh * E_dh;
        E_dw0_lw = sigma_dw0_lw * E_dh;
        E_dsv_d1 = sigma_dsv_d1 * E_dh;

        // phi_w'
        /**
         * if wsp is zero there cannot be any variation of wsp.
         */
        double phi_w_dw0_d1 = 0.;
        double phi_w_dw0_d2 = 0.;
        double phi_w_dw0_d3 = 0.;
        double phi_w_dw0_lh = 0.;
        double phi_w_dw0_lw = 0.;
        double phi_w_dsv_d1 = 0.;
        double phi_w_dwsp = 0.;
        double phi_w_ddepth = 0.;
        double phi_a, phi_b, phi_c;
        double wsp_B, wsp_L, bet_E, bet_L;

        if (wsp > 0.) {

            wsp_B = Math.pow(3.281 * 60 * wsp, B);
            wsp_L = Math.log(3.281 * 60 * wsp);
            bet_E = Math.pow(beta_ratio, -E);
            bet_L = Math.log(beta_ratio);

            phi_a = C_dw0_d1 * wsp_B * bet_E;
            phi_b = C * wsp_B * B_dw0_d1 * wsp_L * bet_E;
            phi_c = C * wsp_B * bet_E *
                    (-E_dw0_d1 * bet_L - E * beta_ratio_dw0_d1 / beta_ratio);
            phi_w_dw0_d1 = phi_a + phi_b + phi_c;

            phi_a = C_dw0_d2 * wsp_B * bet_E;
            phi_b = C * wsp_B * B_dw0_d2 * wsp_L * bet_E;
            phi_c = C * wsp_B * bet_E *
                    (-E_dw0_d2 * bet_L - E * beta_ratio_dw0_d2 / beta_ratio);
            phi_w_dw0_d2 = phi_a + phi_b + phi_c;

            phi_a = C_dw0_d3 * wsp_B * bet_E;
            phi_b = C * wsp_B * B_dw0_d3 * wsp_L * bet_E;
            phi_c = C * wsp_B * bet_E *
                    (-E_dw0_d3 * bet_L - E * beta_ratio_dw0_d3 / beta_ratio);
            phi_w_dw0_d3 = phi_a + phi_b + phi_c;

            phi_a = C_dw0_lh * wsp_B * bet_E;
            phi_b = C * wsp_B * B_dw0_lh * wsp_L * bet_E;
            phi_c = C * wsp_B * bet_E *
                    (-E_dw0_lh * bet_L - E * beta_ratio_dw0_lh / beta_ratio);
            phi_w_dw0_lh = phi_a + phi_b + phi_c;

            phi_a = C_dw0_lw * wsp_B * bet_E;
            phi_b = C * wsp_B * B_dw0_lw * wsp_L * bet_E;
            phi_c = C * wsp_B * bet_E *
                    (-E_dw0_lw * bet_L - E * beta_ratio_dw0_lw / beta_ratio);
            phi_w_dw0_lw = phi_a + phi_b + phi_c;

            phi_a = C_dsv_d1 * wsp_B * bet_E;
            phi_b = C * wsp_B * B_dsv_d1 * wsp_L * bet_E;
            phi_c = C * wsp_B * bet_E *
                    (-E_dsv_d1 * bet_L - (E * beta_ratio_dsv_d1 / beta_ratio));
            phi_w_dsv_d1 = phi_a + phi_b + phi_c;

            phi_w_dwsp = phi_w * B / wsp;

            phi_w_ddepth = phi_w * -E * beta_ratio_ddepth / beta_ratio;

        }

        // vl'
        double vl_dw0_d1 = 0.;
        double vl_dw0_d2 = 0.;
        double vl_dw0_d3 = 0.;
        double vl_dw0_lh = 0.;
        double vl_dw0_lw = 0.;
        double vl_ddepth = 0.;
        double vl_dasp = 0.;
        double vl_dslp = 0.;
        double vl_dwsp = 0.;
        double vl_dwdr = 0.;
        double vl_dsv_d1 = 0.;
        double vx_d1 = 0.;
        double vy_dw0_d1 = 0.;
        double vy_dw0_d2 = 0.;
        double vy_dw0_d3 = 0.;
        double vy_dw0_lh = 0.;
        double vy_dw0_lw = 0.;
        double vy_dsv_d1 = 0.;
        double vy_ddepth = 0.;
        double vy_dwsp = 0.;
        double vy_dwdr = 0.;
        double vy_dslp = 0.;
        double vy_dasp = 0.;

        // special
        double vx_dw0_d1;
        double vx_dw0_d2 = 0.;
        double vx_dw0_d3 = 0.;
        double vx_dw0_lh = 0.;
        double vx_dw0_lw = 0.;
        double vx_dsv_d1 = 0.;
        double vx_ddepth = 0.;
        double vx_dwsp = 0.;
        double vx_dwdr = 0.;
        double vx_dslp = 0.;
        double vx_dasp = 0.;

        if (vl > 0.) {
            vx_dw0_d1 = phi_s_dw0_d1 + phi_w_dw0_d1 * cos_splitRad;
            vy_dw0_d1 = phi_w_dw0_d1 * sin_splitRad;
            vl_dw0_d1 = (vx_dw0_d1 * vx + vy * vy_dw0_d1) / vl;

            vx_dw0_d2 = phi_s_dw0_d2 + phi_w_dw0_d2 * cos_splitRad;
            vy_dw0_d2 = phi_w_dw0_d2 * sin_splitRad;
            vl_dw0_d2 = (vx_dw0_d2 * vx + vy * vy_dw0_d2) / vl;

            vx_dw0_d3 = phi_s_dw0_d3 + phi_w_dw0_d3 * cos_splitRad;
            vy_dw0_d3 = phi_w_dw0_d3 * sin_splitRad;
            vl_dw0_d3 = (vx_dw0_d3 * vx + vy * vy_dw0_d3) / vl;

            vx_dw0_lh = phi_s_dw0_lh + phi_w_dw0_lh * cos_splitRad;
            vy_dw0_lh = phi_w_dw0_lh * sin_splitRad;
            vl_dw0_lh = (vx_dw0_lh * vx + vy * vy_dw0_lh) / vl;

            vx_dw0_lw = phi_s_dw0_lw + phi_w_dw0_lw * cos_splitRad;
            vy_dw0_lw = phi_w_dw0_lw * sin_splitRad;
            vl_dw0_lw = (vx_dw0_lw * vx + vy * vy_dw0_lw) / vl;

            vx_ddepth = phi_s_ddepth + phi_w_ddepth * cos_splitRad;
            vy_ddepth = phi_w_ddepth * sin_splitRad;
            vl_ddepth = (vx_ddepth * vx + vy * vy_ddepth) / vl;

            vx_dsv_d1 = phi_w_dsv_d1 * cos_splitRad;
            vy_dsv_d1 = phi_w_dsv_d1 * sin_splitRad;
            vl_dsv_d1 = (vx_dsv_d1 * vx + vy * vy_dsv_d1) / vl;

            vx_dasp = -phi_w * sin_splitRad;
            vy_dasp = phi_w * cos_splitRad;
            vl_dasp = (vx_dasp * vx + vy * vy_dasp) / vl;

            vx_dslp = phi_s_dslp;
            vl_dslp = vx_dslp * vx / vl;

            vx_dwdr = -phi_w * sin_splitRad;
            vy_dwdr = phi_w * cos_splitRad;
            vl_dwdr = (vx_dwdr * vx + vy * vy_dwdr) / vl;

            vx_dwsp = phi_w_dwsp * cos_splitRad;
            vy_dwsp = phi_w_dwsp * sin_splitRad;
            vl_dwsp = (vx_dwsp * vx + vy * vy_dwsp) / vl;

        }

        /**
         * Spread Direction
         */
        // sdr'
        double sdr_dw0_d1 = 0.;
        double sdr_dw0_d2 = 0.;
        double sdr_dw0_d3 = 0.;
        double sdr_dw0_lh = 0.;
        double sdr_dw0_lw = 0.;
        double sdr_dsv_d1 = 0.;
        double sdr_ddepth = 0.;
        double sdr_dwsp = 0.;
        double sdr_dwdr = 0.;
        double sdr_dslp = 0.;
        double sdr_dasp = 0.;
        double sdr_A = 0.;

        if (vl > 0.) {
            sdr_A = vl * Math.sqrt(Math.pow(vl, 2) - Math.pow(vy, 2));
            sdr_dw0_d1 = (vy_dw0_d1 * vl - vy * vl_dw0_d1) / sdr_A;
            sdr_dw0_d2 = (vy_dw0_d2 * vl - vy * vl_dw0_d2) / sdr_A;
            sdr_dw0_d3 = (vy_dw0_d3 * vl - vy * vl_dw0_d3) / sdr_A;
            sdr_dw0_lh = (vy_dw0_lh * vl - vy * vl_dw0_lh) / sdr_A;
            sdr_dw0_lw = (vy_dw0_lw * vl - vy * vl_dw0_lw) / sdr_A;
            sdr_dsv_d1 = (-vy * vl_dsv_d1) / sdr_A;
            sdr_ddepth = (vy_ddepth * vl - vy * vl_ddepth) / sdr_A;
            sdr_dwsp = (vy_dwsp * vl - vy * vl_dwsp) / sdr_A;
            sdr_dwdr = (vy_dwdr * vl - vy * vl_dwdr) / sdr_A;
            sdr_dslp = (-vy * vl_dslp) / sdr_A;
            sdr_dasp = (vy_dasp * vl - vy * vl_dasp) / sdr_A;
        }

        // parse to sdr_d-Array
        sdr_d[v.indexOf("w0_d1")] = sdr_dw0_d1;
        sdr_d[v.indexOf("w0_d2")] = sdr_dw0_d2;
        sdr_d[v.indexOf("w0_d3")] = sdr_dw0_d3;
        sdr_d[v.indexOf("w0_lh")] = sdr_dw0_lh;
        sdr_d[v.indexOf("w0_lw")] = sdr_dw0_lw;
        sdr_d[v.indexOf("sv_d1")] = sdr_dsv_d1;
        sdr_d[v.indexOf("depth")] = sdr_ddepth;
        sdr_d[v.indexOf("wsp")] = sdr_dwsp;
        sdr_d[v.indexOf("wdr")] = sdr_dwdr;
        sdr_d[v.indexOf("slp")] = sdr_dslp;
        sdr_d[v.indexOf("asp")] = sdr_dasp;

        /**
         * Effectiv Windspeed:
         */
        // efw'
        double efw_dw0_d1 = 0.;
        double efw_dw0_d2 = 0.;
        double efw_dw0_d3 = 0.;
        double efw_dw0_lh = 0.;
        double efw_dw0_lw = 0.;
        double efw_dsv_d1 = 0.;
        double efw_ddepth = 0.;
        double efw_dwsp = 0.;
        double efw_dwdr = 0.;
        double efw_dslp = 0.;
        double efw_dasp = 0.;
        double efw_A = 0.;
        double efw_log = 0.;
        if (wsp > 0.) {
            efw_log = Math.log(vl / (C * Math.pow(beta_ratio, -E)));
            efw_A = efw / (Math.pow(B, 2) * C * beta_ratio * vl);

            efw_dw0_d1 = efw_A * (-B_dw0_d1 * efw_log * C * beta_ratio * vl - B * C_dw0_d1 * beta_ratio * vl + B * C * E_dw0_d1 * Math.log(beta_ratio) * beta_ratio * vl + B * C * E * beta_ratio_dw0_d1 * vl + B * C * beta_ratio * vl_dw0_d1);

            efw_dw0_d2 = efw_A * (-B_dw0_d1 * efw_log * C * beta_ratio * vl - B * C_dw0_d1 * beta_ratio * vl + B * C * E_dw0_d1 * Math.log(beta_ratio) * beta_ratio * vl + B * C * E * beta_ratio_dw0_d1 * vl + B * C * beta_ratio * vl_dw0_d1);

            efw_dw0_d3 = efw_A * (-B_dw0_d3 * efw_log * C * beta_ratio * vl - B * C_dw0_d3 * beta_ratio * vl + B * C * E_dw0_d3 * Math.log(beta_ratio) * beta_ratio * vl + B * C * E * beta_ratio_dw0_d3 * vl + B * C * beta_ratio * vl_dw0_d3);

            efw_dw0_lh = efw_A * (-B_dw0_lh * efw_log * C * beta_ratio * vl - B * C_dw0_lh * beta_ratio * vl + B * C * E_dw0_lh * Math.log(beta_ratio) * beta_ratio * vl + B * C * E * beta_ratio_dw0_lh * vl + B * C * beta_ratio * vl_dw0_lh);

            efw_dw0_lw = efw_A * (-B_dw0_lw * efw_log * C * beta_ratio * vl - B * C_dw0_lw * beta_ratio * vl + B * C * E_dw0_lw * Math.log(beta_ratio) * beta_ratio * vl + B * C * E * beta_ratio_dw0_lw * vl + B * C * beta_ratio * vl_dw0_lw);

            efw_dsv_d1 = efw_A * (-B_dsv_d1 * efw_log * C * beta_ratio * vl - B * C_dsv_d1 * beta_ratio * vl + B * C * E_dsv_d1 * Math.log(beta_ratio) * beta_ratio * vl + B * C * E * beta_ratio_dsv_d1 * vl + B * C * beta_ratio * vl_dsv_d1);

            // depth
            efw_ddepth = efw * (vl_ddepth * beta_ratio + vl * E * beta_ratio_ddepth) / (B * vl * beta_ratio);

            // slp,asp,wsp,wdr
            efw_dslp = efw * vl_dslp / (B * vl);
            efw_dasp = efw * vl_dasp / (B * vl);
            efw_dwsp = efw * vl_dwsp / (B * vl);
            efw_dwdr = efw * vl_dwdr / (B * vl);
        }

        // fill efw_d-Array...
        efw_d[v.indexOf("w0_d1")] = efw_dw0_d1;
        efw_d[v.indexOf("w0_d2")] = efw_dw0_d2;
        efw_d[v.indexOf("w0_d3")] = efw_dw0_d3;
        efw_d[v.indexOf("w0_lh")] = efw_dw0_lh;
        efw_d[v.indexOf("w0_lw")] = efw_dw0_lw;
        efw_d[v.indexOf("sv_d1")] = efw_dsv_d1;
        efw_d[v.indexOf("depth")] = efw_ddepth;
        efw_d[v.indexOf("wsp")] = efw_dwsp;
        efw_d[v.indexOf("wdr")] = efw_dwdr;
        efw_d[v.indexOf("slp")] = efw_dslp;
        efw_d[v.indexOf("asp")] = efw_dasp;

        /**
         * Heatsink: epsilon*Qignintion
         */
        // hsk'
        double hskz_dw;
        hskz_dw = sv_d1 / sw_t * (eps_d1 * q_d1 - hskz / sw_t);
        hsk_dw0_d1 = ((rho_b_dw0_d1 * hskz + rho_b * hskz_dw) * sw_t -
                rho_b * hskz * sv_d1) / Math.pow(sw_t, 2);
        hskz_dw = sv_d2 / sw_t * (eps_d2 * q_d2 - hskz / sw_t);
        hsk_dw0_d2 = ((rho_b_dw0_d2 * hskz + rho_b * hskz_dw) * sw_t -
                rho_b * hskz * sv_d2) / Math.pow(sw_t, 2);
        hskz_dw = sv_d3 / sw_t * (eps_d3 * q_d3 - hskz / sw_t);
        hsk_dw0_d3 = ((rho_b_dw0_d3 * hskz + rho_b * hskz_dw) * sw_t -
                rho_b * hskz * sv_d3) / Math.pow(sw_t, 2);
        hskz_dw = sv_lh / sw_t * (eps_lh * q_lh - hskz / sw_t);
        hsk_dw0_lh = ((rho_b_dw0_lh * hskz + rho_b * hskz_dw) * sw_t -
                rho_b * hskz * sv_lh) / Math.pow(sw_t, 2);
        hskz_dw = sv_lw / sw_t * (eps_lw * q_lw - hskz / sw_t);
        hsk_dw0_lw = ((rho_b_dw0_lw * hskz + rho_b * hskz_dw) * sw_t -
                rho_b * hskz * sv_lw) / Math.pow(sw_t, 2);

        hsk_dm_d1 = rho_b * 25.957 * sw_d1 * eps_d1 / sw_t;
        hsk_dm_d2 = rho_b * 25.957 * sw_d2 * eps_d2 / sw_t;
        hsk_dm_d3 = rho_b * 25.957 * sw_d3 * eps_d3 / sw_t;
        hsk_dm_lh = rho_b * 25.957 * sw_lh * eps_lh / sw_t;
        hsk_dm_lw = rho_b * 25.957 * sw_lw * eps_lw / sw_t;

        // hsk  = rho_b * hskz / sw_t;
        // debuged 26.9.2000!!
        hskz_dw = w0_d1 * eps_d1 * q_d1 * (1 + 452.76 / sv_d1);
        hsk_dsv_d1 = rho_b * ((hskz_dw - hskz * w0_d1 / sw_t) / sw_t);

        hsk_ddepth = rho_b_ddepth * hskz / sw_t;

        /**
        --------------------------------------------------------------
        forward rate of spread
         */
        // ros'
        double cz1, cz2, cz3, cz4;

        cz1 = I_r_dw0_d1 * xi * (1 + vl);
        cz2 = I_r * xi_dw0_d1 * (1 + vl);
        cz3 = I_r * xi * vl_dw0_d1;
        cz4 = (cz1 + cz2 + cz3);
        ros_dw0_d1 = (cz4 - ros * hsk_dw0_d1) / hsk;

        cz1 = I_r_dw0_d2 * xi * (1 + vl);
        cz2 = I_r * xi_dw0_d2 * (1 + vl);
        cz3 = I_r * xi * vl_dw0_d2;
        cz4 = (cz1 + cz2 + cz3);
        ros_dw0_d2 = (cz4 - ros * hsk_dw0_d2) / hsk;

        cz1 = I_r_dw0_d3 * xi * (1 + vl);
        cz2 = I_r * xi_dw0_d3 * (1 + vl);
        cz3 = I_r * xi * vl_dw0_d3;
        cz4 = (cz1 + cz2 + cz3);
        ros_dw0_d3 = (cz4 - ros * hsk_dw0_d3) / hsk;

        cz1 = I_r_dw0_lh * xi * (1 + vl);
        cz2 = I_r * xi_dw0_lh * (1 + vl);
        cz3 = I_r * xi * vl_dw0_lh;
        cz4 = (cz1 + cz2 + cz3);
        ros_dw0_lh = (cz4 - ros * hsk_dw0_lh) / hsk;

        cz1 = I_r_dw0_lw * xi * (1 + vl);
        cz2 = I_r * xi_dw0_lw * (1 + vl);
        cz3 = I_r * xi * vl_dw0_lw;
        cz4 = (cz1 + cz2 + cz3);
        ros_dw0_lw = (cz4 - ros * hsk_dw0_lw) / hsk;

        cz1 = I_r_dsv_d1 * xi * (1 + vl);
        cz2 = I_r * xi_dsv_d1 * (1 + vl);
        cz3 = I_r * xi * vl_dsv_d1;
        cz4 = (cz1 + cz2 + cz3);
        ros_dsv_d1 = (cz4 - ros * hsk_dsv_d1) / hsk;

        cz1 = I_r_ddepth * xi * (1 + vl);
        cz2 = I_r * xi_ddepth * (1 + vl);
        cz3 = I_r * xi * vl_ddepth;
        cz4 = (cz1 + cz2 + cz3);
        ros_ddepth = (cz4 - ros * hsk_ddepth) / hsk;

        cz1 = I_r_dm_d1 * xi * (1 + vl);
        ros_dm_d1 = (cz1 * hsk - I_r * xi * (1 + vl) * hsk_dm_d1) / Math.pow(hsk, 2);
        cz1 = I_r_dm_d2 * xi * (1 + vl);
        ros_dm_d2 = (cz1 * hsk - I_r * xi * (1 + vl) * hsk_dm_d2) / Math.pow(hsk, 2);
        cz1 = I_r_dm_d3 * xi * (1 + vl);
        ros_dm_d3 = (cz1 * hsk - I_r * xi * (1 + vl) * hsk_dm_d3) / Math.pow(hsk, 2);
        cz1 = I_r_dm_lh * xi * (1 + vl);
        ros_dm_lh = (cz1 * hsk - I_r * xi * (1 + vl) * hsk_dm_lh) / Math.pow(hsk, 2);
        cz1 = I_r_dm_lw * xi * (1 + vl);
        ros_dm_lw = (cz1 * hsk - I_r * xi * (1 + vl) * hsk_dm_lw) / Math.pow(hsk, 2);
        ros_dslp = I_r * xi * vl_dslp / hsk;
        ros_dasp = I_r * xi * vl_dasp / hsk;
        ros_dwsp = I_r * xi * vl_dwsp / hsk;
        ros_dwdr = I_r * xi * vl_dwdr / hsk;
        ros_dmx = I_r_dmx * xi * (1 + vl) / hsk;

        // fill ros_d-Array....
        ros_d[0] = ros_dw0_d1;
        ros_d[1] = ros_dw0_d2;
        ros_d[2] = ros_dw0_d3;
        ros_d[3] = ros_dw0_lh;
        ros_d[4] = ros_dw0_lw;
        ros_d[5] = ros_dm_d1;
        ros_d[6] = ros_dm_d2;
        ros_d[7] = ros_dm_d3;
        ros_d[8] = ros_dm_lh;
        ros_d[9] = ros_dm_lw;
        ros_d[10] = ros_dsv_d1;
        ros_d[11] = ros_ddepth;
        ros_d[12] = ros_dmx;
        ros_d[13] = ros_dwsp;
        ros_d[14] = ros_dwdr;
        ros_d[15] = ros_dslp;
        ros_d[16] = ros_dasp;

        isCalculated = true;
        // fini le calcDerivs()
    }
}

