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
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Map",
        id = "com.emxsys.wmt.maps.geomac.GeomacCurrFiresOverlay")
@ActionRegistration(
        displayName = "#CTL_GeomacCurrFiresOverlay",
        lazy = false)   // non-lazy init is required to update the button state before it displayed                    
@RibbonActionReference(path = "Ribbon/TaskPanes/Globe/Manage/Overlays/Labels", position = 100,
        buttonStyle = "toggle",
        description = "#CTL_GeomacCurrFiresOverlay_Hint",
        priority = "top",
        tooltipTitle = "#CTL_GeomacCurrFiresOverlay_TooltipTitle",
        tooltipBody = "#CTL_GeomacCurrFiresOverlay_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/maps/geomac/images/map32.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages(
        {
            "CTL_GeomacCurrFiresOverlay=GeoMAC Current Fires",
            "CTL_GeomacCurrFiresOverlay_TooltipBody=This layer displays point data for large active fire incidents "
            + "that have been entered into the NIFC Oracle database maintained in Kansas City.  This "
            + "database is updated every morning, and includes (or provides links to) information such as "
            + "fire name, percent contained, latitude and longitude coordinates, event ID, size (in acres), "
            + "start date, reporting date, priority, personnel committed, general location, estimated damage, "
            + "estimated cost, and cause.",
            "CTL_GeomacCurrFiresOverlay_TooltipTitle=GeoMAC Current Fires Overlay",
            "CTL_GeomacCurrFiresOverlay_Hint=Activate the GeoMAC Current Fires overlay. \n"
            + "The fire points are updated daily."
        })
public final class GeomacCurrFiresOverlay extends AbstractGisLayerToggleAction {

    private static final Logger logger = Logger.getLogger(GeomacCurrFiresOverlay.class.getName());
    private static final String OVERLAY_NAME = NbBundle.getBundle("com.emxsys.wmt.maps.geomac.Bundle").getString("Layers.LayerCurrFirePoints");
    private static final String ICON_BASE = "com/emxsys/wmt/maps/geomac/images/map.png";

    public GeomacCurrFiresOverlay() {
        // The base class will use the layer's display name to find the GisLayer that will be operated on.
        super(OVERLAY_NAME);

        // Non-lazy initializtion requires us to put the name and icon properties into the action;
        // it's not handled by the registration.
        putValue(Action.NAME, Bundle.CTL_GeomacCurrFiresOverlay());
        putValue("iconBase", ICON_BASE);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // Let the base class perform the toggle action
        super.actionPerformed(event);
    }

    @Override
    public Action createContextAwareInstance(Lookup ignoredActionContext) {
        return new GeomacCurrFiresOverlay();
    }
}
