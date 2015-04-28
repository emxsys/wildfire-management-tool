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
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.Produces;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;

/**
 * Terrain REST Web Service.
 *
 * @author Bruce Schubert
 */
@Path("/terrain")
public class TerrainResource {

    @Context
    private HttpHeaders headers;
    private static final List<MediaType> permittedTypes = Arrays.asList(
            APPLICATION_JSON_TYPE, APPLICATION_XML_TYPE, TEXT_PLAIN_TYPE);

    /**
     * Creates a new instance of TerrainResource
     */
    public TerrainResource() {
    }

    /**
     * Retrieves representation of an instance com.emxsys.gis.api.BasicTerrain.
     *
     * @param mimeType Optional. Either application/json, application/xml or
     * text/plain.
     * @param aspect Aspect in degrees.
     * @param slope Slope in degrees.
     * @param elevation Elevation in meters.
     * @return an instance of BasicTerrain.
     */
    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, TEXT_PLAIN})
    public Response getTerrain(
            @DefaultValue("") @QueryParam("mime-type") String mimeType,
            @QueryParam("aspect") Double aspect,
            @QueryParam("slope") Double slope,
            @QueryParam("elevation") Double elevation) {

        // Dermine representation
        MediaType mediaType = WebUtil.getPermittedMediaType(mimeType, permittedTypes, headers, MediaType.TEXT_PLAIN_TYPE);
        // Create the resource
        BasicTerrain terrain = new BasicTerrain(
                aspect == null ? Double.NaN : aspect,
                slope == null ? Double.NaN : slope,
                elevation == null ? Double.NaN : elevation);
        // Return the representation
        return Response.ok(
                mediaType.equals(TEXT_PLAIN_TYPE) ? terrain.toString() : terrain,
                mediaType).build();

    }
}
