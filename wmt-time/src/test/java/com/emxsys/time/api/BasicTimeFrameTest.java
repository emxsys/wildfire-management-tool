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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Duration;
import java.time.ZonedDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Bruce Schubert
 */
public class BasicTimeFrameTest {

    ZonedDateTime today = ZonedDateTime.now();
    ZonedDateTime tomorrow = today.plusDays(1);
    ZonedDateTime yesterday = today.minusDays(1);

    public BasicTimeFrameTest() {
    }

    @Test
    public void testGetBegin() {
        System.out.println("getBegin");
        BasicTimeFrame instance = new BasicTimeFrame(today, tomorrow);
        ZonedDateTime expResult = today;
        ZonedDateTime result = instance.getBegin();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetBegin() {
        System.out.println("setBegin");
        ZonedDateTime newBegin = yesterday;
        BasicTimeFrame instance = new BasicTimeFrame(today, tomorrow);
        instance.setBegin(newBegin);
        assertEquals(2 * 24 * 60 * 60 * 1000, instance.getDuration().toMillis());
    }

    @Test
    public void testGetEnd() {
        System.out.println("getEnd");
        BasicTimeFrame instance = new BasicTimeFrame(today, tomorrow);
        ZonedDateTime expResult = tomorrow;
        ZonedDateTime result = instance.getEnd();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetEnd() {
        System.out.println("setEnd");
        ZonedDateTime newEnd = tomorrow.plusDays(1);
        BasicTimeFrame instance = new BasicTimeFrame(today, tomorrow);
        instance.setEnd(newEnd);
        assertEquals(2 * 24 * 60 * 60 * 1000, instance.getDuration().toMillis());
    }

    @Test
    public void testGetDuration() {
        System.out.println("getDuration");
        BasicTimeFrame instance = new BasicTimeFrame(today, tomorrow);
        Duration expResult = Duration.ofMillis(24 * 60 * 60 * 1000);
        Duration result = instance.getDuration();
        assertEquals(expResult, result);
    }

    @Test
    public void testAddPropertyChangeListener() {
        System.out.println("addPropertyChangeListener");
        BasicTimeFrame instance = new BasicTimeFrame(today, tomorrow);
        instance.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            switch(evt.getPropertyName()){
                case TimeFrame.PROP_TIMEFRAME_BEGIN:
                    assertEquals(evt.getOldValue(), today);
                    assertEquals(evt.getNewValue(), yesterday);
                    break;                    
                case TimeFrame.PROP_TIMEFRAME_END:
                    assertEquals(evt.getOldValue(), tomorrow);
                    assertEquals(evt.getNewValue(), today);
                    break;
            }
        });
        instance.setBegin(yesterday);
        instance.setEnd(today);
    }

    @Test
    public void testRemovePropertyChangeListener() {
        System.out.println("removePropertyChangeListener");
        BasicTimeFrame instance = new BasicTimeFrame(today, tomorrow);
        PropertyChangeListener listener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        instance.addPropertyChangeListener(listener);
        //instance.setBegin(yesterday);     // correctly throws exception
        
        instance.removePropertyChangeListener(listener);
        instance.setBegin(yesterday);     // should not throw exception
        
    }

}
