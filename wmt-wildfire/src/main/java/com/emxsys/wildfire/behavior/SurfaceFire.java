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
import static com.emxsys.visad.GeneralUnit.*;
import com.emxsys.visad.RealXmlAdapter;
import com.emxsys.visad.Reals;
import com.emxsys.weather.api.Weather;
import static com.emxsys.weather.api.WeatherType.*;
import com.emxsys.wildfire.api.FireBehavior;
import static com.emxsys.wildfire.api.WildfireType.*;
import static java.lang.Math.*;
import java.rmi.RemoteException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.Exceptions;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 * The SurfaceFire class computes the surface fire behavior from the given fire environment
 * variables: fuel, weather and terrain. The fuel's fuel moisture content is typically conditioned
 * to the its temporal-spatial location.
 *
 * @author Bruce Schubert
 */
@XmlRootElement(name = "surfaceFire")
//@XmlType(propOrder = {
//    "rateOfSpread",
//    "rateOfSpreadMax",
//    "directionMaxSpread",
//    "effectiveWindSpeed",
//    "firelineIntensity",
//    "flameLength",
//    "burnable",})
public class SurfaceFire implements FireBehavior {

    private static final Logger logger = Logger.getLogger(SurfaceFire.class.getName());

    // Inputs
    private SurfaceFuel fuelBed;
    private Real aspect;
    private Real slope;
    private Real windSpdMidFlame;
    private Real windDir;

    // Outputs
    private Real rateOfSpreadMax;
    private Real rateOfSpreadNoWindSlope;
    private Real directionMaxSpread;
    private Real effectiveWindSpeed;
    private Real firelineIntensity;
    private Real flameLength;

    // Intermediate wind and slope effects
    private double phiEw;           // Combined wind and slope factors
    private double eccentricity;    // Fire ellipse from effective wind

    private boolean initialized = false;
    private final Object initializing = new Object(); // Lock

    static {
        logger.setLevel(Level.FINE);
    }

