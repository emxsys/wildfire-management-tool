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
package com.emxsys.wmt.cps.layers;

import com.emxsys.gis.api.Coord2D;
import static com.emxsys.gis.api.GisType.ANGLE;
import com.emxsys.gis.api.layer.BasicLayerCategory;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.BasicLayerType;
import com.emxsys.util.AngleUtil;
import com.emxsys.wildfire.behavior.FireEllipse;
import com.emxsys.wildfire.behavior.FireReaction;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.layers.RenderableGisLayer;
import com.emxsys.wmt.globe.render.GlobeEllipse;
import java.time.Duration;
import org.openide.windows.WindowManager;
import visad.Real;

/**
 * The class is renderable representative of a fire's shape.
 *
 * @author Bruce Schubert
 */
public class FireShape {

    // An ellipse is used to represent a wind-driven fire.
    private final GlobeEllipse renderable;
    private RenderableGisLayer layer;

    public FireShape() {
        this.renderable = new GlobeEllipse();
    }

    /**
     * Draws the projected shape from the supplied point of origin after a given duration.
     *
     * @param origin The point of origin for the fire.
     * @param fire The fire behavior.
     * @param duration The time duration.
     */
    public void update(Coord2D origin, FireReaction fire, Duration duration) {
        initialize();
        WindowManager.getDefault().invokeWhenUIReady(() -> {

            // Get the fire's shape after the elapsed duration.
            FireEllipse ellipse = fire.getFireEllipse(duration);

            // Translate the center of the ellipse along the direction of spread so that the
            // ellipse's point of origin ends up located at the given coordinate.
            Real distance = Globe.computeAngularDistance(ellipse.getOriginOffsetFromCenter());
            Coord2D ellipseCenter = Globe.computeGreatCircleCoordinate(origin,
                    ellipse.getHeading(), distance);

            // Draw the renderable
            renderable.update(ellipseCenter,
                    ellipse.getMajorRadius(),
                    ellipse.getMinorRadius(),
                    new Real(ANGLE, AngleUtil.normalize360(ellipse.getHeading().getValue() + 90)));
        });
    }

    void initialize() {
        if (layer == null) {
            // Add the layer the globe. But defer creating the layer until Globe 
            // has been initialized, else WorldWind configs are read from native WW.
            WindowManager.getDefault().invokeWhenUIReady(() -> {
                layer = new RenderableGisLayer("Fire Shape", BasicLayerGroup.Overlay, BasicLayerType.Other, BasicLayerCategory.Other);
                layer.addRenderable(renderable);
                Globe.getInstance().addGisLayer(layer);
            });
        }
    }

}
