/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Utility class for interacting with XML documents.
 *
 * @author Bruce Schubert
 * @version $Id$
 */
public class XmlUtil
{
    private static final Logger logger = Logger.getLogger(XmlUtil.class.getName());


    /**
     * Gets the first child element with the matching tag.
     *
     * @param element containing children
     * @param namespaceURI namespace URI (e.g., "http://emxsys.com/worldwind-shape-attributes")
     * @param tag to search for
     * @return the first Element in the NodeList, or null if not found.
     */
    public static Element getChildElement(Element element, String namespaceURI, String tag)
    {
        NodeList nodes = element.getElementsByTagNameNS(namespaceURI, tag);
        if (nodes != null && nodes.getLength() > 0)
        {
            return (Element) nodes.item(0);
        }
        return null;
    }


    public static Boolean getChildElementBoolean(Element element, String namespaceURI, String tag)
    {
        Element childElement = getChildElement(element, namespaceURI, tag);
        if (childElement == null)
        {
            return null;
        }
        return Boolean.valueOf(childElement.getTextContent());
    }


    public static Double getChildElementDouble(Element element, String namespaceURI, String tag)
    {
        Element childElement = getChildElement(element, namespaceURI, tag);
        if (childElement == null)
        {
            return null;
        }
        return Double.valueOf(childElement.getTextContent());
    }


    public static Integer getChildElementInteger(Element element, String namespaceURI, String tag)
    {
        Element childElement = getChildElement(element, namespaceURI, tag);
        if (childElement == null)
        {
            return null;
        }
        return Integer.valueOf(childElement.getTextContent());
    }


    public static String getChildElementText(Element element, String namespaceURI, String tag)
    {
        Element childElement = getChildElement(element, namespaceURI, tag);
        if (childElement == null)
        {
            return null;
        }
        return childElement.getTextContent();
    }




    public static Element createDoubleElement(Document doc, String namespaceURI, double value, String tag)
    {
        try
        {
            Element element = doc.createElementNS(namespaceURI, tag);
            element.appendChild(doc.createTextNode(Double.toString(value)));
            return element;
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "createDoubleElement failed! Unable to export value ({0}). Reason: {1}", new Object[]
            {
                Double.toString(value), ex.toString()
            });
        }
        return null;
    }


    public static Element createBooleanElement(Document doc, String namespaceURI, boolean value, String tag)
    {
        try
        {
            Element element = doc.createElementNS(namespaceURI, tag);
            element.appendChild(doc.createTextNode(Boolean.toString(value)));
            return element;
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "createBooleanElement failed! Unable to export boolean ({0}). Reason: {1}", new Object[]
            {
                Boolean.toString(value), ex.toString()
            });
        }
        return null;
    }


    public static Element createIntegerElement(Document doc, String namespaceURI, int value, String tag)
    {
        try
        {
            Element element = doc.createElementNS(namespaceURI, tag);
            element.appendChild(doc.createTextNode(Integer.toString(value)));
            return element;
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "createIntegerElement failed! Unable to export integer ({0}). Reason: {1}", new Object[]
            {
                Integer.toString(value), ex.toString()
            });
        }
        return null;
    }


    public static Element createTextElement(Document doc, String namespaceURI, String text, String tag)
    {
        try
        {
            Element element = doc.createElementNS(namespaceURI, tag);
            element.appendChild(doc.createTextNode(text));
            return element;
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "createTextElement failed! Unable to export text ({0}). Reason: {1}", new Object[]
            {
                text, ex.toString()
            });
        }
        return null;
    }
}
