/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wildfire.api;

import visad.Real;

/**
 * Fuel Model interface.
 * 
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public interface FuelModel {

    /**
     * The standard fuel model number compatible with LANDFIRE data sets.
     * @return The unique number identifying the fuel model.
     */
    public int getModelNo() ;

    /**
     * The standard fuel model code.
     * @return The unique code representing the fuel model.
     */
    public String getModelCode();

    /**
     * The fuel type complex name.
     * @return The model name.
     */
    public String getModelName();

    /**
     * The fuel type group that this model belongs to, e.g., Original 13, Standard 40, etc.
     * @return The model group name.
     */
    public String getModelGroup();

    /**
     * The 1 hour dead fuel loading.
     * @return [kg/m2]
     */
    public Real getDead1HrFuelLoad();

    /**
     * The 10 hour dead fuel loading.
     * @return [kg/m2]
     */
    public Real getDead10HrFuelLoad();

    /**
     * The 100 hour dead fuel loading.
     * @return [kg/m2]
     */
    public Real getDead100HrFuelLoad();

    /**
     * The live herbaceous fuel loading.
     * @return [kg/m2]
     */
    public Real getLiveHerbFuelLoad();

    /**
     * The live woody fuel loading.
     * @return  [kg/m2]
     */
    public Real getLiveWoodyFuelLoad();

    /**
     * The fuel type: static or dynamic.
     * @return True if the fuel model is dynamic.
     */
    public boolean isDynamic();

    /**
     * Burnable or non burnable.
     * @return True if the fuel model is burnable
     */
    public boolean isBurnable();

    /**
     * The dead 1 hour fuel surface-area-to-volume ratio.
     * @return [1/m]
     */
    public Real getDead1HrSAVRatio();

    /**
     * The dead 10 hour fuel surface-area-to-volume ratio
     * @return [1/m]
     */
    public Real getDead10HrSAVRatio();

    /**
     * The dead 100 hour fuel surface-area-to-volume ratio
     * @return [1/m]
     */
    public Real getDead100HrSAVRatio();

    /**
     * The live herbaceous fuel surface-area-to-volume ratio.
     * @return [m^2/m^3]
     */
    public Real getLiveHerbSAVRatio();

    /**
     * The live woody fuel surface-area-to-volume ratio.
     * @return [[m^2/m^3]]
     */
    public Real getLiveWoodySAVRatio();

    /**
     * The fuel bed depth.
     * @return [m]
     */
    public Real getFuelBedDepth();

    /**
     * The moisture of extinction.
     * @return [percent]
     */
    public Real getMoistureOfExtinction();

    /**
     * The low heat content.
     * @return [kJ/kg]
     */
    public Real getLowHeatContent();


}
