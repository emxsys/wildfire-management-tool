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
package com.emxsys.wmt.globe;

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.gis.api.event.CursorCoordinateProvider;
import com.emxsys.wmt.gis.api.event.CursorCoordinateEvent;
import com.emxsys.wmt.gis.api.event.CursorCoordinateListener;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateEvent;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateListener;
import com.emxsys.wmt.gis.api.event.ReticuleCoordinateProvider;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.util.Positions;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import javax.swing.event.EventListenerList;

/**
 * The GlobeCoordinateProvider is responsible for notifying listeners upon changes in the cursor's
 * or the reticule's geographical coordinates.
 *
 * @author Bruce Schubert
 * @see CursorCoordinateListener
 * @see ReticuleCoordinateListener
 */
public class GlobeCoordinateProvider implements CursorCoordinateProvider, ReticuleCoordinateProvider {

    private final EventListenerList listenerList = new EventListenerList();
    private Coord3D cursorCoord = GeoCoord3D.INVALID_POSITION;
    private Coord3D reticuleCoord = GeoCoord3D.INVALID_POSITION;

    GlobeCoordinateProvider() {
        WorldWindowGLJPanel wwd = Globe.getInstance().getWorldWindManager().getWorldWindow();
        wwd.addPositionListener(new PositionListener() {

            @Override
            public void moved(PositionEvent pe) {
                GeoCoord3D geoCoord = Positions.toGeoCoord3D(pe.getPosition());
                fireCursorChange(geoCoord);
            }
        });
    }

    @Override
    public void addCursorCoordinateListener(CursorCoordinateListener listener) {
        listenerList.add(CursorCoordinateListener.class, listener);
    }

    @Override
    public void removeCursorCoordinateListener(CursorCoordinateListener listener) {
        listenerList.remove(CursorCoordinateListener.class, listener);
    }

    @Override
    public void addReticuleCoordinateListener(ReticuleCoordinateListener listener) {
        listenerList.add(ReticuleCoordinateListener.class, listener);
    }

    @Override
    public void removeReticuleCoordinateListener(ReticuleCoordinateListener listener) {
        listenerList.remove(ReticuleCoordinateListener.class, listener);
    }

    public void fireCursorChange(Coord3D coord) {
        cursorCoord = coord;
        CursorCoordinateEvent event = null; // lazily create event
        CursorCoordinateListener[] listeners = listenerList.getListeners(CursorCoordinateListener.class);
        if (listeners.length > 0) {
            for (CursorCoordinateListener listener : listeners) {
                if (event == null) {
                    event = new CursorCoordinateEvent(Globe.getInstance(), coord);
                }
                //System.out.println("Firing CursorCoordinateEvent: " + coord);
                listener.updateCoordinate(event);
            }
        }
    }

    public void fireReticuleChange(Object source, Coord3D coord) {
        reticuleCoord = coord;
        ReticuleCoordinateEvent event = null; // lazily create event
        ReticuleCoordinateListener[] listeners = listenerList.getListeners(ReticuleCoordinateListener.class);
        for (ReticuleCoordinateListener listener : listeners) {
            if (event == null) {
                event = new ReticuleCoordinateEvent(source, coord);
            }
            //System.out.println("Firing ReticuleCoordinateEvent: " + coord);
            listener.updateCoordinate(event);
        }
    }

    @Override
    public Coord3D getCursorCoordinate() {
        return cursorCoord;
    }

    @Override
    public Coord3D getReticuleCoordinate() {
        return reticuleCoord;
    }

}
