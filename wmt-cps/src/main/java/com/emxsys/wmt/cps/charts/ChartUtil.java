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
package com.emxsys.wmt.cps.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CompassPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.dial.ArcDialFrame;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.renderer.xy.VectorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.data.xy.VectorXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.ui.TextAnchor;


/**
 * Utility class for assiting in setting up JFreeChart instances
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: ChartUtil.java 315 2012-10-20 13:42:54Z bdschubert $
 */
public class ChartUtil
{

    private ChartUtil()
    {
    }
    /**
     * Long line without arrowhead
     */
    public static final int LINE_NEEDLE = 1;
    /**
     * Long N/S compass pointer
     */
    public static final int LONG_NEEDLE = 2;
    /**
     * Short line without arrowhead
     */
    public static final int PIN_NEEDLE = 3;
    /**
     * Long triangular needle
     */
    public static final int PLUM_NEEDLE = 4;
    /**
     * Standard N/S compass pointer
     */
    public static final int POINTER_NEEDLE = 5;
    public static final int SHIP_NEEDLE = 6;
    /**
     * Wind arrows pointing in direction of flow (opposite of direction)
     */
    public static final int WIND_NEEDLE = 7;
    /**
     * Single arrow
     */
    public static final int ARROW_NEEDLE = 8;
    public static final int MIDDLE_PIN_NEEDLE = 9;


    public static JFreeChart createCommonCompassChart(String title, String subTitle)
    {
        return createCommonCompassChart(title, subTitle, WIND_NEEDLE, Color.red);
    }


    @SuppressWarnings("unchecked")
    public static JFreeChart createCommonCompassChart(String title, String subTitle, int needleType,
        Color roseColor)
    {

        ValueDataset dataset = new DefaultValueDataset(new Double(0.0));
        CompassPlot plot = new CompassPlot(dataset);
        plot.setSeriesNeedle(needleType);

        plot.setSeriesPaint(0, Color.black);        // arrow heads
        plot.setSeriesOutlinePaint(0, Color.black); // arrow shafts and arrow head outline
        plot.setRosePaint(roseColor);
        plot.setRoseHighlightPaint(Color.gray);
        plot.setRoseCenterPaint(Color.white);
        plot.setDrawBorder(false);
        // Create the chart
        JFreeChart chart = new JFreeChart(plot);
        // Set the chart title ...
        chart.setTitle(title);
        // ... and subtitle(s)
        if (subTitle != null)
        {
            chart.addSubtitle(new TextTitle(subTitle));
        }
        //ChartUtilities.applyCurrentTheme(chart);
        return chart;
    }


