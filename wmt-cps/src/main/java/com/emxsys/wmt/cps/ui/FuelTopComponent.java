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

import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.FuelModelProvider;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wildfire.spi.DefaultFuelModelProvider;
import com.emxsys.wmt.cps.Controller;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.Toolkit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.javahelp.Help;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;

/**
 * The FuelTopComponent provides the FuelModelProvider selection interface and displays the
 * current Fuel Model and Fuel Conditions.
 *
 * @author Bruce Schubert
 */
@ConvertAsProperties(
        dtd = "-//com.emxsys.wmt.cps.ui//Fuel//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = FuelTopComponent.PREFERRED_ID,
        iconBase = "com/emxsys/wmt/cps/images/flame-16x16.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_FuelAction",
        preferredID = FuelTopComponent.PREFERRED_ID
)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "com.emxsys.wmt.cps.ui.FuelTopComponent")
@RibbonActionReference(path = "Menu/Window/Show",
        position = 210,
        priority = "top",
        description = "#CTL_FuelAction_Hint",
        tooltipTitle = "#CTL_FuelAction_TooltipTitle",
        tooltipBody = "#CTL_FuelAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/cps/images/flame-16x16.png")
//        tooltipFooter = "#CTL_FuelAction_TooltipFooter",
//        tooltipFooterIcon = "com/terramenta/images/help.png")
@Messages({
    "CTL_FuelTopComponent=Fuel",
    "CTL_FuelTopComponent_Hint=The Fuel Model window.",
    "CTL_FuelAction=Fuel Model",
    "CTL_FuelAction_Hint=Show the Fuel Model.",
    "CTL_FuelAction_TooltipTitle=Show the Fuel Model",
    "CTL_FuelAction_TooltipBody=Activates the Fuel Model window used for visualizing "
    + "the fuel conditions influencing fire behavior.",
    "CTL_FuelAction_TooltipFooter=Press F1 for more help."
})
public final class FuelTopComponent extends TopComponent {

    public static final String PREFERRED_ID = "FuelTopComponent";
    public static final String LAST_FUEL_MODEL_PROVIDER = "wmt.cps.lastFuelModelProvider";

    private FuelModelPanel fuelModelPanel;
    private FuelMoisturePanel fuelMoisturePanel;
    private FuelMoistureScenarioPanel fuelMoistureScenarioPanel;

    /** Listener for changes in the existence of data providersComboBox */
    private Result<FuelModelProvider> lookupFuelModelProviders;

    private final Preferences prefs = NbPreferences.forModule(FuelTopComponent.class);
    private static final Logger logger = Logger.getLogger(FuelTopComponent.class.getName());
    private FuelModel currentFuelModel;

