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
import static com.emxsys.visad.GeneralUnit.degC;
import static com.emxsys.visad.GeneralUnit.degF;
import static com.emxsys.visad.GeneralUnit.foot;
import static com.emxsys.visad.GeneralUnit.mph;
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_1H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_TEMP_F;
import java.util.HashMap;
import java.util.Objects;
import static visad.CommonUnit.radian;
import visad.Real;
import visad.VisADException;

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
        try {
            // Vegetation height [feet]
            double h_v = fuelModel.getFuelBedDepth().getValue(foot);

            // Weather Values
            double W = wx.getWindSpeed().getValue(mph); // 20' wind speed
            double S_c = shaded ? 100. : wx.getCloudCover().getValue(); // [percent]
            double T_a = wx.getAirTemperature().getValue(degF);
            double H_a = wx.getRelativeHumidity().getValue();

            // Atmospheric transparency
            // p    Qualitative description
            // 0.8  exceptionally clear atmosphere
            // 0.75 average clear forest atmosphere
            // 0.7  moderate forest (blue) haze
            // 0.6  dense haze
            double p = 0.75;

            // Terrain Values
            double E = terrain.getElevationMeters();
            double slope = terrain.getSlope().getValue(radian);
            double aspect = terrain.getAspect().getValue(radian);

            // Calculate solar irradiance
            double A = sun.getAltitudeAngle().getValue(radian);
            double Z = sun.getAzimuthAngle().getValue(radian);
            double M = Rothermel.calcOpticalAirMass(A, E);
            double I_a = Rothermel.calcAttenuatedIrradiance(M, S_c, p);
            double I = Rothermel.calcIrradianceOnASlope(slope, aspect, A, Z, I_a);

            // Calculate fuel temperature and humidity immediatly adjacent to fuel
            double U_h = Rothermel.calcWindSpeedNearFuel(W, h_v);
            double T_f = Rothermel.calcFuelTemp(I, T_a, U_h); // fahrenheit

            return getSurfaceFuel(fuelModel, new Real(FUEL_TEMP_F, T_f),
                    wx.getAirTemperature(), wx.getRelativeHumidity(), initialFuelMoisture);

//            double H_f = Rothermel.calcRelativeHumidityNearFuel(H_a, T_f, T_a);
//
//            // Compute fine dead fuel moisture... requires metric values
//            // and temp and humidity adjusted for solar preheating.
//            double T_c = degF.toThat(T_f, degC); // convert to Celsius
//            double m_0 = initialFuelMoisture.getDead1HrFuelMoisture().getValue();
//            double m = Rothermel.calcFineDeadFuelMoisture(m_0, T_c, H_f);   // instantaneous wetting/drying
//
//            // Round the fuel moisture to reduce the number entries in cache
//            Real deadFineFuelMoisture = new Real(FUEL_MOISTURE_1H, m);//MathUtil.round(m, m < 2 ? 1 : 2));
//
//            FuelMoisture adjustedFuelMoisture = FuelMoistureTuple.fromReals(
//                    deadFineFuelMoisture,
//                    initialFuelMoisture.getDead10HrFuelMoisture(),
//                    initialFuelMoisture.getDead100HrFuelMoisture(),
//                    initialFuelMoisture.getLiveHerbFuelMoisture(),
//                    initialFuelMoisture.getLiveWoodyFuelMoisture());
//
//            // TODO: Add fuel temperature to the SurfaceFuel
//            Real fuelTemp = new Real(FUEL_TEMP_F, T_f);
//            return SurfaceFuel.from(fuelModel, adjustedFuelMoisture, fuelTemp);
//
        } catch (VisADException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets a SurfaceFuel object from the given environmental parameters. Dead 1-hour fuel moisture
     * is computed using an "instantaneous" wetting or drying computation, versus the traditional
     * 1-hour time lag formula. This approach is computationally more performant.
     *
     * @param fuelModel The fuel model representative of fuel loading and SAV ratios.
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
                                      Real airTemperature,
                                      Real relHumidity,
                                      FuelMoisture initialFuelMoisture) {
        try {

            // Weather inputs
            double Ta_f = airTemperature.getValue(degF);
            double Ha = relHumidity.getValue();    // %

            // Calculate humidity immediatly adjacent to fuel
            double Tf_f = fuelTemperature.getValue(degF); // fahrenheit
            double Hf = Rothermel.calcRelativeHumidityNearFuel(Ha, Tf_f, Ta_f); // humidity at fuel

            // Compute fine dead fuel moisture... requires metric values;
            // temp and humidity have been adjusted for solar preheating.
            double Tf_c = fuelTemperature.getValue(degC); // celsius
            double m_0 = initialFuelMoisture.getDead1HrFuelMoisture().getValue();
            double m = Rothermel.calcFineDeadFuelMoisture(m_0, Tf_c, Hf);   // instantaneous wetting/drying

            // Round the fuel moisture to reduce the number entries in cache
            Real deadFineFuelMoisture = new Real(FUEL_MOISTURE_1H, m);//MathUtil.round(m, m < 2 ? 1 : 2));

            FuelMoisture adjustedFuelMoisture = FuelMoistureTuple.fromReals(
                    deadFineFuelMoisture,
                    initialFuelMoisture.getDead10HrFuelMoisture(),
                    initialFuelMoisture.getDead100HrFuelMoisture(),
                    initialFuelMoisture.getLiveHerbFuelMoisture(),
                    initialFuelMoisture.getLiveWoodyFuelMoisture());

            // Add fuel temperature to the SurfaceFuel
            Real fuelTemp = new Real(FUEL_TEMP_F, Tf_f);
            return SurfaceFuel.from(fuelModel, adjustedFuelMoisture, fuelTemp);

        } catch (VisADException ex) {
            throw new RuntimeException(ex);
        }
    }

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
