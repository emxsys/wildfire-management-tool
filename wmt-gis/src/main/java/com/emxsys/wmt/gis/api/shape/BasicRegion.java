/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.gis.api.shape;

import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.gis.api.GeoSector;
import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.shape.Region;
import static com.emxsys.wmt.gis.api.shape.Region.*;

import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.Real;

/**
 * BasicRegion implements the Emxsys {@link Region} GIS interface.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class BasicRegion extends AbstractShape implements Region {

    protected GeoSector sector;
    private static final Logger LOG = Logger.getLogger(BasicRegion.class.getName());

    public BasicRegion(GeoSector sector) {
        setSector(sector);
    }

    @Override
    public void setPosition(Coord3D position) {
        // Just updating the current sector -- its not a new sector, thus no sector event ala setSector
        sector.moveTo(position);
        super.setPosition(position); // fires a position changed event
    }

    @Override
    public GeoSector getSector() {
        return this.sector;
    }

    @Override
    public void setSector(GeoSector sector) {
        GeoSector oldSector = getSector();
        try {
            this.sector = sector;
            super.pcs.firePropertyChange(PROP_REGION_SECTOR, oldSector, this.sector);

            Coord2D center = sector.getCenter();
            Real altitude = getPosition() == null ? GeoCoord3D.ZERO_POSITION.getAltitude() : getPosition().getAltitude();
            GeoCoord3D newPos = new GeoCoord3D(center.getLatitude(), center.getLongitude(), altitude);
            super.setPosition(newPos);  // fires a position changed event
        }
        catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

}
