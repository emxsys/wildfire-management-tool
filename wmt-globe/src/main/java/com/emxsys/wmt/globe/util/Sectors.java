/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
package com.emxsys.wmt.globe.util;

import com.emxsys.wmt.gis.api.Box;
import com.emxsys.wmt.gis.api.GeoSector;
import gov.nasa.worldwind.geom.Sector;

/**
 * Utility class for working with WorldWind Sectors.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class Sectors {

    /**
     * Creates a WorldWind Sector from a GeoSector.
     *
     * @param extents used for the new sector's extents.
     * @return A new WorldWind Sector.
     */
    public static Sector fromBox(Box extents) {
        if (extents.isMissing()) {
            throw new IllegalArgumentException("fromGeoSector failed: param has missing values.");
        }
        Sector sector = Sector.fromDegrees(
                extents.getSouthwest().getLatitudeDegrees(), extents.getNortheast().getLatitudeDegrees(),
                extents.getSouthwest().getLongitudeDegrees(), extents.getNortheast().getLongitudeDegrees());
        return sector;
    }

    /**
     * Creates a GeoSector from a WorldWind Sector.
     *
     * @param sector to convert to a GeoSector
     * @return a new GeoSector.
     */
    public static GeoSector toGeoSector(Sector sector) {
        return new GeoSector(
                sector.getMinLatitude().degrees, sector.getMinLongitude().degrees,
                sector.getMaxLatitude().degrees, sector.getMaxLongitude().degrees);

    }

    private Sectors() {
    }
}
