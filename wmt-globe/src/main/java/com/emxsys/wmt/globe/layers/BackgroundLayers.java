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

import com.emxsys.gis.api.layer.MapLayerRegistration;
import com.emxsys.gis.api.layer.MapLayerRegistrations;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.wmt.globe.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwindx.sunlight.SunLayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@MapLayerRegistrations({
    @MapLayerRegistration(
            position = 10,
            name = "Earth",
            actuate = "onLoad",
            role = "Background",
            type = "Raster",
            category = "Satellite",
            displayName = "#CTL_Earth",
            instanceClass = "gov.nasa.worldwind.layers.Earth.BMNGOneImage",
            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
            factoryMethod = "createLayer"),
//    @MapLayerRegistration(
//            position = 100,
//            name = "Stars",
//            role = "Background",
//            type = "Other",
//            category = "Other",
//            actuate = "onLoad",
//            displayName = "#CTL_Stars",
//            instanceClass = "gov.nasa.worldwind.layers.StarsLayer",
//            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
//            factoryMethod = "createLayer"),
//    @MapLayerRegistration(
//            position = 200,
//            name = "Sky",
//            role = "Background",
//            type = "Other",
//            category = "Other",
//            actuate = "onLoad",
//            displayName = "#CTL_Sky",
//            instanceClass = "gov.nasa.worldwind.layers.SkyGradientLayer",
//            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
//            factoryMethod = "createLayer"),
//    @MapLayerRegistration(
//            position = 300,
//            name = "Sun",
//            actuate = "onLoad",
//            role = "Background",
//            type = "Other",
//            category = "Other",
//            displayName = "#CTL_Sun",
//            instanceClass = "gov.nasa.worldwindx.sunlight.SunLayer",
//            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
//            factoryMethod = "createLayer"),
})
@Messages({
    "CTL_Stars=Stars",
    "CTL_Sun=Sun", // Must match name used in Terramenata-Globe SunController.
    "CTL_Sky=Sky",
    "CTL_Earth=Earth",        
})
public class BackgroundLayers {

    public static String LAYER_STARS = Bundle.CTL_Stars();
    public static String LAYER_SUNLIGHT = Bundle.CTL_Sun();
    public static String LAYER_SKY = Bundle.CTL_Sky();
    public static String LAYER_EARTH = Bundle.CTL_Earth();

    public static List<GisLayer> getLayers() {
        ArrayList<GisLayer> list = new ArrayList<>();
        
        // Assign the default layers to the appropriate groups
        LayerList ll = Globe.getInstance().getWorldWindManager().getLayers();
        for (Layer layer : ll) {
            if (layer instanceof StarsLayer) {
                ll.remove(layer);
                list.add(new GisLayerProxy(layer, LAYER_STARS, BasicLayerGroup.Background));
            } else if (layer instanceof SunLayer) {
                ll.remove(layer);
                list.add(new GisLayerProxy(layer, LAYER_SUNLIGHT, BasicLayerGroup.Background));
            } else if (layer instanceof SkyGradientLayer) {
                ll.remove(layer);
                list.add(new GisLayerProxy(layer, LAYER_SKY, BasicLayerGroup.Background));
            }
        }
        
        FileObject layersFolder = FileUtil.getConfigFile("WorldWind/Layers/Background");
        if (layersFolder != null) {
            Collection<? extends Layer> layers = Lookups.forPath(layersFolder.getPath()).lookupAll(Layer.class);
            for (Layer layer : layers) {
                list.add(layer instanceof GisLayer ? (GisLayer) layer : new GisLayerProxy(layer));
            }
        }
        return list;
    }

    private BackgroundLayers() {
    }

}
