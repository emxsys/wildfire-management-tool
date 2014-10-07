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
package com.emxsys.wmt.cps.views.haulchart;

import com.emxsys.visad.FireUnit;
import com.emxsys.visad.GeneralUnit;
import com.emxsys.wildfire.api.WildfirePreferences;
import com.emxsys.wildfire.behavior.SurfaceFire;
import com.emxsys.wildfire.behavior.SurfaceFuel;
import com.emxsys.wmt.cps.Model;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import static org.jfree.chart.ChartPanel.DEFAULT_BUFFER_USED;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYPolygonAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Drawable;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import visad.CommonUnit;
import visad.Unit;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class HaulChartPanel extends javax.swing.JPanel {

    // Chart colors for fire behavior adjectives
    static final int ALPHA = 200;
    public static final Color COLOR_LOW = new Color(128, 127, 255, ALPHA);         // blue
    public static final Color COLOR_MODERATE = new Color(127, 193, 151, ALPHA);    // green
    public static final Color COLOR_ACTIVE = new Color(255, 179, 130, ALPHA);      // tan
    public static final Color COLOR_VERY_ACTIVE = new Color(255, 128, 255, ALPHA); // magenta
    public static final Color COLOR_EXTREME = new Color(253, 128, 124, ALPHA);     // orange
    // Flame Length thresholds
    public static final double FL_THRESHOLD_LOW = 1D;
    public static final double FL_THRESHOLD_MODERATE = 3D;
    public static final double FL_THRESHOLD_ACTIVE = 7D;
    public static final double FL_THRESHOLD_VERY_ACTIVE = 15D;
    int xMin = 10;
    int yMin = 1;
    int xMax = 11000;
    int yMax = 1100;
    private JFreeChart chart;
    private XYSeriesCollection dataset;
    private XYSeries seriesMax;
    private XYSeries seriesFlank;
    private LogAxis xAxis;
    private LogAxis yAxis;
    private TextTitle subTitle;
    private Unit heatUOM;
    private Unit rosUOM;
    private Unit flnUOM;
    private Unit fliUOM;
    private final Unit heatUS = FireUnit.Btu_ft2;
    private final Unit rosUS = FireUnit.chain_hour;
    private final Unit flnUS = GeneralUnit.foot;
    private final Unit fliUS = FireUnit.Btu_ft_s;
    private String heatStr;
    private String rosStr;
    private String flnStr;
    private String fliStr;
    private SurfaceFuel fuel;
    private SurfaceFire fire;
    private final Object fuelLock = new Object();
    private final Object fireLock = new Object();

    private final class MyLogAxis extends LogAxis {

        MyLogAxis(String label) {
            super(label);
            setDefaultAutoRange(new Range(1, 10000.0));
            this.setStandardTickUnits(createTickUnits());
            this.setMinorTickMarksVisible(true);
        }

        /**
         * Returns a collection of tick units for log (base 10) values. Uses a given Locale to
         * create the DecimalFormats.
         *
         * @param locale the locale to use to represent Numbers.
         *
         * @return A collection of tick units for integer values.
         *
         * @since 1.0.7
         */
        public TickUnitSource createTickUnits() {
            TickUnits units = new TickUnits();
            DecimalFormat numberFormat = new DecimalFormat("0");
            units.add(new NumberTickUnit(1, numberFormat, 9));
            return units;
        }
    }

    /**
     * An implementation of the {@link Drawable} interface, to illustrate the use of the
     * {@link org.jfree.chart.annotations.XYDrawableAnnotation} class. Used by MarkerDemo1.java.
     */
    private class CircleDrawer implements Drawable {

        private final Paint outlinePaint;
        private final Stroke outlineStroke;
        private final Paint fillPaint;

        /**
         * Creates a new instance.
         *
         * @param outlinePaint the outline paint.
         * @param outlineStroke the outline stroke.
         * @param fillPaint the fill paint.
         */
        CircleDrawer(Paint outlinePaint,
                     Stroke outlineStroke,
                     Paint fillPaint) {
            this.outlinePaint = outlinePaint;
            this.outlineStroke = outlineStroke;
            this.fillPaint = fillPaint;
        }

        /**
         * Draws the circle.
         *
         * @param g2 the graphics device.
         * @param area the area in which to draw.
         */
        @Override
        public void draw(Graphics2D g2, Rectangle2D area) {
            Ellipse2D ellipse = new Ellipse2D.Double(area.getX(), area.getY(),
                    area.getWidth(), area.getHeight());
            if (this.fillPaint != null) {
                g2.setPaint(this.fillPaint);
                g2.fill(ellipse);
            }
            if (this.outlinePaint != null && this.outlineStroke != null) {
                g2.setPaint(this.outlinePaint);
                g2.setStroke(this.outlineStroke);
                g2.draw(ellipse);
            }

            g2.setPaint(Color.black);
            g2.setStroke(new BasicStroke(1.0f));
            Line2D line1 = new Line2D.Double(area.getCenterX(), area.getMinY(),
                    area.getCenterX(), area.getMaxY());
            Line2D line2 = new Line2D.Double(area.getMinX(), area.getCenterY(),
                    area.getMaxX(), area.getCenterY());
            g2.draw(line1);
            g2.draw(line2);
        }
    }

    /**
     * Creates new form HaulChart
     */
    public HaulChartPanel() {

        initUnitsOfMeasure();
        initComponents();
        initChart();
        // Now update the charts from values in the CPS data model
        Model.getInstance().addPropertyChangeListener(Model.PROP_FUELBED, (PropertyChangeEvent evt) -> {
            synchronized (fuelLock) {
                fuel = (SurfaceFuel) evt.getNewValue();
            }
            plotFireBehavior();
        });
        // Now update the charts from values in the CPS data model
        Model.getInstance().addPropertyChangeListener(Model.PROP_FIREBEHAVIOR, (PropertyChangeEvent evt) -> {
            synchronized (fireLock) {
                fire = (SurfaceFire) evt.getNewValue();
            }
            plotFireBehavior();
        });
        
        // React to Wildfire Options
        WildfirePreferences.addPreferenceChangeListener((e) -> {
            initUnitsOfMeasure();
        });

    }

    private void initUnitsOfMeasure() throws IllegalStateException {
        // Heat Release
        heatUOM = WildfirePreferences.getHeatReleaseUnit();
        heatStr = (heatUOM == FireUnit.kJ_m2 ? "kJ/m^2" : "btu/ft^2");

        // Rate of Spread
        rosUOM = WildfirePreferences.getRateOfSpreadUnit();
        switch (WildfirePreferences.getRateOfSpreadUom()) {
            case WildfirePreferences.UOM_CHAINS:
                rosStr = "ch/hr";
                break;
            case WildfirePreferences.UOM_KPH:
                rosStr = "kph";
                break;
            case WildfirePreferences.UOM_MPH:
                rosStr = "mph";
                break;
            case WildfirePreferences.UOM_MPS:
                rosStr = "mps";
                break;
            default:
                throw new IllegalStateException("unhandled ROS: " + WildfirePreferences.getRateOfSpreadUom());
        }

        // Flame Length
        flnUOM = WildfirePreferences.getFlameLengthUnit();
        flnStr = (flnUOM == CommonUnit.meter ? "m" : "ft");

        // Byram's fire line intensity
        fliUOM = WildfirePreferences.getByramsIntensityUnit();
        fliStr = (fliUOM == FireUnit.kW_m ? "kW/m" : "btu/ft/s");
    }

    /**
     * Plots the fire behavior at the specified x/y (heat/ros)
     *
     * @param heatReleasePerUnitArea x value in btus per unit area
     * @param rateOfSpread y value in chains per hour
     */
    private void plotFireBehavior() {
        // Resetting the chart so we don't display stale data if we don't have a valid fire.
        seriesMax.clear();
        seriesFlank.clear();
        if (fuel == null || fire == null) {
            chart.clearSubtitles();
            return;
        }

        // Updating the subtitle with the fuel model name
        subTitle.setText(fuel.getFuelModel().getModelName());
        if (chart.getSubtitleCount() == 0) {
            chart.addSubtitle(subTitle);
        }

        // Get values in units compatible with Chart        
        double heat = 0;
        double rosMax = 0;
        double rosFlank = 0;
        double fln = 0;
        double fli = 0;
        double heat_US = 0;
        double rosMax_US = 0;
        double btuNoWnd_US = 0;
        double rosFlank_US = 0;
        double fln_US = 0;
        double fli_US = 0;
        try {
            // Use US values for placement inside the chart
            heat_US = fuel.getHeatRelease().getValue(heatUS);
            rosMax_US = fire.getRateOfSpreadMax().getValue(rosUS);
            rosFlank_US = fire.getRateOfSpreadFlanking().getValue(rosUS);
            fln_US = fire.getFlameLength().getValue(flnUS);
            fli_US = fire.getFirelineIntensity().getValue(fliUS);

            // Get values used for labels
            heat = fuel.getHeatRelease().getValue(heatUOM);
            rosMax = fire.getRateOfSpreadMax().getValue(rosUOM);
            rosFlank = fire.getRateOfSpreadFlanking().getValue(rosUOM);
            fln = fire.getFlameLength().getValue(flnUOM);
            fli = fire.getFirelineIntensity().getValue(fliUOM);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
        // Add our two x/y points
        seriesMax.add(heat_US, rosMax_US);
        seriesFlank.add(heat_US, rosFlank_US);
        // Add marker lines to follow Rate of Spread
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.clearRangeMarkers();
        plot.clearDomainMarkers();

        // Add a labeled marker for "flanking" ROS
        Font font = new Font("SansSerif", Font.BOLD, 12);
        DecimalFormat dfRos = new DecimalFormat("#0.0 " + rosStr);
        Marker mrkRosFlank = new ValueMarker(rosFlank_US);
        mrkRosFlank.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        mrkRosFlank.setPaint(Color.blue);
        mrkRosFlank.setLabel(dfRos.format(rosFlank) + " ROS-Flank");
        mrkRosFlank.setLabelFont(font);
        mrkRosFlank.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
        mrkRosFlank.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        plot.addRangeMarker(mrkRosFlank);

        // Add a labeled marker for HPA
        DecimalFormat dfBtu = new DecimalFormat("#0 " + heatStr);
        Marker mrkBtu = new ValueMarker(heat_US);
        mrkBtu.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        mrkBtu.setPaint(Color.black);
        mrkBtu.setLabelFont(font);
        mrkBtu.setLabel(dfBtu.format(heat) + " HPA");
        if (heat_US < 1000) {
            mrkBtu.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
            mrkBtu.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
        } else {
            mrkBtu.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
            mrkBtu.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
        }
        plot.addDomainMarker(mrkBtu);

        // Add a labeled marker for max ROS
        Marker mrkRosMax = new ValueMarker(rosMax_US);
        mrkRosMax.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        mrkRosMax.setPaint(Color.black);
        mrkRosMax.setLabel(dfRos.format(rosMax) + " ROS-Max");
        mrkRosMax.setLabelFont(font);
        mrkRosMax.setLabelAnchor(rosMax_US > 700 ? RectangleAnchor.BOTTOM_LEFT : RectangleAnchor.TOP_LEFT);
        mrkRosMax.setLabelTextAnchor(rosMax_US > 700 ? TextAnchor.TOP_LEFT : TextAnchor.BOTTOM_LEFT);
        plot.addRangeMarker(mrkRosMax);

        // Label FlameLength with arrow and label...
        DecimalFormat dfFln = new DecimalFormat("#0.0 " + flnStr);
        CircleDrawer cd = new CircleDrawer(Color.red, new BasicStroke(1.0f), null);
        XYAnnotation annFln = new XYDrawableAnnotation(heat_US, rosMax_US, 11, 11, cd);
        plot.clearAnnotations();
        plot.addAnnotation(annFln);
        XYPointerAnnotation pointer = new XYPointerAnnotation(
                dfFln.format(fln) + " Flame",
                heat_US,
                rosMax_US, (rosMax_US > 550 ? 3.0 : 5.0) * Math.PI / 4.0);
        pointer.setBaseRadius(35.0);
        pointer.setTipRadius(10.0);
        pointer.setFont(new Font("SansSerif", Font.BOLD, 14));
        pointer.setOutlinePaint(Color.white);
        pointer.setBackgroundPaint(new Color(128, 128, 128, 128));
        pointer.setOutlineVisible(true);
        pointer.setPaint(Color.black);
        pointer.setTextAnchor(TextAnchor.HALF_ASCENT_RIGHT);
        plot.addAnnotation(pointer);

        // Adjust the range to grow if it exceeds the minimum
        // This will also reset the chart in case the user zoomed in/out,
        // which is helpfull because I was unable to reset it interactively.
        xAxis.setRange(xMin, Math.max(xMax, heat_US));
        yAxis.setRange(yMin, Math.max(yMax, rosMax_US));
    }

    private void initChart() {
        createChart();
        ChartPanel chartPanel = new ChartPanel(chart,
                300, //DEFAULT_WIDTH,
                400, //DEFAULT_HEIGHT,
                150, // DEFAULT_MINIMUM_DRAW_WIDTH, // Default = 300
                150, // DEFAULT_MINIMUM_DRAW_HEIGHT,
                DEFAULT_MAXIMUM_DRAW_WIDTH,
                DEFAULT_MAXIMUM_DRAW_HEIGHT,
                DEFAULT_BUFFER_USED,
                false, // properties
                true, // save
                true, // print
                false, // zoom
                true); // tooltips
        chartPanel.setMouseZoomable(false);

        add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * Creates a JFreeChart representing a Hauling Chart.
     *
     * @return A chart.
     */
    private void createChart() {

        String title = "Haul Chart";
        String sub = "fuel model goes here!";
        String xAxisTitle = "Heat per Unit Area (HPA) Btu/ft^2"; // + heatStr;
        String yAxisTitle = "Rate of Spread (ROS) ch/hr"; // + rosStr;

        xAxis = new MyLogAxis(xAxisTitle);
        yAxis = new MyLogAxis(yAxisTitle);
        // Autoranging on a LogAxis doesn't seem to work.
        // Also, range values of 0 do not work with LogAxis (locks up).
        // Instead, set the minimum values to a small number > 0.
        xAxis.setAutoRange(false);
        yAxis.setAutoRange(false);
        xAxis.setRange(xMin, xMax);
        yAxis.setRange(yMin, yMax);

        seriesMax = new XYSeries("Max Spread");
        seriesFlank = new XYSeries("No Wind/No Slope Spread");
        dataset = new XYSeriesCollection(seriesMax);
        dataset.addSeries(seriesFlank);

        chart = ChartFactory.createScatterPlot(title, xAxisTitle, yAxisTitle,
                dataset, PlotOrientation.VERTICAL, true, true, false);

        subTitle = new TextTitle();
        chart.addSubtitle(subTitle);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainPannable(false);
        plot.setRangePannable(false);
        plot.setDomainGridlineStroke(new BasicStroke(1.0f));
        plot.setRangeGridlineStroke(new BasicStroke(1.0f));
        plot.setDomainMinorGridlinesVisible(true);
        plot.setRangeMinorGridlinesVisible(true);
        plot.setDomainMinorGridlineStroke(new BasicStroke(0.1f));
        plot.setRangeMinorGridlineStroke(new BasicStroke(0.1f));
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        addBackgroundColors(renderer);
        addFlameLengthDivisions(renderer);
        addImageAnnotations(renderer);

        ChartUtilities.applyCurrentTheme(chart);
//        XYItemRenderer renderer = new XYLineAndShapeRenderer(false,true);
//        plot.setRenderer(renderer);
//
//        JFreeChart chart = new JFreeChart(
//                title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
//

    }

    private void addBackgroundColors(XYLineAndShapeRenderer renderer) {
        //
        double xEndLow = this.computeHeatAreaBtus(FL_THRESHOLD_LOW, yMin);
        double xEndModerate = this.computeHeatAreaBtus(FL_THRESHOLD_MODERATE, yMin);
        double xEndActive = this.computeHeatAreaBtus(FL_THRESHOLD_ACTIVE, yMin);
        double xEndVeryActive = this.computeHeatAreaBtus(FL_THRESHOLD_VERY_ACTIVE, yMin);
        //
        double yEndLow = this.computeRosChainsPerHour(FL_THRESHOLD_LOW, xMin);
        double yEndModerate = this.computeRosChainsPerHour(FL_THRESHOLD_MODERATE, xMin);
        double yEndActive = this.computeRosChainsPerHour(FL_THRESHOLD_ACTIVE, xMin);
        double yEndVeryActive = this.computeRosChainsPerHour(FL_THRESHOLD_VERY_ACTIVE, xMin);
        //
        XYPolygonAnnotation lowBkgnd = new XYPolygonAnnotation(
                new double[]{
                    xMin, yMin, xEndLow, yMin, xMin, yEndLow
                }, null, null, COLOR_LOW);
        XYPolygonAnnotation modBkgnd = new XYPolygonAnnotation(
                new double[]{
                    xEndLow, yMin, xMin, yEndLow, xMin, yEndModerate, xEndModerate, yMin
                }, null, null, COLOR_MODERATE);
        XYPolygonAnnotation activeBkgnd = new XYPolygonAnnotation(
                new double[]{
                    xEndModerate, yMin, xMin, yEndModerate, xMin, yEndActive, xEndActive, yMin
                }, null, null, COLOR_ACTIVE);
        XYPolygonAnnotation veryActiveBkgnd = new XYPolygonAnnotation(
                new double[]{
                    xEndActive, yMin, xMin, yEndActive, xMin, yEndVeryActive, xEndVeryActive, yMin
                }, null, null, COLOR_VERY_ACTIVE);
        XYPolygonAnnotation extremeBkgnd = new XYPolygonAnnotation(
                new double[]{
                    xEndVeryActive, yMin, xMin, yEndVeryActive, xMax * 10, yMax * 10
                }, null, null, COLOR_EXTREME);
        //
        lowBkgnd.setToolTipText("LOW");
        modBkgnd.setToolTipText("MODERATE");
        activeBkgnd.setToolTipText("ACTIVE");
        veryActiveBkgnd.setToolTipText("VERY ACTIVE");
        extremeBkgnd.setToolTipText("EXTREME");
        //
        renderer.addAnnotation(lowBkgnd, Layer.BACKGROUND);
        renderer.addAnnotation(modBkgnd, Layer.BACKGROUND);
        renderer.addAnnotation(activeBkgnd, Layer.BACKGROUND);
        renderer.addAnnotation(veryActiveBkgnd, Layer.BACKGROUND);
        renderer.addAnnotation(extremeBkgnd, Layer.BACKGROUND);
    }

    private void addFlameLenthLegend(XYLineAndShapeRenderer renderer) {
        final double flameLen = 25;
        Font font = new Font("SansSerif", Font.BOLD, 12);

        // compute the x,y location for the Flame Len label.
        double btu = this.computeHeatAreaBtus(flameLen, flameLen * 3);
        double ros = this.computeRosChainsPerHour(flameLen, btu);
        XYTextAnnotation annFln = new XYTextAnnotation("Flame Length, ft", btu, ros);
        annFln.setFont(font);
        annFln.setPaint(Color.darkGray);
        renderer.addAnnotation(annFln, Layer.BACKGROUND);

        // compute the x,y location for the FLI label
        btu = this.computeHeatAreaBtus(flameLen, flameLen * 15);
        ros = this.computeRosChainsPerHour(flameLen, btu);
        XYTextAnnotation annFli = new XYTextAnnotation("Fireline Intensity, Btu/ft/sec", btu, ros);
        annFli.setFont(font);
        annFli.setPaint(Color.darkGray);
        renderer.addAnnotation(annFli, Layer.BACKGROUND);
    }

    private void addImageAnnotations(XYLineAndShapeRenderer renderer) {
        // Setup background
        Image imgFireBehaviorLow = ImageUtilities.loadImage("com/emxsys/wmt/cps/images/fire-behavior-low.gif", true);
        Image imgFireBehaviorModerate = ImageUtilities.loadImage("com/emxsys/wmt/cps/images/fire-behavior-moderate.gif", true);
        Image imgFireBehaviorActive = ImageUtilities.loadImage("com/emxsys/wmt/cps/images/fire-behavior-active.gif", true);
        Image imgFireBehaviorVeryActive = ImageUtilities.loadImage("com/emxsys/wmt/cps/images/fire-behavior-very-active.gif", true);
        Image imgFireBehaviorExtreme = ImageUtilities.loadImage("com/emxsys/wmt/cps/images/fire-behavior-extreme.gif", true);
        renderer.addAnnotation(new XYImageAnnotation(30, 3, imgFireBehaviorLow, RectangleAnchor.CENTER), Layer.BACKGROUND);
        renderer.addAnnotation(new XYImageAnnotation(110, 11, imgFireBehaviorModerate, RectangleAnchor.CENTER), Layer.BACKGROUND);
        renderer.addAnnotation(new XYImageAnnotation(300, 30, imgFireBehaviorActive, RectangleAnchor.CENTER), Layer.BACKGROUND);
        renderer.addAnnotation(new XYImageAnnotation(600, 60, imgFireBehaviorVeryActive, RectangleAnchor.CENTER), Layer.BACKGROUND);
        renderer.addAnnotation(new XYImageAnnotation(1200, 120, imgFireBehaviorExtreme, RectangleAnchor.CENTER), Layer.BACKGROUND);
    }

    /**
     * Draw the standard flame length division lines
     */
    private void addFlameLengthDivisions(XYLineAndShapeRenderer renderer) {

        // draw flame length divisions
        int[] flameLens
                = {
                    1, 2, 4, 8, 11, 15, 20
                };    // [ft]
        for (int i : flameLens) {
            drawFlameLenDivision(renderer, i, false);
        }
        addFlameLenthLegend(renderer);
    }

    /**
     * Draw a specific flame length division line
     *
     * @param renderer
     * @param flameLen
     * @param drawLegendOnly - if true, draws the legend labels
     */
    private void drawFlameLenDivision(XYLineAndShapeRenderer renderer, double flameLen,
                                      boolean drawLegendOnly) {
        Font font = new Font("SansSerif", Font.BOLD, 12);

        // get BTU value at bottom of chart for give flame length and 1 ch/hr
        double btu = this.computeHeatAreaBtus(flameLen, yMin);
        // ... and get the  ROS value on the left edge of chart for 10 btu/ft^2
        double ros = this.computeRosChainsPerHour(flameLen, xMin);
        // add the line annonation
        renderer.addAnnotation(new XYLineAnnotation(
                btu, yMin,
                xMin, ros,
                new BasicStroke(1.5f), Color.gray),
                Layer.BACKGROUND);

        // Draw flame length labels in the lower diagonal half.
        // Compute new btu and ros to represent x,y values for label placement
        btu = this.computeHeatAreaBtus(flameLen, flameLen * 3);
        ros = this.computeRosChainsPerHour(flameLen, btu);
        XYTextAnnotation flLabel = new XYTextAnnotation(Integer.toString((int) flameLen) + "\'", btu, ros);
        flLabel.setFont(font);
        flLabel.setPaint(Color.darkGray);
        // add the flame len label annonation
        renderer.addAnnotation(flLabel,
                Layer.BACKGROUND);

        // Draw fireline intensity labels in the upper in the diagonal half
        // Compute new btu and ros to represent x,y values for label placement
        btu = this.computeHeatAreaBtus(flameLen, flameLen * 15);
        ros = this.computeRosChainsPerHour(flameLen, btu);
        int fli = (int) Math.round(computeFirelineIntensity(btu, ros));
        if (fli > 1000) {
            fli = (int) Math.round((double) fli / 1000) * 1000;
        } else if (fli > 100) {
            fli = (int) Math.round((double) fli / 100) * 100;
        }
        // add the fireline intensity label annonation
        XYTextAnnotation fliLabel = new XYTextAnnotation(Integer.toString(fli), btu, ros);
        fliLabel.setFont(font);
        fliLabel.setPaint(Color.darkGray);
        renderer.addAnnotation(fliLabel,
                Layer.BACKGROUND);

    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Compute fireline intensity (to be used as color key) from "Fire as a Physical Process" page
     * 47
     *
     * @param heatAreaBtus [btus / ft^2 ]
     * @param rosChainsPerHour [chains per hour]
     * @return fireline intensity [btu / ft / sec]
     */
    double computeFirelineIntensity(double heatAreaBtus, double rosChainsPerHour) {
        // convert chains per hour to feet per minute
        double rosFtPerMin = rosChainsPerHour * 1.100;
        double fli = (heatAreaBtus * rosFtPerMin) / 60d;
        return fli;
    }

    /**
     * Compute the flame length from fireline intensity from Andrews and Rothermel GTR-INT-131 Note:
     * algorythim in "Fire as a Physical Process" page 47 is wrong (typo)
     *
     * @param fliBtuPerFtPerSec - fireline intensity [btu / ft / sec]
     * @return flame length [ft]
     */
    double computeFlameLength(double fliBtuPerFtPerSec) {
        double fl = 0.45 * Math.pow(fliBtuPerFtPerSec, 0.46);
        return fl;
    }

    /**
     * Compute heat/area BTUs from chart flame len and ROS
     *
     * @return [btu / ft^2]
     */
    double computeHeatAreaBtus(double flameLen, double rosChainsPerHour) {
        double fliBtuFtSec = 5.67 * Math.pow(flameLen, 2.17);
        double rosFtPerMin = rosChainsPerHour * 1.100;
        double heatArea = (60 * fliBtuFtSec) / rosFtPerMin;
        return heatArea;
    }

    /**
     * Compute rate of spread from chart flame len and heat area
     *
     * @return [chains/hour]
     */
    double computeRosChainsPerHour(double flameLen, double heatAreaBtus) {
        double fliBtuFtSec = 5.67 * Math.pow(flameLen, 2.17);
        double rosFtPerMin = (60 * fliBtuFtSec) / heatAreaBtus;
        double rosChainsPerHour = rosFtPerMin * 0.9091;
        return rosChainsPerHour;
    }
}
