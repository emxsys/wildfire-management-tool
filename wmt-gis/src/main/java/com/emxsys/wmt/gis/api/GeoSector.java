/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.gis.api;

import com.emxsys.wmt.gis.api.Box;
import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.LineString;
import com.emxsys.wmt.gis.api.Part;
import com.emxsys.wmt.gis.api.Polygon;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.Data;
import visad.Real;
import visad.RealType;
import visad.VisADException;

/**
 * This class represents the geographic extents (bounding box) defined by the SouthWest coordinates
 * (min latitude & longitude) and the NorthEast coordinates (max latitude & longitude).
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class GeoSector extends AbstractGeometry implements Box {

    private Coord2D southwest;
    private Coord2D northeast;
    private final Real ONE_HALF_LAT = new Real(RealType.Latitude, 0.5);
    private final Real ONE_HALF_LON = new Real(RealType.Longitude, 0.5);
    public static GeoSector INVALID_BOX = new GeoSector();

    /**
     * Create a sector with "missing" values.
     */
    public GeoSector() {
        this.northeast = GeoCoord2D.INVALID_POINT;
        this.southwest = GeoCoord2D.INVALID_POINT;
    }

    public GeoSector(double south, double west, double north, double east) {
        this(GeoCoord2D.fromDegrees(south, west), GeoCoord2D.fromDegrees(north, east));
    }

    public GeoSector(LineString lineString) {
        if (lineString.getNumPoints() < 2) {
            throw new IllegalArgumentException("GeoSector: lineString numPoints < 2");
        }
        initialize(lineString.getParts().iterator());
    }

    public GeoSector(Polygon polygon) {
        if (polygon.getNumPoints() < 3) {
            throw new IllegalArgumentException("GeoSector: polygon numPoints < 3");
        }
        initialize(polygon.getParts().iterator());
    }

    private void initialize(Iterator<Part> parts) {
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE;
        while (parts.hasNext()) {
            Part part = parts.next();
            Iterator<double[]> points = part.getPoints().iterator();
            while (points.hasNext()) {
                double[] point = points.next();
                maxLat = Math.max(maxLat, point[0]);
                minLat = Math.min(minLat, point[0]);
                maxLon = Math.max(maxLon, point[1]);
                minLon = Math.min(minLon, point[1]);
            }
        }
        this.southwest = GeoCoord2D.fromDegrees(minLat, minLon);
        this.northeast = GeoCoord2D.fromDegrees(maxLat, maxLon);
    }

    public GeoSector(Coord2D coord1, Coord2D coord2) {
        if (coord1 == null || coord2 == null || coord1.isMissing() || coord2.isMissing()) {
            // If invalid args then create a sector with "missing" values
            this.northeast = new GeoCoord2D();
            this.southwest = new GeoCoord2D();
            return;
        }

        // Assign the min (southwest) and max (northeast) coordinates 
        if (coord1.getLatitude().getValue() < coord2.getLatitude().getValue()
                && coord1.getLongitude().getValue() < coord2.getLongitude().getValue()) {
            // Args are in the SW/NE order
            this.southwest = coord1;
            this.northeast = coord2;
        }
        else if (coord1.getLatitude().getValue() > coord2.getLatitude().getValue()
                && coord1.getLongitude().getValue() > coord2.getLongitude().getValue()) {
            // Args are in the NE/SW order: swap the values
            this.northeast = coord1;
            this.southwest = coord2;
        }
        else {
            // Args are NW and SE: so build the SW and NE coords
            double south;
            double north;
            double west;
            double east;
            // Latitudes
            if (coord1.getLatitude().getValue() < coord2.getLatitude().getValue()) {
                south = coord1.getLatitude().getValue();
                north = coord2.getLatitude().getValue();
            }
            else {
                north = coord1.getLatitude().getValue();
                south = coord2.getLatitude().getValue();
            }
            // Longitudes
            if (coord1.getLongitude().getValue() < coord2.getLongitude().getValue()) {
                west = coord1.getLongitude().getValue();
                east = coord2.getLongitude().getValue();
            }
            else {
                east = coord1.getLongitude().getValue();
                west = coord2.getLongitude().getValue();
            }
            this.southwest = GeoCoord2D.fromDegrees(south, west);
            this.northeast = GeoCoord2D.fromDegrees(north, east);
        }
    }

    @Override
    public Coord2D getNortheast() {
        return this.northeast;
    }

    @Override
    public Coord2D getSouthwest() {
        return this.southwest;
    }

    @Override
    public Coord2D getCenter() {
        try {
            Real lat = (Real) northeast.getLatitude().add(southwest.getLatitude()).multiply(ONE_HALF_LAT);
            Real lon = (Real) northeast.getLongitude().add(southwest.getLongitude()).multiply(ONE_HALF_LON);
            System.out.println(lat.toValueString());
            System.out.println(lon.toValueString());
            return new GeoCoord2D(lat.getValue(), lon.getValue());
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getWidth() {
        try {
            // TODO: Unit tests on getWidth
            Data result = this.northeast.getLongitude().subtract(this.southwest.getLongitude());
            return (Real) result;
        }
        catch (VisADException | RemoteException ex) {
            Logger.getLogger(GeoSector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Real(RealType.Longitude);
    }

    @Override
    public Real getHeight() {
        try {
            // TODO: Unit tests on getHeight
            Data result = this.northeast.getLatitude().subtract(this.southwest.getLatitude());
            return (Real) result;
        }
        catch (VisADException | RemoteException ex) {
            Logger.getLogger(GeoSector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Real(RealType.Latitude);
    }

    public void moveTo(Coord2D center) {
        try {
            Real halfHeight = (Real) getHeight().multiply(ONE_HALF_LAT);
            Real halfWidth = (Real) getWidth().multiply(ONE_HALF_LON);
            Real north = (Real) center.getLatitude().add(halfHeight);
            Real south = (Real) center.getLatitude().subtract(halfHeight);
            Real east = (Real) center.getLongitude().add(halfWidth);
            Real west = (Real) center.getLongitude().subtract(halfWidth);
            this.northeast = new GeoCoord2D(north, east);
            this.southwest = new GeoCoord2D(south, west);
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public boolean isMissing() {
        return this.northeast.isMissing() || this.southwest.isMissing();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeoSector other = (GeoSector) obj;
        if (!Objects.equals(this.southwest, other.southwest)) {
            return false;
        }
        if (!Objects.equals(this.northeast, other.northeast)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash
                + (this.southwest != null ? this.southwest.hashCode() : 0)
                + (this.northeast != null ? this.northeast.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "GeoSector{" + "SW=" + southwest + ", NE=" + northeast + '}';
    }

    @Override
    public boolean intersects(Box other) {
        return this.contains(other.getSouthwest()) || this.contains(other.getNortheast())
                || other.contains(this.getSouthwest()) || other.contains(this.getNortheast())
                // other crosses this veritically
                || (this.southwest.getLatitudeDegrees() >= other.getSouthwest().getLatitudeDegrees()
                && this.northeast.getLatitudeDegrees() <= other.getNortheast().getLatitudeDegrees()
                && this.southwest.getLongitudeDegrees() <= other.getSouthwest().getLongitudeDegrees()
                && this.northeast.getLongitudeDegrees() >= other.getNortheast().getLongitudeDegrees()
                // other crosses this horizontally
                || (this.southwest.getLatitudeDegrees() <= other.getSouthwest().getLatitudeDegrees()
                && this.northeast.getLatitudeDegrees() >= other.getNortheast().getLatitudeDegrees()
                && this.southwest.getLongitudeDegrees() >= other.getSouthwest().getLongitudeDegrees()
                && this.northeast.getLongitudeDegrees() <= other.getNortheast().getLongitudeDegrees()));
    }

    @Override
    public boolean contains(Box other) {
        return contains(other.getSouthwest()) && contains(other.getNortheast());
    }

    @Override
    public boolean contains(Coord2D other) {
        double lat = other.getLatitudeDegrees();
        double lon = other.getLongitudeDegrees();
        return (lat >= this.southwest.getLatitudeDegrees()
                && lat <= this.northeast.getLatitudeDegrees()
                && lon >= this.southwest.getLongitudeDegrees()
                && lon <= this.northeast.getLongitudeDegrees());
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
                            ArrayList<GeoCoord3D> coords = new ArrayList<>(5);
                            coords.add(GeoCoord3D.fromDegrees(southwest.getLatitudeDegrees(), southwest.getLongitudeDegrees()));
                            coords.add(GeoCoord3D.fromDegrees(northeast.getLatitudeDegrees(), southwest.getLongitudeDegrees()));
                            coords.add(GeoCoord3D.fromDegrees(northeast.getLatitudeDegrees(), northeast.getLongitudeDegrees()));
                            coords.add(GeoCoord3D.fromDegrees(southwest.getLatitudeDegrees(), northeast.getLongitudeDegrees()));
                            coords.add(GeoCoord3D.fromDegrees(southwest.getLatitudeDegrees(), southwest.getLongitudeDegrees()));
                            return new GeoPart(coords);
                        }
                        throw new ArrayIndexOutOfBoundsException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported.");
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
        return 5;
    }

    @Override
    public Box getExtents() {
        return this;
    }

    /**
     * Returns a Rectangle2D in geographic coordinates
     *
     * @return a new Rectangle2D.Double
     */
    static public Rectangle2D toRectangle2D(Box box) {
        return new Rectangle2D.Double(
                box.getSouthwest().getLongitudeDegrees(), // west = min  x
                box.getSouthwest().getLatitudeDegrees(), // north = min y
                box.getWidth().getValue(),
                box.getHeight().getValue());
    }
}
