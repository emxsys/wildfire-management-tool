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
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.ShadedTerrainProvider;
import com.emxsys.gis.api.Terrain;
import com.emxsys.gis.api.event.ReticuleCoordinateEvent;
import com.emxsys.gis.api.event.ReticuleCoordinateListener;
import com.emxsys.gis.api.event.ReticuleCoordinateProvider;
import com.emxsys.gis.spi.ShadedTerrainProviderFactory;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.solar.api.SunlightProvider;
import com.emxsys.solar.spi.SunlightProviderFactory;
import com.emxsys.time.api.TimeEvent;
import com.emxsys.time.api.TimeListener;
import com.emxsys.time.api.TimeProvider;
import com.emxsys.time.spi.TimeProviderFactory;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.SpatioTemporalDomain;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.weather.api.SimpleWeatherProvider;
import com.emxsys.weather.api.WeatherTuple;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelModelProvider;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wmt.cps.options.CpsOptions;
import com.emxsys.wmt.cps.views.ForcesTopComponent;
import com.emxsys.wmt.cps.views.forces.PreheatForcePanel;
import com.emxsys.wmt.globe.Globe;
import java.beans.PropertyChangeEvent;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import visad.Real;

/**
 * The CPS Controller class monitors time and location events, specifies the domain, updates the
 * model, and notifies the TopComponent views.
 *
 * @author Bruce Schubert
 */
public class Controller {

    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    private static final Preferences prefs = NbPreferences.forModule(CpsOptions.class);

    /**
     * Gets the Controller singleton instance.
     *
     * @return The singleton.
     */
    public static synchronized Controller getInstance() {
        if (null == instance) {
            instance = new Controller();
        }
        return instance;
        //return ControllerHolder.INSTANCE;
    }
    private static Controller instance;

    // Data providers - Event generators
    private final ShadedTerrainProvider earth;
    private final SunlightProvider sun;
    private final TimeProvider earthClock;
    private ReticuleCoordinateProvider reticule;
    private FuelModelProvider fuels;

    private final SimpleWeatherProvider simpleWeather = new SimpleWeatherProvider();

    // Overrides (these values are read and writted on the EDT)
    private boolean overrideFuelTemp = false;
    private Real fuelTemp = null;

    // Event handlers
    private final CooridinateUpdater coordinateUpdater;
    private final TimeUpdater timeUpdater;
    private final LookupListener reticuleLookupListener;
    private Lookup.Result<ReticuleCoordinateProvider> reticuleResult;

    // The domain
    private TemporalDomain temporalDomain = new TemporalDomain();
    private SpatialDomain spatialDomain = new SpatialDomain();

    // The current project
    private Project currentProject;
    private Lookup.Result<Project> projects;
    private LookupListener projectListener;

    private final PreferenceChangeListener prefsChangeListener;
    private boolean terrainShadingEnabled;

    // CPS Data Model
    private final Model model = Model.getInstance();

