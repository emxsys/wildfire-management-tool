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

import com.emxsys.visad.Tuples;
import static com.emxsys.wildfire.api.WildfireType.*;
import visad.RealTuple;
import visad.Real;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import visad.Data;
import visad.VisADException;

public class FuelMoistureTuple extends RealTuple implements FuelMoisture {

    public static FuelMoistureTuple INVALID = new FuelMoistureTuple();
    public static final int FUEL_MOISTURE_1H_INDEX = Tuples.getIndex(FUEL_MOISTURE_1H, FUEL_CONDITION);
    public static final int FUEL_MOISTURE_10H_INDEX = Tuples.getIndex(FUEL_MOISTURE_10H, FUEL_CONDITION);
    public static final int FUEL_MOISTURE_100H_INDEX = Tuples.getIndex(FUEL_MOISTURE_100H, FUEL_CONDITION);
    public static final int FUEL_MOISTURE_HERB_INDEX = Tuples.getIndex(FUEL_MOISTURE_HERB, FUEL_CONDITION);
    public static final int FUEL_MOISTURE_WOODY_INDEX = Tuples.getIndex(FUEL_MOISTURE_WOODY, FUEL_CONDITION);
    private static final Logger logger = Logger.getLogger(FuelMoistureTuple.class.getName());

    /**
     * Creates a FuelMoistureTuple for WildfireType.FUEL_MOISTURE
     *
     * @param dead1HrFuelMoisture WildfireType.FUEL_MOISTURE_1H
     * @param dead10HrFuelMoisture WildfireType.FUEL_MOISTURE_10H
     * @param dead100HrFuelMoisture WildfireType.FUEL_MOISTURE_100H
     * @param liveHerbFuelMoisture WildfireType.FUEL_MOISTURE_HERB
     * @param liveWoodyFuelMoisture WildfireType.FUEL_MOISTURE_WOODY
     * @return A new FuelMoistureTuple
     */
    public static FuelMoistureTuple fromReals(
            Real dead1HrFuelMoisture, Real dead10HrFuelMoisture, Real dead100HrFuelMoisture,
            Real liveHerbFuelMoisture, Real liveWoodyFuelMoisture) {
        try {
            Real[] reals = new Real[]{
                dead1HrFuelMoisture,
                dead10HrFuelMoisture,
                dead100HrFuelMoisture,
                liveHerbFuelMoisture,
                liveWoodyFuelMoisture};
            return new FuelMoistureTuple(new RealTuple(reals));

        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, Bundle.ERR_FuelConditionCannotCreate(), ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Creates a FuelMoistureTuple from a RealTuple of type FUEL_MOISTURE.
     * @param fuelMoisture A WeatherType.FIRE_WEATHER RealTuple.
     * @return A new WeatherTuple.
     */
    public static FuelMoistureTuple fromRealTuple(RealTuple fuelMoisture) {
        if (!fuelMoisture.getType().equals(FUEL_MOISTURE)) {
            throw new IllegalArgumentException("Incompatible MathType: " + fuelMoisture.getType());
        } else if (fuelMoisture.isMissing()) {
            return INVALID;
        }
        try {
            return new FuelMoistureTuple(fuelMoisture);

        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, "Cannot create FuelMoistureTuple.", ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Creates a new FuelMoistureTuple object from doubles where 100.0 equals 100%
     * @param dead1HrFuelMoisture [percent]
     * @param dead10HrFuelMoisture [percent]
     * @param dead100HrFuelMoisture [percent]
     * @param liveHerbFuelMoisture [percent]
     * @param liveWoodyFuelMoisture [percent]
     * @return A new FuelMoistureTuple.
     */
    public static FuelMoistureTuple fromDoubles(
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
     * @return A new FuelMoistureTuple.
     */
    public static FuelMoistureTuple fromWeatherConditions(WeatherConditions previousWeeksWx) {
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
                return INVALID;
        }
    }

    /**
     * Constructs an instance with from a RealTuple.
     * @param fuelMoistureTuple Fuel moisture values.
     */
    private FuelMoistureTuple(RealTuple fuelMoistureTuple) throws VisADException, RemoteException {
        super(WildfireType.FUEL_MOISTURE, fuelMoistureTuple.getRealComponents(), null);
    }

    /**
     * Construct a new FuelMoistureTuple object with "missing" values.
     */
    public FuelMoistureTuple() {
        super(WildfireType.FUEL_MOISTURE);
    }

    @Override
    public Real getDead1HrFuelMoisture() {
        try {
            return (Real) getComponent(FUEL_MOISTURE_1H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getDead10HrFuelMoisture() {
        try {
            return (Real) getComponent(FUEL_MOISTURE_10H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getDead100HrFuelMoisture() {
        try {
            return (Real) getComponent(FUEL_MOISTURE_100H_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getLiveHerbFuelMoisture() {
        try {
            return (Real) getComponent(FUEL_MOISTURE_HERB_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getLiveWoodyFuelMoisture() {
        try {
            return (Real) getComponent(FUEL_MOISTURE_WOODY_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public boolean isMissing() {
        try {
            Data[] components = getComponents(false);
            if (components == null) {
                return true;
            } else {
                for (Data data : components) {
                    if (data == null || data.isMissing()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (VisADException | RemoteException ex) {
            throw new RuntimeException(ex);
        }
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
