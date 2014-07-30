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
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import visad.Field;
import visad.FlatField;
import visad.GriddedSet;
import visad.MathType;
import visad.RealTuple;
import visad.RealTupleType;
import visad.Set;
import visad.TupleType;
import visad.VisADException;
import visad.georef.LatLonPoint;
import visad.georef.LatLonTuple;
import visad.util.DataUtility;

/**
 *
 * @author Bruce Schubert
 */
public class SpatialFieldTest {

    public SpatialFieldTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testFrom_LatLonPoint_RealTuple() throws VisADException, RemoteException {
        System.out.println("from LatLonPoint RealTuple");
        
        LatLonPoint point = new LatLonTuple(34.25, -119.2);
        RealTuple range = new RealTuple(RealTupleType.Generic3D, new double[]{1, 2, 3});
        
        SpatialField result = SpatialField.from(point, range);
        assertNotNull(result);
        System.out.println(result);
                
        GriddedSet set = DataUtility.createGriddedSet((FlatField) result.getField(), true);
        System.out.println(set);
    }
    
    @Test
    public void testFrom_SpatialDomain() throws VisADException, RemoteException {
        System.out.println("from SpatialDomain");
        
        SpatialDomain domain = new SpatialDomain(new LatLonTuple(34.0, -119.25), new LatLonTuple(34.25, -119.0), 5, 5);
        TupleType rangeType = RealTupleType.Generic3D;
        int numLatLons = domain.getDomainSetLength();
        int numDimensions = rangeType.getDimension();
        
        double[][] range = new double[numDimensions][numLatLons];
        for (int xy = 0; xy < numLatLons; xy++) {
                LatLonPoint pt = domain.getLatLonPointAt(xy);                                
                range[0][xy] = pt.getLatitude().getValue();
                range[1][xy] = pt.getLongitude().getValue();
                range[2][xy] = pt.hashCode();
        }
       
        SpatialField result = SpatialField.from(domain, rangeType, range);
        assertNotNull(result);
        System.out.println(result);
        
        
        //GriddedSet set = DataUtility.createGriddedSet((FlatField) result.getField(), true);
        //System.out.println(set);
     
        // Candidate methods to add to SpatialField or FieldWrapper
        FlatField field = (FlatField) result.getField();
        RealTuple eval =  (RealTuple) field.evaluate(new LatLonTuple(34.2, -119.2));
        System.out.println("evaluate(34.2, -119.2):");
        System.out.println(eval);
        
        System.out.println("RangeSets:");
        Set[] sets = field.getRangeSets();
        for (int i = 0; i < sets.length; i++) {
            Set set = sets[i];
            System.out.print(set);
        }
        
        System.out.print("extracts:");
        System.out.println(field.extract(0));
        System.out.println(field.extract(1));
        System.out.println(field.extract(2));
    }

}
