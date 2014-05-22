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
import com.emxsys.jfree.ChartUtil;
import static com.emxsys.jfree.ChartUtil.WIND_NEEDLE;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.wmt.cps.wx.ManualWeatherProvider;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import org.jfree.chart.plot.CompassPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.dial.ArcDialFrame;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.openide.util.NbBundle;
import visad.Real;

/**
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({
    "CTL_WindDirChartTitle=Direction",
    "CTL_WindSpdChartTitle=Speed",})
public class WindForcePanel extends javax.swing.JPanel {

    private WindDirChart dirChart = new WindDirChart(Bundle.CTL_WindDirChartTitle());
    private WindSpdChart spdChart = new WindSpdChart(Bundle.CTL_WindSpdChartTitle());
    private ChartCanvas dirCanvas;
    private ChartCanvas spdCanvas;
    private JSlider dirSlider = new DirectionSlider(dirChart);
    private JSlider spdSlider = new SpeedSlider(spdChart);

    /**
     * The DirectionSlider updates the WindDirChart and the ManualWeatherProvider.
     */
    private class DirectionSlider extends JSlider {

        DirectionSlider(WindDirChart chart) {
            super(0, 360, 180);
            setPaintLabels(false);
            setPaintTicks(true);
            setMajorTickSpacing(25);
            setOrientation(SwingConstants.VERTICAL);
            addChangeListener((ChangeEvent e) -> {
                int windDir = getValue();
                chart.dataset.setValue(windDir);
                ManualWeatherProvider.getInstance().setWindDirection(new Real(WeatherType.WIND_DIR, windDir));
            });
        }
    }

    private class SpeedSlider extends JSlider {

        SpeedSlider(WindSpdChart chart) {
            super(0, 100, 0);
            setPaintLabels(false);
            setPaintTicks(true);
            setMajorTickSpacing(25);
            setOrientation(SwingConstants.VERTICAL);
            addChangeListener((ChangeEvent e) -> {
                int windSpd = getValue();
                chart.dataset.setValue(windSpd);
                ManualWeatherProvider.getInstance().setWindSpeed(new Real(WeatherType.WIND_SPEED_MPH, windSpd));
            });
        }
    }

    /**
     * WindDirPlot is a CompassPlot stylized for wind direction.
     */
    private class WindDirPlot extends CompassPlot {

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
    private class WindDirChart extends JFreeChart {

        final DefaultValueDataset dataset;

        WindDirChart(String title) {
            this(title, new DefaultValueDataset(0.0));
        }

        WindDirChart(String title, DefaultValueDataset dataset) {
            super(new WindDirPlot(dataset));
            this.dataset = dataset;
            setTitle(title);
        }
    }

    /**
     * WindSpdPlot is a DialPlot stylized for wind speed.
     */
    private class WindSpdPlot extends DialPlot {

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
            double MAX_SPEED = 100;
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

        @Override
        public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
            super.draw(g2, area, anchor, parentState, info); //To change body of generated methods, choose Tools | Templates.
        }

    }

    /**
     * WindSpdChart is a JFreeChart integrated with a WindSpdPlot.
     */
    private class WindSpdChart extends JFreeChart {

        final DefaultValueDataset dataset;

        WindSpdChart(String title) {
            this(title, new DefaultValueDataset(0.0));
        }

        WindSpdChart(String title, DefaultValueDataset dataset) {
            super(new WindSpdPlot(dataset));
            this.dataset = dataset;
            setTitle(title);
            setPadding(RectangleInsets.ZERO_INSETS);
        }
    }

    /**
     * Creates new form WindPanel
     */
    public WindForcePanel() {
        initComponents();

        JFXPanel leftPanel = new JFXPanel();                    // ClockCompass
        JPanel rightPanel = new JPanel(new BorderLayout());     // Dial and controls
        add(leftPanel);
        add(rightPanel);

        // Layout the right panel
        JPanel dialPanel = new JPanel(new BorderLayout());      // Speed dial
        JPanel sliderPanel = new JPanel(new GridLayout(1, 2));  // Slider controls
        dialPanel.add(new ChartPanel(spdChart, DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                200, // DEFAULT_MINIMUM_DRAW_WIDTH, // Default = 300
                DEFAULT_MINIMUM_DRAW_HEIGHT,
                DEFAULT_MAXIMUM_DRAW_WIDTH,
                DEFAULT_MAXIMUM_DRAW_HEIGHT,
                DEFAULT_BUFFER_USED,
                true, // properties
                true, // save
                true, // print
                true, // zoom
                true) // tooltips
        );
        sliderPanel.add(dirSlider);
        sliderPanel.add(spdSlider);
        rightPanel.add(dialPanel, BorderLayout.CENTER);
        rightPanel.add(sliderPanel, BorderLayout.EAST);

        // Create the JavaFX scenes on an FX thread
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            leftPanel.setScene(createDirScene());
            //dialPanel.setScene(createSpdScene());
        });
    }

    private Scene createDirScene() {
        dirCanvas = new ChartCanvas(dirChart);
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(dirCanvas);
        // Bind canvas size to stack pane size. 
        dirCanvas.widthProperty().bind(stackPane.widthProperty());
        dirCanvas.heightProperty().bind(stackPane.heightProperty());

        return new Scene(stackPane);
    }

//    private Scene createSpdScene() {
//        spdCanvas = new ChartCanvas(spdChart);
//        StackPane stackPane = new StackPane();
//        stackPane.getChildren().add(spdCanvas);
//        // Bind canvas size to stack pane size. 
//        spdCanvas.widthProperty().bind(stackPane.widthProperty());
//        spdCanvas.heightProperty().bind(stackPane.heightProperty());
//
//        return new Scene(stackPane);
//    }
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(WindForcePanel.class, "WindForcePanel.border.title"))); // NOI18N
        setLayout(new java.awt.GridLayout(1, 2));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
