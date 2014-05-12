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
package com.emxsys.wmt.globe.scenes;

import com.emxsys.wmt.gis.api.scene.BasicSceneCatalog;
import com.emxsys.wmt.gis.api.scene.SceneCatalog;
import com.emxsys.wmt.util.FilenameUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

/**
 * This class manages a BasicScene XML file. When loaded, this class will create a BasicScene
 * derived object based on the information stored in the file.
 *
 * @author Bruce Schubert
 * @version $Id: BasicSceneDataObject.java 490 2013-03-02 17:59:27Z bdschubert $
 * @see BasicScene
 */
@Messages(
        {
            "LBL_Scene_LOADER=Scene Files",
            "error_cannot_load_scene=Cannot load scene. {0}",
            "error_cannot_save_scene=Cannot save scene. {0}",
            "error_cannot_delete_scene=Cannot delete scene. {0}",})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_Scene_LOADER",
        mimeType = "text/emxsys-worldwind-basicscene+xml",
        position = 2200,
        elementNS = {"http://emxsys.com/worldwind-basicscene"})
@DataObject.Registration(
        mimeType = "text/emxsys-worldwind-basicscene+xml",
        iconBase = "com/emxsys/wmt/core/images/image.png",
        displayName = "#LBL_Scene_LOADER",
        position = 2200)
@ActionReferences({
    @ActionReference(path = "Loaders/text/emxsys-worldwind-basicscene+xml/Actions",
            id = @ActionID(category = "Scenes", id = "com.emxsys.wmt.globe.actions.RestoreSceneAction"), position = 100,
            separatorAfter = 200),
    @ActionReference(path = "Loaders/text/emxsys-worldwind-basicscene+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"), position = 300),
    @ActionReference(path = "Loaders/text/emxsys-worldwind-basicscene+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"), position = 400,
            separatorAfter = 500),
    @ActionReference(path = "Loaders/text/emxsys-worldwind-basicscene+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), position = 600),
    @ActionReference(path = "Loaders/text/emxsys-worldwind-basicscene+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"), position = 700,
            separatorAfter = 800),
//    @ActionReference(
//        path = "Loaders/text/emxsys-worldwind-basicscene+xml/Actions",
//                     id =
//    @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
//                     position = 900,
//                     separatorAfter = 1000),
//    @ActionReference(
//        path = "Loaders/text/emxsys-worldwind-basicscene+xml/Actions",
//                     id =
//    @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
//                     position = 1100,
//                     separatorAfter = 1200),
//    @ActionReference(
//        path = "Loaders/text/emxsys-worldwind-basicscene+xml/Actions",
//                     id =
//    @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
//                     position = 1300),
    @ActionReference(path = "Loaders/text/emxsys-worldwind-basicscene+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), position = 1400)
})
// This file was created by the IDE's New FileType Wizard and then the base class was changed from
// MultiDataObject to XMLDataObject.
public class BasicSceneDataObject extends XMLDataObject {

    private BasicScene scene;
    private BasicSceneCatalog catalog;
    private transient final AtomicReference<State> init = new AtomicReference<>(State.NEW);
    private transient final SaveSupport saveCookie = new SaveSupport();
    private transient final ChangeSupport changeListener = new ChangeSupport();
    private static final long serialVersionUID = 20121211L;
    private static final Logger logger = Logger.getLogger(BasicSceneDataObject.class.getName());

    private enum State {

        NEW, INITIALIZING, INITIALIZED, DELETING, DELETED, INVALID
    };


    public BasicSceneDataObject(FileObject primaryFile, MultiFileLoader loader) throws
            DataObjectExistsException, IOException {
        super(primaryFile, loader);
        initialize();
    }

    protected final void initialize() {
        if (!init.compareAndSet(State.NEW, State.INITIALIZING)) {
            throw new IllegalStateException("Already initialized.");
        }
        readFile();
        init.compareAndSet(State.INITIALIZING, State.INITIALIZED);
    }

