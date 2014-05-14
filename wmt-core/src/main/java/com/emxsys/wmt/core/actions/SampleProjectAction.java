/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.core.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * See reference in XML Layer:
 * <pre> {@code
 *     <folder name="WelcomePage">
 *        <folder name="GettingStartedLinks">
 *            . . .
 *            <file name="sampleproject.instance">
 *                <attr name="SystemFileSystem.localizingBundle" stringvalue="com.emxsys.wmt.core.welcome.content.Bundle"/>
 *                <attr name="instanceClass" stringvalue="com.emxsys.wmt.core.actions.SampleProjectAction"/>
 *                <attr name="instanceOf" stringvalue="javax.swing.Action"/>
 *                <attr name="position" intvalue="200"/>
 *           </file>
 *            . . .
 *        </folder>
 *     </folder>}
 * </pre>
 * @author Bruce Schubert
 */
@ActionID(
        category = "Help",
        id = "com.emxsys.wmt.core.actions.SampleProjectAction"
)
@ActionRegistration(
        displayName = "#CTL_SampleProjectAction"
)
@Messages("CTL_SampleProjectAction=Sample Project")
public final class SampleProjectAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(SampleProjectAction.class.getName());

    @Override
    public void actionPerformed(ActionEvent e) {
        logger.warning("SampleProjectAction behavior has not been implemented.");
        // TODO implement action body
    }
}
