/*
 * Copyright (c) 2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.gis.gml;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.Feature;
import com.emxsys.gis.api.Geometry;
import com.emxsys.gis.api.LineString;
import com.emxsys.gis.api.Part;
import com.emxsys.gis.api.Point;
import com.emxsys.gis.api.Polygon;
import static com.emxsys.gis.gml.GmlConstants.*;
import java.util.Iterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * A Builder pattern for Gml elements.
 *
 * @author Bruce Schubert
 * @version $Id: GmlBuilder.java 554 2013-04-25 22:56:15Z bdschubert $
 */
public class GmlBuilder {

    private final Document doc;
    private Element element = null;

    /**
     *
     * @param doc
     */
    public GmlBuilder(Document doc) {
        this(doc.getDocumentElement());
    }

    /**
     *
     * @param doc
     * @param tagName
     */
    public GmlBuilder(Document doc, String tagName) {
        this(doc.createElement(tagName));
    }

    /**
     *
     * @param doc
     * @param namespaceURI
     * @param qualifiedName
     */
    public GmlBuilder(Document doc, String namespaceURI, String qualifiedName) {
        this(doc.createElementNS(namespaceURI, qualifiedName));
    }

    /**
     *
     * @param element
     */
    public GmlBuilder(Element element) {
        this.doc = element.getOwnerDocument();
        this.element = element;
        Element root = doc.getDocumentElement();
        if (root == null) {
            root = this.element;
        }
        root.setAttribute("xmlns:gml", GML_NS_URI);
    }

    public void append(Coord2D point) {
        this.element.appendChild(createPoint(point));
    }

    public void append(Coord3D position) {
        this.element.appendChild(createPoint(position));
    }

    public void append(Box sector) {
        this.element.appendChild(createBox(sector));
    }

    public void append(Geometry geometry) {
        if (geometry instanceof Point) {
            this.element.appendChild(createPoint(geometry));
        }
        else if (geometry instanceof LineString) {
            if (geometry.getNumParts() > 1) {
                this.element.appendChild(createMultiLineString(geometry));
            }
            else {
                this.element.appendChild(createLineString(geometry));
            }
        }
        else if (geometry instanceof Polygon) {
            this.element.appendChild(createPolygon(geometry));
        }
        else {
            throw new UnsupportedOperationException(geometry.getClass().getSimpleName() + " not supported (yet)");
        }
    }

    public void append(Feature feature) {
        Element e;
        Geometry geometry = feature.getGeometry();
        if (geometry instanceof Point) {
            e = createPoint(geometry);
        }
        else if (geometry instanceof LineString) {
            if (geometry.getNumParts() > 1) {
                e = createMultiLineString(geometry);
            }
            else {
                e = createLineString(geometry);
            }
        }
        else if (geometry instanceof Polygon) {
            e = createPolygon(geometry);
        }
        else {
            throw new UnsupportedOperationException(geometry.getClass().getSimpleName() + " not supported (yet)");
        }
        e.setAttribute(GID_ATTR_NAME, feature.getUniqueID());
        this.element.appendChild(e);

    }

    public Element toElement() {
        return this.element;
    }

    Element createElement(String gmlElement) {
        return this.doc.createElementNS(GML_NS_URI,
                GML_PREFIX + ":" + gmlElement);
    }

    Element createElement(String gmlElement, String srs) {
        Element e = createElement(gmlElement);
        return setSrsAttribute(e, srs);
    }

    Text createTextNode(String text) {
        return this.doc.createTextNode(text);
    }

    Element setSrsAttribute(Element gmlElement, String srs) {
//        gmlElement.setAttributeNS(GML_NS_URI, GML_PREFIX + ":" + SRS_NAME_ATTRIBUTE_NAME, srs);
        gmlElement.setAttribute(SRS_NAME_ATTRIBUTE_NAME, srs);
        return gmlElement;
    }

