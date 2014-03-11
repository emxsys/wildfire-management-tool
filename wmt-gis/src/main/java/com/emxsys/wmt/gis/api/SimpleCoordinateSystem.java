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
package com.emxsys.wmt.gis.api;

import org.openide.util.Exceptions;
import visad.CommonUnit;
import visad.CoordinateSystem;
import visad.CoordinateSystemException;
import visad.RealTupleType;
import visad.RealType;
import visad.Unit;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SimpleCoordinateSystem extends CoordinateSystem {

    private static final RealType[] components
            = {
                RealType.getRealType("Lat", CommonUnit.degree),
                RealType.getRealType("Lon", CommonUnit.degree)
            };
    public static final RealTupleType SimpleLatLonTuple;

    static {
        try {
            SimpleLatLonTuple = new RealTupleType(components);
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
     * Create a NavigationCoordinateSystem that just returns
     * the input tuple.
     *
     * @param reference reference RealTupleType
     *
     * @throws VisADException reference does not contain Latitude/Longitude
     * or couldn't create the necessary VisAD object
     */
    public SimpleCoordinateSystem()
            throws VisADException {
        super(SimpleCoordinateSystem.SimpleLatLonTuple, SimpleCoordinateSystem.SimpleLatLonTuple.getDefaultUnits());
    }

    /**
     * Transform to the reference coordinates
     *
     * @param tuple array of values
     * @return input array
     *
     * @throws VisADException tuple is null or wrong dimension
     */
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
    public double[][] fromReference(double[][] refTuple)
            throws VisADException {
        if (refTuple == null || getDimension() != refTuple.length) {
            throw new VisADException(
                    "Values are null or wrong dimension");
        }
        return refTuple;
    }

    /**
     * See if the object in question is equal to this CoordinateSystem.
     * The two objects are equal if they are the same object or if they
     * are both TrivialNavigations and have the same dimension.
     *
     * @param cs Object in question
     * @return true if they are considered equal, otherwise false.
     */
    public boolean equals(Object cs) {
        if ((cs instanceof SimpleCoordinateSystem
                && ((SimpleCoordinateSystem) cs).getDimension() == getDimension())
                || cs == this) {
            return true;
        }
        else {
            return false;
        }
    }
}
