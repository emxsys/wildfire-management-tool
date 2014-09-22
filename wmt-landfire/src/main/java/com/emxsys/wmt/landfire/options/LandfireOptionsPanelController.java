/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.landfire.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


@OptionsPanelController.TopLevelRegistration(categoryName = "#OptionsCategory_Name_Landfire",
                                             iconBase = "com/emxsys/wmt/landfire/options/database_process32.png",
                                             keywords = "#OptionsCategory_Keywords_Landfire",
                                             keywordsCategory = "Landfire",
                                             position = 50)
public final class LandfireOptionsPanelController extends OptionsPanelController {

    private LandfireOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed = false;
    private boolean shouldLoadPanel = true;

    // TIP: You must have a public ctor to prevent java.lang.IllegalAccessError exceptions in Unit Tests
    public LandfireOptionsPanelController() {
        super();
    }

    @Override
    public void update() {
        if (shouldLoadPanel) {
            getPanel().load();
            changed = false;
            shouldLoadPanel = false;
        }
    }

    @Override
    public void applyChanges() {
        // Must run on separate thread else confirmation blocks causing "Lengthy operation in progress" message box.
        Runnable r = () -> {
            if (isChanged() && confirmSave()) {
                getPanel().store();
                changed = false;
                if (confirmRestart()) {
                    LifecycleManager.getDefault().markForRestart();
                    LifecycleManager.getDefault().exit();
                }
            }
        };
        new Thread(r).start();
        
    }

    private boolean confirmSave() {
        String msg = "Are you sure you want save these changes?\n"
            + "A mistake can disable the LANDFIRE data access.";
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Save LANDFIRE Changes",
            NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
        d.setValue(NotifyDescriptor.NO_OPTION);
        Object result = DialogDisplayer.getDefault().notify(d);
        return (result != null && result == NotifyDescriptor.YES_OPTION);
    }

    private boolean confirmRestart() {
        String msg = "A restart is required to activate these changes.\n"
            + "Do you want to restart the application now?";
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Restart Application",
            NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
        d.setValue(NotifyDescriptor.YES_OPTION);
        Object result = DialogDisplayer.getDefault().notify(d);
        return (result != null && result == NotifyDescriptor.YES_OPTION);
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
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

    private LandfireOptionsPanel getPanel() {
        if (panel == null) {
            panel = new LandfireOptionsPanel(this);
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
