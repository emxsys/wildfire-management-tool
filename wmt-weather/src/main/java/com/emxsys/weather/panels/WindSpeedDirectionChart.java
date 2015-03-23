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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.prefs.PreferenceChangeEvent;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.VectorSeries;
import org.jfree.data.xy.VectorSeriesCollection;
import org.jfree.data.xy.VectorXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.CommonUnit;
import visad.DateTime;
import visad.FlatField;
import visad.FunctionType;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;

/**
 * This class displays winds in an JFreeChart XY graph complete with titles and legends.
 *
 * @author Bruce Schubert
 */
@Messages({
    "CTL_WindChartDomain=Time",
    "CTL_WindChartMPH=(mph)",
    "CTL_WindChartKnots=(kts)",
    "CTL_WindChartKPH=(km/h)",
    "CTL_WindChartMPS=(m/s)",
    "CTL_WindChartLegend=Surface Winds",})
public class WindSpeedDirectionChart extends AbstractWeatherChartPanel {

    private WindChart chart;

    /**
     * Constructor creates new form WeatherChartPanel.
     */
    public WindSpeedDirectionChart() {
        this(new WindChart());
    }

    WindSpeedDirectionChart(WindChart chart) {
        super(chart);
        this.chart = chart;
        initComponents();
    }

    public void setWinds(FlatField weather) {
        this.chart.plotWinds(weather);
    }

    public void setSunlight(Sunlight sunlight) {
        this.chart.setSunlight(sunlight);
    }

    @Override
    public void setDateTime(ZonedDateTime datetime) {
        this.chart.setDateTime(datetime);
    }

    /**
     * The WindChart is a JFreeChart with a specialized XYPlot for displaying temperature, humidity,
     * winds and day/night.
     */
    public static class WindChart extends AbstractWeatherChart {

        private Unit unit;
        /** Dataset for wind vectors */
        private VectorSeriesCollection dataset;
        /** Wind direction and velocity */
        private VectorSeries series;

        /**
         * Constructor for a WeatherChart.
         */
        public WindChart() {
            this(new VectorSeriesCollection(), WeatherPreferences.getWindSpeedUnit());
        }

