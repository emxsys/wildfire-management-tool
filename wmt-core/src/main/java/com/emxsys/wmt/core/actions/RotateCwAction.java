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

import com.terramenta.ribbon.RibbonActionReference;
import com.emxsys.wmt.core.capabilities.RotateCwCapability;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Map", id = "com.emxsys.wmt.core.actions.RotateCwAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/core/images/rotate_cw.png",
        displayName = "#CTL_RotateCwAction", surviveFocusChange = false)
@ActionReference(path = "Toolbars/Map", position = 5200)
@RibbonActionReference(path = "Menu/Home/Rotate",
        position = 200,
        description = "#CTL_RotateCwAction_Hint",
        tooltipTitle = "#CTL_RotateCwAction_TooltipTitle",
        tooltipBody = "#CTL_RotateCwAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/core/images/rotate_cw.png",
        tooltipFooter = "#CTL_RotateCwAction_TooltipFooter",
        tooltipFooterIcon = "com/emxsys/wmt/core/images/keyboard24.png",
        autoRepeatAction = true)

@Messages({
    "CTL_RotateCwAction=Rotate CW",
    "CTL_RotateCwAction_Hint=Rotates view clockwise",
    "CTL_RotateCwAction_TooltipTitle=Rotate Clockwise",
    "CTL_RotateCwAction_TooltipBody=Rotates the view clockwise.",
    "CTL_RotateCwAction_TooltipFooter=Keyboard shortcut: [Shift]+[Left Arrow]"

})
public final class RotateCwAction implements ActionListener {

    private final RotateCwCapability context;

    public RotateCwAction(RotateCwCapability context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        context.rotateClockwise(e);
    }
}
