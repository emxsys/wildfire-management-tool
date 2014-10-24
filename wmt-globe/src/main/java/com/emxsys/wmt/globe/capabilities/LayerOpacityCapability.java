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
package com.emxsys.wmt.globe.capabilities;

import com.emxsys.gis.api.layer.LayerOpacity;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This capability class provides a GisLayer with the ability to adjust its transparency level. An
 * instance of this class should be added to the GisLayer's lookup if it supports this capability.
 *
 * @author Bruce Schubert
 * @version $Id: LayerOpacityCapability.java 254 2012-10-04 23:48:51Z bdschubert $
 */
public class LayerOpacityCapability implements LayerOpacity {

    private static final Logger logger = Logger.getLogger(LayerOpacityCapability.class.getName());
    private final Layer layer;

    public LayerOpacityCapability(Layer layer) {
        this.layer = layer;
    }

    @Override
    public double getOpacity() {
        return this.layer.getOpacity();
    }

    @Override
    public void setOpacity(double opacity) {
        if (opacity < 0.0 || opacity > 1.0) {
            logger.log(Level.WARNING, "setOpacity({0}) - opacity level should be between 0.0 and 1.0.", opacity);
        }
        layer.setOpacity(opacity);
        layer.firePropertyChange(AVKey.LAYER, null, layer);
    }
}
