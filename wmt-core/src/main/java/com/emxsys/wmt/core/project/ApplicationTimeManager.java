/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
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

import com.emxsys.time.api.TimeRegistrar;
import com.emxsys.time.spi.TimeProviderFactory;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;

/**
 * This class updates the application time from the current project.
 *
 * @author Bruce Schubert
 */
public class ApplicationTimeManager implements PropertyChangeListener {

    private TimeRegistrar registrar = null;
    private static final Logger logger = Logger.getLogger(ApplicationTimeManager.class.getName());

    ApplicationTimeManager() {
    }

    /**
     * Handles changes in the project selection by setting the application time to the current
     * project's time settings.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (!evt.getPropertyName().equals(CurrentProjectTracker.PROP_CURRENT_PROJECT_LIST)) {
            logger.log(Level.WARNING, "Inappropriate property change event: {0}", evt.getPropertyName());
            return;
        }
        @SuppressWarnings("unchecked")
        List<Project> projects = (List<Project>) evt.getNewValue();
        if (projects.isEmpty()) {
            // TODO: Set the application time to the system time or a previously cached time.
        }
        else if (projects.size() == 1) {
            // Turn off the old registrar
            if (registrar != null) {
                registrar.deactivate();
            }

            // Set the new time registrar
            Project project = projects.iterator().next();
            registrar = project.getLookup().lookup(TimeRegistrar.class);
            
            if (registrar == null) {
                // TODO: set to system time; log warning
                updateApplicationTime(ZonedDateTime.now());
            }
            else {                
                // Set the time to the current project's last instant.
                updateApplicationTime(registrar.getCurrentTime());
            }
        }
        else {
            logger.warning("Multiple projects selected! Cannot set the application time.");
            logger.log(Level.WARNING, "Inappropriate property change event: {0}", evt.getPropertyName());
        }
    }

    /**
     * Called to update the application time.
     * @param dateTime Date/Time for the application clock.
     */
    public static void updateApplicationTime(final ZonedDateTime dateTime) {
        TimeProviderFactory.getInstance().setTime(dateTime);
    }
}
