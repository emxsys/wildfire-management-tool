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
package com.emxsys.wmt.landfire.layers;

import com.emxsys.gis.api.layer.LayerCategory;


/**
 * LandfireLayerCategory defines the GisLayer categories for LANDFIRE products.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public enum LandfireLayerCategory implements LayerCategory {

    /**
     * Fuel model products. These data can be implemented within models to predict wildland fire
     * behavior and effects, and are useful for strategic fuel treatment prioritization and tactical
     * assessment of fire behavior and effects.
     */
    Fuels,
    /**
     * Existing and potential vegetation products. Vegetation products are used in change detection
     * analysis, for natural resource management, to identify the nation’s major ecosystems, to
     * inventory wildlife habitat.
     */
    Vegetation,
    /**
     * Slope, aspect and elevation products.
     */
    Topographic,
    /**
     * Fire regime products are used for landscape assessments, comparisons of historical to current
     * conditions, and to identify landscape management priorities.
     */
    FireRegimes,
    /**
     * The disturbance products allow LANDFIRE products to remain current and relevant. These data
     * are useful in change detection analysis and other analysis that requires up-to-date land
     * cover information.
     */
    Disturbance;

    @Override
    public String getName() {
        return toString();
    }
}
