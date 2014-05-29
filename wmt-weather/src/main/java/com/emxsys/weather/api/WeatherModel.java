/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com> 
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

import com.emxsys.visad.SpatioTemporalDomain;
import com.emxsys.visad.Times;
import static com.emxsys.weather.api.WeatherType.AIR_TEMP_F;
import static com.emxsys.weather.api.WeatherType.CLOUD_COVER;
import static com.emxsys.weather.api.WeatherType.FIRE_WEATHER;
import static com.emxsys.weather.api.WeatherType.REL_HUMIDITY;
import static com.emxsys.weather.api.WeatherType.WIND_DIR;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_MPH;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Unit;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class WeatherModel {

    /**
     * The temporal-spatial domain for this model.
     */
    private final SpatioTemporalDomain domain;
    /**
     * The wx range tuple type, <br/>
     * {TIME, AIR_TEMP_C, REL_HUMIDITY, WIND_SPEED_SI, WIND_DIR, CLOUD_COVER}
     */
    private static final RealTupleType wxRangeType = FIRE_WEATHER;
    /**
     * The hourly time type
     */
    private static final RealType timeType = RealType.Time;
    /**
     * The fire weather data
     */
    private FieldImpl hourlyWx;

    private static final Logger LOG = Logger.getLogger(WeatherModel.class.getName());

    private List<Real> airTemps = new ArrayList<>();
    private List<Real> humidities = new ArrayList<>();
    private List<Real> windSpds = new ArrayList<>();
    private List<Real> windDirs = new ArrayList<>();
    private List<Real> cloudCover = new ArrayList<>();

    /**
     * Constructs the WeatherModel with a deferred initialization of the Weather FlatField member.
     *
     * @param domain the weather's domain (temporal / geographic bounds)
     */
    public WeatherModel(SpatioTemporalDomain domain,
                        FlatField temperatures, FlatField humidities, FlatField winds) {
        this.domain = domain;
        this.hourlyWx = createHourlyWeather(temperatures, humidities, winds);
    }

    private List<Real> newListFromArray(RealType type, double[] array) {
        List<Real> list = new ArrayList<>(array.length);
        for (double d : array) {
            list.add(new Real(type, d));
        }
        return list;
    }

    /**
     * Constructs the WeatherModel with a deferred initialization of the Weather FlatField member.
     *
     * @param domain the weather's domain (temporal / geographic bounds)
     */
    public WeatherModel(SpatioTemporalDomain domain,
                        List<Real> airTemps,
                        List<Real> humidities,
                        List<Real> windSpds,
                        List<Real> windDirs) {
        // Defer initialization of Weather flatfield member
        this(domain, airTemps, humidities, windSpds, windDirs, false);
    }

    /**
     *
     * @param domain the geographic domain (bounds)
     * @param immediate if true, performs an immediate initialization of the Weather data; if false,
     * the Weather data is initialized on the first call to getWeather.
     */
    public WeatherModel(SpatioTemporalDomain domain,
                        List<Real> airTemps,
                        List<Real> humidities,
                        List<Real> windSpds,
                        List<Real> windDirs,
                        boolean immediate) {
        this.domain = domain;
        this.airTemps = airTemps;
        this.humidities = humidities;
        this.windSpds = windSpds;
        this.windDirs = windDirs;

        if (immediate) {
            getWeatherData();
        }

    }

    /**
     * Math type: ( time -> ( temperature, humidity, ... ) )
     *
     * @return hourly weather
     */
    public final FieldImpl getWeatherData() {
        lazyCreateHourlyWeather();
        return this.hourlyWx;
    }

    /**
     * Math type: ( time -> ( temperature, humidity, ... ) )
     *
     * @return hourly weather
     */
    public final WeatherTuple getWeatherAt(int index) {
        try {
            lazyCreateHourlyWeather();
            RealTuple sample = (RealTuple) this.hourlyWx.getSample(index);
            return new WeatherTuple(sample.getRealComponents());
        } catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    public WeatherTuple getWeather(ZonedDateTime temporal) {
        if (temporal == null) {
            throw new IllegalArgumentException("datetime");
        }
        lazyCreateHourlyWeather();
        try {
            DateTime dateTime = Times.fromZonedDateTime(temporal);
            RealTuple tuple = (RealTuple) this.hourlyWx.evaluate(dateTime, FlatField.NEAREST_NEIGHBOR, FlatField.NO_ERRORS);
            return tuple.isMissing() ? WeatherTuple.INVALID_TUPLE : new WeatherTuple(tuple.getRealComponents());
        } catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    private void generateTestData() {
        try {
            LOG.warning(">>>> Using Wx Text Data! <<<<");

            // Get the temporal domain samples to supply time values
            final float[][] timeValues = this.domain.getTemporalDomainSet().getSamples(false);
            final int numDays = timeValues[0].length / 24;

            // Use the first date values in our domain
            Date date = Times.toDate(new DateTime(timeValues[0][0]));
            RealTuple sunriseSunset = WeatherUtil.newSunriseSunsetTuple(date,
                    6.0, // sunrise hrs local time
                    18.0);  // sunset hrs local time
            RealTuple diurnalTemps = WeatherUtil.newTemperaturesTuple(AIR_TEMP_F,
                    100.0, //cycle.tempAt1400,
                    80.8, //cycle.tempAtSunset,
                    70.8, //cycle.tempAtSunrise,
                    95.0);  //cycle.tempAtNoon);
            RealTuple diurnalRH = WeatherUtil.newTemperaturesTuple(REL_HUMIDITY,
                    6.0, //cycle.humidityAt1400,
                    10.0, //cycle.humidityAtSunset,
                    33.0, //cycle.humidityAtSunrise,
                    10.0);  //cycle.humidityAtNoon);
            Real windSpd = new Real(WIND_SPEED_MPH, 5);
            Real windDir = new Real(WIND_DIR, 90);
            Real cloudCvr = new Real(CLOUD_COVER, 0);

            // Add the diurnal values for each "day" in the time domain
            for (int i = 0; i < numDays; i++) {
                // WeatherType.FIRE_WEATHER ff beginning at 1400 hrs
                FlatField ff = WeatherUtil.makeGeneralFireWeather(sunriseSunset, diurnalTemps, diurnalRH, windSpd, windDir, cloudCvr);
                double[][] values = ff.getValues();

                this.airTemps.addAll(newListFromArray(AIR_TEMP_F, values[1]));
                this.humidities.addAll(newListFromArray(REL_HUMIDITY, values[2]));
                this.windSpds.addAll(newListFromArray(WIND_SPEED_MPH, values[3]));
                this.windDirs.addAll(newListFromArray(WIND_DIR, values[4]));
                this.cloudCover.addAll(newListFromArray(CLOUD_COVER, values[5]));
            }
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void lazyCreateHourlyWeather() {
        if (this.hourlyWx == null) {
            this.hourlyWx = createHourlyWeather();
        }
    }

    private FieldImpl createHourlyWeather(FlatField temperatures, FlatField humidities,
                                          FlatField winds) {
        try {
            // Create a (non-flat) FieldImpl metadata and samples for hourly weather
            FieldImpl wxField = this.domain.newTemporalField(wxRangeType);
            double[][] wxSamples = new double[wxRangeType.getDimension()][wxField.getLength()];

            // Populate the field samples
            for (int i = 0; i < wxField.getLength(); i++) {
                RealTuple W = (RealTuple) winds.getSample(i);
                Real W_s = (Real) W.getComponent(0);
                Real W_d = (Real) W.getComponent(1);
                Real T_a = (Real) temperatures.getSample(i);
                Real H_a = (Real) humidities.getSample(i);

                Unit[] rangeUnits = wxRangeType.getDefaultUnits();
                wxSamples[0][i] = this.domain.getDateTimeAt(i).getValue();
                wxSamples[1][i] = T_a.getValue(rangeUnits[1]);
                wxSamples[2][i] = H_a.getValue(rangeUnits[2]);
                wxSamples[3][i] = W_s.getValue(rangeUnits[3]);
                wxSamples[4][i] = W_d.getValue(rangeUnits[4]);
                wxSamples[5][i] = 0.0; //this.cloudCover.get(i).getValue();    // percent
            }
            wxField.setSamples(wxSamples);
            return wxField;
        } catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }

    }

    private FieldImpl createHourlyWeather() {
        try {
            // Create a (non-flat) FieldImpl metadata and samples for hourly weather
            FieldImpl wxField = this.domain.newTemporalField(wxRangeType);
            double[][] wxSamples = new double[wxRangeType.getDimension()][wxField.getLength()];

            // Populate the field samples
            for (int i = 0; i < wxField.getLength(); i++) {
                // Simply convert (if reqd) our input values  and copy them to the field array
                wxSamples[0][i] = this.domain.getDateTimeAt(i).getValue();
                wxSamples[1][i] = this.airTemps.get(i).getValue(wxRangeType.getDefaultUnits()[1]);
                wxSamples[2][i] = this.humidities.get(i).getValue();    // percent
                wxSamples[3][i] = this.windSpds.get(i).getValue(wxRangeType.getDefaultUnits()[3]);
                wxSamples[4][i] = this.windDirs.get(i).getValue(wxRangeType.getDefaultUnits()[4]);
                wxSamples[5][i] = this.cloudCover.get(i).getValue();    // percent
            }
            wxField.setSamples(wxSamples);

            return wxField;
        } catch (RemoteException | VisADException ex) {
            LOG.severe(ex.toString());
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }

    }
//    private FieldImpl createHourlyWeather()
//    {
//        try
//        {
//            // Create the FlatField metadata for the weather at geographnic locations...
//            FlatField geographicWeather = this.domain.newSpatialField(wxRangeType);
//            
//            // ... and create a (non-flat) FieldImpl metadata for hourly weather
//            FieldImpl hourlyWeather = this.domain.newTemporalField(geographicWeather.getType());
//
//            // Get the spatial domain samples to supply lat/lon values
//            final float[][] spatialValues = this.domain.getSpatialDomainSet().getSamples(false); // true = copy            
//            final int numSpatialValues = spatialValues[0].length;
//
//            // Get the temporal domain samples to supply time values
//            final double[][] timeValues = this.domain.getTemporalDomainSet().getDoubles(false);
//            final int numTimeValues = timeValues[0].length;
//
//            // Create the weather ouput range coincident with the spatial domain
//            double[][] wxSamples = new double[wxRangeType.getDimension()][numSpatialValues];
//
//            // Loop through the time domain
//            for (int i = 0; i < numTimeValues; i++)
//            {
//                // Get a time sample from the domain
//                Real time = new Real(RealType.Time, timeValues[0][i]);
//
//                // Get the supplied input values for the current hour
//                Real airTemp = this.airTemps.get(i);
//                Real humidity = this.humidities.get(i);
//                Real windSpd = this.windSpds.get(i);
//                Real windDir = this.windDirs.get(i);
//                Real cloud = this.cloudCover.get(i);
//
//                // Now loop through the lat/lon domain
//                for (int j = 0; j < numSpatialValues; j++)
//                {
//                    // Get a lat/lon from the domain                    
//                    GeoPointTuple latLon = GeoPointTuple.fromDegrees(
//                            spatialValues[0][j],
//                            spatialValues[1][j]);
//
//                    // TODO: Apply location specific wx adjustments here....
//
//                    // TODO: read winds from WindNinja files
//
//                    // TODO: read wx data from noaa imagery
//
//                    // And finally, update the range samples from the weather history/forecast
//                    wxSamples[0][j] = time.getValue();
//                    wxSamples[1][j] = airTemp.getValue(CommonFireUnit.degC);
//                    wxSamples[2][j] = humidity.getValue();
//                    wxSamples[3][j] = windSpd.getValue(CommonUnit.meterPerSecond);
//                    wxSamples[4][j] = windDir.getValue(CommonUnit.degree);
//                    wxSamples[5][j] = cloud.getValue();  // cloud cover percent
//
//                }
//                // Add our samples to the Weather FlatField
//                geographicWeather.setSamples(wxSamples);
//
//                hourlyWeather.setSample(i, geographicWeather);
//            }
//            return hourlyWeather;
//        }
//        catch (RemoteException ex)
//        {
//            LOG.severe(ex.toString());
//            Exceptions.printStackTrace(ex);
//            throw new RuntimeException(ex);
//        }
//        catch (VisADException ex)
//        {
//            LOG.severe(ex.toString());
//            Exceptions.printStackTrace(ex);
//            throw new RuntimeException(ex);
//        }
//
//    }
}
