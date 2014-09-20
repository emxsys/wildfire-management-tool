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
package com.emxsys.weather.options;

import com.emxsys.weather.api.WeatherType;
import static com.emxsys.weather.options.WeatherOptions.PREF_RH_1200;
import static com.emxsys.weather.options.WeatherOptions.PREF_RH_1400;
import static com.emxsys.weather.options.WeatherOptions.PREF_RH_SUNRISE;
import static com.emxsys.weather.options.WeatherOptions.PREF_RH_SUNSET;
import static com.emxsys.weather.options.WeatherOptions.PREF_AIR_TEMP_1200;
import static com.emxsys.weather.options.WeatherOptions.PREF_AIR_TEMP_1400;
import static com.emxsys.weather.options.WeatherOptions.PREF_AIR_TEMP_SUNRISE;
import static com.emxsys.weather.options.WeatherOptions.PREF_AIR_TEMP_SUNSET;
import static com.emxsys.weather.options.WeatherOptions.getAirTempUnit;
import static java.lang.Math.round;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.Exceptions;
import visad.Real;
import visad.VisADException;

final class DiurnalWeatherOptionsPanel extends javax.swing.JPanel {

    private final DiurnalWeatherOptionsPanelController controller;

    // This listenter marks the controller as changed on any edit of the text field
    private final DocumentListener docListener = new DocumentListener() {

        @Override
        public void insertUpdate(DocumentEvent e) {
            controller.changed();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            controller.changed();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            controller.changed();
        }
    };

    DiurnalWeatherOptionsPanel(DiurnalWeatherOptionsPanelController controller) {
        this.controller = controller;
        initComponents();

        // Llisten to changes in form fields and call controller.changed()
        temp1200.getDocument().addDocumentListener(docListener);
        temp1400.getDocument().addDocumentListener(docListener);
        tempSunrise.getDocument().addDocumentListener(docListener);
        tempSunset.getDocument().addDocumentListener(docListener);
        rh1200.getDocument().addDocumentListener(docListener);
        rh1400.getDocument().addDocumentListener(docListener);
        rhSunrise.getDocument().addDocumentListener(docListener);
        rhSunset.getDocument().addDocumentListener(docListener);
    }

