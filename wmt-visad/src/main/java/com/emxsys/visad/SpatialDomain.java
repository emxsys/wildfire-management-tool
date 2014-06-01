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
import org.openide.util.NbBundle.Messages;
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
@Messages({
    "ERR_SpatialDomainNotInitialized=Domain not initialized."
})

public class SpatialDomain {

    private Linear2DSet spatialDomainSet;
    /** The number of rows in the spatial grid */
    private int nrows;
    /** The number of columns in the spatial grid */
    private int ncols;
    private static final RealTupleType SPATIAL_DOMAIN_TYPE = RealTupleType.LatitudeLongitudeTuple;    // null coord system
    private static final Logger LOG = Logger.getLogger(SpatialDomain.class.getName());

    /**
     * Constructs an uninitialized SpatialDomain; you must call initialize(...) before use.
     */
    public SpatialDomain() {
    }

    /**
     * Constructs a SpatialDomain backed by a LinearLatLonSet.
     *
     * @param minLatLon The southwest coordinate.
     * @param maxLatLon The northeast coordinate.
     * @param nrows The number of rows (latitudes) in the domain.
     * @param ncols The number of columns (longitudes) in the domain.
     */
    public SpatialDomain(LatLonPoint minLatLon, LatLonPoint maxLatLon, int nrows, int ncols) {
        initialize(minLatLon, maxLatLon, nrows, ncols);
    }
    
    /**
     * Constructs a SpatialDomain backed by a LinearLatLonSet consisting of a single point.
     * 
     * @param point The sole lat/lon in the domain.
     */
    public SpatialDomain(LatLonPoint point) {
        initialize(point, point, 1, 1);
    }

    /**
     * Constructs a SpatialDomain from the given Linear2DSet.
     *
     * @param spatialDomainSet The set this domain will represent.
     */
    public SpatialDomain(Linear2DSet spatialDomainSet) {
        this.spatialDomainSet = spatialDomainSet;
        this.nrows = this.spatialDomainSet.getLength(0); // lats
        this.ncols = this.spatialDomainSet.getLength(1); // lons
    }

    /**
     * Constructor extracts temporal and spatial domains from a FieldImpl data type.
     *
     * @param data of type FieldImpl
     * @deprecated Untested!!
     */
    @Deprecated
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

    /**
     * (Re)Initializes the SpatialDomain with a new a LinearLatLonSet.
     *
     * @param minLatLon The southwest coordinate.
     * @param maxLatLon The northeast coordinate.
     * @param nrows The number of rows (latitudes) in the domain.
     * @param ncols The number of columns (longitudes) in the domain.
     */
    public final void initialize(LatLonPoint minLatLon, LatLonPoint maxLatLon, int nrows, int ncols) {
        try {
            if (minLatLon.isMissing() || maxLatLon.isMissing()) {
                String msg = "Cannot initialize spatial domain.  The lat/lon argument(s) has missing values.";
                LOG.severe(msg);
                throw new IllegalArgumentException(msg);
            }
            double minLat = minLatLon.getLatitude().getValue();
            double minLon = minLatLon.getLongitude().getValue();
            double maxLat = maxLatLon.getLatitude().getValue();
            double maxLon = maxLatLon.getLongitude().getValue();
            this.spatialDomainSet = new LinearLatLonSet(SPATIAL_DOMAIN_TYPE, // includes a coord system defn!!!!
                    minLat, maxLat, nrows,
                    minLon, maxLon, ncols,
                    null, // another Coordinate system 
                    null, null, true); // true == cache samples
        }
        catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
            throw new RuntimeException("Cannot initialize spatial domain.", ex);
        }
    }

    /**
     * @return True if this SpatialDomain has been initialized and is ready for use.
     */
    public boolean isInitialized() {
        return this.spatialDomainSet != null;
    }

    /**
     * Gets the lat/lon at the specified row/column index.
     * @param row The row index; must be less that number of rows.
     * @param col The column index; must be less than the number of columns
     * @return A LatLonPoint.
     */
    public LatLonPoint getLatLonPointAt(int row, int col) {
        if (row < 0 || row >= nrows) {
            throw new IllegalArgumentException("Invalid row index: " + row);
        }
        if (col < 0 || col >= ncols) {
            throw new IllegalArgumentException("Invalid column index: " + col);
        }
        return getLatLonPointAt((row * col) + col);
    }

    /**
     * Gets the lat/lon at the specified array index.
     * @param index The array index; must be less than the domain set's length (rows*cols).
     * @return A LatLonPoint.
     */
    public LatLonPoint getLatLonPointAt(int index) {
        try {
            if (!isInitialized()) {
                throw new IllegalStateException(Bundle.ERR_SpatialDomainNotInitialized());
            }
            if (index < 0 || index >= this.spatialDomainSet.getLength()) {
                throw new IllegalArgumentException("Invalid index: " + index);
            }
            double lat = this.spatialDomainSet.getDoubles(false)[0][index];
            double lon = this.spatialDomainSet.getDoubles(false)[1][index];
            return new LatLonTuple(lat, lon);
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a function represented by a FlatField from the spatial domain using the supplied
     * MathType for the range. The caller must set the range samples.
     *
     * @param range The MathType for the function's range.
     * @return A FlatField for the function: (lat,lon) -> (range).
     */
    public FlatField createSimpleSpatialField(MathType range) {
        try {
            if (!isInitialized()) {
                throw new IllegalStateException(Bundle.ERR_SpatialDomainNotInitialized());
            }
            FunctionType functionType = new FunctionType(SPATIAL_DOMAIN_TYPE, range);
            FlatField field = new FlatField(functionType, spatialDomainSet);
            LOG.log(Level.FINE, "createSimpleSpatialField created: {0}", field.getType().prettyString());
            return field;
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public Linear2DSet getDomainSet() {
        return spatialDomainSet;
    }

    public int getSpatialDomainSetLength() {
        try {
            if (!isInitialized()) {
                throw new IllegalStateException(Bundle.ERR_SpatialDomainNotInitialized());
            }
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
        if (!isInitialized()) {
            throw new IllegalStateException(Bundle.ERR_SpatialDomainNotInitialized());
        }
        return this.spatialDomainSet.toString();
    }

}
