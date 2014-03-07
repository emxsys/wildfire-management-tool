/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwind.util.WWUtil;
import java.util.Set;

/**
 *
 * @author Bruce Schubert
 * @version $Id$
 */
public class WmsLayerInfo {

    WMSCapabilities caps;
    AVListImpl params = new AVListImpl();

    public WmsLayerInfo(WMSCapabilities caps, WMSLayerCapabilities layerCaps, WMSLayerStyle style) {
        this.caps = caps;
        // Create the layer info specified by the layer's capabilities entry and the selected style.
        this.params = new AVListImpl();
        this.params.setValue(AVKey.LAYER_NAMES, layerCaps.getName());
        if (style != null) {
            this.params.setValue(AVKey.STYLE_NAMES, style.getName());
        }
        String abs = layerCaps.getLayerAbstract();
        if (!WWUtil.isEmpty(abs)) {
            this.params.setValue(AVKey.LAYER_ABSTRACT, abs);
        }
        this.params.setValue(AVKey.DISPLAY_NAME, makeTitle());
    }

    private String makeTitle() {
        String layerNames = this.params.getStringValue(AVKey.LAYER_NAMES);
        String styleNames = this.params.getStringValue(AVKey.STYLE_NAMES);
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            String layerName = lNames[i];
            WMSLayerCapabilities lc = caps.getLayerByName(layerName);
            String layerTitle = lc.getTitle();
            sb.append(layerTitle != null ? layerTitle : layerName);
            if (sNames == null || sNames.length <= i) {
                continue;
            }
            String styleName = sNames[i];
            WMSLayerStyle style = lc.getStyleByName(styleName);
            if (style == null) {
                continue;
            }
            sb.append(" : ");
            String styleTitle = style.getTitle();
            sb.append(styleTitle != null ? styleTitle : styleName);
        }
        return sb.toString();
    }

    public String getTitle() {
        return params.getStringValue(AVKey.DISPLAY_NAME);
    }

    public String getName() {
        return params.getStringValue(AVKey.LAYER_NAMES);
    }

    public String getAbstract() {
        return params.getStringValue(AVKey.LAYER_ABSTRACT);
    }

    /**
     * Creates a WMS Layer or an Elevation Model.
     *
     * @return
     */
    public Object createComponent() {
        AVList configParams = params.copy(); // Copy to insulate changes from the caller.
        // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
        configParams.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
        configParams.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        configParams.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);
        try {
            String factoryKey = getFactoryKeyForCapabilities(caps);
            Factory factory = (Factory) WorldWind.createConfigurationComponent(factoryKey);
            return factory.createFromConfigSource(caps, configParams);
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Ignore the exception, and just return null.
        }
        return null;
    }

    private static String getFactoryKeyForCapabilities(WMSCapabilities caps) {
        boolean hasApplicationBilFormat = false;
        Set<String> formats = caps.getImageFormats();
        for (String s : formats) {
            if (s.contains("application/bil")) {
                hasApplicationBilFormat = true;
                break;
            }
        }
        return hasApplicationBilFormat ? AVKey.ELEVATION_MODEL_FACTORY : AVKey.LAYER_FACTORY;
    }

}
