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
package com.emxsys.wmt.core.actions;

import com.emxsys.wmt.ribbon.RibbonActionReference;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


@ActionID(
    category = "Tools",
          id = "com.emxsys.wmt.core.actions.RegisterSoftwareAction")
@ActionRegistration(
    iconBase = "images/mail_edit.png",
                    displayName = "#CTL_RegisterSoftwareAction")
@ActionReferences(
{
    @ActionReference(path = "Menu/Help", position = 1600),
})
@RibbonActionReference(path = "Ribbon/TaskPanes/Tools/Launch", position = 200,
                       tooltipTitle = "#CTL_RegisterSoftwareAction_TooltipTitle",
                       tooltipBody = "#CTL_RegisterSoftwareAction_TooltipBody",
                       tooltipIcon = "images/mail_edit32.png",
                       tooltipFooter = "com.emxsys.wmt.core.Bundle#CTL_Default_TooltipFooter",
                       tooltipFooterIcon = "images/help.png")
@Messages(
{
    "CTL_RegisterSoftwareAction=Register Software",
    "CTL_RegisterSoftwareAction_Hint=Register by subscribing to the users mailing list. (ALT-O)",
    "CTL_RegisterSoftwareAction_TooltipTitle=Register Software",
    "CTL_RegisterSoftwareAction_TooltipBody=Register your software by subscribing to the users mailing list.\n"
    + "You can subscribe or unsubscribe at any time by sending an email:\n"
    + "To: sympa@emxsys.java.net\n"
    + "Subject: subscribe users\n"
    + "or Subject: unsubscribe users",
    "CTL_RegisterSoftwareDialogTitle=Register Your Software",
    "CTL_RegisterSoftwareDialogMsg=When you register you subscribe you to an exclusive, "
    + "private mailing list for users.  You'll be notified of new updates and important "
    + "work arounds to problems.  This is a moderated list with low traffic and no spam.  "
    + "Your email address will not be shared.\n\n"
    + "Please subscribe -- it's important that you stay current with new software developments.\n"
    + "You can unsubscribe at any time.",
    "CTL_RegisterSoftwareDialogSubscribe=Subscribe",
    "CTL_RegisterSoftwareDialogUnsubscribe=Unsubscribe",
    "CTL_RegisterSoftwareDialogCancel=Cancel",
    "ERR_CannotLaunchEmail=Cannot launch your email client"
})
public final class RegisterSoftwareAction implements ActionListener
{

    private final String SUBSCRIBE_TO_USERS = "mailto:sympa@emxsys.java.net?subject=subscribe%20users";
    private final String UNSUBSCRIBE_TO_USERS = "mailto:sympa@emxsys.java.net?subject=unsubscribe%20users";
    private final String HELP = "mailto:sympa@emxsys.java.net?subject=help";
    private static final Logger logger = Logger.getLogger(RegisterSoftwareAction.class.getName());
    private final Object SUBSCRIBE_OPTION = Bundle.CTL_RegisterSoftwareDialogSubscribe();
    private final Object UNSUBSCRIBE_OPTION = Bundle.CTL_RegisterSoftwareDialogUnsubscribe();
    private final Object CANCEL_OPTION = Bundle.CTL_RegisterSoftwareDialogCancel();


    @Override
    public void actionPerformed(ActionEvent e)
    {
        // Display a Subscribe/Unsubscribe/Cancel dialog regarding registration
        NotifyDescriptor dialog = new NotifyDescriptor(
            Bundle.CTL_RegisterSoftwareDialogMsg(),
            Bundle.CTL_RegisterSoftwareDialogTitle(),
            NotifyDescriptor.YES_NO_CANCEL_OPTION,
            NotifyDescriptor.INFORMATION_MESSAGE,
            new Object[]
            {
                SUBSCRIBE_OPTION,
                UNSUBSCRIBE_OPTION,
                CANCEL_OPTION
            },
            SUBSCRIBE_OPTION);
        Object result = DialogDisplayer.getDefault().notify(dialog);
        try
        {
            // Open the default email client with prepopulated URI content.
            if (result.equals(SUBSCRIBE_OPTION))
            {
                Desktop.getDesktop().mail(new URI(SUBSCRIBE_TO_USERS));
            }
            else if (result.equals(UNSUBSCRIBE_OPTION))
            {
                Desktop.getDesktop().mail(new URI(UNSUBSCRIBE_TO_USERS));
            }
        }
        catch (IOException exception)
        {
            logger.severe("Cannot launch mail client: " + exception.getMessage());
            Message message = new NotifyDescriptor.Message(Bundle.CTL_RegisterSoftwareAction_TooltipBody());
            message.setTitle(Bundle.ERR_CannotLaunchEmail());
            DialogDisplayer.getDefault().notify(message);
        }
        catch (URISyntaxException exception)
        {
            logger.severe("Bad mailTo URI: " + exception.getInput());
        }
        catch (Exception exception)
        {
            logger.severe(exception.getMessage());
        }
    }
}
