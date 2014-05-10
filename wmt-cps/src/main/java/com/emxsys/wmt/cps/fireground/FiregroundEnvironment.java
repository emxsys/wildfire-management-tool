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

import com.emxsys.wmt.wildfire.api.WildfireType;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import visad.FlatField;
import visad.FunctionType;
import visad.RealTupleType;
import visad.VisADException;


/**
 * The fireground environment represents the conditions surrounding the fuels. This class is an
 * aggregate container for the fuel, terrain and weather conditions.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class FiregroundEnvironment
{
    /**
     * The geographic and temporal domains
     */
    private final SpatioTemporalDomain domain;
    /**
     * The geography
     */
    private final TerrainModel terrain;
    /**
     * The weather
     */
    private final WeatherModel weather;
    /**
     * The fuel
     */
    private final FuelTypeModel fuel;
    /**
     * The FuelMoisture range tuple type,
     */
    private static final RealTupleType rangeType = WildfireType.FUEL_MOISTURE;
    /**
     * The FuelMoisture data
     */
    private FlatField moistures;
    private static final Logger LOG = Logger.getLogger(FiregroundEnvironment.class.getName());


    /**
     * Constructs the FiregroundEnvironment with a deferred initialization of the data members.
     *
     * @param domain the FuelMoisture's domain (geographic bounds)
     */
    public FiregroundEnvironment(SpatioTemporalDomain domain, TerrainModel terrain,
        FuelTypeModel fuel)
    {
        // Defer initialization of FuelMoisture flatfield member
        this(domain, terrain, fuel, false);
    }


    /**
     *
     * @param domain the geographic domain (bounds)
     * @param immediate if true, performs an immediate initialization of the FuelMoisture data; if
     * false, the FuelMoisture data is initialized on the first call to getFuelMoisture.
     */
    public FiregroundEnvironment(SpatioTemporalDomain domain, TerrainModel terrain,
        FuelTypeModel fuel, boolean immediate)
    {
        this.domain = domain;
        this.terrain = terrain;
        this.weather = null;
        this.fuel = fuel;

        if (immediate)
        {
            getFuelMoisture();
        }

    }


    public final FlatField getFuelMoisture()
    {
        if (this.moistures == null)
        {
            this.moistures = createFuelMoisture();
        }
        return this.moistures;
    }


    private FlatField createFuelMoisture()
    {
        try
        {
            // Init the FuelMoisture FlatField using the FunctionType (domain_tuple -> range_tuple)
            FunctionType functionType = new FunctionType(this.domain.getSpatialDomainType(), rangeType);
            FlatField flatField = new FlatField(functionType, this.domain.getSpatialDomainSet());
            int length = this.terrain.getTerrainData().getLength();

            // Create the output range: 1 row for each dim in the FuelMoisture tuple (e.g., 1h, 10h, etc...)
            double[][] rangeSamples = new double[rangeType.getDimension()][length];

            // Loop though the terrain and fuel models to compute the fuel moistures
            for (int i = 0; i < length; i++)
            {
                //
                // And finally, update the range samples from the FuelMoisture
//                for (int dim = 0; dim < tuple.getDimension(); dim++)
//                {
//                    rangeSamples[dim][i] = ((Real) tuple.getComponent(dim)).getValue();
//                }
            }
            // Add our samples to the FuelMoisture FlatField
            flatField.setSamples(rangeSamples);

            return flatField;

        }
        catch (VisADException | RemoteException ex)
        {
            LOG.severe(ex.toString());
        }
        return null;
    }
}
