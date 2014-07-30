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
package com.emxsys.visad;

import com.emxsys.visad.Fields;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.SpatialField;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.visad.Times;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalField;
import java.util.Objects;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import static visad.Data.NEAREST_NEIGHBOR;
import static visad.Data.NO_ERRORS;
import static visad.Data.WEIGHTED_AVERAGE;
import visad.DateTime;
import visad.Field;
import visad.FieldImpl;
import visad.FlatField;
import visad.Irregular2DSet;
import visad.RealTuple;
import visad.RealTupleType;
import visad.VisADException;
import visad.georef.LatLonPoint;
import visad.georef.LatLonTuple;
import visad.util.DataUtility;

/**
 * The SpatioTemporalModel class manages data within a spatio-temporal domain. The class provides
 * accessors to extract and retrieve the data. The model is initialized from the static factory
 * methods.
 *
 * The model is organized as either:
 * <pre>Time -> ((Lat, Lon) -> (Range))</pre> or
 * <pre>(Lat, Lon) -> (Time -> (Range))</pre>
 *
 * @author Bruce Schubert
 */
public class SpatioTemporalModel {

    private static final Logger logger = Logger.getLogger(SpatioTemporalModel.class.getName());

    public static SpatioTemporalModel from(ZonedDateTime time, LatLonPoint point, RealTuple tuple) {
        TemporalDomain timeDomain = new TemporalDomain(time, 1);
        SpatialField spatialField = SpatialField.from(point, tuple);
        return SpatioTemporalModel.from(timeDomain, new SpatialField[]{spatialField});
    }

    public static SpatioTemporalModel from(TemporalDomain time, SpatialField[] ranges) {
        try {
            FieldImpl temporalSpatialWeather = time.createTemporalField(ranges[0].getField().getType());
            final int numTimes = time.getDomainSetLength();
            for (int t = 0; t < numTimes; t++) {
                temporalSpatialWeather.setSample(t, ranges[t].getField());
            }
            return new SpatioTemporalModel(temporalSpatialWeather);
        }
        catch (VisADException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static SpatioTemporalModel from(SpatialDomain space, FlatField[] ranges) {
        try {
            FieldImpl spatioTemporalWeather = space.createSpatialField(ranges[0].getType());
            final int numLatLons = space.getDomainSetLength();
            for (int xy = 0; xy < numLatLons; xy++) {
                spatioTemporalWeather.setSample(xy, ranges[xy]);
            }
            return new SpatioTemporalModel(spatioTemporalWeather);
        }
        catch (VisADException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private final FieldImpl field;

    public SpatioTemporalModel(FieldImpl field) {
        validateField(field);
        this.field = field;
    }

    public RealTuple getTuple(ZonedDateTime time, LatLonPoint point) {

        try {
            // Convert args to VisAD domain types
            DateTime dateTime = Times.fromZonedDateTime(time);
            LatLonTuple latLon = point instanceof LatLonTuple
                    ? (LatLonTuple) point
                    : new LatLonTuple(point.getLatitude(), point.getLongitude());

            RealTuple tuple;
            RealTupleType domainType = DataUtility.getDomainType(field);
            
            if (domainType.equals(RealTupleType.Time1DTuple)) {
                FlatField spatialField = (FlatField) field.evaluate(dateTime, WEIGHTED_AVERAGE, NO_ERRORS);
                if (spatialField.getLength() == 1) {
                    tuple = (RealTuple) spatialField.getSample(0);
                }
                else {
                    tuple = (RealTuple) spatialField.evaluate(latLon, WEIGHTED_AVERAGE, NO_ERRORS);
                }
            }
            else { // domainType == RealTypeTuple.LatitudeLongitudeTuple
                FlatField temporalField;
                if (field.getLength() == 1) {
                    temporalField = (FlatField) field.getSample(0);
                }
                else {
                    temporalField = (FlatField) field.evaluate(latLon, WEIGHTED_AVERAGE, NO_ERRORS);
                }
                tuple = (RealTuple) temporalField.evaluate(dateTime, WEIGHTED_AVERAGE, NO_ERRORS);
            }
            return tuple;

        }
        catch (VisADException | RemoteException ex) {
            logger.log(WARNING, "WeatherField.getWeather() failed.", ex);
        }
        return null;
    }

    public FieldImpl getField() {
        return field;
    }

    private void validateField(Field field) {
        if (!Fields.checkSpatioTemporalDomain(field)) {
            throw new IllegalArgumentException("WeatherField.validateField: field is not a spatio-temporal field");
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.field);
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
        final SpatioTemporalModel other = (SpatioTemporalModel) obj;
        if (!Objects.equals(this.field, other.field)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "WeatherField{" + "field=" + field + '}';
    }

}
