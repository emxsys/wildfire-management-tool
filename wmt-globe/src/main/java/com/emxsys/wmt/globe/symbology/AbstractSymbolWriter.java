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

import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.symbology.Symbol;
import com.emxsys.gis.gml.GmlBuilder;
import com.emxsys.gis.gml.GmlConstants;
import static com.emxsys.gis.gml.GmlConstants.*;
import com.emxsys.util.FilenameUtils;
import static com.emxsys.wmt.globe.symbology.AbstractSymbolWriter.*;
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
 * The BasicSymbol.Writer class is responsible for writing out a symbol to an XML document. It can
 * either create an XML file or update an XML document by invoking either the {@code folder()} or
 * the {@code document()} method, respectively. Follows the Fluent interface pattern. For example:
 *
 * <pre>
 * To update a Symbol XML document:
 * {@code new Writer()
 *      .document(doc)
 *      .symbol(symbol)
 *      .write();
 * }
 *
 * To create a new Symbol XML document on disk:
 * {@code new Writer()
 *      .folder(folder)
 *      .symbol(symbol)
 *      .write();
 * }
 * </pre>
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({
    "# {0} - reason",
    "err.cannot.export.symbol=Cannot export symbol. {0}",})
public abstract class AbstractSymbolWriter implements Symbol.Writer {

    public static final String SMB_PREFIX = "smb";
    public static final String BASIC_SYMBOL_NS_URI = "http://emxsys.com/worldwind-basicsymbol";
    public static final String BASIC_SYMBOL_SCHEMA_FILE = "BasicSymbolSchema.xsd";
    public static final String TAG_SYMBOLS = "TacticalSymbolCollection";
    public static final String TAG_TACTICAL_SYMBOL = "TacticalSymbol";
    public static final String TAG_NAME = "name";
    public static final String TAG_DECRIPTION = "description";
    public static final String TAG_POSITION = "Position";
    public static final String TAG_MILSTD2525ID = "milStd2525Id";
    public static final String ATTR_FACTORY = "factory";
    public static final String ATTR_MOVABLE = "movable";

    private static final Logger logger = Logger.getLogger(AbstractSymbolWriter.class.getName());

    private final BasicSymbol symbol;
    private Document doc;
    private FileObject folder;

    /**
     * Constructs a Writer for the the given Symbol.
     * @param symbol The Symbol to be written to persistent storage.
     */
    public AbstractSymbolWriter(BasicSymbol symbol) {
        this.symbol = symbol;
    }

    /**
     * Creates a Writer that will update the given document with the contents of the given Symbol.
     * @param doc The document to update.
     * @param symbol The symbol to write.
     */
    public AbstractSymbolWriter(Document doc, BasicSymbol symbol) {
        this.doc = doc;
        this.symbol = symbol;
    }

    /**
     * Creates a Writer that will create an XML document on disk with the contents of the given
     * Symbol.
     * @param folder The folder where the XML document will be created.
     * @param symbol The symbol to write.
     */
    public AbstractSymbolWriter(FileObject folder, BasicSymbol symbol) {
        this.symbol = symbol;
        this.folder = folder;
    }

    /**
     * Sets the document to write to. Mutually exclusive with the folder setting.
     * @param doc The document to write to.
     * @return The updated Writer.
     */
    public AbstractSymbolWriter document(Document doc) {
        this.doc = doc;
        this.folder = null;
        return this;
    }

    /**
     * Sets the folder where the new Symbol will be created. Mutually exclusive with the document
     * setting.
     * @param folder The folder where the new Symbol file will be created.
     * @return The updated Writer.
     */
    public AbstractSymbolWriter folder(FileObject folder) {
        this.folder = folder;
        this.doc = null;
        return this;
    }

    public Document getDocument() {
        return doc;
    }

    public BasicSymbol getSymbol() {
        return symbol;
    }

    public FileObject getFolder() {
        return folder;
    }

    /**
     * Writes the contents of a Symbol to a persistent store. Either to the given XML document, or
     * to a new XML file in the given folder.
     * @return Returns the newly created or updated document.
     */
    @Override
    public Document write() {
        if (symbol == null) {
            throw new IllegalArgumentException("AbstractSymbolWriter.write() failed: The symbol object cannot be null");
        }
        if (folder != null) {
            return createDataObject();
        } else if (doc != null) {
            return writeDocument();
        } else {
            throw new IllegalStateException("AbstractSymbolWriter.write() failed: Either the folder or the document must be set.");
        }
    }

