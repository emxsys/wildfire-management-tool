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

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.BasicTerrain;
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.FireBehaviorProvider;
import com.emxsys.wildfire.api.BasicFireBehavior;
import com.emxsys.wildfire.api.FireEnvironment;
import com.emxsys.wildfire.api.FuelCondition;
import com.emxsys.wildfire.api.BasicFuelCondition;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelMoisture;
import static com.emxsys.wildfire.api.WildfireType.*;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import visad.Data;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class FireBehaviorModel {

    /**
     * The time/space domain
     */
    private final SpatioTemporalDomain domain;
    /**
     * The geography
     */
    private final TerrainModel terrain;
    /**
     * The fuel models
     */
    private final FuelTypeModel fuelTypes;
    /**
     * The hourly fuel moisture
     */
    private final FuelTemperatureModel fuelTemps;
    /**
     * The hourly fuel moisture
     */
    private final FuelMoistureModel moistures;
    /**
     * The hourly weather
     */
    private final WeatherModel weather;
    /**
     * The fire behavior calculator
     */
    private final FireBehaviorProvider fireBehaviorService;
    /**
     * The fire behavior range tuple type, <br/>
     * {FIRE_LINE_INTENSITY_SI, FLAME_LENGTH_SI, RATE_OF_SPREAD_SI, DIR_OF_SPREAD, HEAT_RELEASE_SI}
     */
    private static final RealTupleType behaviorType = FIRE_BEHAVIOR;
    /**
     * The hourly time type
     */
    private static final RealType timeType = RealType.getRealTypeByName("Time");
    /**
     * The fire weather data
     */
    private FieldImpl hourlyMaxBehavior;
    private FieldImpl hourlyMinBehavior;
    /**
     * Error logger
     */
    private static final Logger LOG = Logger.getLogger(FireBehaviorModel.class.getName());

    /**
     * Constructs the FireBehaviorModel with a deferred initialization of the hourly fire behavior
     * data.
     *
     */
    public FireBehaviorModel(SpatioTemporalDomain domain,
                             TerrainModel terrain,
                             FuelTypeModel fuel,
                             FuelTemperatureModel temperature,
                             FuelMoistureModel moisture,
                             WeatherModel weather) {
        // Defer initialization of Weather flatfield member
        this(domain, terrain, fuel, temperature, moisture, weather, false);
    }

    public FireBehaviorModel(SpatioTemporalDomain domain,
                             TerrainModel terrain,
                             FuelTypeModel fuel,
                             FuelTemperatureModel temperature,
                             FuelMoistureModel moisture,
                             WeatherModel weather,
                             boolean immediate) {
        this.domain = domain;
        this.terrain = terrain;
        this.fuelTypes = fuel;
        this.fuelTemps = temperature;
        this.moistures = moisture;
        this.weather = weather;

        // Find the Service Provider used to compute the behaviors
        this.fireBehaviorService = Lookup.getDefault().lookup(FireBehaviorProvider.class);

        if (immediate) {
            getMaxFireBehavorData();
        }

    }

    public FireBehaviorModel(FieldImpl fireBehaviorMax, FieldImpl fireBehaviorMin) {
        this.terrain = null;
        this.fuelTypes = null;
        this.fuelTemps = null;
        this.moistures = null;
        this.weather = null;
        this.fireBehaviorService = null;

        // XXX THIS DOESN'T WORK! Linear1DSet not compatible with Gridded1DDoubleSet. ARGH!!
        // Extract temporal spatial domains from the fire behavior data
        //  this.domain = new SpatioTemporalDomain(fireBehaviorMax);
        this.domain = null;

        this.hourlyMaxBehavior = fireBehaviorMax;
        this.hourlyMinBehavior = fireBehaviorMin;

    }

    public SpatioTemporalDomain getDomain() {
        return domain;
    }

    public TerrainModel getTerrain() {
        return terrain;
    }

    public BasicFireBehavior getMaxFireBehavior(DateTime temporal, Coord2D spatial) {
        if (this.hourlyMaxBehavior == null) {
            this.hourlyMaxBehavior = createFireBehavior();
        }
        return getFireBehavior(this.hourlyMaxBehavior, temporal, spatial);
    }

    public BasicFireBehavior getMinFireBehavior(DateTime temporal, Coord2D spatial) {
        if (this.hourlyMinBehavior == null) {
            this.hourlyMinBehavior = createFireBehavior();
        }
        return getFireBehavior(this.hourlyMinBehavior, temporal, spatial);
    }

    private static BasicFireBehavior getFireBehavior(FieldImpl hourlyBehavior, DateTime temporal,
                                                     Coord2D spatial) {
        try {
            RealTuple location = new RealTuple(RealTupleType.LatitudeLongitudeTuple, new double[]{
                spatial.getLatitudeDegrees(), spatial.getLongitudeDegrees()
            });
            FieldImpl field = (FieldImpl) hourlyBehavior.evaluate(temporal, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            RealTuple tuple = (RealTuple) field.evaluate(location, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            return tuple.isMissing() ? BasicFireBehavior.INVALID_TUPLE : new BasicFireBehavior(tuple.getRealComponents());
        } catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    public BasicFireBehavior getMaxFireBehaviorAt(int temporalIndex, int spatialIndex) {
        if (this.hourlyMaxBehavior == null) {
            this.hourlyMaxBehavior = createFireBehavior();
        }
        return getFireBehaviorAt(this.hourlyMaxBehavior, temporalIndex, spatialIndex);
    }

    public BasicFireBehavior getMinFireBehaviorAt(int temporalIndex, int spatialIndex) {
        if (this.hourlyMinBehavior == null) {
            this.hourlyMinBehavior = createFireBehavior();
        }
        return getFireBehaviorAt(this.hourlyMinBehavior, temporalIndex, spatialIndex);
    }

    private static BasicFireBehavior getFireBehaviorAt(FieldImpl hourlyBehavior, int temporalIndex,
                                                       int spatialIndex) {
        try {
            FieldImpl field = (FieldImpl) hourlyBehavior.getSample(temporalIndex);
            RealTuple sample = (RealTuple) field.getSample(spatialIndex);
            return new BasicFireBehavior(sample.getRealComponents());
        } catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Math type:<br/>
     * ( time -> ( (latitude, longitude) -> ( temperature, humidity, ... ) ) )
     *
     * @return hourly weather
     */
    public final FieldImpl getMaxFireBehavorData() {
        if (this.hourlyMaxBehavior == null) {
            this.hourlyMaxBehavior = createFireBehavior();
        }
        return this.hourlyMaxBehavior;
    }

    /**
     * Math type:<br/>
     * ( time -> ( (latitude, longitude) -> ( temperature, humidity, ... ) ) )
     *
     * @return hourly weather
     */
    public final FieldImpl getMinFireBehavorData() {
        if (this.hourlyMaxBehavior == null) {
            this.hourlyMaxBehavior = createFireBehavior();
        }
        return this.hourlyMinBehavior;
    }

    private FieldImpl createFireBehavior() {
        try {
            if (this.fireBehaviorService == null) {
                throw new IllegalStateException("A FireBehaviorService wasn't found.  "
                        + "Ensure the module providing the surface fire behavior is installed.");
            }
            FlatField maxBehaviorFlatField = this.domain.newSpatialField(behaviorType);
            FlatField minBehaviorFlatField = this.domain.newSpatialField(behaviorType);
            FieldImpl hourlyMaxBehaviorField = this.domain.newTemporalField(maxBehaviorFlatField.getType());
            FieldImpl hourlyMinBehaviorField = this.domain.newTemporalField(minBehaviorFlatField.getType());

            final int numLatLons = this.domain.getSpatialDomainSet().getLength();
            final int numTimes = this.domain.getTemporalDomainSet().getLength();

            // Create the fire behavior ouput range(s) based on the spatial domain samples
            double[][] behaviorMaxSamples = new double[behaviorType.getDimension()][numLatLons];
            double[][] behaviorMinSamples = new double[behaviorType.getDimension()][numLatLons];

            // Loop through the time domain
            for (int t = 0; t < numTimes; t++) {
                // Get the general wx at this time
                Weather genWx = this.weather.getWeatherAt(t);
                System.out.println("General Wx: " + genWx.toString());

                // ... and the lat/lon domain
                for (int xy = 0; xy < numLatLons; xy++) {
                    // We know that all the spatial data sets ar coincident 
                    // with the spatial domain, so we can use the same index 
                    // to retrieve the value (versus calling "evaluate" with a lat/lon)

                    FuelModel fuelModel = this.fuelTypes.getFuelModelAt(xy);
                    RealTuple fuelTemp = this.fuelTemps.getFuelTemperatureAt(t, xy);
                    FuelMoisture fuelMoisture = this.moistures.getFuelMoistureAt(t, xy);
                    BasicTerrain terrainTuple = this.terrain.getTerrainSample(xy);

                    // Create the fire behavior input parameters
                    FuelCondition fuelCondition = BasicFuelCondition.fromReals(
                            fuelMoisture, fuelTemp.getRealComponents()[0]);

                    FireEnvironment fire = fireBehaviorService.computeFireBehavior(
                            fuelModel, fuelCondition,
                            genWx, terrainTuple);

                    RealTuple maxFireBehaviorTuple = (RealTuple) fire.fireBehavior;
                    RealTuple minFireBehaviorTuple = (RealTuple) fire.fireBehaviorNoWnd;
                    for (int dim = 0; dim < behaviorType.getDimension(); dim++) {
                        Real maxComponent = (Real) maxFireBehaviorTuple.getComponent(dim);
                        Real minComponent = (Real) minFireBehaviorTuple.getComponent(dim);
                        behaviorMaxSamples[dim][xy] = maxComponent.getValue();
                        behaviorMinSamples[dim][xy] = minComponent.getValue();
                    }

                }
                maxBehaviorFlatField.setSamples(behaviorMaxSamples);
                minBehaviorFlatField.setSamples(behaviorMinSamples);
                hourlyMaxBehaviorField.setSample(t, maxBehaviorFlatField);
                hourlyMinBehaviorField.setSample(t, minBehaviorFlatField);
            }
            this.hourlyMaxBehavior = hourlyMaxBehaviorField;
            this.hourlyMinBehavior = hourlyMinBehaviorField;

            return hourlyMaxBehaviorField;

        } catch (IllegalStateException | VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
        }
        return null;
    }
}
