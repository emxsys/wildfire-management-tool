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

import com.emxsys.wmt.ribbon.RibbonActionReference;
import com.emxsys.wmt.core.capabilities.PresentStatusCapability;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


/**
 * A context sensitive action for setting an item's status to Present/Existing.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(category = "Edit", id = "com.emxsys.wmt.core.actions.MakePresentAction")
@ActionRegistration(iconBase = "images/status_unknown_present.png",
                    displayName = "#CTL_MakePresentAction")
@ActionReferences(
{
})
@RibbonActionReference(path = "Ribbon/TaskPanes/Home/Edit/Status",
                       position = 100,
                       description = "#CTL_MakePresentAction_Hint",
                       autoRepeatAction = false)
@Messages(
{
    "CTL_MakePresentAction=Present",
    "CTL_MakePresentAction_Hint=Sets the selected item's determination/declaration to present or existing."
})
public final class MakePresentAction implements ActionListener
{

    private final PresentStatusCapability context;


    public MakePresentAction(PresentStatusCapability context)
    {
        this.context = context;
    }


    @Override
    public void actionPerformed(ActionEvent ev)
    {
        context.makeStatusExistingOrPresent();
    }
}