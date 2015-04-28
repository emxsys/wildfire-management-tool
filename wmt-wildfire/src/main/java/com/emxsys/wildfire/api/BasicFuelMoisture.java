/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
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
package com.emxsys.wildfire.api;

import com.emxsys.visad.RealXmlAdapter;
import static com.emxsys.wildfire.api.WildfireType.*;
import visad.RealTuple;
import visad.Real;
import java.rmi.RemoteException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.Exceptions;
import visad.VisADException;

/**
 * The BasicFuelMoisture manages the fuel moisture values for a surface fuel.
 *
 * @author Bruce Schubert
 */
@XmlRootElement(name = "fuelMoisture")
@XmlType(propOrder = {"dead1HrFuelMoisture", "dead10HrFuelMoisture", "dead100HrFuelMoisture",
                      "liveHerbFuelMoisture", "liveWoodyFuelMoisture",})
public class BasicFuelMoisture implements FuelMoisture {

    public static BasicFuelMoisture INVALID_FUEL_MOISTURE = new BasicFuelMoisture();
    private static final Logger logger = Logger.getLogger(BasicFuelMoisture.class.getName());

    private Real dead1HrFuelMoisture;
    private Real dead10HrFuelMoisture;
    private Real dead100HrFuelMoisture;
    private Real liveHerbFuelMoisture;
    private Real liveWoodyFuelMoisture;

    /**
     * Creates a BasicFuelMoisture for WildfireType.FUEL_MOISTURE
     *
     * @param dead1HrFuelMoisture WildfireType.FUEL_MOISTURE_1H
     * @param dead10HrFuelMoisture WildfireType.FUEL_MOISTURE_10H
     * @param dead100HrFuelMoisture WildfireType.FUEL_MOISTURE_100H
     * @param liveHerbFuelMoisture WildfireType.FUEL_MOISTURE_HERB
     * @param liveWoodyFuelMoisture WildfireType.FUEL_MOISTURE_WOODY
     * @return A new BasicFuelMoisture
     */
    public static BasicFuelMoisture fromReals(
            Real dead1HrFuelMoisture, Real dead10HrFuelMoisture, Real dead100HrFuelMoisture,
            Real liveHerbFuelMoisture, Real liveWoodyFuelMoisture) {
        return new BasicFuelMoisture(dead1HrFuelMoisture, dead10HrFuelMoisture, dead100HrFuelMoisture,
                liveHerbFuelMoisture, liveWoodyFuelMoisture);
    }

