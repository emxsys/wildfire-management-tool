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

import com.emxsys.gis.api.Coord2D;
import com.emxsys.wmt.globe.util.Positions;
import static com.emxsys.wmt.globe.util.Positions.fromCoord2D;
import com.terramenta.globe.utilities.QuickTipController;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfaceEllipse;
import java.awt.Color;
import visad.Real;

/**
 *
 * @author Bruce Schubert
 */
public class GlobeEllipse extends SurfaceEllipse {

    private BasicShapeAttributes attrs;

    public GlobeEllipse() {
        attrs = new BasicShapeAttributes();
    }

    public GlobeEllipse(Coord2D center, Real majorRadius, Real minorRadius, Real heading) {
        this(new BasicShapeAttributes(),
                fromCoord2D(center),
                majorRadius.getValue(),
                minorRadius.getValue(),
                Angle.fromDegrees(heading.getValue()));
    }

    GlobeEllipse(BasicShapeAttributes normalAttrs, LatLon center, double majorRadius, double minorRadius, Angle heading) {
        super(normalAttrs,
                center,
                majorRadius,
                minorRadius,
                heading);
        this.attrs = normalAttrs;
    }

    public void updateEllipse(Coord2D center, Real majorRadius, Real minorRadius, Real heading) {
        setCenter(Positions.fromCoord2D(center));
        setMajorRadius(majorRadius.getValue());
        setMinorRadius(minorRadius.getValue());
        setHeading(Angle.fromDegrees(heading.getValue()));
    }

    public void setInteriorColor(Color color) {
        attrs.setInteriorMaterial(new Material(color));
        setAttributes(attrs);
    }

    public void setRollOverText(String tooltip) {
        // The key (AVKey.DISPLAY_NAME) is defined in the Terramenta QuickTipController constructor
        if (tooltip == null || tooltip.isEmpty()) {
            removeKey(AVKey.DISPLAY_NAME);
        } else {
            setValue(AVKey.DISPLAY_NAME, tooltip);
        }
    }

    public void setHoverText(String tooltip) {
        // The key (AVKey.DESCRIPTION) is defined in the Terrament QuickTipController constructor
        if (tooltip == null || tooltip.isEmpty()) {
            removeKey(AVKey.DESCRIPTION);
        } else {
            setValue(AVKey.DESCRIPTION, tooltip);
        }
    }

}
