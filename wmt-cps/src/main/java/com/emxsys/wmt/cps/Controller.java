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
import com.emxsys.weather.api.SimpleWeatherProvider;
import com.emxsys.wmt.cps.options.CpsOptions;
import com.emxsys.wmt.cps.ui.ForcesTopComponent;
import com.emxsys.wmt.cps.ui.PreheatForcePanel;
import com.emxsys.wmt.cps.ui.SlopeForcePanel;
import com.emxsys.wmt.globe.Globe;
import java.awt.EventQueue;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
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
import visad.VisADException;

/**
 * The CPS Controller class monitors time and location events, computes values and dispatches
 * messages to the various UI components.
 *
 * @author Bruce Schubert
 */
public class Controller {

    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    private static ForcesTopComponent tc;
    private final ShadedTerrainProvider earth;
    private final SunlightProvider sun;
    private final TimeProvider clock;
    private ReticuleCoordinateProvider reticule;
    private final SimpleWeatherProvider manualWx = new SimpleWeatherProvider();
    private final AtomicReference<ZonedDateTime> timeRef = new AtomicReference<>(ZonedDateTime.now(ZoneId.of("UTC")));
    private final AtomicReference<Coord3D> coordRef = new AtomicReference<>(GeoCoord3D.INVALID_COORD);
    private final AtomicReference<Real> azimuthRef = new AtomicReference<>(new Real(SolarType.AZIMUTH_ANGLE));
    private final AtomicReference<Real> zenithRef = new AtomicReference<>(new Real(SolarType.ZENITH_ANGLE));

    private final TerrainUpdater terrainUpdater;
    private final SolarUpdater solarUpdater;
    private final LookupListener reticuleLookupListener;
    private Lookup.Result<ReticuleCoordinateProvider> reticuleResult;

    private static final Preferences prefs = NbPreferences.forModule(CpsOptions.class);
    private boolean computeTerrestrialShading = prefs.getBoolean(CpsOptions.TERRESTRAL_SHADING_ENABLED, CpsOptions.DEFAULT_TERRESTRAL_SHADING);

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

        // Listen for changes in the CPS Options...
        prefs.addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            computeTerrestrialShading = prefs.getBoolean(CpsOptions.TERRESTRAL_SHADING_ENABLED, CpsOptions.DEFAULT_TERRESTRAL_SHADING);
        });

    }
    
    public SimpleWeatherProvider getSimpleWeather() {
        return this.manualWx;
    }

    /**
     * Convenience method.
     */
    private static ForcesTopComponent getTopComponent() {
        if (tc == null) {
            tc = (ForcesTopComponent) WindowManager.getDefault().findTopComponent(ForcesTopComponent.PREFERRED_ID);
            if (tc == null) {
                throw new IllegalStateException("Cannot find tc: " + ForcesTopComponent.PREFERRED_ID);
            }
        }
        return tc;
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

            Terrain terrain = controller.earth.getTerrain(coordinate);
            ZonedDateTime time = controller.timeRef.get();
            Real azimuth = controller.azimuthRef.get();
            Real zenith = controller.zenithRef.get();

            boolean isShaded = 
                    controller.computeTerrestrialShading && !(azimuth.isMissing() || zenith.isMissing())
                    ? controller.earth.isCoordinateTerrestialShaded(coordinate, azimuth, zenith)
                    : false;

            // Update the CPS UI components (in the Event thread)
            EventQueue.invokeLater(() -> {
                getTopComponent().updateCharts(coordinate, terrain);
                if (controller.computeTerrestrialShading) {
                    getTopComponent().updateCharts(time, azimuth, zenith, isShaded);
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
                boolean isShaded = 
                        controller.computeTerrestrialShading
                        ? controller.earth.isCoordinateTerrestialShaded(controller.coordRef.get(), azimuth, zenith)
                        : false;

                EventQueue.invokeLater(() -> {
                    getTopComponent().updateCharts(time, azimuth, zenith, isShaded);
                });

            } catch (VisADException | RemoteException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

}
