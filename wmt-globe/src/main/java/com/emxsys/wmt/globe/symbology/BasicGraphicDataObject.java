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
package com.emxsys.wmt.globe.symbology;

import com.emxsys.gis.api.symbology.GraphicManager;
import com.emxsys.gis.api.symbology.Graphic;
import com.emxsys.util.FilenameUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * This class represents a tactical graphic stored in an XML file.
 *
 * This file was created by the IDE's New FileType Wizard and the base class was changed from
 * MultiDataObject to XMLDataObject.
 *
 * @see org.openide.loaders.XMLDataObject
 * @see BasicGraphic
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: BasicGraphicDataObject.java 490 2013-03-02 17:59:27Z bdschubert $
 */
@Messages({
    "LBL_BasicGraphic_LOADER=Files of Tactical Graphic",
    "error_cannot_load_graphic=Cannot load graphic. {0}",
    "error_cannot_save_graphic=Cannot save graphic. {0}",
    "error_cannot_delete_graphic=Cannot delete graphic. {0}",})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_BasicGraphic_LOADER",
        mimeType = "text/emxsys-worldwind-basicgraphic+xml",
        elementNS
        = {"http://emxsys.com/worldwind-basicgraphic"})
@DataObject.Registration(
        mimeType = "text/emxsys-worldwind-basicgraphic+xml",
        iconBase = "com/emxsys/wmt/globe/symbology/favorite.png",
        displayName = "#LBL_BasicGraphic_LOADER",
        position = 300)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
            id = @ActionID(category = "Edit", id = "com.emxsys.basicui.actions.EditAction"),
            position = 80),
    @ActionReference(
            path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
            id = @ActionID(category = "Edit", id = "com.emxsys.basicui.actions.DesignAction"),
            position = 90),
    @ActionReference(
            path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),
    @ActionReference(
            path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),
    @ActionReference(
            path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300),
    @ActionReference(
            path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500),
    @ActionReference(
            path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600),
    @ActionReference(
            path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800),
//    @ActionReference(
//        path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
//                     id =
//    @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
//                     position = 900,
//                     separatorAfter = 1000),
//    @ActionReference(
//        path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
//                     id =
//    @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
//                     position = 1100,
//                     separatorAfter = 1200),
//    @ActionReference(
//        path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
//                     id =
//    @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
//                     position = 1300),
    @ActionReference(
            path = "Loaders/text/emxsys-worldwind-basicgraphic+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400)
})
public class BasicGraphicDataObject extends AbstractDataObject {

