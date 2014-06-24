/*
 * Copyright (c) 2014, Bruce Schubert
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
 *     - Neither the name of bruce,  nor the names of its 
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
package com.emxsys.wmt.cps;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.ShadedTerrainProvider;
import com.emxsys.gis.api.Terrain;
import com.emxsys.gis.api.TerrainTuple;
import com.emxsys.gis.api.event.ReticuleCoordinateEvent;
import com.emxsys.gis.api.event.ReticuleCoordinateListener;
import com.emxsys.gis.api.event.ReticuleCoordinateProvider;
import com.emxsys.gis.spi.DefaultShadedTerrainProvider;
import com.emxsys.solar.api.SolarModel;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.solar.api.SunlightProvider;
import com.emxsys.solar.api.SunlightTuple;
import com.emxsys.solar.spi.DefaultSunlightProvider;
import com.emxsys.time.api.TimeEvent;
import com.emxsys.time.api.TimeListener;
import com.emxsys.time.api.TimeProvider;
import com.emxsys.time.spi.DefaultTimeProvider;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.SpatioTemporalDomain;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.weather.api.DiurnalWeatherModel;
import com.emxsys.weather.api.Weather;
import com.emxsys.weather.api.WeatherModel;
import com.emxsys.weather.api.WeatherProvider;
import com.emxsys.wildfire.api.FireBehaviorProvider;
import com.emxsys.wildfire.api.Fuel;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelModelProvider;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import com.emxsys.wildfire.api.FuelProvider;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wildfire.behavior.FireReaction;
import com.emxsys.wildfire.behavior.SurfaceFireModel;
import com.emxsys.wildfire.spi.DefaultFireBehaviorProvider;
import com.emxsys.wildfire.spi.DefaultFuelProvider;
import com.emxsys.wmt.cps.layers.FireShape;
import com.emxsys.wmt.cps.options.CpsOptions;
import com.emxsys.wmt.cps.ui.ForcesTopComponent;
import com.emxsys.wmt.cps.ui.FuelTopComponent;
import com.emxsys.wmt.cps.ui.WeatherTopComponent;
import com.emxsys.wmt.globe.Globe;
import java.awt.EventQueue;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;
import visad.Real;
import visad.VisADException;

/**
 * The CPS Controller class monitors time and location events, computes values and dispatches
 * messages to the various UI components.
 *
 * @author Bruce Schubert
 */
public class Controller {

    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    // Top components that interact with this Controllelr
    private static ForcesTopComponent forcesWindow;
    private static FuelTopComponent fuelWindow;
    private static WeatherTopComponent weatherWindow;
    // Data providers - Event generators
    private final ShadedTerrainProvider earth;
    private final SunlightProvider sun;
    private final TimeProvider earthClock;
    private ReticuleCoordinateProvider reticule;
    private WeatherProvider weather;
    private FuelModelProvider fuels;
    // Fire Behavior Calculator
    private final FuelProvider fuelProvider = DefaultFuelProvider.getInstance();
    private final FireBehaviorProvider fireProvider = DefaultFireBehaviorProvider.getInstance();
    private final SurfaceFireModel fireModel = new SurfaceFireModel();
    private FireShape fireShape;    // Deferred initialization
    
    // Current data values
    private final AtomicReference<ZonedDateTime> timeRef = new AtomicReference<>(ZonedDateTime.now(ZoneId.of("UTC")));
    private final AtomicReference<Coord3D> coordRef = new AtomicReference<>(GeoCoord3D.INVALID_COORD);
    private final AtomicReference<Terrain> terrainRef = new AtomicReference<>(TerrainTuple.INVALID_TERRAIN);
    private final AtomicReference<FuelModel> fuelModelRef = new AtomicReference<>(StdFuelModel.INVALID);
    private final AtomicReference<FuelMoisture> fuelMoistureRef = new AtomicReference<>(FuelMoistureTuple.INVALID);
    private final AtomicReference<Sunlight> sunlightRef = new AtomicReference<>(SunlightTuple.INVALID_TUPLE);
    private final AtomicReference<TemporalDomain> temporalRef = new AtomicReference<>(new TemporalDomain());

    // Event handlers
    private final CooridinateUpdater coordinateUpdater;
    private final TimeUpdater timeUpdater;
    private final LookupListener reticuleLookupListener;
    private Lookup.Result<ReticuleCoordinateProvider> reticuleResult;

    private static final Preferences prefs = NbPreferences.forModule(CpsOptions.class);
    private final PreferenceChangeListener prefsChangeListener;
    private boolean terrainShadingEnabled;

