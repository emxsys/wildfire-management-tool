/*
 * Copyright (c) 2009-2014, Bruce Schubert. <bruce@emxsys.com>
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
 * - Neither the name of the Emxsys company nor the names of its 
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
package com.emxsys.wmt.wildfire.behave;

import static java.lang.Math.*;

/**
 * Experimental version of Behave created to explore CPS sunlit fuel coefficients.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: BehaveExp.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public class BehaveExp extends Behave {

    /** Input candidate: solar angle, noon = 180 [deg] */
    public double solarAngle = 0.;
    /** Input candidate: fuel temp delta from air temp [C] */
    public double fuelTempDelta = 0.;

    double sol_r;
    double sdr_r;
    double phi_p;

    public BehaveExp() {
        super();
    }

    @Override
    protected void calcWindAndSlopeFactor() {
        // compute sdr, efw and phi_t
        super.calcWindAndSlopeFactor();

        phi_p = 0;
        preheatFactor();

        // adjust phi_t based on solar radiation angles, i.e. sunlit vers shaded side
        phi_t += phi_p;

    }

    /**
     * Calculate the solar preheating factor: phi_p <br>
     *
     */
    protected void preheatFactor() {
        sol_r = toRadians(solarAngle);
        sdr_r = toRadians(sdr);

        double deltaRad = abs(sol_r - sdr_r);
        double cos_deltaRad = cos(deltaRad);
        double sunlitFuel = 0;
        if (deltaRad < PI) {
            sunlitFuel = (1 + cos_deltaRad) / 2;
        } else {
            sunlitFuel = (1 - cos_deltaRad) / 2;
        }
        // arbitrary value based on chemical reaction increase for every 10 deg C.
        double preheat = sunlitFuel * (fuelTempDelta / 10);

        phi_p = pow(beta, -0.3) * preheat;
    }

}
