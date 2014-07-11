/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.cps;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.Terrain;
import com.emxsys.gis.api.TerrainTuple;
import com.emxsys.solar.api.SolarModel;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.solar.api.SunlightTuple;
import com.emxsys.visad.SpatioTemporalDomain;
import com.emxsys.weather.api.Weather;
import com.emxsys.weather.api.WeatherTuple;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.FuelMoistureTuple;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wildfire.behavior.FireReaction;
import com.emxsys.wildfire.behavior.Fuelbed;
import com.emxsys.wildfire.behavior.SurfaceFireModel;
import com.emxsys.wmt.cps.layers.FireShape;
import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.VisADException;

/**
 * CPS data model for a point within a temporal-spatial domain.
 *
 * @author Bruce Schubert
 */
public class Model {

    /** Coord3D property */
    public static final String PROP_COORD = "PROP_COORD";
    /** ZonedDateTime property */
    public static final String PROP_DATETIME = "PROP_DATETIME";
    /** SpatioTemporalDomain property */
    public static final String PROP_DOMAIN = "PROP_DOMAIN";
    /** FireReaction property */
    public static final String PROP_FIREBEHAVIOR = "PROP_FIREBEHAVIOR";
    /** Fuelbed property */
    public static final String PROP_FUELBED = "PROP_FUELBED";
    /** FuelModel property */
    public static final String PROP_FUELMODEL = "PROP_FUELMODEL";
    /** FuelMoisture property */
    public static final String PROP_FUELMOISTURE = "PROP_FUELMOISTURE";
    /** Model property */
    public static final String PROP_MODEL = "PROP_MODEL";
    /** Sunlight property */
    public static final String PROP_SUNLIGHT = "PROP_SUNLIGHT";
    /** Terrain property */
    public static final String PROP_TERRAIN = "PROP_TERRAIN";
    /** Weather property */
    public static final String PROP_WEATHER = "PROP_WEATHER";

    private static final Logger logger = Logger.getLogger(Model.class.getName());

    /**
     * @return The Model singleton.
     */
    public static Model getInstance() {
        return ModelHolder.INSTANCE;
    }

    private FireShape fireShape;    // Deferred initialization
    private final SurfaceFireModel fireModel = new SurfaceFireModel();

    // Current data values
    private final AtomicReference<SpatioTemporalDomain> domainRef = new AtomicReference<>();
    private final AtomicReference<ZonedDateTime> timeRef = new AtomicReference<>(ZonedDateTime.now(ZoneId.of("UTC")));
    private final AtomicReference<Coord3D> coordRef = new AtomicReference<>(GeoCoord3D.INVALID_COORD);
    private final AtomicReference<Terrain> terrainRef = new AtomicReference<>(TerrainTuple.INVALID_TERRAIN);
    private final AtomicReference<FuelModel> fuelModelRef = new AtomicReference<>(StdFuelModel.INVALID);
    private final AtomicReference<FuelMoisture> fuelMoistureRef = new AtomicReference<>(FuelMoistureTuple.INVALID);
    private final AtomicReference<Sunlight> sunlightRef = new AtomicReference<>(SunlightTuple.INVALID_TUPLE);
    private final AtomicReference<Weather> weatherRef = new AtomicReference<>(WeatherTuple.INVALID_TUPLE);
    private final AtomicReference<Fuelbed> fuelbedRef = new AtomicReference<>();
    private final AtomicReference<FireReaction> fireBehaviorRef = new AtomicReference<>();

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final BitSet dirtyFlags = new BitSet(Flag.values().length);

    /** Hidden constructor. */
    private Model() {
    }

    /**
     *
     * @return A SpatioTemporalDomain representing the model's domain.
     */
    public SpatioTemporalDomain getDomain() {
        return domainRef.get();
    }

    void setDomain(SpatioTemporalDomain domain) {
        domainRef.set(domain);
        synchronized (dirtyFlags) {
            dirtyFlags.set(Flag.Domain.ordinal());
        }
    }

    /**
     * The current date/time.
     *
     * @return A ZonedDateTime within the model's domain
     */
    public ZonedDateTime getDateTime() {
        return timeRef.get();
    }

    void setDateTime(ZonedDateTime datetime) {
        timeRef.set(datetime);
        synchronized (dirtyFlags) {
            dirtyFlags.set(Flag.Time.ordinal());
        }
    }

    /**
     * The current coordinate.
     *
     * @return A Coord3D within the model's domain.
     */
    public Coord3D getCoord() {
        return coordRef.get();
    }

    void setCoord(Coord3D coord) {
        coordRef.set(coord);
        synchronized (dirtyFlags) {
            dirtyFlags.set(Flag.Coord.ordinal());
        }
    }

    /**
     * The aspect and slope at the current coordinate.
     *
     * @return The Terrain at the current coordinate.
     */
    public Terrain getTerrain() {
        return terrainRef.get();
    }

    void setTerrain(Terrain terrain) {
        terrainRef.set(terrain);
        synchronized (dirtyFlags) {
            dirtyFlags.set(Flag.Terrain.ordinal());
        }
    }

    /**
     * The fuel model at the current coordinate.
     *
     * @return A FuelModel for the current coordinate.
     */
    public FuelModel getFuelModel() {
        return fuelModelRef.get();
    }

    void setFuelModel(FuelModel fuelModel) {
        fuelModelRef.set(fuelModel);
        synchronized (dirtyFlags) {
            dirtyFlags.set(Flag.FuelModel.ordinal());
        }
    }

    /**
     * The conditioned fuel for the current coordinate and time.
     *
     * @return A Fuelbed for the current coordinate and time.
     */
    public Fuelbed getFuelbed() {
        return fuelbedRef.get();
    }

