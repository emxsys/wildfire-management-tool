/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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

import com.emxsys.jfree.ChartUtil;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.wildfire.api.FireEnvironment;
import com.emxsys.wildfire.behavior.SurfaceFire;
import com.emxsys.wmt.cps.Model;
import com.emxsys.wmt.cps.options.CpsOptions;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CompassPlot;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultValueDataset;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import visad.CommonUnit;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public final class SpreadDirectionPanel extends javax.swing.JPanel {

    JFreeChart compassChart;
    JFreeChart dialChart;
    JFreeChart webChart;

    /**
     * Creates new form SpreadDirectionChart
     */
    public SpreadDirectionPanel() {
        initComponents();
        initChartPanels();

        // Add listener to update the charts from values in the CPS data model
        Model.getInstance().addPropertyChangeListener(Model.PROP_FIREBEHAVIOR, (PropertyChangeEvent evt) -> {
            plotFireBehavior((SurfaceFire) evt.getNewValue());
        });
    }

    /**
     * Plots the fire behavior at the specified x/y (heat/ros)
     *
     * @param heatReleasePerUnitArea x value in btus per unit area
     * @param rateOfSpread y value in chains per hour
     */
    private void plotFireBehavior(SurfaceFire fire) {
        if (fire == null) {
            return;
        }

        double dir = fire.getDirectionMaxSpread().getValue();
        double fln = 0;
        boolean useSI;
        String units = "";
        Preferences pref = NbPreferences.forModule(CpsOptions.class);
        String uom = pref.get(CpsOptions.UOM_KEY, CpsOptions.UOM_US);
        if (uom.matches(CpsOptions.UOM_US)) {
            useSI = false;
        } else {
            useSI = true;
        }

        try {
            units = (useSI ? " Meters" : " Feet");
            fln = fire.getFlameLength().getValue(useSI ? CommonUnit.meter : GeneralUnit.foot);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }

        // Plot spread direction
        CompassPlot compassPlot = (CompassPlot) compassChart.getPlot();
        DefaultValueDataset compassData = (DefaultValueDataset) compassPlot.getDatasets()[0];
        compassData.setValue(dir);

        // Determine cardinal point
        //String cardinalPt = headingToString(dir.getValue());
        //compassChartPanel.getChart().setTitle(cardinalPt + " Wind");
        // Plot rate of spread
        DialPlot dialPlot = (DialPlot) dialChart.getPlot();
        StandardDialScale dialScale = (StandardDialScale) dialPlot.getScale(0);
        DefaultValueDataset dialData = (DefaultValueDataset) dialPlot.getDataset();
        dialScale.setMinorTickCount(4);
        dialScale.setMajorTickIncrement(5);
        if (useSI) {
            if (fln < 5) {
                dialScale.setUpperBound(5);
            } else if (fln < 10) {
                dialScale.setUpperBound(10);
            } else if (fln < 25) {
                dialScale.setUpperBound(25);
            } else {
                dialScale.setUpperBound(100);
            }
        } else {
            if (fln < 15) {
                dialScale.setUpperBound(15);
            } else if (fln < 25) {
                dialScale.setUpperBound(25);
            } else if (fln < 50) {
                dialScale.setUpperBound(50);
            } else {
                dialScale.setUpperBound(200);
            }
        }
        dialData.setValue(fln);

        TextTitle subTitle = (TextTitle) dialChart.getSubtitle(0);
        //subTitle.setText(Math.round(fln) + units);
        DecimalFormat df = new DecimalFormat("#0.0 " + units);

        subTitle.setText(df.format(fln));

    }

    private void initChartPanels() {
        // Create the charts ...
        compassChart = ChartUtil.createCommonCompassChart(
                "Direction of Max Spread", null, ChartUtil.PLUM_NEEDLE, Color.RED);

        dialChart = ChartUtil.createCommonDialChart("Flame Length", "", 0, 50);

        leftPanel.add(ChartUtil.createCommonChartPanel(compassChart));
        rightPanel.add(ChartUtil.createCommonChartPanel(dialChart));

        //dialPanel.add(ChartUtil.createCommonChartPanel(dialChart));
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        leftPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        leftPanel.setLayout(new java.awt.BorderLayout());
        add(leftPanel);

        rightPanel.setLayout(new java.awt.BorderLayout());
        add(rightPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel rightPanel;
    // End of variables declaration//GEN-END:variables
}
