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
package com.emxsys.wmt.wildfire.api;

import visad.Real;

/**
 * Fuel Model interface
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: FuelModel.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public interface FuelModel {

    /**
     * the standard fuel model number compatible with LANDFIRE data sets
     * @return the fuel model number
     */
    public int getModelNo() ;

    /**
     * the standard fuel model code
     * @return the fuel model code
     */
    public String getModelCode();

    /**
     * the fuel type complex name
     * @return the model name
     */
    public String getModelName();

    /**
     * the fuel type group that this model belongs to
     * @return the model group
     */
    public String getModelGroup();

    /**
     * the 1 hour dead fuel loading
     * @return [kg/m2]
     */
    public Real getDead1HrFuelLoad();

    /**
     * the 10 hour dead fuel loading
     * @return [kg/m2]
     */
    public Real getDead10HrFuelLoad();

    /**
     * the 100 hour dead fuel loading
     * @return [kg/m2]
     */
    public Real getDead100HrFuelLoad();

    /**
     * the live herbaceous fuel loading
     * @return [kg/m2]
     */
    public Real getLiveHerbFuelLoad();

    /**
     * the live woody fuel loading
     * @return  [kg/m2]
     */
    public Real getLiveWoodyFuelLoad();

    /**
     * the fuel type: static or dynamic
     * @return true if the fuel model is dynamic
     */
    public boolean isDynamic();

    /**
     * the dead 1Hr fuel surface-area-to-volumne ratio
     * @return [1/m]
     */
    public Real getDead1HrSAVRatio();

    /**
     * the dead 10Hr fuel surface-area-to-volumne ratio
     * @return [1/m]
     */
    public Real getDead10HrSAVRatio();

    /**
     * the dead 100Hr fuel surface-area-to-volumne ratio
     * @return [1/m]
     */
    public Real getDead100HrSAVRatio();

    /**
     * the live herbaceous fuel surface-area-to-volumne ratio
     * @return [1/m]
     */
    public Real getLiveHerbSAVRatio();

    /**
     * the live woody fuel surface-area-to-volumne ratio
     * @return [1/m]
     */
    public Real getLiveWoodySAVRatio();

    /**
     * the fuel bed depth
     * @return [m]
     */
    public Real getFuelBedDepth();

    /**
     * the moisture of extinction
     * @return [percent]
     */
    public Real getMoistureOfExtinction();

    /**
     * the low heat content
     * @return [kJ/kg]
     */
    public Real getLowHeatContent();


}
