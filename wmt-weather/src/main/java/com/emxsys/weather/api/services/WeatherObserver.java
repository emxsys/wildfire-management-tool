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
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
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
package com.emxsys.weather.api.services;

import com.emxsys.weather.api.WeatherService;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.weather.api.WeatherModel;
import java.time.Duration;

/**
 * An interface for gathering the current and/or historical weather conditions within a geographical
 * area.
 *
 * @author Bruce Schubert
 */
public interface WeatherObserver extends WeatherService {

    /**
     * Gets the latest weather observations (i.e., current conditions) within the age and inside the
     * area of interest.
     *
     * @param areaOfInterest The geographical area to be examined for weather reporting stations.
     * @param age The permissible age of an observation.
     * @return A {@code WeatherModel} containing the weather observations.
     */
    WeatherModel getLatestObservations(SpatialDomain areaOfInterest, Duration age);

    /**
     * Gets the weather observations within the given time frame and inside the area of interest.
     *
     * @param areaOfInterest The geographical area to be examined for weather reporting stations.
     * @param timeframe The time range for the observations.
     * @return A {@code WeatherModel} containing the weather observations.
     */
    WeatherModel getObservations(SpatialDomain areaOfInterest, TemporalDomain timeframe);

}
