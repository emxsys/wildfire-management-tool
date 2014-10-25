/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com> 
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
package com.emxsys.wmt.cps.data;

import com.emxsys.wmt.cps.fireground.WildlandFireground;
import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.gml.GmlBuilder;
import com.emxsys.gis.gml.GmlParser;
import com.emxsys.wildfire.api.FuelModelProvider;
import com.emxsys.wildfire.api.StdFuelModelProvider;
import com.emxsys.wildfire.spi.FuelModelProviderFactory;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The FiregroundXmlEncoder is responsible for reading and writing the fireground.xml file. Example:
 * <pre>{@code
 * <Fireground xmlns="http://emxsys.com/fireground"
 *              xmlns:gml="http://www.opengis.net/gml"
 *              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *              xsi:schemaLocation="http://emxsys.com/fireground FiregroundSchema.xsd">
 *     <Sectors>
 *         <Sector actory="com.emxsys.wmt.cps.data.markers.pushpins.Pushpin$Builder">
 *             <gml:Box srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
 *                 <gml:coord>
 *                     <gml:X>-119.29632881589397</gml:X>
 *                     <gml:Y>34.28389400988058</gml:Y>
 *                 </gml:coord>
 *                 <gml:coord>
 *                     <gml:X>-119.27115290637133</gml:X>
 *                     <gml:Y>34.29907777245651</gml:Y>
 *                 </gml:coord>
 *             </gml:Box>
 *             <FuelModelProvider providerClass="..." model="..."/>
 *         </Sector>
 *     </Sectors>
 *     <Fuels>
 *         <Substitute original="" subsitution=""/>
 *     </Fuels>
 * </Fireground>
 * }</pre>
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class FiregroundXmlEncoder {

    public static final String FIREGROUND_NS_URI = "http://emxsys.com/fireground";
    public static final String FIREGROUND_SCHEMA_FILE = "FiregroundSchema.xsd";
    public static final String TAG_ROOT = "Fireground";
    public static final String TAG_SECTORS = "Sectors";
    public static final String TAG_SECTOR = "Sector";
    public static final String TAG_FUEL_MODEL_PROVIDER = "FuelModelProvider";
    public static final String ATTR_CLASS = "class";
    public static final String ATTR_MODEL = "model";
    private static final Logger logger = Logger.getLogger(FiregroundXmlEncoder.class.getName());

    /**
     * Returns a list of sectors extracted from the supplied document
     *
     * @param firegroundDoc The fireground document to parse.
     * @return A collection of sector, fuel model provider pairs.
     */
    public static Map<Box, FuelModelProvider> parseSectors(Document firegroundDoc) {
        final HashMap<Box, FuelModelProvider> sectors = new HashMap<>();

        if (firegroundDoc != null) {
            final NodeList sectorNodes = firegroundDoc.getElementsByTagName(TAG_SECTOR);
            for (int i = 0; i < sectorNodes.getLength(); i++) {
                Element sectorNode = (Element) sectorNodes.item(i);
                GeoSector box = parseBox(sectorNode);
                FuelModelProvider provider = parseFuelModelProvider(sectorNode);
                sectors.put(box, provider);
            }
        }
        return sectors;
    }

    /**
     * Parse the Sector's gml:Box element into a GeoSector.
     *
     * @param sectorNode The GML sector node to parse.
     * @return The sector defined by the XML elements; it will contain "missing" values if invalid.
     */
    private static GeoSector parseBox(Element sectorNode) {
        if (sectorNode == null) {
            return new GeoSector();
        }
        return GmlParser.parseBox(sectorNode);
    }

    /**
     * Parse the Sector's FuelModelProvider element.
     *
     * @param sectorNode The sector node containing a FuelModelProvider.
     * @return A FuelModelProvider instance or null if invalid.
     */
    private static FuelModelProvider parseFuelModelProvider(Element sectorNode) {
        if (sectorNode == null) {
            logger.warning("Sector element is null.");
            return null;
        }
        NodeList providers = sectorNode.getElementsByTagName(TAG_FUEL_MODEL_PROVIDER);
        if (providers.getLength() == 0) {
            return null;
        }
        Element providerNode = (Element) providers.item(0);
        String className = providerNode.getAttribute(ATTR_CLASS);
        String fuelModelNo = providerNode.getAttribute(ATTR_MODEL);
        return FuelModelProviderFactory.getInstance(className, fuelModelNo);
    }

//    @SuppressWarnings("UseSpecificCatch")
//    private static FuelModelProvider createFuelModelProvider(String className, String fuelModelNo) {
//        try {
//            // Get the FuelModelProvider subclass
//            Class clazz = Class.forName(className);
//            if (!FuelModelProvider.class.isAssignableFrom(clazz)) {
//                throw new IllegalArgumentException(className + " must be a FuelModelProvider subclass.");
//            }
//            if (fuelModelNo.isEmpty()) {
//                // Invoke the default constructor
//                return (FuelModelProvider) clazz.newInstance();
//            } else {
//                // Invoke the constructor that takes an int fuel model no
//                // See StdFuelModelProvider(Integer)
//                Constructor ctor = clazz.getDeclaredConstructor(Integer.class);
//                ctor.setAccessible(true);
//                return (FuelModelProvider) ctor.newInstance(Integer.parseInt(fuelModelNo));
//            }
//        } catch (Exception ex) {
//            logger.log(Level.SEVERE, "Cannot create fuel model provider.", ex);
//        }
//        return null;
//    }

    /**
     * Writes the sector(s) in the given fireground to the supplied document.
     *
     * @param doc The document to update.
     * @param fireground The fireground containing sectors to write.
     */
    public static synchronized void encodeSectors(final Document doc, final WildlandFireground fireground) {

        // Get the Sectors node -- create it if necessary
        Node sectorsNode = doc.getElementsByTagName(TAG_SECTORS).item(0);
        if (sectorsNode == null) {
            sectorsNode = doc.createElement(TAG_SECTORS);
            final Node root = doc.getElementsByTagName(TAG_ROOT).item(0);
            root.appendChild(sectorsNode);
        }

        // Clear the existing Sectors list...
        final NodeList children = sectorsNode.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            sectorsNode.removeChild(children.item(i));
        }
        // ... and encode the current values 
        final List<Box> sectors = fireground.getSectors();
        for (final Box sector : sectors) {
            // Add a gml:Box to the Sector tag.
            GmlBuilder gmlBuilder = new GmlBuilder(doc, TAG_SECTOR);
            gmlBuilder.append(sector);
            Element sectorElem = gmlBuilder.toElement();

            // Add Fuel Model Provider element
            FuelModelProvider provider = fireground.getFuelModelProvider(sector);
            if (provider != null) {
                Element providerElem = doc.createElement(TAG_FUEL_MODEL_PROVIDER);
                providerElem.setAttribute(ATTR_CLASS, provider.getClass().getName());
                if (provider instanceof StdFuelModelProvider) {
                    int fuelModelNo = ((StdFuelModelProvider) provider).getFuelModelNo();
                    providerElem.setAttribute(ATTR_MODEL, Integer.toString(fuelModelNo));
                }
                sectorElem.appendChild(providerElem);
            }
            sectorsNode.appendChild(sectorElem);
        }

    }
}
