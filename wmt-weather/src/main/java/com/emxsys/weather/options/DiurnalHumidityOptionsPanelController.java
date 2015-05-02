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

import com.emxsys.weather.api.WeatherPreferences;
import static com.emxsys.weather.api.WeatherPreferences.*;
import com.emxsys.weather.wizards.DiurnalWeatherPanelHumidities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

@OptionsPanelController.SubRegistration(
        location = "Weather",
        position = 300,
        displayName = "#AdvancedOption_DisplayName_DiurnalHumidity",
        keywords = "#AdvancedOption_Keywords_DiurnalHumidity",
        keywordsCategory = "Weather/DiurnalHumidity"
)
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_DiurnalHumidity=Daily Humidities",
    "AdvancedOption_Keywords_DiurnalHumidity=diurnal daily hourly humidity"})
public final class DiurnalHumidityOptionsPanelController extends OptionsPanelController {

    private DiurnalWeatherPanelHumidities panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    @Override
    public void update() {
        getPanel().setSunriseHumidity(WeatherPreferences.getRelHumidityValue(PREF_RH_SUNRISE));
        getPanel().setNoonHumidity(WeatherPreferences.getRelHumidityValue(PREF_RH_1200));
        getPanel().set1400Humidity(WeatherPreferences.getRelHumidityValue(PREF_RH_1400));
        getPanel().setSunsetHumidity(WeatherPreferences.getRelHumidityValue(PREF_RH_SUNSET));
        changed = false;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(() -> {
            WeatherPreferences.setRelHumidityValue(PREF_RH_SUNRISE,
                    (int) Math.round(getPanel().getSunriseHumidity().getValue()));
            WeatherPreferences.setRelHumidityValue(PREF_RH_1200,
                    (int) Math.round(getPanel().getNoonHumidity().getValue()));
            WeatherPreferences.setRelHumidityValue(PREF_RH_1400,
                    (int) Math.round(getPanel().get1400Humidity().getValue()));
            WeatherPreferences.setRelHumidityValue(PREF_RH_SUNSET,
                    (int) Math.round(getPanel().getSunsetHumidity().getValue()));
            changed = false;
        });
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private DiurnalWeatherPanelHumidities getPanel() {
        if (panel == null) {
            panel = new DiurnalWeatherPanelHumidities();

            panel.addPropertyChangeListener(WeakListeners.propertyChange((PropertyChangeEvent evt) -> {
                changed();
            }, panel));
        }
        return panel;
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

}
