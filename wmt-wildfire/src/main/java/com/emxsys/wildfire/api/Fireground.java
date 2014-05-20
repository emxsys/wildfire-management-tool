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

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.Gridded1DDoubleSet;


public interface Fireground
{
    /**
     * Get the FireEnvironment at the specified time and place.
     *
     * @param dateTime
     * @param position
     * @return a structure containing fire behavior and environmental data.
     */
    FireEnvironment getFireEnvironment(DateTime dateTime, Coord2D position);

    TreeMap<Date, FireEnvironment> getFireEnvironment(List<Date> dates, Coord2D position);

    /**
     * Add a geographic bounding box that defines the boundaries of the fireground.
     * 
     * @param sector The sector to add.
     * @param provider The fuel model provider mapped to the sector.
     */
    void addSector(Box sector, FuelModelProvider provider);
    


    /**
     * Remove a geographic bounding box that defines the boundaries of the fireground.
     * 
     * @param sector The sector to remove.
     */
    void removeSector(Box sector);


    /**
     * A collection of bounding boxes that define the boundaries of the fireground.
     *
     * @return geographic extents (min/max latitude and longitude)
     */
    List<Box> getSectors();

    /**
     * Gets the FuelModelProvider associated with the given sector.
     * 
     * @param sector The sector to lookup.
     * @return The FuelModelProvider mapped to the sector.
     */
    FuelModelProvider getFuelModelProvider(Box sector);
    
    /**
     * Fuel model variable inputs within the fireground.
     *
     * @return ((latitude, longitude) -> (1HrDead, 10HrDead, 100HrDead, liveHerb, liveWoody, 1HrSAV,
     * depth, extnPct))
     */
    Collection<FlatField> getFuelModels();


    /**
     * Fuel moisture variables within the fireground by time of day.
     *
     * @return (time -> ((latitude, longitude) -> (1hrPct)))
     */
    Collection<FieldImpl> getFuelMoistureDead1hr();


    Collection<FieldImpl> getFuelMoistureDead10hr();


    Collection<FieldImpl> getFuelMoistureDead100hr();


    Collection<FieldImpl> getFuelMoistureLiveHerb();


    Collection<FieldImpl> getFuelMoistureLiveWoody();


    /**
     * Fuel temperature variables within the fireground by time of day.
     *
     * @return (time -> ((latitude, longitude) -> (fuelTemp)))
     */
    Collection<FieldImpl> getFuelTemperature();


    /**
     * Fire behavior predictions within the fireground by time of day.
     *
     * @return (time -> ((latitude, longitude) -> (fli, fl, ros, dir)))
     */
    FlatField getFirePredictions();


    /**
     * Fire behavior observations within the fireground by time of day. 
     * 
     * @return (time -> ((latitude, longitude) -> (fl, ros, dir)))
     */
    FlatField getFireSignatures();


    /**
     * Terrain characteristics within the fireground
     *
     * @return (latitude, longitude) -> (slope, aspect, elevation)
     */
    Collection<FlatField> getTerrain();


    Collection<FieldImpl> getFireBehaviorMax();


    Collection<FieldImpl> getFireBehaviorMin();


    /**
     * @return Math type: ( time -> ( SolarType.TIME, AIR_TEMP_C, REL_HUMIDITY, WIND_SPEED_SI,
     * WIND_DIR, CLOUD_COVER ) )
     */
    Collection<FieldImpl> getFireWeather();


    Gridded1DDoubleSet getTimeSet();


    FlatField getAirTemperature();


    FlatField getRelativeHumidity();


    FlatField getGeneralWinds();


    FlatField getCloudCover();


    public void addPropertyChangeListener(PropertyChangeListener l);


    public void removePropertyChangeListener(PropertyChangeListener l);
}
