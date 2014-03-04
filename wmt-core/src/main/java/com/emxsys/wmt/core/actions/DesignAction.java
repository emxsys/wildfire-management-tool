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
import com.emxsys.wmt.core.capabilities.DesignCapability;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * This context sensitive action invokes the design method on objects implementing the
 * DesignCapability
 * interface.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(category = "Edit", id = "com.emxsys.wmt.core.actions.DesignAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/core/images/design.png",
        displayName = "#CTL_DesignAction")
//@ActionReference(path = "Menu/Edit", position = 505)
@RibbonActionReference(path = "Menu/Home/Edit",
        position = 200,
        priority = "top",
        description = "#CTL_DesignAction_Hint",
        tooltipTitle = "#CTL_DesignAction_TooltipTitle",
        tooltipBody = "#CTL_DesignAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/core/images/design32.png",
        tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
        tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
@Messages(
        {
            "CTL_DesignAction=Design",
            "CTL_DesignAction_Hint=Edit the selected item's shape.",
            "CTL_DesignAction_TooltipTitle=Design Item",
            "CTL_DesignAction_TooltipBody=Activates the design editor for the currently selected item."
        })
public final class DesignAction implements ActionListener {

    private final DesignCapability context;

    public DesignAction(DesignCapability context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                context.design();
            }
        });
    }
}
