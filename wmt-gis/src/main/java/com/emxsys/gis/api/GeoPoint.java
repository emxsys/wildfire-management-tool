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

import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Part;
import com.emxsys.gis.api.Point;
import java.util.Iterator;

/**
 * A GIS 3D Geographic Point.
 *
 * @author Bruce Schubert
 * @version $Id: GeoPoint.java 528 2013-04-18 15:04:46Z bdschubert $
 */
public class GeoPoint extends AbstractGeometry implements Point {

    private GeoCoord3D position;
    private GeoSector extents;
    private GeoPositionPart part;

    public GeoPoint() {
        this(GeoCoord3D.INVALID_POSITION);
    }

    public GeoPoint(GeoCoord3D position) {
        setPosition(position);
    }

    public final void setPosition(GeoCoord3D position) {
        this.position = position;
        this.extents = new GeoSector(position, position);
        this.part = new GeoPositionPart(position);
    }

    @Override
    public GeoCoord3D getPosition() {
        return position;
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
    public int getNumParts() {
        return 1;
    }

    @Override
    public int getNumPoints() {
        return part.getNumPoints();
    }

    @Override
    public Box getExtents() {
        return this.extents;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.position != null ? this.position.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeoPoint other = (GeoPoint) obj;
        if (this.position != other.position && (this.position == null || !this.position.equals(other.position))) {
            return false;
        }
        return true;
    }
}
