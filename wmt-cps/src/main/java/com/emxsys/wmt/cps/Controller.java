/*
 * Copyright (c) 2014, bruce 
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

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.gis.api.ShadedTerrainProvider;
import com.emxsys.wmt.gis.api.Terrain;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateEvent;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateListener;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateProvider;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.solar.api.SolarUtil;
import com.emxsys.wmt.solar.api.SunlightProvider;
import com.emxsys.wmt.solar.spi.DefaultSunlightProvider;
import com.emxsys.wmt.time.api.TimeEvent;
import com.emxsys.wmt.time.api.TimeListener;
import com.emxsys.wmt.time.api.TimeProvider;
import java.awt.EventQueue;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;
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
    private static PrimaryForcesTopComponent tc;
    private ShadedTerrainProvider earth;
    private SunlightProvider sun;
    private TimeProvider clock;
    private ReticuleCoordinateProvider reticule;

    private final AtomicReference<Coord3D> coordRef = new AtomicReference<>(GeoCoord3D.INVALID_POSITION);
    private final AtomicReference<ZonedDateTime> timeRef = new AtomicReference<>(ZonedDateTime.now(ZoneId.of("UTC")));


    private final TerrainUpdater terrainUpdater;
    private final SolarUpdater solarUpdater;
    private Lookup.Result<ReticuleCoordinateProvider> reticuleResult;
    private final LookupListener reticuleLookupListener;

    static {
        logger.setLevel(Level.ALL);
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
     * Constructs the Controller singleton.
     */
    private Controller() {
        // Event handlers used to update charts
        solarUpdater = new SolarUpdater(this);
        terrainUpdater = new TerrainUpdater(this);

        // Data providers
        sun = DefaultSunlightProvider.getInstance();
        earth = Globe.getInstance().getShadedTerrainProvider();
        clock = Lookup.getDefault().lookup(TimeProvider.class);

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
    }

    /**
     * Convenience method.
     */
    private static PrimaryForcesTopComponent getTopComponent() {
        if (tc == null) {
            tc = (PrimaryForcesTopComponent) WindowManager.getDefault().findTopComponent(PrimaryForcesTopComponent.PREFERRED_ID);
            if (tc == null) {
                throw new IllegalStateException("Cannot find tc: " + PrimaryForcesTopComponent.PREFERRED_ID);
            }
        }
        return tc;
    }

    /**
     * ReticuleMonitor monitors the reticule (cross-hairs) layer and updates the UI with the
     * coordinate under the cross-hairs.
     */
    private static class TerrainUpdater implements ReticuleCoordinateListener, Runnable {

        private final Controller controller;
        private static final RequestProcessor processor = new RequestProcessor(SlopePanel.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<ReticuleCoordinateEvent> lastEvent = new AtomicReference<>(new ReticuleCoordinateEvent(this, GeoCoord3D.INVALID_POSITION));
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
            if (event == null) {
                return;
            }
            Coord3D coordinate = event.getCoordinate();
            controller.coordRef.set(coordinate);
            logger.log(Level.FINE, "Coord: {0}", coordinate);

            Terrain terrain = controller.earth == null ? null : controller.earth.getTerrain(coordinate);

            // Update the CPS components (in the Event thread)
            EventQueue.invokeLater(() -> {
                getTopComponent().updateCharts(coordinate, terrain);
            });
        }
    }

    /**
     * The ClockMonitor monitors the clock and updates the CPS components with the current
     * application time.
     */
    private static class SolarUpdater implements TimeListener, Runnable {

        private final Controller controller;
        private static final RequestProcessor processor = new RequestProcessor(PreheatPanel.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<TimeEvent> lastTimeEvent = new AtomicReference<>(new TimeEvent(this, null, null));

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

            Coord3D sunCoord = controller.sun.getSunPosition(time);
            Coord3D curCoord = controller.coordRef.get();
            logger.fine("Sun coord: " + sunCoord);

            RealTuple azimuthAltitude = SolarUtil.getAzimuthAltitude(curCoord, sunCoord);
            logger.fine("Sun Az Al: " + azimuthAltitude);
            
            if (azimuthAltitude.isMissing()) {
                return;
            }
            boolean isShaded = false;// earth.isCoordinateTerrestialShaded(coordEvent.getCoordinate(), timeEvent.getNewTime());
            // Update the CPS components in the Event thread
            EventQueue.invokeLater(() -> {
                try {
                    getTopComponent().updateCharts(time, azimuthAltitude.getRealComponents()[0]);
                } catch (VisADException | RemoteException ex) {
                }
            });

        }
    }

}
