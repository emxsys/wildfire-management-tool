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

import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelCondition;
import com.emxsys.wildfire.api.WeatherConditions;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.Fuel;
import com.emxsys.gis.api.Terrain;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import com.emxsys.wildfire.api.StdFuelModel;
import static com.emxsys.wildfire.behave.BehaveUtil.*;
import static com.emxsys.wildfire.api.WildfireType.FUEL_MOISTURE_1H;
import static com.emxsys.wildfire.api.WildfireType.FUEL_TEMP_F;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;
import visad.CommonUnit;
import visad.Real;
import visad.VisADException;

/**
 * SurfaceFuel is a concrete implementation of the Fuel interface. It represents the current state
 * of a surface fuel.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SurfaceFuel implements Fuel {

    private FuelModel fuelModel;
    private List<FuelCondition> conditions;

    /**
     * Construct fuel from a fuel model number or code.
     *
     * @param fuelModelNo
     */
    public SurfaceFuel(int fuelModelNo) {
        this.fuelModel = StdFuelModel.getFuelModel(fuelModelNo);
        this.conditions = new ArrayList<>();
    }

    /**
     * Construct a fuel object from a fuel model
     *
     * @param fm
     */
    SurfaceFuel(FuelModel fm) {
        this.fuelModel = fm;
        this.conditions = new ArrayList<>();
    }

    /**
     * Adjust the fuel moistures based on the previous weeks weather.
     *
     * @param prevWeekWxConditions
     */
    public void adjustFuelMoistures(WeatherConditions prevWeekWxConditions) {
        FuelMoistureTuple sfm = new FuelMoistureTuple(prevWeekWxConditions);
        //fuelMoistures.put(null, sfm);

    }

    /**
     *
     * @return the fuel model associated with this fuel instance
     */
    @Override
    public FuelModel getFuelModel() {
        return this.fuelModel;
    }

    /**
     *
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
     * Adjust the fuel conditions based on supplied weather.
     *
     * @param prevWeekWxConditions
     */
    @Override
    public void adjustFuelConditions(Sunlight solar, List<Real> airTemps, List<Real> humidities,
                                     Real windSpd, Real windDir, Terrain terrain, FuelMoisture initialFuelMoisture) {

        // TODO: Adjust the sky covert (S_c) based on terrestrial shading or plume shading. 
        // We need to include a list of sky cover values in the params.
        try {

            // hour sunset [solar time]
            double t_s = 0;//FIXME: solar.getSunsetHour();
            // hour sunrise [solar time]
            double t_r = 0;//FIXME: solar.getSunriseHour();

            // Latitude [radians]
            double phi = 0;// FIXME: solar.getLatitude().getValue(CommonUnit.radian);
            // Declination [radians]
            double delta = solar.getDeclination().getValue(CommonUnit.radian);

            // Atmospheric transparency
            // p    Qualitative description
            // 0.8  exceptionally clear atmosphere
            // 0.75 average clear forest atmosphere
            // 0.7  moderate forest (blue) haze
            // 0.6  dense haze            
            double p = 0.75;
            // Sky/cloud cover [percent]
            double S_c = 0;
            // Rainfall [inches]
            double R = 0;

            // Elevation [meters]
            double E = terrain.getElevation().getValue(CommonUnit.meter);
            // Slope [radians]
            double slope = terrain.getSlope().getValue(CommonUnit.radian);
            // Aspect [radians]
            double aspect = terrain.getAspect().getValue(CommonUnit.radian);

            // Vegetation height [feet]
            double h_v = fuelModel.getFuelBedDepth().getValue(GeneralUnit.foot);
            // Wind speed [mph]
            double W = windSpd.getValue(GeneralUnit.mph);

            // Initial fine fuel moisture 
            double m_0 = initialFuelMoisture.getDead1HrFuelMoisture().getValue();
            // Remember the noontime fuel moisture for 1400 calculation
            double m_12 = 0;

            // Compute fuel moisture from preceeding day's WX
            long t = 14; // starting yesterday at 1400 local
            int i = 0;
            conditions.clear();
            do {
                double T_a = airTemps.get(i).getValue(GeneralUnit.degF);
                double H_a = humidities.get(i).getValue();
                double U_h = calcWindSpeedAtFuelLevel(W, h_v);
                double h = calcLocalHourAngle(t);
                double A = calcSolarAltitudeAngle(h, phi, delta);
                double Z = calcSolarAzimuthAngle(h, phi, delta, A);
                double M = calcOpticalAirMass(A, E);
                double I_a = calcAttenuatedIrradiance(M, S_c, p);
                double I = calcIrradianceOnASlope(slope, aspect, A, Z, I_a);
                double T_f = calcFuelTemp(I, T_a, U_h); // fahrenheit
                double H_f = calcRelativeHumidityNearFuel(H_a, T_f, T_a);

                // Compute hourly fine fuel moisture... requires metric values
                // and temp and humidity adjusted for solar preheating.
                double T_c = new Real(FUEL_TEMP_F, T_f).getValue(GeneralUnit.degC);
                double W_k = windSpd.getValue(GeneralUnit.kph);
                double m = calcCanadianHourlyFineFuelMoisture(m_0, H_f, T_c, W_k);
                m_0 = m;
                if (t == 12) {
                    m_12 = m;
                }

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
                cond.solarAzimuthAngle = Math.toDegrees(Z) + 90; // convert from E relative to N relative degs
                conditions.add(cond);

                // Compute the next hour
                t = (t < 23 ? t + 1 : 0);
                i++;

            } // Exit loop at noon of current day
            while (t != 14);

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
            do {
                double T_a = airTemps.get(i).getValue(GeneralUnit.degF);
                double H_a = humidities.get(i).getValue();
                double U_h = calcWindSpeedAtFuelLevel(W, h_v);
                double h = calcLocalHourAngle(t);
                double A = calcSolarAltitudeAngle(h, phi, delta);
                double Z = calcSolarAzimuthAngle(h, phi, delta, A);
                double M = calcOpticalAirMass(A, E);
                double I_a = calcAttenuatedIrradiance(M, S_c, p);
                double I = calcIrradianceOnASlope(slope, aspect, A, Z, I_a);
                double T_f = calcFuelTemp(I, T_a, U_h);
                double H_f = calcRelativeHumidityNearFuel(H_a, T_f, T_a);

                // Compute hourly fine fuel moisture... requires metric values
                //double T_c = airTemps.get(i).getValue(GeneralUnit.degC); -- bug,need to adjust temp for solar heating
                double T_c = new Real(FUEL_TEMP_F, T_f).getValue(GeneralUnit.degC);
                double W_k = windSpd.getValue(GeneralUnit.kph);
                double m = calcCanadianHourlyFineFuelMoisture(m_0, H_f, T_c, W_k);
                m_0 = m;

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
                cond.solarAzimuthAngle = Math.toDegrees(Z) + 90; // convert from E relative to N relative degs
                conditions.add(cond);

                // Compute the next hour
                t = (t < 23 ? t + 1 : 0);
                i++;
                // Exit loop at noon of current day
            } while (t != 13);
            // Output the values
            //FuelChartTopComponent.getDefault().getPanel().plotFireBehavior(dataset);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * run 'java com.emxsys.api.wildland.SurfaceFuel' to test usage of this class
     */
    public static void main(String args[]) {

        SurfaceFuel wf = new SurfaceFuel(5);
        System.out.println(wf.toString());

    }
}
