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
package com.emxsys.wmt.globe.symbology;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.symbology.Graphic;
import com.emxsys.gis.gml.GmlBuilder;
import com.emxsys.gis.gml.GmlConstants;
import static com.emxsys.gis.gml.GmlConstants.*;
import com.emxsys.util.FilenameUtils;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.NbBundle;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The BasicGraphic.Writer class is responsible for writing out a graphic to an XML document. It can
 * either create an XML file or update an XML document by invoking either the {@code folder()} or
 * the {@code document()} method, respectively. Follows the Fluent interface pattern.
 *
 * Derived classes must implement getTemplate().
 *
 * For example:
 *
 * <pre>
 * To update a Graphic XML document:
 * {@code new Writer()
 *      .document(doc)
 *      .graphic(graphic)
 *      .write();
 * }
 *
 * To create a new Graphic XML document on disk:
 * {@code new Writer()
 *      .folder(folder)
 *      .graphic(graphic)
 *      .write();
 * }
 * </pre>
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({
    "# {0} - Reason",
    "err.cannot.export.graphic=Cannot export graphic. {0}",
})
public abstract class AbstractGraphicWriter implements Graphic.Writer {

    public static final String GRF_PREFIX = "grf";
    public static final String BASIC_GRAPHIC_NS_URI = "http://emxsys.com/worldwind-basicgraphic";
    public static final String BASIC_GRAPHIC_SCHEMA_FILE = "BasicGraphicSchema.xsd";
    public static final String TAG_GRAPHICS = "TacticalGraphicCollection";
    public static final String TAG_GRAPHIC = "Graphic";
    public static final String TAG_NAME = "name";
    public static final String TAG_DECRIPTION = "description";
    public static final String TAG_POSITION = "Position";
    public static final String TAG_POSITIONS = "Positions";
    public static final String TAG_MILSTD2525ID = "milStd2525Id";
    public static final String ATTR_NAME = "Name";
    public static final String ATTR_IDENTIFIER = "Identifier";
    public static final String ATTR_TYPE = "Type";
    public static final String ATTR_FACTORY = "factory";
    public static final String ATTR_MOVABLE = "movable";
    public static final String ATTR_UNIQUE_ID = "UUID";

    private static final Logger logger = Logger.getLogger(AbstractGraphicWriter.class.getName());

    private final BasicGraphic graphic;
    private Document doc;
    private FileObject folder;

    /**
     * Constructs a Writer for the the given Graphic.
     * @param graphic The Graphic to be written to persistent storage.
     */
    public AbstractGraphicWriter(BasicGraphic graphic) {
        this.graphic = graphic;
    }

    /**
     * Creates a Writer that will update the given document with the contents of the given Graphic.
     * @param doc The document to update.
     * @param graphic The graphic to write.
     */
    public AbstractGraphicWriter(Document doc, BasicGraphic graphic) {
        this.doc = doc;
        this.graphic = graphic;
    }

    /**
     * Creates a Writer that will create an XML document on disk with the contents of the given
     * Graphic.
     * @param folder The folder where the XML document will be created.
     * @param graphic The graphic to write.
     */
    public AbstractGraphicWriter(FileObject folder, BasicGraphic graphic) {
        this.graphic = graphic;
        this.folder = folder;
    }

    /**
     * Sets the document to write to. Mutually exclusive with the folder setting.
     * @param doc The document to write to.
     * @return The updated Writer.
     */
    public AbstractGraphicWriter document(Document doc) {
        this.doc = doc;
        this.folder = null;
        return this;
    }

    /**
     * Sets the folder where the new Graphic will be created. Mutually exclusive with the document
     * setting.
     * @param folder The folder where the new Graphic file will be created.
     * @return The updated Writer.
     */
    public AbstractGraphicWriter folder(FileObject folder) {
        this.folder = folder;
        this.doc = null;
        return this;
    }

    public Document getDocument() {
        return doc;
    }

    public BasicGraphic getGraphic() {
        return graphic;
    }

    public FileObject getFolder() {
        return folder;
    }

    /**
     * Writes the contents of a Graphic to a persistent store. Either to the given XML document, or
     * to a new XML file in the given folder.
     * @return Returns the newly created or updated document.
     */
    @Override
    public Document write() {
        if (graphic == null) {
            throw new IllegalArgumentException("AbstractGraphicWriter.write() failed: The graphic object cannot be null");
        }
        if (folder != null) {
            return createDataObject();
        } else if (doc != null) {
            return writeDocument();
        } else {
            throw new IllegalStateException("AbstractGraphicWriter.write() failed: Either the folder or the document must be set.");
        }
    }

    /**
     * Create a new BasicGraphicDataObject (file) from a model Graphic object.
     * @return The XML document from the DataObject
     * @see BasicGraphicTemplateHandler
     */
    protected Document createDataObject() {
        try {
            // findFolder() throws an IllegalArgumentException if not found
            DataFolder dataFolder = DataFolder.findFolder(folder);

            // Get the template file to be updated with the Graphic object properties.
            DataObject template = getTemplate();
            if (template == null) {
                throw new IllegalStateException("getTemplate() returned null.");
            }
            // Ensure the filename is unique -- appends a numeral if not
            String filename = FilenameUtils.getUniqueEncodedFilename(folder, graphic.getName(), template.getPrimaryFile().getExt());

            // Store the Graphic object in a parameters collection 
            // for use in a CreateFromTemplateHandler service provider.
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("model", graphic);

            // Create the graphic file from the registered template and the preceeding "model". 
            // Delegates to BasicGraphicTemplateHandler, which is a CreateFromTemplateHandler service provider.
            XMLDataObject dataObject = (XMLDataObject) template.createFromTemplate(dataFolder, filename, parameters);

            return dataObject.getDocument();

        } catch (Exception exception) {
            logger.log(Level.SEVERE, "createDataObject() failed: {0}", exception.toString());
            return null;
        }
    }

