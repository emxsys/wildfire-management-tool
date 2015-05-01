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
package com.emxsys.wmt.cps.views.forces;

import com.emxsys.visad.GeneralUnit;
import com.emxsys.visad.Reals;
import com.emxsys.weather.api.Weather;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.weather.panels.WindForcePanel;
import com.emxsys.wmt.cps.Controller;
import com.emxsys.wmt.cps.Model;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.openide.util.NbBundle;
import visad.CommonUnit;
import visad.Real;

/**
 * Displays the Wind Force.
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({})
public class WindForceView extends javax.swing.JPanel {

    // Properties that are available from this panel
    public static final String PROP_WINDDIR = "PROP_WINDDIR";
    public static final String PROP_WINDSPEED = "PROP_WINDSPEED";

    // Borders used to indicate manual overrides
    private LineBorder lineBorder = new LineBorder(Color.black);
    private EmptyBorder emptyBorder = new EmptyBorder(1, 1, 1, 1);

    // Implementation
    private final WindForcePanel windPanel = new WindForcePanel();

    /**
     * Constructor creates new form WindForcePanel.
     */
    public WindForceView() {
        initComponents();
        windPanel.setBorder(emptyBorder);
        add(windPanel);

        // Update the Conroller et al from inputs in this View
        windPanel.addPropertyChangeListener((e) -> {
            switch (e.getPropertyName()) {
                case com.emxsys.weather.panels.WindForcePanel.PROP_WIND_DIR:
                    Controller.getInstance().setWindDir((Real) e.getNewValue()); 
                    firePropertyChange(PROP_WINDDIR, e.getOldValue(), e.getNewValue());
                    windPanel.setBorder(lineBorder);    // indicates overridden value
                    break;
                case com.emxsys.weather.panels.WindForcePanel.PROP_WIND_SPD:
                    Controller.getInstance().setWindSpeed((Real) e.getNewValue());
                    firePropertyChange(PROP_WINDSPEED, e.getOldValue(), e.getNewValue());
                    windPanel.setBorder(lineBorder);    // indicates overridden value
                    break;
            }
        });

        // Add a listener to syncronize this View to the Model
        Model.getInstance().addPropertyChangeListener(Model.PROP_WEATHER, (PropertyChangeEvent evt) -> {
            Weather weather = (Weather) evt.getNewValue();
            Real newWindDir = weather.getWindDirection();
            Real newWindSpd = weather.getWindSpeed();
            Real oldWindDir = windPanel.getWindDirection();
            Real oldWindSpd = windPanel.getWindSpeed();
            // Update if either new value is not equal to the old value
            if (!newWindSpd.isMissing() && !newWindDir.isMissing()) {
                if (!Reals.nearlyEquals(newWindDir, oldWindDir, CommonUnit.degree, 1.0d)
                        || !Reals.nearlyEquals(newWindSpd, oldWindSpd, GeneralUnit.mph, 1.0d)) {
                    windPanel.setWindSpeed(newWindSpd);
                    windPanel.setWindDirection(newWindDir);
                    windPanel.setBorder(emptyBorder);
                }
            }
        });
        //windPanel.setWindDirection(Model.getInstance().getWeather().getWindDirection());
        //windPanel.setWindSpeed(Model.getInstance().getWeather().getWindSpeed());
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(WindForceView.class, "WindForceView.border.title"))); // NOI18N
        setLayout(new java.awt.BorderLayout());
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WindForceView.class, "WindForceView.border.title")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
