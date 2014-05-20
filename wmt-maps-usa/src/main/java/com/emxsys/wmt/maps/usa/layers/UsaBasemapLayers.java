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
package com.emxsys.wmt.maps.usa.layers;

import com.emxsys.gis.api.layer.MapLayerRegistration;
import com.emxsys.gis.api.layer.MapLayerRegistrations;

import org.openide.util.NbBundle.Messages;

/**
 * Registers the USA Base Map layers.
 * 
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@MapLayerRegistrations(
        {
            @MapLayerRegistration(
                    position = 1000,
                    name = "USDA NAIP",
                    role = "Basemap",
                    type = "Raster",
                    category = "Aerial",
                    actuate = "onLoad",
                    displayName = "#CTL_USDANAIP",
                    config = "nbres:/worldwind/config/USA/USDANAIPWMSImageLayer.xml",
                    instanceClass = "gov.nasa.worldwind.layers.Layer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),
            @MapLayerRegistration(
                    position = 1100,
                    name = "USGS Topo Low Res",
                    role = "Basemap",
                    type = "Raster",
                    category = "Topographic",
                    actuate = "onRequest",
                    displayName = "#CTL_USGSTopoLowRes",
                    config = "nbres:/worldwind/config/USA/USGSTopoLowResLayer.xml",
                    instanceClass = "gov.nasa.worldwind.layers.Layer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),
            @MapLayerRegistration(
                    position = 1200,
                    name = "USGS Topo Med Res",
                    role = "Basemap",
                    type = "Raster",
                    category = "Topographic",
                    actuate = "onRequest",
                    displayName = "#CTL_USGSTopoMedRes",
                    config = "nbres:/worldwind/config/USA/USGSTopoMedResLayer.xml",
                    instanceClass = "gov.nasa.worldwind.layers.Layer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),
            @MapLayerRegistration(
                    position = 1300,
                    name = "USGS Topo High Res",
                    role = "Basemap",
                    type = "Raster",
                    category = "Topographic",
                    actuate = "onRequest",
                    displayName = "#CTL_USGSTopoHighRes",
                    config = "nbres:/worldwind/config/USA/USGSTopoHighResLayer.xml",
                    instanceClass = "gov.nasa.worldwind.layers.Layer",
                    factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
                    factoryMethod = "createLayer"),})
@Messages({
    "CTL_USDANAIP=USDA NAIP",
    "CTL_USGSTopoLowRes=USGS 250k Topographic",
    "CTL_USGSTopoMedRes=USGS 100k Topographic",
    "CTL_USGSTopoHighRes=USGS 24k Topographic"
})
public class UsaBasemapLayers {

    public static String LAYER_USDA_NAIP = Bundle.CTL_USDANAIP();
    public static String LAYER_USGS_TOPO_LOW_RES = Bundle.CTL_USGSTopoLowRes();
    public static String LAYER_USGS_TOPO_MED_RES = Bundle.CTL_USGSTopoMedRes();
    public static String LAYER_USGS_TOPO_HIGH_RES = Bundle.CTL_USGSTopoHighRes();

    private UsaBasemapLayers() {

    }

}
