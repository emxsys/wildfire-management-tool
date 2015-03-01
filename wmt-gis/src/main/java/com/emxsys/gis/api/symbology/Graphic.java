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

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.Entity;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The Graphic interface manages the placement and control of surface-base graphics. The Entity base
 * class makes this class compatible with a Catalog by providing the unique and named interfaces.
 *
 * @see Entity
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public interface Graphic extends Entity {

    public static final String PROP_GRAPHIC_NAME = "PROP_GRAPHIC_NAME";
    public static final String PROP_GRAPHIC_UNIQUE_ID = "PROP_GRAPHIC_UNIQUE_ID";
    public static final String PROP_GRAPHIC_POSITION = "PROP_GRAPHIC_POSITION";
    public static final String PROP_GRAPHIC_DELETED = "PROP_GRAPHIC_DELETED";
    public static final String PROP_GRAPHIC_SELECTED = "PROP_GRAPHIC_SELECTED";
    public static final String PROP_GRAPHIC_VISIBLE = "PROP_GRAPHIC_VISIBLE";

    /**
     * Determines if this instance owns the supplied implementation object.
     *
     * @param impl implementation object.
     * @return true if this instances owns the implementation.
     */
    boolean owns(Object impl);

    /**
     * The Graphic's implementation object(s) should be stored in the lookup.
     *
     * @return the lookup content associated with this graphic.
     */
    Lookup getLookup();

    /**
     * @return the position of the symbol on the globe.
     */
    Coord3D getPosition();

    /**
     * Sets the geographic position of the symbol.
     *
     * @param position the position of the symbol on the globe.
     */
    void setPosition(Coord3D position);

    /**
     * Gets a representation of the symbol, which can be used in UI elements.
     *
     * @return a representation of the symbol.
     */
    Image getImage();

    /**
     * Deletes this graphic from the globe.
     */
    void delete();

    /**
     * @return true if deleted.
     */
    boolean isDeleted();

    /**
     * Determines the if the graphic should be shown on the globe.
     *
     * @param visible true to show; false to hide.
     */
    void setVisible(boolean visible);

    /**
     * @return the visibility state;
     */
    boolean isVisible();

    /**
     * Places this graphic in the selected state. A selected graphic may be rendered differently to
     * signify the selected state.
     *
     * @param selected true to select; false to deselect
     */
    void setSelected(boolean selected);

    /**
     * @return the selected state
     */
    boolean isSelected();

    List<Coord3D> getPositions();

    void setPositions(List<Coord3D> position);

    /**
     * Returns the Renderer associated with this Symbol.
     *
     * @return the renderer, may be null.
     */
    Graphic.Renderer getRenderer();

    /**
     * Attach this symbol to a Renderer
     *
     * @param renderer a Renderer (e.g., a map layer) that will render this symbol.
     */
    void attachToRenderer(Graphic.Renderer renderer);

    /**
     * Detach this symbol from a Renderer
     *
     * @param renderer the Renderer that will no longer render this symbol.
     */
    void detachFromRenderer(Graphic.Renderer renderer);

    /**
     * Gets the factory class used to create this symbol. The provider class may be stored in the
     * file representing this symbol.
     * @return A Builder class.
     */
    Class<? extends Builder> getFactoryClass();

    /**
     * An interface for interacting with the layer that renders the graphic.
     */
    public interface Renderer {

        public static final String PROP_GRAPHIC_ADDED = "PROP_GRAPHIC_ADDED";
        public static final String PROP_GRAPHIC_REMOVED = "PROP_GRAPHIC_REMOVED";

        void addGraphic(Graphic graphic);

        void removeGraphic(Graphic graphic);

        void addGraphics(Collection<? extends Graphic> graphics);

        boolean contains(Graphic graphic);

        void addPropertyChangeListener(PropertyChangeListener listener);

        void removePropertyChangeListener(PropertyChangeListener listener);

        public Lookup getLookup();
    }

    /**
     * A Graphic factory. Uses the Builder pattern.
     */
    public interface Builder {

        /**
         * Creates a Graphic instance.
         *
         * @return A new Graphic.
         */
        Graphic build();
    }

    /**
     * A Graphic writer.
     */
    public interface Writer {

        /**
         * Writes a graphic to a persistent store.
         * @return The updated Document.
         */
        Document write();
    }
}
