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
package com.emxsys.wildfire.behavior;

import static com.emxsys.gis.api.GisType.DISTANCE;
import static java.lang.Math.sqrt;
import java.time.Duration;
import org.openide.util.Exceptions;
import static visad.CommonUnit.meterPerSecond;
import visad.Real;
import visad.VisADException;

/**
 * FireEllipse contains the geometry used represent a wind and/or slope driven fire.
 *
 * @author Bruce Schubert
 */
public class FireEllipse {

    private final Real majorRadius;
    private final Real minorRadius;
    private final Real focalOffset;
    private final Real heading;

    /**
     * Gets an elliptical data structure representing a fire's shape from a point origin after the
     * given duration.
     * @param fire The fire behavior defining the ellipse.
     * @param duration The amount of time the fire burns.
     * 
     * @return A FireEllipse data structure containing the major and minor radii and the offset from
     * the center to the point of origin.
     */
    public static FireEllipse from(SurfaceFire fire, Duration duration) {
        try {
            Real heading = fire.getDirectionMaxSpread();
            double eccentricity = fire.getEccentricity();
            long seconds = duration.getSeconds();
            double a1 = fire.getRateOfSpreadBacking().getValue(meterPerSecond) * seconds;
            double a2 = fire.getRateOfSpreadMax().getValue(meterPerSecond) * seconds;
            double majorRadius = (a1 + a2) / 2.;
            double minorRadius = majorRadius * sqrt(1 - (eccentricity * eccentricity));
            double focalOffset = majorRadius - a1;

            return new FireEllipse(
                    new Real(DISTANCE, majorRadius),
                    new Real(DISTANCE, minorRadius),
                    new Real(DISTANCE, focalOffset),
                    heading);

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public FireEllipse(Real majorRadius, Real minorRadius, Real focalOffset, Real heading) {
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
        this.focalOffset = focalOffset;
        this.heading = heading;
    }

    public Real getMajorRadius() {
        return majorRadius;
    }

    public Real getMinorRadius() {
        return minorRadius;
    }

    public Real getOriginOffsetFromCenter() {
        return focalOffset;
    }

    public Real getHeading() {
        return heading;
    }

}
