/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.cps.views.forces;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.VectorRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.util.LineUtilities;
import org.jfree.data.xy.VectorXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: WindVectorRenderer.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public class WindVectorRenderer extends VectorRenderer {

    /** The length of the base. */
    private final double baseLength = 0.25;
    /** The length of the head. */
    private final double headLength = 0.25;
    private final double scale = 15.0;

    /**
     * This method was copied from the VectorRenderer class and modified to treat the dx and dy
     * values of vector series item as unit vector indicating the wind direction. The y value
     * indicates speed.
     *
     */
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea,
                         PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis,
                         XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {
        // BDS - Start modifications: Added line drawing
        drawPrimaryLine(state, g2, plot, dataset, pass, series,
                item, domainAxis, rangeAxis, dataArea);
        // BDS - End modifications

        double x = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);
        double dx = 0.0;
        double dy = 0.0;

        // BDS - Don't draw vectors for zero wind speeds
        if (y == 0) {
            return;
        }
        if (dataset instanceof VectorXYDataset) {
            dx = ((VectorXYDataset) dataset).getVectorXValue(series, item);
            dy = ((VectorXYDataset) dataset).getVectorYValue(series, item);
        }
        double xx0 = domainAxis.valueToJava2D(x, dataArea,
                plot.getDomainAxisEdge());
        double yy0 = rangeAxis.valueToJava2D(y, dataArea,
                plot.getRangeAxisEdge());

        // BDS - Start modifications: draw a scaled unit vector
        double xx1 = xx0 + (dx * scale);
        double yy1 = yy0 + (-dy * scale);
        // BDS - End modifications

        Line2D line;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation.equals(PlotOrientation.HORIZONTAL)) {
            line = new Line2D.Double(yy0, xx0, yy1, xx1);
        } else {
            line = new Line2D.Double(xx0, yy0, xx1, yy1);
        }
        g2.setPaint(getItemPaint(series, item));
        g2.setStroke(getItemStroke(series, item));
        g2.draw(line);

        // calculate the arrow head and draw it...
        double dxx = (xx1 - xx0);
        double dyy = (yy1 - yy0);
        double bx = xx0 + (1.0 - this.baseLength) * dxx;
        double by = yy0 + (1.0 - this.baseLength) * dyy;

        double cx = xx0 + (1.0 - this.headLength) * dxx;
        double cy = yy0 + (1.0 - this.headLength) * dyy;

        double angle = 0.0;
        if (dxx != 0.0) {
            angle = Math.PI / 2.0 - Math.atan(dyy / dxx);
        }
        double deltaX = 2.0 * Math.cos(angle);
        double deltaY = 2.0 * Math.sin(angle);

        double leftx = cx + deltaX;
        double lefty = cy - deltaY;
        double rightx = cx - deltaX;
        double righty = cy + deltaY;

        GeneralPath p = new GeneralPath();
        if (orientation == PlotOrientation.VERTICAL) {
            p.moveTo((float) xx1, (float) yy1);
            p.lineTo((float) rightx, (float) righty);
            p.lineTo((float) bx, (float) by);
            p.lineTo((float) leftx, (float) lefty);
        } else {  // orientation is HORIZONTAL
            p.moveTo((float) yy1, (float) xx1);
            p.lineTo((float) righty, (float) rightx);
            p.lineTo((float) by, (float) bx);
            p.lineTo((float) lefty, (float) leftx);
        }
        p.closePath();
        g2.draw(p);

        // setup for collecting optional entity info...
        EntityCollection entities;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
            if (entities != null) {
                addEntity(entities, line.getBounds(), dataset, series, item,
                        0.0, 0.0);
            }
        }
    }

    /**
     * This method was copied from the XYLineAndShapeRenderer. No modifications were done.
     * This method draws the lines connecting the items.
     *
     * @param g2 the graphics device.
     * @param state the renderer state.
     * @param dataArea the area within which the data is being drawn.
     * @param plot the plot (can be used to obtain standard color
     * information etc).
     * @param domainAxis the domain axis.
     * @param rangeAxis the range axis.
     * @param dataset the dataset.
     * @param pass the pass.
     * @param series the series index (zero-based).
     * @param item the item index (zero-based).
     */
    protected void drawPrimaryLine(XYItemRendererState state,
                                   Graphics2D g2,
                                   XYPlot plot,
                                   XYDataset dataset,
                                   int pass,
                                   int series,
                                   int item,
                                   ValueAxis domainAxis,
                                   ValueAxis rangeAxis,
                                   Rectangle2D dataArea) {
        if (item == 0) {
            return;
        }

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1) || Double.isNaN(x1)) {
            return;
        }

        double x0 = dataset.getXValue(series, item - 1);
        double y0 = dataset.getYValue(series, item - 1);
        if (Double.isNaN(y0) || Double.isNaN(x0)) {
            return;
        }

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
        double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);

        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        // only draw if we have good values
        if (Double.isNaN(transX0) || Double.isNaN(transY0)
                || Double.isNaN(transX1) || Double.isNaN(transY1)) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();
        ;
        if (orientation == PlotOrientation.HORIZONTAL) {
            state.workingLine.setLine(transY0, transX0, transY1, transX1);
        } else if (orientation == PlotOrientation.VERTICAL) {
            state.workingLine.setLine(transX0, transY0, transX1, transY1);
        }
        boolean visible = LineUtilities.clipLine(state.workingLine, dataArea);
        if (visible) {
            drawFirstPassShape(g2, pass, series, item, state.workingLine);
        }
    }

    /**
     * This method was copied from the XYLineAndShapeRenderer.
     *
     * @param g2 the graphics device.
     * @param pass the pass.
     * @param series the series index.
     * @param item the item index.
     * @param shape the shape.
     */
    protected void drawFirstPassShape(Graphics2D g2, int pass, int series,
                                      int item, Shape shape) {
        g2.setStroke(getItemStroke(series, item));
        g2.setPaint(getItemPaint(series, item));
        g2.draw(shape);
    }
}
