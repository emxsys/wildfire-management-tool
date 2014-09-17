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

import com.emxsys.visad.GeneralType;
import com.emxsys.visad.GeneralUnit;
import static com.emxsys.weather.api.WeatherType.AIR_TEMP_C;
import static com.emxsys.weather.api.WeatherType.AIR_TEMP_F;
import java.awt.Color;
import static java.lang.Math.round;
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
public class TemperaturePanel extends javax.swing.JPanel {

    private TemperatureChart airTempChart;
    private Unit uom;
    private double range;
    private static final Real maxAirTemp = new Real(AIR_TEMP_F, 120);
    private static final Real minAirTemp = new Real(AIR_TEMP_F, 32);

    /**
     * Constructor creates new form AirTempPanel.
     * @param title
     * @param uom Unit of measure for temperatures (Fahrenheit or Celsius)
     * @param initialTemp
     */
    public TemperaturePanel(String title, Unit uom, Real initialTemp) {
        if (!(uom.equals(GeneralUnit.degC) || uom.equals(GeneralUnit.degF))) {
            throw new IllegalArgumentException("Invalid UOM: must be degC or degF, not " + uom.toString());
        }
        initComponents();

        // Initalize the JFreeChart
        airTempChart = new TemperatureChart(title, uom);

        // Add the chart to the layout panel
        thermometerPanel.add(new ChartPanel(airTempChart,
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

        updateUom(uom);

        // Updating the slider should trigger event to update the thermometer
        updateSlider(initialTemp);
    }

    public final void updateUom(Unit uom) {
        try {
            this.uom = uom;
            this.range = maxAirTemp.getValue(uom) - minAirTemp.getValue(uom);
            this.airTempChart.setUnits(uom);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void setTemperature(Real temperature) {
        airTempChart.setTemperature(temperature);
        updateSlider(temperature);
    }

    public Real getTemperature() {
        return airTempChart.getTemperature();
    }

    // Convert air temp to a slider % value.
    private int convertTempToPercentRange(Real temperature) {
        try {
            double fraction = (temperature.getValue(uom) - minAirTemp.getValue(uom)) / range;
            return (int) round(fraction * 100);
        } catch (VisADException ex) {
            return 0;
        }
    }

    private void updateSlider(Real temperature) {
        slider.setValue(convertTempToPercentRange(temperature));
    }

    void updateTemperature(int sliderValue) {
        try {
            // Convert slider % value to an air temp in the configured UOM
            double airTemp = minAirTemp.getValue(uom) + (sliderValue * range / 100.0);
            airTempChart.setTemperature(new Real(GeneralType.TEMPERATURE, airTemp, uom));
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * TemperatureChart is a JFreeChart integrated with a TemperaturePlot.
     */
    private class TemperatureChart extends JFreeChart {

        final DefaultValueDataset dataset;
        Unit uom;

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
            double value = this.dataset.getValue().doubleValue();
            return new Real(uom.equals(GeneralUnit.degF) ? AIR_TEMP_F : AIR_TEMP_C, value);
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

        updateTemperature(slider.getValue());

    }//GEN-LAST:event_sliderStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider slider;
    private javax.swing.JPanel thermometerPanel;
    private javax.swing.ButtonGroup unitsButtonGroup;
    // End of variables declaration//GEN-END:variables

}
