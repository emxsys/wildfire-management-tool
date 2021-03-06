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

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.markers.MarkerPositioner;
import com.emxsys.wmt.globe.markers.MarkerSupport;
import com.emxsys.wmt.globe.markers.ics.IcsMarker;
import com.emxsys.wmt.globe.markers.ics.IcsMarker.Writer;
import com.emxsys.wmt.globe.markers.ics.IcsMarkerEditor;
import com.terramenta.globe.WorldWindManager;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Bruce Schubert
 */
@ActionID(category = "Markers", id = "com.emxsys.wmt.globe.actions.AddIcsMarkerAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/globe/markers/ics/Fire_Origin.png", displayName = "#CTL_AddICSMarkerAction")
@ActionReference(path = "Toolbars/Create", position = 2300)
@RibbonActionReference(path = "Ribbon/TaskPanes/Insert/Markers", position = 300,
        priority = "top",
        description = "#CTL_AddICSMarkerAction_Hint",
        tooltipTitle = "#CTL_AddICSMarkerAction_TooltipTitle",
        tooltipBody = "#CTL_AddICSMarkerAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/markers/ics/Fire_Origin32.png")
@Messages(
        {
            "CTL_AddICSMarkerAction=ICS Marker",
            "CTL_AddICSMarkerAction_Hint=Adds an ICS Marker to the map.",
            "CTL_AddICSMarkerAction_TooltipTitle=Create ICS Marker",
            "CTL_AddICSMarkerAction_TooltipBody=Create a new ICS marker at the location clicked on the globe.",
            "ERR_NoICSMarker_WorldWindNotFound=WorldWind Viewer not found.",
            "ERR_ICSMarkerImageNotFound=Image not found",
            "ERR_ICSMarkerManagerNotFound=A Marker.Renderer was not found in the viewer lookup.",
            "ERR_ICSNullPosition=The gis viewer returned a null lat/lon for the center position."
        })
public final class AddIcsMarkerAction implements ActionListener {

    private final Project context;
    private static final WorldWindManager wwm = Globe.getInstance().getWorldWindManager();
    private static final Logger logger = Logger.getLogger(AddIcsMarkerAction.class.getName());

    /**
     * Constructor for a context-sensitive action that requires a Project.
     *
     * @param context
     */
    public AddIcsMarkerAction(Project context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // We'll place the new marker at the center of the viewer
        Coord3D position = Globe.getInstance().getLocationAtCenter();
        if (position == null) {
            logger.severe(Bundle.ERR_ICSNullPosition());
            return;
        }
        IcsMarker marker = new IcsMarker("Marker", GeoCoord3D.INVALID_COORD);
        // Position the marker on the globe
        MarkerPositioner positioner = new MarkerPositioner(wwm.getWorldWindow(), marker);
        positioner.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals("armed") && evt.getNewValue().equals(false)) {
                if (positioner.isCanceled()) {
                    return;
                }
                // Launch the editor in the "new" marker mode
                boolean success = IcsMarkerEditor.edit(marker, true);
                if (success) {
                    // Save the marker into the current project 
                    marker.getLookup().lookup(Writer.class)
                            .folder(MarkerSupport.getFolderFromCurrentProject())
                            .write();
                }
            }
        });
        // Force keyboard focus to globe
        ((Component) this.wwm.getWorldWindow()).requestFocusInWindow();
        // Invoke the positioner
        positioner.setArmed(true);
    }
}