    /**
     * Loads and initializes a scene from the persistent store.
     *
     */
    protected final void readFile() {
        logger.log(Level.FINE, "readFile() called: {0}", getName());

        // Don't process template files--the XML is not complete/valid.
        FileObject templates = FileUtil.getConfigFile("Templates/Scene");
        if (templates != null) {
            for (FileObject template : templates.getChildren()) {
                if (template.getName().equals(this.getName())) {
                    init.set(State.INVALID);
                    return;
                }
            }
        }
        try {
            // Read a the scene from the xml
            this.scene = BasicSceneXmlEncoder.readDocument(getDocument());
            if (this.scene == null) {
                throw new RuntimeException("readDocument() failed: " + getName());
            }

            // Override the name read from the XML with the filename--the filename IS the scene name
            this.scene.setName(FilenameUtils.decodeFilename(getName()));

            // Ok to add event listener now that the scene is initialized.
            this.scene.addPropertyChangeListener(WeakListeners.propertyChange(this.changeListener, this.scene));
        } catch (Exception exception) {
            logger.severe(Bundle.error_cannot_load_scene(exception.toString()));
            init.set(State.INVALID);
            return;
        }
        try {
            // Add this scene to the project's scene catalog
            Project owner = FileOwnerQuery.getOwner(getPrimaryFile());
            if (owner == null) {
                throw new RuntimeException("Cannot find the project that owns " + getName());
            }
            this.catalog = (BasicSceneCatalog) owner.getLookup().lookup(SceneCatalog.class);
            if (this.catalog == null) {
                throw new RuntimeException("Cannot find a BasicSceneCatalog in " + ProjectUtils.getInformation(owner).getDisplayName());
            }
            this.catalog.addScene(this.scene);
        } catch (Exception exception) {
            logger.warning(exception.toString());
        }
    }

    /**
     * Write the scene to persistent storage.
     */
    protected final void writeFile() {
        // Write the data to the XML document
        try {
            BasicSceneXmlEncoder.writeDocument(getDocument(), this.scene);
            OutputStream output = getPrimaryFile().getOutputStream(FileLock.NONE);
            XMLUtil.write(this.getDocument(), output, "UTF-8");
            output.flush();
            output.close();
            setModified(false);

        } catch (Exception exception) {
            logger.severe(Bundle.error_cannot_save_scene(exception.toString()));
        }
    }

    @Override
    protected int associateLookup() {
        return 1; // delegates to getCookieSet() and ensures FileObject, this and Node are in lookup.
    }

    /**
     * Gets a Lookup that includes the Scene, DataObject and Node objects and the associated
     * capabilities.
     *
     * @return the associated Node's lookup.
     */
    @Override
    public Lookup getLookup() {
        if (!this.isValid()) {
            // Don't allow access to the Node after its been "invalidated" by delete()
            logger.log(Level.WARNING, "getLookup() called on an invalid DataObject: {0}", getName());
            return Lookups.fixed(this, this.scene);
        }
        return getNodeDelegate().getLookup();
    }

    /**
     * Delegates to DataObject.getLookup().lookup().
     */
    @Override
    public <T extends Cookie> T getCookie(Class<T> cls) {
        return getLookup().lookup(cls);
    }

    /**
     * Create a node for the Scene.
     *
     * @return a BasicSceneNode
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

            return new BasicSceneNode(this, this.scene, getCookieSet().getLookup());
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
        if (getLookup().lookup(SaveCookie.class) == null) {
            getCookieSet().add(this.saveCookie);
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
     * Deletes Scene instance, then delegates to base class. Called by base class delete()
     *
     * @throws IOException
     */
    @Override
    protected void handleDelete() throws IOException {
        State state = init.get();
        if (state == State.INITIALIZED || state == State.INVALID) {
            init.set(State.DELETING);
            deleteScene();
            super.handleDelete();
            init.set(State.DELETED);
        } else {
            throw new IllegalStateException("handleDelete() failed - invalid state: " + state);
        }
    }

    /**
     * Removes the scene from the catalog.
     */
    protected final void deleteScene() {
        this.catalog.removeScene(this.scene);
    }

    /**
     * Handles changes in the Scene.
     *
     * @author Bruce Schubert
     */
    private class ChangeSupport implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getSource().equals(scene)) {
                // The scene's internal data changed...notify that a save is needed
                setModified(true);
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
