/*
 * Copyright (C) 2012 Bruce Schubert <bruce@emxsys.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.emxsys.wmt.gis.api.index;

import com.emxsys.wmt.gis.api.GeoCoord2D;
import com.emxsys.wmt.gis.api.GeoSector;
import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.util.TimeUtil;
import java.util.Collection;
import java.util.Random;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;


/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class LinearQuadtreeTest
{

    LinearQuadtree<String> instance;
    GeoSector sector = new GeoSector(34.2, -119.4, 45.4, -75.2);


    public LinearQuadtreeTest()
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
        instance = new LinearQuadtree<>();        
        Random random = new Random(17);
        for (int i = 0; i < 50000; i++)
        {
            double lat = random.nextDouble() * 90 * (random.nextBoolean()? 1 : -1);
            double lon = random.nextDouble() * 180.0 * (random.nextBoolean()? 1 : -1);
            GeoCoord2D tuple = GeoCoord2D.fromDegrees(lat, lon);
            instance.add(tuple, tuple.toString());
    }
        try
        {
            Thread.sleep(1000L);
        }
        catch (InterruptedException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }


    @After
    public void tearDown()
    {
        instance = null;
    }
    


    /**
     * Test of findByRangeSubDivision method, of class LinearQuadtree.
     */
    @Test
    public void testFindByRangeSubDivision()
    {
        System.out.println("findByRangeSubDivision");
        
        long startTimeMs = System.currentTimeMillis();
        Collection<String> result = instance.findByRangeSubDivision(sector);
        assertTrue(result.size() == 405);
        System.out.print("Found " + result.size() + " items. ");
        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        for (String string : result) {
//            System.out.println(string);
//        }
        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByRangeSubDivision(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByRangeSubDivision(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByRangeSubDivision(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByRangeSubDivision(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByRangeSubDivision(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByRangeSubDivision(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
        
//        for (Coord2D geoPoint : result)
//        {
//            //System.out.println(geoPoint);
//        }
    }

    /**
     * Test of findByBruteForce method, of class LinearQuadtree.
     */
    @Test
    public void testFindByBruteForce()
    {
        System.out.println("findByBruteForce");
        
        long startTimeMs = System.currentTimeMillis();
        Collection<String> result = instance.findByBruteForce(sector);
        assertTrue(result.size() == 405);
        System.out.print("Found " + result.size() + " items. ");
        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByBruteForce(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByBruteForce(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByBruteForce(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByBruteForce(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByBruteForce(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
//        
//        startTimeMs = System.currentTimeMillis();
//        result = instance.findByBruteForce(sector);
//        System.out.println("Elapsed: " +TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMs));
        
//        for (Coord2D geoPoint : result)
//        {
//            //System.out.println(geoPoint);
//        }
    }

}
