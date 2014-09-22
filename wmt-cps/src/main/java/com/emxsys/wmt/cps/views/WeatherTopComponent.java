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
package com.emxsys.wmt.cps.views;

import com.emxsys.solar.api.Sunlight;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.api.SimpleWeatherProvider;
import com.emxsys.weather.api.WeatherProvider;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.weather.api.services.WeatherForecaster;
import com.emxsys.weather.api.services.WeatherObserver;
import com.emxsys.weather.api.WeatherOptions;
import com.emxsys.weather.spi.WeatherProviderFactory;
import com.emxsys.wmt.cps.Model;
import com.emxsys.wmt.cps.WeatherManager;
import com.emxsys.wmt.cps.options.CpsOptions;
import com.emxsys.wmt.cps.views.weather.AirTemperaturePanel;
import com.emxsys.wmt.cps.views.weather.RelativeHumidityPanel;
import com.emxsys.wmt.cps.views.weather.WindPanel;
import com.terramenta.ribbon.RibbonActionReference;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import visad.Real;
import visad.RealType;

/**
 * The WeatherTopComponent provides the WeatherProvider selection interface and displays weather
 * charts/graphs.
 */
@ConvertAsProperties(
        dtd = "-//com.emxsys.wmt.cps.views//Weather//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = WeatherTopComponent.PREFERRED_ID,
        iconBase = "com/emxsys/wmt/cps/images/sun_clouds.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_WeatherAction",
        preferredID = WeatherTopComponent.PREFERRED_ID
)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "com.emxsys.wmt.cps.views.WeatherTopComponent")
@RibbonActionReference(path = "Menu/Window/Show",
        position = 220,
        priority = "top",
        description = "#CTL_WeatherAction_Hint",
        tooltipTitle = "#CTL_WeatherAction_TooltipTitle",
        tooltipBody = "#CTL_WeatherAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/cps/images/sun_clouds.png")
//        tooltipFooter = "#CTL_WeatherAction_TooltipFooter",
//        tooltipFooterIcon = "com/terramenta/images/help.png")
@Messages({
    "CTL_WeatherTopComponent=Weather",
    "CTL_WeatherTopComponent_Hint=The Weather window.",
    "CTL_WeatherAction=Weather",
    "CTL_WeatherAction_Hint=Show the Weather.",
    "CTL_WeatherAction_TooltipTitle=Show the Weather",
    "CTL_WeatherAction_TooltipBody=Activates the Weather window used for visualizing "
    + "the weather conditions influencing fire behavior.",
    "CTL_WeatherAction_TooltipFooter=Press F1 for more help."
})
public final class WeatherTopComponent extends TopComponent {

    public static final String PREFERRED_ID = "WeatherTopComponent";
    public static final String PREF_LAST_WX_OBSERVER_SERVICE = "wmt.cps.lastWeatherObserver";
    public static final String PREF_LAST_WX_FORECAST_SERVICE = "wmt.cps.lastWeatherForecaster";
    private JPanel layoutPanel;
    private AirTemperaturePanel airTemperaturePanel;
    private RelativeHumidityPanel relativeHumidityPanel;
    private WindPanel windPanel;
    private final SimpleWeatherProvider simpleWx = new SimpleWeatherProvider();
    private final DiurnalWeatherProvider diurnalWx;
    private WeatherProvider selectedObserver;
    private WeatherProvider selectedForecaster;
    private PreferenceChangeListener prefsChangeListener;

    /** Listener for changes in the existence of data providersComboBox */
    private Result<WeatherProvider> lookupWeatherProviders;
    private final Preferences prefs = NbPreferences.forModule(WeatherTopComponent.class);
    private static final Logger logger = Logger.getLogger(WeatherTopComponent.class.getName());

    /**
     * Constructor.
     */
    public WeatherTopComponent() {        
        logger.fine(PREFERRED_ID + " initializing....");
        
        // Initialize our "manual" weather provider
        diurnalWx = WeatherOptions.newDiurnalWeatherProvider();
        diurnalWx.setSunlight(Model.getInstance().getSunlight());
        // Add a listener to update the Diurnal Weather with the current sunlight
        Model.getInstance().addPropertyChangeListener(Model.PROP_SUNLIGHT, (PropertyChangeEvent evt) -> {
            diurnalWx.setSunlight((Sunlight) evt.getNewValue());
        });

        initComponents();
        initPanels();
        initWeatherProviders();
        setName(Bundle.CTL_WeatherTopComponent());
        setToolTipText(Bundle.CTL_WeatherTopComponent_Hint());
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        logger.config(PREFERRED_ID + " initialized.");
    }

    /**
     * Initializes the fuel model selections.
     */
    private void initWeatherProviders() {


        // Using a LookupListener to reinitialize the combobox whenever the list of providersComboBox changes
        lookupWeatherProviders = Lookup.getDefault().lookupResult(WeatherProvider.class);
        lookupWeatherProviders.addLookupListener((LookupEvent ev) -> {
            // Reinitialize 
            initProvidersComboBox();
        });
        // Init the combobox ocntents for the first time
        initProvidersComboBox();
    }

