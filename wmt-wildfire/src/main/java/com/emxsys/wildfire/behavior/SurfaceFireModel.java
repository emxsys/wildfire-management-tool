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

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.ShadedTerrainProvider;
import com.emxsys.gis.api.Terrain;
import com.emxsys.solar.api.SolarModel;
import com.emxsys.solar.api.SunlightTuple;
import static com.emxsys.visad.GeneralUnit.degC;
import static com.emxsys.visad.GeneralUnit.degF;
import static com.emxsys.visad.GeneralUnit.foot;
import static com.emxsys.visad.GeneralUnit.kph;
import static com.emxsys.visad.GeneralUnit.mph;
import com.emxsys.weather.api.Weather;
import com.emxsys.weather.api.WeatherModel;
import com.emxsys.weather.api.WeatherTuple;
import com.emxsys.wildfire.api.FuelCondition;
import com.emxsys.wildfire.api.FuelConditionTuple;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_1H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_TEMP_F;
import static com.emxsys.wildfire.behave.BehaveUtil.calcCanadianHourlyFineFuelMoisture;
import static com.emxsys.wildfire.behave.BehaveUtil.calcWindSpeedAtFuelLevel;
import static java.lang.Math.round;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import static visad.CommonUnit.radian;
import visad.Real;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class SurfaceFireModel {

    private static final Logger logger = Logger.getLogger(SurfaceFireModel.class.getName());
    private final HashMap<FuelScenario, Fuelbed> fuels = new HashMap<>();
    private final HashMap<FireEnvironment, FireReaction> fires = new HashMap<>();

    public SurfaceFireModel() {
    }

    /**
     * Calculates fuel moistures using the RothermelSupport.calcCanadianHourlyFineFuelMoisture().
     *
     * @param coord Coordinate where the calculation is performed.
     * @param sun Sunlight at the coordinate
     * @param wx Weather at the coordinate
     * @param terrain Terrain at the coordinate
     * @param shaded True if the coordinate is shaded (by terrain or plume)
     * @param fuelModel Fuel at the coordinate
     * @param initialFuelMoisture Previous hour's fuel moisture
     *
     * @return A new FuelConditionTuple for the given conditions.
     * @throws VisADException
     * @see RothermelSupport
     */
    public static FuelConditionTuple calcFuelCondition(Coord2D coord, SunlightTuple sun, WeatherTuple wx,
                                                       Terrain terrain, boolean shaded,
                                                       FuelModel fuelModel,
                                                       FuelMoisture initialFuelMoisture) throws VisADException {
        // Vegetation height [feet]
        double h_v = fuelModel.getFuelBedDepth().getValue(foot);

        // Weather Values                                                
        double W = wx.getWindSpeed().getValue(mph);
        double W_k = wx.getWindSpeed().getValue(kph);
        double S_c = shaded ? 100. : wx.getCloudCover().getValue(); // [percent]
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

        // Calculate solar irradiance 
        double A = sun.getAltitudeAngle().getValue(radian);
        double Z = sun.getAzimuthAngle().getValue(radian);
        double M = Rothermel.calcOpticalAirMass(A, E);
        double I_a = Rothermel.calcAttenuatedIrradiance(M, S_c, p);
        double I = Rothermel.calcIrradianceOnASlope(slope, aspect, A, Z, I_a);

        // Calculate fuel temperature and humidity immediatly adjacent to fuel
        double T_f = Rothermel.calcFuelTemp(I, T_a, U_h); // fahrenheit
        double H_f = Rothermel.calcRelativeHumidityNearFuel(H_a, T_f, T_a);

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

    /**
     * Adjusts the fuel conditions based on previous 24 hour weather.
     *
     * @param time
     * @param coord
     * @param solar
     * @param weather
     * @param earth
     * @param initialFuelMoisture
     * @param fuelModel
     *
     * @return
     */
    public FuelCondition getFuelCondition(ZonedDateTime time,
                                          Coord3D coord,
                                          SolarModel solar,
                                          WeatherModel weather,
                                          ShadedTerrainProvider earth,
                                          FuelMoisture initialFuelMoisture,
                                          FuelModel fuelModel) {
        try {
            // Condition the fuel (fuel moisture) with the preceding 24 hours of weather
            FuelMoisture prevMoisture = initialFuelMoisture;
            FuelConditionTuple condition = null; // return value
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
                Terrain terrain = earth.getTerrain(coord);
                boolean shaded = earth.isCoordinateTerrestialShaded(coord, solarTuple.getAzimuthAngle(), solarTuple.getZenithAngle());

                condition = calcFuelCondition(coord, solarTuple, wxTuple, terrain, shaded, fuelModel, prevMoisture);
                prevMoisture = condition.getFuelMoisture();
            }
            return condition;
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public Fuelbed getFuelbed(FuelModel fuelModel, FuelMoisture fuelMoisture) {
        FuelScenario key = new FuelScenario(fuelModel, fuelMoisture);
        Fuelbed fuelbed = fuels.get(key);
        if (fuelbed == null) {
            // Create a Fuelbed object
            fuelbed = Fuelbed.from(fuelModel, fuelMoisture);
            fuels.put(key, fuelbed);
        }
        return fuelbed;
    }

    public FireReaction getFireBehavior(Fuelbed fuel,
                                        Weather weather,
                                        Terrain terrain) {

        FireEnvironment key = new FireEnvironment(fuel, weather, terrain);
        FireReaction fire = fires.get(key);
        if (fire == null) {
            fire = FireReaction.from(fuel, weather, terrain);
            fires.put(key, fire);
        }
        return fire;
    }

    /**
     * A simple POD structure used as a key in the 'fires' HashMap.
     */
    private class FireEnvironment {

        Fuelbed fuelbed;
        double windSpd;
        int windDir;
        int aspect;
        int slope;

        FireEnvironment(Fuelbed fuelbed, Weather weather, Terrain terrain) {
            this(fuelbed, weather.getWindSpeed(), weather.getWindDirection(), terrain.getAspect(), terrain.getSlope());
        }

        FireEnvironment(Fuelbed fuelbed, Real windSpd, Real windDir, Real aspect, Real slope) {
            this.fuelbed = fuelbed;
            this.windSpd = windSpd.getValue();
            this.windDir = (int) round(windDir.getValue());
            this.aspect = (int) round(aspect.getValue());
            this.slope = (int) round(slope.getValue());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.fuelbed);
            hash = 37 * hash + (int) (Double.doubleToLongBits(this.windSpd) ^ (Double.doubleToLongBits(this.windSpd) >>> 32));
            hash = 37 * hash + this.windDir;
            hash = 37 * hash + this.slope;
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
            final FireEnvironment other = (FireEnvironment) obj;
            if (!Objects.equals(this.fuelbed, other.fuelbed)) {
                return false;
            }
            if (Double.doubleToLongBits(this.windSpd) != Double.doubleToLongBits(other.windSpd)) {
                return false;
            }
            if (this.windDir != other.windDir) {
                return false;
            }
            if (this.aspect != other.aspect) {
                return false;
            }
            return this.slope == other.slope;
        }

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
            return Objects.equals(this.fuelMoisture, other.fuelMoisture);
        }
    }
}
