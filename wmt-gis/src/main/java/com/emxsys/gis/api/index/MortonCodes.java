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
package com.emxsys.gis.api.index;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.util.MathUtil;

/**
 *
 * See: http://fgiesen.wordpress.com/2009/12/13/decoding-morton-codes/<br/>
 * and: http://www-graphics.stanford.edu/~seander/bithacks.html#InterleaveBMN
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: MortonCodes.java 540 2013-04-18 15:48:26Z bdschubert $
 */
public class MortonCodes {

    /**
     * Generates a 64-bit morton code from the supplied coordinate.
     * @param point The coordinate for the morton code.
     * @return A 64-bit morton code.
     */
    public static long generate(Coord2D point) {
        int x = degreesToFixed((float) (point.getLongitudeDegrees()));
        int y = degreesToFixed((float) (point.getLatitudeDegrees()));

        return generate(x, y);
    }

    public static long generate(int x, int y) {
        long z = interleaveBits(x, y);
        return z;
    }

    /**
     * Extracts the x value from a Morton Code.
     *
     * @param code The Morton Code number.
     * @return The x value.
     */
    public static int getX(long code) {
        // Extract the even bits
        return compact64By1(code);
    }

    /**
     * Extracts the y value from a Morton Code.
     *
     * @param code The Morton Code number.
     * @return The y value.
     */
    public static int getY(long code) {
        // Extract the odd bits
        return compact64By1(code >>> 1);
    }

    /**
     * Converts degrees to a 32-bit fixed-point integer.
     * <p/>
     * @param degrees Angle must be between +/-180 degrees
     * @return A fixed-point representation of Q9.22.
     */
    public static int degreesToFixed(float degrees) {
        return MathUtil.floatToFixed(9, 22, degrees + 180.0f);
    }

