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
package com.emxsys.wmt.terrain.spi;

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.terrain.api.TerrainProvider;
import java.beans.PropertyChangeListener;
import org.openide.util.Lookup;
import visad.Real;

/**
 *
 * @author Bruce Schubert
 * @version $Id$
 */
public abstract class TerrainFactory implements TerrainProvider {

    private static TerrainFactory factory = null;

    /**
     * Returns the singleton instance of a TerrainFactory. If a class has been registered as a
     * SolarFactory service provider, then an instance of that class will be returned. Otherwise, an
     * instance of the RothermelSolarFactory will be returned.
     *
     * @return A singleton instance of a TerrainFactory.
     */
    public static TerrainFactory getInstance() {
        if (factory == null) {
            // Check the general Lookup for a service provider
            factory = Lookup.getDefault().lookup(TerrainFactory.class);

            // Use our default factory if none registered.
            if (factory == null) {
                //factory = new ...Factory();
                throw new IllegalStateException("No TerrainFactory instance was found on the global lookup.");
            }
        }
        return factory;
    }

    public abstract Real getElevation(Coord2D point);

    public abstract void addPropertyChangeListener(PropertyChangeListener listener);

    public abstract void addPropertyChangeListener(String propertyName,
                                                   PropertyChangeListener listener);

    public abstract void removePropertyChangeListener(PropertyChangeListener listener);
}
