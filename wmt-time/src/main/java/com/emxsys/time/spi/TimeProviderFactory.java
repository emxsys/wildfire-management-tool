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
package com.emxsys.time.spi;

import com.emxsys.time.api.TimeEvent;
import com.emxsys.time.api.TimeListener;
import com.emxsys.time.api.TimeProvider;
import com.terramenta.time.DatetimeChangeListener;
import com.terramenta.time.DatetimeProvider;
import com.terramenta.time.options.TimeOptions;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.openide.util.Lookup;

/**
 * TimeProviderFactory provides the central time for the application as provided by Terramenta. The
 * default instance can be overridden by registering a TimeProvider service provider on the global
 * lookup.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class TimeProviderFactory {

    private static final Logger logger = Logger.getLogger(TimeProviderFactory.class.getName());
    private static TimeProvider instance = null;

    /**
     * Gets a TimeProvider instance, either from the global lookup or a default implementation.
     *
     * @return A TimeProvider service provider found on the global lookup, or, if not found, a
     * DefaultTimeProvider instance.
     */
    public static TimeProvider getInstance() {
        if (instance == null) {
            // Check the general Lookup for a registered service provider
            instance = Lookup.getDefault().lookup(TimeProvider.class);

            // Use our default instance if no registered provider.
            if (instance == null) {
                instance = new DefaultTimeProvider();
            }
        }
        return instance;
    }

    private TimeProviderFactory() {
    }

    /**
     * The default implementation of a TimeProvider.
     */
    static class DefaultTimeProvider implements TimeProvider, DatetimeChangeListener {

        private final EventListenerList listenerList = new EventListenerList();
        private DatetimeProvider datetimeProvider;
        private ZonedDateTime curTime;

        private DefaultTimeProvider() {
            logger.config("Constructed TimeProvider");
            getDatetimeProvider();
        }

        private DatetimeProvider getDatetimeProvider() {
            if (datetimeProvider == null) {
                datetimeProvider = Lookup.getDefault().lookup(DatetimeProvider.class);

                if (datetimeProvider == null) {
                    throw new IllegalStateException("getDatetimeProvider() unable to locate a DatetimeProvider.");
                }
                datetimeProvider.addChangeListener(this);
                curTime = instantToZonedDateTime(datetimeProvider.getDatetime());
            }
            return datetimeProvider;
        }

        @Override
        public ZonedDateTime getTime() {
            return curTime;
        }

        @Override
        public void setTime(ZonedDateTime utcTime) {
            if (utcTime == null) {
                throw new IllegalArgumentException("setTime() utcTime arg cannot be null.");
            }
            // Update Terramenta
            getDatetimeProvider().setDatetime(utcTime.toInstant());
        }

        @Override
        public void addTimeListener(TimeListener listener) {
            listenerList.add(TimeListener.class, listener);
        }

        @Override
        public void removeTimeListener(TimeListener listener) {
            listenerList.remove(TimeListener.class, listener);
        }

        /**
         * Notifies TimeListeners upon DateProvider updates.
         * @param dateProvider Ignored.
         * @param date New date.
         */
        @Override
        public void onDatetimeChange(Instant oldDatetime, Instant newDatetime) {
            ZonedDateTime oldTime = curTime;
            curTime = instantToZonedDateTime(newDatetime);
            logger.log(Level.FINEST, "update: {0}", curTime.toString());
            TimeEvent timeEvent = new TimeEvent(this, oldTime, curTime);
            for (TimeListener listener : listenerList.getListeners(TimeListener.class)) {
                listener.updateTime(timeEvent);
            }
        }
        
        static ZonedDateTime instantToZonedDateTime(Instant datetime) {
            TimeZone timeZone = TimeZone.getTimeZone(TimeOptions.DEFAULT_TIMEZONE);
            return ZonedDateTime.ofInstant(datetime, timeZone.toZoneId());
        }

    }
}
