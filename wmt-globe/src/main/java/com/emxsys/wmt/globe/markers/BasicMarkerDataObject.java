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
package com.emxsys.wmt.globe.markers;

import com.emxsys.wmt.gis.api.marker.MarkerManager;
import com.emxsys.wmt.util.FilenameUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.xml.sax.SAXException;

/**
 * This class manages a BasicMarker XML file. When loaded, this class will create a BasicMarker
 * derived object based on the information stored in the file.
 *
 * @author Bruce Schubert
 * @see BasicMarker
 */
@Messages({
    "LBL_Marker_LOADER=Marker Files",
    "# {0} - marker name",
    "error_cannot_load_marker=Cannot load marker. {0}",
    "# {0} - marker name",
    "error_cannot_save_marker=Cannot save marker. {0}",
    "# {0} - marker name",
    "error_cannot_delete_marker=Cannot delete marker. {0}",})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_Marker_LOADER",
        mimeType = "text/emxsys-wmt-basicmarker+xml",
        elementNS = {
            "http://emxsys.com/wmt-basicmarker",
            "http://emxsys.com/worldwind-basicmarker"
        })
@DataObject.Registration(
        mimeType = "text/emxsys-wmt-basicmarker+xml",
        iconBase = "com/emxsys/wmt/globe/markers/plain-black.png",
        displayName = "#LBL_Marker_LOADER",
        position = 300)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/emxsys-wmt-basicmarker+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),
    @ActionReference(
            path = "Loaders/text/emxsys-wmt-basicmarker+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300),
    @ActionReference(
            path = "Loaders/text/emxsys-wmt-basicmarker+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500),
    @ActionReference(
            path = "Loaders/text/emxsys-wmt-basicmarker+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600),
    @ActionReference(
            path = "Loaders/text/emxsys-wmt-basicmarker+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800),
//    @ActionReference(
//        path = "Loaders/text/emxsys-worldwind-basicmarker+xml/Actions",
//                     id =
//    @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
//                     position = 900,
//                     separatorAfter = 1000),
//    @ActionReference(
//        path = "Loaders/text/emxsys-worldwind-basicmarker+xml/Actions",
//                     id =
//    @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
//                     position = 1100,
//                     separatorAfter = 1200),
//    @ActionReference(
//        path = "Loaders/text/emxsys-worldwind-basicmarker+xml/Actions",
//                     id =
//    @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
//                     position = 1300),
    @ActionReference(
            path = "Loaders/text/emxsys-wmt-basicmarker+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400)
})
// This file was created by the IDE's New FileType Wizard and then the base class was changed from
// MultiDataObject to XMLDataObject.
public class BasicMarkerDataObject extends XMLDataObject {

    private BasicMarker marker;
    private MarkerManager manager;
    private transient final AtomicReference<State> init = new AtomicReference<>(State.NEW);
    private final transient SaveSupport saveCookie = new SaveSupport();
    private final transient ChangeSupport changeListener = new ChangeSupport();
    private static final long serialVersionUID = 20121126L;
    private static final Logger logger = Logger.getLogger(BasicMarkerDataObject.class.getName());

    private enum State {

        NEW, INITIALIZING, INITIALIZED, DELETING, DELETED, INVALID
    };


    public BasicMarkerDataObject(FileObject primaryFile, MultiFileLoader loader) throws
            DataObjectExistsException, IOException {
        super(primaryFile, loader);
        initialize();
        if (init.get() == State.INITIALIZED) {
            this.marker.setNode(getNodeDelegate());
        }
    }

    protected final void initialize() {
        if (!init.compareAndSet(State.NEW, State.INITIALIZING)) {
            throw new IllegalStateException("Already initialized.");
        }
        readFile();
        init.compareAndSet(State.INITIALIZING, State.INITIALIZED);
    }

    /**
     * Loads and initializes a marker from the persistent store.
     *
     */
    protected final void readFile() {
        logger.log(Level.FINE, "readFile() reading {0}", getName());

        // Don't process template files--the XML is not complete/valid.
        FileObject templates = FileUtil.getConfigFile("Templates/Marker");
        if (templates != null) {
            for (FileObject template : templates.getChildren()) {
                if (template.getName().equals(this.getName())) {
                    init.set(State.INVALID);
                    return;
                }
            }
        }
        // Read the marker file
        try {
            // Build the marker from the Marker.Builder class stored in the XML
            this.marker = (BasicMarker) MarkerSupport.getBuilder(getDocument()).build();
            // Override the property read from the XML, the filename IS the marker name.
            this.marker.setName(FilenameUtils.decodeFilename(getName()));
            // The filesystem ensures unique filenames
            this.marker.setUniqueID(getName());
            // Ok to add the event listener now that the marker is initialized.
            this.marker.addPropertyChangeListener(WeakListeners.propertyChange(this.changeListener, this.marker));
        } catch (IOException | SAXException | RuntimeException exception) {
            logger.severe(Bundle.error_cannot_load_marker(getName() + " caused a " + exception.toString()));
            init.set(State.INVALID);
            return;
        }
        // Associate the marker with the current project's MarkerManager
        try {
            Project owner = FileOwnerQuery.getOwner(getPrimaryFile());
            if (owner == null) {
                throw new RuntimeException("Cannot find the project that owns " + getName());
            }
            this.manager = owner.getLookup().lookup(MarkerManager.class);
            if (this.manager == null) {
                throw new RuntimeException("Cannot find a MarkerManager in " + ProjectUtils.getInformation(owner).getDisplayName());
            }
            this.manager.add(this.marker);
        } catch (RuntimeException exception) {
            logger.log(Level.WARNING, "{0} caused a {1}", new Object[]{
                getName(), exception.toString()
            });
        }
    }

    /**
     * Write the marker to persistent storage.
     */
    protected final void writeFile() {
        try {
            AbstractMarkerWriter writer = this.marker.getLookup().lookup(AbstractMarkerWriter.class);
            if (writer == null) {
                throw new IllegalStateException("marker must have an AbstractMarkerWriter instance in its lookup.");
            }
            // Write to the XML document
            writer.document(getDocument()).write();
            
            // Write the document to disk
            try (OutputStream output = getPrimaryFile().getOutputStream(FileLock.NONE)) {
                XMLUtil.write(this.getDocument(), output, "UTF-8");
                output.flush();
            }
            setModified(false);
            // Add a schema file
            writeSchema(getPrimaryFile().getParent());
        } catch (IOException | SAXException exception) {
            logger.severe(Bundle.error_cannot_save_marker(exception.toString()));
        }
    }

    /**
     * Generates a schema file in the specified folder if one does not exist.
     *
     * @param folder The folder where the schema should reside.
     * @throws IOException
     */
    private static void writeSchema(FileObject folder) throws IOException {
        // Copy an XML schema to the document folder
        File schemaTarget = new File(folder.getPath(), AbstractMarkerWriter.BASIC_MARKER_SCHEMA_FILE);
        if (!schemaTarget.exists()) {
            logger.log(Level.CONFIG, "Creating Schema: {0}", schemaTarget.getPath());
            FileObject schemaSource = MarkerSupport.getLocalSchemaFile("2.0");
            if (schemaSource != null) {
                FileUtil.copyFile(schemaSource, folder, schemaSource.getName());
            } else {
                logger.severe("schemaSource is null!");
            }
        }
    }

    @Override
    protected int associateLookup() {
        return 1; // delegates to getCookieSet() and ensures FileObject, this and Node are in lookup.
    }

    /**
     * Gets a Lookup that includes the Marker, DataObject and Node objects and the associated
     * capabilities.
     *
     * @return the associated Node's lookup.
     */
    @Override
    public Lookup getLookup() {
        if (!this.isValid()) {
            // Don't allow access to the Node after its been "invalidated" by delete()
            logger.log(Level.WARNING, "getLookup() called on an invalid DataObject: {0}", getName());
            return Lookups.fixed(this, this.marker);
        }
        return getNodeDelegate().getLookup();
    }

    /**
     * Delegates to DataObject.getLookup().lookup().
     * @param <T>
     * @param cls
     * @return
     */
    @Override
    public <T extends Cookie> T getCookie(Class<T> cls) {
        return getLookup().lookup(cls);
    }

    /**
     * Create a node for the Marker.
     *
     * @return a BasicMarkerNode
     */
    @Override
    protected Node createNodeDelegate() {
        if (init.get() != State.INITIALIZED) {   // fallback on invalid DataObjects
            return new DataNode(this, Children.LEAF);
        }
        try {
            // Remove the default XML editor and its cookies from this DataObject
            OpenCookie openCookie = getCookieSet().getCookie(OpenCookie.class);
            if (openCookie != null) {
                getCookieSet().remove(openCookie);
            }
            return new BasicMarkerNode(this, this.marker, getCookieSet().getLookup());

        } catch (RuntimeException ex) {
            logger.severe(ex.getMessage());
            return new DataNode(this, Children.LEAF, getCookieSet().getLookup());
        }
    }

    /**
     * Called the Catalog's property change listener; adds/removes the Save cookie.
     *
     * @param modified the new state.
     */
    @Override
    public void setModified(boolean modified) {
        if (init.get() == State.INITIALIZED) {
            if (modified) {
                addSaveCookie();
            } else {
                removeSaveCookie();
            }
            super.setModified(modified);
        }
    }

    /**
     * Adds the SaveCapability cookie.
     */
    private void addSaveCookie() {
        if (getLookup().lookup(SaveCookie.class
        ) == null) {
            getCookieSet()
                    .add(this.saveCookie);
        }
    }

    /**
     * Removes the SaveCapability cookie.
     */
    private void removeSaveCookie() {
        SaveCookie save = getLookup().lookup(SaveCookie.class);
        if (save != null) {
            getCookieSet().remove(save);
        }
    }

    /**
     *
     * @return true if this object is OK to delete.
     */
    boolean canDelete() {
        State state = init.get();
        return (state == State.INITIALIZED || state == State.INVALID);
    }

    /**
     * Deletes Marker instance, then delegates to base class. Called by base class delete()
     *
     * @throws IOException
     */
    @Override
    protected void handleDelete() throws IOException {
        State state = init.get();
        if (state == State.INITIALIZED || state == State.INVALID) {
            init.set(State.DELETING);
            deleteMarker();
            super.handleDelete();
            init.set(State.DELETED);
        } else {
            throw new IllegalStateException("handleDelete() failed - invalid state: " + state);
        }
    }

    /**
     * Removes the marker from the catalog.
     */
    protected final void deleteMarker() {
        if (!this.marker.isDeleted()) {
            this.marker.delete();
        }
        this.manager.remove(this.marker);
    }

    /**
     *
     * @param df
     * @param name
     * @return
     * @throws java.io.IOException
     * @see MarkerCreateFromTemplateHandler
     */
    @Override
    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        DataObject dob = super.handleCreateFromTemplate(df, name);
        if (dob != null) {
            writeSchema(dob.getPrimaryFile().getParent());
        }
        return dob;

    }

    /**
     * Handles changes in the Marker.
     *
     * @author Bruce Schubert
     */
    private class ChangeSupport implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getSource().equals(marker)) {
                switch (pce.getPropertyName()) {
                    case BasicMarker.PROP_MARKER_SELECTED:
                        // ignore selection events...don't set modified flag
                        break;
                    case BasicMarker.PROP_MARKER_DELETED:
                        // The marker was marked for deletion, so delete this DataObject!
                        try {
                            if (canDelete()) {
                                delete();
                            }
                        } catch (IOException exception) {
                            logger.log(Level.SEVERE, "delete() failed: {0}", exception.toString());
                        }
                        break;
                    default:
                        // The marker's internal data changed...notify that a save is needed
                        setModified(true);
                        break;
                }
            }
        }
    }

    /**
     * Save capability class.
     *
     * @author Bruce Schubert
     */
    class SaveSupport implements SaveCookie {

        @Override
        public void save() throws IOException {
            writeFile();
        }
    }
}