    /**
     * Converts a 32-bit fixed-point integer to degrees.
     * <p/>
     * @return A double value in degrees.
     */
    public static double fixedToDegrees(int fixed) {
        double degrees = MathUtil.fixedToDouble(22, fixed);
        return degrees - 180.0;
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
    public static long interleaveBits(int x, int y) {
        long z = 0;
        for (int i = 0; i < Integer.SIZE; i++) // unroll for more speed...
        {
            z |= (x & 1L << i) << i | (y & 1L << i) << (i + 1);
        }
        return z;
    }

    /**
     * Interleave bits of x and y, so that all of the bits of x are in the even positions.<br/><br/>
     * http://www-graphics.stanford.edu/~seander/bithacks.html#InterleaveTableObvious<br/>
     *
     * @param x 32 bit coordinate
     * @param y 32 bit coordinate
     * @return The resulting 64 bit Morton Code.
     */
    public static long interleave64Bits(int x, int y) {
        long z = (expand32By1(y) << 1) + expand32By1(x);
        return z;
    }

    /**
     * Interleave bits of x and y, so that all of the bits of x are in the even positions.<br/><br/>
     * http://www-graphics.stanford.edu/~seander/bithacks.html#InterleaveTableObvious<br/>
     *
     * @param x 32 bit coordinate
     * @param y 32 bit coordinate
     * @return The resulting 64 bit Morton Code.
     */
    public static int interleave32Bits(short x, short y) {
        int z = (expand16By1(y) << 1) + expand16By1(x);
        return z;
    }

    /**
     * Insert a 0 bit before each bit of the 32-bit num.
     * <p/>
     * @param num 32-bit number to be expanded.
     * @return 64-bit bit pattern with 0 bits inter-spaced in the original 16-bit bit pattern.
     */
    public static long expand32By1(int num) {
        // "Insert" a 0 bit after each of the 32 low bits of x
        long x = num & 0x00000000ffffffffL;         // x = ---- ---- ---- ---- ---- ---- ---- ---- fedc ba98 7654 3210 fedc ba98 7654 3210
        x = (x ^ (x << 16)) & 0x0000ffff0000ffffL;  // x = ---- ---- ---- ---- fedc ba98 7654 3210 ---- ---- ---- ---- fedc ba98 7654 3210
        x = (x ^ (x << 8)) & 0x00ff00ff00ff00ffL;   // x = ---- ---- fedc ba98 ---- ---- 7654 3210 ---- ---- fedc ba98 ---- ---- 7654 3210
        x = (x ^ (x << 4)) & 0x0f0f0f0f0f0f0f0fL;   // x = ---- fedc ---- ba98 ---- 7654 ---- 3210 ---- fedc ---- ba98 ---- 7654 ---- 3210
        x = (x ^ (x << 2)) & 0x3333333333333333L;   // x = --fe --dc --ba --98 --76 --54 --32 --10 --fe --dc --ba --98 --76 --54 --32 --10
        x = (x ^ (x << 1)) & 0x5555555555555555L;   // x = -f-e -d-c -b-a -9-8 -7-6 -5-4 -3-2 -1-0 -f-e -d-c -b-a -9-8 -7-6 -5-4 -3-2 -1-0
        return x;
    }

    /**
     * Insert a 0 bit after each bit of the 16-bit num, such that: <br/> f(fedc ba98 7654 3210) =
     * -f-e -d-c -b-a -9-8 -7-6 -5-4 -3-2 -1-0
     * <p/>
     * See:
     * <p/>
     * @param num 16-bit number to be expanded.
     * @return 32-bit pattern with 0 bits inter-spaced in the original 16-bits.
     */
    public static int expand16By1(short num) {
        // "Insert" a 0 bit after each of the 16 low bits of x
        int x = num & 0x0000ffff;        // x = ---- ---- ---- ---- fedc ba98 7654 3210
        x = (x ^ (x << 8)) & 0x00ff00ff; // x = ---- ---- fedc ba98 ---- ---- 7654 3210
        x = (x ^ (x << 4)) & 0x0f0f0f0f; // x = ---- fedc ---- ba98 ---- 7654 ---- 3210
        x = (x ^ (x << 2)) & 0x33333333; // x = --fe --dc --ba --98 --76 --54 --32 --10
        x = (x ^ (x << 1)) & 0x55555555; // x = -f-e -d-c -b-a -9-8 -7-6 -5-4 -3-2 -1-0
        return x;

    }

    /**
     * Insert two 0 bits after each bit of the 10-bit num, such that: <br/> f(---- --98 7654 3210) =
     * ---- 9--8 --7- -6-- 5--4 --3- -2-- 1--0
     * <p/>
     * @param num 10-bit number to be expanded.
     * @return 32-bit pattern with two 0 bits inter-spaced in the original low 10-bits.
     */
    public static int expand10By2(short num) {
        // "Insert" two 0 bits after each of the 10 low bits of x
        int x = num & 0x000003ff;           // x = ---- ---- ---- ---- ---- --98 7654 3210
        x = (x ^ (x << 16)) & 0xff0000ff;   // x = ---- --98 ---- ---- ---- ---- 7654 3210
        x = (x ^ (x << 8)) & 0x0300f00f;    // x = ---- --98 ---- ---- 7654 ---- ---- 3210
        x = (x ^ (x << 4)) & 0x030c30c3;    // x = ---- --98 ---- 76-- --54 ---- 32-- --10
        x = (x ^ (x << 2)) & 0x09249249;    // x = ---- 9--8 --7- -6-- 5--4 --3- -2-- 1--0
        return x;
    }

    /**
     * Inverse of expand32By1 - "delete" all odd-indexed bits
     * <p/>
     * @param num 64-bit number to be compacted.
     * @return 32-bit pattern with inter-spaced bits removed from the original 64-bit pattern
     * pattern.
     */
    public static int compact64By1(long num) {
        // using unsigned shift right (>>>)
        long x = num & 0x5555555555555555L;         // x = -f-e -d-c -b-a -9-8 -7-6 -5-4 -3-2 -1-0 -f-e -d-c -b-a -9-8 -7-6 -5-4 -3-2 -1-0
        x = (x ^ (x >>> 1)) & 0x3333333333333333L;  // x = --fe --dc --ba --98 --76 --54 --32 --10 --fe --dc --ba --98 --76 --54 --32 --10
        x = (x ^ (x >>> 2)) & 0x0f0f0f0f0f0f0f0fL;  // x = ---- fedc ---- ba98 ---- 7654 ---- 3210 ---- fedc ---- ba98 ---- 7654 ---- 3210
        x = (x ^ (x >>> 4)) & 0x00ff00ff00ff00ffL;  // x = ---- ---- fedc ba98 ---- ---- 7654 3210 ---- ---- fedc ba98 ---- ---- 7654 3210
        x = (x ^ (x >>> 8)) & 0x0000ffff0000ffffL;  // x = ---- ---- ---- ---- fedc ba98 7654 3210 ---- ---- ---- ---- fedc ba98 7654 3210
        x = (x ^ (x >>> 16)) & 0x00000000ffffffffL; // x = ---- ---- ---- ---- ---- ---- ---- ---- fedc ba98 7654 3210 fedc ba98 7654 3210

        return (int) x;
    }

    /**
     * Inverse of expand16By1 - "delete" all odd-indexed bits
     * <p/>
     * @param num 32-bit number to be compacted.
     * @return 16-bit bit pattern with inter-spaced bits removed from the original 32-bit bit
     * pattern.
     */
    public static short compact32By1(int num) {
        // using unsigned shift right (>>>)
        int x = num & 0x55555555;         // x = -f-e -d-c -b-a -9-8 -7-6 -5-4 -3-2 -1-0
        x = (x ^ (x >>> 1)) & 0x33333333; // x = --fe --dc --ba --98 --76 --54 --32 --10
        x = (x ^ (x >>> 2)) & 0x0f0f0f0f; // x = ---- fedc ---- ba98 ---- 7654 ---- 3210
        x = (x ^ (x >>> 4)) & 0x00ff00ff; // x = ---- ---- fedc ba98 ---- ---- 7654 3210
        x = (x ^ (x >>> 8)) & 0x0000ffff; // x = ---- ---- ---- ---- fedc ba98 7654 3210
        return (short) x;
    }

    /**
     * Inverse of expand10By2.
     * <p/>
     * @param num 32-bit number to be compacted.
     * @return 16-bit bit pattern with inter-spaced bits removed from the original 32-bit bit
     * pattern.
     */
    public static short compactBy2(int num) {
        // TODO: Consider using unsigned shift right (>>>)
        int x = num & 0x09249249;            // x = ---- 9--8 --7- -6-- 5--4 --3- -2-- 1--0
        x = (x ^ (x >>> 2)) & 0x030c30c3;    // x = ---- --98 ---- 76-- --54 ---- 32-- --10
        x = (x ^ (x >>> 4)) & 0x0300f00f;    // x = ---- --98 ---- ---- 7654 ---- ---- 3210
        x = (x ^ (x >>> 8)) & 0xff0000ff;    // x = ---- --98 ---- ---- ---- ---- 7654 3210
        x = (x ^ (x >>> 16)) & 0x000003ff;   // x = ---- ---- ---- ---- ---- --98 7654 3210
        return (short) x;
    }
}
