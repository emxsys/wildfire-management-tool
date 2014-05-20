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
package com.emxsys.weather.spi;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.weather.api.AbstractWeatherProvider;
import com.emxsys.weather.api.ConditionsObserver;
import com.emxsys.weather.api.PointForecaster;
import com.emxsys.weather.api.Weather;
import com.emxsys.weather.api.WeatherProvider;
import com.emxsys.weather.api.WeatherTuple;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.openide.util.Lookup;

/**
 * The DefaultWeatherProvider supplies a list of registered WeatherProvider service providers.
 *
 * @author Bruce Schubert
 */
public class DefaultWeatherProvider {

    private static ArrayList<WeatherProvider> instances;
    private static final Logger logger = Logger.getLogger(DefaultWeatherProvider.class.getName());

    /**
     * Gets the registered WeatherProviders service providers from the global lookup (if any) plus a
     * SingleWeatherProvider for each of the StdWeatherParams13 and StdWeatherParams40.
     *
     * @return A collection of WeatherProvider instances.
     */
    public static List<WeatherProvider> getInstances() {
        if (instances == null) {

            // Get all the registered service provider instances
            Collection<? extends WeatherProvider> serviceProviders = Lookup.getDefault().lookupAll(WeatherProvider.class);
            instances = new ArrayList<>(serviceProviders);
            serviceProviders.stream().forEach((serviceProvider) -> {
                logger.log(Level.CONFIG, "Providing a {0} instance.", serviceProvider.getClass().getName());
            });
        }
        return instances;
    }

    /**
     * Gets the WeatherProvider instances that contain the given coordinate.
     *
     * @return A collection of WeatherProvider instances that have the PointForecaster capability.
     */
    public static List<WeatherProvider> getPointForecasters() {
        ArrayList<WeatherProvider> providers = new ArrayList<>();

        getInstances().stream()
                .filter((provider) -> (provider.getLookup().lookup(PointForecaster.class)) != null)
                .forEach((provider) -> {
                    providers.add(provider);
                });
        return providers;
    }
    /**
     * Gets the WeatherProvider instances that contain the given coordinate.
     *
     * @return A collection of WeatherProvider instances that have the ConditionsObserver capability.
     */
    public static List<WeatherProvider> getStationObservers() {
        ArrayList<WeatherProvider> providers = new ArrayList<>();

        getInstances().stream()
                .filter((provider) -> (provider.getLookup().lookup(ConditionsObserver.class)) != null)
                .forEach((provider) -> {
                    providers.add(provider);
                });
        return providers;
    }

    /**
     * Gets the WeatherProvider instances that intersect the given extents.
     *
     * @param extents The box that will be tested for intersection with the provider's extents.
     * @return A collection of WeatherProvider instances that are valid for the box.
     */
    public static List<WeatherProvider> getInstances(Box extents) {
        return getInstances();
//        ArrayList<WeatherProvider> providers = new ArrayList<>();
//        
//        getInstances().stream()
//                .filter((provider) -> (provider.getExtents().intersects(extents)))
//                .forEach((provider) -> {
//                    providers.add(provider);
//                });
//        return providers;        
    }

    /**
     *
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    public class DummyWeatherProvider extends AbstractWeatherProvider {

//
        public Weather getWeather(Date utcTime, Coord2D coord) {
            // TODO: lookup the weather, if not found use general weather
            return new WeatherTuple();
        }

        @Override
        public ImageIcon getImageIcon() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
