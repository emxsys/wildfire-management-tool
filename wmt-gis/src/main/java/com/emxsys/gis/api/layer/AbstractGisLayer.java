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
 *     - Neither the name of Bruce Schubert,  nor the names of its 
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
package com.emxsys.gis.api.layer;

import com.emxsys.gis.api.viewer.Viewers;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Bruce Schubert
 */
public abstract class AbstractGisLayer implements GisLayer {
    protected InstanceContent content = new InstanceContent();
    protected Lookup lookup;
    protected String name;
    protected boolean enabled;
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public AbstractGisLayer(String name) {
        this(name, null, null, null);
    }

    public AbstractGisLayer(String name, LayerType type, LayerGroup group, LayerCategory category) {
        this.name = name;
        this.enabled = true;
        updateLayerAttributes(type, group, category);
    }

    protected void updateLayerAttributes(LayerType type, LayerGroup group, LayerCategory category) {
        this.content.add(type == null ? BasicLayerType.Unknown : type);
        this.content.add(group == null ? BasicLayerGroup.Undefined : group);
        this.content.add(category == null ? BasicLayerCategory.Unknown : category);
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
        return name;
    }

    @Override
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        pcs.firePropertyChange(PROP_GISLAYER_NAME, oldName, this.name);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean oldEnabled = this.enabled;
        this.enabled = enabled;
        pcs.firePropertyChange(PROP_GISLAYER_ENABLED, oldEnabled, this.enabled);
        Viewers.refreshViewersContaining(this);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

}
