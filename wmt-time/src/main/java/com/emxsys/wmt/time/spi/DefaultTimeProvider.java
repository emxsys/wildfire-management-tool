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
package com.emxsys.wmt.time.spi;

import com.emxsys.wmt.time.api.TimeEvent;
import com.emxsys.wmt.time.api.TimeListener;
import com.emxsys.wmt.time.api.TimeProvider;
import com.terramenta.time.DateProvider;
import com.terramenta.time.options.TimeOptions;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.event.EventListenerList;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * DefaultTimeProvider provides the central time for the application as provided by Terramenta. The
 * default instance can be overridden by registering a TimeProvider service provider on the global
 * lookup.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class DefaultTimeProvider implements TimeProvider, Observer {

    private static final Logger logger = Logger.getLogger(DefaultTimeProvider.class.getName());
    private static final Preferences prefs = NbPreferences.forModule(TimeOptions.class);
    static TimeProvider instance = null;
    private final EventListenerList listenerList = new EventListenerList();
    private DateProvider dateProvider;
    private ZonedDateTime curTime;
    private final ZoneId UTC_ZONE = ZoneId.of("UTC");

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

    private DefaultTimeProvider() {
        logger.config("Constructed TimeProvider");
        getDateProvider();
    }

    private DateProvider getDateProvider() {
        if (dateProvider == null) {
            dateProvider = Lookup.getDefault().lookup(DateProvider.class);
            if (dateProvider != null) {
                dateProvider.addObserver(this);
            }
        }
        return dateProvider;
    }

    @Override
    public ZonedDateTime getTime() {
        return curTime;
    }

    @Override
    public void setTime(ZonedDateTime utcTime) {
        // Update Terramenta
        getDateProvider().setDate(Date.from(utcTime.toInstant()));
    }

    @Override
    public void addTimeListener(TimeListener listener) {
        listenerList.add(TimeListener.class, listener);
    }

    @Override
    public void removeTimeListener(TimeListener listener) {
        listenerList.remove(TimeListener.class, listener);
    }

    @Override
    public void update(Observable dateProvider, Object date) {
        ZonedDateTime oldTime = curTime;
        TimeZone timeZone = TimeZone.getTimeZone(prefs.get(TimeOptions.TIMEZONE, TimeZone.getDefault().getID()));
        curTime = ZonedDateTime.ofInstant(((Date) date).toInstant(), timeZone.toZoneId());
        logger.log(Level.FINEST, "update: {0}", curTime.toString());
        TimeEvent timeEvent = new TimeEvent(this, oldTime, curTime);
        for (TimeListener listener : listenerList.getListeners(TimeListener.class)) {
            listener.updateTime(timeEvent);
        }
    }

}
