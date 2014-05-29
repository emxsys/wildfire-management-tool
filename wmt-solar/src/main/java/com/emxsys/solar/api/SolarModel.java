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
package com.emxsys.solar.api;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GeoSector;
import static com.emxsys.solar.api.SolarType.SUNLIGHT;
import com.emxsys.solar.spi.DefaultSunlightProvider;
import com.emxsys.visad.SpatioTemporalDomain;
import com.emxsys.visad.Times;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.util.logging.Logger;
import visad.Data;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.Gridded1DDoubleSet;
import visad.Linear2DSet;
import visad.RealTuple;
import visad.RealTupleType;
import visad.VisADException;
import visad.georef.LatLonPoint;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SolarModel {

    /**
     * The time/space domain
     */
    private final SpatioTemporalDomain domain;
    /**
     * The solar data in a spatio-temporal domain
     */
    private FieldImpl solarField;
    /**
     * Error logger
     */
    private static final Logger LOG = Logger.getLogger(SolarModel.class.getName());

    public SolarModel(ZonedDateTime start, int numHours, Box box, int numRows, int numCols, boolean immediate) {
        Gridded1DDoubleSet temporalSet = Times.makeHourlyTimeSet(start, numHours);
        Linear2DSet spatialSet = GeoSector.fromBox(box).toLinear2DSet(numRows, numCols);
        this.domain = new SpatioTemporalDomain(temporalSet, spatialSet);
        if (immediate) {
            createSolarField();
        }
    }

    /**
     * Constructs the SolarModel with deferred initialization of the hourly data.
     *
     * @param domain
     */
    public SolarModel(SpatioTemporalDomain domain) {
        // Defer initialization the field member
        this(domain, false);
    }

    public SolarModel(SpatioTemporalDomain domain, boolean immediate) {
        this.domain = domain;
        if (immediate) {
            createSolarField();
        }
    }

    public SpatioTemporalDomain getDomain() {
        return domain;
    }

    public SunlightTuple getSunlight(ZonedDateTime temporal, Coord2D spatial) {
        if (this.solarField == null) {
            this.solarField = createSolarField();
        }
        return getSunlight(this.solarField, temporal, spatial);
    }

    private static SunlightTuple getSunlight(FieldImpl solarField, ZonedDateTime temporal, Coord2D spatial) {
        try {
            DateTime time = Times.fromZonedDateTime(temporal);
            RealTuple location = new RealTuple(RealTupleType.LatitudeLongitudeTuple, new double[]{
                spatial.getLatitudeDegrees(), spatial.getLongitudeDegrees()
            });
            // Function: (time -> ((lat, lon) -> (sunlight)))
            FieldImpl field = (FieldImpl) solarField.evaluate(time, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            RealTuple tuple = (RealTuple) field.evaluate(location, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            return SunlightTuple.fromRealTuple(tuple);
        } catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    public SunlightTuple getSunlightAt(int temporalIndex, int spatialIndex) {
        if (this.solarField == null) {
            this.solarField = createSolarField();
        }
        return SunlightTuple.fromRealTuple(getSunlightAt(this.solarField, temporalIndex, spatialIndex));
    }

    private static SunlightTuple getSunlightAt(FieldImpl solarField, int temporalIndex, int spatialIndex) {
        try {
            FieldImpl field = (FieldImpl) solarField.getSample(temporalIndex);
            RealTuple tuple = (RealTuple) field.getSample(spatialIndex);
            return SunlightTuple.fromRealTuple(tuple);
        } catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Math type:<br/>
     * ( time -> ( (latitude, longitude) -> ( declination, ... ) ) )
     *
     * @return hourly solar values
     */
    public final FieldImpl getSolarField() {
        if (this.solarField == null) {
            this.solarField = createSolarField();
        }
        return this.solarField;
    }

    private FieldImpl createSolarField() {
        SunlightProvider sun = DefaultSunlightProvider.getInstance();
        try {
            FlatField spatialFlatField = this.domain.newSpatialField(SUNLIGHT);
            FieldImpl spatioTemporalField = this.domain.newTemporalField(spatialFlatField.getType());

            final int numLatLons = this.domain.getSpatialDomainSet().getLength();
            final int numTimes = this.domain.getTemporalDomainSet().getLength();
            double[][] solarSamples = new double[SUNLIGHT.getDimension()][numLatLons];

            // Loop through the temporal domain
            for (int i = 0; i < numTimes; i++) {
                ZonedDateTime time = this.domain.getZonedDateTimeAt(i);

                // Loop through the spatial domain
                for (int xy = 0; xy < numLatLons; xy++) {
                    LatLonPoint latLon = this.domain.getLatLonPointAt(xy);
                    GeoCoord3D coord = GeoCoord3D.fromLatLonPoint(latLon);

                    // Compute solar data at the time and place
                    RealTuple sunlight = sun.getSunlight(time, coord);
                    double[] values = sunlight.getValues();
                    for (int dim = 0; dim < SolarType.SUNLIGHT.getDimension(); dim++) {
                        solarSamples[dim][xy] = values[dim];
                    }
                }
                spatialFlatField.setSamples(solarSamples);
                spatioTemporalField.setSample(i, spatialFlatField);
            }
            this.solarField = spatioTemporalField;

            return spatioTemporalField;

        } catch (IllegalStateException | VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
        }
        return null;
    }
}