    /**
     * Constructor.
     */
    public FuelTopComponent() {
        initComponents();
        initPanels();
        initFuelModelProviders();

        setName(Bundle.CTL_FuelTopComponent());
        setToolTipText(Bundle.CTL_FuelTopComponent_Hint());
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);
    }

    /**
     * Initializes the chart panels.
     */
    private void initPanels() {
        fuelModelPanel = new FuelModelPanel();
        fuelMoisturePanel = new FuelMoisturePanel();
        fuelMoistureScenarioPanel = new FuelMoistureScenarioPanel();

        // Assign panels to the Grid Layout
        centerPanel.add(fuelModelPanel);
        centerPanel.add(fuelMoisturePanel);
        centerPanel.add(fuelMoistureScenarioPanel);
    }

    /**
     * Initializes the fuel model selections.
     */
    private void initFuelModelProviders() {
        // Using a LookupListener to reinitialize the combobox whenever the list of providersComboBox changes
        lookupFuelModelProviders = Lookup.getDefault().lookupResult(FuelModelProvider.class);
        lookupFuelModelProviders.addLookupListener((LookupEvent ev) -> {
            initProvidersComboBox();
        });
        // Init the combobox ocntents for the first time
        initProvidersComboBox();
    }

    /**
     * Populates the combo box.
     */
    private void initProvidersComboBox() {
        // TODO: will need to arbitrate between current project fireground provider and last used provider

        // Load ALL the FuelModelProviders regardless of their extents
        DefaultComboBoxModel<FuelModelProvider> comboBoxModel = new DefaultComboBoxModel<>();
        List<FuelModelProvider> instances = DefaultFuelModelProvider.getInstances();
        instances.stream().forEach((instance) -> {
            comboBoxModel.addElement(instance);
        });
        // Preselect the last used provider, if set, otherwise, use the first provider 
        String lastProviderClassName = prefs.get(LAST_FUEL_MODEL_PROVIDER,
                instances.size() > 0 ? instances.get(0).getClass().getName() : "");
        if (!lastProviderClassName.isEmpty()) {
            // Select the matching class in the combobox model
            for (int i = 0; i < comboBoxModel.getSize(); i++) {
                FuelModelProvider provider = comboBoxModel.getElementAt(i);

                if (provider.getClass().getName().equals(lastProviderClassName)) {
                    comboBoxModel.setSelectedItem(provider);
                    Controller.getInstance().setFuelModelProvider(provider);
                }
            }
        }
        this.providersComboBox.setModel(comboBoxModel);
    }

    /**
     * Update JFreeCharts with the current fuel model.
     *
     * @param fuelModel The current fuel model.
     */
    public void updateCharts(FuelModel fuelModel) {
        currentFuelModel = fuelModel;
        fuelModelPanel.updateChart(fuelModel);
    }

    /**
     * Shows the Java Help for the current fuel model.
     * @param id Help ID (see cps-map.xml)
     */
    private void showHelp(String id) {
        Help help = Lookup.getDefault().lookup(Help.class);
        if (help != null && help.isValidID(id, true)) {
            help.showHelp(new HelpCtx(id));
        } else {
            // Give the user a "Beep" if no help available.
            Toolkit.getDefaultToolkit().beep();
            logger.log(Level.WARNING, "No help available for ID: {0}", id);
        }
    }

    /** This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        upperPanel = new javax.swing.JPanel();
        providersComboBox = new javax.swing.JComboBox();
        infoBtn = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        upperPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FuelTopComponent.class, "FuelTopComponent.upperPanel.border.title"))); // NOI18N

        providersComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                providersComboBoxActionPerformed(evt);
            }
        });

        infoBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emxsys/wmt/cps/ui/help.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(infoBtn, org.openide.util.NbBundle.getMessage(FuelTopComponent.class, "FuelTopComponent.infoBtn.text")); // NOI18N
        infoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout upperPanelLayout = new javax.swing.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addComponent(providersComboBox, 0, 171, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(infoBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(providersComboBox))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        add(upperPanel, java.awt.BorderLayout.PAGE_START);

        centerPanel.setLayout(new java.awt.GridLayout(3, 1));
        add(centerPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void providersComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providersComboBoxActionPerformed
        // Update the Controller with the user's FuelModelProvider selection
        FuelModelProvider provider = (FuelModelProvider) providersComboBox.getSelectedItem();
        
        logger.log(Level.FINE, "Setting the the fuel model provider: {0}", provider);
        Controller.getInstance().setFuelModelProvider(provider);
        
        prefs.put(LAST_FUEL_MODEL_PROVIDER, provider.getClass().getName());
    }//GEN-LAST:event_providersComboBoxActionPerformed

    private void infoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoBtnActionPerformed

        // Build a help string ID that matches an entry in cps-map.xml file ...
        if (currentFuelModel != null) {
            String id;
            switch (currentFuelModel.getModelGroup()) {
                case StdFuelModel.FUEL_MODEL_GROUP_ORIGINAL_13:
                    id = "com.emxsys.wmt.cps.fuelmodel-13";
                    id += currentFuelModel.getModelCode();
                    break;
                case StdFuelModel.FUEL_MODEL_GROUP_STANDARD_40:
                    id = "com.emxsys.wmt.cps.fuelmodel-40-";
                    id += currentFuelModel.getModelCode();
                    break;
                default:
                    logger.log(Level.INFO, "No help for Fuel Model Group: {0}", currentFuelModel.getModelGroup());
                    return;
            }
            // ... and then show the javahelp
            logger.log(Level.FINE, "Invoking help ID: {0}", id);
            showHelp(id);
        }
    }//GEN-LAST:event_infoBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton infoBtn;
    private javax.swing.JComboBox providersComboBox;
    private javax.swing.JPanel upperPanel;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

}
