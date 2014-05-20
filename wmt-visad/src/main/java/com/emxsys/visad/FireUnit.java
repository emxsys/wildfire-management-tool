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
package com.emxsys.visad;

import visad.Unit;
import static com.emxsys.visad.Units.*;

/**
 * Units of measure for fire behavior.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: FireUnit.java 534 2013-04-18 15:26:05Z bdschubert $
 * @see visad.CommonUnit
 * @see GeneralUnit
 * @see Units
 */
public class FireUnit {

    /** Common unit for fuel load - SI */
    public static final Unit kg_m2 = getUnit("kg/(m2)");
    /** Common unit for fuel load - US */
    public static final Unit tons_acre = getUnit("ton/(acre)");
    /** Common unit or surface-area-to-volume - SI */
    public static final Unit m2_m3 = getUnit("m2/(m3)");
    /** Common unit for surface-area-to-volume - US */
    public static final Unit ft2_ft3 = getUnit("ft2/(ft3)");
    /** Common unit for heat of combustion (low heat output) - SI */
    public static final Unit kJ_kg = getUnit("kJ/(kg)");
    /** Common unit for heat of combustion (low heat output) - US */
    public static final Unit Btu_lb = getUnit("Btu/(lb)");
    /** Common unit for Byram's fire line intensity - SI */
    public static final Unit kW_m = getUnit("kW.m-1");
    /** Common unit for Byram's fire line intensity - US */
    public static final Unit Btu_ft_s = getUnit("Btu.ft-1.s-1");
    /** Common unit for heat release per unit area - SI */
    public static final Unit kJ_m2 = getUnit("kJ/(m2)");
    /** Common unit for heat release per unit area - US */
    public static final Unit Btu_ft2 = getUnit("Btu/(ft2)");
    /** Common unit for rate of spread - US */
    public static final Unit chain_hour = getUnit("chain/(hour)");
    /** Common unit for rate of spread - SI */
    public static final Unit meter_minute = getUnit("meter/(minute)");
}
