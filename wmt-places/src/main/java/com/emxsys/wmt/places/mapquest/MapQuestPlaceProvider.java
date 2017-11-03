/*
 * Copyright (c) 2016, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.places.mapquest;

import com.emxsys.wmt.places.yahoo.*;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.wmt.places.api.Place;
import com.emxsys.wmt.places.api.PlaceProvider;
import com.emxsys.util.HttpUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import visad.VisADException;

/**
 *
 * https://developer.yahoo.com/yql/console/
 * @author Bruce Schubert
 */
@ServiceProvider(service = PlaceProvider.class)
public class MapQuestPlaceProvider implements PlaceProvider {

    /** Singleton */
    private static MapQuestPlaceProvider instance;
    /** URI for YQL public service. */
    protected static final String PUBLIC_URI = "https://query.yahooapis.com/v1/public/yql?";
    /** URI for YQL public service. */
    protected static final String OAUTH_URI = "https://query.yahooapis.com/v1/yql?";
    /** AppId for registered 'WMT' project */
    protected static final String APP_ID = NbBundle.getBundle("com.emxsys.wmt.branding.Bundle").getString("YAHOO_APP_ID");
    /** YQL geo.placefinder query param: SQL like text */
    protected static final String QUERY_PLACEFINDER_PARAM = "select * from geo.placefinder where text=\"%1$s\"";
    /** YQL geo.places query param: SQL like text */
    protected static final String QUERY_NEARBY_PLACES_PARAM = "select * from geo.places where text=\"%1$f %2$f\"";
    /** Common logger */
    private static final Logger logger = Logger.getLogger(MapQuestPlaceProvider.class.getName());
    private static final ArrayList<Place> EMPTY_LIST = new ArrayList<>();

    /**
     * Do not call! Used by @ServiceProvider. Use getInstance() or
     * Lookup.getDefault().lookup(YahooPlaceProvider.class) instead.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public MapQuestPlaceProvider() {
        if (instance != null) {
            // This will catch the second invocation, which should indicate incorrect usage.
            throw new IllegalStateException("Do not call constructor. Use getInstance().");
        }
        instance = this;
    }

    /**
     * Get the singleton instance.
     * @return the singleton YahooPlaceProvider found on the global lookup.
     */
    static public MapQuestPlaceProvider getInstance() {
        if (instance == null) {
            //System.out.println(">>> getInstance() -> Looking up the YahooPlaceProvider.class");
            return Lookup.getDefault().lookup(MapQuestPlaceProvider.class);
        }
        return instance;
    }

    /**
     * Returns a collection of Places that match the lookup criteria.
     * @param lookupCriteria criteria to search for
     */
    @Override
    public java.util.List<? extends Place> findMatchingPlaces(String lookupCriteria) {

        if (lookupCriteria == null || lookupCriteria.length() < 1) {
            return EMPTY_LIST;
        }
        String urlString;
        try {
            String query = String.format(QUERY_PLACEFINDER_PARAM, lookupCriteria);

            StringBuilder sb = new StringBuilder();
            sb.append(PUBLIC_URI);
            sb.append("q=");
            sb.append(URLEncoder.encode(query, "UTF-8"));
            sb.append("&appid=");
            sb.append(APP_ID);
            urlString = sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(lookupCriteria);
        }
        String xmlString;
        try {
            URL url = new URL(urlString);
            xmlString = HttpUtil.callWebService(url);
            System.out.println(xmlString);
            
            if (xmlString == null || xmlString.isEmpty()) {
                throw new RuntimeException("null or empty string.");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "callWebService failed: {0}", ex.getMessage());
            return EMPTY_LIST;
        }
        return parsePlaceResults(xmlString);
    }

    /**
     * Returns a collection of Places that are near the supplied coordinate.
     * @param coord the coordinate to search on
     */
    @Override
    public java.util.List<? extends Place> findNearbyPlaces(final Coord2D coord) {
        if (coord == null || coord.isMissing()) {
            return EMPTY_LIST;
        }
        String urlString;
        try {
            String query = String.format(QUERY_NEARBY_PLACES_PARAM, coord.getLatitudeDegrees(), coord.getLongitudeDegrees());

            StringBuilder sb = new StringBuilder();
            sb.append(PUBLIC_URI);
            sb.append("q=");
            sb.append(URLEncoder.encode(query, "UTF-8"));
            urlString = sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(coord.toString());
        }
        String xmlString;
        try {
            URL url = new URL(urlString);
            xmlString = HttpUtil.callWebService(url);
            System.out.println(xmlString);
            
            if (xmlString == null || xmlString.isEmpty()) {
                throw new RuntimeException("null or empty string.");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "callWebService failed: {0}", ex.getMessage());
            return EMPTY_LIST;
        }
        return this.parsePlaceResults(xmlString);
    }


