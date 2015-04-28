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
package com.emxsys.gis.api;

import static com.emxsys.gis.api.GisType.*;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.visad.RealXmlAdapter;
import com.emxsys.visad.Reals;
import java.rmi.RemoteException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.Exceptions;
import visad.CommonUnit;
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 * The BasicTerrain is a concrete implementation of the Terrain interface.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@XmlRootElement(name = "terrain")
@XmlType(propOrder = {"aspect", "slope", "elevation"})
public class BasicTerrain implements Terrain {

    public static final BasicTerrain INVALID_TERRAIN = new BasicTerrain();
    public static final int ASPECT_INDEX = 0;
    public static final int SLOPE_INDEX = 1;
    public static final int ELEVATION_INDEX = 2;
    private Real aspect;
    private Real slope;
    private Real elevation;

    /**
     * Construct a TerrainTuple object from doubles.
     * @param aspect [degrees]
     * @param slope [degrees]
     * @param elevation [meters]
     */
    public BasicTerrain(double aspect, double slope, double elevation) {
        this(new Real(ASPECT, aspect),
                new Real(SLOPE, slope),
                new Real(ELEVATION, elevation));
    }

    /**
     * Construct a TerrainTuple object from Reals. Guarrantees that input parameter values are
     * converted to the member's defined RealTypes.
     * @param aspect must be compatible with GisType.ASPECT
     * @param slope must be compatible with GisType.SLOPE
     * @param elevation must be compatible with GisType.ELEVATION
     */
    public BasicTerrain(Real aspect, Real slope, Real elevation) {
        this.aspect = Reals.convertTo(ASPECT, aspect);
        this.slope = Reals.convertTo(SLOPE, slope);
        this.elevation = Reals.convertTo(ELEVATION, elevation);
    }

    /**
     * Construct a TerrainTuple object from a RealTuple (as returned from a FlatField).
     * @param tuple [aspect,slope,elevation]
     * @throws visad.VisADException
     * @throws java.rmi.RemoteException
     */
    public BasicTerrain(RealTuple tuple) throws VisADException, RemoteException {
        if (tuple.getType() != TERRAIN) {
            throw new IllegalArgumentException("TerrainTuple constructor cannot accept " + tuple.getType() + " types.");
        }
        this.aspect = (Real) tuple.getComponent(ASPECT_INDEX);
        this.slope = (Real) tuple.getComponent(SLOPE_INDEX);
        this.elevation = (Real) tuple.getComponent(ELEVATION_INDEX);
    }

    /**
     * Construct a TerrainTuple object with "missing" values. Used for "Invalid" objects.
     */
    public BasicTerrain() {
        this.aspect = new Real(ASPECT);
        this.slope = new Real(SLOPE);
        this.elevation = new Real(ELEVATION);
    }

    /**
     *
     * @return A TERRAIN RealTuple.
     */
    public RealTuple getTuple() {
        try {
            return new RealTuple(TERRAIN, new Real[]{aspect, slope, elevation}, null);
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            return new RealTuple(TERRAIN);
        }
    }

    /**
     * Aspect is direction that non-flat terrain faces. It is the down-slope direction relative to
     * true north.
     * @return [degrees]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    @Override
    public Real getAspect() {
        return this.aspect;
    }

    public void setAspect(Real aspect) {
        this.aspect = aspect;
    }

    /**
     * Slope is the steepness of the terrain. Zero slope is horizontal, 90 degrees is vertical.
     * @return [degrees]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    @Override
    public Real getSlope() {
        return this.slope;
    }

    public void setSlope(Real slope) {
        this.slope = slope;
    }

    /**
     * Elevation is the height of the terrain.
     * @return [meters]
     */
    @XmlElement
    @XmlJavaTypeAdapter(RealXmlAdapter.class)
    @Override
    public Real getElevation() {
        return this.elevation;
    }

    public void setElevation(Real elevation) {
        this.elevation = elevation;
    }

    /**
     * Is missing any data elements?
     * @return is missing
     */
    public boolean isMissing() {
        return aspect.isMissing() || slope.isMissing() || elevation.isMissing();
    }

    /**
     * Indicates if this Tuple is identical to an object.
     * @param obj The object.
     * @return true if and only if the object is a Tuple and both Tuple-s have identical component
     * sequences.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BasicTerrain)) {
            return false;
        }
        BasicTerrain that = (BasicTerrain) obj;
        return this.aspect.equals(that.aspect)
                && this.slope.equals(that.slope)
                && this.elevation.equals(that.elevation);
    }

    /**
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        return aspect.hashCode() | slope.hashCode() & elevation.hashCode();
    }

    /**
     * Aspect, slope, terrain values in a string.
     * @return string of me
     */
    @Override
    public String toString() {
        return getAspect().toValueString() + ", " + getSlope().toValueString() + ", " + getElevation().toValueString();
    }

    @Override
    public double getAspectDegrees() {
        return aspect.getValue();
    }

    @Override
    public CardinalPoint8 getAspectCardinalPoint8() {
        return CardinalPoint8.fromDegrees(getAspectDegrees());
    }

    @Override
    public double getSlopeDegrees() {
        return slope.getValue();
    }

    @Override
    public double getSlopePercent() {
        long slopePct = 0;
        if (getSlopeDegrees() < 90) {
            try {
                slopePct = Math.round(Math.tan(slope.getValue(CommonUnit.radian)) * 100);
            }
            catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return slopePct;
    }

    @Override
    public double getElevationMeters() {
        return elevation.getValue();
    }

    @Override
    public double getElevationFeet() {
        try {
            return elevation.getValue(GeneralUnit.foot);
        }
        catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            return Double.NaN;
        }
    }

}
