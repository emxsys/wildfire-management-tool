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

import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.api.WeatherPreferences;
import java.awt.Component;
import java.awt.Image;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import visad.Real;

/**
 * A Wizard for setting the DiuralWeatherProvider values.
 *
 * @author Bruce Schubert
 */
@Messages({"CTL_DiurnalWeatherWizardTitle=Daily Weather"})
public final class DiurnalWeatherWizard {

    public static final String PROP_AIR_TEMP_UOM = "airTempUOM";
    public static final String PROP_AIR_TEMP_SUNRISE = "airTempSunrise";
    public static final String PROP_AIR_TEMP_NOON = "airTempNoon";
    public static final String PROP_AIR_TEMP_1400 = "airTemp1400";
    public static final String PROP_AIR_TEMP_SUNSET = "airTempSunset";
    public static final String PROP_REL_HUMIDITY_SUNRISE = "relHumiditySunrise";
    public static final String PROP_REL_HUMIDITY_NOON = "relHumidityNoon";
    public static final String PROP_REL_HUMIDITY_1400 = "relHumidity1400";
    public static final String PROP_REL_HUMIDITY_SUNSET = "relHumiditySunset";
    public static final String PROP_WIND_SPD_UOM = "windSpdUOM";
    public static final String PROP_WIND_SPEEDS = "windSpeeds";
    public static final String PROP_WIND_DIRECTIONS = "windDirections";
    public static final String PROP_CLOUD_COVERS = "cloudCovers";
    private final DiurnalWeatherProvider provider;
    private WizardDescriptor wizard;

    /**
     * Constructor
     * @param provider
     */
    public DiurnalWeatherWizard(DiurnalWeatherProvider provider) {
        this(provider, Bundle.CTL_DiurnalWeatherWizardTitle());
    }

    /**
     * Constructor.
     * @param provider
     * @param title
     */
    public DiurnalWeatherWizard(DiurnalWeatherProvider provider, String title) {
        this.provider = provider;

        // Create the wizard
        wizard = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(getPanels()));

        // Set the title: {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizard.setTitleFormat(new MessageFormat("{0}"));
        wizard.setTitle(title);

        // Override the default image on the left panel.  Scale our image to fill the panel width.
        Image image = ImageUtilities.loadImage("com/emxsys/weather/images/sun_clouds128.png", true)
                .getScaledInstance(195, 195, Image.SCALE_SMOOTH);
        wizard.putProperty(WizardDescriptor.PROP_IMAGE, image);
        wizard.putProperty(WizardDescriptor.PROP_IMAGE_ALIGNMENT, "South"); // North or South (default);
        
        // Set initial units
        wizard.putProperty(PROP_AIR_TEMP_UOM, WeatherPreferences.getAirTempUnit());
        wizard.putProperty(PROP_WIND_SPD_UOM, WeatherPreferences.getWindSpeedUnit());
        // Set initial air temperatures
        wizard.putProperty(PROP_AIR_TEMP_SUNRISE, provider.getTempAtSunrise());
        wizard.putProperty(PROP_AIR_TEMP_NOON, provider.getTempAtNoon());
        wizard.putProperty(PROP_AIR_TEMP_1400, provider.getTempAt1400());
        wizard.putProperty(PROP_AIR_TEMP_SUNSET, provider.getTempAtSunset());
        // Set initial humidities
        wizard.putProperty(PROP_REL_HUMIDITY_SUNRISE, provider.getRelativeHumidityAtSunrise());
        wizard.putProperty(PROP_REL_HUMIDITY_NOON, provider.getRelativeHumidityAtNoon());
        wizard.putProperty(PROP_REL_HUMIDITY_1400, provider.getRelativeHumidityAt1400());
        wizard.putProperty(PROP_REL_HUMIDITY_SUNSET, provider.getRelativeHumidityAtSunset());
        // Set initial winds
        wizard.putProperty(PROP_WIND_SPEEDS, provider.getWindSpeeds());
        wizard.putProperty(PROP_WIND_DIRECTIONS, provider.getWindDirs());
        // Update initial clouds
        wizard.putProperty(PROP_CLOUD_COVERS, provider.getClouds());
        
    }

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new DiurnalWeatherWizardUnits());
        panels.add(new DiurnalWeatherWizardTemps());
        panels.add(new DiurnalWeatherWizardHumidities());
        panels.add(new DiurnalWeatherWizardWinds());
        panels.add(new DiurnalWeatherWizardClouds(provider));
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        return panels;
    }

    /**
     * Display the wizard. The provider will be updated upon finish.
     */
    public void show() {
        if (DialogDisplayer.getDefault().notify(wizard) == WizardDescriptor.FINISH_OPTION) {
            updateProvider();
        }
    }

    /**
     * Update the provider from the wizard properties.
     */
    @SuppressWarnings("unchecked")
    private void updateProvider() {
        provider.initializeAirTemperatures(
                (Real) wizard.getProperty(PROP_AIR_TEMP_SUNRISE),
                (Real) wizard.getProperty(PROP_AIR_TEMP_NOON),
                (Real) wizard.getProperty(PROP_AIR_TEMP_1400),
                (Real) wizard.getProperty(PROP_AIR_TEMP_SUNSET));
        
        provider.initializeRelativeHumidities(
                (Real) wizard.getProperty(PROP_REL_HUMIDITY_SUNRISE),
                (Real) wizard.getProperty(PROP_REL_HUMIDITY_NOON),
                (Real) wizard.getProperty(PROP_REL_HUMIDITY_1400),
                (Real) wizard.getProperty(PROP_REL_HUMIDITY_SUNSET));
        
        provider.initializeWindDirections((TreeMap<LocalTime, Real>) wizard.getProperty(PROP_WIND_DIRECTIONS));
        
        provider.initializeWindSpeeds((TreeMap<LocalTime, Real>) wizard.getProperty(PROP_WIND_SPEEDS));
        
        provider.initializeCloudCovers((TreeMap<LocalTime, Real>) wizard.getProperty(PROP_CLOUD_COVERS));

    }

    /**
     * Display the wizard in a unit test. The method does not exit until either finished or cancel
     * is pressed. Press cancel to "fail" a unit test.
     * @return False if cancel is pressed, otherwise returns true.
     */
    boolean testShow() {
        Object result;
        do {
            // Display the (next) wizard panel
            result = DialogDisplayer.getDefault().notify(wizard);
            if (result == WizardDescriptor.CANCEL_OPTION) {
                return false;
            }
        } while (!(result == WizardDescriptor.FINISH_OPTION || result == WizardDescriptor.CLOSED_OPTION));
        if (result == WizardDescriptor.FINISH_OPTION ) {
            updateProvider();
        }
        return true;
    }
}
