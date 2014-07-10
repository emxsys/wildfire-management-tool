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
package com.emxsys.util;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Bruce Schubert
 */
public class AngleUtilTest {

    public AngleUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testNormalize360() {
        System.out.println("normalize360");
        assertEquals("0", 0, AngleUtil.normalize360(0), 0.0);
        assertEquals("360", 0, AngleUtil.normalize360(360), 0.0);
        assertEquals("361", 1, AngleUtil.normalize360(361), 0.0);
        assertEquals("405", 45, AngleUtil.normalize360(405), 0.0);
        assertEquals("-1", 359, AngleUtil.normalize360(-1), 0.0);
    }

    @Test
    public void testAngularDistanceBetween() {
        System.out.println("angularDistanceBetween");
        assertEquals("270-90", 180, AngleUtil.angularDistanceBetween(270, 90), 0);
        assertEquals("90-270", 180, AngleUtil.angularDistanceBetween(90, 270), 0);
        assertEquals("90-270", 180, AngleUtil.angularDistanceBetween(90, -90), 0);
        assertEquals("355-5", 10, AngleUtil.angularDistanceBetween(355, 5), 0);
        assertEquals("5-355", 10, AngleUtil.angularDistanceBetween(5, 355), 0);
    }

    @Ignore
    @Test
    public void testCrossesMeridian() {
        System.out.println("crossesMeridian");
        double angle1Degrees = 0.0;
        double angle2Degrees = 0.0;
        boolean expResult = false;
        boolean result = AngleUtil.crossesMeridian(angle1Degrees, angle2Degrees);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Ignore
    @Test
    public void testDegreesToCardinalPoint8() {
        System.out.println("degreesToCardinalPoint8");
        double degrees = 0.0;
        String expResult = "";
        String result = AngleUtil.degreesToCardinalPoint8(degrees);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
