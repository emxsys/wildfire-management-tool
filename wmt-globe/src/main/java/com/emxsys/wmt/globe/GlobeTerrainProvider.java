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
package com.emxsys.wmt.globe;

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.ShadedTerrainProvider;
import com.emxsys.wmt.gis.api.Terrain;
import com.emxsys.wmt.gis.api.TerrainTuple;
import com.emxsys.wmt.globe.util.Positions;
import com.emxsys.wmt.solar.api.SolarUtil;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;
import java.util.Date;
import java.util.logging.Logger;
import visad.Real;
import visad.RealType;

//TODO: Implement hi-res terrain and shading tests.
/**
 * GlobeTerrainProvider is concrete TerrainProvider based on the WorldWind globe elevation model.
 *
 * @author Bruce Schubert
 */
public class GlobeTerrainProvider implements ShadedTerrainProvider {

    private final HighResolutionTerrain terrain;
    /** WorldWind globe provides the elevation model */
    private static gov.nasa.worldwind.globes.Globe globe;
    /** The vertical resolution (meters) */
    private static final Double TARGET_RESOLUTION = 20d; // meters, or null for globe's highest resolution
    /** The size of the Terrain's cache. */
    private static final long CACHE_SIZE = (long) 150e6;
    private static final Logger logger = Logger.getLogger(GlobeTerrainProvider.class.getName());

    public GlobeTerrainProvider() {
        // Create a Terrain object that uses high-resolution elevation data to compute intersections.
        this.terrain = new HighResolutionTerrain(lookupGlobe(), TARGET_RESOLUTION);
        this.terrain.setCacheCapacity(CACHE_SIZE); // larger cache speeds up repeat calculations
    }

    private gov.nasa.worldwind.globes.Globe lookupGlobe() {
        if (globe == null) {
            WorldWindow wwd = Globe.getInstance().getWorldWindManager().getWorldWindow();
            if (wwd != null) {
                globe = wwd.getModel().getGlobe();
            }
        }
        return globe;
    }

    private HighResolutionTerrain getHighResolutionTerrain() {
        return this.terrain;
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
        gov.nasa.worldwind.globes.Globe g = lookupGlobe();
        if (g != null) {
            // Convert from VisAD to WW lat/lon
            LatLon latLon = LatLon.fromDegrees(
                    coord.getLatitude().getValue(),
                    coord.getLongitude().getValue());

            // Determine slope and aspect at lat/lon
            Angle[] angles = computeSlopeAndAspect(latLon);
            Angle slope = angles[0];
            Angle aspect = angles[1];
            Real elevation = getElevation(coord);

            return new TerrainTuple(aspect.degrees, slope.degrees, elevation.getValue());
        } else {
            // Globe is null; return a tuple with missing values.
            return new TerrainTuple();
        }
    }

    @Override
    public boolean isCoordinateTerrestialShaded(Coord2D coord, Date datetime) {
        Position positionA = Positions.fromCoord2D(coord);
        Position positionB = Positions.fromCoord2D(SolarUtil.getSunPosition(datetime));
        Intersection[] intersect = terrain.intersect(positionA, positionB);

        // The position is shaded if there is an interesection with the terrain between the two positions
        return intersect != null;
    }

    private double getBestElevation(LatLon latLon) {
        return getFastElevation(latLon);
//        // First, lookup hi resolution terrain, then fallback on view altitude-sensitive elevation
//        Double elevation = terrain.getElevation(latLon);    // does this block?
//        if (elevation == null)
//        {
//            elevation = getFastElevation(latLon);
//        }
//        return elevation.doubleValue();
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
