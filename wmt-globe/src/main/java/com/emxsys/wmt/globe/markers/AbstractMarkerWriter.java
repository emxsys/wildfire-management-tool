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

import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.marker.Marker;
import com.emxsys.gis.gml.GmlBuilder;
import com.emxsys.gis.gml.GmlConstants;
import com.emxsys.util.FilenameUtils;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The BasicMarker.Writer class is responsible for writing out a marker to an XML document. It can
 * either create an XML file or update an XML document by invoking either the {@code folder()} or
 * the {@code document()} method, respectively. Follows the Fluent interface pattern. For example:
 *
 * <pre>
 * To update a Marker XML document:
 * {@code new Writer()
 *      .document(doc)
 *      .marker(marker)
 *      .write();
 * }
 *
 * To create a new Marker XML document on disk:
 * {@code new Writer()
 *      .folder(folder)
 *      .marker(marker)
 *      .write();
 * }
 * </pre>
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({
    "# {0} - reason",
    "err.cannot.export.marker=Cannot export marker. {0}",})
public abstract class AbstractMarkerWriter implements Marker.Writer {

    public static final String BASIC_MARKER_NS_URI = "http://emxsys.com/worldwind-basicmarker";
    public static final String BASIC_MARKER_SCHEMA_FILE = "BasicMarkerSchema.xsd";
    public static final String MKR_PREFIX = "mkr";
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
    // Schema version 2.0 properties
    public static final String TAG_POINT_AS_DEFAULT_IMAGE = "point_as_default_image";
    public static final String TAG_IMAGE_COLOR = "image_color";
    public static final String TAG_LABEL_FONT = "label_font";
    public static final String TAG_LABEL_MATERIAL = "label_material";
    public static final String TAG_LINE_MATERIAL = "line_material";
    public static final String TAG_HEADING = "heading";
    public static final String TAG_HEADING_REFERENCE = "heading_reference";
    public static final String ATTR_ALTITUDE_MODE = "altitude_mode";
    public static final String ATTR_AMBIENT = "ambient";
    public static final String ATTR_DIFFUSE = "diffuse";
    public static final String ATTR_SPECULAR = "specular";
    public static final String ATTR_EMISSION = "emission";
    public static final String ATTR_SHININESS = "shininess";

    private static final Logger logger = Logger.getLogger(AbstractMarkerWriter.class.getName());

    private final BasicMarker marker;
    private Document doc;
    private FileObject folder;

    /**
     * Constructs a Writer for the the given Marker.
     * @param marker The marker to write.
     */
    public AbstractMarkerWriter(BasicMarker marker) {
        this.marker = marker;
    }

    /**
     * Creates a Writer that will update the given document with the contents of the given Marker.
     * @param doc The document to update.
     * @param marker The marker to write.
     */
    public AbstractMarkerWriter(Document doc, BasicMarker marker) {
        this.doc = doc;
        this.marker = marker;
    }

    /**
     * Creates a Writer that will create an XML document on disk with the contents of the given
     * Marker.
     * @param folder The folder where the XML document will be created.
     * @param marker The marker to write.
     */
    public AbstractMarkerWriter(FileObject folder, BasicMarker marker) {
        this.marker = marker;
        this.folder = folder;
    }

    /**
     * Sets the document to write to. Mutually exclusive with the folder setting.
     * @param doc The document to write to.
     * @return The updated Writer.
     */
    public AbstractMarkerWriter document(Document doc) {
        this.doc = doc;
        this.folder = null;
        return this;
    }

    /**
     * Sets the folder where the new Marker will be created. Mutually exclusive with the document
     * setting.
     * @param folder The folder where the new Marker file will be created.
     * @return The updated Writer.
     */
    public AbstractMarkerWriter folder(FileObject folder) {
        this.folder = folder;
        this.doc = null;
        return this;
    }

    public Document getDocument() {
        return doc;
    }

    public BasicMarker getMarker() {
        return marker;
    }

    public FileObject getFolder() {
        return folder;
    }

    /**
     * Writes the contents of a Marker to a persistent store. Either to the given XML document, or
     * to a new XML file in the given folder.
     * @return Returns the newly created or updated document.
     */
    @Override
    public Document write() {
        if (marker == null) {
            throw new IllegalArgumentException("BasicMarker.Writer.write() failed: The marker object cannot be null");
        }
        if (folder != null) {
            return createDataObject();
        } else if (doc != null) {
            return writeDocument();
        } else {
            throw new IllegalStateException("BasicMarker.Writer.write() failed: Either the folder or the document must be set.");
        }
    }

