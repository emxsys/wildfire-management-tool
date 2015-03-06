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

import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.layer.MapLayerRegistration;
import com.emxsys.gis.api.layer.MapLayerRegistrations;
import gov.nasa.worldwind.layers.Layer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 * This class registers all of the base map layers and provides a mechanism for retrieving all of
 * the registered layers in the 'Basemap' group.
 * <p>
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@MapLayerRegistrations({
    @MapLayerRegistration(
            position = 1,
            name = "Shapes",
            actuate = "onLoad",
            role = "Overlay",
            type = "Vector",
            category = "Other",
            displayName = "#CTL_Shapes",
            instanceClass = "com.emxsys.wmt.globe.layers.ShapesLayer",
            factoryClass = "com.emxsys.wmt.globe.layers.ShapesLayer",
            factoryMethod = "newInstance"),
    @MapLayerRegistration(
            position = 10,
            name = "Markers",
            actuate = "onLoad",
            role = "Overlay",
            type = "Vector",
            category = "Other",
            displayName = "#CTL_Markers",
            instanceClass = "com.emxsys.wmt.globe.layers.MarkerLayer",
            factoryClass = "com.emxsys.wmt.globe.layers.MarkerLayer",
            factoryMethod = "newInstance"),
    @MapLayerRegistration(
            position = 20,
            name = "Symbols",
            actuate = "onLoad",
            role = "Overlay",
            type = "Vector",
            category = "Other",
            displayName = "#CTL_Symbols",
            instanceClass = "com.emxsys.wmt.globe.layers.SymbolLayer",
            factoryClass = "com.emxsys.wmt.globe.layers.SymbolLayer",
            factoryMethod = "newInstance"),
    @MapLayerRegistration(
            position = 100,
            name = "Borders",
            actuate = "onRequest",
            role = "Overlay",
            type = "Vector",
            category = "Other",
            displayName = "#CTL_CountryBoundaries",
            instanceClass = "gov.nasa.worldwind.layers.Earth.CountryBoundariesLayer",
            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
            factoryMethod = "createLayer"),
    @MapLayerRegistration(
            position = 200,
            name = "LatLon Graticule",
            actuate = "onRequest",
            role = "Overlay",
            type = "Vector",
            category = "Other",
            displayName = "#CTL_LatLonGraticule",
            instanceClass = "gov.nasa.worldwind.layers.LatLonGraticuleLayer",
            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
            factoryMethod = "createLayer"),
    @MapLayerRegistration(
            position = 300,
            name = "UTM Graticule",
            actuate = "onRequest",
            role = "Overlay",
            type = "Vector",
            category = "Other",
            displayName = "#CTL_UTMGraticule",
            instanceClass = "gov.nasa.worldwind.layers.Earth.UTMGraticuleLayer",
            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
            factoryMethod = "createLayer"),
    @MapLayerRegistration(
            position = 400,
            name = "MGRS Graticule",
            actuate = "onRequest",
            role = "Overlay",
            type = "Vector",
            category = "Other",
            displayName = "#CTL_MGRSGraticule",
            instanceClass = "gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer",
            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
            factoryMethod = "createLayer"),
    @MapLayerRegistration(
            position = 1000,
            name = "Place names",
            role = "Overlay",
            type = "Other",
            category = "Other",
            actuate = "onLoad",
            displayName = "#CTL_PlaceNames",
            instanceClass = "gov.nasa.worldwind.layers.Earth.NASAWFSPlaceNameLayer",
            factoryClass = "com.emxsys.wmt.globe.layers.LayerFactory",
            factoryMethod = "createLayer"),})
@Messages({
    "CTL_Shapes=Shapes",
    "CTL_Markers=Markers",
    "CTL_Symbols=Symbols",
    "CTL_CountryBoundaries=Country Boundaries",
    "CTL_LatLonGraticule=Lat/Lon Graticule",
    "CTL_MGRSGraticule=MGRS Graticule",
    "CTL_UTMGraticule=UTM Graticule",
    "CTL_PlaceNames=Place Names",})
public class OverlayLayers {

    public static String LAYER_SHAPES = Bundle.CTL_Shapes();
    public static String LAYER_MARKERS = Bundle.CTL_Markers();
    public static String LAYER_SYMBOLS = Bundle.CTL_Symbols();
    public static String LAYER_COUNTRY_BOUNDARIES = Bundle.CTL_CountryBoundaries();
    public static String LAYER_LATLON_GRATICULE = Bundle.CTL_LatLonGraticule();
    public static String LAYER_UTM_GRATICULE = Bundle.CTL_UTMGraticule();
    public static String LAYER_MGRS_GRATICULE = Bundle.CTL_MGRSGraticule();
    public static String LAYER_PLACE_NAMES = Bundle.CTL_PlaceNames();

    private static final Logger logger = Logger.getLogger(OverlayLayers.class.getName());

    public static List<GisLayer> getLayers() {

        ArrayList<GisLayer> list = new ArrayList<>();   // return value
        // Process the layers; there are three types:
        // 1) Native WorldWind Layers
        // 2) GisLayers with a Layer super class
        // 3) GisLayers with a Layer aggregate (in the lookup)
        FileObject layersFolder = FileUtil.getConfigFile("WorldWind/Layers/Overlay");
        if (layersFolder != null) {
            // Lookup the classes in the Layer class hierarchy
            Collection<? extends Layer> wwLayers = Lookups.forPath(layersFolder.getPath()).lookupAll(Layer.class);
            wwLayers.stream().forEach((wwLayer) -> {
                list.add(wwLayer instanceof GisLayer ? (GisLayer) wwLayer : new GisLayerProxy(wwLayer));
            });
//            // Lookup the GISLayer with an aggregate Layer object
//            Collection<? extends GisLayer> gisLayers = Lookups.forPath(layersFolder.getPath()).lookupAll(GisLayer.class);
//            gisLayers.stream().forEach((gisLayer) -> {
//                if (!(gisLayer instanceof Layer) && gisLayer.getLookup().lookup(Layer.class) != null) {
//                    list.add(gisLayer);
//                } else {
//                    logger.log(Level.SEVERE, "GisLayer [{0}] was not initialized because it is not a Layer nor has a Layer.", gisLayer.getName());
//                }
//            });
        }
        return list;
    }

    private OverlayLayers() {
    }

}
