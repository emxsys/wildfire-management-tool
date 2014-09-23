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
import static com.emxsys.weather.api.WeatherType.WIND_SPEED;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_KPH;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_KTS;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_MPH;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_SI;
import com.emxsys.weather.api.WeatherPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import org.jfree.chart.ChartPanel;
import static org.jfree.chart.ChartPanel.DEFAULT_BUFFER_USED;
import static org.jfree.chart.ChartPanel.DEFAULT_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH;
import static org.jfree.chart.ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_WIDTH;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.dial.ArcDialFrame;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.openide.util.Exceptions;
import visad.CommonUnit;
import visad.Real;
import visad.Unit;
import visad.VisADException;

/**
 * WindDirChartPanel is ChartPanel containing a DialChart for displaying wind speeds.
 *
 * @author Bruce Schubert
 */
public class WindSpdChartPanel extends ChartPanel {

    private WindSpdChart chart;

    /**
     * Constructor creates new form WindDirPanel
     */
    public WindSpdChartPanel() {
        this(new WindSpdChart());
        initComponents();
    }

    WindSpdChartPanel(WindSpdChart chart) {
        super(chart,
                100, //DEFAULT_WIDTH,
                150, //DEFAULT_HEIGHT,
                50,  // DEFAULT_MINIMUM_DRAW_WIDTH, // 300
                50, //DEFAULT_MINIMUM_DRAW_HEIGHT, // 200
                DEFAULT_MAXIMUM_DRAW_WIDTH,
                DEFAULT_MAXIMUM_DRAW_HEIGHT,
                DEFAULT_BUFFER_USED,
                false, // properties
                false, // save
                false, // print
                false, // zoom
                true); // tooltips

        this.chart = chart;
    }

    public void setTitle(String title) {
        this.chart.setTitle(title);
    }

    public void setSubTitle(String subtitle) {
        this.chart.addSubtitle(new TextTitle(subtitle));
    }

    public void setWindSpeed(Real speed) {
        this.chart.setWindSpeed(speed);
    }

    public Real getWindSpeed() {
        return this.chart.getWindSpeed();
    }

    /**
     * Overrides the default UOM defined in the WeatherPreferences.
     * @param unit Unit of measure to used in this display.
     */
    public void setUnit(Unit unit) {
        if (!(unit.equals(GeneralUnit.mph) || unit.equals(GeneralUnit.knot)
                || unit.equals(GeneralUnit.kph) || unit.equals(CommonUnit.meterPerSecond))) {
            throw new IllegalArgumentException("Invalid unit for wind speed: " + unit);
        }
        Real speed = chart.getWindSpeed();
        chart.setUnit(unit);
        chart.setWindSpeed(speed);
    }

    /**
     * WindSpdChart is a JFreeChart integrated with a WindSpdPlot.
     */
    public static class WindSpdChart extends JFreeChart {

        final DefaultValueDataset dataset;
        private TextTitle subtitle = new TextTitle();
        private Unit uom;

        public WindSpdChart() {
            this(new DefaultValueDataset(0.0));
        }

        WindSpdChart(DefaultValueDataset dataset) {
            super(new WindSpdPlot(dataset));
            addSubtitle(subtitle);
            setPadding(RectangleInsets.ZERO_INSETS);

            this.uom = WeatherPreferences.getWindSpeedUnit();
            this.dataset = dataset;
            this.dataset.addChangeListener((DatasetChangeEvent event) -> {
                try {
                    Real speed = getWindSpeed();
                    String units;
                    if (uom.equals(GeneralUnit.mph)) {
                        units = "mph";
                    } else if (uom.equals(GeneralUnit.kph)) {
                        units = "kph";
                    } else if (uom.equals(GeneralUnit.knot)) {
                        units = "kts";
                    } else {
                        units = "m/s";
                    }
                    subtitle.setText(String.format("%1$.1f %2$s", speed.getValue(), units));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            });

            WeatherPreferences.addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
                if (evt.getKey().equals(WeatherPreferences.PREF_WIND_SPD_UOM)) {
                    setUnit(WeatherPreferences.getWindSpeedUnit());
                }
            });
        }

        public void refresh() {
            dataset.setNotify(true);
        }

        public final void setUnit(Unit uom) {
            Real speed = getWindSpeed();
            this.uom = uom;
            WindSpdPlot plot = (WindSpdPlot) getPlot();
            plot.setScale(uom);
            setWindSpeed(speed);
        }

        public void setWindSpeed(Real speed) {
            try {
                dataset.setValue(speed.isMissing() ? null : speed.getValue(uom));
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public final Real getWindSpeed() {
            Number value = this.dataset.getValue();
            if (value != null) {
                if (uom.equals(GeneralUnit.mph)) {
                    return new Real(WIND_SPEED_MPH, value.doubleValue());
                } else if (uom.equals(GeneralUnit.kph)) {
                    return new Real(WIND_SPEED_KPH, value.doubleValue());
                } else if (uom.equals(GeneralUnit.knot)) {
                    return new Real(WIND_SPEED_KTS, value.doubleValue());
                } else {
                    return new Real(WIND_SPEED_SI, value.doubleValue());
                }
            }
            return new Real(WIND_SPEED);  // "missing" value
        }
    }

    /**
     * WindSpdPlot is a DialPlot stylized for wind speed.
     */
    private static class WindSpdPlot extends DialPlot {

        WindSpdPlot(ValueDataset dataset) {
            super(dataset);
            setView(0.8, 0.37, 0.22, 0.26);
            setInsets(RectangleInsets.ZERO_INSETS);
            // Frame
            ArcDialFrame dialFrame = new ArcDialFrame(-10.0, 20.0);
            dialFrame.setInnerRadius(0.70);
            dialFrame.setOuterRadius(0.90);
            dialFrame.setForegroundPaint(Color.darkGray);
            dialFrame.setStroke(new BasicStroke(2.0f));
            dialFrame.setVisible(true);
            setDialFrame(dialFrame);

            // Dial Background 
            GradientPaint gp = new GradientPaint(
                    new Point(), new Color(180, 180, 180),
                    new Point(), new Color(255, 255, 255));
            DialBackground db = new DialBackground(gp);
            db.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.CENTER_VERTICAL));
            addLayer(db);

            // Scale          
            double MIN_SPEED = 0;
            double MAX_SPEED = 50;
            StandardDialScale scale = new StandardDialScale(MIN_SPEED, MAX_SPEED, -8, 16.0, 10.0, 4);
            scale.setTickRadius(0.82);
            scale.setTickLabelOffset(-0.04);
            scale.setMajorTickIncrement(25.0);
            scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));
            addScale(0, scale);

            // Needle
            DialPointer needle = new DialPointer.Pin();
            needle.setRadius(0.84);
            addLayer(needle);
        }

        void setScale(Unit uom) {
            try {
                Real MAX_MPH = new Real(WIND_SPEED_MPH, 40);
                double MIN_SPEED = 0;
                double MAX_SPEED = Math.ceil(MAX_MPH.getValue(uom));
                StandardDialScale scale = new StandardDialScale(MIN_SPEED, MAX_SPEED, -8, 16.0, 10.0, 4);
                scale.setTickRadius(0.82);
                scale.setTickLabelOffset(-0.04);
                scale.setMajorTickIncrement(25.0);
                scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));
                addScale(0, scale);
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
            super.draw(g2, area, anchor, parentState, info); //To change body of generated methods, choose Tools | Templates.
        }

    }

    /** This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMinimumSize(new java.awt.Dimension(100, 100));
        setPreferredSize(new java.awt.Dimension(150, 150));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 153, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 142, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
