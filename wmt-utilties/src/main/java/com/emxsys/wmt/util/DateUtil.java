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
package com.emxsys.wmt.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class DateUtil
{
    private DateUtil()
    {
    }


    static public String getCurrentDateTimeString()
    {
        return DateFormat.getInstance().format(Calendar.getInstance().getTime());
    }


    /**
     * Rounds the given Date field and all smaller fields.
     *
     * @param date to be rounded
     * @param field used to for rounding
     * @return
     */
    static public Date round(Date date, int field)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        switch (field)
        {
            case Calendar.SECOND:
                // Set the calendar to the top of the second
                if (c.get(Calendar.MILLISECOND) >= 500)
                {
                    c.add(Calendar.SECOND, 1);
                }
                c.set(Calendar.MILLISECOND, 0);
                break;
            case Calendar.MINUTE:
                // Set the calendar to the top of the minute
                if (c.get(Calendar.SECOND) >= 30)
                {
                    c.add(Calendar.MINUTE, 1);
                }
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                break;
            case Calendar.HOUR_OF_DAY:
                // Set the calendar to the top of the hour
                if (c.get(Calendar.MINUTE) >= 30)
                {
                    c.add(Calendar.HOUR_OF_DAY, 1);
                }
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                break;
            case Calendar.DATE:
                // Set the calendar to midnight
                if (c.get(Calendar.HOUR_OF_DAY) >= 12)
                {
                    c.add(Calendar.DATE, 1);
                }
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                break;
            default:
                throw new UnsupportedOperationException("round() not yet supported for field: " + field);
        }
        return c.getTime();
    }
}
