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
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.SpatialField;
import com.emxsys.visad.SpatioTemporalModel;
import com.emxsys.visad.TemporalDomain;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.util.logging.Logger;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.RealTuple;
import visad.RealTupleType;
import visad.VisADException;
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
 *
 * @author Bruce Schubert
 */
public class WeatherModel extends SpatioTemporalModel {

    private static final Logger logger = Logger.getLogger(WeatherModel.class.getName());

    public static WeatherModel from(ZonedDateTime time, Coord2D coord, WeatherTuple tuple) {
        TemporalDomain timeDomain = new TemporalDomain(time, 1);
        SpatialField spatialField = SpatialField.from(coord, tuple);
        return WeatherModel.from(timeDomain, new SpatialField[]{spatialField});
    }

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

    public WeatherTuple getWeather(ZonedDateTime time, Coord2D coord) {
        RealTuple tuple = super.getTuple(time, coord);
        return tuple == null ? WeatherTuple.INVALID_TUPLE : WeatherTuple.fromRealTuple(tuple);
    }

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
        return "WeatherField{" + "field=" + getField() + '}';
    }

}
