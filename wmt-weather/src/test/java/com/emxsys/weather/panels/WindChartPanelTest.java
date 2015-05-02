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
package com.emxsys.weather.panels;

import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.solar.spi.SunlightProviderFactory;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.api.WeatherPreferences;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.weather.spi.WeatherProviderFactory;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import visad.FlatField;
import visad.Real;

/**
 * Test the layout/display of the WeatherChartPanel.
 *
 * @author Bruce Schubert
 */
public class WindChartPanelTest {

    public WindChartPanelTest() {
    }

    @Ignore("interactive test")
    @Test
    public void testBehavior() {
        System.out.println("testBehavior - interactive");


        Sunlight sunlight = SunlightProviderFactory.getInstance().getSunlight(ZonedDateTime.now(), GeoCoord3D.fromDegrees(34.2, -119.2));
        DiurnalWeatherProvider wxProvider = WeatherProviderFactory.newDiurnalWeatherProvider(sunlight);

        TreeMap<LocalTime, Real> windSpeeds = new TreeMap<>();
        windSpeeds.put(LocalTime.of(6, 00), new Real(WeatherType.WIND_SPEED_MPH, 5));
        windSpeeds.put(LocalTime.of(9, 00), new Real(WeatherType.WIND_SPEED_KPH, 10));
        windSpeeds.put(LocalTime.of(12, 00), new Real(WeatherType.WIND_SPEED_KTS, 10));
        wxProvider.initializeWindSpeeds(windSpeeds);
        
        TreeMap<LocalTime, Real> windDirections = new TreeMap<>();
        windDirections.put(LocalTime.of(6, 00), new Real(WeatherType.WIND_DIR, 0));
        windDirections.put(LocalTime.of(9, 00), new Real(WeatherType.WIND_DIR, 45));
        windDirections.put(LocalTime.of(12, 00), new Real(WeatherType.WIND_DIR, 225));
        windDirections.put(LocalTime.of(16, 00), new Real(WeatherType.WIND_DIR, -45));
        wxProvider.initializeWindDirections(windDirections);

        TemporalDomain timeDomain = new TemporalDomain(ZonedDateTime.now(), 24);
        FlatField wx = wxProvider.getHourlyWeather(timeDomain);

        WindSpeedDirectionChart instance = new WindSpeedDirectionChart();
        instance.setTitle("Diurnal Weather");
        instance.setWindForecasts(wx);

        WeatherPreferences.setWindSpeedUom(WeatherPreferences.UOM_MPH);
        assertTrue("Form was invalidated by the user",
                JOptionPane.showConfirmDialog(
                        null, // frame
                        instance,
                        "Are Values in MPH?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null) == JOptionPane.YES_OPTION);

        WeatherPreferences.setWindSpeedUom(WeatherPreferences.UOM_KPH);
        assertTrue("Form was invalidated by the user",
                JOptionPane.showConfirmDialog(
                        null, // frame
                        instance,
                        "Are Values in KPH?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null) == JOptionPane.YES_OPTION);
    }

}
