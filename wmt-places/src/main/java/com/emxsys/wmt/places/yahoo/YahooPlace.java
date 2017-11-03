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

import com.emxsys.gis.api.GeoCoord2D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.wmt.places.api.Place;
import java.rmi.RemoteException;
import java.util.Objects;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import visad.Real;
import visad.VisADException;

/**
 * A named coordinate returned by gazetteers.
 *
 * @author Bruce Schubert
 */
public class YahooPlace extends GeoCoord2D implements Place {

    private String name;
    private String woeid;

    private GeoSector boundingBox;

    public YahooPlace(String woeid, String name, Real lat, Real lon) throws VisADException,
            RemoteException {
        super(lat, lon);
        this.woeid = woeid;
        this.name = name;
        this.boundingBox = new GeoSector(lat.getValue(), lon.getValue(), lat.getValue(), lon.getValue());
    }

    public YahooPlace(String woeid, String name, double lat, double lon) throws VisADException,
            RemoteException {
        this(woeid, name, lat, lon, lat, lon, lat, lon);
    }

    public YahooPlace(String woeid, String name, double lat, double lon, double swLat, double swLon, double neLat, double neLon) throws VisADException,
            RemoteException {
        super(lat, lon);
        this.woeid = woeid;
        this.name = name;
        this.boundingBox = new GeoSector(swLat, swLon, neLat, neLon);
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
     */
    static public YahooPlace fromPlaceNode(Node placeNode) {
        try {
            XPathFactory xpFactory = XPathFactory.newInstance();
            XPath xpath = xpFactory.newXPath();

            String woeid = xpath.evaluate("woeid", placeNode);
            String name = xpath.evaluate("name", placeNode);
            String placeType = xpath.evaluate("placeTypeName", placeNode);
            String locality = xpath.evaluate("locality1", placeNode);
            String admin = xpath.evaluate("admin1", placeNode);

            String placeName = name + " [" + placeType + "]";
            if (!locality.isEmpty()) {
                placeName += ", " + locality;
            }
            if (!admin.isEmpty()) {
                placeName += ", " + admin;
            }

            Node centroidNode = (Node) xpath.evaluate("centroid", placeNode, XPathConstants.NODE);
            String lat = xpath.evaluate("latitude", centroidNode);
            String lon = xpath.evaluate("longitude", centroidNode);

            Node swNode = (Node) xpath.evaluate("boundingBox/southWest", placeNode, XPathConstants.NODE);
            String swLat = xpath.evaluate("latitude", swNode);
            String swLon = xpath.evaluate("longitude", swNode);

            Node neNode = (Node) xpath.evaluate("boundingBox/northEast", placeNode, XPathConstants.NODE);
            String neLat = xpath.evaluate("latitude", neNode);
            String neLon = xpath.evaluate("longitude", neNode);

            YahooPlace place = new YahooPlace(woeid, placeName, 
                    Double.parseDouble(lat), Double.parseDouble(lon),
                    Double.parseDouble(swLat), Double.parseDouble(swLon),
                    Double.parseDouble(neLat), Double.parseDouble(neLon));

            return place;
        } catch (VisADException | RemoteException | XPathExpressionException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Parse the XML results returned from "select * from geo.placefinder ...".
     */
    @Deprecated
    static public YahooPlace fromPlacefinderNode(Node placefinderNode) throws XPathExpressionException, VisADException, RemoteException {
        XPathFactory xpFactory = XPathFactory.newInstance();
        XPath xpath = xpFactory.newXPath();

        String woeid = xpath.evaluate("woeid", placefinderNode);
        String lat = xpath.evaluate("latitude", placefinderNode);
        String lon = xpath.evaluate("longitude", placefinderNode);
        // Build the display name
        StringBuilder displayName = new StringBuilder();
        String[] lines = new String[]{
            "line1", "line2", "line3", "line4"
        };
        for (String line : lines) {
            String str = xpath.evaluate(line, placefinderNode);
            if (str.length() > 0) {
                if (displayName.length() > 0) {
                    displayName.append(", ");
                }
                displayName.append(str);
            }
        }
        return new YahooPlace(woeid, displayName.toString(), Double.parseDouble(lat), Double.parseDouble(lon));
    }

    /**
     * Gets the Yahoo where-on-earth-ID for this Place.
     * @return the where-on-earth-id (woeid)
     */
    public String getWoeid() {
        return woeid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.name);
        hash = 73 * hash + Objects.hashCode(this.woeid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final YahooPlace other = (YahooPlace) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.woeid, other.woeid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "YahooPlace{" + "name=" + name + ", woeid=" + woeid + ", coords=" + super.toString() + ", sector=" + boundingBox.toString() + '}';
    }

}
