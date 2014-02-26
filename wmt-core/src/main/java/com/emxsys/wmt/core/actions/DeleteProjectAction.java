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
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


/**
 * This context sensitive action invokes the DefaultProjectOperations.performDefaultDeleteOperation
 * method and provides custom UI registration for the Ribbon and Menu Bar.
 * 
 * See ProjectAssistant GlobalContextProviderProxy for insight into how the current project is 
 * maintained.
 *
 * @see DefaultProjectOperations
 * @author Bruce Schubert
 */
@ActionID(category = "File", id = "com.emxsys.wmt.core.actions.DeleteProjectAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/core/images/delete.png",
                    displayName = "#CTL_DeleteProjectAction",
                    surviveFocusChange = false,
                    lazy = true)
//@ActionReference(path = "Menu/File", position = 600)
//// Nest this ribbon bar button within the Projects dropdown list
//@RibbonActionReference(path = "Menu/Home/Project/Projects", position = 600,
//                       tooltipTitle = "#CTL_DeleteProjectAction_TooltipTitle",
//                       tooltipBody = "#CTL_DeleteProjectAction_TooltipBody",
//                       tooltipIcon = "com/emxsys/wmt/core/images/folder_delete32.png",
//                       tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
@Messages(
{
    "CTL_DeleteProjectAction=Delete...",
    "CTL_DeleteProjectAction_Hint=Delete the current project.",
    "CTL_DeleteProjectAction_TooltipTitle=Delete Project",
    "CTL_DeleteProjectAction_TooltipBody=Removes the current project from your system.\n"
    + "The Delete Project dialog lets you delete project while leaving the data intact, "
    + "or you can delete the entire project folder and all of its contents."
})
public final class DeleteProjectAction implements ActionListener
{

    private final Project project;


    public DeleteProjectAction(Project context)
    {
        this.project = context;
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        // See ProjectAssistant GlobalContextProviderProxy
        DefaultProjectOperations.performDefaultDeleteOperation(project);
    }
}
//public final class DeleteProjectAction extends AbstractProjectContextAction
//{
//
//    //private static final String ACTION_TO_PROXY = "Actions/Project/org-netbeans-modules-project-ui-DeleteProject.instance";
//    private static final Logger logger = Logger.getLogger(OpenProjectAction.class.getName());
//
//
//    public DeleteProjectAction()
//    {
//        this(Utilities.actionsGlobalContext());
//    }
//
//
//    private DeleteProjectAction(Lookup actionContext)
//    {
//        super(actionContext);
//
//        // iconBase is unused with "eager" ActionRegistrations, so we must set it ourselves
//        this.putValue("iconBase", "com/emxsys/wmt/core/images/delete.png");
//        this.putValue(Action.NAME, Bundle.CTL_DeleteProjectAction());
//    }
//
//
//    @Override
//    public void actionPerformed(ActionEvent event)
//    {
//        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
//        for (Project project : openProjects)
//        {
//            DefaultProjectOperations.performDefaultDeleteOperation(project);
//        }
//
//    }
//
//
//    @Override
//    public Action createContextAwareInstance(Lookup actionContext)
//    {
//        return new DeleteProjectAction(actionContext);
//    }
//}
