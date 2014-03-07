/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.util;

import com.emxsys.wmt.gis.GeoCoord3D;
import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.Coord3D;
import gov.nasa.worldwind.geom.Position;
import org.openide.util.Exceptions;

/**
 * Positions utility class used for converting to/from WorldWind Positions.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class Positions {

    /**
     * Converts a GIS Coord2D to a WorldWind Position.
     *
     * @param coord Supplies lat/lon.
     * @return A position with a ZERO elevation.
     */
    public static Position fromGeoPoint(Coord2D coord) {
        try {
            if (coord == null || coord.isMissing()) {
                return Position.ZERO;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
        return Position.fromDegrees(coord.getLatitudeDegrees(), coord.getLongitudeDegrees());
    }

    /**
     * Converts a GIS Coord3D to a WorldWind Position
     *
     * @param coordinate Supplies lat, lon and latitude
     * @return A new position with the same values as the coordinate parameter.
     */
    public static Position fromGeoPosition(Coord3D coordinate) {
        try {
            if (coordinate == null || coordinate.isMissing()) {
                return Position.ZERO;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
        return Position.fromDegrees(
                coordinate.getLatitudeDegrees(),
                coordinate.getLongitudeDegrees(),
                coordinate.getAltitudeMeters());
    }

    /**
     * Converts a WorldWind Position to a GIS Coord3D
     *
     * @param position Supplies the lat, lon and elevation.
     * @return A new Coord3D with the same values as the position parameter.
     */
    public static GeoCoord3D toGeoPosition(Position position) {
        return GeoCoord3D.fromDegreesAndMeters(
                position.getLatitude().degrees,
                position.getLongitude().degrees,
                position.getElevation());
    }

    private Positions() {
    }

}
