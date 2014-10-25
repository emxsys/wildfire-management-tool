/*
 * Copyright (c) 2009-2014, Bruce Schubert. <bruce@emxsys.com>
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

import com.emxsys.gis.api.Geometry;
import com.emxsys.gis.api.layer.BasicLayerCategory;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.BasicLayerType;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.layer.LayerCategory;
import com.emxsys.gis.api.layer.LayerGroup;
import com.emxsys.gis.api.layer.LayerType;
import com.emxsys.gis.api.viewer.GisViewer;
import com.emxsys.wmt.globe.capabilities.LayerActiveAltitudeCapability;
import com.emxsys.wmt.globe.render.ShapeAdapter;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * A general purpose RenderableLayer for displaying GIS shapes. The existence of this layer in the
 * GisViewer's lookup will add the capability to add Geometry vis a vis the Geometry.Renderer
 * interface.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: ShapesLayer.java 777 2013-06-20 18:25:50Z bdschubert $
 * @see ShapeAdapter
 */
@Messages({})
public class ShapesLayer extends RenderableLayer implements GisLayer, Geometry.Renderer {

    /**
     * Mapping of Geometry shapes to WorldWind renderables.
     */
    private final HashMap<Geometry, Renderable> renderablesMap = new HashMap<>();
    /**
     * The lookup manifests the capabilities for this layer.
     */
    private Lookup lookup;
    /**
     * Lookup contents.
     */
    private final InstanceContent content = new InstanceContent();
    private static final Logger LOG = Logger.getLogger(ShapesLayer.class.getName());

    /**
     * Factory method used to create a WorldWind layer from an .instance file in the XML layer. The
     * WorldWindViewer class iterates over all the WorldWind/Layers instances and invokes the
     * newInstance method. See the XML layer for this module for an example.
     *
     * @param instanceFile The .instance file object specified in the XML layer (layer.xml)
     * @return A ShapeLayer instance
     */
    public static Layer newInstance(FileObject instanceFile) {
// The following commented code block is for getInstance() -- vs newInstance()/
//        // Psuedo singleton: look for an existing instance and if found, initialize it with the 
//        // FileObject, otherwise, use a default instance.
//        Geometry.Renderer renderer = Lookup.getDefault().lookup(Geometry.Renderer.class);
//        ShapesLayer renderableLayer
//                = renderer instanceof ShapesLayer ? (ShapesLayer) renderer : new ShapesLayer();
        
        ShapesLayer renderableLayer = new ShapesLayer();
        // Update GisLayer from xml
        BasicLayerType type = BasicLayerType.fromString((String) instanceFile.getAttribute("type"));
        BasicLayerGroup role = BasicLayerGroup.fromString((String) instanceFile.getAttribute("role"));
        BasicLayerCategory category = BasicLayerCategory.fromString((String) instanceFile.getAttribute("category"));
        renderableLayer.updateLayerAttributes(type, role, category);

        // Perform additional initilization from the layer.xml contents.
        LayerFactory.updateLayerFromFileAttributes(renderableLayer, instanceFile);
        LOG.log(Level.INFO, "Created new instance of {0}", renderableLayer.toString());

        return renderableLayer;
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public ShapesLayer() {
        this.lookup = new AbstractLookup(content);
        this.content.add(this);
        // Add support for active altitudes
        this.content.add(new LayerActiveAltitudeCapability(this));

        // Disable picking
        this.setPickEnabled(false);
    }

    private void updateLayerAttributes(LayerType type, LayerGroup role, LayerCategory category) {
        this.content.add(type == null ? BasicLayerType.Other : type);
        this.content.add(role == null ? BasicLayerGroup.Data : role);
        this.content.add(category == null ? BasicLayerCategory.Unknown : category);

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        super.firePropertyChange(AVKey.LAYER, null, this);
    }

    @Override
    public Lookup getLookup() {
        return this.lookup;
    }

    @Override
    public boolean contains(Geometry shape) {
        return this.renderablesMap.containsKey(shape);
    }

    /**
     * Adds all the shapes to the layer.
     *
     * @param shapes A collection of GIS Shapes.
     */
    @Override
    public void addGeometries(Collection<? extends Geometry> shapes) {
        synchronized (this) {
            // Add each of renderables 
            for (Geometry shape : shapes) {
                addGeometry(shape);
            }
        }
    }

    /**
     * Adds the shape to the layer by instantiating a representative WorldWind Renderable.
     *
     * @param shape
     */
    @Override
    public void addGeometry(Geometry shape) {
        // Use the renderable contained in the shape, if it has one, othewise create a new one.
        Renderable newRenderable = shape.getLookup().lookup(Renderable.class);
        if (newRenderable == null) {
            newRenderable = ShapeAdapter.createRenderable(shape);
        }
        // Update the renderables collection
        Renderable oldRenderable = renderablesMap.put(shape, newRenderable);
        if (oldRenderable != null) {
            super.removeRenderable(oldRenderable);
        }
        // Update the layer
        if (newRenderable != null) {
            super.addRenderable(newRenderable);
            super.firePropertyChange(Geometry.Renderer.PROP_GEOMETRY_ADDED, null, shape);
        }
        refreshLayer();
    }

    @Override
    public void removeGeometry(Geometry shape) {
        // Find the renderable
        Renderable renderable = renderablesMap.remove(shape);
        if (renderable != null) {
            super.removeRenderable(renderable);
            super.firePropertyChange(Geometry.Renderer.PROP_GEOMETRY_REMOVED, shape, null);
            refreshLayer();
        }
    }

    private void refreshLayer() {
        GisViewer viewer = Lookup.getDefault().lookup(GisViewer.class);
        if (viewer != null) {
            viewer.refreshView();
        }
    }
}
