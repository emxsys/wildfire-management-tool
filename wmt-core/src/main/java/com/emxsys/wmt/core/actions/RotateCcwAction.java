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
import com.emxsys.wmt.core.capabilities.RotateCcwCapability;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Map", id = "com.emxsys.wmt.core.actions.RotateCcwAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/core/images/rotate_ccw.png",
        displayName = "#CTL_RotateCcwAction", surviveFocusChange = false)
@ActionReference(path = "Toolbars/Map", position = 3100)
@RibbonActionReference(path = "Ribbon/TaskPanes/Globe/Rotate", position = 100,
        description = "#CTL_RotateCcwAction_Hint",
        tooltipTitle = "#CTL_RotateCcwAction_TooltipTitle",
        tooltipBody = "#CTL_RotateCcwAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/core/images/rotate_ccw.png",
        tooltipFooter = "#CTL_RotateCcwAction_TooltipFooter",
        tooltipFooterIcon = "com/emxsys/wmt/core/images/keyboard24.png",
        autoRepeatAction = true)

@Messages({
    "CTL_RotateCcwAction=Rotate CCW",
    "CTL_RotateCcwAction_Hint=Rotates view counter-clockwise",
    "CTL_RotateCcwAction_TooltipTitle=Rotate Counter-clockwise",
    "CTL_RotateCcwAction_TooltipBody=Rotates the view counter-clockwise.",
    "CTL_RotateCcwAction_TooltipFooter=Keyboard shortcut: [Shift]+[Right Arrow]"

})
public final class RotateCcwAction implements ActionListener {

    private final RotateCcwCapability context;

    public RotateCcwAction(RotateCcwCapability context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        context.rotateCounterClockwise(e);
    }
}
