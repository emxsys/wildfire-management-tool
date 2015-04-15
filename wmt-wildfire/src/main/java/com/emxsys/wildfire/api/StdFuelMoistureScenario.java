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

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public enum StdFuelMoistureScenario {

    VeryLowDead_FullyCuredHerb("Very Low Dead, Fully Cured Herb", 3, 4, 5, 30, 60),
    VeryLowDead_2_3_CuredHerb("Very Low Dead, 2/3 Cured Herb", 3, 4, 5, 60, 90),
    VeryLowDead_1_3_CuredHerb("Very Low Dead, 1/3 Cured Herb", 3, 4, 5, 90, 120),
    VeryLowDead_FullyGreenHerb("Very Low Dead, Fully Green Herb", 3, 4, 5, 120, 150),
    LowDead_FullyCuredHerb("Low Dead, Fully Cured Herb", 6, 7, 8, 30, 60),
    LowDead_2_3_CuredHerb("Low Dead, 2/3 Cured Herb", 6, 7, 8, 60, 90),
    LowDead_1_3_CuredHerb("Low Dead, 1/3 Cured Herb", 6, 7, 8, 90, 120),
    LowDead_FullyGreenHerb("Low Dead, Fully Green Herb", 6, 7, 8, 120, 150),
    ModerateDead_FullyCuredHerb("Moderate Dead, Fully Cured Herb", 9, 10, 11, 30, 60),
    ModerateDead_2_3_CuredHerb("Moderate Dead, 2/3 Cured Herb", 9, 10, 11, 60, 90),
    ModerateDead_1_3_CuredHerb("Moderate Dead, 1/3 Cured Herb", 9, 10, 11, 90, 120),
    ModerateDead_FullyGreenHerb("Moderate Dead, Fully Green Herb", 9, 10, 11, 120, 150),
    HighDead_FullyCuredHerb("High Dead, Fully Cured Herb", 12, 13, 14, 30, 60),
    HighDead_2_3_CuredHerb("High Dead, 2/3 Cured Herb", 12, 13, 14, 60, 90),
    HighDead_1_3_CuredHerb("High Dead, 1/3 Cured Herb", 12, 13, 14, 90, 120),
    HighDead_FullyGreenHerb("High Dead, Fully Green Herb", 12, 13, 14, 120, 150);

    /**
     * Model # - CODE - Model name
     */
    @Override
    public String toString() {
        return scenarioName;
    }

    public FuelMoisture getFuelMoisture() {
        return fuelMoisture;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    private final String scenarioName;
    private final BasicFuelMoisture fuelMoisture;

    StdFuelMoistureScenario(String scenarioName,
                            double dead1Hr, double dead10Hr, double dead100Hr,
                            double liveHerb, double liveWoody) {
        this.scenarioName = scenarioName;
        this.fuelMoisture = BasicFuelMoisture.fromDoubles(dead1Hr, dead10Hr, dead100Hr, liveHerb, liveWoody);
    }
}
