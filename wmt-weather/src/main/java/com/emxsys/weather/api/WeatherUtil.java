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
package com.emxsys.weather.api;

import com.emxsys.visad.GeneralUnit;
import static java.lang.Math.*;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.logging.Level;
import java.util.logging.Logger;
import visad.DateTime;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.VisADException;

/**
 * Utility class for working with Weather objects.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class WeatherUtil {

    static public RealTuple newSunriseSunsetTuple(ZonedDateTime date,
                                                  double timeOfSunrise,
                                                  double timeOfSunset) {
        try {
            int year = date.get(ChronoField.YEAR);
            int day = date.get(ChronoField.DAY_OF_YEAR);
            
            int sunrise = (int) (timeOfSunrise * 3600);
            int sunset = (int) (timeOfSunset * 3600);

            RealTuple times = new RealTuple(new Real[]{
                new DateTime(year, day, sunrise),
                new DateTime(year, day, sunset)
            });

            return times;
        } catch (VisADException ex) {
            Logger.getLogger(WeatherUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(WeatherUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    static public RealTuple newTemperaturesTuple(RealType type,
                                                 double valueAt1400,
                                                 double valueAtSunset,
                                                 double valueAtSunrise,
                                                 double valueAtNoon) {
        try {
            RealTuple values = new RealTuple(
                    new Real[]{
                        new Real(type, valueAt1400),
                        new Real(type, valueAtSunset),
                        new Real(type, valueAtSunrise),
                        new Real(type, valueAtNoon)
                    });
            return values;
        } catch (VisADException | RemoteException ex) {
            Logger.getLogger(WeatherUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     *
     * @param times for sunrise and sunset
     * @param airTemps values at 1400, sunset, sunrise and noon
     * @param humidities values at 1400, sunset, sunrise and noon
     * @param windSpeed general wind speed for entire period
     * @param windDir general wind direction for entire period
     * @param cloudCover general cloud cover for entire period
     * @return
     */
    static public FlatField makeGeneralFireWeather(
            RealTuple times, RealTuple airTemps, RealTuple humidities,
            Real windSpeed, Real windDir, Real cloudCover) {
        try {
            // Create a FunctionType ( index -> ( time, wndSpd, wndDir, airTemp, RH, cloudCover) )
            //  index has no unit, just a name
            // Use FunctionType(MathType domain, MathType range)
            RealType index = RealType.getRealType("index");
            FunctionType funcIndexTuple = new FunctionType(index, WeatherType.FIRE_WEATHER);

            // Create the domain_set, with 24 houly values
            // Integer1DSet(MathType type, int length)
            final int NUM_HOURS = 24;
            Set indexSet = new Integer1DSet(index, NUM_HOURS);

            // Create a FlatField, that is the Data class for the samples
            // Use FlatField(FunctionType type, Set domain_set)
            FlatField valuesFlatField = new FlatField(funcIndexTuple, indexSet);

            // Create the actual samples for the index domain
            double[][] vals = new double[WeatherType.FIRE_WEATHER.getDimension()][NUM_HOURS];
            for (int i = 0; i < NUM_HOURS; i++) {
                // Start at 1400 hours
                double hour = 14 + i;
                RealTuple diurnals = calcDiurnalValues(hour, times, airTemps, humidities);
                vals[0][i] = diurnals.getValues()[0];   // date time
                vals[1][i] = diurnals.getValues()[1];   // temp
                vals[2][i] = diurnals.getValues()[2];   // rh
                vals[3][i] = windSpeed.getValue();
                vals[4][i] = windDir.getValue();
                vals[5][i] = cloudCover.getValue();
            }
            // Store the values in the flat field
            valuesFlatField.setSamples(vals);

            return valuesFlatField;

        } catch (RemoteException ex) {
            Logger.getLogger(WeatherUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VisADException ex) {
            Logger.getLogger(WeatherUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    static private double dateTimeToHourOfDay(Real dateTime) {
        try {
            return dateTime.getValue(GeneralUnit.hour) % 24;
        } catch (VisADException ex) {
            Logger.getLogger(WeatherUtil.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    static private DateTime getMidnight(Real dateTime) {
        try {
            final double secsPerDay = 60 * 60 * 24;
            double secsSinceMidnight = dateTime.getValue() % secsPerDay;
            return new DateTime(dateTime.getValue() - secsSinceMidnight);
        } catch (VisADException ex) {
            Logger.getLogger(WeatherUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

//    static public FlatField calcDiurnalAirTempsC(
//            double localTimeOfSunrise, double localTimeOfSunset,
//            double valueAt1400, double valueAtSunset, double valueAtSunrise, double valueAtNoon)
//            throws VisADException, RemoteException {
//
//        // Organize times args into a tuple to be passed to function
//        RealTuple times = new RealTuple(new Real[]{
//                    new Real(WildfireType.SOLAR_HOUR, localTimeOfSunrise),
//                    new Real(WildfireType.SOLAR_HOUR, localTimeOfSunset)});
//
//        // Organize temperature args into a tuple to be passed to function
//        RealTuple temps = new RealTuple(new Real[]{
//                    new Real(WildfireType.AIR_TEMP_C, valueAt1400),
//                    new Real(WildfireType.AIR_TEMP_C, valueAtSunset),
//                    new Real(WildfireType.AIR_TEMP_C, valueAtSunrise),
//                    new Real(WildfireType.AIR_TEMP_C, valueAtNoon)});
//
//        return calcDiurnalValues(times, temps);
//
//    }
//
//    static public FlatField calcDiurnalHumidities(
//            double localTimeOfSunrise, double localTimeOfSunset,
//            double valueAt1400, double valueAtSunset, double valueAtSunrise, double valueAtNoon)
//            throws VisADException, RemoteException {
//
//        // Organize times args into a tuple to be passed to function
//        RealTuple times = new RealTuple(new Real[]{
//                    new Real(WildfireType.SOLAR_HOUR, localTimeOfSunrise),
//                    new Real(WildfireType.SOLAR_HOUR, localTimeOfSunset)});
//
//        // Organize temperature args into a tuple to be passed to function
//        RealTuple temps = new RealTuple(new Real[]{
//                    new Real(WildfireType.REL_HUMIDITY, valueAt1400),
//                    new Real(WildfireType.REL_HUMIDITY, valueAtSunset),
//                    new Real(WildfireType.REL_HUMIDITY, valueAtSunrise),
//                    new Real(WildfireType.REL_HUMIDITY, valueAtNoon)});
//
//        return calcDiurnalValues(times, temps);
//
//    }
    /**
     *
     * @param times for sunrise and sunset
     * @param values diurnal values at 1400, sunset, sunrise and noon
     * @return FunctionType ( index -> ( time, value ) )
     */
    static public FlatField calcDiurnalValues(RealTuple times, RealTuple values) {
        try {
            // Organize time and either temp or humidity in a Tuple
            RealTupleType diurnalType = new RealTupleType(
                    (RealType) times.getComponent(0).getType(),
                    (RealType) values.getComponent(0).getType());

            // Index has no unit, just a name
            RealType indexType = RealType.getRealType("index");

            // Create a FunctionType ( index -> ( time, temp ) )
            FunctionType funcIndexTuple = new FunctionType(indexType, diurnalType);

            // Create the domain_set, with 24 houly values
            final int LENGTH = 24;
            Set indexSet = new Integer1DSet(indexType, LENGTH);

            // Get the solar cycle params...
            Real midnight = getMidnight((Real) times.getComponent(0));
            double localTimeOfSunrise = dateTimeToHourOfDay((Real) times.getComponent(0));
            double localTimeOfSunset = dateTimeToHourOfDay((Real) times.getComponent(1));
            // Compute the actual data values for time and value
            double[][] vals = new double[2][LENGTH];
            for (int i = 0; i < LENGTH; i++) {
                double localTime = 14 + i;
                // time
                vals[0][i] = midnight.getValue() + (localTime * 60 * 60);
                // value (temp or humidity)
                vals[1][i] = (float) calcDiurnalValueForTimeOfDay(
                        localTime,
                        localTimeOfSunrise,
                        localTimeOfSunset,
                        values.getValues()[0],
                        values.getValues()[1],
                        values.getValues()[2],
                        values.getValues()[3]);
            }
            // Create a FlatField, that is the Data class for the samples
            // Use FlatField(FunctionType type, Set domain_set)
            FlatField valuesFlatField = new FlatField(funcIndexTuple, indexSet);
            // and put the time_temp values from above in it
            valuesFlatField.setSamples(vals);
            return valuesFlatField;
        } catch (RemoteException ex) {
            Logger.getLogger(WeatherUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VisADException ex) {
            Logger.getLogger(WeatherUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     *
     * @param times for sunrise and sunset
     * @param airTemps temperature values at 1400, sunset, sunrise and noon
     * @param humidities relative humidity values at 1400, sunset, sunrise and noon
     * @return FunctionType ( index -> ( time, temperature, humidity ) )
     * @throws VisADException
     * @throws RemoteException
     */
    static public FlatField calcDiurnalValues(RealTuple times,
                                              RealTuple airTemps, RealTuple humidities)
            throws VisADException, RemoteException {

        // Organize time and either temp or humidity in a Tuple
        RealTupleType diurnalType = new RealTupleType(
                (RealType) times.getComponent(0).getType(),
                (RealType) airTemps.getComponent(0).getType(),
                (RealType) humidities.getComponent(0).getType());

        // Index has no unit, just a name
        RealType index = RealType.getRealType("index");

        // Create a FunctionType ( index -> ( time, temp, rh) )
        // Use FunctionType(MathType domain, MathType range)
        FunctionType funcIndexTuple = new FunctionType(index, diurnalType);

        // Create the domain_set, with 24 houly values
        // Integer1DSet(MathType type, int length)
        final int LENGTH = 24;
        Set indexSet = new Integer1DSet(index, LENGTH);

        // Get the solar cycle params...
        Real midnight = getMidnight((Real) times.getComponent(0));
        double localTimeOfSunrise = dateTimeToHourOfDay((Real) times.getComponent(0));
        double localTimeOfSunset = dateTimeToHourOfDay((Real) times.getComponent(1));

        // Compute the actual data values for time, temp and humidity
        double[][] vals = new double[3][LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            double localTime = 14 + i;
            // time
            vals[0][i] = midnight.getValue() + (localTime * 60 * 60);
            // temp
            vals[1][i] = (float) calcDiurnalValueForTimeOfDay(
                    localTime,
                    localTimeOfSunrise,
                    localTimeOfSunset,
                    airTemps.getValues()[0],
                    airTemps.getValues()[1],
                    airTemps.getValues()[2],
                    airTemps.getValues()[3]);
            // humidity
            vals[2][i] = (float) calcDiurnalValueForTimeOfDay(
                    localTime,
                    localTimeOfSunrise,
                    localTimeOfSunset,
                    humidities.getValues()[0],
                    humidities.getValues()[1],
                    humidities.getValues()[2],
                    humidities.getValues()[3]);
        }
        // Create a FlatField, that is the Data class for the samples
        // Use FlatField(FunctionType type, Set domain_set)
        FlatField valuesFlatField = new FlatField(funcIndexTuple, indexSet);

        // and put the time_temp values from above in it
        valuesFlatField.setSamples(vals);

        return valuesFlatField;
    }

    static public RealTuple calcDiurnalValues(double hour,
                                              RealTuple times, RealTuple airTemps, RealTuple humidities)
            throws VisADException, RemoteException {

        double midnight = getMidnight((Real) times.getComponent(0)).getValue();
        double localTimeOfSunrise = dateTimeToHourOfDay((Real) times.getComponent(0));
        double localTimeOfSunset = dateTimeToHourOfDay((Real) times.getComponent(1));

        RealTupleType diurnalType = new RealTupleType(
                (RealType) times.getComponent(0).getType(),
                (RealType) airTemps.getComponent(0).getType(),
                (RealType) humidities.getComponent(0).getType());
        double[] diurnalVals = new double[diurnalType.getDimension()];

        diurnalVals[0] = midnight + (hour * 60 * 60);
        diurnalVals[1] = calcDiurnalValueForTimeOfDay(
                hour,
                localTimeOfSunrise,
                localTimeOfSunset,
                airTemps.getValues()[0],
                airTemps.getValues()[1],
                airTemps.getValues()[2],
                airTemps.getValues()[3]);
        diurnalVals[2] = calcDiurnalValueForTimeOfDay(
                hour,
                localTimeOfSunrise,
                localTimeOfSunset,
                humidities.getValues()[0],
                humidities.getValues()[1],
                humidities.getValues()[2],
                humidities.getValues()[3]);

        return new RealTuple(diurnalType, diurnalVals);

    }

    /**
     * Computes the diurnal values within the daily cycle beginning with the day's 1400 value and
     * ending with next day's 1200 value. This method can compute values for either temperature or
     * relative humidity.
     *
     * @param localTime local time of day for desired value [solar hour]
     * @param localTimeOfSunrise sunrise time [solar hour]
     * @param localTimeOfSunset sunset time [solar hour]
     * @param valueAt1400 value at 1400 hrs
     * @param valueAtSunset value at sunset
     * @param valueAtSunrise value at sunrise
     * @param valueAtNoon value at noon
     * @return computed value at local time within the normal diurnal Sinusoidal curve
     */
    static public double calcDiurnalValueForTimeOfDay(
            double localTime,
            double localTimeOfSunrise, double localTimeOfSunset,
            double valueAt1400, double valueAtSunset, double valueAtSunrise, double valueAtNoon) {

        localTime %= 24;
        double value;

        // Afternoon
        if (localTime >= 13.5 && localTime < localTimeOfSunset) {
            value = calcDiurnalValueForAfternoon(localTime, localTimeOfSunset, valueAt1400, valueAtSunset);
        } // Nightime
        else if ((localTime >= localTimeOfSunset && localTime < 24) || (localTime < localTimeOfSunrise)) {
            value = calcDiurnalValueForNighttime(localTime, localTimeOfSunset, localTimeOfSunrise, valueAtSunset, valueAtSunrise);
        } // Morning
        else if (localTime >= localTimeOfSunrise && localTime < 11.5) {
            value = calcDiurnalValueForMorning(localTime, localTimeOfSunrise, valueAtSunrise, valueAtNoon);
        } // Noon
        else if (localTime >= 11.5 && localTime < 12.5) {
            value = valueAtNoon;
        } // Early afternon
        else {
            value = (valueAtNoon + valueAt1400) / 2;    // Interpolate between noon and 1400
        }
        return value;
    }

    /**
     * Sinusoidal curve linking values at 1400 to values at sunset - used to calculate temperature
     * and humidity for each hour between 1400 and sunset.
     *
     * @see "Rothermel, et al, Modeling moisture content of fine dead wildland fuels: input to the
     * BEHAVE fire prediction system. Research Paper INT-359. 1986. Equations #38 & #39 located on
     * page 22."
     *
     * @param timeProjection
     * @param timeSunset
     * @param valueAt1400
     * @param valueAtSunset
     * @return
     */
    static public double calcDiurnalValueForAfternoon(
            double timeProjection, double timeSunset,
            double valueAt1400, double valueAtSunset) {
        assert (timeProjection >= 13.5);
        return valueAt1400 + (valueAt1400 - valueAtSunset)
                * (cos(toRadians(
                                90 * (timeProjection - 14) / (timeSunset - 14))) - 1);

    }

    /**
     * Sinusoidal curve linking values at sunset to values at sunrise - used to calculate
     * temperature and humidity for each hour between sunset and sunrise.
     *
     * @see "Rothermel, et al, Modeling moisture content of fine dead wildland fuels: input to the
     * BEHAVE fire prediction system. Research Paper INT-359. 1986. Equations #40 & #41 located on
     * page 23."
     *
     * @param timeProjection
     * @param timeOfSunset
     * @param timeOfSunrise
     * @param valueAtSunset
     * @param valueAtSunrise
     * @return
     */
    static public double calcDiurnalValueForNighttime(
            double timeProjection,
            double timeOfSunset, double timeOfSunrise,
            double valueAtSunset, double valueAtSunrise) {
        timeOfSunrise += 24;
        if (timeProjection < timeOfSunset) {
            timeProjection += 24;
        }
        return valueAtSunset + (valueAtSunrise - valueAtSunset)
                * sin(toRadians(
                                90 * (timeProjection - timeOfSunset) / (timeOfSunrise - timeOfSunset)));
    }

    /**
     * Sinusoidal curve linking values at sunrise to values at noon - used to calculate temperature
     * and humidity for each hour between sunrise and 1200 hrs.
     *
     * @see "Rothermel, et al, Modeling moisture content of fine dead wildland fuels: input to the
     * BEHAVE fire prediction system. Research Paper INT-359. 1986. Equations #42 & 43 located on
     * page 24."
     *
     * @param timeProjection
     * @param timeOfSunrise
     * @param valueAtSunrise
     * @param valueAtNoon
     * @return
     */
    static public double calcDiurnalValueForMorning(
            double timeProjection,
            double timeOfSunrise,
            double valueAtSunrise, double valueAtNoon) {
        assert (timeProjection < 12.0);
        return valueAtNoon + (valueAtSunrise - valueAtNoon)
                * cos(toRadians(
                                90 * (timeProjection - timeOfSunrise) / (12.0 - timeOfSunrise)));
    }

    /**
     * Computes the wind speed at the fuel level from 20 foot wind speeds.
     *
     * @see "Rothermel, et al, Modeling moisture content of fine dead wildland fuels: input to the
     * BEHAVE fire prediction system. Research Paper INT-359. 1986. Equations #36"
     *
     * @param U_20 wind speed 20 feet above the vegitation [mph]
     * @param h vegitation height [feet]
     * @return wind speed at vegetation height [mph]
     */
    static public double calcWindSpeedAtFuelLevel(double U_20, double h) {
        // Equation #36
        // The ratio of windspeed at vegetation height to that at
        // 20 feet above the vegitation is given by:
        //  U_h' / U_20+h' = 1 / ln((20 + 0.36 * h') / 0.13 * h')
        //      where:
        //          h' = vegitation height [feet]
        double U_h = (1.0 / log((20 + 0.36 * h) / (0.13 * h))) * U_20;
        return U_h;
    }
}
