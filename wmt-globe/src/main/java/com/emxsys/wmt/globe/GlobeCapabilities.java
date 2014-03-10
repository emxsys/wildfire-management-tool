/*
 * Copyright (c) 2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe;

import com.emxsys.wmt.core.capabilities.PanDownCapability;
import com.emxsys.wmt.core.capabilities.PanLeftCapability;
import com.emxsys.wmt.core.capabilities.PanRightCapability;
import com.emxsys.wmt.core.capabilities.PanUpCapability;
import com.emxsys.wmt.core.capabilities.RotateCcwCapability;
import com.emxsys.wmt.core.capabilities.RotateCwCapability;
import com.emxsys.wmt.core.capabilities.TiltBackCapability;
import com.emxsys.wmt.core.capabilities.TiltUpCapability;
import com.emxsys.wmt.core.capabilities.ZoomInCapability;
import com.emxsys.wmt.core.capabilities.ZoomOutCapability;
import com.terramenta.globe.WorldWindManager;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.orbit.OrbitView;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;

/**
 * GlobeCapabilities is used to couple "capabilities" with ribbon/menu actions. This class is a
 * composite of many discrete capabilities. An instance of this class should be added to the
 * WorldWindManager's lookup.
 * <p>
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: ViewerCapabilities.java 393 2012-12-08 21:10:58Z bdschubert $
 */
@NbBundle.Messages({
    "ERR_NullWorldWindow=The WorldWindow cannot be null.",
    "# {0} - view class",
    "ERR_NotAnOrbitView=The WorldWindow view ({0}) must be an OrbitView.",})