    /**
     * Hidden constructor creates the Controller singleton; attaches listeners.
     */
    private Controller() {
        // Event handlers used to handle temporal and spatial events.
        timeUpdater = new TimeUpdater(this);
        coordinateUpdater = new CooridinateUpdater(this);

        // Data providers
        sun = SunlightProviderFactory.getInstance();
        earth = ShadedTerrainProviderFactory.getInstance();
        earthClock = TimeProviderFactory.getInstance();

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

        // Listen for projects, to control the with/without project behavior
        this.projects = Utilities.actionsGlobalContext().lookupResult(Project.class);
        this.projectListener = (LookupEvent ev) -> {
            Collection<? extends Project> allInstances = projects.allInstances();
            if (allInstances.size() == 1) {
                currentProject = allInstances.iterator().next();
            }
        };
        this.projects.addLookupListener(this.projectListener);
        this.projectListener.resultChanged(null);

        // TODO: Move to Forces view
        // Listen for changes from the manual Fuel Temp input controls
        ForcesTopComponent forcesWindow = ForcesTopComponent.getInstance();

        // Listen for changes from the manual Fuel Temp input controls
        forcesWindow.addFuelTempPropertyChangeListener((PropertyChangeEvent evt) -> {
            switch (evt.getPropertyName()) {
                case PreheatForcePanel.PROP_OVERRIDE_FUEL_TEMP:
                    overrideFuelTemp = (boolean) evt.getNewValue();
                    break;
                case PreheatForcePanel.PROP_FUEL_TEMP:
                    fuelTemp = ((Real) evt.getNewValue());
                    model.modifyFuelbed(fuelTemp);
                    model.computeFireBehavior();
                    model.updateViews();
                    break;
            }
        });

//        forcesWindow.addFuelMoisturePropertyChangeListener((PropertyChangeEvent evt) -> {
//            Real dead1HrFuelMoisture = ((Real) evt.getNewValue());
//            FuelMoisture fm = model.getFuelMoisture();
//            model.modifyFuelbed(FuelMoistureTuple.fromReals(
//                    dead1HrFuelMoisture,
//                    fm.getDead10HrFuelMoisture(),
//                    fm.getDead100HrFuelMoisture(),
//                    fm.getLiveHerbFuelMoisture(),
//                    fm.getLiveWoodyFuelMoisture()));
//            model.computeFireBehavior();
//            model.updateViews();
//        });
        // Listen for changes from the manual Wind controls
        forcesWindow.addWindDirPropertyChangeListener((PropertyChangeEvent evt) -> {
            simpleWeather.setWindDirection((Real) evt.getNewValue());
            model.setWeather(simpleWeather.getWeather());
            model.conditionFuelbed();
            if (overrideFuelTemp && fuelTemp != null) {
                model.modifyFuelbed(fuelTemp);
            }
            model.computeFireBehavior();
            model.updateViews();
        });
        forcesWindow.addWindSpeedPropertyChangeListener((PropertyChangeEvent evt) -> {
            simpleWeather.setWindSpeed((Real) evt.getNewValue());
            model.setWeather(simpleWeather.getWeather());
            model.conditionFuelbed();
            if (overrideFuelTemp && fuelTemp != null) {
                model.modifyFuelbed(fuelTemp);
            }
            model.computeFireBehavior();
            model.updateViews();
        });

        logger.config("Controller initialized.");
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
        model.setFuelMoisture(fuelMoisture);
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

        // Fire a coordinate-based update to change the current FuelModel
        coordinateUpdater.update();
    }

    /**
     * CooridinateUpdater monitors the globe's reticule (cross-hairs) layer and updates the domain
     * and the model with the terrain under the cross-hairs.
     */
    private static class CooridinateUpdater implements ReticuleCoordinateListener, Runnable {

