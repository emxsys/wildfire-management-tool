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
package com.emxsys.wmt.gis;

import org.openide.util.Exceptions;
import visad.CoordinateSystem;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

/**
 * A Simple3DCooridinateSystem for GeoCoord2D and GeoCoord3D.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @see GeoCoord2D
 * @see GeoCoord3D
 */
public class Simple3DCoordinateSystem extends CoordinateSystem {

    private static final RealType[] components
            = {
                // Using system intrisic types to ensure max campatibility with VisAD library functions
                RealType.Latitude,
                RealType.Longitude,
                RealType.Altitude, //        RealType.getRealType("Lat", CommonUnit.degree),
            //        RealType.getRealType("Lon", CommonUnit.degree),
            //        RealType.getRealType("Alt", CommonUnit.meter),
            };
    public static final RealTupleType SimpleLatLonAltTuple;

    static {
        try {
            SimpleLatLonAltTuple = new RealTupleType(components);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the index of RealType.Latitude in the reference RealTupleType.
     *
     * @return index of RealType.Latitude in the reference
     */
    public int getLatitudeIndex() {
        return 0;
    }

    /**
     * Get the index of RealType.Longitude in the reference RealTupleType.
     *
     * @return index of RealType.Longitude in the reference
     */
    public int getLongitudeIndex() {
        return 1;
    }

    /**
     * Get the index of RealType.Altitude in the reference RealTupleType.
     *
     * @return index of RealType.Altitude in the reference
     */
    public int getAltitudeIndex() {
        return 2;
    }

    /**
     * Create a Simple3DCoordinateSystem that just returns the input tuple.
     *
     * @param reference reference RealTupleType
     *
     * @throws VisADException reference does not contain Latitude/Longitude or couldn't create the
     * necessary VisAD object
     */
    public Simple3DCoordinateSystem()
            throws VisADException {
        super(Simple3DCoordinateSystem.SimpleLatLonAltTuple, Simple3DCoordinateSystem.SimpleLatLonAltTuple.getDefaultUnits());
    }

    /**
     * Transform to the reference coordinates
     *
     * @param tuple array of values
     * @return input array
     *
     * @throws VisADException tuple is null or wrong dimension
     */
    @Override
    public double[][] toReference(double[][] tuple)
            throws VisADException {
        if (tuple == null || getDimension() != tuple.length) {
            throw new VisADException(
                    "Values are null or wrong dimension");
        }
        return tuple;
    }

    /**
     * Transform from the reference coordinates
     *
     * @param refTuple array of values
     * @return input array
     *
     * @throws VisADException tuple is null or wrong dimension
     */
    @Override
    public double[][] fromReference(double[][] refTuple)
            throws VisADException {
        if (refTuple == null || getDimension() != refTuple.length) {
            throw new VisADException(
                    "Values are null or wrong dimension");
        }
        return refTuple;
    }

    /**
     * See if the object in question is equal to this CoordinateSystem. The two objects are equal if
     * they are the same object or if they are both TrivialNavigations and have the same dimension.
     *
     * @param cs Object in question
     * @return true if they are considered equal, otherwise false.
     */
    @Override
    public boolean equals(Object cs) {
        if ((cs instanceof Simple3DCoordinateSystem
                && ((Simple3DCoordinateSystem) cs).getDimension() == getDimension())
                || cs == this) {
            return true;
        }
        else {
            return false;
        }
    }
}
