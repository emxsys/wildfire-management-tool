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
package com.emxsys.wmt.globe.actions;

import com.emxsys.gis.api.layer.Layers;
import com.emxsys.gis.api.viewer.Viewers;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.layer.LayerOpacity;
import com.emxsys.wmt.globe.layers.BaseMapLayers;
import com.terramenta.ribbon.RibbonActionReference;
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
        id = "com.emxsys.wmt.globe.actions.BasemapStreetOpenStreetMapBlend")
@ActionRegistration(
        iconBase = "com/emxsys/wmt/globe/images/basemap-openstreet.png",
        displayName = "#CTL_BasemapOpenStreetMapBlend")
@ActionReference(path = "Toolbars/Basemap", position = 4200)
@RibbonActionReference(path = "Ribbon/TaskPanes/Globe/Manage/Basemap/Street", position = 200,
        description = "#CTL_BasemapOpenStreetMapBlend_Hint",
        priority = "top",
        tooltipTitle = "#CTL_BasemapOpenStreetMapBlend_TooltipTitle",
        tooltipBody = "#CTL_BasemapOpenStreetMapBlend_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/images/basemap-openstreet.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages({
    "CTL_BasemapOpenStreetMapBlend=OpenStreetMap Bing Blend",
    "CTL_BasemapOpenStreetMapBlend_Hint=OpenStreetMap and Bing Base Map",
    "CTL_BasemapOpenStreetMapBlend_TooltipTitle=OpenStreetMap/Bing Base Map",
    "CTL_BasemapOpenStreetMapBlend_TooltipBody=Activates a base map from OpenStreetMap and Bing that blends a "
    + "general map portraying lines of transportion with aerial and Microsoft Bing aerial  imagery.\n"
    + "OpenStreetMap (OSM) is a collaborative project to create a free editable map of the world. "
    + "The maps are created using data from portable GPS devices, aerial photography, other free "
    + "sources or simply from local knowledge. See www.openstreetmap.org for more information."
})
public final class BasemapStreetOpenStreetMapBlend implements ActionListener {

    private static final Logger logger = Logger.getLogger(BasemapStreetOpenStreetMapBlend.class.getName());
    private static final String BASEMAP_STREET = BaseMapLayers.LAYER_OPEN_STREET_MAP;
    private static final String BASEMAP_SATELLITE = BaseMapLayers.LAYER_LANDSAT;
    private static final String BASEMAP_AERIAL = BaseMapLayers.LAYER_BING;
    private static final double OPACITY_TRANSLUCENT = 0.7;
    private static final double OPACITY_OPAQUE = 1.0;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Layers.findLayer(BASEMAP_STREET) == null) {
            throw new IllegalStateException(BASEMAP_STREET + " layer not found.");
        }
        // Enable the basemap layer(s)
        List<GisLayer> layers = Layers.getLayersInGroup(BasicLayerGroup.Basemap);
        for (GisLayer layer : layers) {
            if (layer.getName().equals(BASEMAP_STREET)) {
                layer.setEnabled(true);
                layer.getLookup().lookup(LayerOpacity.class).setOpacity(OPACITY_TRANSLUCENT);
            }
            else if (layer.getName().equals(BASEMAP_AERIAL)) {
                layer.setEnabled(true);
                layer.getLookup().lookup(LayerOpacity.class).setOpacity(OPACITY_OPAQUE);
            }
            else if (layer.getName().equals(BASEMAP_SATELLITE)) {
                layer.setEnabled(true);
                layer.getLookup().lookup(LayerOpacity.class).setOpacity(OPACITY_OPAQUE);
            }
            else {
                layer.setEnabled(false);
            }
        }
        Viewers.activatePrimaryViewer();
    }
}
