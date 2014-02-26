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
package com.emxsys.wmt.gis;

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.util.MathUtil;


/**
 * Generates a Morton Code for a coordinate.
 * 
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: MortonCodeFactory.java 540 2013-04-18 15:48:26Z bdschubert $
 */
public class MortonCodeFactory
{

    public static long generateCode(Coord2D point)
    {
        int x = degreesToFixed(point.getLongitudeDegrees());
        int y = degreesToFixed(point.getLatitudeDegrees());
        long z = interleaveBits(x, y);
        return z;
    }


    /**
     * Converts degrees to a 32-bit fixed-point integer.
     * <p/>
     * @param degrees Angle must be between +/-180 degrees
     * @return A fixed-point representation of Q9.22.
     */
    private static int degreesToFixed(double degrees)
    {
        return (int) MathUtil.doubleToFixed(9, 22, degrees + 180.0);
    }


    /**
     * Interleave bits of x and y, so that all of the bits of x are in the even positions.<br/><br/>
     * Source: Interleave bits the obvious way from Twiddling Hacks by Sean Eron Anderson
     * (seander@cs.stanford.edu) -- in the public domain. <br/><br/> See:
     * http://www-graphics.stanford.edu/~seander/bithacks.html#InterleaveTableObvious<br/>
     *
     * @param x 32 bit coordinate
     * @param y 32 bit coordinate
     * @return The resulting 64 bit Morton Code.
     */
    public static long interleaveBits(int x, int y)
    {
        long z = 0;
        for (int i = 0; i < Integer.SIZE; i++) // unroll for more speed...
        {
            z |= (x & 1L << i) << i | (y & 1L << i) << (i + 1);
        }
        return z;
    }


    private MortonCodeFactory()
    {
    }
}
