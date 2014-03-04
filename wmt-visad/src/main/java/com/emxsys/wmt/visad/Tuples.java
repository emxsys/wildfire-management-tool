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

import org.openide.util.Exceptions;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;

/**
 * Utility class for creating and interacting with RealTuples.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: Tuples.java 534 2013-04-18 15:26:05Z bdschubert $
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
        RealTupleType tupleType = (RealTupleType) tuple.getType();
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
        catch (Exception ex) {
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
        catch (Exception ex) {
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
        catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }
}
