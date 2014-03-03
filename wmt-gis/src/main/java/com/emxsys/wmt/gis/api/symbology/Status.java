/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.gis.api.symbology;


/**
 * Status/Operational Condition
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: Status.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public enum Status
{

    PRESENT_EXISTING("P", "PRESENT OR EXISTING"),
    ANTICIPATED_PLANNED("A", "ANTICIPATED OR PLANNED"),
    PRESENT_AND_FULLY_CAPABLE("C", "PRESENT/FULLY CAPABLE"),
    PRESENT_BUT_DAMAGED("D", "PRESENT/DAMAGED"),
    PRESENT_BUT_DESTROYED("X", "PRESENT/DESTROYED"),
    PRESENT_BUT_FULL_TO_CAPACITY("F", "PRESENT/FULL TO CAPACITY");
    public final String code;
    public final String description;


    private Status(String code, String description)
    {
        this.code = code;
        this.description = description;
    }


    public String code()
    {
        return this.code;
    }


    public String description()
    {
        return this.description;
    }


    public static Status get(String code)
    {
        String properCode = code.toUpperCase();
        for (Status si : Status.values())
        {
            if (si.code.equals(properCode))
            {
                return si;
            }
        }
        throw new IllegalArgumentException(code + " is not a valid Battle Dimension code.");
    }


    @Override
    public String toString()
    {
        return code + ", " + description;
    }
}
