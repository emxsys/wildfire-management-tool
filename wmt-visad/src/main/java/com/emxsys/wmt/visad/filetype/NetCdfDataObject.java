/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.visad.filetype;

import com.emxsys.wmt.util.TimeUtil;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import visad.DataImpl;
import visad.VisADException;
import visad.data.netcdf.Plain;

@Messages(
        {
            "LBL_NetCdf_LOADER=Files of NetCDF"
        })
// Register the position late in the sequence so specialized .nc loaders can take presedence.
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_NetCdf_LOADER",
        mimeType = "application/x-netcdf",
        extension
        = {
            "nc"
        },
        position = 10000)
@DataObject.Registration(
        mimeType = "application/x-netcdf",
        iconBase = "com/emxsys/wmt/visad/images/database.png",
        displayName = "#LBL_NetCdf_LOADER",
        position = 300)
@ActionReferences(
        {
            @ActionReference(
                    path = "Loaders/application/x-netcdf/Actions",
                    id
                    = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
                    position = 100,
                    separatorAfter = 200),
            @ActionReference(
                    path = "Loaders/application/x-netcdf/Actions",
                    id
                    = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
                    position = 300),
            @ActionReference(
                    path = "Loaders/application/x-netcdf/Actions",
                    id
                    = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
                    position = 400,
                    separatorAfter = 500),
            @ActionReference(
                    path = "Loaders/application/x-netcdf/Actions",
                    id
                    = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
                    position = 600),
            @ActionReference(
                    path = "Loaders/application/x-netcdf/Actions",
                    id
                    = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
                    position = 700,
                    separatorAfter = 800),
            @ActionReference(
                    path = "Loaders/application/x-netcdf/Actions",
                    id
                    = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
                    position = 900,
                    separatorAfter = 1000),
            @ActionReference(
                    path = "Loaders/application/x-netcdf/Actions",
                    id
                    = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
                    position = 1100,
                    separatorAfter = 1200),
            @ActionReference(
                    path = "Loaders/application/x-netcdf/Actions",
                    id
                    = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
                    position = 1300),
            @ActionReference(
                    path = "Loaders/application/x-netcdf/Actions",
                    id
                    = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
                    position = 1400)
        })
public class NetCdfDataObject extends MultiDataObject {

    public static final String PROP_DATA_STATE = "PROP_DATA_STATE";
    /**
     * Derived classes can safely supply this lookup object to their Node class.
     */
    protected Lookup lookup;
    private DataImpl data;
    private InstanceContent content = new InstanceContent();
    private SaveSupport saveCookie = new SaveSupport();
    private transient final AtomicReference<DataState> state = new AtomicReference<>(DataState.NEW);
    private static final RequestProcessor THREAD_POOL = new RequestProcessor(NetCdfDataObject.class.getName(), 5);
    private static final Logger LOGGER = Logger.getLogger(NetCdfDataObject.class.getName());
    private static final long serialVersionUID = 20130513L;

    static {
        LOGGER.setLevel(Level.ALL);
    }

    /**
     * Constructor that proxies the cookieSet lookup with a dynamic lookup containing the FlatField.
     * The file contents are read in a worker thread which adds the FlatField object to the dynamic
     * lookup when it is finished.
     *
     * @param primaryFile representing FlatField
     * @param loader for FlatField
     * @throws DataObjectExistsException
     * @throws IOException
     */
    public NetCdfDataObject(FileObject primaryFile, MultiFileLoader loader) throws
            DataObjectExistsException, IOException {
        super(primaryFile, loader);

        // Utility method to register editor for this DataObject. The system will make sure that 
        // appropriate cookies (Openable, Editable, CloseCookie, EditorCookie, SaveAsCapable,
        // LineCookie are registered into getCookieSet().
        registerEditor("application/x-netcdf", false);

        // Create a lookup containing this DataObject and it's Actions (via getCookieSet) plus
        // our dynamic content that will contain the FlatField (this.content)
        this.lookup = new ProxyLookup(new AbstractLookup(this.content), getCookieSet().getLookup());
        this.content.add(this.state.get());

        // Read the data
        initialize();
    }

    /**
     * Reads the file contents in a worker thread.
     */
    private void initialize() {
        if (getDataState() != DataState.NEW) {
            throw new IllegalStateException("initialize() state must be NEW, not " + getDataState().toString());
        }

        if (this.getPrimaryFile().getSize() == 0) {
            // A new empty file - don't attempt to read it
            setDataState(DataState.INVALID);
        }
        else {
            setDataState(DataState.INITIALIZING);
            THREAD_POOL.execute(new Runnable() {
                @Override
                public void run() {
                    readFile();
                }
            });
        }
    }

    /**
     * Gets the data represented by the file.
     *
     * @return the current data representation; may be null if the state is not INITIALIZED.
     */
    public DataImpl getData() {
        synchronized (this) {
            return this.data;
        }
    }

    /**
     * Assigns new data to this DataObject. Sets the modified flag to true.
     *
     * @param data to be assigned to this DataObject
     */
    public void setData(DataImpl data) {
        if (data == null) {
            throw new IllegalArgumentException("setData() cannot accept null data argument.");
        }
        synchronized (this) {
            this.data = data;
        }
        setDataState(DataState.INITIALIZED);
        setModified(true);
    }

    /**
     * Adds/removes the Save cookie. Fires a PROP_MODIFIED property change event.
     *
     * @param modified the new state.
     */
    @Override
    public void setModified(boolean modified) {
        if (getDataState() == DataState.INITIALIZED) {
            if (modified) {
                addSaveCookie();
            }
            else {
                removeSaveCookie();
            }
            // Fires a property change event.
            super.setModified(modified);
        }
    }