    /**
     * Gets the Controller singleton instance.
     *
     * @return The singleton.
     */
    public static Controller getInstance() {
        return ControllerHolder.INSTANCE;
    }

    /**
     * Sets the FuelMoisure used by the Controller to determine the Fire Behavior at the current
     * coordinate. Set by the FuelTopComponent.
     *
     * @param fuelMoisture The new FuelMoisture.
     */
    public void setFuelMoisture(FuelMoisture fuelMoisture) {
        if (fuelMoisture == null) {
            throw new IllegalArgumentException("FuelMoisture is null");
        }
        logger.log(Level.CONFIG, "FuelMoisture set to: {0}", fuelMoisture.toString());
        this.fuelMoistureRef.set(fuelMoisture);
    }

    /**
     * Sets the FuelModelProvider used by the Controller to determine the FuelModel at the current
     * coordinate. Called by the FuelTopComponent.
     *
     * @param fuels The new FuelModelProvider.
     */
    public void setFuelModelProvider(FuelModelProvider fuels) {
        if (fuels == null) {
            throw new IllegalArgumentException("FuelModelProvider is null.");
        }
        logger.log(Level.CONFIG, "FuelModelProvider set to: {0}", fuels.toString());
        this.fuels = fuels;
        EventQueue.invokeLater(() -> {
            // Fire a coordinate-based update to change the current FuelModel
            coordinateUpdater.update();
        });
    }

    /**
     * Sets the WeatherProvider used by the Controller to determine the weather at the current
     * coordinate and time. Invoked by the WeatherTopComponent.
     *
     * @param weather The new WeatherProvider.
     */
    public void setWeatherProvider(WeatherProvider weather) {
        if (weather == null) {
            throw new IllegalArgumentException("WeatherProvider is null.");
        }
        logger.log(Level.CONFIG, "WeatherProvider set to: {0}", weather.toString());
        this.weather = weather;

        EventQueue.invokeLater(() -> {
            // Fire a coordinate based update to update the FuelModel and charts
            //terrainUpdater.update();
        });
    }

    /**
     * Hidden constructor creates the Controller singleton; attaches listeners.
     */
    private Controller() {
        // Event handlers used to update charts
        timeUpdater = new TimeUpdater(this);
        coordinateUpdater = new CooridinateUpdater(this);

        // Data providers
        sun = DefaultSunlightProvider.getInstance();
        earth = DefaultShadedTerrainProvider.getInstance();
        earthClock = DefaultTimeProvider.getInstance();

        // Clock listens for TimeEvents and notifies SolarUpdater
        earthClock.addTimeListener(WeakListeners.create(
                TimeListener.class, timeUpdater, earthClock));
        timeUpdater.updateTime(
                new TimeEvent(this, null, ZonedDateTime.now(ZoneId.of("UTC"))));

        // LookupListener waits for arrival of  ReticuleCoordinateProvider ...
        reticuleLookupListener = (LookupEvent le) -> {
            if (reticuleResult != null && reticuleResult.allInstances().iterator().hasNext()) {
                // ... on arrival, reticule listens for ReticuleCoordinateEvents and notifies TerrainUpdater
                reticule = reticuleResult.allInstances().iterator().next();
                reticule.addReticuleCoordinateListener(
                        WeakListeners.create(ReticuleCoordinateListener.class, coordinateUpdater, reticule));
            }
        };
        // Initiate the ReticuleCoordinateProvider lookup 
        reticuleResult = Globe.getInstance().getLookup().lookupResult(ReticuleCoordinateProvider.class);
        reticuleResult.addLookupListener(reticuleLookupListener);
        reticuleLookupListener.resultChanged(null);

        // Listen for changes in the CPS Options/preferences...
        prefsChangeListener = (PreferenceChangeEvent ignored) -> {
            terrainShadingEnabled = prefs.getBoolean(
                    CpsOptions.TERRESTRAL_SHADING_ENABLED,
                    CpsOptions.DEFAULT_TERRESTRAL_SHADING);
        };
        prefs.addPreferenceChangeListener(prefsChangeListener);
        prefsChangeListener.preferenceChange(null);

        logger.config("Controller initialized.");
    }

