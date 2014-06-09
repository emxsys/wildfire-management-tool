/*
 * Copyright (c) 2009-2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wildfire.behave;

/*
 * Original version: Copyright (C) 2001, Andreas Bachmann
 * send comments to:
 *      bachmann@geo.unizh.ch
 * or by letter:
 *      University of Zurich
 *      Deptartment of Geography
 *      Geographic Information Systems Division
 *      Winterthurerstr. 190
 *      8057 Zurich
 *      Switzerland
 */
import java.util.logging.Logger;
import java.util.*;
import static java.lang.Math.*;

/**
 * Calculates the fire behavior according to the Rothermel model.
 *
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
 * @author Andreas Bachmann
 * @version $Id: Behave.java 209 2012-09-05 23:09:19Z bdschubert $
 */
@SuppressWarnings("unchecked")
public class Behave {

    private static final Logger LOG = Logger.getLogger(Behave.class.getName());

    /** links from var-names to indices.. */
    static final public List<String> v = new ArrayList<>(17);

    /** Establish input variable names */
    static {
        v.add("w0_d1");
        v.add("w0_d2");
        v.add("w0_d3");
        v.add("w0_lh");
        v.add("w0_lw");
        v.add("m_d1");
        v.add("m_d2");
        v.add("m_d3");
        v.add("m_lh");
        v.add("m_lw");
        v.add("sv_d1");
        v.add("depth");
        v.add("mx");
        v.add("wsp");
        v.add("wdr");
        v.add("slp");
        v.add("asp");
    }
    static public double noData[] = new double[17];
    /**
     * INSTANCE VARS
     */
    private boolean hasFuel = false;
    private boolean hasNodata = false;
    public boolean isCalculated = false;
    public boolean canDerive = true;
    public Map<String, Double> varHashMap = new HashMap<>(25);
    public Map<String, Double> resHashMap = new HashMap<>(10);
    public Map<String, Double> resHashMapNoWS = new HashMap<>(10);
    // ---------------------------------
    // Rothermel's model input variables
    // ---------------------------------
    public String fuelModelCode;
    /** Input: Fuel model number */
    public int fuelModel = 0;
    /** Input: Dynamic fuel model */
    public boolean isDynamic = false;
    /** Input: Dead 1hr fuel loading [kg/m2] */
    public double w0_d1 = 0.;
    /** Input: Dead 10hr fuel loading [kg/m2] */
    public double w0_d2 = 0.;
    /** Input: Dead 100hr fuel loading [kg/m2] */
    public double w0_d3 = 0.;
    /** Derived: dead herbaceous fuel loading for dynamic fuel models [kg/m2] */
    protected double w0_dh = 0.;
    /** Input: Live herbaceous 1hr fuel loading [kg/m2] */
    public double w0_lh = 0.;
    /** Input: Live woody fuel loading [kg/m2] */
    public double w0_lw = 0.;
    /** Input: Dead 1hr fuel surface to volume ratio [1/m] */
    public double sv_d1 = 0.;
    /** Input: Dead 10hr fuel surface to volume ratio [1/m] */
    public double sv_d2 = 357.6115;     // Albini's constant
    /** Input: Dead 100hr fuel surface to volume ratio [1/m] */
    public double sv_d3 = 98.4252;      // Albini's constant
    /** Input: Live herbaceous fuel surface to volume ratio [1/m] */
    public double sv_lh = 4921.2598;    // Albini's constant
    /** Input: Live woody fuel surface to volume ratio [1/m] */
    public double sv_lw = 4921.2598;    // Albini's constant
    /** Input: Fuel bed depth (delta) [m] */
    public double depth = 0.;
    /** Input: Particle density (rho) [kg/m3] */
    public double rho_p = 512.72341;    // Albini's constant
    /** Input: Particle low heat content [kJ/kg] */
    public double heat = 18606.70194;   // Albini's constant
    /** Input: Total mineral content [%] */
    public double s_t = 5.5;            // Albini's constant
    /** Input: Effective mineral content [%] */
    public double s_e = 1.0;            // Albini's constant
    /** Input: Moisture of extinction, dead fuel [%] */
    public double mx = 0.;
    /** Input: Dead 1hr fuel moisture [%] */
    public double m_d1 = 0.;
    /** Input: Dead 10hr fuel moisture [%] */
    public double m_d2 = 0.;
    /** Input: Dead 100hr fuel moisture [%] */
    public double m_d3 = 0.;
    /** Input: Live herbaceous fuel moisture [%] */
    public double m_lh = 0.;
    /** Input: Live woody fuel moisture [%] */
    public double m_lw = 0.;
    /** Input: Wind speed [m/s] */
    public double wsp = 0.;
    /** Input: Wind dir [Degree], Northern wind = 0.0! */
    public double wdr = 0.;
    /** Input: Slope [Degree] */
    public double slp = 0.;
    /** Input: Aspect [Degree] south facing = 180 ! */
    public double asp = 0.;
    // -----------------------
    // additional variables...
    // -----------------------
    /** bulk density [kg/m3] */
    protected double rho_b = 0.;
    /** mean packing ratio */
    protected double beta = 0.;
    /** optimal packing ratio */
    protected double beta_opt = 0.;
    /** ratio mean/optimal packing ratio */
    protected double beta_ratio = 0.;
    /** net fuel loading */
    private double w_n = 0.;
    /** mineral damping coefficient */
    protected double eta_s = 0.;
    /** moisture damping coefficient */
    protected double eta_M = 0.;
    /** propagating flux ratio */
    protected double xi = 0.;
    /** exponent used in reaction velocity */
    protected double A = 0.;
    /** potential reaction velocity [1/s] */
    protected double gamma = 0.;
    /** maximum reaction velocity [1/s] */
    protected double gamma_max = 0.;
    /** reaction intensity [kW/m2] */
    protected double I_r = 0.;
    /** slope factor */
    protected double phi_s = 0.;
    /** vars in original in Rothermel 1972 wind factor formulae */
    protected double B, C, E = 0.;
    /** wind factor */
    protected double phi_w = 0.;
    /** combined slope/wind factor */
    public double phi_t = 0.;
    /** Vector components */
    protected double vx, vy, vl = 0.;
    /** radians equivalent of asp */
    private double asp_r;
    /** radians equivalent of slp */
    private double slp_r;
    /** radians equivalent of wdr */
    private double wdr_r;
    /** tan of slope, used in slope factor */
    protected double tan_slp;
    private double al;
    private double splitRad;
    protected double cos_splitRad, sin_splitRad;
    private double alDeg, alRad;
    /** auxiliary function output: (sv * w0) */
    protected double sw_d1, sw_d2, sw_d3, sw_lh, sw_lw, sw_d, sw_l, sw_t = 0.;
    /** auxiliary function output: (sv^2 * w0) */
    protected double s2w_d, s2w_l, s2w_t = 0.;
    /** auxiliary function output: (sv * w0^2) */
    protected double sw2_d, sw2_l, sw2_t = 0.;
    /** auxiliary function output: (sv * w0 * m) */
    protected double swm_d, swm_l, swm_t = 0.;
    /** fuel complex surface-area-to-volume ratio [1/m] */
    protected double sigma = 0.;
    /** total weight */
    private double w0 = 0.;
    /** net fuel loading */
    protected double wn_d1, wn_d2, wn_d3, wn_lh, wn_lw, wn_d, wn_l;
    /** effective heating number */
    protected double eps_d1, eps_d2, eps_d3, eps_lh, eps_lw;
    /** heat of preignition */
    protected double q_d1, q_d2, q_d3, q_lh, q_lw;
    /** intermediate heat sink output */
    protected double hskz;
    /** moisture damping ratio of fine fuel loadings, dead/live */
    protected double hn_d1, hn_d2, hn_d3, hn_lh, hn_lw = 0.;
    /** moisture damping ratio of fine fuel loadings, dead/live */
    protected double sumhd, sumhl, sumhdm = 0.;
    /** W' ratio of "fine" fuel loading */
    protected double W = 0.;
    /** moisture damping coefficient */
    protected double eta_Ml, eta_Md = 0.;
    /** moisture ratio */
    protected double rm_d, rm_l = 0.;
    /** Moisture content of dead fine fuel */
    protected double Mf_dead = 0.;
    /** Moisture of extinction of living fuel */
    protected double Mx_live = 0.;
    private double dead, live = 0.;
    protected double curing = 0.;
    // -------------------
    // resulting variables
    // -------------------
    /** Output: spread direction [degree] */
    public double sdr = 0.;
    /** Output: effective wind speed [m/s] */
    public double efw = 0.;
    /** Output: heat sink term [kJ/m3] */
    public double hsk = 0.;
    /** Output: rate of spread [m/s] */
    public double ros = 0.;
    /** Output: flame residence time [s] */
    public double tau = 0.;
    /** Output: heat release per unit area [kJ/m2] */
    public double hpa = 0.;
    /** Output: flame zone depth [m] */
    public double fzd = 0.;
    /** Output: fire line intensity [kW/m] */
    public double fli = 0.;
    /** Output: flame length [m] */
    public double fln = 0.;