    public DataState getDataState() {
        return this.state.get();
    }

    /**
     * Sets the data state and fires a PROP_DATA_STATE property change event.
     *
     * @param newState to be assigned.
     */
    protected DataState setDataState(DataState newState) {
        DataState oldState;
        synchronized (this) {
            oldState = this.state.getAndSet(newState);
            this.content.remove(oldState);
            this.content.add(newState);
        }
        if (oldState != newState) {
            firePropertyChange(PROP_DATA_STATE, oldState, newState);
        }
        return oldState;
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
     * Loads and initializes a FlatField from a NetCDF file; adds a FlatField to the lookup if
     * successful. Fires a PROP_DATA_STATE property change event.
     *
     */
    public void readFile() {
        try {
            if (getDataState() == DataState.INITIALIZED) {
                LOGGER.log(Level.WARNING, "readFile() state already INITIALIED for {0}", this.getPrimaryFile().getPath());
                return;
            }
            if (getDataState() != DataState.INITIALIZING) {
                throw new IllegalStateException("readFile() state must be INITIALIZING, not " + getDataState().toString());
            }

            // Read the NetCDF file data
            LOGGER.log(Level.FINE, "readFile() {0}", this.getPrimaryFile().getPath());
            long startTimeMs = System.currentTimeMillis();
            Plain plain = new Plain();
            DataImpl dataImpl = plain.open(this.getPrimaryFile().getPath());
            synchronized (this) {
                this.data = dataImpl;
                this.content.add(this.data);

                notifyAll();
            }
            // Fire the property change
            setDataState(DataState.INITIALIZED);
            LOGGER.log(Level.INFO, "readFile() {0} finished, took {1} secs.", new Object[]{
                getName(), TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs)
            });
        }
        catch (IOException | VisADException ex) {
            LOGGER.log(Level.SEVERE, "readFile() {0} failed; setting state to INVALID: {1}", new Object[]{
                this.getPrimaryFile().getPath(), ex.toString()
            });
            DataState oldState = setDataState(DataState.INVALID);
            firePropertyChange(PROP_DATA_STATE, oldState, DataState.INVALID);
        }
    }

    /**
     * Write the data to a NetCDF file.
     */
    public void writeFile() {
        LOGGER.log(Level.FINE, "writeFile() {0}", this.getPrimaryFile().getPath());
        DataState dataState = getDataState();
        if (dataState != DataState.INITIALIZED && dataState != DataState.INVALID) {
            throw new IllegalStateException("writeFile() state must be INITIALIZED or INVALID, not " + dataState.toString());
        }
        try {
            long startTimeMs = System.currentTimeMillis();
            synchronized (this) {
                // Write out the NetCDF data using a VisAD Plain Form

                Plain plain = new Plain();
                plain.save(this.getPrimaryFile().getPath(), this.data, true);    // true = overwrite
            }
            LOGGER.log(Level.INFO, "writeFile() {0} finished, took {1} secs.", new Object[]{
                getName(), TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs)
            });

            // Update status and fire property changes
            setDataState(DataState.INITIALIZED);
            setModified(false);
        }
        catch (VisADException | IOException ex) {
            LOGGER.log(Level.SEVERE, "writeFile() {0} failed: {1}", new Object[]{
                this.getPrimaryFile().getPath(), ex.toString()
            });
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Delegates to getCookieSet().getLookup() and makes sure FileObject, this and Node are in the
     * lookup. The Node is created lazily by calling getNodeDelegate().
     *
     * @return 1
     */
    @Override
    protected int associateLookup() {
        return 1;   // 1 = delegate to getCookieSet().getLookup()
    }

    /**
     * Gets a Lookup that includes the FlatField, DataObject and Node objects and the associated
     * capabilities.
     *
     * @return the associated Node's lookup.
     */
    @Override
    public Lookup getLookup() {
        // Per: http://wiki.netbeans.org/DevFaqNodesCustomLookup
        // You really must override createNodeDelegate() or otherwise (in your
        // DataNode subclass) pass your DataObject's Lookup to your DataNode's
        // constructor.
        // Otherwise its lookup will be getCookieSet().getLookup() and nothing
        // added to your InstanceContent will appear in the Lookup of your Node. 
        // So, if you use AbstractLookup in a DataObject, make sure its Node is
        // really using your DataObject's Lookup.
        if (isValid()) {
            return getNodeDelegate().getLookup();
        }
        else {
            // Don't allow access to the Node after its been "invalidated" by delete()
            LOGGER.log(Level.WARNING, "getLookup() called on an invalid DataObject: {0}", getName());
            return Lookups.fixed(this);
        }
    }

    /**
     * Creates a Node to represent this DataObject.
     *
     * @return a new NetCdfDataNode
     */
    @Override
    protected Node createNodeDelegate() {
        return new NetCdfDataNode(this, this.lookup);
    }

    /**
     * Called by base class delete()
     *
     * @throws IOException
     */
    @Override
    protected void handleDelete() throws IOException {
        DataState currState = getDataState();
        if (currState == DataState.INITIALIZED || currState == DataState.INVALID) {
            setDataState(DataState.DELETING);

            // just delete the file
            super.handleDelete();
        }
        else {
            throw new IllegalStateException("handleDelete() failed for [" + getName() + "] - invalid state: " + state.get());
        }
        setDataState(DataState.DELETED);
    }

    /**
     * Save capability class.
     *
     * @author Bruce Schubert
     */
    protected class SaveSupport implements SaveCookie {

        @Override
        public void save() throws IOException {
            writeFile();
        }
    }
}
