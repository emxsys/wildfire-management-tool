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

import com.emxsys.solar.api.Sunlight;
import com.emxsys.solar.api.SunlightTuple;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.api.SimpleWeatherProvider;
import com.emxsys.weather.api.WeatherProvider;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.weather.spi.DefaultWeatherProvider;
import com.emxsys.wmt.cps.Controller;
import com.emxsys.wmt.cps.options.CpsOptions;
import com.terramenta.ribbon.RibbonActionReference;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
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
        dtd = "-//com.emxsys.wmt.cps.ui//Weather//EN",
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
@ActionID(category = "Window", id = "com.emxsys.wmt.cps.ui.WeatherTopComponent")
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
    public static final String LAST_WEATHER_PROVIDER = "wmt.cps.lastWeatherProvider";
    private JPanel layoutPanel;
    private AirTemperaturePanel airTemperaturePanel;
    private RelativeHumidityPanel relativeHumidityPanel;
    private WindPanel windPanel;
    private final SimpleWeatherProvider simpleWx = new SimpleWeatherProvider();
    private final DiurnalWeatherProvider diurnalWx = new DiurnalWeatherProvider();
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
        initComponents();
        initPanels();
        initWeatherProviders();
        setName(Bundle.CTL_WeatherTopComponent());
        setToolTipText(Bundle.CTL_WeatherTopComponent_Hint());
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);
        logger.config(PREFERRED_ID + " initialized.");
    }

    /**
     * Gets the WeatherProvider used for overriding / interacting with the Weather.
     *
     * @return The SimpleWeatherProvider.
     */
    public SimpleWeatherProvider getSimpleWeather() {
        return this.simpleWx;
    }

    public void updateCharts(Sunlight sunlight) {
        diurnalWx.setSunlight(sunlight);
    }

    /**
     * Initializes the fuel model selections.
     */
    private void initWeatherProviders() {

        // Listen for changes in the CPS Options/preferences...
        prefsChangeListener = (PreferenceChangeEvent ignored) -> {
            RealType uom = prefs.get(CpsOptions.UOM_KEY, CpsOptions.UOM_US).equals(CpsOptions.UOM_US)
                    ? WeatherType.AIR_TEMP_F : WeatherType.AIR_TEMP_C;

            Real tempSunrise = new Real(uom, prefs.getInt(CpsOptions.TEMP_SUNRISE_KEY, CpsOptions.DEFAULT_TEMP_SUNRISE));
            Real tempNoon = new Real(uom, prefs.getInt(CpsOptions.TEMP_1200_KEY, CpsOptions.DEFAULT_TEMP_1200));
            Real temp1400 = new Real(uom, prefs.getInt(CpsOptions.TEMP_1400_KEY, CpsOptions.DEFAULT_TEMP_1400));
            Real tempSunset = new Real(uom, prefs.getInt(CpsOptions.TEMP_SUNSET_KEY, CpsOptions.DEFAULT_TEMP_SUNSET));

            Real rhSunrise = new Real(uom, prefs.getInt(CpsOptions.RH_SUNRISE_KEY, CpsOptions.DEFAULT_RH_SUNRISE));
            Real rhNoon = new Real(uom, prefs.getInt(CpsOptions.RH_1200_KEY, CpsOptions.DEFAULT_RH_1200));
            Real rh1400 = new Real(uom, prefs.getInt(CpsOptions.RH_1400_KEY, CpsOptions.DEFAULT_RH_1400));
            Real rhSunset = new Real(uom, prefs.getInt(CpsOptions.RH_SUNSET_KEY, CpsOptions.DEFAULT_RH_SUNSET));

            diurnalWx.initializeAirTemperatures(tempSunrise, tempNoon, temp1400, tempSunset);
            diurnalWx.initializeRelativeHumidities(rhSunrise, rhNoon, rh1400, rhSunset);

        };
        prefs.addPreferenceChangeListener(prefsChangeListener);
        // Fire a change event to load the preferences
        prefsChangeListener.preferenceChange(null);

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
    @SuppressWarnings("unchecked")
    private void initProvidersComboBox() {
        // TODO: will need to arbitrate between current project fireground provider and last used provider

        // Load ALL the WeatherProviders regardless of their extents
        DefaultComboBoxModel<WeatherProvider> comboBoxModel = new DefaultComboBoxModel<>();
        List<WeatherProvider> instances = DefaultWeatherProvider.getInstances();
        instances.stream().forEach((instance) -> {
            comboBoxModel.addElement(instance);
        });
        // Add the Simple and Diurnal weather providers
        comboBoxModel.addElement(simpleWx);
        comboBoxModel.addElement(diurnalWx);

        // Preselect the last used provider, if set, otherwise, just use the first provider 
        String lastProviderClassName = prefs.get(LAST_WEATHER_PROVIDER,
                instances.size() > 0 ? instances.get(0).getClass().getName() : "");
        if (!lastProviderClassName.isEmpty()) {
            // Select the matching class in the combobox model
            for (int i = 0; i < comboBoxModel.getSize(); i++) {
                WeatherProvider provider = comboBoxModel.getElementAt(i);

                if (provider.getClass().getName().equals(lastProviderClassName)) {
                    comboBoxModel.setSelectedItem(provider);
                    Controller.getInstance().setWeatherProvider(provider);
                }
            }
        }
        this.providersComboBox.setModel(comboBoxModel);
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
        providersComboBox = new javax.swing.JComboBox();
        centerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        upperPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(WeatherTopComponent.class, "WeatherTopComponent.upperPanel.border.title"))); // NOI18N

        providersComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                providersComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout upperPanelLayout = new javax.swing.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(providersComboBox, 0, 325, Short.MAX_VALUE)
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(providersComboBox)
        );

        add(upperPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.GridLayout(3, 1));
        add(centerPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void providersComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providersComboBoxActionPerformed
        // Update the Controller.
        WeatherProvider provider = (WeatherProvider) providersComboBox.getSelectedItem();
        logger.log(Level.FINE, "Selected weather provider: {0}", provider);
        Controller.getInstance().setWeatherProvider(provider);
        prefs.put(LAST_WEATHER_PROVIDER, provider.getClass().getName());
    }//GEN-LAST:event_providersComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
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
