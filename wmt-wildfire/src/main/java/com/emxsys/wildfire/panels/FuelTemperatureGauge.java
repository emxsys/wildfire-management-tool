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
package com.emxsys.wildfire.panels;

import com.emxsys.util.HelpUtil;
import com.emxsys.visad.GeneralType;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.wildfire.api.WildfirePreferences;
import static com.emxsys.wildfire.api.WildfireType.FUEL_TEMP_C;
import static com.emxsys.wildfire.api.WildfireType.FUEL_TEMP_F;
import java.awt.Color;
import static java.lang.Math.round;
import org.jfree.chart.ChartPanel;
import static org.jfree.chart.ChartPanel.DEFAULT_BUFFER_USED;
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
 * This panel displays a thermometer depicting fuel temperature. The slider generates PROP_FUEL_TEMP
 * PropertyChangeEvents when changed.
 *
 * @author Bruce Schubert
 */
public final class FuelTemperatureGauge extends javax.swing.JPanel {

    public final static String PROP_FUEL_TEMP = "wildfire.fueltempguage.fueltemp";

    private FuelTemperatureChart chart;
    private Unit uom;
    private double range;
    private String helpID;
    private static final Real maxFuelTemp = new Real(FUEL_TEMP_F, 140);
    private static final Real minFuelTemp = new Real(FUEL_TEMP_F, 32);

    /**
     * Constructor creates new form FuelTemperaturePanel.
     * @param title
     * @param uom Unit of measure for temperatures (Fahrenheit or Celsius)
     * @param initialTemp
     */
    public FuelTemperatureGauge(String title, Unit uom, Real initialTemp) {
        this(title, uom, initialTemp, null);
    }

    /**
     * Constructor creates new form FuelTemperaturePanel.
     * @param title Text displayed as a title
     * @param uom Unit of measure for temperatures (Fahrenheit or Celsius)
     * @param initialTemp Initial temperature
     * @param helpID ID of topic to be displayed (can be null)
     */
    public FuelTemperatureGauge(String title, Unit uom, Real initialTemp, String helpID) {
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
        chart = new FuelTemperatureChart(title, uom);
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
        // Monitor changes in UOM 
        WildfirePreferences.addPreferenceChangeListener(e -> {
            if (e.getKey().equals(WildfirePreferences.PREF_FUEL_TEMP_UOM)) {
                updateUom(WildfirePreferences.getFuelTemperatureUnit());
            }
        });

        updateUom(uom);
        setFuelTemperature(initialTemp);
    }

