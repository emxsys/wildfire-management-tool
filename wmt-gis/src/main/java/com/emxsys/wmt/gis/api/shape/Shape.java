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
package com.emxsys.wmt.gis.api.shape;

import com.emxsys.wmt.gis.api.Coord3D;
import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
 * The Shape interface represents renderable Geometry.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: Shape.java 540 2013-04-18 15:48:26Z bdschubert $
 */
public interface Shape {

    String PROP_SHAPE_NAME = "PROP_SHAPE_NAME";
    String PROP_SHAPE_POSITION = "PROP_SHAPE_POSITION";

    String getName();

    void setName(String text);

    Coord3D getPosition();

    void setPosition(Coord3D position);

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * An interface for a renderer of shapes
     */
    public interface Renderer {

        /**
         * Property name indicating a shape was added to the renderer
         */
        public static final String PROP_SHAPE_ADDED = "PROP_SHAPE_ADDED";
        /**
         * Property name indicating a shape was removed from the renderer
         */
        public static final String PROP_SHAPE_REMOVED = "PROP_SHAPE_REMOVED";

        /**
         * Adds a shape to the collection of shapes for rendering.
         *
         * @param shape The shape to be added.
         */
        void addShape(Shape shape);

        /**
         * Removes a shape from the collection of shapes for rendering.
         *
         * @param shape The shape to be removed.
         */
        void removeShape(Shape shape);

        /**
         * Adds all the shapes in the supplied collection to the collection of shapes for rendering.
         *
         * @param shapes The shapes to be added.
         */
        void addShapes(Collection<? extends Shape> shapes);

        /**
         * Determines if the supplied shape is contained in this renderer.
         *
         * @param shape The shape to look for.
         * @return True if found.
         */
        boolean contains(Shape shape);

        void addPropertyChangeListener(PropertyChangeListener listener);

        void removePropertyChangeListener(PropertyChangeListener listener);
    }
}
