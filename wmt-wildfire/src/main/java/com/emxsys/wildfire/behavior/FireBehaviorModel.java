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

import com.emxsys.gis.api.Terrain;
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.FireEnvironment;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import java.util.HashMap;
import java.util.Objects;
import visad.RealTuple;

/**
 *
 * @author Bruce Schubert
 */
public class FireBehaviorModel {

    /**
     * A simple POD used as a key in the characteristics HashMap.
     */
    private class FuelScenario {

        FuelModel fuelModel;
        FuelMoisture fuelMoisture;

        FuelScenario(FuelModel fuelModel, FuelMoisture fuelMoisture) {
            this.fuelModel = fuelModel;
            this.fuelMoisture = fuelMoisture;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.fuelModel);
            hash = 83 * hash + Objects.hashCode(this.fuelMoisture);
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
            final FuelScenario other = (FuelScenario) obj;
            if (!Objects.equals(this.fuelModel, other.fuelModel)) {
                return false;
            }
            if (!Objects.equals(this.fuelMoisture, other.fuelMoisture)) {
                return false;
            }
            return true;
        }

    }

    private final HashMap<FuelScenario, FuelCharacter> fuelCharacteristics = new HashMap<>();
    private final HashMap<FuelCharacter, RealTuple> combustibles = new HashMap<>();

    public FireEnvironment computeFireBehavior(FuelModel fuelModel,
                                               FuelMoisture fuelMoisture,
                                               Weather weather,
                                               Terrain terrain) {

        FuelScenario scenario = new FuelScenario(fuelModel, fuelMoisture);

        FuelCharacter fuel = fuelCharacteristics.get(scenario);
        if (fuel == null) {
            fuel = Rothermel.getFuelCharacter(fuelModel, fuelMoisture);
            fuelCharacteristics.put(scenario, fuel);
        }

        RealTuple combustible = combustibles.get(fuel);
        if (combustible == null) {
            combustible = Rothermel.getFuelCombustible(fuel);
            combustibles.put(fuel, combustible);
        }
        return null;
    }
}
