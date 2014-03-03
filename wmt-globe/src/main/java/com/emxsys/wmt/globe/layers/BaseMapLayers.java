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

import com.emxsys.wmt.gis.api.layer.MapLayerRegistration;
import com.emxsys.wmt.gis.api.layer.MapLayerRegistrations;
import com.emxsys.wmt.gis.api.layer.BasicLayerCategory;
import com.emxsys.wmt.gis.api.layer.BasicLayerGroup;
import com.emxsys.wmt.gis.api.layer.BasicLayerType;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import gov.nasa.worldwind.layers.Layer;
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
@MapLayerRegistrations(
        {
            @MapLayerRegistration(
                    position = 50,
                    name = "Blue Marble",
                    actuate = "onLoad",
                    role = "Basemap",
                    type = "Raster",
                    category = "Satellite",
                    displayName = "#CTL_BlueMarble",
                    config = "nbres:/config/Earth/BMNGWMSLayer.xml",
                    instanceClass = "gov.nasa.worldwind.layers.Layer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),
            @MapLayerRegistration(
                    position = 100,
                    name = "i3 Landsat",
                    role = "Basemap",
                    type = "Raster",
                    category = "Satellite",
                    actuate = "onLoad",
                    displayName = "#CTL_Landsat",
                    config = "nbres:/config/Earth/LandsatI3WMSLayer.xml",
                    instanceClass = "gov.nasa.worldwind.layers.Layer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),
            @MapLayerRegistration(
                    position = 300,
                    name = "Bing Aerial",
                    role = "Basemap",
                    type = "Raster",
                    category = "Aerial",
                    actuate = "onRequest",
                    displayName = "#CTL_Bing",
                    config = "nbres:/config/Earth/BingImagery.xml",
                    instanceClass = "gov.nasa.worldwind.layers.Layer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),
            @MapLayerRegistration(
                    position = 400,
                    name = "MS Virtual Earth Hybrid",
                    role = "Basemap",
                    type = "Raster",
                    category = "Hybrid",
                    actuate = "onRequest",
                    displayName = "#CTL_MSVirtualEarthHybrid",
                    config = "nbres:/config/Earth/MSVirtualEarthHybridLayer.xml",
                    instanceClass = "gov.nasa.worldwind.layers.Layer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),
            @MapLayerRegistration(
                    position = 500,
                    name = "MS Virtual Earth Aerial",
                    role = "Basemap",
                    type = "Raster",
                    category = "Aerial",
                    actuate = "onRequest",
                    displayName = "#CTL_MSVirtualEarthAerial",
                    config = "nbres:/config/Earth/MSVirtualEarthAerialLayer.xml",
                    instanceClass = "gov.nasa.worldwind.layers.Layer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),
            @MapLayerRegistration(
                    position = 600,
                    name = "MS Virtual Earth Road",
                    role = "Basemap",
                    type = "Raster",
                    category = "Street",
                    actuate = "onRequest",
                    displayName = "#CTL_MSVirtualEarthRoad",
                    config = "nbres:/config/Earth/MSVirtualEarthRoadsLayer.xml",
                    instanceClass = "gov.nasa.worldwind.layers.Layer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),
            @MapLayerRegistration(
                    position = 700,
                    name = "Open Street Map",
                    role = "Basemap",
                    type = "Raster",
                    category = "Street",
                    actuate = "onRequest",
                    displayName = "#CTL_OpenStreetMap",
                    instanceClass = "gov.nasa.worldwind.layers.Earth.OSMMapnikLayer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),
        })
@Messages(
        {
            "CTL_BlueMarble=Blue Marble",
            "CTL_Landsat=i-cubed Landsat",
            "CTL_Bing=Bing Maps - Aerial",
            "CTL_MSVirtualEarthHybrid=MS Virtual Earth - Hybrid",
            "CTL_MSVirtualEarthAerial=MS Virtual Earth - Aerial",
            "CTL_MSVirtualEarthRoad=MS Virtual Earth - Road",
            "CTL_OpenStreetMap=Open Street Map"
        })
public class BaseMapLayers
{

    public static String LAYER_BLUE_MARBLE = Bundle.CTL_BlueMarble();
    public static String LAYER_LANDSAT = Bundle.CTL_Landsat();
    public static String LAYER_BING = Bundle.CTL_Bing();
    public static String LAYER_MSVE_AERIAL = Bundle.CTL_MSVirtualEarthAerial();
    public static String LAYER_MSVE_ROAD = Bundle.CTL_MSVirtualEarthRoad();
    public static String LAYER_MSVE_HYBRID = Bundle.CTL_MSVirtualEarthHybrid();
    public static String LAYER_OPEN_STREET_MAP = Bundle.CTL_OpenStreetMap();

    public static List<GisLayer> getLayers()
    {
        ArrayList<GisLayer> list = new ArrayList<>();

        list.add(new GisLayerAdaptor(new DummyLayer(BasicLayerGroup.Basemap),
                BasicLayerType.Other, BasicLayerGroup.Basemap, BasicLayerCategory.Other));

        FileObject layersFolder = FileUtil.getConfigFile("WorldWind/Layers/Basemap");
        Collection<? extends Layer> layers = Lookups.forPath(layersFolder.getPath()).lookupAll(Layer.class);
        for (Layer layer : layers)
        {
            list.add(layer instanceof GisLayer ? (GisLayer) layer : new GisLayerAdaptor(layer));
        }
        return list;
    }

    private BaseMapLayers()
    {
    }

}
