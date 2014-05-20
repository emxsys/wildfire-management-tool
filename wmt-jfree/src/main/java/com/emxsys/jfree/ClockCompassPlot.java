/*
 * Copyright (c) 2014, Bruce Schubert 
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
package com.emxsys.jfree;

import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.needle.MeterNeedle;
import org.jfree.chart.plot.CompassPlot;

/**
 * A hybrid CompassPlot that includes an analog clock.
 *
 * @author Bruce Schubert
 */
public class ClockCompassPlot extends CompassPlot {

    /**
     * Clock Hands needle style
     */
    public static final int CLOCK_HAND_NEEDLE = 10;
    /**
     * Wind arrows pointing in direction of flow (opposite of direction)
     */
    public static final int WIND_NEEDLE = 7;

    /**
     * Sets the needle for a series. The needle type is one of the following: <ul> <li>0 =
     * {@link ArrowNeedle};</li>
     * <li>1 = {@link LineNeedle};</li> <li>2 = {@link LongNeedle};</li> <li>3 =
     * {@link PinNeedle};</li>
     * <li>4 = {@link PlumNeedle};</li> <li>5 = {@link PointerNeedle};</li> <li>6 =
     * {@link ShipNeedle};</li>
     * <li>7 = {@link WindNeedle};</li> <li>8 = {@link ArrowNeedle};</li> <li>9 =
     * {@link MiddlePinNeedle};</li>
     * </ul>
     *
     * @param index the series index.
     * @param type the needle type.
     *
     * @see #setSeriesNeedle(int)
     */
    @Override
    public void setSeriesNeedle(int index, int type) {
        if (type == CLOCK_HAND_NEEDLE) {
            setSeriesNeedle(index, new ClockHandsNeedle());
        } else {
            super.setSeriesNeedle(index, type);
        }
    }

    /**
     * A needle that is are drawn as clock hands (hour and minute)
     */
    public static class ClockHandsNeedle extends MeterNeedle implements Cloneable, Serializable {

        private enum ClockHand {

            Hour, Minute
        };

        @Override
        public void draw(Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {
            super.draw(g2, plotArea, rotate, angle); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void draw(Graphics2D g2, Rectangle2D plotArea, double angle) {
            super.draw(g2, plotArea, angle); //To change body of generated methods, choose Tools | Templates.
        }

        
        /**
         * Draws the clock hand needles.
         *
         * @param g2 the graphics device.
         * @param plotArea the plot area.
         * @param rotate the rotation point.
         * @param angle the angle.
         */
        @Override
        protected void drawNeedle(Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {
            drawHourHand(g2, plotArea, rotate, angle);
            drawMinuteHand(g2, plotArea, rotate, angle);
        }

        protected void drawHourHand(Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {
            drawHand(ClockHand.Hour, g2, plotArea, rotate, angle);
        }

        protected void drawMinuteHand(Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {
            angle = (angle % 30) * 12;// Hour angle converted to minutes
            drawHand(ClockHand.Minute, g2, plotArea, rotate, angle);
        }

        private void drawHand(ClockHand hand, Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {
            Area shape;
            GeneralPath pointer = new GeneralPath();

            int minY = (int) (plotArea.getMinY());
            //int maxX = (int) (plotArea.getMaxX());
            int maxY = (int) (plotArea.getMaxY());
            int midY = ((maxY - minY) / 2) + minY;

            int midX = (int) (plotArea.getMinX() + (plotArea.getWidth() / 2));
            //int midY = (int) (plotArea.getMinY() + (plotArea.getHeight() / 2));
            int lenX = (int) (plotArea.getWidth() / 10);
            if (lenX < 2) {
                lenX = 2;
            }

            pointer.moveTo(midX - lenX, midY - lenX);
            pointer.lineTo(midX + lenX, midY - lenX);
            if (hand == ClockHand.Hour) {
                pointer.lineTo(midX, minY + (midY - minY) * 0.33); // hour hand len is 2/3 the radius
            } else {
                pointer.lineTo(midX, minY); //  minute hand len is the radius
            }
            pointer.closePath();

            lenX = 4 * lenX;
            Ellipse2D circle = new Ellipse2D.Double(midX - lenX / 2,
                    midY - lenX / 2, lenX, lenX);

            shape = new Area(circle);
            shape.add(new Area(pointer));
            if ((rotate != null) && (angle != 0)) {
                /// we have rotation
                getTransform().setToRotation(angle, rotate.getX(), rotate.getY());
                shape.transform(getTransform());
            }
            defaultDisplay(g2, shape);

        }

        /**
         * Tests another object for equality with this object.
         *
         * @param object the object to test.
         *
         * @return A boolean.
         */
        @Override
        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }
            if (object == this) {
                return true;
            }
            return super.equals(object) && object instanceof ClockHandsNeedle;
        }

        /**
         * Returns a hash code for this instance.
         *
         * @return A hash code.
         */
        @Override
        public int hashCode() {
            return super.hashCode();
        }

        /**
         * Returns a clone of this needle.
         *
         * @return A clone.
         *
         * @throws CloneNotSupportedException if the <code>MiddlePinNeedle</code> cannot be cloned
         * (in theory, this should not happen).
         */
        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        /**
         * For serialization.
         */
        private static final long serialVersionUID = 6237073996403125310L;
    }

}
