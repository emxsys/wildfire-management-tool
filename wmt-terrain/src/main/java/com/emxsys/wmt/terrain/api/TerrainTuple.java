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
package com.emxsys.wmt.terrain.api;

import static com.emxsys.wmt.gis.api.GisType.*;
import com.emxsys.wmt.visad.Reals;
import java.rmi.RemoteException;
import visad.Data;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: TerrainTuple.java 673 2013-05-24 20:02:32Z bdschubert $
 */
public class TerrainTuple extends RealTuple implements Terrain {

    public static final TerrainTuple INVALID_TERRAIN = new TerrainTuple();
    public static final int ASPECT_INDEX = 0;
    public static final int SLOPE_INDEX = 1;
    public static final int ELEVATION_INDEX = 2;
    private Real aspect;
    private Real slope;
    private Real elevation;
    private Data[] components;

    /**
     * Construct a TerrainTuple object from doubles.
     *
     * @param aspect [degrees]
     * @param slope [degrees]
     */
    public TerrainTuple(double aspect, double slope, double elevation) {
        this(new Real(ASPECT, aspect),
                new Real(SLOPE, slope),
                new Real(ELEVATION, elevation));
    }

    /**
     * Construct a TerrainTuple object from Reals.
     *
     * Guarrenties that input parameter values are converted to the member's
     * defined RealTypes.
     *
     * @param aspect must be compatible with TerrainType.ASPECT
     * @param slope must be compatible with TerrainType.SLOPE
     */
    public TerrainTuple(Real aspect, Real slope, Real elevation) {
        super(TERRAIN);
        this.aspect = Reals.convertTo(ASPECT, aspect);
        this.slope = Reals.convertTo(SLOPE, slope);
        this.elevation = Reals.convertTo(ELEVATION, elevation);
    }

    /**
     * Construct a TerrainTuple object from a RealTuple (as returned from a FlatField).
     */
    public TerrainTuple(RealTuple tuple) throws VisADException, RemoteException {
        super(TERRAIN);
        if (tuple.getType() != TERRAIN) {
            throw new IllegalArgumentException("TerrainTuple constructor cannot accept " + tuple.getType() + " types.");
        }
        this.aspect = (Real) tuple.getComponent(ASPECT_INDEX);
        this.slope = (Real) tuple.getComponent(SLOPE_INDEX);
        this.elevation = (Real) tuple.getComponent(ELEVATION_INDEX);
    }

    /**
     * Construct a TerrainTuple object with "missing" values.
     *
     */
    public TerrainTuple() {
        super(TERRAIN);
        this.aspect = new Real(ASPECT);
        this.slope = new Real(SLOPE);
        this.elevation = new Real(ELEVATION);
    }

    /**
     * Aspect is direction that non-flat terrain faces.
     * It is the down-slope direction relative to true north.
     *
     * @return [degrees]
     */
    @Override
    public Real getAspect() {
        return this.aspect;
    }

    /**
     * Slope is the steepness of the terrain.
     * Zero slope is horizontal, 90 degrees is vertical.
     *
     * @return [degrees]
     */
    @Override
    public Real getSlope() {
        return this.slope;
    }

    /**
     * Elevation is the hieght of the terrain.
     *
     * @return [meters]
     */
    @Override
    public Real getElevation() {
        return this.elevation;
    }

    /**
     * Is missing any data elements?
     *
     * @return is missing
     */
    @Override
    public boolean isMissing() {
        return aspect.isMissing() || slope.isMissing() || elevation.isMissing();
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
    public Data getComponent(int i) throws VisADException, RemoteException {
        switch (i) {
            case ASPECT_INDEX:
                return aspect;
            case SLOPE_INDEX:
                return slope;
            case ELEVATION_INDEX:
                return elevation;
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
    public Data[] getComponents(boolean copy) {
        //Create the array and populate it if needed
        if (components == null) {
            Data[] tmp = new Data[getDimension()];
            tmp[ASPECT_INDEX] = aspect;
            tmp[SLOPE_INDEX] = slope;
            tmp[ELEVATION_INDEX] = elevation;
            components = tmp;
        }
        return components;
    }

    /**
     * Indicates if this Tuple is identical to an object.
     *
     * @param obj The object.
     * @return            <code>true</code> if and only if the object is
     * a Tuple and both Tuple-s have identical component
     * sequences.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TerrainTuple)) {
            return false;
        }
        TerrainTuple that = (TerrainTuple) obj;
        return this.aspect.equals(that.aspect)
                && this.slope.equals(that.slope)
                && this.elevation.equals(that.elevation);
    }

    /**
     * Returns the hash code of this object.
     *
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        return aspect.hashCode() | slope.hashCode() & elevation.hashCode();
    }

    /**
     * to string
     *
     * @return string of me
     */
    @Override
    public String toString() {
        return getAspect().toValueString() + ", " + getSlope().toValueString() + ", " + getElevation().toValueString();
    }
}
