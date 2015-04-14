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

import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wildfire.api.StdFuelModelParams13;
import com.emxsys.wildfire.api.StdFuelModelParams40;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


/**
 * REST Web Service
 *
 * @author Bruce Schubert
 */
@Path("/fuelmodels")
public class FuelModelsResource {
    @Context
    private UriInfo context;

    /** Creates a new instance of FuelModelsResource */
    public FuelModelsResource() {
    }

    /**
     * Retrieves representation of an instance of com.emxsys.wmt.web.FuelModelsResource
     *
     * @return an instance of List<StdFuelModel>
     */
    @GET
    @Produces("application/xml")
    public List<StdFuelModel> getAllFuelModels() {
        ArrayList<StdFuelModel> list = new ArrayList<>();
        // Add the Standard 13 FuelModels
        for (StdFuelModelParams13 fbfm : StdFuelModelParams13.values()) {
            list.add(new StdFuelModel.Builder(fbfm).build());
        }
        // Add the Standard 13 FuelModels
        for (StdFuelModelParams40 fbfm : StdFuelModelParams40.values()) {
            list.add(new StdFuelModel.Builder(fbfm).build());
        }
        return list;
    }

    /**
     * POST method for creating an instance of FuelModelResource
     *
     * @param content representation for the new resource
     * @return an HTTP response with content of the created resource
     */
    @POST
    @Consumes("application/xml")
    @Produces("application/xml")
    public Response postFuelModel(StdFuelModel content) {
        //TODO
        return Response.created(context.getAbsolutePath()).build();
    }

    /**
     * Sub-resource locator method for {modelNo}
     *
     * @param modelNo
     * @return
     */
    @Path("{modelNo}")
    public FuelModelResource getFuelModelResource(@PathParam("modelNo") String modelNo) {
        return FuelModelResource.getInstance(modelNo);
    }

}
