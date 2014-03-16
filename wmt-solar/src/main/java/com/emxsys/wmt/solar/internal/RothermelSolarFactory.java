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
package com.emxsys.wmt.solar.internal;

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.gis.api.GeoSector;
import com.emxsys.wmt.gis.api.Latitude;
import com.emxsys.wmt.solar.api.SolarUtil;
import com.emxsys.wmt.solar.api.Sunlight;
import com.emxsys.wmt.solar.api.SunlightHoursTuple;
import com.emxsys.wmt.solar.api.SolarType;
import com.emxsys.wmt.solar.api.SunlightHours;
import com.emxsys.wmt.solar.api.SunlightTuple;
import com.emxsys.wmt.solar.spi.DefaultSunlightProvider;
import com.emxsys.wmt.visad.Reals;
import com.emxsys.wmt.visad.Times;
import static com.emxsys.wmt.solar.internal.RothermelSupport.*;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

import visad.*;

/**
 * A reference to this object can be obtained via SunlightFactory.getInstance();
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: RothermelSunlightFactory.java 675 2013-05-24 20:05:05Z bdschubert $
 */
public class RothermelSolarFactory extends DefaultSunlightProvider {

    private static final Logger logger = Logger.getLogger(RothermelSolarFactory.class.getName());

    public RothermelSolarFactory() {
    }

    @Override
    public Coord3D getSunPosition(Date utcTime) {
        return SolarUtil.getSunPosition(utcTime);
    }

    /**
     * Creates a new SunlightTuple object from VisAD type
     *
     * @param utcTime determines sun position
     * @return SunlightTuple
     */
    @Override
    public Sunlight getSunlight(Date utcTime) {
        GeoCoord3D sunPosition = SolarUtil.getSunPosition(utcTime);
        return new SunlightTuple(sunPosition.getLatitude(), sunPosition.getLongitude());
    }

    /**
     * Creates a new SunlightTuple object from VisAD type
     *
     * @param latitude location on the earth [Degrees]
     * @param date determines declination angle, and sunrise/sunset times.
     * @return SunlightTuple
     */
    @Override
    public SunlightHours getSunlightHours(Real latitude, Date date) {
        Real declination = calcSolarDeclinationAngle(date);
        DateTime[] times;
        Real sunrise;
        Real sunset;
        try {
            times = calcSunriseSunset(latitude, declination, date);
            sunrise = Reals.convertTo(RealType.Time, times[0]);
            sunset = Reals.convertTo(RealType.Time, times[1]);
        } catch (Exception ex) {
            Logger.getLogger(SunlightHoursTuple.class.getName()).log(Level.SEVERE, null, ex);
            sunrise = new Real(SolarType.TIME); // missing value
            sunset = new Real(SolarType.TIME);  // missing value
        }
        return new SunlightHoursTuple(sunrise, sunset);
    }

    /**
     * Function Type: (( date, latitude ) -> ( declination, sunrise, sunset ))
     *
     * @param timeDomain
     * @param latitude1
     * @param latitude2
     * @return A 2x2 FlatField containing
     */
    @Override
    public FlatField makeSolarData(Gridded1DSet timeDomain, Real latitude1, Real latitude2) {
        try {
            // Define our function type: solar data = function(latitude, date)
            RealTupleType domainType
                    = new RealTupleType(RealType.Time, RealType.Latitude);
            RealTupleType rangeType
                    = new RealTupleType(SolarType.DECLINATION, SolarType.SUNRISE_HOUR, SolarType.SUNSET_HOUR);
            FunctionType functionType = new FunctionType(domainType, rangeType);

            // Create the domain ( latitude, date )
            double startTime = timeDomain.getLowX();
            double endTime = timeDomain.getHiX();
            double startLat = latitude1.getValue(CommonUnit.degree);
            double endLat = latitude2.getValue(CommonUnit.degree);
            Linear2DSet domainSet
                    = new Linear2DSet(domainType,
                            startTime, endTime, 2,
                            startLat, endLat, 2);

            // Populate the range ( declination, sunrise, sunset )
            double[][] domainSamples = domainSet.getDoubles();
            double[][] rangeSamples = new double[3][domainSet.getLength()];
            for (int i = 0; i < domainSet.getLength(); i++) {
                DateTime time = new DateTime(domainSamples[0][i]);
                Real lat = Latitude.fromDegrees(domainSamples[1][i]);

                Real declination = calcSolarDeclinationAngle(Times.toDate(time));
                Real sunrise = new Real(SolarType.SUNRISE_HOUR, calcSunriseHour(lat, declination));
                Real sunset = new Real(SolarType.SUNSET_HOUR, calcSunsetHour(lat, declination));

                // TODO: add LocalHourAngle, SolarAltitudeAngle, SolarAzimuthAngle to the solar data tuple
//                // Latitude [radians]
//                double phi = lat.getValue(CommonUnit.radian);
//                // Declination [radialans]
//                double delta = declination.getValue(CommonUnit.radian);
//                // Local time
//                double hour = Times.toClockTime(time);
//                double lha = calcLocalHourAngle(hour);
//                double A = calcSolarAltitudeAngle(lha, phi, delta);
//                double Z = calcSolarAzimuthAngle(lha, phi, delta, A);
                rangeSamples[0][i] = declination.getValue();
                rangeSamples[1][i] = sunrise.getValue();
                rangeSamples[2][i] = sunset.getValue();
            }
            // Create and return the field
            FlatField field = new FlatField(functionType, domainSet);
            field.setSamples(rangeSamples);

            return field;
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Function Type: (( date, latitude ) -> ( declination, sunrise, sunset ))
     *
     * @param timeDomain
     * @param sector
     * @return A 2x2 FlatField containing
     */
    @Override
    public FlatField makeSolarData(Gridded1DSet timeDomain, GeoSector sector) {
        return makeSolarData(
                timeDomain, sector.getSouthwest().getLatitude(), sector.getNortheast().getLatitude());
    }

}
