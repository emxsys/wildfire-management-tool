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
package com.emxsys.wmt.wildfire.api;

import static com.emxsys.wmt.visad.Reals.*;
import static com.emxsys.wmt.wildfire.api.WildfireType.*;
import java.rmi.RemoteException;
import visad.Data;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;


/**
 * This provides a lightweight FireBehavior instance that assumes SI units for fire line intensity,
 * flame length, rate of spread and direction of max spread.
 *
 * The fire behavior data model in VisAD can be represented by a FunctionType:
 *
 * (lat,lon) -> (fli, fl, ros, dir, hpa)
 *
 * Where the domain is (lat,lon) and the range is (fli, fl, ros, dir, hpa).
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: FireBehaviorTuple.java 709 2013-05-31 03:17:13Z bdschubert $
 */
public class FireBehaviorTuple extends RealTuple implements FireBehavior
{
    public static final FireBehaviorTuple INVALID_TUPLE = new FireBehaviorTuple();
    
    private Real fireLineIntensity;
    private Real flameLength;
    private Real rateOfSpread;
    private Real dirOfMaxSpread;
    private Real heatRelease;
    /**
     * Holds the components as we create them
     */
    private Data[] components;


    /**
     * Constructs an instance with missing values.
     */
    public FireBehaviorTuple()
    {
        this(new Real(FIRE_LINE_INTENSITY_SI),
            new Real(FLAME_LENGTH_SI),
            new Real(RATE_OF_SPREAD_SI),
            new Real(DIR_OF_SPREAD),
            new Real(HEAT_RELEASE_SI));
    }


    /**
     * Construct a new instance from doubles using SI units.
     *
     * @param fli fire line intensity [kW/m]
     * @param fl flame length [m]
     * @param ros rate of spread [m/s]
     * @param dir direction of max spread [degrees]
     * @param hpa heat release per unit area [kJ/m2]
     * @throws VisADException
     */
    public FireBehaviorTuple(double fli, double fl, double ros, double dir, double hpa)
        throws VisADException
    {
        this(new Real(FIRE_LINE_INTENSITY_SI, fli),
            new Real(FLAME_LENGTH_SI, fl),
            new Real(RATE_OF_SPREAD_SI, ros),
            new Real(DIR_OF_SPREAD, dir),
            new Real(HEAT_RELEASE_SI, hpa));

    }


    /**
     * Construct a new SurfaceFireBehavior instance from Reals.
     *
     * @param fireLineIntensity fire line intensity [kW/m]
     * @param flameLength flame length [m]
     * @param rateOfSpread rate of spread [m/s]
     * @param dirOfMaxSpread direction of max spread [degrees]
     * @param heatRelase heat release per unit area [kJ/m2]
     */
    public FireBehaviorTuple(Real fireLineIntensity, Real flameLength,
        Real rateOfSpread, Real dirOfMaxSpread, Real heatRelease)
    {
        super(FIRE_BEHAVIOR);
        this.fireLineIntensity = convertTo(FIRE_LINE_INTENSITY_SI, fireLineIntensity);
        this.flameLength = convertTo(FLAME_LENGTH_SI, flameLength);
        this.rateOfSpread = convertTo(RATE_OF_SPREAD_SI, rateOfSpread);
        this.dirOfMaxSpread = convertTo(DIR_OF_SPREAD, dirOfMaxSpread);
        this.heatRelease = convertTo(HEAT_RELEASE_SI, heatRelease);
    }

        /**
     * Construct a new FireBehaviorTuple instance from Reals.
     *
     * @param reals {fireLineIntensity, flameLength, rateOfSpread, dirOfMaxSpread, heatRelase}
     */
    public FireBehaviorTuple(Real[] reals)
    {
        super(FIRE_BEHAVIOR);
        this.fireLineIntensity = convertTo(FIRE_LINE_INTENSITY_SI, reals[0]);
        this.flameLength = convertTo(FLAME_LENGTH_SI, reals[1]);
        this.rateOfSpread = convertTo(RATE_OF_SPREAD_SI, reals[2]);
        this.dirOfMaxSpread = convertTo(DIR_OF_SPREAD, reals[3]);
        this.heatRelease = convertTo(HEAT_RELEASE_SI, reals[4]);
    }


    /**
     * Byram's fire line intensity
     *
     * @return [kW/m]
     */
    @Override
    public visad.Real getFireLineIntensity()
    {
        return this.fireLineIntensity;
    }


    /**
     * Flame length
     *
     * @return [m]
     */
    @Override
    public visad.Real getFlameLength()
    {
        return this.flameLength;
    }


    /**
     * Heat release per unit area
     *
     * @return kJ/m2
     */
    @Override
    public Real getHeatRelease()
    {
        return this.heatRelease;
    }


    /**
     * Rate of spread
     *
     * @return [m/s]
     */
    @Override
    public visad.Real getRateOfSpread()
    {
        return this.rateOfSpread;
    }


    /**
     * Direction of maximum spread
     *
     * @return [degrees]
     */
    @Override
    public Real getDirOfMaxSpread()
    {
        return this.dirOfMaxSpread;
    }


    /**
     * is missing any data elements
     *
     * @return is missing
     */
    @Override
    public boolean isMissing()
    {
        return fireLineIntensity.isMissing() || flameLength.isMissing()
            || rateOfSpread.isMissing() || dirOfMaxSpread.isMissing()
            || heatRelease.isMissing();
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
    public Data getComponent(int i) throws VisADException, RemoteException
    {
        switch (i)
        {
            case 0:
                return fireLineIntensity;
            case 1:
                return flameLength;
            case 2:
                return rateOfSpread;
            case 3:
                return dirOfMaxSpread;
            case 4:
                return heatRelease;
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
    public Data[] getComponents(boolean copy)
    {
        //Create the array and populate it if needed
        if (components == null)
        {
            Data[] tmp = new Data[getDimension()];
            tmp[0] = fireLineIntensity;
            tmp[1] = flameLength;
            tmp[2] = rateOfSpread;
            tmp[3] = dirOfMaxSpread;
            tmp[4] = heatRelease;
            components = tmp;
        }
        return components;
    }


    /**
     * Indicates if this Tuple is identical to an object.
     *
     * @param obj The object.
     * @return            <code>true</code> if and only if the object is a Tuple and both Tuple-s have
     * identical component sequences.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof FireBehaviorTuple))
        {
            return false;
        }

        FireBehaviorTuple that = (FireBehaviorTuple) obj;

        return this.fireLineIntensity.equals(that.fireLineIntensity)
            && this.flameLength.equals(that.flameLength)
            && this.rateOfSpread.equals(that.rateOfSpread)
            && this.dirOfMaxSpread.equals(that.dirOfMaxSpread)
            && this.heatRelease.equals(that.heatRelease);
    }


    /**
     * Returns the hash code of this object.
     *
     * @return The hash code of this object.
     */
    @Override
    public int hashCode()
    {
        return fireLineIntensity.hashCode()
            ^ flameLength.hashCode()
            & rateOfSpread.hashCode()
            | dirOfMaxSpread.hashCode()
            & heatRelease.hashCode();
    }


    /**
     * to string
     *
     * @return string of me
     */
    @Override
    public String toString()
    {
        return "fli: " + getFireLineIntensity().toValueString()
            + ", fln: " + getFlameLength().toValueString()
            + ", ros: " + getRateOfSpread().toValueString()
            + ", dir: " + getDirOfMaxSpread().toValueString()
            + ", hpa: " + getHeatRelease().toValueString();
    }
}
