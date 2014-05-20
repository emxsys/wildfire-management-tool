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

import static com.emxsys.wildfire.api.WildfireType.*;
import visad.RealTuple;
import visad.Real;
import java.rmi.RemoteException;
import visad.Data;
import visad.VisADException;

public class FuelMoistureTuple extends RealTuple implements FuelMoisture {

    /** [percent] */
    private visad.Real dead1HrFuelMoisture;
    /** [percent] */
    private visad.Real dead10HrFuelMoisture;
    /** [percent] */
    private visad.Real dead100HrFuelMoisture;
    /** [percent] */
    private visad.Real liveHerbFuelMoisture;
    /** [percent] */
    private visad.Real liveWoodyFuelMoisture;
    /** tuple array implemenation */
    private Data[] components;

    /**
     * Construct a new FuelMoistureTuple object with "missing" values.
     */
    public FuelMoistureTuple() {
        this(new Real(FUEL_MOISTURE_1H),
                new Real(FUEL_MOISTURE_10H),
                new Real(FUEL_MOISTURE_100H),
                new Real(FUEL_MOISTURE_HERB),
                new Real(FUEL_MOISTURE_WOODY));
    }

    /**
     * Construct a new FuelMoistureTuple object from doubles where 100.0 equals 100%
     *
     * @param dead1HrFuelMoisture [percent]
     * @param dead10HrFuelMoisture [percent]
     * @param dead100HrFuelMoisture [percent]
     * @param liveHerbFuelMoisture [percent]
     * @param liveWoodyFuelMoisture [percent]
     */
    public FuelMoistureTuple(double dead1HrFuelMoisture, double dead10HrFuelMoisture,
            double dead100HrFuelMoisture, double liveHerbFuelMoisture, double liveWoodyFuelMoisture) {
        this(new Real(FUEL_MOISTURE_1H, dead1HrFuelMoisture),
                new Real(FUEL_MOISTURE_10H, dead10HrFuelMoisture),
                new Real(FUEL_MOISTURE_100H, dead100HrFuelMoisture),
                new Real(FUEL_MOISTURE_HERB, liveHerbFuelMoisture),
                new Real(FUEL_MOISTURE_WOODY, liveWoodyFuelMoisture));
    }

    /**
     * The initial dead1HrFuelMoisture values assigned in this constructor
     * are from Rothermel et al, "Modeling moisture content of fine dead
     * wildland fuels: input to the BEHAVE fire prediction system."
     * Research Paper INT-359. 1986.
     *
     * The other values have been arbitrarily assigned for testing purposes.
     * @param previousWeeksWx - WxConditions enum
     */
    public FuelMoistureTuple(WeatherConditions previousWeeksWx) {
        super(FUEL_MOISTURE);
        switch (previousWeeksWx) {
            case HOT_AND_DRY:
                dead1HrFuelMoisture = new Real(FUEL_MOISTURE_1H, 6);
                dead10HrFuelMoisture = new Real(FUEL_MOISTURE_10H, 7);
                dead100HrFuelMoisture = new Real(FUEL_MOISTURE_100H, 8);
                liveHerbFuelMoisture = new Real(FUEL_MOISTURE_HERB, 70);
                liveWoodyFuelMoisture = new Real(FUEL_MOISTURE_WOODY, 70);
                break;
            case BETWEEN_HOTDRY_AND_COOLWET:
                dead1HrFuelMoisture = new Real(FUEL_MOISTURE_1H, 16);
                dead10HrFuelMoisture = new Real(FUEL_MOISTURE_10H, 17);
                dead100HrFuelMoisture = new Real(FUEL_MOISTURE_100H, 18);
                liveHerbFuelMoisture = new Real(FUEL_MOISTURE_HERB, 76);
                liveWoodyFuelMoisture = new Real(FUEL_MOISTURE_WOODY, 76);
                break;
            case COOL_AND_WET:
                dead1HrFuelMoisture = new Real(FUEL_MOISTURE_1H, 76);
                dead10HrFuelMoisture = new Real(FUEL_MOISTURE_10H, 77);
                dead100HrFuelMoisture = new Real(FUEL_MOISTURE_100H, 78);
                liveHerbFuelMoisture = new Real(FUEL_MOISTURE_HERB, 100);
                liveWoodyFuelMoisture = new Real(FUEL_MOISTURE_WOODY, 100);
                break;
        }
    }

