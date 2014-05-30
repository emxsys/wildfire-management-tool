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
package com.emxsys.visad;

import java.rmi.RemoteException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DDoubleSet;
import visad.Linear2DSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;
import visad.georef.LatLonPoint;
import visad.georef.LatLonTuple;

/**
 * A Spatio-Temporal domain defined by time and space.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SpatioTemporalDomain {

    /**
     * The spatial domain type -- a 2D domainType tuple : (latitude, longitude)
     */
    //private static final RealTupleType SPATIAL_DOMAIN_TYPE = GeoCoord2D.DEFAULT_TUPLE_TYPE;    // includes coord system
    private static final RealTupleType SPATIAL_DOMAIN_TYPE = RealTupleType.LatitudeLongitudeTuple;    // null coord system
    /**
     * The temporal domain type
     */
    private static final RealType TEMPORAL_DOMAIN_TYPE = RealType.Time;
    /**
     * The spatial domain set
     */
    private final Linear2DSet spatialDomainSet;
    /**
     * The temporal domain set
     */
    private final Gridded1DDoubleSet temporalDomainSet;
    /**
     * The number of rows in the spatial grid
     */
    private final int nrows;
    /**
     * The number of columns in the spatial grid
     */
    private final int ncols;
    /**
     * The error logger
     */
    private static final Logger LOG = Logger.getLogger(SpatioTemporalDomain.class.getName());

    public SpatioTemporalDomain(Gridded1DDoubleSet timeDomainSet, Linear2DSet spatialDomainSet) {
        this.temporalDomainSet = timeDomainSet;
        this.spatialDomainSet = spatialDomainSet;
        this.nrows = this.spatialDomainSet.getLength(0); // lats
        this.ncols = this.spatialDomainSet.getLength(1); // lons
    }


    public ZonedDateTime getStartDate() {
        Instant instant = Instant.ofEpochSecond((long) this.temporalDomainSet.getDoubleLowX());
        return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

    public ZonedDateTime getZonedDateTimeAt(int index) {
        try {
            if (index < 0 || index >= this.temporalDomainSet.getLength()) {
                throw new IllegalArgumentException("Invalid index: " + index);
            }
            double time = this.temporalDomainSet.getDoubles(false)[0][index];
            Instant instant = Instant.ofEpochSecond((long) time);
            return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public DateTime getDateTimeAt(int index) {
        try {
            if (index < 0 || index >= this.temporalDomainSet.getLength()) {
                throw new IllegalArgumentException("Invalid index: " + index);
            }
            double time = this.temporalDomainSet.getDoubles(false)[0][index];
            return Times.fromDouble(time);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public LatLonPoint getLatLonPointAt(int row, int col) {
        if (row < 0 || row >= nrows) {
            throw new IllegalArgumentException("Invalid row index: " + row);
        }
        if (col < 0 || col >= ncols) {
            throw new IllegalArgumentException("Invalid column index: " + col);
        }
        return getLatLonPointAt((row * col) + col);
    }

    public LatLonPoint getLatLonPointAt(int index) {
        try {
            if (index < 0 || index >= this.spatialDomainSet.getLength()) {
                throw new IllegalArgumentException("Invalid index: " + index);
            }
            double lat = this.spatialDomainSet.getDoubles(false)[0][index];
            double lon = this.spatialDomainSet.getDoubles(false)[1][index];
            //double lat = this.spatialDomainSet.getSamples(false)[0][index];
            //double lon = this.spatialDomainSet.getSamples(false)[1][index];
            return new LatLonTuple(lat, lon);
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public FlatField newSpatialField(MathType range) {
        try {
            FunctionType functionType = new FunctionType(SPATIAL_DOMAIN_TYPE, range);
            FlatField field = new FlatField(functionType, this.spatialDomainSet);
            LOG.log(Level.INFO, "newSpatialField created: {0}", field.getType().prettyString());
            return field;
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }


    public FieldImpl newTemporalField(MathType range) {
        try {
            FunctionType functionType = new FunctionType(TEMPORAL_DOMAIN_TYPE, range);
            FieldImpl field = new FieldImpl(functionType, this.temporalDomainSet);
            LOG.log(Level.INFO, "newTemporalField created: {0}", field.getType().prettyString());
            return field;
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public Linear2DSet getSpatialDomainSet() {
        return spatialDomainSet;
    }

    public int getSpatialDomainSetLength() {
        try {
            return spatialDomainSet.getLength();
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public RealTupleType getSpatialDomainType() {
        return SPATIAL_DOMAIN_TYPE;
    }

    public Gridded1DDoubleSet getTemporalDomainSet() {
        return temporalDomainSet;
    }

    public RealType getTemporalDomainType() {
        return TEMPORAL_DOMAIN_TYPE;
    }

    public int getNumColumns() {
        return ncols;
    }

    public int getNumRows() {
        return nrows;
    }

    @Override
    public String toString() {
        return this.temporalDomainSet.toString() + ", " + this.spatialDomainSet.toString();
    }

}
