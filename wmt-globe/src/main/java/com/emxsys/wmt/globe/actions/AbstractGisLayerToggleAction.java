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
 * - Neither the name of the Emxsys company nor the names of its 
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
package com.emxsys.wmt.globe.actions;

import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.wmt.globe.Globe;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * This abstract class selects or deselects the action's presenter(s) based on the enabled status of
 * a map layer.
 *
 * @see GisLayer
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class AbstractGisLayerToggleAction extends AbstractAction implements
        PropertyChangeListener,
        LookupListener,
        ContextAwareAction {

    private static final Logger logger = Logger.getLogger(AbstractGisLayerToggleAction.class.getName());
    private final String mapName;
    private Lookup.Result<GisLayer> lookupResult;
    private GisLayer gisLayer;

    /**
     * @param mapName the displayName (or name) used to find the GisLayer within the Globe.
     * @see GisLayer
     */
    protected AbstractGisLayerToggleAction(String mapName) {
        this.mapName = mapName;
    }

    /**
     * @return the GisLayer to be toggled.
     */
    public GisLayer getGisLayer() {
        return gisLayer;
    }

    @Override
    public boolean isEnabled() {
        initialize();
        return super.isEnabled();
    }

    /**
     * Initializes the lookupResult and, indirectly, the map layer.
     */
    private void initialize() {
        if (this.lookupResult != null) {
            // Already initialized
            return;
        }
        this.lookupResult = Globe.getInstance().getGisLayerList().getLookupResult();
        this.lookupResult.addLookupListener(this);

        // Update the map layer through a dummy event
        resultChanged(null);
    }

    /**
     * Update the enabled state of the button.
     *
     * @param ignoredEvent from the lookupResult from the GisViewer
     */
    @Override
    public void resultChanged(LookupEvent ignoredEvent) {
        // Check to see if our layer is attached to the viewer.
        boolean isFound = false;
        GisLayer aLayer = null;
        Iterator<? extends GisLayer> i = this.lookupResult.allInstances().iterator();
        while (i.hasNext()) {
            aLayer = i.next();
            if (aLayer.getName().equals(this.mapName)) {
                isFound = true;
                break;
            }
        }
        // Update our layer reference
        if (this.gisLayer == null && isFound) {
            this.gisLayer = aLayer;
            this.gisLayer.addPropertyChangeListener(WeakListeners.propertyChange(this, this.gisLayer));
            propertyChange(null);
        }
        else if (this.gisLayer != null && !isFound) {
            this.gisLayer.removePropertyChangeListener(this);
            this.gisLayer = null;
        }

        // Enable this action and button if the layer exists
        setEnabled(this.gisLayer != null);
    }

    /**
     * Toggle the enabled state of the layer.
     *
     * @param ignoredEvent ignored.
     */
    @Override
    public void actionPerformed(ActionEvent ignoredEvent) {
        if (this.gisLayer == null) {
            throw new IllegalStateException("GisLayer is null.");
        }
        // Changing the enabled state will fire a PropertyChangeEvent which will update the UI.
        this.gisLayer.setEnabled(!this.gisLayer.isEnabled());
    }

    /**
     * Update the selected state of the button.
     * <p>
     * @param ignoredEvent ignored.
     */
    @Override
    public void propertyChange(PropertyChangeEvent ignoredEvent) {
        putValue(Action.SELECTED_KEY, this.gisLayer.isEnabled());
    }
}
