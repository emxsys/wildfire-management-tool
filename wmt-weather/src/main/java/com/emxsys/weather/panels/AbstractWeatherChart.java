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
import static com.emxsys.weather.panels.ChartHelper.createIntervalMarker;
import java.awt.Color;
import java.awt.Font;
import static java.lang.Math.ceil;
import java.rmi.RemoteException;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.openide.util.Exceptions;
import visad.DateTime;
import visad.FunctionType;
import visad.MathType;
import visad.Real;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public abstract class AbstractWeatherChart extends JFreeChart {

    /** Current time marker */
    protected ValueMarker domainMarker;
    /** Day/Night markers */
    protected List<Marker> markers;
    /** Member that determines sunrise and sunset times. */
    protected Sunlight sunlight;

    protected AbstractWeatherChart(Plot plot) {
        super(null, null, plot, false);
        createDomainMarker();
        createLegend();
    }

    /**
     * Create a marker for the datetime.
     */
    private void createDomainMarker() {
        XYPlot plot = (XYPlot) getPlot();
        domainMarker = new ValueMarker(0);
        domainMarker.setPaint(Color.black);
        domainMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        domainMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        plot.addDomainMarker(domainMarker, Layer.FOREGROUND);
    }

    /**
     * Create a custom legend displayed in the upper right region of the plot.
     */
    private void createLegend() {
        // Customize the legend - place inside plot
        XYPlot plot = (XYPlot) getPlot();
        LegendTitle lt = new LegendTitle(plot);
        lt.setItemFont(new Font("Dialog", Font.PLAIN, 9));
        lt.setBackgroundPaint(new Color(200, 200, 255, 100));
        lt.setFrame(new BlockBorder(Color.white));
        lt.setPosition(RectangleEdge.BOTTOM);
        XYTitleAnnotation ta = new XYTitleAnnotation(0.98, 0.98, // coords in data space (0..1)
                lt, RectangleAnchor.TOP_RIGHT);
        ta.setMaxWidth(0.9);
        plot.addAnnotation(ta);
    }

    /**
     * Draws a marker at the supplied datetime
     * @param datetime Value to highlight
     */
    public void setDateTime(ZonedDateTime datetime) {
        double value = datetime == null ? 0 : datetime.toEpochSecond();
        String label = datetime == null ? "" : datetime.toString();
        domainMarker.setValue(value);
        domainMarker.setLabel(label);
        XYPlot plot = (XYPlot) getPlot();
        final int SECS_IN_DAY = 3600 * 24;
        Range range = new Range(value - SECS_IN_DAY, value + SECS_IN_DAY); // 48 hour default range
        plot.getDomainAxis().setRange(range);
        plot.getDomainAxis().centerRange(value);
    }

    protected int findRangeComponentIndex(FunctionType functionType, RealType componentType) {
        if (!functionType.getReal()) {
            throw new IllegalArgumentException("Range must be RealType or RealTypeTuple");
        }
        MathType rangeType = functionType.getRange();
        int index = -1;
        if (rangeType instanceof RealTupleType) {
            index = ((RealTupleType) rangeType).getIndex(componentType);
        } else {
            index = rangeType.equals(componentType) ? 0 : -1;
        }
        return index;
    }

    /**
     * Draws the day/night regions.
     */
    void plotDayNight() {
        XYPlot plot = (XYPlot) getPlot();
        if (markers != null) {
            for (Marker marker : markers) {
                plot.removeDomainMarker(marker, Layer.BACKGROUND);
            }
            markers.clear();
        }
        if (sunlight == null) {
            return;
        }
        markers = ChartHelper.createNightMarkers(sunlight, (XYSeriesCollection) plot.getDataset());
        for (Marker marker : markers) {
            plot.addDomainMarker(marker, Layer.BACKGROUND);
        }
    }

    public void setSunlight(Sunlight sunlight) {
        this.sunlight = sunlight;
        plotDayNight();
    }

    public static class DateTimeToolTipGenerator extends StandardXYToolTipGenerator {

        public DateTimeToolTipGenerator() {
            super();
        }

        @Override
        public String generateToolTip(XYDataset dataset, int series, int item) {
            double x = dataset.getXValue(series, item);
            double y = dataset.getYValue(series, item);
            try {
                DateTime dateTime = new DateTime(x);
                NumberFormat yf = getYFormat();
                return yf.format(y) + " at " + dateTime.toString();
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Displays VisAD DateTime values.
     */
    public static final class DateTimeAxis extends NumberAxis {

        public DateTimeAxis(String label) {
            super(label);
            this.setAutoRange(true);
            this.setAutoRangeIncludesZero(false);
            this.setStandardTickUnits(createTickUnits());
            this.setMinorTickMarksVisible(true);
        }

        /**
         * Returns a collection of tick units for hours expressed in seconds.
         */
        TickUnitSource createTickUnits() {
            TickUnits units = new TickUnits();
            units.add(new NumberTickUnit(3600, new DateTimeFormat(), 0));       // 1hour
            units.add(new NumberTickUnit(3600 * 3, new DateTimeFormat(), 3));     // 3hour
            units.add(new NumberTickUnit(3600 * 6, new DateTimeFormat(), 6));     // 6hour
            return units;
        }
    }

    /**
     * Formats VisAD DateTime values
     */
    public static final class DateTimeFormat extends NumberFormat {

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            try {
                // TODO: switch to ZonedDateTime and format to time preferences.
                double local24HourTime = Times.toClockTime(new DateTime(number));
                long hour = Math.round(local24HourTime);
                if (hour == 0) {
                    toAppendTo.append("mid");
                } else if (hour == 12) {
                    toAppendTo.append("noon");
                } else {
                    toAppendTo.append(Math.round(local24HourTime));
                }
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
            return toAppendTo;
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Number parse(String source, ParsePosition parsePosition) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    /**
     * Draws the day/night regions.
     * @param sunlight Contains sunrise and sunset times
     */
    
}
