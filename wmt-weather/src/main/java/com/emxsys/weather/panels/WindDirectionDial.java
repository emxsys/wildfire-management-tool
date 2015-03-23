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

import static com.emxsys.jfree.ChartUtil.WIND_NEEDLE;
import com.emxsys.weather.api.WeatherType;
import java.awt.Color;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CompassPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.openide.util.Exceptions;
import visad.CommonUnit;
import visad.Real;
import visad.VisADException;

/**
 * WindDirectionDial is ChartPanel containing a wind direction compass plot.
 *
 * @author Bruce Schubert
 */
public class WindDirectionDial extends ChartPanel {

    private WindDirChart chart;

    /**
     * Constructor creates new form WindDirPanel
     */
    public WindDirectionDial() {
        this(new WindDirChart());
        initComponents();
    }

    WindDirectionDial(WindDirChart chart) {
        super(chart,
                150, //DEFAULT_WIDTH,
                150, //DEFAULT_HEIGHT,
                50, // DEFAULT_MINIMUM_DRAW_WIDTH, // Default = 300
                50, // DEFAULT_MINIMUM_DRAW_HEIGHT,
                DEFAULT_MAXIMUM_DRAW_WIDTH,
                DEFAULT_MAXIMUM_DRAW_HEIGHT,
                DEFAULT_BUFFER_USED,
                false, // properties
                false, // save
                false, // print
                false, // zoom
                true); // tooltips)
        this.chart = chart;
    }

    public void setTitle(String title) {
        this.chart.setTitle(title);
    }

    public void setSubTitle(String subtitle) {
        this.chart.addSubtitle(new TextTitle(subtitle));
    }

    public void setWindDirection(Real dir) {
        try {
            if (dir == null || dir.isMissing()) {
                this.chart.dataset.setValue(null);
            } else {
                this.chart.dataset.setValue(dir.getValue(CommonUnit.degree));
            }
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Real getWindDirection() {
        Number value = this.chart.dataset.getValue();
        if (value != null) {
            return new Real(WeatherType.WIND_DIR, value.doubleValue());
        }
        return new Real(WeatherType.WIND_DIR);  // "missing" value
    }

    /**
     * WindDirPlot is a CompassPlot stylized for wind direction.
     */
    static private class WindDirPlot extends CompassPlot {

        WindDirPlot(ValueDataset dataset) {
            super(dataset);
            setRosePaint(Color.blue);
            setRoseHighlightPaint(Color.gray);
            setRoseCenterPaint(Color.white);
            setDrawBorder(false);
            setSeriesNeedle(0, WIND_NEEDLE);
            setSeriesPaint(0, Color.black);        // arrow heads
            setSeriesOutlinePaint(0, Color.black); // arrow shafts and arrow head outline
        }
    }

    /**
     * WindDirChart is a JFreeChart integrated with a WindDirPlot.
     */
    public static class WindDirChart extends JFreeChart {

        final DefaultValueDataset dataset;

        public WindDirChart() {
            this(new DefaultValueDataset(0.0));
        }

        WindDirChart(DefaultValueDataset dataset) {
            super(new WindDirPlot(dataset));
            this.dataset = dataset;
        }
    }

    /** This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMinimumSize(new java.awt.Dimension(100, 100));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
