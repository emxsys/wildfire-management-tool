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
import com.emxsys.wmt.globe.render.SimplePlacemark;
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
import visad.Real;
import visad.RealTuple;
import visad.VisADException;

/**
 * A RenderableGisLayer for displaying MesoWestPlacemark markers. This layer is not created by the
 * normal XML MapLayerRegistration registration process due to dependency issues between the
 * modules. Instead, this class is added to Globe via the module Installer.
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
    private Field lastestWxField;

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
        if (enabled) {
            // Lazy initialization / activation of listener
            if (coordProvider == null) {
                coordProvider = Globe.getInstance().getLookup().lookup(ReticuleCoordinateProvider.class);
                if (coordProvider != null) {
                    coordProvider.addReticuleCoordinateListener((ReticuleCoordinateEvent evt) -> {
                        processEvent(evt);
                    });
                }
            }
        }
    }

    /**
     * Coallese and process ReticuleCoordinateEvent events with a task processor.
     * @param evt
     */
    private void processEvent(ReticuleCoordinateEvent evt) {
        // Save this most recent event
        coordEvent.set(evt);
        
        // Lazy task initialization
        if (updateWeatherTask == null) {
            updateWeatherTask = taskProcessor.create(() -> {
                updateWeather();
            }, true); // true = initiallyFinished state
        }
        
        // Sliding task: coallese the updates into 1 second intervals
        // by 
        if (updateWeatherTask.isFinished()) {
            updateWeatherTask.schedule(1000); // update in 1000 milli second
        }
    }

    /**
     * Update the weather field from the last coordinate.
     */
    private void updateWeather() {
        try {
            Coord3D coord = coordEvent.get().getCoordinate();
            if (coord.isMissing()) {
                return;
            }
            // Test to see if the coord has moved far enough to warrent a new query
            Real distance = Globe.computeGreatCircleDistance(coord, lastCoord);
            if (distance.getValue(GeneralUnit.mile) < 5.0) {
                return;
            }
            // Get the latest weather within the last 24 hours
            lastestWxField = MesoWestWeatherProvider.getInstance().getLatestWeather(
                    coord, Reals.newDistance(25.0, GeneralUnit.mile), Duration.ofHours(24));           
            updateRenderables();
            lastCoord = coord;
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void updateRenderables() {
        try {
            clearRenderables();
            double[][] latLons = lastestWxField.getDomainSet().getDoubles(false); // Don't copy
            for (int i = 0; i < latLons[0].length; i++) {
                GeoCoord3D coord = GeoCoord3D.fromDegrees(latLons[0][i], latLons[1][i]);
                RealTuple wx = (RealTuple)lastestWxField.getSample(i);
                double[] values = wx.getValues();
                String label = wx.isMissing() ? "missing" : String.format("T: %1$.0f, RH: %2$.0f", values[0], values[1]);

                this.addRenderable(new SimplePlacemark(coord, label));
            }
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
}
