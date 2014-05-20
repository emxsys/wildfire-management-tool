/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com> 
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
package com.emxsys.wmt.cps.fireground;

import com.emxsys.wildfire.behave.BehaveUtil;
import com.emxsys.visad.GeneralUnit;
import static com.emxsys.wildfire.api.WildfireType.*;

import org.openide.util.Exceptions;

import visad.Real;
import visad.VisADException;


/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: FuelMoistureUtil.java 315 2012-10-20 13:42:54Z bdschubert $
 */
public class FuelMoistureUtil
{

    /**
     * 
     * @param m_12  initial fuel moisture at noon [percent]
     * @param T_f   air temp immediately adjacent to fuel [fahrenheit]
     * @param H_f   relative humidity immediately adjacent to fuel [percent]
     * @param W     20 foot wind speed [mph]
     * @param R     rainfall amount [inches]
     * @return  1h fine fuel moisture percent from derived from computed FFMC code
     */
    static public Real calcCanadianStandardDailyFineFuelMoisture(
            Real m_12, Real T_f, Real H_f, Real W, Real R)
    {
        try
        {
            // Use US values
            double m = BehaveUtil.calcCanadianStandardDailyFineFuelMoisture(
                    m_12.getValue(GeneralUnit.percent),
                    T_f.getValue(GeneralUnit.degF),
                    H_f.getValue(GeneralUnit.percent),
                    W.getValue(GeneralUnit.mph),
                    R.getValue());

            return new Real(FUEL_MOISTURE_1H, m);
        }
        catch (VisADException ex)
        {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }



    /**
     * 
     * @param m_0   previous hour's fuel moisture [percent]
     * @param H_f   relative humidity adjacent to the fuel [percent]
     * @param T_f   air temperature adjacent to the fuel [celcius]
     * @param W     20' windspeed [kph]
     * @return 1h fine fuel moisture [percent]     
     */
    static public Real calcCanadianHourlyFineFuelMoisture(
            Real m_0, Real T_f, Real H_f, Real W)
    {
        try
        {
            // Use metric values
            double m = BehaveUtil.calcCanadianHourlyFineFuelMoisture(
                    m_0.getValue(GeneralUnit.percent),
                    H_f.getValue(GeneralUnit.percent),
                    T_f.getValue(GeneralUnit.degC),
                    W.getValue(GeneralUnit.kph));

            return new Real(FUEL_MOISTURE_1H, m);
        }
        catch (VisADException ex)
        {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }
}
