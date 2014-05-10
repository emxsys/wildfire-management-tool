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

import static com.emxsys.wmt.behave.BehaveUtil.*;
import com.emxsys.wmt.gis.api.GeoCoord2D;
import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.Terrain;
import com.emxsys.wmt.gis.api.TerrainTuple;
import com.emxsys.wmt.solar.spi.DefaultSunlightProvider;
import com.emxsys.wmt.visad.GeneralUnit;
import com.emxsys.wmt.visad.Reals;
import com.emxsys.wmt.time.Times;
import com.emxsys.wmt.weather.api.Weather;
import com.emxsys.wmt.weather.api.WeatherTuple;
import com.emxsys.wmt.weather.api.WeatherType;
import com.emxsys.wmt.wildfire.api.FuelModel;
import com.emxsys.wmt.wildfire.api.WildfireType;
import java.rmi.RemoteException;

import java.util.Date;
import java.util.logging.Logger;

import visad.CommonUnit;
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
 * Fuel temperatures are a function of time and location.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class FuelTemperatureModel
{
    private final SpatioTemporalDomain domain;
    private final TerrainModel terrain;
    private final WeatherModel weather;
    private final FuelTypeModel fuels;
    /**
     * Fuel Temperature and RH Near Fuel.
     */
    public static final RealTupleType FUEL_TEMP_TUPLE = Reals.newRealTupleType(
        new RealType[]
    {
        WildfireType.FUEL_TEMP_C, WeatherType.REL_HUMIDITY, WildfireType.ELEVATION
    });
    /**
     * The fuel temperature data.
     */
    private FieldImpl fuelTemps;
    /**
     * Error logger
     */
    private static final Logger LOG = Logger.getLogger(FuelTemperatureModel.class.getName());


    public FuelTemperatureModel(SpatioTemporalDomain domain, TerrainModel terrain,
        FuelTypeModel fuels, WeatherModel weather)
    {
        this(domain, terrain, fuels, weather, false);
    }


    public FuelTemperatureModel(SpatioTemporalDomain domain, TerrainModel terrain,
        FuelTypeModel fuels, WeatherModel weather, boolean immediate)
    {
        if (domain == null)
        {            
            throw new IllegalArgumentException("A null domain argument was passed.");
        }
        if (terrain == null)
        {            
            throw new IllegalArgumentException("A null terrain argument was passed.");
        }
        if (fuels == null)
        {            
            throw new IllegalArgumentException("A null fuels argument was passed.");
        }
        if (weather == null)
        {            
            throw new IllegalArgumentException("A null weather argument was passed.");
        }

        this.domain = domain;
        this.terrain = terrain;
        this.weather = weather;
        this.fuels = fuels;
        if (immediate)
        {
            getFuelTemperatureData();
        }
    }


    /**
     * Constructs a FuelTemperatureModel from data loaded from a NetCDF data file.
     *
     * @param fuelTemps loaded from a disk file
     */
    public FuelTemperatureModel(FieldImpl fuelTemps)
    {
        this.domain = null;
        this.terrain = null;
        this.weather = null;
        this.fuels = null;

        this.fuelTemps = fuelTemps;
    }


    public SpatioTemporalDomain getDomain()
    {
        return domain;
    }


    public TerrainModel getTerrain()
    {
        return terrain;
    }


    public final FieldImpl getFuelTemperatureData()
    {
        if (this.fuelTemps == null)
        {
            this.fuelTemps = createHourlyFuelTemperatures();
        }
        return this.fuelTemps;
    }


    public RealTuple getFuelTemperatureAt(int temporalIndex, int spatialIndex)
    {
        if (this.fuelTemps == null)
        {
            this.fuelTemps = createHourlyFuelTemperatures();
        }
        try
        {
            FieldImpl field = (FieldImpl) this.fuelTemps.getSample(temporalIndex);
            RealTuple sample = (RealTuple) field.getSample(spatialIndex);
            return sample;
        }
        catch (Exception ex)
        {
            LOG.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }


    public RealTuple getFuelTemperature(DateTime temporal, Coord2D latLon)
    {
        if (this.fuelTemps == null)
        {
            this.fuelTemps = createHourlyFuelTemperatures();
        }
        try
        {
            RealTuple latLonTuple = new RealTuple(RealTupleType.LatitudeLongitudeTuple, new double[]
            {
                latLon.getLatitudeDegrees(), latLon.getLongitudeDegrees()
            });

            FieldImpl field = (FieldImpl) this.fuelTemps.evaluate(temporal, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            RealTuple sample = (RealTuple) field.evaluate(latLonTuple, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            return sample;
        }
        catch (VisADException | RemoteException ex)
        {
            LOG.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }


    private FieldImpl createHourlyFuelTemperatures()
    {
        try
        {
            SolarFactory solarFactory = SolarFactory.getInstance();

            // Function Type: ((lat, lon) -> (fuel temp, fuel RH))
            FlatField tempsFlatField = this.domain.newSpatialField(FUEL_TEMP_TUPLE);
            // FunctionType return e.g., (hour -> ((lat, lon) -> (fuel temp, fuel RH)))
            FieldImpl hourlyTempsField = this.domain.newTemporalField(tempsFlatField.getType());

            final int numLatLons = this.domain.getSpatialDomainSetLength();
            final int numTimeValues = this.domain.getTemporalDomainSet().getLength();


            // Get the solar data to be used in the computations; no need to compute for 
            // every lat/lon, just use the first coordinate and date values in our domain
            Date date = this.domain.getStartDate();
            Real latitude = this.domain.getGeoPointAt(0).getLatitude();
            final Solar solar = solarFactory.newSolar(latitude, date);


            // Loop through the temporal-spatial domain and compute the range temperature outputs

            for (int t = 0; t < numTimeValues; t++)
            {

                DateTime dateTime = this.domain.getDateTimeAt(t);
                System.out.println("Computing fuel temps for: " + dateTime.toValueString());

                // Get the general weather at this time
                WeatherTuple wx = this.weather.getWeatherAt(t);

                // Compute constant values used for all geographic calculations
                // Latitude [radians]
                double phi = solar.getLatitude().getValue(CommonUnit.radian);
                // Declination [radialans]
                double delta = solar.getDeclination().getValue(CommonUnit.radian);
                // Local time
                double time = Times.toClockTime(dateTime);

                // TODO: add LocalHourAngle, SolarAltitudeAngle, SolarAzimuthAngle to the solar data tuple
                double h = calcLocalHourAngle(time);
                double A = calcSolarAltitudeAngle(h, phi, delta);
                double Z = calcSolarAzimuthAngle(h, phi, delta, A);



                // Generate geographic temperature samples for this hour
                double[][] rangeSamples = new double[FUEL_TEMP_TUPLE.getDimension()][numLatLons];
                for (int j = 0; j < numLatLons; j++)
                {
                    TerrainTuple terrain = this.terrain.getTerrainSample(j);
                    FuelModel fuel = this.fuels.getFuelModelAt(j);
                    if (fuel==null)
                    {
                        rangeSamples[0][j] = 0;// or missing/NAN for fuelTemp
                        rangeSamples[1][j] = 0;//fuelRH
                        rangeSamples[2][j] = 0;//elevation
                        continue;
                    }
                    RealTuple tuple = computeFuelTempAndRH(A, Z, terrain, fuel, wx);

                    // Populate our range samples, performing any necessary unit convertions
                    Real fuelTemp = (Real) tuple.getComponent(0);
                    Real fuelRH = (Real) tuple.getComponent(1);
                    Real elevation = terrain.getElevation();
                    rangeSamples[0][j] = fuelTemp.getValue(FUEL_TEMP_TUPLE.getDefaultUnits()[0]);
                    rangeSamples[1][j] = fuelRH.getValue(FUEL_TEMP_TUPLE.getDefaultUnits()[1]);
                    rangeSamples[2][j] = elevation.getValue(FUEL_TEMP_TUPLE.getDefaultUnits()[2]);
                }
                // Add the samples to the fields
                tempsFlatField.setSamples(rangeSamples);

                hourlyTempsField.setSample(t, tempsFlatField);
            }

            return hourlyTempsField;
        }
        catch (VisADException | RemoteException ex)
        {
            LOG.severe(ex.toString());
        }
        return null;
    }


    private static RealTuple computeFuelTempAndRH(double A, double Z, Terrain terrain,
        FuelModel fuelModel, Weather weather)
    {
        try
        {
            // Atmospheric transparency
            double p = 0.7;
            // Sky/cloud cover [percent]
            double S_c = weather.getCloudCover().getValue();

            // Elevation [meters]
            double E = terrain.getElevation().getValue(CommonUnit.meter);
            // Slope [radians]
            double slope = terrain.getSlope().getValue(CommonUnit.radian);
            // Aspect [radians]
            double aspect = terrain.getAspect().getValue(CommonUnit.radian);

            // Vegetation height [feet]
            double h_v = fuelModel.getFuelBedDepth().getValue(GeneralUnit.foot);
            // Wind speed [mph]
            double W = weather.getWindSpeed().getValue(GeneralUnit.mph);
            // Air temperature [farenheit]
            double T_a = weather.getAirTemperature().getValue(GeneralUnit.degF);
            // RH [%]
            double H_a = weather.getRelativeHumidity().getValue();

            double M = calcOpticalAirMass(A, E);
            double I_a = calcAttenuatedIrradiance(M, S_c, p);
            double I = calcIrradianceOnASlope(slope, aspect, A, Z, I_a);
            double U_h = calcWindSpeedAtFuelLevel(W, h_v);
            double T_f = calcFuelTemp(I, T_a, U_h);
            double H_f = calcRelativeHumidityNearFuel(H_a, T_f, T_a);

            return new RealTuple(new Real[]
            {
                new Real(WildfireType.FUEL_TEMP_F, T_f),
                new Real(WeatherType.REL_HUMIDITY, H_f)
            });
        }
        catch (Exception ex)
        {
            LOG.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }
}
