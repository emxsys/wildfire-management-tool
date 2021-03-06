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

import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord2D;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.RayCastingSupport;
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
     * @see GeoCoord2D
     */
    public static Position fromCoord2D(Coord2D coord) {
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
     * @see GeoCoord3D
     */
    public static Position fromCoord3D(Coord3D coordinate) {
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
     * Converts a WorldWind Position to a GIS Coord2D
     *
     * @param position Supplies the lat, lon and elevation.
     * @return A new Coord2D with the same values as the position parameter.
     */
    public static GeoCoord2D toGeoCoord2D(Position position) {
        if (position==null){
            return GeoCoord2D.INVALID_COORD;
        }
        return GeoCoord2D.fromDegrees(
                position.getLatitude().degrees,
                position.getLongitude().degrees);
    }

    /**
     * Converts a WorldWind Position to a GIS Coord3D
     *
     * @param position Supplies the lat, lon and elevation.
     * @return A new Coord3D with the same values as the position parameter.
     */
    public static GeoCoord3D toGeoCoord3D(Position position) {
        if (position==null){
            return GeoCoord3D.INVALID_COORD;
        }
        return GeoCoord3D.fromDegreesAndMeters(
                position.getLatitude().degrees,
                position.getLongitude().degrees,
                position.getElevation());
    }

    
    /**
     * Returns a WorldWind Position from a screen point (x, y).
     * 
     * Copied from WorldWind's BasicDragger
     *
     * @param x Screen point in X dim.
     * @param y Screen point in Y dim.
     * @return a computed Position at screen x, y.
     */
    public static Position fromScreenPoint(double x, double y) {
        WorldWindow wwd = (WorldWindow) com.emxsys.wmt.globe.Globe.getInstance().getRendererComponent();
        View view = wwd.getView();
        Globe wwGlobe = view.getGlobe();
        Line ray = view.computeRayFromScreenPoint(x, y);
        Position pickPos = null;
        if (view.getEyePosition().getElevation() < wwGlobe.getMaxElevation() * 10) {
            // Use ray casting below some altitude
            // Try ray intersection with current terrain geometry
            Intersection[] intersections = wwd.getSceneController().getTerrain().intersect(ray);
            if (intersections != null && intersections.length > 0) {
                pickPos = wwGlobe.computePositionFromPoint(intersections[0].getIntersectionPoint());
            } else // Fallback on raycasting using elevation data
            {
                pickPos = RayCastingSupport.intersectRayWithTerrain(wwGlobe, ray.getOrigin(), ray.getDirection(),
                        200, 20);
            }
        }
        if (pickPos == null) {
            // Use intersection with sphere at reference altitude.
            Intersection inters[] = wwGlobe.intersect(ray, 0);
            if (inters != null) {
                pickPos = wwGlobe.computePositionFromPoint(inters[0].getIntersectionPoint());
            }
        }
        return pickPos;
    }
    
    private Positions() {
    }

}
