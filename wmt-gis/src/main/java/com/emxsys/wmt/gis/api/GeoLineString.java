/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.gis.api;

import com.emxsys.wmt.gis.api.Box;
import com.emxsys.wmt.gis.api.GisType;
import com.emxsys.wmt.gis.api.LineString;
import com.emxsys.wmt.gis.api.Part;
import edu.wisc.ssec.mcidas.ConversionUtility;
import java.util.Iterator;
import java.util.List;
import visad.Real;

/**
 *
 * @author Bruce Schubert
 * @version $Id$
 */
public class GeoLineString extends AbstractGeometry implements LineString {

    protected GeoSector extents;
    protected GeoPart part;
    private Real length;

    public GeoLineString() {
        this.part = new GeoPart();
        this.extents = new GeoSector();
    }

    public GeoLineString(List<GeoCoord3D> coords) {
        this.part = new GeoPart(coords);
        this.extents = new GeoSector(this);
    }

    @Override
    public Box getExtents() {
        return this.extents;
    }

    @Override
    public int getNumPoints() {
        return this.part.getNumPoints();
    }

    @Override
    public int getNumParts() {
        return 1;
    }

    @Override
    public Iterable<Part> getParts() {
        return new Iterable<Part>() {
            @Override
            public Iterator<Part> iterator() {
                return new Iterator<Part>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index == 0;
                    }

                    @Override
                    public Part next() {
                        if (index == 0) {
                            ++index;
                            return part;
                        }
                        throw new ArrayIndexOutOfBoundsException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        };
    }

    @Override
    public Real getLength() {
        if (this.length == null) {
            double km = 0;
            // Compute combined length of all segments
            for (int i = 0, j = 1; j < part.getNumPoints(); i++, j++) {
                GeoCoord3D coord1 = part.coords.get(i);
                GeoCoord3D coord2 = part.coords.get(j);
                km += ConversionUtility.LatLonToDistance(
                        (float) coord1.getLatitudeDegrees(), (float) coord1.getLongitudeDegrees(),
                        (float) coord2.getLatitudeDegrees(), (float) coord2.getLongitudeDegrees());
            }
            this.length = new Real(GisType.DISTANCE, km * 1000);
        }
        return this.length;
    }

    @Override
    public double getLengthMeters() {
        return getLength().getValue();
    }
}
