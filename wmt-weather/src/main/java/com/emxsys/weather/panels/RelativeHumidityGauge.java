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

import com.emxsys.util.HelpUtil;
import com.emxsys.weather.api.WeatherType;
import java.awt.Color;
import static java.lang.Math.round;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartPanel;
import static org.jfree.chart.ChartPanel.DEFAULT_BUFFER_USED;
import static org.jfree.chart.ChartPanel.DEFAULT_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH;
import static org.jfree.chart.ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import visad.Real;

/**
 *
 * @author Bruce Schubert
 */
public final class RelativeHumidityGauge extends javax.swing.JPanel {

    public final static String PROP_REL_HUMIDITY = "weather.relhumiditypanel.relhumidity";

    private HumidityChart chart;
    private String helpID = null;
    private static final Logger logger = Logger.getLogger(RelativeHumidityGauge.class.getName());

    /**
     * Constructor creates new form RelativeHumidityPanel without help.
     * @param title
     * @param initialValue
     */
    public RelativeHumidityGauge(String title, Real initialValue) {
        this(title, initialValue, null);
    }

    /**
     * Constructor creates new form RelativeHumidityPanel.
     * @param title
     * @param initialValue
     * @param helpID
     */
    public RelativeHumidityGauge(String title, Real initialValue, String helpID) {
        initComponents();
        
        // Initialize the help button
        if (helpID == null || helpID.isEmpty()) {
            this.infoBtn.setVisible(false);
        } else {
            this.helpID = helpID;
        }

        // Initalize the JFreeChart
        chart = new HumidityChart(title);

        // Add the chart to the layout panel
        chartPanel.add(new ChartPanel(chart,
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
        chart.setHumidity(humidity);
        updateSlider(humidity);
    }

    public Real getHumidity() {
        return chart.getHumidity();
    }

    void updateSlider(Real humidity) {
        slider.setValue((int) round(humidity.getValue()));
    }

    void updateHumidityFromSlider(int sliderValue) {
        Real oldValue = getHumidity();
        Real newValue = new Real(WeatherType.REL_HUMIDITY, sliderValue);
        chart.setHumidity(newValue);
        firePropertyChange(PROP_REL_HUMIDITY, oldValue, newValue);
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
            Number value = this.dataset.getValue();
            return new Real(WeatherType.REL_HUMIDITY, value == null ? Double.NaN : value.doubleValue());
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
        chartPanel = new javax.swing.JPanel();
        sliderPanel = new javax.swing.JPanel();
        slider = new javax.swing.JSlider();
        infoBtn = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(200, 2147483647));
        setPreferredSize(new java.awt.Dimension(100, 258));

        chartPanel.setLayout(new java.awt.GridLayout(1, 0));

        sliderPanel.setLayout(new java.awt.BorderLayout());

        slider.setMajorTickSpacing(25);
        slider.setOrientation(javax.swing.JSlider.VERTICAL);
        slider.setPaintTicks(true);
        slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderStateChanged(evt);
            }
        });
        sliderPanel.add(slider, java.awt.BorderLayout.CENTER);

        infoBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emxsys/weather/images/help.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(infoBtn, org.openide.util.NbBundle.getMessage(RelativeHumidityGauge.class, "RelativeHumidityGauge.infoBtn.text")); // NOI18N
        infoBtn.setMaximumSize(new java.awt.Dimension(28, 28));
        infoBtn.setMinimumSize(new java.awt.Dimension(28, 28));
        infoBtn.setPreferredSize(new java.awt.Dimension(28, 28));
        infoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoBtnActionPerformed(evt);
            }
        });
        sliderPanel.add(infoBtn, java.awt.BorderLayout.SOUTH);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sliderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sliderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderStateChanged

        updateHumidityFromSlider(slider.getValue());

    }//GEN-LAST:event_sliderStateChanged

    private void infoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoBtnActionPerformed

        logger.log(Level.FINE, "Invoking help ID: {0}", helpID);
        HelpUtil.showHelp(helpID);
    }//GEN-LAST:event_infoBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel chartPanel;
    private javax.swing.JButton infoBtn;
    private javax.swing.JSlider slider;
    private javax.swing.JPanel sliderPanel;
    private javax.swing.ButtonGroup unitsButtonGroup;
    // End of variables declaration//GEN-END:variables

}
