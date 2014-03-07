/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.solar.internal;

//import com.emxsys.wmt.solar.api.Solar;
//import com.emxsys.wmt.solar.api.SolarType;
//import com.emxsys.wmt.solar.spi.SolarObserver;
//import com.emxsys.wmt.solar.spi.SolarFactory;
//import com.emxsys.wmt.visad.Times;
////import com.emxsys.time.spi.TimeController;
//import com.emxsys.wmt.globe.events.CrosshairsEvent;
//import com.emxsys.wtm.globe.events.CrosshairsListener;
//import com.emxsys.worldwind.util.ViewerUtil;
//import gov.nasa.worldwind.geom.Position;
//import java.awt.EventQueue;
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//import java.beans.PropertyChangeSupport;
//import java.util.Calendar;
//import org.openide.windows.WindowManager;
//import visad.DateTime;
//import visad.Real;
//import visad.RealType;
//
//
///**
// *
// */
//public class WorldWindSolarObserver extends SolarObserver
//{
//    private Calendar calendar;
//    private Real latitude;
//    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
//    /**
//     * Listens to changes in the TimeController.
//     */
//    private PropertyChangeListener timeListener = new PropertyChangeListener()
//    {
//        @Override
//        public void propertyChange(PropertyChangeEvent evt)
//        {
//            if (TimeController.PROP_TIMER_DATETIME.equals(evt.getPropertyName()))
//            {
//                Calendar c = Calendar.getInstance();
//                c.setTime(Times.toDate((DateTime) evt.getNewValue()));
//                if (calendar.get(Calendar.DAY_OF_YEAR) != c.get(Calendar.DAY_OF_YEAR))
//                {
//                    calendar = c;
//                    updateSolarAndNotify();
//                }
//            }
//        }
//    };
//    /**
//     * Listens to changes in the position under the Crosshairs.
//     */
//    private CrosshairsListener terrainListener = new CrosshairsListener()
//    {
//        private Position lastPosition = Position.ZERO;
//
//
//        @Override
//        public void moved(CrosshairsEvent evt)
//        {
//            Position position = evt.getPosition();
//            Position delta = position.subtract(lastPosition);
//            if (Math.abs(delta.latitude.degrees) > 0.5)
//            {
//                latitude = new Real(RealType.Latitude, delta.latitude.degrees);
//                updateSolarAndNotify();
//            }
//        }
//    };
//
//
//    public WorldWindSolarObserver()
//    {
//        this.calendar = Calendar.getInstance();
//        this.latitude = new Real(SolarType.LATITUDE, 0.0);
//
//        // WorldWind may not up and running when this constructor is called, 
//        // so defer initialization until then.
//        WindowManager.getDefault().invokeWhenUIReady(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                // Register a listener on the TimeControl
//                TimeController.getInstance().addPropertyChangeListener(timeListener);
//
//                // Register a listener on the WorldWind GIS Viewer
//                ViewerUtil.getViewerFromLookup().addCrosshairsListener(terrainListener);
//            }
//        });
//
//    }
//
//
//    private void updateSolarAndNotify()
//    {
//        EventQueue.invokeLater(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                Solar solar = SolarFactory.getInstance().newSolar(latitude, calendar.getTime());
//                pcs.firePropertyChange(PROP_SOLAR_VALUE, null, solar);
//            }
//        });
//    }
//
//
//    @Override
//    public void addPropertyChangeListener(PropertyChangeListener listener)
//    {
//        pcs.addPropertyChangeListener(listener);
//        updateSolarAndNotify();
//    }
//
//
//    @Override
//    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
//    {
//        pcs.addPropertyChangeListener(propertyName, listener);
//        updateSolarAndNotify();
//    }
//
//
//    @Override
//    public void removePropertyChangeListener(PropertyChangeListener listener)
//    {
//        pcs.removePropertyChangeListener(listener);
//    }
//}
