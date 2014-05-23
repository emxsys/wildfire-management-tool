/*
 * Copyright (c) 2014, Bruce Schubert
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
package com.emxsys.solar.internal;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.solar.api.SolarType;
import com.emxsys.solar.api.SunlightProvider;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.openide.util.Exceptions;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class SPASunlightProvider implements SunlightProvider {


    @Override
    public RealTuple getSunPosition(ZonedDateTime time, Coord3D observer) {
        SolarData spa = new SolarData(time, observer);
        SolarPositionAlgorithms.spa_calculate(spa);

        try {
            return new RealTuple(SolarType.SUN_POSITION,
                    new Real[]{
                        new Real(SolarType.LATITUDE, spa.delta_prime),
                        new Real(SolarType.LONGITUDE, spa.lamda),
                        new Real(SolarType.AZIMUTH_ANGLE, spa.azimuth),
                        new Real(SolarType.ZENITH_ANGLE, spa.zenith)}, null);
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public RealTuple getSunlight(ZonedDateTime time, Coord3D observer) {
        SolarData spa = new SolarData(time, observer);
        SolarPositionAlgorithms.spa_calculate(spa);

        try {
            //LATITUDE, LONGITUDE, AZIMUTH_ANGLE, ZENITH_ANGLE, SUNRISE_HOUR, SUNSET_HOUR
//            ZonedDateTime sunrise = ZonedDateTime.of(
//                LocalDate.of(spa.year, spa.month, spa.day),
//                LocalTime.ofSecondOfDay((long) (spa.sunrise * 3600)),
//                ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds((int) (spa.timezone * 3600))));
//            ZonedDateTime sunset = ZonedDateTime.of(
//                LocalDate.of(spa.year, spa.month, spa.day),
//                LocalTime.ofSecondOfDay((long) (spa.sunset * 3600)),
//                ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds((int) (spa.timezone * 3600))));
            
            return new RealTuple(SolarType.SUN_POSITION,
                    new Real[]{
                        new Real(SolarType.LATITUDE, spa.delta_prime),
                        new Real(SolarType.LONGITUDE, spa.lamda),
                        new Real(SolarType.AZIMUTH_ANGLE, spa.azimuth),
                        new Real(SolarType.ZENITH_ANGLE, spa.zenith),
                        new Real(SolarType.SUNRISE_HOUR, spa.sunrise),  // sunrise local time (same offset as input time)
                        new Real(SolarType.SUNSET_HOUR, spa.sunset)
                    }, null);
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

}
