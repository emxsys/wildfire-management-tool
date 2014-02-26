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
import com.emxsys.wmt.core.capabilities.LockCapability;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * This is a context sensitive action that toggles the locked state on a LockCapability interface.
 * <p>
 * @see LockCapability
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(category = "Edit", id = "com.emxsys.wmt.core.actions.ToggleLockAction")
@ActionRegistration(displayName = "#CTL_ToggleLockAction",
        surviveFocusChange = false,
        lazy = false)
@ActionReference(path = "Toolbars/Edit", position = 310)
// Create a button on the ribbon bar to represent this action
@RibbonActionReference(path = "Menu/Home/Edit",
        buttonStyle = "toggle",
        priority = "medium",
        description = "#CTL_ToggleLockAction_Hint",
        position = 400,
        autoRepeatAction = false)
@Messages(
        {
            "CTL_ToggleLockAction=Lock",
            "CTL_ToggleLockAction_Hint=Toggle the locked state the selected item; a locked item cannot be moved and/or edited."
        })
public final class ToggleLockAction extends AbstractAction implements LookupListener,
        ContextAwareAction
{

    private static final String ICON_BASE = "com/emxsys/wmt/core/images/lock.png";
    private Lookup.Result<LockCapability> lookupResult;
    private LockCapability context;

    public ToggleLockAction()
    {
        // non-lazy initializtion requires us to put some properties into the action
        putValue(Action.NAME, Bundle.CTL_ToggleLockAction());
        putValue("iconBase", ICON_BASE);
    }

    @Override
    public boolean isEnabled()
    {
        initialize();
        return super.isEnabled();
    }

    /**
     * Initializes the lookupResult and, indirectly, the context.
     */
    private void initialize()
    {
        if (this.lookupResult != null)
        {
            // Already initialized
            return;
        }

        this.lookupResult = Utilities.actionsGlobalContext().lookupResult(LockCapability.class);
        this.lookupResult.addLookupListener(this);

        resultChanged(null);
    }

    @Override
    public void resultChanged(LookupEvent ev)
    {
        boolean shouldEnable = false;
        boolean shouldSelect = false;
        Collection<? extends LockCapability> allInstances = this.lookupResult.allInstances();
        if (allInstances.size() == 1)
        {
            this.context = allInstances.iterator().next();
            shouldSelect = this.context.isLocked();
            shouldEnable = true;
        }
        putValue(Action.SELECTED_KEY, shouldSelect);
        setEnabled(shouldEnable);
    }

    @Override
    public void actionPerformed(ActionEvent ev)
    {
        if (this.context != null)
        {
            this.context.setLocked(!this.context.isLocked());
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {
        return new ToggleLockAction();
    }
}
