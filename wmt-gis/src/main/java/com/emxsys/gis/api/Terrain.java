/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.gis.api;

import visad.Real;

/**
 * A Terrain instance provides the aspect, slope and elevation at a given coordinate.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public interface Terrain {

    /**
     * Aspect is direction that non-flat terrain faces. It is the down-slope direction relative to
     * true north.
     * @return [degrees]
     */
    Real getAspect();

    /**
     * Get the aspect in degrees.
     * @return [degrees]
     */
    double getAspectDegrees();

    /**
     * Get the aspect as a cardinal point.
     * @return
     */
    CardinalPoint8 getAspectCardinalPoint8();

    /**
     * Slope is the steepness of the terrain. Zero slope is horizontal, 90 degrees is vertical.
     * @return [degrees]
     */
    Real getSlope();

    /**
     * Get the slope in degrees (0=no slope, 90 = straight up).
     * @return [degrees]
     */
    double getSlopeDegrees();

    /**
     * Get the slope in percent (0=no slope, 100% = 45 deg).
     * @return [degrees]
     */
    double getSlopePercent();

    /**
     * Elevation is the height of the terrain.
     * @return [meters]
     */
    Real getElevation();

    /**
     * Get the elevation in meters.
     * @return [meters]
     */
    double getElevationMeters();

    /**
     * Get the elevation in feet.
     * @return [feet]
     */
    double getElevationFeet();

}
