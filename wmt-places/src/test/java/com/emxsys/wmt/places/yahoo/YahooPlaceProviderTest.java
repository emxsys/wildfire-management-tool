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
package com.emxsys.wmt.places.yahoo;

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.GeoCoord2D;
import com.emxsys.wmt.places.api.Place;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Bruce Schubert
 */
public class YahooPlaceProviderTest {

    private static String xmlString;

    public YahooPlaceProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + " <query xmlns:yahoo=\"http://www.yahooapis.com/v1/base.rng\" yahoo:count=\"1\"\n"
                + " yahoo:created=\"2013-05-04T23:55:38Z\" yahoo:lang=\"en-US\">\n"
                + "  <results>\n"
                + "      <Result>\n"
                + "          <quality>62</quality>\n"
                + "          <latitude>37.614769</latitude>\n"
                + "          <longitude>-122.391792</longitude>\n"
                + "          <offsetlat>37.614769</offsetlat>\n"
                + "          <offsetlon>-122.391792</offsetlon>\n"
                + "          <radius>1300</radius>\n"
                + "          <name>San Francisco International Airport</name>\n"
                + "          <line1>San Francisco International Airport</line1>\n"
                + "          <line2>San Francisco, CA 94128</line2>\n"
                + "          <line3/>\n"
                + "          <line4>United States</line4>\n"
                + "          <house/>\n"
                + "          <street/>\n"
                + "          <xstreet/>\n"
                + "          <unittype/>\n"
                + "          <unit/>\n"
                + "          <postal>94128</postal>\n"
                + "          <neighborhood/>\n"
                + "          <city>San Francisco</city>\n"
                + "          <county>San Francisco County</county>\n"
                + "          <state>California</state>\n"
                + "          <country>United States</country>\n"
                + "          <countrycode>US</countrycode>\n"
                + "          <statecode>CA</statecode>\n"
                + "          <countycode/>\n"
                + "          <uzip>94128</uzip>\n"
                + "          <hash/>\n"
                + "          <woeid>12521721</woeid>\n"
                + "          <woetype>14</woetype>\n"
                + "      </Result>\n"
                + "  </results>\n"
                + "</query>"
                + "<!-- total: 72 -->\n"
                + "<!-- engine4.yql.ne1.yahoo.com -->";
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
     * Test of getInstance method, of class YahooPlaceProvider.
     */
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        YahooPlaceProvider result = YahooPlaceProvider.getInstance();
        assertNotNull(result);
        // Should throw exeception
        exception.expect(IllegalStateException.class);
        YahooPlaceProvider instance = new YahooPlaceProvider();
    }

    /**
     * Test of findPlaces method, of class YahooPlaceProvider.
     */
    @Test
    public void testFindPlaces() {
        System.out.println("findPlaces");
        String lookupCriteria = "Oxnard";
        YahooPlaceProvider instance = YahooPlaceProvider.getInstance();
        List<? extends Place> result = instance.findPlaces(lookupCriteria);
        for (Place place : result) {
            //System.out.println(" : " +place.toString());
        }
        assertTrue(result.size() > 0);
    }

    /**
     * Test of findNearbyPlaces method, of class YahooPlaceProvider.
     */
    @Test
    public void testFindNearbyPlaces() {
        System.out.println("findNearbyPlaces");
        Coord2D coord = GeoCoord2D.fromDegrees(34.2, -119.2);
        YahooPlaceProvider instance = YahooPlaceProvider.getInstance();
        List<? extends Place> result = instance.findNearbyPlaces(coord);
        for (Place place : result) {
            //System.out.println(" : " +place.toString());
        }
        assertTrue(result.size() > 0);
    }

    /**
     * Test of parsePlacefinderResults method, of class YahooPlaceProvider.
     */
    @Test
    public void testParseResults() {
        System.out.println("parseResults");
        YahooPlaceProvider instance = YahooPlaceProvider.getInstance();
        ArrayList<Place> result = instance.parsePlacefinderResults(xmlString);
        assertEquals(1, result.size());
    }

}
