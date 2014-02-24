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
import com.emxsys.wmt.core.capabilities.DeleteCapability;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


/**
 * This context sensitive action invokes the delete method on a DeleteCapability interface.
 *
 * @see DeleteCapability
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(category = "Edit", id = "com.emxsys.wmt.core.actions.DeleteAction")
@ActionRegistration(iconBase = "images/delete.png",
                    displayName = "#CTL_DeleteAction",
                    surviveFocusChange = false,
                    key = "delete")
@ActionReferences(
{
    //@ActionReference(path = "Menu/Edit", position = 300),
    @ActionReference(path = "Toolbars/Edit", position = 300)
})
@RibbonActionReference(path = "Menu/Home/Edit", 
                       position = 300, 
                       priority = "top",
                       description = "#CTL_DeleteAction_Hint", 
                       autoRepeatAction = false,
                       tooltipTitle = "#CTL_DeleteAction_TooltipTitle",
                       tooltipBody = "#CTL_DeleteAction_TooltipBody",
                       tooltipIcon = "images/delete32.png",
                       tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
                       tooltipFooterIcon = "images/help.png")
                       
@Messages(
{
    "CTL_DeleteAction=Delete",
    "CTL_DeleteAction_Hint=Delete the selected item",
    "CTL_DeleteAction_TooltipTitle=Delete Item",
    "CTL_DeleteAction_TooltipBody=Delete the currently selected item.\n"
    + "Be cautious, you may not be able to recover the deleted item."
})
public final class DeleteAction implements ActionListener
{

    private final DeleteCapability context;


    public DeleteAction(DeleteCapability context)
    {
        this.context = context;
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        context.delete();
    }
}
