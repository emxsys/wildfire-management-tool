/*
 * Copyright (c) 2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.time.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A basic implementation of the TimeRegistrar interface using a BasicTimeFrame.
 */
public class BasicTimeRegistrar implements TimeRegistrar {

    private ZonedDateTime currentTime;
    private BasicTimeFrame timeFrame;
    boolean active = false;
    private static final Logger logger = Logger.getLogger(BasicTimeRegistrar.class.getName());

    public BasicTimeRegistrar(ZonedDateTime begin, ZonedDateTime end, ZonedDateTime current) {
        if (begin == null || end == null || current == null) {
            String msg = "Constructor arg(s) cannot be null.";
            logger.severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.timeFrame = new BasicTimeFrame(begin, end);
        if (!timeFrame.contains(current)) {
            throw new IllegalArgumentException("Constructor(...) : time arg (" + current + ") is outside of time frame.");
        }
        this.currentTime = current;

    }

    public BasicTimeRegistrar(ZonedDateTime begin, Duration duration, ZonedDateTime current) {
        this(begin, begin.plus(duration), current);
    }

    @Override
    public ZonedDateTime getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(ZonedDateTime time) {
        if (timeFrame.contains(time)) {
            currentTime = time;
        } else {
            logger.log(Level.WARNING, "setCurrentTime() call ignored: time ({0}) is outside of time frame.", time);
        }
    }

    @Override
    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    @Override
    public void updateTime(TimeEvent evt) {
        if (active) {
            ZonedDateTime time = evt.getNewTime();
            if (timeFrame.contains(time)) {
                currentTime = time;
            } else if (timeFrame.isBefore(time)) {
                // Time has advanced past the end: reset time
            } else if (timeFrame.isAfter(time)) {
                // Time has reversed past the beginning: reset time 

            }
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void activate() {
        active = true;
    }

    @Override
    public void deactivate() {
        active = false;
    }

    @Override
    public String toString() {
        return "BasicTimeRegistrar{" + "currentTime=" + currentTime + ", timeFrame=" + timeFrame + '}';
    }

}
