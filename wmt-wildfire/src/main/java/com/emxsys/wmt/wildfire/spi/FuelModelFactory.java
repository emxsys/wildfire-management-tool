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
package com.emxsys.wmt.wildfire.spi;

import com.emxsys.wmt.gis.api.Box;
import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.wildfire.api.FuelModel;
import com.emxsys.wmt.wildfire.api.FuelModelProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 *
 * @author Bruce Schubert
 * @version $Id$
 */
public abstract class FuelModelFactory implements FuelModelProvider {

    private static final Logger logger = Logger.getLogger(FuelModelFactory.class.getName());

    protected FuelModelFactory() {
    }

    @Override
    public String toString() {
        return getSource() + " : " + getName();
    }

    /**
     * Allows a subclass to initialize itself, perhaps by returning a subset matching the extents.
     * Called by getInstance(Box).
     *
     * @param extents area of interest
     * @return this or a new instance
     */
    protected FuelModelFactory initialize(Box extents) {
        return this;
    }

    /**
     * Creates a FuelModelFactory that simply returns the given fuel model for locations within the
     * extents and returns null for locations outside the extents.
     *
     * @param extents for the given fuel model
     * @param fuelModel to be returned by the factory
     *
     * @return a SimpleFuelModelFactory
     * @see DefaultFactory
     */
    public static FuelModelFactory newInstance(Box extents, FuelModel fuelModel) {
        return new SimpleFuelModelFactory(extents, fuelModel, "Single");
    }

    /**
     *
     * @return a FuelModelFactory initialized with the given extents
     */
    public static List<FuelModelFactory> getInstances(Box extents) {
        ArrayList<FuelModelFactory> instances = new ArrayList<>();
        {
            Collection<? extends FuelModelFactory> factories = Lookup.getDefault().lookupAll(FuelModelFactory.class);
            for (FuelModelFactory factory : factories) {
                if (factory.getExtents().intersects(extents)) {
                    instances.add(factory.initialize(extents));
                }
            }
        }
        return instances;
    }

    /**
     *
     * @return a FuelModelFactory initialized with the given extents
     */
    public static FuelModelFactory getInstance(Box extents) {
        {
            Collection<? extends FuelModelFactory> factories = Lookup.getDefault().lookupAll(FuelModelFactory.class);
            for (FuelModelFactory factory : factories) {
                if (factory.getExtents().intersects(extents)) {
                    logger.log(Level.INFO, "getInstance() returning a {0}", factory.getClass().getName());
                    return factory.initialize(extents);
                }
            }
            return null;
        }
    }

    /**
     * Gets a registered FiregroundFactory from the global lookup. If a factory has not been
     * registered, then a DefaultFactory will be used.
     *
     * @return
     * @see DefaultFactory
     */
    public static FuelModelFactory getInstance(Coord2D location) {
        {
            Collection<? extends FuelModelFactory> factories = Lookup.getDefault().lookupAll(FuelModelFactory.class);
            for (FuelModelFactory factory : factories) {
                if (factory.getExtents().contains(location)) {
                    logger.log(Level.INFO, "getInstance() returning a {0}", factory.getClass().getName());
                    return factory;
                }
            }
            return null;
        }
    }

    private static class SimpleFuelModelFactory extends FuelModelFactory {

        private Box box;
        private FuelModel fuelModel;
        private String source;

        SimpleFuelModelFactory(Box box, FuelModel fuelModel, String source) {
            this.box = box;
            this.fuelModel = fuelModel;
            this.source = source;
        }

        @Override
        public FuelModel getFuelModel(Coord2D location) {
            return box.contains(location) ? fuelModel : null;
        }

        @Override
        public String getName() {
            return this.fuelModel.getModelName();
        }

        @Override
        public String getSource() {
            return this.source;
        }

        @Override
        public Box getExtents() {
            return this.box;
        }
    }
}
