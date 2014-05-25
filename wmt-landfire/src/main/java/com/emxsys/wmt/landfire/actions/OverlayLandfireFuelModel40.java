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
package com.emxsys.wmt.landfire.actions;

import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.layer.Layers;
import com.emxsys.wmt.globe.actions.AbstractGisLayerToggleAction;
import com.emxsys.wmt.landfire.layers.LandfireLayerCategory;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(category = "Map", id = "com.emxsys.landfire.actions.OverlayLandfireFuelModel40")
@ActionRegistration(
        displayName = "#CTL_OverlayLandfireFuelModel40",
        lazy = false)   // non-lazy init is required to update the button state before it displayed
@ActionReference(path = "Toolbars/Overlays", position = 110)
@RibbonActionReference(path = "Menu/Home/Manage/Overlays/LANDFIRE", position = 100,
        buttonStyle = "toggle",
        description = "#CTL_OverlayLandfireFuelModel40_Hint",
        priority = "top",
        tooltipTitle = "#CTL_OverlayLandfireFuelModel40_TooltipTitle",
        tooltipBody = "#CTL_OverlayLandfireFuelModel40_TooltipBody",
        tooltipIcon = "com/emxsys/markers/pushpins/resources/castshadow-black32.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages({
    "CTL_OverlayLandfireFuelModel40=Standard 40 Fuel Models",
    "CTL_OverlayLandfireFuelModel40_Hint=Standard 40 Fuel Models Overlay",
    "CTL_OverlayLandfireFuelModel40_TooltipTitle=Standard 40 Fuel Models",
    "CTL_OverlayLandfireFuelModel40_TooltipBody=Toggle the display of the standard 40 fire behavior fuel models.\n"
    + "The 40 Scott and Burgan Fire Behavior Fuel Model (FBFM40) layer represents distinct "
    + "distributions of fuel loading found among surface fuel components (live and dead), size "
    + "classes, and fuel types. "
})
public final class OverlayLandfireFuelModel40 extends AbstractGisLayerToggleAction {

    private static final String ICON_BASE = "com/emxsys/wmt/landfire/images/map.png";
    private static final String OVERLAY_NAME
            = NbBundle.getBundle("com.emxsys.wmt.landfire.Bundle").getString("LAYER_FBFM40_Name");

    public OverlayLandfireFuelModel40() {
        super(OVERLAY_NAME);
        // non-lazy initializtion requires us to put some properties into the action
        putValue(Action.NAME, Bundle.CTL_OverlayLandfireFuelModel40());
        putValue("iconBase", ICON_BASE);

    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // First, turn off all other LANDFIRE layers when we enable this layer.
        GisLayer gisLayer = getGisLayer();
        if (gisLayer != null && !gisLayer.isEnabled()) {
            Layers.disableLayersInCategory(LandfireLayerCategory.class);
        }
        // Perform the toggle action
        super.actionPerformed(event);
    }

    @Override
    public Action createContextAwareInstance(Lookup ignoredActionContext) {
        return new OverlayLandfireFuelModel40();
    }
}
