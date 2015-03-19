/*
 * Copyright (c) 2015, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.cps.util;

import com.emxsys.visad.FireUnit;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.behavior.SurfaceFire;
import com.emxsys.wildfire.behavior.SurfaceFuel;
import com.emxsys.wmt.cps.options.CpsOptions;
import java.text.DecimalFormat;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.CommonUnit;
import visad.VisADException;

/**
 * A utility class for SurfaceFire objects.
 *
 * @author Bruce Schubert
 */
@Messages({
    "LBL_FuelModel=Fuel Model: ",
    "LBL_FuelMoisture=Fine Fuel Moisture: ",
    "LBL_MoistureOfExt=Moisture of Ext.: ",
    "LBL_FuelTemp=Fuel Temp. = ",
    "LBL_AirTemp=Air Temp. = ",
    "LBL_RH=Rel. Humidity = ",
    "LBL_WindSpd=Wind Speed = ",
    "LBL_WindDir=Wind Dir. = ",
    "LBL_CloudCover=Cloud Cover = ",
    "LBL_FlameLen=Flame Length = ",
    "LBL_FLI=Byram's Intensity = ",
    "LBL_ROS=Rate of Spread = ",
})
public class CpsUtil {

    // print out inputs and outputs...
    static final DecimalFormat df0 = new DecimalFormat("#,##0");
    static final DecimalFormat df1 = new DecimalFormat("#,##0.0");
    static final DecimalFormat df2 = new DecimalFormat("#,##0.00");
    static final DecimalFormat df3 = new DecimalFormat("#,##0.000");
    static final DecimalFormat df4 = new DecimalFormat("#,##0.0000");
    static final DecimalFormat df5 = new DecimalFormat("#,##0.00000");

    private CpsUtil() {
    }

    /**
     * Returns a string with the fire properties formatted with the current CPS UOM options
     *
     * @param fire
     * @return
     */
    public static String getPrettyString(SurfaceFire fire) {
        String uom = CpsOptions.getUom();
        StringBuilder sb = new StringBuilder();
        try {


            switch (uom) {
                case CpsOptions.UOM_METRIC:
                case CpsOptions.UOM_SI:
                    appendLine(sb, Bundle.LBL_FlameLen() + df1.format(fire.getFlameLength().getValue(CommonUnit.meter)) + "[m]");
                    appendLine(sb, Bundle.LBL_ROS() + df1.format(fire.getRateOfSpreadMax().getValue(GeneralUnit.kph)) + "[kph]");
                    appendLine(sb, Bundle.LBL_FLI() + df1.format(fire.getFirelineIntensity().getValue(FireUnit.kW_m)) + "[kW/m]");
                    break;
                case CpsOptions.UOM_US:
                    appendLine(sb, Bundle.LBL_FlameLen() + df1.format(fire.getFlameLength().getValue(GeneralUnit.foot)) + "[ft]");
                    appendLine(sb, Bundle.LBL_ROS() + df1.format(fire.getRateOfSpreadMax().getValue(GeneralUnit.mph)) + "[mph]");
                    appendLine(sb, Bundle.LBL_FLI()+ df1.format(fire.getFirelineIntensity().getValue(FireUnit.Btu_ft_s)) + "[Btu/ft/s]");
                    break;
            }

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return sb.toString();
    }

    public static String getPrettyString(SurfaceFuel fuel) {
        String uom = CpsOptions.getUom();
        StringBuilder sb = new StringBuilder();
        try {

            FuelModel fuelModel = fuel.getFuelModel();

            switch (uom) {
                case CpsOptions.UOM_METRIC:
                case CpsOptions.UOM_SI:
                    appendLine(sb, Bundle.LBL_FuelModel() + fuelModel.getModelName() + " " + (fuelModel.isDynamic() ? " (D)" : " (S)"));
                    appendLine(sb, Bundle.LBL_FuelTemp() + df1.format(fuel.getFuelTemperature().getValue(GeneralUnit.degC)) + "[C]");
                    appendLine(sb, Bundle.LBL_MoistureOfExt() + df2.format(fuel.getDeadMoistureOfExt().getValue()) + "[%]");
                    appendLine(sb, Bundle.LBL_FuelMoisture() + df2.format(fuel.getDead1HrFuelMoisture().getValue()) + "[%]");
                    break;
                case CpsOptions.UOM_US:
                    appendLine(sb, Bundle.LBL_FuelModel() + fuelModel.getModelName() + " " + (fuelModel.isDynamic() ? " (D)" : " (S)"));
                    appendLine(sb, Bundle.LBL_FuelTemp() + df0.format(fuel.getFuelTemperature().getValue(GeneralUnit.degF)) + "[F]");
                    appendLine(sb, Bundle.LBL_MoistureOfExt() + df2.format(fuel.getDeadMoistureOfExt().getValue()) + "[%]");
                    appendLine(sb, Bundle.LBL_FuelMoisture() + df2.format(fuel.getDead1HrFuelMoisture().getValue()) + "[%]");
                    break;
            }

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return sb.toString();
    }

    /**
     * Returns a string with the fire properties formatted with the current CPS UOM options
     *
     * @param wx
     * @return
     */
    public static String getPrettyString(Weather wx) {
        String uom = CpsOptions.getUom();
        StringBuilder sb = new StringBuilder();
        try {

            switch (uom) {
                case CpsOptions.UOM_METRIC:
                case CpsOptions.UOM_SI:
                    appendLine(sb, Bundle.LBL_AirTemp() + df1.format(wx.getAirTemperature().getValue(GeneralUnit.degC)) + "[C]");
                    appendLine(sb, Bundle.LBL_RH() + df1.format(wx.getRelativeHumidity().getValue()) + "[%]");
                    appendLine(sb, Bundle.LBL_CloudCover() + df0.format(wx.getCloudCover().getValue()) + "[%]");
                    appendLine(sb, Bundle.LBL_WindDir() + df0.format(wx.getWindDirection().getValue()) + "[°]");
                    appendLine(sb, Bundle.LBL_WindSpd() + df1.format(wx.getWindSpeed().getValue(CommonUnit.meterPerSecond)) + "[m/s]");
                    break;
                case CpsOptions.UOM_US:
                    appendLine(sb, Bundle.LBL_AirTemp() + df0.format(wx.getAirTemperature().getValue(GeneralUnit.degF)) + "[F]");
                    appendLine(sb, Bundle.LBL_RH() + df1.format(wx.getRelativeHumidity().getValue()) + "[%]");
                    appendLine(sb, Bundle.LBL_CloudCover() + df0.format(wx.getCloudCover().getValue()) + "[%]");
                    appendLine(sb, Bundle.LBL_WindDir() + df0.format(wx.getWindDirection().getValue()) + "[°]");
                    appendLine(sb, Bundle.LBL_WindSpd() + df1.format(wx.getWindSpeed().getValue(GeneralUnit.mph)) + "[mph]");
                    break;
            }

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return sb.toString();
    }

    private static void appendLine(StringBuilder sb, String s) {
        sb.append(s);
        sb.append('\n');
    }

}
