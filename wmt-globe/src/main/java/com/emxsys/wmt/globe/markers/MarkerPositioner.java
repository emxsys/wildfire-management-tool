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
package com.emxsys.wmt.globe.markers;

import com.emxsys.wmt.globe.util.Positions;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * The MarkerPositioner will set the position of a marker at a clicked position on the globe.
 *
 * Modeled after the Terramenta Annotations AnnotationBuilder.
 * @author Bruce Schubert
 */
public class MarkerPositioner {

    private final BasicMarker marker;
    private final WorldWindow wwd;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean armed = false;
    private boolean canceled = false;
    private static final ArrayList<WeakReference<MarkerPositioner>> instances = new ArrayList<>();

    private final KeyAdapter ka = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent keyEvent) {
            if (armed && keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                cancel();
            }
        }
    };
    private final MouseAdapter ma = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
                if (armed && (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
                    updatePosition();
                }
                mouseEvent.consume();
            }
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
                mouseEvent.consume();
                setArmed(false);
            }
        }
    };

    /**
     * Construct a new marker positioner.
     * @param wwd The world window to get the position from.
     * @param marker The marker to be placed.
     */
    public MarkerPositioner(final WorldWindow wwd, BasicMarker marker) {
        this.wwd = wwd;
        this.marker = marker;
//        instances.add(new WeakReference<>(this));
    }

    /**
     * Arms and disarms the positioner. When armed, the positioner monitors the cursor position and
     * sets the marker at the clicked position.
     * @param shouldArm Pass true to arm the positioner, false to disarm it.
     */
    public void setArmed(boolean shouldArm) {
        if (shouldArm) {
            // Disarm all other positioners
//            for (WeakReference<MarkerPositioner> instance : instances) {
//                MarkerPositioner mp = instance.get();
//                if (mp != null && mp != this) {
//                    mp.cancel();
//                }
//            }
            this.wwd.getInputHandler().addKeyListener(ka);
            this.wwd.getInputHandler().addMouseListener(ma);
            ((Component) wwd).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            this.wwd.getInputHandler().removeKeyListener(ka);
            this.wwd.getInputHandler().removeMouseListener(ma);
            ((Component) wwd).setCursor(Cursor.getDefaultCursor());
        }
        pcs.firePropertyChange("armed", armed, armed = shouldArm);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    private void updatePosition() {
        Position curPos = this.wwd.getCurrentPosition();
        if (curPos == null) {
            return;
        }
        this.marker.setPosition(Positions.toGeoCoord3D(curPos));
    }

    public void cancel() {
        canceled = true;
        setArmed(false);
    }
    
    public boolean isCanceled() {
        return canceled;
    }
}
