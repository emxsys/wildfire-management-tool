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
 *     - Neither the name of Bruce Schubert,  nor the names of its 
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
package com.emxsys.weather.panels;

import com.emxsys.time.spi.TimeProviderFactory;
import com.emxsys.visad.Times;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.ZonedDateTime;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import static org.jfree.chart.ChartPanel.DEFAULT_BUFFER_USED;
import static org.jfree.chart.ChartPanel.DEFAULT_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH;
import static org.jfree.chart.ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT;
import static org.jfree.chart.ChartPanel.DEFAULT_WIDTH;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.openide.util.Exceptions;
import visad.DateTime;
import visad.VisADException;

/**
 * Base class for weather chart panels. Provides a ChartMouseListener that sets the application date
 * when the chart is double clicked
 * @author Bruce Schubert
 */
public abstract class AbstractWeatherChartPanel extends ChartPanel {

    AbstractWeatherChartPanel(JFreeChart chart) {
        super(chart,
                DEFAULT_WIDTH,
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
                true); // tooltips

        // Add a listener that sets the application time to the mouse-click position
        addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                if (event.getTrigger().getClickCount() == 2) {
                    Point2D p = translateScreenToJava2D(event.getTrigger().getPoint());
                    Rectangle2D plotArea = getScreenDataArea();
                    JFreeChart chart = event.getChart();
                    XYPlot plot = chart.getXYPlot();
                    double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
                    double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());

                    // Set the application time to the chart domain click
                    try {
                        DateTime datetime = new DateTime(chartX);
                        TimeProviderFactory.getInstance().setTime(Times.toZonedDateTime(datetime));
                    } catch (VisADException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent ignored) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });

        // Setting the preferred size allows us to control the initial size
        // of the panel when it's dragged-n-dropped in the NetBeans GUI editor.
        setPreferredSize(new java.awt.Dimension(350, 150));
    }

    /**
     * Convenience method.
     * @param title New title.
     */
    public void setTitle(String title) {
        getChart().setTitle(title);
    }

    /**
     * Convenience method.
     * @param subtitle Additional subtitle.
     */
    public void addSubTitle(String subtitle) {
        getChart().addSubtitle(new TextTitle(subtitle));
    }

    /**
     * Convenience method.
     */
    public void clearSubTitles() {
        getChart().clearSubtitles();
    }

    public void refresh() {
        getChart().setNotify(true);
    }

    abstract public void setDateTime(ZonedDateTime datetime);

}
