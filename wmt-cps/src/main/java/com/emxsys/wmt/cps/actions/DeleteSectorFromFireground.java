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

import com.emxsys.wmt.wildfire.api.Fireground;
import com.emxsys.wmt.gis.api.Box;
import com.emxsys.wmt.util.ProjectUtil;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
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

@ActionID(category = "Fire",
        id = "com.emxsys.wmt.cps.actions.DeleteSectorFromFireground")
@ActionRegistration(iconBase = "com/emxsys/wmt/cps/images/layer-delete.png",
        displayName = "#CTL_DeleteSectorFromFireground")
@ActionReference(path = "Toolbars/Fire", position = 300)
@RibbonActionReference(path = "Menu/Process/Fireground", position = 300)
@Messages({
    "CTL_DeleteSectorFromFireground=Delete Fireground Sector ",
    "titleCannotRemoveSector=Cannot Remove Sector from Fireground"
})
public final class DeleteSectorFromFireground extends AbstractAction {

    private final Box context;
    private static final Logger LOG = Logger.getLogger(DeleteSectorFromFireground.class.getName());

    public DeleteSectorFromFireground(Box context) {
        this.context = context;
        putValue(NAME, Bundle.CTL_DeleteSectorFromFireground());
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        // A project that supports a fireground object is required.
        Project project = ProjectUtil.getCurrentProject();
        if (project == null) {
            // Inform the user that a project is required
            displayWarningMessage(Bundle.warningNoProject());
            return;
        }
        // The project must contain a fireground (e.g., the CpsProject class)
        Fireground fireground = project.getLookup().lookup(Fireground.class);
        if (fireground == null) {
            displayWarningMessage(Bundle.warningNoFireground());
            return;
        }
        fireground.removeSector(context);
    }

    private void displayWarningMessage(String warningMessage) {
        // Notify user
        NotifyDescriptor nd = new NotifyDescriptor.Message(
                warningMessage, NotifyDescriptor.WARNING_MESSAGE);
        nd.setTitle(Bundle.titleCannotAddSector());
        DialogDisplayer.getDefault().notify(nd);

        // Log warning
        LOG.log(Level.WARNING, "{0}: {1}", new Object[]{
            Bundle.titleCannotRemoveSector(), warningMessage
        });
    }
}
