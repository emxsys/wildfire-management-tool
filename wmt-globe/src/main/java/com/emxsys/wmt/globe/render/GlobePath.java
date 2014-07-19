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
package com.emxsys.wmt.globe.render;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.wmt.globe.util.Positions;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class renders a line between two points.
 *
 * @author Bruce Schubert
 */
public class GlobePath extends Path {

    public GlobePath() {
    }

    public GlobePath(Coord3D begin, Coord3D end) {
        super(Positions.fromCoord3D(begin), Positions.fromCoord3D(end));
    }

    public void update(Coord3D begin, Coord3D end) {
        if (begin == null || begin.isMissing() || end == null || end.isMissing()) {
            throw new IllegalArgumentException("update() argument(s) null or missing.");
        }
        List<Position> endPoints = new ArrayList<>(2);
        endPoints.add(Positions.fromCoord3D(begin));
        endPoints.add(Positions.fromCoord3D(end));
        super.setPositions(endPoints);
    }
}