    /**
     * Computes the fire behavior for the current coordinate.
     *
     * @throws VisADException
     * @throws RemoteException
     */
    private void computeFireBehavior() throws VisADException, RemoteException {
        if (!validateInputs()) {
            return;
        }
        try {
            // Use the previous 24 hours of sunlight and the current location for fuel conditioning
            ZonedDateTime time = timeRef.get();
            Coord3D coord = coordRef.get();
            SpatioTemporalDomain domain = new SpatioTemporalDomain(
                    new TemporalDomain(time.minusHours(24), 25),
                    new SpatialDomain(coord));
            Terrain terrain = terrainRef.get();
            SolarModel solarModel = new SolarModel(domain, true);
            WeatherModel weatherModel = new DiurnalWeatherModel(domain, weather, true);
            Weather wx = weatherModel.getWeather(time, coord);

            // Condition the fuel to the current weather and solar conditions.
            Fuel fuel = fuelProvider.newFuel(fuelModelRef.get());
            fuel.condition(time, coord, solarModel, weatherModel, terrain, fuelMoistureRef.get());

            // Compute the fire Behavior
//            FireEnvironment fire = fireProvider.computeFireBehavior(
//                    fuel.getFuelModel(),
//                    fuel.getFuelCondition(time),
//                    wx, terrain);
            
            FireReaction fire = fireModel.computeFireBehavior(
                    fuelModelRef.get(),
                    fuelMoistureRef.get(),
                    wx, 
                    terrain);

            if (fireShape==null) {
                fireShape = new FireShape();
            }
            fireShape.update(coord, fire, Duration.ofMinutes(5));
            
            // Compute the hourly fire behavior 
//            List<FireEnvironment> fires = new ArrayList<>();
//            for (FuelCondition cond : fuel.getConditions()) {
//                fires.add(fireModel.computeFireBehavior(fuel.getFuelModel(), cond, windSpd, windDir, terrain));
//            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "computeFireBehavior failed.", e);
            Exceptions.printStackTrace(e);
        }
        // Output the values
        //HaulChartTopComponent.findInstance().plotFireBehavior(fires.get(0));
    }

    private boolean validateInputs() throws RemoteException, VisADException {
        // Validate that we have the necessary inputs to compute fire behavior
        if (coordRef.get().isMissing()) {
            return false;
        }
//        if (date == null || date.isMissing()) {
//            return false;
//        } else if (fuelModel == null) {
//            return false;
//        } else if (fuelMoisture == null || fuelMoisture.isMissing()) {
//            return false;
//        } else if (solar == null || solar.isMissing()) {
//            return false;
//        } else if (airTemp == null || airTemp.isMissing()) {
//            return false;
//        } else if (airTemps == null || airTemps.isEmpty()) {
//            return false;
//        } else if (humidity == null || humidity.isMissing()) {
//            return false;
//        } else if (humidities == null || humidities.isEmpty()) {
//            return false;
//        } else if (location == null || location.isMissing()) {
//            return false;
//        } else if (terrain == null || terrain.isMissing()) {
//            return false;
//        }
        return true;
    }

    /**
     * CooridinateUpdater monitors the reticule (cross-hairs) layer and updates the UI with the
     * terrain under the cross-hairs.
     */
    private static class CooridinateUpdater implements ReticuleCoordinateListener, Runnable {

        private final Controller controller;
        private static final RequestProcessor processor = new RequestProcessor(CooridinateUpdater.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<ReticuleCoordinateEvent> lastEvent = new AtomicReference<>(new ReticuleCoordinateEvent(this, GeoCoord3D.INVALID_COORD));
        private final int UPDATE_INTERVAL_MS = 100;

        CooridinateUpdater(Controller controller) {
            this.controller = controller;
        }

        public void update() {
            // Sliding task: coallese the update events into fixed intervals
            if (this.updatingTask.isFinished()) {
                this.updatingTask.schedule(UPDATE_INTERVAL_MS);
            }
        }

        @Override
        public void updateCoordinate(ReticuleCoordinateEvent evt) {
            this.lastEvent.set(evt);
            update();
        }

        @Override
        @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
        public void run() {

            try {
                ReticuleCoordinateEvent event = this.lastEvent.get();
                if (event == null || controller.earth == null) {
                    return;
                }
                Coord3D coordinate = event.getCoordinate();
                Terrain terrain = controller.earth.getTerrain(coordinate);
                controller.coordRef.set(coordinate);
                controller.terrainRef.set(terrain);

                // Compute terrain shading
                ZonedDateTime time = controller.timeRef.get();
                Sunlight sunlight = controller.sunlightRef.get();
                Real azimuth = sunlight.getAzimuthAngle();
                Real zenith = sunlight.getZenithAngle();
                boolean isShaded
                        = controller.terrainShadingEnabled && !(azimuth.isMissing() || zenith.isMissing())
                        ? controller.earth.isCoordinateTerrestialShaded(coordinate, azimuth, zenith)
                        : false;

                // Get the fuel model data at the coordinate
                FuelModel fuelModel = controller.fuels != null
                        ? controller.fuels.getFuelModel(coordinate)
                        : StdFuelModel.INVALID;
                controller.fuelModelRef.set(fuelModel);
                controller.computeFireBehavior();

                // Update the CPS UI components (in the Event thread)
                EventQueue.invokeLater(() -> {
                    // Update Fuels
                    getFuelTopComponent().updateCharts(fuelModel);
                    // Update Forces
                    getForcesTopComponent().updateCharts(coordinate, terrain);
                    if (controller.terrainShadingEnabled) {
                        getForcesTopComponent().updateCharts(time, azimuth, zenith, isShaded);
                    // Weather

                        // FireBehavior
                    }
                });
            } catch (Exception e) {
                logger.log(Level.SEVERE, "CooridinateUpdater failed.", e);
                Exceptions.printStackTrace(e);
            }
        }
    }

