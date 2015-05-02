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
import com.emxsys.weather.api.WeatherType;
import com.emxsys.weather.panels.AbstractWeatherChart.DateTimeAxis;
import com.emxsys.weather.panels.AbstractWeatherChart.DateTimeToolTipGenerator;
import java.awt.Color;
import java.time.ZonedDateTime;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.FlatField;
import visad.FunctionType;
import visad.VisADException;

/**
 * This class displays the relative humidity and cloud cover in an JFreeChart XY graph complete with
 * titles and legends.
 *
 * @author Bruce Schubert
 */
@Messages({
    "CTL_HumidityChartDomain=Time",
    "CTL_HumidityChartHumidity=Rel. Humidty (%)",
    "CTL_HumidityChartClouds=Sky Cover (%)",})
public class RelativeHumiditySkyCoverChart extends AbstractWeatherChartPanel {

    private HumidityChart chart;

    /**
     * Constructor creates new form HumidityChartPanel.
     */
    public RelativeHumiditySkyCoverChart() {
        this(new HumidityChart());
    }

    RelativeHumiditySkyCoverChart(HumidityChart chart) {
        super(chart);
        this.chart = chart;
        initComponents();
    }

    public void setCloudCoverForecasts(FlatField ff) {
        this.chart.plotClouds(ff, HumidityChart.FORECAST_SERIES);
    }

    public void setCloudCoverObservations(FlatField ff) {
        this.chart.plotClouds(ff, HumidityChart.OBSERVATION_SERIES);
    }

    public void setHumidityForecasts(FlatField ff) {
        this.chart.plotHumidities(ff, HumidityChart.FORECAST_SERIES);
    }

    public void setHumidityObservations(FlatField ff) {
        this.chart.plotHumidities(ff, HumidityChart.OBSERVATION_SERIES);
    }

    public void setSunlight(Sunlight sunlight) {
        this.chart.setSunlight(sunlight);
    }

    @Override
    public void setDateTime(ZonedDateTime datetime) {
        this.chart.setDateTime(datetime);
    }

    /**
     * The HumidityChart is a JFreeChart with a specialized XYPlot for displaying relative humidity,
     * sky cover and day/night.
     */
    public static class HumidityChart extends AbstractWeatherChart {

        /** Dataset for clouds and humidity */
        private XYSeriesCollection xyDataset;
        /** Relative humidity */
        private XYSeries[] seriesH = new XYSeries[2];
        /** Clouds */
        private XYSeries[] seriesC = new XYSeries[2];
        private static final int FORECAST_SERIES = 0;
        private static final int OBSERVATION_SERIES = 1;

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
        private HumidityChart(XYSeriesCollection xyDataset) {
            super(new HumidityPlot(xyDataset));
            this.seriesH[FORECAST_SERIES] = new XYSeries(Bundle.CTL_HumidityChartHumidity());
            this.seriesH[OBSERVATION_SERIES] = new XYSeries("HumidityObservtions");
            this.seriesC[FORECAST_SERIES] = new XYSeries(Bundle.CTL_HumidityChartClouds());
            this.seriesC[OBSERVATION_SERIES] = new XYSeries("CloudCoverObservations");
            this.xyDataset = xyDataset;
            this.xyDataset.addSeries(seriesH[FORECAST_SERIES]);
            this.xyDataset.addSeries(seriesH[OBSERVATION_SERIES]);
            this.xyDataset.addSeries(seriesC[FORECAST_SERIES]);
            this.xyDataset.addSeries(seriesC[OBSERVATION_SERIES]);
        }

        /**
         * Plot the relative humidity. This is the primary dataset.
         * @param weather (hour -> (..., REL_HUMIDITY, ...))
         * @param seriesNo
         */
        public void plotHumidities(FlatField weather, int seriesNo) {
            seriesH[seriesNo].clear();
            try {
                FunctionType functionType = (FunctionType) weather.getType();
                int index = findRangeComponentIndex(functionType, WeatherType.REL_HUMIDITY);
                if (index == -1) {
                    throw new IllegalArgumentException("FlatField must contain REL_HUMIDITY.");
                }

                final float[][] times = weather.getDomainSet().getSamples(false);
                final float[][] values = weather.getFloats(false);

                for (int i = 0; i < times[0].length; i++) {
                    seriesH[seriesNo].add(times[0][i], values[index][i]);
                }
                plotDayNight();

            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         * Plot the cloud cover in the chart. Clouds are the secondary dataset.
         * @param weather (hour -> (..., CLOUD_COVER, ...))
         * @param seriesNo
         */
        public void plotClouds(FlatField weather, int seriesNo) {
            seriesC[seriesNo].clear();
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
                    seriesC[seriesNo].add(times[0][i], value);
                }

            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
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
            xyRenderer.setSeriesPaint(1, new Color(0, 128, 0)); // dark green for humidty
            xyRenderer.setSeriesShape(1, xyRenderer.getSeriesShape(0));
            xyRenderer.setSeriesVisibleInLegend(1, false);

            xyRenderer.setSeriesPaint(2, new Color(0, 0, 128)); // dark blue for cloud cover
            xyRenderer.setSeriesPaint(3, new Color(0, 0, 128)); // dark blue for cloud cover
            xyRenderer.setSeriesShape(3, xyRenderer.getSeriesShape(2));
            xyRenderer.setSeriesVisibleInLegend(3, false);

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
