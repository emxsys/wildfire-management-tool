/*
 * Copyright (c) 2010-2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wildfire.surfacefire;

import com.emxsys.gis.api.Terrain;
import static com.emxsys.visad.FireUnit.*;
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.FireBehaviorProvider;
import com.emxsys.wildfire.api.FireBehaviorTuple;
import com.emxsys.wildfire.api.FireEnvironment;
import com.emxsys.wildfire.api.FuelCondition;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.behave.Behave;
import java.util.Map;
import org.openide.util.Exceptions;
import visad.CommonUnit;
import visad.VisADException;

/**
 * A Fire Behavior Modeling Service for Surface Fires.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SurfaceFireProvider implements FireBehaviorProvider {

    private final Behave behave;
    //private BehaveExp behave;    // experimental version

    public SurfaceFireProvider() {
        behave = new Behave();
        //behave = new BehaveExp();
    }

    /**
     * Computes the wildfire fire behavior using the Rothermel BEHAVE algorithms.
     *
     * @param fuelModel The FuelModel.
     * @param condition The fuel conditions.
     * @param weather The wind speed and wind direction.
     * @param terrain The aspect and slope.
     * @return A FireEnviornment instance containing the fire behavior and burning conditions.
     */
    @Override
    public FireEnvironment computeFireBehavior(FuelModel fuelModel, 
                                               FuelCondition condition, 
                                               Weather weather,
                                               Terrain terrain) {
        try {
            // Set static fuel model vars
            behave.fuelModel = fuelModel.getModelNo();
            behave.isDynamic = fuelModel.isDynamic();
            behave.w0_d1 = fuelModel.getDead1HrFuelLoad().getValue(kg_m2);
            behave.w0_d2 = fuelModel.getDead10HrFuelLoad().getValue(kg_m2);
            behave.w0_d3 = fuelModel.getDead100HrFuelLoad().getValue(kg_m2);
            behave.w0_lh = fuelModel.getLiveHerbFuelLoad().getValue(kg_m2);
            behave.w0_lw = fuelModel.getLiveWoodyFuelLoad().getValue(kg_m2);
            behave.sv_d1 = fuelModel.getDead1HrSAVRatio().getValue();
            behave.sv_d2 = fuelModel.getDead10HrSAVRatio().getValue();
            behave.sv_d3 = fuelModel.getDead100HrSAVRatio().getValue();
            behave.sv_lh = fuelModel.getLiveHerbSAVRatio().getValue();
            behave.sv_lw = fuelModel.getLiveWoodySAVRatio().getValue();
            behave.depth = fuelModel.getFuelBedDepth().getValue();
            behave.mx = fuelModel.getMoistureOfExtinction().getValue();
            behave.heat = fuelModel.getLowHeatContent().getValue();

            // Set moisture content variables
            behave.m_d1 = condition.getDead1HrFuelMoisture().getValue();
            behave.m_d2 = condition.getDead10HrFuelMoisture().getValue();
            behave.m_d3 = condition.getDead100HrFuelMoisture().getValue();
            behave.m_lh = condition.getLiveHerbFuelMoisture().getValue();
            behave.m_lw = condition.getLiveWoodyFuelMoisture().getValue();

            // Add wind and slope
            behave.wsp = weather.getWindSpeed().getValue(CommonUnit.meterPerSecond);
            behave.wdr = weather.getWindDirection().getValue();
            behave.slp = terrain.getSlope().getValue();
            behave.asp = terrain.getAspect().getValue();

            // Experimenal values
//            try {
//                Real tempDelta = (Real) condition.fuelTemp.subtract(condition.airTemp);
//                behave.fuelTempDelta = tempDelta.getValue();
//                behave.solarAngle = condition.solarAzimuthAngle;
//
//            } catch (RemoteException ex) {
//                Exceptions.printStackTrace(ex);
//            }
            // Now compute the fireBehavior behavior
            behave.calc();
            Map<String, Double> maxSpreadResults = behave.getMaxSpreadResults();
            Map<String, Double> noWndNoSlpResults = behave.getNoWindNoSlopeResults();

            FireEnvironment fireEnv = new FireEnvironment();
            fireEnv.model = fuelModel;
            fireEnv.condition = condition;
            fireEnv.fireBehavior = new FireBehaviorTuple(
                    maxSpreadResults.get("fli"),
                    maxSpreadResults.get("fln"),
                    maxSpreadResults.get("ros"),
                    maxSpreadResults.get("sdr"));

            fireEnv.fireBehaviorNoWnd = new FireBehaviorTuple(
                    noWndNoSlpResults.get("fli"),
                    noWndNoSlpResults.get("fln"),
                    noWndNoSlpResults.get("ros"),
                    noWndNoSlpResults.get("sdr"));
            return fireEnv;

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            return new FireEnvironment();
        }

    }
}
