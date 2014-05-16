/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.cps.ui;

import com.emxsys.wmt.cps.PropNames;
import com.emxsys.wmt.cps.charts.FuelModelChart;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.gis.api.viewer.GisViewer;
import com.emxsys.wmt.wildfire.api.FuelModel;
import com.emxsys.wmt.wildfire.api.StdFuelModel;
import com.emxsys.wmt.wildfire.api.StdFuelModelParams13;
import com.emxsys.wmt.wildfire.api.StdFuelModelParams40;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.javahelp.Help;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class FuelModelPanel extends javax.swing.JPanel implements LookupListener {

    private boolean formInitialized = false;
    private boolean manualMode = false;
    private FuelModelChart chart;
    private FuelModel fuelModel;
    private int combo13Selection = 0;
    private int combo40Selection = 0;
    private String selectedGroup = "";
    private GisViewer gisViewer;
    /**
     * listen for changes in the fuel model
     */
    Lookup.Result<FuelModel> lookupResultFuelModel;
    /**
     * listen for changes in the existence of data layers
     */
    Result<GisLayer> lookupResultDataProvider;
    private static final Logger logger = Logger.getLogger(FuelModelPanel.class.getName());

    /**
     * Creates new form FuelModelInputPanel
     */
    public FuelModelPanel() {
        initComponents();
        chart = new FuelModelChart();
        chartPanel.add(chart);
        // TODO: establish the initial selection from the default properties file
        // or from a previous session's property file
        formInitialized = true;
        radioBtn40.doClick();
        manualCheckBox.setSelected(manualMode);
        gisViewer = Lookup.getDefault().lookup(GisViewer.class);
        if (gisViewer == null) {
            logger.severe("A GIS Viewer was not found.  Automatic Fuel Model lookup is disabled.");
        } else {
            lookupResultDataProvider = gisViewer.getLookup().lookupResult(GisLayer.class);
            lookupResultDataProvider.addLookupListener(new LookupListener() {

                /**
                 * Monitor the data providers -- looking for a FuelModel in a provider's lookup.
                 */
                @Override
                public void resultChanged(LookupEvent ev) {
                    checkForFuelModelProvider();
                }
            });

        }
        checkForFuelModelProvider();
        if (lookupResultFuelModel == null) {
            logger.severe("A Fuel Model data provider was not found.  Automatic lookup is disabled.");
        }
    }

    /**
     * Examines the currently loaded data providers looking for a FuelModel capability. If one is
     * found, then a lookup result listener is established on that provider.
     */
    private void checkForFuelModelProvider() {
        if (lookupResultFuelModel == null) {
            // Determine if any of the providers have a FuelModel capability
            // as defined by any StdFuelModelParams40 enum instance in the lookup
            Collection<? extends GisLayer> providers = gisViewer.getLookup().lookupAll(GisLayer.class);
            for (GisLayer provider : providers) {
                if (provider.getLookup().lookup(StdFuelModelParams40.class) != null) {
                    // Add a lookup listener that tracks changes in the
                    // fuel model selected in the GIS
                    lookupResultFuelModel = provider.getLookup().lookupResult(FuelModel.class);
                    lookupResultFuelModel.addLookupListener(this);
                    logger.log(Level.FINE, "Found fuel model provider for FBFM40: {0}", provider.toString());
                    break;
                }
            }
        }
    }

    /**
     * Implements lookup listener on the GIS data - listens for changes in in the fuel model and
     * updates the panel with the current GIS provided model.
     *
     * @param ev
     */
    @Override
    public void resultChanged(LookupEvent ev) {
        if (manualMode) {
            return;
        }
        @SuppressWarnings("unchecked")
        Lookup.Result<FuelModel> rslt = (Result<FuelModel>) ev.getSource();
        Collection<? extends FuelModel> fuelModels = rslt.allInstances();
        if (!fuelModels.isEmpty()) {
            // Get the fuel model from the GIS
            for (FuelModel fm : fuelModels) {

                // TODO: Fix the fuel model group logic - it will break under localization
                // Ensure the fuel model group/classification matches the user's
                // selection.
                if (fm.getModelGroup().equalsIgnoreCase(selectedGroup)
                        || fm.getModelGroup().equalsIgnoreCase(StdFuelModel.FUEL_MODEL_GROUP_UNBURNABLE)) {
                    // Update the property
                    if (setFuelModel(fm)) {
                        // Update the Swing GUI on the AWT thread,
                        // i.e., select the model in the combobox.
                        EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                syncGuiToModel();
                            }
                        });
                    }
                    break;
                }
            }
        }
    }

    /**
     * When added, notify property change listeners of the current values
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        super.addPropertyChangeListener(pl);
        syncModelToGui();
    }

    /**
     * When added, notify property change listeners of the current values
     */
    @Override
    public void addPropertyChangeListener(String string, PropertyChangeListener pl) {
        super.addPropertyChangeListener(string, pl);
        syncModelToGui();
    }

    /**
     * Updates the fuel model property.
     *
     * @param newModel is the new fuel model.
     * @return true only if the property was changed.
     */
    private synchronized boolean setFuelModel(FuelModel newModel) {
        if (this.fuelModel != null && this.fuelModel.equals(newModel)) {
            return false;
        }
        this.fuelModel = newModel;
        return true;
    }

    /**
     * Update members and notify listeners
     */
    private void syncModelToGui() {

        // Don't display properties until all components are created
        if (!formInitialized) {
            return;
        }
        // Update the display chart
        FuelModel fm = (FuelModel) comboFuel.getSelectedItem();
        if (fm != null) {
            setFuelModel(fm);
            chart.updateChart(fm);
            this.firePropertyChange(PropNames.FUEL_MODEL, null, fm);
        }
    }

    /**
     * Update members and notify listeners
     */
    private void syncGuiToModel() {

        // Don't display properties until all components are created
        if (!formInitialized) {
            return;
        }
        // Update the display chart
        comboFuel.setSelectedItem(fuelModel);
        chart.updateChart(fuelModel);
        this.firePropertyChange(PropNames.FUEL_MODEL, null, fuelModel);
    }

    private void showHelp(String id) {
        Help help = Lookup.getDefault().lookup(Help.class);
        if (help != null && help.isValidID(id, true).booleanValue()) {
            help.showHelp(new HelpCtx(id));
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        fbfmPanel = new javax.swing.JPanel();
        radioBtn13 = new javax.swing.JRadioButton();
        radioBtn40 = new javax.swing.JRadioButton();
        comboFuel = new javax.swing.JComboBox();
        infoBtn = new javax.swing.JButton();
        manualCheckBox = new javax.swing.JCheckBox();
        chartPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        buttonGroup1.add(radioBtn13);
        radioBtn13.setText(org.openide.util.NbBundle.getMessage(FuelModelPanel.class, "FuelModelPanel.radioBtn13.text")); // NOI18N
        radioBtn13.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radioBtn13ItemStateChanged(evt);
            }
        });

        buttonGroup1.add(radioBtn40);
        radioBtn40.setText(org.openide.util.NbBundle.getMessage(FuelModelPanel.class, "FuelModelPanel.radioBtn40.text")); // NOI18N
        radioBtn40.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radioBtn40ItemStateChanged(evt);
            }
        });

        comboFuel.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboFuelItemStateChanged(evt);
            }
        });
        comboFuel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboFuelActionPerformed(evt);
            }
        });

        infoBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emxsys/wmt/cps/ui/help.png"))); // NOI18N
        infoBtn.setText(org.openide.util.NbBundle.getMessage(FuelModelPanel.class, "FuelModelPanel.infoBtn.text")); // NOI18N
        infoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoBtnActionPerformed(evt);
            }
        });

        manualCheckBox.setText(org.openide.util.NbBundle.getMessage(FuelModelPanel.class, "FuelModelPanel.manualCheckBox.text")); // NOI18N
        manualCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                manualCheckBoxStateChanged(evt);
            }
        });
        manualCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                manualCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout fbfmPanelLayout = new javax.swing.GroupLayout(fbfmPanel);
        fbfmPanel.setLayout(fbfmPanelLayout);
        fbfmPanelLayout.setHorizontalGroup(
            fbfmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fbfmPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fbfmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fbfmPanelLayout.createSequentialGroup()
                        .addComponent(radioBtn40)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioBtn13)
                        .addGap(18, 18, 18)
                        .addComponent(manualCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(comboFuel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        fbfmPanelLayout.setVerticalGroup(
            fbfmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fbfmPanelLayout.createSequentialGroup()
                .addGroup(fbfmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(infoBtn)
                    .addGroup(fbfmPanelLayout.createSequentialGroup()
                        .addGroup(fbfmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioBtn13)
                            .addComponent(radioBtn40)
                            .addComponent(manualCheckBox))
                        .addComponent(comboFuel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(8, 8, 8))
        );

        add(fbfmPanel, java.awt.BorderLayout.PAGE_END);

        chartPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        chartPanel.setLayout(new java.awt.BorderLayout());
        add(chartPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void radioBtn13ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radioBtn13ItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            comboFuel.removeAllItems();
            for (StdFuelModelParams13 fbfm : StdFuelModelParams13.values()) {
                comboFuel.addItem(StdFuelModel.getFuelModel(fbfm.getModelNo()));
            }
            comboFuel.setSelectedIndex(combo13Selection);
            //selectedGroup = radioBtn13.getText();
            selectedGroup = StdFuelModel.FUEL_MODEL_GROUP_ORIGINAL_13;
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            combo13Selection = comboFuel.getSelectedIndex();
        }
}//GEN-LAST:event_radioBtn13ItemStateChanged

    private void radioBtn40ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radioBtn40ItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            comboFuel.removeAllItems();
            for (StdFuelModelParams40 fbfm : StdFuelModelParams40.values()) {
                comboFuel.addItem(StdFuelModel.getFuelModel(fbfm.getModelNo()));
            }
            comboFuel.setSelectedIndex(combo40Selection);
            //selectedGroup = radioBtn40.getText();
            selectedGroup = StdFuelModel.FUEL_MODEL_GROUP_STANDARD_40;
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            combo40Selection = comboFuel.getSelectedIndex();
        }

}//GEN-LAST:event_radioBtn40ItemStateChanged

    private void comboFuelItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboFuelItemStateChanged
        this.syncModelToGui();

}//GEN-LAST:event_comboFuelItemStateChanged

    private void infoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoBtnActionPerformed

        // Build a help string ID that matches an entry in cps-map.xml file ...
        String id;
        if (radioBtn13.isSelected()) {
            id = "com.emxsys.wmt.cps.fuelmodel-13";
            if (fuelModel != null) {
                id += fuelModel.getModelCode();
            }
        } else {
            id = "com.emxsys.wmt.cps.fuelmodel-40";
            if (fuelModel != null) {
                id += "-" + fuelModel.getModelCode();
            }
        }
        // ... and then show the javahelp
        showHelp(id);
    }//GEN-LAST:event_infoBtnActionPerformed

    private void comboFuelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboFuelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboFuelActionPerformed

    private void manualCheckBoxStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_manualCheckBoxStateChanged
    {//GEN-HEADEREND:event_manualCheckBoxStateChanged
        // TODO add your handling code here:


    }//GEN-LAST:event_manualCheckBoxStateChanged

    private void manualCheckBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_manualCheckBoxItemStateChanged
    {//GEN-HEADEREND:event_manualCheckBoxItemStateChanged
        // TODO add your handling code here:
        manualMode = (evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_manualCheckBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JComboBox comboFuel;
    private javax.swing.JPanel fbfmPanel;
    private javax.swing.JButton infoBtn;
    private javax.swing.JCheckBox manualCheckBox;
    private javax.swing.JRadioButton radioBtn13;
    private javax.swing.JRadioButton radioBtn40;
    // End of variables declaration//GEN-END:variables
}
