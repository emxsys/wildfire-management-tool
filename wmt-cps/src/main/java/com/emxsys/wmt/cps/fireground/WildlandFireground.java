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
package com.emxsys.wmt.cps.fireground;

import com.emxsys.wmt.cps.actions.SelectFuelModelProviderAction;
import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.capabilities.Disposable;
import com.emxsys.gis.api.viewer.Viewers;
import com.emxsys.visad.Times;
import com.emxsys.weather.api.WeatherTuple;
import com.emxsys.wildfire.api.FireEnvironment;
import com.emxsys.wildfire.api.Fireground;
import com.emxsys.wildfire.api.FuelCondition;
import com.emxsys.wildfire.api.FuelModelProvider;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.StdFuelMoistureScenario;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.Gridded1DDoubleSet;
import visad.Real;
import visad.VisADException;

/**
 * The WildlandFireground class represents the boundaries and terrain that comprise the area(s) of
 * interest.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class WildlandFireground implements Fireground, PropertyChangeListener, Disposable {

    public static final String SECTOR_ADDED_EVENT = "sector_added";
    public static final String SECTOR_REMOVED_EVENT = "sector_removed";
    public static final String AIR_TEMPERATURES_ADDED_EVENT = "air_temperatures_added";
    public static final String RELATIVE_HUMIDITIES_ADDED_EVENT = "relative_humidities_added";
    public static final String GENERAL_WINDS_ADDED_EVENT = "general_winds_added";
    public static final String FIRE_BEHAVIOR_ADDED_EVENT = "fire_behavior_added";
    /** FIRE_WEATHER_ADDED_EVENT property change accompanied by weather data in FieldImpl */
    public static final String FIRE_WEATHER_ADDED_EVENT = "fire_weather_added";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

