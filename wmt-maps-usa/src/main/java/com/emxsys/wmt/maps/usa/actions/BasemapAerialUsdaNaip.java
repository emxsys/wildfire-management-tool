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

import com.emxsys.gis.api.layer.Layers;
import com.terramenta.ribbon.RibbonActionReference;
import com.emxsys.gis.api.viewer.Viewers;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.layer.LayerOpacity;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Map",
        id = "com.emxsys.wmt.maps.usa.actions.BasemapAerialUsdaNaip")
@ActionRegistration(
        iconBase = "com/emxsys/wmt/maps/usa/images/basemap-usda-naip.png",
        displayName = "#CTL_BasemapUsdaNaip")
@ActionReference(path = "Toolbars/Basemap", position = 240)
@RibbonActionReference(path = "Ribbon/TaskPanes/Globe/Manage/Basemap/Aerial", position = 400,
        description = "#CTL_BasemapUsdaNaip_Hint",
        priority = "top",
        tooltipTitle = "#CTL_BasemapUsdaNaip_TooltipTitle",
        tooltipBody = "#CTL_BasemapUsdaNaip_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/maps/usa/images/basemap-usda-naip32.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages({
    "CTL_BasemapUsdaNaip=USDA NAIP",
    "CTL_BasemapUsdaNaip_Hint=UDSA NAIP Basemap",
    "CTL_BasemapUsdaNaip_TooltipTitle=USDA NAIP Basemap",
    "CTL_BasemapUsdaNaip_TooltipBody=Activate a basemap using USDA National Agriculture Imagery Program (NAIP) imagery. \n"
    + "The NAIP acquires aerial imagery during the agricultural growing seasons in the continental U.S. "
    + "A primary goal of the NAIP program is to make digital ortho photography available to governmental "
    + "agencies and the public within a year of acquisition. "
})
public final class BasemapAerialUsdaNaip implements ActionListener {

    private static final Logger logger = Logger.getLogger(BasemapAerialUsdaNaip.class.getName());
    private static final String BASEMAP_NAME = NbBundle.getBundle("com.emxsys.wmt.maps.usa.Bundle").getString("LAYER_USDANAIP");

    @Override
    public void actionPerformed(ActionEvent e) {
        GisLayer layer = Layers.findLayer(BASEMAP_NAME);
        if (layer == null) {
            throw new IllegalStateException(BASEMAP_NAME + " layer not found.");
        }
        Layers.enableLayerInGroupExclusive(BASEMAP_NAME, BasicLayerGroup.Basemap);
        Layers.setLayerOpacity(layer, LayerOpacity.OPAQUE);
        Viewers.getPrimaryViewer().setVisible(true);
    }
}
