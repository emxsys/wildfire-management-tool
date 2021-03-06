/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.markers;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.marker.Marker;
import com.emxsys.gis.gml.GmlConstants;
import com.emxsys.gis.gml.GmlParser;
import static com.emxsys.wmt.globe.markers.AbstractMarkerWriter.*;
import com.emxsys.util.FilenameUtils;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The BasicMarker.Builder class is responsible for creating a BasicMarker from a set of parameters
 * that are set via the the Fluent interface pattern. Examples:
 *
 * <pre>
 * To create a new Marker from given parameters:
 * {@code new Builder()
 *      .coordinate(new GeoCoord3D())
 *      .name("Marker")
 *      .build();
 * }
 *
 * To create a new Marker from an XML document:
 * {@code new Builder()
 *      .document(doc)
 *      .build();
 * }
 *
 * To create a new Marker from an XML document with a new name and coordinate:
 * {@code new Builder()
 *      .document(doc)
 *      .name("New Name")
 *      .coordinate(somewhereElse)
 *      .build();
 * }
 * </pre>
 *
 * @author Bruce Schubert
 */
@Messages({
    "# {0} - reason",
    "err.cannot.import.marker=Cannot import marker. {0}",
    "err.document.is.null=Document argument cannot be null.",
    "# {0} - document",
    "err.document.missing.marker={0} is does not have a Marker element.",
    "# {0} - document",
    "err.document.has.extra.markers={0} contains more than one Marker element. Only one Marker will be processed.",})

public abstract class AbstractMarkerBuilder implements Marker.Builder {

    private static final Logger logger = Logger.getLogger(AbstractMarkerBuilder.class.getName());
    private Document doc;
    private XPath xpath;
    private Coord3D coord;
    private String name;
    
    static {
        logger.setLevel(Level.FINE);
    }

    /**
     * Basic constructor.
     */
    public AbstractMarkerBuilder() {
    }

    /**
     * Minimal constructor for a Builder that uses an XML document for the Marker parameters.
     * @param document The document to read.
     */
    public AbstractMarkerBuilder(Document document) {
        this.doc = document;
    }

    public AbstractMarkerBuilder document(Document document) {
        this.doc = document;
        return this;
    }

    public AbstractMarkerBuilder coordinate(Coord3D coord) {
        this.coord = coord;
        return this;
    }

    public AbstractMarkerBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Builds a new BasicMarker from the established parameters.
     * @return a new BasicMarker instance.
     */
    @Override
    public abstract Marker build();
// Suggested method body template
//    {
//        BasicMarker marker = new BasicMarker();
//        if (doc != null) {
//            marker = initializeFromXml(marker);
//        }
//        return initializeFromParameters(marker);
//    }

    public String getName() {
        return name;
    }

    public Coord3D getCoordinate() {
        return coord;
    }

    public Document getDocument() {
        return doc;
    }

    /**
     * Initializes the supplied marker from the established parameters. Allows the parameters to
     * override the settings established by an XML document. Subclasses should call or override.
     * @param marker The marker to update.
     * @return The updated marker.
     */
    protected BasicMarker initializeFromParameters(BasicMarker marker) {
        if (coord != null) {
            if (doc != null) {
                logger.log(Level.INFO, "Overriding the position set by XML document for {0}.", marker.getName());
            }
            marker.setPosition(coord);
        }
        if (name != null) {
            if (doc != null) {
                logger.log(Level.INFO, "Overriding the name set by XML document for {0}.", marker.getName());
            }
            marker.setName(name);
        }
        return marker;
    }

