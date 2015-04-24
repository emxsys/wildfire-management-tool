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
 *     - Neither the name of Bruce Schubert,  nor the names of its 
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

import java.util.Iterator;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Bruce Schubert
 */
public class WebUtil {

    public static MediaType getPermittedMediaType(
            String mimeType, List<MediaType> permittedTypes,
            HttpHeaders headers, MediaType defaultType) {

        // Set the MediaType based on mime-type or Accept header...
        if (mimeType != null && !mimeType.isEmpty()) {
            // When the optional mime-type param is supplied, we'll 
            // use it regardless of the Accepts header (overrides Accept)
            MediaType mediaType = MediaType.valueOf(mimeType);
            if (permittedTypes.contains(mediaType)) {
                return mediaType;
            } else {
                // Throw an exception if the query param is not supported
                throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
            }
        } else {
            // Set the MediaType based on the first acceptable HTTP Accept header
            // from the sorted list of acceptable types
            Iterator<MediaType> iterator = headers.getAcceptableMediaTypes().iterator();
            while (iterator.hasNext()) {
                MediaType acceptedType = iterator.next();
                for (MediaType permittedType : permittedTypes) {
                    if (acceptedType.equals(permittedType) || acceptedType.isCompatible(permittedType)) {
                        return permittedType;
                    }
                }
            }
        }
        // Provided a reasonable default for unmatched MediaType
        return defaultType;
    }

    private WebUtil() {
    }

}
