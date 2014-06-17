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
package com.emxsys.wmt.globe.terrain;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import static com.emxsys.gis.api.GisType.DISTANCE;
import com.emxsys.gis.api.ShadedTerrainProvider;
import com.emxsys.gis.api.Terrain;
import com.emxsys.gis.api.TerrainTuple;
import com.emxsys.wmt.globe.Globe;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;
import static java.lang.Math.PI;
import static java.lang.Math.tan;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;
import static visad.CommonUnit.degree;
import static visad.CommonUnit.meter;
import static visad.CommonUnit.radian;
import visad.Real;
import visad.RealType;
import visad.VisADException;

/**
 * GlobeTerrainProvider is concrete TerrainProvider based on the WorldWind globe elevation model.
 *
 * @author Bruce Schubert
 */
@ServiceProvider(service = ShadedTerrainProvider.class)
public class GlobeTerrainProvider implements ShadedTerrainProvider {

    private HighResolutionTerrain hiResTerrain;
    /** WorldWind globe provides the elevation model */
    private static gov.nasa.worldwind.globes.Globe globe;
    /** The vertical resolution (meters) */
    private static final Double TARGET_RESOLUTION = null; // meters, or null for globe's highest resolution
    /** The size of the Terrain's cache. */
    private static final long CACHE_SIZE = (long) 150e6;
    private static final Logger logger = Logger.getLogger(GlobeTerrainProvider.class.getName());

    public GlobeTerrainProvider() {
    }

    private HighResolutionTerrain getHiResTerrain() {
        // Deferred initialization
        if (hiResTerrain == null) {
            // Create a Terrain object that uses high-resolution elevation data to compute intersections.
            this.hiResTerrain = new HighResolutionTerrain(getGlobe(), TARGET_RESOLUTION);
            this.hiResTerrain.setCacheCapacity(CACHE_SIZE); // larger cache speeds up repeat calculations
        }
        return this.hiResTerrain;
    }

    private gov.nasa.worldwind.globes.Globe getGlobe() {
        if (globe == null) {
            WorldWindow wwd = Globe.getInstance().getWorldWindManager().getWorldWindow();
            if (wwd != null) {
                globe = wwd.getModel().getGlobe();
            }
        }
        return globe;
    }

    @Override
    public Real getElevation(Coord2D coord) {
        if (coord.isMissing()) {
            return new Real(RealType.Altitude);
        }
        double bestElevation = getBestElevation(LatLon.fromDegrees(coord.getLatitudeDegrees(), coord.getLongitudeDegrees()));
        return new Real(RealType.Altitude, bestElevation);
    }

    /**
     * Creates a VisAD based {@link TerrainTuple} from the WorldWind terrain model.
     * @param coord the coordinate where terrain should be determined
     * @return the terrain's aspect, slope and elevation at supplied location
     */
    @Override
    public Terrain getTerrain(Coord2D coord) {
        gov.nasa.worldwind.globes.Globe g = getGlobe();
        // The globe must be available to compute slope and aspect
        if (g != null) {
            // Convert from VisAD to WW lat/lon
            LatLon latLon = LatLon.fromDegrees(
                    coord.getLatitude().getValue(),
                    coord.getLongitude().getValue());

            // Determine slope and aspect at lat/lon
            Angle[] angles = computeSlopeAndAspect(latLon);
            Angle slope = angles[0];
            Angle aspect = angles[1];
            // Get the elevation 
            Real elevation = getElevation(coord);

            return new TerrainTuple(aspect.degrees, slope.degrees, elevation.getValue());
        } else {
            // Globe is null; return a tuple with missing values.
            return new TerrainTuple();
        }
    }

