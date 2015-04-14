/*
 * Copyright (c) 2015, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.solar.api;

import com.emxsys.gis.api.Coord3D;
import java.time.ZonedDateTime;

/**
 *
 * @author Bruce Schubert
 * @version $Id$
 */
public class SunlightObservation {

    private final ZonedDateTime time;
    private final Coord3D coord;
    private final Sunlight sunlight;

    /**
     *
     * @param time The observation time.
     * @param coord The location of the observer.
     * @param sunlight The sunlight observation for the specified time and location.
     */
    public SunlightObservation(ZonedDateTime time, Coord3D coord, Sunlight sunlight) {
        this.time = time;
        this.coord = coord;
        this.sunlight = sunlight;
    }

    /**
     * Get the date/time of the observation.
     *
     * @return The observation date/time.
     */
    public ZonedDateTime getTime() {
        return time;
    }

    /**
     * Get the location of the observer.
     *
     * @return The coordinates of the observer.
     */
    public Coord3D getCoord() {
        return coord;
    }

    /**
     * Get the sunlight 
     * @return The sunlight values at the specified time and location.
     */
    public Sunlight getSunlight() {
        return sunlight;
    }
    
    

}
