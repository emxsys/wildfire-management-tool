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

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.GisType;
import com.emxsys.util.ImageUtil;
import com.emxsys.visad.Reals;
import com.emxsys.weather.api.AbstractWeatherProvider;
import com.emxsys.weather.api.ConditionsObserver;
import com.emxsys.weather.api.WeatherProvider;
import com.emxsys.weather.api.WeatherType;
import java.rmi.RemoteException;
import java.time.Duration;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import visad.Field;
import visad.FlatField;
import visad.FunctionType;
import visad.Irregular2DSet;
import visad.Real;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class SimpleWeatherProvider extends AbstractWeatherProvider {

    private Real windSpd = new Real(WeatherType.WIND_SPEED_KTS);
    private Real windDir = new Real(WeatherType.WIND_DIR);
    private Real airTemp = new Real(WeatherType.AIR_TEMP_F);
    private Real relHumd = new Real(WeatherType.REL_HUMIDITY);

    private static final Logger logger = Logger.getLogger(SimpleWeatherProvider.class.getName());

    /**
     * Default Constructor.
     */
    public SimpleWeatherProvider() {
        // Initialize the lookup with this provider's capabilities
        InstanceContent content = getContent();
        content.add((ConditionsObserver) this::getCurrentWeather);  // functional interface 
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

    /**
     * Gets the current weather values set by the manual inputs.
     *
     * @param coord Ignored, but returned in the Field's domain.
     * @param radius Ignored.
     * @param age Ignored.
     * @return A FlatField containing current weather values from the manual inputs.
     */
    public Field getCurrentWeather(Coord2D coord, Real radius, Duration age) {

        try {
            // Create the domain sample from the coordinate
            float[][] latLonSamples = new float[2][1];
            latLonSamples[0][0] = (float) coord.getLatitudeDegrees();
            latLonSamples[1][0] = (float) coord.getLatitudeDegrees();

            // Create the wx range samples, and init with "missing" values
            double[][] wxSamples = new double[WX_RANGE.getDimension()][1];
            wxSamples[AIR_TEMP_IDX][0] = this.airTemp.getValue();
            wxSamples[HUMIDITY_IDX][0] = this.relHumd.getValue();
            wxSamples[WIND_SPD_IDX][0] = this.windSpd.getValue();
            wxSamples[WIND_DIR_IDX][0] = this.windDir.getValue();

            // Create the domain Set, with 2 columns and 1 rows, using an
            // Gridded2DDoubleSet(MathType type, double[][] samples, lengthX)
            Irregular2DSet domainSet = new Irregular2DSet(GisType.LATLON, latLonSamples);

            // Create a MathType for the function ( (lat, lon ) -> ( air_temp, RH, wind_spd, ... ) )
            FunctionType stationWxFunc = new FunctionType(GisType.LATLON, WX_RANGE);

            // Create a FlatField
            // Use FlatField(FunctionType type, Set domain_set)
            FlatField values_ff = new FlatField(stationWxFunc, domainSet);

            // ...and put the weather values above into it
            values_ff.setSamples(wxSamples);
            return values_ff;

        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public ImageIcon getImageIcon() {
        return ImageUtil.createImageIconFromResource("images/sun_clouds.png", getClass());
    }

}