    private BasicGraphic graphic;
    private GraphicManager manager;
    private PropertyChangeListener changeListener;
    private static final long serialVersionUID = 20121129L;
    private static final Logger logger = Logger.getLogger(BasicGraphicDataObject.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    public BasicGraphicDataObject(FileObject pf, MultiFileLoader loader) throws
            DataObjectExistsException, IOException {
        super(pf, loader);  // invokes readFile
        if (init.get() == State.INITIALIZED) {
            this.graphic.setNode((DataNode) getNodeDelegate());
        }
    }

    /**
     * Reads the file and initializes the Graphic object from the XML contents.
     *
     */
    @Override
    protected final void readFile() {
        logger.log(Level.FINER, "readFile() called for {0}", getName());

        // Don't process template files--the XML is not complete/valid.
        FileObject templates = FileUtil.getConfigFile("Templates/Symbology");
        if (templates != null) {
            for (FileObject template : templates.getChildren()) {
                if (template.getName().equals(this.getName())) {
                    init.set(State.INVALID);
                    return;
                }
            }
        }
        // Create the graphic object
        try {
            // Read a the symbol from the XML document
            this.graphic = (BasicGraphic) SymbologySupport.getGraphicBuilder(getDocument()).build();
            if (this.graphic == null) {
                throw new RuntimeException("readFile() failed for " + getName()
                        + ". Reason: The Graphic.Builder returned null.");
            }

            // Override the name with the filename--the filename IS the symbol's name
            this.graphic.setName(FilenameUtils.decodeFilename(getName()));

            // Ok to add event listener now that the symbol is initialized.
            this.changeListener = new ChangeSupport();
            this.graphic.addPropertyChangeListener(WeakListeners.propertyChange(this.changeListener, this.graphic));
        } catch (Exception exception) {
            logger.severe(Bundle.error_cannot_load_tactical_symbol(exception.toString()));
            this.init.set(State.INVALID);
            return;
        }
        // Add this symbol to the project's symbology catalog
        try {
            // Find the project that this symbol belongs to
            Project owner = FileOwnerQuery.getOwner(getPrimaryFile());
            if (owner == null) {
                throw new RuntimeException("Cannot find the project that owns " + getName());
            }
            // Check for a manager object
            this.manager = owner.getLookup().lookup(GraphicManager.class);
            if (this.manager == null) {
                throw new RuntimeException("Cannot find a GraphicManager in " + ProjectUtils.getInformation(owner).getDisplayName());
            }
            // Add to the catalog so that it can be displayed
            this.manager.add(this.graphic);
        } catch (Exception exception) {
            logger.severe(Bundle.error_cannot_addtactical_symbol(exception.toString()));
        }
    }

    /**
     * Write the symbol to persistent storage.
     *
     */
    @Override
    protected void writeFile() {
        try {
            AbstractGraphicWriter writer = this.graphic.getLookup().lookup(AbstractGraphicWriter.class);
            if (writer == null) {
                throw new IllegalStateException("Graphic must have an AbstractSymbolWriter instance in its lookup.");
            }
            // Write to the XML document
            writer.document(getDocument()).write();

            try (OutputStream output = getPrimaryFile().getOutputStream(FileLock.NONE)) {
                XMLUtil.write(getDocument(), output, "UTF-8");
                output.flush();
            }
            setModified(false);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }
    }

    /**
     * Removes the graphic from the catalog. Called by handleDelete().
     */
    protected final void deleteGraphic() {
        if (!this.graphic.isDeleted()) {
            this.graphic.delete();  // fires property change event
        }
        this.manager.remove(this.graphic);
    }

    /**
     * Called by super class delete()
     *
     * @throws IOException
     */
    @Override
    protected void handleDelete() throws IOException {
        if (init.compareAndSet(State.INITIALIZED, State.DELETING)) {
            // delete the graphic object and the file
            deleteGraphic();
            super.handleDelete();
        } else if (init.compareAndSet(State.INVALID, State.DELETING)) {
            // just delete the file
            super.handleDelete();
        } else {
            throw new IllegalStateException("handleDelete() failed for [" + getName() + "] - invalid state: " + init.get());
        }
        init.set(State.DELETED);
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
     * Generates a schema file in the specified folder if one does not exist.
     *
     * @param folder The folder where the schema should reside.
     * @throws IOException
     */
    private static void writeSchema(FileObject folder) throws IOException {
        // Copy an XML schema to the document folder
        File schemaTarget = new File(folder.getPath(), AbstractGraphicWriter.BASIC_GRAPHIC_SCHEMA_FILE);
        if (!schemaTarget.exists()) {
            logger.log(Level.CONFIG, "Creating Schema: {0}", schemaTarget.getPath());
            FileObject schemaSource = SymbologySupport.getLocalSchemaFile("2.0");
            if (schemaSource != null) {
                FileUtil.copyFile(schemaSource, folder, schemaSource.getName());
            } else {
                logger.severe("schemaSource is null!");
            }
        }
    }

    /**
     * Gets a Lookup that includes the Graphic, DataObject and Node objects and the associated
     * capabilities.
     *
     * @return the associated Node's lookup.
     */
    @Override
    public Lookup getLookup() {
        if (!this.isValid()) {
            // Don't allow access to the Node after its been "invalidated" by delete()
            logger.log(Level.WARNING, "getLookup() called on an invalid DataObject in {0}", getName());
            return Lookups.fixed(this, this.graphic);
        }
        return getNodeDelegate().getLookup();
    }

    /**
     * Create a DataNode for the Graphic.
     *
     * @return a BasicGraphicNode
     */
    @Override
    protected Node createNodeDelegate() {
        try {
            if (init.get() != State.INITIALIZED) {
                throw new RuntimeException("createNodeDelegate() state should be INITIALIZED, "
                        + "not " + init.get() + " in  " + getName());
            }
            return new BasicGraphicNode(this, this.graphic, getCookieSet().getLookup());
        } catch (RuntimeException ex) {
            logger.severe(ex.getMessage());
            return new DataNode(this, Children.LEAF, getCookieSet().getLookup());
        }
    }

    /**
     * Handles changes in the Graphic.
     *
     * @author Bruce Schubert
     */
    private class ChangeSupport implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getSource().equals(graphic)) {
                if (pce.getPropertyName().equals(Graphic.PROP_GRAPHIC_SELECTED)) {
                    // ignore...don't set modified flag
                } else if (pce.getPropertyName().equals(Graphic.PROP_GRAPHIC_DELETED)) {
                    // The graphic was marked for deletion, so delete this DataObject!
                    try {
                        if (canDelete()) {
                            delete();
                        }
                    } catch (IOException exception) {
                        logger.log(Level.SEVERE, "delete failed: {0}", exception.toString());
                        init.set(State.INVALID);
                    }
                } else {
                    // The graphic's internal data changed...notify that a save is needed
                    setModified(true);
                }
            }
        }
    }
}
