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

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GeoLineString;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bruce Schubert
 */
public class Path extends GeoLineString {

    private int pheromoneLevel = 0;
    private Duration duration;
    
    public Path() {
    }

    public Path(List<Coord2D> coords, Duration duration) {
        super(toGeoCoord3D(coords));
        this.duration = duration;
    }

    static List<GeoCoord3D> toGeoCoord3D(List<Coord2D> coords) {
        ArrayList<GeoCoord3D> list = new ArrayList<>(coords.size());
        coords.stream().forEach((coord2D) -> {
            list.add(GeoCoord3D.fromCoord(coord2D));
        });
        return list;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        double meters = getLengthMeters();
        return "Path{" + "length=" + meters + 'm' + " duration=" + duration.toMinutes() + '}';
    }
    
}