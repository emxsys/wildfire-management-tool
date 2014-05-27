/*
 * Copyright (c) 2009-2014, Bruce Schubert <bruce@emxsys.com>
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
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
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
package com.emxsys.wmt.core;

import com.emxsys.wmt.core.project.CurrentProjectTracker;
import com.emxsys.wmt.core.welcome.WelcomeComponent;
import com.emxsys.wmt.core.welcome.WelcomeOptions;
import com.emxsys.wmt.core.welcome.FeedbackSurvey;
import java.util.Set;
import org.openide.modules.ModuleInstall;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {

        WindowManager.getDefault().invokeWhenUIReady(() -> {
            // Install component that tracks the current project and updates the global lookup
            CurrentProjectTracker.getDefault();
            
            // Launch feedback survey...if its active
            FeedbackSurvey.start();
        });

        // Show the Welcome Screen/Start Page
//        WindowManager.getDefault().addWindowSystemListener(new WindowSystemListener() {
//            @Override
//            public void beforeLoad(WindowSystemEvent event) {
//            }
//
//            @Override
//            public void afterLoad(WindowSystemEvent event) {
//            }
//
//            @Override
//            public void beforeSave(WindowSystemEvent event) {
//                WindowManager.getDefault().removeWindowSystemListener(this);
//                WelcomeComponent topComp = null;
//                boolean isEditorShowing = false;
//                Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
//                for (Mode mode : WindowManager.getDefault().getModes()) {
//                    TopComponent tc = mode.getSelectedTopComponent();
//                    if (tc instanceof WelcomeComponent) {
//                        topComp = (WelcomeComponent) tc;
//                    }
//                    if (null != tc && WindowManager.getDefault().isEditorTopComponent(tc)) {
//                        isEditorShowing = true;
//                    }
//                }
//                if (WelcomeOptions.getDefault().isShowOnStartup() && isEditorShowing) {
//                    if (topComp == null) {
//                        topComp = WelcomeComponent.findComp();
//                    }
//                    //activate welcome screen at shutdown to avoid editor initialization
//                    //before the welcome screen is activated again at startup
//                    topComp.open();
//                    topComp.requestActive();
//                }
//                else if (topComp != null) {
//                    topComp.close();
//                }
//            }
//
//            @Override
//            public void afterSave(WindowSystemEvent event) {
//            }
//        });

    }

}
