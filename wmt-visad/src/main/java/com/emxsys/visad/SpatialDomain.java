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
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Irregular2DSet;
import visad.Linear2DSet;
import visad.LinearLatLonSet;
import visad.MathType;
import visad.RealTupleType;
import visad.SampledSet;
import visad.Set;
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

    public static SpatialDomain from(LatLonPoint point) {
        return new SpatialDomain(point, point, 1, 1);
    }

    /**
     * Creates a spatial domain bounding box from two coordinates.
     * @param point1
     * @param point2
     * @return
     */
    public static SpatialDomain from(LatLonPoint point1, LatLonPoint point2) {
        return from(point1, point2, 2, 2);
    }

    /**
     * Creates a spatial domain bounding box from two coordinates.
     * @param point1
     * @param point2
     * @param rows
     * @param cols
     * @return
     */
    public static SpatialDomain from(LatLonPoint point1, LatLonPoint point2, int rows, int cols) {
        try {
            double minLat = Math.min(point1.getLatitude().getValue(), point2.getLatitude().getValue());
            double maxLat = Math.max(point1.getLatitude().getValue(), point2.getLatitude().getValue());
            double minLon = Math.min(point1.getLongitude().getValue(), point2.getLongitude().getValue());
            double maxLon = Math.max(point1.getLongitude().getValue(), point2.getLongitude().getValue());
            LatLonTuple sw = new LatLonTuple(minLat, minLon);
            LatLonTuple ne = new LatLonTuple(maxLat, maxLon);
            return new SpatialDomain(sw, ne, rows, cols);
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    private Set spatialDomainSet;
    /** The number of rows in the spatial grid */
    private int nrows;
    /** The number of columns in the spatial grid */
    private int ncols;
    private static final RealTupleType SPATIAL_DOMAIN_TYPE = RealTupleType.LatitudeLongitudeTuple;    // null coord system
    private static final Logger logger = Logger.getLogger(SpatialDomain.class.getName());

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
     * Constructs a SpatialDomain from the given Linear2DSet.
     *
     * @param spatialDomainSet The set this domain will represent.
     */
    public SpatialDomain(Linear2DSet spatialDomainSet) {
        this.spatialDomainSet = spatialDomainSet;
        this.nrows = spatialDomainSet.getLength(0); // lats
        this.ncols = spatialDomainSet.getLength(1); // lons
    }

    /**
     * Constructor extracts spatial domains from a FieldImpl data type.
     *
     * @param data of type FieldImpl
     * @deprecated Untested!!
     */
    public SpatialDomain(FieldImpl data) {
        // Validate spatial requirements
        FunctionType spatialFunction = (FunctionType) data.getType();
        if (spatialFunction == null || !spatialFunction.getDomain().equals(RealTupleType.LatitudeLongitudeTuple)) {
            throw new IllegalArgumentException("data range's domain type must be [RealType.Latitude, RealType.Longitude] : "
                    + data.getType().prettyString());
        }
        FlatField spatialField = (FlatField) data;
        this.spatialDomainSet = spatialField.getDomainSet();
    }

    public boolean contains(LatLonPoint latLon) {
        if (!isInitialized()) {
            return false;
        }
        Set domainSet = getDomainSet();
        if (domainSet instanceof SampledSet) {
            float[] low = ((SampledSet) domainSet).getLow();
            float[] hi = ((SampledSet) domainSet).getHi();
            double lat = latLon.getLatitude().getValue();
            double lon = latLon.getLongitude().getValue();
            return lat >= low[0] && lat <= hi[0]
                    && lon >= low[1] && lon <= hi[1];
        }
        else {
            throw new UnsupportedOperationException("SpatialDomain.contains() does not support " + domainSet.getClass().getSimpleName());
        }
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
                logger.severe(msg);
                throw new IllegalArgumentException(msg);
            }
            double minLat = minLatLon.getLatitude().getValue();
            double minLon = minLatLon.getLongitude().getValue();
            double maxLat = maxLatLon.getLatitude().getValue();
            double maxLon = maxLatLon.getLongitude().getValue();
            this.spatialDomainSet = new LinearLatLonSet(SPATIAL_DOMAIN_TYPE,
                    minLat, maxLat, nrows,
                    minLon, maxLon, ncols,
                    null, // another Coordinate system 
                    null, null, true); // true == cache samples
            this.nrows = nrows;
            this.ncols = ncols;
        }
        catch (VisADException | RemoteException ex) {
            logger.severe(ex.toString());
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
     * Gets the SW bounding box coordinate of the domain.
     * @return A LatLonTuple.
     */
    public LatLonPoint getMinLatLon() {
        try {
            if (!isInitialized()) {
                throw new IllegalStateException(Bundle.ERR_SpatialDomainNotInitialized());
            }
            double[][] samples = spatialDomainSet.getDoubles(false);
            OptionalDouble lat = Arrays.stream(samples[0]).min();
            OptionalDouble lon = Arrays.stream(samples[1]).min();
            if (lat.isPresent() && lon.isPresent()) {
                return new LatLonTuple(lat.getAsDouble(), lon.getAsDouble());
            }
            else {
                return new LatLonTuple();
            }
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the NE bounding box coordinate of the domain.
     * @return A LatLonTuple
     */
    public LatLonPoint getMaxLatLon() {
        try {
            if (!isInitialized()) {
                throw new IllegalStateException(Bundle.ERR_SpatialDomainNotInitialized());
            }
            double[][] samples = spatialDomainSet.getDoubles(false);
            OptionalDouble lat = Arrays.stream(samples[0]).max();
            OptionalDouble lon = Arrays.stream(samples[1]).max();
            if (lat.isPresent() && lon.isPresent()) {
                return new LatLonTuple(lat.getAsDouble(), lon.getAsDouble());
            }
            else {
                return new LatLonTuple();
            }
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
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
            logger.log(Level.FINE, "createSimpleSpatialField created: {0}", field.getType().prettyString());
            return field;
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a VisAD function represented by a FieldImpl from the spatial domain using the
     * supplied MathType for the range. The caller must set the range samples.
     * @param range The MathType to use as the function's range.
     * @return A FieldImpl with FunctionType (lat, lon) -> (range).
     */
    public FieldImpl createSpatialField(MathType range) {
        if (!isInitialized()) {
            throw new IllegalStateException(Bundle.ERR_TemporalDomainNotInitialized());
        }
        try {
            FunctionType functionType = new FunctionType(SPATIAL_DOMAIN_TYPE, range);
            FieldImpl field = new FieldImpl(functionType, spatialDomainSet);
            logger.log(Level.FINE, "createSpatialField created: {0}", field.getType().prettyString());
            return field;
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public Set getDomainSet() {
        return spatialDomainSet;
    }

    public int getDomainSetLength() {
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

    @Override
    public String toString() {
        if (!isInitialized()) {
            throw new IllegalStateException(Bundle.ERR_SpatialDomainNotInitialized());
        }
        return this.spatialDomainSet.toString();
    }

}
