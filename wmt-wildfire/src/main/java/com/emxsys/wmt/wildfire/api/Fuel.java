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
package com.emxsys.wmt.wildfire.api;

import com.emxsys.wmt.gis.api.Terrain;
import com.emxsys.wmt.solar.api.Sunlight;
import java.util.List;
import visad.Real;

/**
 * The Fuel interface defines the characteristics of wildland fuels used in
 * fire behavior predictions.
 *
 * @author Bruce Schubert
 * @version $Id: Fuel.java 669 2013-05-24 19:56:22Z bdschubert $
 */
public interface Fuel {

    /**
     *
     * @return the fuel model representing the fuel
     */
    FuelModel getFuelModel();

    /**
     *
     * @return the temporal and environmental conditions of the fuel
     */
    List<FuelCondition> getConditions();


    /**
     * Adjust fuel moisture and fuel temperatures.
     * @param solar
     * @param airTemps
     * @param humidities
     * @param windSpd
     * @param windDir
     * @param terrain
     */
    void adjustFuelConditions(Sunlight solar, List<Real> airTemps, List <Real>humidities,
            Real windSpd, Real windDir, Terrain terrain, FuelMoisture fuelMoisture);
}