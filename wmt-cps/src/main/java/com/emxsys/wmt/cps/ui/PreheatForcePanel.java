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
package com.emxsys.wmt.cps.ui;

import com.emxsys.jfree.ChartCanvas;
import com.emxsys.jfree.ClockCompassPlot;
import static com.emxsys.jfree.ClockCompassPlot.CLOCK_HAND_NEEDLE;
import static com.emxsys.jfree.ClockCompassPlot.WIND_NEEDLE;
import com.emxsys.util.AngleUtil;
import com.emxsys.weather.api.SimpleWeatherProvider;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.wmt.cps.Controller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import org.jfree.chart.ChartPanel;
import static org.jfree.chart.ChartPanel.DEFAULT_BUFFER_USED;
import static org.jfree.chart.ChartPanel.DEFAULT_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH;
import static org.jfree.chart.ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_WIDTH;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import visad.CommonUnit;
import visad.Real;
import visad.VisADException;

/**
 * The PreheatForcePanel depicts the direction of the sun's rays onto the terrain and shows the
 * surface temperature of the fuels. The panel listens to the application time and the reticule
 * coordinate.
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({
    "CTL_SolarChartTitle=Solar Heating",
    "CTL_AirTempChartTitle=Air",
    "CTL_FuelTempChartTitle=Fuel",})
public class PreheatForcePanel extends javax.swing.JPanel {

    private static final int AZIMUTH_SERIES = 0;
    private static final int HOUR_SERIES = 1;
    private final SolarChart solarChart = new SolarChart(Bundle.CTL_SolarChartTitle());
    private TemperatureChart airTempChart = new TemperatureChart(Bundle.CTL_AirTempChartTitle());
    private TemperatureChart fuelTempChart = new TemperatureChart(Bundle.CTL_FuelTempChartTitle());
    private JSlider slider;
    private ChartCanvas canvas;

    /**
     * SolarPlot is a ClockCompassPlot stylized for solar heating.
     */
    private class SolarPlot extends ClockCompassPlot {

        SolarPlot() {
            setRosePaint(Color.orange);
            setRoseHighlightPaint(Color.gray);
            setRoseCenterPaint(Color.white);
            setDrawBorder(false);

            // The first (default) dataset is the direction of Solar Radiation
            setSeriesNeedle(AZIMUTH_SERIES, WIND_NEEDLE);
            setSeriesPaint(AZIMUTH_SERIES, Color.red);        // arrow heads
            setSeriesOutlinePaint(AZIMUTH_SERIES, Color.red); // arrow shafts and arrow head outline

            // The second  dataset is the Time / Clock
            ValueDataset dataset = new DefaultValueDataset(new Double(0.0));
            addDataset(dataset, null);
            setSeriesNeedle(HOUR_SERIES, CLOCK_HAND_NEEDLE);
            setSeriesPaint(HOUR_SERIES, Color.black);        // arrow heads
            setSeriesOutlinePaint(HOUR_SERIES, Color.black); // arrow shafts and arrow head outline        
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
     * TemperaturePlot is a ThermometerPlot stylized for air/fuel temperature.
     */
    private class TemperaturePlot extends ThermometerPlot {

        TemperaturePlot(ValueDataset dataset) {
            super(dataset);
            setBulbRadius(30);
            setColumnRadius(15);
            //setThermometerStroke(new BasicStroke(2.0f));
            //setThermometerPaint(Color.darkGray);
            //setGap(3);
            setUnits(ThermometerPlot.UNITS_FAHRENHEIT);
            setRange(0.0, 200.0);
            setMercuryPaint(Color.red);
//            setSubrange(0, 0.0, 85.0);
//            setSubrangePaint(0, Color.red);
//            setSubrange(1, 85.0, 125.0);
//            setSubrangePaint(1, Color.green);
//            setSubrange(2, 125.0, 200.0);
//            setSubrangePaint(2, Color.red);    
            setOutlineVisible(false);
        }
    }

    /**
     * TemperatureChart is a JFreeChart integrated with a TemperaturePlot.
     */
    private class TemperatureChart extends JFreeChart {

        final DefaultValueDataset dataset;

        TemperatureChart(String title) {
            this(title, new DefaultValueDataset(0.0));
        }

        TemperatureChart(String title, DefaultValueDataset dataset) {
            super(new TemperaturePlot(dataset));
            this.dataset = dataset;
            setTitle(title);
            getPlot().setBackgroundPaint(this.getBackgroundPaint());
        }
    }

    /**
     * Creates new form PreheatForcePanel.
     */
    public PreheatForcePanel() {
        initComponents();

        // Create the primary panels
        JFXPanel leftPanel = new JFXPanel();
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Create the thermometers
        JPanel thermometerPanel = new JPanel(new GridLayout(1, 2));
        thermometerPanel.add(new ChartPanel(airTempChart,
                DEFAULT_WIDTH,
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
        thermometerPanel.add(new ChartPanel(fuelTempChart,
                DEFAULT_WIDTH,
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
        
        rightPanel.add(thermometerPanel, BorderLayout.CENTER);
        
        // Create the slider for the air temp
        this.slider = new JSlider(0, 200, 100);
        this.slider.setPaintLabels(false);
        this.slider.setPaintTicks(true);
        this.slider.setMajorTickSpacing(25);
        this.slider.setOrientation(SwingConstants.VERTICAL);
        this.slider.addChangeListener((ChangeEvent e) -> {
            int airTemp = slider.getValue();
            airTempChart.dataset.setValue(airTemp);
            Controller.getInstance().getSimpleWeather().setAirTemperature(new Real(WeatherType.AIR_TEMP_F, airTemp));
        });
        rightPanel.add(this.slider, BorderLayout.EAST);

        // Add the panel to the Grid layout
        add(leftPanel);
        add(rightPanel);

        // Create the JavaFX scene (ChartCanvas) on an FX thread
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            leftPanel.setScene(createScene());
        });
    }

    private Scene createScene() {
        canvas = new ChartCanvas(solarChart);
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(canvas);
        // Bind canvas size to stack pane size. 
        canvas.widthProperty().bind(stackPane.widthProperty());
        canvas.heightProperty().bind(stackPane.heightProperty());

        return new Scene(stackPane);
    }

    /**
     * Updates the JFreeCharts.
     *
     * @param time UTC time.
     */
    public void updateCharts(ZonedDateTime time, Real azimuth, Real zenith, boolean isShaded) {

        try {
            // Update the Azimuth Plot
            ClockCompassPlot compassPlot = (ClockCompassPlot) solarChart.getPlot();
            double A = azimuth.getValue(CommonUnit.degree);
            DefaultValueDataset compassData = (DefaultValueDataset) compassPlot.getDatasets()[AZIMUTH_SERIES];
            compassData.setValue(A);

            // Update the Hour plot
            DefaultValueDataset hourData = (DefaultValueDataset) compassPlot.getDatasets()[HOUR_SERIES];
            final double DEG_PER_HOUR12 = 360 / 12.0;
            double hour = time.get(ChronoField.MINUTE_OF_DAY) / 60.;
            double hourDegrees = (hour % 12.0) * DEG_PER_HOUR12;
            hourData.setValue(hourDegrees);

            // Color the background based on the Zenith angle (above or below the horizon)
            double Z = Math.abs(zenith.getValue(CommonUnit.degree));
            if (Z < 90) {
                // Sun above the horizon
                compassPlot.setSeriesPaint(AZIMUTH_SERIES, Color.red);
                compassPlot.setSeriesOutlinePaint(AZIMUTH_SERIES, Color.red);
                compassPlot.setRoseCenterPaint(isShaded ? Color.lightGray : Color.white);
                String title = String.format("%1$s Solar Heating", AngleUtil.degreesToCardinalPoint8(A));
                solarChart.setTitle(title);
            } else {
                // Sun below the horizon
                compassPlot.setSeriesPaint(AZIMUTH_SERIES, Color.lightGray);
                compassPlot.setSeriesOutlinePaint(AZIMUTH_SERIES, Color.lightGray);
                compassPlot.setRoseCenterPaint(Color.darkGray);
                String title = "Nighttime";
                solarChart.setTitle(title);
            }
            canvas.draw();

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
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

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PreheatForcePanel.class, "PreheatForcePanel.border.title"))); // NOI18N
        setLayout(new java.awt.GridLayout(1, 2));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
