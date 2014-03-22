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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;


/**
 * This class provides the application's window title with the selected project's name.
 *
 * @see GlobalActionContextProxy
 * @author Bruce Schubert
 * @version $Id: MainWindowTitleManager.java 344 2012-11-25 21:50:24Z bdschubert $
 */
public class MainWindowTitleManager
{

    private static Lookup.Result<Project> lookupResults;
    private static LookupListener lookupListener;
    private static final Logger logger = Logger.getLogger(MainWindowTitleManager.class.getName());


    static
    {
        logger.setLevel(Level.ALL);
    }


    private MainWindowTitleManager()
    {
    }


    /**
     * Creates a LookupListener on the Project.class that handles changes in the project selection.
     */
    public static void activate()
    {
        if (lookupResults == null)
        {
            logger.config("Initializing global context lookup listener for Projects");

            // Monitor the existance of Projects in the global context lookup
            lookupResults = Utilities.actionsGlobalContext().lookupResult(Project.class);
            // Create the listener on the lookupResults
            lookupListener = new LookupListener()
            {
                // Update window title when the Project changes
                @Override
                public void resultChanged(LookupEvent ignored)
                {
                    String projectName;
                    Collection<? extends Project> projects = lookupResults.allInstances();
                    if (projects.isEmpty())
                    {
                        projectName = "<No Project>";
                    }
                    else if (projects.size() == 1)
                    {
                        Project project = projects.iterator().next();
                        projectName = ProjectUtils.getInformation(project).getDisplayName();
                    }
                    else
                    {
                        projectName = "Multiple Projects";
                    }
                    CurrentProjectTracker.getDefault().updateWindowTitle(projectName);

                }
            };
            // Activate the listener
            lookupResults.addLookupListener(lookupListener);
            lookupListener.resultChanged(null);
        }
    }

}
