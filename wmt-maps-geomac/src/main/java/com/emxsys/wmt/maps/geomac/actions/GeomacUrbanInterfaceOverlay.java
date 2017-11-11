/*
 * Copyright (c) 2009-2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.maps.geomac.actions;

import com.emxsys.wmt.globe.actions.AbstractGisLayerToggleAction;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Map",
        id = "com.emxsys.wmt.maps.geomac.GeomacUrbanInterfaceOverlay")
@ActionRegistration(
        displayName = "#CTL_GeomacUrbanInterfaceOverlay",
        lazy = false)   // non-lazy init is required to update the button state before it displayed                    
@RibbonActionReference(path = "Ribbon/TaskPanes/Globe/Manage/Overlays/Boundaries", position = 100,
        buttonStyle = "toggle",
        description = "#CTL_GeomacUrbanInterfaceOverlay_Hint",
        priority = "top",
        tooltipTitle = "#CTL_GeomacUrbanInterfaceOverlay_TooltipTitle",
        tooltipBody = "#CTL_GeomacUrbanInterfaceOverlay_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/maps/geomac/images/map32.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages(
        {
            "CTL_GeomacUrbanInterfaceOverlay=GeoMAC Urban Interface",
            "CTL_GeomacUrbanInterfaceOverlay_TooltipBody=The Urban Interface comes from the Nighttime Lights of the "
            + "World dataset which contains the first satellite-based global inventory of human "
            + "settlements, derived from nighttime data from the Defense Meteorological Satellite Program "
            + "(DMSP) Operational Linescan System (OLS). The DMSP-OLS has the unique capability to observe "
            + "faint sourcesof visible-near infrared emissions present at the Earth's surface, including "
            + "cities, towns, villages, gas flares,and fires. National Geophysical Data Center (NGDC) has "
            + "developed algorithms for producing georeferenced fire and nighttime lights products.",
            "CTL_GeomacUrbanInterfaceOverlay_TooltipTitle=GeoMAC Urban Interface Overlay",
            "CTL_GeomacUrbanInterfaceOverlay_Hint=Activate the GeoMAC Wildland Urban Interface overlay. "
        })
public final class GeomacUrbanInterfaceOverlay extends AbstractGisLayerToggleAction {

    private static final Logger logger = Logger.getLogger(GeomacUrbanInterfaceOverlay.class.getName());
    private static final String OVERLAY_NAME = NbBundle.getBundle("com.emxsys.wmt.maps.geomac.Bundle").getString("Layers.Layer0");
    private static final String ICON_BASE = "com/emxsys/wmt/maps/geomac/images/map.png";

    public GeomacUrbanInterfaceOverlay() {
        // The base class will use the layer's display name to find the GisLayer that will be operated on.
        super(OVERLAY_NAME);

        // Non-lazy initializtion requires us to put the name and icon properties into the action;
        // it's not handled by the registration.
        putValue(Action.NAME, Bundle.CTL_GeomacUrbanInterfaceOverlay());
        putValue("iconBase", ICON_BASE);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // Let the base class perform the toggle action
        super.actionPerformed(event);
    }

    @Override
    public Action createContextAwareInstance(Lookup ignoredActionContext) {
        return new GeomacUrbanInterfaceOverlay();
    }
}