    /**
     * Parse the XML results returned from "select * from geo.places ...". Example:
     * <pre>
     * {@code
     *<?xml version="1.0" encoding="UTF-8"?>
     * <query xmlns:yahoo="http://www.yahooapis.com/v1/base.rng" yahoo:count="1"
     * yahoo:created="2013-05-04T23:55:38Z" yahoo:lang="en-US">
     *  <results>
     *      <Result>
     *          <quality>62</quality>
     *          <latitude>37.614769</latitude>
     *          <longitude>-122.391792</longitude>
     *          <offsetlat>37.614769</offsetlat>
     *          <offsetlon>-122.391792</offsetlon>
     *          <radius>1300</radius>
     *          <name>San Francisco International Airport</name>
     *          <line1>San Francisco International Airport</line1>
     *          <line2>San Francisco, CA 94128</line2>
     *          <line3/>
     *          <line4>United States</line4>
     *          <house/>
     *          <street/>
     *          <xstreet/>
     *          <unittype/>
     *          <unit/>
     *          <postal>94128</postal>
     *          <neighborhood/>
     *          <city>San Francisco</city>
     *          <county>San Francisco County</county>
     *          <state>California</state>
     *          <country>United States</country>
     *          <countrycode>US</countrycode>
     *          <statecode>CA</statecode>
     *          <countycode/>
     *          <uzip>94128</uzip>
     *          <hash/>
     *          <woeid>12521721</woeid>
     *          <woetype>14</woetype>
     *      </Result>
     *  </results>
     *</query> 
     * }
     * </pre>
     *
     * @param xmlString
     */
    protected ArrayList<Place> parsePlacefinderResults(String xmlString) {
        try {
            Document doc = getDoc(xmlString);
            XPath xpath = XPathFactory.newInstance().newXPath();
            String expr = "/query/results/Result";
            NodeList results = (NodeList) xpath.evaluate(expr, doc, XPathConstants.NODESET);
            ArrayList<Place> places = new ArrayList<>(results.getLength());
            for (int i = 0; i < results.getLength(); i++) {
                YahooPlace place = YahooPlace.fromPlacefinderNode(results.item(i));
                places.add(place);
            }
            return places;
        } catch (XPathExpressionException | VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
        return EMPTY_LIST;
    }

    /**
     * Parse the XML results returned from "select * from geo.places ...". Example:
     * <pre>
     * {@code
     * <query xmlns:yahoo="http://www.yahooapis.com/v1/base.rng" yahoo:count="10" yahoo:created="2014-03-19T17:08:55Z" yahoo:lang="en-US">
     *  <results>
     *      <place xmlns="http://where.yahooapis.com/v1/schema.rng" xml:lang="en-US" yahoo:uri="http://where.yahooapis.com/v1/place/56574623">
     *          <woeid>56574623</woeid>
     *          <placeTypeName code="22">Suburb</placeTypeName>
     *          <name>Oxnard Airport</name>
     *          <country code="US" type="Country" woeid="23424977">United States</country>
     *          <admin1 code="US-CA" type="State" woeid="2347563">California</admin1>
     *          <admin2 code="" type="County" woeid="12587725">Ventura</admin2>
     *          <admin3/>
     *          <locality1 type="Town" woeid="2467212">Oxnard</locality1>
     *          <locality2 type="Suburb" woeid="56574623">Oxnard Airport</locality2>
     *          <postal type="Zip Code" woeid="12796704">93030</postal>
     *          <centroid>
     *              <latitude>34.200260</latitude>
     *              <longitude>-119.207932</longitude>
     *          </centroid>
     *          <boundingBox>
     *              <southWest>
     *                  <latitude>34.197441</latitude>
     *                  <longitude>-119.221138</longitude>
     *              </southWest>
     *              <northEast>
     *                  <latitude>34.203091</latitude>
     *                  <longitude>-119.194633</longitude>
     *              </northEast>
     *          </boundingBox>
     *          <areaRank>1</areaRank>
     *          <popRank>0</popRank>
     *          <timezone type="Time Zone" woeid="56043663">America/Los_Angeles</timezone>
     *      </place>
     *  </results>
     * </query>
     * }
     * </pre>
     *
     * @param xmlString
     */
    protected ArrayList<Place> parsePlaceResults(String xmlString) {
        try {
            Document doc = getDoc(xmlString);
            XPath xpath = XPathFactory.newInstance().newXPath();
            final String expr = "/query/results/place";
            NodeList results = (NodeList) xpath.evaluate(expr, doc, XPathConstants.NODESET);
            ArrayList<Place> places = new ArrayList<>(results.getLength());
            for (int i = 0; i < results.getLength(); i++) {
                YahooPlace place = YahooPlace.fromPlaceNode(results.item(i));
                places.add(place);
            }
            return places;
        } catch (XPathExpressionException e) {
            logger.log(Level.SEVERE, "URLException {0}", xmlString);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "RuntimeException {0}", xmlString);
        }
        return EMPTY_LIST;
    }

    static Document getDoc(String xmlString) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(false);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            return docBuilder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));

        } catch (ParserConfigurationException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (SAXException | IOException ex) {
            throw new RuntimeException(ex);
        }

    }

}
