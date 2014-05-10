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
package com.emxsys.wmt.globe.actions;

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.markers.MarkerPositioner;
import com.emxsys.wmt.globe.markers.MarkerSupport;
import com.emxsys.wmt.globe.markers.pushpins.Pushpin;
import com.emxsys.wmt.globe.markers.pushpins.Pushpin.Writer;
import com.emxsys.wmt.globe.markers.pushpins.PushpinEditor;
import com.terramenta.globe.WorldWindManager;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javafx.beans.value.WeakChangeListener;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;

/**
 * Creates a new Pushpin marker at the current view location and opens an editor on the new pushpin.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(category = "Markers", id = "com.emxsys.wmt.globe.actions.AddPushpinAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/globe/markers/pushpins/castshadow-green.png",
        displayName = "#CTL_AddPushpinAction")
@ActionReference(path = "Toolbars/Create", position = 200)
@RibbonActionReference(path = "Menu/Insert/Markers",
        position = 200,
        description = "#CTL_AddPushpinAction_Hint",
        priority = "top",
        tooltipTitle = "#CTL_AddPushpinAction_TooltipTitle",
        tooltipBody = "#CTL_AddPushpinAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/markers/pushpins/castshadow-black32.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages(
        {
            "CTL_AddPushpinAction=Pushpin",
            "CTL_AddPushpinAction_Hint=Adds a pushpin to the map.",
            "CTL_AddPushpinAction_TooltipTitle=Create Pushpin",
            "CTL_AddPushpinAction_TooltipBody=Create a new pushpin at the clicked location on the globe.",
            "ERR_NoPushpin_WorldWindNotFound=WorldWind Viewer not found.",
            "ERR_PushpinImageNotFound=Image not found",
            "ERR_PushpinManagerNotFound=A Marker.Renderer was not found in the viewer lookup.",
            "ERR_NullPosition=The GIS viewer returned a null lat/lon for the center position.",
            "ERR_NullPushpin=The DataObject does not have a Pushpin in its lookup."
        })
public final class AddPushpinAction implements ActionListener {

    private static final Logger logger = Logger.getLogger(AddPushpinAction.class.getName());
    private final Project context;
    private final WorldWindManager wwm = Globe.getInstance().getWorldWindManager();

    /**
     * Constructor for a context-sensitive action that requires a Project.
     * @param context
     */
    public AddPushpinAction(Project context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Create a pushpin in memory (not a DataObject)
        Pushpin pushpin = new Pushpin("Pushpin", GeoCoord3D.INVALID_POSITION);

        // Position the marker on the globe where clicked. The positioner is released on click or ESC.
        MarkerPositioner positioner = new MarkerPositioner(wwm.getWorldWindow(), pushpin);
        PropertyChangeListener listener = (PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals("armed") && evt.getNewValue().equals(false)) {
                if (positioner.isCanceled()) {
                    return;
                }
                // Launch the editor in the "new" pushpin mode
                boolean success = PushpinEditor.edit(pushpin, true);
                if (success) {
                    // Save the pushpin into the current project 
                    pushpin.getLookup().lookup(Writer.class)
                            .folder(MarkerSupport.getFolderFromCurrentProject())
                            .write();
                }
            }
        };
        positioner.addPropertyChangeListener(WeakListeners.propertyChange(listener, positioner));

        // Force keyboard focus to globe
        Globe.getInstance().getRendererComponent().requestFocusInWindow();
        // Invoke the positioner
        positioner.setArmed(true);
    }
}