    public final void updateUom(Unit uom) {
        try {
            this.uom = uom;
            this.range = maxFuelTemp.getValue(uom) - minFuelTemp.getValue(uom);
            this.chart.setUnits(uom);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Real getFuelTemperature() {
        return chart.getFuelTemperature();
    }

    public void setFuelTemperature(Real value) {
        chart.setFuelTemperature(value);
        updateSlider(value);
    }

    public void setAirTemperature(Real value) {
        chart.setAirTemperatureMark(value);
    }

    private void updateSlider(Real temperature) {
        slider.setValue(convertTempToPercentRange(temperature));
    }

    // Convert fuel temp to a slider % value.
    private int convertTempToPercentRange(Real temperature) {
        try {
            if (temperature == null || temperature.isMissing()) {
                return 0;
            } else {
                double fraction = (temperature.getValue(uom) - minFuelTemp.getValue(uom)) / range;
                return (int) round(fraction * 100);
            }
        } catch (VisADException ex) {
            return 0;
        }
    }

    private void updateTemperatureFromSlider(int sliderValue) {
        try {
            // Convert slider % value to an air temp in the configured UOM
            double fuelTemp = minFuelTemp.getValue(uom) + (sliderValue * range / 100.0);
            Real oldTemp = getFuelTemperature();
            Real newTemp = new Real(GeneralType.TEMPERATURE, fuelTemp, uom);
            chart.setFuelTemperature(newTemp);
            // Notify listeners
            firePropertyChange(PROP_FUEL_TEMP, oldTemp, newTemp);

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * FuelTemperatureChart is a JFreeChart integrated with a TemperaturePlot.
     */
    private class FuelTemperatureChart extends JFreeChart {

        final DefaultValueDataset dataset;
        Unit uom;

        FuelTemperatureChart() {
            this(new DefaultValueDataset(0.0));
        }

        FuelTemperatureChart(DefaultValueDataset dataset) {
            super(new TemperaturePlot(dataset));
            this.dataset = dataset;
        }

        FuelTemperatureChart(String title, Unit uom) {
            this(title, uom, new DefaultValueDataset(0.0));
        }

        FuelTemperatureChart(String title, Unit uom, DefaultValueDataset dataset) {
            super(new TemperaturePlot(dataset));
            this.dataset = dataset;
            setUnits(uom);
            setTitle(title);
            getPlot().setBackgroundPaint(this.getBackgroundPaint());
        }

        Real getFuelTemperature() {
            Number value = this.dataset.getValue();
            return new Real(uom.equals(GeneralUnit.degF) ? FUEL_TEMP_F : FUEL_TEMP_C,
                    value == null ? Double.NaN : value.doubleValue());
        }

        void setFuelTemperature(Real temperature) {
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

        void setAirTemperatureMark(Real temperature) {
            TemperaturePlot plot = (TemperaturePlot) chart.getPlot();
            try {
                double airTemp = round(temperature.getValue(uom));
                plot.setSubrange(0, -100, airTemp);
                plot.setSubrange(1, airTemp, 200);
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
                plot.setRange(minFuelTemp.getValue(uom), maxFuelTemp.getValue(uom));
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
            setMercuryPaint(Color.red);

            // Initially set the subranges off the scale to prevent drawing tick marks
            setSubrange(0, -100, 200);
            setSubrangePaint(0, Color.red);
            setSubrange(1, -100, -100);
            setSubrangePaint(1, Color.red);
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

        chartPanel = new javax.swing.JPanel();
        sliderPanel = new javax.swing.JPanel();
        slider = new javax.swing.JSlider();
        infoBtn = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(200, 2147483647));
        setPreferredSize(new java.awt.Dimension(100, 283));

        chartPanel.setPreferredSize(new java.awt.Dimension(60, 0));
        chartPanel.setLayout(new java.awt.GridLayout(1, 0));

        sliderPanel.setLayout(new java.awt.BorderLayout());

        slider.setMajorTickSpacing(10);
        slider.setOrientation(javax.swing.JSlider.VERTICAL);
        slider.setPaintTicks(true);
        slider.setMaximumSize(new java.awt.Dimension(30, 32767));
        slider.setMinimumSize(new java.awt.Dimension(10, 17));
        slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderStateChanged(evt);
            }
        });
        sliderPanel.add(slider, java.awt.BorderLayout.CENTER);

        infoBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emxsys/wildfire/images/help.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(infoBtn, org.openide.util.NbBundle.getMessage(FuelTemperatureGauge.class, "FuelTemperatureGauge.infoBtn.text")); // NOI18N
        infoBtn.setMaximumSize(new java.awt.Dimension(28, 28));
        infoBtn.setMinimumSize(new java.awt.Dimension(28, 28));
        infoBtn.setPreferredSize(new java.awt.Dimension(28, 28));
        infoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoBtnActionPerformed(evt);
            }
        });
        sliderPanel.add(infoBtn, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(34, 34, 34))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGap(0, 120, Short.MAX_VALUE)
                    .addComponent(sliderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(sliderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .addContainerGap()))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderStateChanged

        updateTemperatureFromSlider(slider.getValue());

    }//GEN-LAST:event_sliderStateChanged

    private void infoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoBtnActionPerformed

        HelpUtil.showHelp(helpID);

    }//GEN-LAST:event_infoBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel chartPanel;
    private javax.swing.JButton infoBtn;
    private javax.swing.JSlider slider;
    private javax.swing.JPanel sliderPanel;
    // End of variables declaration//GEN-END:variables

}
