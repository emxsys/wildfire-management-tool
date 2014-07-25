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

import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import visad.Field;
import visad.FieldImpl;
import visad.FlatField;
import static visad.RealTupleType.Generic3D;
import visad.VisADException;
import visad.georef.LatLonPoint;
import visad.georef.LatLonTuple;

/**
 *
 * @author Bruce Schubert
 */
public class FieldsTest {

    public FieldsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testCheckSpatioTemporalDomain() throws VisADException, RemoteException {
        System.out.println("checkSpatioTemporalDomain");

        TemporalDomain temporalDomain = new TemporalDomain(ZonedDateTime.now(), 24);
        SpatialDomain spatialDomain = new SpatialDomain(new LatLonTuple(34.0, -120.0), new LatLonTuple(35.0, -119.0), 3, 3);
        final int numLatLons = spatialDomain.getSpatialDomainSetLength();
        final int numTimes = temporalDomain.getTemporalDomainSetLength();

        // Test (time) -> ((space) -> (range))
        {
            FlatField spatialField = spatialDomain.createSimpleSpatialField(Generic3D);
            FieldImpl temporalSpatialField = temporalDomain.createTemporalField(spatialField.getType());
            for (int t = 0; t < numTimes; t++) {
                double[][] rangeSamples = new double[Generic3D.getDimension()][numLatLons];
                for (int xy = 0; xy < numLatLons; xy++) {
                    LatLonPoint pt = spatialDomain.getLatLonPointAt(xy);
                    rangeSamples[0][xy] = pt.getLatitude().getValue();
                    rangeSamples[1][xy] = pt.getLongitude().getValue();
                    rangeSamples[2][xy] = t;
                }
                spatialField.setSamples(rangeSamples);
                temporalSpatialField.setSample(t, spatialField);
            }
            boolean result = Fields.checkSpatioTemporalDomain(temporalSpatialField);
            assertTrue("checkSpatioTemporalDomain", result);
            System.out.println(temporalSpatialField);
        }
        // Test (space) -> ((time) -> (range))
        {
            FlatField temporalField = temporalDomain.createSimpleTemporalField(Generic3D);
            FieldImpl spatioTemporalField = spatialDomain.createSpatialField(temporalField.getType());
            for (int xy = 0; xy < numLatLons; xy++) {
                LatLonPoint pt = spatialDomain.getLatLonPointAt(xy);
                double[][] rangeSamples = new double[Generic3D.getDimension()][numTimes];
                for (int t = 0; t < numTimes; t++) {
                    rangeSamples[0][t] = pt.getLatitude().getValue();
                    rangeSamples[1][t] = pt.getLongitude().getValue();
                    rangeSamples[2][t] = t;
                }
                temporalField.setSamples(rangeSamples);
                spatioTemporalField.setSample(xy, temporalField);
            }
            boolean result = Fields.checkSpatioTemporalDomain(spatioTemporalField);
            assertTrue("checkSpatioTemporalDomain", result);
            System.out.println(spatioTemporalField);
        }
    }

}
