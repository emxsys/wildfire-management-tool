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

import com.emxsys.visad.GeneralUnit;
import com.emxsys.visad.Times;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.weather.api.WeatherPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.prefs.PreferenceChangeEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.DateTime;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;

/**
 * This class displays the air temperature weather in an JFreeChart XY graph complete with titles and
 * legends.
 *
 * @author Bruce Schubert
 */
@Messages({
    "CTL_TemperatureChartDomain=Time",
    "CTL_TemperatureChartRange=Temperature",
    "CTL_TemperatureChartLegend=Temperature",})
public class TemperatureChartPanel extends ChartPanel {

    private TemperatureChart chart;

    /**
     * Constructor creates new form TemperatureChartPanel.
     */
    public TemperatureChartPanel() {
        this(new TemperatureChart());
    }

    TemperatureChartPanel(TemperatureChart chart) {
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

    public void setTemperatures(FlatField ff) {
        this.chart.plotTemperatures(ff);
    }

    public void refresh() {
        this.chart.setNotify(true);
    }

    /**
     * The TemperatureChart is a JFreeChart with a specialized XYPlot for displaying temperature,
     * humidity, winds and day/night.
     */
    public static class TemperatureChart extends JFreeChart {

        private Unit airTempUnit;


        /** Dataset for temperature and humidity */
        private XYSeriesCollection dataset;
        /** Air temperature */
        private XYSeries series;
        /** Day/Night markers */
        private ArrayList<Marker> markers = new ArrayList<>();

        /**
         * Constructor for a TemperatureChart.
         */
        public TemperatureChart() {
            this(new XYSeriesCollection());
        }

        /**
         * Constructor implementation.
         * @param xyDataset
         * @param vecDataset
         */
        TemperatureChart(XYSeriesCollection dataset) {
            super(new TemperaturePlot(dataset));

            this.series = new XYSeries(Bundle.CTL_TemperatureChartLegend());
            this.dataset = dataset;
            this.dataset.addSeries(series);

            this.airTempUnit = WeatherPreferences.getAirTempUnit();
            WeatherPreferences.addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
                switch (evt.getKey()) {
                    case WeatherPreferences.PREF_AIR_TEMP_UOM:
                        setAirTempUnit(WeatherPreferences.getAirTempUnit());
                        break;
                }
            });            
            this.removeLegend();
        }

