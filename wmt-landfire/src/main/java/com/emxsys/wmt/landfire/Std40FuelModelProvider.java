/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.landfire;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.capabilities.QueryableByPoint;
import com.emxsys.gis.api.viewer.GisViewer;
import com.emxsys.wildfire.api.AbstractFuelModelProvider;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelModelProvider;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wmt.landfire.layers.FBFM40Layer;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.lookup.ServiceProvider;

/**
 * The Std40FuelModelProvider provides StdFuelModel instances from the FBFM40Layer.
 * 
 * @author Bruce Schubert
 */
@ServiceProvider(service = FuelModelProvider.class, position = 1000)
public class Std40FuelModelProvider extends AbstractFuelModelProvider {

    private static final Logger logger = Logger.getLogger(Std40FuelModelProvider.class.getName());
    private Lookup.Result<FBFM40Layer> fuelModelLayers;
    private FBFM40Layer fuelModelLayer;
    private final GeoSector extents = new GeoSector(22.6952681387, -128.0067177405, 51.6768794844, -65.2077897436);

    public Std40FuelModelProvider() {

        GisViewer viewer = Lookup.getDefault().lookup(GisViewer.class);
        if (viewer == null) {
            logger.severe("A GisViewer was not found.  Fuel Model layer monitoring is disabled.");
            throw new IllegalStateException("A GisViewer was not found.");
        } else {
            // Listen for the existance of fuel model layers
            // TODO: this seems kinda clunky...we 'should' be able to interrogate fuels w/o a viewer
            this.fuelModelLayers = viewer.getGisLayerList().getLookup().lookupResult(FBFM40Layer.class);
            this.fuelModelLayers.addLookupListener((LookupEvent ev) -> {
                checkForFuelModelLayer();
            });
        }
        checkForFuelModelLayer();
    }

    /**
     * Examines the currently loaded data providers looking for a FuelModel
     * capability. If one is found, then a lookup result listener is established
     * on that provider.
     */
    private void checkForFuelModelLayer() {
        Collection<? extends FBFM40Layer> allInstances = this.fuelModelLayers.allInstances();
        if (allInstances.isEmpty()) {
            this.fuelModelLayer = null;
            logger.config("A Std 40 Fuel Model layer is not available. Fuel Model lookup is disabled.");
            return;
        }

        for (FBFM40Layer layer : allInstances) {
            this.fuelModelLayer = layer;
            logger.log(Level.CONFIG, "Found a fuel model provider for FBFM40: {0}", layer.toString());
            break;
        }
    }

    @Override
    public Box getExtents() {
        return this.extents;
    }

    /**
     * Gets the FuelModel at the given location.
     * @param location The location where the fuel model is sampled.
     * @return The fuel model at the location, or StdFuelModel.INVALID if not found.
     */    
    @Override
    public FuelModel getFuelModel(Coord2D location) {
        // Get the query capability object
        QueryableByPoint query = this.fuelModelLayer.getLookup().lookup(QueryableByPoint.class);
        if (query == null) {
            throw new IllegalStateException("FuelModel layer doesn't support QueryableByPoint");
        }
        // Find the fuel model at this location
        Iterator<?> results = query.getObjectsAtLatLon(location).getResults().iterator();
        if (results.hasNext()) {
            Object objectAtLatLon = results.next();
            if (objectAtLatLon != null && objectAtLatLon instanceof StdFuelModel) {
                return (StdFuelModel) objectAtLatLon;
            }
        }
        logger.log(Level.FINE, "No FuelModel found for {0}", location);        
        return StdFuelModel.INVALID;
    }

    @Override
    public String getSource() {
        return "LANDFIRE";
    }

    @Override
    public String getName() {
        return "Standard 40 Fuel Models";
    }

}
