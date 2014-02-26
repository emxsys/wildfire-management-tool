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
package com.emxsys.wmt.visad;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import visad.CommonUnit;
import visad.Real;
import visad.RealTupleType;
import visad.RealType;
import visad.Unit;
import visad.VisADException;


/**
 * A utility class for creating common Real objects.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: Reals.java 534 2013-04-18 15:26:05Z bdschubert $
 * @see Tuples
 */
public class Reals
{

    private static final Logger logger = Logger.getLogger(Reals.class.getName());


    private Reals()
    {
    }


    /**
     * Converts a Real to the specified RealType.
     *
     * @param newType is the specified RealType to be returned
     * @param value is the existing Real value to convert
     * @return a Real of the specified type with a converted value, throws if the value cannot be
     * converted.
     */
    public static Real convertTo(RealType newType, final Real value)
    {
        try
        {
            if (value.getType().equals(newType))
            {
                return value;
            }
            else
            {
                return new Real(newType, value.getValue(newType.getDefaultUnit()));
            }
        }
        catch (VisADException ex)
        {
            Logger.getLogger(Reals.class.getName()).log(Level.SEVERE,
                "Real " + value.toString()
                + "cannot be converted to RealType " + newType.toString(), ex);
            Exceptions.printStackTrace(ex);
            throw new IllegalArgumentException(ex);
        }
    }


    /**
     * Creates a new RealTypleType - used by static initializers to catch exceptions.
     *
     * @param types an array of RealTypes that comprise the tuple.
     * @return a new RealTupleType defined by the supplied types.
     */
    public static RealTupleType newRealTupleType(RealType[] types)
    {
        try
        {
            return new RealTupleType(types);
        }
        catch (VisADException ex)
        {
            Logger.getLogger(Reals.class.getName()).log(Level.SEVERE, null, ex);
            Exceptions.printStackTrace(ex);
            return null;
        }
    }


    /**
     * Creates a new Real of the specified type and unit of measure.
     *
     * @param type for example RealType.Altitude
     * @param value the value to be represented by the Real
     * @param unit the unit of measure for the value, for example CommonUnit.meter
     * @return a new Real with the given RealType and Unit.
     */
    public static Real newInstance(RealType type, double value, Unit unit)
    {
        try
        {
            return new Real(type, value, unit);
        }
        catch (VisADException ex)
        {
            logger.log(Level.SEVERE, "Double " + Double.toString(value)
                + "cannot be converted to RealType " + type.toString(), ex);
            throw new IllegalArgumentException(ex);
        }

    }


    /**
     * Creates a new Real of RealType.Altitude in meters.
     *
     * @param value altitude in meters.
     * @return a RealType.Altitude
     */
    public static Real newAltitude(double value)
    {
        return newAltitude(value, CommonUnit.meter);
    }


    /**
     * Creates a new Real of RealType.Altitude
     *
     * @param value altitude
     * @param unit unit of measure, e.g., CommonUnit.meter
     * @return a RealType.Altitude
     */
    public static Real newAltitude(double value, Unit unit)
    {
        return newInstance(RealType.Altitude, value, unit);
    }


    /**
     * Creates a new Real of RealType.Latitude in degrees.
     *
     * @param value latitude in degrees.
     * @return a RealType.Latitude
     */
    public static Real newLatitude(double value)
    {
        return newLatitude(value, CommonUnit.degree);
    }


    /**
     * Creates a new Real of RealType.Latitude.
     *
     * @param value latitude
     * @param unit unit of measure, e.g., CommonUnit.degree or CommonUnit.radian
     * @return a RealType.Latitude
     */
    public static Real newLatitude(double value, Unit unit)
    {
        return newInstance(RealType.Latitude, value, unit);
    }


    /**
     * Creates a new Real of RealType.Longitude in degrees.
     *
     * @param value longitude in degrees.
     * @return a RealType.Longitude
     */
    public static Real newLongitude(double value)
    {
        return newLongitude(value, CommonUnit.degree);
    }


    /**
     * Creates a new Real of RealType.Longitude.
     *
     * @param value longitude
     * @param unit unit of measure, e.g., CommonUnit.degree or CommonUnit.radian
     * @return a RealType.Longitude
     */
    public static Real newLongitude(double value, Unit unit)
    {
        return newInstance(RealType.Longitude, value, unit);
    }
}
