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

import static java.lang.Math.*;


/**
 * Useful mathematic functions.
 * <p/>
 * @author Bruce Schubert
 * @version $Revision$
 */
public class MathUtil
{

    public final static double EPSILON_TOLERANCE = 0.0000001;


    /**
     * Compare two doubles with a given epsilon (tolerance)
     *
     * @param a       lhs value to be compared
     * @param b       rhs value to be compared
     * @param epsilon tolerance
     * @return true if the difference between a and b is less than epsilon
     */
    public static boolean nearlyEquals(double a, double b, double epsilon)
    {
        if (a == b)
        {
            return true;
        }
        return (abs(a - b) < epsilon);
    }


    /**
     * Compare two doubles using a computed epsilon based on a default tolerance.
     * The epsilon will be 0.00001% (tolerance) of the largest value (a or b).
     * <p/>
     * @param a lhs value to be compared
     * @param b rhs value to be compared
     * @return true if the difference between a and b is less than the computed epsilon.
     */
    public static boolean nearlyEquals(double a, double b)
    {
        if (a == b)
        {
            return true;
        }
        // If the difference is less than epsilon, treat as equal
        double epsilon = EPSILON_TOLERANCE * max(abs(a), abs(b));
        return abs(a - b) < epsilon;
    }


    /**
     * Returns a fixed-point number as defined by the Q format. Q is a fixed point number format
     * where the number of fractional bits (and optionally the number of integer bits) is specified.
     * For example, a Q15 number has 15 fractional bits; a Q1.14 number has 1 integer bit and 14
     * fractional bits. The Q notation is written as Qm.n.
     * <p/>
     * To convert a number from floating point to Qm.n format:<br/>
     * 1. Multiply the floating point number by 2^n<br/>
     * 2. Round to the nearest integer<br/>
     *
     * @param m      the number of bits set aside to designate the two's complement integer portion
     * of the number, exclusive of the sign bit (therefore if m is not specified it is
     * taken as zero).
     * @param n      the number of bits used to designate the fractional portion of the number, i.e.
     * the number of bits to the right of the binary point. (If n = 0, the Q numbers
     * are integers — the degenerate case).
     * @param number the floating point number to be converted.
     * @return A fixed-point value as defined by the Q format.
     */
    public static long doubleToFixed(int m, int n, double number)
    {
        if ((m + n + 1) > 64)
        {
            throw new IllegalArgumentException("Incorrect Q format: m (" + m + ") + n (" + n + ") must be less than 64.");
        }
        // Ensure integer part is < 2^m (allowing for round up of fractional part)
        if (round(abs(number)) > (1L << m))
        {
            throw new IllegalArgumentException("number (" + number + ") is too big for the specified m (" + m + ") component: round(" + number + ") > pow(2," + m + ")");
        }
        long fixed = doubleToFixed(n, number);

        return fixed;
    }


    /**
     * Returns a fixed-point number as defined by the Q format. Q is a fixed point number format
     * where the number of fractional bits (and optionally the number of integer bits) is specified.
     * For example, a Q15 number has 15 fractional bits; a Q1.14 number has 1 integer bit and 14
     * fractional bits. The Q notation is written as Qm.n.
     * <p/>
     * To convert a number from floating point to Qm.n format:<br/>
     * 1. Multiply the floating point number by 2^n<br/>
     * 2. Round to the nearest integer<br/>
     *
     * @param m      the number of bits set aside to designate the two's complement integer portion
     * of the number, exclusive of the sign bit (therefore if m is not specified it is
     * taken as zero).
     * @param n      the number of bits used to designate the fractional portion of the number, i.e.
     * the number of bits to the right of the binary point. (If n = 0, the Q numbers
     * are integers — the degenerate case).
     * @param number the floating point number to be converted.
     * @return A fixed-point value as defined by the Q format.
     */
    public static int floatToFixed(int m, int n, float number)
    {
        if ((m + n + 1) > 32)
        {
            throw new IllegalArgumentException("Incorrect Q format: m (" + m + ") + n (" + n + ") must be less than 32.");
        }
        // Ensure integer part is < 2^m (allowing for round up of fractional part)
        if (round(abs(number)) > (1 << m))
        {
            throw new IllegalArgumentException("number (" + number + ") is too big for the specified m (" + m + ") component: round(" + number + ") > pow(2," + m + ")");
        }
        int fixed = floatToFixed(n, number);

        return fixed;
    }


    /**
     * Returns a 64 bit fixed-point representation by scaling the double value by a scale factor
     * that is a power of 2.
     *
     * @param numScaleBits The number of bits used to determine the scaling factor, i.e, scale
     * factor = pow(2,numBits). This is also the number of bits used to designate the fractional
     * portion of the number, i.e. the number of bits to the right of the binary point.
     * @param number       The floating point number to be scaled.
     * @return A 64 bit fixed-point value.
     */
    public static long doubleToFixed(int numScaleBits, double number)
    {
        // 1. Multiply the floating point number by scale (2^numBits)
        // 2. Round to the nearest integer       
        
        // TODO: consider using scalb, e.g., (long) Math.scalb(number, numScaleBits)
        return round(number * (1L << numScaleBits));
    }


    /**
     * Returns a 32 bit fixed-point representation by scaling the float value by a scale factor
     * that is a power of 2.
     *
     * @param numScaleBits The number of bits used to determine the scaling factor, i.e, scale
     * factor = pow(2,numBits). This is also the number of bits used to designate the fractional
     * portion of the number, i.e. the number of bits to the right of the binary point.
     * @param number       The floating point number to be scaled.
     * @return A 32 bit fixed-point value.
     */
    public static int floatToFixed(int numScaleBits, float number)
    {
        // 1. Multiply the floating point number by scale (2^numBits)
        // 2. Round to the nearest integer       
        return round(number * (1 << numScaleBits));
    }


    /**
     * Returns a double from a fixed-point value -- the inverse of doubleToFixed. The return value
     * will be within 1/pow(2,numScaleBits) of the original floating point number.
     *
     * @param numScaleBits The number of bits used to determine the scaling factor, i.e, scale
     * factor = pow(2,numBits). Also the number of bits used to designate the fractional portion of
     * the number, i.e. the number of bits to the right of the binary point.
     * @param fixedPoint   The 64 bit fixed point number.
     * @return A double within the resolution of 1/pow(2,numScaleBits).
     */
    public static double fixedToDouble(int numScaleBits, long fixedPoint)
    {
        return (double) fixedPoint / (1L << numScaleBits);
    }


    /**
     * Returns a float from a fixed-point value -- the inverse of floatToFixed. The return value
     * will be within 1/pow(2,numScaleBits) of the original floating point number.
     *
     * @param numScaleBits The number of bits used to determine the scaling factor, i.e, scale
     * factor = pow(2,numBits). Also the number of bits used to designate the fractional portion of
     * the number, i.e. the number of bits to the right of the binary point.
     * @param fixedPoint   The32 bit fixed-point number.
     * @return A float within the resolution of 1/pow(2,numScaleBits).
     */
    public static float fixedToFloat(int numScaleBits, int fixedPoint)
    {
        return (float) fixedPoint / (1 << numScaleBits);
    }
}