    /** This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabelTime = new javax.swing.JLabel();
        jLabelTemp = new javax.swing.JLabel();
        jLabelRH = new javax.swing.JLabel();
        jLabelSunrise = new javax.swing.JLabel();
        tempSunrise = new javax.swing.JFormattedTextField();
        rhSunrise = new javax.swing.JFormattedTextField();
        jLabelNoon = new javax.swing.JLabel();
        temp1200 = new javax.swing.JFormattedTextField();
        rh1200 = new javax.swing.JFormattedTextField();
        jLabel1400 = new javax.swing.JLabel();
        temp1400 = new javax.swing.JFormattedTextField();
        rh1400 = new javax.swing.JFormattedTextField();
        jLabelSunset = new javax.swing.JLabel();
        tempSunset = new javax.swing.JFormattedTextField();
        rhSunset = new javax.swing.JFormattedTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jLabelWeatherCycle = new javax.swing.JLabel();

        jPanel1.setLayout(new java.awt.GridLayout(5, 3, 5, 0));

        jLabelTime.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelTime.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTime, org.openide.util.NbBundle.getMessage(DiurnalWeatherOptionsPanel.class, "DiurnalWeatherOptionsPanel.jLabelTime.text")); // NOI18N
        jPanel1.add(jLabelTime);

        jLabelTemp.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelTemp.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTemp, org.openide.util.NbBundle.getMessage(DiurnalWeatherOptionsPanel.class, "DiurnalWeatherOptionsPanel.jLabelTemp.text")); // NOI18N
        jPanel1.add(jLabelTemp);

        jLabelRH.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelRH.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelRH, org.openide.util.NbBundle.getMessage(DiurnalWeatherOptionsPanel.class, "DiurnalWeatherOptionsPanel.jLabelRH.text")); // NOI18N
        jPanel1.add(jLabelRH);

        jLabelSunrise.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSunrise, org.openide.util.NbBundle.getMessage(DiurnalWeatherOptionsPanel.class, "DiurnalWeatherOptionsPanel.jLabelSunrise.text")); // NOI18N
        jLabelSunrise.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jPanel1.add(jLabelSunrise);

        tempSunrise.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("##0"))));
        tempSunrise.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(tempSunrise);

        rhSunrise.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        rhSunrise.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(rhSunrise);

        jLabelNoon.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelNoon, org.openide.util.NbBundle.getMessage(DiurnalWeatherOptionsPanel.class, "DiurnalWeatherOptionsPanel.jLabelNoon.text")); // NOI18N
        jLabelNoon.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jPanel1.add(jLabelNoon);

        temp1200.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("##0"))));
        temp1200.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(temp1200);

        rh1200.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        rh1200.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(rh1200);

        jLabel1400.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1400, org.openide.util.NbBundle.getMessage(DiurnalWeatherOptionsPanel.class, "DiurnalWeatherOptionsPanel.jLabel1400.text")); // NOI18N
        jLabel1400.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jPanel1.add(jLabel1400);

        temp1400.setColumns(4);
        temp1400.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("##0"))));
        temp1400.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(temp1400);

        rh1400.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        rh1400.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(rh1400);

        jLabelSunset.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSunset, org.openide.util.NbBundle.getMessage(DiurnalWeatherOptionsPanel.class, "DiurnalWeatherOptionsPanel.jLabelSunset.text")); // NOI18N
        jLabelSunset.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jPanel1.add(jLabelSunset);

        tempSunset.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("##0"))));
        tempSunset.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(tempSunset);

        rhSunset.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        rhSunset.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(rhSunset);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelWeatherCycle, org.openide.util.NbBundle.getMessage(DiurnalWeatherOptionsPanel.class, "DiurnalWeatherOptionsPanel.jLabelWeatherCycle.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 89, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelWeatherCycle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelWeatherCycle))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(75, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private String getAirTempText(String propertyKey) {
        Real real = WeatherOptions.getAirTempPreference(propertyKey);
        int value = (int) round(real.getValue());
        return Integer.toString(value);
    }

    private void setAirTempPref(String propertyKey, String number) {
        try {
            Real value = new Real(WeatherType.AIR_TEMP, Integer.parseInt(number), getAirTempUnit());
            WeatherOptions.setAirTempPreference(propertyKey, value);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private String getHumidityText(String propertyKey) {
        Real real = WeatherOptions.getRelHumidityPreference(propertyKey);
        int value = (int) round(real.getValue());
        return Integer.toString(value);
    }

    private void setHumidityPref(String propertyKey, String number) {
        WeatherOptions.setRelHumidityPreference(propertyKey, Integer.parseInt(number));
    }

    void load() {
        this.tempSunrise.setText(getAirTempText(PREF_AIR_TEMP_SUNRISE));
        this.temp1200.setText(getAirTempText(PREF_AIR_TEMP_1200));
        this.temp1400.setText(getAirTempText(PREF_AIR_TEMP_1400));
        this.tempSunset.setText(getAirTempText(PREF_AIR_TEMP_SUNSET));

        this.rhSunrise.setText(getHumidityText(PREF_RH_SUNRISE));
        this.rh1200.setText(getHumidityText(PREF_RH_1200));
        this.rh1400.setText(getHumidityText(PREF_RH_1400));
        this.rhSunset.setText(getHumidityText(PREF_RH_SUNSET));
    }

    void store() {
        setAirTempPref(PREF_AIR_TEMP_SUNRISE, this.tempSunrise.getText());
        setAirTempPref(PREF_AIR_TEMP_1200, this.temp1200.getText());
        setAirTempPref(PREF_AIR_TEMP_1400, this.temp1400.getText());
        setAirTempPref(PREF_AIR_TEMP_SUNSET, this.tempSunset.getText());

        setHumidityPref(PREF_RH_SUNRISE, this.rhSunrise.getText());
        setHumidityPref(PREF_RH_1200, this.rh1200.getText());
        setHumidityPref(PREF_RH_1400, this.rh1400.getText());
        setHumidityPref(PREF_RH_SUNSET, this.rhSunset.getText());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1400;
    private javax.swing.JLabel jLabelNoon;
    private javax.swing.JLabel jLabelRH;
    private javax.swing.JLabel jLabelSunrise;
    private javax.swing.JLabel jLabelSunset;
    private javax.swing.JLabel jLabelTemp;
    private javax.swing.JLabel jLabelTime;
    private javax.swing.JLabel jLabelWeatherCycle;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JFormattedTextField rh1200;
    private javax.swing.JFormattedTextField rh1400;
    private javax.swing.JFormattedTextField rhSunrise;
    private javax.swing.JFormattedTextField rhSunset;
    private javax.swing.JFormattedTextField temp1200;
    private javax.swing.JFormattedTextField temp1400;
    private javax.swing.JFormattedTextField tempSunrise;
    private javax.swing.JFormattedTextField tempSunset;
    // End of variables declaration//GEN-END:variables
}
