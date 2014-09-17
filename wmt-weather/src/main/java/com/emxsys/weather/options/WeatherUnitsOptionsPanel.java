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

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

final class WeatherUnitsOptionsPanel extends javax.swing.JPanel {

    private static final Preferences prefs = NbPreferences.forModule(WeatherOptions.class);
    private String initialAirTempUOM = "";
    private String initialWindSpdUOM = "";
    private boolean shouldRestart = false;
    private final WeatherUnitsOptionsPanelController controller;

    WeatherUnitsOptionsPanel(WeatherUnitsOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }

    /** This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        fahrenheitButton = new javax.swing.JRadioButton();
        celsiusButton = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        mphButton = new javax.swing.JRadioButton();
        ktsButton = new javax.swing.JRadioButton();
        kphButton = new javax.swing.JRadioButton();
        mpsButton = new javax.swing.JRadioButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(WeatherUnitsOptionsPanel.class, "WeatherUnitsOptionsPanel.jLabel3.text")); // NOI18N
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WeatherUnitsOptionsPanel.class, "WeatherUnitsOptionsPanel.jLabel1.text")); // NOI18N

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        buttonGroup1.add(fahrenheitButton);
        org.openide.awt.Mnemonics.setLocalizedText(fahrenheitButton, org.openide.util.NbBundle.getMessage(WeatherUnitsOptionsPanel.class, "WeatherUnitsOptionsPanel.fahrenheitButton.text")); // NOI18N
        fahrenheitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fahrenheitButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(celsiusButton);
        org.openide.awt.Mnemonics.setLocalizedText(celsiusButton, org.openide.util.NbBundle.getMessage(WeatherUnitsOptionsPanel.class, "WeatherUnitsOptionsPanel.celsiusButton.text")); // NOI18N
        celsiusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                celsiusButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(fahrenheitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(celsiusButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fahrenheitButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(celsiusButton)
                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WeatherUnitsOptionsPanel.class, "WeatherUnitsOptionsPanel.jLabel2.text")); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        buttonGroup2.add(mphButton);
        org.openide.awt.Mnemonics.setLocalizedText(mphButton, org.openide.util.NbBundle.getMessage(WeatherUnitsOptionsPanel.class, "WeatherUnitsOptionsPanel.mphButton.text")); // NOI18N
        mphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mphButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(ktsButton);
        org.openide.awt.Mnemonics.setLocalizedText(ktsButton, org.openide.util.NbBundle.getMessage(WeatherUnitsOptionsPanel.class, "WeatherUnitsOptionsPanel.ktsButton.text")); // NOI18N
        ktsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ktsButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(kphButton);
        org.openide.awt.Mnemonics.setLocalizedText(kphButton, org.openide.util.NbBundle.getMessage(WeatherUnitsOptionsPanel.class, "WeatherUnitsOptionsPanel.kphButton.text")); // NOI18N
        kphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kphButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(mpsButton);
        org.openide.awt.Mnemonics.setLocalizedText(mpsButton, org.openide.util.NbBundle.getMessage(WeatherUnitsOptionsPanel.class, "WeatherUnitsOptionsPanel.mpsButton.text")); // NOI18N
        mpsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mpsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mphButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ktsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(kphButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mpsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mphButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ktsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(kphButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mpsButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fahrenheitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fahrenheitButtonActionPerformed
        controller.changed();
    }//GEN-LAST:event_fahrenheitButtonActionPerformed

    private void celsiusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_celsiusButtonActionPerformed
        controller.changed();
    }//GEN-LAST:event_celsiusButtonActionPerformed

    private void mphButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mphButtonActionPerformed
        controller.changed();
    }//GEN-LAST:event_mphButtonActionPerformed

    private void ktsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ktsButtonActionPerformed
        controller.changed();
    }//GEN-LAST:event_ktsButtonActionPerformed

    private void kphButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kphButtonActionPerformed
        controller.changed();
    }//GEN-LAST:event_kphButtonActionPerformed

    private void mpsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mpsButtonActionPerformed
        controller.changed();
    }//GEN-LAST:event_mpsButtonActionPerformed

    void load() {
        initialAirTempUOM = prefs.get(WeatherOptions.PREF_AIR_TEMP_UOM, WeatherOptions.UOM_FAHRENHEIT);
        initialWindSpdUOM = prefs.get(WeatherOptions.PREF_WIND_SPD_UOM, WeatherOptions.UOM_MPH);
        switch (initialAirTempUOM) {
            case WeatherOptions.UOM_FAHRENHEIT:
                this.fahrenheitButton.setSelected(true);
                break;
            case WeatherOptions.UOM_CELSIUS:
                this.celsiusButton.setSelected(true);
                break;
        }
        switch (initialWindSpdUOM) {
            case WeatherOptions.UOM_MPH:
                this.mphButton.setSelected(true);
                break;
            case WeatherOptions.UOM_KPH:
                this.kphButton.setSelected(true);
                break;
            case WeatherOptions.UOM_KTS:
                this.ktsButton.setSelected(true);
                break;
            case WeatherOptions.UOM_MPS:
                this.mpsButton.setSelected(true);
                break;
        }
        shouldRestart = false;
    }

    void store() {
        if (this.fahrenheitButton.isSelected()) {
            prefs.put(WeatherOptions.PREF_AIR_TEMP_UOM, WeatherOptions.UOM_FAHRENHEIT);
        } else if (this.celsiusButton.isSelected()) {
            prefs.put(WeatherOptions.PREF_AIR_TEMP_UOM, WeatherOptions.UOM_CELSIUS);
        }

        if (this.mphButton.isSelected()) {
            prefs.put(WeatherOptions.PREF_WIND_SPD_UOM, WeatherOptions.UOM_MPH);
        } else if (this.kphButton.isSelected()) {
            prefs.put(WeatherOptions.PREF_WIND_SPD_UOM, WeatherOptions.UOM_KPH);
        } else if (this.ktsButton.isSelected()) {
            prefs.put(WeatherOptions.PREF_WIND_SPD_UOM, WeatherOptions.UOM_KTS);
        } else if (this.mpsButton.isSelected()) {
            prefs.put(WeatherOptions.PREF_WIND_SPD_UOM, WeatherOptions.UOM_MPS);
        }

        String currentAirTempUOM = prefs.get(WeatherOptions.PREF_AIR_TEMP_UOM, WeatherOptions.UOM_FAHRENHEIT);
        String currentWindSpdUOM = prefs.get(WeatherOptions.PREF_WIND_SPD_UOM, WeatherOptions.UOM_MPH);
        if (!(currentAirTempUOM.equals(initialAirTempUOM) && currentWindSpdUOM.equals(initialWindSpdUOM))) {
            shouldRestart = true;
        }
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    public boolean getShouldRestart() {
        return shouldRestart;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JRadioButton celsiusButton;
    private javax.swing.JRadioButton fahrenheitButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton kphButton;
    private javax.swing.JRadioButton ktsButton;
    private javax.swing.JRadioButton mphButton;
    private javax.swing.JRadioButton mpsButton;
    // End of variables declaration//GEN-END:variables
}
