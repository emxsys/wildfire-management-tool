/*
 * Copyright (C) 2011 Bruce Schubert <bruce@emxsys.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.emxsys.wmt.gis.api;

import java.rmi.RemoteException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import visad.CommonUnit;
import static org.junit.Assert.*;
import visad.DerivedUnit;
import visad.Real;
import visad.RealType;
import visad.ScaledUnit;
import visad.Unit;
import visad.VisADException;



/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class AltitudeTest
{
    
    public AltitudeTest()
    {
    }



    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }



    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }



    /**
     * Test of fromMeters method, of class Altitude.
     */
    @Test
    public void testFromMeters()
    {
        System.out.println("fromMeters");
        double meters = Math.PI;
        Real expect = new Real(RealType.Altitude, meters);
        Real result = Altitude.fromMeters(meters);
        assertEquals(expect, result);
        assertEquals(expect.getValue(), result.getValue(), 0.0);
    }



    /**
     * Test of fromFeet method, of class Altitude.
     */
    @Test
    public void testFromFeet()
    {
        System.out.println("fromFeet");
        double feet = 3.2808399;
        double meters = 1.0;
        Real expect = new Real(RealType.Altitude, meters);
        Real result = Altitude.fromFeet(feet);
        try
        {
            System.out.println("  Expect: " + expect.toValueString());
            System.out.println("  Result: " + result.toValueString());
        }
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
        assertEquals(expect.getValue(), result.getValue(), 0.00000001);
    }



    /**
     * Test of fromReal method, of class Altitude.
     */
    @Test
    public void testFromReal()
    {
        System.out.println("fromReal");
        double feet = 1.0;
        double meters = 0.3048;
        try
        {
            Real expect = Altitude.fromFeet(feet);
            Real result = Altitude.fromReal(expect);
            Real dblchk = new Real(RealType.Altitude, meters);
            System.out.println("  Expect: " + expect.toValueString());
            System.out.println("  Result: " + result.toValueString());
            System.out.println("  DblChk: " + dblchk.toValueString());
            
            assertEquals(expect, result);
            assertEquals(expect.getValue(), result.getValue(), 0.00000001);
            assertEquals(dblchk.getValue(), result.getValue(), 0.00000001);
        }
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
}
