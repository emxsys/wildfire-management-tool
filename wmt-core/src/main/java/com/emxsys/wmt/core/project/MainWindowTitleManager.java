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
package com.emxsys.wmt.core.project;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.windows.WindowManager;

/**
 * This class provides the application's window title with the selected project's name.
 *
 * @author Bruce Schubert
 */
public class MainWindowTitleManager implements PropertyChangeListener {

    private static String originalWindowTitle;
    private static final Logger logger = Logger.getLogger(MainWindowTitleManager.class.getName());

    MainWindowTitleManager() {
    }

    /**
     * Handles changes in the project selection.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (!evt.getPropertyName().equals(CurrentProjectTracker.PROP_CURRENT_PROJECT_LIST)) {
            logger.log(Level.WARNING, "Inappropriate property change event: {0}", evt.getPropertyName());
        }
        String projectName;
        @SuppressWarnings("unchecked")
        List<Project> projects = (List<Project>) evt.getNewValue();
        if (projects.isEmpty()) {
            projectName = "<No Project>";
        }
        else if (projects.size() == 1) {
            Project project = projects.iterator().next();
            projectName = ProjectUtils.getInformation(project).getDisplayName();
        }
        else {
            projectName = "Multiple Projects";
        }
        updateWindowTitle(projectName);
    }

    /**
     * Called to update the window title with a project name.
     * @param projectName name used in window title
     */
    public static void updateWindowTitle(final String projectName) {
        // We have to do this on the AWT thread, so we use the invokeWhenUIReady
        // method which can be called from any thread.
        {
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    Frame mainWindow = WindowManager.getDefault().getMainWindow();
                    if (originalWindowTitle == null) {
                        originalWindowTitle = mainWindow.getTitle();
                    }
                    String title;
                    if (projectName == null || projectName.isEmpty()) {
                        title = originalWindowTitle;
                    }
                    else {
                        title = projectName + " - " + originalWindowTitle;
                    }
                    mainWindow.setTitle(title);
                }
            });
        }
    }

}
