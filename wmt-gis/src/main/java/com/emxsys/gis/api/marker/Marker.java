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

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.Feature;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;

/**
 * The Marker interface manages the placement and control of place marks.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: Marker.java 540 2013-04-18 15:48:26Z bdschubert $
 */
public interface Marker extends Feature {

    public static final String PROP_MARKER_NAME = "PROP_MARKER_NAME";
    public static final String PROP_MARKER_POSITION = "PROP_MARKER_POSITION";
    public static final String PROP_MARKER_SYMBOL = "PROP_MARKER_SYMBOL";
    public static final String PROP_MARKER_DELETED = "PROP_MARKER_DELETED";
    public static final String PROP_MARKER_SELECTED = "PROP_MARKER_SELECTED";
    public static final String PROP_MARKER_VISIBLE = "PROP_MARKER_VISIBLE";

    /**
     * Gets the text used to identify this marker. May be a filename; may be used as a label.
     *
     * @return the name that represents this marker
     */
    @Override
    String getName();

    /**
     * Establishes a new name for this marker.
     *
     * @param name the new textual representation
     */
    @Override
    void setName(String name);

    /**
     * Gets the position this marker on the globe.
     *
     * @return the position where this marker is anchored on the map.
     */
    Coord3D getPosition();

    /**
     * Establishes a new position for the marker on the globe, i.e., "moves" a marker.
     *
     * @param position new coordinates for this marker
     */
    void setPosition(Coord3D position);

    /**
     * Gets the image used for rendering this marker.
     *
     * @return the image; may be null
     */
    Image getImage();

    /**
     * Establishes a new image to be displayed.
     *
     * @param image the new symbol for this marker
     */
    void setImage(Image image);

    /**
     * Deletes this marker from the globe.
     */
    void delete();

    /**
     * @return true if deleted.
     */
    boolean isDeleted();

    /**
     * Determines the if the marker is shown on the globe.
     *
     * @param visible true to show; false to hide.
     */
    void setVisible(boolean visible);

    /**
     * @return the visibility state;
     */
    boolean isVisible();

    /**
     * Places this marker in the selected state. A selected marker may be rendered differently to
     * signify the selected state.
     *
     * @param selected true to select; false to deselect
     */
    void setSelected(boolean selected);

    /**
     * @return the selected state
     */
    boolean isSelected();

    /**
     * @param listener to be added to this marker
     */
    @Override
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * @param listener to be removed from this marker
     */
    @Override
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Gets the factory class used to create this marker. The provider class may be stored in the
     * file representing this marker.
     */
    Class<? extends Builder> getFactoryClass();

    /**
     *
     */
    public interface Renderer {

        public static final String PROP_MARKER_ADDED = "PROP_MARKER_ADDED";
        public static final String PROP_MARKER_REMOVED = "PROP_MARKER_REMOVED";

        void addMarker(Marker marker);

        void removeMarker(Marker marker);

        void addMarkers(Collection<? extends Marker> markers);

        boolean contains(Marker marker);

        void addPropertyChangeListener(PropertyChangeListener listener);

        void removePropertyChangeListener(PropertyChangeListener listener);
    }

    /**
     * A Marker factory. Use the Builder pattern.
     */
    public interface Builder {

        /**
         * Creates a Marker instance.
         *
         * @return A new Marker.
         */
        Marker build();
    }

    /**
     * A Marker writer.
     */
    public interface Writer {

        /**
         * Writes a marker to a persistent store.
         * @return The updated Document.
         */
        Document write();
        
    }
}
