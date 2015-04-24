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

import com.emxsys.wildfire.api.BasicFuelModel;
import com.emxsys.wildfire.api.StdFuelModelParams13;
import com.emxsys.wildfire.api.StdFuelModelParams40;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.PathParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author Bruce Schubert
 */
@Path("/fuelmodels")
public class FuelModelsResource {

    @Context
    HttpHeaders headers;

    /**
     * Creates a new instance of FuelModelsResource
     */
    public FuelModelsResource() {
    }

    /**
     * Get a representation of a com.emxsys.wildfire.BasicFuelModel.
     *
     * @param category Optional. Either all, standard or original.
     * @param mimeType Optional. Either application/json, application/xml or
     * text/plain.
     * @return A list of BasicFuelModel representations.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response getFuelModels(
            @DefaultValue("all") @QueryParam("category") String category,
            @DefaultValue("") @QueryParam("mime-type") String mimeType) {

        // Populate an ArrayList with the desired fuel model categories
        ArrayList<BasicFuelModel> models = new ArrayList<>();
        if (category.equals("all") || category.contains("original")) {
            // Add the Original 13 FuelModels
            for (StdFuelModelParams13 fbfm : StdFuelModelParams13.values()) {
                models.add(new BasicFuelModel.Builder(fbfm).build());
            }
        }
        if (category.equals("all") || category.contains("standard")) {
            // Add the Standard 40 FuelModels
            for (StdFuelModelParams40 fbfm : StdFuelModelParams40.values()) {
                models.add(new BasicFuelModel.Builder(fbfm).build());
            }
        }

        // Set the MediaType based on mime-type or Accept header...
        MediaType mediaType = getMediaType(mimeType);

        if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
            // Handle TEXT/PLAIN type here with a CSV representation
            StringBuilder sb = new StringBuilder();
            sb.append("ModelNo,ModelName,ModelGroup\n");
            for (BasicFuelModel model : models) {
                sb.append(model.getModelNo())
                        .append(',').append('\"').append(model.getModelName()).append('\"')
                        .append(',').append('\"').append(model.getModelGroup()).append('\"')
                        .append('\n');
            }
            return Response.ok(sb.toString(), mediaType).build();

        } else {
            // Otherwise, let JAXB/Jersey handle vai the message body writer for the
            // given MediaType. We have to to wrap the collection in a GenericEntity
            // object in order to preserve information on the parameterised type.
            GenericEntity<List<BasicFuelModel>> entity = new GenericEntity<List<BasicFuelModel>>(models) {
            };
            return Response.ok(entity, mediaType).build();
        }
    }

    /**
     * Sub-resource locator method for {modelNo}. Retrieves representation of an
     * instance of com.emxsys.wmt.wildfire.BasicFuelModel
     *
     * @param modelNo
     * @param mimeType
     * @return A representation of BasicFuelModel.
     */
    @GET
    @Path("/{modelNo : \\d+}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response getFuelModel(
            @PathParam("modelNo") String modelNo,
            @DefaultValue("") @QueryParam("mime-type") String mimeType) {

        BasicFuelModel fuelModel = BasicFuelModel.from(Integer.parseInt(modelNo));
        MediaType mediaType = getMediaType(mimeType);
        return Response.ok(
                mediaType.equals(MediaType.TEXT_PLAIN_TYPE) ? fuelModel.toString() : fuelModel,
                mediaType).build();
    }

    private MediaType getMediaType(String mimeType) {
        final List<MediaType> permittedTypes
                = Arrays.asList(APPLICATION_JSON_TYPE, APPLICATION_XML_TYPE, TEXT_PLAIN_TYPE);
        return WebUtil.getPermittedMediaType(mimeType, permittedTypes, headers, TEXT_PLAIN_TYPE);
    }

}
