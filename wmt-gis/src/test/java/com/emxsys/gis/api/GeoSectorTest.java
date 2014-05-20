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
package com.emxsys.gis.api;

import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.Latitude;
import com.emxsys.gis.api.Longitude;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.util.MathUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import visad.Real;



/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class GeoSectorTest
{

    public GeoSectorTest()
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

    @Test
    public void testContains()
    {
        System.out.println("testContains");
        GeoSector first = new GeoSector(34.2, -119.2, 34.4, -119.0);
        GeoSector second = new GeoSector(34.25, -119.15, 34.35, -119.05);
        Coord2D expect = GeoCoord2D.fromDegrees(34.4, -119.0);
        assertTrue(first.contains(first));
        assertTrue(first.contains(second));
        assertFalse(second.contains(first));
        assertTrue(first.contains(GeoCoord2D.fromDegrees(34.3, -119.1)));
        assertTrue(first.contains(GeoCoord2D.fromDegrees(34.2, -119.2)));
        assertTrue(first.contains(GeoCoord2D.fromDegrees(34.4, -119.0)));
        assertFalse(first.contains(GeoCoord2D.fromDegrees(34.5, -119.0)));
        assertFalse(first.contains(GeoCoord2D.fromDegrees(34.4, -118.9999)));
    }

    @Test
    public void testIntersects()
    {
        System.out.println("testIntersects");
        GeoSector first = new GeoSector(34.2, -119.2, 34.4, -119.0);
        GeoSector second = new GeoSector(34.25, -119.15, 34.35, -119.05);
        GeoSector third = new GeoSector(34.25, -119.15, 34.5, -118.00);
        GeoSector fourth = new GeoSector(34.1, -119.15, 34.5, -119.05);
        GeoSector fifth = new GeoSector(34.25, -120.0, 34.35, -118.0);
        assertTrue(first.intersects(first));
        assertTrue(first.intersects(second));
        assertTrue(first.intersects(third));
        assertTrue(first.intersects(fourth));
        assertTrue(first.intersects(fifth));
        assertTrue(first.intersects(first));
        assertTrue(second.intersects(first));
        assertTrue(third.intersects(first));
        assertTrue(fourth.intersects(first));
        assertTrue(fifth.intersects(first));
    }


    /**
     * Test of getNortheast method, of class GeoSector.
     */
    @Test
    public void testGetNortheast()
    {
        System.out.println("getNortheast");
        GeoSector instance = new GeoSector(34.2, -119.2, 34.4, -119.0);
        Coord2D expect = GeoCoord2D.fromDegrees(34.4, -119.0);
        Coord2D result = instance.getNortheast();
        System.out.println("  expect: " + expect.toString());
        System.out.println("  result: " + result.toString());
        assertTrue(MathUtil.nearlyEquals(expect.getLatitudeDegrees(), result.getLatitudeDegrees()));
        assertTrue(MathUtil.nearlyEquals(expect.getLongitudeDegrees(), result.getLongitudeDegrees()));
    }



    /**
     * Test of getSouthwest method, of class GeoSector.
     */
    @Test
    public void testGetSouthwest()
    {
        System.out.println("getSouthwest");
        GeoSector instance = new GeoSector(34.2, -119.2, 34.4, -119.0);
        Coord2D expect = GeoCoord2D.fromDegrees(34.2, -119.2);
        Coord2D result = instance.getSouthwest();
        System.out.println("  expect: " + expect.toString());
        System.out.println("  result: " + result.toString());
        assertTrue(MathUtil.nearlyEquals(expect.getLatitudeDegrees(), result.getLatitudeDegrees()));
        assertTrue(MathUtil.nearlyEquals(expect.getLongitudeDegrees(), result.getLongitudeDegrees()));
    }



    /**
     * Test of getWidth method, of class GeoSector.
     */
    @Test
    public void testGetWidth()
    {
        System.out.println("getWidth");
        GeoSector instance = new GeoSector(34.2, -119.2, 34.4, -119.0);
        Real expect = Longitude.fromDegrees(0.2);
        Real result = instance.getWidth();
        assertTrue(MathUtil.nearlyEquals(expect.getValue(), result.getValue()));
    }



    /**
     * Test of getHeight method, of class GeoSector.
     */
    @Test
    public void testGetHeight()
    {
        System.out.println("getHeight");
        GeoSector instance = new GeoSector(34.2, -119.2, 34.4, -119.0);
        Real expect = Latitude.fromDegrees(0.2);
        Real result = instance.getHeight();
        assertTrue(MathUtil.nearlyEquals(expect.getValue(), result.getValue()));
    }



    /**
     * Test of isMissing method, of class GeoSector.
     */
    @Test
    public void testIsMissing()
    {
        System.out.println("isMissing");
        GeoSector instance = new GeoSector();
        boolean expResult = true;
        boolean result = instance.isMissing();
        assertEquals(expResult, result);
    }



    /**
     * Test of equals method, of class GeoSector.
     */
    @Test
    public void testEquals()
    {
        System.out.println("equals");
        Object obj = null;
        GeoSector instance = new GeoSector(34.2, -119.2, 34.4, -119.0);
        GeoSector instance2 = new GeoSector(34.2, -119.2, 34.4, -119.0);
        GeoSector instance3 = new GeoSector(34.21, -119.2, 34.4, -119.0);
        assertEquals(instance, instance);
        assertEquals(instance, instance2);
        assertTrue(instance.equals(instance2));
        assertFalse(instance.equals(instance3));
    }

}
