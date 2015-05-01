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
import com.emxsys.gis.api.Coords;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.SpatialField;
import com.emxsys.visad.SpatioTemporalModel;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.visad.Times;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.Data;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DDoubleSet;
import visad.MathType;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.VisADException;
import visad.georef.LatLonTuple;
import visad.util.DataUtility;

/**
 * The WeatherModel class manages weather data within a spatio-temporal domain. The class provides
 * accessors to extract and retrieve the data. The model is initialized from the static factory
 * methods.
 *
 * The model is organized as either:
 * <pre>Time -> ((Lat, Lon) -> (Weather))</pre> or
 * <pre>(Lat, Lon) -> (Time -> (Weather))</pre>
 *
 *
 * @author Bruce Schubert
 */
public class WeatherModel extends SpatioTemporalModel {

    private static final Logger logger = Logger.getLogger(WeatherModel.class.getName());

    /**
     * Creates a WeatherModel at a single location at a single point in time.
     * @param time Point in time.
     * @param coord Location.
     * @param weather Weather values.
     * @return A new WeatherModel instance.
     */
    public static WeatherModel from(ZonedDateTime time, Coord2D coord, BasicWeather weather) {
        TemporalDomain timeDomain = new TemporalDomain(time, 1);
        SpatialField spatialField = SpatialField.from(coord, weather.getTuple());
        return WeatherModel.from(timeDomain, new SpatialField[]{spatialField});
    }

