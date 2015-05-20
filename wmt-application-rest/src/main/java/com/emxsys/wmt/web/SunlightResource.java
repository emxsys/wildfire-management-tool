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
import com.emxsys.solar.api.BasicSunlight;
import com.emxsys.solar.spi.SunlightProviderFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import javax.ws.rs.core.Context;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.openide.util.Exceptions;

/**
 * REST Web Service. Retrieves representation of an instance of
 * com.emxsys.solar.BasicSunliht. Example:
 * <pre>GET http://localhost:8080/sunlight?time=2015-04-13T13:34:25-07:00[America/Los_Angeles]&latitude=34.25&longitude=-119.2</pre>
 *
 * @author Bruce Schubert
 */
@Path("/sunlight")
public class SunlightResource {

    @Context
    HttpHeaders headers;

    /**
     * Creates a new instance of SunlightResource
     */
    public SunlightResource() {
    }

    /**
     * Example:
     * <pre>GET http://localhost:8080/sunlight?time=2015-04-13T13:34:25-07:00[America/Los_Angeles]&latitude=34.25&longitude=-119.2</pre>
     *
     * @param time An ISO Date/Time string.
     * @param latitude Latitude in degrees.
     * @param longitude Longitude in degrees.
     * @param mimeType Optional. "application/json", "application/xml" or
     * "text/plain".
     * @return A Response containing entity representation.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response getResource(
            @QueryParam("time") String time,
            @QueryParam("latitude") double latitude,
            @QueryParam("longitude") double longitude,
            @QueryParam("mime-type") String mimeType) {

        try {
            // Validate params and decode JavaScript encoded params
            if (time == null || time.isEmpty()) {
                // mandatory parameter
                // TODO: throw
            } else {
                time = URLDecoder.decode(time.replace("+", "%2B"), "UTF-8");
            }
            if (mimeType != null && !mimeType.isEmpty()) {
                // Optional param
                mimeType = URLDecoder.decode(mimeType.replace("+", "%2B"), "UTF-8");
            }

            // Get the resource entity
            BasicSunlight sunlight = getSunlight(time, latitude, longitude);

            MediaType mediaType = MediaType.APPLICATION_XML_TYPE;  // default.

            // When the optional mime-type param is supplied, we override the Accept header
            if (mimeType != null && !mimeType.isEmpty()) {
                // Use mime-type param for representation
                switch (mimeType) {
                    case MediaType.APPLICATION_JSON:
                        mediaType = MediaType.APPLICATION_JSON_TYPE;
                        break;
                    case MediaType.APPLICATION_XML:
                        mediaType = MediaType.APPLICATION_XML_TYPE;
                        break;
                    case MediaType.TEXT_PLAIN:
                        mediaType = MediaType.TEXT_PLAIN_TYPE;
                        break;
                    default:
                        return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
                }
            } else {
                // Set the MediaType based on the first acceptable HTTP Accept header
                // from the sorted list of acceptable types (one of the @Produces entries)
                Iterator<MediaType> iterator = headers.getAcceptableMediaTypes().iterator();
                if (iterator.hasNext()) {
                    mediaType = iterator.next();
                }
            }
            return Response.ok(mediaType.getType().equals("text") ? sunlight.toString() : sunlight, mediaType)
                    .build();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return Response.serverError().entity(ex.toString()).build();
        }

    }

    /**
     * Factory method creates a new BasicSunlight instance
     *
     * @param time An ISO Date/Time string.
     * @param latitude Latitude in degrees.
     * @param longitude Longitude in degrees.
     * @return an instance of BasicSunlight
     */
    public static BasicSunlight getSunlight(String time, double latitude, double longitude) {
        ZonedDateTime datetime = ZonedDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        GeoCoord3D coord = GeoCoord3D.fromDegrees(latitude, longitude);
        return SunlightProviderFactory.getInstance().getSunlight(datetime, coord);
    }

// All the following commented code was replaced with a simple call to Response.ok(...)
//    
//    @Context
//    private MessageBodyWorkers workers;
//    private Response getRepresentation(BasicSunlight sunlight, MediaType mediaType) {
//
//        // Handle TEXT/PLAIN type here...
//        if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
//            return Response.ok(sunlight.toString(), mediaType).build();
//        }
//        // Otherwise, let JAXB/Jersey handle vai the message body writer for the given MediaType
//        
//        MessageBodyWriter<BasicSunlight> writer = workers.getMessageBodyWriter(
//                BasicSunlight.class, BasicSunlight.class, new Annotation[]{}, mediaType);
//
//        // Create a buffer into which the Sunlight will be serialized
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        try {
//            // MultivaluedHashMap headers requires EE7, else ClassNotFoundException (see project properties)
//            MultivaluedMap<String, Object> httpHeaders = new DummyMultivaluedMap(); // no headers
//
//            // Use the MBW to serialize sunlight to byte array
//            writer.writeTo(sunlight, BasicSunlight.class, BasicSunlight.class,
//                    new Annotation[]{}, mediaType, httpHeaders, outputStream);
//
//        } catch (IOException e) {
//            throw new RuntimeException("Error while serializing BasicSunlight.", e);
//        }
//        return Response.ok(outputStream.toString(), mediaType).build();
//    }
}
