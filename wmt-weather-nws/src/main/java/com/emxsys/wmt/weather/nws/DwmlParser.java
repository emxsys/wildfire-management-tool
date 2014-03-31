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
import com.emxsys.wmt.util.KeyValue;
import com.emxsys.wmt.util.XmlUtil;
import com.emxsys.wmt.weather.api.WeatherType;
import static com.emxsys.wmt.weather.nws.NwsTypes.*;
import java.rmi.RemoteException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DDoubleSet;
import visad.Real;
import visad.RealType;
import visad.Set;
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
 *           ...
 *       </time-layout>
 *       <time-layout time-coordinate="local" summarization="none">
 *           <layout-key>k-p3h-n20-2</layout-key>
 *           <start-valid-time>2014-03-28T08:00:00-07:00</start-valid-time>
 *           ...
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
 *                ...
 *            </temperature>
 *            <wind-speed type="sustained" units="knots" time-layout="k-p3h-n36-1">
 *                <name>Wind Speed</name>
 *                <value>5</value>
 *                ...
 *            </wind-speed>
 *            <direction type="wind" units="degrees true" time-layout="k-p3h-n36-1">
 *                <name>Wind Direction</name>
 *                <value>50</value>
 *                ...
 *            </direction>
 *            <cloud-amount type="total" units="percent" time-layout="k-p3h-n36-1">
 *                <name>Cloud Cover Amount</name>
 *                <value>29</value>
 *                ...
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
 *                ...
 *            </wind-speed>
 *            <humidity type="relative" units="percent" time-layout="k-p3h-n36-1">
 *                <name>Relative Humidity</name>
 *                <value>93</value>
 *                ...
 *            </humidity>
 *            <weather time-layout="k-p3h-n36-1">
 *                <name>Weather Type, Coverage, and Intensity</name>
 *                <weather-conditions>
 *                    <value coverage="patchy" intensity="none" weather-type="fog" qualifier="none">
 *                        <visibility xsi:nil="true"/>
 *                    </value>
 *                </weather-conditions>
 *                <weather-conditions/>
 *                ...
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
    private static final Logger logger = Logger.getLogger(DwmlParser.class.getName());

    private final Document doc;
    private final XPath xpath;

    static {
        logger.setLevel(Level.ALL);
    }

    /**
     * Constructs a parser for the given XML document.
     * @param xmlString
     */
    public DwmlParser(String xmlString) {
        doc = XmlUtil.getDoc(xmlString);
        xpath = XPathFactory.newInstance().newXPath();
    }

    /**
     * Parses the document.
     * @return An array of FlatFields.
     */
    public List<FlatField> parse() {
        try {
            Node headNode = (Node) xpath.evaluate(TAG_HEAD, doc, XPathConstants.NODE);
            Node dataNode = (Node) xpath.evaluate(TAG_DATA, doc, XPathConstants.NODE);

            Map<String, Coord2D> locations = parseLocations(dataNode);
            Map<String, ArrayList<OffsetDateTime>> timeLayouts = parseTimeLayouts(dataNode);

            // Create a FlatField for each point
            ArrayList<FlatField> fields = new ArrayList<>();
            for (String location : locations.keySet()) {
                // Select the parameters node that has the given location attribute
                String paramExp = "parameters[@applicable-location='" + location + "']";
                Node paramsNode = (Node) xpath.compile(paramExp).evaluate(dataNode, XPathConstants.NODE);

                // Read the values into Real arrays using the native units
                KeyValue<String, ArrayList<Real>> airTemps = parseTemperatures(paramsNode);
                KeyValue<String, ArrayList<Real>> humidities = parseRelativeHumidity(paramsNode);
                KeyValue<String, ArrayList<Real>> windSpeeds = parseWindSpeeds(paramsNode, "sustained");
                KeyValue<String, ArrayList<Real>> windGusts = parseWindSpeeds(paramsNode, "gust");
                KeyValue<String, ArrayList<Real>> directions = parseWindDirections(paramsNode);
                KeyValue<String, ArrayList<Real>> cloudCover = parseCloudCover(paramsNode);

                // Now create the Function: ( time -> ( weather ) )
                // Create the time domain samples (using the time-layout from the temperatures)
                ArrayList<OffsetDateTime> times = timeLayouts.get(airTemps.getKey());
                double[][] timeSamples = new double[1][times.size()];
                for (int i = 0; i < times.size(); i++) {
                    timeSamples[0][i] = times.get(i).toEpochSecond();
                }
                // Create the wx range samples, converting the units as necessary.
                double[][] wxSamples = new double[WX_TUPLE_TYPE.getDimension()][times.size()];
                for (int dim = 0; dim < WX_TUPLE_TYPE.getDimension(); dim++) {
                    Unit defaultUnit = ((RealType) (WX_TUPLE_TYPE.getComponent(dim))).getDefaultUnit();
                    ArrayList<Real> values;
                    if (dim == AIR_TEMP_IDX) {
                        values = airTemps.getValue();
                    } else if (dim == HUMIDITY_IDX) {
                        values = humidities.getValue();
                    } else if (dim == WIND_SPD_IDX) {
                        values = windSpeeds.getValue();
                    } else if (dim == WIND_DIR_IDX) {
                        values = directions.getValue();
                    } else if (dim == CLOUD_CVR_IDX) {
                        values = cloudCover.getValue();
                    } else {
                        throw new IllegalStateException("unprocessed tuple index: " + dim);
                    }
                    for (int i = 0; i < times.size(); i++) {
                        wxSamples[dim][i] = values.get(i).getValue(defaultUnit);
                    }
                }
                // Create the wx range samples, converting the units as necessary.
//                double[][] wxSamples = new double[WX_TUPLE_TYPE.getDimension()][times.size()];
//                for (int dim = 0; dim < WX_TUPLE_TYPE.getDimension(); dim++) {
//                    for (int i = 0; i < times.size(); i++) {
//                        if (dim == AIR_TEMP_IDX) {
//                            Unit defaultUnit = ((RealType) (WX_TUPLE_TYPE.getComponent(AIR_TEMP_IDX))).getDefaultUnit();
//                            wxSamples[dim][i] = airTemps.getValue().get(i).getValue(defaultUnit);
//                        } else if (dim == HUMIDITY_IDX) {
//                            wxSamples[dim][i] = humidities.getValue().get(i).getValue();
//                        } else if (dim == WIND_SPD_IDX) {
//                            Unit defaultUnit = ((RealType) (WX_TUPLE_TYPE.getComponent(WIND_SPD_IDX))).getDefaultUnit();
//                            wxSamples[dim][i] = windSpeeds.getValue().get(i).getValue(defaultUnit);
//                        } else if (dim == WIND_DIR_IDX) {
//                            wxSamples[dim][i] = directions.getValue().get(i).getValue();
//                        } else if (dim == CLOUD_CVR_IDX) {
//                            wxSamples[dim][i] = cloudCover.getValue().get(i).getValue();
//                        } else {
//                            throw new IllegalStateException("unprocessed tuple index: " + dim );
//                        }
//                    }
//                }
                // Create the domain Set, a 1-D sequence with no regular interval.
                // Use Gridded1DDoubleSet(MathType type, double[][] samples, lengthX)
                Set timeSet = new Gridded1DDoubleSet(RealType.Time, timeSamples, timeSamples[0].length);

                // Create a MathType for the function ( ( time ) -> ( air_temp, RH, wind_spd, ... ) )
                FunctionType wxFuncOfTime = new FunctionType(RealType.Time, WX_TUPLE_TYPE);

                // Create the FlatField.
                // Use FlatField(FunctionType type, Set domain_set)
                FlatField ff = new FlatField(wxFuncOfTime, timeSet);
                // ...and put the weather values above into it
                ff.setSamples(wxSamples);

                fields.add(ff);
            }
            return fields;
        } catch (XPathExpressionException | VisADException | RemoteException ex) {
            logger.severe(ex.getMessage());
        }
        return null;
    }

    /**
     *
     * @param dataNode The node containing the {@code location} NodeList.
     * @return
     * @throws XPathExpressionException
     */
    Map<String, Coord2D> parseLocations(Node dataNode) throws XPathExpressionException {
        HashMap<String, Coord2D> map = new HashMap<>();
        NodeList nodes = (NodeList) xpath.evaluate("location", dataNode, XPathConstants.NODESET);
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
     * @param dataNode The node containing the {@code time-layout} NodeList.
     * @return
     * @throws XPathExpressionException
     */
    Map<String, ArrayList<OffsetDateTime>> parseTimeLayouts(Node dataNode) throws XPathExpressionException {
        HashMap<String, ArrayList<OffsetDateTime>> map = new HashMap<>();
        NodeList nodes = (NodeList) xpath.evaluate("time-layout", dataNode, XPathConstants.NODESET);
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
     * @param paramsNode The node containing the {@code temperature} Node
     * @return
     * @throws XPathExpressionException
     */
    KeyValue<String, ArrayList<Real>> parseTemperatures(Node paramsNode) throws XPathExpressionException {
        Node node = (Node) xpath.evaluate("temperature", paramsNode, XPathConstants.NODE);
        String key = xpath.evaluate("@time-layout", node);
        String units = xpath.evaluate("@units", node);
        RealType type = units.equals("Fahrenheit") ? WeatherType.AIR_TEMP_F : WeatherType.AIR_TEMP_C;
        NodeList values = (NodeList) xpath.evaluate("value", node, XPathConstants.NODESET);
        ArrayList<Real> reals = createRealsArray(values, type);
        return new KeyValue<>(key, reals);
    }

    /**
     *
     * @param paramsNode The node containing {@code wind-speed} Node
     * @param windType "sustained" or "gust".
     * @return
     * @throws XPathExpressionException
     */
    KeyValue<String, ArrayList<Real>> parseWindSpeeds(Node paramsNode, String windType) throws XPathExpressionException {
        Node node = (Node) xpath.evaluate("wind-speed[@type='" + windType + "']", paramsNode, XPathConstants.NODE);
        String key = xpath.evaluate("@time-layout", node);
        String units = xpath.evaluate("@units", node);
        RealType type = units.equals("knots") ? WeatherType.WIND_SPEED_KTS : WeatherType.WIND_SPEED_SI;
        NodeList values = (NodeList) xpath.evaluate("value", node, XPathConstants.NODESET);
        ArrayList<Real> reals = createRealsArray(values, type);
        return new KeyValue<>(key, reals);
    }

    /**
     *
     * @param paramsNode The node containing the wind {@code direction} Node
     * @return
     * @throws XPathExpressionException
     */
    KeyValue<String, ArrayList<Real>> parseWindDirections(Node paramsNode) throws XPathExpressionException {
        Node node = (Node) xpath.evaluate("direction", paramsNode, XPathConstants.NODE);
        String key = xpath.evaluate("@time-layout", node);
        NodeList values = (NodeList) xpath.evaluate("value", node, XPathConstants.NODESET);
        ArrayList<Real> reals = createRealsArray(values, WeatherType.WIND_DIR);
        return new KeyValue<>(key, reals);
    }

    /**
     *
     * @param paramsNode The node containing the relative {@code humidity} Node
     * @return
     * @throws XPathExpressionException
     */
    KeyValue<String, ArrayList<Real>> parseRelativeHumidity(Node paramsNode) throws XPathExpressionException {
        Node node = (Node) xpath.evaluate("humidity[@type='relative']", paramsNode, XPathConstants.NODE);
        String key = xpath.evaluate("@time-layout", node);
        NodeList values = (NodeList) xpath.evaluate("value", node, XPathConstants.NODESET);
        ArrayList<Real> reals = createRealsArray(values, WeatherType.REL_HUMIDITY);
        return new KeyValue<>(key, reals);
    }

    /**
     *
     * @param paramsNode The node containing the {@code cloud-amount} Node
     * @return
     * @throws XPathExpressionException
     */
    KeyValue<String, ArrayList<Real>> parseCloudCover(Node paramsNode) throws XPathExpressionException {
        HashMap<String, ArrayList<Real>> map = new HashMap<>();
        Node node = (Node) xpath.evaluate("cloud-amount", paramsNode, XPathConstants.NODE);
        String key = xpath.evaluate("@time-layout", node);
        NodeList values = (NodeList) xpath.evaluate("value", node, XPathConstants.NODESET);
        ArrayList<Real> reals = createRealsArray(values, WeatherType.CLOUD_COVER);
        return new KeyValue<>(key, reals);
    }

    static ArrayList<Real> createRealsArray(NodeList values, RealType type) throws DOMException, NumberFormatException {
        ArrayList<Real> reals = new ArrayList<>();
        for (int j = 0; j < values.getLength(); j++) {
            String num = values.item(j).getTextContent();
            reals.add(new Real(type, Double.parseDouble(num)));
        }
        return reals;
    }

    static void printReals(Map<String, ArrayList<Real>> map) {

        map.entrySet().stream().map((entry) -> {
            System.out.println("<Key: " + entry.getKey() + ">");
            return entry;
        }).map((entry) -> {
            System.out.println("<Values>");
            return entry;
        }).map((entry) -> entry.getValue()).forEach((list) -> {
            list.stream().forEach((r) -> {
                System.out.println(" > " + r.toValueString());
            });
        });

    }
}
