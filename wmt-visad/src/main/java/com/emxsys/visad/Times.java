/*
 * Copyright (c) 2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.visad;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.openide.util.Exceptions;
import visad.*;

/**
 * Utility class for converting to/from VisAD DateTime objects.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class Times {

    private Times() {
    }

    /**
     * Convenient method to get a new DateTime from a Java ZonedDateTime.
     *
     * @param time A Java ZonedDateTime.
     * @return A UTC DateTime (RealType.Time)
     */
    static public DateTime fromZonedDateTime(ZonedDateTime time) {
        try {
            LocalDateTime localTime = time.toLocalDateTime();
            Instant instant = localTime.toInstant(time.getOffset());
            return new DateTime(instant.getEpochSecond());
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Convenient method to get a new DateTime object from a Java Calendar Date object.
     *
     * @param date A Java Date.
     * @return A DateTime object (RealType.Time)
     */
    @Deprecated
    static public DateTime fromDate(Date date) {
        try {
            return new DateTime(date);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Convenient method to get a new DateTime object from a double value
     *
     * @param seconds seconds from epoch (equivalent to DataTime.getValue().
     * @return A DateTime object (RealType.Time)
     */
    static public DateTime fromDouble(double seconds) {
        try {
            return new DateTime(seconds);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalArgumentException(ex);
        }
    }

    private static Calendar setCalendarToHour(Calendar cal, double hour) {
        int h = (int) (hour % 24);
        int m = (int) ((hour - h) * 60);
        int s = (int) Math.round(((hour - h) * 3600) % 60);
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);
        cal.set(Calendar.SECOND, s);
        return cal;
    }

    /**
     * Convenient method to get a new DateTime object from the current Calendar time.
     *
     * @return A DateTime object (RealType.Time)
     */
    static public DateTime fromCurrentTime() {
        return fromDate(Calendar.getInstance().getTime());
    }

    static public DateTime getMidnightGMT(Real dateTime) {
        if (!RealType.Time.equals(dateTime.getType())) {
            throw new IllegalArgumentException("Argument math type must be: "
                    + RealType.Time.toString() + ", not " + dateTime.getType().toString());
        }
        try {
            final double SECS_PER_DAY = 86400;    // 60 * 60 * 24;
            double secsSinceMidnight = dateTime.getValue() % SECS_PER_DAY;
            return new DateTime(dateTime.getValue() - secsSinceMidnight);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Return a new Java ZonedDateTime UTC object from a VisAD DateTime.
     *
     * @param dateTime A RealType.Time or DateTime object.
     * @return A ZonedDateTime UTC object.
     */
    static public ZonedDateTime toZonedDateTime(Real dateTime) {
        if (!RealType.Time.equals(dateTime.getType())) {
            throw new IllegalArgumentException("Argument math type must be: "
                    + RealType.Time.toString() + ", not " + dateTime.getType().toString());
        }
        try {
            Instant instant = Instant.ofEpochSecond((long) (dateTime.getValue(CommonUnit.secondsSinceTheEpoch)));
            return ZonedDateTime.ofInstant(instant, ZoneId.of("Z"));
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Convert a DateTime in stored in GMT to a 24 clock time. E.g., 2011-12-01 14:30GMT returns
     * 14.5.
     *
     * @param dateTime A Real(Time) or DateTime object
     * @return A decimal representing the 24 hour clock time in the GMT.
     */
    static public double toClockTimeGMT(Real dateTime) {
        if (!RealType.Time.equals(dateTime.getType())) {
            throw new IllegalArgumentException("Argument math type must be: "
                    + RealType.Time.toString() + ", not " + dateTime.getType().toString());
        }
        try {
            double val = dateTime.getValue(GeneralUnit.hour);
            val %= 24.0;
            return val;
        }
        catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Convert a DateTime in stored in GMT to 24 hour clock time in the local timezone. E.g., in
     * PST, 2011-12-01 14:30GMT returns 6.5.
     *
     * @param dateTime A Real(Time) or DateTime object
     * @return A decimal representing 24 hour clock time in the local timezone.
     */
    static public double toClockTime(Real dateTime) {
        if (!RealType.Time.equals(dateTime.getType())) {
            throw new IllegalArgumentException("Argument math type must be: "
                    + RealType.Time.toString() + ", not " + dateTime.getType().toString());
        }
        try {
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            double RAW_TZ_OFFSET_HOURS = tz.getRawOffset() / 3600000;
            double DST_TZ_OFFSET_HOURS = tz.getDSTSavings() / 3600000;

            double hours = dateTime.getValue(GeneralUnit.hour);
            hours += RAW_TZ_OFFSET_HOURS + (tz.inDaylightTime(cal.getTime()) ? DST_TZ_OFFSET_HOURS : 0);
            hours %= 24.0;
            return hours;
        }
        catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }

    }

    /**
     * Convert the seconds value from a DateTime to 24 hour clock time in the local timezone. E.g.,
     * in PST, 2011-12-01 14:30GMT returns 6.5.
     *
     * @param dateTimeSecsGMT A typical value stored in a time domain.
     * @return A decimal representing 24 hour clock time in the local timezone.
     */
    static public double toClockTime(double dateTimeSecsGMT) {
        try {
            return toClockTime(new DateTime(dateTimeSecsGMT));
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Creates a new daily time set starting with the given datetime for the specified number of
     * days.
     * @param start
     * @param numDays
     * @return A new time domain set.
     */
    public static Gridded1DDoubleSet makeDailyTimeSet(ZonedDateTime start, int numDays) {
        if (numDays < 1) {
            throw new IllegalArgumentException("numDays must be greater than 0");
        }
        try {
            double times[] = new double[numDays];
            for (int i = 0; i < times.length; i++) {
                ZonedDateTime time = start.plusDays(i);
                times[i] = time.toEpochSecond();
            }
            Gridded1DDoubleSet timeSet = DateTime.makeTimeSet(times);
            return timeSet;
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Creates a new hourly time set starting with the given datetime for the specified number of
     * hours.
     * @param start
     * @param numHours
     * @return
     */
    public static Gridded1DDoubleSet makeHourlyTimeSet(ZonedDateTime start, int numHours) {
        if (numHours < 1) {
            throw new IllegalArgumentException("number of hours must be greater than 0");
        }
        try {
            double times[] = new double[numHours];
            for (int i = 0; i < times.length; i++) {
                ZonedDateTime time = start.plusHours(i);
                times[i] = time.toEpochSecond();
            }
            Gridded1DDoubleSet timeSet = DateTime.makeTimeSet(times);
            return timeSet;
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    public static DateTime getFirstDateTime(Gridded1DSet timeSet) {
        try {
            double value = timeSet.getLowX();
            return new DateTime(value);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    public static DateTime getLastDateTime(Gridded1DSet timeSet) {
        try {
            double value = timeSet.getHiX();
            return new DateTime(value);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }
}
