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
import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * This context sensitive action invokes the DefaultProjectOperations.performDefaultRenameOperation
 * and provides custom UI registration for the Ribbon and Menu Bar.
 *
 * @see DefaultProjectOperations
 * @author Bruce Schubert
 */
@ActionID(
        category = "File",
        id = "com.emxsys.wmt.core.actions.RenameProjectAction")
@ActionRegistration(
        displayName = "#CTL_RenameProjectAction",
        surviveFocusChange = true,
        lazy = false)
//@ActionReference(path = "Menu/File", position = 300)
//// Nest this ribbon bar button within the Projects dropdown list
//@RibbonActionReference(path = "Ribbon/TaskPanes/Home/Project/Projects", position = 300,
//                       tooltipTitle = "#CTL_RenameProjectAction_TooltipTitle",
//                       tooltipBody = "#CTL_RenameProjectAction_TooltipBody",
//                       tooltipIcon = "com/emxsys/wmt/core/images/folder_edit32.png",
//                       tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
@Messages(
        {
            "CTL_RenameProjectAction=Rename...",
            "CTL_RenameProjectAction_Hint=Rename the current project.",
            "CTL_RenameProjectAction_TooltipTitle=Rename Project",
            "CTL_RenameProjectAction_TooltipBody=Assign a new name the project and the project folder.\n"
            + "The Rename Project dialog lets change the display name of the project, "
            + "and it lets you can change the name of folder that contains the project's files."
        })
public final class RenameProjectAction extends AbstractProjectContextAction {

    public RenameProjectAction() {
        this(Utilities.actionsGlobalContext());
    }

    private RenameProjectAction(Lookup actionContext) {
        super(actionContext);
        // iconBase is unused with "eager" ActionRegistrations, so we must set it ourselves
        this.putValue("iconBase", "com/emxsys/wmt/core/images/edit.png");
        this.putValue(Action.NAME, Bundle.CTL_RenameProjectAction());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (Project project : openProjects) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new RenameProjectAction(actionContext);
    }
}
