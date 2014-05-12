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

import com.emxsys.wmt.globe.layers.WidgetLayers;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Map",
        id = "com.emxsys.wmt.globe.actions.OverlayCrosshairs")
@ActionRegistration(
        displayName = "#CTL_OverlayCrosshairs",
        lazy = false)
@ActionReference(path = "Toolbars/Overlays", position = 5100)
@com.terramenta.ribbon.RibbonActionReference(path = "Menu/Home/Manage/Overlays/Controls", position = 100,
        buttonStyle = "toggle",
        description = "#CTL_OverlayCrosshairs_Hint",
        priority = "top",
        tooltipTitle = "#CTL_OverlayCrosshairs_TooltipTitle",
        tooltipBody = "#CTL_OverlayCrosshairs_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/images/crosshairs.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages({
    "CTL_OverlayCrosshairs=Crosshairs",
    "CTL_OverlayCrosshairs_Hint=Crosshairs Overlay",
    "CTL_OverlayCrosshairs_TooltipTitle=Crosshairs Overlay",
    "CTL_OverlayCrosshairs_TooltipBody=Toggle the display of the crosshairs."
})
public final class OverlayCrosshairs extends AbstractGisLayerToggleAction {

    private static final String ICON_BASE = "com/emxsys/wmt/globe/images/crosshairs32.png";
    private static final String OVERLAY_NAME = WidgetLayers.LAYER_RETICULE;

    public OverlayCrosshairs() {
        super(OVERLAY_NAME);
        // non-lazy initializtion requires us to put some properties into the action
        putValue(Action.NAME, Bundle.CTL_OverlayCrosshairs());
        putValue("iconBase", ICON_BASE);

    }

    @Override
    public Action createContextAwareInstance(Lookup ignoredActionContext) {
        return new OverlayCrosshairs();
    }
}
