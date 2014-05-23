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
package com.emxsys.weather.api;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GisType;
import com.emxsys.solar.api.SolarType;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.solar.spi.DefaultSunlightProvider;
import com.emxsys.time.spi.DefaultTimeProvider;
import com.emxsys.visad.Tuples;
import com.emxsys.weather.api.AbstractWeatherProvider;
import com.emxsys.weather.api.ConditionsObserver;
import com.emxsys.weather.api.WeatherProvider;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import javax.swing.ImageIcon;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import visad.Field;
import visad.FlatField;
import visad.FunctionType;
import visad.Irregular2DSet;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
@ServiceProvider(service = WeatherProvider.class)
public class DiurnalWeatherProvider extends AbstractWeatherProvider {

    private Real tempAtSunrise;
    private Real tempAtNoon;
    private Real tempAt1400;
    private Real tempAtSunset;
    private Real rhAtSunrise;
    private Real rhAtNoon;
    private Real rhAt1400;
    private Real rhAtSunset;
    private RealTuple sunlight;

    /**
     * Constructor.
     */
    public DiurnalWeatherProvider() {
        // Initialize the lookup with this provider's capabilities
        InstanceContent content = getContent();
        content.add((ConditionsObserver) this::getCurrentWeather);  // functional interface 
    }

    public void initializeAirTempuratures(Real tempAtSunrise, Real tempAtNoon, Real tempAt1400, Real tempAtSunset) {
        this.tempAtSunrise = tempAtSunrise;
        this.tempAtNoon = tempAtNoon;
        this.tempAt1400 = tempAt1400;
        this.tempAtSunset = tempAtSunset;
    }

    public void initializeRelativeHumidities(Real rhAtSunrise, Real rhAtNoon, Real rhAt1400, Real rhAtSunset) {
        this.rhAtSunrise = rhAtSunrise;
        this.rhAtNoon = rhAtNoon;
        this.rhAt1400 = rhAt1400;
        this.rhAtSunset = rhAtSunset;
    }

