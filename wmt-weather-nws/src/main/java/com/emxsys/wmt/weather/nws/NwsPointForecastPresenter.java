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

import com.emxsys.gis.api.Coord2D;
import com.emxsys.weather.api.PointForecastWebPresenter;
import java.net.URL;

/**
 * This class presents a point forecast as an HTML web page.
 *
 * @author Bruce Schubert
 */
public class NwsPointForecastPresenter implements PointForecastWebPresenter {

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

    @Override
    public String getForecastHtml(Coord2D coord) {
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
                + "			<img alt=\"National Weather Service\" height=\"100\" src=\"" + logo + "\" width=\"100\" />\n"
                + "		</td>\n"
                + "		<th colspan=\"4\">Point Forecast for " + coord.toString() + "</th>\n"
                + "	</tr>\n"
                + "	<tr>\n"
                + "		<td style=\"width: 50px\" valign=\"top\">\n"
                + "		<a href=\"" + QUICK_FORECAST_PAGE + "\"><img alt=\"Quick Forecast\" height=\"45\" src=\"" + quick + "\" width=\"50\" /></a><br/>\n"
                + "		<a href=\"" + QUICK_FORECAST_PAGE + "\">Quick Forecast</a></td>\n"
                + "		<td style=\"width: 50px\" valign=\"top\">\n"
                + "		<a href=\"" + SVN_DAY_PRINTABLE_PAGE + "\"><img alt=\"7-Day Forecast\" height=\"45\" src=\"" + svnday + "\" width=\"50\" /></a><br/>\n"
                + "		<a href=\"" + SVN_DAY_PRINTABLE_PAGE + "\">7-Day</a></td>\n"
                + "		<td style=\"width: 50px\" valign=\"top\">\n"
                + "		<a href=\"" + HOURLY_GRAPH_PAGE + "\"><img alt=\"Hourly Graph\" height=\"45\" src=\"" + hourly + "\" width=\"50\" /></a><br/>\n"
                + "		<a href=\"" + HOURLY_GRAPH_PAGE + "\">Hourly Graph</a></td>\n"
                + "		<td style=\"width: 50px\" valign=\"top\">\n"
                + "		<a href=\"" + TABULAR_PAGE + "\"><img alt=\"Tabular Data\" height=\"45\" src=\"" + tabular + "\" width=\"50\" /></a><br/>\n"
                + "		<a href=\"" + TABULAR_PAGE + "\">Tabular Data</a></td>\n"
                + "	</tr>\n"
                + "</table>\n"
                + "</body>"
                + "</html>";

        String html = String.format(htmlTemplate, coord.getLatitudeDegrees(), coord.getLongitudeDegrees());
        //System.out.println(html);
        return html;
    }

}
