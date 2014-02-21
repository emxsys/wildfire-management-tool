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

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;


/**
 * This abstract class enables or disables the action based on there being open projects.
 * The Lookup context is ignored. Instead, the class listens for changes in the OpenProjects
 * object and enables the action when there is one or more projects in its container.
 * 
 * @see OpenProjects
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: AbstractProjectContextAction.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public abstract class AbstractProjectContextAction extends AbstractAction implements
    ContextAwareAction,
    PropertyChangeListener
{

    protected AbstractProjectContextAction(Lookup ignored)
    {
        OpenProjects source = OpenProjects.getDefault();
        source.addPropertyChangeListener(WeakListeners.propertyChange(this, source));        
        // Set the initial state
        propertyChange(null);
    }


    @Override
    public void setEnabled(final boolean enabled)
    {
        if (!EventQueue.isDispatchThread())
        {
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    setEnabled(enabled);
                }
            });
        }
        else
        {
            super.setEnabled(enabled);
        }
    }


    @Override
    public boolean isEnabled()
    {
        return super.isEnabled();
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        setEnabled(OpenProjects.getDefault().getOpenProjects().length > 0);
    }
}
