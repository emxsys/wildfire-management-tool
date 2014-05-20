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
package com.emxsys.gis.api.layer;

/**
 * This capability provides the ability to adjust a layer's opacity within a viewer. An instance of
 * this interface should be placed in the GisLayer's lookup if it supports this capability.
 *
 * @author Bruce Schubert
 * @version $Id: LayerOpacity.java 234 2012-10-04 21:44:23Z bdschubert $
 */
public interface LayerOpacity {

    public static final double TRANSPARENT = 0.0;
    public static final double TRANSLUCENT = 0.50;
    public static final double OPAQUE = 1.0;

    /**
     * Get the opacity level.
     *
     * @return a value between 0.0 and 1.0 where 1.0 is opaque and 0.0 is transparent.
     */
    double getOpacity();

    /**
     * Set the opacity level.
     *
     * @param opacity a value between 0.0 and 1.0 where 1.0 is opaque and 0.0 is transparent.
     */
    void setOpacity(double opacity);
}
