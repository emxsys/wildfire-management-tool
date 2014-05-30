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
import visad.DataImpl;
import visad.DateTime;
import visad.Field;
import visad.FieldImpl;
import visad.FunctionType;
import visad.Gridded1DDoubleSet;
import visad.MathType;
import visad.RealType;
import visad.SetType;
import visad.VisADException;

/**
 * A Temporal domain defined by a time set.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class TemporalDomain {

    private static final RealType TEMPORAL_DOMAIN_TYPE = RealType.Time;
    private final Gridded1DDoubleSet temporalDomainSet;
    private int timeZoneOffset = 0;
    private static final Logger LOG = Logger.getLogger(TemporalDomain.class.getName());

    /**
     * Constructs a TemporalDomain beginning at the given start time, for the specified number of
     * hours. The time zone offset is determined the start time ZoneId.
     *
     * @param start The start time for the domain
     * @param numHours The number of hours in the domain.
     */
    public TemporalDomain(ZonedDateTime start, int numHours) {
        this(Times.makeHourlyTimeSet(start, numHours), start.getOffset().getTotalSeconds());
    }

    /**
     * Constructs a TemporalDomain from a Gridded1DDoubleSet of type RealType.Time
     *
     * @param timeDomainSet The time domain in UTC values.
     * @param offsetSeconds The time zone offset (from UTC) in seconds.
     */
    public TemporalDomain(Gridded1DDoubleSet timeDomainSet, int offsetSeconds) {
        this.temporalDomainSet = timeDomainSet;
        this.timeZoneOffset = offsetSeconds;
    }

    /**
     * Constructor extracts temporal domains from a FieldImpl data type.
     *
     * @param data of type FieldImpl
     */
    public TemporalDomain(DataImpl data) {
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
            this.temporalDomainSet = (Gridded1DDoubleSet) field.getDomainSet();

        }
        catch (IllegalArgumentException | VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalArgumentException(ex.toString());
        }
    }

    public ZonedDateTime getStart() {
        Instant instant = Instant.ofEpochSecond((long) this.temporalDomainSet.getDoubleLowX());
        return ZonedDateTime.ofInstant(instant, ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds(timeZoneOffset)));
    }

    public ZonedDateTime getZonedDateTimeAt(int index) {
        try {
            if (index < 0 || index >= this.temporalDomainSet.getLength()) {
                throw new IllegalArgumentException("Invalid index: " + index);
            }
            double time = this.temporalDomainSet.getDoubles(false)[0][index];
            Instant instant = Instant.ofEpochSecond((long) time);
            return ZonedDateTime.ofInstant(instant, ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds(timeZoneOffset)));
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public DateTime getDateTimeAt(int index) {
        try {
            if (index < 0 || index >= this.temporalDomainSet.getLength()) {
                throw new IllegalArgumentException("Invalid index: " + index);
            }
            double time = this.temporalDomainSet.getDoubles(false)[0][index];
            return Times.fromDouble(time);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public FieldImpl newTemporalField(MathType range) {
        try {
            FunctionType functionType = new FunctionType(TEMPORAL_DOMAIN_TYPE, range);
            FieldImpl field = new FieldImpl(functionType, this.temporalDomainSet);
            LOG.log(Level.CONFIG, "newTemporalField created: {0}", field.getType().prettyString());
            return field;
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    public Gridded1DDoubleSet getTemporalDomainSet() {
        return temporalDomainSet;
    }

    public RealType getTemporalDomainType() {
        return TEMPORAL_DOMAIN_TYPE;
    }

    @Override
    public String toString() {
        return this.temporalDomainSet.toString();
    }

}
