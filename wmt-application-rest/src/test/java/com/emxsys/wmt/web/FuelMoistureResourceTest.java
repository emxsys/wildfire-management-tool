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

import com.emxsys.util.JsonUtil;
import com.emxsys.util.XmlUtil;
import com.emxsys.wildfire.api.BasicFuelMoisture;
import com.emxsys.wildfire.api.WeatherConditions;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Bruce Schubert
 */
public class FuelMoistureResourceTest extends JerseyTest {

    public FuelMoistureResourceTest() throws Exception {
        super("com.emxsys.wmt.web");
    }

    @Test
    public void testGetDefaults() {
        System.out.println("TESTING: getDefaults");
        BasicFuelMoisture expResult = BasicFuelMoisture.INVALID;
        ClientResponse response = super.webResource.path("fuelmoisture")
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.APPLICATION_XML + " but found: " + response.getType(),
                response.getType().equals(MediaType.APPLICATION_XML_TYPE));
        //System.out.println(">>>> " + XmlUtil.format(response.getEntity(String.class)));
        BasicFuelMoisture result = response.getEntity(BasicFuelMoisture.class);
        System.out.println(result.toString());
        assertTrue(result.equals(expResult));
    }

    @Test
    public void testGetXml() {
        System.out.println("TESTING: getXml");
        double dead1Hr = 1.0;
        double dead10Hr = 2.0;
        double dead100Hr = 3.0;
        double herb = 4.0;
        double woody = 5.0;
        BasicFuelMoisture expResult = BasicFuelMoisture.fromDoubles(dead1Hr, dead10Hr, dead100Hr, herb, woody);
        ClientResponse response = super.webResource.path("fuelmoisture")
                .queryParam("dead1Hr", Double.toString(dead1Hr))
                .queryParam("dead10Hr", Double.toString(dead10Hr))
                .queryParam("dead100Hr", Double.toString(dead100Hr))
                .queryParam("herb", Double.toString(herb))
                .queryParam("woody", Double.toString(woody))
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.APPLICATION_XML + " but found: " + response.getType(),
                response.getType().equals(MediaType.APPLICATION_XML_TYPE));
        //System.out.println(">>>> " + XmlUtil.format(response.getEntity(String.class)));
        BasicFuelMoisture result = response.getEntity(BasicFuelMoisture.class);
        System.out.println(result.toString());
        assertTrue(result.equals(expResult));
    }

    @Test
    public void testGetJson() {
        System.out.println("TESTING: getJson");
        double dead1Hr = 1.0;
        double dead10Hr = 2.0;
        double dead100Hr = 3.0;
        double herb = 4.0;
        double woody = 5.0;
        BasicFuelMoisture expResult = BasicFuelMoisture.fromDoubles(dead1Hr, dead10Hr, dead100Hr, herb, woody);
        ClientResponse response = super.webResource.path("fuelmoisture")
                .queryParam("dead1Hr", Double.toString(dead1Hr))
                .queryParam("dead10Hr", Double.toString(dead10Hr))
                .queryParam("dead100Hr", Double.toString(dead100Hr))
                .queryParam("herb", Double.toString(herb))
                .queryParam("woody", Double.toString(woody))
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.APPLICATION_JSON + " but found: " + response.getType(),
                response.getType().equals(MediaType.APPLICATION_JSON_TYPE));
        //System.out.println(">>>> " + JsonUtil.format(response.getEntity(String.class)));
        BasicFuelMoisture result = response.getEntity(BasicFuelMoisture.class);
        System.out.println(result.toString());
        assertTrue(result.equals(expResult));
    }

    @Test
    public void testGetText() {
        System.out.println("TESTING: getText");
        double dead1Hr = 1.0;
        double dead10Hr = 2.0;
        double dead100Hr = 3.0;
        double herb = 4.0;
        double woody = 5.0;
        BasicFuelMoisture expResult = BasicFuelMoisture.fromDoubles(dead1Hr, dead10Hr, dead100Hr, herb, woody);
        ClientResponse response = super.webResource.path("fuelmoisture")
                .queryParam("dead1h", Double.toString(dead1Hr))
                .queryParam("dead10h", Double.toString(dead10Hr))
                .queryParam("dead100h", Double.toString(dead100Hr))
                .queryParam("herb", Double.toString(herb))
                .queryParam("woody", Double.toString(woody))
                .accept(MediaType.TEXT_PLAIN)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.TEXT_PLAIN + " but found: " + response.getType(),
                response.getType().equals(MediaType.TEXT_PLAIN_TYPE));
        System.out.println(">>>> " + response.getEntity(String.class));
        //assertTrue(response.getEntity(BasicFuelMoisture.class).equals(expResult));
    }

    @Test
    public void testHotAndDry() {
        System.out.println("TESTING: Hot and Dry");
        BasicFuelMoisture expResult = BasicFuelMoisture.fromWeatherConditions(WeatherConditions.HOT_AND_DRY);
        ClientResponse response = super.webResource.path("fuelmoisture")
                .queryParam("conditions", "hot_and_dry")
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.APPLICATION_JSON + " but found: " + response.getType(),
                response.getType().equals(MediaType.APPLICATION_JSON_TYPE));
        //System.out.println(">>>> " + response.getEntity(BasicFuelMoisture.class).toString());
        assertTrue(response.getEntity(BasicFuelMoisture.class).equals(expResult));
    }

    @Test
    public void testCoolAndWet() {
        System.out.println("TESTING: Cool and Wet");
        BasicFuelMoisture expResult = BasicFuelMoisture.fromWeatherConditions(WeatherConditions.COOL_AND_WET);
        ClientResponse response = super.webResource.path("fuelmoisture")
                .queryParam("conditions", "cool_and_wet")
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.APPLICATION_JSON + " but found: " + response.getType(),
                response.getType().equals(MediaType.APPLICATION_JSON_TYPE));
        //System.out.println(">>>> " + response.getEntity(String.class));
        assertTrue(response.getEntity(BasicFuelMoisture.class).equals(expResult));
    }

    @Test
    public void testBetweenHotAndCool() {
        System.out.println("TESTING: Between Hot and Cool");
        BasicFuelMoisture expResult = BasicFuelMoisture.fromWeatherConditions(WeatherConditions.BETWEEN_HOTDRY_AND_COOLWET);
        ClientResponse response = super.webResource.path("fuelmoisture")
                .queryParam("conditions", "between_hot_and_cool")
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.APPLICATION_JSON + " but found: " + response.getType(),
                response.getType().equals(MediaType.APPLICATION_JSON_TYPE));
        //System.out.println(">>>> " + response.getEntity(String.class));
        assertTrue(response.getEntity(BasicFuelMoisture.class).equals(expResult));
    }

}