    //**********************************************************************
    /**
     * Methods
     * */
    // just for convenience
    static void show(String text) {
        System.out.println(text);
    }

    /**
     * Build array with nodata-values
     * @param key
     * @param val
     */
    static public void setNodataValue(String key, double val) {
        int i = v.indexOf(key.toLowerCase());
        if (i >= 0) {
            noData[i] = val;
        } else {
            show("undefined parameter :" + key);
        }
    }

    /**
     * Initializes class variables
     */
    public Behave() {
        // initialize noData[] with -9999;
        for (int i = 0; i < noData.length; i++) {
            noData[i] = -9999.;
        }
    }

    /**
     * Calculates the Rothermel equations. <br>
     *
     * Wrapper for the actual calculation, it first checks for consistent data.
     */
    public void calc() {
        resetOutputs();
        updateParameterList();
        checkNodata();
        try {
            if (!hasNodata) {
                //calcRothermel();

                // Compute environment variables (fuel moisture etc.)...
                calcEnvironment();
                // ... then compute "no wind/no slope" fire behavior
                calcFireBehavior();
                updateResultList(resHashMapNoWS);

                // Now apply wind and slope to the environment
                calcWindAndSlopeFactor();
                // ... and recompute to obtain max spread fire behavior
                calcFireBehavior();
                updateResultList(resHashMap);

            } else {
                LOG.fine("No data.");
                resetOutputs();
                updateResultList(resHashMapNoWS);
                updateResultList(resHashMap);
            }
        } catch (Exception e) {
            LOG.fine(e.getMessage());
            resetOutputs();
            updateResultList(resHashMapNoWS);
            updateResultList(resHashMap);
        }
        //updateResultList();
        if ((ros == Double.NaN) && (hasFuel)) {
            System.out.println("ros not calculated! hasFuel-Flag: " + hasFuel
                    + " hasNodata-Flag :" + hasNodata);
        }
    }

    /**
     * Allows verification of input parameters.
     * @return true if fuel present and all input params have been set.
     */
    public boolean verify() {
        checkFuel();
        checkNodata();
        return hasFuel && !hasNodata;
    }

