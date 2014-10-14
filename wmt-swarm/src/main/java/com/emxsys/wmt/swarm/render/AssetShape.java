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
package com.emxsys.wmt.swarm.render;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Geometry;
import com.emxsys.gis.api.layer.BasicLayerCategory;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.BasicLayerType;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.layers.RenderableGisLayer;
import com.emxsys.wmt.globe.render.GlobePolygon;
import com.emxsys.wmt.globe.render.GlobeSector;
import com.emxsys.wmt.swarm.api.Asset;
import org.openide.windows.WindowManager;

/**
 * The class is renders an Asset's geometry.
 *
 * @author Bruce Schubert
 */
public class AssetShape {

    // An ellipse is used to represent a wind-driven fire.
    private final GlobeSector renderable;
    private RenderableGisLayer layer;

    public AssetShape() {
        this.renderable = new GlobeSector();
    }

    /**
     * Draws the projected shape from the supplied point of origin after a given duration.
     *
     * @param origin The point of origin for the fire.
     * @param fire The fire behavior.
     * @param duration The time duration.
     */
    public void update(Asset asset) {
        initialize();
        WindowManager.getDefault().invokeWhenUIReady(() -> {

            // Get the asset's shape
            Geometry geometry = asset.getGeometry();
            Box extents = geometry.getExtents();
            // Create the renderable
            renderable.setSector(extents);
        });
    }

    void initialize() {
        if (layer == null) {
            // Add the layer the globe. But defer creating the layer until the Globe 
            // has been initialized, else WorldWind configs are read from native WW 
            // instead of XML Layer specs.
            WindowManager.getDefault().invokeWhenUIReady(() -> {
                layer = new RenderableGisLayer("Assets", BasicLayerGroup.Overlay, BasicLayerType.Other, BasicLayerCategory.Other);
                layer.addRenderable(renderable);
                Globe.getInstance().addGisLayer(layer);
            });
        }
    }

}
