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
package com.emxsys.wmt.cps.fireground;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.visad.Times;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DDoubleSet;
import visad.Linear2DSet;
import visad.LinearLatLonSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;


/**
 * The fireground domain defined by time and space.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SpatioTemporalDomain
{
    public static final int DAILY_CYCLE_START_HOUR = 14;
    public static final int DEFAULT_NUM_CYCLES = 2;
    /**
     * The spatial domain type -- a 2D domainType tuple : (latitude, longitude)
     */
    //private static final RealTupleType spatialDomainType = GeoCoord2D.DEFAULT_TUPLE_TYPE;    // includes coord system
    private static final RealTupleType spatialDomainType = RealTupleType.LatitudeLongitudeTuple;    // null coord system
    /**
     * The temporal domain type
     */
    private static final RealType temporalDomainType = RealType.Time;
    /**
     * The geographic bounds
     */
    private final Box sector;
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


    public SpatioTemporalDomain(Box sector, Gridded1DDoubleSet timeset)
    {
        // this(sector, Calendar.getInstance().getTime(), DEFAULT_NUM_CYCLES);
        this.sector = sector;

        double height = sector.getHeight().getValue();
        double width = sector.getWidth().getValue();
        this.nrows = (int) (height / 0.00027); // ~30m at equator
        this.ncols = (int) (width / 0.00027); // ~30m at equator
        this.spatialDomainSet = createSpatialDomainSet(sector, nrows, ncols);
        this.temporalDomainSet = timeset;
    }


    /**
     *
     * @param sector Extents of geographic domain.
     * @param startDate Beginning of temporal domain.
     * @param numCycles Number of 24 hour periods (days).
     */
    public SpatioTemporalDomain(GeoSector sector, Date startDate, int numCycles)
    {
        this.sector = sector;

        double height = sector.getHeight().getValue();
        double width = sector.getWidth().getValue();
        this.nrows = (int) (height / 0.00027); // ~30m at equator
        this.ncols = (int) (width / 0.00027); // ~30m at equator
        this.spatialDomainSet = createSpatialDomainSet(sector, nrows, ncols);
        this.temporalDomainSet = createTemporalDomainSet(startDate, numCycles);
    }


