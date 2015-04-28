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
package com.emxsys.weather.api;

import com.emxsys.util.ImageUtil;
import com.emxsys.visad.Reals;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.SpatialField;
import com.emxsys.visad.TemporalDomain;
import static com.emxsys.weather.api.WeatherType.FIRE_WEATHER;
import com.emxsys.weather.api.services.WeatherObserver;
import java.time.Duration;
import java.time.ZonedDateTime;
import javax.swing.ImageIcon;
import org.openide.util.lookup.InstanceContent;
import visad.Real;

/**
 *
 * @author Bruce Schubert
 */
public class SimpleWeatherProvider extends AbstractWeatherProvider {

    private Real windSpd = new Real(WeatherType.WIND_SPEED_KTS);
    private Real windDir = new Real(WeatherType.WIND_DIR);
    private Real airTemp = new Real(WeatherType.AIR_TEMP_F);
    private Real relHumd = new Real(WeatherType.REL_HUMIDITY);
    private Real cldCovr = new Real(WeatherType.CLOUD_COVER);

    /**
     * Default Constructor. The weather values are all "missing".
     */
    public SimpleWeatherProvider() {
        // Initialize the lookup with this provider's capabilities
        InstanceContent content = getContent();
        content.add(new WeatherObserver() {

            /**
             * Gets a WeatherModel from the weather values stored in this provider.
             * @param areaOfInterest Each coordinate in the spatial domain will get the same values.
             * @param age Ignored. The current time will be used for the temporal domain.
             * @return a new WeatherModel.
             */
            @Override
            public WeatherModel getLatestObservations(SpatialDomain areaOfInterest, Duration age) {
                TemporalDomain timeDomain = TemporalDomain.from(ZonedDateTime.now());
                return getObservations(areaOfInterest, timeDomain);
            }

            /**
             * Gets a WeatherModel from the weather values stored in this provider.
             * @param areaOfInterest Each coordinate in the spatial domain will get the same weather
             * values.
             * @param timeframe Each time in the domain will get the same weather value.
             * @return a new WeatherModel.
             */
            @Override
            public WeatherModel getObservations(SpatialDomain areaOfInterest, TemporalDomain timeframe) {

                final int numTimes = timeframe.getDomainSetLength();
                final int numLatLons = areaOfInterest.getDomainSetLength();

                SpatialField[] fields = new SpatialField[numTimes];
                for (int t = 0; t < numTimes; t++) {
                    double[][] rangeSamples = new double[FIRE_WEATHER.getDimension()][numLatLons];
                    for (int xy = 0; xy < numLatLons; xy++) {
                        rangeSamples[0][xy] = airTemp.getValue();
                        rangeSamples[1][xy] = relHumd.getValue();
                        rangeSamples[2][xy] = windSpd.getValue();
                        rangeSamples[3][xy] = windDir.getValue();
                        rangeSamples[4][xy] = cldCovr.getValue();
                    }
                    fields[t] = SpatialField.from(areaOfInterest, FIRE_WEATHER, rangeSamples);
                }
                return WeatherModel.from(timeframe, fields);
            }
        });
    }

    @Override
    public String getName() {
        return "Simple Weather";
    }

    public void setWeather(Weather wx) {
        setWindSpeed(wx.getWindSpeed());
        setWindDirection(wx.getWindDirection());
        setAirTemperature(wx.getAirTemperature());
        setRelativeHumdity(wx.getRelativeHumidity());
        setCloudCover(wx.getCloudCover());
    }

    public void setWindSpeed(Real windSpd) {
        this.windSpd = Reals.convertTo(WeatherType.WIND_SPEED_KTS, windSpd);
    }

    public void setWindDirection(Real windDir) {
        this.windDir = Reals.convertTo(WeatherType.WIND_DIR, windDir);
    }

    public void setAirTemperature(Real airTemp) {
        this.airTemp = Reals.convertTo(WeatherType.AIR_TEMP_F, airTemp);
    }

    public void setRelativeHumdity(Real relHumd) {
        this.relHumd = Reals.convertTo(WeatherType.REL_HUMIDITY, relHumd);
    }

    public void setCloudCover(Real cldCovr) {
        this.cldCovr = Reals.convertTo(WeatherType.CLOUD_COVER, cldCovr);
    }

    /**
     * Gets the current weather values.
     * @return A {@code BasicWeather} containing the weather values.
     */
    public BasicWeather getWeather() {
        return BasicWeather.fromReals(
                this.airTemp,
                this.relHumd,
                this.windSpd,
                this.windDir,
                this.cldCovr);
    }

    @Override
    public ImageIcon getImageIcon() {
        return ImageUtil.createImageIconFromResource("images/sun_clouds.png", getClass());
    }

}
