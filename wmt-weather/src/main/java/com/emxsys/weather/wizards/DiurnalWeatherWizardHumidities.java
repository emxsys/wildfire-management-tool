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

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import visad.Real;

public class DiurnalWeatherWizardHumidities implements WizardDescriptor.Panel<WizardDescriptor> {

    private DiurnalWeatherPanelHumidities component;

    public DiurnalWeatherWizardHumidities() {
    }

    // Get the visual component for the panel. In this humiditylate, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public DiurnalWeatherPanelHumidities getComponent() {
        if (component == null) {
            component = new DiurnalWeatherPanelHumidities();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {

        Real value = (Real) wiz.getProperty(DiurnalWeatherWizard.PROP_REL_HUMIDITY_SUNRISE);
        if (value != null) {
            getComponent().setSunriseHumidity(value);
        }
        value = (Real) wiz.getProperty(DiurnalWeatherWizard.PROP_REL_HUMIDITY_NOON);
        if (value != null) {
            getComponent().setNoonHumidity(value);
        }
        value = (Real) wiz.getProperty(DiurnalWeatherWizard.PROP_REL_HUMIDITY_1400);
        if (value != null) {
            getComponent().set1400Humidity(value);
        }
        value = (Real) wiz.getProperty(DiurnalWeatherWizard.PROP_REL_HUMIDITY_SUNSET);
        if (value != null) {
            getComponent().setSunsetHumidity(value);
        }
        getComponent().repaint();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(DiurnalWeatherWizard.PROP_REL_HUMIDITY_SUNRISE, getComponent().getSunriseHumidity());
        wiz.putProperty(DiurnalWeatherWizard.PROP_REL_HUMIDITY_NOON, getComponent().getNoonHumidity());
        wiz.putProperty(DiurnalWeatherWizard.PROP_REL_HUMIDITY_1400, getComponent().get1400Humidity());
        wiz.putProperty(DiurnalWeatherWizard.PROP_REL_HUMIDITY_SUNSET, getComponent().getSunsetHumidity());
    }

}
