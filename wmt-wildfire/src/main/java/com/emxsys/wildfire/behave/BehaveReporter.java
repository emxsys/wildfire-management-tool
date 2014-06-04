/*
 * Copyright (c) 2009-2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wildfire.behave;

import java.text.DecimalFormat;

/**
 * The BehaveReporter generates a pretty printout of the Behave data. This extremely nice output
 * originated from the BehaveTest.java class by Andreas Bachmann.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class BehaveReporter {

    private BehaveReporter() {
    }

    /**
     * Pretty print of Behave inputs and outputs.
     *
     * @return formatted text
     */
    public static String report(Behave behave) {
        // This extremely nice output was copied from the BehaveTest.java
        // class by Andreas Bachmann.

        // Build a StringBuilder(non-thread Safe) or StringBuffer for our output
        StringBuilder sb = new StringBuilder();

        // print out inputs and outputs...
        DecimalFormat df1 = new DecimalFormat("#,##0.0");
        DecimalFormat df2 = new DecimalFormat("#,##0.00");
        DecimalFormat df3 = new DecimalFormat("#,##0.000");
        DecimalFormat df4 = new DecimalFormat("#,##0.0000");
        DecimalFormat df5 = new DecimalFormat("#,##0.00000");

        appendLine(sb, "Fuel Model: " + behave.fuelModel + " " + behave.fuelModelCode + (behave.isDynamic ? " (D)" : " (S)"));
        appendLine(sb, " ");
        appendLine(sb, "  Effective mineral content    s_e          [%] ="
                + lPad(df1.format(behave.s_e), 10));
        appendLine(sb, "  Total mineral content        s_t          [%] ="
                + lPad(df1.format(behave.s_t), 10));
        appendLine(sb, "  Heat content                 heat     [kJ/kg] ="
                + lPad(df2.format(behave.heat), 10));
        appendLine(sb, "  Particle density             rho_p    [kg/m3] ="
                + lPad(df2.format(behave.rho_p), 10));
        appendLine(sb, "  Fuel bed depth               d            [m] ="
                + lPad(df2.format(behave.depth), 10));
        appendLine(sb, "  Moisture of extinction       mx           [%] ="
                + lPad(df2.format(behave.mx), 10));
        appendLine(sb, "  Fuel load transferred        curing       [%] ="
                + lPad(df2.format(behave.curing * 100), 10));
        appendLine(sb, " ");

        // static fuel properties
        appendLine(sb, "  Size |  Fuel Load | Surface-to-Volume- | Moisture");
        appendLine(sb, "       |    [kg/m2] | Ratio        [1/m] |      [%]");
        appendLine(sb, " ---------------------------------------------------");
        appendLine(sb, "    d1 |"
                + lPad(df3.format(behave.w0_d1), 10) + "  |"
                + lPad(df3.format(behave.sv_d1), 18) + "  |"
                + lPad(df1.format(behave.m_d1), 8));
        appendLine(sb, "    d2 |"
                + lPad(df3.format(behave.w0_d2), 10) + "  |"
                + lPad(df3.format(behave.sv_d2), 18) + "  |"
                + lPad(df1.format(behave.m_d2), 8));
        appendLine(sb, "    d3 |"
                + lPad(df3.format(behave.w0_d3), 10) + "  |"
                + lPad(df3.format(behave.sv_d3), 18) + "  |"
                + lPad(df1.format(behave.m_d3), 8));
        appendLine(sb, "    lh |"
                + lPad(df3.format(behave.w0_lh), 10) + "  |"
                + lPad(df3.format(behave.sv_lh), 18) + "  |"
                + lPad(df1.format(behave.m_lh), 8));
        appendLine(sb, "    lw |"
                + lPad(df3.format(behave.w0_lw), 10) + "  |"
                + lPad(df3.format(behave.sv_lw), 18) + "  |"
                + lPad(df1.format(behave.m_lw), 8));
//        if (behave.isDynamic) {
//            appendLine(sb, "   (dh)|"
//                    + lPad(df3.format(behave.w0_dh), 10));
//        }
        appendLine(sb, " ");

        // wind and slope inputs
        appendLine(sb, "  Wind                         |  Terrain");
        appendLine(sb, " ---------------------------------------------------------");
        appendLine(sb, "   wind speed     [m/s]" + lPad(df2.format(behave.wsp), 7)
                + " |   slope      [deg]" + lPad(df2.format(behave.slp), 7));
        appendLine(sb, "   wind direction [deg]" + lPad(df2.format(behave.wdr), 7)
                + " |   aspect     [deg]" + lPad(df2.format(behave.asp), 7));
        appendLine(sb, " ");
        appendLine(sb, "  => Wind factor                phi_w       [-] ="
                + lPad(df4.format(behave.phi_w), 12));
        appendLine(sb, "  => Slope factor               phi_s       [-] ="
                + lPad(df4.format(behave.phi_s), 12));
        appendLine(sb, "  => Wind and slope factor      phi_t       [-] ="
                + lPad(df4.format(behave.phi_t), 12));
        appendLine(sb, "  => Effective Wind speed       efw      [km/h] ="
                + lPad(df2.format(behave.efw / 0.27778), 10)); // m/s to kph
        appendLine(sb, "  => Direction of max. spread   sdr       [deg] ="
                + lPad(df1.format(behave.sdr), 9));
        appendLine(sb, " ");
        
        appendLine(sb, "  => Characteristic sv-ratio    sigma     [1/m] ="
                + lPad(df2.format(behave.sigma), 10));
        appendLine(sb, "  => Live moisture of extintion Mx_live     [%] ="
                + lPad(df2.format(behave.Mx_live), 10));
        appendLine(sb, "  => Ratio dead/live fine fuels W           [-] ="
                + lPad(df4.format(behave.W), 10));
        appendLine(sb, "  => Mean bulk density          rho_b   [kg/m3] ="
                + lPad(df4.format(behave.rho_b), 12));
        appendLine(sb, "  => Packing ratio              beta        [-] ="
                + lPad(df5.format(behave.beta), 13));
        appendLine(sb, "  => Relative packing ratio     beta_ratio  [-] ="
                + lPad(df5.format(behave.beta_ratio), 13));
        appendLine(sb, " ");

        appendLine(sb, "  => Rate of spread             ros     [m/min] ="
                + lPad(df4.format(behave.ros * 60), 12));
        appendLine(sb, "  => Heat per area              hpa     [kJ/m2] ="
                + lPad(df2.format(behave.hpa), 10));
        appendLine(sb, "  => Fire line intensity        fli      [kW/m] ="
                + lPad(df2.format(behave.fli), 10));
        appendLine(sb, "  => Flame length               fln         [m] ="
                + lPad(df2.format(behave.fln), 10));
        appendLine(sb, "  => Reaction intensity         I_r     [kW/m2] ="
                + lPad(df2.format(behave.I_r), 10));
        appendLine(sb, " ");

        appendLine(sb, "  => Heat sink                  hsk     [kJ/m3] ="
                + lPad(df2.format(behave.hsk), 10));
        appendLine(sb, "  => Propagating flux ratio     xi          [-] ="
                + lPad(df4.format(behave.xi), 12));
        appendLine(sb, "  => flame residence time       tau         [s] ="
                + lPad(df2.format(behave.tau), 10));
        appendLine(sb, "  => flame zone depth           fzd         [m] ="
                + lPad(df3.format(behave.fzd), 11));
        appendLine(sb, " ");


        return sb.toString();
    }

    private static void appendLine(StringBuilder sb, String s) {
        sb.append(s);
        sb.append('\n');
    }

    private static String lPad(String t, int outputWidth) {
        while (t.length() < outputWidth) {
            t = " " + t;
        }
        return t;
    }
}
