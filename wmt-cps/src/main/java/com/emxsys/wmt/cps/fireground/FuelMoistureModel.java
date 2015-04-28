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

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.visad.Times;
import com.emxsys.weather.api.BasicWeather;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.wildfire.api.BasicFuelMoisture;
import com.emxsys.wildfire.api.FuelMoisture;
import static com.emxsys.wildfire.api.WildfireType.*;
import static com.emxsys.wmt.cps.fireground.FuelMoistureUtil.*;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.Data;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Linear2DSet;
import visad.LinearLatLonSet;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class FuelMoistureModel {

    private final SpatioTemporalDomain domain;
    /** The fireground elevations */
    private final TerrainModel terrain;
    /** The fuel models */
    private final FuelTypeModel fuel;
    private final WeatherModel weather;
    private final FuelTemperatureModel fuelTemps;
    /** A fuel moisture scenario for deriving fuel moistures */
    private final FuelMoisture scenario;
    /** The hourly 1 hour fuel moisture data */
    private FieldImpl moisture1h;
    /** The hourly 10 hour fuel moisture data */
    private FieldImpl moisture10h;
    /** The hourly 100 hour fuel moisture data */
    private FieldImpl moisture100h;
    /** The hourly live herbaceous fuel moisture data */
    private FieldImpl moistureHerb;
    /** The hourly live woody fuel moisture data */
    private FieldImpl moistureWoody;
    private static final Logger LOG = Logger.getLogger(FuelMoistureModel.class.getName());

    /**
     * Constructs the FuelMoistureModel with a deferred initialization of the FuelMoisture FlatField
     * member.
     *
     * @param domain the FuelMoisture's domain (geographic bounds)
     */
    public FuelMoistureModel(SpatioTemporalDomain domain, TerrainModel terrain,
                             FuelTypeModel fuel, FuelTemperatureModel temps, WeatherModel weather,
                             FuelMoisture scenario) {
        // Defer initialization of FuelMoisture flatfield member
        this(domain, terrain, fuel, temps, weather, scenario, false);
    }

    /**
     *
     * @param domain the geographic domain (bounds)
     * @param immediate if true, performs an immediate initialization of the FuelMoisture data; if
     * false, the FuelMoisture data is initialized on the first call to getFuelMoistureData.
     */
    public FuelMoistureModel(SpatioTemporalDomain domain, TerrainModel terrain,
                             FuelTypeModel fuel, FuelTemperatureModel temps, WeatherModel weather,
                             FuelMoisture scenario, boolean immediate) {
        this.domain = domain;
        this.terrain = terrain;
        this.fuel = fuel;
        this.weather = weather;
        this.fuelTemps = temps;
        this.scenario = scenario;
        if (immediate) {
            getDead1HrFuelMoistureData();
            getDead10HrFuelMoistureData();
            getDead100HrFuelMoistureData();
            getLiveHerbFuelMoistureData();
            getLiveWoodyFuelMoistureData();
        }

    }

    public FuelMoistureModel(FieldImpl moisture1h, FieldImpl moisture10h, FieldImpl moisture100h,
                             FieldImpl moistureHerb, FieldImpl moistureWoody) {
        this.domain = null;
        this.terrain = null;
        this.fuel = null;
        this.weather = null;
        this.fuelTemps = null;
        this.scenario = null;

        this.moisture1h = moisture1h;
        this.moisture10h = moisture10h;
        this.moisture100h = moisture100h;
        this.moistureHerb = moistureHerb;
        this.moistureWoody = moistureWoody;
    }

    public Real getDead1HrFuelMoisture(Real hour, Coord2D latLon) {
        return evaluateFuelMoisture(getDead1HrFuelMoistureData(), hour, latLon);
    }

    public Real getDead10HrFuelMoisture(Real hour, Coord2D latLon) {
        return evaluateFuelMoisture(getDead10HrFuelMoistureData(), hour, latLon);
    }

    public Real getDead100HrFuelMoisture(Real hour, Coord2D latLon) {
        return evaluateFuelMoisture(getDead100HrFuelMoistureData(), hour, latLon);
    }

    public Real getLiveHerbFuelMoisture(Real hour, Coord2D latLon) {
        return evaluateFuelMoisture(getLiveHerbFuelMoistureData(), hour, latLon);
    }

    public Real getLiveWoodyFuelMoisture(Real hour, Coord2D latLon) {
        return evaluateFuelMoisture(getLiveWoodyFuelMoistureData(), hour, latLon);
    }

    public BasicFuelMoisture getFuelMoisture(Real hour, Coord2D latLon) {
        BasicFuelMoisture fuelMoisture = BasicFuelMoisture.fromReals(
                getDead1HrFuelMoisture(hour, latLon),
                getDead10HrFuelMoisture(hour, latLon),
                getDead100HrFuelMoisture(hour, latLon),
                getLiveHerbFuelMoisture(hour, latLon),
                getLiveWoodyFuelMoisture(hour, latLon));
        return fuelMoisture;
    }

    public BasicFuelMoisture getFuelMoistureAt(int timeIndex, int spatialIndex) {
        try {
            Real dead1Hr = (Real) ((FieldImpl) getDead1HrFuelMoistureData().getSample(timeIndex)).getSample(spatialIndex);
            Real dead10Hr = (Real) ((FieldImpl) getDead10HrFuelMoistureData().getSample(timeIndex)).getSample(0);
            Real dead100Hr = (Real) ((FieldImpl) getDead100HrFuelMoistureData().getSample(timeIndex)).getSample(0);
            Real liveHerb = (Real) ((FieldImpl) getLiveHerbFuelMoistureData().getSample(timeIndex)).getSample(0);
            Real liveWoody = (Real) ((FieldImpl) getLiveWoodyFuelMoistureData().getSample(timeIndex)).getSample(0);

            BasicFuelMoisture fuelMoisture = BasicFuelMoisture.fromReals(dead1Hr, dead10Hr, dead100Hr, liveHerb, liveWoody);
            return fuelMoisture;
        } catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public final FieldImpl getDead1HrFuelMoistureData() {
        if (this.moisture1h == null) {
            this.moisture1h = createFineFuelMoisture(this.scenario.getDead1HrFuelMoisture());
        }
        return this.moisture1h;
    }

    public final FieldImpl getDead10HrFuelMoistureData() {
        if (this.moisture10h == null) {
            this.moisture10h = createFixedFuelMoisture(this.scenario.getDead10HrFuelMoisture());
        }
        return this.moisture10h;
    }

    public final FieldImpl getDead100HrFuelMoistureData() {
        if (this.moisture100h == null) {
            this.moisture100h = createFixedFuelMoisture(this.scenario.getDead100HrFuelMoisture());
        }
        return this.moisture100h;
    }

    public final FieldImpl getLiveHerbFuelMoistureData() {
        if (this.moistureHerb == null) {
            this.moistureHerb = createFixedFuelMoisture(this.scenario.getLiveHerbFuelMoisture());
        }
        return this.moistureHerb;
    }

    public final FieldImpl getLiveWoodyFuelMoistureData() {
        if (this.moistureWoody == null) {
            this.moistureWoody = createFixedFuelMoisture(this.scenario.getLiveWoodyFuelMoisture());
        }
        return this.moistureWoody;
    }

    private Real evaluateFuelMoisture(FieldImpl temporalSpatialData, Real hour, Coord2D latLon) {
        Real value = null;
        try {
            RealTuple latLonTuple = new RealTuple(RealTupleType.LatitudeLongitudeTuple, new double[]{
                latLon.getLatitudeDegrees(), latLon.getLongitudeDegrees()
            });

            FlatField spatialData = (FlatField) temporalSpatialData.evaluate(hour, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            value = (Real) spatialData.evaluate(latLonTuple, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
        } catch (VisADException | RemoteException ex) {
            LOG.warning(ex.toString());
            Exceptions.printStackTrace(ex);
        }
        return value;
    }

    /**
     * Compute the fine fuel moisture which varies by time and location: <br/>
     * (time -> ((latitude, longitude) -> (fuel moisture))
     *
     * @param initialFuelMoisture
     * @return hourly fuel moistures
     *
     * @param initialFuelMoisture
     * @return
     */
    private FieldImpl createFineFuelMoisture(Real initialFuelMoisture) {
        try {
            FlatField moistureFlatField = this.domain.newSpatialField(FUEL_MOISTURE_1H);
            FieldImpl hourlyMoistureField = this.domain.newTemporalField(moistureFlatField.getType());

            final int numLatLons = this.domain.getSpatialDomainSetLength();
            final int numTimes = this.domain.getTemporalDomainSet().getLength();
            float[][] values = new float[1][numLatLons];
            float[] m_1400 = new float[numLatLons];
            float[] m_1200 = new float[numLatLons];
            float[] prev_m = new float[numLatLons];

            // Update the the field samples: loop through the time domain
            for (int t = 0; t < numTimes; t++) {
                DateTime dateTime = this.domain.getDateTimeAt(t);
                double local24HourTime = Times.toClockTime(dateTime);

                // Set the fuel moisture sample(s) in the terrain's lat/lon domain
                for (int xy = 0; xy < numLatLons; xy++) {
                    BasicWeather genWx = weather.getWeatherAt(t);
                    RealTuple fuelCond = fuelTemps.getFuelTemperatureAt(t, xy);

                    Real T_f = fuelCond.getRealComponents()[0]; // Temp adjacent fuel
                    Real H_f = fuelCond.getRealComponents()[1]; // Humidity adjacent fuel
                    Real W = genWx.getWindSpeed();              // 20 ft wind speed
                    Real R = new Real(WeatherType.RAINFALL_INCH, 0.0);// Rainfall [inches]

                    // Get the previous hour's value fuel moisture
                    Real m_0 = (t > 0) ? new Real(FUEL_MOISTURE_1H, prev_m[xy]) : initialFuelMoisture;
                    Real m_14 = (m_1400[xy] > 0f) ? new Real(FUEL_MOISTURE_1H, m_1400[xy]) : initialFuelMoisture;

                    // Noontime weather is used to compute 1400 fuel moisture; 
                    // it will be used in a subsequent iteration in the loop.
                    if (local24HourTime >= 11.5 && local24HourTime < 12.5) {
                        Real m = calcCanadianStandardDailyFineFuelMoisture(m_0, T_f, H_f, W, R);
                        m_1400[xy] = (float) m.getValue();
                    }

                    // Compute the hourly fine fuel moisture...
                    float m;

                    // At 1300 intepolate between noon and 1400
                    if (local24HourTime >= 12.5 && local24HourTime < 13.5) {
                        m = (float) (m_0.getValue() + m_14.getValue()) / 2.0f;
                    } // At 1400 use the fuel moisture that was computed at 1200 (see above)
                    else if (local24HourTime >= 13.5 && local24HourTime < 14.5) {
                        m = (float) m_14.getValue();
                    } // Otherwise, compute fine fuel moisture for this hour
                    else {
                        m = (float) calcCanadianHourlyFineFuelMoisture(m_0, T_f, H_f, W).getValue();
                    }
                    values[0][xy] = m;
                    prev_m[xy] = m;

                }

                // Add our samples to the fuel moisture FlatField
                moistureFlatField.setSamples(values);

                System.out.println("Hour = " + t + ", Tuple[0] = " + moistureFlatField.getSample(0).toString());

                // ... and then set the sample in the hourly field
                hourlyMoistureField.setSample(t, moistureFlatField);
            }
            return hourlyMoistureField;
        } catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
        }
        return null;
    }

    /**
     * Create a non-variable fuel moisture organized as: <br/>
     * (time -> ((latitude, longitude) -> (fuel moisture))
     *
     * @param fuelMoisture
     * @return hourly fuel moistures
     */
    private FieldImpl createFixedFuelMoisture(Real fuelMoisture) {
        try {
            // Instead of using the spatial domain's set, we'll define 
            // a very simple domain with just one sample
            Box sector = this.domain.getSector();
            int numSamples = 1;
            Linear2DSet fixedDomainSet = new LinearLatLonSet(
                    this.domain.getSpatialDomainType(),
                    sector.getSouthwest().getLatitudeDegrees(), sector.getNortheast().getLatitudeDegrees(), numSamples,
                    sector.getSouthwest().getLongitudeDegrees(), sector.getNortheast().getLongitudeDegrees(), numSamples);

            // Create the simplified FlatField for fuel moisture
            FunctionType functionType = new FunctionType(this.domain.getSpatialDomainType(), fuelMoisture.getType());
            FlatField moistureFlatField = new FlatField(functionType, fixedDomainSet);

            // ... and a non-flat FieldImpl for hourly moisture return value
            //FieldImpl hourlyFieldImpl = new FieldImpl(hourlyFunctionType, timeSet);
            FieldImpl hourlyFieldImpl = this.domain.newTemporalField(functionType);

            // Create the output range: just one row for with one value
            double[][] moistureSample
                    = {
                        {
                            fuelMoisture.getValue()
                        }
                    };

            // Update the the field samples: Loop through the time domain
            int tSamples = this.domain.getTemporalDomainSet().getLength();
            for (int t = 0; t < tSamples; t++) {
                // Set the fuel moisture sample(s) in the lat/lon domain ...
                moistureFlatField.setSamples(moistureSample);

                // ... and then set the sample in the hourly domain
                hourlyFieldImpl.setSample(t, moistureFlatField);
            }
            return hourlyFieldImpl;

        } catch (VisADException | RemoteException ex) {
            LOG.severe(ex.toString());
        }
        return null;
    }
}
