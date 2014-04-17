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
package com.emxsys.wmt.weather.api;

import javax.swing.ImageIcon;
import org.openide.util.Lookup;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public interface WeatherProvider extends Lookup.Provider {

    /**
     * Gets an icon representative of this provider, e.g., Yahoo, NWS, WeatherUnderground.
     * @return An ImageIcon; may be null
     */
    ImageIcon getImageIcon();

//    /**
//     * Gets a point forecast for the given position.
//     * @param coord The position for the forecast
//     * @return A VisAD field in the form ( Time -> ( Weather ) )
//     */
//    public Field getPointForecast(Coord2D coord);
//    
//    /**
//     * Gets a point forecast HTML presentation for the given position.
//     * @param coord The position for the forecast.
//     * @return An HTML web page containing the forecast information.
//     */
//    public String getPointForecastPage(Coord2D coord);
//    
//
//    Weather getWeather(Date utcTime, Coord2D coord);

//    void addForecast();
//    void addObservation();
//    
//    FlatField generateTemperatures(Gridded1DSet timeDomain);
//
//    FlatField generateHumidities(Gridded1DSet timeDomain);
//
//    FlatField generateWinds(Gridded1DSet timeDomain);
}
