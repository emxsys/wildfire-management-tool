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
import com.emxsys.visad.GeneralType;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.weather.api.WeatherPreferences;
import static com.emxsys.weather.api.WeatherType.AIR_TEMP_C;
import static com.emxsys.weather.api.WeatherType.AIR_TEMP_F;
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
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.openide.util.Exceptions;
import visad.Real;
import visad.Unit;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class AirTemperatureGauge extends javax.swing.JPanel {

    public final static String PROP_AIR_TEMP = "weather.airtemppanel.airtemp";

    private TemperatureChart chart;
    private Real temperature;
    private Unit uom;
    private double range;
    private String helpID = null;
    private static final Real maxAirTemp = new Real(AIR_TEMP_F, 120);
    private static final Real minAirTemp = new Real(AIR_TEMP_F, 32);
    private static final Logger logger = Logger.getLogger(AirTemperatureGauge.class.getName());

    /**
     * Constructor creates new form TemperaturePanel.
     * @param title Title displayed above thermometer dial.
     * @param uom Unit of measure for temperatures (Fahrenheit or Celsius)
     * @param initialTemp
     */
    public AirTemperatureGauge(String title, Unit uom, Real initialTemp) {
        this(title, uom, initialTemp, null);
    }

    /**
     * Constructor creates new form TemperaturePanel.
     * @param title Title displayed above thermometer dial.
     * @param uom Unit of measure for temperatures (Fahrenheit or Celsius)
     * @param initialTemp
     * @param helpID The javahelp ID to be associated with the help button (can be null).
     */
    public AirTemperatureGauge(String title, Unit uom, Real initialTemp, String helpID) {
        if (!(uom.equals(GeneralUnit.degC) || uom.equals(GeneralUnit.degF))) {
            throw new IllegalArgumentException("Invalid UOM: must be degC or degF, not " + uom.toString());
        }
        initComponents();

        // Initialize the help button
        if (helpID == null || helpID.isEmpty()) {
            this.infoBtn.setVisible(false);
        } else {
            this.helpID = helpID;
        }
        // Initalize the JFreeChart
        chart = new TemperatureChart(title, uom);

        // Add the chart to the layout panel
        chartPanel.add(new ChartPanel(chart,
                105, // DEFAULT_WIDTH,
                200, // DEFAULT_HEIGHT,
                100, // DEFAULT_MINIMUM_DRAW_WIDTH, // Default = 300
                DEFAULT_MINIMUM_DRAW_HEIGHT,
                DEFAULT_MAXIMUM_DRAW_WIDTH,
                DEFAULT_MAXIMUM_DRAW_HEIGHT,
                DEFAULT_BUFFER_USED,
                false, // properties
                false, // save
                false, // print
                false, // zoom
                true // tooltips
        ));

        WeatherPreferences.addPreferenceChangeListener(e -> {
            if (e.getKey().equals(WeatherPreferences.PREF_AIR_TEMP_UOM)) {
                updateUom(WeatherPreferences.getAirTempUnit());
            }
        });
        updateUom(uom);

        setTemperature(initialTemp);

    }

    public final void updateUom(Unit uom) {
        try {
            this.uom = uom;
            this.range = maxAirTemp.getValue(uom) - minAirTemp.getValue(uom);
            this.chart.setUnits(uom);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Real getTemperature() {
        return chart.getTemperature();
    }

    public final void setTemperature(Real temperature) {
        this.temperature = temperature;
        chart.setTemperature(temperature);
        updateSlider(temperature);
    }

    private void updateSlider(Real temperature) {
        slider.setValue(convertTempToPercentRange(temperature));
    }

    // Convert air temp to a slider % value.
    private int convertTempToPercentRange(Real temperature) {
        try {
            if (temperature == null || temperature.isMissing()) {
                return 0;
            } else {
                double fraction = (temperature.getValue(uom) - minAirTemp.getValue(uom)) / range;
                return (int) round(fraction * 100);
            }
        } catch (VisADException ex) {
            return 0;
        }
    }

    private void updateTemperatureFromSlider(int sliderValue) {
        try {
            // Convert slider % value to an air temp in the configured UOM
            double airTemp = minAirTemp.getValue(uom) + (sliderValue * range / 100.0);
            Real oldTemp = getTemperature();
            Real newTemp = new Real(GeneralType.TEMPERATURE, airTemp, uom);
            chart.setTemperature(newTemp);
            // Notify listeners
            firePropertyChange(PROP_AIR_TEMP, oldTemp, newTemp);

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }


    private class TemperatureChartPanel extends ChartPanel {

        private TemperatureChart chart;

        TemperatureChartPanel() {
            this(new TemperatureChart());
        }

        TemperatureChartPanel(TemperatureChart chart) {
            super(chart,
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

            );
            this.chart = chart;
        }

        TemperatureChart getTemperatureChart() {
            return this.chart;
        }
    }

    /**
     * TemperatureChart is a JFreeChart integrated with a TemperaturePlot.
     */
    private class TemperatureChart extends JFreeChart {

        final DefaultValueDataset dataset;
        Unit uom;

        TemperatureChart() {
            this(new DefaultValueDataset(0.0));
        }

        TemperatureChart(DefaultValueDataset dataset) {
            super(new TemperaturePlot(dataset));
            this.dataset = dataset;
        }

        TemperatureChart(String title, Unit uom) {
            this(title, uom, new DefaultValueDataset(0.0));
        }

        TemperatureChart(String title, Unit uom, DefaultValueDataset dataset) {
            super(new TemperaturePlot(dataset));
            this.dataset = dataset;
            setUnits(uom);
            setTitle(title);
            getPlot().setBackgroundPaint(this.getBackgroundPaint());
        }

        Real getTemperature() {
            Number value = this.dataset.getValue();
            return new Real(uom.equals(GeneralUnit.degF) ? AIR_TEMP_F : AIR_TEMP_C,
                    value == null ? Double.NaN : value.doubleValue());
        }

        void setTemperature(Real temperature) {
            if (temperature == null || temperature.isMissing()) {
                this.dataset.setValue(null);
                return;
            }
            try {
                this.dataset.setValue(round(temperature.getValue(uom)));
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Unit getUnits() {
            return uom;
        }

        final void setUnits(Unit uom) {
            this.uom = uom;
            TemperaturePlot plot = (TemperaturePlot) getPlot();
            plot.setUnits(uom.equals(GeneralUnit.degF) ? ThermometerPlot.UNITS_FAHRENHEIT : ThermometerPlot.UNITS_CELCIUS);
            try {
                plot.setRange(minAirTemp.getValue(uom), maxAirTemp.getValue(uom));
            } catch (VisADException ex) {
            }
        }
    }

    /**
     * TemperaturePlot is a ThermometerPlot stylized for air temperature.
     */
    private class TemperaturePlot extends ThermometerPlot {

        TemperaturePlot(ValueDataset dataset) {
            super(dataset);
            setBulbRadius(30);
            setColumnRadius(15);
            //setThermometerStroke(new BasicStroke(2.0f));
            //setThermometerPaint(Color.darkGray);
            //setGap(3);

//            setUnits(uom.equals(GeneralUnit.degF) ? ThermometerPlot.UNITS_FAHRENHEIT : ThermometerPlot.UNITS_CELCIUS);
//            try {
//                setRange(minAirTemp.getValue(uom), maxAirTemp.getValue(uom));
//            } catch (VisADException ex) {
//            }
            setMercuryPaint(Color.red.brighter());
            // Set the subranges off the scale to prevent drawing tick marks
            setSubrange(0, -100, -100);
            setSubrange(1, -100, -100);
            setSubrange(2, -100, -100);
            setUseSubrangePaint(false);
//            setSubrange(0, 0.0, 85.0);
//            setSubrangePaint(0, Color.red);
//            setSubrange(1, 85.0, 125.0);
//            setSubrangePaint(1, Color.green);
//            setSubrange(2, 125.0, 200.0);
//            setSubrangePaint(2, Color.red);    
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

        chartPanel.setLayout(new java.awt.GridLayout(1, 0));

        sliderPanel.setLayout(new java.awt.BorderLayout());

        slider.setMajorTickSpacing(10);
        slider.setOrientation(javax.swing.JSlider.VERTICAL);
        slider.setPaintTicks(true);
        slider.setMaximumSize(new java.awt.Dimension(30, 32767));
        slider.setMinimumSize(new java.awt.Dimension(30, 17));
        slider.setPreferredSize(new java.awt.Dimension(28, 200));
        slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderStateChanged(evt);
            }
        });
        sliderPanel.add(slider, java.awt.BorderLayout.CENTER);

        infoBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emxsys/weather/images/help.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(infoBtn, org.openide.util.NbBundle.getMessage(AirTemperatureGauge.class, "AirTemperatureGauge.infoBtn.text")); // NOI18N
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
            .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sliderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderStateChanged

        updateTemperatureFromSlider(slider.getValue());

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
