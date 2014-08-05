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
import javax.swing.ImageIcon;
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
     * Default Constructor.
     */
    public SimpleWeatherProvider() {
        // Initialize the lookup with this provider's capabilities
        //InstanceContent content = getContent();
        //content.add((StationObserver) this::getCurrentWeather);  // functional interface 
    }

    @Override
    public String getName() {
        return "Simple Weather";
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
     * @return A {@code WeatherTuple} containing the weather values.
     */
    public WeatherTuple getWeather() {
        return WeatherTuple.fromReals(
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
