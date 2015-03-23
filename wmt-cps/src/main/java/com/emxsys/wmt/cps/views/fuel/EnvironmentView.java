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
package com.emxsys.wmt.cps.views.fuel;

import com.emxsys.visad.GeneralUnit;
import com.emxsys.visad.Reals;
import com.emxsys.weather.api.Weather;
import com.emxsys.weather.api.WeatherPreferences;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.weather.panels.AirTemperatureGauge;
import com.emxsys.weather.panels.SkyCoverGauge;
import com.emxsys.weather.panels.RelativeHumidityGauge;
import com.emxsys.wmt.cps.Controller;
import com.emxsys.wmt.cps.Model;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import visad.Real;

/**
 * This panel is a view of the fuel's environment. It hosts a side-by-side AirTemperatureGauge and a
 * RelativeHumidityGauge.
 *
 * @author Bruce Schubert
 */
public class EnvironmentView extends javax.swing.JPanel {

    // Properties that are available from this panel
    public static final String PROP_AIRTEMP = "EnvironmentView.AirTemp";
    public static final String PROP_RELHUMIDITY = "EnvironmentView.RelHumidity";
    public static final String PROP_SKYCOVER = "EnvironmentView.SkyCover";

    // Implementations
    private AirTemperatureGauge airTempPanel;
    private RelativeHumidityGauge relHumidityPanel;
    private SkyCoverGauge skyCoverPanel;

    private LineBorder lineBorder = new LineBorder(Color.black);
    private EmptyBorder emptyBorder = new EmptyBorder(1, 1, 1, 1);

    /** Creates new form FuelMoisturePanel */
    public EnvironmentView() {
        initComponents();
        
        // See com.emxsys.wmt.cps.docs.cps-map.xml for help ids.
        this.airTempPanel = new AirTemperatureGauge("Air Temp.", WeatherPreferences.getAirTempUnit(),
                new Real(WeatherType.AIR_TEMP_F, 59), 
                "com.emxsys.wmt.cps.airtemperature");
        this.relHumidityPanel = new RelativeHumidityGauge("Humidty", 
                new Real(WeatherType.REL_HUMIDITY, 20),
                "com.emxsys.wmt.cps.relativehumidity");
        this.skyCoverPanel = new SkyCoverGauge("Sky Cover", 
                new Real(WeatherType.CLOUD_COVER, 10),
                "com.emxsys.wmt.cps.skycover");
        add(airTempPanel);
        add(relHumidityPanel);
        add(skyCoverPanel);

        // Update this View from the Model
        Model.getInstance().addPropertyChangeListener(Model.PROP_WEATHER, (PropertyChangeEvent evt) -> {
            Weather wx = Model.getInstance().getWeather();

            Real newAirTemp = wx.getAirTemperature();
            if (!Reals.nearlyEquals(newAirTemp, airTempPanel.getTemperature(), GeneralUnit.degF, 0.5d)) {
                airTempPanel.setTemperature(newAirTemp);
                airTempPanel.setBorder(emptyBorder);
            }
            Real newHumidity = wx.getRelativeHumidity();
            if (!newHumidity.equals(relHumidityPanel.getHumidity())) {
                relHumidityPanel.setHumidity(newHumidity);
                relHumidityPanel.setBorder(emptyBorder);
            }
            Real newSkyCover = wx.getCloudCover();
            if (!newSkyCover.equals(skyCoverPanel.getCloudCover())) {
                skyCoverPanel.setSkyCover(newSkyCover);
                skyCoverPanel.setBorder(emptyBorder);
            }
        });

        // Update the Controller et al from inputs in this View
        airTempPanel.addPropertyChangeListener(AirTemperatureGauge.PROP_AIR_TEMP, (e) -> {
            Controller.getInstance().setAirTemperature((Real) e.getNewValue());
            firePropertyChange(PROP_AIRTEMP, e.getOldValue(), e.getNewValue());
            airTempPanel.setBorder(lineBorder);    // indicates manual override
        });
        relHumidityPanel.addPropertyChangeListener(RelativeHumidityGauge.PROP_REL_HUMIDITY, (e) -> {
            Controller.getInstance().setRelativeHumidity((Real) e.getNewValue());
            firePropertyChange(PROP_RELHUMIDITY, e.getOldValue(), e.getNewValue());
            relHumidityPanel.setBorder(lineBorder);    // indicates manual override
        });
        skyCoverPanel.addPropertyChangeListener(SkyCoverGauge.PROP_SKY_COVER, (e) -> {
            Controller.getInstance().setSkyCover((Real) e.getNewValue());
            firePropertyChange(PROP_SKYCOVER, e.getOldValue(), e.getNewValue());
            skyCoverPanel.setBorder(lineBorder);    // indicates manual override
        });

    }

    public void updateAirTemperature(Real airTemp) {
        airTempPanel.setTemperature(airTemp);
    }

    public void updateRelativeHumidity(Real relHumidity) {
        relHumidityPanel.setHumidity(relHumidity);
    }

    public void updateSkyCover(Real skyCover) {
        skyCoverPanel.setSkyCover(skyCover);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(EnvironmentView.class, "EnvironmentView.border.title"))); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
