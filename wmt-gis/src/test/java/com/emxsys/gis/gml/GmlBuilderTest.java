/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.gis.gml;

import com.emxsys.gis.gml.GmlConstants;
import com.emxsys.gis.gml.GmlBuilder;
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import java.io.IOException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Bruce Schubert
 */
public class GmlBuilderTest {

    static final String TEST_NS_URI = "http://emxsys.com/test";
    static Schema schema;
    Document doc;
    int indent;

    public GmlBuilderTest() throws SAXException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        URL schemaUrl = GmlBuilderTest.class.getResource("test.xsd");
        System.out.println("Loading Schema (" + schemaUrl + ") ... ");
        SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = f.newSchema(schemaUrl);
        System.out.print("Loaded!");

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws ParserConfigurationException {
//        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
//        f.setNamespaceAware(true);
//        DocumentBuilder b = f.newDocumentBuilder();
//        DOMImplementation impl = b.getDOMImplementation();
//        this.doc = impl.createDocument(TEST_NS_URI, "test:Geometry", null);

        this.doc = XMLUtil.createDocument("test:Geometry", TEST_NS_URI, null, null);
        this.indent = 0;
    }

    @After
    public void tearDown() throws SAXException, IOException {
        System.out.print("Validating XML...");
        schema.newValidator().validate(new DOMSource(this.doc));
        System.out.println("Passed!\n");
    }

    /**
     * Test of append method, of class GmlBuilder.
     */
    @Test
    public void testAppend_GeoPoint() throws IOException {
        System.out.println("append");
        Coord2D point = GeoCoord2D.fromDegrees(34.2, -119.2);   // KOXR
        GmlBuilder instance = new GmlBuilder(doc, TEST_NS_URI, "test:GeoPoint");
        instance.append(point);
        instance.append(point);
        Element element = instance.toElement();
        assertNotNull(element);

        doc.getDocumentElement().appendChild(element);
        //doRecursiveList(doc);
        XMLUtil.write(doc, System.out, "UTF-8");
    }

    /**
     * Test of append method, of class GmlBuilder.
     */
    @Test
    public void testAppend_GeoPosition() throws IOException {
        System.out.println("append");
        Coord3D point = GeoCoord3D.fromDegreesAndMeters(34.2, -119.2, 34.0);   // KOXR
        GmlBuilder instance = new GmlBuilder(doc, TEST_NS_URI, "test:GeoPosition");
        instance.append(point);
        instance.append(point);
        Element element = instance.toElement();
        assertNotNull(element);

        doc.getDocumentElement().appendChild(element);
        //doRecursiveList(doc);
        XMLUtil.write(doc, System.out, "UTF-8");
    }

    /**
     * Test of append method, of class GmlBuilder.
     */
    @Test
    public void testAppend_GeoSector() throws IOException {
        System.out.println("append");
        Coord2D point1 = GeoCoord2D.fromDegrees(34.2, -119.2);   // KOXR
        Coord2D point2 = GeoCoord2D.fromDegrees(34.5, -119.5);   // NW
        GeoSector sector = new GeoSector(point1, point2);
        GmlBuilder instance = new GmlBuilder(doc, TEST_NS_URI, "test:GeoSector");
        instance.append(sector);
        Element element = instance.toElement();
        assertNotNull(element);

        doc.getDocumentElement().appendChild(element);
        //doRecursiveList(doc);
        XMLUtil.write(doc, System.out, "UTF-8");
    }

    /**
     * Test of toElement method, of class GmlBuilder.
     */
    @Test
    @Ignore
    public void testToElement() throws IOException {
        System.out.println("toElement");
        Coord3D point = GeoCoord3D.fromDegreesAndMeters(34.2, -119.2, 34.0);   // KOXR
        GmlBuilder instance = new GmlBuilder(doc, TEST_NS_URI, "test:Elements");
        instance.append(point);
        instance.append(point);
        Element element = instance.toElement();
        assertNotNull(element);

        doc.getDocumentElement().appendChild(element);
        XMLUtil.write(doc, System.out, "UTF-8");

    }

    /**
     * Test of createElement method, of class GmlBuilder.
     */
    @Test
    @Ignore
    public void testCreateElement() {
        System.out.println("createElement");

        GmlBuilder instance = new GmlBuilder(doc, "test:CreateElement");
        for (String gmlElement : GmlConstants.GEOMETRY_ELEMENT_NAMES) {
            Element result = instance.createElement(gmlElement);
            assertNotNull(result);
            // System.out.println("\t" + result.getNodeName());
        }
    }

    void doRecursiveList(Node p) {
        if (p == null) {
            return;
        }
        indent++;
        NodeList nodes = p.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n == null) {
                continue;
            }
            doNode(n);
        }
        indent--;
    }

    void doNode(Node n) {
        for (int i = 0; i < indent; i++) {
            System.out.print("\t");
        }
        switch (n.getNodeType()) {
            case Node.ELEMENT_NODE:
                System.out.println("ELEMENT<" + n.getNodeName() + ">");
                doRecursiveList(n);
                break;
            case Node.TEXT_NODE:
                String text = n.getNodeValue();
                if (text.isEmpty()) {
                    break;
                }
                System.out.println("TEXT: " + text);
                break;
            default:
                System.out.println("OTHER NODE: " + n.getNodeType() + " : " + n.getClass());
                break;
        }
    }
}
