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
package com.emxsys.wmt.project.capabilities;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.scene.Scene;
import com.emxsys.gis.api.scene.Scene.Factory;
import com.emxsys.gis.api.viewer.GisViewer;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.util.ProjectUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * This class provides the capability for restoring a project's last GIS view when it selected. An
 * instance of this class should be placed in a project's lookup.
 *
 * @author Bruce Schubert
 * @version $Id: ProjectSelectionHandler.java 543 2013-04-18 20:13:07Z bdschubert $
 */
public class ProjectSelectionHandler {

    private final Project project;
    private Coord3D lastPosition;
    private Scene lastScene;
    private static Lookup.Result<Project> lookupResults;
    private static LookupListener lookupListener;
    private static Project lastProject;
    private static final Logger logger = Logger.getLogger(ProjectSelectionHandler.class.getName());

    static {
        logger.setLevel(null);
    }

    public ProjectSelectionHandler(Project project) {
        this.project = project;
        initialize();
    }

    /**
     * Creates a LookupListener on the Project.class that handles changes in the project selection.
     *
     */
    private static void initialize() {
        if (lookupResults == null) {
            logger.fine("Initializing...");

            // Monitor the existance of a Project in the global context lookup
            lookupResults = Utilities.actionsGlobalContext().lookupResult(Project.class);

            // Create the listener
            lookupListener = new LookupListener() {
                /**
                 * Determines if the project that is selected or deselected has a
                 * ProjectSelectionHandler
                 */
                @Override
                public void resultChanged(LookupEvent ignored) {
                    Project currProject = ProjectUtil.getCurrentProject();
                    if (currProject != null) {
                        // Handle if the current project is different than the last project
                        if (lastProject != null && !currProject.getProjectDirectory().equals(lastProject.getProjectDirectory())) {
                            ProjectSelectionHandler handler;
                            handler = lastProject.getLookup().lookup(ProjectSelectionHandler.class);
                            if (handler != null) {
                                handler.saveLastPosition();
                            }
                            handler = currProject.getLookup().lookup(ProjectSelectionHandler.class);
                            if (handler != null) {
                                handler.restoreLastPosition();
                            }
                        }
                        lastProject = currProject;
                    }
                }
            };

            // Activate the listener
            lookupResults.addLookupListener(lookupListener);
        }
        // Fire the listener
        lookupListener.resultChanged(null);
    }

    /**
     * Saves the owning project's last position.
     */
    public void saveLastPosition() {
        String displayName = ProjectUtils.getInformation(this.project).getDisplayName();
        GisViewer viewer = Globe.getInstance();
        if (viewer != null) {
            Factory scenefactory = viewer.getLookup().lookup(Factory.class);
            if (scenefactory != null) {
                this.lastScene = scenefactory.createScene();
            }
            this.lastPosition = viewer.getLocationAtCenter();
            logger.log(Level.FINE, "{0}: saved last position - {1}", new Object[]{displayName, lastPosition.toString()});
        }
    }

    /**
     * Restores the owning project's last position.
     */
    public void restoreLastPosition() {
        String displayName = ProjectUtils.getInformation(this.project).getDisplayName();
        GisViewer viewer = Globe.getInstance();
        if (viewer != null) {
            if (this.lastScene != null) {
                logger.log(Level.FINE, "{0}: restoring last scene to {1}", new Object[]{displayName, lastPosition.toString()});
                this.lastScene.restore();
            } else if (this.lastPosition != null) {
                logger.log(Level.FINE, "{0}: restoring last position to {1}", new Object[]{displayName, lastPosition.toString()});
                viewer.centerOn(this.lastPosition);
            }
        }
    }
}
