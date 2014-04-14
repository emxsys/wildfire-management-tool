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
import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.openide.util.Exceptions;
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

    public static final String IMAGE_ICON_NAME = "nws_logo.png";
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

    private static final String HOURLY_GRAPH_PAGE = "http://forecast.weather.gov/MapClick.php"
            + "?w0=t&w1=td&w2=wc&w3=sfcwind&w4=sky&w5=pop&w6=rh&w8=rain&AheadHour=0"
            + "&Submit=Submit"
            + "&FcstType=graphical"
            + "&textField1=%1$f" // Lat
            + "&textField2=%2$f" // Lon
            + "&site=all"
            + "&menu=1";
    private static final String TABULAR_PAGE = "http://forecast.weather.gov/MapClick.php"
            + "?w0=t&w1=td&w2=wc&w3=sfcwind&w3u=1&w4=sky&w5=pop&w6=rh&w8=rain&AheadHour=0"
            + "&Submit=Submit"
            + "&FcstType=digital"
            + "&textField1=%1$f" // Lat
            + "&textField2=%2$f" // Lon
            + "&site=all"
            + "&unit=0"
            + "&menu=1"
            + "&dd=&bw=";
    private static final String SVN_DAY_PRINTABLE_PAGE = "http://forecast.weather.gov/MapClick.php"
            + "?lat=%1$f"
            + "&lon=%2$f"
            + "&unit=0"
            + "&lg=english"
            + "&FcstType=text"
            + "&TextType=2";
    private static final String SVN_DAY_TEXT_ONLY_PAGE = "http://forecast.weather.gov/MapClick.php"
            + "?lat=%1$f"
            + "&lon=%2$f"
            + "&unit=0"
            + "&lg=english"
            + "&FcstType=text"
            + "&TextType=1";
    private static final String SVN_DAY_FORECAST_PAGE = "http://forecast.weather.gov/MapClick.php"
            + "?lat=%1$f"
            + "&lon=%2$f"
            + "&smap=1"
            + "&unit=0"
            + "&lg=en"
            + "&FcstType=text";
    private static final String QUICK_FORECAST_PAGE = "http://forecast.weather.gov/afm/PointClick.php"
            + "?lat=%1$f"
            + "&lon=%2$f";
    /** Singleton */
    private static NwsWeatherProvider instance;
    private static final Logger logger = Logger.getLogger(NwsWeatherProvider.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

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

    public String getPointForecastPage(Coord2D coord) {
        URL logo = getClass().getResource("nws_logo.png");
        URL quick = getClass().getResource("quick.jpg");
        URL svnday = getClass().getResource("7day.jpg");
        URL hourly = getClass().getResource("hourlygraph.jpg");
        URL tabular = getClass().getResource("tabular.jpg");
        String htmlTemplate = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                + "\n"
                + "<head>\n"
                + "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />\n"
                + "<title>National Weather Service Point Forecast</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<table>\n"
                + "	<tr>\n"
                + "		<td rowspan=\"2\" style=\"width: 100px; height: 100px\">\n"
                + "			<img alt=\"National Weather Service\" height=\"100\" src=\""+logo+"\" width=\"100\" />\n"
                + "		</td>\n"
                + "		<th colspan=\"4\">Point Forecast for "+ coord.toString() +"</th>\n"
                + "	</tr>\n"
                + "	<tr>\n"
                + "		<td style=\"width: 50px\" valign=\"top\">\n"
                + "		<a href=\""+QUICK_FORECAST_PAGE+"\"><img alt=\"Quick Forecast\" height=\"45\" src=\""+quick+"\" width=\"50\" /></a><br/>\n"
                + "		<a href=\""+QUICK_FORECAST_PAGE+"\">Quick Forecast</a></td>\n"
                + "		<td style=\"width: 50px\" valign=\"top\">\n"
                + "		<a href=\""+SVN_DAY_PRINTABLE_PAGE+"\"><img alt=\"7-Day Forecast\" height=\"45\" src=\""+svnday+"\" width=\"50\" /></a><br/>\n"
                + "		<a href=\""+SVN_DAY_PRINTABLE_PAGE+"\">7-Day</a></td>\n"
                + "		<td style=\"width: 50px\" valign=\"top\">\n"
                + "		<a href=\""+HOURLY_GRAPH_PAGE+"\"><img alt=\"Hourly Graph\" height=\"45\" src=\""+hourly+"\" width=\"50\" /></a><br/>\n"
                + "		<a href=\""+HOURLY_GRAPH_PAGE+"\">Hourly Graph</a></td>\n"
                + "		<td style=\"width: 50px\" valign=\"top\">\n"
                + "		<a href=\""+TABULAR_PAGE+"\"><img alt=\"Tabular Data\" height=\"45\" src=\""+tabular+"\" width=\"50\" /></a><br/>\n"
                + "		<a href=\""+TABULAR_PAGE+"\">Tabular Data</a></td>\n"
                + "	</tr>\n"
                + "</table>\n"
                + "</body>"
                + "</html>";
                
                String html = String.format(htmlTemplate, coord.getLatitudeDegrees(), coord.getLongitudeDegrees());
                System.out.println(html);
                return html;
    }

    @Override
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
        String dwml;
        try {
            dwml = HttpUtil.callWebService(urlString);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "callWebService() failed: {0}", ex.getMessage());
            return null;
        }
        // Parse the XML
        List<FlatField> fields = new DwmlParser(dwml).parse();
        if (fields.isEmpty()) {
            return null;
        }
        return fields.get(0);
    }

}
