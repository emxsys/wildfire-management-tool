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

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.layer.BasicLayerCategory;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.BasicLayerType;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.visad.GeneralType;
import com.emxsys.visad.Units;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.layers.RenderableGisLayer;
import com.emxsys.wmt.globe.render.GlobePath;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;
import static visad.CommonUnit.meter;
import visad.Real;
import visad.VisADException;

/**
 * This class is responsible for rendering a solar ray.
 *
 * @author Bruce Schubert
 */
public class SolarRay {

    // A path between two points is used to represent a solar ray.
    private final GlobePath path;
    private static RenderableGisLayer layer;

    public SolarRay() {
        this.path = new GlobePath();
        
        BasicShapeAttributes attr = new BasicShapeAttributes();
        attr.setInteriorMaterial(Material.YELLOW);
        attr.setOutlineMaterial(Material.ORANGE);
        this.path.setAttributes(attr);
    }

    /**
     * Draws the projected shape from the supplied point of origin after a given duration.
     *
     * @param coord The point of intersection for the solar ray.
     * @param sunlight The sunlight data.
     */
    public void update(Coord3D coord, Sunlight sunlight) {
        if (coord.isMissing()
                || sunlight.getSubsolarLatitude().isMissing()
                || sunlight.getSubsolarLongitude().isMissing()
                || sunlight.getZenithAngle().isMissing()) {
            return;
        }
        initialize();
        WindowManager.getDefault().invokeWhenUIReady(() -> {

            try {
                // Create a coordinate at the sun's position
                Real au = new Real(GeneralType.Distance, 1, Units.getUnit("au"));
                Coord3D sunCoord = GeoCoord3D.fromDegreesAndMeters(
                        sunlight.getSubsolarLatitude().getValue(),
                        sunlight.getSubsolarLongitude().getValue(),
                        au.getValue(meter));

                // Disable solar ray during night.
                if (sunlight.getZenithAngle().getValue() > 90.0) {
                    path.setVisible(false);
                    return;
                }

                // Update and draw the ray
                path.update(coord, sunCoord);
                if (!path.isVisible()) {
                    path.setVisible(true);
                }
                Globe.getInstance().refreshView();

            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    /**
     * Initialize the layer.
     */
    private void initialize() {
        if (layer == null) {
            // Add the layer the globe. But defer creating the layer until Globe 
            // has been initialized, else WorldWind configs are read from native WW.
            WindowManager.getDefault().invokeWhenUIReady(() -> {
                layer = new RenderableGisLayer(
                        "Solar Rays",
                        BasicLayerGroup.Overlay,
                        BasicLayerType.Other,
                        BasicLayerCategory.Other);
                layer.addRenderable(path);
                Globe.getInstance().addGisLayer(layer);
            });
        }
    }

}
