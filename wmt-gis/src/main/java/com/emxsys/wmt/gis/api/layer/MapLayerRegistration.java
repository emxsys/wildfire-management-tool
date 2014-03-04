/*
 * Copyright (c) 2014, Bruce Schubert. <bruce@emxsys.com>
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
 * - Neither the name of the Emxsys company nor the names of its 
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import org.openide.awt.ActionID;

/**
 * This interface defines the elements used to register WorldWind Layers.
 * <p>
 * <p>
 * @author Bruce Schubert <bruce@emxsys.com>
 */
//            <file name="Graphics.instance">
//                <attr name="name" stringvalue="Graphics"/>
//                <attr name="type" stringvalue="Vector"/>
//                <attr name="role" stringvalue="Data"/>
//                <attr name="category" stringvalue="Other"/>
//                <attr name="actuate" stringvalue="onLoad"/>
//                <attr name="opacity" doublevalue="0.8"/>
//                <attr name="position" intvalue="1249"/>
//                <attr name="displayName" bundlevalue="com.emxsys.wmt.globe.Bundle#WorldWind/Layers/Graphics.instance"/>
//                <attr name="instanceClass" stringvalue="com.emxsys.wmt.globe.layers.GraphicLayer"/>
//                <attr name="instanceCreate" methodvalue="com.emxsys.wmt.globe.layers.GraphicLayer.getInstance"/>
//            </file>
@Retention(RetentionPolicy.SOURCE)
@Target(
        {
            ElementType.TYPE, ElementType.FIELD, ElementType.METHOD
        })
public @interface MapLayerRegistration {

    /**
     * One must specify name of the map layer.
     */
    String name();

    /**
     * One can specify the URL of a configuration file for the map layer.
     */
    String config() default "";

    /**
     * The position in the collection of map layers.
     */
    int position() default Integer.MAX_VALUE;

    /**
     * One can specify name of the layer as it should appear in a layer manager.
     */
    String displayName() default "";

    /**
     * One can specify type of data for a map layer. E.g., Raster, Vector, Elevation, Other, Unknown
     */
    String type() default "Unknown";

    /**
     * One can specify the role of a map layer, used for ordering and grouping maps in a layer
     * manager. E.g., Background, Basemap, Overlay, Data, Symbology, Analytic, Undefined, and
     * Widget.
     */
    String role() default "Basemap";

    /**
     * One can specify the category of a map layer, used for grouping maps within the menu system.
     * E.g., Satellite, Aerial, Street, Topographic, Thematic, Hybrid, Other, Unknown;
     */
    String category() default "Unknown";

    /**
     * One can specify whether a map layer is enabled at startup. E.g., onLoad, onRequest.
     */
    String actuate() default "onLoad";

    /**
     * One can specify the opacity (or transparency) of a map layer. A value of 0 (transparent) to 1
     * (opaque).
     */
    double opacity() default 1.0;

    /**
     * One can specify the type of object to be created.
     */
    String instanceClass() default "";

    /**
     * One can specify the factory class.
     */
    String factoryClass() default "";

    /**
     * One can specify the factory class.
     */
    String factoryMethod() default "";

}