    /**
     * The TimeUpdater monitors the clock and updates the CPS components with the current
     * earth time.
     */
    private static class TimeUpdater implements TimeListener, Runnable {

        private final Controller controller;
        private static final RequestProcessor processor = new RequestProcessor(TimeUpdater.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<TimeEvent> lastTimeEvent = new AtomicReference<>(new TimeEvent(this, null, null));

        TimeUpdater(Controller controller) {
            this.controller = controller;
        }

        @Override
        public void updateTime(TimeEvent evt) {
            this.lastTimeEvent.set(evt);
            if (this.updatingTask.isFinished()) {
                this.updatingTask.run();
            }
        }

        @Override
        @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
        public void run() {
            TimeEvent timeEvent = this.lastTimeEvent.get();
            if (timeEvent == null) {
                return;
            }
            ZonedDateTime time = timeEvent.getNewTime();
            TemporalDomain temporalDomain = new TemporalDomain(time.minusHours(24), 24);
            controller.timeRef.set(time);
            controller.temporalRef.set(temporalDomain);

            try {
                Sunlight sunlight = controller.sun.getSunlight(time, controller.coordRef.get());
                if (sunlight.isMissing()) {
                    return;
                }
                controller.sunlightRef.set(sunlight);
                Real azimuth = sunlight.getAzimuthAngle();
                Real zenith = sunlight.getZenithAngle();
                boolean isShaded = controller.terrainShadingEnabled
                        ? controller.earth.isCoordinateTerrestialShaded(
                                controller.coordRef.get(), azimuth, zenith)
                        : false;

                // Update the CPS UI ...
                EventQueue.invokeLater(() -> {
                    // Forces
                    getForcesTopComponent().updateCharts(time, azimuth, zenith, isShaded);
                    // Weather
                    getWeatherTopComponent().updateCharts(sunlight);
                    // Fuels

                });

            } catch (Exception ex) {
                logger.log(Level.SEVERE, "TimeUpdater failed.", ex);
                Exceptions.printStackTrace(ex);
            }

        }
    }

    /** Helper. */
    public static ForcesTopComponent getForcesTopComponent() {
        if (forcesWindow == null) {
            forcesWindow = (ForcesTopComponent) WindowManager.getDefault().findTopComponent(ForcesTopComponent.PREFERRED_ID);
            if (forcesWindow == null) {
                throw new IllegalStateException("Cannot find tc: " + ForcesTopComponent.PREFERRED_ID);
            }
        }
        return forcesWindow;
    }

    /** Helper. */
    public static FuelTopComponent getFuelTopComponent() {
        if (fuelWindow == null) {
            fuelWindow = (FuelTopComponent) WindowManager.getDefault().findTopComponent(FuelTopComponent.PREFERRED_ID);
            if (fuelWindow == null) {
                throw new IllegalStateException("Cannot find tc: " + FuelTopComponent.PREFERRED_ID);
            }
        }
        return fuelWindow;
    }

    /** Helper. */
    public static WeatherTopComponent getWeatherTopComponent() {
        if (weatherWindow == null) {
            weatherWindow = (WeatherTopComponent) WindowManager.getDefault().findTopComponent(WeatherTopComponent.PREFERRED_ID);
            if (weatherWindow == null) {
                throw new IllegalStateException("Cannot find tc: " + WeatherTopComponent.PREFERRED_ID);
            }
        }
        return weatherWindow;
    }

    /**
     * Singleton implementation.
     */
    private static class ControllerHolder {

        private static final Controller INSTANCE = new Controller();
    }
}
