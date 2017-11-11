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
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * This is a context sensitive action that toggles the locked state on a BasicSymbol. The symbol
 * must have the BasicSymbol
 * <p>
 * @see BasicSymbol
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(category = "Edit", id = "com.emxsys.wmt.globe.actions.ToggleLockSymbolAction")
@ActionRegistration(displayName = "#CTL_ToggleLockSymbolAction",
        surviveFocusChange = false,
        lazy = false)
// Note, this action registered in a contextual ribbon task pane instead of the general Menu system.
// See layer.xml in this module.
@RibbonActionReference(path = "Ribbon/TaskPanes/SymbolTools/Edit", position = 400,
        buttonStyle = "toggle",
        priority = "top",
        description = "#CTL_ToggleLockSymbolAction_Hint",
        tooltipTitle = "#CTL_ToggleLockSymbolAction_TooltipTitle",
        tooltipBody = "#CTL_ToggleLockSymbolAction_TooltipBody",
        autoRepeatAction = false)
@Messages({
    "CTL_ToggleLockSymbolAction=Lock",
    "CTL_ToggleLockSymbolAction_Hint=Toggle the locked state the selected symbol.",
    "CTL_ToggleLockSymbolAction_TooltipTitle=Toggle Lock",
    "CTL_ToggleLockSymbolAction_TooltipBody=Locks or unlocks the currently selected symbol."
            + "A locked symbol cannot be moved."

})
public final class ToggleLockSymbolAction extends AbstractAction implements LookupListener,
        ContextAwareAction {

    private static final String ICON_BASE = "com/emxsys/wmt/core/images/lock.png";
    private Lookup.Result<BasicSymbol> lookupResult;
    private BasicSymbol context;

    public ToggleLockSymbolAction() {
        // non-lazy initializtion requires us to put some properties into the action
        putValue(Action.NAME, Bundle.CTL_ToggleLockAction());
        putValue("iconBase", ICON_BASE);
    }

    @Override
    public boolean isEnabled() {
        initialize();
        return super.isEnabled();
    }

    /**
     * Initializes the lookupResult and, indirectly, the context.
     */
    private void initialize() {
        if (this.lookupResult != null) {
            // Already initialized
            return;
        }
        this.lookupResult = Utilities.actionsGlobalContext().lookupResult(BasicSymbol.class);
        this.lookupResult.addLookupListener(this);

        resultChanged(null);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        boolean shouldEnable = false;
        boolean shouldSelect = false;
        Collection<? extends BasicSymbol> allInstances = this.lookupResult.allInstances();
        if (allInstances.size() == 1) {
            this.context = allInstances.iterator().next();
            shouldSelect = !this.context.isMovable();
            shouldEnable = true;
        }
        putValue(Action.SELECTED_KEY, shouldSelect);
        setEnabled(shouldEnable);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (this.context != null) {
            this.context.setMovable(!this.context.isMovable());
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ToggleLockSymbolAction();
    }
}
