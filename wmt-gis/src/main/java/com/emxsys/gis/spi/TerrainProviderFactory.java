/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.gis.spi;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Terrain;
import com.emxsys.gis.api.TerrainProvider;
import com.emxsys.gis.api.TerrainTuple;
import org.openide.util.Lookup;
import visad.Real;
import visad.RealType;

/**
 * TerrainProviderFactory. The default instance can be overridden by registering a TerrainProvider
 * service provider on the global lookup.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class TerrainProviderFactory implements TerrainProvider {

    private static TerrainProvider instance = null;

    /**
     * Returns the singleton instance of a TerrainProvider. If a class has been registered as a
 TerrainProvider service provider, then an instance of that class will be returned. Otherwise,
 an instance of the TerrainProviderFactory will be returned.
     *
     * @return A singleton instance of a TerrainProvider
     */
    public static TerrainProvider getInstance() {
        if (instance == null) {
            // Check the general Lookup for a service provider
            instance = Lookup.getDefault().lookup(TerrainProvider.class);

            // Use our default provider if no registered provider.
            if (instance == null) {
                instance = new TerrainProviderFactory();
            }
        }
        return instance;
    }

    private TerrainProviderFactory() {
    }

    @Override
    public Terrain getTerrain(Coord2D coord) {
        return TerrainTuple.INVALID_TERRAIN;
    }

    @Override
    public Real getElevation(Coord2D coord) {
        // Return a Real with a "missing" value
        return new Real(RealType.Altitude);
    }

}
