/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.markers;

import com.emxsys.wmt.gis.api.GeoSector;
import com.emxsys.wmt.gis.api.marker.Marker;
import com.emxsys.wmt.gis.gml.GmlBuilder;
import static com.emxsys.wmt.gis.gml.GmlConstants.*;
import com.emxsys.wmt.gis.gml.GmlParser;
import com.emxsys.wmt.util.TimeUtil;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is responsible for parsing and encoding the XML for a BasicMarker. Example:
 * <pre>
 * {@code
 *<MarkerCollection xmlns="http://emxsys.com/wmt-basicmarker"
 *                  xmlns:gml="http://www.opengis.net/gml"
 *                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *                  xsi:schemaLocation="http://emxsys.com/worldwind-basicmarker BasicMarkerSchema.xsd">
 *    <gml:boundedBy>
 *        <gml:Box srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
 *            <gml:coord>
 *                <gml:X>-119.2</gml:X>
 *                <gml:Y>34.2</gml:Y>
 *            </gml:coord>
 *            <gml:coord>
 *                <gml:X>-119.2</gml:X>
 *                <gml:Y>34.2</gml:Y>
 *            </gml:coord>
 *        </gml:Box>
 *    </gml:boundedBy>
 *    <gml:featureMember>
 *       <Marker factory="com.emxsys.worldwind.markers.BasicMarker$MarkerFactory"
 *               fid="mkr-6074920447686404197"
 *               movable="true">
 *            <gml:description/>
 *            <gml:name>KOXR</gml:name>
 *            <gml:pointProperty>
 *                <gml:Point srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
 *                    <gml:coordinates>-119.2,34.2,15.0</gml:coordinates>
 *                </gml:Point>
 *            </gml:pointProperty>
 *            <Symbol>
 *                <image_url/>
 *                <image_offset_x>0.5</image_offset_x>
 *                <image_offset_y>0.0</image_offset_y>
 *                <label_offset_x>0.9</label_offset_x>
 *                <label_offset_y>0.6</label_offset_y>
 *                <image_scale>1.0</image_scale>
 *            </Symbol>
 *        </Marker>
 *    </gml:featureMember>
 *</Markers>
 * } </pre>
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages({
    "# {0} - reason",
    "err.cannot.import.marker=Cannot import marker. {0}",
    "# {0} - reason",
    "err.cannot.export.marker=Cannot export marker. {0}",
    "err.document.is.null=Document argument cannot be null.",
    "# {0} - document",
    "err.document.missing.marker={0} is does not have a Marker element.",
    "# {0} - document",
    "err.document.has.extra.markers={0} contains more than one Marker element. Only one Marker will be processed.",})
public class BasicMarkerXmlEncoder {

    public static final String BASIC_MARKER_NS_URI = "http://emxsys.com/worldwind-basicmarker";
    public static final String BASIC_MARKER_SCHEMA_FILE = "BasicMarkerSchema.xsd";
    public static final String TAG_MARKERS = "MarkerCollection";
    public static final String TAG_MARKER = "Marker";
    public static final String TAG_NAME = "name";
    public static final String TAG_DECRIPTION = "description";
    public static final String TAG_POSITION = "Position";
    public static final String TAG_SYMBOL = "Symbol";
    public static final String TAG_PROVIDER = "Provider";
    public static final String TAG_IMAGE_URL = "image_url";
    public static final String TAG_IMAGE_OFFSET_X = "image_offset_x";
    public static final String TAG_IMAGE_OFFSET_Y = "image_offset_y";
    public static final String TAG_LABEL_OFFSET_X = "label_offset_x";
    public static final String TAG_LABEL_OFFSET_Y = "label_offset_y";
    public static final String TAG_IMAGE_SCALE = "image_scale";
    public static final String ATTR_FACTORY = "factory";
    public static final String ATTR_MOVABLE = "movable";
    private static final Logger logger = Logger.getLogger(BasicMarkerXmlEncoder.class.getName());
    private static Schema schema;

    static {
        logger.setLevel(Level.ALL);
    }

