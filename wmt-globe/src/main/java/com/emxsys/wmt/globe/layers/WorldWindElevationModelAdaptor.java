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
package com.emxsys.wmt.globe.layers;

import com.emxsys.gis.api.layer.GisLayer;
import gov.nasa.worldwind.globes.ElevationModel;
import java.beans.PropertyChangeListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class WorldWindElevationModelAdaptor implements GisLayer {

    private final ElevationModel elevationModel;
    protected InstanceContent content = new InstanceContent();
    protected Lookup lookup;

    public WorldWindElevationModelAdaptor(ElevationModel elevationModel) {
        this.elevationModel = elevationModel;
        // add the layer implementation to this provider's lookup
        this.content.add(elevationModel);
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new AbstractLookup(content);
        }
        return lookup;
    }

    @Override
    public String getName() {
        return elevationModel.getName();
    }

    @Override
    public void setName(String name) {
        elevationModel.setName(name);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        // TODO consider using elevationModel.setNetworkRetrievalEnabled(true);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.elevationModel.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.elevationModel.removePropertyChangeListener(listener);
    }
}