        public final void setAirTempUnit(Unit newUnit) {
            if (this.airTempUnit.equals(newUnit)) {
                return;
            }
            try {
                // Refresh the temperature series with the new Unit of measure
                XYSeries oldSeries = (XYSeries) series.clone();
                series.clear();
                for (int i = 0; i < oldSeries.getItemCount(); i++) {
                    try {
                        XYDataItem item = oldSeries.getDataItem(i);
                        // convert item to new unit of measure
                        item.setY(airTempUnit.toThat(item.getYValue(), newUnit));
                        series.add(item);
                    } catch (UnitException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                // Now set the new air temp unit property
                this.airTempUnit = newUnit;
                this.series.fireSeriesChanged();
            } catch (CloneNotSupportedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         * Plot the supplied temperatures in the chart.
         * @param weather (hour -> (temperature))
         */
        public void plotTemperatures(FlatField weather) {
            series.clear();
            try {
                // TODO test math types for compatablity and tuple index
                FunctionType functionType = (FunctionType) weather.getType();
                int index = findRangeComponentIndex(functionType, WeatherType.AIR_TEMP_F);
                Unit unit = GeneralUnit.degF;
                if (index == -1) {
                    index = findRangeComponentIndex(functionType, WeatherType.AIR_TEMP_C);
                    unit = GeneralUnit.degC;
                }
                if (index == -1) {
                    throw new IllegalArgumentException("FlatField must contain AIR_TEMP_C or AIR_TEMP_F.");
                }

                final float[][] times = weather.getDomainSet().getSamples(false);
                final float[][] values = weather.getFloats(false);

                for (int i = 0; i < times[0].length; i++) {
                    // Add values to the series in the preferred UOM
                    float value = values[index][i];
                    series.add(times[0][i], airTempUnit.equals(unit)
                            ? value
                            : airTempUnit.toThis(value, unit));
                }
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         *
         * @param solarData FunctionType: ( time, latitude ) -> ( declination, sunrise, sunset )
         */
        public void plotDayNight(FlatField solarData) {
//            XYPlot plot = (XYPlot) getPlot();
//            for (Marker marker : markers) {
//                plot.removeDomainMarker(marker, Layer.BACKGROUND);
//            }
//            markers.clear();
//            try {
//                Set domainSet = solarData.getDomainSet();
//                int length = domainSet.getLength();
//                RealTuple sample1 = DataUtility.getSample(domainSet, 0);
//                RealTuple sample2 = DataUtility.getSample(domainSet, length - 1);
//
//                Real lat = (Real) sample1.getComponent(1);
//                Real startDate = (Real) sample1.getComponent(0);
//                Real endDate = (Real) sample2.getComponent(0);
//                Real timeSpan = (Real) endDate.subtract(startDate);
//                int numDays = (int) timeSpan.getValue(GeneralUnit.day);
//                for (int i = 0; i < numDays; i++) {
//                    // Day (between sunrise and sunset)
//                    Real days = new Real(RealType.Time, i, GeneralUnit.day);
//                    Real datetime = new DateTime((Real) startDate.add(days));
//
//                    Date date = Times.toDate(datetime)
//                    RealTuple sunrise_sunset = (RealTuple) solarData.evaluate(Tuples.fromReal(datetime, lat));
//                    Real sunrise = Tuples.getComponent(SolarType.SUNRISE_HOUR, sunrise_sunset);
//                    Real sunset = Tuples.getComponent(SolarType.SUNSET_HOUR, sunrise_sunset);
//
//                    DateTime sunrise1 = Times.fromDate(date, sunrise.getValue(GeneralUnit.hour));
//                    DateTime sunset1 = Times.fromDate(date, sunset.getValue(GeneralUnit.hour));
//
////                Marker marker = createIntervalMarker(sunrise1, sunset1, "Day", new Color(255, 255, 255, 25));
////                dayMarkers.add(marker);
//                    // Night (need to compute next day's sunrise
//                    days = new Real(RealType.Time, i + 1, GeneralUnit.day);
//                    datetime = new DateTime((Real) startDate.add(days));
//                    date = Times.toDate(datetime);
//                    sunrise_sunset = (RealTuple) solarData.evaluate(Tuples.fromReal(datetime, lat));
//                    sunrise = Tuples.getComponent(SolarType.SUNRISE_HOUR, sunrise_sunset);
//                    sunset = Tuples.getComponent(SolarType.SUNSET_HOUR, sunrise_sunset);
//                    DateTime sunrise2 = Times.fromDate(date, sunrise.getValue(GeneralUnit.hour));
//                    //DateTime sunset2 = Times.fromDate(date, sunset.getValue(GeneralUnit.hour));
//
//                    Marker marker = createIntervalMarker(sunset1, sunrise2, "Night", new Color(0, 0, 255, 25));
//                    markers.add(marker);
//                }
//                for (Marker marker : markers) {
//                    plot.addDomainMarker(marker, Layer.BACKGROUND);
//                }
//                plot.getDomainAxis().setRange(startDate.getValue(), endDate.getValue());
//            } catch (VisADException | RemoteException ex) {
//                Exceptions.printStackTrace(ex);
//            }

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

    private static class TemperaturePlot extends XYPlot {

        TemperaturePlot(XYDataset dataset) {
            super(dataset,
                    new DateTimeAxis(Bundle.CTL_TemperatureChartDomain()),
                    new NumberAxis(Bundle.CTL_TemperatureChartRange()),
                    new XYLineAndShapeRenderer()); // XYSplineRenderer());

            // Customize range axis
            NumberAxis rangeAxis1 = (NumberAxis) getRangeAxis();
            rangeAxis1.setAutoRangeIncludesZero(false);
            rangeAxis1.setAutoRange(true);

            // Customize the renderer 
            XYItemRenderer xyRenderer = getRenderer();
            xyRenderer.setBaseToolTipGenerator(new DateTimeToolTipGenerator());
            xyRenderer.setSeriesPaint(0, new Color(128,0,0));   // dark red
            
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
     * Create a marker band used to depict daytime or nighttime.
     */
    public static Marker createIntervalMarker(DateTime begin, DateTime end, String label,
                                              Color color) {
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
        public TickUnitSource createTickUnits() {
            TickUnits units = new TickUnits();
            units.add(new NumberTickUnit(3600, new DateTimeFormat(), 0));       // 1hour
            units.add(new NumberTickUnit(3600 * 3, new DateTimeFormat(), 3));     // 3hour
            units.add(new NumberTickUnit(3600 * 6, new DateTimeFormat(), 6));     // 6hour
            return units;
        }
    }

    private static final class DateTimeFormat extends NumberFormat {

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            try {
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
