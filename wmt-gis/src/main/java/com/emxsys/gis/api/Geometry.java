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
package com.emxsys.gis.api;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.openide.util.Lookup;

/**
 * The geometry of a shape.
 *
 * @author Bruce Schubert
 */
public interface Geometry extends Lookup.Provider {

    /**
     * Get the extents (bounding box) for this geometry.
     *
     * @return rectangular bounding box
     */
    public Box getExtents();

    /**
     * Get total number of points in all parts of this geometry.
     *
     * @return total number of points in all parts
     */
    public int getNumPoints();

    /**
     * Get number of parts comprising this geometry.
     *
     * @return number of parts
     */
    public int getNumParts();

    /**
     * Get the parts of this geometry, in the form of an iterator.
     *
     * @return an iterator over the parts of this feature. Each item is a Part.
     * @see Part
     */
    public Iterable<Part> getParts();

    /**
     * An interface for a renderer of Geometry shapes.
     */
    public interface Renderer {

        /**
         * Property name indicating a Geometry object was added to the renderer
         */
        public static final String PROP_GEOMETRY_ADDED = "PROP_GEOMETRY_ADDED";
        /**
         * Property name indicating a shape was removed from the renderer
         */
        public static final String PROP_GEOMETRY_REMOVED = "PROP_GEOMETRY_REMOVED";

        /**
         * Adds a Geometry shape to the collection of shapes for rendering.
         *
         * @param shape object to be added.
         */
        void addGeometry(Geometry shape);

        /**
         * Removes a shape from the collection of shapes for rendering.
         *
         * @param shape object to be removed.
         */
        void removeGeometry(Geometry shape);

        /**
         * Adds all the shapes in the supplied collection to the collection of shapes for rendering.
         *
         * @param shapes collection to be added.
         */
        void addGeometries(Collection<? extends Geometry> shapes);

        /**
         * Determines if the supplied shape is contained in this renderer.
         *
         * @param shape The shape to look for.
         * @return True if found.
         */
        boolean contains(Geometry shape);

        void addPropertyChangeListener(PropertyChangeListener listener);

        void removePropertyChangeListener(PropertyChangeListener listener);
    }
}