    /**
     * Check if there is any fuel => hasFuel <br/>
     * Updates total weight => w0 <br/>
     */
    public void checkFuel() {

        // Total weight
        w0 = w0_d1 + w0_d2 + w0_d3 + w0_lh + w0_lw;
        hasFuel = w0 > 0.0;
    }

    /**
     * Is there fuel?
     * @return true if fuel is present.
     */
    public boolean hasFuel() {
        checkFuel();
        return hasFuel;
    }

    /**
     * Is there Nodata?
     * @return true if any input parameter has not been set.
     */
    public boolean hasNodata() {
        checkNodata();
        return hasNodata;
    }

    /**
     * Returns the rate of spread
     */
    public double getRos() {
        return ros;
    }

    public Map<String, Double> getMaxSpreadResults() {
        return resHashMap;
    }

    public Map<String, Double> getNoWindNoSlopeResults() {
        return resHashMapNoWS;
    }

    /**
     * Set the fuel model number.
     *
     * @param fuelModel a number identifying a fuel model
     */
    public void setFuelModel(int fuelModel) {
        this.fuelModel = fuelModel;
    }

    /**
     * Sets the mean value, i.e. the expectation value, of a parameter. Useful for loading
     * parameters from a property file.
     *
     * @param key Name of a Parameter
     * @param value Value of parameter
     */
    public void setParameterMean(String key, double value) {
        // evaluate String...
        if (key.equalsIgnoreCase("w0_d1")) {
            w0_d1 = value;
        } else if (key.equalsIgnoreCase("w0_d2")) {
            w0_d2 = value;
        } else if (key.equalsIgnoreCase("w0_d3")) {
            w0_d3 = value;
        } else if (key.equalsIgnoreCase("w0_lh")) {
            w0_lh = value;
        } else if (key.equalsIgnoreCase("w0_lw")) {
            w0_lw = value;
        } else if (key.equalsIgnoreCase("m_d1")) {
            m_d1 = value;
        } else if (key.equalsIgnoreCase("m_d2")) {
            m_d2 = value;
        } else if (key.equalsIgnoreCase("m_d3")) {
            m_d3 = value;
        } else if (key.equalsIgnoreCase("m_lh")) {
            m_lh = value;
        } else if (key.equalsIgnoreCase("m_lw")) {
            m_lw = value;
        } else if (key.equalsIgnoreCase("sv_d1")) {
            sv_d1 = value;
        } else if (key.equalsIgnoreCase("sv_d2")) {
            sv_d2 = value;
        } else if (key.equalsIgnoreCase("sv_d3")) {
            sv_d3 = value;
        } else if (key.equalsIgnoreCase("sv_lh")) {
            sv_lh = value;
        } else if (key.equalsIgnoreCase("sv_lw")) {
            sv_lw = value;
        } else if (key.equalsIgnoreCase("rho_p")) {
            rho_p = value;
        } else if (key.equalsIgnoreCase("heat")) {
            heat = value;
        } else if (key.equalsIgnoreCase("depth")) {
            depth = value;
        } else if (key.equalsIgnoreCase("s_e")) {
            s_e = value;
        } else if (key.equalsIgnoreCase("s_t")) {
            s_t = value;
        } else if (key.equalsIgnoreCase("mx")) {
            mx = value;
        } else if (key.equalsIgnoreCase("wsp")) {
            wsp = value;
        } else if (key.equalsIgnoreCase("wdr")) {
            wdr = value;
        } else if (key.equalsIgnoreCase("slp")) {
            slp = value;
        } else if (key.equalsIgnoreCase("asp")) {
            asp = value;
        } else if (key.equalsIgnoreCase("fuelModel")) {
            fuelModel = new Double(value).intValue();
        }
    }

    /**
     * Updates the internal parameter list
     */
    void updateParameterList() {
        varHashMap.put("w0_d1", w0_d1);
        varHashMap.put("w0_d2", w0_d2);
        varHashMap.put("w0_d3", w0_d3);
        varHashMap.put("w0_lh", w0_lh);
        varHashMap.put("w0_lw", w0_lw);
        varHashMap.put("m_d1", m_d1);
        varHashMap.put("m_d2", m_d2);
        varHashMap.put("m_d3", m_d3);
        varHashMap.put("m_lh", m_lh);
        varHashMap.put("m_lw", m_lw);
        varHashMap.put("sv_d1", sv_d1);
        varHashMap.put("sv_d2", sv_d2);
        varHashMap.put("sv_d3", sv_d3);
        varHashMap.put("sv_lh", sv_lh);
        varHashMap.put("sv_lw", sv_lw);
        varHashMap.put("rho_p", rho_p);
        varHashMap.put("heat", heat);
        varHashMap.put("depth", depth);
        varHashMap.put("s_e", s_e);
        varHashMap.put("s_t", s_t);
        varHashMap.put("mx", mx);
        varHashMap.put("wsp", wsp);
        varHashMap.put("wdr", wdr);
        varHashMap.put("slp", slp);
        varHashMap.put("asp", asp);
    }

    /**
     * Resets the internal outputs
     */
    void resetOutputs() {
        I_r = 0.0;
        sdr = 0.0;
        efw = 0.0;
        hsk = 0.0;
        ros = 0.0;
        tau = 0.0;
        hpa = 0.0;
        fzd = 0.0;
        fli = 0.0;
        fln = 0.0;
    }

