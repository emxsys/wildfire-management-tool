/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.markers.ics;

import com.emxsys.wmt.gis.api.marker.Marker;
import org.w3c.dom.Document;

/**
 * This a legacy class.
 * @author Bruce Schubert
 */
@Deprecated
public class IcsMarker {

    public static class IcsMarkerFactory implements Marker.Builder {

        private final com.emxsys.wmt.globe.markers.ics.IcsMarker.Builder delegate;

        /**
         * Creates a legacy factory that can create a IcsMarker from an XML document via a delegate.
         * @param doc
         */
        public IcsMarkerFactory(Document doc) {
            System.out.println(">>> Creating delgate: com.emxsys.wmt.globe.markers.ics.IcsMarker.Builder");
            this.delegate = new com.emxsys.wmt.globe.markers.ics.IcsMarker.Builder(doc);
            if (this.delegate == null) {
                throw new IllegalStateException("Could not create delegate: com.emxsys.wmt.globe.markers.ics.IcsMarker.Builder");
            }
        }

        /**
         * @return A new IcsMarker instance via a delegate factory.
         */
        @Override
        public Marker build() {
            return delegate.build();
        }
    }
}
