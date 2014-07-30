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
package com.emxsys.wmt.weather.nws;

import com.emxsys.weather.api.AbstractWeatherProvider;
import com.emxsys.weather.api.WeatherProvider;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

/**
 * National Weather Service NDFD Point Forecast weather service.
 *
 * @author Bruce Schubert
 */
@ServiceProvider(service = WeatherProvider.class)
public class NwsWeatherProvider extends AbstractWeatherProvider {

    public static final String IMAGE_ICON_NAME = "nws_logo.png";
    /** Singleton */
    private static NwsWeatherProvider instance;
    private static final Logger logger = Logger.getLogger(NwsWeatherProvider.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    /**
     * Get the singleton instance.
     * @return the singleton NwsWeatherProvider found on the global lookup.
     */
    static public NwsWeatherProvider getInstance() {
        if (instance == null) {
            // Calls default constructor
            Lookup.Result<WeatherProvider> result = Lookup.getDefault().lookupResult(WeatherProvider.class);
            for (Lookup.Item<WeatherProvider> item : result.allItems()) {
                if (item.getType().equals(NwsWeatherProvider.class)) {
                    return (NwsWeatherProvider) item.getInstance();
                }
            }
        }
        return instance;
    }

    /**
     * Do not call! Used by @ServiceProvider.
     *
     * Use getInstance() or Lookup.getDefault().lookup(MesoWestWeatherProvider.class) instead.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public NwsWeatherProvider() {
        // Catch the second invocation, which should indicate incorrect usage.
        if (instance != null) {
            throw new IllegalStateException("Do not call constructor. Use getInstance() or Lookup.");
        }
        instance = this;

        // Initialize the lookup with this provider's capabilities
        InstanceContent content = getContent();
        content.add(new NwsForecastService());
        content.add(new NwsPointForecastPresenter());
    }

    @Override
    public String getName() {
        return "NWS Weather";
    }

    @Override
    public ImageIcon getImageIcon() {
        URL imgURL = getClass().getResource("nws_logo32.png");
        if (imgURL == null) {
            logger.warning("Image icon " + IMAGE_ICON_NAME + " was not found on the classpath.");
            return null;
        }
        ImageIcon imageIcon = new ImageIcon(imgURL);
        imageIcon.setDescription(getClass().getSimpleName());
        return imageIcon;
    }
}
