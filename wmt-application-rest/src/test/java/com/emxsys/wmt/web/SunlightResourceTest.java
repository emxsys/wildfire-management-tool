/*
 * Copyright (c) 2015, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.web;

import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.solar.api.BasicSunlight;
import com.emxsys.util.JsonUtil;
import com.emxsys.util.XmlUtil;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Bruce Schubert
 */
public class SunlightResourceTest extends JerseyTest {

    public SunlightResourceTest() throws Exception {
        super("com.emxsys.wmt.web");
    }

    @Test
    public void testSunlightResource1() {
        System.out.println("TESTING: URI Specified Representation: XML mime-type");
        String time = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        GeoCoord3D coord = GeoCoord3D.fromDegrees(34.25, -119.2);
        ClientResponse response = null;
        String entity = null;
        
        // The following fails (404) because the '?' is converted to "%3F".  Must use queryParams() instead.
        // com.sun.jersey.api.client.UniformInterfaceException: 
        // GET http://localhost:9998/sunlight%3Ftime=2015-04-13T13:34:25.426-07:00%5BAmerica/Los_Angeles%5D&latitude=34.25&longitude=-119.2 returned a response status of 404
        //        String responseMsg = this.webResource.path("sunlight"
        //            + "?time=" + time
        //            + "&latitude=" + coord.getLatitudeDegrees()
        //            + "&longitude=" + coord.getLongitudeDegrees()).getResource(String.class);
        response = super.webResource.path("sunlight")
                .queryParam("time", time)
                .queryParam("latitude", Double.toString(coord.getLatitudeDegrees()))
                .queryParam("longitude", Double.toString(coord.getLongitudeDegrees()))
                .queryParam("mime-type", MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertTrue("Expecting 200. Response Status: " + response.getStatus(), response.getStatus() == 200);
        assertTrue("Expecting XML. Response Type: " + response.getType(), response.getType().equals(MediaType.APPLICATION_XML_TYPE));
        entity = response.getEntity(String.class);
        assertTrue("Should look like XML:\n" + entity, entity.startsWith("<"));
    }

    @Test
    public void testSunlightResource2() {
        System.out.println("TESTING: Server-driven Negociation: Accepts XML");
        String time = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        GeoCoord3D coord = GeoCoord3D.fromDegrees(34.25, -119.2);
        ClientResponse response = null;
        String entity = null;
        
        response = super.webResource.path("sunlight")
                .queryParam("time", time)
                .queryParam("latitude", Double.toString(coord.getLatitudeDegrees()))
                .queryParam("longitude", Double.toString(coord.getLongitudeDegrees()))
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(ClientResponse.class);
        assertTrue("Expecting 200. Response Status: " + response.getStatus(), response.getStatus() == 200);
        assertTrue("Expecting XML. Response Type: " + response.getType(), response.getType().equals(MediaType.APPLICATION_XML_TYPE));
        entity = response.getEntity(String.class);
        assertTrue("Looks like XML:\n" + entity, entity.startsWith("<"));

        System.out.println(">>>> XML Representation:\n" + XmlUtil.format(entity));
    }

    @Test
    public void testSunlightResource3() {
        System.out.println("TESTING: URI Specified Representation: JSON mime-type");
        String time = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        GeoCoord3D coord = GeoCoord3D.fromDegrees(34.25, -119.2);
        ClientResponse response = null;
        String entity = null;

        response = super.webResource.path("sunlight")
                .queryParam("time", time)
                .queryParam("latitude", Double.toString(coord.getLatitudeDegrees()))
                .queryParam("longitude", Double.toString(coord.getLongitudeDegrees()))
                .queryParam("mime-type", MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue("Expecting 200. Response Status: " + response.getStatus(), response.getStatus() == 200);
        assertTrue("Expecting JSON. Response Type: " + response.getType(), response.getType().equals(MediaType.APPLICATION_JSON_TYPE));
        entity = response.getEntity(String.class);
        assertTrue("Looks like JSON:\n" + entity, entity.trim().startsWith("{"));

    }

    @Test
    public void testSunlightResource4() {
        System.out.println("TESTING: Server-driven Negociation: Accepts JSON");
        String time = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        GeoCoord3D coord = GeoCoord3D.fromDegrees(34.25, -119.2);
        ClientResponse response = null;
        String entity = null;

        response = super.webResource.path("sunlight")
                .queryParam("time", time)
                .queryParam("latitude", Double.toString(coord.getLatitudeDegrees()))
                .queryParam("longitude", Double.toString(coord.getLongitudeDegrees()))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
        assertTrue("Expecting 200. Response Status: " + response.getStatus(), response.getStatus() == 200);
        assertTrue("Expecting JSON. Response Type: " + response.getType(), response.getType().equals(MediaType.APPLICATION_JSON_TYPE));
        entity = response.getEntity(String.class);
        assertTrue("Looks like JSON:\n" + entity, entity.trim().startsWith("{"));

        System.out.println(">>>> JSON Representation:\n" + JsonUtil.format(entity));
    }

    @Test
    public void testSunlightResource5() {
        System.out.println("TESTING: URI Specified Representation: Text mime-type");
        String time = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        GeoCoord3D coord = GeoCoord3D.fromDegrees(34.25, -119.2);
        ClientResponse response = null;
        String entity = null;
        response = super.webResource.path("sunlight")
                .queryParam("time", time)
                .queryParam("latitude", Double.toString(coord.getLatitudeDegrees()))
                .queryParam("longitude", Double.toString(coord.getLongitudeDegrees()))
                .queryParam("mime-type", MediaType.TEXT_PLAIN)
                .get(ClientResponse.class);
        assertTrue("Expecting 200. Response Status: " + response.getStatus(), response.getStatus() == 200);
        assertTrue("Expecting Text. Response Type: " + response.getType(), response.getType().equals(MediaType.TEXT_PLAIN_TYPE));
        entity = response.getEntity(String.class);
        assertTrue("Looks like Text:\n" + entity, entity.substring(0, 9).equals("Sunlight:"));
    }

    @Test
    public void testSunlightResource6() {
        System.out.println("TESTING: Server-driven Negociation: Accepts Text");
        String time = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        GeoCoord3D coord = GeoCoord3D.fromDegrees(34.25, -119.2);
        ClientResponse response = null;
        String entity = null;
        response = super.webResource.path("sunlight")
                .queryParam("time", time)
                .queryParam("latitude", Double.toString(coord.getLatitudeDegrees()))
                .queryParam("longitude", Double.toString(coord.getLongitudeDegrees()))
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(ClientResponse.class);
        assertTrue("Expecting 200. Response Status: " + response.getStatus(), response.getStatus() == 200);
        assertTrue("Expecting Text. Response Type: " + response.getType(), response.getType().equals(MediaType.TEXT_PLAIN_TYPE));
        entity = response.getEntity(String.class);
        assertTrue("Looks like Text:\n" + entity, entity.substring(0, 9).equals("Sunlight:"));

        System.out.println(">>>> Text Representation:\n" + entity);
    }
    
    @Test
    public void testSunlightResource7() {
        System.out.println("TESTING: Unsupported Accepts MediaType");
        String time = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        GeoCoord3D coord = GeoCoord3D.fromDegrees(34.25, -119.2);
        ClientResponse response = null;
        String entity = null;
        response = super.webResource.path("sunlight")
                .queryParam("time", time)
                .queryParam("latitude", Double.toString(coord.getLatitudeDegrees()))
                .queryParam("longitude", Double.toString(coord.getLongitudeDegrees()))
                .accept(MediaType.TEXT_HTML_TYPE)
                .get(ClientResponse.class);
        assertTrue("Expecting 406. Response Status: " + response.getStatus(), response.getStatus() == 406);
    }

    @Test
    public void testSunlightResource8() {
        System.out.println("TESTING: Unsupported mime-type");
        String time = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        GeoCoord3D coord = GeoCoord3D.fromDegrees(34.25, -119.2);
        ClientResponse response = null;
        String entity = null;
        response = super.webResource.path("sunlight")
                .queryParam("time", time)
                .queryParam("latitude", Double.toString(coord.getLatitudeDegrees()))
                .queryParam("longitude", Double.toString(coord.getLongitudeDegrees()))
                .queryParam("mime-type",MediaType.TEXT_HTML)
                .get(ClientResponse.class);
        assertTrue("Expecting 415. Response Status: " + response.getStatus(), response.getStatus() == 415);
    }

    @Test
    public void testGetSunlight() {
        System.out.println("getSunlight");
        String isoDateTime = ZonedDateTime.of(2014, 05, 29, 15, 00, 00, 00, ZoneId.of("-8")).format(DateTimeFormatter.ISO_DATE_TIME);
        double latitude = 34.25;
        double longitude = -119.25;
        SunlightResource instance = new SunlightResource();
        BasicSunlight result = SunlightResource.getSunlight(isoDateTime, latitude, longitude);
        assertNotNull(result);
    }

}
