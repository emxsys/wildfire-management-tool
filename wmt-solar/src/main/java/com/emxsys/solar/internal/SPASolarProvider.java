/*
 * Copyright (c) 2014, bruce 
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
 *     - Neither the name of bruce,  nor the names of its 
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

import com.emxsys.gis.api.Coord2D;
import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.solar.api.SolarType;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.solar.api.SunlightHours;
import com.emxsys.solar.api.SunlightTuple;
import com.emxsys.solar.spi.DefaultSunlightProvider;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.FlatField;
import visad.Gridded1DSet;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author bruce
 */
public class SPASolarProvider extends DefaultSunlightProvider {

    @Override
    public FlatField makeSolarData(Gridded1DSet timeDomain, GeoSector sector) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FlatField makeSolarData(Gridded1DSet timeDomain, Real latitude1, Real latitude2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Coord3D getSunPosition(ZonedDateTime time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Sunlight getSunlight(ZonedDateTime time, Coord2D coord) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SunlightHours getSunlightHours(Real latitude, Date utcTime) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Real getSolarTime(Real longitude, Date utcTime) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

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

}
