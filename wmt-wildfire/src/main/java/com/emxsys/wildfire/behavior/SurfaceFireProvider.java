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
package com.emxsys.wildfire.behavior;

import com.emxsys.gis.api.Terrain;
import com.emxsys.weather.api.Weather;
import static java.lang.Math.round;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;
import visad.Real;

/**
 *
 * @author Bruce Schubert
 */
public class SurfaceFireProvider {

    private static final Logger logger = Logger.getLogger(SurfaceFireProvider.class.getName());
    private final HashMap<FireEnvironment, SurfaceFire> fires = new HashMap<>();

    public SurfaceFireProvider() {
    }


    public SurfaceFire getFireBehavior(SurfaceFuel fuel, Weather weather, Terrain terrain) {

        FireEnvironment key = new FireEnvironment(fuel, weather, terrain);
        SurfaceFire fire = fires.get(key);
        if (fire == null) {
            fire = SurfaceFire.from(fuel, weather, terrain);
            fires.put(key, fire);
        }
        return fire;
    }

    /**
     * A simple POD structure used as a key in the 'fires' HashMap.
     */
    private class FireEnvironment {

        SurfaceFuel fuelbed;
        double windSpd;
        int windDir;
        int aspect;
        int slope;

        FireEnvironment(SurfaceFuel fuelbed, Weather weather, Terrain terrain) {
            this(fuelbed, weather.getWindSpeed(), weather.getWindDirection(), terrain.getAspect(), terrain.getSlope());
        }

        FireEnvironment(SurfaceFuel fuelbed, Real windSpd, Real windDir, Real aspect, Real slope) {
            this.fuelbed = fuelbed;
            this.windSpd = windSpd.getValue();
            this.windDir = (int) round(windDir.getValue());
            this.aspect = (int) round(aspect.getValue());
            this.slope = (int) round(slope.getValue());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.fuelbed);
            hash = 37 * hash + (int) (Double.doubleToLongBits(this.windSpd) ^ (Double.doubleToLongBits(this.windSpd) >>> 32));
            hash = 37 * hash + this.windDir;
            hash = 37 * hash + this.slope;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FireEnvironment other = (FireEnvironment) obj;
            if (!Objects.equals(this.fuelbed, other.fuelbed)) {
                return false;
            }
            if (Double.doubleToLongBits(this.windSpd) != Double.doubleToLongBits(other.windSpd)) {
                return false;
            }
            if (this.windDir != other.windDir) {
                return false;
            }
            if (this.aspect != other.aspect) {
                return false;
            }
            return this.slope == other.slope;
        }

    }

}
