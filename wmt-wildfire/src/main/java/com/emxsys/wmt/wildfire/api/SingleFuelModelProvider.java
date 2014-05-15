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
package com.emxsys.wmt.wildfire.api;

import com.emxsys.wmt.gis.api.Box;
import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.GeoSector;

/**
 * The SingleFuelModelProvider provides a fuel model for the given extents.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SingleFuelModelProvider implements FuelModelProvider {

    private final Box box;
    private final FuelModel fuelModel;
    private final String source;

    /**
     * Constructs a single fuel model provider for the world extents.
     * @param fuelModel The fuel model to provide.
     */
    public SingleFuelModelProvider(FuelModel fuelModel) {
        this(GeoSector.WORLD, fuelModel);
    }

    /**
     * Constructs a single fuel model provider for the given extents.
     * @param box The provider's geographical extents.
     * @param fuelModel The fuel model to provide.
     */
    public SingleFuelModelProvider(Box box, FuelModel fuelModel) {
        this(box, fuelModel, "User");

    }

    /**
     * Constructs a single fuel model provider for the given extents.
     * @param box The provider's geographical extents.
     * @param fuelModel The fuel model to provide.
     * @param source The name of the source, e.g., "User".
     */
    public SingleFuelModelProvider(Box box, FuelModel fuelModel, String source) {
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

    @Override
    public String toString() {
        return "SingleFuelModelProvider{" + "box=" + box + ", fuelModel=" + fuelModel+ '}';
    }
}