    /**
     * Creates a BasicFuelMoisture from a RealTuple of type FUEL_MOISTURE.
     * @param tuple A WeatherType.FIRE_WEATHER RealTuple.
     * @return A new WeatherTuple.
     */
    public static BasicFuelMoisture fromRealTuple(RealTuple tuple) {

        if (!tuple.getType().equals(FUEL_MOISTURE)) {
            throw new IllegalArgumentException("Incompatible MathType: " + tuple.getType());
        }
        try {
            return new BasicFuelMoisture(
                    (Real) tuple.getComponent(FUEL_MOISTURE_1H_INDEX),
                    (Real) tuple.getComponent(FUEL_MOISTURE_10H_INDEX),
                    (Real) tuple.getComponent(FUEL_MOISTURE_100H_INDEX),
                    (Real) tuple.getComponent(FUEL_MOISTURE_HERB_INDEX),
                    (Real) tuple.getComponent(FUEL_MOISTURE_WOODY_INDEX));
        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, "Cannot create FuelMoistureTuple.", ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Creates a new BasicFuelMoisture object from doubles where 100.0 equals 100%
     * @param dead1HrFuelMoisture [percent]
     * @param dead10HrFuelMoisture [percent]
     * @param dead100HrFuelMoisture [percent]
     * @param liveHerbFuelMoisture [percent]
     * @param liveWoodyFuelMoisture [percent]
     * @return A new BasicFuelMoisture.
     */
    public static BasicFuelMoisture fromDoubles(
            double dead1HrFuelMoisture, double dead10HrFuelMoisture, double dead100HrFuelMoisture,
            double liveHerbFuelMoisture, double liveWoodyFuelMoisture) {
        return fromReals(
                new Real(FUEL_MOISTURE_1H, dead1HrFuelMoisture),
                new Real(FUEL_MOISTURE_10H, dead10HrFuelMoisture),
                new Real(FUEL_MOISTURE_100H, dead100HrFuelMoisture),
                new Real(FUEL_MOISTURE_HERB, liveHerbFuelMoisture),
                new Real(FUEL_MOISTURE_WOODY, liveWoodyFuelMoisture));
    }

    /**
     * The initial dead1HrFuelMoisture values assigned used in this method are from Rothermel et al,
     * "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE fire prediction
     * system." Research Paper INT-359. 1986.
     *
     * The other values have been arbitrarily assigned for testing purposes.
     * @param previousWeeksWx - WxConditions enum
     * @return A new BasicFuelMoisture.
     */
    public static BasicFuelMoisture fromWeatherConditions(WeatherConditions previousWeeksWx) {
        switch (previousWeeksWx) {
            case HOT_AND_DRY:
                return fromReals(
                        new Real(FUEL_MOISTURE_1H, 6),
                        new Real(FUEL_MOISTURE_10H, 7),
                        new Real(FUEL_MOISTURE_100H, 8),
                        new Real(FUEL_MOISTURE_HERB, 70),
                        new Real(FUEL_MOISTURE_WOODY, 70));
            case BETWEEN_HOTDRY_AND_COOLWET:
                return fromReals(
                        new Real(FUEL_MOISTURE_1H, 16),
                        new Real(FUEL_MOISTURE_10H, 17),
                        new Real(FUEL_MOISTURE_100H, 18),
                        new Real(FUEL_MOISTURE_HERB, 76),
                        new Real(FUEL_MOISTURE_WOODY, 76));
            case COOL_AND_WET:
                return fromReals(
                        new Real(FUEL_MOISTURE_1H, 76),
                        new Real(FUEL_MOISTURE_10H, 77),
                        new Real(FUEL_MOISTURE_100H, 78),
                        new Real(FUEL_MOISTURE_HERB, 100),
                        new Real(FUEL_MOISTURE_WOODY, 100));
            default:
                return INVALID_FUEL_MOISTURE;
        }
    }

    /**
     * Construct a new FuelMoistureTuple object with "missing" values.
     */
    public BasicFuelMoisture() {
        this.dead1HrFuelMoisture = new Real(FUEL_MOISTURE_1H);
        this.dead10HrFuelMoisture = new Real(FUEL_MOISTURE_10H);
        this.dead100HrFuelMoisture = new Real(FUEL_MOISTURE_100H);
        this.liveHerbFuelMoisture = new Real(FUEL_MOISTURE_HERB);
        this.liveWoodyFuelMoisture = new Real(FUEL_MOISTURE_WOODY);
    }

    /**
     * Copy constructor performs a shallow copy of the immutable members.
     * @param copy
     */
    public BasicFuelMoisture(FuelMoisture copy) {
        this.dead1HrFuelMoisture = copy.getDead1HrFuelMoisture();
        this.dead10HrFuelMoisture = copy.getDead10HrFuelMoisture();
        this.dead100HrFuelMoisture = copy.getDead100HrFuelMoisture();
        this.liveHerbFuelMoisture = copy.getLiveHerbFuelMoisture();
        this.liveWoodyFuelMoisture = copy.getLiveWoodyFuelMoisture();
    }
    
    /**
     * Constructs a new BasicFuelMoisture instance with the given values.
     *
     * @param dead1HrFuelMoisture [percent]
     * @param dead10HrFuelMoisture [percent]
     * @param dead100HrFuelMoisture [percent]
     * @param liveHerbFuelMoisture [percent]
     * @param liveWoodyFuelMoisture [percent]
     */
    public BasicFuelMoisture(Real dead1HrFuelMoisture, Real dead10HrFuelMoisture, Real dead100HrFuelMoisture,
                             Real liveHerbFuelMoisture, Real liveWoodyFuelMoisture) {
        this.dead1HrFuelMoisture = dead1HrFuelMoisture;
        this.dead10HrFuelMoisture = dead10HrFuelMoisture;
        this.dead100HrFuelMoisture = dead100HrFuelMoisture;
        this.liveHerbFuelMoisture = liveHerbFuelMoisture;
        this.liveWoodyFuelMoisture = liveWoodyFuelMoisture;
    }

    /**
     * Gets a WildfireType.FUEL_MOISTURE tuple.
     * @return A new WildfireType.FUEL_MOISTURE RealTuple.
     */
    public RealTuple getTuple() {
        Real[] reals = new Real[]{
            dead1HrFuelMoisture,
            dead10HrFuelMoisture,
            dead100HrFuelMoisture,
            liveHerbFuelMoisture,
            liveWoodyFuelMoisture};
        try {
            return new RealTuple(FUEL_MOISTURE, reals, null);
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            return new RealTuple(FUEL_MOISTURE);
        }
    }

    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getDead1HrFuelMoisture() {
        return this.dead1HrFuelMoisture;
    }

    public void setDead1HrFuelMoisture(Real dead1HrFuelMoisture) {
        this.dead1HrFuelMoisture = dead1HrFuelMoisture;
    }

    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getDead10HrFuelMoisture() {
        return this.dead10HrFuelMoisture;
    }

    public void setDead10HrFuelMoisture(Real dead10HrFuelMoisture) {
        this.dead10HrFuelMoisture = dead10HrFuelMoisture;
    }

    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getDead100HrFuelMoisture() {
        return this.dead100HrFuelMoisture;
    }

    public void setDead100HrFuelMoisture(Real dead100HrFuelMoisture) {
        this.dead100HrFuelMoisture = dead100HrFuelMoisture;
    }

    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getLiveHerbFuelMoisture() {
        return this.liveHerbFuelMoisture;
    }

    public void setLiveHerbFuelMoisture(Real liveHerbFuelMoisture) {
        this.liveHerbFuelMoisture = liveHerbFuelMoisture;
    }

    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getLiveWoodyFuelMoisture() {
        return this.liveWoodyFuelMoisture;
    }

    public void setLiveWoodyFuelMoisture(Real liveWoodyFuelMoisture) {
        this.liveWoodyFuelMoisture = liveWoodyFuelMoisture;
    }

    /**
     * Gets a plain text representation
     *
     * @return string of me
     */
    @Override
    public String toString() {
        return "1-hr: " + getDead1HrFuelMoisture().toValueString()
                + "; 10-hr: " + getDead10HrFuelMoisture().toValueString()
                + "; 100-hr: " + getDead100HrFuelMoisture().toValueString()
                + "; herb: " + getLiveHerbFuelMoisture().toValueString()
                + "; woody: " + getLiveWoodyFuelMoisture().toValueString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final BasicFuelMoisture other = (BasicFuelMoisture) obj;
        if (!Objects.equals(this.dead1HrFuelMoisture, other.dead1HrFuelMoisture)) {
            return false;
        }
        if (!Objects.equals(this.dead10HrFuelMoisture, other.dead10HrFuelMoisture)) {
            return false;
        }
        if (!Objects.equals(this.dead100HrFuelMoisture, other.dead100HrFuelMoisture)) {
            return false;
        }
        if (!Objects.equals(this.liveHerbFuelMoisture, other.liveHerbFuelMoisture)) {
            return false;
        }
        if (!Objects.equals(this.liveWoodyFuelMoisture, other.liveWoodyFuelMoisture)) {
            return false;
        }
        return true;
    }

}
