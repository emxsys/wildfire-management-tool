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
package com.emxsys.wmt.weather.api.yahoo;

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.util.ImageUtil;
import com.emxsys.wmt.weather.api.Weather;
import com.emxsys.wmt.weather.api.WeatherProvider;
import java.util.Date;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import visad.Field;

/**
 * @author Bruce Schubert
 */
public class YahooWeatherProvider implements WeatherProvider {

    /** URI for YQL public service. */
    protected static final String PUBLIC_URI = "http://query.yahooapis.com/v1/public/yql?";
    /** URI for YQL public service. */
    protected static final String OAUTH_URI = "http://query.yahooapis.com/v1/yql?";
    /** AppId for registered 'WMT' app */
    protected static final String APP_ID = "appid=94tzMP34";
    /** YQL query param: SQL like text */
    protected static final String QUERY_PARAM = "select * from geo.placefinder where text=\"%1$s\"";
    /** Common logger */
    private static final Logger LOG = Logger.getLogger(YahooWeatherProvider.class.getName());

    /**
     * <pre>
     * Top-Level Elements
     *
     * Element     Description
     * xml         The Weather RSS feed conforms to XML 1.0. No child elements.
     * rss         The Weather RSS feed conforms to RSS 2.0.
     * Child elements: channel
     *
     * Channel Elements
     *
     * The channel element contains metadata about the feed and its contents.
     * Element      Description
     * title        The title of the feed, which includes the location city.
     *              For example "Yahoo! Weather - Sunnyvale, CA"
     * link         The URL for the Weather page of the forecast for this location.
     *              For example http://us.rd.yahoo.com/dailynews/rss/weather/ Sunnyvale__CA/
     *              http://weather.yahoo.com/ forecast/USCA1116_f.html
     * language     The language of the weather forecast, for example, en-us for US English.
     * description  The overall description of the feed including the location, for example
     *              "Yahoo! Weather for Sunnyvale, CA"
     * lastBuildDate 	The last time the feed was updated. The format is in the date format defined
     *                  by RFC822 Section 5, for example Mon, 256 Sep 17:25:18 -0700.
     * ttl          Time to Live; how long in minutes this feed should be cached.
     * yweather:location 	The location of this forecast. Attributes:
     *
     * city:        city name (string)
     * region:      state, territory, or region, if given (string)
     * country:     two-character country code. (string)
     *
     * yweather:units 	Units for various aspects of the forecast. Attributes:
     *
     *      temperature:    degree units, f for Fahrenheit or c for Celsius (character)
     *      distance:       units for distance, mi for miles or km for kilometers (string)
     *      pressure:       units of barometric pressure, in for pounds per square inch or mb for millibars (string)
     *      speed:          units of speed, mph for miles per hour or kph for kilometers per hour (string)
     *
     * Note that the default RSS feed uses Fahrenheit degree units and English units for all other attributes
     * (miles, pounds per square inch, miles per hour). If Celsius has been specified as the degree
     * units for the feed (using the u request parameter), all the units are in metric format
     * (Celsius, kilometers, millibars, kilometers per hour).
     *
     * yweather:wind 	Forecast information about wind. Attributes:
     *
     *      chill:      wind chill in degrees (integer)
     *      direction:  wind direction, in degrees (integer)
     *      speed:      wind speed, in the units specified in the speed attribute of the
     *                  yweather:units element (mph or kph). (integer)
     *
     * yweather:atmosphere 	Forecast information about current atmospheric pressure, humidity, and
     *                      visibility. Attributes:
     *
     *      humidity:   humidity, in percent (integer)
     *      visibility, in the units specified by the distance attribute of the yweather:units
     *                  element (mi or km). Note that the visibility is specified as the
     *                  actual value * 100. For example, a visibility of 16.5 miles will be
     *                  specified as 1650. A visibility of 14 kilometers will appear as 1400. (integer)
     *      pressure:   barometric pressure, in the units specified by the pressure attribute of
     *                  the yweather:units element (in or mb). (float).
     *      rising:     state of the barometric pressure: steady (0), rising (1), or falling (2).
     *                  (integer: 0, 1, 2)
     *
     * yweather:astronomy 	Forecast information about current astronomical conditions. Attributes:
     *
     *      sunrise:    today's sunrise time. The time is a string in a local time format of
     *                  "h:mm am/pm", for example "7:02 am" (string)
     *      sunset:     today's sunset time. The time is a string in a local time format of
     *                  "h:mm am/pm", for example "4:51 pm" (string)
     *
     * image 	The image used to identify this feed. See Image Elements for element descriptions
     *
     * Child elements: url, title, link, width, height
     * item 	The local weather conditions and forecast for a specific location. See Item Elements for element descriptions.
     * Child elements: title, link, description, guid, pubDate, geo:lat, geo:long, yweather:forecast
     * Image Elements
     *
     * The image element describes an image or icon used to identify the feed.
     * Element      Description
     * title        The title of the image, for example "Yahoo! Weather"
     * link         The URL of Yahoo! Weather
     * url          The URL of the image
     * width        The width of the image, in pixels
     * height       The height of the image, in pixels
     * Item Elements
     *
     * The item element contains current conditions and forecast for the given location. There are
     * multiple yweather:forecast elements for today and tomorrow.
     * Element      Description
     * title        The forecast title and time, for example "Conditions for New York, NY at 1:51 pm EST"
     * link         The Yahoo! Weather URL for this forecast.
     * description 	A simple summary of the current conditions and tomorrow's forecast, in HTML
     *              format, including a link to Yahoo! Weather for the full forecast.
     * guid         Unique identifier for the forecast, made up of the location ID, the date, and
     *              the time. The attribute isPermaLink is false.
     * pubDate      The date and time this forecast was posted, in the date format defined by
     *              RFC822 Section 5, for example Mon, 25 Sep 17:25:18 -0700.
     * geo:lat      The latitude of the location.
     * geo:long     The longitude of the location.
     *
     * yweather:condition 	The current weather conditions. Attributes:
     *
     *      text:   a textual description of conditions, for example, "Partly Cloudy" (string)
     *      code:   the condition code for this forecast. You could use this code to choose a text
     *              description or image for the forecast. The possible values for this element are
     *              described in Condition Codes (integer)
     *      temp:   the current temperature, in the units specified by the yweather:units element (integer)
     *      date:   the current date and time for which this forecast applies. The date is in
     *              RFC822 Section 5 format, for example "Wed, 30 Nov 2005 1:56 pm PST" (string)
     *
     * yweather:forecast 	The weather forecast for a specific day. The item element contains
     *                      multiple forecast elements for today and tomorrow. Attributes:
     *
     *      day:    day of the week to which this forecast applies.
     *              Possible values are Mon Tue Wed Thu Fri Sat Sun (string)
     *      date:   the date to which this forecast applies. The date is in "dd Mmm yyyy" format,
     *              for example "30 Nov 2005" (string)
     *      low:    the forecasted low temperature for this day, in the units specified by the
     *              yweather:units element (integer)
     *      high:   the forecasted high temperature for this day, in the units specified by the
     *              yweather:units element (integer)
     *      text:   a textual description of conditions, for example, "Partly Cloudy" (string)
     *      code:   the condition code for this forecast. You could use this code to choose a text
     *              description or image for the forecast. The possible values for this element are
     *              described in Condition Codes (integer)
     *
     * Condition Codes
     *
     * Condition codes are used in the yweather:forecast element to describe the current conditions.
     * Code   Description
     * 0 	tornado
     * 1 	tropical storm
     * 2 	hurricane
     * 3 	severe thunderstorms
     * 4 	thunderstorms
     * 5 	mixed rain and snow
     * 6 	mixed rain and sleet
     * 7 	mixed snow and sleet
     * 8 	freezing drizzle
     * 9 	drizzle
     * 10 	freezing rain
     * 11 	showers
     * 12 	showers
     * 13 	snow flurries
     * 14 	light snow showers
     * 15 	blowing snow
     * 16 	snow
     * 17 	hail
     * 18 	sleet
     * 19 	dust
     * 20 	foggy
     * 21 	haze
     * 22 	smoky
     * 23 	blustery
     * 24 	windy
     * 25 	cold
     * 26 	cloudy
     * 27 	mostly cloudy (night)
     * 28 	mostly cloudy (day)
     * 29 	partly cloudy (night)
     * 30 	partly cloudy (day)
     * 31 	clear (night)
     * 32 	sunny
     * 33 	fair (night)
     * 34 	fair (day)
     * 35 	mixed rain and hail
     * 36 	hot
     * 37 	isolated thunderstorms
     * 38 	scattered thunderstorms
     * 39 	scattered thunderstorms
     * 40 	scattered showers
     * 41 	heavy snow
     * 42 	scattered snow showers
     * 43 	heavy snow
     * 44 	partly cloudy
     * 45 	thundershowers
     * 46 	snow showers
     * 47 	isolated thundershowers
     * 3200 	not available
     * </pre>
     */
    @Override
    public Weather getWeather(Date utcTime, Coord2D coord) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ImageIcon getImageIcon() {
        return ImageUtil.createImageIconFromResource("yql.png", getClass());
        
    }

    @Override
    public Field getPointForecast(Coord2D coord) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
