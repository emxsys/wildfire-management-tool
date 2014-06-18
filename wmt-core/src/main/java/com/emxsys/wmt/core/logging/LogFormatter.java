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
 *     - Neither the name of Bruce Schubert,  nor the names of its 
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
package com.emxsys.wmt.core.logging;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import org.netbeans.core.startup.logging.NbFormatter; // Private Package Referenced: See POM notes.

/**
 *
 * @author Bruce Schubert
 */
public class LogFormatter extends Formatter {

    private boolean includeSource = true;
    private boolean includeTimestamp = true;

    @Override
    public String format(LogRecord record) {
        String logMsg = NbFormatter.FORMATTER.format(record);
        StringBuilder sb = new StringBuilder();

        // Prepends a timestope
        if (includeTimestamp) {
            Instant instant = Instant.ofEpochMilli(record.getMillis());
            ZonedDateTime timestamp = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            sb.append(timestamp.toLocalTime().toString());
            sb.append(' ');
        }

        // Replace "[logger]:" with "[logger caller]:"
        if (includeSource) {
            // Locate the brackets surrounding the logger name
            int beg = logMsg.indexOf('[');
            int end = logMsg.indexOf("]:");
            if (beg >= 0 && end > beg) {
                String source;
                if (record.getSourceClassName() != null) {
                    source = record.getSourceClassName();
                    if (record.getSourceMethodName() != null) {
                        source += " " + record.getSourceMethodName();
                    }
                }
                else {
                    source = record.getLoggerName();
                }
                // Replace text between the brackets
                sb.append(logMsg.substring(0, beg + 1));
                sb.append(source);
                sb.append(logMsg.substring(end));
            }
            else {
                sb.append(logMsg);
            }
        }
        else {
            sb.append(logMsg);
        }
        return sb.toString();
    }

    public void includeSource(boolean includeSource) {
        this.includeSource = includeSource;
    }

    public void includeTimeStamp(boolean includeTimeStamp) {
        this.includeTimestamp = includeTimeStamp;
    }

}