    /**
     * Creates a temporal-spatial organized WeatherModel.
     * @param domain The temporal domain.
     * @param ranges An array of (Lat, Lon) -> (Weather) range samples for the time domain.
     * @return A new WeatherModel organized as <pre>Time -> ((Lat, Lon) -> (Weather))</pre>
     */
    public static WeatherModel from(TemporalDomain domain, SpatialField[] ranges) {
        try {
            FieldImpl temporalSpatialWeather = domain.createTemporalField(ranges[0].getField().getType());
            final int numTimes = domain.getDomainSetLength();
            for (int t = 0; t < numTimes; t++) {
                temporalSpatialWeather.setSample(t, ranges[t].getField());
            }
            return new WeatherModel(temporalSpatialWeather);
        } catch (VisADException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a spatial-temporal organized WeatherModel.
     * @param domain The spatial domain.
     * @param ranges An array of Time -> (Weather) range samples for the spatial domain.
     * @return A new WeatherModel organized as <pre>(Lat, Lon) -> (Time -> (Weather))</pre>
     */
    public static WeatherModel from(SpatialDomain domain, FlatField[] ranges) {
        try {
            FieldImpl spatioTemporalWeather = domain.createSpatialField(ranges[0].getType());
            final int numLatLons = domain.getDomainSetLength();
            for (int xy = 0; xy < numLatLons; xy++) {
                spatioTemporalWeather.setSample(xy, ranges[xy]);
            }
            return new WeatherModel(spatioTemporalWeather);
        } catch (VisADException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public WeatherModel(FieldImpl field) {
        super(field);
    }

    /**
     * Gets the Weather at the specific time and place
     * @param time Time at which to sample the weather.
     * @param coord Place at which to sample the weather.
     * @return A {@code FIRE_WEATHER} tuple.
     */
    public BasicWeather getWeather(ZonedDateTime time, Coord2D coord) {
        RealTuple tuple = super.getTuple(time, coord);
        return tuple == null ? BasicWeather.INVALID_WEATHER : BasicWeather.fromRealTuple(tuple);
    }

    public Gridded1DDoubleSet getTemporalDomainSet() {
        try {
            MathType domainType = DataUtility.getDomainType(getField());
            boolean isSpatialThenTemporal = domainType.equals(RealTupleType.LatitudeLongitudeTuple);
            if (isSpatialThenTemporal) {
                FlatField temporalField = (FlatField) getField().getSample(0);
                return (Gridded1DDoubleSet) temporalField.getDomainSet();
            } else {
                return (Gridded1DDoubleSet) getField().getDomainSet();
            }
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     *
     * @return An ZonedDateTime array containing [first, last].
     */
    public DateTime[] getTemporalBounds() {
        Gridded1DDoubleSet set = getTemporalDomainSet();
        double doubleLowX = set.getDoubleLowX();
        double doubleHiX = set.getDoubleHiX();
        return new DateTime[]{
            Times.fromDouble(doubleLowX),
            Times.fromDouble(doubleHiX)
        };
    }

    public boolean contains(ZonedDateTime time) {
        double timeVal = Times.fromZonedDateTime(time).getValue();
        DateTime[] bounds = getTemporalBounds();
        return bounds[0].getValue() <= timeVal
                && bounds[1].getValue() >= timeVal;
    }

    /**
     * Gets a spatial weather {@code FlatField} for the given time.
     *
     * @param time The time used to evaluate the weather.
     * @return A (Lat, Lon) -> (Weather) {@code FlatField}.
     */
    public FlatField getSpatialWeatherAt(ZonedDateTime time) {
        FieldImpl field = getField();
        DateTime dateTime = Times.fromZonedDateTime(time);

        try {
            MathType domainType = DataUtility.getDomainType(field);
            boolean isSpatialThenTemporal = domainType.equals(RealTupleType.LatitudeLongitudeTuple);
            if (isSpatialThenTemporal) {
                // Handle model function type: (Lat,Lon) -> (Time -> (Weather))

                // Get the weather math type (we know the FieldImpl's range is a FlatField)
                RealTupleType wxTupleType = ((FunctionType) DataUtility.getRangeType(field)).getFlatRange();

                // Merge the temporal field for each coord into a new range sample-array (wxSamples)
                int numLatLons = field.getLength();
                double[][] wxSamples = new double[wxTupleType.getDimension()][numLatLons];
                for (int i = 0; i < numLatLons; i++) {
                    FlatField temporalField = (FlatField) field.getSample(i);
                    RealTuple wxTuple = (RealTuple) temporalField.evaluate(dateTime, Data.WEIGHTED_AVERAGE, Data.DEPENDENT);

                    double[] wxValues = wxTuple.getValues();
                    for (int j = 0; j < wxSamples.length; j++) {
                        wxSamples[j][i] = wxValues[j];
                    }
                }
                // Return a new (Lat, Lon) -> (Weather) FlatField
                FlatField spatialWeatherField = new FlatField(
                        new FunctionType(RealTupleType.LatitudeLongitudeTuple, wxTupleType),
                        field.getDomainSet());
                spatialWeatherField.setSamples(wxSamples);
                return spatialWeatherField;

            } else {
                // Handle model function type: Time -> ((Lat,Lon) -> (Weather))
                // Simply get the temporal sample
                return (FlatField) field.evaluate(dateTime, Data.WEIGHTED_AVERAGE, Data.DEPENDENT);
            }
        } catch (VisADException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the weather for the given coordinate in the form: (Time) -> (Weather).
     *
     * @param coord The coordinate used to evaluate the weather.
     * @return A (Time) -> (Weather) {@code FlatField}.
     */
    public FlatField getTemporalWeatherAt(Coord2D coord) {
        FieldImpl field = getField();
        LatLonTuple latLon = Coords.toLatLonTuple(coord);

        try {
            MathType domainType = DataUtility.getDomainType(field);
            boolean isSpatialThenTemporal = domainType.equals(RealTupleType.LatitudeLongitudeTuple);
            if (isSpatialThenTemporal) {
                
                // Handle model function type: (Lat,Lon) -> (Time -> (Weather)) ...
                
                // ... Simply get the (Time -> (Weather)) FlatField
                return (FlatField) field.evaluate(latLon, Data.WEIGHTED_AVERAGE, Data.DEPENDENT);
            } else {
                
                // Handle model function type: Time -> ((Lat,Lon) -> (Weather))...

                // First, merge the spatial field for each time into a new range sample-array (wxSamples)
                int numTimes = field.getLength();
                RealTupleType wxTupleType = getWeatherRangeTupleType();
                double[][] wxSamples = new double[wxTupleType.getDimension()][numTimes];
                for (int i = 0; i < numTimes; i++) {
                    FlatField spatialField = (FlatField) field.getSample(i);
                    RealTuple wxTuple = (RealTuple) spatialField.evaluate(latLon, Data.WEIGHTED_AVERAGE, Data.DEPENDENT);

                    double[] wxValues = wxTuple.getValues();
                    for (int j = 0; j < wxSamples.length; j++) {
                        wxSamples[j][i] = wxValues[j];
                    }
                }
                // ... Then return a new (Time) -> (Weather) FlatField
                FlatField temporalWeatherField = new FlatField(
                        new FunctionType(RealType.Time, wxTupleType),
                        field.getDomainSet());
                temporalWeatherField.setSamples(wxSamples);
                return temporalWeatherField;
            }

        } catch (VisADException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    RealTupleType getWeatherRangeTupleType() throws VisADException, RemoteException {
        // We know the FieldImpl's range is a FlatField
        FunctionType temporalFunc = (FunctionType) DataUtility.getRangeType(getField());
        return temporalFunc.getFlatRange();
    }

    /**
     *
     * @return A (Lat, Lon) -> (Weather) {@code FlatField}.
     */
    public FlatField getLatestWeather() {
        FieldImpl field = getField();
        try {
            MathType domainType = DataUtility.getDomainType(field);
            boolean isSpatialThenTemporal = domainType.equals(RealTupleType.LatitudeLongitudeTuple);
            if (isSpatialThenTemporal) {
                // (Lat, Lon) -> (Time -> (Weather))
                // Merge last temporal field for each coord into a new range sample array
                FunctionType temporalFunc = (FunctionType) DataUtility.getRangeType(field);
                RealTupleType wxTupleType = temporalFunc.getFlatRange();

                int numLatLons = field.getLength();
                double[][] wxSamples = new double[wxTupleType.getDimension()][numLatLons];
                for (int i = 0; i < numLatLons; i++) {
                    FlatField temporalField = (FlatField) field.getSample(i);
                    RealTuple wxTuple = (RealTuple) temporalField.getSample(temporalField.getLength() - 1);
                    double[] wxValues = wxTuple.getValues();
                    for (int j = 0; j < wxSamples.length; j++) {
                        wxSamples[j][i] = wxValues[j];
                    }
                }
                // Return a (Lat, Lon) -> (Weather) FlatField
                FlatField spatialWeatherField = new FlatField(new FunctionType(
                        RealTupleType.LatitudeLongitudeTuple, wxTupleType),
                        field.getDomainSet());
                spatialWeatherField.setSamples(wxSamples);
                return spatialWeatherField;

            } else {
                // FieldImpl: Time -> ((Lat, Lon) -> (Weather))
                // Simply get the last temporal sample.
                int len = field.getLength();
                return (FlatField) field.getSample(len - 1);
            }
        } catch (VisADException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        try {
            //return "WeatherField{" + "field=" + getField() + '}';
            FieldImpl field = getField();
            MathType domainType = DataUtility.getDomainType(field);
            boolean isSpatialThenTemporal = domainType.equals(RealTupleType.LatitudeLongitudeTuple);
            if (isSpatialThenTemporal) {

                // Get the weather math type (we know the FieldImpl's range is a FlatField)
                RealTupleType wxTupleType = ((FunctionType) DataUtility.getRangeType(field)).getFlatRange();
                sb.append(wxTupleType);
                sb.append("\n");

                // Merge the temporal field for each coord into a new range sample-array (wxSamples)
                int numLatLons = field.getLength();
                SampledSet latLonSet = (SampledSet) field.getDomainSet();
                for (int i = 0; i < numLatLons; i++) {
                    sb.append(DataUtility.getSample(latLonSet, i));
                    sb.append("\n");
                    
                    FlatField temporalField = (FlatField) field.getSample(i);
                    double[][] samples = temporalField.getDomainSet().getDoubles(false);
                    for (int j = 0; j < samples[0].length; j++) {
                        sb.append(Times.fromDouble(samples[0][j]));
                        sb.append(" -> ");
                        sb.append(temporalField.getSample(j));
                        sb.append("\n");
                    }
                }
            }
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
        return sb.toString();
    }
}
