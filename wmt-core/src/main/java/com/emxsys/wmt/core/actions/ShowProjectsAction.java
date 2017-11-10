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

import com.emxsys.util.ModuleUtil;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * This action proxies the default show projects logical tab and provides a custom UI registration
 * for the Menu Bar and Toolbars. The original org-netbeans-modules-project-ui-logical-tab-action
 * action references have been hidden from the Menu Bar folders within the XML layer.
 *
 * @see org-netbeans-modules-project-ui-logical-tab-action
 * @author Bruce Schubert
 */
@ActionID(category = "File", id = "com.emxsys.wmt.core.actions.ShowProjectsAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/core/images/folder_info.png",
        displayName = "#CTL_ShowProjectsAction",
        surviveFocusChange = false,
        lazy = true)
@RibbonActionReference(path = "Ribbon/TaskPanes/Window/Show", position = 2100,
        tooltipTitle = "#CTL_ShowProjectsAction_TooltipTitle",
        tooltipBody = "#CTL_ShowProjectsAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/core/images/folder_info32.png",
        tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
        tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
@Messages({
    "CTL_ShowProjectsAction=Show Projects",
    "CTL_ShowProjectsAction_Hint=Show the Projects window.",
    "CTL_ShowProjectsAction_TooltipTitle=Show Open Projects",
    "CTL_ShowProjectsAction_TooltipBody=Shows your open projects. \n"
    + "Displays the \"logical view\" (versus physical view) of your open projects."
})

public final class ShowProjectsAction implements ActionListener {

    private static final String DELEGATE = "Actions/Project/org-netbeans-modules-project-ui-logical-tab-action.instance";
    private static final Logger logger = Logger.getLogger(ShowProjectsAction.class.getName());

    @Override
    public void actionPerformed(ActionEvent event) {
        Action delegate = ModuleUtil.getAction(DELEGATE);
        if (delegate == null) {
            RuntimeException exception = new RuntimeException(DELEGATE + " was not found.");
            logger.log(Level.SEVERE, DELEGATE, exception);
            throw exception;
        }
        logger.fine("Delegating to " + DELEGATE);
        delegate.actionPerformed(event);
    }
}
