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
import com.emxsys.gis.api.event.ReticuleCoordinateEvent;
import com.emxsys.gis.api.event.ReticuleCoordinateListener;
import com.emxsys.gis.api.event.ReticuleCoordinateProvider;
import com.emxsys.gis.spi.DefaultShadedTerrainProvider;
import com.emxsys.solar.api.SolarType;
import com.emxsys.solar.api.SunlightProvider;
import com.emxsys.solar.spi.DefaultSunlightProvider;
import com.emxsys.time.api.TimeEvent;
import com.emxsys.time.api.TimeListener;
import com.emxsys.time.api.TimeProvider;
import com.emxsys.time.spi.DefaultTimeProvider;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.api.SimpleWeatherProvider;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelModelProvider;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wmt.cps.options.CpsOptions;
import com.emxsys.wmt.cps.ui.ForcesTopComponent;
import com.emxsys.wmt.cps.ui.FuelTopComponent;
import com.emxsys.wmt.cps.ui.PreheatForcePanel;
import com.emxsys.wmt.cps.ui.SlopeForcePanel;
import com.emxsys.wmt.cps.ui.WeatherTopComponent;
import com.emxsys.wmt.globe.Globe;
import java.awt.EventQueue;
import java.rmi.RemoteException;
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
import visad.RealTuple;
import visad.RealType;
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
    private final TimeProvider clock;
    private FuelModelProvider fuels;
    private ReticuleCoordinateProvider reticule;

    private final SimpleWeatherProvider simpleWx = new SimpleWeatherProvider();
    private final DiurnalWeatherProvider diurnalWx = new DiurnalWeatherProvider();

    private final AtomicReference<ZonedDateTime> timeRef = new AtomicReference<>(ZonedDateTime.now(ZoneId.of("UTC")));
    private final AtomicReference<Coord3D> coordRef = new AtomicReference<>(GeoCoord3D.INVALID_COORD);
    private final AtomicReference<FuelModel> fuelModelRef = new AtomicReference<>(StdFuelModel.INVALID);
    private final AtomicReference<Real> azimuthRef = new AtomicReference<>(new Real(SolarType.AZIMUTH_ANGLE));
    private final AtomicReference<Real> zenithRef = new AtomicReference<>(new Real(SolarType.ZENITH_ANGLE));

    private final TerrainUpdater terrainUpdater;
    private final SolarUpdater solarUpdater;
    private final LookupListener reticuleLookupListener;
    private Lookup.Result<ReticuleCoordinateProvider> reticuleResult;

    private static final Preferences prefs = NbPreferences.forModule(CpsOptions.class);
    private final PreferenceChangeListener prefsChangeListener;

    private boolean computeTerrestrialShading;

    static {
        logger.setLevel(Level.FINE);
    }

    /**
     * Gets the Controller singleton instance.
     *
     * @return The singleton.
     */
    public static Controller getInstance() {
        return ControllerHolder.INSTANCE;
    }

    /**
     * Singleton implementation.
     */
    private static class ControllerHolder {

        private static final Controller INSTANCE = new Controller();
    }

    /**
     * Constructs the Controller singleton; attaches listeners.
     */
    private Controller() {
        // Event handlers used to update charts
        solarUpdater = new SolarUpdater(this);
        terrainUpdater = new TerrainUpdater(this);

        // Data providers
        sun = DefaultSunlightProvider.getInstance();
        earth = DefaultShadedTerrainProvider.getInstance();
        clock = DefaultTimeProvider.getInstance();

        // Clock listens for TimeEvents and notifies SolarUpdater
        clock.addTimeListener(WeakListeners.create(TimeListener.class, solarUpdater, clock));
        solarUpdater.updateTime(new TimeEvent(this, null, ZonedDateTime.now(ZoneId.of("UTC"))));

        // LookupListener waits for arrival of  ReticuleCoordinateProvider ...
        reticuleLookupListener = (LookupEvent le) -> {
            if (reticuleResult != null && reticuleResult.allInstances().iterator().hasNext()) {
                // ... on arrival, reticule listens for ReticuleCoordinateEvents and notifies TerrainUpdater
                reticule = reticuleResult.allInstances().iterator().next();
                reticule.addReticuleCoordinateListener(
                        WeakListeners.create(ReticuleCoordinateListener.class, terrainUpdater, reticule));
            }
        };
        // Initiate the ReticuleCoordinateProvider lookup 
        reticuleResult = Globe.getInstance().getLookup().lookupResult(ReticuleCoordinateProvider.class);
        reticuleResult.addLookupListener(reticuleLookupListener);
        reticuleLookupListener.resultChanged(null);

        // Listen for changes in the CPS Options/preferences...
        prefsChangeListener = (PreferenceChangeEvent ignored) -> {
            RealType uom = prefs.get(CpsOptions.UOM_KEY, CpsOptions.UOM_US).matches(CpsOptions.UOM_US)
                    ? WeatherType.AIR_TEMP_F : WeatherType.AIR_TEMP_C;
            Real tempSunrise = new Real(uom, prefs.getInt(CpsOptions.TEMP_SUNRISE_KEY, CpsOptions.DEFAULT_TEMP_SUNRISE));
            Real tempNoon = new Real(uom, prefs.getInt(CpsOptions.TEMP_1200_KEY, CpsOptions.DEFAULT_TEMP_1200));
            Real temp1400 = new Real(uom, prefs.getInt(CpsOptions.TEMP_1400_KEY, CpsOptions.DEFAULT_TEMP_1400));
            Real tempSunset = new Real(uom, prefs.getInt(CpsOptions.TEMP_SUNSET_KEY, CpsOptions.DEFAULT_TEMP_SUNSET));

            Real rhSunrise = new Real(uom, prefs.getInt(CpsOptions.RH_SUNRISE_KEY, CpsOptions.DEFAULT_RH_SUNRISE));
            Real rhNoon = new Real(uom, prefs.getInt(CpsOptions.RH_1200_KEY, CpsOptions.DEFAULT_RH_1200));
            Real rh1400 = new Real(uom, prefs.getInt(CpsOptions.RH_1400_KEY, CpsOptions.DEFAULT_RH_1400));
            Real rhSunset = new Real(uom, prefs.getInt(CpsOptions.RH_SUNSET_KEY, CpsOptions.DEFAULT_RH_SUNSET));

            diurnalWx.initializeAirTemperatures(tempSunrise, tempNoon, temp1400, tempSunset);
            diurnalWx.initializeRelativeHumidities(rhSunrise, rhNoon, rh1400, rhSunset);

            computeTerrestrialShading = prefs.getBoolean(CpsOptions.TERRESTRAL_SHADING_ENABLED, CpsOptions.DEFAULT_TERRESTRAL_SHADING);
        };
        prefs.addPreferenceChangeListener(prefsChangeListener);
        // Fire a change event to load the preferences
        prefsChangeListener.preferenceChange(null);

    }

    public SimpleWeatherProvider getSimpleWeather() {
        return this.simpleWx;
    }

    /**
     * Sets the FuelModelProvider used by the Controller to determine the FuelModel at the current
     * coordinate. Called by the FuelTopComponent.
     *
     * @param fuels The new FuelModelProvider.
     */
    public void setFuelModelProvider(FuelModelProvider fuels) {
        if (fuels==null) {
            throw new IllegalArgumentException("FuelModelProvider is null.");
        }
        logger.log(Level.CONFIG, "New FuelModelProvider: {0}", fuels.toString());
        this.fuels = fuels;
    }

    /**
     * Convenience method.
     */
    private static ForcesTopComponent getForcesTopComponent() {
        if (forcesWindow == null) {
            forcesWindow = (ForcesTopComponent) WindowManager.getDefault().findTopComponent(ForcesTopComponent.PREFERRED_ID);
            if (forcesWindow == null) {
                throw new IllegalStateException("Cannot find tc: " + ForcesTopComponent.PREFERRED_ID);
            }
        }
        return forcesWindow;
    }

    /**
     * Convenience method.
     */
    private static FuelTopComponent getFuelTopComponent() {
        if (fuelWindow == null) {
            fuelWindow = (FuelTopComponent) WindowManager.getDefault().findTopComponent(FuelTopComponent.PREFERRED_ID);
            if (fuelWindow == null) {
                throw new IllegalStateException("Cannot find tc: " + FuelTopComponent.PREFERRED_ID);
            }
        }
        return fuelWindow;
    }

    /**
     * TerrainUpdater monitors the reticule (cross-hairs) layer and updates the UI with the
     * terrain under the cross-hairs.
     */
    private static class TerrainUpdater implements ReticuleCoordinateListener, Runnable {

        private final Controller controller;
        private static final RequestProcessor processor = new RequestProcessor(SlopeForcePanel.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<ReticuleCoordinateEvent> lastEvent = new AtomicReference<>(new ReticuleCoordinateEvent(this, GeoCoord3D.INVALID_COORD));
        private final int UPDATE_INTERVAL_MS = 100;

        TerrainUpdater(Controller controller) {
            this.controller = controller;
        }

        @Override
        public void updateCoordinate(ReticuleCoordinateEvent evt) {
            // Sliding task: coallese the update events into fixed intervals
            this.lastEvent.set(evt);
            if (this.updatingTask.isFinished()) {
                this.updatingTask.schedule(UPDATE_INTERVAL_MS);
            }
        }

        @Override
        public void run() {
            ReticuleCoordinateEvent event = this.lastEvent.get();
            if (event == null || controller.earth == null) {
                return;
            }
            Coord3D coordinate = event.getCoordinate();
            controller.coordRef.set(coordinate);

            // Get the terrain related data
            Terrain terrain = controller.earth.getTerrain(coordinate);
            ZonedDateTime time = controller.timeRef.get();
            Real azimuth = controller.azimuthRef.get();
            Real zenith = controller.zenithRef.get();
            boolean isShaded
                    = controller.computeTerrestrialShading && !(azimuth.isMissing() || zenith.isMissing())
                    ? controller.earth.isCoordinateTerrestialShaded(coordinate, azimuth, zenith) : false;

            // Get the fuel model data at the coordinate
            FuelModel fuelModel = controller.fuels != null 
                    ? controller.fuels.getFuelModel(coordinate) : StdFuelModel.INVALID;
            controller.fuelModelRef.set(fuelModel);

            // Update the CPS UI components (in the Event thread)
            EventQueue.invokeLater(() -> {
                // Update Fuels
                getFuelTopComponent().updateCharts(fuelModel);
                
                // Update Forces
                getForcesTopComponent().updateCharts(coordinate, terrain);
                if (controller.computeTerrestrialShading) {
                    getForcesTopComponent().updateCharts(time, azimuth, zenith, isShaded);
                }
            });
        }
    }

    /**
     * The SolarUpdater monitors the clock and updates the CPS components with the current
     * application time.
     */
    private static class SolarUpdater implements TimeListener, Runnable {

        private final Controller controller;
        private static final RequestProcessor processor = new RequestProcessor(PreheatForcePanel.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<TimeEvent> lastTimeEvent = new AtomicReference<>(new TimeEvent(this, null, null));
        // Array indicies
        private static final int AZIMUTH_INDEX = SolarType.SUN_POSITION.getIndex(SolarType.AZIMUTH_ANGLE);
        private static final int ZENITH_INDEX = SolarType.SUN_POSITION.getIndex(SolarType.ZENITH_ANGLE);

        SolarUpdater(Controller controller) {
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
        public void run() {
            TimeEvent timeEvent = this.lastTimeEvent.get();
            if (timeEvent == null) {
                return;
            }
            ZonedDateTime time = timeEvent.getNewTime();
            controller.timeRef.set(time);

            try {
                RealTuple sunPosition = controller.sun.getSunPosition(time, controller.coordRef.get());
                if (sunPosition.isMissing()) {
                    return;
                }
                Real azimuth = (Real) sunPosition.getComponent(AZIMUTH_INDEX);
                Real zenith = (Real) sunPosition.getComponent(ZENITH_INDEX);
                controller.azimuthRef.set(azimuth);
                controller.zenithRef.set(zenith);
                boolean isShaded
                        = controller.computeTerrestrialShading
                        ? controller.earth.isCoordinateTerrestialShaded(controller.coordRef.get(), azimuth, zenith)
                        : false;

                EventQueue.invokeLater(() -> {
                    getForcesTopComponent().updateCharts(time, azimuth, zenith, isShaded);
                });

            } catch (VisADException | RemoteException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

}
