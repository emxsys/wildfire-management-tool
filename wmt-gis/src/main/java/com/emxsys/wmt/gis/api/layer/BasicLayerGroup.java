/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.gis.api.layer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BasicLayerGroup defines some common roles of a GisLayer. Roles may be used for the general
 * ordering layers within the viewer.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: BasicLayerGroup.java 769 2013-06-20 18:11:51Z bdschubert $
 */
public enum BasicLayerGroup implements LayerGroup {

    /**
     * Low resolution background layers for the globe, plus atmosphere and stars.
     */
    Background,
    /**
     * Opaque base maps.
     */
    Basemap,
    /**
     * Translucent overlays draw on top of base maps.
     */
    Overlay,
    /**
     * Data, e.g., shapefiles, kml, gml
     */
    Data,
    /**
     * Markers, tactical graphics
     */
    Symbology,
    /**
     * Analytical representations and animations.
     */
    Analytic,
    /**
     * Undefined roles
     */
    Undefined,
    /**
     * User interface controls, compass, legends.
     */
    Widget;
    private static final Logger logger = Logger.getLogger(BasicLayerGroup.class.getName());

    /**
     * Returns the BaslicLayerRole who's name matches the given text. Used to create a role from a
     * name in the layer.xml file.
     *
     * @param text used to match the name of a BasicLayerGroup enum.
     * @return a role matching a predefined enum; returns Undefined if no match.
     */
    public static BasicLayerGroup fromString(String text) {
        if (text != null) {
            for (BasicLayerGroup group : values()) {
                if (text.equalsIgnoreCase(group.toString())) {
                    return group;
                }
            }
        }
        logger.log(Level.SEVERE, "{0} is not a valid Layer Role.", text);
        return Undefined;
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public int getIndex() {
        return ordinal();
    }

}
