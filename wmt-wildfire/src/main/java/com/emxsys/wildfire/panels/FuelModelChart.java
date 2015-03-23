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
package com.emxsys.wildfire.panels;

import com.emxsys.visad.FireUnit;
import com.emxsys.wildfire.api.FuelModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openide.util.Exceptions;
import visad.Unit;
import visad.VisADException;

/**
 * A BarChart for Fuel Model fuel loading
 * @author Bruce Schubert
 */
public class FuelModelChart extends javax.swing.JPanel {

    ChartPanel chartPanel;
    JFreeChart chart;

    /** Creates new form FuelModelChart */
    public FuelModelChart() {
        initComponents();
        chart = createFuelModelChart();
        chartPanel = new ChartPanel(chart);
        add(chartPanel);
    }

    public void updateChart(FuelModel fuelModel) {
        // Update the display chart
        chart.setTitle(fuelModel.getModelName());
        CategoryDataset fuelLoading = createFuelLoadingDataset(fuelModel);
        //CategoryDataset savRatio = createSAVRatioDataset(model);
        CategoryPlot plot = (CategoryPlot) chartPanel.getChart().getPlot();
        plot.setDataset(0, fuelLoading);
        //plot.setDataset(1, savRatio);
    }

    /**
     * Creates a chart.
     *
     * @param dataset the dataset.
     * @return The chart.
     */
    private static JFreeChart createFuelModelChart() {
        // Create the chart with the initial dataset
        JFreeChart chart = ChartFactory.createBarChart(
                null, //fuelModel.getModelName(),
                null, //"Fuel Type",
                "Fuel Load",
                createFuelLoadingDataset(null),
                PlotOrientation.VERTICAL,
                false, // include legend
                true, // tool tips?
                false); // URL generator?

        // get a reference to the plot for further customization
//        CategoryPlot plot = (CategoryPlot) chart.getPlot();
//        ValueAxis axis2 = new NumberAxis("SAV Ratio");
//        plot.setRangeAxis(1, axis2);
//        plot.setDataset(1, createSAVRatioDataset(null));
//        plot.mapDatasetToRangeAxis(1, 1);
//
//        CategoryItemRenderer renderer2 = new LevelRenderer();
//        renderer2.setSeriesPaint(0, Color.blue);
//        renderer2.setSeriesStroke(0, new BasicStroke(3.0f));
//        plot.setRenderer(1, renderer2);
//        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        return chart;
    }

    private static CategoryDataset createFuelLoadingDataset(FuelModel fuelModel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            // row keys...
            String series1 = "Fuel Load";
            // column keys...
            String category1 = "1-hr";
            String category2 = "10-hr";
            String category3 = "100-hr";
            String category4 = "Herb.";
            String category5 = "Woody";
            // create the dataset...
            Unit uom = FireUnit.tons_acre;
            double d1 = fuelModel == null ? 0 : fuelModel.getDead1HrFuelLoad().getValue(uom);
            double d10 = fuelModel == null ? 0 : fuelModel.getDead10HrFuelLoad().getValue(uom);
            double d100 = fuelModel == null ? 0 : fuelModel.getDead100HrFuelLoad().getValue(uom);
            double lh = fuelModel == null ? 0 : fuelModel.getLiveHerbFuelLoad().getValue(uom);
            double lw = fuelModel == null ? 0 : fuelModel.getLiveWoodyFuelLoad().getValue(uom);
            dataset.addValue(d1, series1, category1);
            dataset.addValue(d10, series1, category2);
            dataset.addValue(d100, series1, category3);
            dataset.addValue(lh, series1, category4);
            dataset.addValue(lw, series1, category5);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return dataset;
    }

    private static CategoryDataset createSAVRatioDataset(FuelModel fuelModel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            // row keys...
            String series2 = "SAV Ratio";
            // column keys...
            String category1 = "1-hr";
            String category2 = "10-hr";
            String category3 = "100-hr";
            String category4 = "Herb.";
            String category5 = "Woody";
            // create the dataset...
            dataset.addValue(fuelModel.getDead100HrSAVRatio().getValue(FireUnit.ft2_ft3), series2, category1);
            dataset.addValue(0, series2, category2);
            dataset.addValue(0, series2, category3);
            dataset.addValue(fuelModel.getLiveHerbSAVRatio().getValue(FireUnit.ft2_ft3), series2, category4);
            dataset.addValue(fuelModel.getLiveWoodySAVRatio().getValue(FireUnit.ft2_ft3), series2, category5);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        return dataset;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