    /**
     * Updates the list containing the max spread results
     */
    void updateResultList(Map<String, Double> resHashMap) {
        resHashMap.put("I_r", I_r);
        resHashMap.put("sdr", sdr);
        resHashMap.put("efw", efw);
        resHashMap.put("hsk", hsk);
        resHashMap.put("ros", ros);
        resHashMap.put("tau", tau);
        resHashMap.put("hpa", hpa);
        resHashMap.put("fzd", fzd);
        resHashMap.put("fli", fli);
        resHashMap.put("fln", fln);
    }

    /**
     * Updates the list containing the no wind, no slope results
     */
    void updateResultListNoWS() {
        resHashMapNoWS.put("sdr", sdr);
        resHashMapNoWS.put("efw", efw);
        resHashMapNoWS.put("hsk", hsk);
        resHashMapNoWS.put("ros", ros);
        resHashMapNoWS.put("tau", tau);
        resHashMapNoWS.put("hpa", hpa);
        resHashMapNoWS.put("fzd", fzd);
        resHashMapNoWS.put("fli", fli);
        resHashMapNoWS.put("fln", fln);
    }

    /**
     * Checks if all input parameters have been set.
     */
    void checkNodata() {
        // check each parameter to see if it has been changed
        // from the noData value (nominally -9999.)
        hasNodata = false;
        if (noData[v.indexOf("w0_d1")] == w0_d1) {
            hasNodata = true;
        } else if (noData[v.indexOf("w0_d2")] == w0_d2) {
            hasNodata = true;
        } else if (noData[v.indexOf("w0_d3")] == w0_d3) {
            hasNodata = true;
        } else if (noData[v.indexOf("w0_lh")] == w0_lh) {
            hasNodata = true;
        } else if (noData[v.indexOf("w0_lw")] == w0_lw) {
            hasNodata = true;
        } else if (noData[v.indexOf("m_d1")] == m_d1) {
            hasNodata = true;
        } else if (noData[v.indexOf("m_d2")] == m_d2) {
            hasNodata = true;
        } else if (noData[v.indexOf("m_d3")] == m_d3) {
            hasNodata = true;
        } else if (noData[v.indexOf("m_lh")] == m_lh) {
            hasNodata = true;
        } else if (noData[v.indexOf("m_lw")] == m_lw) {
            hasNodata = true;
        } else if (noData[v.indexOf("sv_d1")] == sv_d1) {
            hasNodata = true;
        } else if (noData[v.indexOf("depth")] == depth) {
            hasNodata = true;
        } else if (noData[v.indexOf("mx")] == mx) {
            hasNodata = true;
        } else if (noData[v.indexOf("wsp")] == wsp) {
            hasNodata = true;
        } else if (noData[v.indexOf("wdr")] == wdr) {
            hasNodata = true;
        } else if (noData[v.indexOf("slp")] == slp) {
            hasNodata = true;
        } else if (noData[v.indexOf("asp")] == asp) {
            hasNodata = true;
        }
    }

    /** ************************************************************************
     * The main logic of Rothermel wildfire behavior calculation.
     *
     ************************************************************************ */
    void calcRothermel() throws Exception {

        // reset flags
        isCalculated = false;
        canDerive = true;

        // reset assumptions
        if (w0_d2 == 0) {
            sv_d2 = 0;
        }
        if (w0_d3 == 0) {
            sv_d3 = 0;
        }

        // transfer cured herbaceous fuel to dead herb fuel load
        transferDeadFuel();

        // prepare fuel parameters
        calcFuel();

        // mineral damping coefficient: eta_s
        mineralDamping();

        // moisture damping coefficient: eta_M
        moistureDamping();

        // reaction velocity: gamma
        reactionVelocity();

        // reaction intensity: I_r
        reactionIntensity();

        // propagating flux ratio: xi
        propagatingFluxRatio();

        // heat sink: hsk
        heatSink();

        // rate of spread (no wind, no slope): ros
        rateOfSpreadNoWindNoSlope();

        // wind and slope: phi_t
        calcWindAndSlopeFactor();

        // rate of spread: ros
        rateOfSpreadWithWindAndSlope();

        /** ******************************************************************** */
        /* additional fire behaviour results                                   */
        //
        /* flame residence time: tau               */
        /* Anderson 1969, in Albini (1976), p.91:  */
        /* tau = 384/ sigma   [min]                */
        tau = 384. * 60 / (sigma * 0.3048); // [s]
        /* heat release per unit area: hpa */
        hpa = I_r * tau;
        /* flame zone depth	*/
        fzd = ros * tau;
        /* fireline intensity */
        fli = I_r * fzd;
        /* flame length                                   */
        /* based on Byram (1959), in Albini (1976), p. 91 */
        fln = 0.0775 * pow(fli, 0.46);

        /* it's over...*/
        isCalculated = true;
    }

    void calcEnvironment() throws Exception {

        // reset flags
        isCalculated = false;
        canDerive = true;

        // reset assumptions
        if (w0_d2 == 0) {
            sv_d2 = 0;
        }
        if (w0_d3 == 0) {
            sv_d3 = 0;
        }
        phi_t = 0;

        // transfer cured herbaceous fuel to dead herb fuel load
        transferDeadFuel();

        // prepare fuel parameters
        calcFuel();

        // mineral damping coefficient: eta_s
        mineralDamping();

        // moisture damping coefficient: eta_M
        moistureDamping();

        // reaction velocity: gamma
        reactionVelocity();

        // reaction intensity: I_r
        reactionIntensity();

        // propagating flux ratio: xi
        propagatingFluxRatio();

        // heat sink: hsk
        heatSink();
    }

