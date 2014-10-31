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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * CurrentProjectTracker is responsible for
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class CurrentProjectTracker {

    public static final String PROP_CURRENT_PROJECT_LIST = "current_project.list";

    /** Instance of the default project assistant. */
    private static CurrentProjectTracker defaultInstance = null;
    private static Lookup.Result<Project> lookupResults;
    private static LookupListener lookupListener;
    private ArrayList<Project> currentProjects = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(DefaultCurrentProjectTracker.class.getName());
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Protected constructor. Clients should call getDefault() to get an instance. Subclasses must
     * call.
     */
    protected CurrentProjectTracker() {
    }

    /**
     * Activates the project tracking.
     */
    public void activate() {
        if (lookupResults == null) {
            logger.config("Initializing global context lookup listener for Projects");

            // Monitor the existance of Projects in the global context lookup
            lookupResults = Utilities.actionsGlobalContext().lookupResult(Project.class);
            // Create the listener on the lookupResults
            lookupListener = new LookupListener() {
                // Update window title when the Project changes
                @Override
                public void resultChanged(LookupEvent ignored) {

                    Collection<? extends Project> projects = lookupResults.allInstances();
                    @SuppressWarnings("unchecked")
                    ArrayList<Project> oldProjects = (ArrayList<Project>) currentProjects.clone(); // shallow copy
                    currentProjects.clear();
                    currentProjects.addAll(projects);
                    pcs.firePropertyChange(PROP_CURRENT_PROJECT_LIST, oldProjects, currentProjects);
                }
            };
            // Activate the listener
            lookupResults.addLookupListener(lookupListener);
            lookupListener.resultChanged(null);
        }
    }

    public List<Project> getCurrentProjects() {
        if (lookupResults == null) {
            logger.warning("The project tracker has not been activated. Call CurrentProjectTracker.getDefault().activate()");
        }
        return currentProjects;
    }

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
     * Adds a property change listener that handles {@code PROP_CURRENT_PROJECT_LIST} events. The
     * event will contain {@code List<Project>} collections in the old and new value event
     * properties.
     * @param listener Listener to receive PROP_CURRENT_PROJECT_LIST events.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    ///////////////////////////////////////////////////////////////////////////
    /**
     * This subclass keeps the main window title up-to-date with the currently selected project
     * name.
     *
     * @author Bruce Schubert
     */
    public static class DefaultCurrentProjectTracker extends CurrentProjectTracker {

        public DefaultCurrentProjectTracker() {

            // The window title manager will call updateWindowTitle whenever the project selection changes
            addPropertyChangeListener(new MainWindowTitleManager());
        }

    }
}