    // -----------------------------
    // Fundemental Coord2D Types
    // -----------------------------
    /**
     * Coordinates conveyed by a single string. By default the coordinates in a tuple are separated
     * by commas, and successive tuples are separated by a space character (#x20).
     *
     * @param p 2D point
     * @return {@code <coordinates>} element
     */
    Element createCoordinates(Coord2D p) {
        // coordinates string: x,y
        StringBuilder sb = new StringBuilder(50);
        sb.append(p.getLongitudeDegrees());
        sb.append(DEFAULT_COORDINATE_SEPARATOR);
        sb.append(p.getLatitudeDegrees());

        Element coordinates = createElement(COORDINATES_ELEMENT_NAME);
        coordinates.appendChild(createTextNode(sb.toString()));
        return coordinates;
    }

    /**
     * Coordinates conveyed by a single string. By default the coordinates in a tuple are separated
     * by commas, and successive tuples are separated by a space character (#x20).
     *
     * @param p 3D point
     * @return {@code <coordinates>} element
     */
    Element createCoordinates(Coord3D p) {
        // coordinates string: x,y
        StringBuilder sb = new StringBuilder(75);
        sb.append(p.getLongitudeDegrees());
        sb.append(DEFAULT_COORDINATE_SEPARATOR);
        sb.append(p.getLatitudeDegrees());
        sb.append(DEFAULT_COORDINATE_SEPARATOR);
        sb.append(p.getAltitudeMeters());

        Element coordinates = createElement(COORDINATES_ELEMENT_NAME);
        coordinates.appendChild(createTextNode(sb.toString()));
        return coordinates;
    }

    /**
     * Coordinates conveyed by a single string. By default the coordinates in a tuple are separated
     * by commas, and successive tuples are separated by a space character (#x20).
     *
     * @param p a point geometry part
     * @return {@code <coordinates>} element
     */
    Element createCoordinates(Part p) {
        Element coordinates = createElement(COORDINATES_ELEMENT_NAME);
        coordinates.appendChild(createTextNode(toCoordinates(p)));
        return coordinates;
    }

    /**
     * Coord element that encapsulate tuple components.
     *
     * @param p 2D point
     * @return {@code <coord>} element
     */
    Element createCoord(Coord2D p) {
        Element coord = createElement(COORD_ELEMENT_NAME);
        Element xx = createElement(X_ELEMENT_NAME);
        Element yy = createElement(Y_ELEMENT_NAME);
        xx.appendChild(createTextNode(Double.toString(p.getLongitudeDegrees())));
        yy.appendChild(createTextNode(Double.toString(p.getLatitudeDegrees())));
        coord.appendChild(xx);
        coord.appendChild(yy);
        return coord;
    }

    /**
     *
     * @param p 3D point
     * @return {@code <coord>} element
     */
    Element createCoord(Coord3D p) {
        Element coord = createElement(COORD_ELEMENT_NAME, SRS_WGS84);
        Element xx = createElement(X_ELEMENT_NAME);
        Element yy = createElement(Y_ELEMENT_NAME);
        Element zz = createElement(Y_ELEMENT_NAME);
        xx.appendChild(createTextNode(Double.toString(p.getLongitudeDegrees())));
        yy.appendChild(createTextNode(Double.toString(p.getLatitudeDegrees())));
        zz.appendChild(createTextNode(Double.toString(p.getAltitudeMeters())));
        coord.appendChild(xx);
        coord.appendChild(yy);
        coord.appendChild(zz);
        return coord;
    }

    // -------------------------
    // Primitive Geometry Types
    // -------------------------
    /**
     * A Point is defined by a single coordinate tuple. The Point element is used to encode
     * instances of the Point geometry class. Each {@code <Point>} element encloses either a single
     * {@code <coord>} element or a {@code <coordinates>} element containing exactly one coordinate
     * tuple
     *
     * @param p 2D point
     * @return {@code <Point>} element
     */
    Element createPoint(Coord2D p) {
        Element point = createElement(POINT_ELEMENT_NAME, SRS_WGS84);
        Element coordinates = createCoordinates(p);
        point.appendChild(coordinates);
        return point;
    }

