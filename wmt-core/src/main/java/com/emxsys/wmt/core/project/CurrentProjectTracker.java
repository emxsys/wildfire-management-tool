/*
 * Copyright (c) 2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.core.project;

import java.awt.Frame;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * CurrentProjectTracker is responsible for
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: CurrentProjectTracker.java 486 2013-03-02 12:58:00Z bdschubert $
 */
public abstract class CurrentProjectTracker {

    /** Instance of the default project assistant. */
    private static CurrentProjectTracker defaultInstance = null;

    /** Dummy constructor. Clients should call getDefault to get a ProjectAssistant instance. */
    CurrentProjectTracker() {
    }

    /**
     * Updates the main window title with the given project name.
     * @param projectName name to be used in the window title.
     */
    public abstract void updateWindowTitle(String projectName);

    /**
     * Singleton instance accessor method for a project assistant. Provides entry point for further
     * work with project assistant API of the system. The default behavior is to return a static
     * instance of the DefaultCurrentProjectTracker. Clients can override the default behavior by
     * adding a instance of the CurrentProjectTracker to the global lookup.
     *
     * @return instance of project assistant installed in the system
     * @see DefaultCurrentProjectTracker
     */
    public static CurrentProjectTracker getDefault() {
        CurrentProjectTracker instance = Lookup.getDefault().lookup(CurrentProjectTracker.class);
        return (instance != null) ? instance : getDefaultInstance();
    }

    private synchronized static CurrentProjectTracker getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new DefaultCurrentProjectTracker();
        }
        return defaultInstance;
    }

    /**
     * This class keeps the main window title up-to-date with the currently selected project name.
     *
     * @author Bruce Schubert
     */
    public static class DefaultCurrentProjectTracker extends CurrentProjectTracker {

        private static String originalWindowTitle;

        public DefaultCurrentProjectTracker() {
            // The window title manager will call updateWindowTitle whenever the project selection changes
            MainWindowTitleManager.activate();
        }

        /**
         * Called by the MainWindowTitleManager to update the window title when a project is
         * selected.
         * @param projectName name used in window title
         * @see MainWindowTitleManager
         */
        @Override
        public void updateWindowTitle(final String projectName) {
            // We have to do this on the AWT thread, so we use the invokeWhenUIReady
            // method which can be called from any thread.
            {
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        Frame mainWindow = WindowManager.getDefault().getMainWindow();
                        if (originalWindowTitle == null) {
                            originalWindowTitle = mainWindow.getTitle();
                        }
                        String title;
                        if (projectName == null || projectName.isEmpty()) {
                            title = originalWindowTitle;
                        }
                        else {
                            title = projectName + " - " + originalWindowTitle;
                        }
                        mainWindow.setTitle(title);
                    }
                });
            }
        }
    }
}
