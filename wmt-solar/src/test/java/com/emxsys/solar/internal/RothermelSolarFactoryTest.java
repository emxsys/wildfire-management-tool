/*
 * Copyright (c) 2014, bruce 
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
 *     - Neither the name of bruce,  nor the names of its 
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
package com.emxsys.solar.internal;

import com.emxsys.solar.internal.RothermelSolarFactory;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.solar.api.SunlightHours;
import java.time.ZonedDateTime;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import visad.FlatField;
import visad.Gridded1DSet;
import visad.Real;

/**
 *
 * @author bruce
 */
public class RothermelSolarFactoryTest {

    private static JFrame frame;
    private static boolean interactive = false;

    public RothermelSolarFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        frame = new JFrame("Test");
        if (interactive) {
            interactive = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                    frame, "Display interative results?", "Test Options",
                    JOptionPane.YES_NO_OPTION);
        }
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
     * Test of getSolarTime method, of class RothermelSolarFactory.
     */
    @Ignore
    @Test
    public void testGetSolarTime() {
        System.out.println("getSolarTime");
        Real longitude = null;
        Date utcTime = null;
        RothermelSolarFactory instance = new RothermelSolarFactory();
        Real expResult = null;
        Real result = instance.getSolarTime(longitude, utcTime);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSunPosition method, of class RothermelSolarFactory.
     */
    @Ignore
    @Test
    public void testGetSunPosition() {
        System.out.println("getSunPosition");
        ZonedDateTime time = null;
        RothermelSolarFactory instance = new RothermelSolarFactory();
        Coord3D expResult = null;
        Coord3D result = instance.getSunPosition(time);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSunlight method, of class RothermelSolarFactory.
     */
    @Test
    public void testGetSunlight() {
        System.out.println("getSunlight");
        ZonedDateTime time = ZonedDateTime.now();
        Coord2D coord = GeoCoord2D.fromDegrees(34.25, -119.2);
        RothermelSolarFactory instance = new RothermelSolarFactory();
        Sunlight sunlight = instance.getSunlight(time, coord);
        System.out.println(sunlight);

    }

    /**
     * Test of getSunlightHours method, of class RothermelSolarFactory.
     */
    @Ignore
    @Test
    public void testGetSunlightHours() {
        System.out.println("getSunlightHours");
        Real latitude = null;
        Date date = null;
        RothermelSolarFactory instance = new RothermelSolarFactory();
        SunlightHours expResult = null;
        SunlightHours result = instance.getSunlightHours(latitude, date);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeSolarData method, of class RothermelSolarFactory.
     */
    @Ignore
    @Test
    public void testMakeSolarData_3args() {
        System.out.println("makeSolarData");
        Gridded1DSet timeDomain = null;
        Real latitude1 = null;
        Real latitude2 = null;
        RothermelSolarFactory instance = new RothermelSolarFactory();
        FlatField expResult = null;
        FlatField result = instance.makeSolarData(timeDomain, latitude1, latitude2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeSolarData method, of class RothermelSolarFactory.
     */
    @Ignore
    @Test
    public void testMakeSolarData_Gridded1DSet_GeoSector() {
        System.out.println("makeSolarData");
        Gridded1DSet timeDomain = null;
        GeoSector sector = null;
        RothermelSolarFactory instance = new RothermelSolarFactory();
        FlatField expResult = null;
        FlatField result = instance.makeSolarData(timeDomain, sector);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
