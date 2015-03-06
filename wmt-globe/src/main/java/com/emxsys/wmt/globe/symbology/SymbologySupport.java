/*
 * Copyright (c) 2012-2015, Bruce Schubert <bruce@emxsys.com>
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

import com.emxsys.gis.api.symbology.Graphic;
import com.emxsys.gis.api.symbology.GraphicManager;
import com.emxsys.gis.api.symbology.Symbol;
import com.emxsys.gis.api.symbology.SymbolManager;
import com.emxsys.gis.gml.GmlConstants;
import static com.emxsys.wmt.globe.symbology.AbstractGraphicWriter.*;
import static com.emxsys.wmt.globe.symbology.BasicSymbolWriter.*;
import com.emxsys.util.TimeUtil;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Bruce Schubert
 */
public class SymbologySupport {

    private static final Logger logger = Logger.getLogger(SymbologySupport.class.getName());
    private static Schema schema;
    private static NamespaceContext namespaceContext;

    /**
     * Gets a factory object from the class embedded in the Symbol element.
     * @param document The document containing the Symbol element.
     * @return A new Symbol.Builder instance; throws on error.
     */
    public static Graphic.Builder getGraphicBuilder(Document document) {

        String clazz = null;
        try {
            NodeList list = document.getElementsByTagName("Graphic");
            if (list.getLength() == 0) {
                list = document.getElementsByTagName(GRF_PREFIX + ":Graphic");
            }
            if (list.getLength() == 0) {
                throw new IllegalArgumentException("Document does not contain a Graphic or grf:Graphic element.");
            }
            // Construct a factory object.
            // Use the Graphic.Builder(Document document) interface.
            clazz = ((Element) list.item(0)).getAttribute("factory");
            return (Graphic.Builder) Class.forName(clazz)
                    .getConstructor(Document.class)
                    .newInstance(document);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Cannot create Graphic.Factory: \"{0}\". Reason: {1}",
                    new Object[]{clazz, ex.toString()});
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets a factory object from the class embedded in the Symbol element.
     * @param document The document containing the Symbol element.
     * @return A new Symbol.Builder instance; throws on error.
     */
    public static Symbol.Builder getSymbolBuilder(Document document) {

        String clazz = null;
        try {
            NodeList list = document.getElementsByTagName(TAG_TACTICAL_SYMBOL);
            if (list.getLength() == 0) {
                list = document.getElementsByTagName(SMB_PREFIX + ":" + TAG_TACTICAL_SYMBOL);
            }
            if (list.getLength() == 0) {
                throw new IllegalArgumentException("Document does not contain a " + TAG_TACTICAL_SYMBOL
                        + " or " + SMB_PREFIX + ":" + TAG_TACTICAL_SYMBOL + " element.");
            }
            // Construct a factory object.
            // Use the Symbol.Builder(Document document) interface.
            clazz = ((Element) list.item(0)).getAttribute(BasicSymbolWriter.ATTR_FACTORY);
            return (Symbol.Builder) Class.forName(clazz)
                    .getConstructor(Document.class)
                    .newInstance(document);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Cannot create Symbol.Factory: \"{0}\". Reason: {1}",
                    new Object[]{clazz, ex.toString()});
            throw new RuntimeException(ex);
        }
    }

    /**
     * Called by Builder clients (e.g., actions) to get the folder for new Symbols.
     * @return A FileObject for the "symbology" folder; returns null if not found.
     */
    public static FileObject getFolderFromCurrentProject() {
        Project currentProject = Utilities.actionsGlobalContext().lookup(Project.class);
        if (currentProject != null) {
            SymbolManager symMgr = currentProject.getLookup().lookup(SymbolManager.class);
            if (symMgr != null) {
                return symMgr.getFolder();
            }
            GraphicManager grfMgr = currentProject.getLookup().lookup(GraphicManager.class);
            if (grfMgr != null) {
                return grfMgr.getFolder();
            }
        }
        return null;
    }

    /**
     * Gets a BasicSymbolSchema.xsd file resource.
     * @param version The schema version, e.g., 1.0, 2.0 or later.
     * @return A FileObject for the BASIC_Symbol_SCHEMA_FILE resource.
     * @see URLMapper#findFileObject(java.net.URL)
     */
    public static FileObject getLocalSchemaFile(String version) {
        // Get a file object from a jar file entry (URL).
        URL resource = BasicSymbolWriter.class.getResource("schemas/" + version + "/" + BASIC_SYMBOL_SCHEMA_FILE);
        return URLMapper.findFileObject(resource);
    }

    /**
     * Loads the BasicSymbolSchema.xsd schema. Used in Unit tests.
     * @return Schema representing {@code BasicSymbolSchema.xsd}.
     */
    static Schema getSymbolSchema(String version) {
        if (schema == null) {
            SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL schemaUrl = BasicSymbolWriter.class.getResource("schemas/" + version + "/" + BASIC_SYMBOL_SCHEMA_FILE);
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

    /**
     * Loads the BasicSymbolSchema.xsd schema. Used in Unit tests.
     * @return Schema representing {@code BasicSymbolSchema.xsd}.
     */
    static Schema getGraphicSchema(String version) {
        if (schema == null) {
            SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL schemaUrl = BasicSymbolWriter.class.getResource("schemas/" + version + "/" + BASIC_GRAPHIC_SCHEMA_FILE);
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

    /**
     * @return An XPath NamespaceContext for BasicSymbols
     */
    public static NamespaceContext getNamespaceContext() {
        if (namespaceContext == null) {
            namespaceContext = new NamespaceContext() {
                @Override
                public Iterator getPrefixes(String namespaceURI) {
                    throw new UnsupportedOperationException("getPrefixes");
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    throw new UnsupportedOperationException("getPrefix");
                }

                @Override
                public String getNamespaceURI(String prefix) {
                    // xmlns="http://emxsys.com/worldwind-basicsymbol"
                    // xmlns:gml="http://www.opengis.net/gml"
                    // xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    // xsi:schemaLocation="http://emxsys.com/worldwind-basicSymbol BasicSymbolSchema.xsd"
                    switch (prefix) {
                        case SMB_PREFIX:
                            return BASIC_SYMBOL_NS_URI;
                        case GRF_PREFIX:
                            return BASIC_GRAPHIC_NS_URI;
                        case XMLConstants.DEFAULT_NS_PREFIX:
                            return XMLConstants.DEFAULT_NS_PREFIX;
                        case XMLConstants.XML_NS_PREFIX:
                            return XMLConstants.XML_NS_URI;
                        case GmlConstants.GML_PREFIX:
                            return GmlConstants.GML_NS_URI;
                        default:
                            throw new IllegalStateException(prefix);
                    }
                }

            };
        }
        return namespaceContext;
    }
}
