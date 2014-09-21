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
 *     - Neither the name of Bruce Schubert,  nor the names of its 
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
package com.emxsys.wmt.swarm.fireants;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.GeoCoord2D;
import static com.emxsys.gis.api.GisType.ANGLE;
import static com.emxsys.gis.api.GisType.DISTANCE;
import com.emxsys.gis.api.Terrain;
import static com.emxsys.visad.GeneralUnit.foot;
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelModelProvider;
import com.emxsys.wildfire.api.FuelMoisture;
import static com.emxsys.wildfire.api.StdFuelMoistureScenario.VeryLowDead_FullyCuredHerb;
import com.emxsys.wildfire.behavior.SurfaceFire;
import com.emxsys.wildfire.behavior.SurfaceFireProvider;
import com.emxsys.wildfire.behavior.SurfaceFuel;
import com.emxsys.wildfire.behavior.SurfaceFuelProvider;
import com.emxsys.wildfire.spi.FuelModelProviderFactory;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.swarm.api.Agent;
import com.emxsys.wmt.swarm.api.GoalForage;
import java.time.Duration;
import java.util.List;
import static visad.CommonUnit.meter;
import visad.Real;
import visad.UnitException;

/**
 *
 * @author Bruce Schubert
 */
public class Forage extends GoalForage {

    private static final SurfaceFuelProvider fuelProvider = new SurfaceFuelProvider();
    private static final SurfaceFireProvider fireProvider = new SurfaceFireProvider();
    private static FuelModelProvider fuelModelProvider;
    private FuelModel lastFuelModel;

    public Forage() {
    }

    @Override
    public Coord2D selectDestination(Agent agent, Duration duration) {

        try {
            // Choose a direction to travel
            Real azimuth = chooseDirection();

            // Compute the distance a FireAnt agent would travel along the azimuth
            Real distance = computeRateOfSpread(agent, duration.toMillis(), azimuth);

            if (distance.getValue() <= 0) {
                return agent.getLocation();
            }

            // Get the destination end point
            return Globe.computeGreatCircleCoordinate(
                    agent.getLocation(),
                    azimuth,
                    distance);

        } catch (UnburnableException ex) {
            return GeoCoord2D.INVALID_COORD;
        } catch (UnitException ex) {
            throw new IllegalStateException(ex);
        }

    }

    @Override
    public Validation validateDestination(Agent agent, Coord2D destination) {

        if (destination.isMissing()) {
            return Validation.Invalid;
        }
        // Test if the destination is within the fireground (domain)
        Box extents = agent.getEnvironment().getExtents();
        if (!extents.contains(destination)) {
            return Validation.Invalid;
        }
        // >>>> Test for intersection with an asset (food) <<<<
        if (agent.getEnvironment().doesPointIntersectAsset(destination)) {
            return Validation.Success;
        }

        // Test for intersection with obstacle
        if (agent.getEnvironment().doesPointIntersectObstacle(destination)) {
            return Validation.Invalid;
        }

        // Test validity of fuel model at the destination 
        FuelModel fuelModel = fuelModelProvider.getFuelModel(destination);
        if (fuelModel.isBurnable()) {
            lastFuelModel = fuelModel;
            return Validation.Valid;
        } else {
            return Validation.Invalid;
        }

    }

    private Real chooseDirection() {
        return new Real(ANGLE, Math.random() * 360.);
    }

    /**
     *
     * @param millis
     * @param direction
     * @return [meters]
     * @throws UnitException
     */
    private Real computeRateOfSpread(Agent agent, long millis, Real direction) throws UnitException {
        if (fuelModelProvider == null) {
            List<FuelModelProvider> providers = FuelModelProviderFactory.getInstances();
            for (FuelModelProvider provider : providers) {
                if (provider.getClass().getName().equals("com.emxsys.wmt.landfire.Std40FuelModelProvider")) {
                    fuelModelProvider = provider;
                }
            }
            if (fuelModelProvider == null) {
                throw new IllegalStateException("com.emxsys.wmt.landfire.Std40FuelModelProvider not found.");
            }
        }
        Coord2D location = agent.getLocation();
        if (lastFuelModel == null) {
            lastFuelModel = fuelModelProvider.getFuelModel(location);
        }
        if (!lastFuelModel.isBurnable()) {
            throw new UnburnableException("Fuel model is not burnable.");
        }
        FuelMoisture fuelMoisture = VeryLowDead_FullyCuredHerb.getFuelMoisture();
        Terrain terrain = agent.getEnvironment().getTerrain(location);
        Weather weather = agent.getEnvironment().getWeather(location);
        SurfaceFuel fuel = fuelProvider.getSurfaceFuel(lastFuelModel, fuelMoisture);
        SurfaceFire fire = fireProvider.getFireBehavior(fuel, weather, terrain);
        Real ros = fire.getRateOfSpreadAtAzimuth(direction);

        // ros [ft/min]
        double distance = ros.getValue() * millis * 60000;
        return new Real(DISTANCE, foot.toThat(distance, meter));
    }

    private class UnburnableException extends RuntimeException {

        public UnburnableException(String message) {
            super(message);
        }

    }

}
