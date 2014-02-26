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

import com.terramenta.ribbon.RibbonActionReference;
import com.emxsys.wmt.gis.Layers;
import com.emxsys.wmt.gis.Viewers;
import com.emxsys.wmt.gis.layer.BasicLayerGroup;
import com.emxsys.wmt.gis.layer.api.GisLayer;
import com.emxsys.wmt.gis.layer.api.LayerOpacity;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;


@ActionID(
    category = "Map",
          id = "com.emxsys.wmt.maps.usa.actions.BasemapTopographic")
@ActionRegistration(
    iconBase = "com/emxsys/wmt/maps/usa/images/basemap-usgs-topo.png",
                    displayName = "#CTL_BasemapTopographic")
@ActionReferences(
{
    @ActionReference(path = "Menu/Map/Basemap/Topographic", position = 100),
    @ActionReference(path = "Toolbars/Basemap", position = 310)
})
@RibbonActionReference(path = "Menu/Home/Map/Basemap/Topographic",
                       position = 100,
                       description = "#CTL_BasemapTopographic_Hint",
                       priority = "top",
                       tooltipTitle = "#CTL_BasemapTopographic_TooltipTitle",
                       tooltipBody = "#CTL_BasemapTopographic_TooltipBody",
                       tooltipIcon = "com/emxsys/wmt/maps/usa/images/basemap-usgs-topo32.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages(
{
    "CTL_BasemapTopographic=USGS Topographic",
    "CTL_BasemapTopographic_Hint=24K, 100K and 250K Topographic Basemap",
    "CTL_BasemapTopographic_TooltipTitle=USGS Topographic Basemap",
    "CTL_BasemapTopographic_TooltipBody=Activate the USGS Topographic basemap. \n"
    + "The basemap is comprised of USGS 24K, 100K and 25K topographic maps."
})
public final class BasemapTopographic implements ActionListener
{

    private static final Logger logger = Logger.getLogger(BasemapTopographic.class.getName());
    private static final String BASEMAP_LOW_RES = NbBundle.getBundle("com.emxsys.wmt.maps.usa.Bundle").getString("LAYER_USGSTopoLowRes");
    private static final String BASEMAP_MED_RES = NbBundle.getBundle("com.emxsys.wmt.maps.usa.Bundle").getString("LAYER_USGSTopoMedRes");
    private static final String BASEMAP_HIGH_RES = NbBundle.getBundle("com.emxsys.wmt.maps.usa.Bundle").getString("LAYER_USGSTopoHighRes");


    @Override
    public void actionPerformed(ActionEvent e)
    {
        List<GisLayer> layers = Layers.getLayersInRole(BasicLayerGroup.Basemap);
        for (GisLayer layer : layers)
        {
            boolean shouldEnable =
                layer.getName().equals(BASEMAP_LOW_RES)
                || layer.getName().equals(BASEMAP_MED_RES)
                || layer.getName().equals(BASEMAP_HIGH_RES);
            layer.setEnabled(shouldEnable);
            if (shouldEnable)
            {
                layer.getLookup().lookup(LayerOpacity.class).setOpacity(LayerOpacity.OPAQUE);
            }
        }
        Viewers.getPrimaryViewer().setVisible(true);
    }
}
