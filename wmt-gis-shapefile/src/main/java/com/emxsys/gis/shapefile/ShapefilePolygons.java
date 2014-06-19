/*
 * Copyright (c) 2011-2014, Bruce Schubert. <bruce@emxsys.com> 
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
package com.emxsys.gis.shapefile;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.AbstractSurfaceShape;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygons;
import gov.nasa.worldwind.util.CompoundVecBuffer;

/**
 * This class represents a polygon feature read from a {@link ShapefileDataObject}. It suppresses
 * the ability to move the polygon feature, which is a behavior provided by the
 * {@link AbstractSurfaceShape} ancestor class.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class ShapefilePolygons extends SurfacePolygons {

    private long featureId = 0;

    public ShapefilePolygons(Sector sector, CompoundVecBuffer buffer) {
        super(sector, buffer);
    }

    public ShapefilePolygons(CompoundVecBuffer buffer) {
        super(buffer);
    }

    public long getFeatureId() {
        return featureId;
    }

    public void setFeatureId(long featureId) {
        this.featureId = featureId;
    }

    @Override
    public void setHighlighted(boolean highlighted) {
        super.setHighlighted(highlighted);
        ShapeAttributes attrs = this.getActiveAttributes();
        System.out.println("Highlighted Feature ID: " + getFeatureId());
    }

    /**
     * ShapefilePolygons cannot be moved.
     * @param position ignored.
     */
    @Override
    public void move(Position position) {
        //super.move(position);
    }

    /**
     * ShapefilePolygons cannot be moved.
     * @param position ignored
     */
    @Override
    public void moveTo(Position position) {
        //super.moveTo(position);
    }

}
