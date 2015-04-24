/*
 * Copyright (c) 2010-2014, Bruce Schubert. <bruce@emxsys.com>
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

import static com.emxsys.visad.Reals.*;
import com.emxsys.visad.Tuples;
import static com.emxsys.wildfire.api.WildfireType.FUEL_CONDITION;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_100H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_10H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_1H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_HERB;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_WOODY;
import static com.emxsys.wildfire.api.WildfireType.FUEL_TEMP_C;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 * BasicFuelCondition is a concrete implementation of the FuelCondition interface.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages({
    "ERR_FuelConditionCannotCreate=Cannot create FuelConditionTuple.",
    "# {0} - MathType",
    "ERR_FuelConditionWrongMathType=Incompatible MathType: {0}."
})
public class BasicFuelCondition extends RealTuple implements FuelCondition {

    /** A tuple with "missing" components */
    public static final BasicFuelCondition INVALID_TUPLE = new BasicFuelCondition();
    public static final int FUEL_MOISTURE_1H_INDEX = Tuples.getIndex(FUEL_MOISTURE_1H, FUEL_CONDITION);
    public static final int FUEL_MOISTURE_10H_INDEX = Tuples.getIndex(FUEL_MOISTURE_10H, FUEL_CONDITION);
    public static final int FUEL_MOISTURE_100H_INDEX = Tuples.getIndex(FUEL_MOISTURE_100H, FUEL_CONDITION);
    public static final int FUEL_MOISTURE_HERB_INDEX = Tuples.getIndex(FUEL_MOISTURE_HERB, FUEL_CONDITION);
    public static final int FUEL_MOISTURE_WOODY_INDEX = Tuples.getIndex(FUEL_MOISTURE_WOODY, FUEL_CONDITION);
    public static final int FUEL_TEMP_INDEX = Tuples.getIndex(FUEL_TEMP_C, FUEL_CONDITION);
    private static final Logger logger = Logger.getLogger(BasicFuelCondition.class.getName());

    /**
     * Creates a BasicFuelCondition from a RealTuple of type FUEL_CONDITION.
     * @param fuelCondition A WildfireType.FUEL_CONDITION RealTuple.
     * @return A new BasicFuelCondition.
     */
    public static BasicFuelCondition fromRealTuple(RealTuple fuelCondition) {
        if (!fuelCondition.getType().equals(WildfireType.FUEL_CONDITION)) {
            throw new IllegalArgumentException(Bundle.ERR_FuelConditionWrongMathType(fuelCondition.getType()));
        } else if (fuelCondition.isMissing()) {
            return INVALID_TUPLE;
        }
        try {
            return new BasicFuelCondition(fuelCondition);

        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, Bundle.ERR_FuelConditionCannotCreate(), ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Creates a BasicFuelCondition.
     * @param fuelMoisture
     * @param fuelTemperature
     * @return A new BasicFuelCondition.
     */
    public static BasicFuelCondition fromReals(FuelMoisture fuelMoisture, Real fuelTemperature) {
        try {
            Real[] reals = new Real[]{
                fuelMoisture.getDead1HrFuelMoisture(),
                fuelMoisture.getDead10HrFuelMoisture(),
                fuelMoisture.getDead100HrFuelMoisture(),
                fuelMoisture.getLiveHerbFuelMoisture(),
                fuelMoisture.getLiveWoodyFuelMoisture(),
                convertTo(FUEL_TEMP_C, fuelTemperature),};
            return new BasicFuelCondition(new RealTuple(reals));

        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, Bundle.ERR_FuelConditionCannotCreate(), ex);
            throw new IllegalStateException(ex);
        }

    }

    /**
     * Constructs an instance with from a RealTuple.
     * @param FuelCondition
     */
    BasicFuelCondition(RealTuple fuelConditionTuple) throws VisADException, RemoteException {
        super(WildfireType.FUEL_CONDITION, fuelConditionTuple.getRealComponents(), null);
    }

    /**
     * Constructs an instance with missing values.
     */
    public BasicFuelCondition() {
        super(WildfireType.FUEL_CONDITION);
    }

    public BasicFuelMoisture getFuelMoisture() {
        return BasicFuelMoisture.fromReals(
                getDead1HrFuelMoisture(),
                getDead10HrFuelMoisture(),
                getDead100HrFuelMoisture(),
                getLiveHerbFuelMoisture(),
                getLiveWoodyFuelMoisture());
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
    public Real getFuelTemperature() {
        try {
            return (Real) getComponent(FUEL_TEMP_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
