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
import com.emxsys.visad.GeneralUnit;
import com.emxsys.weather.api.WeatherPreferences;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.weather.panels.AbstractWeatherChart.DateTimeAxis;
import com.emxsys.weather.panels.AbstractWeatherChart.DateTimeToolTipGenerator;
import java.awt.Color;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleInsets;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.FlatField;
import visad.FunctionType;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;

/**
 * This class displays the air temperature weather in an JFreeChart XY graph complete with titles
 * and legends.
 *
 * @author Bruce Schubert
 */
@Messages({
    "CTL_TemperatureChartDomain=Time",
    "CTL_TemperatureChartRange=Temperature",
    "CTL_TemperatureChartFahrenheit=(°F)",
    "CTL_TemperatureChartCelsius=(°C)",
    "CTL_TemperatureChartLegend=Temperature",})
public class TemperatureChartPanel extends AbstractWeatherChartPanel {

    // JFreeChart for Temperature
    private TemperatureChart chart;

    /**
     * Constructor creates new form TemperatureChartPanel.
     */
    public TemperatureChartPanel() {
        this(new TemperatureChart());
    }

    TemperatureChartPanel(TemperatureChart chart) {
        super(chart);
        this.chart = chart;
        initComponents();
    }

    public void setTemperatures(FlatField ff) {
        this.chart.plotTemperatures(ff);
    }

    public void setSunlight(Sunlight sunlight) {
        this.chart.setSunlight(sunlight);
    }

    public void setDateTime(ZonedDateTime datetime) {
        this.chart.setDateTime(datetime);
    }

    /**
     * The TemperatureChart is a JFreeChart with a specialized XYPlot for displaying temperature,
     * humidity, winds and day/night.
     */
    public static class TemperatureChart extends AbstractWeatherChart {

        /** Unit of measure for displaying temperature */
        private Unit unit;
        /** Datasets for temperature and dew point */
        private XYSeriesCollection dataset;
        /** Air temperature */
        private XYSeries series;
        /** Sunlight for sunrise and sunset times */
        private Sunlight sunlight;
        /** Day/Night markers */
        private List<Marker> markers;
        /** Marker for current time */
        private ValueMarker domainMarker;

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
            super(new TemperaturePlot(dataset, WeatherPreferences.getAirTempUnit()));

            this.unit = WeatherPreferences.getAirTempUnit();
            this.series = new XYSeries(getSeriesNameAndUom(this.unit));
            this.dataset = dataset;
            this.dataset.addSeries(series);

