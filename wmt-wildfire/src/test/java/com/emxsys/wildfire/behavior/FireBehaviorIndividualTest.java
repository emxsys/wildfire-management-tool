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
package com.emxsys.wildfire.behavior;

import com.emxsys.util.AngleUtil;
import static com.emxsys.util.AngleUtil.normalize360;
import static com.emxsys.weather.api.WeatherType.WIND_DIR;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_MPH;
import com.emxsys.wildfire.api.StdFuelModel;
import static com.emxsys.wildfire.api.StdFuelModelParams13.FBFM04;
import static com.emxsys.wildfire.api.StdFuelMoistureScenario.VeryLowDead_FullyCuredHerb;
import static com.emxsys.wildfire.api.WildfireType.ASPECT;
import static com.emxsys.wildfire.api.WildfireType.SLOPE;
import static org.junit.Assert.*;
import org.junit.Test;
import visad.Real;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class FireBehaviorIndividualTest {

    public FireBehaviorIndividualTest() {
    }

    @Test
    public void testCalcWindAndSlopeEffects() throws VisADException {
        System.out.println("calcWindAndSlopeEffects");
        SurfaceFuel fuelbed = SurfaceFuel.from(StdFuelModel.from(FBFM04), VeryLowDead_FullyCuredHerb.getFuelMoisture());
        // Test Wind Direction 
        int n = 1;
        for (int i = 0; i < 360; i += 30) {
            SurfaceFire fire = new SurfaceFire(fuelbed,
                    new Real(WIND_SPEED_MPH, 5),
                    new Real(WIND_DIR, i),
                    new Real(ASPECT, 0),
                    new Real(SLOPE, 0));
            double expResult = AngleUtil.normalize360(i - 180);
            assertEquals("(" + n++ + ") Wind Dir: " + i, expResult, fire.getDirectionMaxSpread().getValue(), 1);
        }
    }

}
