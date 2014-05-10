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
import com.emxsys.wmt.gis.api.TerrainTuple;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.gis.spi.DefaultShadedTerrainProvider;
import com.emxsys.wmt.gis.api.viewer.GisViewer;
import com.emxsys.wmt.wildfire.api.WildfireType;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import visad.Data;
import visad.FlatField;
import visad.FunctionType;
import visad.Linear2DSet;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;


/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class TerrainModel
{
    /**
     * The terrain domain
     */
    private final SpatioTemporalDomain domain;
    /**
     * The terrain range tuple type, e.g., slope, aspect and elevation
     */
    private static final RealTupleType rangeType = WildfireType.TERRAIN;
    /**
     * The terrain data
     */
    private FlatField terrain;
    /**
     * The terrain data provider
     */
    private final TerrainFactory terrainFactory;
    /**
     * The Elevation data lookup
     */
    private GisLayer demLayer;
    /**
     * The Aspect data lookup
     */
    private GisLayer aspLayer;
    /**
     * The Slope data lookup
     */
    private GisLayer slpLayer;
    /**
     * Error logging
     */
    private static final Logger LOG = Logger.getLogger(TerrainModel.class.getName());


    /**
     * Constructs the TerrainModel with a deferred initialization of the terrain FlatField member.
     *
     * @param domain the terrain's domain (geographic bounds)
     */
    public TerrainModel(SpatioTemporalDomain domain)
    {
        // Defer initialization of terrain flatfield member
        this(domain, false);
    }


    /**
     *
     * @param domain the geographic domain (bounds)
     * @param immediate if true, performs an immediate initialization of the terrain data; if false,
     * the terrain data is initialized on the first call to getTerrain.
     */
    public TerrainModel(SpatioTemporalDomain domain, boolean immediate)
    {
        this.domain = domain;
        // Find the TerrainProvider Service Provider (possibly from either the WorldWind or LANDFIRE module)
        this.terrainFactory = TerrainFactory.getInstance();//Lookup.getDefault().lookup(TerrainProvider.class);
        checkForDEMLayer();

        if (immediate)
        {
            getTerrainData();
        }

    }

    /**
     * Constructor used by the FiregroundDataObject after loading a terrain data file from disk.
     */
    public TerrainModel(FlatField terrain)
    {
        this.domain = null;
        this.terrain = terrain;
        this.terrainFactory = TerrainFactory.getInstance();
    }

    /**
     * Examines the currently loaded data providers looking for a Altitude capability. If one is
     * found, then a lookup result listener is established on that provider.
     */
    private void checkForDEMLayer()
    {
        if (this.demLayer == null)
        {
            GisViewer viewer = Lookup.getDefault().lookup(GisViewer.class);
            if (viewer == null)
            {
                LOG.severe("A GisViewer was not found. DEM lookup is disabled.");
                return;
            }

            // Determine if any of the layers have an Altitude capability            
            Collection<? extends GisLayer> layers = viewer.getLookup().lookupAll(GisLayer.class);
            nested_loop:
            for (GisLayer layer : layers)
            {
                Collection<? extends RealType> lookupAll = layer.getLookup().lookupAll(RealType.class);
                for (RealType realType : lookupAll)
                {
                    if (realType.equals(RealType.Altitude))
                    {
                        this.demLayer = layer;
                        LOG.log(Level.INFO, "Found elevation provider: {0}", layer.toString());
                        break nested_loop;
                    }
                }
            }
        }
        if (this.demLayer == null)
        {
            LOG.severe("A DEM layer is not available. LANDFIRE elevation lookup is disabled.");
        }
    }


    public final FlatField getTerrainData()
    {
        if (this.terrain == null)
        {
            this.terrain = createTerrain();
        }
        return this.terrain;
    }


    public final TerrainTuple getTerrainSample(int i)
    {
        TerrainTuple value = null;
        try
        {
            RealTuple tuple = (RealTuple) getTerrainData().getSample(i);
            Real[] reals = tuple.getRealComponents();
            value = new TerrainTuple(reals[0], reals[1], reals[2]);
        }
        catch (VisADException | RemoteException ex)
        {
            LOG.warning(ex.toString());
            Exceptions.printStackTrace(ex);
        }
        return value;
    }


    public TerrainTuple getTerrain(Coord2D latLon)
    {
        TerrainTuple value = null;
        GeoCoord2D latLonTuple = latLon instanceof GeoCoord2D ? (GeoCoord2D) latLon
            : GeoCoord2D.fromDegrees(latLon.getLatitudeDegrees(), latLon.getLongitudeDegrees());

        try
        {
            // XXX
            RealTuple tuple = (RealTuple) getTerrainData().evaluate(latLonTuple, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);

            value = new TerrainTuple(tuple.getRealComponents()[0],
                tuple.getRealComponents()[1],
                tuple.getRealComponents()[2]);
        }
        catch (VisADException | RemoteException ex)
        {
            LOG.warning(ex.toString());
            Exceptions.printStackTrace(ex);
        }
        return value;
    }


    public SpatioTemporalDomain getDomain()
    {
        return this.domain;
    }


    public Linear2DSet getDomainSet()
    {
        return this.domain.getSpatialDomainSet();
    }


    public RealTupleType getDomainType()
    {
        return this.domain.getSpatialDomainType();
    }


    private FlatField createTerrain()
    {
        try
        {
            if (this.terrainFactory == null)
            {
                throw new IllegalStateException("A TerrainFactory wasn't found.  "
                    + "Ensure the module providing the terrain is installed.");
            }
            // Init the Terrain FlatField using the FunctionType (domain_tuple -> range_tuple)
            FunctionType functionType = new FunctionType(this.domain.getSpatialDomainType(), rangeType);
            FlatField flatField = new FlatField(functionType, this.domain.getSpatialDomainSet());

            // Get a copy of the domain samples (an array of lat/lon points) 
            float[][] domainSamples = this.domain.getSpatialDomainSet().getSamples(false); // true = copy
            int numSamples = this.domain.getSpatialDomainSet().getLength();

            // Create the output range: 1 row for each dim in the terrain tuple (e.g., slope, aspect, elevation = 3)
            double[][] rangeSamples = new double[rangeType.getDimension()][numSamples];
            for (int i = 0; i < numSamples; i++)
            {
                // Get a lat/lon from the domain ...                   
                double lat = domainSamples[0][i];
                double lon = domainSamples[1][i];
                GeoCoord2D latLon = GeoCoord2D.fromDegrees(lat, lon);


                // ... and then get the terrain at that lat/lon
                TerrainTuple tuple = (TerrainTuple) this.terrainFactory.newTerrain(latLon);

                // TEST
// The LANDFIRE Dem layer is useless: the resolution is extremely low!                
//                Real testElv;
//                if (this.demLayer != null)
//                {
//                    // Get the query capability object
//                    QueryableByPoint query = this.demLayer.getLookup().lookup(QueryableByPoint.class);
//                    if (query == null)
//                    {
//                        throw new IllegalStateException("DEM Layer does not support QueryableByPoint");
//                    }
//                    Iterator<?> results = query.getObjectsAtLatLon(latLon).getResults().iterator();
//                    if (results.hasNext())
//                    {
//                        Object objectAtLatLon = results.next();
//                        if (objectAtLatLon instanceof Real)
//                        {
//                            testElv = ((Real) objectAtLatLon);
//                            System.out.println(tuple.getElevation().toValueString() + " test: " + testElv.toValueString());
//                        }
//
//                    }
//
//                }


                // And finally, update the range samples from the terrain
                for (int dim = 0; dim < tuple.getDimension(); dim++)
                {
                    rangeSamples[dim][i] = ((Real) tuple.getComponent(dim)).getValue();
                }
            }
            // Add our samples to the terrain FlatField
            flatField.setSamples(rangeSamples);

            return flatField;

        }
        catch (IllegalStateException | VisADException | RemoteException ex)
        {
            LOG.severe(ex.toString());
        }
        return null;
    }
}