            // Customize the units when preferences change
            WeatherPreferences.addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
                switch (evt.getKey()) {
                    case WeatherPreferences.PREF_AIR_TEMP_UOM:
                        setAirTempUnit(WeatherPreferences.getAirTempUnit());
                        break;
                }
            });
        }

        /**
         * Gets the string used for the legend based on the supplied unit of measure.
         * @param unit Unit of measure used in legend.
         * @return E.g. "Temperature (F)"
         */
        private static String getSeriesNameAndUom(Unit unit) {
            if (unit.equals(GeneralUnit.degF)) {
                return Bundle.CTL_TemperatureChartLegend() + " " + Bundle.CTL_TemperatureChartFahrenheit();
            } else if (unit.equals(GeneralUnit.degC)) {
                return Bundle.CTL_TemperatureChartLegend() + " " + Bundle.CTL_TemperatureChartCelsius();
            } else {
                throw new IllegalArgumentException("unhandled unit: " + unit.toString());
            }
        }

        /**
         * Changes the unit of measure.
         * @param newUnit Unit used for the range and legend.
         */
        public final void setAirTempUnit(Unit newUnit) {
            if (this.unit.equals(newUnit)) {
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
                        item.setY(unit.toThat(item.getYValue(), newUnit));
                        series.add(item);
                    } catch (UnitException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                // Update the range units and scale
                ((TemperaturePlot) getPlot()).setRangeUnit(newUnit);

                // Now set the new air temp unit property
                this.unit = newUnit;

                this.series.setKey(getSeriesNameAndUom(newUnit)); // updates the legend text
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
                FunctionType functionType = (FunctionType) weather.getType();
                int index = findRangeComponentIndex(functionType, WeatherType.AIR_TEMP_F);
                if (index == -1) {
                    index = findRangeComponentIndex(functionType, WeatherType.AIR_TEMP_C);
                }
                if (index == -1) {
                    throw new IllegalArgumentException("FlatField must contain AIR_TEMP_C or AIR_TEMP_F.");
                }
                Unit wxUnit = functionType.getRealComponents()[index].getDefaultUnit();
                final float[][] times = weather.getDomainSet().getSamples(false);
                final float[][] values = weather.getFloats(false);

                for (int i = 0; i < times[0].length; i++) {
                    // Add values to the series in the preferred UOM
                    float value = values[index][i];
                    series.add(times[0][i], this.unit.equals(wxUnit)
                            ? value
                            : this.unit.toThis(value, wxUnit));
                }
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         * Sets the sunrise and sunset hours used to depict nighttime.
         * @param sunlight
         */
        public void setSunlight(Sunlight sunlight) {
            this.sunlight = sunlight;
            plotDayNight();
        }

        /**
         * Draws the day/night markers.
         */
        void plotDayNight() {
            TemperaturePlot plot = (TemperaturePlot) getPlot();
            if (markers != null) {
                for (Marker marker : markers) {
                    plot.removeDomainMarker(marker, Layer.BACKGROUND);
                }
                markers.clear();
            }
            if (sunlight == null) {
                return;
            }

            markers = ChartHelper.createNightMarkers(sunlight, dataset);
            for (Marker marker : markers) {
                plot.addDomainMarker(marker, Layer.BACKGROUND);
            }
        }

    }

    private static final class TemperaturePlot extends XYPlot {

        TemperaturePlot(XYDataset dataset, Unit unit) {
            super(dataset,
                    new DateTimeAxis(Bundle.CTL_TemperatureChartDomain()),
                    new NumberAxis(),
                    new XYLineAndShapeRenderer()); // XYSplineRenderer());

            // Customize range axis
            NumberAxis rangeAxis = (NumberAxis) getRangeAxis();
            rangeAxis.setAutoRangeIncludesZero(false);
            rangeAxis.setAutoRange(true);
            rangeAxis.setAutoRangeMinimumSize(30);

            // Customize the renderer 
            XYItemRenderer xyRenderer = getRenderer();
            xyRenderer.setBaseToolTipGenerator(new DateTimeToolTipGenerator());
            xyRenderer.setSeriesPaint(0, new Color(128, 0, 0));   // dark red

            // Customize the plot
            setRangeUnit(unit); // Update the range axis label
            setDomainCrosshairVisible(true);
            setDomainZeroBaselineVisible(true);
            setDomainCrosshairLockedOnData(false);
            setRangeCrosshairVisible(true);
            setRangeZeroBaselineVisible(true);
            setRangeCrosshairLockedOnData(true);

            setBackgroundPaint(Color.lightGray);
            setOutlinePaint(Color.darkGray);
            setDomainGridlinePaint(Color.white);
            setRangeGridlinePaint(Color.white);

            setAxisOffset(new RectangleInsets(4, 4, 4, 4));

        }

        void setRangeUnit(Unit unit) {
            if (unit.equals(GeneralUnit.degF)) {
                //getRangeAxis().setLabel(Bundle.CTL_TemperatureChartFahrenheit());
                getRangeAxis().setAutoRangeMinimumSize(30);
            } else if (unit.equals(GeneralUnit.degC)) {
                //getRangeAxis().setLabel(Bundle.CTL_TemperatureChartCelsius());
                getRangeAxis().setAutoRangeMinimumSize(15);
            } else {
                throw new IllegalArgumentException("unhandled unit: " + unit.toString());
            }

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
