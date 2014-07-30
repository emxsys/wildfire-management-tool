/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
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
package com.emxsys.visad;

import java.rmi.RemoteException;
import java.util.Objects;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.CoordinateSystem;
import visad.Field;
import visad.FlatField;
import visad.FunctionType;
import visad.RealTuple;
import visad.Set;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;
import visad.georef.LatLonPoint;

/**
 *
 * @author Bruce Schubert
 */
@Messages({
    "# {0} - method name",
    "# {1} - argument class or name",
    "SpatialField_NullArgument=SpatialField.{0}: {1} null argument not permitted.",
    "# {0} - method name",
    "# {1} - inequallity example",
    "SpatialField_InvalidArrayLength=SpatialField.{0}: invalid array length - {1}",}
)
public class SpatialField {

    /**
     * Creates a SpatialField for a single point.
     *
     * @param point The point for the spatial domain.
     * @param tuple The tuple used for the range values.
     * @return A new SpatialField with a FlatField representing the function: ((lat/lon) ->
     * (range)).
     */
    public static SpatialField from(LatLonPoint point, RealTuple tuple) {
        SpatialDomain domain = SpatialDomain.from(point);
        TupleType rangeType = (TupleType) tuple.getType();
        double[] values = tuple.getValues();
        double[][] range = new double[tuple.getLength()][1];
        for (int i = 0; i < values.length; i++) {
            range[i][0] = values[i];
        }
        return from(domain, rangeType, range);
    }

    /**
     * Creates a SpatialField from a SpatialDomain and range of double values. The default range
     * implementation will be floats unless the TupleType component types have Sets declared.
     *
     * @param domain Two-dimensional domain of type {@code RealTypeTuple.LatitudeLongitudeTuple}.
     * @param rangeType The range MathType.
     * @param rangeValues The range values - array dimensions: [range dimension][domain length]
     * @return A new SpatialField with a FlatField representing the function: ((lat/lon) ->
     * (range)).
     */
    public static SpatialField from(SpatialDomain domain, TupleType rangeType, double[][] rangeValues) {
//        try {
//            // Override the default float range implemantion with doubles
//            MathType[] mathTypes = rangeType.getComponents();
//            Set[] rangeSets = new Set[mathTypes.length];
//            for (int i = 0; i < mathTypes.length; i++) {
//                rangeSets[i] = new DoubleSet(mathTypes[i]);
//            }
//            return from(domain, rangeType, null, rangeValues);
//        }
//        catch (VisADException ex) {
//            Exceptions.printStackTrace(ex);
//            throw new RuntimeException(ex);
//        }
        return from(domain, rangeType, null, rangeValues);
    }

    /**
     * Creates a SpatialField from a SpatialDomain and range of double values.
     *
     * @param domain Two-dimensional domain of type {@code RealTypeTuple.LatitudeLongitudeTuple}.
     * @param rangeType The range MathType.
     * @param rangeSets The Sets (e.g., FloatSet, DoubleSet, etc.) to use for the range values; may be null.
     * @param rangeValues The range values - array dimensions: [range dimension][domain length]
     * @return A new SpatialField with a FlatField representing the function: ((lat/lon) ->
     * (range)).
     */
    public static SpatialField from(SpatialDomain domain, TupleType rangeType, Set[] rangeSets, double[][] rangeValues) {
        if (domain == null) {
            throw new IllegalArgumentException(Bundle.SpatialField_NullArgument("from", SpatialDomain.class));
        }
        else if (rangeType == null) {
            throw new IllegalArgumentException(Bundle.SpatialField_NullArgument("from", TupleType.class));
        }
        else if (rangeSets != null && rangeSets.length != rangeType.getDimension()) {
            throw new IllegalArgumentException(Bundle.SpatialField_InvalidArrayLength("from",
                    "rangeSets.length (" + rangeSets.length + ") != rangeType dimension (" + rangeType.getDimension() + ")"));
        }
        else if (rangeValues == null) {
            throw new IllegalArgumentException(Bundle.SpatialField_NullArgument("from", "rangeValues"));
        }
        else if (rangeValues.length != rangeType.getDimension()) {
            throw new IllegalArgumentException(Bundle.SpatialField_InvalidArrayLength("from",
                    "rangeValues.length (" + rangeValues.length + ") != rangeType dimension (" + rangeType.getDimension() + ")"));
        }
        else if (rangeValues[0].length != domain.getDomainSetLength()) {
            throw new IllegalArgumentException(Bundle.SpatialField_InvalidArrayLength("from",
                    "rangeValues[0].length (" + rangeValues[0].length + ") != domain set length (" + domain.getDomainSetLength() + ")"));
        }
        try {
            FunctionType functionType = new FunctionType(domain.getSpatialDomainType(), rangeType);
            FlatField field = new FlatField(functionType, domain.getDomainSet(), (CoordinateSystem) null, rangeSets, (Unit[]) null);
            field.setSamples(rangeValues);
            return new SpatialField(field);
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex);
        }
    }

    private final Field field;

    public SpatialField(Field field) {
        this.field = field;
    }

    public final Field getField() {
        return field;
    }

    @Override
    public String toString() {
        return "SpatialField{" + "field=" + field + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.field);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SpatialField other = (SpatialField) obj;
        if (!Objects.equals(this.field, other.field)) {
            return false;
        }
        return true;
    }

}
