/*
 * Copyright (C) 2011 Bruce Schubert <bruce@emxsys.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.emxsys.wmt.gis.api;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import visad.CommonUnit;
import visad.Data;
import visad.Real;
import visad.RealType;



/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class GeoPointTupleTest
{
    private static final double LAT_OXR = 34.2;
    private static final double LON_OXR = -119.2;
    
    
    public GeoPointTupleTest()
    {
    }



    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }



    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }
    


    @Before
    public void setUp()
    {
    }
    


    @After
    public void tearDown()
    {
    }



    /**
     * Test of fromDegrees method, of class GeoCoord2D.
     */
    @Test
    public void testFromDegrees()
    {
        System.out.println("fromDegrees");
        GeoCoord2D expect = null;
        GeoCoord2D result = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        assertEquals(LAT_OXR, result.getLatitudeDegrees(), 0.00001);
        assertEquals(LON_OXR, result.getLongitudeDegrees(), 0.00001);
    }



    /**
     * Test of getLatitude method, of class GeoCoord2D.
     */
    @Test
    public void testGetLatitude()
    {
        System.out.println("getLatitude");
        GeoCoord2D instance = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        Real expect = new Real(RealType.Latitude, LAT_OXR);
        Real result = instance.getLatitude();
        assertEquals(expect, result);
        assertEquals(expect.getValue(), result.getValue(), 0.00000001);
    }



    /**
     * Test of getLongitude method, of class GeoCoord2D.
     */
    @Test
    public void testGetLongitude()
    {
        System.out.println("getLongitude");
        GeoCoord2D instance = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        Real expect = new Real(RealType.Longitude, LON_OXR);
        Real result = instance.getLongitude();
        assertEquals(expect, result);
        assertEquals(expect.getValue(), result.getValue(), 0.00000001);
    }



    /**
     * Test of getLatitudeDegrees method, of class GeoCoord2D.
     */
    @Test
    public void testGetLatitudeDegrees()
    {
        System.out.println("getLatitudeDegrees");
        GeoCoord2D instance = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        double result = instance.getLatitudeDegrees();
        assertEquals(LAT_OXR, result, 0.00000001);
        assertEquals(LAT_OXR, result, 0.0);
    }



    /**
     * Test of getLongitudeDegrees method, of class GeoCoord2D.
     */
    @Test
    public void testGetLongitudeDegrees()
    {
        System.out.println("getLongitudeDegrees");
        GeoCoord2D instance = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        double result = instance.getLongitudeDegrees();
        assertEquals(LON_OXR, result, 0.00000001);
        assertEquals(LON_OXR, result, 0.0);
    }



    /**
     * Test of isMissing method, of class GeoCoord2D.
     */
    @Test
    public void testIsMissing()
    {
        System.out.println("isMissing");
        GeoCoord2D instance = new GeoCoord2D();
        boolean expect = true;
        boolean result = instance.isMissing();
        assertEquals(expect, result);

        instance = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        assertEquals(false, instance.isMissing());

    }



    /**
     * Test of getComponent method, of class GeoCoord2D.
     */
    @Test
    public void testGetComponent() throws Exception
    {
        System.out.println("getComponent");
        GeoCoord2D instance = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        Data expectLat = new Real(RealType.Latitude, LAT_OXR);
        Data expectLon = new Real(RealType.Longitude, LON_OXR);

        assertEquals(expectLat, instance.getComponent(0));
        assertEquals(expectLon, instance.getComponent(1));
        assertFalse(instance.getComponent(0).equals(instance.getComponent(1)));
    }



    /**
     * Test of getComponents method, of class GeoCoord2D.
     */
    @Test
    public void testGetComponents()
    {
        System.out.println("getComponents");
        boolean copy = true;
        GeoCoord2D instance = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        Data[] expResult = {new Real(RealType.Latitude, LAT_OXR), new Real(RealType.Longitude, LON_OXR)};
        Data[] result = instance.getComponents(copy);
        assertArrayEquals(expResult, result);
    }



    /**
     * Test of equals method, of class GeoCoord2D.
     */
    @Test
    public void testEquals()
    {
        System.out.println("equals");
        GeoCoord2D instance1 = new GeoCoord2D();
        GeoCoord2D instance2 = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        GeoCoord2D instance3 = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        GeoCoord2D instance4 = GeoCoord2D.fromDegrees(LAT_OXR+1, LON_OXR+1);
        boolean expect = false;
        boolean result = instance1.equals(instance2);
        assertEquals(expect, result);
        
        expect = true;
        result = instance2.equals(instance2);
        assertEquals(expect, result);

        expect = true;
        result = instance2.equals(instance3);
        assertEquals(expect, result);

        expect = false;
        result = instance3.equals(instance4);
        assertEquals(expect, result);
    }



    /**
     * Test of hashCode method, of class GeoCoord2D.
     */
    @Test
    public void testHashCode()
    {
        System.out.println("hashCode");
        GeoCoord2D instance1 = new GeoCoord2D();
        GeoCoord2D instance2 = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        GeoCoord2D instance3 = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        GeoCoord2D instance4 = GeoCoord2D.fromDegrees(LAT_OXR+1, LON_OXR+1);
        boolean expect = false;
        boolean result = instance1.hashCode() == instance2.hashCode();
        assertEquals(expect, result);
        
        expect = true;
        result = instance1.hashCode() == instance1.hashCode();
        assertEquals(expect, result);

        expect = true;
        result = instance2.hashCode() == instance2.hashCode();
        assertEquals(expect, result);

        expect = true;
        result = instance2.hashCode() == instance3.hashCode();
        assertEquals(expect, result);

        expect = false;
        result = instance3.hashCode() == instance4.hashCode();
        assertEquals(expect, result);
    }



    /**
     * Test of toString method, of class GeoCoord2D.
     */
    @Test
    public void testToString()
    {
        System.out.println("toString");
        GeoCoord2D instance = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        String result = instance.toString();
        System.out.println("  " + result);
        assertTrue(!result.isEmpty());
    }
    
    /**
     * Test of toXml method, of class GeoPositionTuple.
     */
    @Test
    public void testToXml() throws ParserConfigurationException
    {
        System.out.println("toXml");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        GeoCoord2D instance = GeoCoord2D.fromDegrees(LAT_OXR, LON_OXR);
        Element element = instance.toXmlElement(doc, "Point");
        NamedNodeMap attributes = element.getAttributes();
        
        System.out.println("  " + element.getNodeName());
        for (int i = 0; i < attributes.getLength(); i++)
        {
            Node item = attributes.item(i);
            System.out.println("    " + item.toString());
        }
        assertTrue(attributes.getLength() > 0);
    }
    
}
