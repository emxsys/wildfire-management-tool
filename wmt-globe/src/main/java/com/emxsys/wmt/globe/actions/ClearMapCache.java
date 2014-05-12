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

//import com.emxsys.worldwind.cachecleaner.DataCacheViewer;
import com.emxsys.wmt.globe.cache.DataCacheViewer;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 *
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(id = "com.emxsys.wmt.globe.actions.ClearMapCache", category = "Tools")
@ActionRegistration(displayName = "#CTL_ClearMapCache",
        iconBase = "com/emxsys/wmt/globe/images/mapset-remove.png",
        iconInMenu = true)
@ActionReference(path = "Toolbars/Map Tools", position = 1400)
@RibbonActionReference(path = "Menu/Tools/Map Cache", position = 400,
        description = "#CTL_ClearMapCache_Hint",
        priority = "top",
        tooltipTitle = "#CTL_ClearMapCache_TooltipTitle",
        tooltipBody = "#CTL_ClearMapCache_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/images/mapset-remove.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")

@Messages(        {
            "CTL_ClearMapCache=Remove Maps",
            "CTL_ClearMapCache_Hint=Remove maps from your system.",
            "CTL_ClearMapCache_TooltipTitle=Remove Maps",
            "CTL_ClearMapCache_TooltipBody=Remove maps from your system's map cache.\n"
            + "This action will open a dialog where you can select the map types and date ranges to delete.",
            "LBL_ClearMapCache_DialogTitle=Select Map Sets to Delete"
        })
public class ClearMapCache extends AbstractAction {

    public ClearMapCache() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DataCacheViewer dataCacheViewer = new DataCacheViewer();
        WindowManager.getDefault().getMainWindow().setCursor(Cursor.getDefaultCursor());
        
        dataCacheViewer.displayModal(Bundle.LBL_ClearMapCache_DialogTitle());
    }

}
