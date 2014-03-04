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

import com.emxsys.wmt.gis.Viewers;
import com.emxsys.wmt.gis.api.layer.BasicLayerCategory;
import com.emxsys.wmt.gis.api.layer.BasicLayerLegend;
import com.emxsys.wmt.gis.api.layer.BasicLayerGroup;
import com.emxsys.wmt.gis.api.layer.BasicLayerType;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.gis.api.layer.LayerCategory;
import com.emxsys.wmt.gis.api.layer.LayerLegend;
import com.emxsys.wmt.gis.api.layer.LayerGroup;
import com.emxsys.wmt.gis.api.layer.LayerType;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * This adaptor class presents a WorldWind Layer as a GisLayer with LayerActiveAltitudeRange and
 * LayerOpacity capabilities.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class GisLayerAdaptor implements GisLayer {

    protected InstanceContent content = new InstanceContent();
    protected Lookup lookup;
    private final Layer layer;

    public GisLayerAdaptor(Layer layer) {
        this.layer = layer;
        // add the layer implementation to this provider's lookup
        this.content.add(this.layer);

        updateCapabilities();

        // The LayerFactory stored the FileObject in the AVKey
        FileObject instanceFile = (FileObject) layer.getValue(LayerFactory.LAYER_INSTANCE_FILE_KEY);
        if (instanceFile != null) {
            BasicLayerType type = BasicLayerType.fromString((String) instanceFile.getAttribute("type"));
            BasicLayerGroup role = BasicLayerGroup.fromString((String) instanceFile.getAttribute("role"));
            BasicLayerCategory category = BasicLayerCategory.fromString((String) instanceFile.getAttribute("category"));

            BasicLayerLegend legend = null;
            String legendImage = (String) instanceFile.getAttribute("legendImage");
            if (legendImage != null) {
                ImageIcon image = ImageUtilities.loadImageIcon(legendImage, false);
                if (image != null) {
                    legend = new BasicLayerLegend();
                    legend.add(image);
                }
            }
            updateLayerAttributes(type, role, category, legend);
        }
    }

    public GisLayerAdaptor(Layer layer, LayerType type, LayerGroup role, LayerCategory category) {
        this.layer = layer;
        // add the layer implementation to this provider's lookup
        this.content.add(this.layer);

        updateCapabilities();
        updateLayerAttributes(type, role, category, null);
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
        return layer.getName();
    }

    @Override
    public void setName(String name) {
        layer.setName(name);
    }

    @Override
    public boolean isEnabled() {
        return layer.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        layer.setEnabled(enabled);
        layer.firePropertyChange(AVKey.LAYER, null, layer);
        Viewers.refreshViewersContaining(this);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.layer.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.layer.removePropertyChangeListener(listener);
    }

    private void updateLayerAttributes(LayerType type,
                                       LayerGroup group,
                                       LayerCategory category,
                                       LayerLegend legend) {
        this.content.add(type == null ? BasicLayerType.Unknown : type);
        this.content.add(group == null ? BasicLayerGroup.Undefined : group);
        this.content.add(category == null ? BasicLayerCategory.Unknown : category);
        if (legend != null) {
            this.content.add(legend);
        }
    }

    private void updateCapabilities() {
        // Add support for active altitudes
        this.content.add(new BasicLayerActiveAltitude(this.layer));
        // Add support for opacity
        this.content.add(new BasicLayerOpacity(this.layer));
    }
}
