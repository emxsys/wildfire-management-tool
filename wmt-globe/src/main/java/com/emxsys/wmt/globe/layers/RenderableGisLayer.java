/*
 * Copyright (c) 2014, Bruce Schubert. <bruce@emxsys.com>
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

import com.emxsys.gis.api.layer.LayerCategory;
import com.emxsys.gis.api.layer.LayerGroup;
import com.emxsys.gis.api.layer.LayerType;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A Renderable GisLayer for displaying WorldWind Renderables.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class RenderableGisLayer extends GisLayerProxy {

    private static final Logger logger = Logger.getLogger(RenderableGisLayer.class.getName());

    public RenderableGisLayer() {
        super(new RenderableLayer());
    }

    public RenderableGisLayer(String name) {
        super(new RenderableLayer());
        setName(name);
    }

    public RenderableGisLayer(String name, LayerGroup group, LayerType type, LayerCategory category) {
        super(new RenderableLayer(), type, group, category);
        setName(name);
    }

    public void addRenderable(Renderable renderable) {
        RenderableLayer impl = (RenderableLayer) getLayerImpl();
        impl.addRenderable(renderable);
    }

    public void addRenderables(Collection<? extends Renderable> renderables) {
        RenderableLayer impl = (RenderableLayer) getLayerImpl();
        impl.addRenderables(renderables);
    }

    public void clearRenderables() {
        RenderableLayer impl = (RenderableLayer) getLayerImpl();
        impl.removeAllRenderables();
    }

    public void removeRenderable(Renderable renderable) {
        RenderableLayer impl = (RenderableLayer) getLayerImpl();
        impl.removeRenderable(renderable);
    }
}
