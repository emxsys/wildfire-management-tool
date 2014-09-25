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
        id = "com.emxsys.wmt.maps.geomac.GeomacHistFirePerimOverlay")
@ActionRegistration(
        displayName = "#CTL_GeomacHistFirePerimOverlay",
        lazy = false)   // non-lazy init is required to update the button state before it displayed                    

@RibbonActionReference(path = "Menu/Home/Manage/Overlays/Hazards",
        buttonStyle = "toggle",
        position = 100,
        description = "#CTL_GeomacHistFirePerimOverlay_Hint",
        priority = "top",
        tooltipTitle = "#CTL_GeomacHistFirePerimOverlay_TooltipTitle",
        tooltipBody = "#CTL_GeomacHistFirePerimOverlay_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/maps/geomac/images/map32.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages(
        {
            "CTL_GeomacHistFirePerimOverlay=GeoMAC Historic Fire Perimeters",
            "CTL_GeomacHistFirePerimOverlay_TooltipBody=This layer contains historic fire perimeters that were "
            + "submitted to GeoMAC by the incidents beginning in the year 2000 and continuing through "
            + "the previous calendar year (for example, in 2010, the Historic Fire Perimeter layer contains "
            + "perimeters from the years 2000 through the year 2009). More than one perimeter may be shown "
            + "for each fire if acreage differs greatly. Beginning in the year 2011, for fires with the same "
            + "name, only the fire with the latest date and time is shown. For all perimeters available from "
            + "GeoMAC for each fire, please see individual fire perimeter layers by specific year available "
            + "on the http site",
            "CTL_GeomacHistFirePerimOverlay_TooltipTitle=GeoMAC Historic Fire Perimeters Overlay",
            "CTL_GeomacHistFirePerimOverlay_Hint=Activate the GeoMAC Historic Fire Perimeters overlay."
        })
public final class GeomacHistFirePerimOverlay extends AbstractGisLayerToggleAction {

    private static final Logger logger = Logger.getLogger(GeomacHistFirePerimOverlay.class.getName());
    private static final String OVERLAY_NAME = NbBundle.getBundle("com.emxsys.wmt.maps.geomac.Bundle").getString("Layers.Layer6");
    private static final String ICON_BASE = "com/emxsys/wmt/maps/geomac/images/map.png";

    public GeomacHistFirePerimOverlay() {
        // The base class will use the layer's display name to find the GisLayer that will be operated on.
        super(OVERLAY_NAME);

        // Non-lazy initializtion requires us to put the name and icon properties into the action;
        // it's not handled by the registration.
        putValue(Action.NAME, Bundle.CTL_GeomacHistFirePerimOverlay());
        putValue("iconBase", ICON_BASE);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // Let the base class perform the toggle action
        super.actionPerformed(event);
    }

    @Override
    public Action createContextAwareInstance(Lookup ignoredActionContext) {
        return new GeomacHistFirePerimOverlay();
    }
}
