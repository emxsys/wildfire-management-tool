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

import com.emxsys.util.XmlUtil;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.test.framework.JerseyTest;
import javax.ws.rs.core.MediaType;
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

        ClientResponse fuelModel = super.webResource.path("fuelmodels/6")
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue("Status: expected 200 but got " + fuelModel.getStatus(), fuelModel.getStatus() == 200);

        ClientResponse fuelMoisture = super.webResource.path("fuelmoisture")
                .queryParam("dead1Hr", Double.toString(1.0))
                .queryParam("dead10Hr", Double.toString(2.0))
                .queryParam("dead100Hr", Double.toString(3.0))
                .queryParam("herb", Double.toString(4.0))
                .queryParam("woody", Double.toString(5.0))
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue("Status: expected 200 but got " + fuelMoisture.getStatus(), fuelMoisture.getStatus() == 200);

        
        FormDataMultiPart formData = new FormDataMultiPart();
        // On the server-side, use @FormParam to process formData.field(s)
        // and use @FormDataParam to process formData.bodyPart.
        formData.field("fuelModel",  fuelModel.getEntity(String.class), MediaType.APPLICATION_JSON_TYPE);
        formData.field("fuelMoisture", fuelMoisture.getEntity(String.class), MediaType.APPLICATION_JSON_TYPE);
        
        ClientResponse response = super.webResource.path("surfacefuel")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(MediaType.APPLICATION_XML)
                .post(ClientResponse.class, formData);

        assertTrue("Status: expected 200 but got " + response.getStatus(), response.getStatus() == 200);
        assertTrue("Expecting: " + MediaType.APPLICATION_XML + " but found: " + response.getType(),
                response.getType().equals(MediaType.APPLICATION_XML_TYPE));
        System.out.println("SurfaceFire XML >>>>\n" + XmlUtil.format(response.getEntity(String.class)));
    }

}