    /**
     * Create a new BasicSymbolDataObject (file) from a model Symbol object.
     * @return The XML document from the DataObject
     * @see BasicSymbolTemplateHandler
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
            String filename = FilenameUtils.getUniqueEncodedFilename(folder, symbol.getName(), template.getPrimaryFile().getExt());

            // Get the registered templateFile (could be from a subclass).
            
            // Create the symbol file from our templateFile. Delegates to BasicSymbolTemplateHandler,
            // which is a CreateFromTemplateHandler service provider.
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("model", symbol);
            BasicSymbolDataObject dataObject = (BasicSymbolDataObject) template.createFromTemplate(dataFolder, filename, parameters);

            return dataObject.getDocument();

        } catch (IllegalStateException | IllegalArgumentException | IOException | SAXException exception) {
            logger.log(Level.SEVERE, "createDataObject() failed: {0}", exception.toString());
            return null;
        }
    }

    /**
     * Gets the template file used by createDataObject(). Subclasses must define the template.
     *
     * @return A template file file for new Symbols.
     */
    protected abstract DataObject getTemplate();

    /**
     * Performs a complete rewrite of the symbol XML file via DOM.
     * @return Returns the updated document.
     */
    protected Document writeDocument() {
        // Clear any existing element in prep for saving new symbol data
        final NodeList children = doc.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            doc.removeChild(children.item(i));
        }
        // Create the nodes
        Element root = createSymbolsElement(new GeoSector(symbol.getPosition(), symbol.getPosition()));
        Element feature = doc.createElementNS(GmlConstants.GML_NS_URI, GmlConstants.GML_PREFIX + ":" + GmlConstants.FEATURE_MEMBER_PROPERTY_ELEMENT_NAME);
        Element symbolElement = createSymbolElement();
        if (root != null && feature != null && symbolElement != null) {
            // Assemble the document
            doc.appendChild(root);
            root.appendChild(feature);
            feature.appendChild(symbolElement);
        } else {
            logger.log(Level.SEVERE, "writeDocument unable to export symbol ({0}).", symbol.toString());
        }
        return doc;
    }

    /**
     * Creates an XML Element that represents the Symbol Collection. Created within the
     * BASIC_SYMBOL_NS_URI.
     * @param extents The geographical extents of the collection.
     * @return A new Element representing the symbol collection.
     */
    protected Element createSymbolsElement(GeoSector extents) {
        try {
            Element element = doc.createElementNS(BASIC_SYMBOL_NS_URI, SMB_PREFIX + ":" + TAG_SYMBOLS);
            element.setAttribute("xmlns", BASIC_SYMBOL_NS_URI);
            element.setAttribute("xmlns:gml", GmlConstants.GML_NS_URI);
            element.setAttribute("xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            element.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation", BASIC_SYMBOL_NS_URI + " " + BASIC_SYMBOL_SCHEMA_FILE);
            GmlBuilder gmlBuilder = new GmlBuilder(doc, GmlConstants.GML_NS_URI, GmlConstants.GML_PREFIX + ":" + GmlConstants.BOUNDED_BY_PROPERTY_ELEMENT_NAME);
            gmlBuilder.append(extents);
            element.appendChild(gmlBuilder.toElement());
            return element;
        } catch (DOMException ex) {
            logger.log(Level.SEVERE, "exportXml failed! Unabled to export symbol ({0}). Reason: {1}", new Object[]{extents.toString(), ex.toString()});
        }
        return null;
    }

    /**
     * Creates an XML Element that represents the Symbol. Created within the BASIC_SYMBOL_NS_URI.
     * @return A new Element representing the symbol.
     */
    protected Element createSymbolElement() {
        try
        {
            Element smb = doc.createElementNS(BASIC_SYMBOL_NS_URI, TAG_TACTICAL_SYMBOL);
            smb.setAttribute(GmlConstants.FID_ATTR_NAME, "smb-" + symbol.getUniqueID());
            smb.setAttribute(ATTR_FACTORY, symbol.getFactoryClass().getName());
            smb.setAttribute(ATTR_MOVABLE, Boolean.toString(symbol.isMovable()));

            Element desc = doc.createElementNS(GML_NS_URI, GML_PREFIX + ":" + GmlConstants.DESCRIPTION_PROPERTY_ELEMENT_NAME);
            //desc.appendChild(doc.createTextNode(symbol.getDescription()));
            smb.appendChild(desc);

            Element name = doc.createElementNS(GML_NS_URI, GML_PREFIX + ":" + GmlConstants.NAME_PROPERTY_ELEMENT_NAME);
            name.appendChild(doc.createTextNode(symbol.getName()));
            smb.appendChild(name);

            GmlBuilder gmlBuilder = new GmlBuilder(doc, GML_NS_URI, GML_PREFIX + ":" + GmlConstants.POINT_PROPERTY_ELEMENT_NAME);
            gmlBuilder.append(symbol.getPosition());
            smb.appendChild(gmlBuilder.toElement());

            Element milstd2525_id = doc.createElementNS(BASIC_SYMBOL_NS_URI, TAG_MILSTD2525ID);
            milstd2525_id.appendChild(doc.createTextNode(symbol.getIdentifier()));
            smb.appendChild(milstd2525_id);

            return smb;
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "createTacticalSymbolElement failed! Unabled to export symbol ({0}). Reason: {1}", new Object[]
            {
                symbol.toString(), ex.toString()
            });
        }
        return null;
    }

}