    /**
     * Gets the template file used by createDataObject(). Subclasses must define the template.
     *
     * @return A template file file for new Graphics.
     */
    protected abstract DataObject getTemplate();

    /**
     * Performs a complete rewrite of the graphic XML file via DOM.
     * @return Returns the updated document.
     */
    protected Document writeDocument() {
        // Clear any existing element in prep for saving new graphic data
        final NodeList children = doc.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            doc.removeChild(children.item(i));
        }
        // Create the nodes
        Element root = createGraphicsElement(new GeoSector(graphic.getPosition(), graphic.getPosition()));
        Element feature = doc.createElementNS(GmlConstants.GML_NS_URI, GmlConstants.GML_PREFIX + ":" + GmlConstants.FEATURE_MEMBER_PROPERTY_ELEMENT_NAME);
        Element graphicElement = createGraphicElement();
        if (root != null && feature != null && graphicElement != null) {
            // Assemble the document
            doc.appendChild(root);
            root.appendChild(feature);
            feature.appendChild(graphicElement);
        } else {
            logger.log(Level.SEVERE, "writeDocument unable to export graphic ({0}).", graphic.toString());
        }
        return doc;
    }

    /**
     * Creates an XML Element that represents the Graphic Collection. Created within the
     * BASIC_GRAPHIC_NS_URI.
     * @param extents The geographical extents of the collection.
     * @return A new Element representing the graphic collection.
     */
    protected Element createGraphicsElement(GeoSector extents) {
        try {
            Element element = doc.createElementNS(BASIC_GRAPHIC_NS_URI, GRF_PREFIX + ":" + TAG_GRAPHICS);
            element.setAttribute("xmlns", BASIC_GRAPHIC_NS_URI);
            element.setAttribute("xmlns:gml", GmlConstants.GML_NS_URI);
            element.setAttribute("xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            element.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation", BASIC_GRAPHIC_NS_URI + " " + BASIC_GRAPHIC_SCHEMA_FILE);
            GmlBuilder gmlBuilder = new GmlBuilder(doc, GmlConstants.GML_NS_URI, GmlConstants.GML_PREFIX + ":" + GmlConstants.BOUNDED_BY_PROPERTY_ELEMENT_NAME);
            gmlBuilder.append(extents);
            element.appendChild(gmlBuilder.toElement());
            return element;
        } catch (DOMException ex) {
            logger.log(Level.SEVERE, "exportXml failed! Unabled to export graphic ({0}). Reason: {1}", new Object[]{extents.toString(), ex.toString()});
        }
        return null;
    }

    /**
     * Creates an XML Element that represents the Graphic. Created within the BASIC_GRAPHIC_NS_URI.
     * Encodes the Graphic to an XML Element, e.g.,
     * <pre>{@code
     * <tagName Identifier="..." Name="..." Type="..." UUID="..."/>
     *      <Positions>
     *          <Position/>
     *          <Position/>
     *          ...
     *      </Positions>
     * </tagName>
     * }</pre>
     * @return A new Element representing the graphic.
     */
    protected Element createGraphicElement() {
        try {
            Element grf = doc.createElementNS(BASIC_GRAPHIC_NS_URI, TAG_GRAPHIC);
            grf.setAttribute(GmlConstants.FID_ATTR_NAME, GRF_PREFIX + "-" + graphic.getUniqueID());
            grf.setAttribute(ATTR_FACTORY, graphic.getFactoryClass().getName());
            grf.setAttribute(ATTR_MOVABLE, Boolean.toString(graphic.isMovable()));
            grf.setAttribute(ATTR_TYPE, graphic.getType());

            Element desc = doc.createElementNS(GML_NS_URI, GML_PREFIX + ":" + GmlConstants.DESCRIPTION_PROPERTY_ELEMENT_NAME);
            grf.appendChild(desc);

            Element name = doc.createElementNS(GML_NS_URI, GML_PREFIX + ":" + GmlConstants.NAME_PROPERTY_ELEMENT_NAME);
            name.appendChild(doc.createTextNode(graphic.getName()));
            grf.appendChild(name);

            GmlBuilder gmlBuilder = new GmlBuilder(doc, GML_NS_URI, GML_PREFIX + ":" + GmlConstants.POINT_PROPERTY_ELEMENT_NAME);
            gmlBuilder.append(graphic.getPosition());
            grf.appendChild(gmlBuilder.toElement());

            Element milstd2525_id = doc.createElementNS(BASIC_GRAPHIC_NS_URI, TAG_MILSTD2525ID);
            milstd2525_id.appendChild(doc.createTextNode(graphic.getIdentifier()));
            grf.appendChild(milstd2525_id);

            grf.appendChild(positionsToElement(doc, graphic.getPositions()));

            return grf;

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "createTacticalGraphicElement failed! Unabled to export graphic ({0}). Reason: {1}", new Object[]{
                graphic.toString(), ex.toString()
            });
        }
        return null;
    }

    private static synchronized Element positionsToElement(final Document doc, List<Coord3D> positions) {
        Element element = doc.createElementNS(BASIC_GRAPHIC_NS_URI, TAG_POSITIONS);
        for (Coord3D position : positions) {
            if (position.isMissing()) {
                logger.log(Level.WARNING, "Storing an 'invalid' position");
            }
            element.appendChild(GeoCoord3D.toXmlElement(doc, TAG_POSITION, position));
        }
        return element;
    }

}
