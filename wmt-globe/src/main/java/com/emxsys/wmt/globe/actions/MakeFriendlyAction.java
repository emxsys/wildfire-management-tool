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

import com.terramenta.ribbon.RibbonActionReference;
import com.emxsys.wmt.globe.capabilities.FriendlyAffiliationCapability;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Edit", id = "com.emxsys.wmt.globe.actions.MakeFriendlyAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/globe/images/affiliation_friend_gnd.png",
        displayName = "#CTL_MakeFriendlyAction")

@RibbonActionReference(path = "Ribbon/TaskPanes/SymbolTools/Affiliation",
        position = 100,
        description = "#CTL_MakeFriendlyAction_Hint",
        autoRepeatAction = false)
@Messages(
        {
            "CTL_MakeFriendlyAction=Friendly",
            "CTL_MakeFriendlyAction_Hint=Sets the selected object's affiliation to friend."
        })
public final class MakeFriendlyAction implements ActionListener {

    private final FriendlyAffiliationCapability context;

    public MakeFriendlyAction(FriendlyAffiliationCapability context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.makeAffiliationFriendly();
    }
}
