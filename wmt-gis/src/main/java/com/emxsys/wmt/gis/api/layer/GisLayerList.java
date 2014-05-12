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
package com.emxsys.wmt.gis.api.layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * A collection of GisLayers managed by a Lookup meant to be monitored by actions with a lookup
 * listener.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class GisLayerList implements Lookup.Provider {

    private final InstanceContent content = new InstanceContent();
    private final AbstractLookup lookup = new AbstractLookup(content);
    private static final Logger logger = Logger.getLogger(GisLayerList.class.getName());

    static {
//        logger.setLevel(Level.ALL);
    }

    public GisLayerList() {
    }

    /**
     * Gets the collection of layers managed by the lookup.
     * @return the layers in the lookup
     */
    public Collection<? extends GisLayer> getLayers() {
        return this.lookup.lookupAll(GisLayer.class);
    }

    /**
     * Convenience method gets a Lookup.Result suitable for lookup listeners.
     * @return a Lookup.Result for GisLayers on the internal lookup.
     */
    public Lookup.Result<GisLayer> getLookupResult() {
        return this.lookup.lookupResult(GisLayer.class);
    }

    /**
     * Adds a layer to the lookup. layer.
     * @param layer layer to be added
     */
    public void add(GisLayer layer) {
        this.content.add(layer);
        logger.log(Level.FINE, "{0} added.", layer.getName());
    }

    /**
     * Removes the layer from the lookup.
     * @param layer layer to be removed
     */
    public void remove(GisLayer layer) {
        this.content.remove(layer);
        logger.log(Level.FINE, "{0} removed.", layer.getName());
    }

    /**
     * Finds the layer with the given name.
     * @param layerName name to search for
     * @return the layer if found; null otherwise
     */
    public GisLayer find(String layerName) {
        Collection<? extends GisLayer> layers = getLayers();
        logger.log(Level.FINE, "find: getLayers size is {0}", layers.size() );
        for (GisLayer layer : layers) {
            if (layer.getName().equals(layerName)) {
                return layer;
            }
        }
        logger.log(Level.FINE, "find({0}) not found!", layerName);
        return null;
    }

    /**
     * Gets the lookup containing the GisLayers.
     * @return the lookup
     */
    @Override
    public Lookup getLookup() {
        return this.lookup;
    }

    /**
     * Disables all the layers within the given category.
     * @param clazz the layer category class to be disabled
     */
    public void disableLayersInCategory(Class<? extends LayerCategory> clazz) {
        Collection<? extends GisLayer> layers = getLayers();
        for (GisLayer layer : layers) {
            if (layer.isEnabled() && layer.getLookup().lookup(clazz) != null) {
                layer.setEnabled(false);
            }
        }

    }

    /**
     * Enables the specified layer and disables all other layers in the given group.
     * @param layerName name of the layer to be enabled
     * @param layerGroup the group to act on
     */
    public void enableLayerInGroupExclusive(String layerName, LayerGroup layerGroup) {
        List<GisLayer> layers = getLayersInGroup(layerGroup);
        for (GisLayer layer : layers) {
            layer.setEnabled(layer.getName().equals(layerName));
        }
    }

    /**
     * Gets a collection of layers within the given category.
     * @param category layer category to retrieve
     * @return a collection of layers that share the given category
     */
    public List<GisLayer> getLayersInCategory(LayerCategory category) {
        ArrayList<GisLayer> list = new ArrayList<>(); // return value
        Collection<? extends GisLayer> layers = getLayers();
        for (GisLayer layer : layers) {
            Collection<? extends LayerCategory> categories = layer.getLookup().lookupAll(LayerCategory.class);
            for (LayerCategory cat : categories) {
                if (cat.equals(category)) {
                    list.add(layer);
                }
            }
        }
        return list;
    }

    /**
     * Gets a collection of layers within the given group.
     * @param group layer group to retrieve
     * @return the collection of layers within the group
     */
    public List<GisLayer> getLayersInGroup(LayerGroup group) {
        ArrayList<GisLayer> list = new ArrayList<>(); // return value
        Collection<? extends GisLayer> layers = getLayers();
        for (GisLayer layer : layers) {
            Collection<? extends LayerGroup> groups = layer.getLookup().lookupAll(group.getClass());
            for (LayerGroup layerRole : groups) {
                if (layerRole.getName().equals(group.getName())) {
                    list.add(layer);
                }
            }
        }
        return list;
    }

    public List<GisLayer> getLayersWithAttribute(Class<? extends Object> clazz) {
        ArrayList<GisLayer> list = new ArrayList<>(); // return value
        Collection<? extends GisLayer> layers = getLayers();
        for (GisLayer layer : layers) {
            if (layer.getLookup().lookup(clazz) != null) {
                list.add(layer);
            }
        }
        return list;
    }
}
