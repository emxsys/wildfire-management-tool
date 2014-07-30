/*
 * Copyright (c) 2010-2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wildfire.api;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.Terrain;
import com.emxsys.solar.api.SolarModel;
import com.emxsys.weather.api.WeatherModelObsolete;
import java.time.ZonedDateTime;

/**
 * The Fuel interface defines the characteristics of wildland fuels used in fire behavior
 * predictions.
 *
 * @author Bruce Schubert
 */
public interface Fuel {

    /**
     * @return the fuel model representing the fuel
     */
    FuelModel getFuelModel();

    /**
     * @param time
     * @return the temporal and environmental conditions of the fuel
     */
    FuelCondition getFuelCondition(ZonedDateTime time);

    /**
     * Conditions the fuel moisture based on the solar and weather.
     * @param time The date/time used to determine the solar and weather conditions.
     * @param coord The coordinated used to determine the solar and weather conditions.
     * @param solar A spatio-temporal model containing SolarTypes.
     * @param weather A spatio-temporal model containing the WeatherTypes.
     * @param terrain The aspect and slope where the fuel is located.
     * @param fuelMoisture The initial fuel moisture.
     */
    void condition(ZonedDateTime time, Coord3D coord, SolarModel solar, WeatherModelObsolete weather, Terrain terrain, FuelMoisture fuelMoisture);
}
