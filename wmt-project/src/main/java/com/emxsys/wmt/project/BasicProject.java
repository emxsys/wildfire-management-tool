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

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.GeoCoord2D;
import com.emxsys.wmt.gis.api.capabilities.Disposable;
import com.emxsys.wmt.gis.api.marker.MarkerManager;
import com.emxsys.wmt.gis.api.scene.BasicSceneCatalog;
import com.emxsys.wmt.gis.api.symbology.GraphicCatalog;
import com.emxsys.wmt.gis.api.symbology.SymbolCatalog;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.project.capabilities.ProjectSelectionHandler;
import com.emxsys.wmt.wildfire.api.Fireground;
import com.emxsys.wmt.wildfire.api.FiregroundProvider;
import com.emxsys.wmt.wildfire.spi.DefaultFiregroundProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * This class represents a basic Emxsys project in memory. The project's properties are stored in
 * its lookup.
 *
 * This project type can be extended through the ProjectServiceProvider annotation using
 * "com-emxsys-basic-project" for the projectType parameter.
 *
 * @author Bruce Schubert
 * @version $Id: BasicProject.java 695 2013-05-28 11:19:15Z bdschubert $
 */
public class BasicProject implements Project {

    /**
     * "projectType" string used by ProjectServiceProviders extending this project
     */
    public static final String PROJECT_TYPE = "com-emxsys-basic-project";
    public static final String CONFIG_FOLDER_NAME = "config"; //NOI18N
    public static final String DATA_FOLDER_NAME = "data"; //NOI18N
    public static final String MARKER_FOLDER_NAME = "markers"; //NOI18N
    public static final String SCENE_FOLDER_NAME = "scenes"; //NOI18N
    public static final String SYMBOLOGY_FOLDER_NAME = "symbology"; //NOI18N
    public static final String FIREGROUND_FOLDER_NAME = "fireground";  //NOI18N
    public static final String FIREGROUND_FILENAME = "fireground";  //NOI18N
    public static final String STARTUP_LATITUDE = "startupLatitudeDegrees"; //NOI18N
    public static final String STARTUP_LONGITUDE = "startupLongitudeDegrees"; //NOI18N
    public static final boolean CREATE_IF_MISSING = true;
    public static final boolean DO_NOT_CREATE = false;
    static final String CONFIG_PROPFILE_NAME = "emxsys.properties"; //NOI18N
    static final String LEGACY_CONFIG_PROPFILE_NAME = "cps.properties"; //NOI18N
    private final FileObject projectFolder;
    private final ProjectState projectState;
    final BasicProjectProperties projectProperties;
    Lookup baseLookup;
    Lookup compositeLookup;
    InstanceContent content = new InstanceContent();
    //private boolean ungoingDeletion = false;
    private final AtomicReference<State> init = new AtomicReference<>(State.NEW);
    private final AtomicReference<Operation> operation = new AtomicReference<>(Operation.IDLE);
    private static final RequestProcessor THREAD_POOL = new RequestProcessor("BasicProject processor", 1);
    private static final Logger logger = Logger.getLogger(BasicProject.class.getName());

    private enum State {

        NEW, INITILIZING, INITIALIZED, CLOSING, CLOSED, DELETING, DELETED
    };

    private enum Operation {

        IDLE, COPYING, DELETING, MOVING, RENAMING
    };

    static {
        logger.setLevel(Level.ALL);
    }

    /**
     * Lightweight constructor. Heavyweight operations are deferred to the ProjectOpenHook.
     *
     * @param projectFolder the folder containing the project
     * @param projectState the state of the project, e.g., modified
     */
    BasicProject(FileObject projectFolder, ProjectState projectState) {
        this.projectFolder = projectFolder;
        this.projectState = projectState;
        this.projectProperties = loadProjectProperties();
    }

    /**
     * Get the root folder/directory for this project.
     *
     * @return the root folder of the project
     */
    @Override
    public FileObject getProjectDirectory() {
        return this.projectFolder;
    }

    /**
     * Gets the display name for this project.
     *
     * @return the name of this project
     */
    public String getProjectName() {
        return ProjectUtils.getInformation(BasicProject.this).getDisplayName();
    }

