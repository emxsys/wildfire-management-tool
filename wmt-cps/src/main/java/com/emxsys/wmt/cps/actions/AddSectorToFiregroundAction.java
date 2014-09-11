/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com> 
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
package com.emxsys.wmt.cps.actions;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.viewer.SelectedSector;
import com.emxsys.util.ProjectUtil;
import com.emxsys.wildfire.api.Fireground;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(id = "com.emxsys.wmt.cps.actions.AddSectorToFireground", category = "Fire")
@ActionRegistration(displayName = "Add Sector to Fireground",
        iconBase = "com/emxsys/wmt/cps/images/layer-add.png",
        iconInMenu = true)
@ActionReference(path = "Toolbars/Fire", position = 1200)
@RibbonActionReference(path = "Menu/Process/Fireground", position = 200)
@Messages({ //    "CTL_AddSectorToFireground=Add Sector to Fireground",
//    "ERR_NoProject=A project must be designated to perform this action.\n"
//    + "You must open or create a project.",
//    "ERR_NoFireground=The selected project does not support this action.\n"
//    + "You must designate a main project that supports a Fireground.",
//    "ERR_CannotAddSector=Cannot Add Sector to Fireground"
})
public class AddSectorToFiregroundAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(AddSectorToFiregroundAction.class.getName());
    SelectedSector context;

    public AddSectorToFiregroundAction(SelectedSector context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // A project that supports a fireground object is required.
        Project project = ProjectUtil.getCurrentProject();
        if (project == null) {
            // Inform the user that a project is required
            displayWarningMessage("A project must be designated to perform this action.\n"
                    + "You must open or create a project.");
            return;
        }

        // The project must contain a fireground (see the WmtProject class)
        Fireground fireground = project.getLookup().lookup(Fireground.class);
        if (fireground == null) {
            displayWarningMessage("The selected project does not support this action.\n"
                    + "You must designate a main project that supports a Fireground.");
            return;
        }

        // XXX: The current development restriction for just one sector per fireground 
        // TODO: Allow multiple sectors
        List<Box> sectors = fireground.getSectors();
        if (!sectors.isEmpty()) {
            if (!getUserConfirmation("Remove Existing Sector?",
                    "Currently, only one sector is allowed per fireground. "
                    + "Is it OK to replace the existing sector with this one?")) {
                return;
            }
        }

        // Select a fuel model provider to be assigned to this sector
        SelectFuelModelProviderAction selectFuelModel = new SelectFuelModelProviderAction(context.getSelectedSector());
        selectFuelModel.actionPerformed(null);

        // Add the designated sector to the fireground
        fireground.addSector(context.getSelectedSector(), selectFuelModel.getFuelModelProvider());

        // Turn off the sector editor
        context.getSectorEditor().disableSectorSelector();
    }

    private void displayWarningMessage(String warningMessage) {
        // Notify user
        NotifyDescriptor nd = new NotifyDescriptor.Message(
                warningMessage, NotifyDescriptor.WARNING_MESSAGE);
        nd.setTitle("Cannot Add Sector to Fireground");
        DialogDisplayer.getDefault().notify(nd);

        // Log warning
        LOG.log(Level.WARNING, "{0}: {1}", new Object[]{
            "Cannot Add Sector to Fireground", warningMessage
        });
    }

    private boolean getUserConfirmation(String dialogTitle, String confirmationMessage) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                confirmationMessage, dialogTitle, NotifyDescriptor.OK_CANCEL_OPTION);
        Object result = DialogDisplayer.getDefault().notify(nd);
        return result == NotifyDescriptor.OK_OPTION;
    }

}
