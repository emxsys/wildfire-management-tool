/*
 * Copyright (c) 2009-2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.actions;

import com.emxsys.wmt.globe.Globe;
import com.terramenta.ribbon.RibbonActionReference;
import gov.nasa.worldwind.WorldWind;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(id = "com.emxsys.wmt.globe.actions.WorkOffline", category = "Tools")
@ActionRegistration(displayName = "#CTL_WorkOffline",
        iconBase = "com/emxsys/wmt/globe/images/world_remove.png",
        iconInMenu = true)
@ActionReference(path = "Toolbars/Map Tools", position = 4100)
@RibbonActionReference(path = "Menu/Tools/Network",
        position = 100,
        priority = "top",
        buttonStyle = "toggle",
        description = "#CTL_WorkOffline_Hint",
        tooltipTitle = "#CTL_WorkOffline_TooltipTitle",
        tooltipBody = "#CTL_WorkOffline_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/images/world_remove.png")
@Messages({
    "CTL_WorkOffline=Work Offline",
    "CTL_WorkOffline_Hint=Work Offline",
    "CTL_WorkOffline_TooltipTitle=Work Offline",
    "CTL_WorkOffline_TooltipBody=When enabled, this action will preempts to connect to the network.\n"
    + "This can improve performance if you are actually disconnected from the network."})
public class WorkOffline extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        // Toggle the offline state
        WorldWind.setOfflineMode(!WorldWind.isOfflineMode());
        
        // Cause data retrieval to resume if now online
        if (!WorldWind.isOfflineMode()) {
            Globe.getInstance().refreshView();
        }
    }


}
