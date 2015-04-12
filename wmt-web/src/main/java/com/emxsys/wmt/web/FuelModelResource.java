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

import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.StdFuelModel;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;

/**
 * REST Web Service
 *
 * @author Bruce Schubert
 */
public class FuelModelResource {
    private FuelModel fuelModel;

    /** Creates a new instance of FuelModelResource */
    private FuelModelResource(String modelNo) {
        this.fuelModel = StdFuelModel.from(Integer.parseInt(modelNo));
    }

    /** Get instance of the FuelModelResource
     * @param modelNo
     * @return  
     */
    public static FuelModelResource getInstance(String modelNo) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of FuelModelResource class.
        return new FuelModelResource(modelNo);
    }

    /**
     * Retrieves representation of an instance of com.emxsys.wmt.web.FuelModelResource
     * @return an instance of com.emxsys.wildfire.api.FuelModel
     */
    @GET
    @Produces("application/xml")
    public FuelModelBean getFuelModel() {
        //TODO return proper representation object
        return new FuelModelBean(this.fuelModel);
    }

    /**
     * PUT method for updating or creating an instance of FuelModelResource
     * @param content representation for the resource
     */
    @PUT
    @Consumes("application/xml")
    public void putFuelModel(FuelModelBean content) {
    }

    /**
     * DELETE method for resource FuelModelResource
     */
    @DELETE
    public void delete() {
    }
}