    @SuppressWarnings("unchecked")
    public static JFreeChart createCommonDialChart(String title, String subTitle)
    {

        ValueDataset dataset = new DefaultValueDataset(new Double(0.0));
        // get data for diagrams
        DialPlot plot = new DialPlot();
        plot.setView(0.78, 0.37, 0.22, 0.26);
        plot.setDataset(dataset);

//        ArcDialFrame dialFrame = new ArcDialFrame(0.0, 45.0);
        ArcDialFrame dialFrame = new ArcDialFrame(-10.0, 20.0);
        dialFrame.setInnerRadius(0.70);
        dialFrame.setOuterRadius(0.90);
        dialFrame.setForegroundPaint(Color.darkGray);
        dialFrame.setStroke(new BasicStroke(3.0f));
        plot.setDialFrame(dialFrame);

        GradientPaint gp = new GradientPaint(new Point(),
            new Color(255, 255, 255), new Point(),
            new Color(240, 240, 240));
        DialBackground sdb = new DialBackground(gp);
        sdb.setGradientPaintTransformer(new StandardGradientPaintTransformer(
            GradientPaintTransformType.VERTICAL));
        plot.addLayer(sdb);

//        StandardDialScale scale = new StandardDialScale(0, 100, 2, 45,
        StandardDialScale scale = new StandardDialScale(0, 50, -8, 16.0, 10.0, 4);
        scale.setTickRadius(0.82);
        scale.setTickLabelOffset(-0.04);
        scale.setMajorTickIncrement(25.0);
        scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));
        plot.addScale(0, scale);

        DialPointer needle = new DialPointer.Pin();
        needle.setRadius(0.84);
        plot.addLayer(needle);
        // Create the chart
        JFreeChart chart = new JFreeChart(plot);
        // Set the chart title ...
        chart.setTitle(title);
        // ... and subtitle(s)
        if (subTitle != null)
        {
            List subTitles = new ArrayList();
            subTitles.add(new TextTitle(subTitle));
            chart.setSubtitles(subTitles);
        }
        return chart;
    }


    @SuppressWarnings("unchecked")
    public static JFreeChart createCommonWebChart(String title, String subTitle, int numCategories)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        // Offset the first category to the NE instead of at directly at 3:00
        // 90 = 12:00, 0 = 3:00
        plot.setStartAngle(90 - (360 / numCategories) / 2);
        plot.setInteriorGap(.20);
        plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());

        JFreeChart chart = new JFreeChart(title, TextTitle.DEFAULT_FONT, plot, false);
        LegendTitle legend = new LegendTitle(plot);
        legend.setPosition(RectangleEdge.BOTTOM);
        chart.addSubtitle(legend);
        if (subTitle != null)
        {
            chart.addSubtitle(new TextTitle(subTitle));
        }
        return chart;
    }


    public static JFreeChart createWindChart(
        VectorXYDataset dataset, Date sunset[], Date sunrise[],
        String title, String timeAxisLabel, String valueAxisLabel)
    {

        ValueAxis timeAxis = new DateAxis(timeAxisLabel);
        timeAxis.setLowerMargin(0.02);  // reduce the default margins
        timeAxis.setUpperMargin(0.02);

        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        valueAxis.setAutoRangeIncludesZero(false);  // override default

        XYToolTipGenerator toolTipGenerator =
            StandardXYToolTipGenerator.getTimeSeriesInstance();

        VectorRenderer renderer = new VectorRenderer();
        renderer.setBaseToolTipGenerator(toolTipGenerator);

        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, null);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        // Create date/time intervals for deliniating between day and night
        plot.addDomainMarker(createIntervalMarker(sunset[0], sunrise[0], "Last Night",
            new Color(0, 0, 255, 25)), Layer.BACKGROUND);
        plot.addDomainMarker(createIntervalMarker(sunrise[0], sunset[1], "Today",
            new Color(255, 255, 255, 25)), Layer.BACKGROUND);
        plot.addDomainMarker(createIntervalMarker(sunset[1], sunrise[1], "Tonight",
            new Color(0, 0, 255, 25)), Layer.BACKGROUND);

        plot.setRenderer(renderer);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);

        return chart;

    }


    public static JFreeChart createDiurnalChart(
        XYDataset dataset, String title, String timeAxisLabel, String valueAxisLabel)
    {

        ValueAxis timeAxis = new DateAxis(timeAxisLabel);
        timeAxis.setLowerMargin(0.02);  // reduce the default margins
        timeAxis.setUpperMargin(0.02);

        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        valueAxis.setAutoRangeIncludesZero(false);  // override default

        XYToolTipGenerator toolTipGenerator =
            StandardXYToolTipGenerator.getTimeSeriesInstance();
        XYLineAndShapeRenderer renderer = new CpsXYLineAndShapeRenderer(true, true);
        renderer.setBaseToolTipGenerator(toolTipGenerator);
        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);

        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, null);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        plot.setRenderer(renderer);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);

        return chart;

    }


    /**
     * Create a marker band used to depict daytime or nighttime.
     */
    public static Marker createIntervalMarker(Date begin, Date end, String label, Color color)
    {
        IntervalMarker marker = new IntervalMarker(
            begin.getTime(),
            end.getTime(),
            color, new BasicStroke(1.0f), null, null, 1.0f);
        marker.setLabel(label);
        marker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
        marker.setLabelFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 9));
        marker.setLabelTextAnchor(TextAnchor.BASELINE_LEFT);
        return marker;
    }


    public static ChartPanel createCommonChartPanel(JFreeChart chart)
    {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 250));
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        Border border = BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(4, 4, 4, 4),
            BorderFactory.createEtchedBorder());
        chartPanel.setBorder(border);

        return chartPanel;
    }
}