    @Override
    public boolean isCoordinateTerrestialShaded(Coord3D coord, Real azimuth, Real zenith) {
        try {
            if (coord.isMissing() || azimuth.isMissing() || zenith.isMissing()) {
                logger.log(Level.WARNING, "Illegal argument(s) in isCoordinateTerrestialShaded({0}, {1}, {2})", new Object[]{coord, azimuth, zenith});
                return false;
            }

            // Is sun below the horizon?
            if (zenith.getValue(degree) > 90) {
                return true;
            }
            // Compute the position of object that would obscure the sun at fixed distance from the coord.
            // Set distance to one nautical mile, e.g., one minute of latitude.
            final Real distance = new Real(DISTANCE, 1852);
            Coord2D endPos = Globe.computeGreatCircleCoordinate(coord, azimuth, distance);

            // Compute the height of a fake Sun object that would obscure the real Sun at the 
            // end positon--ignoring the curvature of earth (negligable over short distances).
            double tanAltitudeAngle = tan(PI / 2 - zenith.getValue(radian));
            double distanceMeters = Globe.computeGreatCircleDistance(coord, endPos).getValue(meter);
            double heightMeters = tanAltitudeAngle * distanceMeters; // height of fake Sun

            // Determine position offsets above the terrain
            LatLon latlonA = LatLon.fromDegrees(coord.getLatitudeDegrees(), coord.getLongitudeDegrees());
            LatLon latlonB = LatLon.fromDegrees(endPos.getLatitudeDegrees(), endPos.getLongitudeDegrees());
            double elevA = getBestElevation(latlonA);
            double elevB = getBestElevation(latlonB);
            double offsetA = Math.max(coord.getAltitudeMeters() - elevA, 0);
            double offsetB = Math.max(heightMeters - elevB, 0);  // AGL of fake Sun 

            // Test for intersecting terrain
            Position positionA = new Position(latlonA, offsetA);
            Position positionB = new Position(latlonB, offsetB);
            Intersection[] intersect = getHiResTerrain().intersect(positionA, positionB);

            // The position is shaded if there is an intersection with the terrain between the two positions.
            return intersect != null;

        } catch (VisADException ex) {
            logger.warning(ex.getMessage());
            return false;
        }
    }

    private double getBestElevation(LatLon latLon) {
//        return getFastElevation(latLon);
        // First, lookup hi resolution terrain, then fallback on view altitude-sensitive elevation
        Double elevation = getHiResTerrain().getElevation(latLon);    // does this block?
        if (elevation == null) {
            elevation = getFastElevation(latLon);
        }
        return elevation;
    }

    private double getFastElevation(LatLon latLon) {
        return globe.getElevation(latLon.latitude, latLon.longitude);
    }

    /**
     * Computes the slope and aspect of a position on the globe. Accuracy is dependent on the
     * globe's elevation model, which is low resolution when "zoomed out". Accuracy is better when
     * zoomed in.
     *
     * @param position where slope and aspect are determined.
     * @return [slope, aspect] Angle array.
     */
    private Angle[] computeSlopeAndAspect(LatLon position) {

        // Establish three points that define a triangle around the center position
        // to be used for determining the slope and aspect of the terrain (roughly 10 meters per side)
        LatLon n1 = LatLon.rhumbEndPosition(position, Angle.ZERO, Angle.fromDegrees(-0.00005)); // due south
        LatLon n2 = LatLon.rhumbEndPosition(n1, Angle.fromDegrees(-60), Angle.fromDegrees(0.0001)); // northwest
        LatLon n3 = LatLon.rhumbEndPosition(n1, Angle.fromDegrees(60), Angle.fromDegrees(0.0001)); // northeast

        // Get the cartesian coords for the points
        Vec4 p1 = globe.computePointFromPosition(new Position(n1, getBestElevation(n1)));
        Vec4 p2 = globe.computePointFromPosition(new Position(n2, getBestElevation(n2)));
        Vec4 p3 = globe.computePointFromPosition(new Position(n3, getBestElevation(n3)));

        // Compute an upward pointing normal of the triangle and other essential vectors
        Vec4 terrainNormal = p1.subtract3(p3).cross3(p1.subtract3(p2)).normalize3();
        Vec4 surfaceNormal = globe.computeSurfaceNormalAtLocation(position.getLatitude(), position.getLongitude());
        Vec4 north = globe.computeNorthPointingTangentAtLocation(position.getLatitude(), position.getLongitude());

        // Compute terrain slope -- the delta between surface normal and terrain normal
        Angle slope = terrainNormal.angleBetween3(surfaceNormal);

        //System.out.println("Slope: " + slope);
        // Compute the terrain aspect -- get a perpendicular vector projected onto
        // surface normal which is in the same plane as the north vector. Get delta
        // with north vector and use  dot product to determine aspect angle's sign (+/- 180)
        Vec4 perpendicular = terrainNormal.perpendicularTo3(surfaceNormal);
        Angle aspect = perpendicular.angleBetween3(north);
        double direction = Math.signum(-surfaceNormal.cross3(north).dot3(perpendicular));
        aspect = aspect.multiply(direction);

        //System.out.println("Aspect: " + aspect);
        return new Angle[]{
            slope, aspect
        };
    }

}
