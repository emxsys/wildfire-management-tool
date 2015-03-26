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
import com.emxsys.solar.api.Sunlight;
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import static com.emxsys.wildfire.api.WildfireType.FUEL_TEMP_F;
import java.util.HashMap;
import java.util.Objects;
import visad.Real;

/**
 * The SurfaceFuelProvider is a SurfaceFuel factory capable of caching SurfaceFuel objects. The
 * surface fuel's dead 1-hour fuel moisture is computed using an "instantaneous" wetting or drying
 * computation, versus the traditional 1-hour time lag formula. Per K. Anderson, "This approach
 * produces diurnal variations closer to expected values and when used in fire-growth modeling,
 * over-predictions are reduced by 30%."
 * <p>
 * References:
 * <ul>
 * <li><a name="bib_1002"></a>Anderson, K., 2009, A Comparison of Hourly Fire Fuel Moisture Code
 * Calculations within Canada, Canadian Forest Service
 * </ul>
 *
 * @author Bruce Schubert
 */
public class SurfaceFuelProvider {

    /** SurfaceFuel cache */
    private final HashMap<FuelScenario, SurfaceFuel> cache = new HashMap<>();

    /**
     * Gets a cached SurfaceFuel object the from the given parameters, computes and caches the fuel
     * object if not found.
     *
     * @param fuelModel The fuel model representative of fuel loading and SAV ratios.
     * @param fuelMoisture The 'adjusted' or active fuel moisture values to use.
     * @return A SurfaceFuel object created from the fuel model and fuel moisture values.
     */
    public SurfaceFuel getSurfaceFuel(FuelModel fuelModel, FuelMoisture fuelMoisture) {

        FuelScenario key = new FuelScenario(fuelModel, fuelMoisture);
        SurfaceFuel fuel = cache.get(key);
        if (fuel == null) {
            Real fuelTemp = new Real(FUEL_TEMP_F); // "missing" value
            fuel = SurfaceFuel.from(fuelModel, fuelMoisture, fuelTemp);
            cache.put(key, fuel);
        }
        return fuel;
    }

    /**
     * Gets a SurfaceFuel object from the given environmental parameters. Dead 1-hour fuel moisture
     * is computed using an "instantaneous" wetting or drying computation, versus the traditional
     * 1-hour time lag formula. This approach is computationally more performant.
     *
     * @param fuelModel The fuel model representative of fuel loading and SAV ratios.
     * @param sun The current sunlight prevailing upon the fuel
     * @param wx The current weather acting on the fuel
     * @param terrain The slope, aspect and elevation of the fuel
     * @param shaded Set true if the fuel is currently shaded (by terrain or plume or night)
     * @param initialFuelMoisture Previous hour's fuel moisture - determines a wetting or drying
     * trend.
     *
     * @return A new SurfaceFuel for the given conditions.
     *
     * @see Rothermel
     *
     */
    public SurfaceFuel getSurfaceFuel(FuelModel fuelModel,
                                      Sunlight sun, Weather wx,
                                      Terrain terrain, boolean shaded,
                                      FuelMoisture initialFuelMoisture) {

        Real fuelTemp = SurfaceFuel.computeFuelTemperature(fuelModel, sun, wx, terrain, shaded);
        return getSurfaceFuel(fuelModel, fuelTemp, wx, initialFuelMoisture);
    }

    /**
     * Gets a SurfaceFuel object from the given environmental parameters. Dead 1-hour fuel moisture
     * is computed using an "instantaneous" wetting or drying computation, versus the traditional
     * 1-hour time lag formula. This approach is computationally more performant.
     *
     * @param fuelModel The representative fuel model with fuel loading and SAV ratios.
     * @param fuelTemperature The solar preheated fuel temperature.
     * @param weather Weather containing air temperature and relative humidity.
     * @param initialFuelMoisture Previous hour's fuel moisture - determines a wetting or drying
     * trend.
     *
     * @return A new SurfaceFuel for the given conditions.
     *
     * @see Rothermel
     *
     */
    public SurfaceFuel getSurfaceFuel(FuelModel fuelModel,
                                      Real fuelTemperature,
                                      Weather weather,
                                      FuelMoisture initialFuelMoisture) {

        Real dead1HrFuelMoisture = SurfaceFuel.computeFineFuelMoisture(
                fuelTemperature,
                weather.getAirTemperature(),
                weather.getRelativeHumidity(),
                initialFuelMoisture.getDead1HrFuelMoisture());

        FuelMoistureTuple adjustedFuelMoisture = FuelMoistureTuple.fromReals(
                dead1HrFuelMoisture,
                initialFuelMoisture.getDead10HrFuelMoisture(),
                initialFuelMoisture.getDead100HrFuelMoisture(),
                initialFuelMoisture.getLiveHerbFuelMoisture(),
                initialFuelMoisture.getLiveWoodyFuelMoisture());

        return SurfaceFuel.from(fuelModel, adjustedFuelMoisture, fuelTemperature);
    }

    /**
     * A simple POD structure used as a key in the 'fuels' HashMap.
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
            hash = 61 * hash + Objects.hashCode(this.fuelModel);
            hash = 61 * hash + Objects.hashCode(this.fuelMoisture);
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
            return Objects.equals(this.fuelMoisture, other.fuelMoisture);
        }
    }

}
