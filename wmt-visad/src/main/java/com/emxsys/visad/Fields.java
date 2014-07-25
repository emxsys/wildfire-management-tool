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
import java.util.Collection;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.Field;
import visad.FunctionType;
import visad.MathType;
import visad.RealTupleType;
import visad.VisADException;
import visad.georef.LatLonPoint;
import visad.util.DataUtility;

/**
 * Utility class for interacting with Field objects.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages(
        {
            "# {0} - Argument name",
            "Fields_NullArgument=The {0} argument cannot be null."
        }
)
public class Fields {

    private Fields() {
    }

    /**
     * Checks whether the field argument has a spatiotemporal (space-time) domain.
     * @param field The Field to check.
     * @return True if the field's domain is either: <pre>((time) -> (space))</pre> or <pre>((space) -> (time))</pre>
     */
    public static boolean checkSpatioTemporalDomain(Field field) {
        if (field == null) {
            throw new IllegalArgumentException(Bundle.Fields_NullArgument("field"));
        }
        try {
            MathType domainType = DataUtility.getDomainType(field);
            if (domainType.equals(RealTupleType.Time1DTuple)) {
                MathType rangeType = DataUtility.getRangeType(field);
                if (rangeType instanceof FunctionType) {
                    if (((FunctionType) rangeType).getDomain().equals(RealTupleType.LatitudeLongitudeTuple)) {
                        return true;
                    }
                }
            }
            else if (domainType.equals(RealTupleType.LatitudeLongitudeTuple)) {
                MathType rangeType = DataUtility.getRangeType(field);
                if (rangeType instanceof FunctionType) {
                    if (((FunctionType) rangeType).getDomain().equals(RealTupleType.Time1DTuple)) {
                        return true;
                    }
                }
            }
            return false;
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }
    
    public static float[][] createLatLonSamples(Collection<? extends LatLonPoint> points) {
        float[][] latLonSamples = new float[2][points.size()];
        int xy = 0;
        for (LatLonPoint latLonPoint : points) {
            latLonSamples[0][xy] = (float) latLonPoint.getLatitude().getValue();
            latLonSamples[1][xy] = (float) latLonPoint.getLongitude().getValue();
            ++xy;
        }
        return latLonSamples;
    }
}