    /**
     * Creates a SurfaceFire instance.
     * @param fuelbed The conditioned fuel complex.
     * @param weather The weather with 20ft wind speeds.
     * @param terrain The terrain aspect and slope at the point of interest.
     * @return A new SurfaceFire instance.
     */
    public static SurfaceFire from(SurfaceFuel fuelbed, Weather weather, Terrain terrain) {

        try {
            // TODO: Convert 10m wind speeds to 20' winds if units in KPH/SI 
            double wndSpd20Ft = weather.getWindSpeed().getValue(mph);
            double fuelDepth = fuelbed.getFuelBedDepth().getValue(foot);
            double midFlameWndSpd = Rothermel.calcWindSpeedMidFlame(wndSpd20Ft, fuelDepth);
            return new SurfaceFire(fuelbed,
                    new Real(WIND_SPEED_MPH, midFlameWndSpd),
                    weather.getWindDirection(),
                    terrain.getAspect(),
                    terrain.getSlope());
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Package constructor.
     * @param fuelBed The fuel complex.
     * @param windSpdMidFlame Mid-flame wind speed.
     * @param windDir Customary wind direction (direction wind is blowing FROM).
     * @param aspect Terrain aspect (South-facing slope is 180 degrees).
     * @param slope Terrain slope angle (steepness) in degrees.
     */
    SurfaceFire(SurfaceFuel fuelBed, Real windSpdMidFlame, Real windDir, Real aspect, Real slope) {
        this.fuelBed = fuelBed;
        this.windSpdMidFlame = windSpdMidFlame;
        this.windDir = windDir;
        this.aspect = aspect;
        this.slope = slope;
        initialize();
    }

    /**
     * Constructor.
     */
    public SurfaceFire() {
        this.fuelBed = new SurfaceFuel();
        this.windSpdMidFlame = new Real(WIND_SPEED_MPH);
        this.windDir = new Real(WIND_DIR);
        this.aspect = new Real(ASPECT);
        this.slope = new Real(SLOPE);
        initialize();
    }

    private void initialize() {

        if (!this.fuelBed.isInitialized()
                || this.aspect.isMissing() || this.slope.isMissing()
                || this.windSpdMidFlame.isMissing() || this.windDir.isMissing()) {
            initialized = false;
            rateOfSpreadMax = new Real(ROS);
            rateOfSpreadNoWindSlope = new Real(ROS);
            directionMaxSpread = new Real(DIR_OF_SPREAD);
            effectiveWindSpeed = new Real(WIND_SPEED_MPH);
            firelineIntensity = new Real(FIRE_LINE_INTENSITY_US);
            flameLength = new Real(FLAME_LENGTH_US);
        } else {
            initialized = true;
            calcFireBehavior();
        }
    }

    private boolean isInitialized() {
        return this.initialized;
    }

    /**
     * Gets an immutable WildfireType.FIRE_BEHAVIOR tuple from this instance.
     * @return A new WildfireType.FIRE_BEHAVIOR tuple.
     */
    public RealTuple getTuple() {
        try {
            return new RealTuple(FIRE_BEHAVIOR,
                    new Real[]{
                        Reals.convertTo(FIRE_LINE_INTENSITY_SI, firelineIntensity),
                        Reals.convertTo(FLAME_LENGTH_SI, flameLength),
                        Reals.convertTo(RATE_OF_SPREAD_SI, rateOfSpreadMax),
                        Reals.convertTo(DIR_OF_SPREAD, directionMaxSpread)
                    }, null);
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            return new RealTuple(FIRE_BEHAVIOR);
        }
    }

    /**
     * Gets the fuel complex that generates this fire behavior.
     * @return A SurfaceFuel instance representing the fuel bed.
     */
    @XmlElement
    public SurfaceFuel getFuelBed() {
        return this.fuelBed;
    }

    public void setFuelBed(SurfaceFuel fuelBed) {
        if (fuelBed == null) {
            throw new IllegalArgumentException("fuelBed cannot be null.");
        }
        synchronized (initializing) {
            this.fuelBed = fuelBed;
            initialize();
        }
    }

    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getAspect() {
        return aspect;
    }

    public void setAspect(Real aspect) {
        if (aspect == null) {
            throw new IllegalArgumentException("aspect cannot be null.");
        }
        synchronized (initializing) {
            this.aspect = aspect;
            initialize();
        }
    }

    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getSlope() {
        return slope;
    }

    public void setSlope(Real slope) {
        if (slope == null) {
            throw new IllegalArgumentException("slope cannot be null.");
        }
        synchronized (initializing) {
            this.slope = slope;
            initialize();
        }
    }

    /**
     * Gets the 20ft mid-flame wind speed.
     * @return
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getMidFlameWindSpeed() {
        return windSpdMidFlame;
    }

    public void setMidFlameWindSpeed(Real midFlameWindSpeed) {
        if (midFlameWindSpeed == null) {
            throw new IllegalArgumentException("midFlameWindSpeed cannot be null.");
        }
        synchronized (initializing) {
            this.windSpdMidFlame = Reals.convertTo(WIND_SPEED_MPH, midFlameWindSpeed);
            initialize();
        }
    }

    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getWindDirection() {
        return windDir;
    }

    public void setWindDirection(Real windDir) {
        if (windDir == null) {
            throw new IllegalArgumentException("windDir cannot be null.");
        }
        synchronized (initializing) {
            this.windDir = windDir;
            initialize();
        }
    }

    /**
     * Gets the maximum rate of spread of the heading fire: ros.
     * @return Heading fire rate of spread, ros [ft/min]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    @Override
    public Real getRateOfSpreadMax() {
        return this.rateOfSpreadMax;
    }

    /**
     * Gets the rate of spread of the backing fire: ros.
     * @return Backing fire rate of spread, ros [ft/min]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getRateOfSpreadBacking() {
        double ros = 0;
        if (this.fuelBed.isBurnable()) {
            // From FireLib 1.04, firelib.c by Collin D. Bevins
            ros = getRateOfSpreadMax().getValue() * (1. - eccentricity) / (1. + eccentricity);
        }
        return new Real(ROS, ros);
    }

    /**
     * Gets the rate of spread of the flanking fire (90 degrees from direction of max spread): ros.
     * @return Flanking fire rate of spread, ros [ft/min]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getRateOfSpreadFlanking() {
        synchronized (initializing) {
            if (isInitialized()) {
                double ros = 0;
                if (this.fuelBed.isBurnable()) {
                    double azimuth = AngleUtil.normalize360(getDirectionMaxSpread().getValue() + 90);
                    return getRateOfSpreadAtAzimuth(new Real(azimuth));
                }
                return new Real(ROS, ros);
            } else {
                return new Real(ROS);
            }
        }
    }

    /**
     * Gets the rate of spread of the fire along a given azimuth (true north): ros.
     * @param azimuth A true north azimuth [degrees].
     * @return The rate of spread, ros, along the azimuth [ft/min].
     */
    public Real getRateOfSpreadAtAzimuth(Real azimuth) {
        synchronized (initializing) {
            if (isInitialized()) {
                double ros = 0;
                if (this.fuelBed.isBurnable()) {
                    // Angle between maximum spread azimuth and requested azimuth.
                    double dir = AngleUtil.angularDistanceBetween(
                            getDirectionMaxSpread().getValue(), azimuth.getValue());

                    // Calculate the fire spread rate in this azimuth. 
                    // From FireLib 1.04, firelib.c by Collin D. Bevins
                    double denom = 1. - eccentricity * cos(toRadians(dir));
                    if (denom > 0) {
                        ros = getRateOfSpreadMax().getValue() * (1. - eccentricity) / denom;
                    } else {
                        ros = getRateOfSpreadNoWindNoSlope().getValue();
                    }
                }
                return new Real(ROS, ros);
            } else {
                return new Real(ROS);
            }
        }
    }

    /**
     * Gets the rate of spread without wind or slope.
     * @return ros [ft/min]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getRateOfSpreadNoWindNoSlope() {
        synchronized (initializing) {
            if (this.rateOfSpreadNoWindSlope == null || this.rateOfSpreadNoWindSlope.isMissing()) {
                if (this.fuelBed.isBurnable()) {
                    double ros = Rothermel.rateOfSpreadNoWindNoSlope(
                            fuelBed.getReactionIntensity().getValue(),
                            fuelBed.getPropagatingFluxRatio().getValue(),
                            fuelBed.getHeatSink().getValue());
                    this.rateOfSpreadNoWindSlope = new Real(ROS, ros);
                    try {
                        if (logger.isLoggable(Level.FINER)) {
                            logger.log(Level.FINER, "ROS [{0}] (Wind=0,Slope=0): {1} [chn/hr]",
                                    new Object[]{fuelBed.getFuelModel().getModelCode(),
                                                 rateOfSpreadNoWindSlope.getValue(FireUnit.chain_hour)});
                        }
                    } catch (VisADException ex) {
                    }
                } else {
                    this.rateOfSpreadNoWindSlope = new Real(ROS, 0);
                }
            }
            return this.rateOfSpreadNoWindSlope;
        }
    }

    /**
     * Gets the direction of maximum spread.
     * @return [degrees]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    @Override
    public Real getDirectionMaxSpread() {
        synchronized (initializing) {
            return this.directionMaxSpread;
        }
    }

    /**
     * Gets the effective wind speed of the combined wind and slope, constrained to Rothermel's
     * maximum wind speed formula.
     * @return [mph]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getEffectiveWindSpeed() {
        synchronized (initializing) {
            return this.effectiveWindSpeed;
        }
    }

    /**
     * Gets Byram's fireline intensity: I.
     * @return [Btu/ft/s]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    @Override
    public Real getFirelineIntensity() {
        synchronized (initializing) {
            return this.firelineIntensity;
        }
    }

    /**
     * Gets the flame length.
     * @return [ft]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    @Override
    public Real getFlameLength() {
        synchronized (initializing) {
            return this.flameLength;
        }
    }

    /**
     * Gets the eccentricity of the elliptical fire shape caused by wind and slope effects.
     *
     * @return The eccentricity of the fire ellipse.
     */
    //@XmlElement
    //@XmlJavaTypeAdapter(RealXmlAdapter.class)
    public double getEccentricity() {
        return isInitialized() ? eccentricity : 0;
    }

    /**
     * Computes the fire behavior elements.
     */
    protected void calcFireBehavior() {
        if (!isInitialized()) {
            throw new IllegalStateException("SurfaceFire is not initilized.");
        }
        if (this.fuelBed.isBurnable()) {

            // Compute phiEw, effective wind and spread direction, and eccentricity
            calcWindAndSlopeEffects(windSpdMidFlame, windDir, aspect, slope);

            double ros0 = getRateOfSpreadNoWindNoSlope().getValue();
            double rosMax = 0;
            if (phiEw <= 0) {
                rosMax = ros0;  // No wind, no slope
            } else {
                rosMax = ros0 * (1 + phiEw);
            }
            this.rateOfSpreadNoWindSlope = new Real(ROS, ros0);
            this.rateOfSpreadMax = new Real(ROS, rosMax);

            double flameZoneDepth = Rothermel.flameZoneDepth(
                    rosMax, fuelBed.getFlameResidenceTime().getValue());
            double fli = Rothermel.firelineIntensity(
                    flameZoneDepth, fuelBed.getReactionIntensity().getValue());
            double fl = Rothermel.flameLength(fli);

            // Outputs
            this.firelineIntensity = new Real(FIRE_LINE_INTENSITY_US, fli);
            this.flameLength = new Real(FLAME_LENGTH_US, fl);
        } else {
            rateOfSpreadNoWindSlope = new Real(ROS, 0);
            rateOfSpreadMax = new Real(ROS, 0);
            directionMaxSpread = new Real(DIR_OF_SPREAD, 0);
            effectiveWindSpeed = new Real(WIND_SPEED_MPH, 0);
            firelineIntensity = new Real(FIRE_LINE_INTENSITY_US, 0);
            flameLength = new Real(FLAME_LENGTH_US, 0);

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
            double split = wndDir - slpDir;

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
            // Intermediate fire ellipse eccentricity
            this.eccentricity = Rothermel.eccentricity(effectiveWnd);

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
        if (this.rateOfSpreadNoWindSlope == null) {
            calcFireBehavior();
        }
        return "FireReaction{" + "ROS=" + rateOfSpreadMax + ", DIR=" + directionMaxSpread + ", I=" + firelineIntensity + ", L=" + flameLength + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.fuelBed);
        hash = 47 * hash + Objects.hashCode(this.aspect);
        hash = 47 * hash + Objects.hashCode(this.slope);
        hash = 47 * hash + Objects.hashCode(this.windSpdMidFlame);
        hash = 47 * hash + Objects.hashCode(this.windDir);
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
        final SurfaceFire other = (SurfaceFire) obj;
        if (!Objects.equals(this.fuelBed, other.fuelBed)) {
            return false;
        }
        if (!Objects.equals(this.aspect, other.aspect)) {
            return false;
        }
        if (!Objects.equals(this.slope, other.slope)) {
            return false;
        }
        if (!Objects.equals(this.windSpdMidFlame, other.windSpdMidFlame)) {
            return false;
        }
        if (!Objects.equals(this.windDir, other.windDir)) {
            return false;
        }
        return true;
    }

}
