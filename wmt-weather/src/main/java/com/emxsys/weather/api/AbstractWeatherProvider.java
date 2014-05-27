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
package com.emxsys.weather.api;

import com.emxsys.visad.Reals;
import static com.emxsys.weather.api.WeatherType.AIR_TEMP_F;
import static com.emxsys.weather.api.WeatherType.REL_HUMIDITY;
import static com.emxsys.weather.api.WeatherType.WIND_DIR;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_KTS;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import visad.RealTupleType;
import visad.RealType;

/**
 * Derived classes must implement getImage()
 * @author Bruce Schubert
 */
public abstract class AbstractWeatherProvider implements WeatherProvider {

    private final InstanceContent content = new InstanceContent();
    private AbstractLookup lookup;
    
    /** MathType to represent the weather: air_temp, RH, wind_spd, wind_dir */
    protected static final RealTupleType WX_RANGE = Reals.newRealTupleType(
            new RealType[]{AIR_TEMP_F, REL_HUMIDITY, WIND_SPEED_KTS, WIND_DIR});
    protected static final int AIR_TEMP_IDX = WX_RANGE.getIndex(AIR_TEMP_F);
    protected static final int HUMIDITY_IDX = WX_RANGE.getIndex(REL_HUMIDITY);
    protected static final int WIND_SPD_IDX = WX_RANGE.getIndex(WIND_SPEED_KTS);
    protected static final int WIND_DIR_IDX = WX_RANGE.getIndex(WIND_DIR);
    
    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new AbstractLookup(content);
        }
        return lookup;
    }
    
    @Override
    public <T> T getCapability(Class<T> clazz) {
        return getLookup().lookup(clazz);
    }

    /**
     * Gets the content of lookup so derived classes can add and remove content.
     * @return The lookup content.
     */
    protected InstanceContent getContent() {
        return content;
    }

    @Override
    public String toString() {
        return getName();
    }

}
