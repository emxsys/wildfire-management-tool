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

import com.emxsys.wmt.core.welcome.WelcomeComponent;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Show the welcome screen.
 *
 * @author Bruce Schubert
 */
@ActionID(id = "com.emxsys.wmt.core.welcome.ShowWelcomeAction", category = "Help")
@ActionRegistration(displayName = "#CTL_ShowWelcomeAction",
        iconBase = "com/emxsys/wmt/core/images/light_bulb_info.png",
        iconInMenu = false)
@RibbonActionReference(path = "Menu/Window/Show", position = 3000,
        tooltipTitle = "#CTL_ShowWelcomeAction_TooltipTitle",
        tooltipBody = "#CTL_ShowWelcomeAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/core/images/light_bulb_info32.png",
        tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
        tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
@Messages({
    "LBL_Action=Start Page",
    "CTL_ShowWelcomeAction=Show Start Page",
    "CTL_ShowWelcomeAction_Hint=Show the Start Page window.",
    "CTL_ShowWelcomeAction_TooltipTitle=Show Start Page",
    "CTL_ShowWelcomeAction_TooltipBody=Shows the Start Page window. \n"
    + "This \"welcome page\" contains tips and news about the software."
})
public class ShowWelcomeAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        WelcomeComponent topComp = null;
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        for (TopComponent tc : tcs) {
            if (tc instanceof WelcomeComponent) {
                topComp = (WelcomeComponent) tc;
                break;
            }
        }
        if (topComp == null) {
            topComp = WelcomeComponent.findComp();
        }

        topComp.open();
        topComp.requestActive();
    }
}
