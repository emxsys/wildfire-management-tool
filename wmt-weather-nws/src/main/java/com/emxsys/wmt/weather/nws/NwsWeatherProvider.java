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

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.util.HttpUtil;
import com.emxsys.wmt.weather.api.Weather;
import com.emxsys.wmt.weather.api.WeatherProvider;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import visad.Field;
import visad.FlatField;

/**
 * National Weather Service NDFD Point Forecast weather service.
 *
 * National Digital Forecast Database XML Web Service
 * <pre>
 * Forecast Element Names
 *
 * NDFD Parameter                           Input Name
 * ==============                           ==========
 * Maximum Temperature                      maxt
 * Minimum Temperature                      mint
 * 3 Hourly Temperature                     temp
 * Dewpoint Temperature                     dew
 * Apparent Temperature                     appt
 * 12 Hour Probability of Precipitation 	pop12
 * Liquid Precipitation Amount              qpf
 * Snowfall Amount                          snow
 * Cloud Cover Amount                       sky
 * Relative Humidity                        rh
 * Maximum Relative Humidity                maxrh
 * Minimum Relative Humidity                minrh
 * Wind Speed                               wspd
 * Wind Direction                           wdir
 * Wind Gust                                wgust
 * Weather                                  wx
 * Weather Icons                            icons
 * Wave Height                              waveh
 * Ice Accumulation                         iceaccum
 * Watches, Warnings, and Advisories        wwa
 * Convective Hazard Outlook                conhazo
 * Fire Weather from Wind and Relative Humidity 	critfireo
 * Fire Weather from Dry Thunderstorms              dryfireo
 * Real-time Mesoscale Analysis Precipitation                   precipa_r
 * Real-time Mesoscale Analysis GOES Effective Cloud Amount 	sky_r
 * Real-time Mesoscale Analysis Dewpoint Temperature            td_r
 * Real-time Mesoscale Analysis Temperature                     temp_r
 * Real-time Mesoscale Analysis Wind Direction                  wdir_r
 * Real-time Mesoscale Analysis Wind Speed                      wspd_r
 * Probability of Tornadoes                                     ptornado
 * Probability of Hail                                          phail
 * Probability of Damaging Thunderstorm Winds                   ptstmwinds
 * Probability of Extreme Tornadoes                             pxtornado
 * Probability of Extreme Hail                                  pxhail
 * Probability of Extreme Thunderstorm Winds                    pxtstmwinds
 * Probability of Severe Thunderstorms                          ptotsvrtstm
 * Probability of Extreme Severe Thunderstorms                  pxtotsvrtstm
 * Probability of 8- To 14-Day Average Temperature Above Normal tmpabv14d
 * Probability of 8- To 14-Day Average Temperature Below Normal tmpblw14d
 * Probability of One-Month Average Temperature Above Normal 	tmpabv30d
 * Probability of One-Month Average Temperature Below Normal 	tmpblw30d
 * Probability of Three-Month Average Temperature Above Normal 	tmpabv90d
 * Probability of Three-Month Average Temperature Below Normal 	tmpblw90d
 * Probability of 8- To 14-Day Total Precipitation Above Median prcpabv14d
 * Probability of 8- To 14-Day Total Precipitation Below Median prcpblw14d
 * Probability of One-Month Total Precipitation Above Median 	prcpabv30d
 * Probability of One-Month Total Precipitation Below Median 	prcpblw30d
 * Probability of Three-Month Total Precipitation Above Median 	prcpabv90d
 * Probability of Three-Month Total Precipitation Below Median 	prcpblw90d
 * Probabilistic Tropical Cyclone Wind Speed >34 Knots (Incremental) 	incw34
 * Probabilistic Tropical Cyclone Wind Speed >50 Knots (Incremental) 	incw50
 * Probabilistic Tropical Cyclone Wind Speed >64 Knots (Incremental) 	incw64
 * Probabilistic Tropical Cyclone Wind Speed >34 Knots (Cumulative) 	cumw34
 * Probabilistic Tropical Cyclone Wind Speed >50 Knots (Cumulative) 	cumw50
 * Probabilistic Tropical Cyclone Wind Speed >64 Knots (Cumulative) 	cumw64
 * </pre>
 * @author Bruce Schubert
 */
@ServiceProvider(service = WeatherProvider.class)
public class NwsWeatherProvider implements WeatherProvider {

    /** URI for MapClick service */
    protected static final String MAP_CLICK_URI = "http://forecast.weather.gov/MapClick.php?";
    /** NDFD URI for Official REST service. */
    protected static final String NDFD_REST_URI = "http://graphical.weather.gov/xml/sample_products/browser_interface/ndfdXMLclient.php?";
    /** NDFD Single Point query */
    private static final String NDFD_POINT_FORECAST
            = "lat=%1$f"
            + "&lon=%2$f"
            + "&product=time-series"
            + "&begin=%3$s"
            + "&end=%4$s"
            + "&Unit=e" // e or m
            + "&temp=temp"
            + "&rh=rh"
            + "&wspd=wspd"
            + "&wdir=wdir"
            + "&sky=sky"
            + "&wgust=wgust"
            + "&wx=wx"
            + "&critfireo=critfireo";

    /** Singleton */
    private static NwsWeatherProvider instance;
    private static final Logger logger = Logger.getLogger(NwsWeatherProvider.class.getName());

    /**
     * Get the singleton instance.
     * @return the singleton YahooPlaceProvider found on the global lookup.
     */
    static public NwsWeatherProvider getInstance() {
        if (instance == null) {
            return Lookup.getDefault().lookup(NwsWeatherProvider.class);
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
    }

    @Override
    public Weather getWeather(Date utcTime, Coord2D coord) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Field getPointForecast(Coord2D coord) {
        // Build the query string and URL
        String pointForecastQuery = String.format(NDFD_POINT_FORECAST,
                coord.getLatitudeDegrees(),
                coord.getLongitudeDegrees(),
                "", // empty = first available time
                "");    // empty = last available time
        StringBuilder sb = new StringBuilder();
        sb.append(NDFD_REST_URI).append(pointForecastQuery);
        String urlString = sb.toString();
        System.out.println(urlString);

        // Invoke the REST service and get the JSON results
        String dwml = HttpUtil.callWebService(urlString);
        System.out.println(dwml);

        // Parse the XML
        List<FlatField> fields = new DwmlParser(dwml).parse();
        if (!fields.isEmpty()) {
            return fields.get(0);
        }
        return null;
    }

}
