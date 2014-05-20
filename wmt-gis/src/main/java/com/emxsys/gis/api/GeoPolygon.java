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
package com.emxsys.gis.api;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.GisType;
import com.emxsys.gis.api.Part;
import com.emxsys.gis.api.Polygon;
import edu.wisc.ssec.mcidas.ConversionUtility;
import java.util.Iterator;
import java.util.List;
//import no.geosoft.cc.geometry.Geometry;
import visad.Real;

/**
 * Polygon shape geometry.
 *
 * @author Bruce Schubert
 * @version $Id$
 */
public class GeoPolygon extends AbstractGeometry implements Polygon {

    protected GeoSector extents;
    protected GeoPart part;
    private Real length;
    private Real area;

    public GeoPolygon() {
        this.part = new GeoPart();
        this.extents = new GeoSector();
    }

    /**
     * Constructs a polygon with a single ring.
     *
     * @param coords
     */
    public GeoPolygon(List<GeoCoord3D> coords) {
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
    public Real getArea() {
        if (this.area == null) {
            double[][] xy = part.toLambertAzimuthalEqualArea();
            if (xy != null) {
                throw new UnsupportedOperationException("Not implemented: need GDAL");
//                double areaSqMtr =Geometry.computePolygonArea(xy[0], xy[1]);
//                try
//                {
//                    this.area = new Real(GisType.AREA, areaSqMtr / 10000, GeneralUnit.hectare);
//                }
//                catch (VisADException ex)
//                {
//                    Exceptions.printStackTrace(ex);
//                    this.area = new Real(GisType.AREA);
//                }
            }
            else {
                this.area = new Real(GisType.AREA);
            }
        }
        return this.area;
    }

    @Override
    public double getAreaSquareMeters() {
        return this.area.getValue() * 10000;
    }

    @Override
    public Real getPerimiterLength() {
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
    public double getPerimeterLengthMeters() {
        return getPerimiterLength().getValue();
    }
    /**
     * http://paulbourke.net/geometry/polygonmesh/ Paul Bourke: Area is calculated by projecting
     * lines from each vertex to some horizontal line below the lowest part of the polygon. The
     * enclosed region from each line segment is made up of a triangle and rectangle. Sum these
     * areas together noting that the areas outside the polygon eventually cancel as the polygon
     * loops around to the beginning.
     *
     * The only restriction that will be placed on the polygon for this technique to work is that
     * the polygon must not be self intersecting.
     */
//    private void computeAreaAndPerimiter(Part part)

}
