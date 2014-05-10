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
import com.emxsys.wmt.gis.api.Box;
import com.emxsys.wmt.gis.api.GeoSector;
import com.emxsys.wmt.gis.gml.GmlBuilder;
import com.emxsys.wmt.gis.gml.GmlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class FiregroundXmlEncoder
{
    public static final String FIREGROUND_NS_URI = "http://emxsys.com/fireground";
    public static final String FIREGROUND_SCHEMA_FILE = "FiregroundSchema.xsd";
    public static final String TAG_ROOT = "Fireground";
    public static final String TAG_SECTORS = "Sectors";
    public static final String TAG_SECTOR = "Sector";
    public static final String TAG_FUEL_MODEL_PROVIDER_ATTR = "FuelModelProvider";
    public static final String TAG_MOVABLE = "movable";
    public static final String TAG_NORMAL_ATTR = "normalAttributes";
    public static final String TAG_HIGHLIGHT_ATTR = "highlightAttributes";
    private static final Logger logger = Logger.getLogger(FiregroundXmlEncoder.class.getName());


    public static List<Box> parseSectors(Document fgDoc)
    {
        final List<Box> sectors = new ArrayList<>();

        if (fgDoc != null)
        {
            final NodeList sectorNodes = fgDoc.getElementsByTagName(TAG_SECTOR);
            for (int i = 0; i < sectorNodes.getLength(); i++)
            {
                sectors.add(parseSector((Element) sectorNodes.item(i)));
            }
        }
        return sectors;
    }


    /**
     * Parse the Sector elements into a GeoSector: e.g.,
     *
     * @param sectorNode
     * @return the sector defined by the XML elements; it will contain "missing" values if invalid.
     */
    private static GeoSector parseSector(Element sectorNode)
    {
        if (sectorNode == null)
        {
            return new GeoSector();
        }
        return GmlParser.parseBox(sectorNode);
    }


    public static synchronized void encodeSectors(final Document doc,
        final WildlandFireground fireground)
    {

        // Get the Sectors node -- create it if necessary
        Node sectorsNode = doc.getElementsByTagName(TAG_SECTORS).item(0);
        if (sectorsNode == null)
        {
            sectorsNode = doc.createElement(TAG_SECTORS);
            final Node root = doc.getElementsByTagName(TAG_ROOT).item(0);
            root.appendChild(sectorsNode);
        }

        // Clear the existing Sectors list...
        final NodeList children = sectorsNode.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--)
        {
            sectorsNode.removeChild(children.item(i));
        }
        // ... and encode the current values 
        final List<Box> sectors = fireground.getSectors();
        for (final Box sector : sectors)
        {
            GmlBuilder gmlBuilder = new GmlBuilder(doc, TAG_SECTOR);
            gmlBuilder.append(sector);
            Element sectorElem = gmlBuilder.toElement();
            sectorsNode.appendChild(sectorElem);            
        }

    }
}
