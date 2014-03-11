/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.layers;

import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.GlobeCoordinateProvider;
import com.emxsys.wmt.globe.util.Positions;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.render.DrawContext;
import java.util.logging.Logger;

/**
 * The ReticuleLayer renders a crosshair reticule on the globe and publishes it's geographical
 * coordinate to ReticuleCoordinateEvent listeners.
 *
 * @author Bruce Schubert
 */
public class ReticuleLayer extends CrosshairLayer {

    private Position lastPosition;
    private final GlobeCoordinateProvider eventProvider;
    private static final Logger logger = Logger.getLogger(ReticuleLayer.class.getName());

    public ReticuleLayer() {
        super("com/emxsys/wmt/globe/images/32x32-crosshair-outline.png");
        eventProvider = Globe.getInstance().getLookup().lookup(GlobeCoordinateProvider.class);
        if (eventProvider == null) {
            logger.warning("A GlobeCoordinateProvider was not found in the Globe's lookup. No ReticuleChangeEvents will be fired.");
        }
    }

    @Override
    public void render(DrawContext dc) {
        super.render(dc);
        if (isEnabled()) {
            // Get the position under the crosshairs
            Position position = dc.getViewportCenterPosition();
            if (position == null || position.equals(lastPosition)) {
                return;
            }
            lastPosition = position;
            // Notify listeners
            if (eventProvider != null) {
                eventProvider.fireReticuleChange(this, Positions.toGeoCoord3D(position));
            }
        }
    }

    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        if (!enable) {
            lastPosition = Position.ZERO; // this triggers notification if re-enabled
            if (eventProvider != null) {
                eventProvider.fireReticuleChange(this, GeoCoord3D.INVALID_POSITION);
            }
        }

    }

}
