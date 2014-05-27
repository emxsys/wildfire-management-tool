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
package com.emxsys.wmt.cps.ui;

import com.emxsys.wildfire.api.FuelMoisture;
import com.emxsys.wildfire.api.StdFuelMoistureScenario;
import com.emxsys.wmt.cps.Controller;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbPreferences;

/**
 * FuelMoistureScenarioPanel
 *
 * @author Bruce Schubert
 */
public class FuelMoistureScenarioPanel extends javax.swing.JPanel {
    public static final String LAST_FUEL_MOISTURE = "wmt.cps.lastFuelMoisture";
    private final Preferences prefs = NbPreferences.forModule(FuelMoistureScenarioPanel.class);
    

    /** Creates new form FuelMoistureScenarioPanel */
    public FuelMoistureScenarioPanel() {
        initComponents();
        initScenariosComboBox();
    }

    /**
     * Populates the combo box.
     */
    private void initScenariosComboBox() {
        // Load ALL the FuelModelProviders regardless of their extents
        DefaultComboBoxModel<StdFuelMoistureScenario> comboBoxModel = new DefaultComboBoxModel<>();
        StdFuelMoistureScenario[] scenarios = StdFuelMoistureScenario.values();
        for (StdFuelMoistureScenario scenario : scenarios) {
            comboBoxModel.addElement(scenario);
        }
        // Preselect the last used provider, if set, otherwise, use the first provider 
        String lastValueName = prefs.get(LAST_FUEL_MOISTURE,
                StdFuelMoistureScenario.VeryLowDead_FullyCuredHerb.name());
        if (!lastValueName.isEmpty()) {
            // Select the matching value in the combobox model
            for (int i = 0; i < comboBoxModel.getSize(); i++) {
                StdFuelMoistureScenario scenario = comboBoxModel.getElementAt(i);

                if (scenario.name().equals(lastValueName)) {
                    comboBoxModel.setSelectedItem(scenario);
                    Controller.getInstance().setFuelMoisture(scenario.getFuelMoisture());
                }
            }
        }        
        
        scenariosComboBox.setModel(comboBoxModel);
    }

    /** This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        scenariosComboBox = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel1hr = new javax.swing.JLabel();
        jLabel10hr = new javax.swing.JLabel();
        jLabel100hr = new javax.swing.JLabel();
        jLabelHerb = new javax.swing.JLabel();
        jLabelWoody = new javax.swing.JLabel();
        jText1hr = new javax.swing.JTextField();
        jText10hr = new javax.swing.JTextField();
        jText100hr = new javax.swing.JTextField();
        jTextHerb = new javax.swing.JTextField();
        jTextWoody = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jLabel1.text")); // NOI18N

        scenariosComboBox.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        scenariosComboBox.setDoubleBuffered(true);
        scenariosComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                scenariosComboBoxItemStateChanged(evt);
            }
        });
        scenariosComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scenariosComboBoxActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jPanel2.border.title"))); // NOI18N
        jPanel2.setLayout(new java.awt.GridLayout(2, 5));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1hr, org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jLabel1hr.text")); // NOI18N
        jPanel2.add(jLabel1hr);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10hr, org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jLabel10hr.text")); // NOI18N
        jPanel2.add(jLabel10hr);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel100hr, org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jLabel100hr.text")); // NOI18N
        jPanel2.add(jLabel100hr);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelHerb, org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jLabelHerb.text")); // NOI18N
        jPanel2.add(jLabelHerb);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelWoody, org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jLabelWoody.text")); // NOI18N
        jPanel2.add(jLabelWoody);

        jText1hr.setEditable(false);
        jText1hr.setText(org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jText1hr.text")); // NOI18N
        jPanel2.add(jText1hr);

        jText10hr.setEditable(false);
        jText10hr.setText(org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jText10hr.text")); // NOI18N
        jPanel2.add(jText10hr);

        jText100hr.setEditable(false);
        jText100hr.setText(org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jText100hr.text")); // NOI18N
        jPanel2.add(jText100hr);

        jTextHerb.setEditable(false);
        jTextHerb.setText(org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jTextHerb.text")); // NOI18N
        jPanel2.add(jTextHerb);

        jTextWoody.setEditable(false);
        jTextWoody.setText(org.openide.util.NbBundle.getMessage(FuelMoistureScenarioPanel.class, "FuelMoistureScenarioPanel.jTextWoody.text")); // NOI18N
        jPanel2.add(jTextWoody);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scenariosComboBox, 0, 245, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(scenariosComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void scenariosComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_scenariosComboBoxItemStateChanged
        //relay event
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            // TODO: Update controller with fuel moisture tuple

        }
    }//GEN-LAST:event_scenariosComboBoxItemStateChanged

    private void scenariosComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scenariosComboBoxActionPerformed
        // Update UI moisture values
        StdFuelMoistureScenario scenario = (StdFuelMoistureScenario) scenariosComboBox.getSelectedItem();
        FuelMoisture fuelMoisture = scenario.getFuelMoisture();
        jText1hr.setText(Integer.toString((int) fuelMoisture.getDead1HrFuelMoisture().getValue()));
        jText10hr.setText(Integer.toString((int) fuelMoisture.getDead10HrFuelMoisture().getValue()));
        jText100hr.setText(Integer.toString((int) fuelMoisture.getDead100HrFuelMoisture().getValue()));
        jTextHerb.setText(Integer.toString((int) fuelMoisture.getLiveHerbFuelMoisture().getValue()));
        jTextWoody.setText(Integer.toString((int) fuelMoisture.getLiveWoodyFuelMoisture().getValue()));
        // Update the Controller
        Controller.getInstance().setFuelMoisture(fuelMoisture);
    }//GEN-LAST:event_scenariosComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel100hr;
    private javax.swing.JLabel jLabel10hr;
    private javax.swing.JLabel jLabel1hr;
    private javax.swing.JLabel jLabelHerb;
    private javax.swing.JLabel jLabelWoody;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jText100hr;
    private javax.swing.JTextField jText10hr;
    private javax.swing.JTextField jText1hr;
    private javax.swing.JTextField jTextHerb;
    private javax.swing.JTextField jTextWoody;
    private javax.swing.JComboBox scenariosComboBox;
    // End of variables declaration//GEN-END:variables

}
