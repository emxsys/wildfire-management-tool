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
package com.emxsys.weather.wizards;

import com.emxsys.weather.api.WeatherPreferences;
import static com.emxsys.weather.api.WeatherPreferences.*;
import com.emxsys.weather.panels.AirTemperatureGauge;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import org.openide.util.NbBundle.Messages;
import visad.Real;
import visad.Unit;

@Messages({"CTL_DiurnalTemperatures=Air Temperatures",
           "CTL_DiurnalTemperatureSunrise=Sunrise",
           "CTL_DiurnalTemperatureNoon=Noon",
           "CTL_DiurnalTemperature1400=Afternoon",
           "CTL_DiurnalTemperatureSunset=Sunset",})
public final class DiurnalWeatherPanelTemps extends JPanel {

    private final AirTemperatureGauge panelSunrise;
    private final AirTemperatureGauge panelNoon;
    private final AirTemperatureGauge panel1400;
    private final AirTemperatureGauge panelSunset;
    private final PropertyChangeListener listener;

    public DiurnalWeatherPanelTemps() {
        initComponents();

        // Initialize temperature gauge controls
        panelSunrise = new AirTemperatureGauge(Bundle.CTL_DiurnalTemperatureSunrise(),
                WeatherPreferences.getAirTempUnit(),
                WeatherPreferences.getAirTempValue(PREF_AIR_TEMP_SUNRISE));
        panelNoon = new AirTemperatureGauge(Bundle.CTL_DiurnalTemperatureNoon(),
                WeatherPreferences.getAirTempUnit(),
                WeatherPreferences.getAirTempValue(PREF_AIR_TEMP_1200));
        panel1400 = new AirTemperatureGauge(Bundle.CTL_DiurnalTemperature1400(),
                WeatherPreferences.getAirTempUnit(),
                WeatherPreferences.getAirTempValue(PREF_AIR_TEMP_1400));
        panelSunset = new AirTemperatureGauge(Bundle.CTL_DiurnalTemperatureSunset(),
                WeatherPreferences.getAirTempUnit(),
                WeatherPreferences.getAirTempValue(PREF_AIR_TEMP_SUNSET));

        jPanel1.add(panelSunrise);
        jPanel2.add(panelNoon);
        jPanel3.add(panel1400);
        jPanel4.add(panelSunset);

        // Pass on gauge notification to parent
        listener = (PropertyChangeEvent evt) -> {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        };
        panelSunrise.addPropertyChangeListener(listener);
        panelNoon.addPropertyChangeListener(listener);
        panel1400.addPropertyChangeListener(listener);
        panelSunset.addPropertyChangeListener(listener);

    }

    void setUom(Unit uom) {
        panelSunrise.updateUom(uom);
        panelNoon.updateUom(uom);
        panel1400.updateUom(uom);
        panelSunset.updateUom(uom);
    }

    public Real getSunriseTemp() {
        return panelSunrise.getTemperature();
    }

    public Real getNoonTemp() {
        return panelNoon.getTemperature();
    }

    public Real get1400Temp() {
        return panel1400.getTemperature();
    }

    public Real getSunsetTemp() {
        return panelSunset.getTemperature();
    }

    public void setSunriseTemp(Real value) {
        panelSunrise.setTemperature(value);
    }

    public void setNoonTemp(Real value) {
        panelNoon.setTemperature(value);
    }

    public void set1400Temp(Real value) {
        panel1400.setTemperature(value);
    }

    public void setSunsetTemp(Real value) {
        panelSunset.setTemperature(value);
    }

    @Override
    public String getName() {
        return Bundle.CTL_DiurnalTemperatures();
    }

    /** This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(400, 300));
        setPreferredSize(new java.awt.Dimension(600, 300));
        setLayout(new java.awt.GridLayout(1, 4, 3, 0));

        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.setLayout(new java.awt.BorderLayout());
        add(jPanel1);

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel2.setLayout(new java.awt.BorderLayout());
        add(jPanel2);

        jPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel3.setLayout(new java.awt.BorderLayout());
        add(jPanel3);

        jPanel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel4.setLayout(new java.awt.BorderLayout());
        add(jPanel4);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables

}
