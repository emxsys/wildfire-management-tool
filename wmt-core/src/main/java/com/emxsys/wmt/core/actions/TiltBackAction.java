/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com>
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
 * - Neither the name of the Emxsys company nor the names of its 
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
package com.emxsys.wmt.core.actions;

import com.emxsys.wmt.core.capabilities.TiltBackCapability;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Map", id = "com.emxsys.wmt.core.actions.TiltBackAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/core/images/tilt_down.png",
        displayName = "#CTL_TiltBackAction", surviveFocusChange = false)
@ActionReference(path = "Toolbars/Map", position = 3300)
@RibbonActionReference(path = "Ribbon/TaskPanes/Globe/Rotate", position = 300,
        description = "#CTL_TiltBackAction_Hint",
        tooltipTitle = "#CTL_TiltBackAction_TooltipTitle",
        tooltipBody = "#CTL_TiltBackAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/core/images/tilt_down.png",
        tooltipFooter = "#CTL_TiltBackAction_TooltipFooter",
        tooltipFooterIcon = "com/emxsys/wmt/core/images/keyboard24.png",
        autoRepeatAction = true)

@Messages({
    "CTL_TiltBackAction=Tilt Back",
    "CTL_TiltBackAction_Hint=Tilt backwards",
    "CTL_TiltBackAction_TooltipTitle=Tilt Back",
    "CTL_TiltBackAction_TooltipBody=Tilts the view backwards.",
    "CTL_TiltBackAction_TooltipFooter=Keyboard shortcut: [Shift]+[Down Arrow] or [Page Down]"
})
public final class TiltBackAction implements ActionListener {

    private final TiltBackCapability context;

    public TiltBackAction(TiltBackCapability context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        context.tiltBack(e);
    }
}
