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
import com.emxsys.wmt.util.ModuleUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * This action proxies the default NewProject action and provides custom UI registration for the
 * Ribbon, Menu Bar and Toolbars. The original NewProject action references have been hidden from
 * the
 * Menu Bar and Toolbars folders within the XML layer
 *
 * @see org.netbeans.modules.project.ui.actions.NewProject
 * @author Bruce Schubert
 */
@ActionID(
        category = "Project",
        id = "com.emxsys.wmt.core.actions.NewProjectAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/core/images/add.png",
        displayName = "#CTL_NewProjectAction")
@ActionReferences(
        {
            //@ActionReference(path = "Menu/File", position = 200),
            @ActionReference(path = "Toolbars/File", position = 100),
            @ActionReference(path = "Shortcuts", name = "O-N")
        })
// Nest this ribbon bar button within the Projects dropdown list
//@RibbonActionReference(path = "Menu/Home/Project/Projects", position = 100,
//                       tooltipTitle = "#CTL_NewProjectAction_TooltipTitle",
//                       tooltipBody = "#CTL_NewProjectAction_TooltipBody",
//                       tooltipIcon = "com/emxsys/wmt/core/images/folder_add32.png",
//                       tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
@Messages(
        {
            "CTL_NewProjectAction=New...",
            "CTL_NewProjectAction_Hint=Creates and opens a new project. (ALT-N)",
            "CTL_NewProjectAction_TooltipTitle=New Project",
            "CTL_NewProjectAction_TooltipBody=Create and open a new project.\n"
            + "The New Project Wizard allows you to select a template for your new project."
        })
public final class NewProjectAction implements ActionListener {

    private static final Logger logger = Logger.getLogger(NewProjectAction.class.getName());
    private static final String ACTION_TO_PROXY = "Actions/Project/org-netbeans-modules-project-ui-NewProject.instance";

    @Override
    public void actionPerformed(ActionEvent event) {
        Action proxiedAction = ModuleUtil.getAction(ACTION_TO_PROXY);
        if (proxiedAction == null) {
            RuntimeException exception = new IllegalArgumentException(ACTION_TO_PROXY + " was not found.");
            logger.severe(exception.toString());
            throw exception;
        }
        proxiedAction.actionPerformed(event);
    }
}
