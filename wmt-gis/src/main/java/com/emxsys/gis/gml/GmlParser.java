/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
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

import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GeoSector;
import static com.emxsys.gis.gml.GmlConstants.*;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Bruce Schubert
 */
public class GmlParser {

    private static final Logger logger = Logger.getLogger(GmlParser.class.getName());

    /**
     * Parses the supplied Element containing a 2D or 3D gml:Point.
     *
     * @param parent element containing a Point element
     * @return a new 2D Coord2D
     */
    public static GeoCoord2D parsePoint(Element parent) {
        Element point = getElement(parent, POINT_ELEMENT_NAME);
        Element coordinates = getElement(point, COORDINATES_ELEMENT_NAME);
        // parse gml:coordinates
        if (coordinates != null) {
            double[][] values = parseCoordinates(coordinates.getTextContent());
            if (values.length == 1) {
                return GeoCoord2D.fromDegrees(values[0][1], values[0][0]); // Coord2D args: lat then lon
            }
        }
        // TODO: parse gml:coord elements
        return GeoCoord2D.INVALID_POINT;
    }

    /**
     * Parses the supplied Element containing a gml:coord.
     *
     * @param coord element containing a gml:X and gml:Y elements
     * @return a new 2D Coord2D
     */
    public static GeoCoord2D parseCoordXY(Element coord) {
        Element X = getElement(coord, X_ELEMENT_NAME);
        Element Y = getElement(coord, Y_ELEMENT_NAME);
        if (X != null && Y != null) {
            double lon = Double.valueOf(X.getTextContent());
            double lat = Double.valueOf(Y.getTextContent());
            return GeoCoord2D.fromDegrees(lat, lon); // Coord2D args: lat then lon

        }
        // TODO: parse gml:coord elements
        return GeoCoord2D.INVALID_POINT;
    }

    /**
     * Parses the supplied Element containing a 3D gml:Point.
     *
     * @param parent element containing a Point element
     * @return a new 3D Coord3D
     */
    public static GeoCoord3D parsePosition(Element parent) {
        Element point = getElement(parent, POINT_ELEMENT_NAME);
        Element coordinates = getElement(point, COORDINATES_ELEMENT_NAME);
        // parse gml:coordinates
        if (coordinates != null) {
            double[][] values = parseCoordinates(coordinates.getTextContent());
            if (values.length == 1) {
                if (values[0].length == 3) {
                    return GeoCoord3D.fromDegreesAndMeters(values[0][1], values[0][0], values[0][2]); // Coord2D args: lat then lon
                }
            }
        }
        // TODO: parse gml:coord elements
        return GeoCoord3D.INVALID_COORD;
    }

    /**
     * Parses the supplied Element containing a gml:Box.
     *
     * @param parent element containing a Box element
     * @return a new Box
     */
    public static GeoSector parseBox(Element parent) {
        Element box = getElement(parent, BOX_ELEMENT_NAME);
        NodeList elements = getElements(box, COORD_ELEMENT_NAME);
        Element minLatLon = (Element) elements.item(0);
        Element maxLatLon = (Element) elements.item(1);
        // parse gml:coord element
        GeoCoord2D sw = parseCoordXY(minLatLon);
        GeoCoord2D ne = parseCoordXY(maxLatLon);

        return new GeoSector(sw, ne);
    }

    /**
     *
     * @param coordinates
     * @return double[count][dimensions]
     */
    static double[][] parseCoordinates(String coordinates) {
        String[] tuples = coordinates.split(DEFAULT_TUPLE_SEPARATOR);
        double[][] tupleValues = new double[tuples.length][];
        for (int i = 0; i < tuples.length; i++) {
            String[] coords = tuples[i].split(DEFAULT_COORDINATE_SEPARATOR);
            double[] values = new double[coords.length];
            for (int j = 0; j < coords.length; j++) {
                values[j] = Double.valueOf(coords[j]);
            }
            tupleValues[i] = values;
        }
        return tupleValues;
    }

    private static Element getElement(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagNameNS(GML_NS_URI, tagName);
        if (nodes != null && nodes.getLength() > 0) {
            return (Element) nodes.item(0);
        }
        return null;
    }

    private static NodeList getElements(Element element, String tagName) {
        return element.getElementsByTagNameNS(GmlConstants.GML_NS_URI, tagName);
    }

}
