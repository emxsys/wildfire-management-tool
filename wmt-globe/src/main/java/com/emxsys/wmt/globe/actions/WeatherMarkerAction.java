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
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.markers.MarkerSupport;
import com.emxsys.wmt.globe.markers.weather.WeatherMarker;
import com.emxsys.wmt.globe.markers.weather.WeatherMarkerEditor;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
@ActionID(category = "Markers", id = "com.emxsys.wmt.globe.actions.AddWeatherMarkerAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/globe/markers/weather/sun_clouds.png", displayName = "#CTL_AddWeatherMarkerAction")
@ActionReference(path = "Toolbars/Create", position = 300)
@RibbonActionReference(path = "Menu/Insert/Create",
        position = 300,
        priority = "top",
        description = "#CTL_AddWeatherMarkerAction_Hint",
        tooltipTitle = "#CTL_AddWeatherMarkerAction_TooltipTitle",
        tooltipBody = "#CTL_AddWeatherMarkerAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/markers/weather/sun_clouds32.png")
@Messages(
        {
            "CTL_AddWeatherMarkerAction=Weather Marker",
            "CTL_AddWeatherMarkerAction_Hint=Adds an Weather Marker to the map.",
            "CTL_AddWeatherMarkerAction_TooltipTitle=Create Weather Marker",
            "CTL_AddWeatherMarkerAction_TooltipBody=Create a new Weather marker at the center of the map.",
            "ERR_NoWeatherMarker_WorldWindNotFound=WorldWind Viewer not found.",
            "ERR_WeatherMarkerImageNotFound=Image not found",
            "ERR_WeatherMarkerManagerNotFound=A Marker.Renderer was not found in the viewer lookup.",
            "ERR_WeatherNullPosition=The gis viewer returned a null lat/lon for the center position."
        })
public final class WeatherMarkerAction implements ActionListener {

    private final Project context;
    private static final Logger logger = Logger.getLogger(WeatherMarkerAction.class.getName());

    /**
     * Constructor for a context-sensitive action that requires a Project.
     *
     * @param context
     */
    public WeatherMarkerAction(Project context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // We'll place the new marker at the center of the viewer
        Coord3D position = Globe.getInstance().getLocationAtCenter();
        if (position == null) {
            logger.severe(Bundle.ERR_WeatherNullPosition());
            return;
        }

        // Create and edit the marker
        WeatherMarker marker = new WeatherMarker("Wx Marker", position);
        boolean success = WeatherMarkerEditor.edit(marker, true);
        if (success) {
            // Save the pushpin into the current project 
            new WeatherMarker.Writer()
                    .marker(marker)
                    .folder(MarkerSupport.getFolderFromCurrentProject())
                    .write();
        }
    }
}
