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
package com.emxsys.wmt.cps.views.forces;

import com.emxsys.jfree.ClockCompassPlot;
import static com.emxsys.jfree.ClockCompassPlot.CLOCK_HAND_NEEDLE;
import static com.emxsys.jfree.ClockCompassPlot.WIND_NEEDLE;
import com.emxsys.solar.api.Sunlight;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.visad.Reals;
import com.emxsys.weather.api.Weather;
import com.emxsys.wildfire.api.WildfirePreferences;
import com.emxsys.wildfire.api.WildfireType;
import com.emxsys.wildfire.behavior.SurfaceFuel;
import com.emxsys.wildfire.panels.FuelTemperatureGauge;
import com.emxsys.wmt.cps.Controller;
import com.emxsys.wmt.cps.Model;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import static java.lang.Math.min;
import static java.lang.Math.round;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.jfree.chart.ChartPanel;
import static org.jfree.chart.ChartPanel.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import visad.CommonUnit;
import visad.Real;
import visad.VisADException;

/**
 * The PreheatForceView depicts the direction of the sun's rays onto the terrain and shows the
 * surface temperature of the fuels. The panel listens to the application time and the reticule
 * coordinate.
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({
    "CTL_SolarChartTitle=Solar Heating",
    "CTL_FuelMoistureChartTitle=Fine Fuel Moisture",
    "CTL_FuelTempChartTitle=Fuel Temp."
})
public final class PreheatForceView extends javax.swing.JPanel {

    // Properties that are available from this panel
    public static final String PROP_FUELTEMP = "PreheatForceView.FuelTemp";

    private SolarChart solarChart;
    private MoistureChart fuelMoistureGauge;
    private FuelTemperatureGauge fuelTempGauge;

    private LineBorder lineBorder = new LineBorder(Color.black);
    private EmptyBorder emptyBorder = new EmptyBorder(1, 1, 1, 1);

    // SolarChart/SolarPlot members
    private static final int SOLAR_AZIMUTH_SERIES = 0;
    private static final int SOLAR_HOUR_SERIES = 1;
    private DateTimeFormatter titleFormatter = DateTimeFormatter.ofPattern("dd-MMM, HH:mm z");

    //private DateTimeFormatter titleFormatter = DateTimeFormatter.ofPattern(TimeOptions.getTimeFormat());
    //private ChartCanvas canvas;
    /**
     * Creates new form PreheatForcePanel.
     */
    public PreheatForceView() {
        initComponents();
        initializeView();

        // Syncronize this View to the Model via PropertyChangeEvents
        Model.getInstance().addPropertyChangeListener(Model.PROP_SUNLIGHT, (PropertyChangeEvent evt) -> {
            updateSunlight((Sunlight) evt.getNewValue());
        });
        updateSunlight(Model.getInstance().getSunlight());

        Model.getInstance().addPropertyChangeListener(Model.PROP_SHADED, (PropertyChangeEvent evt) -> {
            updateShading((boolean) evt.getNewValue());
        });
        
        Model.getInstance().addPropertyChangeListener(Model.PROP_WEATHER, (PropertyChangeEvent evt) -> {
            Weather wx = Model.getInstance().getWeather();
            updateAirTemp(wx.getAirTemperature());
        });
        
        Model.getInstance().addPropertyChangeListener(Model.PROP_FUELBED, (PropertyChangeEvent evt) -> {
            SurfaceFuel fuel = (SurfaceFuel) evt.getNewValue();
            if (fuel != null) {
                Real newFuelTemp = fuel.getFuelTemperature();
                Real oldFuelTemp = fuelTempGauge.getFuelTemperature();
                // Update the fuel chart if the new value is not equal to the existing value
                if (!Reals.nearlyEquals(newFuelTemp, oldFuelTemp, GeneralUnit.degF, 1.0d)) {
                    fuelTempGauge.setFuelTemperature(fuel.getFuelTemperature());
                    fuelTempGauge.setBorder(emptyBorder);
                }
                fuelMoistureGauge.setMoisture(fuel.isBurnable() ? fuel.getDead1HrFuelMoisture() : null);
                fuelMoistureGauge.setMoistureOfExtinction(fuel.isBurnable() ? fuel.getFuelModel().getMoistureOfExtinction() : null);
            }
        });


        // Update the Controller from inputs in this View
        this.fuelTempGauge.addPropertyChangeListener(FuelTemperatureGauge.PROP_FUEL_TEMP, (PropertyChangeEvent evt) -> {
            Real fuelTemp = (Real) evt.getNewValue();
            Controller.getInstance().setFuelTemperature(fuelTemp);
            firePropertyChange(PROP_FUELTEMP, (Real) evt.getOldValue(), fuelTemp);
            fuelTempGauge.setBorder(lineBorder);    // indicates manual override
        });

    }

    /**
     * Updates the solar azimuth plot.
     */
    public void updateSunlight(Sunlight sun) {

        try {
            SolarPlot solarPlot = (SolarPlot) solarChart.getPlot();

            // Update the Azimuth Plot (solar vectors)
            solarPlot.setAzimuthAngle(sun.getAzimuthAngle());

            // Color the background based on the Zenith angle (above or below the horizon)
            double Z = Math.abs(sun.getZenithAngle().getValue(CommonUnit.degree));
            solarPlot.night = (Z > 90);

            // Update the Time Plot (clock hands)
            solarPlot.setClockTime(sun.getDateTime().toLocalTime());

            // Set the title
            //  Using date/time instead            
            //String title = solarPlot.night ? "Night" : String.format("%1$s Solar Heating", AngleUtil.degreesToCardinalPoint8(A));
            //solarChart.setTitle(title);
            refresh();

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void updateShading(boolean isShaded) {
        SolarPlot solarPlot = (SolarPlot) solarChart.getPlot();
        solarPlot.shaded = isShaded;
        refresh();
    }

    /**
     * Updates the solar azimuth plot.
     */
    public void refresh() {

        SolarPlot solarPlot = (SolarPlot) solarChart.getPlot();

        Color seriesColor = solarPlot.night || solarPlot.shaded ? Color.lightGray : Color.red;
        Color centerColor = solarPlot.night ? Color.darkGray : Color.white;
        solarPlot.setSeriesPaint(SOLAR_AZIMUTH_SERIES, seriesColor);
        solarPlot.setSeriesOutlinePaint(SOLAR_AZIMUTH_SERIES, seriesColor);
        solarPlot.setRoseCenterPaint(centerColor);
        //canvas.draw();
    }

    /**
     * Sets the tick mark indicating air temperature.
     *
     * @param airTemperature
     */
    public void updateAirTemp(Real airTemperature) {
        fuelTempGauge.setAirTemperature(airTemperature);
    }

    private void initializeView() {
        solarChart = new SolarChart(Bundle.CTL_SolarChartTitle());
        fuelMoistureGauge = new MoistureChart(Bundle.CTL_FuelMoistureChartTitle());
        fuelTempGauge = new FuelTemperatureGauge(Bundle.CTL_FuelTempChartTitle(),
                WildfirePreferences.getFuelTemperatureUnit(),
                new Real(WildfireType.FUEL_TEMP_F, 0), "com.emxsys.wmt.cps.fueltemperature");

        // Override the default title font size so we can display long date time strings
        TextTitle title = solarChart.getTitle();
        Font font = title.getFont().deriveFont(11);
        title.setFont(font);

        leftPanel.add(new ChartPanel(solarChart,
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
                true // tooltips
        ));

        // Create the fuel properties panel
        JPanel fuelPropertiesPanel = new JPanel(); // Layout Manager with percentages
        fuelPropertiesPanel.setLayout(new BoxLayout(fuelPropertiesPanel, BoxLayout.X_AXIS));

        temperaturePanel.add(fuelTempGauge);
        moisturePanel.add(new ChartPanel(fuelMoistureGauge,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
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
        //rightPanel.add(fuelPropertiesPanel, BorderLayout.CENTER);

//        // Create the slider for the fuel temp (140 deg max)
//        this.slider = new JSlider(32, 140, 59); // fahrenheit
//        this.slider.setPaintLabels(false);
//        this.slider.setPaintTicks(true);
//        this.slider.setMajorTickSpacing(10);
//        this.slider.setOrientation(SwingConstants.VERTICAL);
//        this.slider.setEnabled(false);
//
//        JPanel sliderPanel = new JPanel(new BorderLayout());
//        sliderPanel.add(this.slider, BorderLayout.CENTER);
//
//        // Add the panel to the Grid layout
//        rightPanel.add(sliderPanel, BorderLayout.EAST);
//        add(leftPanel);
//        add(rightPanel);
    }

//    private Scene createScene() {
//        canvas = new ChartCanvas(solarChart);
//        StackPane stackPane = new StackPane();
//        stackPane.getChildren().add(canvas);
//        // Bind canvas size to stack pane size. 
//        canvas.widthProperty().bind(stackPane.widthProperty());
//        canvas.heightProperty().bind(stackPane.heightProperty());
//
//        return new Scene(stackPane);
//    }
    /**
     * MoistureChart is a JFreeChart integrated with a MoisturePlot.
     */
    private class MoistureChart extends JFreeChart {

        final DefaultValueDataset dataset;

        MoistureChart(String title) {
            this(title, new DefaultValueDataset(0.0));
        }

        MoistureChart(String title, DefaultValueDataset dataset) {
            super(new MoisturePlot(dataset));
            this.dataset = dataset;
            setTitle(title);
            getPlot().setBackgroundPaint(this.getBackgroundPaint());
        }

        void setMoisture(Real moisture) {
            if (moisture == null || moisture.isMissing()) {
                this.dataset.setValue(null);
                return;
            }
            this.dataset.setValue(min(round(moisture.getValue()), 100));
        }

        void setMoistureOfExtinction(Real extinction) {
            double value = (extinction == null || extinction.isMissing())
                    ? 0 : extinction.getValue();
            MoisturePlot plot = (MoisturePlot) getPlot();
            plot.updateMoistureSubranges(value);
        }
    }

    /**
     * SolarPlot is a ClockCompassPlot stylized for solar heating.
     */
    private class SolarPlot extends ClockCompassPlot {

        boolean shaded = false;
        boolean night = false;

        SolarPlot() {
            setRosePaint(Color.orange);
            setRoseHighlightPaint(Color.gray);
            setRoseCenterPaint(Color.white);
            setDrawBorder(false);

            // The first (default) dataset is the direction of Solar Radiation
            setSeriesNeedle(SOLAR_AZIMUTH_SERIES, WIND_NEEDLE);
            setSeriesPaint(SOLAR_AZIMUTH_SERIES, Color.red);        // arrow heads
            setSeriesOutlinePaint(SOLAR_AZIMUTH_SERIES, Color.red); // arrow shafts and arrow head outline

            // The second  dataset is the Time / Clock
            ValueDataset dataset = new DefaultValueDataset(new Double(0.0));
            addDataset(dataset, null);
            setSeriesNeedle(SOLAR_HOUR_SERIES, CLOCK_HAND_NEEDLE);
            setSeriesPaint(SOLAR_HOUR_SERIES, Color.black);        // clock hands
            setSeriesOutlinePaint(SOLAR_HOUR_SERIES, Color.black); //        
        }

        /**
         * Update the solar vectors
         * @param aziumthAngle 
         */
        public void setAzimuthAngle(Real aziumthAngle) {
            try {
                // Update the Azimuth Plot (solar vectors)
                DefaultValueDataset compassData = (DefaultValueDataset) getDatasets()[SOLAR_AZIMUTH_SERIES];
                double A = aziumthAngle.getValue(CommonUnit.degree);
                compassData.setValue(A);
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         * Update the clock hands
         * @param localTime 
         */
        public void setClockTime(LocalTime localTime) {
            // Update the Time Plot (clock hands)
            DefaultValueDataset hourData = (DefaultValueDataset) getDatasets()[SOLAR_HOUR_SERIES];

            final double DEG_PER_HOUR_12 = 360 / 12.0;
            double hour = localTime.get(ChronoField.MINUTE_OF_DAY) / 60.0;
            double hourDegrees = (hour % 12.0) * DEG_PER_HOUR_12;
            hourData.setValue(hourDegrees);

        }
    }

    /**
     * SolarChart is a JFreeChart integrated with a SolarPlot.
     */
    private class SolarChart extends JFreeChart {

        SolarChart(String title) {
            super(new SolarPlot());
            setTitle(title);
        }
    }

    /**
     * MoisturePlot is a ThermometerPlot stylized for fuel moisture.
     */
    class MoisturePlot extends ThermometerPlot {

        MoisturePlot(ValueDataset dataset) {
            super(dataset);
            setBulbRadius(30);
            setColumnRadius(15);
            //setThermometerStroke(new BasicStroke(2.0f));
            //setThermometerPaint(Color.darkGray);
            //setGap(3);
            setUnits(ThermometerPlot.UNITS_NONE);
            setRange(0.0, 40.0);
            updateMoistureSubranges(0);    // initial rangle
            setOutlineVisible(false);
        }

        final void updateMoistureSubranges(double moistureOfExt) {
            setSubrange(0, 0.0, moistureOfExt);      // burnable
            setSubrangePaint(0, Color.orange);
            setSubrange(1, moistureOfExt, 100.0);    // unburnable
            setSubrangePaint(1, Color.green);
            setSubrange(2, -1, -1);         // Set off the scale to hide
            setSubrangePaint(2, Color.orange);
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        leftPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();
        temperaturePanel = new javax.swing.JPanel();
        moisturePanel = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PreheatForceView.class, "PreheatForceView.border.title"))); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        leftPanel.setBorder(null);
        leftPanel.setPreferredSize(new java.awt.Dimension(100, 100));
        leftPanel.setLayout(new java.awt.BorderLayout());
        add(leftPanel);

        rightPanel.setLayout(new javax.swing.BoxLayout(rightPanel, javax.swing.BoxLayout.LINE_AXIS));

        temperaturePanel.setBorder(null);
        temperaturePanel.setPreferredSize(new java.awt.Dimension(70, 100));
        temperaturePanel.setLayout(new java.awt.BorderLayout());
        rightPanel.add(temperaturePanel);

        moisturePanel.setBorder(null);
        moisturePanel.setPreferredSize(new java.awt.Dimension(30, 100));
        moisturePanel.setLayout(new java.awt.BorderLayout());
        rightPanel.add(moisturePanel);

        add(rightPanel);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel moisturePanel;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel temperaturePanel;
    // End of variables declaration//GEN-END:variables
}