    @Override
    public Lookup getLookup() {
        if (this.baseLookup == null) {
            // Project info
            this.content.add(this); // required for LazyLookupProviders in createCompositeLookup()
            this.content.add(new BasicProjectInfo(this));
            this.content.add(new BasicProjectLogicalView(this));
            this.content.add(new BasicProjectCustomizerProvider(this));
            // Now we add our project specific content
            this.content.add(this.projectState);
            this.content.add(this.projectProperties);
            // Actions and handlers
            this.content.add(new ActionProviderImpl());
            this.content.add(new ProjectCopyOperation());
            this.content.add(new ProjectDeleteOperation());
            this.content.add(new ProjectMoveOperation());
            this.content.add(new ProjectOpenedOrClosedHook());
            this.content.add(new ProjectSelectionHandler(this));

            this.baseLookup = new AbstractLookup(this.content);
            // We allow third party lookup providers to extend this projectType when this
            // project's lookup is queried for a service. A service provider class or factory
            // method can extend this project with an annotation like this:
            //  @ProjectServiceProvider(service=..., projectType="com-emxsys-basic-project")
            this.compositeLookup = LookupProviderSupport.createCompositeLookup(
                    this.baseLookup, "Projects/" + PROJECT_TYPE + "/Lookup"); //NOI18N        
        }
        return this.compositeLookup;
    }

    /**
     * Opens the project's data files.
     */
    public void open() {
        boolean isNew = this.init.compareAndSet(State.NEW, State.INITILIZING);
        if (!isNew && !this.init.compareAndSet(State.CLOSED, State.INITILIZING)) {
            throw new IllegalStateException("Cannot open, state must be NEW or CLOSED, not " + this.init.get());
        }
        THREAD_POOL.post(() -> {
            logger.log(Level.INFO, "Loading project {0} data files...", getProjectName()); //NOI18N

            final ProgressHandle handle = ProgressHandleFactory.createHandle("Loading data files...");
            handle.start(); // start in indeterminate mode
            try {
                handle.progress("Loading scenes...");
                loadScenes(SCENE_FOLDER_NAME);

                handle.progress("Loading markers...");
                loadMarkers(MARKER_FOLDER_NAME);

                handle.progress("Loading symbology...");
                loadSymbology(SYMBOLOGY_FOLDER_NAME);

                handle.progress("Loading fireground...");
                loadFireground(FIREGROUND_FOLDER_NAME);

                // Ok to Load/convert file formats in the project root now
                // that the project folder hierarchay has been established
                handle.progress("Loading/converting legacy files...");
                loadLegacyFiles();

                init.set(State.INITIALIZED);
            } catch (Exception exception) {
                logger.severe(exception.toString());
            } finally {
                handle.finish();
                logger.log(Level.INFO, "Finished loading project {0} data files...", getProjectName()); //NOI18N                logger.log(Level.INFO, "Opening project folder {0} ...", getProjectDirectory().getName());
            }
        });
    }

    /**
     * Close the project and release the file resources.
     */
    public void close() {
        if (!this.init.compareAndSet(State.INITIALIZED, State.CLOSING)) {
            throw new IllegalStateException("Cannot close, state must be INITIALIZED, not " + this.init.get());
        }
        MarkerManager markerManager = getLookup().lookup(MarkerManager.class);
        if (markerManager != null) {
            markerManager.dispose();
            this.content.remove(markerManager);
        }
        GraphicCatalog graphicCatalog = getLookup().lookup(GraphicCatalog.class);
        if (graphicCatalog != null) {
            graphicCatalog.dispose();
            this.content.remove(graphicCatalog);
        }
        SymbolCatalog symbolCatalog = getLookup().lookup(SymbolCatalog.class);
        if (symbolCatalog != null) {
            symbolCatalog.dispose();
            this.content.remove(symbolCatalog);
        }
        BasicSceneCatalog sceneCatalog = getLookup().lookup(BasicSceneCatalog.class);
        if (sceneCatalog != null) {
            this.content.remove(sceneCatalog);
        }
        Fireground fireground = getLookup().lookup(Fireground.class);
        if (fireground != null) {
            this.content.remove(fireground);
        }
        // TODO: query project extensions; find a way to signal them that the project is closed.
        Collection<? extends Disposable> lookupAll = this.compositeLookup.lookupAll(Disposable.class);
        lookupAll.stream().forEach((object) -> {
            object.dispose();
        });

        init.set(State.CLOSED);
    }

    /**
     * Creates a Properties object from the configuration file.
     *
     * @returns a Properties object that sets the project's "dirty" state whenever a property is
     * updated.
     * @see ProjectState
     */
    private BasicProjectProperties loadProjectProperties() {
        BasicProjectProperties properties = new BasicProjectProperties(this.projectState);
        FileObject fo = getConfigFolder().getFileObject(CONFIG_PROPFILE_NAME);
        if (fo != null) {
            try {
                properties.load(fo.getInputStream());
            } catch (IOException exception) {
                logger.log(Level.SEVERE, "Cannot load project properties.", exception); //NOI18N
            }
        }
        return properties;
    }

