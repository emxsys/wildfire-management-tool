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

import com.emxsys.gis.api.layer.LayerActiveAltitudeRange;
import com.emxsys.visad.Reals;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import visad.CommonUnit;
import visad.Real;
import visad.VisADException;

/**
 * This capability class implements the ability set the min/max active altitude range for a layer. A
 * GisLayer implementation should add an instance this class to its lookup if it supports this
 * capability.
 *
 * @author Bruce Schubert
 * @version $Id: LayerActiveAltitudeCapability.java 254 2012-10-04 23:48:51Z bdschubert $
 */
public class LayerActiveAltitudeCapability implements LayerActiveAltitudeRange {

    private final Layer layer;

    public LayerActiveAltitudeCapability(final Layer layer) {
        this.layer = layer;
    }

    @Override
    public Real getMaxActiveAltitude() {
        return Reals.newAltitude(this.layer.getMaxActiveAltitude());
    }

    @Override
    public Real getMinActiveAltitude() {
        return Reals.newAltitude(this.layer.getMinActiveAltitude());
    }

    @Override
    public void setMaxActiveAltitude(Real altitude) {
        try {
            double meters = altitude.getValue(CommonUnit.meter);
            this.layer.setMaxActiveAltitude(meters);
            this.layer.firePropertyChange(AVKey.LAYER, null, layer);
        } catch (VisADException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setMinActiveAltitude(Real altitude) {
        try {
            double meters = altitude.getValue(CommonUnit.meter);
            this.layer.setMinActiveAltitude(meters);
            this.layer.firePropertyChange(AVKey.LAYER, null, layer);
        } catch (VisADException ex) {
            throw new RuntimeException(ex);
        }
    }
}
