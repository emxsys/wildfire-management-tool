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

import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.util.MathUtil;
import com.emxsys.wildfire.api.StdFuelMoistureScenario;
import static java.lang.Math.*;

/**
 * Utility class that calculates solar insolation, temperature, humidity, surface wind speed, and
 * fine fuel moisture content per the 1986 Rothermel et al equations.
 *
 * <ul> <li style="bullet"><a name="bib_1000"></a>Rothermel et al, 1986, Modeling Moisture Content
 * of Fine Dead Wildland Fuels: Input to the BEHAVE Fire Prediction System, Research Paper, INT-359,
 * USDA Forest Service, Intermountain Research Station </ul>
 *
 * @author Bruce Schubert
 */
public class BehaveUtil {

    /**
     * Compute the Canadian Standard Daily Fine Fuel Moisture Code (FFMC) computes the fuel moisture
     * for early afternoon from noon-time weather conditions or forecasts. <br/> Rothermel, et al,
     * "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE fire prediction
     * system." Research Paper INT-359. 1986. Equations located on page 47. <br/> Note: FFMC: Low =
     * 0 - 72; Moderate = 73 - 77; High = 78 - 82; Extreme > 82
     *
     * @param m_0 initial fuel moisture at noon [percent]
     * @param T_f air temp immediately adjacent to fuel [farenheit]
     * @param H_f relative humidity immediately adjacent to fuel [percent]
     * @param W 20 foot wind speed [mph]
     * @param R rainfall amount [inches]
     * @return fuel moisture percent from derived from computed FFMC code
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
        double E_D = (0.942 * pow(H_f, 0.679)) + 11 * exp((H_f / 10) - 10);

        // Equation #4 - equilibrium wetting curve
        double E_W = (0.597 * pow(H_f, 0.768)) + 14 * exp((H_f / 8) - 12.5);

        // m - fine fuel moisture adjusted for humidity and wind
        double m;
        if (MathUtil.nearlyEquals(m_R, E_D)) {
            m = m_R;
        } // Wetting
        else if (m_R < E_D) {
            // fuel moisture is below the drying curve so a wetting trend is in effect
            // Equation #5
            //m = E_W + (m_R - E_W) / 1.9953;   -- original
            m = E_W + (E_W - m_R) / 1.9953;     // corrected based on Anderson 2009 87-10 
        } // Drying
        else {
            // fuel moisture is above the drying curve so a drying trend is in effect

            // Here we constrain 20' wind to between 1 and 14 mph
            W = min(max(1.0, W), 14.0);
            // Equations #6 and #7
            double x = 0.424 * (1 - pow(H_f / 100, 1.7)) + 0.088 * pow(W, 0.5) * (1 - pow(H_f / 100, 8));
            m = E_D + (m_R - E_D) / pow(10, x);
        }
        // compute fine fuel moisture delta for temperature
        double delta = 0;
        if (f_0 < 99.0) {
            delta = max(-16.0, (T_f - 70d) * (0.63 - 0.0065 * f_R));
        }
        // final FFMC code constrained to between 0 and 99
        double f = max(0, min(99d, 101d - m + delta));

        // FFMC code converted to fuel moisture
        return 101d - f;
    }

    /**
     *
     * Note: The calcCanadianStandardDailyFineFuelMoisure provides the first m_0. Subsequently, the
     * previous hour's m becomes m_0.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations located on page 47.
     *
     * @param m_0 previous hour's fuel moisture [percent]
     * @param H relative humidity [percent]
     * @param T_c air temperature [celcius]
     * @param W_k 20' windspeed [kph]
     * @return fuel moisture [percent]
     */
    static public double calcCanadianHourlyFineFuelMoisture(double m_0, double H, double T_c,
                                                            double W_k) {
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
            // Equations #3a and #3b compute log drying rate for hourly computation, log base 10
            double k_a = 0.424 * (1 - pow(H / 100, 1.7)) + 0.0694 * pow(W_k, 0.5) * (1 - pow(H / 100, 8));
            double k_d = k_a * 0.0579 * exp(0.0365 * T_c);
            // Equation #5a computes final fuel moisture percent
            m = E_d + (m_0 - E_d) * exp(-2.303 * k_d);
        } else if (m_0 < E_w) {
            // Equation #4a and #4b compute log wetting rate for hourly computation, log base 10
            double k_b = 0.424 * (1 - (pow((100 - H) / 100, 1.7)))
                    + 0.0694 * pow(W_k, 0.5) * (1 - (pow((100 - H) / 100, 8)));
            double k_w = k_b * 0.0579 * exp(0.0365 * T_c);
            // Equation #5b computes final fuel moisture percent
            //m = E_w + (E_w - m_0) * exp(-2.303 * k_w);    // anderson 2009 (77-5b)
            m = E_w - (E_w - m_0) * exp(-2.303 * k_w);    // rothemel pg 48
        } else {
            m = m_0;
        }
        return m;
    }

    /**
     * Compute difference between fuel temperature and the air temperature due to solar heating and
     * wind cooling effects.
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
     * @param H_a relative humidity of the air [percent]
     * @param T_f fuel temperature [fahrenheit]
     * @param T_a air temperature [fahrenheit]
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
     * @param I_a irradiance at the forest floor perpendicular to the solar ray [cal/cm2*min]
     * @param r2 The earth-sun (center of mass) distance squared
     * @param A solar elevation angle to the sun (-90 <= A <= 90) [radians] @return
     * I - incident radiation on the forest floor [cal/cm2*min]
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
     * Computes the irradiance on a slope (neglecting the small variation in r)
     *
     * @param alpha slope angle from horizontal at slope azimuth [radians]
     * @param beta aspect of the slope [radians]
     * @param A solar altitude (elevation) angle [radians]
     * @param Z solar azimuth angle (relative to East) [radians]
     * @param I_a attenuated irradiance [cal/cm2*min]
     * @return incident radiation intensity [cal/cm2*min]
     */
    static public double calcIrradianceOnASlope(double alpha, double beta, double A, double Z,
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

        double tanPsi = tan(alpha) * sin(Z - beta);
        double psi = atan(tanPsi);
        double sinZeta = sin(A - psi) * cos(alpha) / cos(psi);

        // I is the incident radiation intensity
        double I = I_a * sinZeta;

        // Post condition: I >= 0
        return (I > 0) ? I : 0;
    }

    /**
     * Computes the solar altitude angle A, i.e., how high is the sun from the horizon.
     *
     * @param h hour angle from the local 6am [(360/24)*(t - 6.0)]
     * @param phi latitude [radians]
     * @param delta solar declination [radians]
     * @return solar altitude angle [radians]
     */
    static public double calcSolarAltitudeAngle(double h, double phi, double delta) {
        // Rothermel et al, 1986, page 10
        // Equation #4
        // The solar altitude angle A, is given by
        //  sin A = sin h * cos delta * cos phi + sin delta * sin phi
        // where:
        //  A =   solar altitude angle
        double sinA = (sin(h) * cos(delta) * cos(phi)) + (sin(delta) * sin(phi));
        return asin(sinA);
    }

    static public double calcHourAngle(double A, double phi, double delta) {
        // This equation computes the hour angle from the solar altitude angle
        double sinh = (sin(A) - sin(phi) * sin(delta)) / (cos(phi) * cos(delta));
        return asin(sinh);
    }

    /**
     * Computes the time of sunrise for latitudes less that 66.5 Rothermel et al, 1986, page 48
     * Based on Equation #1 sin h = (sin A - sin phi * sin delta) / (cos phi * cos delta);
     *
     * The text includes additional conditions for testing for perpetual day or perpetual night...
     * not implemented.
     */
    static public double calcSunrise(double phi, double delta) {
        assert (abs(phi) < toDegrees(66.5));
        // TODO: Add tests for perpetual day/night (return 0 for no sunrise/sunset)

        // This equation computes the hour angle when the solar altitude angle = 0
        double h = asin(0 - sin(phi) * sin(delta)) / (cos(phi) * cos(delta));
        double t = (toDegrees(h) / 15) + 6;  // 15 degrees of rotation per hour; relative to 0600hrs
        return t;
    }

    static public double calcSunset(double phi, double delta) {
        double t_r = calcSunrise(phi, delta);
        double t_s = 24 - t_r;
        return t_s;
    }

    /**
     * Computes the solar azimuth angle, Z, i.e., where is sun relative to East. At 0600 local time
     * the solar azimuth is 0 degrees; at 1800 it is 180 degrees.
     *
     * @param h hour angle from the local 6am [(360/24)*(t - 6.0)] [radians]
     * @param phi latitude [radians]
     * @param delta solar declination [radians]
     * @param A solar altitude angle [radians]
     * @return solar azimuth angle 0 <= Z <= 2PI relative to East [radians]
     */
    static public double calcSolarAzimuthAngle(double h, double phi, double delta, double A) {
        // Rothermel et al, 1986, page 11
        // Equation #5 and #6. The solar azimuth angle Z, is given by simple
        // ratios of equations 5 and 6.

        // Eq #5
        double tanZ = ((sin(h) * cos(delta) * sin(phi)) - (sin(delta) * cos(phi)))
                / (cos(h) * cos(delta));
        // Eq #6
        double cosZ = cos(h) * (cos(delta) / cos(A));

        // Compare cos and tan values to determine which inverse function to use
        // and which  quadrant of the circle to assign the value to (relative
        // to East). But first, invert the tan value when in the southern hemisphere to
        // sync it up with the cos value which is not influenced by the sign of phi --
        // A angle used in cosZ is is always positive at noon regardless of hemisphere.
        double Z = 0;
        if (phi < 0) {
            tanZ = -tanZ;
        }
        if (tanZ >= 0 && cosZ >= 0) {
            Z = atan(tanZ);         // late morning (east to south)
        } else if (tanZ < 0 && cosZ < 0) {
            Z = acos(cosZ);         // early afternnon (south to west)
        } else if (tanZ >= 0 && cosZ < 0) {
            Z = atan(tanZ) + PI;    // night (west to north)
        } else {
            Z = 2 * PI - acos(cosZ);// early morning (north to east)
        }
        return Z;
    }

    /**
     * Computes hour angle from local 6am
     *
     * @param timeProjection local time in 24hr format
     * @return hour angle 0600=0 deg; 1200=90 deg; 1800=180 deg; 0000=270[radians]
     */
    static public double calcLocalHourAngle(double t) {
        // deg per hour = (360.0 / 24.0) = 15
        double h = 15 * ((t >= 6.0 ? t : t + 24) - 6.0);
        return toRadians(h);
    }

    /**
     * Computes the earth-sun (center of mass) distance squared.
     *
     * @param delta solar declination [radians]
     * @return r2 earth-sun distance squared
     */
    static public double calcEarthSunDistanceSqrd(double delta) {
        // Rothermel et al, 1986, page 11
        // Equation #7
        // The earth-sun distance (squared) by analytic soltion to tabular values
        double r2 = 0.999847 + (0.001406 * toDegrees(delta));
        return r2;
    }

    /**
     * Computes the solar declination angle for a given day of the year. Solar Declination angle is
     * the angle between a plane perpendicular to incoming solar radiation and the rotational axis
     * of the earth. It varies form +23.5 degress on June 21/22 and -23.5 degrees on December 21/22.
     *
     * Declination is analogous to latitude on Earth's surface, and measures an angular displacement
     * north or south from the projection of Earth's equator on the celestial sphere to the location
     * of a celestial body.
     *
     * @param NJ the julian date (day of the year)
     * @return the solar declination angle [degrees]
     */
    static public double calcSolarDeclinationAngle(long NJ) {
        // Rothermel et al, 1986, page 11
        // Equation #8
        // 0.9863 = 360 degrees /365 days
        double delta = 23.5 * sin(toRadians(0.9863) * (284 + NJ));
        return toRadians(delta);
    }

    /**
     * Computes the julian date (day of the year)
     *
     * @param Mo
     * @param Dy
     * @param Yr
     * @return NJ - the day of the year
     */
    static public int calcJulianDate(int Mo, int Dy, int Yr) {
        // Rothermel et al, 1986, page 11
        // Equation #8

        // epsilon is the Julian date correction for Feburary and leap years.
        int epsilon = Mo == 1 ? 2 : Mo == 2 ? 3 : (Yr % 4) == 0 && Mo > 2 ? 1 : 0;
        // Compute Julian date
        int NJ = (int) round(31 * (Mo - 1) + Dy - (0.4 * Mo) - 1.8 + epsilon);
        return NJ;
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
        double tau_c = (1 - S_c / 100.0); // cloud shade transmittance
        double tau_t = 1;               // tree shade transmittance (default 1 for now)
        double tau_n = tau_t * tau_c;
        double I_M = I_o * pow(p, M);   // incident radiation attenuated by atmosphere
        double I_a = I_M * tau_n;       // irradiance at forest floor perpendicular to solar ray
        return I_a;
    }

    /**
     * Computes the optical air mass, i.e., the ratio of the optical path length of radiation
     * through the atmosphere at angle A, to the path length toward the zenith at sea level.
     *
     * @param A the solar altitude angle [radians]
     * @param E the elevation at angle A [feet]
     * @return M the optical air mass ratio
     */
    static public double calcOpticalAirMass(double A, double E) {
        // Equation #16
        // M = (absolute_pressure / sea_level_pressure) csc A = exp(-0.0000448E) csc A
        double M = 0;
        if (A > 0) {
            M = exp(-0.0000448 * E) * 1.0 / sin(A);
        }
        return M;
    }

    /**
     * Sinusoidal curve linking 1400 temp to temp at sunset - used to calculate temperature for each
     * hour between 1400 and sunset.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #38 located on page 22.
     *
     * @param timeProjection
     * @param timeSunset
     * @param temp1400
     * @param rhSunset
     * @return
     */
    static public double calcAirTempLateAfternoon(double timeProjection, double timeSunset,
                                                  double temp1400, double tempSunset) {
        assert (timeProjection >= 14);
        return temp1400 + (temp1400 - tempSunset) * (cos(toRadians(90 * (timeProjection - 14) / (timeSunset - 14))) - 1);
    }

    /**
     * Sinusoidal curve linking sunset temp to temp at sunrise - used to calculate temperature for
     * each hour between sunset and sunrise
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #40 located on page 23.
     *
     * @param timeProjection
     * @param timeSunset
     * @param timeSunrise
     * @param rhSunset
     * @param rhSunrise
     * @return
     */
    static public double calcAirTempNighttime(double timeProjection, double timeSunset,
                                              double timeSunrise, double tempSunset, double tempSunrise) {
        timeSunrise += 24;
        if (timeProjection < timeSunset) {
            timeProjection += 24;
        }
        return tempSunset + (tempSunrise - tempSunset) * sin(toRadians(90 * (timeProjection - timeSunset) / (timeSunrise - timeSunset)));
    }

    /**
     * Sinusoidal curve linking sunrise temp to temp at noon - used to calculate temperature for
     * each hour between sunrise and 1200 hrs.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #42 located on page 24.
     *
     * @param timeProjection
     * @param timeSunrise
     * @param rhSunrise
     * @param rh1200
     * @return
     */
    static public double calcAirTempMorning(double timeProjection, double timeSunrise,
                                            double tempSunrise, double temp1200) {
        assert (timeProjection <= 12.0);
        return temp1200 + (tempSunrise - temp1200) * cos(toRadians(90 * (timeProjection - timeSunrise) / (12.0 - timeSunrise)));
    }

    /**
     * Sinusoidal curve linking 1400 humidity to humidity at sunset - used to calculate humidity for
     * each hour between 1400 and sunset.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equation #39 located on page 22.
     *
     *
     * @param timeProjection
     * @param timeSunset
     * @param rh1400
     * @param rhSunset
     * @return
     */
    static public double calcHumidityLateAfternoon(double timeProjection, double timeSunset,
                                                   double rh1400, double rhSunset) {
        assert (timeProjection >= 14);
        assert (timeProjection <= timeSunset);

        return rh1400 + (rh1400 - rhSunset) * (cos(toRadians(90 * (timeProjection - 14) / (timeSunset - 14))) - 1);
    }

    /**
     * Sinusoidal curve linking sunset humidity to humidity at sunrise - used to calculate relative
     * humidity for each hour between sunset and sunrise
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #41 located on page 23.
     *
     * @param timeProjection
     * @param timeSunset
     * @param timeSunrise
     * @param rhSunset
     * @param rtSunrise
     * @return
     */
    static public double calcHumidityNighttime(double timeProjection, double timeSunset,
                                               double timeSunrise, double rhSunset, double rhSunrise) {
        timeSunrise += 24;
        if (timeProjection < timeSunset) {
            timeProjection += 24;
        }
        return rhSunset + (rhSunrise - rhSunset) * sin(toRadians(90 * (timeProjection - timeSunset) / (timeSunrise - timeSunset)));
    }

    /**
     * Sinusoidal curve linking sunrise humidty to humidty at noon - used to calculate humidty for
     * each hour between sunrise and 1200 hrs.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #43 located on page 24.
     *
     * @param timeProjection
     * @param timeSunrise
     * @param rhSunrise
     * @param rh1200
     * @return
     */
    static public double calcHumidityMorning(double timeProjection, double timeSunrise,
                                             double rhSunrise, double rh1200) {
        assert (timeProjection <= 12.0);
        return rh1200 + (rhSunrise - rh1200) * cos(toRadians(90 * (timeProjection - timeSunrise) / (12.0 - timeSunrise)));
    }

    /**
     * Computes the wind speed at the fuel level from 20 foot wind speeds
     *
     * @param U_20 wind speed 20 feet above the vegitation [mph]
     * @param h vegitation height [feet]
     * @return wind speed at vegetation height
     */
    static public double calcWindSpeedAtFuelLevel(double U_20, double h) {
        // Equation #36
        // The ratio of windspeed at vegetation height to that at
        // 20 feet above the vegitation is given by:
        //  U_h' / U_20+h' = 1 / ln((20 + 0.36 * h') / 0.13 * h')
        //      where:
        //          h' = vegitation height [feet]
        if (h == 0) {
            h = 0.1;
        }
        double U_h = (1.0 / log((20 + 0.36 * h) / (0.13 * h))) * U_20;
        return U_h;
    }

    public static void main(String[] args) {

        FuelMoisture fm = StdFuelMoistureScenario.LowDead_FullyCuredHerb.getFuelMoisture();
        double m_0 = 4.2;           // [percent]
        double T_a = 70;            // [farenheit]
        double H_a = 21;            // [percent]
        double W = 21.5 * 1.609;    // [mph]
        int S_c = 0;                // [percent]
        double h_v = 1;             // vegetation height [feet]
        double E = 0;               // sea level
        double p = 0.7;             // atmospheric transparency
        double phi = toRadians(30); // texas

        double delta = calcSolarDeclinationAngle(calcJulianDate(4, 4, 2009));
        double h = calcLocalHourAngle(15.2);   // local time
        double A = calcSolarAltitudeAngle(h, phi, delta);
        double M = calcOpticalAirMass(A, E);
        double I_a = calcAttenuatedIrradiance(M, S_c, p);
        double r2 = calcEarthSunDistanceSqrd(toRadians(0.0));
        double I = calcSolarIrradianceOnHorzSurface(I_a, r2, A);
        double U_h = calcWindSpeedAtFuelLevel(W, h_v);
        double T_f = calcFuelTemp(I, T_a, U_h);
        double H_f = calcRelativeHumidityNearFuel(H_a, T_f, T_a);

        double T_m = (T_a - 32) * .5556; // [celcius]
        double W_m = W * 1.609;    // [kph]
        for (int i = 0; i <= 24; i++) {
            double result = calcCanadianHourlyFineFuelMoisture(m_0, H_f, T_m, W_m);
            System.out.println("CanadianHourlyFineFuelMoisture = " + result
                    + " @ " + (i + 14 < 24 ? i + 14 : i - 10) + ":00 local");
            m_0 = result;
        }

    }
}
