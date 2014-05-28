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
package com.emxsys.solar.internal;

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.Latitude;
import com.emxsys.solar.api.SolarType;
import com.emxsys.solar.api.SolarUtil;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.solar.api.SunlightHours;
import static com.emxsys.solar.internal.RothermelSupport.*;
import com.emxsys.util.AngleUtil;
import com.emxsys.visad.Reals;
import com.emxsys.visad.Times;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

import visad.*;

/**
 * A reference to this object can be obtained via SunlightProvider.getInstance();
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: RothermelSunlightFactory.java 675 2013-05-24 20:05:05Z bdschubert $
 */
public class RothermelSolarFactory  {

    private static final Logger logger = Logger.getLogger(RothermelSolarFactory.class.getName());

    public RothermelSolarFactory() {
    }


    /**
     * Creates a new Sunlight instance for the given date and location.
     *
     * @param time The local time for the sunlight calculation.
     * @param coord The location for the sunlight calculation.
     * @return A new SunlightTuple for the given date and location.
     */
    public Sunlight getSunlight(ZonedDateTime time, Coord2D coord) {

        try {
            ZonedDateTime utc = time.withZoneSameInstant(ZoneId.of("Z"));
            System.out.println(time);
            System.out.println(utc);
            
            Coord3D sunPosition = SolarUtil.getSunPosition(utc);
            System.out.println("Sun Position: " + sunPosition.toString());
            
            Real declination = RothermelSupport.calcSolarDeclinationAngle(utc.getDayOfYear());

            // calc local hour angle via reference to sun longitude and cur longitude.
            double sunLongitude = sunPosition.getLongitudeDegrees();
            double curLongitude = coord.getLongitudeDegrees();
            double solarHour = AngleUtil.angularDistanceBetween(sunLongitude, curLongitude) / 15; // 15 DEGREES per HOUR
            System.out.println("Solar Hour: " + solarHour);
            // Sun is rising (neg solar hour) if it's longitude is east of the current location.
            if (Math.signum(sunLongitude) >= Math.signum(curLongitude)) {
                solarHour *= curLongitude < sunLongitude ? -1 : 1;
            } else {
                // Special case for handling longitudes crossing the int'l dateline
                solarHour *= sunLongitude < curLongitude ? -1 : 1;
            }
            solarHour += 12; // Noon is 1200 in Rothermel equations.
            System.out.println("Solar Hour: " + solarHour);
            
            double h = RothermelSupport.calcLocalHourAngle(solarHour);              // hour angle [radians]
            double phi = coord.getLatitude().getValue(CommonUnit.radian);           // latitude [radians]
            //double delta = sunPosition.getLatitude().getValue(CommonUnit.radian);   // declination [radians]
            double delta = declination.getValue(CommonUnit.radian);                 // declination [radians]
                    
            double A = RothermelSupport.calcSolarAltitudeAngle(h, phi, delta);      // altitude angle [radians]
            double Z = RothermelSupport.calcSolarAzimuthAngle(h, phi, delta, A);    // azimuth angle [radians]
            
            double t_r = calcSunriseSolarHour(coord.getLatitude(), sunPosition.getLatitude());
            double t_s = 24 - t_r;
  
            throw new UnsupportedOperationException("SunlightTuple is obsolete");
//            return new SunlightTuple(
//                    declination, 
//                    sunPosition.getLongitude(), 
//                    new Real(SolarType.ALTITUDE_ANGLE, Math.toDegrees(A)), 
//                    new Real(SolarType.AZIMUTH_ANGLE, Math.toDegrees(Z) + 90), // adjust for 0600 reference
//                    new Real(SolarType.SUNRISE_HOUR, t_r), 
//                    new Real(SolarType.SUNSET_HOUR, t_s));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    /**
     * Creates a new SunlightTuple object from VisAD type
     *
     * @param latitude location on the earth [Degrees]
     * @param date determines declination angle, and sunrise/sunset times.
     * @return SunlightTuple
     */
    public SunlightHours getSunlightHours(Real latitude, Date date) {
        int julianDay = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("Z")).getDayOfYear();
        Real declination = calcSolarDeclinationAngle(julianDay);
        DateTime[] times;
        Real sunrise;
        Real sunset;
        try {
            times = calcSunriseSunset(latitude, declination, date);
            sunrise = Reals.convertTo(RealType.Time, times[0]);
            sunset = Reals.convertTo(RealType.Time, times[1]);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            sunrise = new Real(SolarType.TIME); // missing value
            sunset = new Real(SolarType.TIME);  // missing value
        }
        throw new UnsupportedOperationException("SunlightTuple is obsolete");
        //return new SunlightHoursTuple(sunrise, sunset);
    }

    /**
     * Function Type: (( date, latitude ) -> ( declination, sunrise, sunset ))
     *
     * @param timeDomain
     * @param latitude1
     * @param latitude2
     * @return A 2x2 FlatField containing
     */
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

                int julian = LocalDateTime.ofInstant(Times.toDate(time).toInstant(),ZoneId.of("Z")).getDayOfYear();
                Real declination = calcSolarDeclinationAngle(julian);
                Real sunrise = new Real(SolarType.SUNRISE_HOUR, calcSunriseSolarHour(lat, declination));
                Real sunset = new Real(SolarType.SUNSET_HOUR, calcSunsetSolarHour(lat, declination));

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
    public FlatField makeSolarData(Gridded1DSet timeDomain, GeoSector sector) {
        return makeSolarData(
                timeDomain, sector.getSouthwest().getLatitude(), sector.getNortheast().getLatitude());
    }

    public RealTuple getSunPosition(ZonedDateTime time, Coord3D observer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
