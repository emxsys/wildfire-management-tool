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
import com.emxsys.gis.api.symbology.Symbol;
import com.emxsys.gis.gml.GmlConstants;
import com.emxsys.gis.gml.GmlParser;
import static com.emxsys.wmt.globe.symbology.BasicSymbolWriter.*;
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
 * The BasicSymbolBuilder is responsible for creating a BasicSymbol from a set of parameters that
 * are set via the the Fluent interface pattern. Concrete classes must implement the build() method.
 * Examples:
 *
 * <pre>
 * To create a new Symbol from given parameters:
 * {@code new Builder()
 *      .coordinate(new GeoCoord3D())
 *      .symbolId("ehipch---------")
 *      .name("Symbol")
 *      .build();
 * }
 *
 * To create a new Symbol from an XML document:
 * {@code new Builder()
 *      .document(doc)
 *      .build();
 * }
 *
 * To create a new Symbol from an XML document with a new name and coordinate:
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
    "err.cannot.import.symbol=Cannot import Symbol. {0}",
    "err.symbol.document.is.null=Symbol document argument cannot be null.",
    "# {0} - document",
    "err.document.missing.symbol={0} is does not have a Symbol element.",
    "# {0} - document",
    "err.document.has.extra.symbols={0} contains more than one Symbol element. Only one Symbol will be processed.",})

public class BasicSymbolBuilder implements Symbol.Builder {

    private static final Logger logger = Logger.getLogger(BasicSymbolBuilder.class.getName());
    private Document doc;
    private XPath xpath;
    private Coord3D coord;
    private String symbolId;
    private String name;

    static {
        logger.setLevel(Level.FINE);
    }

    /**
     * Basic constructor.
     */
    public BasicSymbolBuilder() {
    }

    /**
     * Minimal constructor for a Builder that uses an XML document for the Symbol parameters.
     * @param document The document to read.
     */
    public BasicSymbolBuilder(Document document) {
        this.doc = document;
    }

    public BasicSymbolBuilder document(Document document) {
        this.doc = document;
        return this;
    }

    public BasicSymbolBuilder coordinate(Coord3D coord) {
        this.coord = coord;
        return this;
    }

    public BasicSymbolBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BasicSymbolBuilder symbolId(String symbolId) {
        this.symbolId = symbolId;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public Coord3D getCoordinate() {
        return coord;
    }

    public Document getDocument() {
        return doc;
    }

    /**
     * Builds a new BasicSymbol from the established parameters.
     * <pre>
     * Suggested build() method body template:
     * {@code
     *    {
     *        BasicSymbol Symbol = new BasicSymbol();
     *        if (doc != null) {
     *            Symbol = initializeFromXml(Symbol);
     *        }
     *        return initializeFromParameters(Symbol);
     *    }
     * }</pre>
     * @return a new BasicSymbol instance.
     */
    @Override
    public Symbol build() {
        BasicSymbol symbol = new BasicSymbol();

        // Initialize from an XML document, if provided.
        if (getDocument() != null) {
            symbol = initializeFromXml(symbol);
        }

        // Now, initialize from the builder parameters, if provided
        // (it's permissible to override the XML parameters)
        return initializeFromParameters(symbol);
    }

    /**
     * Initializes the supplied Symbol from the established Builder parameters. Allows the
     * parameters to override the settings established by an XML document. Subclasses should call or
     * override.
     * @param symbol The Symbol to update.
     * @return The updated Symbol.
     */
    protected BasicSymbol initializeFromParameters(BasicSymbol symbol) {
        if (coord != null && symbolId != null) {
            if (doc != null) {
                logger.log(Level.INFO, "Overriding the symbol and position set by XML document for {0}.", symbol.getName());
            }
            symbol.assignTacticalSymbol(symbolId, coord);
        } else if (coord != null) {
            if (doc != null) {
                logger.log(Level.INFO, "Overriding the position set by XML document for {0}.", symbol.getName());
            }
            symbol.setPosition(coord);
        } else if (symbolId != null && !symbolId.isEmpty()) {
            if (doc != null) {
                logger.log(Level.INFO, "Overriding the symbol set by XML document for {0}.", symbol.getName());
            }
            symbol.assignTacticalSymbol(symbolId, symbol.getPosition());
        }
        if (name != null) {
            if (doc != null) {
                logger.log(Level.INFO, "Overriding the name set by XML document for {0}.", symbol.getName());
            }
            symbol.setName(name);
        }
        return symbol;
    }

    /**
     * Initializes the supplied Symbol from the Builder's XML document.
     * @param symbol The BasicSymbol (or derived) instance to be initialized.
     * @return The initialized Symbol; throws a RuntimeException on error.
     * @throws RuntimeException
     */
    protected BasicSymbol initializeFromXml(BasicSymbol symbol) {
        this.xpath = XPathFactory.newInstance().newXPath();
        this.xpath.setNamespaceContext(SymbologySupport.getNamespaceContext());
        NodeList nodeList;
        try {
            // Get the Symbol node, there should be [0-1]
            nodeList = (NodeList) xpath.evaluate("//" + SMB_PREFIX + ":" + TAG_TACTICAL_SYMBOL, doc, XPathConstants.NODESET);
            if (nodeList.getLength() == 0) {
                throw new IllegalStateException(Bundle.err_document_missing_symbol(doc.getDocumentURI()));
            } else if (nodeList.getLength() > 1) {
                logger.warning(Bundle.err_document_has_extra_symbols(doc.getDocumentURI()));
            }
            Element symElement = (Element) nodeList.item(0);

            // First, we must build the tactical symbol implementation. The symbol implementation
            // is a prerequisite to updating any other symbol properties.
            //  1) Read the MILSTD2525C ID 
            String identifier = xpath.evaluate(SMB_PREFIX + ":" + TAG_MILSTD2525ID, symElement);
            if (identifier == null || identifier.isEmpty()) {
                String msg = TAG_MILSTD2525ID + " attribute is missing or empty.";
                logger.severe(msg);
                // Use an Unknown symbol as the default for missing IDs
                //identifier = "SUGP-----------"; // Unknown/Ground Track (empty symbol)
                identifier = "SUZP-----------"; // Unknown/Unknown (question mark)
            }
            //  2) Read the position
            Element pntElem = (Element) xpath.evaluate(GmlConstants.GML_PREFIX + ":" + GmlConstants.POINT_PROPERTY_ELEMENT_NAME, symElement, XPathConstants.NODE);
            if (pntElem == null) { // pointProperty tag not found--look for depreciated Position tag.
                pntElem = (Element) xpath.evaluate(SMB_PREFIX + ":" + TAG_POSITION, symElement, XPathConstants.NODE); // depreciated tag
            }
            Coord3D coordinate = GmlParser.parsePosition(pntElem);
            //  3) Create the implementation: assign the tactical symbol at the given position.
            symbol.assignTacticalSymbol(identifier, coordinate);

            // Set the name property
            symbol.setName(xpath.evaluate(GmlConstants.GML_PREFIX + ":" + GmlConstants.NAME_PROPERTY_ELEMENT_NAME, symElement));

            // Set the unique ID
            //String SymbolID = symElement.getAttribute(GmlConstants.FID_ATTR_NAME);
            //symbol.setUniqueID(SymbolID);
            String movable = symElement.getAttribute(ATTR_MOVABLE);
            symbol.setMovable(movable.isEmpty() ? true : Boolean.parseBoolean(movable));

            return symbol;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "initializeFromXml encountered {0}", ex.toString());
            throw new RuntimeException("Builder.initializeFromXml() failed!");
        }
    }

}
