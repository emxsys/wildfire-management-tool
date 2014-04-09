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
package com.emxsys.wmt.gis.api.index;

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.GeoCoord2D;
import java.util.Collection;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Bruce Schubert
 */
public class MortonCodesTest {

    public MortonCodesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of generate method, of class MortonCodes.
     */
    @Test
    public void testGenerate_Coord2D() {
        System.out.println("generate");

        TreeMap<Long, Coord2D> codes = new TreeMap<Long, Coord2D>();
        for (int lon = -180; lon <= 180; lon++) {
            for (int lat = -90; lat <= 90; lat++) {
                Coord2D point = GeoCoord2D.fromDegrees(lat, lon);
                long morton = MortonCodes.generate(point);
//                System.out.println("lat: " + lat + ", lon: " + lon + " = " + morton);
                Coord2D existing = codes.put(morton, point);
                assertNull(existing);
            }
        }
        for (int lon = -180; lon <= 180; lon++) {
            for (int lat = -90; lat <= 90; lat++) {
                long morton = MortonCodes.generate(GeoCoord2D.fromDegrees(lat, lon));
                Coord2D value = codes.get(morton);
                assertNotNull(value);
            }
        }
    }

    /**
     * Test of interleaveBits method, of class MortonCodes.
     */
    @Test
    public void testInterleaveBits() {
        System.out.println("interleaveBits");
        int x = 0;
        int y = 0xFFFFFFFF;
        long z = 0xAAAAAAAAAAAAAAAAL;
        long result = MortonCodes.interleaveBits(x, y);
        System.out.println(Integer.toBinaryString(x) + " X " + Integer.toBinaryString(y) + " = " + Long.toBinaryString(result));
        assertEquals(Integer.toBinaryString(x) + " X " + Integer.toBinaryString(y) + " = " + Long.toBinaryString(z), z, result);
 
        x = 0xFFFFFFFF;
        y = 0xFFFFFFFF;
        z = 0xFFFFFFFFFFFFFFFFL;
        result = MortonCodes.interleaveBits(x, y);
        System.out.println(Integer.toBinaryString(x) + " X " + Integer.toBinaryString(y) + " = " + Long.toBinaryString(result));
        assertEquals(Integer.toBinaryString(x) + " X " + Integer.toBinaryString(y) + " = " + Long.toBinaryString(z), z, result);

        x = 0x00FF00FF;
        y = 0x00FF00FF;
        z = 0x0000FFFF0000FFFFL;
        result = MortonCodes.interleaveBits(x, y);
        System.out.println(Integer.toBinaryString(x) + " X " + Integer.toBinaryString(y) + " = " + Long.toBinaryString(result));
        assertEquals(Integer.toBinaryString(x) + " X " + Integer.toBinaryString(y) + " = " + Long.toBinaryString(z), z, result);

        x = 0xFF00FF00;
        y = 0x00FF00FF;
        z = 0x5555AAAA5555AAAAL;
        result = MortonCodes.interleaveBits(x, y);
        System.out.println(Integer.toBinaryString(x) + " X " + Integer.toBinaryString(y) + " = " + Long.toBinaryString(result));
        System.out.println(Integer.toBinaryString(x) + " x ");
        System.out.println(Integer.toBinaryString(y) + " y ");
        System.out.println(Long.toBinaryString(z) + " z ");
        System.out.println(Long.toBinaryString(result) + " result ");
        assertEquals(Integer.toBinaryString(x) + " X " + Integer.toBinaryString(y) + " = " + Long.toBinaryString(z), z, result);
    }

}