    /**
     * Populates the combo box.
     */
    @SuppressWarnings({"unchecked"})
    private void initProvidersComboBox() {
        // TODO: will need to arbitrate between current project fireground provider and last used provider

        // Load ALL the WeatherProviders regardless of their extents
        DefaultComboBoxModel<WeatherProvider> observers = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<WeatherProvider> forecasters = new DefaultComboBoxModel<>();
        List<WeatherProvider> instances = WeatherProviderFactory.getInstances();
        instances.stream().forEach((instance) -> {
            if (instance.hasService(WeatherObserver.class)) {
                observers.addElement(instance);
            }
            if (instance.hasService(WeatherForecaster.class)) {
                forecasters.addElement(instance);
            }
        });

        // Preselect the last used services, if set, otherwise, just use the first service
        String lastWxObsService = prefs.get(PREF_LAST_WX_OBSERVER_SERVICE,
                instances.size() > 0 ? instances.get(0).getClass().getName() : "");
        if (!lastWxObsService.isEmpty()) {
            // Select the matching class in the combobox model
            for (int i = 0; i < observers.getSize(); i++) {
                WeatherProvider provider = observers.getElementAt(i);
                if (provider.getClass().getName().equals(lastWxObsService)) {
                    observers.setSelectedItem(provider);
                    WeatherManager.getInstance().setObserver(provider.getService(WeatherObserver.class));
                }
            }
        }
        observers.addElement(diurnalWx);
        
        String lastWxFcstService = prefs.get(PREF_LAST_WX_FORECAST_SERVICE,
                instances.size() > 0 ? instances.get(0).getClass().getName() : "");
        if (!lastWxObsService.isEmpty()) {
            // Select the matching class in the combobox model
            for (int i = 0; i < forecasters.getSize(); i++) {
                WeatherProvider provider = forecasters.getElementAt(i);
                if (provider.getClass().getName().equals(lastWxFcstService)) {
                    forecasters.setSelectedItem(provider);
                    WeatherManager.getInstance().setForecaster(provider.getService(WeatherForecaster.class));
                }
            }
        }
        forecasters.addElement(new DiurnalWeatherProvider());
        
        this.observersComboBox.setModel(observers);
        this.forecastersComboBox.setModel(forecasters);
    }

    /**
     * Initializes the chart panels.
     */
    private void initPanels() {
        airTemperaturePanel = new AirTemperaturePanel();
        relativeHumidityPanel = new RelativeHumidityPanel();
        windPanel = new WindPanel();

        centerPanel.add(airTemperaturePanel);
        centerPanel.add(relativeHumidityPanel);
        centerPanel.add(windPanel);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        upperPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        observersComboBox = new javax.swing.JComboBox();
        forecastersComboBox = new javax.swing.JComboBox();
        configObserverButton = new javax.swing.JButton();
        configForecasterButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        upperPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(WeatherTopComponent.class, "WeatherTopComponent.upperPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WeatherTopComponent.class, "WeatherTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WeatherTopComponent.class, "WeatherTopComponent.jLabel2.text")); // NOI18N

        observersComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                observersComboBoxActionPerformed(evt);
            }
        });

        forecastersComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forecastersComboBoxActionPerformed(evt);
            }
        });

        configObserverButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emxsys/wmt/cps/images/process.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(configObserverButton, org.openide.util.NbBundle.getMessage(WeatherTopComponent.class, "WeatherTopComponent.configObserverButton.text")); // NOI18N
        configObserverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configObserverButtonActionPerformed(evt);
            }
        });

        configForecasterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emxsys/wmt/cps/views/process.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(configForecasterButton, org.openide.util.NbBundle.getMessage(WeatherTopComponent.class, "WeatherTopComponent.configForecasterButton.text")); // NOI18N
        configForecasterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configForecasterButtonActionPerformed(evt);
            }
        });

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emxsys/wmt/cps/images/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(WeatherTopComponent.class, "WeatherTopComponent.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout upperPanelLayout = new javax.swing.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(forecastersComboBox, 0, 244, Short.MAX_VALUE)
                    .addComponent(observersComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(configObserverButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configForecasterButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(observersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configObserverButton)
                    .addComponent(refreshButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(forecastersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configForecasterButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(upperPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.GridLayout(3, 1));
        add(centerPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void configForecasterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configForecasterButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_configForecasterButtonActionPerformed

    private void forecastersComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forecastersComboBoxActionPerformed
        
        // Update the Weather Manager
        WeatherProvider provider = (WeatherProvider) forecastersComboBox.getSelectedItem();
        logger.log(Level.FINE, "Selected weather provider: {0}", provider);
        WeatherManager.getInstance().setForecaster(provider.getService(WeatherForecaster.class));
        prefs.put(PREF_LAST_WX_FORECAST_SERVICE, provider.getClass().getName());
        
    }//GEN-LAST:event_forecastersComboBoxActionPerformed

    private void observersComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_observersComboBoxActionPerformed
        
        // Update the Weather Manager
        WeatherProvider provider = (WeatherProvider) observersComboBox.getSelectedItem();
        logger.log(Level.FINE, "Selected weather provider: {0}", provider);
        WeatherManager.getInstance().setObserver(provider.getService(WeatherObserver.class));
        prefs.put(PREF_LAST_WX_OBSERVER_SERVICE, provider.getClass().getName());
        
    }//GEN-LAST:event_observersComboBoxActionPerformed

    private void configObserverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configObserverButtonActionPerformed
        
        // Configure the weather source
        WeatherProvider provider = (WeatherProvider) observersComboBox.getSelectedItem();
        Action action = provider.getConfigAction();
        if (action != null) {
            action.actionPerformed(evt);
            WeatherManager.getInstance().refreshModels();
        }
        
    }//GEN-LAST:event_configObserverButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton configForecasterButton;
    private javax.swing.JButton configObserverButton;
    private javax.swing.JComboBox forecastersComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox observersComboBox;
    private javax.swing.JButton refreshButton;
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