public class GlobeCapabilities implements PanUpCapability, PanDownCapability,
        PanLeftCapability, PanRightCapability,
        ZoomInCapability, ZoomOutCapability,
        RotateCcwCapability, RotateCwCapability,
        TiltUpCapability, TiltBackCapability {

    public static final double DEFAULT_PAN_STEP = 0.8;
    public static final double DEFAULT_TILT_STEP = 0.8;
    public static final double DEFAULT_ZOOM_STEP = 0.8;
    public static final double DEFAULT_HEADING_STEP = 1.0;
    private final WorldWindManager wwm;

    public GlobeCapabilities(WorldWindManager wwm) {
        this.wwm = wwm;
    }

    @Override
    public void panUp(ActionEvent event) {
        panMap(Angle.fromDegrees(0.0), DEFAULT_PAN_STEP);
    }

    @Override
    public void panDown(ActionEvent event) {
        panMap(Angle.fromDegrees(180.0), DEFAULT_PAN_STEP);
    }

    @Override
    public void panLeft(ActionEvent event) {
        panMap(Angle.fromDegrees(270.0), DEFAULT_PAN_STEP);
    }

    @Override
    public void panRight(ActionEvent event) {
        panMap(Angle.fromDegrees(90.0), DEFAULT_PAN_STEP);
    }

    @Override
    public void zoomIn(ActionEvent event) {
        zoomMap(-DEFAULT_ZOOM_STEP);
    }

    @Override
    public void zoomOut(ActionEvent event) {
        zoomMap(DEFAULT_ZOOM_STEP);
    }

    @Override
    public void rotateCounterClockwise(ActionEvent event) {
        rotateMap(Angle.fromDegrees(-DEFAULT_HEADING_STEP));
    }

    @Override
    public void rotateClockwise(ActionEvent event) {
        rotateMap(Angle.fromDegrees(DEFAULT_HEADING_STEP));
    }

    @Override
    public void tiltUp(ActionEvent event) {
        tiltMap(Angle.fromDegrees(-DEFAULT_TILT_STEP));
    }

    @Override
    public void tiltBack(ActionEvent event) {
        tiltMap(Angle.fromDegrees(DEFAULT_TILT_STEP));
    }

    protected void panMap(Angle direction, double panStep) {
        WorldWindowGLJPanel wwd = wwm.getWorldWindow();
        if (wwd == null) {
            throw new IllegalStateException(Bundle.ERR_NullWorldWindow());
        }
        View view = wwd.getView();
        if (!(view instanceof OrbitView)) {
            throw new IllegalStateException(Bundle.ERR_NotAnOrbitView(view.getClass()));
        }
        OrbitView orbitView = (OrbitView) view;
        Angle heading = orbitView.getHeading().add(direction);
        Angle distance = computePanAmount(wwd.getModel().getGlobe(), orbitView, panStep);
        LatLon newViewCenter = LatLon.greatCircleEndPosition(orbitView.getCenterPosition(), heading, distance);
        // Turn around if passing by a pole - TODO: better handling of the pole crossing situation
        if (this.isPathCrossingAPole(newViewCenter, orbitView.getCenterPosition())) {
            orbitView.setHeading(Angle.POS180.subtract(orbitView.getHeading()));
        }
        // Set new center pos
        orbitView.setCenterPosition(new Position(newViewCenter, orbitView.getCenterPosition().getElevation()));
        wwd.redrawNow();

    }

    protected void rotateMap(Angle amount) {
        WorldWindowGLJPanel wwd = wwm.getWorldWindow();
        if (wwd == null) {
            throw new IllegalStateException(Bundle.ERR_NullWorldWindow());
        }
        View view = wwd.getView();
        view.setHeading(view.getHeading().add(amount));
        wwd.redrawNow();
    }

    protected void tiltMap(Angle amount) {
        WorldWindowGLJPanel wwd = wwm.getWorldWindow();
        if (wwd == null) {
            throw new IllegalStateException(Bundle.ERR_NullWorldWindow());
        }
        View view = wwd.getView();
        if (!(view instanceof OrbitView)) {
            throw new IllegalStateException(Bundle.ERR_NotAnOrbitView(view.getClass()));
        }
        OrbitView orbitView = (OrbitView) view;
        orbitView.setPitch(orbitView.getPitch().add(amount));
        wwd.redrawNow();
    }

    protected void zoomMap(double amount) {
        WorldWindowGLJPanel wwd = wwm.getWorldWindow();
        if (wwd == null) {
            throw new IllegalStateException(Bundle.ERR_NullWorldWindow());
        }
        View view = wwd.getView();
        if (!(view instanceof OrbitView)) {
            throw new IllegalStateException(Bundle.ERR_NotAnOrbitView(view.getClass()));
        }
        OrbitView orbitView = (OrbitView) view;
        orbitView.setZoom(computeNewZoom(orbitView, amount));
        wwd.redrawNow();
    }

    protected Angle computePanAmount(Globe globe, OrbitView view, double panStep) {
        // This logic was copied from the WorldWind Java SDK: ViewControlsSelectListener by Patrick Murris

        // Compute globe angular distance depending on eye altitude
        Position eyePos = view.getEyePosition();
        double radius = globe.getRadiusAt(eyePos);
        double minValue = 0.5 * (180.0 / (Math.PI * radius)); // Minimum change ~0.5 meters
        double maxValue = 1.0; // Maximum change ~1 degree

        // Compute an interpolated value between minValue and maxValue, using (eye altitude)/(globe radius) as
        // the interpolant. Interpolation is performed on an exponential curve, to keep the value from
        // increasing too quickly as eye altitude increases.
        double a = eyePos.getElevation() / radius;
        a = (a < 0 ? 0 : (a > 1 ? 1 : a));
        double expBase = 2.0; // Exponential curve parameter.
        double value = minValue + (maxValue - minValue) * ((Math.pow(expBase, a) - 1.0) / (expBase - 1.0));

        // return distance from center point
        return Angle.fromDegrees(value * panStep);
    }

    protected boolean isPathCrossingAPole(LatLon p1, LatLon p2) {
        return Math.abs(p1.getLongitude().degrees - p2.getLongitude().degrees) > 20
                && Math.abs(p1.getLatitude().degrees - 90 * Math.signum(p1.getLatitude().degrees)) < 10;
    }

    protected double computeNewZoom(OrbitView view, double amount) {
        // This logic was copied from the WorldWind Java SDK: ViewControlsSelectListener by Patrick Murris
        double coeff = 0.05;
        double change = coeff * amount;
        double logZoom = view.getZoom() != 0 ? Math.log(view.getZoom()) : 0;
        // Zoom changes are treated as logarithmic values. This accomplishes two things:
        // 1) Zooming is slow near the globe, and fast at great distances.
        // 2) Zooming in then immediately zooming out returns the viewer to the same zoom value.    
        return Math.exp(logZoom + change);

    }

}
