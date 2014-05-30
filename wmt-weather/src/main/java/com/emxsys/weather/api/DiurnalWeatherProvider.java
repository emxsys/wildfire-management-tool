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
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GisType;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.solar.spi.DefaultSunlightProvider;
import com.emxsys.time.spi.DefaultTimeProvider;
import com.emxsys.util.ImageUtil;
import com.emxsys.visad.Reals;
import com.emxsys.visad.TemporalDomain;
import static com.emxsys.weather.api.WeatherType.AIR_TEMP_C;
import static com.emxsys.weather.api.WeatherType.CLOUD_COVER;
import static com.emxsys.weather.api.WeatherType.FIRE_WEATHER;
import static com.emxsys.weather.api.WeatherType.REL_HUMIDITY;
import static com.emxsys.weather.api.WeatherType.WIND_DIR;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_SI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import org.openide.util.Exceptions;
import org.openide.util.lookup.InstanceContent;
import visad.Field;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Irregular2DSet;
import visad.Real;
import visad.VisADException;

/**
 * The DiurnalWeatherProvider creates hourly weather for 24 hour cycle.
 *
 * @author Bruce Schubert
 */
public class DiurnalWeatherProvider extends AbstractWeatherProvider {

    private Real tempAtSunrise = new Real(AIR_TEMP_C);
    private Real tempAtNoon = new Real(AIR_TEMP_C);
    private Real tempAt1400 = new Real(AIR_TEMP_C);
    private Real tempAtSunset = new Real(AIR_TEMP_C);
    private Real rhAtSunrise = new Real(REL_HUMIDITY);
    private Real rhAtNoon = new Real(REL_HUMIDITY);
    private Real rhAt1400 = new Real(REL_HUMIDITY);
    private Real rhAtSunset = new Real(REL_HUMIDITY);
    private Sunlight sunlight;
    private TreeMap<LocalTime, Real> windSpds = new TreeMap<>();
    private TreeMap<LocalTime, Real> windDirs = new TreeMap<>();
    private TreeMap<LocalTime, Real> clouds = new TreeMap<>();

    /**
     * Constructor.
     */
    public DiurnalWeatherProvider() {
    }

    /**
     * Constructor.
     * @param date The date used to obtain sunrise and sunset.
     * @param coord The coordinate used to obtain sunrise and sunset.
     */
    public DiurnalWeatherProvider(ZonedDateTime date, Coord3D coord) {
        // Initialize the lookup with this provider's capabilities
        InstanceContent content = getContent();
        content.add((ConditionsObserver) this::getCurrentWeather);  // functional interface 
        sunlight = DefaultSunlightProvider.getInstance().getSunlight(date, coord);
    }

    @Override
    public String getName() {
        return "Diurnal Weather";
    }

    public void initializeAirTemperatures(Real tempAtSunrise, Real tempAtNoon, Real tempAt1400, Real tempAtSunset) {
        this.tempAtSunrise = Reals.convertTo(AIR_TEMP_C, tempAtSunrise);
        this.tempAtNoon = Reals.convertTo(AIR_TEMP_C, tempAtNoon);
        this.tempAt1400 = Reals.convertTo(AIR_TEMP_C, tempAt1400);
        this.tempAtSunset = Reals.convertTo(AIR_TEMP_C, tempAtSunset);
    }

    public void initializeRelativeHumidities(Real rhAtSunrise, Real rhAtNoon, Real rhAt1400, Real rhAtSunset) {
        this.rhAtSunrise = rhAtSunrise;
        this.rhAtNoon = rhAtNoon;
        this.rhAt1400 = rhAt1400;
        this.rhAtSunset = rhAtSunset;
    }

    public void initializeWindSpeeds(TreeMap<LocalTime, Real> windSpeeds) {
        Collection<Real> values = windSpeeds.values();
        for (Real real : values) {
            if (real.getValue() < 0) {
                throw new IllegalArgumentException("neg values not allowed");
            }
        }
        this.windSpds = windSpeeds;
    }

    public void initializeWindDirections(TreeMap<LocalTime, Real> windDirections) {
        this.windDirs = windDirections;
    }

    public void initializeCloudCovers(TreeMap<LocalTime, Real> cloudCovers) {
        Collection<Real> values = cloudCovers.values();
        for (Real real : values) {
            if (real.getValue() < 0) {
                throw new IllegalArgumentException("neg values not allowed");
            }
        }
        this.clouds = cloudCovers;
    }

    public void initializeSunlight(ZonedDateTime date, Coord3D coord) {
        sunlight = DefaultSunlightProvider.getInstance().getSunlight(date, coord);
    }

    public void setSunlight(Sunlight sunlight) {
        this.sunlight = sunlight;
    }

