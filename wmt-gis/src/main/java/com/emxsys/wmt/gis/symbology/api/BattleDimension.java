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
package com.emxsys.wmt.gis.symbology.api;


/**
 * Battle Dimension.
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: BattleDimension.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public enum BattleDimension
{

    GROUND("G", "GROUND"),
    AIR("A", "AIR"),
    SEA_SURFACE("S", "SEA SURFACE"),
    SEA_SUBSURFACE("U", "SEA SUBSURFACE"),
    SPACE("P", "SPACE"),
    SOF("F", "SOF"),
    OTHER("X", "OTHER (No frame)"),
    UNKNOWN("Z", "UNKNOWN");
    public final String code;
    public final String description;


    private BattleDimension(String code, String description)
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


    public static BattleDimension get(String code)
    {
        String properCode = code.toUpperCase();
        for (BattleDimension si : BattleDimension.values())
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