    private void calcFireBehavior() {

        // rate of spread: ros
        if (phi_t > 0.) {
            ros = I_r * xi * (1 + phi_t) / hsk;
        } else {
            ros = I_r * xi / hsk;
        }
        /* flame residence time: tau               */
        /* Anderson 1969, in Albini (1976), p.91:  */
        /* tau = 384/ sigma   [min]                */
        tau = 384. * 60 / (sigma * 0.3048); // [s]
        /* heat release per unit area: hpa */
        hpa = I_r * tau;
        /* flame zone depth	*/
        fzd = ros * tau;
        /* fireline intensity */
        fli = I_r * fzd;
        /* flame length                                   */
        /* based on Byram (1959), in Albini (1976), p. 91 */
        fln = 0.0775 * pow(fli, 0.46);
    }

    /**
     * Transfers the cured portion of live herbaceous fuels into the dead herbaceous fuel load:
     * w0_dh
     */
    protected void transferDeadFuel() {
        if (isDynamic) {
            if (m_lh >= 120.) {
                curing = 0.0;   // fully green
            } else if (m_lh <= 30) {
                curing = 1.0;   // fully cured
            } else {
                // interpolate between 30 and 120 percents
                curing = 1.0 - ((m_lh - 30.) / 90.);
            }
            // transfer cured herbaceous fuel into the dead herbaceous fuel load
            w0_dh = w0_lh * curing;
            w0_lh -= w0_dh;

            // compute a new dead 1hr surface area-to-volume ratio
            // with cured live herbaceous fuel mixed in.
            sv_d1 = ((sv_d1 * sv_d1 * w0_d1) + (sv_lh * sv_lh * w0_dh))
                    / ((sv_d1 * w0_d1) + (sv_lh * w0_dh));
            // ... and add dead herbaceous fuel to dead 1hr fuel
            w0_d1 += w0_dh;
            //w0_dh = 0; -- save this value for reporting
        }
    }

    /**
     * Calculates fuel, including: <br/>
     * - characteristic surface-to-volume ratio (sigma)<br/>
     * - bulk densities (rho_b)<br/>
     * - packing ratios (beta, beta_opt, beta_ratio)<br/>
     * - net fuel loadings (wn_..)<br/>
     *
     * Exceptions are thrown if<br/>
     * - w0 <= 0.0 no fuel specified<br/> - sw_t <= 0.0 surface-to-voume-ratios not properly
     * specified<br/> - depth <= 0.0 depth of fuel bed not properly specified<br/> @throws
     * java.lang.Exception
     */
    protected void calcFuel() {
        // reset Fuel flag
        hasFuel = true;
        /* reset all values to 0. ***************************/
        sigma = 0.;
        rho_b = 0.;
        beta = 0.;
        beta_opt = 0.;
        beta_ratio = 0.;
        // sw_
        sw_d1 = 0.;
        sw_d2 = 0.;
        sw_d3 = 0.;
        sw_lh = 0.;
        sw_d = 0.;
        sw_l = 0.;
        sw_t = 0.;
        s2w_d = 0.;
        s2w_l = 0.;
        s2w_t = 0.;
        sw2_d = 0.;
        sw2_l = 0.;
        sw2_t = 0.;
        swm_d = 0.;
        swm_l = 0.;
        //
        wn_d1 = 0.;
        wn_d2 = 0.;
        wn_d3 = 0.;
        wn_lh = 0.;
        wn_lw = 0.;
        wn_d = 0.;
        wn_l = 0.;

        /** *********************************************************** */
        // computing characteristic values
        //
        checkFuel();
        if (!hasFuel) {
            throw new IllegalStateException(" no fuel specified");
        }

        // auxiliary variables
        sw_d1 = sv_d1 * w0_d1;
        sw_d2 = sv_d2 * w0_d2;
        sw_d3 = sv_d3 * w0_d3;
        sw_lh = sv_lh * w0_lh;
        sw_lw = sv_lw * w0_lw;
        sw_d = sw_d1 + sw_d2 + sw_d3;
        sw_l = sw_lh + sw_lw;
        sw_t = sw_d + sw_l;

        // s2w = (sv^2 * w0)
        s2w_d = (sw_d1 * sv_d1) + (sw_d2 * sv_d2) + (sw_d3 * sv_d3);
        s2w_l = (sw_lh * sv_lh) + (sw_lw * sv_lw);
        s2w_t = s2w_d + s2w_l;

        // sw2 = (sv * w0^2)
        sw2_d = (sw_d1 * w0_d1) + (sw_d2 * w0_d2) + (sw_d3 * w0_d3);
        sw2_l = (sw_lh * w0_lh) + (sw_lw * w0_lw);
        sw2_t = sw2_d + sw2_l;

        // swm = (sv * w0 * m)
        swm_d = (sw_d1 * m_d1) + (sw_d2 * m_d2) + (sw_d3 * m_d3);
        swm_l = (sw_lh * m_lh) + (sw_lw * m_lw);

        //
        /**
         * characteristic surface to volume ratio => sigma Rothermel 1972: eq. (71) and (72)
         */
        if (sw_t <= 0.0) {
            throw new IllegalStateException("Surface-to-volume-ratio not defined!");
        }
        sigma = s2w_t / sw_t;

        /**
         * mean bulk density Rothermel 1972: eq. (74)
         */
        // see further down "beta"
        // rho_b should not be bigger than 0.5 of the particle density
        //
        if (depth <= 0.) {
            throw new IllegalStateException("invalid fuel bed depth: " + depth);
        }
        rho_b = w0 / depth;

        /**
         * packing ratios
         */
        // mean packing ratio
        beta = rho_b / rho_p;
        // should be between 0. and 0.5?
        // in Rothermel 1972, p.18-19, values are between 0 and 0.12
        if ((beta > 1) || (beta < 0)) {
            System.out.println("Mean packing ration [beta] out of limits [0,1]: " + beta);
        }

        // optimal packing ratio
        // in Rothermel 1972, #69, beta_opt = 3.348 * pow(sigma, -0.8189);
        beta_opt = 8.8578 * pow(sigma, -0.8189);

        // ratio mean / optimal packing
        beta_ratio = beta / beta_opt;

        /**
         * Net fuel loading Rothermel 1972: eq. (60), adjusted by Albini 1976, p.88
         */
        // compute the net combustible fuel loading of each fuel element, wn,
        // from w0 which includes the non-combustible mineral component s_t
        wn_d1 = w0_d1 * (1 - s_t / 100);
        wn_d2 = w0_d2 * (1 - s_t / 100);
        wn_d3 = w0_d3 * (1 - s_t / 100);
        wn_lh = w0_lh * (1 - s_t / 100);
        wn_lw = w0_lw * (1 - s_t / 100);
        // Rothermel 53. mean total surface area
        double A_d1 = sw_d1 / rho_p;
        double A_d2 = sw_d2 / rho_p;
        double A_d3 = sw_d3 / rho_p;
        double A_lh = sw_lh / rho_p;
        double A_lw = sw_lw / rho_p;
        // Rotherml 54 & 55
        double A_d = A_d1 + A_d2 + A_d3;
        double A_l = A_lh + A_lw;
        double A_t = A_d + A_l;
        // Rothermel 56. weighing parameters used in the net fuel loading.
        double f_d1 = A_d1 / A_d;
        double f_d2 = A_d2 / A_d;
        double f_d3 = A_d3 / A_d;
        double f_lh = A_lh / A_l;
        double f_lw = A_lw / A_l;

        // Rothermel 1972: eq. (59)
        if (sw_d > 0) {
            wn_d = ((1 - s_t / 100) * sw2_d) / sw_d;
            dead = f_d1 * wn_d1 + f_d2 * wn_d2 + f_d3 * wn_d3;
        }
        if (sw_l > 0) {
            wn_l = (1 - s_t / 100) * (w0_lh + w0_lw);   // Different algorithm for net live, matches BehavePlus outputs
            live = f_lh * wn_lh + f_lw * wn_lw;
        }
    }

