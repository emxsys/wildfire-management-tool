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
package com.emxsys.wmt.weather.mesowest.layers;

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateEvent;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateProvider;
import com.emxsys.wmt.gis.api.layer.BasicLayerCategory;
import com.emxsys.wmt.gis.api.layer.BasicLayerGroup;
import com.emxsys.wmt.gis.api.layer.BasicLayerType;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.layers.RenderableGisLayer;
import com.emxsys.wmt.visad.GeneralUnit;
import com.emxsys.wmt.visad.Reals;
import com.emxsys.wmt.weather.mesowest.MesoWestWeatherProvider;
import java.rmi.RemoteException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import visad.Field;
import visad.RealTuple;
import visad.VisADException;

/**
 * A RenderableGisLayer for displaying MesoWestPlacemark markers. This layer is not created by the 
 * normal XML MapLayerRegistration registration process due to dependency issues between the modules. 
 * Instead, this class is added to Globe via the module Installer.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages({
    "CTL_MesoWest=MesoWest"})

public final class MesoWestLayer extends RenderableGisLayer {

    /** The layer name as it appears in the Layer Manager */
    public static String LAYER_MESOWEST = Bundle.CTL_MesoWest();
    /** The coordinate event obtained by the listener */
    private final AtomicReference<ReticuleCoordinateEvent> coordEvent = new AtomicReference<>(
            new ReticuleCoordinateEvent(this, GeoCoord3D.INVALID_POSITION));
    /** Event task processor */
    private static final RequestProcessor taskProcessor = new RequestProcessor(MesoWestLayer.class);
    /** Event task */
    private Task updateWeatherTask;
    /** Event provider */
    private ReticuleCoordinateProvider coordProvider = null;
    /** Last coordinate processed */
    private Coord3D lastCoord = GeoCoord3D.ZERO_POSITION;

    /**
     * Constructor for a MesoWestLayer with default attributes.
     */
    public MesoWestLayer() {
        super(LAYER_MESOWEST, BasicLayerGroup.Overlay, BasicLayerType.Other, BasicLayerCategory.Other);
        setEnabled(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (coordProvider == null) {
            coordProvider = Globe.getInstance().getLookup().lookup(ReticuleCoordinateProvider.class);
            if (coordProvider != null) {
                coordProvider.addReticuleCoordinateListener((ReticuleCoordinateEvent evt) -> {
                    processEvent(evt);
                });
            }
        }
    }

    /**
     * Coal/ese and process ReticuleCoordinateEvent events with a task processor.
     * @param evt 
     */
    private void processEvent(ReticuleCoordinateEvent evt) {
        coordEvent.set(evt);
        // Lazy task initialization
        if (updateWeatherTask == null) {
            updateWeatherTask = taskProcessor.create(() -> {
                updateWeather();
            }, true); // true = initiallyFinished state
        }
        // Sliding task: coallese the update events into 1000ms intervals
        if (updateWeatherTask.isFinished()) {
            updateWeatherTask.schedule(1000); // update in 1 second
        }
    }

    /**
     * Update the weather field from the last coordinate.
     */
    private void updateWeather() {
        ReticuleCoordinateEvent evt = coordEvent.get();
        Coord3D coord = evt.getCoordinate();
        if (coord.isMissing()) {
            return;
        }
        try {
            RealTuple delta = (RealTuple) coord.subtract(lastCoord);
            double[] values = delta.getValues();
            if ((Math.abs(values[0]) > 0.1) || (Math.abs(values[1]) > 0.1)) {
                //System.out.println(delta.longString());
                lastCoord = coord;
                Field latestWx = MesoWestWeatherProvider.getInstance().getLatestWeather(
                        coord, Reals.newDistance(25.0, GeneralUnit.mile), Duration.ofHours(24));
                //System.out.println(curWx.toString());
            }
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
