/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.cps.charts;

import com.emxsys.solar.api.SolarType;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.visad.Times;
import com.emxsys.visad.Tuples;
import com.emxsys.weather.api.WeatherType;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.rmi.RemoteException;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;

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
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.DefaultWindDataset;
import org.jfree.data.xy.VectorSeries;
import org.jfree.data.xy.VectorSeriesCollection;
import org.jfree.data.xy.VectorXYDataset;
import org.jfree.data.xy.WindDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.openide.util.Exceptions;
import visad.DateTime;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.VisADException;
import visad.util.DataUtility;

/**
 * This class display the two-day general weather in an XY graph complete with titles and legends.
 *
 * @author Bruce Schubert
 * @version $Id: WeatherChartPanel.java 724 2013-06-01 13:21:34Z bdschubert $
 */
public class WeatherChartPanel extends javax.swing.JPanel {

    /** Panel used for displaying a JFreeChart in a GUI */
    ChartPanel chartPanel;
    /** Dataset for temperature and humidity */
    private XYSeriesCollection xyDataset;
    /** Dataset for wind vectors */
    private VectorSeriesCollection vecDataset;
    /** Air temperature */
    private XYSeries seriesTa;
    /** Relative humidity */
    private XYSeries seriesHa;
    /** Wind direction and velocity */
    private VectorSeries seriesW;
    /** Day/Night markers */
    private ArrayList<Marker> markers = new ArrayList<>();
    private WindDataset windDataset = new DefaultWindDataset();

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

    /** Creates new form FuelFlammabilityChart */
    public WeatherChartPanel() {
        initComponents();
        initChart();
    }

