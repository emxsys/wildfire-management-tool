/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.solar.api;

import com.emxsys.wmt.visad.Reals;
import java.rmi.RemoteException;
import visad.Data;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: SolarTuple.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public class SolarTuple extends RealTuple implements Solar {

    private Real latitude;
    private Real declination;
    private Real sunrise;
    private Real sunset;
    private Data[] components;

    public SolarTuple(Real latitude, Real declination, Real sunrise, Real sunset) {
        super(SolarType.SOLAR_DATA);
        this.latitude = latitude;
        this.declination = declination;
        this.sunrise = sunrise;
        this.sunset = sunset;

    }

    public SolarTuple() {
        super(SolarType.SOLAR_DATA);
        this.latitude = new Real(SolarType.LATITUDE);
        this.declination = new Real(SolarType.DECLINATION);
        this.sunrise = new Real(SolarType.TIME);
        this.sunset = new Real(SolarType.TIME);
    }

    /**
     * Latitude is earth latutide for which the sunrise/sunset times apply.
     *
     * @return [degrees]
     */
    @Override
    public Real getLatitude() {
        return this.latitude;
    }

    /**
     * Declination is the earth's tilt angle relative to the sun at the given date.
     *
     * @return [degrees]
     */
    @Override
    public Real getDeclination() {
        return this.declination;
    }

    /**
     * Sunrise is the time at which daylight begins.
     * The time is in solar hours, where at 12:00 noon, the sun is at its highest
     * point in the sky. This time is independent of timezones.
     *
     * @return sunrise [solar time]
     */
    @Override
    public Real getSunrise() {
        return this.sunrise;
    }

    /**
     * Sunset is the time at which daylight ends.
     * The time is in solar hours, where at 12:00 noon, the sun is at its highest
     * point in the sky. This time is independent of timezones.
     *
     * @return sunset [solar time]
     */
    @Override
    public Real getSunset() {
        return this.sunset;
    }

    /**
     * Sunrise is the time at which daylight begins.
     * The time is in solar hours, where at 12:00 noon, the sun is at its highest
     * point in the sky. This time is independent of timezones.
     *
     * @return sunrise [solar hour]
     */
    @Override
    public double getSunriseHour() {
        return convertDateTimeToHour(this.sunrise);
    }

    /**
     * Sunset is the time at which daylight ends.
     * The time is in solar hours, where at 12:00 noon, the sun is at its highest
     * point in the sky. This time is independent of timezones.
     *
     * @return sunset [solar hour]
     */
    @Override
    public double getSunsetHour() {
        return convertDateTimeToHour(this.sunset);
    }

    /**
     * Convert from seconds-since-epoch to hours-since-midnight
     *
     * @param datetime
     * @return hour of day
     */
    private double convertDateTimeToHour(Real datetime) {
        //
        double val = Reals.convertTo(SolarType.SOLAR_HOUR, datetime).getValue();
        val %= 24;
        return val;
    }

    /**
     * Is missing any data elements?
     *
     * @return is missing
     */
    @Override
    public boolean isMissing() {
        return latitude.isMissing() || declination.isMissing()
                || sunrise.isMissing() || sunset.isMissing();
    }

    /**
     * Get the i'th component.
     *
     * @param i Which one
     * @return The component
     *
     * @throws RemoteException On badness
     * @throws VisADException On badness
     */
    @Override
    public Data getComponent(int i) throws VisADException, RemoteException {
        switch (i) {
            case 0:
                return latitude;
            case 1:
                return declination;
            case 2:
                return sunrise;
            case 3:
                return sunset;
            default:
                throw new IllegalArgumentException("Wrong component number:" + i);
        }
    }

    /**
     * Create, if needed, and return the component array.
     *
     * @return components
     */
    @Override
    public Data[] getComponents(boolean copy) {
        //Create the array and populate it if needed
        if (components == null) {
            Data[] tmp = new Data[getDimension()];
            tmp[0] = latitude;
            tmp[1] = declination;
            tmp[2] = sunrise;
            tmp[3] = sunset;
            components = tmp;
        }
        return components;
    }

    /**
     * Indicates if this Tuple is identical to an object.
     *
     * @param obj The object.
     * @return            <code>true</code> if and only if the object is
     * a Tuple and both Tuple-s have identical component
     * sequences.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SolarTuple)) {
            return false;
        }
        SolarTuple that = (SolarTuple) obj;
        return this.latitude.equals(that.latitude)
                && this.declination.equals(that.declination)
                && this.sunrise.equals(that.sunrise)
                && this.sunset.equals(that.sunset);
    }

    /**
     * Returns the hash code of this object.
     *
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        return latitude.hashCode() | declination.hashCode()
                & sunrise.hashCode() & sunset.hashCode();
    }

    /**
     * to string
     *
     * @return string of me
     */
    @Override
    public String toString() {
        return getLatitude() + " " + getDeclination() + " " + getSunrise() + " " + getSunset();
    }
}