    /**
     * Calculate the mineral damping coefficient: eta_s <br>
     * Uses effective mineral content: s_e <br>
     * Rothermel 1972: eq. (62)
     */
    protected void mineralDamping() {
        eta_s = 0.174 * pow(s_e / 100, -0.19);
    }

    /**
     * Calculates the moisture damping coefficients for dead and live fuel: eta_M.<br/>
     * <br/>
     * moisture damping coefficient weighting factors for live moisture of extinction...
     * <br/>
     * Rothermel (1972): eq. (88)<br/>
     * (mx)_living = 2.9W(1-(M_f)_dead/0.3) - 0.226 (min = 0.3)<br/>
     * <br/>
     * => Albini (1976): page 89!<br/>
     * (mx)_living = 2.9W'(1-(M'_f)_dead/(mx)_dead) - 0.226 (min = mx)<br/>
     * <br/>
     *
     * Exceptions thrown if mx <= 0.
     */
    protected void moistureDamping() {
        // reset variables...
        hn_d1 = 0.;
        hn_d2 = 0.;
        hn_d3 = 0.;
        hn_lh = 0.;
        hn_lw = 0.;
        sumhd = 0.;
        sumhl = 0.;
        sumhdm = 0.;
        W = 0.;
        Mf_dead = 0.;
        Mx_live = 0.;
        eta_Ml = 0.;
        eta_Md = 0.;
        eta_M = 0.;

        /* --------------------------------------------------------
         * Ratio of "fine fuel loadings, dead/live
         *  W' = SUM(w0_d*exp(-138/sv_d*)/SUM(w0_l*exp(-500/sv_l*)
         *  0.20482 = Multiplier for [pound/ft2] to [kg/m2]
         *     -452.76 = -138 / 0.3048
         *     -1640.2  = -500 / 0.3048
         */
        if (sv_d1 > 0.) {
            hn_d1 = 0.20482 * w0_d1 * exp(-452.76 / sv_d1);
        }
        if (sv_d2 > 0.) {
            hn_d2 = 0.20482 * w0_d2 * exp(-452.76 / sv_d2);
        }
        if (sv_d3 > 0.) {
            hn_d3 = 0.20482 * w0_d3 * exp(-452.76 / sv_d3);
        }
        if (sv_lh > 0.) {
            hn_lh = 0.20482 * w0_lh * exp(-1640.42 / sv_lh);
        }
        if (sv_lw > 0.) {
            hn_lw = 0.20482 * w0_lw * exp(-1640.42 / sv_lw);
        }

        // sum up...
        sumhd = hn_d1 + hn_d2 + hn_d3; 
        sumhl = hn_lh + hn_lw;
        sumhdm = (hn_d1 * m_d1) + (hn_d2 * m_d2) + (hn_d3 * m_d3);

        //
        if (mx <= 0.) {
            throw new IllegalStateException("invalid value: Moisture of extinction (mx): " + mx);
        }

        /*
         moisture damping for live fuel
         */
        // calc only if there is any live fuel available...
        // sw_l > 0 ensures that sumhl > 0
        if (sw_l > 0.) {

            // W' ratio of "fine" fuel loading, dead/living
            W = sumhd / sumhl;

            // Moisture content of "fine" dead fuel
            if (sumhd > 0) {
                Mf_dead = sumhdm / sumhd;
            }

            // Moisture of extinction of living fuel
            // Albini (1976): page 89.
            Mx_live = (2.9 * W * (1 - Mf_dead / mx) - 0.226) * 100;

            /*
             * Check for Minimum of Mx_live
             *        Mx_live = max(Mx_live,mx)
             *
             * if Mx_live is lower than mx, we have a problem with the
             * calculation of the error, as the function is no longer continuous
             *
             */
            if (Mx_live < mx) {
                canDerive = false;
                Mx_live = mx;
            }
            // dead moisture ratio
            rm_l = swm_l / (sw_l * Mx_live);
        }

        // moisture ratios
        // Rothermel (1972): eq. (65) & (66)
        if (sw_d > 0) {
            rm_d = swm_d / (sw_d * mx);
        }

        // moisture damping coefficient
        // Rothermel (1972): eq. (64)
        // damping coefficients range from 0 to 1 (Rothermel 1972, p.11!).
        // 0 means a moisture ratio rm_* greater than 1, i.e. the moisture
        //   content is higher than the moisture of extinction
        //
        eta_Md = 1 - 2.59 * (rm_d) + 5.11 * pow(rm_d, 2) - 3.52 * pow(rm_d, 3);
        eta_Ml = 1 - 2.59 * (rm_l) + 5.11 * pow(rm_l, 2) - 3.52 * pow(rm_l, 3);

        // check for eta_* lower than 0;
        if (eta_Md < 0) {
            eta_Md = 0.;
        }
        if (eta_Ml < 0) {
            eta_Ml = 0.;
        }

        //
        eta_M = (wn_d * eta_Md) + (wn_l * eta_Ml);
    }