        /**
         * Constructor implementation.
         * @param xyDataset
         * @param dataset
         */
        WindChart(VectorSeriesCollection dataset, Unit unit) {
            super(new WindPlot(dataset, unit));
            this.unit = unit;
            this.series = new VectorSeries(getSeriesNameAndUom(unit));
            this.dataset = dataset;
            this.dataset.addSeries(series);

            WeatherPreferences.addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
                if (evt.getKey().equals(WeatherPreferences.PREF_WIND_SPD_UOM)) {
                    setWindSpeedUnit(WeatherPreferences.getWindSpeedUnit());
                }
            });
        }

        private static String getSeriesNameAndUom(Unit unit) {
            if (unit.equals(GeneralUnit.mph)) {
                return Bundle.CTL_WindChartLegend() + " " + Bundle.CTL_WindChartMPH();
            } else if (unit.equals(GeneralUnit.knot)) {
                return Bundle.CTL_WindChartLegend() + " " + Bundle.CTL_WindChartKnots();
            } else if (unit.equals(GeneralUnit.kph)) {
                return Bundle.CTL_WindChartLegend() + " " + Bundle.CTL_WindChartKPH();
            } else if (unit.equals(CommonUnit.meterPerSecond)) {
                return Bundle.CTL_WindChartLegend() + " " + Bundle.CTL_WindChartMPS();
            } else {
                throw new IllegalArgumentException("unhandled unit: " + unit.toString());
            }
        }

        public final void setWindSpeedUnit(Unit newUnit) {
            if (this.unit.equals(newUnit)) {
                return;
            }
            // Refresh the series with the new unit of measure
            VectorSeries newSeries = new VectorSeries(getSeriesNameAndUom(newUnit));

            for (int i = 0; i < series.getItemCount(); i++) {
                try {
                    double time = series.getXValue(i);
                    double speed = series.getYValue(i);
                    double vectorX = series.getVectorXValue(i);
                    double vectorY = series.getVectorYValue(i);
                    // convert speed  to new unit of measure
                    speed = unit.toThat(speed, newUnit);
                    newSeries.add(time, speed, vectorX, vectorY);
                } catch (UnitException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            // Replace the old series
            dataset.removeSeries(series);
            dataset.addSeries(newSeries);
            series = newSeries;
            series.fireSeriesChanged();

            unit = newUnit;
        }

        public void plotWinds(FlatField weather) {
            series.clear();
            try {
                int dirIndex = getWindDirIndex(weather);
                if (dirIndex == -1) {
                    throw new IllegalArgumentException("FlatField must contain WIND_DIR");
                }
                int spdIndex = getWindSpeedIndex(weather);
                if (spdIndex == -1) {
                    throw new IllegalArgumentException("FlatField must contain WIND_SPEED_...");
                }
                Unit unit = weather.getDefaultRangeUnits()[spdIndex];

                final float[][] times = weather.getDomainSet().getSamples(false);
                final float[][] values = weather.getFloats(false);
                for (int i = 0; i < times[0].length; i++) {
                    double dir = values[dirIndex][i];
                    double speed = unit.equals(unit)
                            ? values[spdIndex][i]
                            : unit.toThis(values[spdIndex][i], unit);
                    // draw direction wind is blowing (vs from)
                    double deltax = -Math.sin(Math.toRadians(dir));
                    double deltay = -Math.cos(Math.toRadians(dir));
                    series.add(times[0][i], speed, deltax, deltay);
                }
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         *
         */
        @Override
        public void plotDayNight() {
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
            markers = ChartHelper.createNightMarkers(sunlight, (VectorSeriesCollection) plot.getDataset());
            for (Marker marker : markers) {
                plot.addDomainMarker(marker, Layer.BACKGROUND);
            }
        }

        private int getWindDirIndex(FlatField weather) {
            FunctionType functionType = (FunctionType) weather.getType();
            return findRangeComponentIndex(functionType, WeatherType.WIND_DIR);
        }

        private int getWindSpeedIndex(FlatField weather) {
            FunctionType functionType = (FunctionType) weather.getType();
            int spdIndex = findRangeComponentIndex(functionType, WeatherType.WIND_SPEED_KTS);
            if (spdIndex == -1) {
                spdIndex = findRangeComponentIndex(functionType, WeatherType.WIND_SPEED_MPH);
            }
            if (spdIndex == -1) {
                spdIndex = findRangeComponentIndex(functionType, WeatherType.WIND_SPEED_KPH);
            }
            return spdIndex;
        }

    }

    private static final class WindPlot extends XYPlot {

        WindPlot(VectorSeriesCollection dataset, Unit unit) {
            super(dataset,
                    new DateTimeAxis(Bundle.CTL_WindChartDomain()),
                    new NumberAxis(),
                    new WindVectorRenderer());

            // Customize the Wind range
            NumberAxis rangeAxis = (NumberAxis) getRangeAxis();
            rangeAxis.setAutoRange(true);
            rangeAxis.setAutoRangeIncludesZero(true);
            setRangeUnit(unit);

            // Customize the renderer for winds
            WindVectorRenderer vecRenderer = (WindVectorRenderer) getRenderer();
            vecRenderer.setSeriesPaint(0, new Color(128, 0, 128));    // purple
            vecRenderer.setBaseToolTipGenerator(new WindVectorToolTipGenerator());

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

        void setRangeUnit(Unit unit) {
            if (unit.equals(GeneralUnit.mph)) {
                getRangeAxis().setAutoRangeMinimumSize(20);
            } else if (unit.equals(GeneralUnit.knot)) {
                getRangeAxis().setAutoRangeMinimumSize(20);
            } else if (unit.equals(GeneralUnit.kph)) {
                getRangeAxis().setAutoRangeMinimumSize(30);
            } else if (unit.equals(CommonUnit.meterPerSecond)) {
                getRangeAxis().setAutoRangeMinimumSize(10);
            } else {
                throw new IllegalArgumentException("unhandled unit: " + unit.toString());
            }

        }

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
