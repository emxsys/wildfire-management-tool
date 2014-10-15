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
    id = "com.emxsys.wmt.maps.geomac.GeomacPrevFirePerimOverlay")
@ActionRegistration(
    displayName = "#CTL_GeomacPrevFirePerimOverlay",
    lazy = false)   // non-lazy init is required to update the button state before it displayed                    
@RibbonActionReference(path = "Menu/Home/Manage/Overlays/Hazards",
                       buttonStyle = "toggle",
                       position = 100,
                       description = "#CTL_GeomacPrevFirePerimOverlay_Hint",
                       priority = "top",
                       tooltipTitle = "#CTL_GeomacPrevFirePerimOverlay_TooltipTitle",
                       tooltipBody = "#CTL_GeomacPrevFirePerimOverlay_TooltipBody",
                       tooltipIcon = "com/emxsys/wmt/maps/geomac/images/map32.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages(
    {
    "CTL_GeomacPrevFirePerimOverlay=GeoMAC Previous Fire Perimeters",
    "CTL_GeomacPrevFirePerimOverlay_TooltipBody=This layer contains all fire perimeters that were submitted "
        + "to GeoMAC by field offices for the current year but are currently inactive.. Perimeters "
    + "are collected in the field by a variety of means, including infrared flights, and by using a "
    + "GPS unit to map the perimeter.\n" +
"\n" +
"Please NOTE: GeoMAC only displays perimeter data as they are submitted by field offices. Since data "
    + "are not received for all fires, you may not be able to view perimeters for every fire. Once a "
    + "perimeter is more than two days old, or is replaced by a more current perimeter, it is "
    + "displayed in blue-gray in the Past 2011 Perimeters layer. Also, perimeters are usually no "
    + "longer collected in the field once a fire is no longer growing in size and is mostly contained.",
    "CTL_GeomacPrevFirePerimOverlay_TooltipTitle=GeoMAC Previous Fire Perimeters Overlay",
    "CTL_GeomacPrevFirePerimOverlay_Hint=Activate the GeoMAC Previous Fire Perimeters overlay. \n"
    + "The fire perimeters are updated every one or two days."
})
public final class GeomacPrevFirePerimOverlay extends AbstractGisLayerToggleAction
{
    private static final Logger logger = Logger.getLogger(GeomacPrevFirePerimOverlay.class.getName());
    private static final String OVERLAY_NAME = NbBundle.getBundle("com.emxsys.wmt.maps.geomac.Bundle").getString("Layers.LayerPrevFirePerims");
    private static final String ICON_BASE = "com/emxsys/wmt/maps/geomac/images/map.png";


    public GeomacPrevFirePerimOverlay()
    {
        // The base class will use the layer's display name to find the GisLayer that will be operated on.
        super(OVERLAY_NAME);

        // Non-lazy initializtion requires us to put the name and icon properties into the action;
        // it's not handled by the registration.
        putValue(Action.NAME, Bundle.CTL_GeomacPrevFirePerimOverlay());
        putValue("iconBase", ICON_BASE);
    }


    @Override
    public void actionPerformed(ActionEvent event)
    {
        // Let the base class perform the toggle action
        super.actionPerformed(event);
    }


    @Override
    public Action createContextAwareInstance(Lookup ignoredActionContext)
    {
        return new GeomacPrevFirePerimOverlay();
    }
}