    /**
     * Calculates the propagating flux ratio: xi <br/>
     *
     * Rothermel 1972: eq. (42) <br/>
     * Formula: <br/>
     * with sigma[1/ft]: <br/>
     * xi = exp[(0.792 + 0.681* sqrt(sigma))*(beta + 0.1)] / (192 + 0.259*sigma) <br/>
     * with sigma[1/m] : <br/>
     * xi = exp[(0.792 + 0.681*sqrt(.3048)*sqrt(sigma))*(beta + 0.1)] / (192 + 0.259*0.3048*sigma)
     */
    protected void propagatingFluxRatio() {
        xi = exp((0.792 + 0.37597 * sqrt(sigma)) * (beta + 0.1)) / (192 + 0.0791 * sigma);
    }

    /**
     * Calculates the reaction intensity: I_r <br>
     */
    protected void reactionIntensity() {
        I_r = gamma * heat * eta_s * eta_M;
    }

    /**
     * Calculates the heat sink term: hsk <br>
     *
     * Rothermel (1972): eq. (77) + (78)
     */
    protected void heatSink() {
        /**
         * Effective heating number: epsilon = exp(-138 / sigma_ft) (14) = exp(-138 / (sigma_m *
         * 0.3048)) conversion! = exp( -452.76 / sigma)
         */
        // if there is no fuel, go back...
        if (sw_t <= 0.) {
            throw new IllegalStateException("Fuel error (sw_t): " + sw_t);
        }

        if (sv_d1 > 0.0) {
            eps_d1 = exp(-452.76 / sv_d1);
        }
        if (sv_d2 > 0.0) {
            eps_d2 = exp(-452.76 / sv_d2);
        }
        if (sv_d3 > 0.0) {
            eps_d3 = exp(-452.76 / sv_d3);
        }
        if (sv_lh > 0.0) {
            eps_lh = exp(-452.76 / sv_lh);
        }
        if (sv_lw > 0.0) {
            eps_lw = exp(-452.76 / sv_lw);
        }
        /**
         * Heat of Preignition: Q_ig, [Btu/lb] = 1.05506 kJ / 0.4535 kg = 2.3265 kJ/kg Q_ig = 250.0
         * + 1116 * M_f ; M_f [fraction] = 581.5 + 2.3265 *(0.01 * M_f) ; M_f [%]
         */
        q_d1 = 581.5 + 25.957 * m_d1;
        q_d2 = 581.5 + 25.957 * m_d2;
        q_d3 = 581.5 + 25.957 * m_d3;
        q_lh = 581.5 + 25.957 * m_lh;
        q_lw = 581.5 + 25.957 * m_lw;

        /**
         * Heat Sink
         */
        hskz = (sw_d1 * eps_d1 * q_d1) + (sw_d2 * eps_d2 * q_d2) + (sw_d3 * eps_d3 * q_d3)
                + (sw_lh * eps_lh * q_lh) + (sw_lw * eps_lw * q_lw);
        hsk = rho_b * hskz / sw_t;
    }

