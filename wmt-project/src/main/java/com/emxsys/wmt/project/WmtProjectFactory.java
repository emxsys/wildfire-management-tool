/*
 * Copyright (c) 2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager.Result;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;


/**
 * This ProjectFactory ServiceProvider creates in-memory projects from the disk folder contents.
 *
 * @author Bruce Schubert
 */
@ServiceProvider(service = ProjectFactory.class)
public class WmtProjectFactory implements ProjectFactory2
{

    private static final Logger logger = Logger.getLogger(WmtProjectFactory.class.getName());
    private static final ImageIcon projectIcon = ImageUtilities.loadImageIcon(WmtProjectInfo.ICON_BASE, false);


    /**
     * Performant mechanism to determine if a folder is a project folder -- doesn't call
     * loadProject().
     *
     * @param projectDirectory to test
     * @return project's icon or null.
     */
    @Override
    public Result isProject2(FileObject projectDirectory)
    {
        if (isProject(projectDirectory))
        {
            return new Result(projectIcon);
        }
        return null;
    }


    /**
     * Specifies whether a given folder is a project folder. Test for the existence of
     * emxsys.properties or cps.properties in the config folder.
     *
     * @param folder folder to test
     * @return true if the folder is a WmtProject
     */
    @Override
    public boolean isProject(FileObject folder)
    {
        // Test whether the folder contains a config sub-folder
        FileObject configDir = folder.getFileObject(WmtProject.CONFIG_FOLDER_NAME);
        if (configDir == null || !configDir.isFolder())
        {
            return false;
        }
        // Test for the existance of the project's property file
        boolean fileExists = configDir.getFileObject(WmtProject.CONFIG_PROPFILE_NAME) != null;
        boolean legacyFileExists = configDir.getFileObject(WmtProject.LEGACY_CONFIG_PROPFILE_NAME) != null;

        return fileExists || legacyFileExists;
    }


    /**
     * Load a {@code WmtProject} from a folder on the disk. This method can be invoked via the
     * ProjectChooser UI in its attempt to get ProjectInformation from the project's Lookup; so, the
     * constructor and lookup implementations should be lightweight.
     *
     * @param projectDirectory the root folder of the project.
     * @param state a callback allowing the project to notify ProjectManager if "dirty"
     * @return a new {@code WmtProject} if it exists on disk, else null.
     */
    @Override
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException
    {
        Project project = null;
        if (isProject(projectDirectory))
        {
            project = new WmtProject(projectDirectory, state);
        }
        return project;
    }


    /**
     * Saves the project to the persistent store.
     */
    @Override
    public void saveProject(final Project project)
    {
        // First check that the project folder is there, if not, the project cannot be saved.
        FileObject projectRoot = project.getProjectDirectory();
        if (projectRoot == null)
        {
            RuntimeException exception = new IllegalStateException("Project folder is missing."); //NOI18N
            logger.log(Level.SEVERE, "The project cannot be saved.", exception); //NOI18N
            throw exception;
        }
        // Force the creation of the sub folders if they are missing/deleted:
        WmtProject.getSubfolder(projectRoot, WmtProject.DATA_FOLDER_NAME, WmtProject.CREATE_IF_MISSING);
        // others...

        saveProjectProperties(project);
    }


    /**
     * Saves the project properties.
     *
     * @param project a {@code WmtProject} who's properties will be saved
     */
    static void saveProjectProperties(final Project project)
    {
        FileObject configFolder = WmtProject.getSubfolder(project.getProjectDirectory(), WmtProject.CONFIG_FOLDER_NAME, WmtProject.CREATE_IF_MISSING);
        try
        {
            // Find the project properties file, creating it if necessary:
            FileObject propertiesFile = configFolder.getFileObject(WmtProject.CONFIG_PROPFILE_NAME);
            if (propertiesFile == null)
            {
                propertiesFile = configFolder.createData(WmtProject.CONFIG_PROPFILE_NAME);
            }
            // Get the project properties from the project's Lookup and write them to a property file
            Properties properties = project.getLookup().lookup(ProjectProperties.class);
            File file = FileUtil.toFile(propertiesFile);
            properties.store(new FileOutputStream(file), "Project Properties:"); //NOI18N
        }
        catch (IOException exception)
        {
            logger.log(Level.SEVERE, "Could not save the project properties.", exception); //NOI18N
        }
    }
}
