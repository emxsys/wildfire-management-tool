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

import com.emxsys.wmt.gis.Layers;
import com.emxsys.wmt.gis.Viewers;
import com.emxsys.wmt.gis.api.layer.BasicLayerGroup;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.gis.api.layer.LayerOpacity;
import com.emxsys.wmt.globe.layers.BaseMapLayers;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Map",
        id = "com.emxsys.wmt.globe.actions.BasemapStreetMsVirtualEarthRoad")
@ActionRegistration(
        iconBase = "com/emxsys/wmt/globe/images/basemap-vearth-hybrid.png",
        displayName = "#CTL_BasemapMsVirtualEarthRoad")
@ActionReference(path = "Toolbars/Basemap", position = 430)
@RibbonActionReference(path = "Menu/Home/Manage/Basemap/Street", position = 300,
        description = "#CTL_BasemapMsVirtualEarthRoad_Hint",
        priority = "top",
        tooltipTitle = "#CTL_BasemapMsVirtualEarthRoad_TooltipTitle",
        tooltipBody = "#CTL_BasemapMsVirtualEarthRoad_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/images/basemap-vearth-hybrid.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages(
        {
            "CTL_BasemapMsVirtualEarthRoad=MS Virtual Earth Road",
            "CTL_BasemapMsVirtualEarthRoad_Hint=Microsoft Virtual Earth Road Base Map",
            "CTL_BasemapMsVirtualEarthRoad_TooltipTitle=Microsoft Virtual Earth Road Base Map",
            "CTL_BasemapMsVirtualEarthRoad_TooltipBody=Activates a base map from Microsoft that is a "
            + "general map portraying lines of transportion."
        })
public final class BasemapStreetlMsVirtualEarthRoad implements ActionListener
{

    private static final Logger logger = Logger.getLogger(BasemapStreetlMsVirtualEarthRoad.class.getName());
    private static final String BASEMAP_NAME = BaseMapLayers.LAYER_MSVE_ROAD;

    @Override
    public void actionPerformed(ActionEvent e)
    {
        GisLayer layer = Layers.findLayer(BASEMAP_NAME);
        if (layer == null)
        {
            throw new IllegalStateException(BASEMAP_NAME + " layer not found.");
        }
        Layers.enableLayerInRoleExclusive(BASEMAP_NAME, BasicLayerGroup.Basemap);
        Layers.setLayerOpacity(layer, LayerOpacity.OPAQUE);
        Viewers.activatePrimaryViewer();
    }
}
