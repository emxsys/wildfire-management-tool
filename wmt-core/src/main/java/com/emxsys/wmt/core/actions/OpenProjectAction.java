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
 * This action proxies the default OpenProject action and provides custom UI registration for the
 * Ribbon, Menu Bar and Toolbars. The original OpenProject action references have been hidden from
 * the Menu Bar and Toolbar folders within the XML layer
 *
 * @see org.netbeans.modules.project.ui.actions.OpenProject
 * @author Bruce Schubert
 */
@ActionID(
        category = "Project",
        id = "com.emxsys.wmt.core.actions.OpenProjectAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/core/images/accept.png",
        displayName = "#CTL_OpenProjectAction")
@ActionReferences(
        {
            //    @ActionReference(path = "Menu/File", position = 100),
            @ActionReference(path = "Toolbars/File", position = 401),
            @ActionReference(path = "Shortcuts", name = "O-O")
        })
//// Nest this ribbon bar button within the Projects dropdown list
//@RibbonActionReference(path = "Menu/Home/Project/Projects", position = 200,
//                       tooltipTitle = "#CTL_OpenProjectAction_TooltipTitle",
//                       tooltipBody = "#CTL_OpenProjectAction_TooltipBody",
//                       tooltipIcon = "com/emxsys/wmt/core/images/folder_accept32.png",
//                       tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
@Messages(
        {
            "CTL_OpenProjectAction=Open...",
            "CTL_OpenProjectAction_Hint=Locate and open a project. (ALT-O)",
            "CTL_OpenProjectAction_TooltipTitle=Open Project",
            "CTL_OpenProjectAction_TooltipBody=Choose an existing project to open.\n"
            + "The Open Project Dialog lets you browse your system for an existing project to work with."
        })
public final class OpenProjectAction implements ActionListener {

    private static final String DELEGATE = "Actions/Project/org-netbeans-modules-project-ui-OpenProject.instance";
    private static final Logger logger = Logger.getLogger(OpenProjectAction.class.getName());

    @Override
    public void actionPerformed(ActionEvent event) {
        Action delegate = ModuleUtil.getAction(DELEGATE);
        if (delegate == null) {
            RuntimeException exception = new IllegalArgumentException(DELEGATE + " was not found.");
            logger.severe(exception.toString());
            throw exception;
        }
        logger.info("Delegating to " + DELEGATE);
        delegate.actionPerformed(event);

//        // Normally, the delegate sets the focus to the ProjectManager after opening a project;
//        // this action will attempt to restore the focus the TC that was active when the action
//        // was invoked.
//        TopComponent tc = TopComponent.getRegistry().getActivated();        
//        Project project = ProjectUtil.chooseSingleProject();
//        if (project != null)
//        {
//                Project[] array = new Project[] {project};
//                OpenProjects.getDefault().open(array, false);
//                ProjectUtil.selectInProjectManager(project);
//        }
//
//        if (tc != null)
//        {
//            logger.log(Level.INFO, "Activating {0} TopComponent.", tc.getName());
//            tc.requestActive();
//        }       
    }
}
