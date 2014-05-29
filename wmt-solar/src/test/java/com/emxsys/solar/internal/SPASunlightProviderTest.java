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

package com.emxsys.solar.internal;

import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.TerrainTuple;
import com.emxsys.visad.Times;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import visad.FieldImpl;
import visad.Gridded1DDoubleSet;
import visad.Linear2DSet;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class SPASunlightProviderTest {
    
    public SPASunlightProviderTest() {
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
     * Test of getSunPosition method, of class SPASunlightProvider.
     */
    @Test
    public void testGetSunPosition() throws VisADException, RemoteException {
        System.out.println("getSunPosition");
        //    spa.year          = 2003;
        //    spa.month         = 10;
        //    spa.day           = 17;
        //    spa.hour          = 12;
        //    spa.minute        = 30;
        //    spa.second        = 30;
        //    spa.timezone      = -7.0;
        //    spa.delta_ut1     = 0;
        //    spa.delta_t       = 67;
        //    spa.longitude     = -105.1786;
        //    spa.latitude      = 39.742476;
        //    spa.elevation     = 1830.14;
        //    spa.pressure      = 820;
        //    spa.temperature   = 11;
        //    spa.slope         = 30;
        //    spa.azm_rotation  = -10;
        //    spa.atmos_refract = 0.5667;
        ZonedDateTime date = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 1, ZoneId.of("-7"));
        GeoCoord3D observer = GeoCoord3D.fromDegreesAndMeters(39.742476, -105.1786, 0/*1830.14*/);

        SolarData spa = new SolarData(date, observer, new TerrainTuple(-10, 30, 0), new Real(11), new Real(820));
        SolarPositionAlgorithms.spa_calculate(spa);
        
        SPASunlightProvider instance = new SPASunlightProvider();
        RealTuple tuple = instance.getSunPosition(date, observer);
        System.out.println(observer + " @ " + date);
        System.out.println(tuple.longString());
    }

    /**
     * Test of getSunlight method, of class SPASunlightProvider.
     */
    @Test
    public void testGetSunlight() throws VisADException, RemoteException {
        System.out.println("getSunlight");
        //    spa.year          = 2003;
        //    spa.month         = 10;
        //    spa.day           = 17;
        //    spa.hour          = 12;
        //    spa.minute        = 30;
        //    spa.second        = 30;
        //    spa.timezone      = -7.0;
        //    spa.delta_ut1     = 0;
        //    spa.delta_t       = 67;
        //    spa.longitude     = -105.1786;
        //    spa.latitude      = 39.742476;
        //    spa.elevation     = 1830.14;
        //    spa.pressure      = 820;
        //    spa.temperature   = 11;
        //    spa.slope         = 30;
        //    spa.azm_rotation  = -10;
        //    spa.atmos_refract = 0.5667;
        ZonedDateTime date = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 1, ZoneId.of("-7"));
        GeoCoord3D observer = GeoCoord3D.fromDegreesAndMeters(39.742476, -105.1786, 0/*1830.14*/);

        SolarData spa = new SolarData(date, observer, new TerrainTuple(-10, 30, 0), new Real(11), new Real(820));
        SolarPositionAlgorithms.spa_calculate(spa);
        
        SPASunlightProvider instance = new SPASunlightProvider();
        RealTuple tuple = instance.getSunlight(date, observer);
        System.out.println(observer + " @ " + date);
        System.out.println(tuple.longString());
        
        
        date = ZonedDateTime.now();
        observer = GeoCoord3D.fromDegrees(34.25, -119.2);
        tuple = instance.getSunlight(date, observer);
        System.out.println(observer + " @ " + date);
        System.out.println(tuple.longString());
        

//        Gridded1DDoubleSet timeDomain = Times.makeHourlyTimeSet(ZonedDateTime.now(), 12);
//        Linear2DSet spatialDomain = GeoSector.CONUS.toLinear2DSet(1, 1);
//        FieldImpl sunlight = instance.getSunlight(timeDomain, spatialDomain);
//        System.out.println(sunlight);

        
    }
    
}
