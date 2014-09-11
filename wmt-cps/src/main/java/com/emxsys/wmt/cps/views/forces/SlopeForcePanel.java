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

import com.emxsys.jfree.ChartUtil;
import com.emxsys.gis.api.Terrain;
import com.emxsys.util.AngleUtil;
import com.emxsys.wmt.cps.Model;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.logging.Logger;
import org.jfree.chart.ChartPanel;
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
import org.openide.util.NbBundle.Messages;

/**
 * The SlopeForcePanel depicts the terrain's aspect and slope with a compass chart and dial chart
 respectively. Slope is one of the primary forces in the CPS assessment of fire behavior.
 *
 * @author Bruce Schubert
 */
@Messages({
    "# {0} - cardinal point",
    "CTL_AspectChartTitle={0} Aspect",
    "# {0} - percent",
    "CTL_SlopeChartTitle={0}% Grade",
    "# {0} - degrees",
    "CTL_SlopeChartSubTitle={0}° Slope",})
public class SlopeForcePanel extends javax.swing.JPanel {

    private static final Logger logger = Logger.getLogger(SlopeForcePanel.class.getName());
    private JFreeChart aspectChart;
    private SlopeChart slopeChart = new SlopeChart(Bundle.CTL_SlopeChartTitle(0));

    /**
     * SlopePlot is a DialPlot stylized for angle of slope.
     */
    private class SlopePlot extends DialPlot {

        SlopePlot(ValueDataset dataset) {
            super(dataset);
            // Set the viewport of the circular dial;
            // Set to pper right quadrant of circle with extra left and bottom for labels
            setView(0.4, 0.0, 0.6, 0.6);    
            setInsets(RectangleInsets.ZERO_INSETS);
            
            // Frame
            ArcDialFrame dialFrame = new ArcDialFrame(-2.0, 94.0);
            dialFrame.setInnerRadius(0.40);
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
            double MIN_SLOPE = 0;
            double MAX_SLOPE = 90;
            StandardDialScale scale = new StandardDialScale(MIN_SLOPE, MAX_SLOPE, 0, 90.0, 10.0, 4);
            scale.setTickRadius(0.6);
            scale.setTickLabelFormatter(new DecimalFormat("#0°"));
            scale.setTickLabelOffset(-0.1);
            scale.setMajorTickIncrement(10.0);  // Labeled increments
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
     * SlopeChart is a JFreeChart integrated with a SlopePlot.
     */
    private class SlopeChart extends JFreeChart {

        final DefaultValueDataset dataset;

        SlopeChart(String title) {
            this(title, new DefaultValueDataset(0.0));
        }

        SlopeChart(String title, DefaultValueDataset dataset) {
            super(new SlopePlot(dataset));
            this.dataset = dataset;
            setTitle(title);
            setPadding(RectangleInsets.ZERO_INSETS);
        }
    }
    
    /**
     * Creates new form SlopePanel
     */
    public SlopeForcePanel() {
        initComponents();
        createCharts();
        
        // Add a listener to the CPS model 
        Model.getInstance().addPropertyChangeListener(Model.PROP_TERRAIN, (PropertyChangeEvent evt) -> {
            updateCharts((Terrain) evt.getNewValue());
        });
    }

    /**
     * Updates the JFreeCharts.
     *
     * @param terrain
     */
    private void updateCharts(Terrain terrain) {

        // Update Aspect Chart
        double aspect = AngleUtil.normalize360(terrain.getAspectDegrees());
        CompassPlot compassPlot = (CompassPlot) aspectChart.getPlot();
        DefaultValueDataset compassData = (DefaultValueDataset) compassPlot.getDatasets()[0];
        compassData.setValue(aspect);
        aspectChart.setTitle(Bundle.CTL_AspectChartTitle(AngleUtil.degreesToCardinalPoint8(aspect)));

        // Update Slope Chart
        DialPlot dialPlot = (DialPlot) slopeChart.getPlot();
        DefaultValueDataset dialData = (DefaultValueDataset) dialPlot.getDataset();
        dialData.setValue(terrain.getSlopeDegrees());
        slopeChart.setTitle(Bundle.CTL_SlopeChartTitle(Long.toString(Math.round(terrain.getSlopePercent()))));
    }

    /**
     * Creates the JFreeCharts.
     */
    private void createCharts() {
        aspectChart = ChartUtil.createCommonCompassChart(Bundle.CTL_AspectChartTitle(""), null, ChartUtil.WIND_NEEDLE, Color.GREEN);
        aspectPanel.add(new ChartPanel(aspectChart));
        slopePanel.add(new ChartPanel(slopeChart));
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

        aspectPanel = new javax.swing.JPanel();
        slopePanel = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SlopeForcePanel.class, "SlopeForcePanel.border.title"))); // NOI18N
        setLayout(new java.awt.GridLayout(1, 2));

        aspectPanel.setLayout(new javax.swing.BoxLayout(aspectPanel, javax.swing.BoxLayout.LINE_AXIS));
        add(aspectPanel);

        slopePanel.setLayout(new javax.swing.BoxLayout(slopePanel, javax.swing.BoxLayout.LINE_AXIS));
        add(slopePanel);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aspectPanel;
    private javax.swing.JPanel slopePanel;
    // End of variables declaration//GEN-END:variables

}
