/*
 * Copyright (c) 2010-2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.gis.api.index;

import com.emxsys.wmt.gis.api.GeoSector;
import com.emxsys.wmt.gis.api.Coord2D;
import java.util.*;
import java.util.Map.Entry;

/**
 * A container of type T, indexed by morton codes. 
 * @author Bruce Schubert <bruce@emxsys.com>
 * @param <T>
 */
public class LinearQuadtree<T> {

    final long ODD_MASK = 0xAAAAAAAAAAAAAAAAL;
    final long EVEN_MASK = 0x5555555555555555L;
    final long ODD_HASH_MASK = 0xAAAAAAAAAAA00000L;
    final long EVEN_HASH_MASK = 0x5555555555000000L;
    private final TreeMap<Long, Set<T>> map = new TreeMap<>();

    public LinearQuadtree() {
    }

    /**
     * Adds an item to the container.
     * @param pt The coordinate used to generate a morton code.
     * @param item The item to be placed in the container.
     * @return Returns true if the item is not already in the container.
     */
    public boolean add(Coord2D pt, T item) {
        long mortonCode = MortonCodes.generate(pt);
        Set<T> values = map.get(mortonCode);
        if (values == null) {
            values = new HashSet<>();
            map.put(mortonCode, values);
        }
        return values.add(item);
    }

    public Collection<T> findByBruteForce(GeoSector sector) {
        HashSet<T> result = new HashSet<>();

        long zMin = MortonCodes.generate(sector.getSouthwest());
        long zMax = MortonCodes.generate(sector.getNortheast());
        long yMin = zMin & ODD_MASK;
        long yMax = zMax & ODD_MASK;
        long xMin = zMin & EVEN_MASK;
        long xMax = zMax & EVEN_MASK;

        Set<Long> keySet = map.subMap(zMin, zMax).keySet();
//        System.out.println("findByBruteForce...");
//        System.out.println(" - Num keys in sector: " + keySet.size());
        for (Long key : keySet) {
            long x = key & EVEN_MASK;
            long y = key & ODD_MASK;
            if (x >= xMin && x <= xMax && y >= yMin && y <= yMax) {
                Set<T> values = map.get(key);
                result.addAll(values);
            }
        }
//        System.out.println(" - Num results: " + result.size());
        return result;
    }

    public Collection<T> findByRangeSubDivision(GeoSector sector) {
        HashSet<T> result = new HashSet<>();
        long zMin = MortonCodes.generate(sector.getSouthwest());
        long zMax = MortonCodes.generate(sector.getNortheast());
        
        Range inputRange = new Range(zMin, zMax);
        ArrayList<Range> subRanges = new ArrayList<>();
        HashSet<Long> keysToIgnore = new HashSet<>();
//        System.out.println("findByRangeSubDivision...");
        scanRangeForKeys(inputRange, subRanges, keysToIgnore);

// Old: Faster!
        for (Range range : subRanges) {
            Set<Entry<Long, Set<T>>> entrySet = map.subMap(range.min, range.max).entrySet();
            for (Entry<Long, Set<T>> entry : entrySet) {
                if (keysToIgnore.contains(entry.getKey())) {
                    continue;
                }
                result.addAll(entry.getValue());
            }
        }
// Java8: Slower  :(
//        subRanges.stream().map((range) -> map.subMap(range.min, range.max).entrySet()).forEach((entrySet) -> {
//            entrySet.stream().filter((entry) -> !(keysToIgnore.contains(entry.getKey()))).forEach((entry) -> {
//                result.addAll(entry.getValue());
//            });
//        });
        
//        System.out.println(" - Num ranges to test: " + subRanges.size());
//        System.out.println(" - Num keys in ranges: " + numKeys);
//        System.out.println(" - Num keys to ignore: " + keysToIgnore.size());
//        System.out.println(" - Num results: " + result.size());
        return result;
    }

    private void scanRangeForKeys(Range inputRange,
                                  ArrayList<Range> outputRanges,
                                  Set<Long> keysOutsideRange) {
        // Start a recursive scan
        scanRangeForKeys(inputRange, outputRanges, keysOutsideRange, 0);
    }

