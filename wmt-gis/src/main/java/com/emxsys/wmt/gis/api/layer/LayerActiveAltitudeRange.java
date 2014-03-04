/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.gis.api.layer;

import visad.Real;

/**
 * This capability provides the ability to set the range of altitudes in which a layer is active.
 * A GisLayer should add an instance of this interface to its lookup if it supports the ability to
 * be active within a range of viewer altitudes.
 *
 * @author Bruce Schubert
 * @version $Id: LayerActiveAltitudeRange.java 234 2012-10-04 21:44:23Z bdschubert $
 */
public interface LayerActiveAltitudeRange {

    /**
     * Gets the maximum altitude in which the layer is active.
     *
     * @return the maximum altitude.
     */
    Real getMaxActiveAltitude();

    /**
     * Gets the minimum altitude in which the layer is active.
     *
     * @return the minimum altitude.
     */
    Real getMinActiveAltitude();

    /**
     * Sets the maximum altitude at which the layer is active.
     *
     * @param altitude the new maximum altitude.
     */
    void setMaxActiveAltitude(Real altitude);

    /**
     * Sets the minimum altitude at which the layer is active.
     *
     * @param altitude the new minimum altitude.
     */
    void setMinActiveAltitude(Real altitude);
}
