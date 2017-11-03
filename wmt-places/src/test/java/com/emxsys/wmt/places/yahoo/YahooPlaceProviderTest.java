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

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.wmt.places.api.Place;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Bruce Schubert
 */
public class YahooPlaceProviderTest {

    private static String RESULTS_STRING;

    public YahooPlaceProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        RESULTS_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<query xmlns:yahoo=\"http://www.yahooapis.com/v1/base.rng\" yahoo:count=\"2\" yahoo:created=\"2016-12-19T14:09:01Z\" yahoo:lang=\"en-US\">"
                + " <results>"
                + "     <place xml:lang=\"en-US\" xmlns=\"http://where.yahooapis.com/v1/schema.rng\" xmlns:yahoo=\"http://www.yahooapis.com/v1/base.rng\" yahoo:uri=\"http://where.yahooapis.com/v1/place/2467212\"><woeid>2467212</woeid><placeTypeName code=\"7\">Town</placeTypeName><name>Oxnard</name><country code=\"US\" type=\"Country\" woeid=\"23424977\">United States</country><admin1 code=\"US-CA\" type=\"State\" woeid=\"2347563\">California</admin1><admin2 code=\"\" type=\"County\" woeid=\"12587725\">Ventura</admin2><admin3/><locality1 type=\"Town\" woeid=\"2467212\">Oxnard</locality1><locality2/><postal/><centroid><latitude>34.196049</latitude><longitude>-119.181396</longitude></centroid><boundingBox><southWest><latitude>34.119469</latitude><longitude>-119.264526</longitude></southWest><northEast><latitude>34.261009</latitude><longitude>-119.120689</longitude></northEast></boundingBox><areaRank>1</areaRank><popRank>1</popRank><timezone type=\"Time Zone\" woeid=\"56043663\">America/Los_Angeles</timezone></place>"
                + "     <place xml:lang=\"en-US\" xmlns=\"http://where.yahooapis.com/v1/schema.rng\" xmlns:yahoo=\"http://www.yahooapis.com/v1/base.rng\" yahoo:uri=\"http://where.yahooapis.com/v1/place/92169266\"><woeid>92169266</woeid><placeTypeName code=\"16\">LandFeature</placeTypeName><name>Oxnard</name><country code=\"US\" type=\"Country\" woeid=\"23424977\">United States</country><admin1 code=\"US-NM\" type=\"State\" woeid=\"2347590\">New Mexico</admin1><admin2 code=\"\" type=\"County\" woeid=\"12589279\">Bernalillo</admin2><admin3/><locality1 type=\"Town\" woeid=\"2352824\">Albuquerque</locality1><locality2 type=\"Suburb\" woeid=\"56190913\">Huning Castle</locality2><postal type=\"Zip Code\" woeid=\"12794952\">87104</postal><centroid><latitude>35.086231</latitude><longitude>-106.666313</longitude></centroid><boundingBox><southWest><latitude>35.085911</latitude><longitude>-106.666573</longitude></southWest><northEast><latitude>35.086441</latitude><longitude>-106.666107</longitude></northEast></boundingBox><areaRank>1</areaRank><popRank>1</popRank><timezone type=\"Time Zone\" woeid=\"56043665\">America/Denver</timezone></place>"
                + " </results>"
                + "</query><!-- total: 11 -->"
                + "<!-- prod_gq1_1;paas.yql;queryyahooapiscomproductiongq1;a987ab05-c58d-11e6-8297-f0921c12e67c -->";
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
    public void testFindMatchingPlaces() {
        System.out.println("findMatchingPlaces");
        String lookupCriteria = "Oxnard";
        YahooPlaceProvider instance = YahooPlaceProvider.getInstance();
        List<? extends Place> result = instance.findMatchingPlaces(lookupCriteria);
        for (Place place : result) {
            System.out.println(" : " + place.toString());
        }
        assertTrue("Expected > 0 results", result.size() > 0);
    }

    /**
     * Test of findNearbyPlaces method, of class YahooPlaceProvider.
     */
    @Ignore
    @Test
    @Deprecated
    public void testFindNearbyPlaces() {
        System.out.println("findNearbyPlaces");
        Coord2D coord = GeoCoord2D.fromDegrees(34.2, -119.2);
        YahooPlaceProvider instance = YahooPlaceProvider.getInstance();
        List<? extends Place> result = instance.findNearbyPlaces(coord);
        for (Place place : result) {
            //System.out.println(" : " +place.toString());
        }
        assertTrue("Expected > 0 results", result.size() > 0);
    }

    /**
     * Test of parsePlacefinderResults method, of class YahooPlaceProvider.
     */
    @Test
    public void testParseResults() {
        System.out.println("parseResults");
        YahooPlaceProvider instance = YahooPlaceProvider.getInstance();
        ArrayList<Place> result = instance.parsePlaceResults(RESULTS_STRING);
        assertEquals("Expected 2 results", 2, result.size());
    }

}
