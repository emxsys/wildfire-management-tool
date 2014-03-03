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
package com.emxsys.wmt.gis.api.symbology;

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.Entity;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;


/**
 * The Symbol interface manages the placement and control of point-based icons.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public interface Symbol extends Entity
{

    public static final String PROP_SYMBOL_NAME = "PROP_SYMBOL_NAME";
    public static final String PROP_SYMBOL_UNIQUE_ID = "PROP_SYMBOL_UNIQUE_ID";
    public static final String PROP_SYMBOL_POSITION = "PROP_SYMBOL_POSITION";
    public static final String PROP_SYMBOL_DELETED = "PROP_SYMBOL_DELETED";
    public static final String PROP_SYMBOL_SELECTED = "PROP_SYMBOL_SELECTED";
    public static final String PROP_SYMBOL_VISIBLE = "PROP_SYMBOL_VISIBLE";


    /**
     * Determines if this instance owns the supplied implementation object.
     *
     * @param impl implementation object.
     * @return true if this instances owns the implemenation.
     */
    boolean owns(Object impl);


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
     * Deletes this symbol from the globe.
     */
    void delete();


    /**
     * @return true if deleted.
     */
    boolean isDeleted();


    /**
     * Determines the if the symbol should be shown on the globe.
     *
     * @param visible true to show; false to hide.
     */
    void setVisible(boolean visible);


    /**
     * @return the visibility state;
     */
    boolean isVisible();


    /**
     * Places this symbol in the selected state. A selected symbol may be rendered differently to
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
     * Returns the Renderer associated with this Symbol.
     *
     * @return the renderer, may be null.
     */
    Symbol.Renderer getRenderer();


    /**
     * Attach this symbol to a Renderer
     *
     * @param renderer a Renderer (e.g., a map layer) that will render this symbol.
     */
    void attachToRenderer(Symbol.Renderer renderer);


    /**
     * Detach this symbol from a Renderer
     *
     * @param renderer the Renderer that will no longer render this symbol.
     */
    void detachFromRenderer(Symbol.Renderer renderer);


    /**
     * Gets the factory class used to create this symbol. The provider class may be stored in the
     * file representing this symbol.
     */
    Class<? extends Factory> getFactoryClass();


    /**
     * The Symbol.Renderer interface defines the methods used to manage add and remove Symbols to
     * and from a GisLayer.
     *
     * @see GisLayer
     */
    public interface Renderer
    {

        public static final String PROP_SYMBOL_ADDED = "PROP_SYMBOL_ADDED";
        public static final String PROP_SYMBOL_REMOVED = "PROP_SYMBOL_REMOVED";


        /**
         * Renderers should store the renderer implementation object(s) in the lookup.
         *
         * @return a Lookup containing the renderer implementation.
         */
        Lookup getLookup();


        /**
         * Adds a symbol to a GisLayer implementation.
         *
         * @param symbol symbol to add
         */
        void addSymbol(Symbol symbol);


        /**
         * Removes a symbol from the GisLayer implementation.
         *
         * @param symbol symbol to remove
         */
        void removeSymbol(Symbol symbol);


        /**
         * Adds a collection of symbols en masse the the GisLayer implementation
         *
         * @param symbols
         */
        void addSymbols(Collection<? extends Symbol> symbols);


        /**
         * Determines if the GisLayer implementation contains the symbol.
         *
         * @param symbol symbol to look for.
         * @return true if contained.
         */
        boolean contains(Symbol symbol);


        void addPropertyChangeListener(PropertyChangeListener listener);


        void removePropertyChangeListener(PropertyChangeListener listener);
    }


    /**
     * The Factory interface defines factory methods for creating Symbols from XML.
     */
    public interface Factory
    {

        /**
         * Creates a Symbol instance.
         *
         * @return a new Symbol.
         */
        Symbol newSymbol();


        /**
         * Creates a DataObject representing the supplied Symbol in the specified folder.
         *
         * @param symbol assigned to the DataObject
         * @param folder where the DataObject is created
         * @return a new DataObject
         */
        DataObject createDataObject(Symbol symbol, FileObject folder);
    }
}
