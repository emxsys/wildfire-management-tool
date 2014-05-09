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
package com.emxsys.wmt.cps;

import static com.emxsys.wmt.cps.charts.CompassClockPlot.CLOCK_HAND_NEEDLE;
import static com.emxsys.wmt.cps.charts.CompassClockPlot.WIND_NEEDLE;
import com.emxsys.wmt.cps.charts.CompassClockPlot;
import com.emxsys.wmt.util.AngleUtil;
import java.awt.Color;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import visad.CommonUnit;
import visad.Real;
import visad.VisADException;

/**
 * The HeatPanel depicts the direction of the sun's rays onto the terrain and shows the surface
 * temperature of the fuels. The panel listens to the application time and the reticule coordinate.
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({
    "CTL_PreheatChartTitle=Solar Heating",})
public class HeatPanel extends javax.swing.JPanel {

    private JFreeChart solarChart;
    private final int AZIMUTH_SERIES = 0;
    private final int HOUR_SERIES = 1;

    /**
     * Creates new form PreheatPanel
     */
    public HeatPanel() {
        initComponents();
        createCharts();
    }

    /**
     * Updates the JFreeCharts.
     *
     * @param time UTC time.
     */
    public void updateCharts(ZonedDateTime time, Real azimuth, Real zenith, boolean isShaded) {

        try {
            // Update the Azimuth Plot
            CompassClockPlot compassPlot = (CompassClockPlot) solarChart.getPlot();
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

        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    /**
     * Creates the JFreeCharts.
     */
    private void createCharts() {
        //solarChart = ChartUtil.createCommonCompassChart(Bundle.CTL_PreheatChartTitle(), null, ChartUtil.WIND_NEEDLE, Color.ORANGE);
        CompassClockPlot plot = new CompassClockPlot();

        plot.setRosePaint(Color.orange);
        plot.setRoseHighlightPaint(Color.gray);
        plot.setRoseCenterPaint(Color.white);
        plot.setDrawBorder(false);
        
        // The first (default) dataset is the direction of Solar Radiation
        //ValueDataset dataset2 = new DefaultValueDataset(new Double(0.0));
        //plot.addDataset(dataset2, null);
        plot.setSeriesNeedle(AZIMUTH_SERIES, WIND_NEEDLE);
        plot.setSeriesPaint(AZIMUTH_SERIES, Color.red);        // arrow heads
        plot.setSeriesOutlinePaint(AZIMUTH_SERIES, Color.red); // arrow shafts and arrow head outline
        
        // The second  dataset is the Time / Clock
        ValueDataset dataset = new DefaultValueDataset(new Double(0.0));
        plot.addDataset(dataset, null);
        plot.setSeriesNeedle(HOUR_SERIES, CLOCK_HAND_NEEDLE);
        plot.setSeriesPaint(HOUR_SERIES, Color.black);        // arrow heads
        plot.setSeriesOutlinePaint(HOUR_SERIES, Color.black); // arrow shafts and arrow head outline


        // Create the chart
        solarChart = new JFreeChart(plot);
        
        // Set the chart title ...
        solarChart.setTitle(Bundle.CTL_PreheatChartTitle());
        // ... and subtitle(s)
        //chart.addSubtitle(new TextTitle(subTitle));
        
        solarPanel.add(new ChartPanel(solarChart));
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

        solarPanel = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(HeatPanel.class, "HeatPanel.border.title"))); // NOI18N

        solarPanel.setLayout(new javax.swing.BoxLayout(solarPanel, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(solarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(solarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel solarPanel;
    // End of variables declaration//GEN-END:variables

}
