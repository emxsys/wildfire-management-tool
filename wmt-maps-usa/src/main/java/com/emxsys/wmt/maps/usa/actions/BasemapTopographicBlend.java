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
package com.emxsys.wmt.maps.usa.actions;

import com.emxsys.wmt.maps.usa.layers.UsaBasemapLayers;
import com.terramenta.ribbon.RibbonActionReference;
import com.emxsys.wmt.gis.api.layer.Layers;
import com.emxsys.wmt.gis.api.viewer.Viewers;
import com.emxsys.wmt.gis.api.layer.BasicLayerGroup;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.gis.api.layer.LayerActiveAltitudeRange;
import com.emxsys.wmt.gis.api.layer.LayerOpacity;
import com.emxsys.wmt.visad.Reals;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Map",
        id = "com.emxsys.wmt.maps.usa.actions.BasemapTopographicBlend")
@ActionRegistration(
        iconBase = "com/emxsys/wmt/maps/usa/images/basemap-usgs-topo.png",
        displayName = "#CTL_BasemapTopographicBlend")
@ActionReference(path = "Toolbars/Basemap", position = 320)
@RibbonActionReference(path = "Menu/Home/Manage/Basemap/Topographic",
        position = 200,
        description = "#CTL_BasemapTopographicBlend_Hint",
        priority = "top",
        tooltipTitle = "#CTL_BasemapTopographicBlend_TooltipTitle",
        tooltipBody = "#CTL_BasemapTopographicBlend_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/maps/usa/images/basemap-usgs-topo.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages({
    "CTL_BasemapTopographicBlend=USGS Topographic NAIP Blend",
    "CTL_BasemapTopographicBlend_Hint=Topographic and Aerial Imagergy Blend Basemap",
    "CTL_BasemapTopographicBlend_TooltipTitle=USGS Topographic Blend Basemap",
    "CTL_BasemapTopographicBlend_TooltipBody=Activate the blended USGS topographic and USDA aerial imagery basemap.\n"
    + "USGS 24K, 100K and 250K topographic maps are blended with USDA NAIP aerial imagery to provide an enhanced"
    + "display of the earth's topography and physical features."
})
public final class BasemapTopographicBlend implements ActionListener {

    private static final Logger logger = Logger.getLogger(BasemapTopographicBlend.class.getName());
    //private static final String BASEMAP_SATELLITE = NbBundle.getBundle("com.emxsys.maps.usa.Bundle").getString("LAYER_Landsat");
    private static final String BASEMAP_AERIAL = UsaBasemapLayers.LAYER_USDA_NAIP;
    private static final String BASEMAP_LOW_RES = UsaBasemapLayers.LAYER_USGS_TOPO_LOW_RES;
    private static final String BASEMAP_MED_RES = UsaBasemapLayers.LAYER_USGS_TOPO_MED_RES;
    private static final String BASEMAP_HIGH_RES = UsaBasemapLayers.LAYER_USGS_TOPO_HIGH_RES;
    private static final double THRESHOLD_LOW_TO_MED_RES = 20000;
    private static final double THRESHOLD_MED_TO_HIGH_RES = 5000;

    @Override
    public void actionPerformed(ActionEvent e) {
        List<GisLayer> layers = Layers.getLayersInGroup(BasicLayerGroup.Basemap);
        for (GisLayer layer : layers) {
            // Default layer properties
            boolean enabled = true;
            double maxActiveAltitude = 1000000000.0;
            double minActiveAltitude = 0.0;
            double opacity = LayerOpacity.TRANSLUCENT;

            if (layer.getName().equals(BASEMAP_LOW_RES)) {
                minActiveAltitude = THRESHOLD_LOW_TO_MED_RES;
            }
            else if (layer.getName().equals(BASEMAP_MED_RES)) {
                maxActiveAltitude = THRESHOLD_LOW_TO_MED_RES;
                minActiveAltitude = THRESHOLD_MED_TO_HIGH_RES;
            }
            else if (layer.getName().equals(BASEMAP_HIGH_RES)) {
                maxActiveAltitude = THRESHOLD_MED_TO_HIGH_RES;
                minActiveAltitude = 0.0;
            }
            else if (layer.getName().equals(BASEMAP_AERIAL)) {
                opacity = LayerOpacity.OPAQUE;
            }
//            else if (layer.getName().equals(BASEMAP_SATELLITE))
//            {
//                opacity = LayerOpacity.OPAQUE;
//            }
            // Disable all other layers
            else {
                enabled = false;
            }

            layer.setEnabled(enabled);

            LayerActiveAltitudeRange activeAltitudeRange = layer.getLookup().lookup(LayerActiveAltitudeRange.class);
            if (enabled && activeAltitudeRange != null) {
                activeAltitudeRange.setMaxActiveAltitude(Reals.newAltitude(maxActiveAltitude));
                activeAltitudeRange.setMinActiveAltitude(Reals.newAltitude(minActiveAltitude));
            }
            LayerOpacity layerOpacity = layer.getLookup().lookup(LayerOpacity.class);
            if (enabled && layerOpacity != null) {
                layerOpacity.setOpacity(opacity);
            }
        }
        Viewers.getPrimaryViewer().setVisible(true);
    }
}
