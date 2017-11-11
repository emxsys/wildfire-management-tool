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
 * - Neither the name of the Emxsys company nor the names of its 
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
package com.emxsys.wmt.core.actions;

import com.terramenta.ribbon.RibbonActionReference;
import com.emxsys.util.ScreenCaptureUtil;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bruce Schubert
 */
@ActionID(
        category = "Screenshot",
        id = "com.emxsys.wmt.core.actions.ScreenshotWindowAction")
@ActionRegistration(
        iconBase = "com/emxsys/wmt/core/images/snapshot-window.png",
        displayName = "#CTL_ScreenshotWindowAction")
@ActionReference(path = "Toolbars/Create", position = 1200)
@RibbonActionReference(path = "Ribbon/TaskPanes/Tools/Create/Screenshot", position = 200,
        priority = "top",
        menuText = "#CTL_ScreenshotWindowAction_Text",
        description = "#CTL_ScreenshotWindowAction_Hint",
        tooltipTitle = "#CTL_ScreenshotWindowAction_TooltipTitle",
        tooltipBody = "#CTL_ScreenshotWindowAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/core/images/window32.png",
        tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
        tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
@Messages(
        {
            "CTL_ScreenshotWindowAction=Screenshot Window",
            "CTL_ScreenshotWindowAction_Text=Of Current Window",
            "CTL_ScreenshotWindowAction_Hint=Creates a screenshot of the current Window",
            "CTL_ScreenshotWindowAction_TooltipTitle=Screenshot of Window",
            "CTL_ScreenshotWindowAction_TooltipBody=Capture a screenshot of the active window and saves it to a file."
        })
public final class ScreenshotWindowAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        // By invoking later, we allow the action presenter to reset before the screen is captured.
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
                if (tc != null) {
                    Rectangle r = new Rectangle(tc.getLocationOnScreen(), tc.getBounds().getSize());
                    ScreenCaptureUtil.captureToFile(r);
                }
            }
        });
    }
}
