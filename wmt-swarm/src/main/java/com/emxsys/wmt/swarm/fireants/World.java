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

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.api.services.WeatherObserver;
import com.emxsys.wmt.swarm.api.Asset;
import com.emxsys.wmt.swarm.api.Environment;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bruce Schubert
 */
public class World extends Environment {

    private GeoCoord2D nest;
    private ArrayList<FireAnt> ants = new ArrayList<>();
    
    public World() {
        // Setup environment
        setExtents(new GeoSector(
                GeoCoord2D.fromDegrees(34.4, -119.2),
                GeoCoord2D.fromDegrees(34.50, -119.4)));

        Asset asset = new Asset(Asset.Value.High, new GeoSector(
                GeoCoord2D.fromDegrees(34.49, -119.35),
                GeoCoord2D.fromDegrees(34.495, -119.36)));
        addAsset(asset);

        nest = GeoCoord2D.fromDegrees(34.41, -119.25);
        for (int i = 0; i < 10; i++) {
            ants.add(new FireAnt(new Forage(), nest, this));
}
        
        // Initialize world time to noon 
        setTime(ZonedDateTime.now().withHour(12).plusDays(1));
        
        DiurnalWeatherProvider wxProvider = getLookup().lookup(DiurnalWeatherProvider.class);
        wxProvider.initializeSunlight(getTime(), GeoCoord3D.fromCoord(nest));
        
        SpatialDomain area = SpatialDomain.from(getExtents().getSouthwest(), getExtents().getNortheast());
        TemporalDomain timeframe = TemporalDomain.from(getTime(), getTime().plusDays(2));
        setWeatherModel(wxProvider.getService(WeatherObserver.class).getObservations(area, timeframe));
        
    }

    public Coord2D getNest() {
        return nest;
    }

    public List<FireAnt> getAnts() {
        return ants;
    }
}
