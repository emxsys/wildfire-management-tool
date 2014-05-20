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

import com.emxsys.gis.gml.GmlBuilder;
import com.emxsys.gis.gml.GmlParser;
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import java.io.IOException;
import javax.xml.XMLConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Bruce Schubert
 */
public class GmlParserTest {

    /**
     * See the test.xmd in the same folder.
     */
    static final String TEST_NS_URI = "http://emxsys.com/test";
    Document doc;

    public GmlParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        this.doc = XMLUtil.createDocument("test:Geometry", TEST_NS_URI, null, null);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of parsePoint method, of class GmlParser.
     */
    @Test
    public void testParsePoint() throws IOException {
        System.out.println("parsePoint");
        GeoCoord2D point = GeoCoord2D.fromDegrees(34.2, -119.2);

        GmlBuilder gmlBuilder = new GmlBuilder(doc, TEST_NS_URI, "test:GeoPoint");
        gmlBuilder.append(point);
        Element element = gmlBuilder.toElement();
        this.doc.getDocumentElement().appendChild(element);
        XMLUtil.write(this.doc, System.out, "UTF-8");

        Coord2D result = GmlParser.parsePoint(element);
        assertEquals(point, result);
    }

    /**
     * Test of parsePosition method, of class GmlParser.
     */
    @Test
    public void testParsePosition() throws IOException {
        System.out.println("parsePosition");
        GeoCoord3D position = GeoCoord3D.fromDegreesAndMeters(34.3333, -119.33333, 123.456789);

        GmlBuilder gmlBuilder = new GmlBuilder(doc, TEST_NS_URI, "test:GeoPosition");
        gmlBuilder.append(position);
        Element element = gmlBuilder.toElement();
        this.doc.getDocumentElement().appendChild(element);
        XMLUtil.write(this.doc, System.out, "UTF-8");

        Coord3D result = GmlParser.parsePosition(element);
        assertEquals(position, result);
    }

    /**
     * Test of parseCoordinates method, of class GmlParser.
     */
    @Test
    public void testParseCoordinates() {
        System.out.println("parseCoordinates");
        String coordinates = "-119.33333,34.66666,123.456789 1,2,3 4,5,6";
        double[][] expResult = {{-119.33333, 34.66666, 123.456789}, {1, 2, 3}, {4, 5, 6}};
        double[][] result = GmlParser.parseCoordinates(coordinates);
        assertArrayEquals(expResult, result);
    }
}
