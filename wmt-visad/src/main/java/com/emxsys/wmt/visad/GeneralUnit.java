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
package com.emxsys.wmt.visad;

import visad.Unit;
import static com.emxsys.wmt.visad.Units.*;


/**
 * General units of measure not defined in visad.CommunUnit.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: GeneralUnit.java 534 2013-04-18 15:26:05Z bdschubert $
 * @see visad.CommonUnit
 * @see FireUnit
 * @see Units
 */
public class GeneralUnit
{
    /**
     * Common unit for fuel bed depth, flame length - US
     */
    public static final Unit foot = getUnit("ft");
    /**
     * Common unit for rainfall, inches - US
     */
    public static final Unit inch = getUnit("inch");
    /**
     * Common unit for humidity and moisture of extinction of dead fuel
     */
    public static final Unit percent = getUnit("percent");
    /**
     * Common unit for temperature in degrees Celsius
     */
    public static final Unit degC = getUnit("degC");
    /**
     * Common unit for temperature in degrees Fahrenheit
     */
    public static final Unit degF = getUnit("degF");
    /**
     * Common unit for speed in miles per hour
     */
    public static final Unit mph = getUnit("mile/(hour)");
    /**
     * Common unit for speed in kilometers per hour
     */
    public static final Unit kph = getUnit("kilometer/(hour)");
    /**
     * Common unit for speed in knots
     */
    public static final Unit knot = getUnit("kt");
    /**
     * Common unit for time in hours
     */
    public static final Unit hour = getUnit("h");
    /**
     * Common unit for time in hours
     */
    public static final Unit day = getUnit("d");
    /**
     * Common unit for area in hectares
     */
    public static final Unit hectare = getUnit("hectare");
    /**
     * Common unit for area in acres
     */
    public static final Unit acre = getUnit("acre");
}