    /**
     * Gets the diurnal weather cycles for the given temporal domain.
     *
     * @param domain The time domain.
     * @return A FlatField (time -> FIRE_WEATHER).
     */
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
    public Field getDailyWeather(TemporalDomain domain) {
        if (sunlight == null) {
            throw new IllegalStateException("Sunlight has not been initialized.");
        }
        try {
            // Create a FieldImpl and the samples for the hourly weather range
            FieldImpl wxField = domain.newTemporalField(FIRE_WEATHER); // FunctionType: time -> fire weather)
            double[][] wxSamples = new double[FIRE_WEATHER.getDimension()][wxField.getLength()];

            // Create the wx range samples...
            for (int i = 0; i < wxField.getLength(); i++) {
                ZonedDateTime time = domain.getZonedDateTimeAt(i);
                wxSamples[AIR_TEMP_IDX][i] = getAirTemperature(time.toLocalTime()).getValue();
                wxSamples[HUMIDITY_IDX][i] = getRelativeHumidity(time.toLocalTime()).getValue();
                wxSamples[WIND_SPD_IDX][i] = getWindSpeed(time.toLocalTime()).getValue();
                wxSamples[WIND_DIR_IDX][i] = getWindDirection(time.toLocalTime()).getValue();
                wxSamples[CLOUD_COVER_IDX][i] = getCloudCover(time.toLocalTime()).getValue();
            }
            // ...and put the weather values above into it
            wxField.setSamples(wxSamples);
            return wxField;

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Gets the current temperature and humidity values from the diurnal curves and wind patterns.
     *
     * @param coord Parameter is ignored, but it is returned in the Field's domain.
     * @param radius_ignored Ignored parameter.
     * @param age_ignored Ignored parameter.
     * @return A FlatField containing current weather values from the diurnal datasets.
     */
    public Field getCurrentWeather(Coord2D coord, Real radius_ignored, Duration age_ignored) {

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
            wxSamples[WIND_SPD_IDX][0] = 0;
            wxSamples[WIND_DIR_IDX][0] = 0;
            wxSamples[CLOUD_COVER_IDX][0] = 0;

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
        Real sunrise = sunlight.getSunriseHour();
        Real sunset = sunlight.getSunsetHour();
        double t_sr = sunrise.getValue();
        double t_ss = sunset.getValue();
        double t = localTime.get(ChronoField.SECOND_OF_DAY) / 3600.;

        double val;
        if (t < t_sr || t > t_ss) {
            val = calcValueNighttime(t, t_ss, t_sr, tempAtSunset.getValue(), tempAtSunrise.getValue());
        } else if (t < 12.) {
            val = calcValueMorning(t, t_sr, tempAtSunrise.getValue(), tempAtNoon.getValue());
        } else if (t > 14.) {
            val = calcValueLateAfternoon(t, t_ss, tempAt1400.getValue(), tempAtSunset.getValue());
        } else {
            val = calcValueEarlyAfternoon(t, tempAtNoon.getValue(), tempAt1400.getValue());
        }
        return new Real(WeatherType.AIR_TEMP_C, val);
    }

    protected Real getRelativeHumidity(LocalTime localTime) {
        Real sunrise = sunlight.getSunriseHour();
        Real sunset = sunlight.getSunsetHour();
        double t_sr = sunrise.getValue();
        double t_ss = sunset.getValue();
        double t = localTime.get(ChronoField.SECOND_OF_DAY) / 3600.;

        double val;
        if (t < t_sr || t > t_ss) {
            val = calcValueNighttime(t, t_ss, t_sr, rhAtSunset.getValue(), rhAtSunrise.getValue());
        } else if (t < 12.) {
            val = calcValueMorning(t, t_sr, rhAtSunrise.getValue(), rhAtNoon.getValue());
        } else if (t > 14.) {
            val = calcValueLateAfternoon(t, t_ss, rhAt1400.getValue(), rhAtSunset.getValue());
        } else {
            val = calcValueEarlyAfternoon(t, rhAtNoon.getValue(), rhAt1400.getValue());
        }
        return new Real(WeatherType.REL_HUMIDITY, val);
    }

    protected Real getWindSpeed(LocalTime localTime) {
        Entry<LocalTime, Real> entry = windSpds.floorEntry(localTime);
        return (entry == null) ? new Real(WIND_SPEED_SI, 0) : Reals.convertTo(WIND_SPEED_SI, entry.getValue());
    }

    protected Real getWindDirection(LocalTime localTime) {
        Entry<LocalTime, Real> entry = windDirs.floorEntry(localTime);
        return (entry == null) ? new Real(WIND_DIR, 0) : entry.getValue();
    }

    protected Real getCloudCover(LocalTime localTime) {
        Entry<LocalTime, Real> entry = clouds.floorEntry(localTime);
        return (entry == null) ? new Real(CLOUD_COVER, 0) : entry.getValue();
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
    static private double calcValueMorning(double time, double timeAtSunrise,
                                           double valueAtSunrise, double valueAtNoon) {
        assert (time <= 12.0);
        return valueAtNoon + (valueAtSunrise - valueAtNoon) * cos(toRadians(90 * (time - timeAtSunrise) / (12.0 - timeAtSunrise)));
    }

    @Override
    public ImageIcon getImageIcon() {
        return ImageUtil.createImageIconFromResource("images/sun_clouds.png", getClass());
    }
}
