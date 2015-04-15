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
package com.emxsys.solar.api;

import com.emxsys.visad.RealXmlAdaptor;
import java.rmi.RemoteException;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.Exceptions;
import visad.Data;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 * BasicSunlight is a concrete implementation of the Sunlight interface using a SUNLIGHT
 * RealTupleType.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@XmlRootElement(name = "sunlight")
@XmlType(propOrder
        = {"subsolarLatitude", "subsolarLongitude",
           "azimuthAngle", "altitudeAngle", "zenithAngle",
           "localHourAngle", "sunriseHourAngle", "sunsetHourAngle",
           "sunriseTime", "sunsetTime", "sunTransitTime",
           "missing"
        })
public class BasicSunlight implements Sunlight {

    /**
     * A tuple with "missing" components 
     */
    public static final BasicSunlight INVALID_SUNLIGHT = new BasicSunlight();

    /**
     * A factory method to create a BasicSunlight instance from a tuple.
     * @param sunightTuple A SolarType.SUNLIGHT tuple.
     * @return A new BasicSunlight instance.
     */
    public static BasicSunlight fromRealTuple(RealTuple sunightTuple) {
        if (!sunightTuple.getType().equals(SolarType.SUNLIGHT)) {
            throw new IllegalArgumentException("Incompatible MathType: " + sunightTuple.getType());
        } else if (sunightTuple.isMissing()) {
            return INVALID_SUNLIGHT;
        }
        try {

            return new BasicSunlight(sunightTuple);

        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    /** The data implementation */
    private RealTuple tuple;

    /**
     * Constructs and instance with missing values.
     * @param sunlight
     */
    BasicSunlight(RealTuple sunlightTuple) throws VisADException, RemoteException {
        this.tuple = sunlightTuple;
        //super(SolarType.SUNLIGHT, sunlightTuple.getRealComponents(), null);
    }

    /**
     * Constructs and instance with missing values.
     */
    public BasicSunlight() {
        this.tuple = new RealTuple(SolarType.SUNLIGHT);
        //super(SolarType.SUNLIGHT);
    }

    /**
     * Gets the SolarType.SUNLIGHT implementation tuple, including: SUBSOLAR_LATITUDE,
     * SUBSOLAR_LONGITUDE, AZIMUTH_ANGLE, ZENITH_ANGLE, ALTITUDE_ANGLE, HOUR_ANGLE,
     * SUNRISE_HOUR_ANGLE, SUNSET_HOUR_ANGLE, SUNRISE_HOUR, SUNSET_HOUR, SUNTRANSIT_HOUR,
     * ZONE_OFFSET_HOUR.
     *
     * @return A RealTuple of type SolarType.SUNLIGHT.
     * @see SolarType
     */
    public RealTuple getTuple() {
        return this.tuple;
    }

    /**
     * Gets the declination: the earth's tilt angle relative to the sun at a given date and time. It
     * is also the latitude where the sun is directly overhead (subsolar point).
     * @return The declination angle [degrees].
     */
    @Override
    public Real getDeclination() {
        try {
            // Subsolar point latitude is same as declination angle
            return (Real) tuple.getComponent(SolarType.SUBSOLAR_LATITUDE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the subsolar latitude: the earth latitude for where the sun is overhead at a given date
     * and time. This is identical to the declination.
     * @return [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getSubsolarLatitude() {
        try {
            return (Real) tuple.getComponent(SolarType.SUBSOLAR_LATITUDE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the subsolar longitude: the earth longitude for where the sun is overhead at a given
     * date and time.
     * @return [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getSubsolarLongitude() {
        try {
            return (Real) tuple.getComponent(SolarType.SUBSOLAR_LONGITIDUE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getAzimuthAngle() {
        try {
            return (Real) tuple.getComponent(SolarType.AZIMUTH_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the solar zenith angle: where is sun relative to the observer's zenith.
     * @return The solar zenith angle. [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getZenithAngle() {
        try {
            return (Real) tuple.getComponent(SolarType.ZENITH_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the solar altitude angle: how high the sun is from the horizon.
     * @return The solar altitude angle (possibly corrected for atmospheric refraction). [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getAltitudeAngle() {
        try {
            return (Real) tuple.getComponent(SolarType.ALTITUDE_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the local hour angle relative to the observer.
     * @return Hour angle between the sun and the observer. [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getLocalHourAngle() {
        try {
            return (Real) tuple.getComponent(SolarType.HOUR_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the solar hour angle for sunrise.
     * @return Sunrise hour angle from solar noon. [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getSunriseHourAngle() {
        try {
            return (Real) tuple.getComponent(SolarType.SUNRISE_HOUR_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the solar hour angle for sunset.
     * @return Sunset hour angle from solar noon. [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getSunsetHourAngle() {
        try {
            return (Real) tuple.getComponent(SolarType.SUNSET_HOUR_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the local time of sunrise.
     * @return Sunrise time in the local time zone. [hours]
     */
    @Override
    public Real getSunriseHour() {
        try {
            return (Real) tuple.getComponent(SolarType.SUNRISE_HOUR_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the local time of sunset.
     * @return Sunset time in the local time zone. [hours]
     */
    @Override
    public Real getSunsetHour() {
        try {
            return (Real) tuple.getComponent(SolarType.SUNSET_HOUR_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the local sun transit time (or solar noon).
     * @return The time of solar noon in local time zone. [hours]
     */
    @Override
    public Real getSunTransitHour() {
        try {
            return (Real) tuple.getComponent(SolarType.SUNTRANSIT_HOUR_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets the time zone offset.
     * @return The offset from UTC to local time. [hours]
     */
    @Override
    public Real getZoneOffsetHour() {
        try {
            return (Real) tuple.getComponent(SolarType.ZONE_OFFSET_HOUR_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Tests whether any data elements are missing.
     *
     * @return true if a data element is missing.
     */
    @Override
    @XmlElement
    public boolean isMissing() {
        try {
            Data[] components = tuple.getComponents(false);
            if (components == null) {
                return true;
            } else {
                for (Data data : components) {
                    if (data == null || data.isMissing()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (VisADException | RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the local time of sunrise.
     * @return Sunrise time in the local time zone as ISO OFFSET TIME string.
     */
    @XmlElement
    public String getSunriseTime() {
        return toOffsetTime(getSunriseHour(), getZoneOffsetHour()).format(DateTimeFormatter.ISO_OFFSET_TIME);
    }

    /**
     * Gets the local time of sunset.
     * @return Sunset time in the local time zone as ISO OFFSET TIME string.
     */
    @XmlElement
    public String getSunsetTime() {
        return toOffsetTime(getSunsetHour(), getZoneOffsetHour()).format(DateTimeFormatter.ISO_OFFSET_TIME);
    }

    /**
     * Gets the local sun transit time (or solar noon).
     * @return Solar noon time in the local time zone as ISO OFFSET TIME string
     */
    @XmlElement
    public String getSunTransitTime() {
        return toOffsetTime(getSunTransitHour(), getZoneOffsetHour()).format(DateTimeFormatter.ISO_OFFSET_TIME);
    }

    private static OffsetTime toOffsetTime(Real hour, Real offset) {
        LocalTime localTime = LocalTime.ofSecondOfDay((long) (hour.getValue() * 3600));
        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds((int) (offset.getValue() * 3600));
        return OffsetTime.of(localTime, zoneOffset);
    }

    @Override
    public String toString() {
        try {
            return this.tuple.longString();
        } catch (VisADException | RemoteException ex) {
            return this.tuple.toString();
        }
    }

}
