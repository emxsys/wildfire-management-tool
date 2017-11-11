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

import com.emxsys.wmt.globe.layers.BackgroundLayers;
import com.emxsys.wmt.globe.layers.WidgetLayers;
import com.terramenta.ribbon.RibbonActionReference;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Map",
        id = "com.emxsys.wmt.globe.actions.EffectSunlight")
@ActionRegistration(
        displayName = "#CTL_EffectSunlight",
        lazy = false)
@ActionReference(path = "Toolbars/Effects", position = 1300)
@RibbonActionReference(path = "Ribbon/TaskPanes/Globe/Manage/Effects/Controls", position = 300,
        buttonStyle = "toggle",
        description = "#CTL_EffectSunlight_Hint",
        priority = "top",
        tooltipTitle = "#CTL_EffectSunlight_TooltipTitle",
        tooltipBody = "#CTL_EffectSunlight_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/images/sun24.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages({
    "CTL_EffectSunlight=Sunlight",
    "CTL_EffectSunlight_Hint=Sunlight Effect",
    "CTL_EffectSunlight_TooltipTitle=Sunlight Effect",
    "CTL_EffectSunlight_TooltipBody=Toggle the display of the sunlight."
})
public final class EffectSunlight extends AbstractGisLayerToggleAction {

    private static final String ICON_BASE = "com/emxsys/wmt/globe/images/sun.png";
    private static final String EFFECT_NAME = BackgroundLayers.LAYER_SUNLIGHT;

    public EffectSunlight() {
        super(EFFECT_NAME);
        // non-lazy initializtion requires us to put some properties into the action
        putValue(Action.NAME, Bundle.CTL_EffectSunlight());
        putValue("iconBase", ICON_BASE);

    }

    @Override
    public Action createContextAwareInstance(Lookup ignoredActionContext) {
        return new EffectSunlight();
    }
}
