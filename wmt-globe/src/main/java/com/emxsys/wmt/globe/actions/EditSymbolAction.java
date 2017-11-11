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
package com.emxsys.wmt.globe.actions;

import com.emxsys.wmt.globe.symbology.BasicSymbol;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * This context sensitive action invokes the edit method on BasicSymbols.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(category = "Edit", id = "com.emxsys.wmt.globe.actions.EditSymbolAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/core/images/edit.png",
        displayName = "#CTL_EditSymbolAction")

// Note, this action registered in a contextual ribbon task pane instead of the general Menu system.
// See layer.xml in this module.
@RibbonActionReference(path = "Ribbon/TaskPanes/SymbolTools/Edit", position = 100,
        priority = "top",
        description = "#CTL_EditSymbolAction_Hint",
        tooltipTitle = "#CTL_EditSymbolAction_TooltipTitle",
        tooltipBody = "#CTL_EditSymbolAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/core/images/edit32.png")
//        tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
//        tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
@Messages({
    "CTL_EditSymbolAction=Edit",
    "CTL_EditSymbolAction_Hint=Edit the selected marker.",
    "CTL_EditSymbolAction_TooltipTitle=Edit Symbol",
    "CTL_EditSymbolAction_TooltipBody=Activates the editor for the currently selected symbol."
})
public final class EditSymbolAction implements ActionListener {

    private final BasicSymbol context;

    public EditSymbolAction(BasicSymbol context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        EventQueue.invokeLater(() -> {
            context.edit();
        });
    }
}
