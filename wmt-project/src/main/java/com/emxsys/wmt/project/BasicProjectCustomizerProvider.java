/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
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
package com.emxsys.wmt.project;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Bruce Schubert
 * @version $Id: BasicProjectCustomizerProvider.java 446 2012-12-12 17:25:13Z bdschubert $
 */
public class BasicProjectCustomizerProvider implements CustomizerProvider
{

    private final Project project;
    public static final String CUSTOMIZER_FOLDER_PATH =
        "Projects/com-emxsys-wmt-project/Customizer";


    public BasicProjectCustomizerProvider(Project project)
    {
        this.project = project;
    }


    @Override
    public void showCustomizer()
    {
        Dialog dialog = ProjectCustomizer.createCustomizerDialog(
            //Path to layer folder:
            CUSTOMIZER_FOLDER_PATH,
            //Lookup, which must contain, at least, the Project:
            Lookups.fixed(project),
            //Preselected category:
            "",
            //OK button listener:
            new OkOptionListener(),
            //HelpCtx for Help button of dialog:
            null);
        dialog.setTitle(ProjectUtils.getInformation(project).getDisplayName());
        dialog.setVisible(true);
    }


    private class OkOptionListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            StatusDisplayer.getDefault().setStatusText("OK button clicked for "
                + project.getProjectDirectory().getName() + " customizer!");
        }
    }
}