//    /**
//     * Constructor extracts temporal and spatial domains from a FieldImpl data type.
//     * 
//     * @param data of type FieldImpl
//     */
//    public SpatioTemporalDomain(DataImpl data)
//    {
//        try
//        {
//            // Validate temporal requirements
//            FunctionType temporalFunction = (FunctionType) data.getType();
//            if (temporalFunction == null
//                || !(temporalFunction.getDomain().getComponent(0).equals(RealType.Time)))
//            {
//                throw new IllegalArgumentException("data domain type must be RealType.Time : "
//                    + data.getType().prettyString());
//            }
//
//            // Validate spatial requirements
//            FunctionType spatialFunction = (FunctionType) temporalFunction.getRange();
//            if (spatialFunction == null
//                || !(spatialFunction.getDomain().equals(RealTupleType.LatitudeLongitudeTuple)
//                || spatialFunction.getDomain().equals(GeoCoord2D.DEFAULT_TUPLE_TYPE)))
//            {
//                throw new IllegalArgumentException("data range's domain type must be [RealType.Latitude, RealType.Longitude] : "
//                    + data.getType().prettyString());
//            }
//            FieldImpl field = (FieldImpl) data;
//            FlatField spatialField = (FlatField) field.getSample(0);
//
//            this.temporalDomainSet = (Gridded1DDoubleSet) field.getDomainSet();
//            this.spatialDomainSet = (LinearLatLonSet) spatialField.getDomainSet();
//
//            this.nrows = spatialDomainSet.getLength(0); // latitudes
//            this.ncols = spatialDomainSet.getLength(1); // longitudes
//
//            float[] low = spatialDomainSet.getLow();
//            float[] hi = spatialDomainSet.getHi();
//            float south = low[0];
//            float west = low[1];
//            float north = hi[0];
//            float east = hi[1];
//            this.sector = new GeoSector(south, west, north, east);
//        }
//        catch (IllegalArgumentException | VisADException | RemoteException ex)
//        {
//            Exceptions.printStackTrace(ex);
//            throw new IllegalArgumentException(ex.toString());
//        }
//    }
//
//
//    private void initialize(Linear2DSet spatialSet, Gridded1DDoubleSet timeset)
//    {
//    }


    private static Linear2DSet createSpatialDomainSet(Box sector, int nrows, int ncols)
    {
        if (sector.isMissing())
        {
            String msg = "Cannot initialize spatial domain.  The sector argument has missing values.";
            LOG.severe(msg);
            throw new IllegalArgumentException(msg);
        }
        double minLat = sector.getSouthwest().getLatitude().getValue();
        double minLon = sector.getSouthwest().getLongitude().getValue();
        double maxLat = sector.getNortheast().getLatitude().getValue();
        double maxLon = sector.getNortheast().getLongitude().getValue();
        try
        {
            // XXX  I don't understand the interaction between the 
            //      Type's coord system and the Set's coord system! 
            //      Maybe the two must be compatible (or both null)
            //  
            // Create the domainType input Set: 
            return new LinearLatLonSet(spatialDomainType, // includes a coord system defn!!!!
                minLat, maxLat, nrows,
                minLon, maxLon, ncols,
                //RealTupleType.LatitudeLongitudeTuple.getCoordinateSystem(), //doesn't work with evaluate from dynamic data
                //GeoCoord2D.DEFAULT_COORD_SYS, // doesn't work with ParticleAnalytics evaluate() loaded from file
                null, // another Coordinate system 
                null, null, true); // true == cache samples
        }
        catch (VisADException ex)
        {
            LOG.severe(ex.toString());
            throw new RuntimeException("Cannot initialize spatial domain.", ex);
        }
    }


    private static Gridded1DDoubleSet createTemporalDomainSet(Date startDate, int numCycles)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, DAILY_CYCLE_START_HOUR);  // 1400 local time
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        int numHours = numCycles * 24;
        try
        {
            DateTime[] times = new DateTime[numHours];
            for (int i = 0; i < times.length; i++)
            {
                times[i] = new DateTime(cal.getTime());
                cal.add(Calendar.HOUR, 1);
            }
            Gridded1DDoubleSet timeSet = DateTime.makeTimeSet(times);
            return timeSet;
        }
        catch (VisADException ex)
        {
            LOG.severe(ex.toString());
            throw new RuntimeException("Cannot initialize temporal domain.", ex);
        }
    }


    public Date getStartDate()
    {
        try
        {
            return Times.toDate(new DateTime(this.temporalDomainSet.getDoubleLowX()));
        }
        catch (VisADException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }


    public DateTime getDateTimeAt(int index)
    {
        try
        {
            if (index < 0 || index >= this.temporalDomainSet.getLength())
            {
                throw new IllegalArgumentException("Invalid index: " + index);
            }
            float time = this.temporalDomainSet.getSamples(false)[0][index];
            return new DateTime(time);
        }
        catch (VisADException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }


    public GeoCoord2D getGeoPointAt(int row, int col)
    {
        if (row < 0 || row >= nrows)
        {
            throw new IllegalArgumentException("Invalid row index: " + row);
        }
        if (col < 0 || col >= ncols)
        {
            throw new IllegalArgumentException("Invalid column index: " + col);
        }
        return getGeoPointAt((row * col) + col);
    }


    public GeoCoord2D getGeoPointAt(int index)
    {
        try
        {
            if (index < 0 || index >= this.spatialDomainSet.getLength())
            {
                throw new IllegalArgumentException("Invalid index: " + index);
            }
//            double lat = this.spatialDomainSet.getDoubles(false)[0][index];
//            double lon = this.spatialDomainSet.getDoubles(false)[1][index];
            double lat = this.spatialDomainSet.getSamples(false)[0][index];
            double lon = this.spatialDomainSet.getSamples(false)[1][index];
            return GeoCoord2D.fromDegrees(lat, lon);
        }
        catch (VisADException ex)
        {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }


    public FlatField newSpatialField(MathType range)
    {
        try
        {
            FunctionType functionType = new FunctionType(SpatioTemporalDomain.spatialDomainType, range);
            FlatField field = new FlatField(functionType, this.spatialDomainSet);
            LOG.log(Level.INFO, "newSpatialField created: {0}", field.getType().prettyString());
            return field;
        }
        catch (VisADException ex)
        {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }


//    public FlatField newTemporalField(MathType range)
//    {
//        try
//        {
//            FunctionType functionType = new FunctionType(SpatioTemporalDomain.temporalDomainType, range);
//            FlatField field = new FlatField(functionType, this.temporalDomainSet);
//            LOG.info("newTemporalField created: " + field.getType().prettyString());
//            return field;
//        }
//        catch (VisADException ex)
//        {
//            Exceptions.printStackTrace(ex);
//            throw new RuntimeException(ex);
//        }
//    }
    public FieldImpl newTemporalField(MathType range)
    {
        try
        {
            FunctionType functionType = new FunctionType(SpatioTemporalDomain.temporalDomainType, range);
            FieldImpl field = new FieldImpl(functionType, this.temporalDomainSet);
            LOG.log(Level.INFO, "newTemporalField created: {0}", field.getType().prettyString());
            return field;
        }
        catch (VisADException ex)
        {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }


    public Linear2DSet getSpatialDomainSet()
    {
        return spatialDomainSet;
    }


    public int getSpatialDomainSetLength()
    {
        try
        {
            return spatialDomainSet.getLength();
        }
        catch (VisADException ex)
        {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }


    public RealTupleType getSpatialDomainType()
    {
        return spatialDomainType;
    }


    public Gridded1DDoubleSet getTemporalDomainSet()
    {
        return temporalDomainSet;
    }


    public RealType getTemporalDomainType()
    {
        return temporalDomainType;
    }


    public Box getSector()
    {
        return sector;
    }


    public int getNumColumns()
    {
        return ncols;
    }


    public int getNumRows()
    {
        return nrows;
    }


    @Override
    public String toString()
    {
        return this.temporalDomainSet.toString() + ", " + this.spatialDomainSet.toString();
    }
    
    
}