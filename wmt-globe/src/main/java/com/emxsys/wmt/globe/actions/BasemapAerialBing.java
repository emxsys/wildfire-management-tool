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
        id = "com.emxsys.wmt.globe.actions.BasemapAerialBing")
@ActionRegistration(
        iconBase = "com/emxsys/wmt/globe/images/basemap-bing-aerial.png",
        displayName = "#CTL_BasemapBing")
@ActionReference(path = "Toolbars/Basemap", position = 2100)
@RibbonActionReference(path = "Menu/Home/Manage/Basemap/Aerial", position = 100,
        description = "#CTL_BasemapBing_Hint",
        priority = "top",
        tooltipTitle = "#CTL_BasemapBing_TooltipTitle",
        tooltipBody = "#CTL_BasemapBing_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/images/basemap-bing-aerial.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages({
    "CTL_BasemapBing=Bing",
    "CTL_BasemapBing_Hint=Bing Basemap",
    "CTL_BasemapBing_TooltipTitle=Bing Basemap",
    "CTL_BasemapBing_TooltipBody=Activate a basemap using Microsoft Bing aerial imagery."
})
public final class BasemapAerialBing implements ActionListener {

    private static final Logger logger = Logger.getLogger(BasemapAerialBing.class.getName());
    private static final String BASEMAP_NAME = BaseMapLayers.LAYER_BING;

    @Override
    public void actionPerformed(ActionEvent e) {
        List<GisLayer> layers = Layers.getLayersInGroup(BasicLayerGroup.Basemap);
        for (GisLayer layer : layers) {
            // Enable just this basemap 
            layer.setEnabled(layer.getName().equals(BASEMAP_NAME));

            // Reset the opacity for all layers in case a previous basemap was using transparency
            // This action will assure the visability of other basemaps should the user intereact 
            // with a layer manager.
            Layers.setLayerOpacity(layer, LayerOpacity.OPAQUE);
        }
        Viewers.activatePrimaryViewer();
    }
}
