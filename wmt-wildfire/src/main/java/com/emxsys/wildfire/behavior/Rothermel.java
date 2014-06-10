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

import com.emxsys.wildfire.api.FireBehaviorTuple;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import static com.emxsys.wildfire.api.WildfireType.COMBUSTIBLE;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class Rothermel {

    private static final Logger logger = Logger.getLogger(Rothermel.class.getName());

    public static FireBehaviorTuple getFireBehavior(FuelModel model, FuelMoisture moisture) {
        FuelBed fuelCharacter = getFuelCharacter(model, moisture);
        RealTuple fuelCombustible = getFuelCombustible(fuelCharacter);
        RealTuple fireReaction = getFireReaction(fuelCombustible);

        return FireBehaviorTuple.INVALID_TUPLE;
    }

    /**
     * Gets a FUEL_BED tuple derived from the supplied FuelModel and FuelMoisture
 objects. It transfers the cured live herbaceous into the dead 1 hour fuels.
     *
     * @param model The representative fuel model.
     * @param moisture The fuel moisture scenario.
     * @return A new FUEL_BED tuple; an "isMissing" tuple will be returned on error.
     */
    public static FuelBed getFuelCharacter(FuelModel model, FuelMoisture moisture) {
        return FuelBed.from(model, moisture);
    }

    /**
     * Gets a COMBUSTIBLE tuple derived from the supplied FuelBed tuple.
     *
     * In all fire behavior simulation systems that use the Rothermel model, total mineral content
     * is 5.55 percent, effective (silica-free) mineral content is 1.00 percent, and oven-dry fuel
     * particle density is 513 kg/m3 (32 lb/ft3).
     *
     * @param fuel The representative FUEL_BED.
     * @return A new COMBUSTIBLE tuple; an "isMissing" tuple will be returned on error.
     */
    public static RealTuple getFuelCombustible(FuelBed fuel) {
        if (fuel.isMissing()) {
            throw new IllegalArgumentException("Missing commponent(s): " + fuel);
        }
        try {
            Real sigma = fuel.getCharacteristicSAV();
            Real rho_b = fuel.getMeanBulkDensity();
            Real beta = fuel.getMeanPackingRatio();
            Real beta_opt = fuel.getOptimalPackingRatio();
            Real eta_s = fuel.getMineralDamping();
            Real eta_M1 = fuel.getMoistureDamping();
            Real Mx_live = fuel.getLiveMoistureOfExt();

            RealTuple tuple = new RealTuple(COMBUSTIBLE, new Real[]{
                sigma,
                Mx_live,
                rho_b,
                beta,
                beta_opt}, null);

            // Conditional logging
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(tuple.longString());
            }
            return tuple;

        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            return new RealTuple(COMBUSTIBLE);
        }

    }

    public static RealTuple getFireReaction(RealTuple combustible) {
        if (!combustible.getType().equals(COMBUSTIBLE)) {
            throw new IllegalArgumentException(
                    "Incompatible MathType: " + combustible.getType());
        } else if (combustible.isMissing()) {
            throw new IllegalArgumentException(
                    "Missing commponent(s): " + combustible);
        }
        try {
            return new RealTuple(COMBUSTIBLE);

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return new RealTuple(COMBUSTIBLE);
        }
    }

}