    /**
     * Create a new BasicMarkerDataObject (file) from a model Marker object.
     * @return The XML document from the DataObject
     * @see BasicMarkerTemplateHandler
     */
    protected Document createDataObject() {
        try {
            // throws an IllegalArgumentException if not found
            DataFolder dataFolder = DataFolder.findFolder(folder);
            DataObject template = getTemplate();
            if (template == null) {
                throw new IllegalStateException("getTemplate() returned null.");
            }
            // Ensure the filename is unique -- appends a numeral if not
            String filename = FilenameUtils.getUniqueEncodedFilename(folder, marker.getName(), template.getPrimaryFile().getExt());

            // Get the registered templateFile (could be from a subclass).
            // Create the marker file from our templateFile. Delegates to BasicMarkerTemplateHandler,
            // which is a CreateFromTemplateHandler service provider.
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("model", marker);
            BasicMarkerDataObject dataObject = (BasicMarkerDataObject) template.createFromTemplate(dataFolder, filename, parameters);

            return dataObject.getDocument();

        } catch (IllegalStateException | IllegalArgumentException | IOException | SAXException exception) {
            logger.log(Level.SEVERE, "createDataObject() failed: {0}", exception.toString());
            return null;
        }
    }

    /**
     * Gets the template file used by createDataObject(). Subclasses must define the template.
     *
     * @return A template file file for new Markers.
     */
    protected abstract DataObject getTemplate();

