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
package com.emxsys.weather.panels;

import com.emxsys.solar.api.Sunlight;
import com.emxsys.util.TimeUtil;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.visad.Times;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import static java.lang.Math.ceil;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.VectorSeriesCollection;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.openide.util.Exceptions;
import visad.DateTime;
import visad.Real;
import visad.RealType;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
class ChartHelper {

    static void createLegend(XYPlot plot) {
        // Customize the legend - place inside plot
        LegendTitle lt = new LegendTitle(plot);
        lt.setItemFont(new Font("Dialog", Font.PLAIN, 9));
        lt.setBackgroundPaint(new Color(200, 200, 255, 100));
        lt.setFrame(new BlockBorder(Color.white));
        lt.setPosition(RectangleEdge.BOTTOM);
        XYTitleAnnotation ta = new XYTitleAnnotation(0.98, 0.98, // coords in data space (0..1)
                lt, RectangleAnchor.TOP_RIGHT);
        ta.setMaxWidth(0.48);
        plot.addAnnotation(ta);
    }

    /**
     * Draws the day/night regions.
     * @param sunlight Contains sunrise and sunset times
     */
    static List<Marker> createNightMarkers(Sunlight sunlight, XYSeriesCollection xyDataset) {
        ArrayList<Marker> markers = new ArrayList<>();

        try {
            DateTime startDate = Times.fromDouble(xyDataset.getDomainLowerBound(true)); // include interval
            DateTime endDate = Times.fromDouble(xyDataset.getDomainUpperBound(true));   // include interval
            Real timeSpan = (Real) endDate.subtract(startDate);

            int numDays = (int) ceil(timeSpan.getValue(GeneralUnit.day));
            for (int i = 0; i < numDays; i++) {
                // Day (between sunrise and sunset)
                Real days = new Real(RealType.Time, i, GeneralUnit.day);
                Real datetime = new DateTime((Real) startDate.add(days));

                double rises = sunlight.getSunriseHour().getValue();    // local hour
                double sets = sunlight.getSunsetHour().getValue();      // local hour
                ZonedDateTime date = Times.toZonedDateTime(datetime);   // UTC zone
                ZonedDateTime local = TimeUtil.toZoneOffset(date, sunlight.getZoneOffsetHour().getValue());
                ZonedDateTime sunrise1 = local.withHour((int) rises).withMinute((int) ((rises * 60) % 60));
                ZonedDateTime sunset1 = local.withHour((int) sets).withMinute((int) ((sets * 60) % 60));
                // compute next day's sunrise
                ZonedDateTime sunrise2 = sunrise1.plusDays(1);
                Marker marker = createIntervalMarker(
                        Times.fromZonedDateTime(sunset1),
                        Times.fromZonedDateTime(sunrise2),
                        "Night",
                        new Color(0, 0, 255, 25));
                markers.add(marker);
            }
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
        return markers;
    }

    static List<Marker> createNightMarkers(Sunlight sunlight, VectorSeriesCollection vecDataset) {
        ArrayList<Marker> markers = new ArrayList<>();

        try {
            double lower = vecDataset.getXValue(0, 0);
            double upper = vecDataset.getXValue(0, vecDataset.getItemCount(0) - 1);
            DateTime startDate = Times.fromDouble(lower);
            DateTime endDate = Times.fromDouble(upper); 
            Real timeSpan = (Real) endDate.subtract(startDate);

            int numDays = (int) ceil(timeSpan.getValue(GeneralUnit.day));
            for (int i = 0; i < numDays; i++) {
                // Day (between sunrise and sunset)
                Real days = new Real(RealType.Time, i, GeneralUnit.day);
                Real datetime = new DateTime((Real) startDate.add(days));

                double rises = sunlight.getSunriseHour().getValue();    // local hour
                double sets = sunlight.getSunsetHour().getValue();      // local hour
                ZonedDateTime date = Times.toZonedDateTime(datetime);   // UTC zone
                ZonedDateTime local = TimeUtil.toZoneOffset(date, sunlight.getZoneOffsetHour().getValue());
                ZonedDateTime sunrise1 = local.withHour((int) rises).withMinute((int) ((rises * 60) % 60));
                ZonedDateTime sunset1 = local.withHour((int) sets).withMinute((int) ((sets * 60) % 60));
                // compute next day's sunrise
                ZonedDateTime sunrise2 = sunrise1.plusDays(1);
                Marker marker = createIntervalMarker(
                        Times.fromZonedDateTime(sunset1),
                        Times.fromZonedDateTime(sunrise2),
                        "Night",
                        new Color(0, 0, 255, 25));
                markers.add(marker);
            }
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
        return markers;
    }

    static List<Marker> createNightMarkers(Sunlight sunlight, NumberAxis domainAxis) {
        ArrayList<Marker> markers = new ArrayList<>();

        try {
            Range range = domainAxis.getRange();
            double lower = range.getLowerBound();
            double upper = range.getUpperBound();
            DateTime startDate = Times.fromDouble(lower);
            DateTime endDate = Times.fromDouble(upper); 
            Real timeSpan = (Real) endDate.subtract(startDate);

            int numDays = (int) ceil(timeSpan.getValue(GeneralUnit.day));
            for (int i = 0; i < numDays; i++) {
                // Day (between sunrise and sunset)
                Real days = new Real(RealType.Time, i, GeneralUnit.day);
                Real datetime = new DateTime((Real) startDate.add(days));

                double rises = sunlight.getSunriseHour().getValue();    // local hour
                double sets = sunlight.getSunsetHour().getValue();      // local hour
                ZonedDateTime date = Times.toZonedDateTime(datetime);   // UTC zone
                ZonedDateTime local = TimeUtil.toZoneOffset(date, sunlight.getZoneOffsetHour().getValue());
                ZonedDateTime sunrise1 = local.withHour((int) rises).withMinute((int) ((rises * 60) % 60));
                ZonedDateTime sunset1 = local.withHour((int) sets).withMinute((int) ((sets * 60) % 60));
                // compute next day's sunrise
                ZonedDateTime sunrise2 = sunrise1.plusDays(1);
                Marker marker = createIntervalMarker(
                        Times.fromZonedDateTime(sunset1),
                        Times.fromZonedDateTime(sunrise2),
                        "Night",
                        new Color(0, 0, 255, 25));
                markers.add(marker);
            }
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
        return markers;
    }

    /**
     * Create a marker band used to depict daytime or nighttime.
     */
    static Marker createIntervalMarker(DateTime begin, DateTime end,
                                       String label, Color color) {
        IntervalMarker marker
                = new IntervalMarker(
                        begin.getValue(),
                        end.getValue(),
                        color, new BasicStroke(1.0f), null, null, 1.0f);
        marker.setLabel(label);
        marker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
        marker.setLabelFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 9));
        marker.setLabelTextAnchor(TextAnchor.BASELINE_LEFT);
        return marker;
    }

}
