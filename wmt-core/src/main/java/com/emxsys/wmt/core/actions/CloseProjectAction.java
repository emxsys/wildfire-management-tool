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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


/**
 * This context sensitive action invokes the OpenProjects.close method action and provides custom UI
 * registration for the Ribbon and Menu Bar.
 * 
 * See ProjectAssistant GlobalContextProviderProxy for insight into how the current project is 
 * maintained.
 *
 * {@link http://wiki.netbeans.org/DevFaqActionContextSensitive}
 *
 * @see OpenProjects
 * @author Bruce Schubert
 */
@ActionID(category = "File", id = "com.emxsys.wmt.core.actions.CloseProjectAction")
@ActionRegistration(iconBase = "images/remove.png",
                    displayName = "#CTL_CloseProjectAction",
                    surviveFocusChange = false,
                    lazy = true)
@ActionReference(path = "Menu/File", position = 500)
//// Nest this ribbon bar button within the Projects dropdown list
//@RibbonActionReference(path = "Ribbon/TaskPanes/Home/Project/Projects", position = 500,
//                       tooltipTitle = "#CTL_CloseProjectAction_TooltipTitle",
//                       tooltipBody = "#CTL_CloseProjectAction_TooltipBody",
//                       tooltipIcon = "images/folder_remove32.png",
//                       tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "images/help.png")
@Messages(
{
    "CTL_CloseProjectAction=Close",
    "CTL_CloseProjectAction_Hint=Close the current project.",
    "CTL_CloseProjectAction_TooltipTitle=Close Project",
    "CTL_CloseProjectAction_TooltipBody=Closes the current project.\n"
    + "If there are modified files, you will be prompted to save the files, "
    + "discard the changes, or cancel the close."
})

public final class CloseProjectAction implements ActionListener
{

    private final Project project;


    public CloseProjectAction(Project context)
    {
        this.project = context;
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        OpenProjects.getDefault().close(new Project[] { project });
    }
}
