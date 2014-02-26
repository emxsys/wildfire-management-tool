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
package com.emxsys.wmt.gis;

import com.emxsys.wmt.gis.api.Part;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.openide.util.Exceptions;
import visad.CoordinateSystem;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;
import visad.data.hdfeos.LambertAzimuthalEqualArea;


/**
 * The GeoPart class can represent the point sequence for LineString or a Polygon linear ring.
 */
public class GeoPart implements Part
{
    ArrayList<GeoCoord3D> coords = new ArrayList<>();
    private double[] xx;
    private double[] yy;
    private double[] zz;


    GeoPart()
    {
    }


    GeoPart(Collection<GeoCoord3D> coords)
    {
        this.coords.addAll(coords);
    }


    @Override
    public int getNumDimensions()
    {
        return 3;
    }


    @Override
    public int getNumPoints()
    {
        return this.coords.size();
    }


    /**
     * Returns an iterator on the coordinates. A call to next() will return an array representing
     * the next point's coordinates (x,y,z).
     *
     * @return an iterator on the point
     */
    @Override
    public Iterable<double[]> getPoints()
    {
        return new Iterable<double[]>()
        {
            @Override
            public Iterator<double[]> iterator()
            {
                return new Iterator<double[]>()
                {
                    private int index = 0;


                    @Override
                    public boolean hasNext()
                    {
                        return index < coords.size();
                    }


                    /**
                     * Returns an array of coordinates in [lat, lon, alt] order.
                     *
                     * @return double [latitude, longitude, altitude]
                     */
                    @Override
                    public double[] next()
                    {
                        if (hasNext())
                        {
                            return coords.get(index++).getValues();
                        }
                        throw new ArrayIndexOutOfBoundsException();
                    }


                    @Override
                    public void remove()
                    {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        };
    }


    /**
     *
     * @return array of longitude values
     */
    @Override
    public double[] getX()
    {
        if (this.xx == null)
        {
            initXYZ();
        }
        return xx;
    }


    /**
     *
     * @return array of latitude values
     */
    @Override
    public double[] getY()
    {
        if (this.yy == null)
        {
            initXYZ();
        }
        return yy;
    }


    /**
     *
     * @return array of altitude values
     */
    @Override
    public double[] getZ()
    {
        if (this.zz == null)
        {
            initXYZ();
        }
        return zz;
    }


    private void initXYZ()
    {
        xx = new double[this.getNumPoints()];
        yy = new double[this.getNumPoints()];
        zz = new double[this.getNumPoints()];
        int i = 0;
        for (GeoCoord3D coord : this.coords)
        {
            double[] values = coord.getValues();
            xx[i] = values[GeoCoord3D.LON_TUPLE_INDEX];
            yy[i] = values[GeoCoord3D.LAT_TUPLE_INDEX];
            zz[i] = values[GeoCoord3D.ALT_TUPLE_INDEX];
            ++i;
        }
    }


    /**
     * Get an array of Cartesian coordinates in a Lambert Azimuthal Equal Area projection. Useful for computing
     * the area of a polygon.
     * @return array of double[dim][numPoints] in an equal area projection, null on error.
     */
    public double[][] toLambertAzimuthalEqualArea()
    {
        double[] xx = getX();   // longitude
        double[] yy = getY();   // latitude
        double[][] values_in = new double[2][xx.length];
        try
        {
            for (int i = 0; i < xx.length; i++)
            {
                values_in[0][i] = xx[i];// * Data.DEGREES_TO_RADIANS;
                values_in[1][i] = yy[i];// * Data.DEGREES_TO_RADIANS;
            }

            // Use the first coordinate as the center of the projection
            double lon_center = values_in[0][0];
            double lat_center = values_in[1][0];
            RealType reals[] =
            {
                RealType.Longitude, RealType.Latitude
            };
            RealTupleType Reference = new RealTupleType(reals);
            CoordinateSystem lamaz_cs =
                new LambertAzimuthalEqualArea(Reference, lon_center, lat_center);

            // Convert from Reference (Lon,Lat) type
            double[][] values_out = lamaz_cs.fromReference(values_in);

            return values_out;
        }
        catch (VisADException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
