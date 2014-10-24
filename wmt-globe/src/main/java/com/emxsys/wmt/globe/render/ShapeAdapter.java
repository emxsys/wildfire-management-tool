/*
 * Copyright (c) 2009-2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.render;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Geometry;
import com.emxsys.gis.api.LineString;
import com.emxsys.gis.api.Part;
import com.emxsys.gis.api.Polygon;
import com.emxsys.gis.api.shape.Region;
import com.emxsys.gis.api.shape.Shape;
import com.emxsys.wmt.globe.util.Sectors;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceSector;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adapts GIS Geometry objects to a WorldWind Renderable.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class ShapeAdapter {

    private static final Logger logger = Logger.getLogger(ShapeAdapter.class.getName());

    public static Renderable createRenderable(Shape shape) {
        BasicShapeAttributes outlineAttributes = new BasicShapeAttributes();
        outlineAttributes.setDrawInterior(false);
        outlineAttributes.setDrawOutline(true);

        if (shape instanceof Region) {
            Region region = (Region) shape;
            SurfaceSector ss = new SurfaceSector(Sectors.fromBox(region.getSector()));
            ss.setAttributes(outlineAttributes);
            return ss;
        } else {
            logger.log(Level.WARNING, "createRenderable does not support: {0}", shape.getClass().getName());
            return null;
        }
    }

    /**
     * Creates a Renderable from a GIS Geometry object.
     *
     * @param shape to be rendered
     * @return a Renderable that represents the shape.
     */
    public static Renderable createRenderable(Geometry shape) {
        return createRenderable(shape, null, null);
    }

    /**
     * Creates a Renderable from a GIS Geometry object.
     *
     * @param shape to be rendered
     * @param normalAttrs used for normal rendering
     * @param highlightAttrs used for highlighting during mouse-overs/picking
     * @return a Renderable that represents the shape.
     */
    public static Renderable createRenderable(Geometry shape, ShapeAttributes normalAttrs,
                                              ShapeAttributes highlightAttrs) {
        BasicShapeAttributes outlineAttributes = new BasicShapeAttributes();
        outlineAttributes.setDrawInterior(false);
        outlineAttributes.setDrawOutline(true);

        if (shape instanceof Box) {
            SurfaceSector ss = new SurfaceSector(Sectors.fromBox((Box) shape));
            ss.setAttributes(normalAttrs == null ? outlineAttributes : normalAttrs);
            ss.setHighlightAttributes(highlightAttrs);
            return ss;
        } else if (shape instanceof Coord2D) {
            return null;
        } else if (shape instanceof LineString) {
            Iterator<Part> parts = shape.getParts().iterator();
            if (parts.hasNext()) {
                PartAdapter partAdapter = new PartAdapter(parts.next());
                gov.nasa.worldwind.render.Polyline poly = new gov.nasa.worldwind.render.Polyline(partAdapter);
                // TODO: set attributes

                return poly;
            }
            return null;
        } else if (shape instanceof Polygon) {
            Iterator<Part> parts = shape.getParts().iterator();
            if (parts.hasNext()) {
                PartAdapter partAdapter = new PartAdapter(parts.next());
                gov.nasa.worldwind.render.Polygon poly = new gov.nasa.worldwind.render.Polygon(partAdapter);
                // TODO: set attributes

                return poly;
            }
            return null;
        } else {
            logger.log(Level.WARNING, "createRenderable does not support: {0}", shape.getClass().getName());
            return null;
        }
    }

    /**
     * Adapts a Part to an Iterable<Postion> object.
     */
    static class PartAdapter implements Iterable<Position> {

        private final Part part;

        PartAdapter(Part part) {
            this.part = part;
        }

        @Override
        public Iterator<Position> iterator() {
            return new Iterator<Position>() {
                Iterator<double[]> iterator = part.getPoints().iterator();

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Position next() {
                    double[] latLonAlt = iterator.next();
                    return new Position(LatLon.fromDegrees(latLonAlt[0], latLonAlt[1]), latLonAlt[2]);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
    };
}
