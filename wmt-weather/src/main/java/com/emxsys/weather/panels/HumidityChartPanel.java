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
import com.emxsys.visad.Times;
import com.emxsys.weather.api.WeatherType;
import java.awt.Color;
import java.awt.Font;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.DateTime;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

/**
 * This class displays the general weather in an JFreeChart XY graph complete with titles and
 * legends.
 *
 * @author Bruce Schubert
 */
@Messages({
    "CTL_HumidityChartDomain=Time",
    "CTL_HumidityChartHumidity=Rel. Humidty (%)",
    "CTL_HumidityChartClouds=Sky Cover (%)",})
public class HumidityChartPanel extends ChartPanel {

    private HumidityChart chart;

    /**
     * Constructor creates new form HumidityChartPanel.
     */
    public HumidityChartPanel() {
        this(new HumidityChart());
    }

    HumidityChartPanel(HumidityChart chart) {
        super(chart,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                200, // DEFAULT_MINIMUM_DRAW_WIDTH, // Default = 300
                DEFAULT_MINIMUM_DRAW_HEIGHT,
                DEFAULT_MAXIMUM_DRAW_WIDTH,
                DEFAULT_MAXIMUM_DRAW_HEIGHT,
                DEFAULT_BUFFER_USED,
                true, // properties
                true, // save
                true, // print
                true, // zoom
                true); // tooltips

        this.chart = chart;

        initComponents();
        // Setting the preferred size allows us to control the initial size
        // of the panel when it's dragged-n-dropped in the NetBeans GUI editor.
        setPreferredSize(new java.awt.Dimension(350, 150));

    }

    public void setTitle(String title) {
        this.chart.setTitle(title);
    }

    public void addSubTitle(String subtitle) {
        this.chart.addSubtitle(new TextTitle(subtitle));
    }

    public void clearSubTitles() {
        this.chart.clearSubtitles();
    }

    public void setCloudCover(FlatField ff) {
        this.chart.plotClouds(ff);
    }

    public void setHumidities(FlatField ff) {
        this.chart.plotHumidities(ff);
    }

    public void setSunlight(Sunlight sunlight) {
        this.chart.setSunlight(sunlight);
    }

    public void refresh() {
        this.chart.setNotify(true);
    }

    /**
     * The HumidityChart is a JFreeChart with a specialized XYPlot for displaying relative humidity,
     * sky cover and day/night.
     */
    public static class HumidityChart extends JFreeChart {

        /** Dataset for clouds and humidity */
        private XYSeriesCollection xyDataset;
        /** Relative humidity */
        private XYSeries seriesH;
        /** Clouds */
        private XYSeries seriesC;
        /** Day/Night markers */
        private List<Marker> markers;
        private Sunlight sunlight;

        /**
         * Constructor for a HumidityChart.
         */
        public HumidityChart() {
            this(new XYSeriesCollection());
        }

        /**
         * Constructor implementation.
         * @param xyDataset
         */
        HumidityChart(XYSeriesCollection xyDataset) {
            super(
                    null, // title
                    null, // title font
                    new HumidityPlot(xyDataset),
                    false); // don't create default legend

            seriesH = new XYSeries(Bundle.CTL_HumidityChartHumidity());
            seriesC = new XYSeries(Bundle.CTL_HumidityChartClouds());

            this.xyDataset = xyDataset;
            this.xyDataset.addSeries(seriesH);
            this.xyDataset.addSeries(seriesC);

            // Customize the legend - place inside plot
            HumidityPlot plot = (HumidityPlot) getPlot();
            LegendTitle lt = new LegendTitle(plot);
            lt.setItemFont(new Font("Dialog", Font.PLAIN, 9));
            lt.setBackgroundPaint(new Color(200, 200, 255, 100));
            lt.setFrame(new BlockBorder(Color.white));
            lt.setPosition(RectangleEdge.BOTTOM);
            XYTitleAnnotation ta = new XYTitleAnnotation(0.98, 0.98, // coords in data space (0..1)
                    lt, RectangleAnchor.TOP_RIGHT);
            ta.setMaxWidth(0.90);
            plot.addAnnotation(ta);
        }

