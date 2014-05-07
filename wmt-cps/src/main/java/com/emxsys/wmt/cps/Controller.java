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
import com.emxsys.wmt.gis.api.TerrainProvider;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateEvent;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateListener;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateProvider;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.util.Positions;
import com.emxsys.wmt.solar.api.SolarUtil;
import com.emxsys.wmt.solar.api.SunlightProvider;
import com.emxsys.wmt.solar.spi.DefaultSunlightProvider;
import com.emxsys.wmt.time.api.TimeEvent;
import com.emxsys.wmt.time.api.TimeListener;
import com.emxsys.wmt.time.api.TimeProvider;
import com.emxsys.wmt.util.AngleUtil;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.awt.EventQueue;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;
import visad.VisADException;

/**
 * The CPS Controller class monitors time and location events, computes values and dispatches
 * messages to the various UI components.
 *
 * @author Bruce Schubert
 */
public class Controller {

    private ShadedTerrainProvider earth;
    private SunlightProvider sun;
    private TimeProvider clock;

    private final AtomicReference<Coord3D> coordRef = new AtomicReference<>(GeoCoord3D.INVALID_POSITION);
    private final AtomicReference<ZonedDateTime> timeRef = new AtomicReference<>(ZonedDateTime.now(ZoneId.of("UTC")));

    private Coord3D coordinate;
    private Terrain terrain;
    private ZonedDateTime time;
    private double solarHour;

    private final ReticuleMonitor reticuleMonitor;
    private final ClockMonitor clockMonitor;
    private Lookup.Result<ReticuleCoordinateProvider> reticuleResult;
    private final LookupListener reticuleLookupListener;

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
        // Event handlers
        clockMonitor = new ClockMonitor(this);
        reticuleMonitor = new ReticuleMonitor(this);

        // Data providers
        sun = DefaultSunlightProvider.getInstance();
        earth = Globe.getInstance().getShadedTerrainProvider();
        clock = Lookup.getDefault().lookup(TimeProvider.class);

        // Listener for the TimeEvents
        clock.addTimeListener(WeakListeners.create(TimeListener.class, clockMonitor, clock));
        clockMonitor.updateTime(new TimeEvent(this, null, ZonedDateTime.now(ZoneId.of("UTC"))));

        // Listen for the arrival of a ReticuleCoordinateProvider on the lookup (handles resultChanged())
        reticuleLookupListener = (LookupEvent le) -> {
            if (reticuleResult != null && reticuleResult.allInstances().iterator().hasNext()) {
                ReticuleCoordinateProvider reticule = reticuleResult.allInstances().iterator().next();

                // Listener for the ReticuleCoordinateEvents
                reticule.addReticuleCoordinateListener(
                        WeakListeners.create(ReticuleCoordinateListener.class, reticuleMonitor, reticule));
            }
        };
        reticuleResult = Globe.getInstance().getLookup().lookupResult(ReticuleCoordinateProvider.class);
        reticuleResult.addLookupListener(reticuleLookupListener);
        reticuleLookupListener.resultChanged(null);
    }

    /**
     * ReticuleMonitor monitors the reticule (cross-hairs) layer and updates the UI with the
     * coordinate under the cross-hairs.
     */
    private static class ReticuleMonitor implements ReticuleCoordinateListener, Runnable {

        private final Controller controller;
        private static final RequestProcessor processor = new RequestProcessor(SlopePanel.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<ReticuleCoordinateEvent> lastEvent = new AtomicReference<>(new ReticuleCoordinateEvent(this, GeoCoord3D.INVALID_POSITION));
        private final int UPDATE_INTERVAL_MS = 100;

        ReticuleMonitor(Controller controller) {
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
            controller.coordinate = event.getCoordinate();
            controller.coordRef.set(event.getCoordinate());

            controller.terrain = controller.earth == null ? null : controller.earth.getTerrain(controller.coordinate);

            // Update the CPS components (in the Event thread)
            EventQueue.invokeLater(() -> {
                PrimaryForcesTopComponent tc = (PrimaryForcesTopComponent) WindowManager.getDefault().findTopComponent("PrimaryForcesTopComponent");
                tc.updateCharts(controller.coordinate, controller.terrain);
            });
        }
    }

    /**
     * The ClockMonitor monitors the clock and updates the CPS components with the current
     * application time.
     */
    private static class ClockMonitor implements TimeListener, Runnable {

        private final Controller controller;
        private static final RequestProcessor processor = new RequestProcessor(PreheatPanel.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<TimeEvent> lastTimeEvent = new AtomicReference<>(new TimeEvent(this, null, null));

        ClockMonitor(Controller controller) {
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
            controller.time = timeEvent.getNewTime();
            controller.timeRef.set(timeEvent.getNewTime());
            Coord3D sunCoord = controller.sun.getSunPosition(Date.from(controller.time.toInstant()));
            Coord3D curCoord = controller.coordRef.get();

            try {
                double sunAngle = SolarUtil.getSolarAzimuthAngle(Date.from(controller.time.toInstant()), curCoord);

//            double sunLongitude = sunPosition.getLongitudeDegrees();
//            double curLongitude = curPosition.getLongitudeDegrees();
//
//            controller.solarHour = AngleUtil.angularDistanceBetween(sunLongitude, curLongitude) / 15; // 15 DEGREES per HOUR
//            // Sun is rising (neg solar hour) if it's longitude is east of the current location.
//            if (Math.signum(sunLongitude) >= Math.signum(curLongitude)) {
//                controller.solarHour *= curLongitude < sunLongitude ? -1 : 1;
//            } else { // Special case for handling longitudes crossing the int'l dateline
//                controller.solarHour *= sunLongitude < curLongitude ? -1 : 1;
//            }
                boolean isShaded = false;// earth.isCoordinateTerrestialShaded(coordEvent.getCoordinate(), timeEvent.getNewTime());
                // Update the CPS components (in the Event thread)
                EventQueue.invokeLater(() -> {
                    PrimaryForcesTopComponent tc = (PrimaryForcesTopComponent) WindowManager.getDefault().findTopComponent("PrimaryForcesTopComponent");
                    tc.updateCharts(controller.time, sunAngle);
                });
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

}
