/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
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
package com.emxsys.wmt.core.utilities;

import java.awt.FlowLayout;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Utilities for interacting with Projects.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages(
{
    "dialogTitle=Select a Main Project"
})
public final class ProjectUtil
{

    public static final String PROJECT_LOGICAL_TAB_ID = "projectTabLogical_tc";
    public static final String PROJECT_FILE_TAB_ID = "projectTab_tc";


    /**
     * Gets the current project from the global context.
     *
     * @return the current project, or null if not found
     */
    public static Project getCurrentProject()
    {
        // Get the current selection...
        Project project = Utilities.actionsGlobalContext().lookup(Project.class);
        // ... or search the current project hierarchy
        if (project == null)
        {
            project = findProjectThatOwnsNode(Utilities.actionsGlobalContext().lookup(Node.class));
        }
        return project;
    }


    /**
     * Recursively searches the node hierarchy for the project that owns a node.
     *
     * @param node a node to test for a Project in its or its ancestor's lookup.
     * @return the Project that owns the node, or null if not found
     */
    public static Project findProjectThatOwnsNode(Node node)
    {
        if (node != null)
        {
            Project project = node.getLookup().lookup(Project.class);
            if (project == null)
            {
                DataObject dataObject = node.getLookup().lookup(DataObject.class);
                if (dataObject != null)
                {
                    project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
                }
            }
            return (project == null) ? findProjectThatOwnsNode(node.getParentNode()) : project;
        }
        else
        {
            return null;
        }

    }


    /**
     * Gets the Project Manager TopComponent window. Finds the TopComponent with the
     * PROJECT_LOGICAL_TAB_ID id.
     *
     * @return TopComponent with PROJECT_LOGICAL_TAB_ID
     */
    public static ExplorerManager getProjectsExplorerManager()
    {
        TopComponent tc = WindowManager.getDefault().findTopComponent(PROJECT_LOGICAL_TAB_ID);
        return ((ExplorerManager.Provider) tc).getExplorerManager();

    }


    /**
     * Selects the supplied project in the Project Manager window.
     *
     * @param project to be selected
     */
    public static void selectInProjectManager(Project project)
    {
        final TopComponent projectTab = WindowManager.getDefault().findTopComponent(PROJECT_LOGICAL_TAB_ID);
        ExplorerManager em = ((ExplorerManager.Provider) projectTab).getExplorerManager();
        Node root = em.getRootContext();
        Node projNode = root.getChildren().findChild(ProjectUtils.getInformation(project).getName());
        if (projNode != null)
        {
            try
            {
                projectTab.open();
                projectTab.requestActive();
                Node[] nodes = new Node[]{ projNode };
                em.setExploredContextAndSelection(projNode, nodes);
                projectTab.setActivatedNodes(nodes);
            }
            catch (Exception ignore)
            {
                // may ignore it
            }
        }
    }


    /**
     * Selects the supplied Node in the current project in the Project Manager window.
     *
     * @param node to be selected
     */
    public static void selectInCurrentProject(Node node)
    {
        Project currentProject = getCurrentProject();
        Project nodesProject = findProjectThatOwnsNode(node);
        if (currentProject != null && currentProject.equals(nodesProject))
        {
            ExplorerManager em = getProjectsExplorerManager();
            try
            {
                em.setSelectedNodes(new Node[]{ node });
            }
            catch (PropertyVetoException ignored)
            {
            }
        }
    }


    /**
     * Allows the user to choose a single project directory to open.
     *
     * @return
     */
    static public Project chooseSingleProject()
    {
        // Get the ProjectChooserFactory that's registered in the global lookup 
        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.setMultiSelectionEnabled(false);

        // Show the chooser
        int option = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());

        if (option == JFileChooser.APPROVE_OPTION)
        {

            File projectToBeOpenedFile = chooser.getSelectedFile();
            FileObject projectToBeOpened = FileUtil.toFileObject(projectToBeOpenedFile);
            try
            {
                return ProjectManager.getDefault().findProject(projectToBeOpened);
            }
            catch (Exception exeception)
            {
                Exceptions.printStackTrace(exeception);
            }
        }
        return null;

    }


    /**
     * Gets the main project if one has been designated; if not designated, this method will prompt
     * the user to select a main project.
     *
     * @return the designated project, or null if not determined.
     * @deprecated Use getCurrentProject() instead.
     */
    @Deprecated()
    public static Project getMainProject()
    {
        Project project = OpenProjects.getDefault().getMainProject();
        if (project == null)
        {
            project = forceMainProjectSelection();
        }
        return project;

    }


    /**
     * Force the selection of a main project.
     *
     * This code was inspired by the Puzzle GIS project by Johann Sorel.
     *
     * @return the designated project, or null if not selected
     * @see org.puzzle.core.actions.ActionUtils
     */
    public static Project forceMainProjectSelection()
    {
        Project candidate = null;
        final Project[] projects = OpenProjects.getDefault().getOpenProjects();

        if (projects.length == 1)
        {
            candidate = projects[0];
            OpenProjects.getDefault().setMainProject(candidate);
        }
        else if (projects.length > 1)
        {

            Vector<ProjectName> projectNames = new Vector<ProjectName>();
            for (Project project : projects)
            {
                projectNames.add(new ProjectName(project));
            }
            final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            final JLabel lbl = new JLabel(Bundle.dialogTitle());
            final JComboBox box = new JComboBox(projectNames);
            box.setSelectedIndex(0);
            panel.add(lbl);
            panel.add(box);

            final NotifyDescriptor desc = new DialogDescriptor(panel, "", true, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.OK_OPTION, null);
            DialogDisplayer.getDefault().notify(desc);
            candidate = ((ProjectName) box.getSelectedItem()).project;
            OpenProjects.getDefault().setMainProject(candidate);
        }

        return candidate;
    }


    private static class ProjectName
    {

        Project project;
        String name;


        public ProjectName(Project project)
        {
            this.project = project;
            ProjectInformation info = ProjectUtils.getInformation(project);
            if (info != null)
            {
                name = info.getDisplayName();
            }
            else
            {
                name = project.getProjectDirectory().getName();
            }
        }


        @Override
        public String toString()
        {
            return name;
        }
    }
}
