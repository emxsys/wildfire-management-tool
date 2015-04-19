/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class for interacting with XML documents.
 *
 * @author Bruce Schubert
 * @version $Id$
 */
public class XmlUtil {

    private static final Logger logger = Logger.getLogger(XmlUtil.class.getName());

    /**
     * Creates a new Document from an String containing XML.
     * @param xmlString String containing XML content.
     * @return A new Document.
     */
    public static Document newDocumentFromString(String xmlString) {
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        Document doc = null;
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
        }
        catch (ParserConfigurationException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        catch (SAXException | IOException ex) {
            throw new RuntimeException(ex);
        }
        return doc;
    }

    /**
     * Creates a new Document from an InputStream.
     * @param in Stream containing XML content.
     * @return A new Document.
     */
    public static Document newDocumentFromInputStream(InputStream in) {
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        Document doc = null;

        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        try {
            doc = builder.parse(new InputSource(in));
        }
        catch (SAXException | IOException ex) {
            throw new RuntimeException(ex);
        }
        return doc;
    }

    public static String convertDocumentToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            // below code to remove XML declaration
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            return format(output);
        }
        catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Pretty-prints XML, supplied as a string.
     * @param xml Unformatted XML.
     * @return Formatted XML
     */
    public static String format(String xml) {
        try {
            final InputSource src = new InputSource(new StringReader(xml));
            final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
            final boolean keepDeclaration = xml.startsWith("<?xml");

            //May need this: System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");
            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            final LSSerializer writer = impl.createLSSerializer();

            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
            writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set this to true if the declaration is needed to be outputted.

            return writer.writeToString(document);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the first child element with the matching tag.
     *
     * @param parent Element containing children
     * @param namespaceURI Namespace URI (e.g., "http://emxsys.com/worldwind-shape-attributes")
     * @param tag to search for
     * @return the first Element in the parent element's NodeList, or null if not found.
     */
    public static Element getChildElement(Element parent, String namespaceURI, String tag) {
        NodeList nodes;
        if (namespaceURI == null || namespaceURI.isEmpty()) {
            nodes = parent.getElementsByTagName(tag);
        }
        else {
            nodes = parent.getElementsByTagNameNS(namespaceURI, tag);
        }
        if (nodes != null && nodes.getLength() > 0) {
            return (Element) nodes.item(0);
        }
        return null;
    }

    /**
     * Gets the first child element with the matching tag as a Boolean.
     *
     * @param parent Node containing the children.
     * @param namespaceURI Namespace URI (e.g., "http://emxsys.com/worldwind-shape-attributes").
     * @param tag The to search for.
     * @return The first Element in the NodeList as Boolean, or null if not found.
     */
    public static Boolean getChildElementBoolean(Element parent, String namespaceURI, String tag) {
        Element childElement = getChildElement(parent, namespaceURI, tag);
        if (childElement == null) {
            return null;
        }
        return Boolean.valueOf(childElement.getTextContent());
    }

    /**
     * Gets the first child element with the matching tag as a Double.
     *
     * @param parent Node containing the children.
     * @param namespaceURI Namespace URI (e.g., "http://emxsys.com/worldwind-shape-attributes").
     * @param tag The to search for.
     * @return The first Element in the NodeList as Double, or null if not found.
     */
    public static Double getChildElementDouble(Element parent, String namespaceURI, String tag) {
        Element childElement = getChildElement(parent, namespaceURI, tag);
        if (childElement == null) {
            return null;
        }
        return Double.valueOf(childElement.getTextContent());
    }

    /**
     * Gets the first child element with the matching tag as an Integer.
     *
     * @param parent Node containing the children.
     * @param namespaceURI Namespace URI (e.g., "http://emxsys.com/worldwind-shape-attributes").
     * @param tag The to search for.
     * @return The first Element in the NodeList as Integer, or null if not found.
     */
    public static Integer getChildElementInteger(Element parent, String namespaceURI, String tag) {
        Element childElement = getChildElement(parent, namespaceURI, tag);
        if (childElement == null) {
            return null;
        }
        return Integer.valueOf(childElement.getTextContent());
    }

    /**
     * Gets the first child element with the matching tag.
     *
     * @param parent Node containing the children.
     * @param namespaceURI Namespace URI (e.g., "http://emxsys.com/worldwind-shape-attributes").
     * @param tag The to search for.
     * @return The first Element in the NodeList as a String, or null if not found.
     */
    public static String getChildElementText(Element parent, String namespaceURI, String tag) {
        Element childElement = getChildElement(parent, namespaceURI, tag);
        if (childElement == null) {
            return null;
        }
        return childElement.getTextContent();
    }

    public static Element createDoubleElement(Document doc, String namespaceURI, double value, String tag) {
        try {
            Element element = doc.createElementNS(namespaceURI, tag);
            element.appendChild(doc.createTextNode(Double.toString(value)));
            return element;
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "createDoubleElement failed! Unable to export value ({0}). Reason: {1}", new Object[]{
                Double.toString(value), ex.toString()
            });
        }
        return null;
    }

    public static Element createBooleanElement(Document doc, String namespaceURI, boolean value, String tag) {
        try {
            Element element = doc.createElementNS(namespaceURI, tag);
            element.appendChild(doc.createTextNode(Boolean.toString(value)));
            return element;
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "createBooleanElement failed! Unable to export boolean ({0}). Reason: {1}", new Object[]{
                Boolean.toString(value), ex.toString()
            });
        }
        return null;
    }

    public static Element createIntegerElement(Document doc, String namespaceURI, int value, String tag) {
        try {
            Element element = doc.createElementNS(namespaceURI, tag);
            element.appendChild(doc.createTextNode(Integer.toString(value)));
            return element;
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "createIntegerElement failed! Unable to export integer ({0}). Reason: {1}", new Object[]{
                Integer.toString(value), ex.toString()
            });
        }
        return null;
    }

    public static Element createTextElement(Document doc, String namespaceURI, String text, String tag) {
        try {
            Element element = doc.createElementNS(namespaceURI, tag);
            element.appendChild(doc.createTextNode(text));
            return element;
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "createTextElement failed! Unable to export text ({0}). Reason: {1}", new Object[]{
                text, ex.toString()
            });
        }
        return null;
    }
}
