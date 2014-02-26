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
package com.emxsys.wmt.gis.api;

import visad.Real;
import visad.georef.LatLonPoint;


/**
 * A 2D geographic coordinate.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public interface Coord2D extends LatLonPoint
{

    /**
     * Get the latitude of this point
     *
     * @return Real representing the latitude
     */
    @Override
    Real getLatitude();


    /**
     * Get the longitude of this point
     *
     * @return Real representing the longitude
     */
    @Override
    Real getLongitude();


    /**
     * Get the latitude of this point
     *
     * @return double representing the latitude in degrees.
     */
    double getLatitudeDegrees();


    /**
     * Get the longitude of this point
     *
     * @return double representing the longitude in degrees.
     */
    double getLongitudeDegrees();


    /**
     * See if this geographic coordinate is equal to the object in question. Two points are equal if
     * they are the same object, or if their lat/lon components are equal.
     *
     * @param obj object in question
     */
    @Override
    boolean equals(Object obj);


     /**
     * Tests whether any data elements are missing.
     *
     * @return true if a data element is missing.
     */
    @Override
    boolean isMissing();
}
