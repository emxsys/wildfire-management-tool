/*
 * Copyright (c) 2015, Bruce Schubert <bruce@emxsys.com>
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
import com.emxsys.gis.api.symbology.Graphic;
import com.emxsys.gis.gml.GmlConstants;
import com.emxsys.gis.gml.GmlParser;
import static com.emxsys.wmt.globe.symbology.AbstractGraphicWriter.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The AbstractGraphicBuilder is responsible for creating a BasicGraphic from a set of parameters
 * that are set via the the Fluent interface pattern. 
 * Concrete classes must implement the build() method.
 * Examples:
 *
 * <pre>
 * To create a new Graphic from given parameters:
 * {@code new Builder()
 *      .coordinate(new GeoCoord3D())
 *      .name("Graphic")
 *      .build();
 * }
 *
 * To create a new Graphic from an XML document:
 * {@code new Builder()
 *      .document(doc)
 *      .build();
 * }
 *
 * To create a new Graphic from an XML document with a new name and coordinate:
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
    "err.cannot.import.graphic=Cannot import Graphic. {0}",
    "err.graphic.document.is.null=Graphic document argument cannot be null.",
    "# {0} - document",
    "err.document.missing.graphic={0} is does not have a Graphic element.",
    "# {0} - document",
    "err.document.has.extra.graphics={0} contains more than one Graphic element. Only one Graphic will be processed.",})

public abstract class AbstractGraphicBuilder implements Graphic.Builder {

    private static final Logger logger = Logger.getLogger(AbstractGraphicBuilder.class.getName());
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
    public AbstractGraphicBuilder() {
    }

    /**
     * Minimal constructor for a Builder that uses an XML document for the Graphic parameters.
     * @param document The document to read.
     */
    public AbstractGraphicBuilder(Document document) {
        this.doc = document;
    }

    public AbstractGraphicBuilder document(Document document) {
        this.doc = document;
        return this;
    }

    public AbstractGraphicBuilder coordinate(Coord3D coord) {
        this.coord = coord;
        return this;
    }

    public AbstractGraphicBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Builds a new BasicGraphic from the established parameters.
     * <pre>
     * Suggested build() method body template: 
     * {@code 
     *    {
     *        BasicGraphic Graphic = new BasicGraphic();
     *        if (doc != null) {
     *            Graphic = initializeFromXml(Graphic);
     *        }
     *        return initializeFromParameters(Graphic);
     *    }
     * }</pre>
     * @return a new BasicGraphic instance.
     */
    @Override
    public abstract Graphic build();

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
     * Initializes the supplied Graphic from the established parameters. Allows the parameters to
     * override the settings established by an XML document. Subclasses should call or override.
     * @param graphic The Graphic to update.
     * @return The updated Graphic.
     */
    protected BasicGraphic initializeFromParameters(BasicGraphic graphic) {
        if (coord != null) {
            if (doc != null) {
                logger.log(Level.INFO, "Overriding the position set by XML document for {0}.", graphic.getName());
            }
            graphic.setPosition(coord);
        }
        if (name != null) {
            if (doc != null) {
                logger.log(Level.INFO, "Overriding the name set by XML document for {0}.", graphic.getName());
            }
            graphic.setName(name);
        }
        return graphic;
    }

    /**
     * Initializes the supplied Graphic from the Builder's XML document.
     * @param graphic The BasicGraphic (or derived) instance to be initialized.
     * @return The initialized Graphic; throws a RuntimeException on error.
     * @throws RuntimeException
     */
    protected BasicGraphic initializeFromXml(BasicGraphic graphic) {
        this.xpath = XPathFactory.newInstance().newXPath();
        this.xpath.setNamespaceContext(SymbologySupport.getNamespaceContext());
        NodeList nodeList;
        try {
            // Get the Graphic node, there should be [0-1]
            nodeList = (NodeList) xpath.evaluate("//" + GRF_PREFIX + ":" + TAG_GRAPHIC, doc, XPathConstants.NODESET);
            if (nodeList.getLength() == 0) {
                throw new IllegalStateException(Bundle.err_document_missing_graphic(doc.getDocumentURI()));
            } else if (nodeList.getLength() > 1) {
                logger.warning(Bundle.err_document_has_extra_graphics(doc.getDocumentURI()));
            }
            // Set the name
            Element symElement = (Element) nodeList.item(0);
            graphic.setName(xpath.evaluate(GmlConstants.GML_PREFIX + ":" + GmlConstants.NAME_PROPERTY_ELEMENT_NAME, symElement));
            
            // Get the position
            Element pntElem = (Element) xpath.evaluate(GmlConstants.GML_PREFIX + ":" + GmlConstants.POINT_PROPERTY_ELEMENT_NAME, symElement, XPathConstants.NODE);
            if (pntElem == null) { // pointProperty tag not found--look for depreciated Position tag.
                pntElem = (Element) xpath.evaluate(GRF_PREFIX + ":" +TAG_POSITION, symElement, XPathConstants.NODE); // depreciated tag
            }
            Coord3D coordinate = GmlParser.parsePosition(pntElem);

            // Get the MILSTD2525C ID 
            String identifier = xpath.evaluate(GRF_PREFIX + ":" + TAG_MILSTD2525ID, symElement);
            if (identifier == null || identifier.isEmpty())
            {
                String msg = TAG_MILSTD2525ID + " attribute is missing or empty.";
                logger.severe(msg);
                // Use an Unknown Ground Track graphic as the default for missing IDs
                identifier = "SUGP-----------";
            }
            
            // Assign the tactical graphic
            graphic.makeTacticalGraphic(identifier, graphic.getPositions());
            
            // Set the unique ID
            //String GraphicID = symElement.getAttribute(GmlConstants.FID_ATTR_NAME);
            //graphic.setUniqueID(GraphicID);
            
            String movable = symElement.getAttribute(ATTR_MOVABLE);
            graphic.setMovable(movable.isEmpty() ? true : Boolean.parseBoolean(movable));
            
            return graphic;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "initializeFromXml encountered {0}", ex.toString());
            throw new RuntimeException("Builder.initializeFromXml() failed!");
        }
    }


}