    /**
     * Calculates the combined wind and slope factor: phi_t<br/>
     * -> spread direction: sdr<br>
     * -> effective wind spd: efw<br>
     * -> wind and slope coefficient: phi_t<br>
     * assumptions: wsp > 0. and/or slp > 0.
     */
    protected void calcWindAndSlopeFactor() {
        // reset values
        phi_t = 0.;
        vl = 0.;

        // calculate the wind and slope factor
        slopeFactor();  // -> phi_s
        windFactor();   // -> phi_w

        // combine the two factors using a vector sum..
        // conversion of input values....
        asp_r = toRadians(asp);
        wdr_r = toRadians(wdr);

        // Flip Aspect
        // -> upslope direction is needed
        if (asp_r < PI) {
            asp_r = asp_r + PI;
        } else {
            asp_r = asp_r - PI;
        }

        /*
         * Flip Wind Direction
         * standard meteorological definitions says
         *        winddirection == direction where the wind is blowing FROM
         * for the calculation we need
         *        winddirection == direction where the is blowing TO
         */
        if (wdr_r < PI) {
            wdr_r = wdr_r + PI;
        } else {
            wdr_r = wdr_r - PI;
        }

        /* the following code according to fireLib.c
         * 1. normalize for upslope direction
         * 2. consider differing angle of wind by splitAngle
         */

        /*
         * BDS 20090901 - suspicious code: the delta betweeen 275 and 90 != 265 and 90.
         * splitRad appears to be the 'width' of the angle between wind and slope
         * vectors (versus the bearing between the two).  Normalizing this value to
         * +/- 180 does appear necessary for the sine and cosine functions that follow.
         *
         splitRad = abs(wdr_r - asp_r) >= PI ?
         wdr_r + asp_r - 2 * PI :
         wdr_r - asp_r;
         */
        // delta between wind and aspect
        splitRad = wdr_r - asp_r;

        cos_splitRad = cos(splitRad);
        sin_splitRad = sin(splitRad);

        vx = phi_s + phi_w * cos_splitRad;
        vy = phi_w * sin_splitRad;
        vl = sqrt(vx * vx + vy * vy);
        //
        al = asin(vy / vl);
        //
        if (vx >= 0.) {
            alRad = (vy >= 0.) ? asp_r + al : asp_r + al + 2 * PI;
        } else {
            alRad = asp_r - al + PI;
        }
        alDeg = toDegrees(alRad);
        if (alDeg > 360) {
            alDeg -= 360.;
        }
        // Spread direction
        sdr = alDeg;

        /** *********************************************************************
         * effective windspeed actually this is only the inverse function of phi_w
         * ********************************************************************* */
        efw = (pow(vl / (C * pow(beta_ratio, -E)), 1 / B)) / 196.85;
        // rothermel 87: sets an upper limit on
        // the wind multiplication factor
        if (efw > 0.024 * I_r) {
            efw = min(efw, 0.024 * I_r);
            phi_t = C * pow(196.85 * efw, B) * pow(beta_ratio, -E);
            // flag that derivations are not allowed!
            canDerive = false;
        } else {
            phi_t = vl;
        }
    }

    /**
     * Calculate the slope factor: phi_s <br>
     * Called from calcWindAndSlopeFactor()
     */
    protected void slopeFactor() {
        slp_r = toRadians(slp);
        tan_slp = tan(slp_r);
        phi_s = 5.275 * pow(beta, -0.3) * pow(tan_slp, 2);
    }

    /**
     * Calculates the wind factor: phi_w.
     *
     * Called from calcWindAndSlopeFactor().<br/>
     *
     * conversion:<br/>
     * sigma [1/ft] = sigma[1/m] * 0.3048!<br/>
     * original formulae in Rothermel 1972, eq. 79,81,82,83,84<br/>
     * B = 0.013298 * pow(sigma,0.54);<br/>
     * C = 7.47 * exp(-0.06919 * pow(sigma,0.55));<br/>
     * E = 0.715 * exp(0.0001094 * sigma);<br/>
     *
     */
    protected void windFactor() {
        B = 0.02526 * pow(sigma * 0.3048, 0.54);
        C = 7.47 * exp(-0.133 * pow(sigma * 0.3048, 0.55));
        E = 0.715 * exp(-0.000359 * 0.3048 * sigma);
        phi_w = C * pow(3.281 * 60 * wsp, B) * pow(beta_ratio, -E);
    }

    /**
     * Calculates the forward rate of spread without wind and slope: ros <br>
     */
    protected void rateOfSpreadNoWindNoSlope() {
        ros = I_r * xi / hsk;
    }

    /**
     * Calculate rate of spread with wind and slope: ros <br>
     *
     * Rothermel 1972: (52), [m/s]
     */
    protected void rateOfSpreadWithWindAndSlope() {
        if (phi_t > 0.) {
            ros = I_r * xi * (1 + phi_t) / hsk;
        } else {
            ros = I_r * xi / hsk;
        }
    }

    /**
     * Calculates the reaction velocity: gamma' <br>
     * <br>
     * exponent A:<br>
     * => Rothermel 1972 eq.(70), replaced by Albini (1976) (p.88) <br>
     * A = 133 * sigma**-0.7913 ;sigma[1/ft] <br>
     * = 133 * 0.3048**-0.7913 * sigma**-0.7913 ;sigma[1/m] <br>
     * = 340.53 * sigma**-0.7913 ;sigma[1/m] <br>
     * <br>
     * maximum reaction velocity: <br>
     * => Rothermel 1972: (68), based on (36) <br>
     * conversion: <br>
     * gamma_max [min-1] = 60 gamma_max [s-1] <br>
     * Formulae: <br>
     * gamma_max = sigma**1.5 / (495 + 0.0594* sigma**1.5) <br>
     * counter = sigma**1.5 ;sigma [1/ft] <br>
     * = 1 * pow(0.3048, 1.5) * sigma**1.5 ;sigma [1/m] <br>
     * = 0.16828 * sigma**1.5 <br>
     * <br>
     * denominator = 495 + 0.0594 * sigma**1.5 ;sigma[1/ft] <br>
     * = 495*60 + 0.0594*60*0.16828 * sigma**1.5 ;sigma[1/m] <br>
     * = 29700 + 0.5997 * sigma**1.5 ;sigma[1/m] <br>
     */
    protected void reactionVelocity() {
        A = 340.53 * pow(sigma, -0.7913);
        gamma_max = 0.16828 * pow(sigma, 1.5)
                / (29700 + 0.5997 * pow(sigma, 1.5));
        gamma = gamma_max * pow(beta_ratio, A)
                * exp(A * (1 - beta_ratio));

    }

    @Override
    public String toString() {
        return BehaveReporter.report(this);

    }

}