    private void scanRangeForKeys(Range inputRange,
                                  ArrayList<Range> outputRanges,
                                  Set<Long> keysOutsideRange, long zLast) {
        long zMin = inputRange.min;
        long zMax = inputRange.max;
        // Get the bit patterns representing the corners of the range 
        // (note, these are not the x and y values)
        long yMin = zMin & ODD_MASK;
        long xMin = zMin & EVEN_MASK;
        long yMax = zMax & ODD_MASK;
        long xMax = zMax & EVEN_MASK;

        // stats
        int numKeysOutsideRange = 0;
        int numSubdivides = 0;

        if (zLast < zMax) {
            // Get the keys that are within the range
            Set<Long> keySet = map.subMap(Math.max(zLast, zMin), zMax).keySet();

            // Test each key to see if it is within the region defined by min/max x,y
            for (Long z : keySet) {
                // Extract the x and y bit patterns
                long x = z & EVEN_MASK;
                long y = z & ODD_MASK;

                // Test if x or y are outside the bounds of the range
                if (x < xMin || x > xMax || y < yMin || y > yMax) {
                    keysOutsideRange.add(z);
                    numKeysOutsideRange++;
                    if (numKeysOutsideRange > 3) {
                        Range[] r = subDivideRange(inputRange);
                        scanRangeForKeys(r[0], outputRanges, keysOutsideRange, z);
                        scanRangeForKeys(r[1], outputRanges, keysOutsideRange, z);
                        numSubdivides++;
                        break;
                    }
                }
            }
        }
        // Save the inputRange if it wasn't subdivided
        if (numSubdivides == 0) {
            outputRanges.add(inputRange);
        }
    }

    private Range[] subDivideRange(Range range) {
        int xMin = MortonCodes.getX(range.min);
        int yMin = MortonCodes.getY(range.min);

        int xMax = MortonCodes.getX(range.max);
        int yMax = MortonCodes.getY(range.max);

        // Find the first significant bit that differs to determine whether to split on the x or y axis
        long leadingZeros = Long.numberOfLeadingZeros(range.min ^ range.max);

        // If an 'even' bit differs, then split on the y axis, otherwise split on x axis
        if (leadingZeros % 2 == 0) {   // vertical split: inherit x values from range and compute new y values...
            int[] yMaxMin = computeLitMaxBigMin(yMin, yMax);
            return new Range[]{
                new Range(MortonCodes.generate(xMin, yMin), MortonCodes.generate(xMax, yMaxMin[0])),
                new Range(MortonCodes.generate(xMin, yMaxMin[1]), MortonCodes.generate(xMax, yMax))
            };
        }
        else {   // horizontal split: inherit y values from range and compute new x values...
            int[] xMaxMin = computeLitMaxBigMin(xMin, xMax);
            return new Range[]{
                new Range(MortonCodes.generate(xMin, yMin), MortonCodes.generate(xMaxMin[0], yMax)),
                new Range(MortonCodes.generate(xMaxMin[1], yMin), MortonCodes.generate(xMax, yMax))
            };
        }
    }

    /**
     * Computes LITMAX and BIGMIN values.
     *
     * @param min
     * @param max
     * @return A integer array containing LITMAX and BIGMIN, e.g., [LITMAX, BIGMIN]
     */
    public static int[] computeLitMaxBigMin(int min, int max) {
        if (min == max) {
            return new int[]{
                min, max
            };
        }
        else if (min > max) {
            throw new IllegalArgumentException("min greater than max");
        }

        int lsbMask = Integer.highestOneBit(min ^ max);     // 0100 = highestOneBit(0110 ^ 0011)
        lsbMask += lsbMask - 1;                             // 0111 = 0100 + 0011
        int msbMask = ~lsbMask;                             // 1000

        // LITMAX is 'all common significant bits' in the range followed by a 0 and then 1's.
        // 0011 = (0110 & 1000) | (0011)
        int litMax = (min & msbMask) | Integer.highestOneBit(lsbMask) - 1;

        // BIGMAX is 'all common significant bits' in the range followed by a 1 and then 0's.
        // 0100 = (1010 & 1000) | (0100)
        int bigMin = (min & msbMask) | Integer.highestOneBit(lsbMask);

        return new int[]{
            litMax, bigMin
        };
    }

    private static class Range {

        long min;
        long max;

        Range(long min, long max) {
            this.min = min;
            this.max = max;
        }
    }
}