    /**
     * Gets the current temperature and humidity values from the diurnal curves and wind patterns.
     *
     * @param coord Ignored, but returned in the Field's domain.
     * @param radius Ignored.
     * @param age Ignored.
     * @return A FlatField containing current weather values from the diurnal datasets.
     */
    public Field getCurrentWeather(Coord2D coord, Real radius, Duration age) {

        // Get the application time
        ZonedDateTime time = DefaultTimeProvider.getInstance().getTime();
        this.sunlight = DefaultSunlightProvider.getInstance().getSunlight(time, GeoCoord3D.fromCoord(coord));

        try {
            // Create the domain sample from the coordinate
            float[][] latLonSamples = new float[2][1];
            latLonSamples[0][0] = (float) coord.getLatitudeDegrees();
            latLonSamples[1][0] = (float) coord.getLatitudeDegrees();

            // Create the wx range samples, and init with "missing" values
            double[][] wxSamples = new double[WX_RANGE.getDimension()][1];
            wxSamples[AIR_TEMP_IDX][0] = getAirTemperature(time.toLocalTime()).getValue();
            wxSamples[HUMIDITY_IDX][0] = getRelativeHumidity(time.toLocalTime()).getValue();
            wxSamples[WIND_SPD_IDX][0] = Double.NaN;
            wxSamples[WIND_DIR_IDX][0] = Double.NaN;

            // Create the domain Set, with 2 columns and 1 rows, using an
            // Gridded2DDoubleSet(MathType type, double[][] samples, lengthX)
            Irregular2DSet domainSet = new Irregular2DSet(GisType.LATLON, latLonSamples);

            // Create a MathType for the function ( (lat, lon ) -> ( air_rh, RH, wind_spd, ... ) )
            FunctionType stationWxFunc = new FunctionType(GisType.LATLON, WX_RANGE);

            // Create a FlatField
            // Use FlatField(FunctionType type, Set domain_set)
            FlatField values_ff = new FlatField(stationWxFunc, domainSet);

            // ...and put the weather values above into it
            values_ff.setSamples(wxSamples);
            return values_ff;

        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    protected Real getAirTemperature(LocalTime localTime) {
        Real sunrise = Tuples.getComponent(SolarType.SUNRISE_HOUR, sunlight);
        Real sunset = Tuples.getComponent(SolarType.SUNSET_HOUR, sunlight);
        double t_sr = sunrise.getValue();
        double t_ss = sunset.getValue();
        double t = localTime.get(ChronoField.SECOND_OF_DAY) / 3600.;

        double val;
        if (t < t_sr || t > t_ss) {
            val = calcValueNighttime(t, t_ss, t_sr, tempAtSunset.getValue(), tempAtSunrise.getValue());
        } else if (t < 12.) {
            val = calcAirTempMorning(t, t_sr, tempAtSunrise.getValue(), tempAtNoon.getValue());
        } else if (t > 14.) {
            val = calcValueLateAfternoon(t, t_ss, tempAt1400.getValue(), tempAtSunset.getValue());
        } else {
            val = calcValueEarlyAfternoon(t, tempAtNoon.getValue(), tempAt1400.getValue());
        }
        return new Real(WeatherType.AIR_TEMP_F, val);
    }

    protected Real getRelativeHumidity(LocalTime localTime) {
        Real sunrise = Tuples.getComponent(SolarType.SUNRISE_HOUR, sunlight);
        Real sunset = Tuples.getComponent(SolarType.SUNSET_HOUR, sunlight);
        double t_sr = sunrise.getValue();
        double t_ss = sunset.getValue();
        double t = localTime.get(ChronoField.SECOND_OF_DAY) / 3600.;

        double val;
        if (t < t_sr || t > t_ss) {
            val = calcValueNighttime(t, t_ss, t_sr, rhAtSunset.getValue(), rhAtSunrise.getValue());
        } else if (t < 12.) {
            val = calcAirTempMorning(t, t_sr, rhAtSunrise.getValue(), rhAtNoon.getValue());
        } else if (t > 14.) {
            val = calcValueLateAfternoon(t, t_ss, rhAt1400.getValue(), rhAtSunset.getValue());
        } else {
            val = calcValueEarlyAfternoon(t, rhAtNoon.getValue(), rhAt1400.getValue());
        }
        return new Real(WeatherType.AIR_TEMP_F, val);
    }

    /**
     * Sinusoidal curve linking 1400 value to value at sunset - used to calculate temperature and
     * humidity between between 1400 and sunset.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #38 located on page 22.
     *
     * @param time
     * @param timeAtSunset
     * @param valueAt1400
     * @param rhSunset
     * @return
     */
    static private double calcValueLateAfternoon(double time, double timeAtSunset,
                                                 double valueAt1400, double valueAtSunset) {
        return valueAt1400 + (valueAt1400 - valueAtSunset) * (cos(toRadians(90 * (time - 14) / (timeAtSunset - 14))) - 1);
    }

    /**
     * Linear interpolation between value at noon and value at 1400.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #38 located on page 22.
     *
     * @param time
     * @param valueAtNoon
     * @param valueAt1400
     * @return
     */
    static private double calcValueEarlyAfternoon(double time, double valueAtNoon, double valueAt1400) {
        double range = valueAtNoon - valueAt1400;
        double x = (time - 12.) / 2.;
        return valueAtNoon + range * x;
    }

    /**
     * Sinusoidal curve linking sunset value to value at sunrise - used to calculate temperature and
     * humidity between sunset and sunrise.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #40 located on page 23.
     *
     * @param time
     * @param timeAtSunset
     * @param timeAtSunrise
     * @param valueAtSunset
     * @param valueAtSunrise
     * @return
     */
    static private double calcValueNighttime(double time, double timeAtSunset,
                                             double timeAtSunrise, double valueAtSunset, double valueAtSunrise) {
        timeAtSunrise += 24;
        if (time < timeAtSunset) {
            time += 24;
        }
        return valueAtSunset + (valueAtSunrise - valueAtSunset) * sin(toRadians(90 * (time - timeAtSunset) / (timeAtSunrise - timeAtSunset)));
    }

    /**
     * Sinusoidal curve linking sunrise value to value at noon - used to calculate temperature and
     * humidity for each hour between sunrise and 1200 hrs.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #42 located on page 24.
     *
     * @param time
     * @param timeAtSunrise
     * @param valueAtSunrise
     * @param valueAtNoon
     * @return
     */
    static private double calcAirTempMorning(double time, double timeAtSunrise,
                                             double valueAtSunrise, double valueAtNoon) {
        assert (time <= 12.0);
        return valueAtNoon + (valueAtSunrise - valueAtNoon) * cos(toRadians(90 * (time - timeAtSunrise) / (12.0 - timeAtSunrise)));
    }

    /**
     * Sinusoidal curve linking 1400 humidity to humidity at sunset - used to calculate humidity for
     * each hour between 1400 and sunset.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equation #39 located on page 22.
     *
     *
     * @param timeProjection
     * @param timeSunset
     * @param rh1400
     * @param rhSunset
     * @return
     */
    static private double calcHumidityLateAfternoon(double timeProjection, double timeSunset,
                                                    double rh1400, double rhSunset) {
        assert (timeProjection >= 14);
        assert (timeProjection <= timeSunset);

        return rh1400 + (rh1400 - rhSunset) * (cos(toRadians(90 * (timeProjection - 14) / (timeSunset - 14))) - 1);
    }

    /**
     * Sinusoidal curve linking sunset humidity to humidity at sunrise - used to calculate relative
     * humidity for each hour between sunset and sunrise
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #41 located on page 23.
     *
     * @param timeProjection
     * @param timeSunset
     * @param timeSunrise
     * @param rhSunset
     * @param rtSunrise
     * @return
     */
    static private double calcHumidityNighttime(double timeProjection, double timeSunset,
                                                double timeSunrise, double rhSunset, double rhSunrise) {
        timeSunrise += 24;
        if (timeProjection < timeSunset) {
            timeProjection += 24;
        }
        return rhSunset + (rhSunrise - rhSunset) * sin(toRadians(90 * (timeProjection - timeSunset) / (timeSunrise - timeSunset)));
    }

    /**
     * Sinusoidal curve linking sunrise humidty to humidty at noon - used to calculate humidty for
     * each hour between sunrise and 1200 hrs.
     *
     * Rothermel, et al, "Modeling moisture content of fine dead wildland fuels: input to the BEHAVE
     * fire prediction system." Research Paper INT-359. 1986. Equations #43 located on page 24.
     *
     * @param timeProjection
     * @param timeSunrise
     * @param rhSunrise
     * @param rh1200
     * @return
     */
    static private double calcHumidityMorning(double timeProjection, double timeSunrise,
                                              double rhSunrise, double rh1200) {
        assert (timeProjection <= 12.0);
        return rh1200 + (rhSunrise - rh1200) * cos(toRadians(90 * (timeProjection - timeSunrise) / (12.0 - timeSunrise)));
    }

    @Override
    public ImageIcon getImageIcon() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | rhlates.
    }

}