    /**
     * A Point is defined by a single coordinate tuple.
     *
     * @param p 3D point
     * @return {@code <Point>} element
     */
    Element createPoint(Coord3D p) {
        Element point = createElement(POINT_ELEMENT_NAME, SRS_WGS84);
        Element coordinates = createCoordinates(p);
        point.appendChild(coordinates);
        return point;
    }

    /**
     * A Point is defined by a single coordinate tuple.
     *
     * @param g
     * @return {@code <Point>} element
     */
    Element createPoint(Geometry g) {
        Part p = g.getParts().iterator().next();
        if (p.getNumPoints() > 1) {
            throw new IllegalArgumentException("createPoint: num points > 1");
        }
        Element point = createElement(POINT_ELEMENT_NAME);
        Element coordinates = createCoordinates(p);
        point.appendChild(coordinates);
        return point;
    }

    /**
     * The Box structure defines an extent using a pair of coordinate tuples. The Box element is
     * used to encode extents. Each {@code <Box>} element encloses either a sequence of two
     * {@code <coord>} elements or a {@code <coordinates>} element containing exactly two coordinate
     * tuples; the first of these is constructed from the minimum values measured along all axes,
     * and the second is constructed from the maximum values measured along all axes.
     *
     * @param s extents
     * @return {@code <Box>} element
     */
    Element createBox(Box s) {
        Element box = createElement(BOX_ELEMENT_NAME, SRS_WGS84);
        Element coordMin = createCoord(s.getSouthwest());
        Element coordMax = createCoord(s.getNortheast());
        box.appendChild(coordMin);
        box.appendChild(coordMax);
        return box;
    }

    /**
     * A LineString is defined by two or more coordinate tuples, with linear interpolation between
     * them. A LineString is a piece-wise linear path defined by a list of coordinates that are
     * assumed to be connected by straight line segments. A closed path is indicated by having
     * coincident first and last coordinates. At least two coordinates are required.
     *
     * @param g line geometry
     * @return {@code <LineString>} element
     */
    Element createLineString(Geometry g) {
        if (g.getNumParts() > 1) {
            throw new IllegalArgumentException("createLineString: num parts > 1");
        }
        return createLineString(g.getParts().iterator().next());
    }

    Element createLineString(Part p) {
        if (p.getNumPoints() < 2) {
            throw new IllegalArgumentException("createLineString: num points < 2");
        }
        Element line = createElement(LINESTRING_ELEMENT_NAME, SRS_WGS84);
        Element coordinates = createCoordinates(p);
        line.appendChild(coordinates);
        return line;
    }

    /**
     * A LinearRing is defined by four or more coordinate tuples, with linear interpolation between.
     * A LinearRing is a closed, simple piece-wise linear path which is defined by a list of
     * coordinates that are assumed to be connected by straight line segments. The last coordinate
     * must be coincident with the first coordinate and at least four coordinates are required (the
     * three to define a ring plus the fourth duplicated one). Since a LinearRing is used in the
     * construction of Polygons (which specify their own SRS), the srsName attribute is not needed.
     *
     * @param p ring geometry part
     * @return {@code <LinearRing>} element
     */
    Element createLinearRing(Part p) {
        if (p.getNumPoints() < 4) {
            throw new IllegalArgumentException("createLinearRing: num points < 4");
        }
        Element point = createElement(LINEARRING_ELEMENT_NAME, SRS_WGS84);
        Element coordinates = createCoordinates(p);
        point.appendChild(coordinates);
        return point;
    }

