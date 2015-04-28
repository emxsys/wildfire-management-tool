/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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

import com.emxsys.visad.RealXmlAdapter;
import static com.emxsys.visad.Reals.*;
import static com.emxsys.weather.api.WeatherType.*;
import java.rmi.RemoteException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.Exceptions;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 * BasicWeather is a concrete implementation of the Weather interface.
 *
 * The weather data model in VisAD can be represented by a FunctionType:
 *
 * (lat,lon,time) -> (air_temp, relative_humidity, wind_speed, wind_dir, cloud_cover)
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@XmlRootElement(name = "weather")
@XmlType(propOrder = {"airTemperature", "relativeHumidity", "windSpeed", "windDirection", "cloudCover"})
public class BasicWeather implements Weather {

    /** A tuple with "missing" components */
    public static final BasicWeather INVALID_WEATHER = new BasicWeather();
    private static final Logger logger = Logger.getLogger(BasicWeather.class.getName());

    /**
     * Creates a BasicWeather from a RealTuple of type FIRE_WEATHER.
     * @param fireWeather A WeatherType.FIRE_WEATHER RealTuple.
     * @return A new BasicWeather.
     */
    public static BasicWeather fromRealTuple(RealTuple fireWeather) {
        if (!fireWeather.getType().equals(FIRE_WEATHER)) {
            throw new IllegalArgumentException("Incompatible MathType: " + fireWeather.getType());
        } else if (fireWeather.isMissing()) {
            return INVALID_WEATHER;
        }
        try {
            // Use factory method to ensure the tuple components are converted to the expected units.
            Real[] reals = fireWeather.getRealComponents();
            return fromReals(reals[0], reals[1], reals[2], reals[3], reals[4]);
        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, "Cannot create WeatherTuple.", ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Construct a BasicWeather object from Reals. Guarantees that input parameter values are
     * converted to the RealTupleType's specified RealTypes.
     *
     * @param airTemperature must be compatible with WildfireType.AIR_TEMP_C
     * @param relativeHumidity must be compatible with WildfireType.REL_HUMIDITY
     * @param windSpeed must be compatible with WildfireType.WIND_SPEED_SI
     * @param windDirection must be compatible with WildfireType.WIND_DIR
     * @param cloudCover must be compatible with WildfireType.CLOUD_COVER
     * @return A new BasicWeather.
     */
    public static BasicWeather fromReals(Real airTemperature, Real relativeHumidity,
                                         Real windSpeed, Real windDirection, Real cloudCover) {
        try {
            Real[] reals = new Real[]{
                convertTo(AIR_TEMP_F, airTemperature),
                convertTo(REL_HUMIDITY, relativeHumidity),
                convertTo(WIND_SPEED_KTS, windSpeed),
                convertTo(WIND_DIR, windDirection),
                convertTo(CLOUD_COVER, cloudCover)
            };
            return new BasicWeather(new RealTuple(reals));

        } catch (VisADException | RemoteException ex) {
            logger.log(Level.SEVERE, "Cannot create WeatherTuple.", ex);
            throw new IllegalStateException(ex);
        }

    }

    private Real airTemperature;
    private Real relativeHumidity;
    private Real windSpeed;
    private Real windDirection;
    private Real cloudCover;

    /**
     * Constructs an instance with supplied values.
     * @param Weather
     */
    BasicWeather(RealTuple tuple) throws VisADException, RemoteException {
        this.airTemperature = (Real) tuple.getComponent(AIR_TEMP_INDEX);
        this.relativeHumidity = (Real) tuple.getComponent(REL_HUMIDITY_INDEX);
        this.windSpeed = (Real) tuple.getComponent(WIND_SPEED_INDEX);
        this.windDirection = (Real) tuple.getComponent(WIND_DIR_INDEX);
        this.cloudCover = (Real) tuple.getComponent(CLOUD_COVER_INDEX);
    }

    /**
     * Constructs an instance with missing values.
     */
    public BasicWeather() {
        this.airTemperature = new Real(AIR_TEMP_F);
        this.relativeHumidity = new Real(REL_HUMIDITY);
        this.windSpeed = new Real(WIND_SPEED_KTS);
        this.windDirection = new Real(WIND_DIR);
        this.cloudCover = new Real(CLOUD_COVER);

    }

    public RealTuple getTuple() {
        try {
            return new RealTuple(WeatherType.FIRE_WEATHER,
                    new Real[]{airTemperature,
                               relativeHumidity,
                               windSpeed,
                               windDirection,
                               cloudCover}, null);
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            return new RealTuple(FIRE_WEATHER);
        }

    }

    /**
     * Air temperature
     * @return [degF]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getAirTemperature() {
        return this.airTemperature;
    }

    public void setAirTemperature(Real airTemperature) {
        this.airTemperature = convertTo(AIR_TEMP_F, airTemperature);
    }

    /**
     * Relative humidity
     * @return [percent]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getRelativeHumidity() {
        return this.relativeHumidity;
    }

    public void setRelativeHumidity(Real relativeHumidity) {
        this.relativeHumidity = convertTo(REL_HUMIDITY, relativeHumidity);
    }

    /**
     * Wind Speed
     * @return [kts]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getWindSpeed() {
        return this.windSpeed;
    }

    public void setWindSpeed(Real windSpeed) {
        this.windSpeed = convertTo(WIND_SPEED_KTS, windSpeed);
    }

    /**
     * Wind direction
     * @return [deg]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getWindDirection() {
        return this.windDirection;
    }

    public void setWindDirection(Real windDirection) {
        this.windDirection = convertTo(WIND_DIR, windDirection);
    }

    /**
     * Cloud cover
     * @return [percent]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    public Real getCloudCover() {
        return this.cloudCover;
    }

    public void setCloudCover(Real cloudCover) {
        this.cloudCover = convertTo(CLOUD_COVER, cloudCover);
    }

    /**
     * to string
     * @return string of me
     */
    @Override
    public String toString() {
        return "Air: " + getAirTemperature().toValueString()
                + ", RH: " + getRelativeHumidity().toValueString()
                + ", Spd: " + getWindSpeed().toValueString()
                + ", Dir: " + getWindDirection().toValueString()
                + ", Sky: " + getCloudCover().toValueString();
    }

    public boolean isMissing() {
        return this.airTemperature.isMissing()
                || this.relativeHumidity.isMissing()
                || this.windSpeed.isMissing()
                || this.windDirection.isMissing()
                || this.cloudCover.isMissing();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.airTemperature);
        hash = 89 * hash + Objects.hashCode(this.relativeHumidity);
        hash = 89 * hash + Objects.hashCode(this.windSpeed);
        hash = 89 * hash + Objects.hashCode(this.windDirection);
        hash = 89 * hash + Objects.hashCode(this.cloudCover);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BasicWeather other = (BasicWeather) obj;
        if (!Objects.equals(this.airTemperature, other.airTemperature)) {
            return false;
        }
        if (!Objects.equals(this.relativeHumidity, other.relativeHumidity)) {
            return false;
        }
        if (!Objects.equals(this.windSpeed, other.windSpeed)) {
            return false;
        }
        if (!Objects.equals(this.windDirection, other.windDirection)) {
            return false;
        }
        if (!Objects.equals(this.cloudCover, other.cloudCover)) {
            return false;
        }
        return true;
    }

}
