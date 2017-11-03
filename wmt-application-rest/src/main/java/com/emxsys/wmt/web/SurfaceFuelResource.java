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
import com.emxsys.wildfire.api.BasicFuelMoisture;
import com.emxsys.wildfire.behavior.SurfaceFuel;
import com.emxsys.wildfire.behavior.SurfaceFuelProvider;
import com.sun.jersey.multipart.FormDataParam;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;

/**
 * Surface Fuel REST Web Service.
 *
 * @author Bruce Schubert
 */
@Path("/surfacefuel")
public class SurfaceFuelResource {

    @Context
    private HttpHeaders headers;
    // 
    private static final List<MediaType> permittedTypes = Arrays.asList(APPLICATION_JSON_TYPE, APPLICATION_XML_TYPE, TEXT_PLAIN_TYPE);
    static final SurfaceFuelProvider fuelProvider = new SurfaceFuelProvider();

    /** Creates a new instance of SurfaceFuelResource */
    public SurfaceFuelResource() {
    }

    /**
     * Retrieves representation of an instance of
     * com.emxsys.wildfire.behavior.SurfaceFuel
     *
     * @param mimeType Optional. A specified mime-type overrides the Accepts
     * header.
     * @param fuelModel An XML or JSON FuelModel representation.
     * @param fuelMoisture An XML or JSON FuelMoisture representation.
     * @return A Response containing a SurfaceFuel entity.
     */
    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createSurfaceFuel(
            @DefaultValue("") @FormDataParam("mime-type") String mimeType,
            @FormDataParam("fuelModel") BasicFuelModel fuelModel,
            @FormDataParam("fuelMoisture") BasicFuelMoisture fuelMoisture) {

        // Preconditions
        if (fuelModel == null || fuelMoisture == null) {
            throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
        }
        // Dermine representation
        MediaType mediaType = WebUtil.getPermittedMediaType(mimeType, 
                permittedTypes, headers, MediaType.TEXT_PLAIN_TYPE);

        // Create the resource
        SurfaceFuel fuel = fuelProvider.getSurfaceFuel(fuelModel, fuelMoisture);
        if (fuel == null) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        // Return the representation
        return Response.ok(
                mediaType.equals(TEXT_PLAIN_TYPE) ? fuel.toString() : fuel,
                mediaType).build();
    }

}