    /**
     * Initializes the supplied marker from the Builder's XML document.
     * @param marker The BasicMarker (or derived) instance to be initialized.
     * @return The initialized marker; throws a RuntimeException on error.
     * @throws RuntimeException
     */
    protected BasicMarker initializeFromXml(BasicMarker marker) {
        this.xpath = XPathFactory.newInstance().newXPath();
        this.xpath.setNamespaceContext(MarkerSupport.getNamespaceContext());
        NodeList list;
        try {
            // Get the Marker node, there should be [0-1]
            list = (NodeList) xpath.evaluate("//" + MKR_PREFIX + ":" + TAG_MARKER, doc, XPathConstants.NODESET);
            if (list.getLength() == 0) {
                throw new IllegalStateException(Bundle.err_document_missing_marker(doc.getDocumentURI()));
            } else if (list.getLength() > 1) {
                logger.warning(Bundle.err_document_has_extra_markers(doc.getDocumentURI()));
            }
            Element mkrElem = (Element) list.item(0);
            marker.setName(xpath.evaluate(GmlConstants.GML_PREFIX + ":" + GmlConstants.NAME_PROPERTY_ELEMENT_NAME, mkrElem));
            Element pntElem = (Element) xpath.evaluate(GmlConstants.GML_PREFIX + ":" + GmlConstants.POINT_PROPERTY_ELEMENT_NAME, mkrElem, XPathConstants.NODE);
            if (pntElem == null) { // pointProperty tag not found--look for depreciated Position tag.
                pntElem = (Element) xpath.evaluate(MKR_PREFIX + ":" +TAG_POSITION, mkrElem, XPathConstants.NODE); // depreciated tag
            }
            
            // Get the Symbol
            marker.setPosition(GmlParser.parsePosition(pntElem));
            Element symElem = (Element) xpath.evaluate(MKR_PREFIX + ":" + TAG_SYMBOL, mkrElem, XPathConstants.NODE);
            if (symElem != null) {
                String imgAddress = xpath.evaluate(MKR_PREFIX + ":" + TAG_IMAGE_URL, symElem);
                String imgOffsetX = xpath.evaluate(MKR_PREFIX + ":" + TAG_IMAGE_OFFSET_X, symElem);
                String imgOffsetY = xpath.evaluate(MKR_PREFIX + ":" + TAG_IMAGE_OFFSET_Y, symElem);
                String lblOffsetX = xpath.evaluate(MKR_PREFIX + ":" + TAG_LABEL_OFFSET_X, symElem);
                String lblOffsetY = xpath.evaluate(MKR_PREFIX + ":" + TAG_LABEL_OFFSET_Y, symElem);
                String imgScale = xpath.evaluate(MKR_PREFIX + ":" + TAG_IMAGE_SCALE, symElem);
                Boolean pntDefaultImage = (Boolean) xpath.evaluate(MKR_PREFIX + ":" + TAG_POINT_AS_DEFAULT_IMAGE, symElem, XPathConstants.BOOLEAN);

                // Update the marker's symbol implementation -- use defaults where necessary
                PointPlacemark placemark = marker.getLookup().lookup(PointPlacemark.class);
                PointPlacemarkAttributes attributes = new PointPlacemarkAttributes(placemark.getAttributes());

                Offset imgOffset = new Offset(imgOffsetX.isEmpty() ? 0.5 : Double.valueOf(imgOffsetX), imgOffsetY.isEmpty() ? 0.0 : Double.valueOf(imgOffsetY), AVKey.FRACTION, AVKey.FRACTION);
                Offset lblOffset = new Offset(lblOffsetX.isEmpty() ? 0.9 : Double.valueOf(lblOffsetX), lblOffsetY.isEmpty() ? 0.6 : Double.valueOf(lblOffsetY), AVKey.FRACTION, AVKey.FRACTION);
                URL localResource = findLocalResource(imgAddress, marker.getClass());
                logger.log(Level.FINE, "image resource: {0}", localResource == null ? "null" : localResource.toString());
                attributes.setImageAddress(localResource == null ? null : localResource.toString());
                attributes.setScale(imgScale.isEmpty() ? 1.0 : Double.parseDouble(imgScale));
                attributes.setImageOffset(imgOffset);
                attributes.setLabelOffset(lblOffset);
                attributes.setUsePointAsDefaultImage(pntDefaultImage);
                
                placemark.setAttributes(attributes);
            }
            //String markerID = element.getAttribute(GmlConstants.FID_ATTR_NAME);
            String movable = mkrElem.getAttribute(ATTR_MOVABLE);
            marker.setMovable(movable.isEmpty() ? true : Boolean.parseBoolean(movable));
            return marker;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "initializeFromXml encountered {0}", ex.toString());
            throw new RuntimeException("Builder.initializeFromXml() failed!");
        }
    }

    public static URL findLocalResource(String resourceUrl, Class clazz) {
        if (resourceUrl == null || resourceUrl.isEmpty()) {
            return null;
        }
        return clazz.getResource(FilenameUtils.getFilename(resourceUrl));
    }

}
