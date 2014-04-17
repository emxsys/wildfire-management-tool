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
package com.emxsys.wmt.weather.spi;

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.weather.api.AbstractWeatherProvider;
import com.emxsys.wmt.weather.api.Weather;
import com.emxsys.wmt.weather.api.WeatherProvider;
import com.emxsys.wmt.weather.api.WeatherTuple;
import java.util.Date;
import javax.swing.ImageIcon;
import org.openide.util.Lookup;
import visad.Field;
import visad.FlatField;
import visad.Gridded1DSet;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class DefaultWeatherProvider extends AbstractWeatherProvider {

    private static WeatherProvider instance = null;

    /**
     * Returns the singleton instance of a WeatherProvider. If a class has been registered as a
     * WeatherProvider service provider, then an instance of that class will be returned. Otherwise,
     * an instance of the DefaultWeatherProvider will be returned.
     *
     * @return A singleton instance of a WeatherProvider.
     */
    public static WeatherProvider getInstance() {
        if (instance == null) {
            // Check the general Lookup for a service provider
            instance = Lookup.getDefault().lookup(WeatherProvider.class);

            // Use our default factory if no registered provider.
            if (instance == null) {
                instance = new DefaultWeatherProvider();
            }
        }
        return instance;
    }

//    @Override
//    public FlatField generateTemperatures(Gridded1DSet timeDomain) {
//        throw new UnsupportedOperationException("generateTemperatures");
//    }
//
//    @Override
//    public FlatField generateHumidities(Gridded1DSet timeDomain) {
//        throw new UnsupportedOperationException("generateHumidities");
//    }
//
//    @Override
//    public FlatField generateWinds(Gridded1DSet timeDomain) {
//        throw new UnsupportedOperationException("generateWinds");
//    }
//
    public Weather getWeather(Date utcTime, Coord2D coord) {
        // TODO: lookup the weather, if not found use general weather
        return new WeatherTuple();
    }

//    @Override
//    public void addForecast() {
//        throw new UnsupportedOperationException("addForecast");
//    }
//
//    @Override
//    public void addObservation() {
//        throw new UnsupportedOperationException("addObservation");
//    }

    @Override
    public ImageIcon getImageIcon() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
