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
package com.emxsys.time.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * A simple time frame with a derived duration.
 */
public class BasicTimeFrame implements TimeFrame {

    private ZonedDateTime begin;
    private ZonedDateTime end;

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public BasicTimeFrame(ZonedDateTime begin, ZonedDateTime end) {
        this.begin = begin;
        this.end = end;
    }

    /**
     * @return The beginning of the time frame.
     */
    @Override
    public ZonedDateTime getBegin() {
        return this.begin;
    }

    /**
     * Sets the beginning.
     * @param newBegin The beginning of the time frame.
     */
    public void setBegin(ZonedDateTime newBegin) {
        ZonedDateTime oldBegin = this.begin;
        this.begin = newBegin;
        pcs.firePropertyChange(PROP_TIMEFRAME_BEGIN, oldBegin, newBegin);
    }

    /**
     * @return The end of the time frame.
     */
    @Override
    public ZonedDateTime getEnd() {
        return this.end;
    }

    /**
     * Sets the end.
     * @param newEnd The end of the time frame.
     */
    public void setEnd(ZonedDateTime newEnd) {
        ZonedDateTime oldEnd = this.end;
        this.end = newEnd;
        pcs.firePropertyChange(PROP_TIMEFRAME_END, oldEnd, newEnd);
    }

    /**
     * Gets the computed duration of the timeframe.
     * @return The length of the time frame.
     */
    @Override
    public Duration getDuration() {
        return Duration.between(begin, end);
    }

    /**
     * Add a listener for PROP_TIMEFRAME_BEGIN and PROP_TIMEFRAME_END events.
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a listener.
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public boolean contains(ZonedDateTime time) {
        return ((time.isAfter(getBegin()) || time.isEqual(getBegin()))
                && (time.isBefore(getEnd()) || time.isEqual(getEnd())));

    }

}
