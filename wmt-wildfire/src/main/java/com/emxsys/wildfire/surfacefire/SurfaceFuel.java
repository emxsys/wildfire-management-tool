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
package com.emxsys.wildfire.surfacefire;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.Terrain;
import com.emxsys.solar.api.SolarModel;
import com.emxsys.solar.api.SunlightTuple;
import static com.emxsys.visad.GeneralUnit.*;
import com.emxsys.weather.api.WeatherModel;
import com.emxsys.weather.api.WeatherTuple;
import com.emxsys.wildfire.api.Fuel;
import com.emxsys.wildfire.api.FuelCondition;
import com.emxsys.wildfire.api.FuelConditionTuple;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import com.emxsys.wildfire.api.StdFuelModel;
import static com.emxsys.wildfire.api.WildfireType.*;
import static com.emxsys.wildfire.behave.BehaveUtil.*;
import java.time.ZonedDateTime;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import static visad.CommonUnit.*;
import visad.Real;
import visad.VisADException;

/**
 * SurfaceFuel is a concrete implementation of the Fuel interface. It represents the state of a
 * surface fuel at a moment in time.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SurfaceFuel implements Fuel {

    private final FuelModel fuelModel;
    private final TreeMap<ZonedDateTime, FuelCondition> conditions = new TreeMap<>();
    private static final Logger logger = Logger.getLogger(SurfaceFuel.class.getName());

    /**
     * Construct SufaceFuel from a standard fuel model number or code.
     *
     * @param fuelModelNo An original 13 or standard 40 fuel model number.
     */
    public SurfaceFuel(int fuelModelNo) {
        this.fuelModel = StdFuelModel.from(fuelModelNo);
    }

    /**
     * Construct a SurfaceFuel object from a fuel model.
     *
     * @param fm Fuel model for the SurfaceFuel instance.
     */
    SurfaceFuel(FuelModel fm) {
        this.fuelModel = fm;
    }

    /**
     * @return the fuel model associated with this fuel instance
     */
    @Override
    public FuelModel getFuelModel() {
        return this.fuelModel;
    }

    /**
     * @return the environmental and temporal conditions of the fuel.
     */
    @Override
    public FuelCondition getFuelCondition(ZonedDateTime time) {
        Entry<ZonedDateTime, FuelCondition> entry = conditions.floorEntry(time);
        return entry == null ? FuelConditionTuple.INVALID_TUPLE : entry.getValue();
    }

    @Override
    public String toString() {
        return fuelModel.toString() + " " + conditions.toString();
    }

    /**
     * Adjusts the fuel conditions based on previous 24hr weather.
     * @param time
     * @param coord
     * @param solar
     * @param weather
     * @param terrain
     * @param initialFuelMoisture
     */
    @Override
    public void condition(ZonedDateTime time, Coord3D coord,
                          SolarModel solar, WeatherModel weather,
                          Terrain terrain, FuelMoisture initialFuelMoisture) {
        // TODO: Adjust the sky covert (S_c) based on terrestrial shading or plume shading. 
        try {
            // Condition the Fuel with the preceding 24 hours of weather
            FuelMoisture prevMoisture = initialFuelMoisture;
            for (int i = 24; i >= 0; i--) {
                ZonedDateTime earlier = time.minusHours(i);
                SunlightTuple solarTuple = solar.getSunlight(earlier, coord);
                if (solarTuple.isMissing()) {
                    logger.log(Level.WARNING, "solarTuple has missing values @ {0}", earlier);
                    continue;
                    //throw new IllegalArgumentException("solar");
                }
                WeatherTuple wxTuple = weather.getWeather(earlier, coord);
                if (wxTuple.isMissing()) {
                    logger.log(Level.WARNING, "wxTuple has missing values @ {0}", earlier);
                    continue;
                    //throw new IllegalArgumentException("weather");
                }
                FuelConditionTuple condition = calcFuelCondition(coord, solarTuple, wxTuple, terrain, fuelModel, prevMoisture);
                conditions.put(earlier, condition);

                prevMoisture = condition.getFuelMoisture();
            }
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Calculates fuel moistures using the RothermelSupport.calcCanadianHourlyFineFuelMoisture()
     * @param coord Coordinate where the calculation is performed.
     * @param sun Sunlight at the coordinate
     * @param wx Weather at the coordinate
     * @param terrain Terrain at the coordinate
     * @param fuelModel Fuel at the coordinate
     * @param initialFuelMoisture Previous hour's fuel moisture
     * @return A new FuelConditionTuple for the given conditions.
     * @throws VisADException
     * @see RothermelSupport
     */
    static FuelConditionTuple calcFuelCondition(Coord2D coord, SunlightTuple sun, WeatherTuple wx,
                                                Terrain terrain, FuelModel fuelModel,
                                                FuelMoisture initialFuelMoisture) throws VisADException {
        // Vegetation height [feet]
        double h_v = fuelModel.getFuelBedDepth().getValue(foot);

        // Weather Values                                                
        double W = wx.getWindSpeed().getValue(mph);
        double W_k = wx.getWindSpeed().getValue(kph);
        double S_c = wx.getCloudCover().getValue();
        double T_a = wx.getAirTemperature().getValue(degF);
        double H_a = wx.getRelativeHumidity().getValue();
        double U_h = calcWindSpeedAtFuelLevel(W, h_v);
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

        // Solar Values
        //double h = calcLocalHourAngle(t); // from 0600
        double h = sun.getLocalHourAngle().getValue(radian);    // TODO: from 0600
        double phi = coord.getLatitude().getValue(radian);
        double delta = sun.getDeclination().getValue(radian);

        // Calculate solar irradiance 
        double A = calcSolarAltitudeAngle(h, phi, delta);
        double Z = calcSolarAzimuthAngle(h, phi, delta, A); // Relative to East
        double M = calcOpticalAirMass(A, E);
        double I_a = calcAttenuatedIrradiance(M, S_c, p);
        double I = calcIrradianceOnASlope(slope, aspect, A, Z, I_a);

        // Calculate fuel temperature and humidity immediatly adjacent to fuel
        double T_f = calcFuelTemp(I, T_a, U_h); // fahrenheit
        double H_f = calcRelativeHumidityNearFuel(H_a, T_f, T_a);

        // Compute hourly fine fuel moisture... requires metric values
        // and temp and humidity adjusted for solar preheating.
        double T_c = new Real(FUEL_TEMP_F, T_f).getValue(degC);
        double m_0 = initialFuelMoisture.getDead1HrFuelMoisture().getValue();
        double m = calcCanadianHourlyFineFuelMoisture(m_0, H_f, T_c, W_k);

        FuelMoistureTuple adjustedFuelMoisture = FuelMoistureTuple.fromReals(
                new Real(FUEL_MOISTURE_1H, m),
                initialFuelMoisture.getDead10HrFuelMoisture(),
                initialFuelMoisture.getDead100HrFuelMoisture(),
                initialFuelMoisture.getLiveHerbFuelMoisture(),
                initialFuelMoisture.getLiveWoodyFuelMoisture());

        FuelConditionTuple cond = FuelConditionTuple.fromReals(
                adjustedFuelMoisture, new Real(FUEL_TEMP_F, T_f));

        return cond;
    }
}
