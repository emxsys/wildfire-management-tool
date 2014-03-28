/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
package com.emxsys.wmt.weather.nws;

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.GeoCoord2D;
import com.emxsys.wmt.util.XmlUtil;
import com.emxsys.wmt.visad.GeneralUnit;
import com.emxsys.wmt.weather.api.WeatherType;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.validation.Schema;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import visad.Field;
import visad.Real;
import visad.RealType;
import visad.Unit;
import visad.VisADException;

/**
 * This class is responsible for parsing DWML a Forecast. An example:
 * <pre>
 * {@code
 * <dwml xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 *      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0"
 *      xsi:noNamespaceSchemaLocation="http://www.nws.noaa.gov/forecasts/xml/DWMLgen/schema/DWML.xsd">
 *   <head>
 *       <product srsName="WGS 1984" concise-name="time-series" operational-mode="official">
 *           <title>NOAA's National Weather Service Forecast Data</title>
 *           <field>meteorological</field>
 *           <category>forecast</category>
 *           <creation-date refresh-frequency="PT1H">2014-03-28T12:28:58Z</creation-date>
 *       </product>
 *       <source>
 *           <more-information>http://www.nws.noaa.gov/forecasts/xml/</more-information>
 *           <production-center>
 *               Meteorological Development Laboratory
 *               <sub-center>Product Generation Branch</sub-center>
 *           </production-center>
 *           <disclaimer>http://www.nws.noaa.gov/disclaimer.html</disclaimer>
 *           <credit>http://www.weather.gov/</credit>
 *           <credit-logo>http://www.weather.gov/images/xml_logo.gif</credit-logo>
 *           <feedback>http://www.weather.gov/feedback.php</feedback>
 *       </source>
 *   </head>
 *   <data>
 *       <location>
 *           <location-key>point1</location-key>
 *           <point latitude="34.25" longitude="-119.20"/>
 *       </location>
 *       <moreWeatherInformation applicable-location="point1">
 *           http://forecast.weather.gov/MapClick.php?textField1=34.25&textField2=-119.20
 *       </moreWeatherInformation>
 *       <time-layout time-coordinate="local" summarization="none">
 *           <layout-key>k-p3h-n36-1</layout-key>
 *           <start-valid-time>2014-03-28T08:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-28T11:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-28T14:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-28T17:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-28T20:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-28T23:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-29T02:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-29T05:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-29T08:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-29T11:00:00-07:00</start-valid-time>
 *       </time-layout>
 *       <time-layout time-coordinate="local" summarization="none">
 *           <layout-key>k-p3h-n20-2</layout-key>
 *           <start-valid-time>2014-03-28T08:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-28T11:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-28T14:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-28T17:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-28T20:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-28T23:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-29T02:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-29T05:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-29T08:00:00-07:00</start-valid-time>
 *           <start-valid-time>2014-03-29T11:00:00-07:00</start-valid-time>
 *        </time-layout>
 *        <time-layout time-coordinate="local" summarization="none">
 *           <layout-key>k-p24h-n8-3</layout-key>
 *            <start-valid-time>2014-03-28T01:00:00-07:00</start-valid-time>
 *            <end-valid-time>2014-03-28T05:00:00-07:00</end-valid-time>
 *            <start-valid-time>2014-03-28T05:00:00-07:00</start-valid-time>
 *            <end-valid-time>2014-03-29T05:00:00-07:00</end-valid-time>
 *            <start-valid-time>2014-03-29T05:00:00-07:00</start-valid-time>
 *       </time-layout>
 *        <parameters applicable-location="point1">
 *            <temperature type="hourly" units="Fahrenheit" time-layout="k-p3h-n36-1">
 *                <name>Temperature</name>
 *                <value>49</value>
 *                <value>61</value>
 *                <value>66</value>
 *                <value>65</value>
 *                <value>57</value>
 *                <value>55</value>
 *                <value>52</value>
 *                <value>49</value>
 *                <value>50</value>
 *            </temperature>
 *            <wind-speed type="sustained" units="knots" time-layout="k-p3h-n36-1">
 *                <name>Wind Speed</name>
 *                <value>5</value>
 *                <value>10</value>
 *                <value>10</value>
 *                <value>10</value>
 *                <value>10</value>
 *                <value>2</value>
 *                <value>2</value>
 *                <value>2</value>
 *                <value>5</value>
 *                <value>13</value>
 *            </wind-speed>
 *            <direction type="wind" units="degrees true" time-layout="k-p3h-n36-1">
 *                <name>Wind Direction</name>
 *                <value>50</value>
 *                <value>240</value>
 *                <value>240</value>
 *                <value>240</value>
 *                <value>240</value>
 *                <value>340</value>
 *                <value>340</value>
 *                <value>40</value>
 *                <value>250</value>
 *                <value>250</value>
 *            </direction>
 *            <cloud-amount type="total" units="percent" time-layout="k-p3h-n36-1">
 *                <name>Cloud Cover Amount</name>
 *                <value>29</value>
 *                <value>26</value>
 *                <value>26</value>
 *                <value>27</value>
 *                <value>27</value>
 *                <value>72</value>
 *                <value>72</value>
 *                <value>85</value>
 *                <value>85</value>
 *                <value>35</value>
 *            </cloud-amount>
 *            <fire-weather type="risk from wind and relative humidity" time-layout="k-p24h-n8-3">
 *                <name>
 *                    Fire Weather Outlook from Wind and Relative Humidity
 *                </name>
 *                <value>No Areas</value>
 *                <value>No Areas</value>
 *                <value>No Areas</value>
 *            </fire-weather>
 *            <wind-speed type="gust" units="knots" time-layout="k-p3h-n20-2">
 *                <name>Wind Speed Gust</name>
 *                <value>5</value>
 *                <value>14</value>
 *                <value>14</value>
 *                <value>14</value>
 *                <value>14</value>
 *                <value>2</value>
 *                <value>2</value>
 *                <value>2</value>
 *                <value>5</value>
 *                <value>18</value>
 *            </wind-speed>
 *            <humidity type="relative" units="percent" time-layout="k-p3h-n36-1">
 *                <name>Relative Humidity</name>
 *                <value>93</value>
 *                <value>65</value>
 *                <value>56</value>
 *                <value>63</value>
 *                <value>83</value>
 *                <value>89</value>
 *                <value>92</value>
 *                <value>100</value>
 *                <value>100</value>
 *                <value>67</value>
 *            </humidity>
 *            <weather time-layout="k-p3h-n36-1">
 *                <name>Weather Type, Coverage, and Intensity</name>
 *                <weather-conditions/>
 *                <weather-conditions/>
 *                <weather-conditions/>
 *                <weather-conditions/>
 *                <weather-conditions/>
 *                <weather-conditions>
 *                    <value coverage="patchy" intensity="none" weather-type="fog" qualifier="none">
 *                        <visibility xsi:nil="true"/>
 *                    </value>
 *                </weather-conditions>
 *                <weather-conditions>
 *                    <value coverage="patchy" intensity="none" weather-type="fog" qualifier="none">
 *                        <visibility xsi:nil="true"/>
 *                    </value>
 *                </weather-conditions>
 *                <weather-conditions>
 *                    <value coverage="patchy" intensity="none" weather-type="fog" qualifier="none">
 *                        <visibility xsi:nil="true"/>
 *                    </value>
 *                </weather-conditions>
 *                <weather-conditions>
 *                    <value coverage="patchy" intensity="none" weather-type="fog" qualifier="none">
 *                        <visibility xsi:nil="true"/>
 *                    </value>
 *                </weather-conditions>
 *                <weather-conditions/>
 *            </weather>
 *        </parameters>
 *    </data>
 *</dwml>
 * } </pre>
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages(
        {
            "# {0} - reason",
            "err.cannot.import.forecast=Cannot import forecast. {0}",})
public class DwmlParser {

    public static final String DWML_SCHEMA_URI = "http://www.nws.noaa.gov/forecasts/xml/DWMLgen/schema/DWML.xsd";
    public static final String TAG_HEAD = "/dwml/head";
    public static final String TAG_DATA = "/dwml/data";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_TIME_LAYOUT = "time-layout";
    public static final String TAG_PARAMETERS = "parameters";
    public static final String TAG_TEMPERATURE = "temperature";
    public static final String TAG_MORE_WX_INFO = "moreWeatherInformation";
    private static final Logger logger = Logger.getLogger(DwmlParser.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    public static Field parseDwml(String xmlString) {
        try {
            Document doc = XmlUtil.getDoc(xmlString);
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node headNode = (Node) xpath.evaluate(TAG_HEAD, doc, XPathConstants.NODE);
            Node dataNode = (Node) xpath.evaluate(TAG_DATA, doc, XPathConstants.NODE);

            Map<String, Coord2D> locations
                    = parseLocations(
                            (NodeList) xpath.evaluate(TAG_LOCATION, dataNode, XPathConstants.NODESET));
            Map<String, ArrayList<OffsetDateTime>> timeMap
                    = parseTimeLayouts(
                            (NodeList) xpath.evaluate(TAG_TIME_LAYOUT, dataNode, XPathConstants.NODESET));

            for (String location : locations.keySet()) {
                // Select the parameters node that has the given location attribute
                Node paramsNode = (Node) xpath.compile("parameters[@applicable-location='" + location + "']").evaluate(dataNode, XPathConstants.NODE);

                Map<String, ArrayList<Real>> tempMap
                        = parseTemperatures(
                                (Node) xpath.evaluate("temperature", paramsNode, XPathConstants.NODE));

                Map<String, ArrayList<Real>> humidityMap
                        = parseRelativeHumidity(
                                (Node) xpath.evaluate("humidity[@type='relative']", paramsNode, XPathConstants.NODE));

                Map<String, ArrayList<Real>> windSpdMap
                        = parseWindSpeeds(
                                (Node) xpath.evaluate("wind-speed[@type='sustained']", paramsNode, XPathConstants.NODE));

                Map<String, ArrayList<Real>> windDirMap
                        = parseWindDirections(
                                (Node) xpath.evaluate("direction", paramsNode, XPathConstants.NODE));

                Map<String, ArrayList<Real>> cloudCoverMap
                        = parseCloudCover(
                                (Node) xpath.evaluate("cloud-amount", paramsNode, XPathConstants.NODE));

                Map<String, ArrayList<Real>> windGustMap
                        = parseWindSpeeds(
                                (Node) xpath.evaluate("wind-speed[@type='gust']", paramsNode, XPathConstants.NODE));

                
                
//                printReals(tempMap);
//                printReals(humidityMap);
//                printReals(windSpdMap);
//                printReals(windGustMap);
//                printReals(windDirMap);
//                printReals(cloudCoverMap);

            }

        } catch (XPathExpressionException ex) {
            Exceptions.printStackTrace(ex);

        }
        return null;
    }

    /**
     *
     * @param nodes The {@code location} NodeList.
     * @return
     * @throws XPathExpressionException
     */
    static Map<String, Coord2D> parseLocations(NodeList nodes) throws XPathExpressionException {
        HashMap<String, Coord2D> map = new HashMap<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (int i = 0; i < nodes.getLength(); i++) {
            String key = xpath.evaluate("location-key", nodes.item(i));
            String lat = xpath.evaluate("point/@latitude", nodes.item(i));
            String lon = xpath.evaluate("point/@longitude", nodes.item(i));
            map.put(key, GeoCoord2D.fromDegrees(Double.parseDouble(lat), Double.parseDouble(lon)));
        }
        return map;
    }

    /**
     *
     * @param nodes The {@code time-layout} NodeList.
     * @return
     * @throws XPathExpressionException
     */
    static Map<String, ArrayList<OffsetDateTime>> parseTimeLayouts(NodeList nodes) throws XPathExpressionException {
        HashMap<String, ArrayList<OffsetDateTime>> map = new HashMap<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (int i = 0; i < nodes.getLength(); i++) {
            String key = xpath.evaluate("layout-key", nodes.item(i));
            NodeList times = (NodeList) xpath.evaluate("start-valid-time", nodes.item(i), XPathConstants.NODESET);
            ArrayList<OffsetDateTime> values = new ArrayList<>();
            for (int j = 0; j < times.getLength(); j++) {
                String time = times.item(j).getTextContent();
                values.add(OffsetDateTime.parse(time));
            }
            map.put(key, values);
        }
        return map;
    }

    /**
     *
     * @param node The {@code temperature} Node
     * @return
     * @throws XPathExpressionException
     */
    static Map<String, ArrayList<Real>> parseTemperatures(Node node) throws XPathExpressionException {
        HashMap<String, ArrayList<Real>> map = new HashMap<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        String key = xpath.evaluate("@time-layout", node);
        String units = xpath.evaluate("@units", node);
        RealType type = units.equals("Fahrenheit") ? WeatherType.AIR_TEMP_F : WeatherType.AIR_TEMP_C;
        NodeList values = (NodeList) xpath.evaluate("value", node, XPathConstants.NODESET);
        ArrayList<Real> reals = createRealsArray(values, type);
        map.put(key, reals);
        return map;
    }

    /**
     *
     * @param node The sustained {@code wind-speed} Node
     * @return
     * @throws XPathExpressionException
     */
    static Map<String, ArrayList<Real>> parseWindSpeeds(Node node) throws XPathExpressionException {
        HashMap<String, ArrayList<Real>> map = new HashMap<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        String key = xpath.evaluate("@time-layout", node);
        String units = xpath.evaluate("@units", node);
        RealType type = units.equals("knots") ? WeatherType.WIND_SPEED_KTS : WeatherType.WIND_SPEED_SI;
        NodeList values = (NodeList) xpath.evaluate("value", node, XPathConstants.NODESET);
        ArrayList<Real> reals = createRealsArray(values, type);
        map.put(key, reals);
        return map;
    }

    /**
     *
     * @param node The wind {@code direction} Node
     * @return
     * @throws XPathExpressionException
     */
    static Map<String, ArrayList<Real>> parseWindDirections(Node node) throws XPathExpressionException {
        HashMap<String, ArrayList<Real>> map = new HashMap<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        String key = xpath.evaluate("@time-layout", node);
        NodeList values = (NodeList) xpath.evaluate("value", node, XPathConstants.NODESET);
        ArrayList<Real> reals = createRealsArray(values, WeatherType.WIND_DIR);
        map.put(key, reals);
        return map;
    }

    /**
     *
     * @param node The relative {@code humidity} Node
     * @return
     * @throws XPathExpressionException
     */
    static Map<String, ArrayList<Real>> parseRelativeHumidity(Node node) throws XPathExpressionException {
        HashMap<String, ArrayList<Real>> map = new HashMap<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        String key = xpath.evaluate("@time-layout", node);
        NodeList values = (NodeList) xpath.evaluate("value", node, XPathConstants.NODESET);
        ArrayList<Real> reals = createRealsArray(values, WeatherType.REL_HUMIDITY);
        map.put(key, reals);
        return map;
    }

    /**
     *
     * @param node The {@code cloud-amount} Node
     * @return
     * @throws XPathExpressionException
     */
    static Map<String, ArrayList<Real>> parseCloudCover(Node node) throws XPathExpressionException {
        HashMap<String, ArrayList<Real>> map = new HashMap<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        String key = xpath.evaluate("@time-layout", node);
        NodeList values = (NodeList) xpath.evaluate("value", node, XPathConstants.NODESET);
        ArrayList<Real> reals = createRealsArray(values, WeatherType.CLOUD_COVER);
        map.put(key, reals);
        return map;
    }

    private static ArrayList<Real> createRealsArray(NodeList values, RealType type) throws DOMException, NumberFormatException {
        ArrayList<Real> reals = new ArrayList<>();
        for (int j = 0; j < values.getLength(); j++) {
            String num = values.item(j).getTextContent();
            reals.add(new Real(type, Double.parseDouble(num)));
        }
        return reals;
    }

    static void printReals(Map<String, ArrayList<Real>> map) {

        for (Map.Entry<String, ArrayList<Real>> entry : map.entrySet()) {
            System.out.println("<Key: " + entry.getKey() + ">");
            System.out.println("<Values>");
            List<Real> list = entry.getValue();
            for (Real r : list) {
                System.out.println(" > " + r.toValueString());
            }
        }

    }
}
