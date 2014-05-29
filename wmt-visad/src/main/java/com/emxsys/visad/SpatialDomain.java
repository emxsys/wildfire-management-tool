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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.DataImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Linear2DSet;
import visad.LinearLatLonSet;
import visad.MathType;
import visad.RealTupleType;
import visad.VisADException;
import visad.georef.LatLonPoint;
import visad.georef.LatLonTuple;

/**
 * A Spatial domain defined by 2-dimensional space.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SpatialDomain {

    //private static final RealTupleType SPATIAL_DOMAIN_TYPE = GeoCoord2D.DEFAULT_TUPLE_TYPE;    // includes coord system
    private static final RealTupleType SPATIAL_DOMAIN_TYPE = RealTupleType.LatitudeLongitudeTuple;    // null coord system
    private final Linear2DSet spatialDomainSet;
    /** The number of rows in the spatial grid */
    private final int nrows;
    /** The number of columns in the spatial grid */
    private final int ncols;
    private static final Logger LOG = Logger.getLogger(SpatialDomain.class.getName());

    public SpatialDomain(Linear2DSet spatialDomainSet) {
        this.spatialDomainSet = spatialDomainSet;
        this.nrows = this.spatialDomainSet.getLength(0); // lats
        this.ncols = this.spatialDomainSet.getLength(1); // lons
    }

    /**
     * Constructor extracts temporal and spatial domains from a FieldImpl data type.
     *
     * @param data of type FieldImpl
     */
    public SpatialDomain(DataImpl data) {
        // Validate spatial requirements
        FunctionType spatialFunction = (FunctionType) data.getType();
        if (spatialFunction == null || !spatialFunction.getDomain().equals(RealTupleType.LatitudeLongitudeTuple)) {
            throw new IllegalArgumentException("data range's domain type must be [RealType.Latitude, RealType.Longitude] : "
                    + data.getType().prettyString());
        }

        FlatField spatialField = (FlatField) data;

        this.spatialDomainSet = (LinearLatLonSet) spatialField.getDomainSet();
        this.nrows = spatialDomainSet.getLength(0); // latitudes
        this.ncols = spatialDomainSet.getLength(1); // longitudes

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

    public int getNumColumns() {
        return ncols;
    }

    public int getNumRows() {
        return nrows;
    }

    @Override
    public String toString() {
        return this.spatialDomainSet.toString();
    }

}
