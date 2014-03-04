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
package com.emxsys.wmt.util;

/**
 * The zoom factor is directly proportional to the magnification of the scene being viewed.
 * The relationship between zoom and FOV is slightly more complex, and is given by the following:
 *
 * fov = 2 * arctan(1 / zoom)
 * zoom = 1 / tan(fov / 2)  *
 * where fov is the horizontal or width-wise Field Of View (ie. in the camera's horizontal plane
 * and the image's x-direction). It must be in radians if the trigonometric functions use radians
 * instead of degrees. (Degree-radian conversions here.)
 * Note that a zoom value of exactly 1 gives a 90 degree horizontal FOV, but a zoom of 2 does not
 * give a horizontal FOV of 45 degrees.
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class OpticsUtil {

    /**
     *
     * @param fovDeg field of view in degrees
     * @return zoom level where a 90 degree FOV equals 1x zoom
     */
    public static double fovToZoom(double fovDeg) {
        // zoom = 1 / tan(fov / 2)
        double zoom = 1 / (Math.tan(Math.toRadians(fovDeg) / 2));
        return zoom;
    }
}
