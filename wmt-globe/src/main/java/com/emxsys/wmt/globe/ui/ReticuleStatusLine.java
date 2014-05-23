/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.ui;

import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.Terrain;
import com.emxsys.gis.api.TerrainProvider;
import com.emxsys.gis.api.event.ReticuleCoordinateEvent;
import com.emxsys.gis.api.event.ReticuleCoordinateListener;
import com.emxsys.gis.api.event.ReticuleCoordinateProvider;
import com.emxsys.wmt.globe.Globe;
import gov.nasa.worldwind.geom.Angle;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.ServiceProvider;
import visad.VisADException;

/**
 * This class displays the current reticule position in the NetBeans status bar.
 *
 * @author Bruce Schubert
 */
public class ReticuleStatusLine implements ReticuleCoordinateListener, Runnable {

    public static JLabel LATITUDE_CELL = new ReticuleStatusLineComponent(ReticuleStatusLineComponent.Type.LATITUDE);
    public static JLabel LONGITUDE_CELL = new ReticuleStatusLineComponent(ReticuleStatusLineComponent.Type.LONGITUDE);
    public static JLabel ELEVATION_CELL = new ReticuleStatusLineComponent(ReticuleStatusLineComponent.Type.ELEVATION);
    public static JLabel ASPECT_CELL = new ReticuleStatusLineComponent(ReticuleStatusLineComponent.Type.ASPECT);
    public static JLabel SLOPE_CELL = new ReticuleStatusLineComponent(ReticuleStatusLineComponent.Type.SLOPE);
    private static final ReticuleStatusLine instance = new ReticuleStatusLine();
    private static final RequestProcessor RP = new RequestProcessor(ReticuleStatusLine.class);
    private static final Logger logger = Logger.getLogger(ReticuleStatusLine.class.getName());
    private final AtomicReference<ReticuleCoordinateEvent> event = new AtomicReference<>(
            new ReticuleCoordinateEvent(this, GeoCoord3D.INVALID_COORD));
    private Task TASK;

    public static ReticuleStatusLine getInstance() {
        return instance;
    }

    /**
     * Call to attach this listener to the ReticuleCoordinateProvider.
     */
    public void initialize() {
        ReticuleCoordinateProvider provider = Globe.getInstance().getLookup().lookup(ReticuleCoordinateProvider.class);
        if (provider == null) {
            logger.warning("A ReticuleCoordinateProvider is not in the Globe lookup. Status line inop.");
            return;
        }
        provider.addReticuleCoordinateListener(instance);
    }

    private ReticuleStatusLine() {
    }

    private static void clearStatusLine() {
        LATITUDE_CELL.setText("");
        LONGITUDE_CELL.setText("");
        ELEVATION_CELL.setText("");
        ASPECT_CELL.setText("");
        SLOPE_CELL.setText("");
    }

    static Component panelWithSeparator(JLabel cell) {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(3, 3); // Y-unimportant -> gridlayout will stretch it
            }
        };
        separator.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(separator, BorderLayout.WEST);
        panel.add(cell);
        return panel;
    }

    @Override
    public void updateCoordinate(ReticuleCoordinateEvent evt) {
        this.event.set(evt);

        if (this.TASK == null) {   // XXX Can't initilize this in constructor w/o throwing Null exception...don't know why.
            this.TASK = RP.create(this, true); // true == initiallyFinished
        }
        // Sliding task: coallese the update events into 100ms intervals
        if (this.TASK.isFinished()) {
            this.TASK.schedule(100); // update in 1/10 of second
        }
    }

    @Override
    public void run() {
        try {
            ReticuleCoordinateEvent evt = event.get();
            if (evt.getCoordinate().isMissing()) {
                clearStatusLine();
                return;
            }
            double[] latDMS = Angle.fromDegrees(evt.getCoordinate().getLatitudeDegrees()).toDMS();
            double sign = Math.signum(evt.getCoordinate().getLatitudeDegrees());
            String latStr = String.format("%2d\u00B0 %5.2f\u2019 %s  (%s)",
                    (int) (latDMS[0] * sign),
                    (latDMS[1] + (latDMS[2] / 60.0)),
                    (sign >= 0.0 ? "N" : "S"),
                    evt.getCoordinate().getLatitude().toValueString());
            LATITUDE_CELL.setText(latStr);
            LATITUDE_CELL.setToolTipText(evt.getCoordinate().getLatitude().longString());

            double[] lonDMS = Angle.fromDegrees(evt.getCoordinate().getLongitudeDegrees()).toDMS();
            sign = Math.signum(evt.getCoordinate().getLongitudeDegrees());
            String lonStr = String.format("%3d\u00B0 %5.2f\u2019 %s  (%s)",
                    (int) (lonDMS[0] * sign),
                    (lonDMS[1] + (lonDMS[2] / 60.0)),
                    (sign >= 0.0 ? "E" : "W"),
                    evt.getCoordinate().getLongitude().toValueString());
            LONGITUDE_CELL.setText(lonStr);
            LONGITUDE_CELL.setToolTipText(evt.getCoordinate().getLongitude().longString());


            TerrainProvider terrainProvider = Globe.getInstance().getLookup().lookup(TerrainProvider.class);
            if (terrainProvider != null) {
                Terrain terrain = terrainProvider.getTerrain(evt.getCoordinate());
                ELEVATION_CELL.setText("Elev " + Long.toString((long) terrain.getElevationFeet()) + " feet");
                ELEVATION_CELL.setToolTipText(terrain.getElevation().longString());
                ASPECT_CELL.setText(terrain.isMissing() ? "" : "Aspect " + terrain.getAspectCardinalPoint8());
                ASPECT_CELL.setToolTipText(terrain.getAspect().longString());
                SLOPE_CELL.setText(terrain.isMissing() ? "" : "Slope " + Long.toString((long) terrain.getSlopePercent()) + "%");
                SLOPE_CELL.setToolTipText(terrain.getSlope().longString());
            }

        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @ServiceProvider(service = StatusLineElementProvider.class, position = 100)
    public static final class Latitude implements StatusLineElementProvider {

        @Override
        public Component getStatusLineElement() {
            return panelWithSeparator(LATITUDE_CELL);
        }
    }

    @ServiceProvider(service = StatusLineElementProvider.class, position = 101)
    public static final class Longitude implements StatusLineElementProvider {

        @Override
        public Component getStatusLineElement() {
            return panelWithSeparator(LONGITUDE_CELL);
        }
    }

    @ServiceProvider(service = StatusLineElementProvider.class, position = 102)
    public static final class Elevation implements StatusLineElementProvider {

        @Override
        public Component getStatusLineElement() {
            return panelWithSeparator(ELEVATION_CELL);
        }
    }

    @ServiceProvider(service = StatusLineElementProvider.class, position = 103)
    public static final class Aspect implements StatusLineElementProvider {

        @Override
        public Component getStatusLineElement() {
            return panelWithSeparator(ASPECT_CELL);
        }
    }

    @ServiceProvider(service = StatusLineElementProvider.class, position = 104)
    public static final class Slope implements StatusLineElementProvider {

        @Override
        public Component getStatusLineElement() {
            return panelWithSeparator(SLOPE_CELL);
        }
    }
}