    /**
     * Adds support for Scenes by placing a SceneCatalog in the project lookup.
     *
     * @param folderName name of folder containing scenes
     */
    private void loadScenes(String folderName) {
        logger.log(Level.INFO, "Loading {0} scenes...", getProjectName());
        FileObject subfolder = getSubfolder(getProjectDirectory(), folderName, CREATE_IF_MISSING);
        this.content.add(new BasicSceneCatalog(subfolder));

        // Force the loading of the scenes so scene switching is available when projects are switched
        DataFolder dataFolder = DataFolder.findFolder(subfolder);
        DataObject[] children = dataFolder.getChildren();
        logger.log(Level.FINE, "Loaded {0} scenes.", children.length);

    }

    /**
     * Loads the markers found in a folder, adds support for Markers by placing a MarkerManager in
     * the lookup.
     *
     * @param folderName name of folder containing markers
     */
    private void loadMarkers(String folderName) {
        logger.log(Level.INFO, "Loading {0} markers...", getProjectName());
        FileObject subfolder = getSubfolder(getProjectDirectory(), folderName, CREATE_IF_MISSING);
        this.content.add(new MarkerManager(subfolder));

        // Force the loading of the children so the markers are shown on the map 
        DataFolder dataFolder = DataFolder.findFolder(subfolder);
        DataObject[] children = dataFolder.getChildren();
        logger.log(Level.FINE, "Loaded {0} markers.", children.length);
    }

    /**
     * Loads the MIL-STD 2525C tactical graphics and symbols found in a folder, adds support for
     * Tactical Graphics and Symbols by adding GraphicCatalog and SymbolCatalog to the lookup.
     *
     * @param folderName name of folder containing symbology
     */
    private void loadSymbology(String folderName) {
        logger.log(Level.INFO, "Loading {0} MIL-STD 2525C symbology...", getProjectName());
        FileObject subfolder = getSubfolder(getProjectDirectory(), folderName, CREATE_IF_MISSING);
        this.content.add(new GraphicCatalog(subfolder));
        this.content.add(new SymbolCatalog(subfolder));

        // Force the loading of the children so the symbols appear on the map without any user input
        DataFolder dataFolder = DataFolder.findFolder(subfolder);
        DataObject[] children = dataFolder.getChildren();
        logger.log(Level.FINE, "Loaded {0} symbols and graphics.", children.length);
    }

    /**
     * Adds support for firegrounds by adding a Fireground object to the project's lookup.
     *
     * @param folderName name of folder containing fireground data.
     */
    private void loadFireground(String folderName) {
        logger.log(Level.INFO, "Loading {0} fireground...", getProjectName());
        FileObject subfolder = getSubfolder(getProjectDirectory(), folderName, CREATE_IF_MISSING);
        // Use the factory to get a fireground dataobject from the disk file(s)        
        FiregroundProvider factory = DefaultFiregroundProvider.getInstance();
        // Read the fireground.xml file
        DataObject dataObject = factory.getFiregroundDataObject(subfolder, FIREGROUND_FILENAME);
        if (dataObject == null) {
            // Create the file
            dataObject = factory.newFiregroundDataObject(subfolder, FIREGROUND_FILENAME);
        }
        // Add the Fireground to the project's lookup 
        this.content.add(dataObject.getLookup().lookup(Fireground.class));
    }

    /**
     * Loads and converts and legacy files stored in the root of the project folder.
     *
     */
    private void loadLegacyFiles() {
        logger.log(Level.INFO, "Loading {0} ancillary and legacy project files...", getProjectName());
        // Force the loading of the children so registered dataobjects are processed
        DataFolder dataFolder = DataFolder.findFolder(getProjectDirectory());
        DataObject[] children = dataFolder.getChildren();
        logger.log(Level.FINE, "Loaded {0} files from the project root folder.", children.length);
    }

    /**
     * Returns the meta data file(s) that defines a project. These are found in the config folder.
     * This method is used by the copy and delete operations.
     *
     * @return the meta data file(s) in the config folder
     *
     * @see ProjectCopyOperation
     * @see ProjectDeleteOperation
     */
    private List<FileObject> getMetadataFiles() {
        List<FileObject> metadataFiles = Arrays.asList(getConfigFolder().getChildren());
        return metadataFiles;
    }