    /**
     * Performs a complete rewrite of a marker xml file.
     *
     * @param doc document to update
     * @param marker marker to export
     */
    public static synchronized void writeDocument(final Document doc, final BasicMarker marker) {
        // Clear any existing element in prep for saving new marker data
        final NodeList children = doc.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            doc.removeChild(children.item(i));
        }
        // Write the data
        Element root = createMarkersElement(doc, new GeoSector(marker.getPosition(), marker.getPosition()));
        Element feature = doc.createElementNS(GML_NS_URI, GML_PREFIX + ":" + FEATURE_MEMBER_PROPERTY_ELEMENT_NAME);
        Element markerElement = createMarkerElement(doc, marker);
        if (root != null && feature != null && markerElement != null) {
            doc.appendChild(root);
            root.appendChild(feature);
            feature.appendChild(markerElement);
        } else {
            logger.log(Level.SEVERE, "writeDocument unabled to export marker ({0}).", marker.toString());
        }
    }

    /**
     * Creates an XML Element that represents the Marker Collection. Created within the
     * BASIC_MARKER_NS_URI.
     *
     * @param doc used to create the Element
     * @return a new Element representing the marker collection
     */
    static Element createMarkersElement(Document doc, GeoSector extents) {
        try {
            Element element = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_MARKERS);
            element.setAttribute("xmlns", BASIC_MARKER_NS_URI);
            element.setAttribute("xmlns:gml", GML_NS_URI);
            element.setAttribute("xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            element.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation", BASIC_MARKER_NS_URI + " " + BASIC_MARKER_SCHEMA_FILE);

            GmlBuilder gmlBuilder = new GmlBuilder(doc, GML_NS_URI, GML_PREFIX + ":" + BOUNDED_BY_PROPERTY_ELEMENT_NAME);
            gmlBuilder.append(extents);
            element.appendChild(gmlBuilder.toElement());

            return element;
        } catch (DOMException ex) {
            logger.log(Level.SEVERE, "exportXml failed! Unabled to export marker ({0}). Reason: {1}", new Object[]{
                extents.toString(), ex.toString()
            });
        }
        return null;
    }

    /**
     * Creates an XML Element that represents the Marker. Created within the BASIC_MARKER_NS_URI.
     *
     * @param doc used to create the Element
     * @param marker to export
     * @return a new Element representing the marker
     */
    static Element createMarkerElement(Document doc, BasicMarker marker) {
        try {
            Element mkr = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_MARKER);
            mkr.setAttribute(FID_ATTR_NAME, "mkr-" + Long.toString(marker.getUniqueID()));
            mkr.setAttribute(ATTR_FACTORY, marker.getFactoryClass().getName());
            mkr.setAttribute(ATTR_MOVABLE, Boolean.toString(marker.isMovable()));

            Element desc = doc.createElementNS(GML_NS_URI, GML_PREFIX + ":" + DESCRIPTION_PROPERTY_ELEMENT_NAME);
            //name.appendChild(doc.createTextNode(marker.getDescription()));
            mkr.appendChild(desc);

            Element name = doc.createElementNS(GML_NS_URI, GML_PREFIX + ":" + NAME_PROPERTY_ELEMENT_NAME);
            name.appendChild(doc.createTextNode(marker.getName()));
            mkr.appendChild(name);

            GmlBuilder gmlBuilder = new GmlBuilder(doc, GML_NS_URI, GML_PREFIX + ":" + POINT_PROPERTY_ELEMENT_NAME);
            gmlBuilder.append(marker.getPosition());
            mkr.appendChild(gmlBuilder.toElement());

            PointPlacemark placemark = marker.getLookup().lookup(PointPlacemark.class);
            PointPlacemarkAttributes attributes = placemark.getAttributes();
            if (attributes != null) {
                Element symbol = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_SYMBOL);
                Element img_url = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_IMAGE_URL);
                Element img_off_x = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_IMAGE_OFFSET_X);
                Element img_off_y = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_IMAGE_OFFSET_Y);
                Element lbl_off_x = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_LABEL_OFFSET_X);
                Element lbl_off_y = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_LABEL_OFFSET_Y);
                Element img_scale = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_IMAGE_SCALE);

                img_url.appendChild(doc.createTextNode(getFilename(attributes.getImageAddress())));
                img_off_x.appendChild(doc.createTextNode(Double.toString(attributes.getImageOffset().getX())));
                img_off_y.appendChild(doc.createTextNode(Double.toString(attributes.getImageOffset().getY())));
                lbl_off_x.appendChild(doc.createTextNode(Double.toString(attributes.getLabelOffset().getX())));
                lbl_off_y.appendChild(doc.createTextNode(Double.toString(attributes.getLabelOffset().getY())));
                img_scale.appendChild(doc.createTextNode(Double.toString(attributes.getScale())));

                symbol.appendChild(img_url);
                symbol.appendChild(img_off_x);
                symbol.appendChild(img_off_y);
                symbol.appendChild(lbl_off_x);
                symbol.appendChild(lbl_off_y);
                symbol.appendChild(img_scale);
                mkr.appendChild(symbol);
            }
            return mkr;
        } catch (DOMException ex) {
            logger.log(Level.SEVERE, "exportXml failed! Unabled to export marker ({0}). Reason: {1}", new Object[]{
                marker.toString(), ex.toString()
            });
        }
        return null;
    }

    public static BasicMarker readDocument(Document doc) {
        try {
            if (doc == null) {
                throw new IllegalArgumentException(Bundle.err_document_is_null());
            }
            // Get the Marker node, there should only be one [0-1]
            final NodeList markerNodes = doc.getElementsByTagName(TAG_MARKER);
            if (markerNodes.getLength() == 0) {
                throw new IllegalStateException(Bundle.err_document_missing_marker(doc.getDocumentURI()));
            } else if (markerNodes.getLength() > 1) {
                logger.warning(Bundle.err_document_has_extra_markers(doc.getDocumentURI()));
            }

            // Process first (and only) marker element
            return createMarkerFromElement((Element) markerNodes.item(0));

        } catch (IllegalArgumentException | IllegalStateException ex) {
            logger.severe(Bundle.err_cannot_import_marker(ex.toString()));
        }
        return null;
    }

    /**
     * Creates a BasicMarker from the XML element.
     *
     * @param element Marker element
     * @return a new BasicMarker, or null on error.
     */
    static BasicMarker createMarkerFromElement(final Element element) {
        // Get the factory class
        String clazz = element.getAttribute(ATTR_FACTORY);
        Marker.Factory markerFactory = findMarkerFactory(clazz);
        
        try {
            if (markerFactory == null) {
                throw new IllegalStateException("Marker.Factory \"" + clazz + "\" cannot be null.");
            }
            // Create a Marker via it's factory...
            Marker marker = markerFactory.newMarker();
        
            if (marker instanceof BasicMarker) {
                // ...and initialize it from the XML
                return initializeMarker(element, (BasicMarker) marker);
            } else {
                throw new IllegalStateException("Not a BasicMarker type.");
            }
        } catch (IllegalArgumentException ex) {
            logger.severe(Bundle.err_cannot_import_marker(ex.toString()));
        }
        return null;
    }

    /**
     * Initializes the marker from the XML content.
     *
     * @param element contains the XML content to be imported
     * @param marker to be updated
     * @return The updated marker.
     */
    static BasicMarker initializeMarker(final Element element, BasicMarker marker) {
        if (element == null || marker == null) {
            logger.severe("importXml failed. ");
            throw new IllegalArgumentException("Argument(s) cannot be null.");
        }

        marker.setName(getChildElement(element, GML_NS_URI, NAME_PROPERTY_ELEMENT_NAME).getTextContent());
        marker.setPosition(GmlParser.parsePosition(getChildElement(element, GML_NS_URI, POINT_PROPERTY_ELEMENT_NAME)));
        Element symbol = getChildElement(element, BASIC_MARKER_NS_URI, TAG_SYMBOL);
        if (symbol != null) {
            String imgAddress = getChildElement(symbol, BASIC_MARKER_NS_URI, TAG_IMAGE_URL).getTextContent();
            String imgOffsetX = getChildElement(symbol, BASIC_MARKER_NS_URI, TAG_IMAGE_OFFSET_X).getTextContent();
            String imgOffsetY = getChildElement(symbol, BASIC_MARKER_NS_URI, TAG_IMAGE_OFFSET_Y).getTextContent();
            String lblOffsetX = getChildElement(symbol, BASIC_MARKER_NS_URI, TAG_LABEL_OFFSET_X).getTextContent();
            String lblOffsetY = getChildElement(symbol, BASIC_MARKER_NS_URI, TAG_LABEL_OFFSET_Y).getTextContent();
            String imgScale = getChildElement(symbol, BASIC_MARKER_NS_URI, TAG_IMAGE_SCALE).getTextContent();

            // Update the marker's symbol implementation -- use defaults where necessary
            PointPlacemark placemark = marker.getLookup().lookup(PointPlacemark.class);
            PointPlacemarkAttributes attributes = placemark.getAttributes();
            if (attributes == null) {
                attributes = new PointPlacemarkAttributes();
                placemark.setAttributes(attributes);
            }
            Offset imgOffset = new Offset(
                    imgOffsetX.isEmpty() ? 0.5 : Double.valueOf(imgOffsetX),
                    imgOffsetY.isEmpty() ? 0.0 : Double.valueOf(imgOffsetY),
                    AVKey.FRACTION, AVKey.FRACTION);
            Offset lblOffset = new Offset(
                    lblOffsetX.isEmpty() ? 0.9 : Double.valueOf(lblOffsetX),
                    lblOffsetY.isEmpty() ? 0.6 : Double.valueOf(lblOffsetY),
                    AVKey.FRACTION, AVKey.FRACTION);
            URL localResource = findLocalResource(imgAddress, marker.getClass());
            logger.log(Level.FINE, "image resource: {0}", (localResource == null ? "null" : localResource.toString()));
            attributes.setImageAddress(localResource == null ? null : localResource.toString());
            attributes.setScale(imgScale.isEmpty() ? 1.0 : Double.parseDouble(imgScale));
            attributes.setImageOffset(imgOffset);
            attributes.setLabelOffset(lblOffset);
        }
        //String markerID = element.getAttribute(GmlConstants.FID_ATTR_NAME);

        String movable = element.getAttribute(ATTR_MOVABLE);
        marker.setMovable(movable.isEmpty() ? true : Boolean.parseBoolean(movable));

        return marker;
    }

    /**
     *
     * @return A FileObject for the BASIC_MARKER_SCHEMA_FILE resource.
     * @see URLMapper#findFileObject(java.net.URL)
     */
    public static FileObject getLocalSchemaFile() {
        // Get a file object from a jar file entry (URL).
        URL resource = BasicMarkerXmlEncoder.class.getResource(BASIC_MARKER_SCHEMA_FILE);
        return URLMapper.findFileObject(resource);
    }

    /**
     * Loads the BasicMarkerSchema.xsd schema.
     * @return Schema representing {@code BasicMarkerSchema.xsd}.
     */
    static Schema getMarkerSchema() {
        if (schema == null) {
            SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL schemaUrl = BasicMarkerXmlEncoder.class.getResource(BASIC_MARKER_SCHEMA_FILE);
            try {
                logger.log(Level.CONFIG, "Loading Schema ({0}) ...", schemaUrl);
                long startMs = System.currentTimeMillis();
                schema = f.newSchema(schemaUrl);
                logger.log(Level.FINE, "Schema loaded: {0} secs", TimeUtil.msToSecs(System.currentTimeMillis() - startMs));
            } catch (SAXException exception) {
                logger.severe(exception.getMessage());
            }
        }
        return schema;
    }

    private static Marker.Factory findMarkerFactory(String clazz) {
        Lookup.Template<Marker.Factory> template = new Lookup.Template<>(Marker.Factory.class, clazz, null);
        Item<Marker.Factory> item = Lookup.getDefault().lookupItem(template);
        if (item == null) {
            logger.log(Level.SEVERE, "Cannot create Marker.Factory: \"{0}\" was not found on the global lookup.", clazz);
            return null;
        }
        return item.getInstance();
    }

    private static Element getChildElement(Element element, String namespaceURI, String localTagName) {
        NodeList nodes = element.getElementsByTagNameNS(namespaceURI, localTagName);
        if (nodes != null && nodes.getLength() > 0) {
            return (Element) nodes.item(0);
        }
        return null;
    }

    private static String stripJarFileUrl(String resourceUrl) {
        if (resourceUrl == null) {
            return null;
        }
        int index = resourceUrl.indexOf("!/");
        return index == -1 ? resourceUrl : resourceUrl.substring(index + 1);
    }

    private static String getFilename(String resourceUrl) {
        if (resourceUrl == null) {
            return "";
        }
        File file = new File(resourceUrl);
        return file.getName();
    }

    public static URL findLocalResource(String resourceUrl, Class clazz) {
        if (resourceUrl == null || resourceUrl.isEmpty()) {
            return null;
        }
        return clazz.getResource(getFilename(resourceUrl));
    }

}
