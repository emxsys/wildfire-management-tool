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

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import static com.emxsys.solar.api.SolarType.*;
import com.emxsys.visad.RealXmlAdaptor;
import com.emxsys.visad.Reals;
import java.rmi.RemoteException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.Exceptions;
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
        = {"observerTime", "observerLatitude", "observerLongitude", "observerAltitude",
           "subsolarLatitude", "subsolarLongitude",
           "azimuthAngle", "altitudeAngle", "zenithAngle",
           "localHourAngle", "sunriseHourAngle", "sunsetHourAngle",
           "sunriseTime", "sunsetTime", "sunTransitTime",
        })
public class BasicSunlight implements Sunlight {

    /**
     * A tuple with "missing" components
     */
    public static final BasicSunlight INVALID = new BasicSunlight();

    /**
     * A factory method to create a BasicSunlight instance from a tuple.
     * @param time The date/time for the sunlight observation.
     * @param location The coordinates of the observer.
     * @param sunightTuple A SolarType.SUNLIGHT tuple.
     * @return A new BasicSunlight instance.
     */
    public static BasicSunlight fromRealTuple(ZonedDateTime time, Coord3D location, RealTuple sunightTuple) {
        if (!sunightTuple.getType().equals(SolarType.SUNLIGHT)) {
            throw new IllegalArgumentException("Incompatible MathType: " + sunightTuple.getType());
        } else if (sunightTuple.isMissing()) {
            return INVALID;
        }
        try {

            return new BasicSunlight(time, location, sunightTuple);

        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    private ZonedDateTime dateTime;
    private Coord3D location;
    private Real subsolarLatitude;
    private Real subsolarLongitude;
    private Real azimuthAngle;
    private Real altitudeAngle;
    private Real zenithAngle;
    private Real localHourAngle;
    private Real sunriseHourAngle;
    private Real sunsetHourAngle;
    private Real sunriseHour;
    private Real sunsetHour;
    private Real sunTransitHour;
    private Real zoneOffsetHour;

    /**
     * Constructs and instance with missing values.
     * @param sunlight
     */
    BasicSunlight(ZonedDateTime time, Coord3D coord, RealTuple tuple) throws VisADException, RemoteException {
        this.dateTime = time;
        this.location = coord;
        try {
            this.subsolarLatitude = (Real) tuple.getComponent(SolarType.SUBSOLAR_LATITUDE_INDEX);
            this.subsolarLongitude = (Real) tuple.getComponent(SolarType.SUBSOLAR_LONGITIDUE_INDEX);
            this.azimuthAngle = (Real) tuple.getComponent(SolarType.AZIMUTH_ANGLE_INDEX);
            this.zenithAngle = (Real) tuple.getComponent(SolarType.ZENITH_ANGLE_INDEX);
            this.altitudeAngle = (Real) tuple.getComponent(SolarType.ALTITUDE_ANGLE_INDEX);
            this.localHourAngle = (Real) tuple.getComponent(SolarType.HOUR_ANGLE_INDEX);
            this.sunriseHourAngle = (Real) tuple.getComponent(SolarType.SUNRISE_HOUR_ANGLE_INDEX);
            this.sunsetHourAngle = (Real) tuple.getComponent(SolarType.SUNSET_HOUR_ANGLE_INDEX);
            this.sunriseHour = (Real) tuple.getComponent(SolarType.SUNRISE_HOUR_INDEX);
            this.sunsetHour = (Real) tuple.getComponent(SolarType.SUNSET_HOUR_INDEX);
            this.sunTransitHour = (Real) tuple.getComponent(SolarType.SUNTRANSIT_HOUR_INDEX);
            this.zoneOffsetHour = (Real) tuple.getComponent(SolarType.ZONE_OFFSET_HOUR_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }

    }

    /**
     * Constructs and instance with missing values.
     */
    public BasicSunlight() {
        this.dateTime = ZonedDateTime.ofInstant(Instant.EPOCH,ZoneId.of("Z"));
        this.location = GeoCoord3D.INVALID_COORD;
        this.subsolarLatitude = new Real(SUBSOLAR_LATITUDE);
        this.subsolarLongitude = new Real(SUBSOLAR_LONGITUDE);
        this.azimuthAngle = new Real(AZIMUTH_ANGLE);
        this.zenithAngle = new Real(ZENITH_ANGLE);
        this.altitudeAngle = new Real(ALTITUDE_ANGLE);
        this.localHourAngle = new Real(HOUR_ANGLE);
        this.sunriseHourAngle = new Real(SUNRISE_HOUR_ANGLE);
        this.sunsetHourAngle = new Real(SUNSET_HOUR_ANGLE);
        this.sunriseHour = new Real(SUNRISE_HOUR);
        this.sunsetHour = new Real(SUNSET_HOUR);
        this.sunTransitHour = new Real(SUNTRANSIT_HOUR);
        this.zoneOffsetHour = new Real(ZONE_OFFSET_HOUR);

    }

    /**
     * Gets a SolarType.SUNLIGHT tuple, including: SUBSOLAR_LATITUDE, SUBSOLAR_LONGITUDE,
     * AZIMUTH_ANGLE, ZENITH_ANGLE, ALTITUDE_ANGLE, HOUR_ANGLE, SUNRISE_HOUR_ANGLE,
     * SUNSET_HOUR_ANGLE, SUNRISE_HOUR, SUNSET_HOUR, SUNTRANSIT_HOUR, ZONE_OFFSET_HOUR.
     *
     * @return A new RealTuple with RealTupleType SolarType.SUNLIGHT.
     * @see SolarType
     */
    public RealTuple getTuple() {
        Real[] reals = new Real[]{
            this.subsolarLatitude,
            this.subsolarLongitude,
            this.azimuthAngle,
            this.zenithAngle,
            this.altitudeAngle,
            this.localHourAngle,
            this.sunriseHourAngle,
            this.sunsetHourAngle,
            this.sunriseHour,
            this.sunsetHour,
            this.sunTransitHour,
            this.zoneOffsetHour};
        try {
            return new RealTuple(SUNLIGHT, reals, null);
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            return new RealTuple(SUNLIGHT);
        }
    }

    /**
     * Gets the date/time for when the sunlight is calculated.
     * @return The sunlight observation time.
     */
    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Gets the date/time for when the sunlight is calculated.
     * @return Date/time as ISO DATE TIME string.
     */
    @XmlElement
    public String getObserverTime() {
        return dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * Gets the location from where the sunlight is calculated.
     * @return The sunlight observer's location.
     */
    public Coord3D getLocation() {
        return location;
    }

    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getObserverLatitude() {
        return location.getLatitude();
    }

    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getObserverLongitude() {
        return location.getLongitude();
    }

    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getObserverAltitude() {
        return location.getAltitude();
    }

    /**
     * Gets the declination: the earth's tilt angle relative to the sun at a given date and time. It
     * is also the latitude where the sun is directly overhead (subsolar point).
     * @return The declination angle [degrees].
     */
    @Override
    public Real getDeclination() {
        return Reals.convertTo(DECLINATION, this.subsolarLatitude);
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
        return this.subsolarLatitude;
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
        return this.subsolarLongitude;
    }

    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getAzimuthAngle() {
        return this.azimuthAngle;
    }

    /**
     * Gets the solar zenith angle: where is sun relative to the observer's zenith.
     * @return The solar zenith angle. [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getZenithAngle() {
        return this.zenithAngle;
    }

    /**
     * Gets the solar altitude angle: how high the sun is from the horizon.
     * @return The solar altitude angle (possibly corrected for atmospheric refraction). [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getAltitudeAngle() {
        return this.altitudeAngle;
    }

    /**
     * Gets the local hour angle relative to the observer.
     * @return Hour angle between the sun and the observer. [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getLocalHourAngle() {
        return this.localHourAngle;
    }

    /**
     * Gets the solar hour angle for sunrise.
     * @return Sunrise hour angle from solar noon. [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getSunriseHourAngle() {
        return this.sunriseHourAngle;
    }

    /**
     * Gets the solar hour angle for sunset.
     * @return Sunset hour angle from solar noon. [degrees]
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdaptor.class)
    public Real getSunsetHourAngle() {
        return this.sunsetHourAngle;
    }

    /**
     * Gets the local time of sunrise.
     * @return Sunrise time in the local time zone. [hours]
     */
    @Override
    public Real getSunriseHour() {
        return this.sunriseHour;
    }

    /**
     * Gets the local time of sunset.
     * @return Sunset time in the local time zone. [hours]
     */
    @Override
    public Real getSunsetHour() {
        return this.sunsetHour;
    }

    /**
     * Gets the local sun transit time (or solar noon).
     * @return The time of solar noon in local time zone. [hours]
     */
    @Override
    public Real getSunTransitHour() {
        return this.sunTransitHour;
    }

    /**
     * Gets the time zone offset.
     * @return The offset from UTC to local time. [hours]
     */
    @Override
    public Real getZoneOffsetHour() {
        return this.zoneOffsetHour;
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
        return "Sunlight:"
                + "\n Observation Time: " + getDateTime()
                + "\n Observer's Location: " + getLocation()
                + "\n Declination: " + getDeclination().toValueString()
                + "\n Subsolar Latitude: " + subsolarLatitude.toValueString()
                + "\n Subsolar Longitude: " + subsolarLongitude.toValueString()
                + "\n Azimuth Angle: " + azimuthAngle.toValueString()
                + "\n Altitude Angle: " + altitudeAngle.toValueString()
                + "\n Zenith Angle: " + zenithAngle.toValueString()
                + "\n Local Hour Angle: " + localHourAngle.toValueString()
                + "\n Sunrise Hour Angle: " + sunriseHourAngle.toValueString()
                + "\n Sunset Hour Angle: " + sunsetHourAngle.toValueString()
                + "\n Sunrise Hour: " + sunriseHour.toValueString()
                + "\n Sunset Hour: " + sunsetHour.toValueString()
                + "\n Sun Transit Hour: " + sunTransitHour.toValueString()
                + "\n Zone Offset Hour: " + zoneOffsetHour.toValueString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.dateTime);
        hash = 19 * hash + Objects.hashCode(this.location);
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
        final BasicSunlight other = (BasicSunlight) obj;
        if (!Objects.equals(this.dateTime, other.dateTime)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        return true;
    }

}
