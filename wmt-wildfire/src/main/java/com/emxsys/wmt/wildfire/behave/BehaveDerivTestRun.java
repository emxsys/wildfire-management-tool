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
 * - Neither the name of the Emxsys company nor the names of its 
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
package com.emxsys.behave;

/*
 * Original version: Copyright (C) 2001, Andreas Bachmann
 * send comments to:
 *      bachmann@geo.unizh.ch
 * or by letter:
 *      University of Zurich
 *      Deptartment of Geography
 *      Geographic Information Systems Division
 *      Winterthurerstr. 190
 *      8057 Zurich
 *      Switzerland
 */


import com.emxsys.behave.BehaveDeriv;
import java.util.*;
import java.text.*;
import java.io.*;
import javax.swing.JFileChooser;

/**
 * Class to run BehaveDeriv
 * 
 * @author andreas bachmann
 * @version 1.0 april 2001
 */
public class BehaveDerivTestRun {

    public static void main(String[] args) {
        String filename;
        if (args.length < 1) {
            JFileChooser jf = new JFileChooser("src/com/emxsys/behave/data");
            jf.showOpenDialog(null);
            File file = jf.getSelectedFile();
            filename = file.getPath();
        } else {
            filename = args[0];
        }
        System.out.println("Using file " + filename);
        BehaveDeriv b = new BehaveDeriv();
        Properties p;
        //
        p = new Properties();
        try {
            p.load(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse the values...
        String key = " ";
        String value;
        StringTokenizer propValString;
        StringTokenizer fieldTag;

        for (Iterator i = p.keySet().iterator(); i.hasNext();) {
            try {
                key = (String) i.next();
                // get the values for the corresponding key in the properties
                propValString = new StringTokenizer(p.getProperty(key));
                value = propValString.nextToken(); // the first is the mean
                b.setParameterMean(key, Double.parseDouble(value));
                //
                if (propValString.hasMoreTokens()) {
                    value = propValString.nextToken(); // this is the stdv
                    b.setParameterStdv(key, Double.parseDouble(value));
                } else {
                    b.setParameterStdv(key, 0.0);
                }
            } catch (NoSuchElementException e) {
                System.out.println("missing element! " + key);
            }
        }

        /////////////////////////////////////////////////////////////
        // This part added by Lewis Ntaimo December 6, 2002
        // alter the wind speed and direction
        b.wsp = 3;
        b.wdr = 90;
        ///////////////////////////////////

        //
        // calculate everything...
        b.calc();
        b.calcDerivs();
        b.calcVariances();
        //
        // do the print out...
        DecimalFormat df1 = new DecimalFormat("#,##0.0");
        DecimalFormat df2 = new DecimalFormat("#,##0.00");
        DecimalFormat df3 = new DecimalFormat("#,##0.000");
        DecimalFormat df4 = new DecimalFormat("#,##0.0000");
        DecimalFormat df5 = new DecimalFormat("#,##0.00000");
        DecimalFormat df6 = new DecimalFormat("#,##0.000000");
        DecimalFormat df8 = new DecimalFormat("#,##0.00000000");
        //
        double mean;
        double stdv;
        double pdrv;
        double varpa;
        double varpp;
        double vartot = b.getValue("rosVar");
        //
        show(" ");
        show(" Var   |       Mean |       Stdv | part.Deriv |   VarPart       [%]");
        show("--------------------------------------------------------------------");
        mean = b.getValue("w0_d1");
        stdv = b.getValue("w0_d1Stdv");
        pdrv = b.getValue("w0_d1PDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" w0_d1 |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        mean = b.getValue("w0_d2");
        stdv = b.getValue("w0_d2Stdv");
        pdrv = b.getValue("w0_d2PDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" w0_d2 |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        mean = b.getValue("w0_d3");
        stdv = b.getValue("w0_d3Stdv");
        pdrv = b.getValue("w0_d3PDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" w0_d3 |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        mean = b.getValue("w0_lh");
        stdv = b.getValue("w0_lhStdv");
        pdrv = b.getValue("w0_lhPDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" w0_lh |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        mean = b.getValue("w0_lw");
        stdv = b.getValue("w0_lwStdv");
        pdrv = b.getValue("w0_lwPDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" w0_lw |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        mean = b.getValue("sv_d1");
        stdv = b.getValue("sv_d1Stdv");
        pdrv = b.getValue("sv_d1PDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" sv_d1 |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        mean = b.getValue("sv_d2");
        show(" sv_d2 |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 12) + " |" +
                lPad("-", 6));
        mean = b.getValue("sv_d3");
        show(" sv_d3 |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 12) + " |" +
                lPad("-", 6));
        mean = b.getValue("sv_lh");
        show(" sv_lh |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 12) + " |" +
                lPad("-", 6));
        mean = b.getValue("sv_lw");
        show(" sv_lw |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 12) + " |" +
                lPad("-", 6));
        // moisture content
        mean = b.getValue("m_d1");
        stdv = b.getValue("m_d1Stdv");
        pdrv = b.getValue("m_d1PDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" m_d1  |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        mean = b.getValue("m_d2");
        stdv = b.getValue("m_d2Stdv");
        pdrv = b.getValue("m_d2PDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" m_d2  |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        mean = b.getValue("m_d3");
        stdv = b.getValue("m_d3Stdv");
        pdrv = b.getValue("m_d3PDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" m_d3  |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        mean = b.getValue("m_lh");
        stdv = b.getValue("m_lhStdv");
        pdrv = b.getValue("m_lhPDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" m_lh  |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        mean = b.getValue("m_lw");
        stdv = b.getValue("m_lwStdv");
        pdrv = b.getValue("m_lwPDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" m_lw  |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        //
        // depth
        mean = b.getValue("depth");
        stdv = b.getValue("depthStdv");
        pdrv = b.getValue("depthPDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" d     |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        // moisture of extinction
        mean = b.getValue("mx");
        stdv = b.getValue("mxStdv");
        pdrv = b.getValue("mxPDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" mx    |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        //
        // particle density
        mean = b.getValue("rho_p");
        show(" rho_p |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 12) + " |" +
                lPad("-", 6));
        // heat content
        mean = b.getValue("heat");
        show(" heat  |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 11) + " |" +
                lPad("-", 12) + " |" +
                lPad("-", 6));
        //
        // slope
        mean = b.getValue("slp");
        stdv = b.getValue("slpStdv");   // in radian!
        pdrv = b.getValue("slpPDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" slp   |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(Math.toDegrees(stdv)), 11) + " |" + // convert to degrees!
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        // asp
        mean = b.getValue("asp");
        stdv = b.getValue("aspStdv");  // in radian!
        pdrv = b.getValue("aspPDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" asp   |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(Math.toDegrees(stdv)), 11) + " |" + // convert to degrees!
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        // wsp
        mean = b.getValue("wsp");
        stdv = b.getValue("wspStdv");
        pdrv = b.getValue("wspPDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" wsp   |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        // wdr
        mean = b.getValue("wdr");
        stdv = b.getValue("wdrStdv"); // in radian!
        pdrv = b.getValue("wdrPDros");
        varpa = stdv * stdv * pdrv * pdrv;
        varpp = varpa * 100 / vartot;
        show(" wdr   |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(Math.toDegrees(stdv)), 11) + " |" + // convert to degrees!
                lPad(df6.format(pdrv), 11) + " |" +
                lPad(df8.format(varpa), 12) + " |" +
                lPad(df1.format(varpp), 6));
        show("--------------------------------------------------------------------");
        //
        // ros
        mean = b.getValue("ros");
        stdv = b.getValue("rosStdv"); // in radian!
        show(" ros   |" +
                lPad(df3.format(mean), 11) + " |" +
                lPad(df4.format(stdv), 11) + " |" +
                "            |" +
                lPad(df8.format(vartot), 12) + " | 100.0");
/////////////////////////////////////////////////////////////////
        show(" ");
        //for(int i = 0; i < b.ros_d.length; i++)
        // show(" " + b.rosVar);
    }

    public static void usage() {
        show("Usage: java BehaveDerivTest input_file");
    }

    public static void show(String text) {
        System.out.println(text);
    }

    public static String lPad(String t, int outputWidth) {
        while (t.length() < outputWidth) {
            t = " " + t;
        }
        return t;
    }

    public static String rPad(String t, int outputWidth) {
        while (t.length() < outputWidth) {
            t = t + " ";
        }
        return t;
    }
}
