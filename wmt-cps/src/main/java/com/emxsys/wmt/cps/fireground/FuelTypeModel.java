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

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.GeoCoord2D;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.wildfire.api.FuelModel;
import com.emxsys.wmt.wildfire.api.StdFuelModel;
import com.emxsys.wmt.wildfire.api.FuelModelProvider;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import visad.Data;
import visad.FlatField;
import visad.FunctionType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;


/**
 * This class represents the fuel types within the fireground as StdFuelModel40 code numbers.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class FuelTypeModel
{
    /**
     * The fuel domain
     */
    private final SpatioTemporalDomain domain;
    /**
     * The fuel model data lookup
     */
    private FuelModelProvider fuelModelProvider;
    /**
     * The fuel range tuple type
     */
    private static final RealType rangeType = RealType.Generic;
    /**
     * The fuel model data
     */
    private FlatField fuels;
    /**
     * listen for changes in the existence of fuel model data layers
     */
    Lookup.Result<GisLayer> lookupResultDataProvider;
    private static final Logger logger = Logger.getLogger(FuelTypeModel.class.getName());


    /**
     * Constructs the FiregroundTerrain with a deferred initialization of the terrain FlatField
     * member.
     *
     * @param domain the terrain's domain (geographic bounds)
     */
    public FuelTypeModel(SpatioTemporalDomain domain, FuelModelProvider fuelModelProvider)
    {
        // Defer initialization of terrain flatfield member
        this(domain, fuelModelProvider, false);
    }


    /**
     *
     * @param domain the geographic domain (bounds)
     * @param immediate if true, performs an immediate initialization of the fuel model data; if
     * false, the data is initialized on the first call to getFuelData.
     */
    public FuelTypeModel(SpatioTemporalDomain domain, FuelModelProvider fuelModelProvider,boolean immediate)
    {
        this.domain = domain;
        this.fuelModelProvider = fuelModelProvider;

        if (immediate)
        {
            getFuelData();
        }

    }


    /**
     * Constructs a FuelTypeModel from a FlatField loaded from a disk file.
     *
     * @param fuels loaded from a NetCDF disk file.
     */
    public FuelTypeModel(FlatField fuels)
    {
        this.domain = null;
        this.fuels = fuels;
    }


    public final FlatField getFuelData()
    {
        lazyCreateFuels();
        return this.fuels;
    }


    public final FuelModel getFuelModelAt(int index)
    {
        lazyCreateFuels();
        try
        {
            Real sample = (Real) this.fuels.getSample(index);
            int fuelModelNo = (int) Math.round(sample.getValue());
            return StdFuelModel.getFuelModel(fuelModelNo);
        }
        catch (VisADException | RemoteException ex)
        {
            logger.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }


    public final FuelModel getFuelModel(Coord2D latLon)
    {
        lazyCreateFuels();

        try
        {
            RealTuple latLonTuple = new RealTuple(RealTupleType.LatitudeLongitudeTuple, new double[]
            {
                latLon.getLatitudeDegrees(), latLon.getLongitudeDegrees()
            });
            Real fuel = (Real) this.fuels.evaluate(latLonTuple, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            int fuelModelNo = (int) Math.round(fuel.getValue());

            return StdFuelModel.getFuelModel(fuelModelNo);
        }
        catch (VisADException | RemoteException ex)
        {
            logger.severe(ex.toString());
            throw new RuntimeException(ex);
        }
    }


    private void lazyCreateFuels()
    {
        if (this.fuels == null)
        {
            this.fuels = createFuels();
        }
    }


    private FlatField createFuels()
    {
        try
        {
            // Init the Terrain FlatField using the FunctionType (domain_tuple -> range_tuple)
            FunctionType functionType = new FunctionType(this.domain.getSpatialDomainType(), rangeType);
            FlatField flatField = new FlatField(functionType, this.domain.getSpatialDomainSet());

            // Get a copy of the domain samples (an array of lat/lon points) 
            float[][] spatialValues = this.domain.getSpatialDomainSet().getSamples(true); // true = copy
            int nrows = this.domain.getNumRows();
            int ncols = this.domain.getNumColumns();

            // Create the output range: a fuel model number for each domain sample
            double[][] rangeSamples = new double[1][ncols * nrows];
            for (int col = 0; col < ncols; col++)
            {
                for (int row = 0; row < nrows; row++)
                {
                    final int i = (col * nrows) + row;

                    // Get a lat/lon from the domain ...                    
                    GeoCoord2D latLon = GeoCoord2D.fromDegrees(spatialValues[0][i], spatialValues[1][i]);
                    FuelModel fuelModel = this.fuelModelProvider.getFuelModel(latLon);
                    if (fuelModel==null)
                    {
                        fuelModel = StdFuelModel.INVALID;
                        logger.log(Level.WARNING, "createFuels : No FuelModel for {0}, using INVALID.", latLon.toString());                        
                    }
                    // Assign a fuel model number
                    rangeSamples[0][i] = fuelModel.getModelNo();

                }
            }
            // Add our samples to the terrain FlatField
            flatField.setSamples(rangeSamples);

            return flatField;

        }
        catch (VisADException | IllegalStateException | RemoteException ex)
        {
            logger.severe(ex.toString());
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
