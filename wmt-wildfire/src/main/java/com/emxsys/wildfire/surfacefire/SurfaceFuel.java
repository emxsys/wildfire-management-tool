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
import com.emxsys.weather.api.WeatherType;
import com.emxsys.wildfire.api.Fuel;
import com.emxsys.wildfire.api.FuelCondition;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import com.emxsys.wildfire.api.StdFuelModel;
import static com.emxsys.wildfire.api.WildfireType.*;
import static com.emxsys.wildfire.behave.BehaveUtil.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;
import static visad.CommonUnit.*;
import visad.Real;
import visad.VisADException;

/**
 * SurfaceFuel is a concrete implementation of the Fuel interface. It represents the current state
 * of a surface fuel.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SurfaceFuel implements Fuel {

    private final FuelModel fuelModel;
    private final List<FuelCondition> conditions = new ArrayList<>();

    /**
     * Construct SufaceFuel from a standard fuel model number or code.
     *
     * @param fuelModelNo An original 13 or standard 40 fuel model number.
     */
    public SurfaceFuel(int fuelModelNo) {
        this.fuelModel = StdFuelModel.getFuelModel(fuelModelNo);
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
    public List<FuelCondition> getConditions() {
        return this.conditions;
    }

    @Override
    public String toString() {
        return fuelModel.toString() + " " + conditions.toString();
    }

    /**
     * Adjust the fuel conditions based on previous 24hr weather.
     * @param time
     * @param coord
     * @param solar
     * @param weather
     * @param terrain
     * @param initialFuelMoisture
     */
    @Override
    public void adjustFuelConditions(ZonedDateTime time, Coord3D coord,
                                     SolarModel solar, WeatherModel weather, 
                                     Terrain terrain, FuelMoisture initialFuelMoisture) {

        // TODO: Adjust the sky covert (S_c) based on terrestrial shading or plume shading. 
        // We need to include a list of sky cover values in the params.
        try {
            // Condition the Fuel with the preceding 24 hours of weather
            FuelMoisture prevMoisture = initialFuelMoisture;
            for (int i = 24; i >= 0; i--) {
                ZonedDateTime earlier = time.minusHours(i);
                SunlightTuple solarTuple = solar.getSunlight(earlier, coord);
                if (solarTuple.isMissing()) {
                    throw new IllegalArgumentException("solar");
                }
                WeatherTuple wxTuple = weather.getWeather(earlier);
                if (wxTuple.isMissing()) {
                    throw new IllegalArgumentException("weather");
                }
                FuelCondition condition = calcFuelCondition(coord, solarTuple, wxTuple, terrain, fuelModel, prevMoisture);
                prevMoisture = condition.fuelMoisture;
                conditions.add(condition);
            }

//            Real sunrise_hour = Tuples.getComponent(SolarType.SUNRISE_HOUR_ANGLE, solarTuple);
//            Real sunset_hour = Tuples.getComponent(SolarType.SUNSET_HOUR_ANGLE, solarTuple);
//            // hour sunset [solar time]
//            double t_s = sunset_hour.getValue() / 15;
//            // hour sunrise [solar time]
//            double t_r = sunrise_hour.getValue() / 15;
//
//            // Solar Observer Latitude [radians]
//            double phi = coord.getLatitude().getValue(radian);
//
//            // Declination [radians]    
//            // TODO: Change tuple to include DECLINATION
//            Real declination = Tuples.getComponent(SolarType.SUBSOLAR_LATITUDE, solarTuple);
//            double delta = declination.getValue(radian);
//
//            // Atmospheric transparency
//            // p    Qualitative description
//            // 0.8  exceptionally clear atmosphere
//            // 0.75 average clear forest atmosphere
//            // 0.7  moderate forest (blue) haze
//            // 0.6  dense haze            
//            double p = 0.75;
//            // Sky/cloud cover [percent]
//            double S_c = Tuples.getComponent(WeatherType.CLOUD_COVER, wxTuple).getValue();
//            // Rainfall [inches]
//            double R = Tuples.getComponent(WeatherType.RAINFALL_INCH, wxTuple).getValue(inch);
//
//            // Elevation [meters]
//            double E = terrain.getElevation().getValue(meter);
//            // Slope [radians]
//            double slope = terrain.getSlope().getValue(radian);
//            // Aspect [radians]
//            double aspect = terrain.getAspect().getValue(radian);
//
//            // Vegetation height [feet]
//            double h_v = fuelModel.getFuelBedDepth().getValue(foot);
//            // Wind speed [mph]
//            double W = Tuples.getComponent(WeatherType.WIND_SPEED_MPH, wxTuple).getValue(mph);
//
//            // Initial fine fuel moisture 
//            double m_0 = initialFuelMoisture.getDead1HrFuelMoisture().getValue();
//            // Remember the noontime fuel moisture for 1400 calculation
//            double m_12 = 0;
//
            // Compute fuel moisture from preceeding day's WX
//            long t = 14; // starting yesterday at 1400 local
//            int i = 0;
//            conditions.clear();
//            do {
//                wxTuple = weather.getWeather(time.minusHours(24));
//                if (wxTuple.isMissing()) {
//                    throw new IllegalArgumentException("weather");
//                }
//                double T_a = Tuples.getComponent(WeatherType.AIR_TEMP_F, wxTuple).getValue(degF);
//                double H_a = humidities.get(i).getValue();
//                double U_h = calcWindSpeedAtFuelLevel(W, h_v);
//                double h = calcLocalHourAngle(t);
//                double A = calcSolarAltitudeAngle(h, phi, delta);
//                double Z = calcSolarAzimuthAngle(h, phi, delta, A);
//                double M = calcOpticalAirMass(A, E);
//                double I_a = calcAttenuatedIrradiance(M, S_c, p);
//                double I = calcIrradianceOnASlope(slope, aspect, A, Z, I_a);
//                double T_f = calcFuelTemp(I, T_a, U_h); // fahrenheit
//                double H_f = calcRelativeHumidityNearFuel(H_a, T_f, T_a);
//
//                // Compute hourly fine fuel moisture... requires metric values
//                // and temp and humidity adjusted for solar preheating.
//                double T_c = new Real(FUEL_TEMP_F, T_f).getValue(GeneralUnit.degC);
//                double W_k = windSpd.getValue(GeneralUnit.kph);
//                double m = calcCanadianHourlyFineFuelMoisture(m_0, H_f, T_c, W_k);
//                m_0 = m;
//                if (t == 12) {
//                    m_12 = m;
//                }
//
//                FuelMoistureTuple adjustedFuelMoisture = new FuelMoistureTuple(
//                        new Real(FUEL_MOISTURE_1H, m),
//                        initialFuelMoisture.getDead10HrFuelMoisture(),
//                        initialFuelMoisture.getDead100HrFuelMoisture(),
//                        initialFuelMoisture.getLiveHerbFuelMoisture(),
//                        initialFuelMoisture.getLiveWoodyFuelMoisture());
//
//                FuelCondition cond = new FuelCondition();
//                cond.airTemp = new Real(WeatherType.AIR_TEMP_F, T_a);
//                cond.fuelTemp = new Real(FUEL_TEMP_F, T_f);
//                cond.fuelMoisture = adjustedFuelMoisture;
//                cond.solarAzimuthAngle = Math.toDegrees(Z) + 90; // convert from E relative to N relative degs
//                conditions.add(cond);
//
//                // Compute the next hour
//                t = (t < 23 ? t + 1 : 0);
//                i++;
//
//            } // Exit loop at noon of current day
//            while (t != 14);
//            // Compute early afternoon (1400) fine fuel moisture from noon wx
//            // using Std Daily Fuel Moisture calc
//            t = 14; // Using wx at noon to compute 1400
//            {
//                double U_h = calcWindSpeedAtFuelLevel(W, h_v);
//                double h = calcLocalHourAngle(t);
//                double A = calcSolarAltitudeAngle(h, phi, delta);
//                double Z = calcSolarAzimuthAngle(h, phi, delta, A);
//                double M = calcOpticalAirMass(A, E);
//                double I_a = calcAttenuatedIrradiance(M, S_c, p);
//                double I = calcIrradianceOnASlope(slope, aspect, A, Z, I_a);
//                double T_a12 = airTemps.get(i-2).getValue(GeneralUnit.degF); // get temp at noon
//                double T_a14 = airTemps.get(i).getValue(GeneralUnit.degF);   // get temp at 1400
//                double T_f12 = calcFuelTemp(I, T_a12, U_h);
//                double T_f14 = calcFuelTemp(I, T_a14, U_h);
//                double H_a12 = humidities.get(i-2).getValue();   // get humidity at noon
//                double H_a14 = humidities.get(i).getValue();     // get humidity at 1400
//                double H_f = calcRelativeHumidityNearFuel(H_a12, T_f12, T_a12);
//                
//                // compute std fuel moisture for 1400 with 1200 wx
//                double m = calcCanadianStandardDailyFineFuelMoisture(m_12, T_f12, H_f, W, R);                
//                m_0 = m;
//                
//                FuelMoistureTuple adjustedFuelMoisture = new FuelMoistureTuple(
//                    new Real(FUEL_MOISTURE_1H, m),
//                    initialFuelMoisture.getDead10HrFuelMoisture(),
//                    initialFuelMoisture.getDead100HrFuelMoisture(),
//                    initialFuelMoisture.getLiveHerbFuelMoisture(),
//                    initialFuelMoisture.getLiveWoodyFuelMoisture());
//
//                // Although, fuel moisture was base on 1200 hrs, record the 1400 hrs conditions
//                FuelCondition cond = new FuelCondition();
//                cond.airTemp = new Real(WeatherType.AIR_TEMP_F, T_a14);
//                cond.fuelTemp = new Real(FUEL_TEMP_F, T_f14);
//                cond.fuelMoisture = adjustedFuelMoisture;
//                cond.solarAzimuthAngle = Math.toDegrees(Z) + 90; // convert from E relative to N relative degs
//                conditions.add(cond);   // 1400 hours
//
////                // Note, we skiped 1300 hours, just 
////                cond = new FuelCondition();
////                cond.airTemp = new Real(WeatherType.AIR_TEMP_F, T_12);
////                cond.fuelTemp = new Real(FUEL_TEMP_F, T_f);
////                cond.fuelMoisture = adjustedFuelMoisture;
////                conditions.add(cond);   // 1300 hours
//                i++;
//            }
//            // After noon WX
//            t = 15;
//            i = 24;
//            do {
//                double T_a = airTemps.get(i).getValue(GeneralUnit.degF);
//                double H_a = humidities.get(i).getValue();
//                double U_h = calcWindSpeedAtFuelLevel(W, h_v);
//                double h = calcLocalHourAngle(t);
//                double A = calcSolarAltitudeAngle(h, phi, delta);
//                double Z = calcSolarAzimuthAngle(h, phi, delta, A);
//                double M = calcOpticalAirMass(A, E);
//                double I_a = calcAttenuatedIrradiance(M, S_c, p);
//                double I = calcIrradianceOnASlope(slope, aspect, A, Z, I_a);
//                double T_f = calcFuelTemp(I, T_a, U_h);
//                double H_f = calcRelativeHumidityNearFuel(H_a, T_f, T_a);
//
//                // Compute hourly fine fuel moisture... requires metric values
//                //double T_c = airTemps.get(i).getValue(GeneralUnit.degC); -- bug,need to adjust temp for solar heating
//                double T_c = new Real(FUEL_TEMP_F, T_f).getValue(GeneralUnit.degC);
//                double W_k = windSpd.getValue(GeneralUnit.kph);
//                double m = calcCanadianHourlyFineFuelMoisture(m_0, H_f, T_c, W_k);
//                m_0 = m;
//
//                FuelMoistureTuple adjustedFuelMoisture = new FuelMoistureTuple(
//                        new Real(FUEL_MOISTURE_1H, m),
//                        initialFuelMoisture.getDead10HrFuelMoisture(),
//                        initialFuelMoisture.getDead100HrFuelMoisture(),
//                        initialFuelMoisture.getLiveHerbFuelMoisture(),
//                        initialFuelMoisture.getLiveWoodyFuelMoisture());
//
//                FuelCondition cond = new FuelCondition();
//                cond.airTemp = new Real(WeatherType.AIR_TEMP_F, T_a);
//                cond.fuelTemp = new Real(FUEL_TEMP_F, T_f);
//                cond.fuelMoisture = adjustedFuelMoisture;
//                cond.solarAzimuthAngle = Math.toDegrees(Z) + 90; // convert from E relative to N relative degs
//                conditions.add(cond);
//
//                // Compute the next hour
//                t = (t < 23 ? t + 1 : 0);
//                i++;
//                // Exit loop at noon of current day
//            } while (t != 13);
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
     * @return
     * @throws VisADException
     * @see RothermelSupport
     */
    static FuelCondition calcFuelCondition(Coord2D coord, SunlightTuple sun, WeatherTuple wx,
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

        FuelMoistureTuple adjustedFuelMoisture = new FuelMoistureTuple(
                new Real(FUEL_MOISTURE_1H, m),
                initialFuelMoisture.getDead10HrFuelMoisture(),
                initialFuelMoisture.getDead100HrFuelMoisture(),
                initialFuelMoisture.getLiveHerbFuelMoisture(),
                initialFuelMoisture.getLiveWoodyFuelMoisture());

        FuelCondition cond = new FuelCondition();
        cond.airTemp = new Real(WeatherType.AIR_TEMP_F, T_a);
        cond.fuelTemp = new Real(FUEL_TEMP_F, T_f);
        cond.fuelMoisture = adjustedFuelMoisture;
        cond.solarAzimuthAngle = sun.getAzimuthAngle().getValue(degree);

        return cond;
    }

//    /**
//     * Compute early afternoon (1400) fine fuel moisture from noon wx using Std Daily Fuel Moisture
//     * calculation.
//     */
//    private static void calcEarlyAfternoonFuel() {
//        // Early afternoon.-If the moisture is needed between 1200 and 1600, the daily
//        // value is sufficient and no adjustments are necessary. Personal discussions with
//        // Van Wagner confirm this view.
//        {
//            t = 14; // Using wx at noon to compute 1400
//            double U_h = calcWindSpeedAtFuelLevel(W, h_v);
//            double h = calcLocalHourAngle(t);
//            double A = calcSolarAltitudeAngle(h, phi, delta);
//            double Z = calcSolarAzimuthAngle(h, phi, delta, A);
//            double M = calcOpticalAirMass(A, E);
//            double I_a = calcAttenuatedIrradiance(M, S_c, p);
//            double I = calcIrradianceOnASlope(slope, aspect, A, Z, I_a);
//            double T_a12 = airTemps.get(i - 2).getValue(GeneralUnit.degF); // get temp at noon
//            double T_a14 = airTemps.get(i).getValue(GeneralUnit.degF);   // get temp at 1400
//            double T_f12 = calcFuelTemp(I, T_a12, U_h);
//            double T_f14 = calcFuelTemp(I, T_a14, U_h);
//            double H_a12 = humidities.get(i - 2).getValue();   // get humidity at noon
//            double H_a14 = humidities.get(i).getValue();     // get humidity at 1400
//            double H_f = calcRelativeHumidityNearFuel(H_a12, T_f12, T_a12);
//
//            // compute std fuel moisture for 1400 with 1200 wx
//            double m = calcCanadianStandardDailyFineFuelMoisture(m_12, T_f12, H_f, W, R);
//            m_0 = m;
//
//            FuelMoistureTuple adjustedFuelMoisture = new FuelMoistureTuple(
//                    new Real(FUEL_MOISTURE_1H, m),
//                    initialFuelMoisture.getDead10HrFuelMoisture(),
//                    initialFuelMoisture.getDead100HrFuelMoisture(),
//                    initialFuelMoisture.getLiveHerbFuelMoisture(),
//                    initialFuelMoisture.getLiveWoodyFuelMoisture());
//
//            // Although, fuel moisture was base on 1200 hrs, record the 1400 hrs conditions
//            FuelCondition cond = new FuelCondition();
//            cond.airTemp = new Real(WeatherType.AIR_TEMP_F, T_a14);
//            cond.fuelTemp = new Real(FUEL_TEMP_F, T_f14);
//            cond.fuelMoisture = adjustedFuelMoisture;
//            cond.solarAzimuthAngle = Math.toDegrees(Z) + 90; // convert from E relative to N relative degs
//            conditions.add(cond);   // 1400 hours
//
////                // Note, we skiped 1300 hours, just 
////                cond = new FuelCondition();
////                cond.airTemp = new Real(WeatherType.AIR_TEMP_F, T_12);
////                cond.fuelTemp = new Real(FUEL_TEMP_F, T_f);
////                cond.fuelMoisture = adjustedFuelMoisture;
////                conditions.add(cond);   // 1300 hours
//            i++;
//        }
//    }
//    private static void calcAfterNoonFuel() {
//        //Late afternoon.-In late afternoon, in addition to the 1400 weather, the user
//        //must supply a weather forecast at the projection time. The temperature and
//        //humidity are assumed to follow cosine curves as shown in figure 8. If sunset
//        //conditions are hotter and drier than 1400, the curves will arc in opposite forms.
//        double T_a = airTemps.get(i).getValue(GeneralUnit.degF);
//        double H_a = humidities.get(i).getValue();
//        double U_h = calcWindSpeedAtFuelLevel(W, h_v);
//        double h = calcLocalHourAngle(t);
//        double A = calcSolarAltitudeAngle(h, phi, delta);
//        double Z = calcSolarAzimuthAngle(h, phi, delta, A);
//        double M = calcOpticalAirMass(A, E);
//        double I_a = calcAttenuatedIrradiance(M, S_c, p);
//        double I = calcIrradianceOnASlope(slope, aspect, A, Z, I_a);
//        double T_f = calcFuelTemp(I, T_a, U_h);
//        double H_f = calcRelativeHumidityNearFuel(H_a, T_f, T_a);
//
//        // Compute hourly fine fuel moisture... requires metric values
//        //double T_c = airTemps.get(i).getValue(GeneralUnit.degC); -- bug,need to adjust temp for solar heating
//        double T_c = new Real(FUEL_TEMP_F, T_f).getValue(GeneralUnit.degC);
//        double W_k = windSpd.getValue(GeneralUnit.kph);
//        double m = calcCanadianHourlyFineFuelMoisture(m_0, H_f, T_c, W_k);
//        m_0 = m;
//
//        FuelMoistureTuple adjustedFuelMoisture = new FuelMoistureTuple(
//                new Real(FUEL_MOISTURE_1H, m),
//                initialFuelMoisture.getDead10HrFuelMoisture(),
//                initialFuelMoisture.getDead100HrFuelMoisture(),
//                initialFuelMoisture.getLiveHerbFuelMoisture(),
//                initialFuelMoisture.getLiveWoodyFuelMoisture());
//
//        FuelCondition cond = new FuelCondition();
//        cond.airTemp = new Real(WeatherType.AIR_TEMP_F, T_a);
//        cond.fuelTemp = new Real(FUEL_TEMP_F, T_f);
//        cond.fuelMoisture = adjustedFuelMoisture;
//        cond.solarAzimuthAngle = Math.toDegrees(Z) + 90; // convert from E relative to N relative degs
//        conditions.add(cond);
//
//    }
}
