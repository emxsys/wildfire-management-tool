/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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

import com.emxsys.wmt.gis.api.viewer.Viewers;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.gis.api.layer.LayerCategory;
import com.emxsys.wmt.gis.api.layer.LayerOpacity;
import com.emxsys.wmt.gis.api.layer.LayerGroup;
import com.emxsys.wmt.gis.api.viewer.GisViewer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;

/**
 * Utility class for working with Layers.
 *
 * @author Bruce Schubert
 */
@Messages({
    "ERR_NullGisViewer=GisViewer cannot be null."
})
public class Layers {

    private static final Logger logger = Logger.getLogger(Layers.class.getName());

    private Layers() {
    }

    /**
     * Disable each GisLayer that matches the LayerCategory within the active GisViewer's lookup.
     *
     * @param clazz a class implementing the LayerCategory interface.
     * @see GisLayer
     * @see GisViewer
     */
    public static void disableLayersInCategory(Class<? extends LayerCategory> clazz) {
        GisViewer viewer = Viewers.getPrimaryViewer();
        if (viewer == null) {
            throw new IllegalStateException(Bundle.ERR_NullGisViewer());
        }
        viewer.getGisLayerList().disableLayersInCategory(clazz);
    }

    public static void enableLayerInGroupExclusive(String layerName, LayerGroup layerGroup) {
        GisViewer viewer = Viewers.getPrimaryViewer();
        if (viewer == null) {
            throw new IllegalStateException(Bundle.ERR_NullGisViewer());
        }
        viewer.getGisLayerList().enableLayerInGroupExclusive(layerName, layerGroup);
    }

    public static GisLayer findLayer(String layerName) {
        GisViewer viewer = Viewers.getPrimaryViewer();
        if (viewer == null) {
            throw new IllegalStateException(Bundle.ERR_NullGisViewer());
        }
        return viewer.getGisLayerList().find(layerName);
    }

    public static List<GisLayer> getLayersInCategory(LayerCategory category) {
        GisViewer viewer = Viewers.getPrimaryViewer();
        if (viewer == null) {
            throw new IllegalStateException(Bundle.ERR_NullGisViewer());
        }
        return viewer.getGisLayerList().getLayersInCategory(category);
    }

    public static List<GisLayer> getLayersInGroup(LayerGroup group) {
        GisViewer viewer = Viewers.getPrimaryViewer();
        if (viewer == null) {
            throw new IllegalStateException(Bundle.ERR_NullGisViewer());
        }
        return viewer.getGisLayerList().getLayersInGroup(group);
    }

    public static List<GisLayer> getLayersWithAttribute(Class<? extends Object> clazz) {
        GisViewer viewer = Viewers.getPrimaryViewer();
        if (viewer == null) {
            throw new IllegalStateException(Bundle.ERR_NullGisViewer());
        }
            return viewer.getGisLayerList().getLayersWithAttribute(clazz);
    }

    /**
     * Sets a layer's opacity level if the layer supports this capability. The layer must implement
     * the LayerOpacity interface or have an implementation of it in its lookup. This will method
     * fail silently if the supplied layer does not support opacity.
     *
     * @param layer the layer to set.
     * @param opacityLevel the opacity level.
     */
    public static void setLayerOpacity(GisLayer layer, double opacityLevel) {
        if (layer == null) {
            throw new IllegalArgumentException("setLayerOpacity layer argument cannot be null.");
        }
        LayerOpacity layerOpacity = layer instanceof LayerOpacity
                ? (LayerOpacity) layer
                : layer.getLookup().lookup(LayerOpacity.class);

        if (layerOpacity != null) {
            layerOpacity.setOpacity(opacityLevel);
        }
        else {
            logger.log(Level.INFO, "setLayerOpacity: {0} does not support opacity.", layer.getName());
        }
    }

}
