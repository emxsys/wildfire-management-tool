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

import com.emxsys.gis.api.BasicTerrain;
import com.emxsys.util.JsonUtil;
import com.emxsys.util.XmlUtil;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Bruce Schubert
 */
public class TerrainResourceTest extends JerseyTest {

    public TerrainResourceTest() throws Exception {
        super("com.emxsys.wmt.web");
    }

    @Test
    public void testGetDefaults() {
        System.out.println("TESTING: getDefaults");
        BasicTerrain expResult = BasicTerrain.INVALID_TERRAIN;
        ClientResponse response = super.webResource.path("terrain")
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.APPLICATION_XML + " but found: " + response.getType(),
                response.getType().equals(MediaType.APPLICATION_XML_TYPE));
        //System.out.println(">>>> " + XmlUtil.format(response.getEntity(String.class)));
        BasicTerrain result = response.getEntity(BasicTerrain.class);
        System.out.println(result.toString());
        assertTrue(result.equals(expResult));
    }

    @Test
    public void testGetXml() {
        System.out.println("TESTING: getXml");
        double aspect = 1.0;
        double slope = 2.0;
        double elevation = 3.0;
        BasicTerrain expResult = new BasicTerrain(aspect, slope, elevation);
        ClientResponse response = super.webResource.path("terrain")
                .queryParam("aspect", Double.toString(aspect))
                .queryParam("slope", Double.toString(slope))
                .queryParam("elevation", Double.toString(elevation))
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.APPLICATION_XML + " but found: " + response.getType(),
                response.getType().equals(MediaType.APPLICATION_XML_TYPE));
        //System.out.println(">>>> " + XmlUtil.format(response.getEntity(String.class)));
        BasicTerrain result = response.getEntity(BasicTerrain.class);
        System.out.println(result.toString());
        assertTrue(result.equals(expResult));
    }

    @Test
    public void testGetJson() {
        System.out.println("TESTING: getJson");
        double aspect = 1.0;
        double slope = 2.0;
        double elevation = 3.0;
        BasicTerrain expResult = new BasicTerrain(aspect, slope, elevation);
        ClientResponse response = super.webResource.path("terrain")
                .queryParam("aspect", Double.toString(aspect))
                .queryParam("slope", Double.toString(slope))
                .queryParam("elevation", Double.toString(elevation))
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.APPLICATION_JSON + " but found: " + response.getType(),
                response.getType().equals(MediaType.APPLICATION_JSON_TYPE));
        //System.out.println(">>>> " + JsonUtil.format(response.getEntity(String.class)));
        BasicTerrain result = response.getEntity(BasicTerrain.class);
        System.out.println(result.toString());
        assertTrue(result.equals(expResult));
    }

    @Test
    public void testGetText() {
        System.out.println("TESTING: getText");
        double aspect = 1.0;
        double slope = 2.0;
        double elevation = 3.0;
        BasicTerrain expResult = new BasicTerrain(aspect, slope, elevation);
        ClientResponse response = super.webResource.path("terrain")
                .queryParam("aspect", Double.toString(aspect))
                .queryParam("slope", Double.toString(slope))
                .queryParam("elevation", Double.toString(elevation))
                .accept(MediaType.TEXT_PLAIN)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.TEXT_PLAIN + " but found: " + response.getType(),
                response.getType().equals(MediaType.TEXT_PLAIN_TYPE));
        System.out.println(">>>> " + response.getEntity(String.class));
        //assertTrue(response.getEntity(BasicTerrain.class).equals(expResult));
    }

}
