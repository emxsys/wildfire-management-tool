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
package com.emxsys.visad;

import java.rmi.RemoteException;
import org.openide.util.Exceptions;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

/**
 * Utility class for creating and interacting with RealTuples.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @see RealTuple
 */
public class Tuples {

    private Tuples() {
    }

    /**
     * Returns the index of the first component of the specified type.
     *
     * @param type specification to look for
     * @param tuple array to be searched
     * @return Index of the first matching type, or -1 if not found.
     */
    public static int getIndex(MathType type, RealTuple tuple) {
        return getIndex(type, (RealTupleType) tuple.getType());
    }

    /**
     * Returns the index of the first component of the specified type.
     *
     * @param type specification to look for.
     * @param tupleType the RealTupleType to be searched.
     * @return Index of the first matching type, or -1 if not found.
     */
    public static int getIndex(MathType type, RealTupleType tupleType) {
        return tupleType.getIndex(type);
    }

    /**
     * Returns the first component of specified type.
     *
     * @param type Specified type
     * @param tuple Tuple to search
     * @return Real component matching the type; throws if not found.
     */
    public static Real getComponent(RealType type, RealTuple tuple) {
        int index = getIndex(type, tuple);
        if (index < 0) {
            throw new IllegalArgumentException("type not found in tuple");
        }
        try {
            return (Real) tuple.getComponent(index);
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    public static RealTuple fromReal(Real a, Real b) {
        try {
            return new RealTuple(
                    new Real[]{
                        a, b
                    });
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    public static RealTuple fromReal(Real a, Real b, Real c) {
        try {
            return new RealTuple(
                    new Real[]{
                        a, b, c
                    });
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    public static double[][] makeSamples(RealTuple tuple) {
        double[][] samples = new double[tuple.getDimension()][1];
        double[] values = tuple.getValues();
        for (int dim = 0; dim < tuple.getDimension(); dim++) {
            samples[dim][0] = values[dim];
        }
        return samples;
    }
}
