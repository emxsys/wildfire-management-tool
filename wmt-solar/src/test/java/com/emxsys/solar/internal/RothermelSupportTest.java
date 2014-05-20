/*
 * Copyright (c) 2014, bruce 
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
 *     - Neither the name of bruce,  nor the names of its 
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
package com.emxsys.solar.internal;

import com.emxsys.solar.internal.RothermelSupport;
import com.emxsys.gis.api.Latitude;
import static com.emxsys.solar.internal.RothermelSupport.*;
import java.awt.Color;
import static java.lang.Math.*;
import java.time.LocalDate;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import visad.CommonUnit;
import visad.DateTime;
import visad.Real;
import visad.VisADException;

/**
 *
 * @author bruce
 */
public class RothermelSupportTest {

    private static JFrame frame;
    private static boolean interactive = false;

    public RothermelSupportTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        frame = new JFrame("Test");
        if (interactive) {
            interactive = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                    frame, "Display interative results?", "Test Options",
                    JOptionPane.YES_NO_OPTION);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        frame.dispose();
    }

    @Before
    public void setUp() throws VisADException {
    }

    @After
    public void tearDown() {
    }

    ChartPanel createXYChart(String title,
                             String xAxisTitle, String yAxisTitle, XYSeriesCollection dataset) {

        NumberAxis xAxis = new NumberAxis(xAxisTitle);
        xAxis.setAutoRangeIncludesZero(false);

        NumberAxis yAxis = new NumberAxis(yAxisTitle);
        yAxis.setAutoRangeIncludesZero(false);

        XYSplineRenderer renderer = new XYSplineRenderer();

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));

        JFreeChart chart = new JFreeChart(title,
                JFreeChart.DEFAULT_TITLE_FONT, plot, true);

        return new ChartPanel(chart);
    }

    /**
     * Test of calcLocalHourAngle method, of class RothermelSupport.
     */
    @Test
    public void testCalcLocalHourAngle_double() {
        System.out.println("calcLocalHourAngle");

        if (!interactive) {
            // local noon
            double localTime = 12.0;
            double h = calcLocalHourAngle(localTime);
            double expResult = toRadians(90);
            assertEquals(expResult, h, 0.1);
            // morning
            localTime = 6.0;
            h = calcLocalHourAngle(localTime);
            expResult = 0;
            assertEquals(expResult, h, 0.1);
            // evening
            localTime = 18.0;
            h = calcLocalHourAngle(localTime);
            expResult = toRadians(180);
            assertEquals(expResult, h, 0.1);
            // midnight
            localTime = 24.0;
            h = calcLocalHourAngle(localTime);
            expResult = toRadians(270);
            assertEquals(expResult, h, 0.1);
        } else {
            // Graph the equation
            XYSeries series = new XYSeries("Series 1");
            for (int i = 0; i <= 24; i++) {
                double h = calcLocalHourAngle(i);   // local time
                series.add(i, toDegrees(h));
            }
            XYSeriesCollection dataset = new XYSeriesCollection(series);
            ChartPanel chart = createXYChart("calcLocalHourAngle",
                    "Hour", "Angle [degrees]", dataset);

            assertTrue(JOptionPane.showConfirmDialog(frame, chart, "Validate",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
        }
    }

    /**
     * Test of calcSolarDeclinationAngle method, of class RothermelSupport.
     */
    @Test
    public void testCalcSolarDeclinationAngle() throws VisADException {
        System.out.println("calcSolarDeclinationAngle");
        if (!interactive) {
            // Winter solstice
            int NJ = LocalDate.of(2009, 12, 21).getDayOfYear();
            double delta = calcSolarDeclinationAngle(NJ).getValue(CommonUnit.radian);
            double expResult = toRadians(-23.5);
            assertEquals(expResult, delta, 0.01);
            // Summer solstice
            NJ = LocalDate.of(2009, 6, 21).getDayOfYear();
            delta = calcSolarDeclinationAngle(NJ).getValue(CommonUnit.radian);
            expResult = toRadians(23.5);
            assertEquals(expResult, delta, 0.01);
            // spring equinox
            NJ = LocalDate.of(2009, 3, 21).getDayOfYear();
            delta = calcSolarDeclinationAngle(NJ).getValue(CommonUnit.radian);
            expResult = 0.0;
            assertEquals(expResult, delta, 0.5);
            // fall equinox
            NJ = LocalDate.of(2009, 9, 21).getDayOfYear();
            delta = calcSolarDeclinationAngle(NJ).getValue(CommonUnit.radian);
            expResult = 0.0;
            assertEquals(expResult, delta, 0.5);
        } else {
            // Graph the equation
            XYSeries series = new XYSeries("Series 1");
            for (int day = 0; day < 366; day++) {
                double angle = calcSolarDeclinationAngle(day).getValue();
                series.add(day, angle);
            }
            XYSeriesCollection dataset = new XYSeriesCollection(series);
            ChartPanel chart = createXYChart("calcSolarDeclinationAngle",
                    "Day", "Angle [degrees]", dataset);
            assertTrue(JOptionPane.showConfirmDialog(frame, chart, "Validate",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
        }
    }

    /**
     * Test of calcSolarAltitudeAngle method, of class RothermelSupport.
     */
    @Test
    public void testCalcSolarAltitudeAngle() throws VisADException {
        System.out.println("calcSolarAltitudeAngle");
        double delta = calcSolarDeclinationAngle(LocalDate.of(2009, 3, 22).getDayOfYear()).getValue(CommonUnit.radian);
        if (!interactive) {
            double phi = toRadians(0);              // equator
            double h = calcLocalHourAngle(6.0);     // norning - local time
            double A = calcSolarAltitudeAngle(h, phi, delta);
            double expResult = toRadians(0);
            assertEquals("@0600", expResult, A, 0.01);

            h = calcLocalHourAngle(12.0);   // noon - local time
            A = calcSolarAltitudeAngle(h, phi, delta);
            expResult = toRadians(90);
            assertEquals("@1200", expResult, A, 0.01);

            h = calcLocalHourAngle(18.0);   // evening - local time
            A = calcSolarAltitudeAngle(h, phi, delta);
            expResult = toRadians(0);
            assertEquals("@1800", expResult, A, 0.01);

        } else {
            // create a dataset...
            XYSeriesCollection dataset = new XYSeriesCollection();
            double[] latitudes = {-23.5, 0, 23.5, 35, 60};
            for (double lat : latitudes) {
                double phi = toRadians(lat);
                XYSeries series = new XYSeries("Series " + lat);
                for (int i = 0; i <= 24; i++) {
                    double h = calcLocalHourAngle(i);   // local time
                    double A = calcSolarAltitudeAngle(h, phi, delta);
                    series.add(i, toDegrees(A));
                }
                dataset.addSeries(series);
            }
            ChartPanel chart = createXYChart("calcSolarAltitudeAngle",
                    "hour", "angle [degrees]", dataset);
            assertTrue(JOptionPane.showConfirmDialog(frame, chart, "Validate",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
        }

    }

    /**
     * Test of calcSolarAzimuthAngle method, of class RothermelSupport.
     */
    @Test
    public void testCalcSolarAzimuthAngle() throws VisADException {
        System.out.println("calcSolarAzimuthAngle");
        int NJ = LocalDate.of(2009, 9, 21).getDayOfYear();    // vernal equinox
        //int NJ = LocalDate.of(2009, 6, 21).getDayOfYear();      // summer
        double delta = calcSolarDeclinationAngle(NJ).getValue(CommonUnit.radian);
        if (!interactive) {
            double phi = toRadians(0);              // equator
            double h = calcLocalHourAngle(6.0);     // morning - local time
            double A = calcSolarAltitudeAngle(h, phi, delta);
            double Z = calcSolarAzimuthAngle(h, phi, delta, A);
            double expResult = toRadians(0);
            assertEquals("@0600", expResult, Z, 0.01);

            h = calcLocalHourAngle(12.0);           // noon - local time
            A = calcSolarAltitudeAngle(h, phi, delta);
            Z = calcSolarAzimuthAngle(h, phi, delta, A);
            expResult = toRadians(90);
            assertEquals("@1200", expResult, Z, 0.01);

            h = calcLocalHourAngle(18.0);           // evening - local time
            A = calcSolarAltitudeAngle(h, phi, delta);
            Z = calcSolarAzimuthAngle(h, phi, delta, A);
            expResult = toRadians(180);
            assertEquals("@1800", expResult, Z, 0.01);
        } else {
            double phi = toRadians(-34.2);               // oxnard
            // graph the equation
            XYSeries seriesTan = new XYSeries("tanZ");
            XYSeries seriesCos = new XYSeries("cosZ");
            XYSeries seriesh = new XYSeries("hour");
            XYSeries seriesA = new XYSeries("altitude");
            XYSeries seriesZ = new XYSeries("Azimuth");
            XYSeries seriesR = new XYSeries("Ratio");
            for (int i = 0; i < 24; i++) {
                double h = calcLocalHourAngle(i);       // local time
                double A = calcSolarAltitudeAngle(h, phi, delta);
                double Z = calcSolarAzimuthAngle(h, phi, delta, A);

                double tanZ = ((sin(h) * cos(delta) * sin(phi)) - (sin(delta) * cos(phi)))
                        / (cos(h) * cos(delta));
                double cosZ = cos(h) * cos(delta) / cos(A);

                if (i > 0 && i != 12) {
                    seriesTan.add(i, tanZ);
                    seriesCos.add(i, cosZ);
                }
                seriesh.add(i, toDegrees(h));
                seriesA.add(i, toDegrees(A));
                seriesZ.add(i, toDegrees(Z));
                seriesR.add(i, tanZ / cosZ);
            }
            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(seriesZ);
            //dataset.addSeries(seriesCos);
            //dataset.addSeries(seriesTan);
            dataset.addSeries(seriesA);
            ChartPanel chart = createXYChart("calcSolarAzimuthAngle @ " + toDegrees(phi),
                    "Local Time [hour]", "Angle [degrees]", dataset);
            assertTrue(JOptionPane.showConfirmDialog(frame, chart, "Validate",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
        }

    }

    /**
     * Test of calcSunriseSunset method, of class RothermelSupport.
     */
    @Ignore
    @Test
    public void testCalcSunriseSunset() {
        System.out.println("calcSunriseSunset");
        Real latitude = null;
        Real declination = null;
        Date date = null;
        DateTime[] expResult = null;
        DateTime[] result = RothermelSupport.calcSunriseSunset(latitude, declination, date);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calcSunriseSolarHour method, of class RothermelSupport.
     */
    @Test
    public void testCalcSunriseSolarHour() {
        System.out.println("calcSunriseSolarHour");

        if (!interactive) {
            Real latitude = Latitude.fromDegrees(0);
            Real declination = calcSolarDeclinationAngle(LocalDate.of(2009, 3, 21).getDayOfYear()); // spring
            double t_r = calcSunriseSolarHour(latitude, declination);   // sunrise
            double expResult = 5 + (58.0 / 60);
            assertEquals("vernal equinox", expResult, t_r, 0.05);
        } else {

            // Graph the equation
            XYSeries series = new XYSeries("Series 1");

            for (int day = 1; day <= 366; day++) {
                Real latitude = Latitude.fromDegrees(34.2);
                Real declination = calcSolarDeclinationAngle(day);
                double h = calcSunriseSolarHour(latitude, declination);   // sunrise
                series.add(day, h);
            }
            XYSeriesCollection dataset = new XYSeriesCollection(series);
            ChartPanel chart = createXYChart("calcSunriseSolarHour",
                    "Day", "Hour", dataset);

            assertTrue(JOptionPane.showConfirmDialog(frame, chart, "Validate",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
        }
    }

    /**
     * Test of calcSunsetSolarHour method, of class RothermelSupport.
     */
    @Test
    public void testCalcSunsetSolarHour() {
        System.out.println("calcSunsetSolarHour");
        Real latitude = Latitude.fromDegrees(0);
        Real declination = calcSolarDeclinationAngle(LocalDate.of(2009, 3, 21).getDayOfYear()); // spring
        double t_s = calcSunsetSolarHour(latitude, declination);   // sunrise
        double expResult = 18 + (2.0 / 60);
        assertEquals("spring equinox", expResult, t_s, 0.05);
    }

    /**
     * Test of calcLocalHourAngle method, of class RothermelSupport.
     */
    @Ignore
    @Test
    public void testCalcLocalHourAngle_Real() {
        System.out.println("calcLocalHourAngle");
        Real time = null;
        Real expResult = null;
        Real result = RothermelSupport.calcLocalHourAngle(time);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
