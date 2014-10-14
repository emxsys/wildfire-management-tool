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
package com.emxsys.wmt.swarm.api;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Terrain;
import com.emxsys.gis.api.TerrainProvider;
import com.emxsys.gis.spi.TerrainProviderFactory;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.api.Weather;
import com.emxsys.weather.api.WeatherModel;
import com.emxsys.weather.api.WeatherProvider;
import com.emxsys.weather.api.services.WeatherForecaster;
import com.emxsys.weather.api.services.WeatherObserver;
import com.emxsys.wildfire.behavior.SurfaceFireProvider;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Bruce Schubert
 */
public class Environment implements Lookup.Provider {

    private final InstanceContent content = new InstanceContent();
    private Lookup lookup;
    private Box extents;
    private ZonedDateTime time;
    private TerrainProvider terrainProvider;
    private WeatherProvider weatherProvider;
    private WeatherForecaster wxForecaster;
    private WeatherModel weatherModel;
    private final ArrayList<Path> paths = new ArrayList<>();

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            // Deferred initialization
            content.add(TerrainProviderFactory.getInstance());
            content.add(new DiurnalWeatherProvider());
            content.add(new SurfaceFireProvider());

            lookup = new AbstractLookup(content);
        }
        return lookup;
    }

    public Box getExtents() {
        return this.extents;
    }

    public void setExtents(Box extents) {
        this.extents = extents;
    }

    public ZonedDateTime getTime() {
        return this.time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
        
    }

    public void addAsset(Asset asset) {
        content.add(asset);
    }

    public Collection<? extends Asset> getAssets() {
        return getLookup().lookupAll(Asset.class);
    }

    public Collection<? extends Obstacle> getObstacles() {
        return getLookup().lookupAll(Obstacle.class);
    }

    public Terrain getTerrain(Coord2D coord) {
        if (terrainProvider == null) {
            terrainProvider = getLookup().lookup(TerrainProvider.class);
        }
        return terrainProvider.getTerrain(coord);
    }

    public void setWeatherModel(WeatherModel weatherModel) {
        this.weatherModel = weatherModel;
    }

    public Weather getWeather(Coord2D coord) {
        return weatherModel.getWeather(time, coord);
    }

    public boolean doesPointIntersectAsset(Coord2D destination) {
        Collection<? extends Asset> assets = getAssets();
        for (Asset asset : assets) {
            if (asset.getGeometry().getExtents().contains(destination)) {
                // TODO - add point-in-polygon test.
                // TODO - add segment line-intersection-test
                return true;
            }
        }
        return false;            
    }

    public boolean doesPointIntersectObstacle(Coord2D destination) {
        Collection<? extends Obstacle> obstacles = getObstacles();
        for (Obstacle obstacle : obstacles) {
            if (obstacle.getGeometry().getExtents().contains(destination)) {
                // TODO - add point-in-polygon test.
                // TODO - add segment line-intersection-test
                return true;
            }
        }
        return false;
    }

    public void addPath(Path path) {
        paths.add(path);
    }
    
    public List<Path> getPaths() {
        return paths;
    }
}
