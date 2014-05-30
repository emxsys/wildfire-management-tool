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

import static com.emxsys.solar.api.SolarType.*;
import com.emxsys.visad.Tuples;
import java.rmi.RemoteException;
import org.openide.util.Exceptions;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 * SunlightTuple is a concrete implementation of the Sunlight interface using a SUNLIGHT
 * RealTupleType.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SunlightTuple extends RealTuple implements Sunlight {

    /** A tuple with "missing" components */
    public static final SunlightTuple INVALID_TUPLE = new SunlightTuple();
    public static final int SUBSOLAR_LATITUDE_INDEX = Tuples.getIndex(SUBSOLAR_LATITUDE, SUNLIGHT);
    public static final int SUBSOLAR_LONGITIDUE_INDEX = Tuples.getIndex(SUBSOLAR_LONGITUDE, SUNLIGHT);
    public static final int AZIMUTH_ANGLE_INDEX = Tuples.getIndex(AZIMUTH_ANGLE, SUNLIGHT);
    public static final int ZENITH_ANGLE_INDEX = Tuples.getIndex(ZENITH_ANGLE, SUNLIGHT);
    public static final int ALTITUDE_ANGLE_INDEX = Tuples.getIndex(ALTITUDE_ANGLE, SUNLIGHT);
    public static final int HOUR_ANGLE_INDEX = Tuples.getIndex(HOUR_ANGLE, SUNLIGHT);
    public static final int SUNRISE_HOUR_ANGLE_INDEX = Tuples.getIndex(SUNRISE_HOUR_ANGLE, SUNLIGHT);
    public static final int SUNSET_HOUR_ANGLE_INDEX = Tuples.getIndex(SUNSET_HOUR_ANGLE, SUNLIGHT);
    public static final int SUNTRANSIT_HOUR_INDEX = Tuples.getIndex(SUNTRANSIT_HOUR, SUNLIGHT);
    public static final int SUNRISE_HOUR_INDEX = Tuples.getIndex(SUNRISE_HOUR, SUNLIGHT);
    public static final int SUNSET_HOUR_INDEX = Tuples.getIndex(SUNSET_HOUR, SUNLIGHT);
    public static final int ZONE_OFFSET_HOUR_INDEX = Tuples.getIndex(ZONE_OFFSET_HOUR, SUNLIGHT);

    public static SunlightTuple fromRealTuple(RealTuple sunightTuple) {
        if (!sunightTuple.getType().equals(SolarType.SUNLIGHT)) {
            throw new IllegalArgumentException("Incompatible MathType: " + sunightTuple.getType());
        } else if (sunightTuple.isMissing()) {
            return INVALID_TUPLE;
        }
        try {

            return new SunlightTuple(sunightTuple);

        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Constructs and instance with missing values.
     * @param sunlight
     */
    SunlightTuple(RealTuple sunlightTuple) throws VisADException, RemoteException {
        super(SolarType.SUNLIGHT, sunlightTuple.getRealComponents(), null);
    }

    /**
     * Constructs and instance with missing values.
     */
    public SunlightTuple() {
        super(SolarType.SUNLIGHT);
    }

    @Override
    public Real getDeclination() {
        try {
            // Subsolar point latitude is same as declination angle
            return (Real) getComponent(SUBSOLAR_LATITUDE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getSubsolarLatitude() {
        try {
            return (Real) getComponent(SUBSOLAR_LATITUDE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getSubsolarLongitude() {
        try {
            return (Real) getComponent(SUBSOLAR_LONGITIDUE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getAzimuthAngle() {
        try {
            return (Real) getComponent(AZIMUTH_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getZenithAngle() {
        try {
            return (Real) getComponent(ZENITH_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getAltitudeAngle() {
        try {
            return (Real) getComponent(ALTITUDE_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getLocalHourAngle() {
        try {
            return (Real) getComponent(HOUR_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getSunriseHourAngle() {
        try {
            return (Real) getComponent(SUNRISE_HOUR_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getSunsetHourAngle() {
        try {
            return (Real) getComponent(SUNSET_HOUR_ANGLE_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getSunriseHour() {
        try {
            return (Real) getComponent(SUNRISE_HOUR_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getSunsetHour() {
        try {
            return (Real) getComponent(SUNSET_HOUR_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getSunTransitHour() {
        try {
            return (Real) getComponent(SUNTRANSIT_HOUR_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Real getZoneOffsetHour() {
        try {
            return (Real) getComponent(ZONE_OFFSET_HOUR_INDEX);
        } catch (VisADException | RemoteException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