    /**
     * Construct a WildlandFuelMoisture object from Reals.
     *
     * @param dead1HrFuelMoisture WildfireType.FUEL_MOISTURE_1H
     * @param dead10HrFuelMoisture WildfireType.FUEL_MOISTURE_10H
     * @param dead100HrFuelMoisture WildfireType.FUEL_MOISTURE_100H
     * @param liveHerbFuelMoisture WildfireType.FUEL_MOISTURE_HERB
     * @param liveWoodyFuelMoisture WildfireType.FUEL_MOISTURE_WOODY
     */
    public FuelMoistureTuple(Real dead1HrFuelMoisture, Real dead10HrFuelMoisture,
            Real dead100HrFuelMoisture, Real liveHerbFuelMoisture, Real liveWoodyFuelMoisture) {
        super(FUEL_MOISTURE);
        this.dead1HrFuelMoisture = dead1HrFuelMoisture;
        this.dead10HrFuelMoisture = dead10HrFuelMoisture;
        this.dead100HrFuelMoisture = dead100HrFuelMoisture;
        this.liveHerbFuelMoisture = liveHerbFuelMoisture;
        this.liveWoodyFuelMoisture = liveWoodyFuelMoisture;
    }

    @Override
    public visad.Real getDead1HrFuelMoisture() {
        return this.dead1HrFuelMoisture;
    }

    @Override
    public visad.Real getDead10HrFuelMoisture() {
        return this.dead10HrFuelMoisture;
    }

    @Override
    public visad.Real getDead100HrFuelMoisture() {
        return this.dead100HrFuelMoisture;
    }

    @Override
    public visad.Real getLiveHerbFuelMoisture() {
        return this.liveHerbFuelMoisture;
    }

    @Override
    public visad.Real getLiveWoodyFuelMoisture() {
        return this.liveWoodyFuelMoisture;
    }

    public void setDead100HrFuelMoisture(Real dead100HrFuelMoisture) {
        this.dead100HrFuelMoisture = dead100HrFuelMoisture;
    }

    public void setDead10HrFuelMoisture(Real dead10HrFuelMoisture) {
        this.dead10HrFuelMoisture = dead10HrFuelMoisture;
    }

    public void setDead1HrFuelMoisture(Real dead1HrFuelMoisture) {
        this.dead1HrFuelMoisture = dead1HrFuelMoisture;
    }

    public void setLiveHerbFuelMoisture(Real liveHerbFuelMoisture) {
        this.liveHerbFuelMoisture = liveHerbFuelMoisture;
    }

    public void setLiveWoodyFuelMoisture(Real liveWoodyFuelMoisture) {
        this.liveWoodyFuelMoisture = liveWoodyFuelMoisture;
    }

    /**
     * is missing any data elements
     *
     * @return is missing
     */
    @Override
    public boolean isMissing() {
        return dead1HrFuelMoisture.isMissing() || dead10HrFuelMoisture.isMissing()
                || dead100HrFuelMoisture.isMissing() || liveHerbFuelMoisture.isMissing()
                || liveWoodyFuelMoisture.isMissing();
    }

    /**
     * Get the i'th component.
     *
     * @param i Which one
     * @return The component
     *
     * @throws RemoteException On badness
     * @throws VisADException On badness
     */
    @Override
    public Data getComponent(int i) throws VisADException, RemoteException {
        switch (i) {
            case 0:
                return dead1HrFuelMoisture;
            case 1:
                return dead10HrFuelMoisture;
            case 2:
                return dead100HrFuelMoisture;
            case 3:
                return liveHerbFuelMoisture;
            case 4:
                return liveWoodyFuelMoisture;
            default:
                throw new IllegalArgumentException("Wrong component number:" + i);
        }
    }

    /**
     * Create, if needed, and return the component array.
     *
     * @return components
     */
    @Override
    public Data[] getComponents(boolean copy) {
        //Create the array and populate it if needed
        if (components == null) {
            Data[] tmp = new Data[getDimension()];
            tmp[0] = dead1HrFuelMoisture;
            tmp[1] = dead10HrFuelMoisture;
            tmp[2] = dead100HrFuelMoisture;
            tmp[3] = liveHerbFuelMoisture;
            tmp[4] = liveWoodyFuelMoisture;
            components = tmp;
        }
        return components;
    }

    /**
     * Indicates if this Tuple is identical to an object.
     *
     * @param obj         The object.
     * @return            <code>true</code> if and only if the object is
     *                    a Tuple and both Tuple-s have identical component
     *                    sequences.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FuelMoistureTuple)) {
            return false;
        }

        FuelMoistureTuple that = (FuelMoistureTuple) obj;

        return this.dead1HrFuelMoisture.equals(that.dead1HrFuelMoisture)
                && this.dead10HrFuelMoisture.equals(that.dead10HrFuelMoisture)
                && this.dead100HrFuelMoisture.equals(that.dead100HrFuelMoisture)
                && this.liveHerbFuelMoisture.equals(that.liveHerbFuelMoisture)
                && this.liveWoodyFuelMoisture.equals(that.liveWoodyFuelMoisture);
    }

    /**
     * Returns the hash code of this object.
     * @return            The hash code of this object.
     */
    @Override
    public int hashCode() {
        return dead1HrFuelMoisture.hashCode()
                ^ dead10HrFuelMoisture.hashCode()
                & dead100HrFuelMoisture.hashCode()
                | (liveHerbFuelMoisture.hashCode() & liveWoodyFuelMoisture.hashCode());
    }

    /**
     * to string
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
}
