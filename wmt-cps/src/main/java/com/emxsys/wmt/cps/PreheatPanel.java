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

import com.emxsys.wmt.cps.charts.ChartUtil;
import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.globe.util.Positions;
import com.emxsys.wmt.util.AngleUtil;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.awt.Color;
import java.time.ZonedDateTime;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CompassPlot;
import org.jfree.data.general.DefaultValueDataset;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import visad.CommonUnit;
import visad.Real;
import visad.VisADException;

/**
 * The PreheatPanel depicts the direction of the sun's rays onto the terrain and shows the surface
 * temperature of the fuels. The panel listens to the application time and the reticule coordinate.
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({
    "CTL_PreheatChartTitle=Solar Heating",})
public class PreheatPanel extends javax.swing.JPanel {

    private JFreeChart solarChart;

    /**
     * Creates new form PreheatPanel
     */
    public PreheatPanel() {
        initComponents();
        createCharts();
    }

    /**
     * Updates the JFreeCharts.
     *
     * @param time UTC time.
     */
    public void updateCharts(ZonedDateTime time, Real sunAzimuth) {
        
        try {
            double angle = sunAzimuth.getValue(CommonUnit.degree);
            CompassPlot compassPlot = (CompassPlot) solarChart.getPlot();
            DefaultValueDataset compassData = (DefaultValueDataset) compassPlot.getDatasets()[0];
            compassData.setValue(angle);
            
//        if (hour >= solar.getSunriseHour() && hour <= solar.getSunsetHour())
            {
            compassPlot.setSeriesPaint(0, Color.black);
            compassPlot.setSeriesOutlinePaint(0, Color.black);
            String title = String.format("%1$s Solar Heating", AngleUtil.degreesToCardinalPoint8(angle));
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
        solarChart = ChartUtil.createCommonCompassChart(Bundle.CTL_PreheatChartTitle(), null, ChartUtil.WIND_NEEDLE, Color.ORANGE);
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

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PreheatPanel.class, "PreheatPanel.border.title"))); // NOI18N

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
