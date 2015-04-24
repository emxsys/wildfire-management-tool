/*
 * Copyright (c) 2014-2015, Bruce Schubert
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
import com.emxsys.solar.api.BasicSunlight;
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
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelModelProvider;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.BasicFuelModel;
import com.emxsys.wmt.cps.options.CpsOptions;
import com.emxsys.wmt.globe.Globe;
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
public final class Controller {

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
    }

    private static Controller instance;

    // Data providers - Event generators
    private final ShadedTerrainProvider earth;
    private final SunlightProvider sun;
    private final TimeProvider earthClock;
    private ReticuleCoordinateProvider reticule;
    private FuelModelProvider fuels;

    private final SimpleWeatherProvider simpleWeather = new SimpleWeatherProvider();

    // Overrides (unsyncronized: these values are read and written on the EDT)
    private Real airTemp = null;
    private Real relHumidity = null;
    private Real windDir = null;
    private Real windSpd = null;
    private Real skyCover = null;
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
    private final LookupListener projectListener;

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

        // Clock listens for TimeEvents and notifies TimeUpdater
        earthClock.addTimeListener(WeakListeners.create(TimeListener.class, timeUpdater, earthClock));
        timeUpdater.updateTime(new TimeEvent(this, null, earthClock.getTime()));

        // LookupListener waits for arrival of a ReticuleCoordinateProvider ...
        reticuleLookupListener = (LookupEvent le) -> {
            if (reticuleResult != null && reticuleResult.allInstances().iterator().hasNext()) {
                // ... on arrival, reticule listens for ReticuleCoordinateEvents and notifies CoordianteUpdater
                reticule = reticuleResult.allInstances().iterator().next();
                reticule.addReticuleCoordinateListener(
                        WeakListeners.create(ReticuleCoordinateListener.class, coordinateUpdater, reticule));
                reticule.getReticuleCoordinate();
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

        logger.config("Controller initialized.");
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
        updateFuelModel();
    }

    /**
     * Sets the initial fuel moisture used to determine the fire behavior at the current
     * coordinate.
     *
     * @param fuelMoisture The new FuelMoisture.
     */
    public void setFuelMoisture(FuelMoisture fuelMoisture) {
        if (fuelMoisture == null) {
            throw new IllegalArgumentException("FuelMoisture is null");
        }
        logger.log(Level.CONFIG, "FuelMoisture set to: {0}", fuelMoisture.toString());
        model.setFuelMoisture(fuelMoisture);
        updateFireBehavior();
        updateViews();
    }

    public void setAirTemperature(Real airTemp) {
        this.airTemp = airTemp;
        this.fuelTemp = null;   // reset 
        updateWeather();
        updateFireBehavior();
        updateViews();
    }

    public void setRelativeHumidity(Real relHumidity) {
        this.relHumidity = relHumidity;
        updateWeather();
        updateFireBehavior();
        updateViews();
    }

    public void setWindDir(Real windDir) {
        this.windDir = windDir;
        updateWeather();
        updateFireBehavior();
        updateViews();
    }

    public void setWindSpeed(Real windSpd) {
        this.windSpd = windSpd;
        this.fuelTemp = null;   // reset 
        updateWeather();
        updateFireBehavior();
        updateViews();
    }

    public void setSkyCover(Real skyCover) {
        this.skyCover = skyCover;
        this.fuelTemp = null;   // reset 
        updateWeather();
        updateFireBehavior();
        updateViews();
    }

    public void setFuelTemperature(Real fuelTemp) {
        this.fuelTemp = fuelTemp;
        updateFireBehavior();
        updateViews();
    }

    void updateSpatialDomain(Coord3D coord) {
        spatialDomain = SpatialDomain.from(coord);
        SpatioTemporalDomain domain = new SpatioTemporalDomain(temporalDomain, spatialDomain);
        model.setCoord(coord);
        model.setDomain(domain);
    }

    void updateTemporalDomain(ZonedDateTime time) {
        temporalDomain = new TemporalDomain(time.minusHours(24), 25);
        SpatioTemporalDomain domain = new SpatioTemporalDomain(temporalDomain, spatialDomain);
        model.setDateTime(time);
        model.setDomain(domain);

    }

    /**
     * Updates the solar angles and sun position using the current coordinate and time.
     */
    void updateSunlight() {
        BasicSunlight sunlight = sun.getSunlight(model.getDateTime(), model.getCoord());
        if (sunlight.equals(BasicSunlight.INVALID)) {
            
            return;
        }
        model.setSunlight(sunlight);
    }

    void updateTerrain() {
        Terrain terrain = earth.getTerrain(model.getCoord());
        model.setTerrain(terrain);
    }

    /**
     * Updates the terrain shading at the current coordinate.
     */
    void updateTerrainShading() {
        Sunlight sunlight = model.getSunlight();
        Real azimuth = sunlight.getAzimuthAngle();
        Real zenith = sunlight.getZenithAngle();
        GeoCoord2D subsolarPoint = GeoCoord2D.fromReals(sunlight.getSubsolarLatitude(), sunlight.getSubsolarLongitude());
        boolean isShaded
                = terrainShadingEnabled //&& !(azimuth.isMissing() || zenith.isMissing())
                        ? earth.isCoordinateTerrestialShaded(model.getCoord(), subsolarPoint)
                        //? earth.isCoordinateTerrestialShaded(coord, azimuth, zenith)
                        : false;
        model.setShaded(isShaded);
    }

    /**
     * Updates the weather using the current coordinate and time.
     */
    void updateWeather() {
        Weather wx = WeatherManager.getInstance().getWeatherAt(model.getCoord(), model.getDateTime());
        model.setWeather(applyWeatherOverrides(wx));

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
    }

    Weather applyWeatherOverrides(Weather weather) {

        // Copy the weather into a mutable weather provider 
        simpleWeather.setWeather(weather);

        // Apply wx overrides
        if (airTemp != null && !airTemp.isMissing()) {
            simpleWeather.setAirTemperature(airTemp);
        }
        if (relHumidity != null && !relHumidity.isMissing()) {
            simpleWeather.setRelativeHumdity(relHumidity);
        }
        if (windDir != null) {
            simpleWeather.setWindDirection(windDir);
        }
        if (windSpd != null) {
            simpleWeather.setWindSpeed(windSpd);
        }
        if (skyCover != null) {
            simpleWeather.setCloudCover(skyCover);
        }
        return simpleWeather.getWeather();
    }

    /**
     * Reset the weather overrides.
     */
    void clearWeatherOverrides() {
        airTemp = null;
        relHumidity = null;
        windDir = null;
        windSpd = null;
        skyCover = null;
    }

    /**
     * Updates the fuel model data at the current coordinate.
     */
    void updateFuelModel() {
        FuelModel fuelModel = fuels != null
                ? fuels.getFuelModel(model.getCoord())
                : BasicFuelModel.INVALID;
        model.setFuelModel(fuelModel);

    }

    /**
     * Updates the fire behavior using the current weather.
     */
    void updateFireBehavior() {
        model.conditionFuelbed();
        if (fuelTemp != null && !fuelTemp.isMissing()) {
            model.modifyFuelbed(fuelTemp);
        }
        model.computeFireBehavior();
    }

    /**
     * Updates the View in the Model-View-Controller relationship.
     */
    void updateViews() {
        model.publishUpdates();
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

        /**
         * Queues a request to update the model based the current coordinate.
         */
        public void update() {
            // Sliding task: coallese the update events into fixed intervals
            if (this.updatingTask.isFinished()) {
                this.updatingTask.schedule(UPDATE_INTERVAL_MS);
            }
        }

        /**
         * ReticuleCoordinateListener event handler.
         *
         * @param evt Contains the coordinate under the crosshairs.
         */
        @Override
        public void updateCoordinate(ReticuleCoordinateEvent evt) {
            this.lastEvent.set(evt);
            update();
        }

        /**
         * Runnable for the RequestProcessor.Task that updates the model based on the current
         * coordinate.
         */
        @Override
        @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
        public void run() {
            try {
                // Examine the last ReticuleCoordinateEvent event for a new coordinate.
                ReticuleCoordinateEvent event = this.lastEvent.get();
                if (event == null || controller.earth == null) {
                    return;
                }

                Coord3D coord = event.getCoordinate();
                if (coord.isMissing()) {
                    return;
                }

                controller.updateSpatialDomain(coord);
                controller.updateTerrain();
                controller.updateTerrainShading();

                // Must provide the weather provider with the current coord before updating weather model
                WeatherManager.getInstance().updateSpatialDomain(coord);
                controller.updateWeather();

                // Must reset the fuel temperature override whenever the coordinate changes
                controller.fuelTemp = null;   // reset fuel temp.
                controller.updateFuelModel();
                controller.updateFireBehavior();

                // Update the GUI 
                controller.updateViews();

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

        /**
         * TimeListener event handler that updates the model when the time changes.
         *
         * @param evt Contains the application date/time.
         */
        @Override
        public void updateTime(TimeEvent evt) {
            this.lastTimeEvent.set(evt);
            if (this.updatingTask.isFinished()) {
                this.updatingTask.run();
            }
        }

        /**
         * Runnable for the RequestProcessor.Task that updates the model from the current time.
         */
        @Override
        @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
        public void run() {

            // 
            TimeEvent timeEvent = this.lastTimeEvent.get();
            if (timeEvent == null) {
                return;
            }
            ZonedDateTime time = timeEvent.getNewTime();
            controller.updateTemporalDomain(time);

            try {
                // Update solar angles and position
                controller.updateSunlight();
                controller.updateTerrainShading();

                // Must update the weather providers with the time before updating the Weather
                WeatherManager.getInstance().updateTime(time);
                controller.clearWeatherOverrides();  // Cancel the wx overrides
                controller.updateWeather(); // Update the model

                // Update the fuelbed and fire behavior
                controller.updateFireBehavior();

                // Update the GUI 
                controller.updateViews();

            } catch (Exception ex) {
                logger.log(Level.SEVERE, "TimeUpdater failed.", ex);
                Exceptions.printStackTrace(ex);
            }

        }
    }

}