// Fire environment inputs
    private final List<Box> sectors = new ArrayList<>();
    private FlatField firePredictions;
    private FlatField fireSignatures;
    private FlatField wxForecasts;
    private FlatField wxObservations;
    // General weather inputs
    private Gridded1DDoubleSet timeset;
    private FlatField temperatures;
    private FlatField humidities;
    private FlatField winds;
    //
    // Models derived from inputs
    private final Map<Box, SpatioTemporalDomain> domains = new HashMap<>();
    private final Map<Box, TerrainModel> terrainModels = new HashMap<>();
    private final Map<Box, WeatherModel> wxModels = new HashMap<>();
    private final Map<Box, FuelModelProvider> fuelModelProviders = new HashMap<>();
    private final Map<Box, FuelTypeModel> fuelTypeModels = new HashMap<>();
    private final Map<Box, FuelTemperatureModel> fuelTempModels = new HashMap<>();
    private final Map<Box, FuelMoistureModel> fuelMoistureModels = new HashMap<>();
    private final Map<Box, FireBehaviorModel> fireBehaviorModels = new HashMap<>();

    private boolean cancelAnalysis = false;
    private boolean analysisRunning = false;

    private static final Logger logger = Logger.getLogger(WildlandFireground.class.getName());

    /**
     *
     */
    public WildlandFireground() {

    }

    /**
     * Gets a collection of FireEnvironments for a set of dates (times).
     *
     * @param dates for which the fire environments are desired
     * @param position at which the fire environments are desired
     * @return sorted map of fire environments
     */
    @Override
    public TreeMap<Date, FireEnvironment> getFireEnvironment(List<Date> dates, Coord2D position) {
        TreeMap<Date, FireEnvironment> map = new TreeMap<>();
        dates.stream().forEach((date) -> {
            DateTime dateTime = Times.fromDate(date);
            map.put(date, getFireEnvironment(dateTime, position));
        });
        return map;
    }

    /**
     * Gets the FireEnvironment for the given date/time.
     *
     * @param dateTime at which the fire environment is desired
     * @param position at which the fire environment is desired
     * @return the fire environment at the given time and place; may return null.
     */
    @Override
    public FireEnvironment getFireEnvironment(DateTime dateTime, Coord2D position) {
        // Getting the inputs necessary to build a FireEnvironment instance
        FireBehaviorModel behave = null;
        FuelTypeModel fuel = null;
        FuelMoistureModel moistures = null;
        FuelTemperatureModel temps = null;
        WeatherModel weather = null;
        for (Box box : sectors) {
            if (box.contains(position)) {
                behave = this.fireBehaviorModels.get(box);
                fuel = this.fuelTypeModels.get(box);
                moistures = this.fuelMoistureModels.get(box);
                temps = this.fuelTempModels.get(box);
                weather = this.wxModels.get(box);
            }
        }
        if (behave == null || fuel == null || moistures == null || temps == null || weather == null) {
            return null;
        }

        // Populating the FireEnvironment 
        FireEnvironment fe = new FireEnvironment();
        try {
            fe.model = fuel.getFuelModel(position);
            fe.fireBehavior = behave.getMaxFireBehavior(dateTime, position);
            fe.fireBehaviorNoWnd = behave.getMinFireBehavior(dateTime, position);
            fe.condition = new FuelCondition();
            fe.condition.airTemp = weather.getWeather(dateTime).getAirTemperature();
            fe.condition.fuelTemp = (Real) temps.getFuelTemperature(dateTime, position).getComponent(0);
            fe.condition.fuelMoisture = moistures.getFuelMoisture(dateTime, position);
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
        return fe;
    }

    public void startAnalysis() {
        if (analysisRunning) {
            return;
        }
        Thread t = new AnalysisThread(this);
        t.start(); // start the task and progress visualization
    }

    private void analyze(ProgressHandle handle) {
        analysisRunning = true;
        String taskname = "Analyzing fireground";
        final int NUM_STEPS = 7;
        handle.start(NUM_STEPS);    // Start with 7 steps
        try {
            int step = 0;
            cancelAnalysis = false;
            while (!cancelAnalysis && step < NUM_STEPS) {
                switch (step) {
                    case 0:
                        handle.setDisplayName(taskname + " - Initializing spatial/temporal domains...");
                        initSpatioTemporalDomains();
                        break;
                    case 1:
                        handle.setDisplayName(taskname + " - Initializing terrain...");
                        initTerrain();
                        break;
                    case 2:
                        handle.setDisplayName(taskname + " - Initializing weather...");
                        initWeather();
                        break;
                    case 3:
                        handle.setDisplayName(taskname + " - Initializing fuel types...");
                        initFuelTypes();
                        break;
                    case 4:
                        handle.setDisplayName(taskname + " - Initializing fuel temperatures...");
                        initFuelTemps();
                        break;
                    case 5:
                        handle.setDisplayName(taskname + " - Initializing fuel moistures...");
                        initFuelMoistures();
                        break;
                    case 6:
                        handle.setDisplayName(taskname + " - Computing fire behaviors...");
                        initFireBehaviors();
                        break;
                    default:
                        throw new IllegalStateException("incorrect number of steps");
                }
                handle.progress(step++);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "analyze() failed: {0}", ex.toString());
            Exceptions.printStackTrace(ex);
        } finally {
            handle.finish();
        }
        analysisRunning = false;
    }

    public void resetModels() {
        this.domains.clear();
        this.terrainModels.clear();
        this.fuelTypeModels.clear();
        this.fuelMoistureModels.clear();
        this.fuelTempModels.clear();
        this.wxModels.clear();
        this.fireBehaviorModels.clear();
    }

    @Override
    public void dispose() {
        this.sectors.stream().forEach((box) -> {
            Viewers.removeFromViewers(box);
        });
        this.sectors.clear();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public Box findSectorContaining(Coord2D coord) {
        for (Box box : sectors) {
            if (box.contains(coord)) {
                return box;
            }
        }
        return null;
    }

    @Override
    public List<Box> getSectors() {
        return this.sectors;
    }

    /**
     * Adds a sector to the fireground.
     *
     * @param sector the sector to be added
     */
    @Override
    public void addSector(Box sector, FuelModelProvider fuelModels) {
        // TODO: Remove single sector restriction!
        // XXX Removing existing sectors--only one sector per fireground at this time
        Object[] oldSectors = this.sectors.toArray();
        if (!this.sectors.isEmpty()) {
            this.sectors.clear();
            this.fuelModelProviders.clear();
        }
        this.sectors.add(sector);

        if (fuelModels == null) {
            throw new IllegalArgumentException("add sector failed: FuelModelProvider is null.");
        }
        this.fuelModelProviders.put(sector, fuelModels);
        resetModels();
        initSpatioTemporalDomains();

        // XXX cleaning up--only one sector per fireground
        if (oldSectors.length > 0) {
            for (Object object : oldSectors) {
                Viewers.removeFromViewers((Box) object);
                pcs.firePropertyChange(SECTOR_REMOVED_EVENT, object, null);
            }
        }
        Viewers.addToViewers(sector);
        pcs.firePropertyChange(SECTOR_ADDED_EVENT, null, sector);

    }

    @Override
    public void removeSector(Box sector) {
        this.sectors.remove(sector);
        this.fuelModelProviders.remove(sector);
        resetModels();
        initSpatioTemporalDomains();
        Viewers.removeFromViewers(sector);
        pcs.firePropertyChange(SECTOR_REMOVED_EVENT, sector, null);
    }

    @Override
    public FuelModelProvider getFuelModelProvider(Box sector) {
        return this.fuelModelProviders.get(sector);
    }

    @Override
    public Collection<FlatField> getFuelModels() {
        // Return our FuelModel collection as a FlatField collection
        ArrayList<FlatField> list = new ArrayList<>();
        this.sectors.stream().forEach((box) -> {
            FuelTypeModel ftm = this.fuelTypeModels.get(box);
            if (ftm != null) {
                list.add(ftm.getFuelData());
            }
        });
        return list;
    }

    @Override
    public Collection<FieldImpl> getFuelMoistureDead1hr() {
        // Return our FuelMoisture collection as a FlatField collection
        ArrayList<FieldImpl> list = new ArrayList<>();
        this.sectors.stream().forEach((box) -> {
            FuelMoistureModel fmm = this.fuelMoistureModels.get(box);
            if (fmm != null) {
                list.add(fmm.getDead1HrFuelMoistureData());
            }
        });
        return list;
    }

    @Override
    public Collection<FieldImpl> getFuelMoistureDead10hr() {
        // Return our FuelMoisture collection as a FlatField collection
        ArrayList<FieldImpl> list = new ArrayList<>();
        this.sectors.stream().forEach((box) -> {
            FuelMoistureModel fmm = this.fuelMoistureModels.get(box);
            if (fmm != null) {
                list.add(fmm.getDead10HrFuelMoistureData());
            }
        });
        return list;
    }

    @Override
    public Collection<FieldImpl> getFuelMoistureDead100hr() {
        // Return our FuelMoisture collection as a FlatField collection
        ArrayList<FieldImpl> list = new ArrayList<>();
        this.sectors.stream().forEach((box) -> {
            FuelMoistureModel fmm = this.fuelMoistureModels.get(box);
            if (fmm != null) {
                list.add(fmm.getDead100HrFuelMoistureData());
            }
        });
        return list;
    }

    @Override
    public Collection<FieldImpl> getFuelMoistureLiveHerb() {
        // Return our FuelMoisture collection as a FlatField collection
        ArrayList<FieldImpl> list = new ArrayList<>();
        this.sectors.stream().forEach((box) -> {
            FuelMoistureModel fmm = this.fuelMoistureModels.get(box);
            if (fmm != null) {
                list.add(fmm.getLiveHerbFuelMoistureData());
            }
        });
        return list;
    }

    @Override
    public Collection<FieldImpl> getFuelMoistureLiveWoody() {
        // Return our FuelMoisture collection as a FlatField collection
        ArrayList<FieldImpl> list = new ArrayList<>();
        this.sectors.stream().forEach((box) -> {
            FuelMoistureModel fmm = this.fuelMoistureModels.get(box);
            if (fmm != null) {
                list.add(fmm.getLiveWoodyFuelMoistureData());
            }
        });
        return list;
    }

    /**
     *
     * {@inheritDoc }
     */
    @Override
    public Collection<FieldImpl> getFuelTemperature() {
        // Return our FuelModel collection as a FlatField collection
        ArrayList<FieldImpl> list = new ArrayList<>();
        this.sectors.stream().forEach((box) -> {
            FuelTemperatureModel fmm = this.fuelTempModels.get(box);
            if (fmm != null) {
                list.add(fmm.getFuelTemperatureData());
            }
        });
        return list;
    }

    @Override
    public FlatField getFirePredictions() {
        return this.firePredictions;
    }

    @Override
    public FlatField getFireSignatures() {
        return this.fireSignatures;
    }

    @Override
    public Collection<FlatField> getTerrain() {
        // Return our TerrainModel collection as a FlatField collection
        ArrayList<FlatField> list = new ArrayList<>();
        this.sectors.stream().forEach((box) -> {
            TerrainModel model = this.terrainModels.get(box);
            if (model != null) {
                list.add(this.terrainModels.get(box).getTerrainData());
            }
        });
        return list;
    }

    public WeatherTuple getFireWeather(DateTime dateTime) {
        WeatherModel model = this.wxModels.entrySet().iterator().next().getValue();
        if (model != null) {
            return model.getWeather(dateTime);
        }
        return new WeatherTuple();
    }

    /**
     * @return Math type: ( time -> ( SolarType.TIME, AIR_TEMP_C, REL_HUMIDITY, WIND_SPEED_SI,
     * WIND_DIR, CLOUD_COVER ) )
     */
    @Override
    public Collection<FieldImpl> getFireWeather() {
        // Return our WeatherModel collection as a FieldImpl collection
        ArrayList<FieldImpl> list = new ArrayList<>();
        this.sectors.stream().filter((box) -> (this.wxModels.containsKey(box))).forEach((box) -> {
            list.add(this.wxModels.get(box).getWeatherData());
        });
        return list;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initSpatioTemporalDomains() {
        if (this.sectors.isEmpty()) {
            logger.severe("Cannot initialize domain; sectors have not been defined.");
            return;
        }
        if (this.timeset == null || this.timeset.isMissing()) {
            logger.severe("Cannot initialize domain; timeset has not been defined.");
            return;
        }

        long startTimeMillis = System.currentTimeMillis();
        this.domains.clear();

        sectors.stream().map((sector) -> {
            SpatioTemporalDomain domain = new SpatioTemporalDomain(sector, this.timeset);
            this.domains.put(sector, domain);
            return domain;
        }).forEach((domain) -> {
            logger.log(Level.FINE, "initSpatioTemporalDomain created {0}", domain.toString());
        });
        logger.log(Level.INFO, "initSpatioTemporalDomain elapsed time: {0} secs", ((System.currentTimeMillis() - startTimeMillis) / 1000));

    }

    private void initTerrain() {
        if (this.domains.isEmpty()) {
            logger.severe("Cannot initialize terrain; the domain has not been defined.");
            return;
        }

        // Get a time stamp for logging purposes
        long startTimeMillis = System.currentTimeMillis();

        // Loop thru all the sector domains
        this.terrainModels.clear();
        Iterator<Entry<Box, SpatioTemporalDomain>> iterator = domains.entrySet().iterator();
        while (iterator.hasNext()) {
            SpatioTemporalDomain domain = iterator.next().getValue();
            TerrainModel terrain = new TerrainModel(domain, true); // true = immediate initialization
            // Add the terrain to the sector/terrain map.
            this.terrainModels.put(domain.getSector(), terrain);
        }

        logger.log(Level.INFO, "initTerrain elapsed time: {0} secs", ((System.currentTimeMillis() - startTimeMillis) / 1000));

    }

    private void initWeather() {
        if (this.domains.isEmpty()) {
            logger.warning("Cannot initialize weather; domains have not been defined.");
            return;
        }
        if (this.temperatures == null || this.humidities == null || this.winds == null) {
            logger.severe("Cannot initialize weather; general weather elements have been defined.");
            return;
        }

        // Time the duration of this task
        long startTimeMillis = System.currentTimeMillis();

        this.wxModels.clear();
        Iterator<Entry<Box, SpatioTemporalDomain>> iterator = domains.entrySet().iterator();
        while (iterator.hasNext()) {
            SpatioTemporalDomain domain = iterator.next().getValue();
            WeatherModel wxModel = new WeatherModel(domain, temperatures, humidities, winds);
            this.wxModels.put(domain.getSector(), wxModel);

            pcs.firePropertyChange(FIRE_WEATHER_ADDED_EVENT, null, wxModel.getWeatherData());
        }

        logger.log(Level.INFO, "initWeather elapsed time: {0} secs", ((System.currentTimeMillis() - startTimeMillis) / 1000));

    }

    private void initFuelTypes() {
        if (this.domains.isEmpty()) {
            logger.severe("Cannot initialize fuel model; the domain has not been defined.");
            return;
        }

        long startTimeMillis = System.currentTimeMillis();
        this.fuelTypeModels.clear();

        Iterator<Entry<Box, SpatioTemporalDomain>> iterator = this.domains.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Box, SpatioTemporalDomain> entry = iterator.next();
            Box sector = entry.getKey();
            SpatioTemporalDomain domain = entry.getValue();
            FuelModelProvider provider = this.fuelModelProviders.get(sector);
            if (provider == null) {
                provider = selectFuelModelProviderForSector(sector);
            }
            FuelTypeModel fuel = new FuelTypeModel(domain, provider, true);
            this.fuelTypeModels.put(sector, fuel);
        }
        logger.log(Level.INFO, "initFuelModels elapsed time: {0} secs", ((System.currentTimeMillis() - startTimeMillis) / 1000));

    }

    private void initFuelTemps() {
        // Compute the boundary (extents) for the fuels.
        if (this.sectors.isEmpty()) {
            logger.warning("Cannot initialize fuel temps; sectors have not been defined.");
            return;
        }

        long startTimeMillis = System.currentTimeMillis();
        this.fuelTempModels.clear();

        Iterator<Entry<Box, SpatioTemporalDomain>> iterator = domains.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Box, SpatioTemporalDomain> entry = iterator.next();
            Box sector = entry.getKey();
            SpatioTemporalDomain domain = entry.getValue();
            TerrainModel terrain = terrainModels.get(sector);
            FuelTypeModel fuel = fuelTypeModels.get(sector);
            WeatherModel wx = wxModels.get(sector);
            FuelTemperatureModel temps = new FuelTemperatureModel(domain, terrain, fuel, wx, true); // true = immediate initialization

            this.fuelTempModels.put(sector, temps);

        }
        logger.log(Level.INFO, "initFuelTemps elapsed time: {0} secs", ((System.currentTimeMillis() - startTimeMillis) / 1000));

    }

    private FuelModelProvider selectFuelModelProviderForSector(Box sector) {
        SelectFuelModelProviderAction selectAction = new SelectFuelModelProviderAction(sector);
        selectAction.actionPerformed(null);
        return selectAction.getFuelModelProvider();
    }

    /**
     * Displays the Fuel Temperatures Window.
     */
    private class FuelTempWindowLauncher implements Runnable {

        private final FuelTemperatureModel temps;

        FuelTempWindowLauncher(FuelTemperatureModel temps) {
            this.temps = temps;
        }

        @Override
        public void run() {
            // TODO: update FuelTemp UI
            // code to be invoked when system UI is ready
//            FuelTempTopComponent tc = new FuelTempTopComponent(temps);
//            tc.open();
//            tc.requestActive();
        }
    }

    private void initFuelMoistures() {
        // Compute the boundary (extents) for the fuels.
        if (this.sectors.isEmpty()) {
            logger.warning("Cannot initialize fuel moistures; sectors have not been defined.");
            return;
        }

        long startTimeMillis = System.currentTimeMillis();
        this.fuelMoistureModels.clear();

        Iterator<Entry<Box, SpatioTemporalDomain>> iterator = domains.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Box, SpatioTemporalDomain> entry = iterator.next();
            Box sector = entry.getKey();
            SpatioTemporalDomain domain = entry.getValue();
            TerrainModel terrain = terrainModels.get(sector);
            FuelTypeModel fuelTypes = fuelTypeModels.get(sector);
            WeatherModel wx = wxModels.get(sector);
            FuelTemperatureModel fuelTemps = fuelTempModels.get(sector);
            FuelMoisture scenario = StdFuelMoistureScenario.VeryLowDead_FullyCuredHerb.getFuelMoisture();
            FuelMoistureModel moistures = new FuelMoistureModel(domain, terrain, fuelTypes, fuelTemps, wx, scenario, true); // true = immediate initialization

            this.fuelMoistureModels.put(sector, moistures);
        }
        logger.log(Level.INFO, "initFuelMoistures elapsed time: {0} secs", ((System.currentTimeMillis() - startTimeMillis) / 1000));

    }

    private void initFireBehaviors() {
        try {

            // Compute the fire behavior the fuels.
            if (this.sectors.isEmpty()) {
                logger.warning("Cannot initialize fire behaviors; sectors have not been defined.");
                return;
            }

            long startTimeMillis = System.currentTimeMillis();
            this.fireBehaviorModels.clear();

            Iterator<Entry<Box, SpatioTemporalDomain>> iterator = domains.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Box, SpatioTemporalDomain> entry = iterator.next();
                Box sector = entry.getKey();
                SpatioTemporalDomain domain = entry.getValue();

                TerrainModel terrain = terrainModels.get(sector);
                WeatherModel wx = wxModels.get(sector);
                FuelTypeModel fuelTypes = fuelTypeModels.get(sector);
                FuelTemperatureModel fuelTemps = fuelTempModels.get(sector);
                FuelMoistureModel moistures = fuelMoistureModels.get(sector);

                FireBehaviorModel behaviors = new FireBehaviorModel(domain, terrain, fuelTypes, fuelTemps, moistures, wx, true);
                this.fireBehaviorModels.put(sector, behaviors);

                // Notify the fire behavior data object of the change
                pcs.firePropertyChange(FIRE_BEHAVIOR_ADDED_EVENT, null, behaviors);
            }
            logger.log(Level.INFO, "initFireBehaviors elapsed time: {0} secs", ((System.currentTimeMillis() - startTimeMillis) / 1000));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Displays the Fire Behavior Window
     */
    private class FireBehaviorWindowLauncher implements Runnable {

        private final FireBehaviorModel behaviors;

        FireBehaviorWindowLauncher(FireBehaviorModel behaviors) {
            this.behaviors = behaviors;
        }

        @Override
        public void run() {
            // code to be invoked when system UI is ready
            //FireBehaviorTopComponent tc = new FireBehaviorTopComponent(behaviors);
            //FireBehaviorTopComponent2 tc2 = new FireBehaviorTopComponent2(behaviors); // streamlines prototype version 2 
            //FireBehaviorTopComponent3 tc3 = new FireBehaviorTopComponent3(behaviors); // fli prototype version 3
            //tc.open();
            //tc2.open();
            //tc3.open();
            //tc.requestActive();
            //tc3.requestActive();
        }
    }

    public void addTimeset(Gridded1DDoubleSet timeset) {
        this.timeset = timeset;
    }

    public void addWeather(FlatField temperatures, FlatField humidities, FlatField winds) {
        addTemperatures(temperatures);
        addHumidities(humidities);
        addWinds(winds);
        // Initialize the weather models
        initWeather();
    }

    public void addTemperatures(FlatField temperatures) {
        this.temperatures = temperatures;
        pcs.firePropertyChange(AIR_TEMPERATURES_ADDED_EVENT, null, this.temperatures);
    }

    public void addHumidities(FlatField humidities) {
        this.humidities = humidities;
        pcs.firePropertyChange(RELATIVE_HUMIDITIES_ADDED_EVENT, null, this.humidities);
    }

    public void addWinds(FlatField winds) {
        this.winds = winds;
        pcs.firePropertyChange(GENERAL_WINDS_ADDED_EVENT, null, this.winds);
    }

    public void addTerrain(Box sector, FlatField terrain) {
        TerrainModel model = new TerrainModel(terrain);
        this.terrainModels.put(sector, model);
        //pcs.firePropertyChange(, null, null);
    }

    public void addFuelTypes(Box sector, FlatField fuelTypes) {
        FuelTypeModel model = new FuelTypeModel(fuelTypes);
        this.fuelTypeModels.put(sector, model);
        //pcs.firePropertyChange(, null, null);
    }

    public void addFuelTemperatures(Box sector, FieldImpl fuelTemps) {
        FuelTemperatureModel model = new FuelTemperatureModel(fuelTemps);
        this.fuelTempModels.put(sector, model);
        //pcs.firePropertyChange(, null, null);
    }

    public void addFuelMoistures(Box sector, FieldImpl dead1hr, FieldImpl dead10hr,
                                 FieldImpl dead100hr, FieldImpl liveHerb, FieldImpl liveWoody) {
        FuelMoistureModel model = new FuelMoistureModel(
                dead1hr, dead10hr, dead100hr, liveHerb, liveWoody);
        this.fuelMoistureModels.put(sector, model);
        //pcs.firePropertyChange(, null, null);
    }

    public void addFireBehavior(Box sector, FieldImpl maxBehaviors, FieldImpl minBehaviors) {
        FireBehaviorModel model = new FireBehaviorModel(maxBehaviors, minBehaviors);
        this.fireBehaviorModels.put(sector, model);
        pcs.firePropertyChange(FIRE_BEHAVIOR_ADDED_EVENT, null, this.fireBehaviorModels);
    }

    @Override
    public Gridded1DDoubleSet getTimeSet() {
        return this.timeset;
    }

    @Override
    public FlatField getAirTemperature() {
        return this.temperatures;
    }

    @Override
    public FlatField getRelativeHumidity() {
        return this.humidities;
    }

    @Override
    public FlatField getGeneralWinds() {
        return this.winds;
    }

    @Override
    public FlatField getCloudCover() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<FieldImpl> getFireBehaviorMax() {
        List<FieldImpl> list = new ArrayList<>();
        Collection<FireBehaviorModel> values = this.fireBehaviorModels.values();
        values.stream().forEach((fireBehaviorModel) -> {
            list.add(fireBehaviorModel.getMaxFireBehavorData());
        });
        return list;
    }

    @Override
    public Collection<FieldImpl> getFireBehaviorMin() {
        List<FieldImpl> list = new ArrayList<>();
        Collection<FireBehaviorModel> values = this.fireBehaviorModels.values();
        values.stream().forEach((fireBehaviorModel) -> {
            list.add(fireBehaviorModel.getMinFireBehavorData());
        });
        return list;
    }

    private class AnalysisThread extends Thread implements Cancellable {

        private final WildlandFireground fireground;

        AnalysisThread(WildlandFireground fireground) {
            super("Fireground Analysis Thread");
            this.fireground = fireground;
        }

        @Override
        public void run() {
            // Create a 'cancellable' progress bar
            ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Analyzing fireground", this);

            cancelAnalysis = false; // flag used to exit analyze method
            fireground.analyze(progressHandle);

            progressHandle.finish();
        }

        @Override
        public boolean cancel() {
            if (isAlive()) {
                // Cancel thread
                cancelAnalysis = true;
            }
            // Return true on success; false if unable.
            return true;
        }
    }

    public FlatField getWxForecasts() {
        return wxForecasts;
    }

    public FlatField getWxObservations() {
        return wxObservations;
    }
}