    /**
     * A Polygon is defined by an outer boundary and zero or more inner boundaries which are in turn
     * defined by LinearRings. A Polygon is a connected surface. Any pair of points in the polygon
     * can be connected to one another by a path. The boundary of the Polygon is a set of
     * LinearRings. We distinguish the outer (exterior) boundary and the inner (interior)
     * boundaries; the LinearRings of the interior boundary cannot cross one another and cannot be
     * contained within one another. There must be at most one exterior boundary and zero or more
     * interior boundary elements. The ordering of LinearRings and whether they form clockwise or
     * anti-clockwise paths is not important.
     *
     * @param g polygon geometry
     * @return {@code <Polygon>} element
     */
    Element createPolygon(Geometry g) {

        Element polygon = createElement(POLYGON_ELEMENT_NAME, SRS_WGS84);
        Iterator<Part> iterator = g.getParts().iterator();
        boolean isFirstRing = true;
        while (iterator.hasNext()) {
            Element boundary = createElement(isFirstRing ? EXTERIOR_ELEMENT_NAME : INTERIOR_ELEMENT_NAME);
            Element ring = createLinearRing(iterator.next());
            boundary.appendChild(ring);
            polygon.appendChild(boundary);
            isFirstRing = false;
        }
        return polygon;
    }

    // -------------------------
    // Aggregate Geometry Types
    // -------------------------
    /**
     * A MultiLineString is a homogenous collection of LineStrings. A MultiLineString is defined by
     * one or more LineStrings, referenced through lineStringMember elements.
     *
     * @param g multi-part line geometry
     * @return {@code <MultiLineString>} element
     */
    Element createMultiLineString(Geometry g) {
        Element multiline = createElement(MULTILINESTRING_ELEMENT_NAME, SRS_WGS84);
        Iterator<Part> iterator = g.getParts().iterator();
        while (iterator.hasNext()) {
            Element member = createElement(LINESTRING_MEMBER_ELEMENT_NAME);
            Element line = createLineString(iterator.next());
            member.appendChild(line);
            multiline.appendChild(member);
        }
        return multiline;
    }

    String toCoordinates(Part p) {
        // coordinate tuples string: x,y,z x,y,z x,y,z ...
        int bufSize = p.getNumDimensions() * p.getNumPoints() * 20;
        StringBuilder sb = new StringBuilder(bufSize);
        for (int i = 0; i < p.getNumPoints(); i++) {
            if (i > 0) {
                sb.append(DEFAULT_TUPLE_SEPARATOR);
            }
            sb.append(p.getX()[i]);
            sb.append(DEFAULT_COORDINATE_SEPARATOR);
            sb.append(p.getY()[i]);
            if (p.getNumDimensions() == 3) {
                sb.append(DEFAULT_COORDINATE_SEPARATOR);
                sb.append(p.getZ()[i]);
            }
        }
        return sb.toString();
    }

    String toCoordinates(double[] x, double[] y) {
        // coordinate tuples string: x,y x,y x,y ...
        StringBuilder sb = new StringBuilder(50);
        for (int i = 0; i < x.length; i++) {
            if (i > 0) {
                sb.append(DEFAULT_TUPLE_SEPARATOR);
            }
            sb.append(x[i]);
            sb.append(DEFAULT_COORDINATE_SEPARATOR);
            sb.append(y[i]);
        }
        return sb.toString();
    }

    String toCoordinates(double[] x, double[] y, double[] z) {
        // coordinate tuples string: x,y,z x,y,z ...
        StringBuilder sb = new StringBuilder(50);
        for (int i = 0; i < x.length; i++) {
            if (i > 0) {
                sb.append(DEFAULT_TUPLE_SEPARATOR);
            }
            sb.append(x[i]);
            sb.append(DEFAULT_COORDINATE_SEPARATOR);
            sb.append(y[i]);
            sb.append(DEFAULT_COORDINATE_SEPARATOR);
            sb.append(z[i]);
        }
        return sb.toString();
    }
}
