/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
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

import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.Feature;
import com.emxsys.gis.api.Part;
import java.util.Iterator;

/**
 * GeoPointPart2D allows a GeoCoord2D to represent a Part in a point shape.
 *
 * @author Bruce Schubert
 * @version $Id: GeoPointPart2D.java 528 2013-04-18 15:04:46Z bdschubert $
 * @see Feature
 */
public class GeoPointPart2D implements Part {

    private GeoCoord2D point;

    public GeoPointPart2D() {
        this.point = GeoCoord2D.INVALID_POINT;
    }

    public GeoPointPart2D(GeoCoord2D point) {
        this.point = point;
    }

    public void setPoint(GeoCoord2D point) {
        this.point = point;
    }

    @Override
    public int getNumDimensions() {
        return 2;
    }

    @Override
    public int getNumPoints() {
        return 1;
    }

    /**
     * Returns an iterator on the coordinates. A call to next() will return an array representing
     * the point's coordinates (x,y).
     *
     * @return an iterator on the point
     */
    @Override
    public Iterable<double[]> getPoints() {
        return new Iterable<double[]>() {
            @Override
            public Iterator<double[]> iterator() {
                return new Iterator<double[]>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index == 0;
                    }

                    @Override
                    public double[] next() {
                        if (index == 0) {
                            ++index;
                            return point.getValues();
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
    public double[] getX() {
        return new double[]{
            point.getLongitudeDegrees()
        };
    }

    @Override
    public double[] getY() {
        return new double[]{
            point.getLatitudeDegrees()
        };
    }

    @Override
    public double[] getZ() {
        //throw new UnsupportedOperationException("getZ() not supported. Only two dimensions.");
        return new double[]{
            0.0
        };
    }
}
