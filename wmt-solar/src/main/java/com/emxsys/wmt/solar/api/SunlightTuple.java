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
 * A SunlightTuple contains the position of the sun for a given date and time.
 * 
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SunlightTuple extends RealTuple implements Sunlight {

    private final Real declination;
    private final Real longitude;
    private Data[] components;

    public SunlightTuple(Real declination, Real longitude) {
        super(SolarType.SUNLIGHT_ANGLES);
        this.declination = declination;
        this.longitude = longitude;

    }

    public SunlightTuple() {
        super(SolarType.SUNLIGHT_ANGLES);
        this.declination = new Real(SolarType.DECLINATION);
        this.longitude = new Real(SolarType.LONGITUDE);
    }

    /**
     * Declination is the earth's tilt angle relative to the sun at the given date and time.
     *
     * @return [degrees]
     */
    @Override
    public Real getDeclination() {
        return this.declination;
    }

    /**
     * Longitude is the earth's longitude where the sun is overhead at the given date and time.
     *
     * @return [degrees]
     */
    @Override
    public Real getLongitude() {
        return this.longitude;
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
        return longitude.isMissing() || declination.isMissing();
    }

    /**
     * Get the i'th component.
     *
     * @param i Which one
     * @return The component
     * @throws RemoteException On badness
     * @throws VisADException On badness
     */
    @Override
    public Data getComponent(int i) throws VisADException, RemoteException {
        switch (i) {
            case 0:
                return declination;
            case 1:
                return longitude;
            default:
                throw new IllegalArgumentException("Wrong component number:" + i);
        }
    }

    /**
     * Create, if needed, and return the component array.
     *
     * @param copy ignored
     * @return components
     */
    @Override
    public Data[] getComponents(boolean copy) {
        //Create the array and populate it if needed
        if (components == null) {
            Data[] tmp = new Data[getDimension()];
            tmp[0] = declination;
            tmp[1] = longitude;
            components = tmp;
        }
        return components;
    }

    /**
     * Indicates if this Tuple is identical to an object.
     *
     * @param obj The object.
     * @return true if and only if the object is
     * a Tuple and both Tuple-s have identical component
     * sequences.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SunlightTuple)) {
            return false;
        }
        SunlightTuple that = (SunlightTuple) obj;
        return this.longitude.equals(that.longitude)
                && this.declination.equals(that.declination);
    }

    /**
     * Returns the hash code of this object.
     *
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        return longitude.hashCode() | declination.hashCode();
    }

    /**
     * to string
     *
     * @return string of me
     */
    @Override
    public String toString() {
        return getDeclination() + " " + getLongitude();
    }
}
