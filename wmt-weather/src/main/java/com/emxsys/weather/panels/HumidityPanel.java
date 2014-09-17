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

import com.emxsys.weather.api.WeatherType;
import java.awt.Color;
import static java.lang.Math.round;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartPanel;
import static org.jfree.chart.ChartPanel.DEFAULT_BUFFER_USED;
import static org.jfree.chart.ChartPanel.DEFAULT_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH;
import static org.jfree.chart.ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import visad.Real;

/**
 *
 * @author Bruce Schubert
 */
public final class HumidityPanel extends javax.swing.JPanel {

    private HumidityChart humidityChart;

    /**
     * Constructor creates new form AirTempPanel.
     * @param title
     * @param initialValue
     */
    public HumidityPanel(String title, Real initialValue) {
        initComponents();

        // Initalize the JFreeChart
        humidityChart = new HumidityChart(title);

        // Add the chart to the layout panel
        thermometerPanel.add(new ChartPanel(humidityChart,
                105, //DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                100, // DEFAULT_MINIMUM_DRAW_WIDTH, // Default = 300
                DEFAULT_MINIMUM_DRAW_HEIGHT,
                DEFAULT_MAXIMUM_DRAW_WIDTH,
                DEFAULT_MAXIMUM_DRAW_HEIGHT,
                DEFAULT_BUFFER_USED,
                true, // properties
                true, // save
                true, // print
                true, // zoom
                true // tooltips
        ));

        // Updating the slider should trigger event to update the thermometer
        updateSlider(initialValue);
    }

    public void setHumidity(Real humidity) {
        humidityChart.setHumidity(humidity);
        updateSlider(humidity);
    }

    public Real getHumidity() {
        return humidityChart.getHumidity();
    }

    void updateSlider(Real humidity) {
        slider.setValue((int) round(humidity.getValue()));
    }

    void updateHumidity(int sliderValue) {
        humidityChart.setHumidity(new Real(WeatherType.REL_HUMIDITY, sliderValue));
    }

    /**
     * HumidityChart is a JFreeChart integrated with a HumidityPlot.
     */
    private class HumidityChart extends JFreeChart {

        final DefaultValueDataset dataset;

        HumidityChart(String title) {
            this(title, new DefaultValueDataset(50.0));
        }

        HumidityChart(String title, DefaultValueDataset dataset) {
            super(new HumidityPlot(dataset));
            this.dataset = dataset;
            
            setTitle(title);
            addSubtitle(new TextTitle("%"));
            
            getPlot().setBackgroundPaint(this.getBackgroundPaint());
        }

        Real getHumidity() {
            double value = this.dataset.getValue().doubleValue();
            return new Real(WeatherType.REL_HUMIDITY, value);
        }

        void setHumidity(Real humidity) {
            if (humidity == null || humidity.isMissing()) {
                this.dataset.setValue(null);
                return;
            }
            this.dataset.setValue(round(humidity.getValue()));
        }

    }

    /**
     * HumidityPlot is a ThermometerPlot stylized for air humidity.
     */
    private class HumidityPlot extends ThermometerPlot {

        HumidityPlot(ValueDataset dataset) {
            super(dataset);
            setUnits(ThermometerPlot.UNITS_NONE);
            setBulbRadius(30);
            setColumnRadius(15);
            //setThermometerStroke(new BasicStroke(2.0f));
            //setThermometerPaint(Color.darkGray);
            //setGap(3);
            setMercuryPaint(Color.blue);
            // Set the subranges off the scale to prevent drawing tick marks
            setSubrange(0, -100, -100);
            setSubrange(1, -100, -100);
            setSubrange(2, -100, -100);
            setUseSubrangePaint(false);
            setOutlineVisible(false);
        }
    }

    /** This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        unitsButtonGroup = new javax.swing.ButtonGroup();
        thermometerPanel = new javax.swing.JPanel();
        slider = new javax.swing.JSlider();

        setMaximumSize(new java.awt.Dimension(200, 2147483647));

        thermometerPanel.setLayout(new java.awt.GridLayout(1, 0));

        slider.setOrientation(javax.swing.JSlider.VERTICAL);
        slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(thermometerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(slider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(thermometerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(slider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderStateChanged

        updateHumidity(slider.getValue());

    }//GEN-LAST:event_sliderStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider slider;
    private javax.swing.JPanel thermometerPanel;
    private javax.swing.ButtonGroup unitsButtonGroup;
    // End of variables declaration//GEN-END:variables

}