        /**
         * Plot the relative humidity. This is the primary dataset.
         * @param weather (hour -> (..., REL_HUMIDITY, ...))
         */
        public void plotHumidities(FlatField weather) {
            seriesH.clear();
            try {
                FunctionType functionType = (FunctionType) weather.getType();
                int index = findRangeComponentIndex(functionType, WeatherType.REL_HUMIDITY);
                if (index == -1) {
                    throw new IllegalArgumentException("FlatField must contain REL_HUMIDITY.");
                }

                final float[][] times = weather.getDomainSet().getSamples(false);
                final float[][] values = weather.getFloats(false);

                for (int i = 0; i < times[0].length; i++) {
                    seriesH.add(times[0][i], values[index][i]);
                }
                plotDayNight();

            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         * Plot the cloud cover in the chart. Clouds are the secondary dataset.
         * @param weather (hour -> (..., CLOUD_COVER, ...))
         */
        public void plotClouds(FlatField weather) {
            seriesC.clear();
            try {
                // TODO test math types for compatablity and tuple index
                FunctionType functionType = (FunctionType) weather.getType();
                int index = findRangeComponentIndex(functionType, WeatherType.CLOUD_COVER);
                if (index == -1) {
                    throw new IllegalArgumentException("FlatField must contain CLOUD_COVER.");
                }

                final float[][] times = weather.getDomainSet().getSamples(false);
                final float[][] values = weather.getFloats(false);

                for (int i = 0; i < times[0].length; i++) {
                    float value = values[index][i];
                    seriesC.add(times[0][i], value);
                }

            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void setSunlight(Sunlight sunlight) {
            this.sunlight = sunlight;
            plotDayNight();
        }

        /**
         * Draws the day/night regions.
         */
        void plotDayNight() {
            HumidityPlot plot = (HumidityPlot) getPlot();
            if (markers != null) {
                for (Marker marker : markers) {
                    plot.removeDomainMarker(marker, Layer.BACKGROUND);
                }
                markers.clear();
            }
            if (sunlight == null) {
                return;
            }

            markers = ChartHelper.createNightMarkers(sunlight, xyDataset);
            for (Marker marker : markers) {
                plot.addDomainMarker(marker, Layer.BACKGROUND);
            }
        }

        private int findRangeComponentIndex(FunctionType functionType, RealType componentType) {
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
    }

    private static class HumidityPlot extends XYPlot {

        HumidityPlot(XYDataset dataset) {
            super(dataset,
                    new DateTimeAxis(Bundle.CTL_HumidityChartDomain()),
                    new NumberAxis(),
                    new XYLineAndShapeRenderer()); // XYSplineRenderer());

            // Customize the RH / Cloud Cover range
            NumberAxis rangeAxis1 = (NumberAxis) getRangeAxis();
            rangeAxis1.setAutoRange(false);
            rangeAxis1.setRange(0, 100);

            // Customize the renderer for Temp and RH
            XYItemRenderer xyRenderer = getRenderer();
            xyRenderer.setBaseToolTipGenerator(new DateTimeToolTipGenerator());
            xyRenderer.setSeriesPaint(0, new Color(0, 128, 0)); // dark green for humidty
            xyRenderer.setSeriesPaint(1, new Color(0, 0, 128)); // dark blue for cloud cover

            setDomainCrosshairVisible(true);
            setRangeCrosshairVisible(true);
            setDomainZeroBaselineVisible(true);
            setRangeZeroBaselineVisible(true);
            setBackgroundPaint(Color.lightGray);
            setDomainGridlinePaint(Color.white);
            setRangeGridlinePaint(Color.white);
            setAxisOffset(new RectangleInsets(4, 4, 4, 4));
            setOutlinePaint(Color.darkGray);
        }

    }

    static class DateTimeToolTipGenerator extends StandardXYToolTipGenerator {

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
    private static final class DateTimeFormat extends NumberFormat {

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

    /** This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