    /**
     * Returns the data file(s) within the project. Used by the copy and delete operations.
     *
     * @return the files and folders within the project folder.
     *
     * @see ProjectCopyOperation
     * @see ProjectDeleteOperation
     */
    private List<FileObject> getDataFiles() {
        List<FileObject> dataFiles = Arrays.asList(getProjectDirectory().getChildren());
        return dataFiles;
    }

    private FileObject getConfigFolder() {
        return getSubfolder(this.projectFolder, CONFIG_FOLDER_NAME, CREATE_IF_MISSING);
    }

    /**
     * Utility to get or create a sub-folder.
     *
     * @param parent the parent folder.
     * @param folderName the name of the sub-folder.
     * @param createIfMissing create the sub-folder if it doesn't exist.
     * @return the sub-folder; may be null.
     */
    static FileObject getSubfolder(FileObject parent, String folderName, boolean createIfMissing) {
        if (parent == null) {
            IllegalArgumentException iae = new IllegalArgumentException("parent cannot be null.");
            logger.severe(iae.getMessage());
            throw iae;
        }
        if (folderName == null || folderName.isEmpty()) {
            IllegalArgumentException iae = new IllegalArgumentException("folderName cannot be null or empty.");
            logger.severe(iae.getMessage());
            throw iae;
        }
        FileObject result = parent.getFileObject(folderName);
        if (result == null && createIfMissing) {
            try {
                result = parent.createFolder(folderName);
            } catch (IOException exception) {
                logger.log(Level.SEVERE, "Unable to create folder " + folderName, exception); //NOI18N
            }
        }
        return result;
    }

    public void restoreStartupPosition() throws NumberFormatException {
        String lat = this.projectProperties.getProperty(STARTUP_LATITUDE);
        String lon = this.projectProperties.getProperty(STARTUP_LONGITUDE);
        if (lat != null && lon != null) {
            Coord2D latLon = GeoCoord2D.fromDegrees(Double.parseDouble(lat), Double.parseDouble(lon));

            if (latLon.isMissing()) {
                logger.warning("The startup latitude/longitude is invalid: cannot restore view position."); //NOI18N
                return;
            }
            Globe.getInstance().centerOn(latLon);
        }
    }

    public void saveStartupPosition() {
        Coord3D pos = Globe.getInstance().getLocationAtCenter();
        this.projectProperties.put(STARTUP_LATITUDE, Double.toString(pos.getLatitudeDegrees()));
        this.projectProperties.put(STARTUP_LONGITUDE, Double.toString(pos.getLongitudeDegrees()));
        BasicProjectFactory.saveProjectProperties(this);
    }

    /**
     * Provides the ability for a project to have various actions registered in the project's
     * lookup.
     */
    private class ActionProviderImpl implements ActionProvider {

        private final String[] supported = new String[]{
            ActionProvider.COMMAND_DELETE,
            ActionProvider.COMMAND_COPY,
            ActionProvider.COMMAND_RENAME,
            ActionProvider.COMMAND_MOVE
        };

        @Override
        public String[] getSupportedActions() {
            return this.supported;
        }

        @Override
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            if (command.equalsIgnoreCase(ActionProvider.COMMAND_DELETE)) {
                DefaultProjectOperations.performDefaultDeleteOperation(BasicProject.this);
            } else if (command.equalsIgnoreCase(ActionProvider.COMMAND_COPY)) {
                DefaultProjectOperations.performDefaultCopyOperation(BasicProject.this);
            } else if (command.equalsIgnoreCase(ActionProvider.COMMAND_RENAME)) {
                DefaultProjectOperations.performDefaultRenameOperation(BasicProject.this, null);
            } else if (command.equalsIgnoreCase(ActionProvider.COMMAND_MOVE)) {
                DefaultProjectOperations.performDefaultMoveOperation(BasicProject.this);
            }
        }