    /**
     * The fire behavior for the current coordinate and time.
     *
     * @return A FireReaction for the current coordinate and time.
     */
    public FireReaction getFireBehavior() {
        return fireBehaviorRef.get();
    }

    /**
     * The fuel moisture for the current coordinate and time.
     *
     * @return A FuelMoisture for the current coordinate and time.
     */
    public FuelMoisture getFuelMoisture() {
        return fuelMoistureRef.get();
    }

    void setFuelMoisture(FuelMoisture fuelMoisture) {
        fuelMoistureRef.set(fuelMoisture);
        synchronized (dirtyFlags) {
            dirtyFlags.set(Flag.FuelMoisture.ordinal());
        }
    }

    /**
     * The sunlight for the current coordinate and time.
     *
     * @return A Sunlight for the current coordinate and time.
     */
    public Sunlight getSunlight() {
        return sunlightRef.get();
    }

    void setSunlight(Sunlight sunlight) {
        sunlightRef.set(sunlight);
        synchronized (dirtyFlags) {
            dirtyFlags.set(Flag.Sunlight.ordinal());
        }
    }

    /**
     * The weather for the current coordinate and time.
     *
     * @return A Weather for the current coordinate and time.
     */
    public Weather getWeather() {
        return weatherRef.get();
    }

    void setWeather(Weather weather) {
        weatherRef.set(weather);
        synchronized (dirtyFlags) {
            dirtyFlags.set(Flag.Weather.ordinal());
        }
    }

    /**
     * Computes the fire behavior for the current coordinate.
     *
     * @throws VisADException
     * @throws RemoteException
     */
    public void computeFireBehavior() {
        if (!validateInputs()) {
            return;
        }
        try {
            // Use the previous 24 hours of sunlight and the current location for fuel conditioning
            ZonedDateTime time = timeRef.get();
            Coord3D coord = coordRef.get();
            Terrain terrain = terrainRef.get();
            SolarModel solarModel = new SolarModel(domainRef.get(), true);

            // Condition the fuel to the current weather and solar conditions.
//            Fuel fuel = fuelProvider.newFuel(fuelModelRef.get());
//            fuel.condition(time, coord, solarModel, weatherModel, terrain, fuelMoistureRef.get());
            // Compute the fire Behavior
//            FireEnvironment fire = fireProvider.getFireReaction(
//                    fuel.getFuelModel(),
//                    fuel.getFuelCondition(time),
//                    wx, terrain);
            Fuelbed fuelbed = fireModel.getFuelbed(fuelModelRef.get(), fuelMoistureRef.get());
            FireReaction fire = fireModel.getFireBehavior(fuelbed, weatherRef.get(), terrain);

            fuelbedRef.set(fuelbed);
            fireBehaviorRef.set(fire);
            synchronized (dirtyFlags) {
                dirtyFlags.set(Flag.Fuelbed.ordinal());
                dirtyFlags.set(Flag.FireBehavior.ordinal());
            }
            if (fireShape == null) {
                fireShape = new FireShape();
            }
            fireShape.update(coord, fire, Duration.ofMinutes(5));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "computeFireBehavior failed.", e);
            Exceptions.printStackTrace(e);
        }
    }

    private boolean validateInputs() {
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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(property, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(property, listener);
    }

    public void updateViews() {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Clone and reset the dirty flags (Note: a BitSet doesn't support multithreading)
                BitSet flags;
                synchronized (dirtyFlags) {
                    if (dirtyFlags.cardinality() > 0) {
                        flags = (BitSet) dirtyFlags.clone();
                        dirtyFlags.clear();
                    } else {
                        return;
                    }
                }
                // Notify property listeners that listener on the entire "model"
                pcs.firePropertyChange(PROP_MODEL, null, Model.this);

                // Notify individual property listeners
                for (int i = 0; i < flags.length(); i++) {
                    if (flags.get(i)) {
                        Flag flag = Flag.values()[i];
                        switch (flag) {
                            case Domain:
                                pcs.firePropertyChange(PROP_DOMAIN, null, domainRef.get());
                                break;
                            case Time:
                                pcs.firePropertyChange(PROP_DATETIME, null, timeRef.get());
                                break;
                            case Coord:
                                pcs.firePropertyChange(PROP_COORD, null, coordRef.get());
                                break;
                            case Terrain:
                                pcs.firePropertyChange(PROP_TERRAIN, null, terrainRef.get());
                                break;
                            case Sunlight:
                                pcs.firePropertyChange(PROP_SUNLIGHT, null, sunlightRef.get());
                                break;
                            case Weather:
                                pcs.firePropertyChange(PROP_WEATHER, null, weatherRef.get());
                                break;
                            case FuelModel:
                                pcs.firePropertyChange(PROP_FUELMODEL, null, fuelModelRef.get());
                                break;
                            case FuelMoisture:
                                pcs.firePropertyChange(PROP_FUELMOISTURE, null, fuelMoistureRef.get());
                                break;
                            case Fuelbed:
                                pcs.firePropertyChange(PROP_FUELBED, null, fuelbedRef.get());
                                break;
                            case FireBehavior:
                                pcs.firePropertyChange(PROP_FIREBEHAVIOR, null, fireBehaviorRef.get());
                                break;
                            default:
                                throw new UnsupportedOperationException("Unhandled dirty flag: " + flag);
                        }

                    }
                }
            }
        });

    }

    /**
     * Singleton implementation.
     */
    private static class ModelHolder {

        private static final Model INSTANCE = new Model();
    }

// Note: The ordering of these flags controls the order of the property change notifications
    private enum Flag {

        Domain, Time, Coord, Terrain, Sunlight, Weather, FuelModel, FuelMoisture, Fuelbed, FireBehavior
    }

}