    /**
     * Plot the supplied temperatures in the chart.
     *
     * @param temperatures (hour -> (temperature))
     */
    public void plotTemperatures(FlatField temperatures) {
        seriesTa.clear();
        try {
            // TODO test math types for compatablity and tuple index
            FunctionType functionType = (FunctionType) temperatures.getType();
            int index = findRangeComponentIndex(functionType, WeatherType.AIR_TEMP_F);
            if (index == -1) {
                index = findRangeComponentIndex(functionType, WeatherType.AIR_TEMP_C);
            }

            if (index == -1) {
                throw new IllegalArgumentException("FlatField must contain AIR_TEMP_C or AIR_TEMP_F.");
            }

            final float[][] times = temperatures.getDomainSet().getSamples(false);
            final float[][] values = temperatures.getFloats(false);

            for (int i = 0; i < times[0].length; i++) {
                seriesTa.add(times[0][i], values[index][i]);
            }

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void plotHumidities(FlatField humidities) {
        seriesHa.clear();
        try {
            FunctionType functionType = (FunctionType) humidities.getType();
            int index = findRangeComponentIndex(functionType, WeatherType.REL_HUMIDITY);
            if (index == -1) {
                throw new IllegalArgumentException("FlatField must contain REL_HUMIDITY.");
            }

            final float[][] times = humidities.getDomainSet().getSamples(false);
            final float[][] values = humidities.getFloats(false);

            for (int i = 0; i < times[0].length; i++) {
                seriesHa.add(times[0][i], values[index][i]);
            }

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void plotWinds(FlatField winds) {
        seriesW.clear();
        try {
            FunctionType functionType = (FunctionType) winds.getType();
            int dirIndex = findRangeComponentIndex(functionType, WeatherType.WIND_DIR);
            if (dirIndex == -1) {
                throw new IllegalArgumentException("FlatField must contain WIND_DIR");
            }
            int spdIndex = findRangeComponentIndex(functionType, WeatherType.WIND_SPEED_KTS);
            if (spdIndex == -1) {
                spdIndex = findRangeComponentIndex(functionType, WeatherType.WIND_SPEED_MPH);
            }
            if (spdIndex == -1) {
                spdIndex = findRangeComponentIndex(functionType, WeatherType.WIND_SPEED_KPH);
            }
            if (spdIndex == -1) {
                throw new IllegalArgumentException("FlatField must contain WIND_SPEED_...");
            }

            final float[][] times = winds.getDomainSet().getSamples(false);
            final float[][] values = winds.getFloats(false);
            for (int i = 0; i < times[0].length; i++) {
                double speed = values[spdIndex][i];
                double dir = values[dirIndex][i];
                // draw direction wind is blowing (vs from)
                double deltax = -Math.sin(Math.toRadians(dir));
                double deltay = -Math.cos(Math.toRadians(dir));
                seriesW.add(times[0][i], speed, deltax, deltay);
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
        XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
        for (Marker marker : markers) {
            plot.removeDomainMarker(marker, Layer.BACKGROUND);
        }
        markers.clear();
        try {
            Set domainSet = solarData.getDomainSet();
            int length = domainSet.getLength();
            RealTuple sample1 = DataUtility.getSample(domainSet, 0);
            RealTuple sample2 = DataUtility.getSample(domainSet, length - 1);

            Real lat = (Real) sample1.getComponent(1);
            Real startDate = (Real) sample1.getComponent(0);
            Real endDate = (Real) sample2.getComponent(0);
            Real timeSpan = (Real) endDate.subtract(startDate);
            int numDays = (int) timeSpan.getValue(GeneralUnit.day);
            for (int i = 0; i < numDays; i++) {
                // Day (between sunrise and sunset)
                Real days = new Real(RealType.Time, i, GeneralUnit.day);
                Real datetime = new DateTime((Real) startDate.add(days));

                ZonedDateTime date = Times.toZonedDateTime(datetime);
                RealTuple sunrise_sunset = (RealTuple) solarData.evaluate(Tuples.fromReal(datetime, lat));
                Real sunrise = Tuples.getComponent(SolarType.SUNRISE_HOUR, sunrise_sunset);
                Real sunset = Tuples.getComponent(SolarType.SUNSET_HOUR, sunrise_sunset);

                DateTime sunrise1 = Times.fromZonedDateTime(date.withHour((int) sunrise.getValue(GeneralUnit.hour)));
                DateTime sunset1 = Times.fromZonedDateTime(date.withHour((int) sunset.getValue(GeneralUnit.hour)));

//                Marker marker = createIntervalMarker(sunrise1, sunset1, "Day", new Color(255, 255, 255, 25));
//                dayMarkers.add(marker);
                // Night (need to compute next day's sunrise
                days = new Real(RealType.Time, i + 1, GeneralUnit.day);
                datetime = new DateTime((Real) startDate.add(days));
                date = Times.toZonedDateTime(datetime);
                sunrise_sunset = (RealTuple) solarData.evaluate(Tuples.fromReal(datetime, lat));
                sunrise = Tuples.getComponent(SolarType.SUNRISE_HOUR, sunrise_sunset);
                sunset = Tuples.getComponent(SolarType.SUNSET_HOUR, sunrise_sunset);
                DateTime sunrise2 = Times.fromZonedDateTime(date.withHour((int) sunrise.getValue(GeneralUnit.hour)));
                //DateTime sunset2 = Times.fromDate(date, sunset.getValue(GeneralUnit.hour));

                Marker marker = createIntervalMarker(sunset1, sunrise2, "Night", new Color(0, 0, 255, 25));
                markers.add(marker);
            }
            for (Marker marker : markers) {
                plot.addDomainMarker(marker, Layer.BACKGROUND);
            }
            plot.getDomainAxis().setRange(startDate.getValue(), endDate.getValue());
        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
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

    private void initChart() {
        seriesTa = new XYSeries("Temperature");
        seriesHa = new XYSeries("Humidity");
        seriesW = new VectorSeries("Wind");

        xyDataset = new XYSeriesCollection();
        xyDataset.addSeries(seriesTa);
        xyDataset.addSeries(seriesHa);

        vecDataset = new VectorSeriesCollection();
        vecDataset.addSeries(seriesW);

        chartPanel = createXYVectorChart("Weather", "Time",
                "Temp / Percent", "Wind Speed",
                xyDataset, vecDataset);

        // Setting the preferred size allows us to control the initial size
        // of the panel when it's dragged-n-dropped in the NetBeans GUI editor.
        chartPanel.setPreferredSize(new java.awt.Dimension(350, 150));

        add(chartPanel, BorderLayout.CENTER);
    }

    /**
     *
     * @param title Chart title
     * @param domainLabel x-axis label
     * @param rangeLabel1 Primary y-axis label
     * @param rangleLabel2 Secondary y-axis label
     * @param xyDataset Primary dataset
     * @param vecDataset Secondary dataset
     * @return ChartPanel instance with XY and Vector series
     */
    private ChartPanel createXYVectorChart(
            String title, String domainLabel,
            String rangeLabel1, String rangeLabel2,
            XYSeriesCollection xyDataset, VectorSeriesCollection vecDataset) {
        // Time domain
        NumberAxis domainAxis = new DateTimeAxis(domainLabel);

        // Temp / RH range
        NumberAxis rangeAxis1 = new NumberAxis(rangeLabel1);
        rangeAxis1.setAutoRangeIncludesZero(false);
        rangeAxis1.setAutoRange(true);

        // Wind range
        NumberAxis rangeAxis2 = new NumberAxis(rangeLabel2);
        rangeAxis2.setAutoRangeIncludesZero(true);
        rangeAxis2.setAutoRangeMinimumSize(20.0);

        // Renderer for Temp and RH
        XYSplineRenderer xyRenderer = new XYSplineRenderer();
        xyRenderer.setBaseToolTipGenerator(new DateTimeToolTipGenerator());

        // Renderer for winds
        WindVectorRenderer vecRenderer = new WindVectorRenderer();
        vecRenderer.setSeriesPaint(0, Color.magenta);
        vecRenderer.setBaseToolTipGenerator(new WindVectorToolTipGenerator());

        // Create the chart body domain and primary range axis
        XYPlot plot
                = new XYPlot(xyDataset, domainAxis, rangeAxis1, xyRenderer);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
        plot.setOutlinePaint(Color.darkGray);

        // now add the secondary axis
        plot.setDataset(1, vecDataset);
        plot.setRangeAxis(1, rangeAxis2);
        plot.setRenderer(1, vecRenderer);
        plot.mapDatasetToRangeAxis(1, 1);

        ChartPanel panel = new ChartPanel(
                new JFreeChart(plot));
//                new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true));
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);

        return panel;
    }

    static class WindVectorToolTipGenerator extends StandardXYToolTipGenerator {

        public WindVectorToolTipGenerator() {
            super();
        }

        @Override
        public String generateToolTip(XYDataset dataset, int series, int item) {
            if (dataset instanceof VectorXYDataset) {
                try {
                    VectorXYDataset d = (VectorXYDataset) dataset;
                    double x = dataset.getXValue(series, item);
                    double y = Math.round(dataset.getYValue(series, item));
                    double dx = d.getVectorXValue(series, item);
                    DateTime dateTime = new DateTime(x);
                    NumberFormat yf = getYFormat();
                    return yf.format(y) + " (" + Math.toDegrees(Math.asin(dx)) + " deg) at " + dateTime.toString();
                } catch (VisADException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return super.generateToolTip(dataset, series, item);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
