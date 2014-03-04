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

import com.emxsys.wmt.gis.GeoSector;
import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.gis.api.layer.GisLayerList;
import java.awt.Component;
import org.openide.util.Lookup;
import visad.Real;

/**
 * This interface provides the common capabilities of a viewer of GIS data.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: GisViewer.java 540 2013-04-18 15:48:26Z bdschubert $
 */
public interface GisViewer extends Lookup.Provider //, ShowCapability
{

    @Override
    Lookup getLookup();

    /**
     * Shows or hides the window representing this viewer.
     * @param show true to show
     */
    void setVisible(boolean show);

    /**
     * Gets the current window visibility
     * @return true if being shown
     */
    boolean isVisible();

    /**
     * Adds a layer to the viewer.
     * @param layer the layer to be added.
     */
    void addGisLayer(GisLayer layer);

    /**
     * Removes a layer from the viewer.
     * @param layer the layer to be removed.
     */
    void removeGisLayer(GisLayer layer);

    /**
     * Gets the collection of layers in the viewer.
     * @return the layers in the viewer
     */
    GisLayerList getGisLayerList();

    /**
     * Centers the viewer on a geographic coordinate.
     * @param latlon the coordinates to center on.
     */
    void centerOn(Coord2D latlon);

    /**
     * Get the geographic coordinates at the screen center.
     * @return the latitude, longitude and altitude at the center.
     */
    Coord3D getLocationAtCenter();

    /**
     * Gets the geographic coordinates at a specified screen position.
     * @param x screen x position.
     * @param y screen y position.
     * @return the latitude, longitude and altitude at the screen x, y.
     */
    Coord3D getLocationAtScreenPoint(double x, double y);

    /**
     * Gets the name of this viewer, which may be used for a window title.
     * @return the viewer name.
     */
    String getName();

    /**
     * Gets the component that renders the GIS view.
     * @return the rendering implementation component.
     */
    Component getRendererComponent();

    /**
     * Initializes the implementation.
     */
    void initializeResources();

    /**
     * Refreshes the viewer. Typically called after adding or removing a layer or feature.
     */
    void refreshView();

    /**
     * Compute a rectangular sector who's edges are a fixed distance from the center position.
     * @param center the lat/lon point on which the sector is centered.
     * @param radius the radius of a circle bounded by the sector.
     * @return a rectangular sector centered on the supplied coordinates.
     */
    public GeoSector computeSector(Coord2D center, Real radius);
}
