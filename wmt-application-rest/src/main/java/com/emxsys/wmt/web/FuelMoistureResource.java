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
import com.emxsys.wildfire.api.WeatherConditions;
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
 * Fuel Moisture REST Web Service.
 *
 * @author Bruce Schubert
 */
@Path("fuelmoisture")
public class FuelMoistureResource {

    @Context
    private HttpHeaders headers;

    /** Creates a new instance of FuelMoistureResource */
    public FuelMoistureResource() {
    }

    /**
     * Retrieves representation of an instance
     * com.emxsys.wildfire.api.BasicFuelMoisture.
     *
     * @param mimeType Optional. Either application/json, application/xml or
     * text/plain.
     * @param conditions Optional. Either hot_and_dry, cool_and_wet, or
     * between_hot_and_cool.
     * @param dead1Hr Optional, default value NaN.
     * @param dead10Hr Optional, default value NaN.
     * @param dead100Hr Optional, default value NaN.
     * @param herb Optional, default value NaN.
     * @param woody Optional, default value NaN.
     * @return an instance of BasicFuelMoisture
     */
    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, TEXT_PLAIN})
    public Response getFuelMoisture(
            @DefaultValue("") @QueryParam("mime-type") String mimeType,
            @DefaultValue("") @QueryParam("conditions") String conditions,
            @QueryParam("dead1Hr") Double dead1Hr,
            @QueryParam("dead10Hr") Double dead10Hr,
            @QueryParam("dead100Hr") Double dead100Hr,
            @QueryParam("herb") Double herb,
            @QueryParam("woody") Double woody) {

        // Dermine representation
        List<MediaType> permittedTypes = Arrays.asList(APPLICATION_JSON_TYPE, APPLICATION_XML_TYPE, TEXT_PLAIN_TYPE);
        MediaType mediaType = WebUtil.getPermittedMediaType(mimeType, permittedTypes, headers, MediaType.TEXT_PLAIN_TYPE);

        BasicFuelMoisture fuelMoisture = null;
        switch (conditions) {
            case "hot_and_dry":
                fuelMoisture = BasicFuelMoisture.fromWeatherConditions(WeatherConditions.HOT_AND_DRY);
                break;
            case "between_hot_and_cool":
                fuelMoisture = BasicFuelMoisture.fromWeatherConditions(WeatherConditions.BETWEEN_HOTDRY_AND_COOLWET);
                break;
            case "cool_and_wet":
                fuelMoisture = BasicFuelMoisture.fromWeatherConditions(WeatherConditions.COOL_AND_WET);
                break;
            default:
                fuelMoisture = BasicFuelMoisture.fromDoubles(
                        dead1Hr == null ? Double.NaN : dead1Hr,
                        dead10Hr == null ? Double.NaN : dead10Hr,
                        dead100Hr == null ? Double.NaN : dead100Hr,
                        herb == null ? Double.NaN : herb,
                        woody == null ? Double.NaN : woody);

        }
        return Response.ok(
                mediaType.equals(TEXT_PLAIN_TYPE) ? fuelMoisture.toString() : fuelMoisture,
                mediaType).build();

    }
}
