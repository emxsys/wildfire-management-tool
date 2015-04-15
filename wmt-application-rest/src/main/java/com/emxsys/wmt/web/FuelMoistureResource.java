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

import com.emxsys.wildfire.api.BasicFuelMoisture;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


/**
 * Fuel Moisture REST Web Service.
 *
 * @author Bruce Schubert
 */
@Path("fuelmoisture")
public class FuelMoistureResource {
    @Context
    private UriInfo context;

    /** Creates a new instance of FuelMoistureResource */
    public FuelMoistureResource() {
    }

    /**
     * Retrieves representation of an instance com.emxsys.wildfire.api.BasicFuelMoisture.
     *
     * @return an instance of BasicFuelMoisture
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public BasicFuelMoisture getXmlOrJson(@QueryParam("dead1Hr") double dead1Hr,
                                          @QueryParam("dead10Hr") double dead10Hr,
                                          @QueryParam("dead100Hr") double dead100Hr,
                                          @QueryParam("herb") double herb,
                                          @QueryParam("woody") double woody) {
        return BasicFuelMoisture.fromDoubles(dead1Hr, dead10Hr, dead100Hr, herb, woody);
    }

    /**
     * Retrieves representation of an instance com.emxsys.wildfire.api.BasicFuelMoisture.
     *
     * @return an instance of BasicFuelMoisture
     */
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getTxt(@QueryParam("dead1Hr") double dead1Hr,
                         @QueryParam("dead10Hr") double dead10Hr,
                         @QueryParam("dead100Hr") double dead100Hr,
                         @QueryParam("herb") double herb,
                         @QueryParam("woody") double woody) {
        return BasicFuelMoisture.fromDoubles(dead1Hr, dead10Hr, dead100Hr, herb, woody).toString();
    }

}
