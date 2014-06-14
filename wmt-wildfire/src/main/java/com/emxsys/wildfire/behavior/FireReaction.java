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
 *     - Neither the name of Bruce Schubert,  nor the names of its 
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

import com.emxsys.gis.api.Terrain;
import com.emxsys.util.AngleUtil;
import com.emxsys.visad.FireUnit;
import com.emxsys.visad.Reals;
import com.emxsys.weather.api.Weather;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_MPH;
import static com.emxsys.wildfire.api.WildfireType.DIR_OF_SPREAD;
import static com.emxsys.wildfire.api.WildfireType.FIRE_LINE_INTENSITY_US;
import static com.emxsys.wildfire.api.WildfireType.FLAME_LENGTH_US;
import static com.emxsys.wildfire.api.WildfireType.ROS;
import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.Real;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class FireReaction {

    private static final Logger logger = Logger.getLogger(FireReaction.class.getName());

    // Inputs
    private final Fuelbed fuelBed;
    private final Real aspect;
    private final Real slope;
    private final Real windSpd;
    private final Real windDir;

    // Outputs
    private Real rateOfSpread;
    private Real rateOfSpreadMax;
    private Real directionMaxSpread;
    private Real effectiveWindSpeed;
    private Real firelineIntensity;
    private Real flameLength;

    // Intermediate wind and slope effects
    private double phiEw;           // Combined wind and slope factors

    static {
        logger.setLevel(Level.ALL);
    }

    public static FireReaction from(Fuelbed fuelBed, Weather weather, Terrain terrain) {

        return new FireReaction(fuelBed, weather.getWindSpeed(), weather.getWindDirection(), terrain.getAspect(), terrain.getSlope());
    }

    /**
     * Constructor.
     * @param fuelBed
     * @param windSpd
     * @param windDir
     * @param aspect
     * @param slope
     */
    public FireReaction(Fuelbed fuelBed, Real windSpd, Real windDir, Real aspect, Real slope) {
        this.fuelBed = fuelBed;
        this.windSpd = windSpd;
        this.windDir = windDir;
        this.aspect = aspect;
        this.slope = slope;
    }

    /**
     * Gets the maximum rate of spread of the fire: ros.
     * @return ros [ft/min]
     */
    public Real getRateOfSpread() {
        if (this.rateOfSpreadMax == null) {
            if (this.fuelBed.getIsBurnable()) {
                calcFireBehavior();
            } else {
                this.rateOfSpreadMax = new Real(ROS, 0);
            }
        }
        return this.rateOfSpreadMax;
    }

    /**
     * Gets the rate of spread without wind or slope.
     * @return ros [ft/min]
     */
    public Real getRateOfSpreadNoWindNoSlope() {
        if (this.rateOfSpread == null) {
            if (this.fuelBed.getIsBurnable()) {
                double ros = Rothermel.rateOfSpreadNoWindNoSlope(
                        fuelBed.getReactionIntensity().getValue(),
                        fuelBed.getPropagatingFluxRatio().getValue(),
                        fuelBed.getHeatSink().getValue());
                this.rateOfSpread = new Real(ROS, ros);
                try {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "ROS [{0}] (Wind=0,Slope=0): {1} [chn/hr]",
                                new Object[]{fuelBed.getFuelModel().getModelCode(),
                                             rateOfSpread.getValue(FireUnit.chain_hour)});
                    }
                } catch (VisADException ex) {
                }
            } else {
                this.rateOfSpreadMax = new Real(ROS, 0);
            }
        }
        return rateOfSpread;
    }

    /**
     * Gets the direction of maximum spread.
     * @return [degrees]
     */
    public Real getDirectionMaxSpread() {
        if (this.directionMaxSpread == null) {
            if (this.fuelBed.getIsBurnable()) {
                calcFireBehavior();
            } else {
                this.directionMaxSpread = new Real(DIR_OF_SPREAD, 0);
            }
        }
        return this.directionMaxSpread;
    }

    /**
     * Gets the effective wind speed of the combined wind and slope, constrained to Rothermel's
     * maximum wind speed formula.
     * @return [mph]
     */
    public Real getEffectiveWindSpeed() {
        if (this.effectiveWindSpeed == null) {
            if (this.fuelBed.getIsBurnable()) {
                calcFireBehavior();
            } else {
                this.effectiveWindSpeed = new Real(WIND_SPEED_MPH, 0);
            }
        }
        return this.effectiveWindSpeed;
    }

    /**
     * Gets Byram's fireline intensity: I.
     * @return [Btu/ft/s]
     */
    public Real getFirelineIntensity() {
        if (this.firelineIntensity == null) {
            if (this.fuelBed.getIsBurnable()) {
                calcFireBehavior();
            } else {
                this.firelineIntensity = new Real(FIRE_LINE_INTENSITY_US, 0);
            }
        }
        return this.firelineIntensity;
    }

    /**
     * Gets the flame length.
     * @return [ft]
     */
    public Real getFlameLength() {
        if (this.flameLength == null) {
            if (this.fuelBed.getIsBurnable()) {
                calcFireBehavior();
            } else {
                this.flameLength = new Real(FLAME_LENGTH_US, 0);
            }
        }
        return this.flameLength;
    }

    /**
     * Computes the fire behavior elements.
     */
    protected void calcFireBehavior() {
        if (this.fuelBed.getIsBurnable()) {
            if (this.rateOfSpread == null) {

                // Compute phiEw, effective wind and spread direction
                calcWindAndSlopeEffects(windSpd, windDir, aspect, slope);

                double ros0 = getRateOfSpreadNoWindNoSlope().getValue();
                double rosMax = 0;
                if (phiEw <= 0) {
                    rosMax = ros0;  // No wind, no slope
                } else {
                    rosMax = ros0 * (1 + phiEw);
                }
                this.rateOfSpread = new Real(ROS, ros0);
                this.rateOfSpreadMax = new Real(ROS, rosMax);

                double flameZoneDepth = Rothermel.flameZoneDepth(
                        rosMax, fuelBed.getFlameResidenceTime().getValue());
                double fli = Rothermel.firelineIntensity(
                        flameZoneDepth, fuelBed.getReactionIntensity().getValue());
                double fl = Rothermel.flameLength(fli);

                // Outputs
                this.firelineIntensity = new Real(FIRE_LINE_INTENSITY_US, fli);
                this.flameLength = new Real(FLAME_LENGTH_US, fl);
            }
        }
    }

    /**
     * Calculates the combined wind and slope effects, including the wind and slope coefficients,
     * and the direction of maximum spread.
     *
     * Both the wind coefficient (phi W) and the slope coefficient (phi S) have the effect of
     * increasing the proportion of heat reaching the adjacent fuel. They act as multipliers of the
     * reaction intensity.
     *
     * Rothermel 1972: eq. (44)
     *
     * @param windSpd The 20' wind speed.
     * @param windDir The customary wind direction: the true-north angle the wind is FROM [degrees].
     * @param aspect The terrain aspect: e.g., a South aspect would have a 180 degree value
     * [degrees].
     * @param slope The terrain slope angle [degrees].
     *
     */
    protected void calcWindAndSlopeEffects(Real windSpd, Real windDir, Real aspect, Real slope) {

        try {
            // Inputs
            double sigma = fuelBed.getCharacteristicSAV().getValue();
            double beta_ratio = fuelBed.getRelativePackingRatio().getValue();
            double I_r = fuelBed.getReactionIntensity().getValue();

            // TODO: Convert windspeed from 20' to midflame
            // Get wind and slope cooefficients (phiW and phiS)
            double windFactor = Rothermel.windFactor(
                    windSpd.getValue(FireUnit.ft_min),
                    sigma, beta_ratio);
            double slopeFactor = Rothermel.slopeFactor(
                    slope.getValue(),
                    fuelBed.getMeanPackingRatio().getValue());

            // Convert wind direction to where the is blowing TO (i.e., spread direction)
            double wndDir = AngleUtil.normalize360(windDir.getValue() + 180);

            // Convert terrain aspect to an upslope direction
            double slpDir = AngleUtil.normalize360(aspect.getValue() + 180);

            // Get the angle between the wind vector and the upslope vector
            double split = AngleUtil.angularDistanceBetween(wndDir, slpDir);

            double slpRad = toRadians(slpDir);
            double splitRad = toRadians(split);

            // Get the combined wind and slope vector components for max ROS
            double vx = slopeFactor + (windFactor * cos(splitRad));
            double vy = windFactor * sin(splitRad);
            double vl = sqrt(vx * vx + vy * vy); // new combined wind and slope coeeficient

            // Calculate direction of maximum spread
            double aRad = asin(vy / vl);    // vector direction relative to upslope
            double dirRad;                  // vector direction rotated to compass angle
            if (vx >= 0.) {
                dirRad = (vy >= 0.)
                        ? slpRad + aRad
                        : slpRad + aRad + 2 * PI;
            } else {
                dirRad = slpRad - aRad + PI;
            }
            // Spread direction [degrees]
            double spreadDirMax = AngleUtil.normalize360(toDegrees(dirRad));

            // Intermediate combined wind and slope factor
            phiEw = vl;

            // Effective windspeed [ft/min]
            // Rothermel eq. (87) sets an upper limit on the wind multiplication factor
            double effectiveWnd = Rothermel.effectiveWindSpeed(phiEw, beta_ratio, sigma);
            if (effectiveWnd > 0.9 * I_r) {
                effectiveWnd = min(effectiveWnd, 0.9 * I_r);
                phiEw = Rothermel.windFactor(effectiveWnd, sigma, beta_ratio);

            }
            // Outputs
            this.directionMaxSpread = new Real(DIR_OF_SPREAD, spreadDirMax);
            this.effectiveWindSpeed = Reals.convertTo(WIND_SPEED_MPH, new Real(ROS, effectiveWnd));

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String toString() {
        if (this.rateOfSpread == null) {
            calcFireBehavior();
        }
        return "FireReaction{" + "ROS=" + rateOfSpreadMax + ", DIR=" + directionMaxSpread + ", I=" + firelineIntensity + ", L=" + flameLength + '}';
    }

}