        private static final RequestProcessor processor = new RequestProcessor(CooridinateUpdater.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<ReticuleCoordinateEvent> lastEvent = new AtomicReference<>(new ReticuleCoordinateEvent(this, GeoCoord3D.INVALID_COORD));
        private final int UPDATE_INTERVAL_MS = 100;
        private final Controller controller;

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

                Coord3D coord = event.getCoordinate();
                if (coord.isMissing()) {
                    return;
                }

                // Update the terrain
                Terrain terrain = controller.earth.getTerrain(coord);
                controller.model.setCoord(coord);
                controller.model.setTerrain(terrain);

                // Update the temporal-spatial domain
                controller.spatialDomain = SpatialDomain.from(coord);
                SpatioTemporalDomain domain = new SpatioTemporalDomain(controller.temporalDomain, controller.spatialDomain);
                controller.model.setDomain(domain);

                // Update the fire environment
                Sunlight sunlight = controller.model.getSunlight();
                Real azimuth = sunlight.getAzimuthAngle();
                Real zenith = sunlight.getZenithAngle();
                GeoCoord2D subsolarPoint = GeoCoord2D.fromReals(sunlight.getSubsolarLatitude(), sunlight.getSubsolarLongitude());
                boolean isShaded
                        = controller.terrainShadingEnabled && !(azimuth.isMissing() || zenith.isMissing())
                                ? controller.earth.isCoordinateTerrestialShaded(coord, subsolarPoint)
                                //? controller.earth.isCoordinateTerrestialShaded(coord, azimuth, zenith)
                                : false;
                controller.model.setShaded(isShaded);

                // Update the Weather
                WeatherManager.getInstance().updateCoord(coord);
                WeatherTuple weather = WeatherManager.getInstance().getWeatherAt(coord, controller.model.getDateTime());
                controller.model.setWeather(weather);
                // Update the Weather
//                if (controller.weather != null) {
//                    if (controller.weather instanceof DiurnalWeatherProvider) {
//                        controller.model.setWeather(((DiurnalWeatherProvider)simpleWeather).getWeatherObservation(controller.model.getDateTime(), null));
//                    }
//                }

                // Get the fuel model data at the coordinate
                FuelModel fuelModel = controller.fuels != null
                        ? controller.fuels.getFuelModel(coord)
                        : StdFuelModel.INVALID;
                controller.model.setFuelModel(fuelModel);

                // Update the fuel bed - use the fuel temp override if provided
                controller.model.conditionFuelbed();
                if (controller.overrideFuelTemp && controller.fuelTemp != null) {
                    controller.model.modifyFuelbed(controller.fuelTemp);
                }

                // Update the fire behavior
                controller.model.computeFireBehavior();

                // Update the GUI 
                controller.model.updateViews();

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

        private static final RequestProcessor processor = new RequestProcessor(TimeUpdater.class);
        private final RequestProcessor.Task updatingTask = processor.create(this, true); // true = initiallyFinished
        private final AtomicReference<TimeEvent> lastTimeEvent = new AtomicReference<>(new TimeEvent(this, null, null));
        private final Controller controller;

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

            // Update the temporal-spatial domain
            ZonedDateTime time = timeEvent.getNewTime();
            controller.temporalDomain = new TemporalDomain(time.minusHours(24), 25);

            WeatherManager.getInstance().updateTime(time);

            SpatioTemporalDomain domain = new SpatioTemporalDomain(controller.temporalDomain, controller.spatialDomain);
            controller.model.setDateTime(time);
            controller.model.setDomain(domain);

            try {
                // Update solar angles and position
                Coord3D coord = controller.model.getCoord();
                Sunlight sunlight = controller.sun.getSunlight(time, coord);
                if (sunlight.isMissing()) {
                    return;
                }
                controller.model.setSunlight(sunlight);

                // Update the Weather
                WeatherManager.getInstance().updateTime(time);
                WeatherTuple weather = WeatherManager.getInstance().getWeatherAt(coord, time);
                controller.model.setWeather(weather);

//                if (controller.forecastProvider != null) {
//                    if (controller.forecastProvider instanceof DiurnalWeatherProvider) {
//                        // Update sunrise/sunset times
//                        DiurnalWeatherProvider diurnalWx = (DiurnalWeatherProvider) controller.weatherForecaster;
//                        diurnalWx.setSunlight(sunlight); 
//                    }
//                    
//                    if (controller.forecastProvider.hasCapability(WeatherForecaster.class)) {
//                        WeatherForecaster wxObs = controller.forecastProvider.getCapability(WeatherForecaster.class);
//                        controller.model.setWeather(wxObs.getSpotWeather(time, coord));                        
//                    }
////                    else if (controller.weatherSource.hasCapability(PointForecaster.class)) {
//                        PointForecaster forecaster = controller.weatherSource.getCapability(PointForecaster.class);
//                        Field forecast = forecaster.getForecast(coord);
//                        RealTuple wx = (RealTuple) forecast.evaluate(new DateTime(time.toEpochSecond()));
//
//                    }
//                }
                // Update the fire environment
                Real azimuth = sunlight.getAzimuthAngle();
                Real zenith = sunlight.getZenithAngle();
                GeoCoord2D subsolarPoint = GeoCoord2D.fromReals(sunlight.getSubsolarLatitude(), sunlight.getSubsolarLongitude());
                boolean isShaded = controller.terrainShadingEnabled
                        ? controller.earth.isCoordinateTerrestialShaded(coord, subsolarPoint)
                        //? controller.earth.isCoordinateTerrestialShaded(coord, azimuth, zenith)
                        : false;
                controller.model.setShaded(isShaded);
                // Update the fuel
                controller.model.conditionFuelbed();
                if (controller.overrideFuelTemp && controller.fuelTemp != null) {
                    controller.model.modifyFuelbed(controller.fuelTemp);
                }
                // Update the fire
                controller.model.computeFireBehavior();

                // Update the GUI 
                controller.model.updateViews();

            } catch (Exception ex) {
                logger.log(Level.SEVERE, "TimeUpdater failed.", ex);
                Exceptions.printStackTrace(ex);
            }

        }
    }

    /**
     * Singleton implementation.
     */
//    private static class ControllerHolder {
//
//        private static final Controller INSTANCE = new Controller();
//    }
}
