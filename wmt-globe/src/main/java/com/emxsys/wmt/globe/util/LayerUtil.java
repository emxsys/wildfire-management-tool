/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.util;

import com.emxsys.wmt.globe.layers.WmsLayerInfo;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.util.ModuleUtil;
import com.emxsys.wmt.globe.layers.GisLayerAdaptor;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * This utility class provided convenient methods for discovering WorldWind layers from the XML
 * layer filesystem (i.e., the NetBeans XML layer).
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: LayerUtil.java 628 2013-05-12 19:58:52Z bdschubert $
 */
public class LayerUtil {

    private static final Logger logger = Logger.getLogger(LayerUtil.class.getName());

    private LayerUtil() {
    }

    /**
     * Return a collection of layers belonging to a category.
     *
     * @param category a textual grouping attribute assigned to the layer.
     * @return layers in the given category
     */
    public static List<Layer> getLayersInCategory(String category) {
        LinkedHashMap<String, ArrayList<Layer>> hashMap = createLayerCategoryMap();
        return hashMap.get(category);
    }

    /**
     * Return a collection of layers belonging to a category.
     *
     * @param category a textual grouping attribute assigned to the layer.
     * @return layers in the given category
     */
    public static List<GisLayer> getGisLayersInCategory(String category) {
        LinkedHashMap<String, ArrayList<Layer>> hashMap = createLayerCategoryMap();
        ArrayList<Layer> layers = hashMap.get(category);
        ArrayList<GisLayer> gisLayers = new ArrayList<>(layers.size());
        for (Layer layer : layers) {
            gisLayers.add(new GisLayerAdaptor(layer));
        }
        return gisLayers;
    }

    public static List<GisLayer> getGisLayersInRole(String role) {
        LinkedHashMap<String, ArrayList<Layer>> hashMap = createLayerRoleMap();
        ArrayList<Layer> wwLayers = hashMap.get(role);
        ArrayList<GisLayer> gisLayers = new ArrayList<>(wwLayers.size());
        for (Layer layer : wwLayers) {
            gisLayers.add(new GisLayerAdaptor(layer));
        }
        return gisLayers;
    }

    /**
     * Determines if a layer is contained in a given category.
     *
     * @param category A category defined in the XML Layer: "WorldWind/Layers".
     * @param layerDisplayName The display name assigned to a WorldWind Layer.
     * @return true if the category contains a WorldWind Layer named displayName.
     */
    public static boolean contains(String category, String layerDisplayName) {
        List<Layer> layers = getLayersInCategory(category);
        for (Layer layer : layers) {
            if (layer.getName().equals(layerDisplayName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the collection of layers mapped to each category.
     *
     * @return Layers mapped to categories.
     */
    private static LinkedHashMap<String, ArrayList<Layer>> createLayerCategoryMap() {
        LinkedHashMap<String, ArrayList<Layer>> map = new LinkedHashMap<>();

        List<FileObject> children = ModuleUtil.getSortedChildren("WorldWind/Layers");
        for (FileObject fo : children) {
            // Get the Layer that will be added to the category collection
            Layer layer = getLayer(fo);
            if (layer == null) {
                continue;
            }

            String categoryName = (String) fo.getAttribute("category");

            // Provide a default category if one was not assigned
            if (categoryName == null) {
                categoryName = "Other";
            } // Ignore those that are explicitly hidden
            else if (categoryName.equalsIgnoreCase("Hidden")) {
                continue;
            }

            // Create the layer list container for each new category
            if (!map.containsKey(categoryName)) {
                map.put(categoryName, new ArrayList<Layer>());
            }

            // Save the layer in its category
            map.get(categoryName).add(layer);
        }
        return map;
    }

    /**
     * Return the collection of layers mapped to each category.
     *
     * @return Layers mapped to categories.
     */
    private static LinkedHashMap<String, ArrayList<Layer>> createLayerRoleMap() {
        LinkedHashMap<String, ArrayList<Layer>> map = new LinkedHashMap<>();

        List<FileObject> children = ModuleUtil.getSortedChildren("WorldWind/Layers");
        for (FileObject fo : children) {
            // Get the Layer that will be added to the category collection
            Layer layer = getLayer(fo);
            if (layer == null) {
                continue;
            }

            String roleName = (String) fo.getAttribute("role");

            // Provide a default role if one was not assigned
            if (roleName == null) {
                roleName = "Other";
            } // Ignore those that are explicitly hidden
            else if (roleName.equalsIgnoreCase("Hidden")) {
                continue;
            }

            // Create the layer list container for each new category
            if (!map.containsKey(roleName)) {
                map.put(roleName, new ArrayList<Layer>());
            }

            // Save the layer in its grouping
            map.get(roleName).add(layer);
        }
        return map;
    }

    /**
     * Instantiates a WorldWind Layer from a FileObject.
     *
     * @param fo a configuration file specified in "WorldWind/Layers".
     * @return a WorldWind Layer.
     */
    private static Layer getLayer(FileObject fo) {
        try {
            DataObject ob;
            ob = DataObject.find(fo);
            //InstanceCookie ck = ob.getLookup().lookup(InstanceCookie.class);
            InstanceCookie ck = ob.getLookup().lookup(InstanceCookie.class);
            if (ck != null) {
                return (Layer) ck.instanceCreate();
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException | ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     *
     * @param serverUri accessed to retrieve layer inforamtion
     * @return Set of WMS layer information objects
     */
    public static Set<WmsLayerInfo> getLayerInfoFromServer(URI serverUri) {
        WMSCapabilities caps;
        TreeSet<WmsLayerInfo> layerInfos = new TreeSet<>(new Comparator<WmsLayerInfo>() {
            @Override
            public int compare(WmsLayerInfo infoA, WmsLayerInfo infoB) {
                String nameA = infoA.getName();
                String nameB = infoB.getName();
                return nameA.compareTo(nameB);
            }
        });

        try {
            caps = WMSCapabilities.retrieve(serverUri);
            caps.parse();
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return layerInfos;
        }

        // Gather up all the named layers and make a world wind layer for each.
        final List<WMSLayerCapabilities> namedLayerCaps = caps.getNamedLayers();
        if (namedLayerCaps == null) {
            return layerInfos;
        }

        try {
            for (WMSLayerCapabilities lc : namedLayerCaps) {
                Set<WMSLayerStyle> styles = lc.getStyles();
                if (styles == null || styles.isEmpty()) {
                    WmsLayerInfo layerInfo = new WmsLayerInfo(caps, lc, null);
                    layerInfos.add(layerInfo);
                } else {
                    for (WMSLayerStyle style : styles) {
                        WmsLayerInfo layerInfo = new WmsLayerInfo(caps, lc, style);
                        layerInfos.add(layerInfo);
                    }
                }
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        return layerInfos;

    }

}
