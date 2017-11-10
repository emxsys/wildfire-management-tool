/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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

import com.emxsys.gis.api.viewer.SectorEditor;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * A context aware action that listens for a SelectSelectorSupport object in the lookup. This is the
 * Action delegate class that handles the request to select a geographic region.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: SelectSector.java 394 2012-12-08 21:11:58Z bdschubert $
 */
@ActionID(id = "com.emxsys.wmt.globe.actions.SelectSector", category = "Tools")
@ActionRegistration(displayName = "#CTL_SelectSector",
        iconBase = "com/emxsys/wmt/globe/images/select.png",
        iconInMenu = true)
@ActionReference(path = "Toolbars/Map Tools", position = 1100)
@RibbonActionReference(path = "Ribbon/TaskPanes/Tools/Map Cache", position = 100,
        priority = "top",
        description = "#CTL_SelectSector_Hint",
        tooltipTitle = "#CTL_SelectSector_TooltipTitle",
        tooltipBody = "#CTL_SelectSector_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/images/select.png")
@Messages({
    "CTL_SelectSector=Select Sector",
    "CTL_SelectSector_Hint=Select a Sector.",
    "CTL_SelectSector_TooltipTitle=Select a Sector",
    "CTL_SelectSector_TooltipBody=Select the geographical region that defines the sector for downloading.",})
public class SelectSector extends AbstractAction {

    private SectorEditor context;

    public SelectSector(SectorEditor context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.context.enableSectorSelector();
    }
}
