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
package com.emxsys.weather.panels;

import com.emxsys.visad.GeneralUnit;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.weather.panels.WindDirectionDial.WindDirChart;
import com.emxsys.weather.panels.WindSpeedDial.WindSpdChart;
import java.awt.Dimension;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import org.openide.util.Exceptions;
import visad.Real;
import visad.Unit;
import visad.VisADException;

/**
 * The WindForcePanel is a combined WindDirectionDial and WindSpeedDial used for editing wind speed
 * and direction.
 *
 * @author Bruce Schubert
 */
public class WindForcePanel extends javax.swing.JPanel {

    public final static String PROP_WIND_SPD = "weather.windpanel.windspd";
    public final static String PROP_WIND_DIR = "weather.windpanel.winddir";

    private final WindDirectionDial dirPanel = new WindDirectionDial();
    private final WindSpeedDial spdPanel = new WindSpeedDial();
    private final JSlider dirSlider = new DirectionSlider((WindDirChart) dirPanel.getChart());
    private final JSlider spdSlider = new SpeedSlider((WindSpdChart) spdPanel.getChart());
    private Real lastWindDir = new Real(WeatherType.WIND_DIR, 180); // also the default initial value

    /** Creates new form WindPanel */
    public WindForcePanel() {
        initComponents();
        jPanel1.add(dirPanel);
        jPanel1.add(dirSlider);
        jPanel2.add(spdPanel);
        jPanel2.add(spdSlider);
    }

    /**
     *
     * @return The wind speed in the configured UOM.
     */
    public Real getWindSpeed() {
        return spdPanel.getWindSpeed();
    }

    public void setWindSpeedUnit(Unit unit) {
        spdPanel.setWindSpeedUnit(unit);
    }

    public void setWindSpeed(Real speed) {
        try {
            if (speed == null || speed.isMissing() || speed.getValue() == 0) {
                spdSlider.setValue(0);
            } else {
                // Convert speed to units used by slider (MPH x 10)
                // Slider will generate event that updates the chart.
                spdSlider.setValue((int) (speed.getValue(GeneralUnit.mph) * 10));
            }
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Real getWindDirection() {
        Real windDir = dirPanel.getWindDirection();
        // the missing value is used to nullify the directional arrows when the speed is zero.
        if (windDir.isMissing()) {
            return lastWindDir;
        } else {
            return windDir;
        }
    }

    public void setWindDirection(Real direction) {
        // Slider will generate event that updates the chart.
        if (direction.isMissing()) {
            dirPanel.setWindDirection(null);
        } else {
            // Cache the wind dir so we can restore it when speed is > 0.
            lastWindDir = direction;
            dirSlider.setValue((int) direction.getValue());
        }
    }

    /**
     * The DirectionSlider updates the WindDirChart and fires a property change event.
     */
    private class DirectionSlider extends JSlider {

        DirectionSlider(WindDirChart chart) {
            super(0, 360, 180);
            setMaximumSize(new Dimension(200, 30));
            setPaintLabels(false);
            setPaintTicks(true);
            setPaintTrack(true);
            setMajorTickSpacing(30);
            setOrientation(SwingConstants.HORIZONTAL);
            addChangeListener((ChangeEvent e) -> {
                Real oldDir = getWindDirection();
                Real newDir = new Real(WeatherType.WIND_DIR, getValue());
                dirPanel.setWindDirection(newDir);
                if (this.isEnabled()) {
                    WindForcePanel.this.firePropertyChange(PROP_WIND_DIR, oldDir, newDir);
                }
            });
        }
    }

    /**
     * The SpeedSlider updates the WindSpdChart.
     */
    private class SpeedSlider extends JSlider {

        SpeedSlider(WindSpdChart chart) {
            super(0, 500, 0);    // SpeedSlider UOM is MPH x 10
            setMaximumSize(new Dimension(200, 30));
            setPaintLabels(false);
            setPaintTicks(true);
            setMajorTickSpacing(50);
            setOrientation(SwingConstants.HORIZONTAL);
            addChangeListener((ChangeEvent e) -> {
                int value = getValue();
                Real oldSpd = getWindSpeed();
                Real newSpd = new Real(WeatherType.WIND_SPEED_MPH, value / 10.);
                // Panel will draw in the speed in the configured UOM.
                spdPanel.setWindSpeed(newSpd);
                if (this.isEnabled()) {
                    WindForcePanel.this.firePropertyChange(PROP_WIND_SPD, oldSpd, newSpd);
                }
                // Hide the wind direction arrows when speed is zero
                if (value == 0) {
                    dirPanel.setWindDirection(null);
                } else if (oldSpd.getValue() == 0) {
                    dirPanel.setWindDirection(lastWindDir);
                }

            });
        }

    }

    /** This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(200, 130));

        jPanel1.setMaximumSize(new java.awt.Dimension(400, 400));
        jPanel1.setMinimumSize(new java.awt.Dimension(50, 50));
        jPanel1.setPreferredSize(new java.awt.Dimension(100, 130));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel2.setMaximumSize(new java.awt.Dimension(400, 400));
        jPanel2.setMinimumSize(new java.awt.Dimension(50, 50));
        jPanel2.setPreferredSize(new java.awt.Dimension(100, 130));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables

}
