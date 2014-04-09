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
package com.emxsys.wmt.gis.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import no.geosoft.cc.geometry.Geometry;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Bruce Schubert
 */
public class GeoPartTest
{
    ArrayList<GeoCoord3D> coords = new ArrayList<>();


    public GeoPartTest()
    {
    }


    @BeforeClass
    public static void setUpClass()
    {
    }


    @AfterClass
    public static void tearDownClass()
    {
    }


    @Before
    public void setUp()
    {
        coords.add(GeoCoord3D.fromDegrees(34.0, -118.0));
        coords.add(GeoCoord3D.fromDegrees(34.0, -119.0));
        coords.add(GeoCoord3D.fromDegrees(35.0, -119.0));
        coords.add(GeoCoord3D.fromDegrees(35.0, -118.0));
        coords.add(GeoCoord3D.fromDegrees(34.0, -118.0));
    }


    @After
    public void tearDown()
    {
    }


    /**
     * Test of getNumDimensions method, of class GeoPart.
     */
    @Test
    public void testGetNumDimensions()
    {
        System.out.println("getNumDimensions");
        GeoPart instance = new GeoPart(coords);
        int expResult = 3;
        int result = instance.getNumDimensions();
        assertEquals(expResult, result);
    }


    /**
     * Test of getNumPoints method, of class GeoPart.
     */
    @Test
    public void testGetNumPoints()
    {
        System.out.println("getNumPoints");
        GeoPart instance = new GeoPart(coords);
        int expResult = 5;
        int result = instance.getNumPoints();
        assertEquals(expResult, result);
    }


    /**
     * Test of getPoints method, of class GeoPart.
     */
    @Test
    public void testGetPoints()
    {
        System.out.println("getPoints");
        GeoPart instance = new GeoPart(coords);
        Iterator<double[]> iterator = instance.getPoints().iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            i++;
            double[] xy = iterator.next();
            System.out.println("    lat: " + xy[0] + ", lon: " + xy[1]);
        }
    }


    /**
     * Test of getX method, of class GeoPart.
     */
    @Test
    public void testGetX()
    {
        System.out.println("getX");
        GeoPart instance = new GeoPart(coords);
        double[] expResult =
        {
            -118, -119.0, -119.0, -118.0, -118
        };
        double[] result = instance.getX();
        assertTrue(Arrays.equals(expResult, result));

    }


    /**
     * Test of getY method, of class GeoPart.
     */
    @Test
    public void testGetY()
    {
        System.out.println("getY");
        GeoPart instance = new GeoPart(coords);
        double[] expResult =
        {
            34.0, 34.0, 35.0, 35.0, 34.0
        };
        double[] result = instance.getY();
        assertTrue(Arrays.equals(expResult, result));
    }


    /**
     * Test of getZ method, of class GeoPart.
     */
    @Test
    public void testGetZ()
    {
        System.out.println("getZ");
        GeoPart instance = new GeoPart(coords);
        double[] expResult =
        {
            0, 0, 0, 0, 0
        };
        double[] result = instance.getZ();
        assertTrue(Arrays.equals(expResult, result));
    }


    /**
     * Test of toLambertAzimuthalEqualArea method, of class GeoPart.
     */
    @Test
    public void testToLambertAzimuthaEqualArea()
    {
        System.out.println("toLambertAzimuthaEqualArea");
        GeoPart instance = new GeoPart(coords);
        double[][] xy = instance.toLambertAzimuthalEqualArea();
        for (int i = 0; i < xy[0].length; i++)
        {
            System.out.println("    x: " + (long) xy[0][i] + ", y: " + (long) xy[1][i]);
        }
        System.out.println("     width: " + Math.abs(((long) xy[0][0]) - ((long) xy[0][1])));
        System.out.println("     height: " + Math.abs(((long) xy[1][1]) - ((long) xy[1][2])));

        double area = Geometry.computePolygonArea(xy[0], xy[1]);
        double hectares = Math.abs(area/10000);
        System.out.println("    hectares: " + ((long)hectares) );
        
        assertEquals(hectares, 1017800, 100);

    }
}