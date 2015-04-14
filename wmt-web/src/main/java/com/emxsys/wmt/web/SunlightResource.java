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
import com.emxsys.solar.api.SunlightTuple;
import com.emxsys.solar.spi.SunlightProviderFactory;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;


/**
 * REST Web Service
 *
 * @author Bruce Schubert
 */
@Path("/sunlight")
public class SunlightResource {
    @Context
    private UriInfo context;

    /** Creates a new instance of SunlightResource */
    public SunlightResource() {
    }

    /**
     * Retrieves representation of an instance of com.emxsys.wmt.web.SunlightResource Example:
     * <pre>GET http://localhost:8080/sunlight?time=2015-04-13T13:34:25-07:00[America/Los_Angeles]&latitude=34.25&longitude=-119.2</pre>
     *
     * @param time An ISO Date/Time string.
     * @param latitude Latitude in degrees.
     * @param longitude Longitude in degrees.
     * @return an instance of SunlightTuple
     */
    @GET
    @Produces({"application/xml",})
    public SunlightTuple getSunlight(@QueryParam("time") String time,
                                     @QueryParam("latitude") double latitude,
                                     @QueryParam("longitude") double longitude) {
        ZonedDateTime datetime = ZonedDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        GeoCoord3D coord = GeoCoord3D.fromDegrees(latitude, longitude);
        return SunlightProviderFactory.getInstance().getSunlight(datetime, coord);
    }

    /**
     * PUT method for updating or creating an instance of SunlightResource
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes("application/xml")
    public void putSunlight(SunlightTuple content) {
    }
}
