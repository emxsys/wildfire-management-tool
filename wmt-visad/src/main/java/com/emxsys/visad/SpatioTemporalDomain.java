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
import java.time.ZonedDateTime;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.DateTime;
import visad.Gridded1DDoubleSet;
import visad.Real;
import visad.VisADException;
import visad.georef.LatLonPoint;

/**
 * A Spatio-Temporal domain is a simple composite of a TemporalDomain and a SpatialDomain.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SpatioTemporalDomain {

    private static final Logger LOG = Logger.getLogger(SpatioTemporalDomain.class.getName());
    private TemporalDomain temporalDomain = new TemporalDomain();
    private SpatialDomain spatialDomain = new SpatialDomain();

    public SpatioTemporalDomain(LatLonPoint minLatLon, LatLonPoint maxLatLon, Gridded1DDoubleSet timeset, int timezoneSeconds) {
        initializeSpatialDomain(maxLatLon, minLatLon);
        temporalDomain.initialize(timeset, timezoneSeconds);
    }

    private void initializeSpatialDomain(LatLonPoint maxLatLon, LatLonPoint minLatLon) {
        try {
            Real height = (Real) (maxLatLon.getLatitude().subtract(minLatLon.getLatitude()));
            Real width = (Real) (maxLatLon.getLongitude().subtract(minLatLon.getLongitude()));
            int nrows = (int) (height.getValue() / 0.00027); // ~30m at equator
            int ncols = (int) (width.getValue() / 0.00027); // ~30m at equator
            spatialDomain.initialize(minLatLon, maxLatLon, nrows, ncols);
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public SpatioTemporalDomain(LatLonPoint minLatLon, LatLonPoint maxLatLon, ZonedDateTime startDate, int numHours) {
        temporalDomain.initialize(startDate, numHours);
        initializeSpatialDomain(maxLatLon, minLatLon);
    }

    public SpatioTemporalDomain(TemporalDomain temporalDomain, SpatialDomain spatialDomain) {
        if (temporalDomain==null) {
            throw new IllegalArgumentException("TemporalDomain is null.");
        } else if (spatialDomain==null) {
            throw new IllegalArgumentException("SpatialDomain is null.");
        }
        this.temporalDomain = temporalDomain;
        this.spatialDomain = spatialDomain;
    }

    public ZonedDateTime getStartDate() {
        return this.temporalDomain.getStart();
    }

    public ZonedDateTime getZonedDateTimeAt(int index) {
        return this.temporalDomain.getZonedDateTimeAt(index);
    }

    public DateTime getDateTimeAt(int index) {
        return this.temporalDomain.getDateTimeAt(index);
    }

    public LatLonPoint getLatLonPointAt(int row, int col) {
        return this.spatialDomain.getLatLonPointAt(row, col);
    }

    public LatLonPoint getLatLonPointAt(int index) {
        return this.spatialDomain.getLatLonPointAt(index);
    }

    public SpatialDomain getSpatialDomain() {
        return spatialDomain;
    }

    public TemporalDomain getTemporalDomain() {
        return temporalDomain;
    }

    @Override
    public String toString() {
        return this.temporalDomain.toString() + ", " + this.spatialDomain.toString();
    }

}
