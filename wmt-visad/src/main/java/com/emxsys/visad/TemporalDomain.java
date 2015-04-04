/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com> 
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

import java.rmi.RemoteException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.DateTime;
import visad.Field;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DDoubleSet;
import visad.Gridded1DSet;
import visad.MathType;
import visad.RealType;
import visad.Set;
import visad.VisADException;

/**
 * A Temporal domain defined by a Gridded1DDoubleSet time set.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages({
    "ERR_TemporalDomainNotInitialized=Domain not initialized."
})
public class TemporalDomain {

    public static TemporalDomain from(ZonedDateTime dateTime) {
        return from(dateTime, 0);
    }

    public static TemporalDomain from(ZonedDateTime dateTime, int numHours) {
        return new TemporalDomain(dateTime, numHours);
    }

    public static TemporalDomain from(ZonedDateTime begin, ZonedDateTime end) {
        try {
            int utcOffset = begin.getOffset().getTotalSeconds();
            Gridded1DSet set = DateTime.makeTimeSet(new DateTime[]{
                Times.fromZonedDateTime(begin),
                Times.fromZonedDateTime(end)});
            return new TemporalDomain(set, utcOffset);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    private static final RealType TIME_DOMAIN_TYPE = RealType.Time;
    private Set timeDomainSet;
    private int timeZoneOffset = 0;
    private static final Logger logger = Logger.getLogger(TemporalDomain.class.getName());

    /**
     * Constructs an uninitialized TemporalDomain. You must invoke initialize(...) before using.
     */
    public TemporalDomain() {
    }

    /**
     * Constructs a TemporalDomain beginning at the given start time, for the specified number of
     * hours. The time zone offset is determined the start time ZoneId.
     *
     * @param start Sets the start time for the domain; also sets timezone offset from UTC.
     * @param numHours The number of hours in the domain.
     */
    public TemporalDomain(ZonedDateTime start, int numHours) {
        initialize(Times.makeHourlyTimeSet(start, numHours), start.getOffset().getTotalSeconds());
    }

    /**
     * Constructs a TemporalDomain from a Gridded1DDoubleSet of type RealType.Time
     *
     * @param timeDomainSet The time domain in UTC values.
     * @param offsetSeconds The local time zone offset (from UTC) in seconds.
     */
    public TemporalDomain(Set timeDomainSet, int offsetSeconds) {
        initialize(timeDomainSet, offsetSeconds);
    }

    /**
     * (Re)Initializes the TemporalDomain beginning at the given start time, for the specified
     * number of hours. The time zone offset is determined the start time ZoneId.
     *
     * @param start Sets the start time for the domain; also sets timezone offset from UTC.
     * @param numHours The number of hours in the domain.
     */
    public final void initialize(ZonedDateTime start, int numHours) {
        initialize(Times.makeHourlyTimeSet(start, numHours), start.getOffset().getTotalSeconds());
    }

    /**
     * (Re)Initializes the TemporalDomain from a Gridded1DDoubleSet of type RealType.Time
     *
     * @param timeDomainSet The time domain in UTC values.
     * @param offsetSeconds The time zone offset (from UTC) in seconds.
     */
    public final void initialize(Set timeDomainSet, int offsetSeconds) {
        // TODO: validate the timeDomain parameter
        this.timeDomainSet = timeDomainSet;
        this.timeZoneOffset = offsetSeconds;
    }

    /**
     * @return True if the domain has been initialized and is ready for use.
     */
    public boolean isInitialized() {
        return timeDomainSet != null;
    }

    public boolean contains(ZonedDateTime time) {
        if (this.getEarliest().isAfter(time) || this.getLatest().isBefore(time)) {
            return false;
        }
        return true;
    }

    /**
     * Constructor extracts temporal domains from a FieldImpl data type.
     *
     * @param data of type FieldImpl
     * @deprecated Untested!!
     */
    @Deprecated
    public TemporalDomain(FieldImpl data) {
        try {
            // Validate temporal requirements
            FunctionType temporalFunction = (FunctionType) data.getType();
            if (temporalFunction == null
                    || !(temporalFunction.getDomain().getComponent(0).equals(RealType.Time))) {
                throw new IllegalArgumentException("data domain type must be RealType.Time : "
                        + data.getType().prettyString());
            }
            // Extract the time set
            Field field = (Field) data;
            timeDomainSet = (Gridded1DDoubleSet) field.getDomainSet();

        }
        catch (IllegalArgumentException | VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalArgumentException(ex.toString());
        }
    }

    /**
     * Gets the number of times in the domain.
     * @return
     */
    public int getDomainSetLength() {
        try {
            if (!isInitialized()) {
                throw new IllegalStateException(Bundle.ERR_SpatialDomainNotInitialized());
            }
            return timeDomainSet.getLength();
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the earliest datetime in the domain.
     * @return A ZonedDateTime with the same time zone offset as the domain initializer.
     */
    public ZonedDateTime getEarliest() {
        try {
            if (!isInitialized()) {
                throw new IllegalStateException(Bundle.ERR_TemporalDomainNotInitialized());
            }
            Instant instant = Instant.ofEpochSecond((long) timeDomainSet.getDoubles(false)[0][0]);
            return ZonedDateTime.ofInstant(instant, ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds(timeZoneOffset)));
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the latest datetime in the domain.
     * @return A ZonedDateTime with the same time zone offset as the domain initializer.
     */
    public ZonedDateTime getLatest() {
        try {
            if (!isInitialized()) {
                throw new IllegalStateException(Bundle.ERR_TemporalDomainNotInitialized());
            }
            //Instant instant = Instant.ofEpochSecond((long) timeDomainSet.getDoubleHiX());
            double[][] samples = timeDomainSet.getDoubles(false);
            Instant instant = Instant.ofEpochSecond((long) samples[0][samples[0].length - 1]);
            return ZonedDateTime.ofInstant(instant, ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds(timeZoneOffset)));
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the datetime at the specified array index.
     * @param index The array index; must be less the domain set's length (e.g., numHours).
     * @return A ZonedDateTime [time zone offset].
     */
    public ZonedDateTime getZonedDateTimeAt(int index) {
        if (!isInitialized()) {
            throw new IllegalStateException(Bundle.ERR_TemporalDomainNotInitialized());
        }
        try {
            if (index < 0 || index >= timeDomainSet.getLength()) {
                throw new IllegalArgumentException("Invalid index: " + index);
            }
            double time = timeDomainSet.getDoubles(false)[0][index];
            Instant instant = Instant.ofEpochSecond((long) time);
            return ZonedDateTime.ofInstant(instant, ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds(timeZoneOffset)));
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Gets the VisAD DateTime at the specified index.
     * @param index The array index; must be less the domain set's length (e.g., numHours).
     * @return A VisAD DateTime [UTC].
     */
    public DateTime getDateTimeAt(int index) {
        if (!isInitialized()) {
            throw new IllegalStateException(Bundle.ERR_TemporalDomainNotInitialized());
        }
        try {
            if (index < 0 || index >= timeDomainSet.getLength()) {
                throw new IllegalArgumentException("Invalid index: " + index);
            }
            double time = timeDomainSet.getDoubles(false)[0][index];
            return Times.fromDouble(time);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Creates a VisAD function represented by a FieldImpl from the temporal domain using the
     * supplied MathType for the range. The caller must set the range samples.
     * @param range The MathType to use as the function's range.
     * @return A FieldImpl with FunctionType (time) -> (range).
     */
    public FieldImpl createTemporalField(MathType range) {
        if (!isInitialized()) {
            throw new IllegalStateException(Bundle.ERR_TemporalDomainNotInitialized());
        }
        try {
            FunctionType functionType = new FunctionType(TIME_DOMAIN_TYPE, range);
            FieldImpl field = new FieldImpl(functionType, timeDomainSet);
            logger.log(Level.FINE, "createTemporalField created: {0}", field.getType().prettyString());
            return field;
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a simple VisAD function represented by a FlatField from the temporal domain using the
     * supplied MathType for the range. The caller must set the range samples.
     * @param range The MathType to use as the function's range.
     * @return A FieldFlat with FunctionType (time) -> (range).
     */
    public FlatField createSimpleTemporalField(MathType range) {
        if (!isInitialized()) {
            throw new IllegalStateException(Bundle.ERR_TemporalDomainNotInitialized());
        }
        try {
            FunctionType functionType = new FunctionType(TIME_DOMAIN_TYPE, range);
            FlatField field = new FlatField(functionType, timeDomainSet);
            logger.log(Level.FINE, "createSimpleTemporalField created: {0}", field.getType().prettyString());
            return field;
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public Set getDomainSet() {
        return timeDomainSet;
    }

    public RealType getDomainType() {
        return TIME_DOMAIN_TYPE;
    }

    @Override
    public String toString() {
        if (timeDomainSet == null) {
            throw new IllegalStateException(Bundle.ERR_TemporalDomainNotInitialized());
        }
        StringBuilder sb = new StringBuilder("TemporalDomain: ");
        sb.append(timeDomainSet.getType().prettyString());
        try {
            sb.append("\n  Length: ");
            sb.append(timeDomainSet.getLength());
            double[][] times = timeDomainSet.getDoubles();
            for (int i = 0; i < times[0].length; i++) {
                DateTime dt = Times.fromDouble(times[0][i]);
                sb.append("\n    ");
                sb.append(dt.timeString());
            }
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return sb.toString();
    }

}
