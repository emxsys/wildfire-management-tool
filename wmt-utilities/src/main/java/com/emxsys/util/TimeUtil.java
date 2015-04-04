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
package com.emxsys.util;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Class utility for new Java DateTime objects.
 * 
 * @see DateUtil
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class TimeUtil {

    /**
     * Convert a long ("time_t") to seconds and thousandths. From the Java Cookbook by Ian Darwin.
     *
     * Example that outputs elapsed time for a task:
     * <pre>
     * long startTimeMillis = System.currentTimeMillis();
     * // Do something ...
     * System.out.println("Elapsed: " + TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMillis));
     * </pre>
     *
     * Source: Java Cookbook by Ian Darwin
     * 
     * @param msTime Time in milliseconds
     * @return Time in seconds: "123 s"
     */
    public static String msToSecs(long msTime) {
        return Double.toString(msTime / 1000D) + " s";
    }
    
    /**
     * Returns a the time truncated to the start of the day (midnight).
     * @param time The date/time to truncate.
     * @return A new date/time set to midnight.
     */
    public static ZonedDateTime toStartOfDay(ZonedDateTime time) {
        return time.truncatedTo(ChronoUnit.DAYS);
    }
    
    public static ZonedDateTime toUTC(ZonedDateTime time) {
        return time.withZoneSameInstant(ZoneId.of("UTC"));
    }
    
    public static ZonedDateTime toZoneOffset(ZonedDateTime time, double offsetHours) {
        return time.withZoneSameInstant(ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds((int) (offsetHours * 3600))));        
    }
}
