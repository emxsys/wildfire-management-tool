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
package com.emxsys.wmt.gis.api.viewer;

import com.emxsys.wmt.gis.api.Geometry;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.gis.api.viewer.GisViewer;
import java.util.Collection;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * A utility class with convenient methods for interacting with the GIS.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: Viewers.java 769 2013-06-20 18:11:51Z bdschubert $
 */
public class Viewers {

    private static final Logger logger = Logger.getLogger(Viewers.class.getName());

    private Viewers() {
    }

    /**
     * Adds a Geometry object to compatible viewers for display.
     *
     * @param geometry to be displayed
     */
    public static void addToViewers(Geometry geometry) {
        // Place it in all compatible viewers
        Collection<? extends GisViewer> viewers = Lookup.getDefault().lookupAll(GisViewer.class);
        for (GisViewer viewer : viewers) {
            // Look for a "layer" in the form of a Geometry.Renderer
            Geometry.Renderer renderer = viewer.getLookup().lookup(Geometry.Renderer.class);
            if (renderer != null) {
                renderer.addGeometry(geometry);
            }
        }
    }

    /**
     * Removes the Geometry from viewers.
     *
     * @param geometry to be removed
     */
    public static void removeFromViewers(Geometry geometry) {
        // Remove it from the viewers
        Collection<? extends GisViewer> viewers = Lookup.getDefault().lookupAll(GisViewer.class);
        for (GisViewer viewer : viewers) {
            Geometry.Renderer renderer = viewer.getLookup().lookup(Geometry.Renderer.class);
            if (renderer != null) {
                renderer.removeGeometry(geometry);
            }
        }
    }

    /**
     * Activates the primary GisViewer.
     */
    public static void activatePrimaryViewer() {
        GisViewer primaryViewer = getPrimaryViewer();
        if (primaryViewer != null) {
            primaryViewer.setVisible(true);
        }
        else {
            logger.warning("activatePrimaryViewer() could not find a GisViewer to activate.");
        }
    }

    /**
     * Gets the primary GisViewer.
     *
     * @return the active GisViewer; may return null;
     */
    public static GisViewer getPrimaryViewer() {

        Collection<? extends GisViewer> viewers = Lookup.getDefault().lookupAll(GisViewer.class);
        if (viewers.isEmpty()) {
            return null;
        }
        else if (viewers.size() == 1) {
            return viewers.iterator().next();
        }
        else {
            throw new UnsupportedOperationException("Support for muliple viewers not implemented yet");
        }
    }

    /**
     * Invokes GisViewer.refreshView on viewers containing the layer.
     *
     * @param layer the map layer to look for in the viewer's lookup.
     * @see GisViewer
     */
    public static void refreshViewersContaining(GisLayer layer) {
        // Refresh the viewers that reference this layer
        Collection<? extends GisViewer> viewers = Lookup.getDefault().lookupAll(GisViewer.class);
        for (GisViewer viewer : viewers) {
            Collection<? extends GisLayer> layers = viewer.getLookup().lookupAll(GisLayer.class);
            if (layers.contains(layer)) {
                viewer.refreshView();
            }
        }
    }

}
