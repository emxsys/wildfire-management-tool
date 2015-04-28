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
import com.emxsys.weather.api.BasicWeather;
import com.emxsys.wildfire.behavior.SurfaceFire;
import com.emxsys.wildfire.behavior.SurfaceFireProvider;
import com.emxsys.wildfire.behavior.SurfaceFuel;
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
import javax.ws.rs.core.Response.Status;

/**
 * Surface Fire REST Web Service.
 *
 * @author Bruce Schubert
 */
@Path("surfacefire")
public class SurfaceFireResource {

    private static final SurfaceFireProvider provider = new SurfaceFireProvider();
    private static final List<MediaType> permittedTypes = Arrays.asList(APPLICATION_JSON_TYPE, APPLICATION_XML_TYPE, TEXT_PLAIN_TYPE);

    @Context
    private HttpHeaders headers;

    /** Creates a new instance of SurfaceFuelResource */
    public SurfaceFireResource() {
    }

    /**
     * Retrieves representation of an instance of
     * com.emxsys.wildfire.behavior.SurfaceFire
     *
     * @param mimeType Optional. The specified mime-type overrides the Accepts
     * header.
     * @param fuel An XML or JSON FuelModel representation.
     * @param weather An XML or JSON Weather representation.
     * @param terrain An XML or JSON Terrain representation.
     * @return A Response containing a SurfaceFire entity.
     */
    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createSurfaceFuel(
            @DefaultValue("") @FormParam("mime-type") String mimeType,
            @FormParam("fuel") SurfaceFuel fuel,
            @FormParam("weather") BasicWeather weather,
            @FormParam("terrain") BasicTerrain terrain) {

        // Validate preconditions
        if (fuel == null || weather == null || terrain == null) {
            throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
        }
        // Dermine the proper representation
        MediaType mediaType = WebUtil.getPermittedMediaType(mimeType, permittedTypes, headers, MediaType.TEXT_PLAIN_TYPE);

        // Create the resource
        SurfaceFire fire = provider.getFireBehavior(fuel, weather, terrain);
        if (fire == null) {
            throw new WebApplicationException(
                    new RuntimeException("SurfaceFireProvider.getFireBehavior() returned null"),
                    Status.INTERNAL_SERVER_ERROR);
        }
        // If TEXT is requested, simply return a String, otherwise, let JAXB
        // perform the marshalling.
        return Response.ok(
                mediaType.equals(TEXT_PLAIN_TYPE) ? fire.toString() : fire,
                mediaType).build();
    }

}
