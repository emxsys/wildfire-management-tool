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

import com.emxsys.wmt.gis.api.GeoSector;
import com.emxsys.wmt.gis.api.marker.Marker;
import com.emxsys.wmt.gis.gml.GmlBuilder;
import com.emxsys.wmt.gis.gml.GmlConstants;
import com.emxsys.wmt.util.FilenameUtils;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * BasicMarker.Writer class. Follows the Fluent interface pattern. E.g.,
 * <pre>
 * To update a Marker XML document:
 * {@code new Writer()
 *      .document(doc)
 *      .marker(marker)
 *      .write();
 * }
 * To create a new Marker XML document:
 * {@code new Writer()
 *      .folder(folder)
 *      .marker(marker)
 *      .write();
 * } *
 * </pre>
 */
@NbBundle.Messages({
    "# {0} - reason",
    "err.cannot.export.marker=Cannot export marker. {0}",})
public class BasicMarkerWriter implements Marker.Writer {

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
    // See package-info.java for the declaration of the BasicMarkerTemplate
    private static final String TEMPLATE_CONFIG_FILE = "Templates/Marker/BasicMarkerTemplate.xml";
    private static DataObject templateFile;
    private static final Logger logger = Logger.getLogger(BasicMarkerWriter.class.getName());
    private Document doc;
    private BasicMarker marker;
    private FileObject folder;

    public BasicMarkerWriter() {
    }

    /**
     * Sets the document to write to. Mutually exclusive with the folder setting.
     * @param doc The document to write to.
     * @return The updated Writer.
     */
    public BasicMarkerWriter document(Document doc) {
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
    public BasicMarkerWriter folder(FileObject folder) {
        this.folder = folder;
        this.doc = null;
        return this;
    }

    /**
     * Sets the Marker to be written to a persistent store. Mandatory.
     * @param marker The Marker to write.
     * @return The updated Writer.
     */
    public BasicMarkerWriter marker(BasicMarker marker) {
        this.marker = marker;
        return this;
    }

    /**
     * Writes the contents of a Marker to a persistent store. Either to the given XML document, or
     * to a new XML file in the given folder.
     */
    @Override
    public void write() {
        if (marker == null) {
            throw new IllegalArgumentException("BasicMarker.Writer.write() failed: The marker object cannot be null");
        }
        if (folder != null) {
            createDataObject();
        } else if (doc != null) {
            writeDocument();
        } else {
            throw new IllegalStateException("BasicMarker.Writer.write() failed: Either the folder or the document must be set.");
        }
    }

    /**
     * Create a new BasicMarkerDataObject (file) from a model Marker object.
     * @see BasicMarkerTemplateHandler
     */
    protected void createDataObject() {
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
            template.createFromTemplate(dataFolder, filename, parameters);
        } catch (IllegalStateException | IllegalArgumentException | IOException exception) {
            logger.log(Level.SEVERE, "createDataObject() failed: {0}", exception.toString());
        }
    }

    /**
     * Gets the template file used by createDataObject(). Allows subclasses to define the template.
     *
     * @return A template file file for new Markers.
     */
    protected DataObject getTemplate() {
        if (templateFile == null) {
            try {
                templateFile = DataObject.find(FileUtil.getConfigFile(TEMPLATE_CONFIG_FILE));
            } catch (DataObjectNotFoundException ex) {
                logger.log(Level.SEVERE, "BasicMarker.MarkerFactory.getTemplate() cannot find: {0}", TEMPLATE_CONFIG_FILE);
            }
        }
        return templateFile;
    }

    /**
     * Performs a complete rewrite of the marker XML file via DOM.
     */
    protected void writeDocument() {
        // Clear any existing element in prep for saving new marker data
        final NodeList children = doc.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            doc.removeChild(children.item(i));
        }
        // Write the data
        Element root = createMarkersElement(new GeoSector(marker.getPosition(), marker.getPosition()));
        Element feature = doc.createElementNS(GmlConstants.GML_NS_URI, GmlConstants.GML_PREFIX + ":" + GmlConstants.FEATURE_MEMBER_PROPERTY_ELEMENT_NAME);
        Element markerElement = createMarkerElement();
        if (root != null && feature != null && markerElement != null) {
            doc.appendChild(root);
            root.appendChild(feature);
            feature.appendChild(markerElement);
        } else {
            logger.log(Level.SEVERE, "writeDocument unable to export marker ({0}).", marker.toString());
        }
    }

    /**
     * Creates an XML Element that represents the Marker Collection. Created within the
     * BASIC_MARKER_NS_URI.
     * @param extents The geographical extents of the collection.
     * @return A new Element representing the marker collection.
     */
    protected Element createMarkersElement(GeoSector extents) {
        try {
            Element element = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_MARKERS);
            element.setAttribute("xmlns", BASIC_MARKER_NS_URI);
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
            Element mkr = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_MARKER);
            mkr.setAttribute(GmlConstants.FID_ATTR_NAME, "mkr-" + Long.toString(marker.getUniqueID()));
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
                Element symbol = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_SYMBOL);
                Element img_url = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_IMAGE_URL);
                Element img_off_x = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_IMAGE_OFFSET_X);
                Element img_off_y = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_IMAGE_OFFSET_Y);
                Element lbl_off_x = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_LABEL_OFFSET_X);
                Element lbl_off_y = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_LABEL_OFFSET_Y);
                Element img_scale = doc.createElementNS(BASIC_MARKER_NS_URI, TAG_IMAGE_SCALE);
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
                mkr.appendChild(symbol);
            }
            return mkr;
        } catch (DOMException ex) {
            logger.log(Level.SEVERE, "exportXml failed! Unabled to export marker ({0}). Reason: {1}", new Object[]{marker.toString(), ex.toString()});
        }
        return null;
    }

}
