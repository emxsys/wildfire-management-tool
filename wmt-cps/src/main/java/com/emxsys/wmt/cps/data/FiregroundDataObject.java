/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com> 
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
package com.emxsys.wmt.cps.data;

import com.emxsys.wmt.cps.fireground.WildlandFireground;
import com.emxsys.gis.api.Box;
import com.emxsys.visad.filetype.NetCdfDataObject;
import com.emxsys.wildfire.api.FuelModelProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.XMLUtil;
import org.xml.sax.SAXException;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.Gridded1DDoubleSet;
import visad.Linear1DSet;
import visad.VisADException;

/**
 * This class represents the contents of an XML Fireground file when the data loader finds one in
 * our project folder.
 *
 * @see org.openide.loaders.XMLDataObject
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@NbBundle.Messages({"LBL_Fireground_LOADER=Files of Fireground"})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_Fireground_LOADER",
        mimeType = "text/fireground+xml",
        position = 1000,
        elementNS = {"http://emxsys.com/fireground"})
@DataObject.Registration(
        mimeType = "text/fireground+xml",
        iconBase = "com/emxsys/wmt/cps/images/region.png",
        displayName = "#LBL_Fireground_LOADER",
        position = 1000)
@ActionReferences({
    @ActionReference(path = "Loaders/text/fireground+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),
//    @ActionReference(path = "Loaders/text/fireground+xml/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
//            position = 300),
//    @ActionReference(path = "Loaders/text/fireground+xml/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
//            position = 400,
//            separatorAfter = 500),
//    @ActionReference(path = "Loaders/text/fireground+xml/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
//            position = 600),
//    @ActionReference(path = "Loaders/text/fireground+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
//            position = 700,
//            separatorAfter = 800),
//    @ActionReference(path = "Loaders/text/fireground+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
//            position = 900,
//            separatorAfter = 1000),
//    @ActionReference(path = "Loaders/text/fireground+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
//            position = 1100,
//            separatorAfter = 1200),
//    @ActionReference(path = "Loaders/text/fireground+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
//            position = 1300),
    @ActionReference(path = "Loaders/text/fireground+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400)
})
public class FiregroundDataObject extends XMLDataObject {

    public static final String GENERAL_AIR_TEMPS_FILE = "general_air_temps.nc";
    public static final String GENERAL_HUMIDITIES_FILE = "general_humidities.nc";
    public static final String GENERAL_WINDS_FILE = "general_winds.nc";
    public static final String TERRAIN_FILE = "terrain.nc";
    public static final String FUEL_MODEL_FILE = "fuel_model.nc";
    public static final String FUEL_TEMPERATURE_FILE = "fuel_temperature.nc";
    public static final String FUEL_DEAD_1HR_FILE = "fuel_dead_1hr.nc";
    public static final String FUEL_DEAD_10HR_FILE = "fuel_dead_10hr.nc";
    public static final String FUEL_DEAD_100HR_FILE = "fuel_dead_100hr.nc";
    public static final String FUEL_LIVE_HERB_FILE = "fuel_live_herb.nc";
    public static final String FUEL_LIVE_WOODY_FILE = "fuel_live_woody.nc";
    public static final String FIRE_BEHAVIOR_MAX_FILE = "fire_behavior_max.nc";
    public static final String FIRE_BEHAVIOR_MIN_FILE = "fire_behavior_min.nc";
    public static final String WEATHER_FOLDER = "weather";
    public static final String BEHAVE_FOLDER = "behave";
    public static final String PREF_WAIT_TIME_MS = "file_load_wait_time_ms";
    public static final int DEFAULT_WAIT_TIME_MS = 5000;
    private static int waitTimeMs = -1;
    private Lookup lookup;
    private final InstanceContent content = new InstanceContent();

    /** The primary object that this DataObject represents. */
    private WildlandFireground fireground;

    /** Capability added to the lookup when modified. */
    private final SaveCapability saveCookie = new SaveCapability();

    /** Listener for changes in the Fireground object. */
    private final PropertyChangeListener firegroundListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getSource() instanceof WildlandFireground) {
                setModified(true);
            }
        }
    };

    /** Listener for changes in the fireground folder */
    private final FileChangeListener folderListener = new SimpleFileChangeListener();

    private static final Logger logger = Logger.getLogger(FiregroundDataObject.class.getName());

    /**
     * Constructor.
     *
     * @param primaryFile to load
     * @param loader to use
     */
    public FiregroundDataObject(FileObject primaryFile, MultiFileLoader loader) throws
            DataObjectExistsException, IOException {
        super(primaryFile, loader);
        if (waitTimeMs == -1) {
            Preferences pref = NbPreferences.forModule(this.getClass());
            waitTimeMs = pref.getInt(PREF_WAIT_TIME_MS, DEFAULT_WAIT_TIME_MS);
        }
        createFireground();
    }

    /**
     * Creates the Fireground object backed by this DataObject.
     */
    private void createFireground() {
        this.fireground = new WildlandFireground();
        this.content.add(this.fireground);

        // Update the fireground from the disk files
        readFireground();

        // Listen to changes which drive the setModified flag
        this.fireground.addPropertyChangeListener(this.firegroundListener);
    }

    /**
     *
     * @return
     */
    @Override
    public Lookup getLookup() {
        // Per: http://wiki.netbeans.org/DevFaqNodesCustomLookup
        // If you are really sure that nothing is going to use your DataObject's 
        // CookieSet at all, you can omit merging getCookieSet().getLookup() into 
        // the ProxyLookup in the constructor. However, many things will not work 
        // correctly if the DataObject itself cannot be found in its own Lookup. 
        // If you are going to do that, replace getCookieSet().getLookup() with 
        // Lookups.singleton(this) to ensure it is present and cannot be removed
        // or replaced.
        if (this.lookup == null) {
            // If you want to use the CookieSet, you'll need to remove the EditorCookie first, 
            // otherwise you might get this error:
            //      IllegalStateException: may not call Project.getLookup().lookup(...) 
            //      inside loadFireground registered under @ProjectServiceProvider
            // That's because the EditorCookie queries the owner project's lookup for a charset.
//            CookieSet cookies = getCookieSet();
//            EditorCookie cookie = cookies.getCookie(EditorCookie.class);
//            if (cookie != null)
//                cookies.remove(cookie);
//            this.lookup = new ProxyLookup(cookies.getLookup(), new AbstractLookup(this.content));
            this.lookup = new ProxyLookup(Lookups.singleton(this), new AbstractLookup(this.content));
        }
        return lookup;
    }

    /**
     * Creates a Node to represent this DataObject.
     *
     * @return a new FiregroundDataNode
     */
    @Override
    protected Node createNodeDelegate() {
        // Per: http://wiki.netbeans.org/DevFaqNodesCustomLookup
        // You really must override createNodeDelegate() or otherwise (in your 
        // DataNode subclass) pass your DataObject's Lookup to your DataNode's 
        // constructor. 
        // Otherwise its lookup will be getCookieSet().getLookup() and nothing 
        // added to your InstanceContent will appear in the Lookup of your Node. 
        // So, if you use AbstractLookup in a DataObject, make sure its Node is
        // really using your DataObject's Lookup.
        FiregroundDataNode node = new FiregroundDataNode(this, getLookup());
        node.setIconBaseWithExtension("com/emxsys/wmt/cps/images/polygon.png");
        return node;

    }

    /**
     * Adds or removes a SaveCookie to/from the lookup.
     *
     * @param modified flag adds SaveCookie if true
     */
    @Override
    public void setModified(boolean modified) {
        super.setModified(modified);

        if (modified) {
            addSaveCookie();
        } else {
            removeSaveCookie();
        }
    }

    private void addSaveCookie() {
        if (getLookup().lookup(SaveCookie.class) == null) {
            getCookieSet().add(saveCookie);
        }
    }

    /**
     * This method retrieves the Fireground object associated with the XmlDataObject.
     *
     * @return The associated fireground.
     */
    private void readFireground() {
        final ProgressHandle handle = ProgressHandleFactory.createHandle("Loading fireground");
        handle.start(); // start in indeterminate mode

        // Step 1: Initialize the spatial domain from the sector(s).
        // Step 2: Initialize the temporal domain - we derive this from the weather data's domain.
        // Step 3: Intitialize the data models
        try {
            FileObject parentFolder = this.getPrimaryFile().getParent();

            // Read the fireground xml data
            // Initialize the spatial domain
            Map<Box,FuelModelProvider> sectorFuels = FiregroundXmlEncoder.parseSectors(this.getDocument());
            sectorFuels.keySet().stream().forEach((box) -> {
                FuelModelProvider provider = sectorFuels.get(box);
                this.fireground.addSector(box, provider);
            });                        
 
            /*
            // Load the weather data from the weather folder
            FileObject weatherFolder = parentFolder.getFileObject(WEATHER_FOLDER);
            if (weatherFolder == null) {
                throw new IllegalStateException("Cannot open " + WEATHER_FOLDER);
            }
            DataImpl airtemps = loadWeatherData(weatherFolder, GENERAL_AIR_TEMPS_FILE);
            DataImpl rh = loadWeatherData(weatherFolder, GENERAL_HUMIDITIES_FILE);
            DataImpl winds = loadWeatherData(weatherFolder, GENERAL_WINDS_FILE);

            // Initailize the temporal domain: create the timeset based on any one of the weather 
            // fields they should all have a coincident time domain.
            if (airtemps != null) {
                Linear1DSet domainSet = (Linear1DSet) ((FlatField) airtemps).getDomainSet();
                Gridded1DDoubleSet timeset = new Gridded1DDoubleSet(domainSet.getType(), domainSet.getDoubles(true), domainSet.getLength());
                this.fireground.addTimeset(timeset);
                this.fireground.addWeather((FlatField) airtemps, (FlatField) rh, (FlatField) winds);
            } else {
                logger.warning("Unable to set the temporal domain from air temps.");
            }

            // Load the fire behavior data models from the behave folder 
            FileObject behaveFolder = parentFolder.getFileObject(BEHAVE_FOLDER);
            if (behaveFolder != null) {
                int suffix = 1;
                for (Box box : sectors) {
                    DataImpl terrain = loadFireBehaviorData(behaveFolder, getSuffixedFilename(TERRAIN_FILE, suffix));
                    DataImpl fuelTypes = loadFireBehaviorData(behaveFolder, getSuffixedFilename(FUEL_MODEL_FILE, suffix));
                    DataImpl fuelTemps = loadFireBehaviorData(behaveFolder, getSuffixedFilename(FUEL_TEMPERATURE_FILE, suffix));
                    DataImpl fuelDead1hr = loadFireBehaviorData(behaveFolder, getSuffixedFilename(FUEL_DEAD_1HR_FILE, suffix));
                    DataImpl fuelDead10hr = loadFireBehaviorData(behaveFolder, getSuffixedFilename(FUEL_DEAD_10HR_FILE, suffix));
                    DataImpl fuelDead100hr = loadFireBehaviorData(behaveFolder, getSuffixedFilename(FUEL_DEAD_100HR_FILE, suffix));
                    DataImpl fuelLiveHerb = loadFireBehaviorData(behaveFolder, getSuffixedFilename(FUEL_LIVE_HERB_FILE, suffix));
                    DataImpl fuelLiveWoody = loadFireBehaviorData(behaveFolder, getSuffixedFilename(FUEL_LIVE_WOODY_FILE, suffix));
                    DataImpl maxBehaviors = loadFireBehaviorData(behaveFolder, getSuffixedFilename(FIRE_BEHAVIOR_MAX_FILE, suffix));
                    DataImpl minBehaviors = loadFireBehaviorData(behaveFolder, getSuffixedFilename(FIRE_BEHAVIOR_MIN_FILE, suffix));

                    this.fireground.addTerrain(box, (FlatField) terrain);
                    this.fireground.addFuelTypes(box, (FlatField) fuelTypes);
                    this.fireground.addFuelTemperatures(box, (FieldImpl) fuelTemps);
                    this.fireground.addFuelMoistures(box,
                            (FieldImpl) fuelDead1hr, (FieldImpl) fuelDead10hr, (FieldImpl) fuelDead100hr,
                            (FieldImpl) fuelLiveHerb, (FieldImpl) fuelLiveWoody);
                    this.fireground.addFireBehavior(box, (FieldImpl) maxBehaviors, (FieldImpl) minBehaviors);

                    ++suffix;
                }
            }
*/            
        } // Fail silently
        catch (Exception ex) {
            logger.log(Level.SEVERE, "read fireground failed!", ex);
        } finally {
            handle.finish();
        }
    }

    private DataImpl loadWeatherData(FileObject weatherFolder, String filename) throws IOException {
        try {
            FileObject weatherFile = weatherFolder.getFileObject(filename);
            if (weatherFile == null) {
                throw new IllegalArgumentException(filename);
            }
            DataObject dataObj = DataObject.find(weatherFile);
            return getDataImpl(dataObj, waitTimeMs);

        } catch (IllegalArgumentException | DataObjectNotFoundException | InterruptedException e) {
            logger.log(Level.SEVERE, "loadWeatherData() failed for {0} in {1} : {2}",
                    new Object[]{filename, weatherFolder.getName(), e.toString()});
        }
        return null;
    }

    
    private DataImpl loadFireBehaviorData(FileObject behaveFolder, String filename) throws IOException {
        try {
            FileObject behaveFile = behaveFolder.getFileObject(filename);
            if (behaveFile == null) {
                throw new IllegalArgumentException(filename);
            }
            DataObject dataObj = DataObject.find(behaveFile);
            return getDataImpl(dataObj, waitTimeMs);

        } catch (IllegalArgumentException | DataObjectNotFoundException | InterruptedException e) {
            logger.log(Level.SEVERE, "loadFireBehaviorData() failed for {0} in {1} : {2}",
                    new Object[]{filename, behaveFolder.getName(), e.toString()});
        }
        return null;
    }

    /**
     * This method writes the Fireground object to XML.
     *
     */
    private void writeFireground() {
        final ProgressHandle handle = ProgressHandleFactory.createHandle("Saving fireground");
        handle.start(); // start in indeterminate mode

        try {
            // TODO: Need to write out the fuel model provider
            // Write the fireground xml file
            FiregroundXmlEncoder.encodeSectors(this.getDocument(), this.fireground);
            try (OutputStream output = getPrimaryFile().getOutputStream(FileLock.NONE)) {
                XMLUtil.write(this.getDocument(), output, "UTF-8");
                output.flush();
            }

            // Write out the weather netCDF files
            FileObject parentFolder = this.getPrimaryFile().getParent();
            FileObject weatherFolder = FileUtil.createFolder(parentFolder, WEATHER_FOLDER);
            if (weatherFolder != null) {
                saveWeatherData(this.fireground.getAirTemperature(), weatherFolder, GENERAL_AIR_TEMPS_FILE);
                saveWeatherData(this.fireground.getRelativeHumidity(), weatherFolder, GENERAL_HUMIDITIES_FILE);
                saveWeatherData(this.fireground.getGeneralWinds(), weatherFolder, GENERAL_WINDS_FILE);
            }

            // Write out fire behavior netCDF files
            FileObject behaveFolder = FileUtil.createFolder(parentFolder, BEHAVE_FOLDER);
            if (behaveFolder != null) {
                saveBehaveData(this.fireground.getTerrain(), behaveFolder, TERRAIN_FILE);
                saveBehaveData(this.fireground.getFuelModels(), behaveFolder, FUEL_MODEL_FILE);
                saveBehaveData(this.fireground.getFuelTemperature(), behaveFolder, FUEL_TEMPERATURE_FILE);
                saveBehaveData(this.fireground.getFuelMoistureDead1hr(), behaveFolder, FUEL_DEAD_1HR_FILE);
                saveBehaveData(this.fireground.getFuelMoistureDead10hr(), behaveFolder, FUEL_DEAD_10HR_FILE);
                saveBehaveData(this.fireground.getFuelMoistureDead100hr(), behaveFolder, FUEL_DEAD_100HR_FILE);
                saveBehaveData(this.fireground.getFuelMoistureLiveHerb(), behaveFolder, FUEL_LIVE_HERB_FILE);
                saveBehaveData(this.fireground.getFuelMoistureLiveWoody(), behaveFolder, FUEL_LIVE_WOODY_FILE);
                saveBehaveData(this.fireground.getFireBehaviorMax(), behaveFolder, FIRE_BEHAVIOR_MAX_FILE);
                saveBehaveData(this.fireground.getFireBehaviorMin(), behaveFolder, FIRE_BEHAVIOR_MIN_FILE);
            }

            setModified(false);
        } catch (IOException | SAXException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            handle.finish();
        }
    }

    /**
     * Save weather data to disk files.
     *
     * @param data
     * @param weatherFolder
     * @param filename
     */
    private void saveWeatherData(DataImpl data, FileObject weatherFolder, String filename) {
        if (data == null) {
            return;
        }
        try {
            // Look for an existing file
            FileObject fo = weatherFolder.getFileObject(filename);
            if (fo == null) {
                // Create the file
                fo = weatherFolder.createData(filename);
            }
            // Find the registered data object for this file type.
            DataObject dataObj = DataObject.find(fo);
            if (dataObj instanceof WeatherDataObject) {
                WeatherDataObject wxDataObj = (WeatherDataObject) dataObj;
                wxDataObj.setData(data);
                wxDataObj.writeFile();
            } else {
                throw new RuntimeException("Incompatible DataObject: " + dataObj.getClass().getName());
            }
        } catch (IOException | RuntimeException ex) {
            logger.log(Level.SEVERE, "saveWeatherData() failed: {0}", ex.toString());
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Save fire behavior data to disk files.
     *
     * @param fields
     * @param behaveFolder
     * @param baseFilename
     */
    private void saveBehaveData(Collection<? extends FieldImpl> fields,
                                FileObject behaveFolder,
                                String baseFilename) {
        // Save each field with a numerical suffix appended to the filename
        int suffix = 1;
        for (FieldImpl data : fields) {
            if (data == null) {
                return;
            }
            String filename = getSuffixedFilename(baseFilename, suffix++);
            try {
                // Look for an existing file
                FileObject fo = behaveFolder.getFileObject(filename);
                if (fo == null) {
                    // Create the file
                    fo = behaveFolder.createData(filename);
                }
                // Find the registered data object for this file type.
                //  See BehaveResolver.xml for registrations.
                DataObject dataObj = DataObject.find(fo);
                if (dataObj instanceof NetCdfDataObject) {
                    NetCdfDataObject ncDataObj = (NetCdfDataObject) dataObj;
                    ncDataObj.setData(data);
                    ncDataObj.writeFile();
                } else {
                    throw new RuntimeException("Incompatible DataObject: " + dataObj.getClass().getName());
                }
            } catch (IOException | RuntimeException ex) {
                logger.log(Level.SEVERE, "saveBehaveData() failed: {0}", ex.toString());
                throw new IllegalStateException(ex);
            }
        }
    }

    @Override
    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        logger.log(Level.FINE, "handleCreateFromTemplate({0}, {1}) called.", new Object[]{df.getName(), name});
        DataObject dob = super.handleCreateFromTemplate(df, name);
        FileUtil.createFolder(df.getPrimaryFile(), WEATHER_FOLDER);
        FileUtil.createFolder(df.getPrimaryFile(), BEHAVE_FOLDER);
        return dob;
    }

    private void removeSaveCookie() {
        SaveCookie save = getLookup().lookup(SaveCookie.class);

        if (save
                != null) {
            getCookieSet().remove(save);
        }
    }

    private String getSuffixedFilename(String baseFilename, int suffix) {
        int dot = baseFilename.lastIndexOf(".");
        String name = baseFilename.substring(0, dot) + "-" + suffix;
        String ext = baseFilename.substring(dot);
        String filename = name + ext;
        return filename;

    }

    /**
     * Save capability class
     */
    private class SaveCapability implements SaveCookie {

        @Override
        public void save() {
            try {
                writeFireground();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "save() failed.", e);
            }
        }
    }

    class SimpleFileChangeListener extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (fe.getFile().getMIMEType().equals("text/emxsys-fireground-sector+xml")) {
                try {
                    DataObject dob = DataObject.find(fe.getFile());
                    Box extents = dob.getLookup().lookup(Box.class);
                    fireground.addSector(extents, null);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    /**
     * Gets the DataImpl from the DataObject's lookup with a wait on the loading thread.
     *
     * @param dob suppling the DataImpl
     * @param msWaitTime time to wait for loading to complete
     * @return the DataImpl stored in the lookup; may be null.
     * @throws InterruptedException
     */
    static DataImpl getDataImpl(DataObject dob, long msWaitTime) throws InterruptedException {
        DataImpl data = dob.getLookup().lookup(DataImpl.class);
        synchronized (dob) {
            if (data == null) {
                logger.log(Level.FINE, "getDataImpl() waiting {1} ms for {0}", new Object[]{
                    dob.getName(), msWaitTime
                });

                dob.wait(msWaitTime);
                data = dob.getLookup().lookup(DataImpl.class);
            }
        }
        return data;
    }
}