        @Override
        public boolean isActionEnabled(String command, Lookup context) throws
                IllegalArgumentException {
            for (String supportedAction : getSupportedActions()) {
                if (command.equals(supportedAction)) {
                    return true;
                }
            }
            throw new IllegalArgumentException(command);
        }
    }

    /**
     * Support class for deleting a project from disk. Used by
     * DefaultProjectOperations.performDefaultDeleteOperation().
     *
     * @see DefaultProjectOperations
     */
    private class ProjectDeleteOperation implements DeleteOperationImplementation {

        @Override
        public void notifyDeleting() throws IOException {
            logger.fine("Project starting delete ...");
            BasicProject.this.init.set(State.DELETING);
        }

        @Override
        public void notifyDeleted() throws IOException {
            logger.info("Project deleted.");
            BasicProject.this.init.set(State.DELETED);
            BasicProject.this.projectState.notifyDeleted();
        }

        @Override
        public List<FileObject> getMetadataFiles() {
            return BasicProject.this.getMetadataFiles();
        }

        @Override
        public List<FileObject> getDataFiles() {
            return BasicProject.this.getDataFiles();
        }
    }

    /**
     * Support class for copying a project to a new location. Used by
     * DefaultProjectOperations.performDefaultDeleteOperation().
     *
     * @see DefaultProjectOperations
     */
    private class ProjectCopyOperation implements CopyOperationImplementation {

        @Override
        public void notifyCopying() throws IOException {
            logger.fine("Project starting copy...");
            BasicProject.this.operation.set(Operation.COPYING);
        }

        @Override
        public void notifyCopied(Project original, File originalPath, String nueName)
                throws IOException {
            logger.log(Level.INFO, "Project copied to {0}", nueName);
            Properties properties = BasicProject.this.getLookup().lookup(Properties.class);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            properties.put("copied", sdf.format(cal.getTime()));
            BasicProject.this.operation.set(Operation.IDLE);
        }

        @Override
        public List<FileObject> getMetadataFiles() {
            return BasicProject.this.getMetadataFiles();
        }

        @Override
        public List<FileObject> getDataFiles() {
            return BasicProject.this.getDataFiles();
        }
    }

    /**
     * Support class for both renaming and moving a project to a new location. Used by
     * DefaultProjectOperations.performDefaultDeleteOperation().
     *
     * @see DefaultProjectOperations
     */
    private class ProjectMoveOperation implements MoveOrRenameOperationImplementation {

        @Override
        public List<FileObject> getMetadataFiles() {
            return BasicProject.this.getMetadataFiles();
        }

        @Override
        public List<FileObject> getDataFiles() {
            return BasicProject.this.getDataFiles();
        }

        @Override
        public void notifyRenaming() throws IOException {
            logger.fine("Project starting rename...");
            BasicProject.this.operation.set(Operation.RENAMING);
        }

        @Override
        public void notifyRenamed(String string) throws IOException {
            logger.log(Level.INFO, "Project renamed to {0}", string);
            BasicProject.this.operation.set(Operation.IDLE);
        }

        @Override
        public void notifyMoving() throws IOException {
            logger.fine("Project starting move...");
            BasicProject.this.operation.set(Operation.MOVING);
        }

        @Override
        public void notifyMoved(Project original, File file, String nueName) throws IOException {
            logger.log(Level.INFO, "Project starting moved to {0}", nueName);
            BasicProject.this.operation.set(Operation.IDLE);
            if (original != null) {
                ProjectState prjState = original.getLookup().lookup(ProjectState.class);
                prjState.notifyDeleted();
            }
        }
    }

    /**
     * This class registers/unregisters a property change listener when the project is opened or
     * closed. This cannot be done in the ProjectFactory due to some threading issues so we perform
     * this action in this "hook" of which an instance is found in the project's lookup.
     *
     * @author Bruce Schubert
     * @version $Id: BasicProject.java 695 2013-05-28 11:19:15Z bdschubert $
     */
    public class ProjectOpenedOrClosedHook extends ProjectOpenedHook {

        @Override
        protected void projectOpened() {
            synchronized (this) {
                logger.log(Level.INFO, "Project opened: {0}", ProjectUtils.getInformation(BasicProject.this).getDisplayName()); //NOI18N
                open();
                //restoreStartupPosition();
            }
        }

        @Override
        protected void projectClosed() {
            String displayName = ProjectUtils.getInformation(BasicProject.this).getDisplayName();
            logger.log(Level.INFO, "Closing project {0}...", displayName); //NOI18N

            // Save the startup position for next time.
            if (operation.get() != Operation.DELETING) {
                // Save the default startup position if it doesn't exist
                BasicProjectProperties properties = BasicProject.this.getLookup().lookup(BasicProjectProperties.class);
                String lat = properties.getProperty(BasicProject.STARTUP_LATITUDE);
                String lon = properties.getProperty(BasicProject.STARTUP_LONGITUDE);
                if (lat == null || lon == null) {
                    logger.log(Level.INFO, "...Saving view coordinates for {0}", displayName); //NOI18N
                    saveStartupPosition();
                }
            }

            // Do the heavy work here
            close();

            logger.log(Level.INFO, "Project closed: {0}", displayName); //NOI18N
        }
    }
}
