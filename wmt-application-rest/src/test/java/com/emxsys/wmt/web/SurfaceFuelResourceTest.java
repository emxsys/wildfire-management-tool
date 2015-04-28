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
import com.emxsys.wildfire.api.BasicFuelModel;
import com.emxsys.wildfire.api.BasicFuelMoisture;
import com.emxsys.wildfire.api.WeatherConditions;
import com.emxsys.wildfire.behavior.SurfaceFuel;
import com.emxsys.wildfire.behavior.SurfaceFuelProvider;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.test.framework.JerseyTest;
import javax.ws.rs.core.MediaType;
import static javax.ws.rs.core.MediaType.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Bruce Schubert
 */
public class SurfaceFuelResourceTest extends JerseyTest {

    public SurfaceFuelResourceTest() throws Exception {
        super("com.emxsys.wmt.web");
    }

    @Test
    public void testCreateSurfaceFuel() {
        System.out.println("TESTING: createSurfaceFuel");
        SurfaceFuelResource instance = new SurfaceFuelResource();

        // Get a Fuel Model
        ClientResponse fuelModelResponse = super.webResource.path("fuelmodels/6")
                .accept(APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue("Status: expected 200 but got " + fuelModelResponse.getStatus(), fuelModelResponse.getStatus() == 200);
        BasicFuelModel fuelModel = fuelModelResponse.getEntity(BasicFuelModel.class);
        System.out.println(fuelModel);
        // Get the Fuel Moisture
        ClientResponse fuelMoistureResponse = super.webResource.path("fuelmoisture")
                .queryParam("conditions", "hot_and_dry")
                .accept(APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue("Status: expected 200 but got " + fuelMoistureResponse.getStatus(), fuelMoistureResponse.getStatus() == 200);
        BasicFuelMoisture fuelMoisture = fuelMoistureResponse.getEntity(BasicFuelMoisture.class);
        System.out.println(fuelMoisture);
        // Get the "expected" Fuel Bed 
        SurfaceFuelProvider provider = new SurfaceFuelProvider();
        SurfaceFuel expResult = provider.getSurfaceFuel(fuelModel, fuelMoisture);
        System.out.println(expResult);

        // On the server-side, use @FormParam to process formData.field(s)
        // and use @FormDataParam to process formData.bodyPart.
        FormDataMultiPart formData = new FormDataMultiPart();
        formData.field("fuelModel", fuelModel, APPLICATION_JSON_TYPE);
        formData.field("fuelMoisture", fuelMoisture, APPLICATION_JSON_TYPE);
        ClientResponse fuelResponse = super.webResource.path("surfacefuel")
                .type(MULTIPART_FORM_DATA_TYPE)
                .accept(APPLICATION_XML)
                .post(ClientResponse.class, formData);
        assertTrue("Status: expected 200 but got " + fuelResponse.getStatus(), fuelResponse.getStatus() == 200);
        assertTrue("Expecting: " + APPLICATION_XML + " but found: " + fuelResponse.getType(),
                fuelResponse.getType().equals(APPLICATION_XML_TYPE));
        SurfaceFuel entity = fuelResponse.getEntity(SurfaceFuel.class);
        assertTrue(entity.equals(expResult));
        System.out.println("Text Representation >>>>\n" + entity.toString());
    }

    @Test
    public void testGetXml() {
        System.out.println("TESTING: getXml");

        BasicFuelModel fuelModel = BasicFuelModel.from(6);
        BasicFuelMoisture fuelMoisture = BasicFuelMoisture.fromWeatherConditions(WeatherConditions.HOT_AND_DRY);

        // On the server-side, use @FormParam to process formData.field(s)
        // and use @FormDataParam to process formData.bodyPart.
        FormDataMultiPart formData = new FormDataMultiPart();
        formData.field("fuelModel", fuelModel, APPLICATION_XML_TYPE);
        formData.field("fuelMoisture", fuelMoisture, APPLICATION_XML_TYPE);
        ClientResponse fuelResponse = super.webResource.path("surfacefuel")
                .type(MULTIPART_FORM_DATA_TYPE)
                .accept(APPLICATION_XML)
                .post(ClientResponse.class, formData);
        assertTrue("Status: expected 200 but got " + fuelResponse.getStatus(), fuelResponse.getStatus() == 200);
        assertTrue("Expecting: " + APPLICATION_XML + " but found: " + fuelResponse.getType(),
                fuelResponse.getType().equals(APPLICATION_XML_TYPE));
        String entity = fuelResponse.getEntity(String.class);
        assertTrue("Looks like XML:\n" + entity, entity.startsWith("<"));
        System.out.println(">>>> XML Representation:\n" + XmlUtil.format(entity));
    }

    @Test
    public void testGetJson() {
        System.out.println("TESTING: getXml");

        BasicFuelModel fuelModel = BasicFuelModel.from(6);
        BasicFuelMoisture fuelMoisture = BasicFuelMoisture.fromWeatherConditions(WeatherConditions.HOT_AND_DRY);

        // On the server-side, use @FormParam to process formData.field(s)
        // and use @FormDataParam to process formData.bodyPart.
        FormDataMultiPart formData = new FormDataMultiPart();
        formData.field("fuelModel", fuelModel, APPLICATION_JSON_TYPE);
        formData.field("fuelMoisture", fuelMoisture, APPLICATION_JSON_TYPE);
        ClientResponse fuelResponse = super.webResource.path("surfacefuel")
                .type(MULTIPART_FORM_DATA_TYPE)
                .accept(APPLICATION_JSON)
                .post(ClientResponse.class, formData);
        assertTrue("Status: expected 200 but got " + fuelResponse.getStatus(), fuelResponse.getStatus() == 200);
        assertTrue("Expecting: " + APPLICATION_JSON + " but found: " + fuelResponse.getType(),
                fuelResponse.getType().equals(APPLICATION_JSON_TYPE));
        String entity = fuelResponse.getEntity(String.class);
        assertTrue("Looks like JSON:\n" + entity, entity.trim().startsWith("{"));
        System.out.println(">>>> JSON Representation:\n" + JsonUtil.format(entity));
    }

}
