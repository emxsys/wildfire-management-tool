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
package com.emxsys.gis.api.marker;

import com.emxsys.gis.api.AbstractFeature;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoPoint;
import com.emxsys.gis.api.FeatureClass;
import com.emxsys.gis.api.Geometry;
import com.emxsys.gis.api.marker.Marker;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import visad.VisADException;

/**
 * AbstractMarker implements the Emxsys {@link Marker} GIS interface, which is backed by a WorldWind
 * {@link PointPlacemark} implementation.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: AbstractMarker.java 540 2013-04-18 15:48:26Z bdschubert $
 */
public abstract class AbstractMarker extends AbstractFeature implements Marker {

    protected String name;
    private boolean deleted = false;
    private boolean selected = false;
    private boolean visible = true;
    protected GeoPoint geometry = new GeoPoint();
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static final Logger logger = Logger.getLogger(AbstractMarker.class.getName());

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        String oldName = getName();
        this.name = name;
        pcs.firePropertyChange(PROP_MARKER_NAME, oldName, this.name);
    }

    @Override
    public Coord3D getPosition() {
        return this.geometry.getPosition();
    }

    @Override
    public void setPosition(Coord3D position) {
        Coord3D oldPos = getPosition();
        GeoCoord3D newPos;
        try {
            newPos = new GeoCoord3D(position);
        }
        catch (VisADException | RemoteException ex) {
            newPos = GeoCoord3D.INVALID_POSITION;
        }
        this.geometry.setPosition(newPos);
        pcs.firePropertyChange(PROP_MARKER_POSITION, oldPos, newPos);
    }

    @Override
    public void delete() {
        // As a matter of practice, prevent multiple deletes 
        if (!isDeleted()) {
            setSelected(false);
            boolean oldDeleted = this.deleted;
            this.deleted = true;
            pcs.firePropertyChange(PROP_MARKER_DELETED, oldDeleted, this.deleted);
        }
    }

    @Override
    public boolean isDeleted() {
        return this.deleted;
    }

    @Override
    public void setSelected(boolean selected) {
        boolean oldSelected = this.selected;
        this.selected = selected;
        pcs.firePropertyChange(PROP_MARKER_SELECTED, oldSelected, this.selected);
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public void setVisible(boolean visible) {
        boolean oldVisible = this.visible;
        this.visible = true;
        pcs.firePropertyChange(PROP_MARKER_VISIBLE, oldVisible, this.visible);
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    abstract public Image getImage();

    @Override
    abstract public void setImage(Image symbol);

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
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + (this.geometry != null ? this.geometry.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractMarker other = (AbstractMarker) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.geometry != other.geometry && (this.geometry == null || !this.geometry.equals(other.geometry))) {
            return false;
        }
        return true;
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
