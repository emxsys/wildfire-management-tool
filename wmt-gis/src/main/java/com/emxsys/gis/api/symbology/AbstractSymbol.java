/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.gis.api.symbology;

import com.emxsys.gis.api.AbstractFeature;
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.FeatureClass;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GeoPoint;
import com.emxsys.gis.api.Geometry;
import com.emxsys.gis.api.symbology.Symbol;
import com.emxsys.gis.api.viewer.GisViewer;
import com.emxsys.gis.api.viewer.Viewers;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * AbstractSymbol implements the Emxsys {@link Symbol} GIS interface, which is backed by a WorldWind
 * {@link MilStd2525TacticalSymbol} implementation within the WorldWind module.
 *
 * @see com.emxsys.worldwind.symbology.BasicSymbol
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class AbstractSymbol extends AbstractFeature implements Symbol {

    private String uniqueID;
    protected String name;
    private boolean deleted = false;
    private boolean selected = false;
    private boolean visible = true;
    private Symbol.Renderer renderer;
    protected GeoCoord3D position;
    protected GeoPoint geometry = new GeoPoint();
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static Random random = new Random();
    private static final Logger LOG = Logger.getLogger(AbstractSymbol.class.getName());

    public AbstractSymbol() {
        this.uniqueID = UUID.randomUUID().toString();
    }

    public AbstractSymbol(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        String oldName = getName();
        this.name = name;
        pcs.firePropertyChange(PROP_SYMBOL_NAME, oldName, this.name);
    }

    @Override
    public String getUniqueID() {
        return uniqueID;
    }

    @Override
    public void setUniqueID(String uniqueID) {
        String oldUniqueID = getUniqueID();
        this.uniqueID = uniqueID;
        pcs.firePropertyChange(PROP_SYMBOL_UNIQUE_ID, oldUniqueID, this.uniqueID);
    }

    @Override
    public Coord3D getCoordinates() {
        return this.position;
    }

    @Override
    public void setCoordinates(Coord3D location) {
        Coord3D oldLocation = getCoordinates();
        try {
            this.position = new GeoCoord3D(location);
        }
        catch (Exception ex) {
            this.position = GeoCoord3D.INVALID_COORD;
        }
        // Sync the GIS geometry with the implementation
        this.geometry.setPosition(position);
        pcs.firePropertyChange(PROP_SYMBOL_POSITION, oldLocation, this.position);
    }

    @Override
    abstract public Image getImage();

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Symbol.Renderer getRenderer() {
        if (renderer == null) {
            GisViewer activeViewer = Viewers.getPrimaryViewer();
            if (activeViewer != null) {
                renderer = activeViewer.getLookup().lookup(Symbol.Renderer.class);
            }
        }
        return renderer;
    }

    @Override
    public void delete() {
        boolean oldDeleted = this.deleted;
        this.deleted = true;
        pcs.firePropertyChange(PROP_SYMBOL_DELETED, oldDeleted, this.deleted);
    }

    @Override
    public boolean isDeleted() {
        return this.deleted;
    }

    @Override
    public void setSelected(boolean selected) {
        boolean oldSelected = this.selected;
        this.selected = selected;
        pcs.firePropertyChange(PROP_SYMBOL_SELECTED, oldSelected, this.selected);
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public void setVisible(boolean visible) {
        boolean oldVisible = this.visible;
        this.visible = true;
        pcs.firePropertyChange(PROP_SYMBOL_VISIBLE, oldVisible, this.visible);
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public FeatureClass getFeatureClass() {
        return FeatureClass.POINT;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }
}
