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
package com.emxsys.wildfire.spi;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.wildfire.api.FuelModelProvider;
import com.emxsys.wildfire.api.SingleFuelModelProvider;
import com.emxsys.wildfire.api.StdFuelModelParams13;
import com.emxsys.wildfire.api.StdFuelModelParams40;
import com.emxsys.wildfire.api.StdFuelModelProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * The DefaultFuelModelProvider supplies a list of registered FuelModelProvider service providers
 * plus the system defined Std 13 and Std 40 SingleFuelModelProviders.
 *
 * @author Bruce Schubert
 */
public class DefaultFuelModelProvider {

    private static ArrayList<FuelModelProvider> instances;
    private static final Logger logger = Logger.getLogger(DefaultFuelModelProvider.class.getName());

    /**
     * Gets the registered FuelModelProviders service providers from the global lookup (if any) plus
     * a SingleFuelModelProvider for each of the StdFuelModelParams13 and StdFuelModelParams40.
     *
     * @return A collection of FuelModelProvider instances.
     */
    public static List<FuelModelProvider> getInstances() {
        if (instances == null) {

            // Get all the registered service provider instances
            Collection<? extends FuelModelProvider> serviceProviders = Lookup.getDefault().lookupAll(FuelModelProvider.class);
            instances = new ArrayList<>(serviceProviders);
            serviceProviders.stream().forEach((serviceProvider) -> {
                logger.log(Level.CONFIG, "Providing a {0} instance.", serviceProvider.getClass().getName());
            });

            // Add the Standard 13 FuelModels
            for (StdFuelModelParams13 fbfm : StdFuelModelParams13.values()) {
                instances.add(new StdFuelModelProvider(fbfm.getModelNo()));
            }
            logger.log(Level.CONFIG, "Providing Std 13 {0} instances.", SingleFuelModelProvider.class.getSimpleName());
            // Add the Standard 40 FuelModels
            for (StdFuelModelParams40 fbfm : StdFuelModelParams40.values()) {
                instances.add(new StdFuelModelProvider(fbfm.getModelNo()));
            }
            logger.log(Level.CONFIG, "Providing Std 40 {0} instances.", SingleFuelModelProvider.class.getSimpleName());
        }
        return instances;
    }

    /**
     * Gets the FuelModelProvider instances that contain the given coordinate.
     *
     * @param coord The coordinate that will be tested against the provider's extents.
     * @return A collection of FuelModelProvider instances that are valid for the coordinate.
     */
    public static List<FuelModelProvider> getInstances(Coord2D coord) {
        ArrayList<FuelModelProvider> providers = new ArrayList<>();

        getInstances().stream()
                .filter((provider) -> (provider.getExtents().contains(coord)))
                .forEach((provider) -> {
                    providers.add(provider);
                });
        return providers;
    }
    
    /**
     * Gets the FuelModelProvider instances that intersect the given extents.
     *
     * @param extents The box that will be tested for intersection with the provider's extents.
     * @return A collection of FuelModelProvider instances that are valid for the box.
     */
    public static List<FuelModelProvider> getInstances(Box extents)
    {
        ArrayList<FuelModelProvider> providers = new ArrayList<>();

        getInstances().stream()
                .filter((provider) -> (provider.getExtents().intersects(extents)))
                .forEach((provider) -> {
                    providers.add(provider);
                });
        return providers;
      }
    
}