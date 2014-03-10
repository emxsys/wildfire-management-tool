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
package com.emxsys.wmt.globe.layers;

import com.emxsys.wmt.gis.api.layer.BasicLayerGroup;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.gis.api.layer.MapLayerRegistration;
import com.emxsys.wmt.gis.api.layer.MapLayerRegistrations;
import com.emxsys.wmt.globe.Globe;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 * This class registers all of the Widget layers and provides a mechanism for retrieving all of the
 * registered layers in the 'Widget' group.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@MapLayerRegistrations({
    @MapLayerRegistration(
            position = 10,
            name = "Reticule Overlay",
            role = "Widget",
            type = "Other",
            category = "Other",
            actuate = "onLoad",
            displayName = "#CTL_Reticule",
            instanceClass = "com.emxsys.wmt.globe.layers.ReticuleLayer",
            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
            factoryMethod = "createLayer"), //    @MapLayerRegistration(
//            position = 100,
//            name = "Compass Overlay",
//            role = "Widget",
//            type = "Other",
//            category = "Other",
//            actuate = "onLoad",
//            displayName = "#CTL_Compass",
//            instanceClass = "gov.nasa.worldwind.layers.CompassLayer",
//            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
//            factoryMethod = "createLayer"),
//    @MapLayerRegistration(
//            position = 200,
//            name = "View Controls Overlay",
//            role = "Widget",
//            type = "Other",
//            category = "Other",
//            actuate = "onLoad",
//            displayName = "#CTL_Controls",
//            instanceClass = "com.emxsys.wmt.globe.layers.FixedViewControlsLayer",
//            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
//            factoryMethod = "createLayer"),
//    @MapLayerRegistration(
//            position = 300,
//            name = "World Map Overlay",
//            role = "Widget",
//            type = "Other",
//            category = "Other",
//            actuate = "onLoad",
//            displayName = "#CTL_WorldMap",
//            instanceClass = "gov.nasa.worldwind.layers.WorldMapLayer",
//            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
//            factoryMethod = "createLayer"),
//    @MapLayerRegistration(
//            position = 400,
//            name = "Scalebar Overlay",
//            role = "Widget",
//            type = "Other",
//            category = "Other",
//            actuate = "onLoad",
//            displayName = "#CTL_Scalebar",
//            instanceClass = "gov.nasa.worldwind.layers.ScalebarLayer",
//            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
//            factoryMethod = "createLayer"),
})
@Messages({
    "CTL_Compass=Compass Overlay",
    "CTL_Controls=Controls Overlay",
    "CTL_Reticule=Crosshairs Overlay",
    "CTL_Scalebar=Scalebar Overlay",
    "CTL_WorldMap=World Map Overlay",})
public class WidgetLayers {

    public static String LAYER_COMPASS = Bundle.CTL_Compass();
    public static String LAYER_CONTROLS = Bundle.CTL_Controls();
    public static String LAYER_RETICULE = Bundle.CTL_Reticule();
    public static String LAYER_SCALEBAR = Bundle.CTL_Scalebar();
    public static String LAYER_WORLDMAP = Bundle.CTL_WorldMap();

    public static List<GisLayer> getLayers() {
        ArrayList<GisLayer> list = new ArrayList<>();
        // Assign the default layers to the appropriate groups
        LayerList ll = Globe.getInstance().getWorldWindManager().getLayers();
        for (Layer layer : ll) {
            if (layer instanceof CompassLayer) {
                ll.remove(layer);
                list.add(new GisLayerAdaptor(layer, LAYER_COMPASS, BasicLayerGroup.Widget));
            } else if (layer instanceof WorldMapLayer) {
                ll.remove(layer);
                list.add(new GisLayerAdaptor(layer, LAYER_WORLDMAP, BasicLayerGroup.Widget));
            } else if (layer instanceof ViewControlsLayer) {
                ll.remove(layer);
                list.add(new GisLayerAdaptor(layer, LAYER_CONTROLS, BasicLayerGroup.Widget));
            } else if (layer instanceof ScalebarLayer) {
                ll.remove(layer);
                list.add(new GisLayerAdaptor(layer, LAYER_SCALEBAR, BasicLayerGroup.Widget));
            }
        }

        FileObject layersFolder = FileUtil.getConfigFile("WorldWind/Layers/Widget");
        if (layersFolder != null) {
            Collection<? extends Layer> layers = Lookups.forPath(layersFolder.getPath()).lookupAll(Layer.class);
            for (Layer layer : layers) {
                list.add(layer instanceof GisLayer ? (GisLayer) layer : new GisLayerAdaptor(layer));
            }
        }
        return list;
    }

    private WidgetLayers() {
    }

}