    /**
     * Performs a complete rewrite of the marker XML file via DOM.
     * @return Returns the updated document.
     */
    protected Document writeDocument() {
        // Clear any existing element in prep for saving new marker data
        final NodeList children = doc.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            doc.removeChild(children.item(i));
        }
        // Create the nodes
        Element root = createMarkersElement(new GeoSector(marker.getPosition(), marker.getPosition()));
        Element feature = doc.createElementNS(GmlConstants.GML_NS_URI, GmlConstants.GML_PREFIX + ":" + GmlConstants.FEATURE_MEMBER_PROPERTY_ELEMENT_NAME);
        Element markerElement = createMarkerElement();
        if (root != null && feature != null && markerElement != null) {
            // Assemble the document
            doc.appendChild(root);
            root.appendChild(feature);
            feature.appendChild(markerElement);
        } else {
            logger.log(Level.SEVERE, "writeDocument unable to export marker ({0}).", marker.toString());
        }
        return doc;
    }

    /**
     * Creates an XML Element that represents the Marker Collection. Created within the
     * BASIC_MARKER_NS_URI.
     * @param extents The geographical extents of the collection.
     * @return A new Element representing the marker collection.
     */
    protected Element createMarkersElement(GeoSector extents) {
        try {
            Element element = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_MARKERS);
            //Element element = doc.createElement(TAG_MARKERS);
            //element.setAttribute("xmlns:mkr", BASIC_MARKER_NS_URI);
            element.setAttribute("xmlns:gml", GmlConstants.GML_NS_URI);
            element.setAttribute("xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            element.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation", BASIC_MARKER_NS_URI + " " + BASIC_MARKER_SCHEMA_FILE);
            GmlBuilder gmlBuilder = new GmlBuilder(doc, GmlConstants.GML_NS_URI, GmlConstants.GML_PREFIX + ":" + GmlConstants.BOUNDED_BY_PROPERTY_ELEMENT_NAME);
            gmlBuilder.append(extents);
            element.appendChild(gmlBuilder.toElement());
            return element;
        } catch (DOMException ex) {
            logger.log(Level.SEVERE, "exportXml failed! Unabled to export marker ({0}). Reason: {1}", new Object[]{extents.toString(), ex.toString()});
        }
        return null;
    }

    /**
     * Creates an XML Element that represents the Marker. Created within the BASIC_MARKER_NS_URI.
     * @return A new Element representing the marker.
     */
    protected Element createMarkerElement() {
        try {
            Element mkr = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_MARKER);
            //mkr.setAttributeNS(GmlConstants.GML_NS_URI, GmlConstants.FID_ATTR_NAME, "mkr-" + marker.getUniqueID());
            mkr.setAttribute(ATTR_FACTORY, marker.getFactoryClass().getName());
            mkr.setAttribute(ATTR_MOVABLE, Boolean.toString(marker.isMovable()));
            Element desc = doc.createElementNS(GmlConstants.GML_NS_URI, GmlConstants.GML_PREFIX + ":" + GmlConstants.DESCRIPTION_PROPERTY_ELEMENT_NAME);
            //name.appendChild(doc.createTextNode(marker.getDescription()));
            mkr.appendChild(desc);
            
            Element name = doc.createElementNS(GmlConstants.GML_NS_URI, GmlConstants.GML_PREFIX + ":" + GmlConstants.NAME_PROPERTY_ELEMENT_NAME);
            name.appendChild(doc.createTextNode(marker.getName()));
            mkr.appendChild(name);
            
            GmlBuilder gmlBuilder = new GmlBuilder(doc, GmlConstants.GML_NS_URI, GmlConstants.GML_PREFIX + ":" + GmlConstants.POINT_PROPERTY_ELEMENT_NAME);
            gmlBuilder.append(marker.getPosition());
            mkr.appendChild(gmlBuilder.toElement());
            
            PointPlacemark placemark = marker.getLookup().lookup(PointPlacemark.class);
            PointPlacemarkAttributes attributes = placemark.getAttributes();
            if (attributes != null) {
                Element symbol = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_SYMBOL);
                symbol.setAttribute(ATTR_ALTITUDE_MODE, Integer.toString(placemark.getAltitudeMode()));
                // Version 1.0 properties
                Element img_url = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_IMAGE_URL);
                Element img_off_x = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_IMAGE_OFFSET_X);
                Element img_off_y = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_IMAGE_OFFSET_Y);
                Element lbl_off_x = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_LABEL_OFFSET_X);
                Element lbl_off_y = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_LABEL_OFFSET_Y);
                Element img_scale = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_IMAGE_SCALE);
                img_url.appendChild(doc.createTextNode(FilenameUtils.getFilename(attributes.getImageAddress())));
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
                // Version 2.0 properties
                Element pnt_default_image = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_POINT_AS_DEFAULT_IMAGE);
                Element img_color = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_IMAGE_COLOR);
                Element lbl_font = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_LABEL_FONT);
                Element lbl_material = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_LABEL_MATERIAL);
                Element line_material = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_LINE_MATERIAL);
                Element heading = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_HEADING);
                Element heading_ref = doc.createElementNS(BASIC_MARKER_NS_URI, MKR_PREFIX + ":" + TAG_HEADING_REFERENCE);

                if (attributes.getImageColor() != null) {
                    img_color.appendChild(doc.createTextNode(Integer.toHexString(attributes.getImageColor().getRGB())));
                    symbol.appendChild(img_color);
                }
                pnt_default_image.appendChild(doc.createTextNode(Boolean.toString(attributes.isUsePointAsDefaultImage())));
                symbol.appendChild(pnt_default_image);

                if (attributes.getLabelFont() != null) {
                    lbl_font.appendChild(doc.createTextNode(attributes.getLabelFont().toString()));
                    symbol.appendChild(lbl_font);
                }
                if (attributes.getLabelMaterial() != null) {
                    lbl_material.setAttribute(ATTR_AMBIENT, Integer.toHexString(attributes.getLabelMaterial().getAmbient().getRGB()));
                    lbl_material.setAttribute(ATTR_DIFFUSE, Integer.toHexString(attributes.getLabelMaterial().getDiffuse().getRGB()));
                    lbl_material.setAttribute(ATTR_SPECULAR, Integer.toHexString(attributes.getLabelMaterial().getSpecular().getRGB()));
                    lbl_material.setAttribute(ATTR_EMISSION, Integer.toHexString(attributes.getLabelMaterial().getEmission().getRGB()));
                    lbl_material.setAttribute(ATTR_SHININESS, Double.toString(attributes.getLabelMaterial().getShininess()));
                    symbol.appendChild(lbl_material);
                }
                if (attributes.getLineMaterial() != null) {
                    line_material.setAttribute(ATTR_AMBIENT, Integer.toHexString(attributes.getLabelMaterial().getAmbient().getRGB()));
                    line_material.setAttribute(ATTR_DIFFUSE, Integer.toHexString(attributes.getLabelMaterial().getDiffuse().getRGB()));
                    line_material.setAttribute(ATTR_SPECULAR, Integer.toHexString(attributes.getLabelMaterial().getSpecular().getRGB()));
                    line_material.setAttribute(ATTR_EMISSION, Integer.toHexString(attributes.getLabelMaterial().getEmission().getRGB()));
                    line_material.setAttribute(ATTR_SHININESS, Double.toString(attributes.getLabelMaterial().getShininess()));
                    symbol.appendChild(line_material);
                }
                if (attributes.getHeading() != null) {
                    heading.appendChild(doc.createTextNode(attributes.getHeading().toString()));
                    heading_ref.appendChild(doc.createTextNode(attributes.getHeadingReference()));
                    symbol.appendChild(heading);
                    symbol.appendChild(heading_ref);
                }
                mkr.appendChild(symbol);
            }
            return mkr;
        } catch (DOMException ex) {
            logger.log(Level.SEVERE, "exportXml failed! Unabled to export marker ({0}). Reason: {1}", new Object[]{marker.toString(), ex.toString()});
        }
        return null;
    }

}
