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
package com.emxsys.gis.api.layer;

import java.util.Arrays;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class creates XML layer entries for the Globe module. This class is modeled after the
 * org.netbeans.modules.openide.awt.ActionProcessor by Jaroslav Tulach.
 * <p>
 * @author Bruce Schubert <bruce@emxsys.com>
 * <p>
 * @see ActionProcessor
 */
@ServiceProvider(service = Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({
    "com.emxsys.gis.api.layer.MapLayerRegistration",
    "com.emxsys.gis.api.layer.MapLayerRegistrations"
})
public final class MapLayerProcessor extends LayerGeneratingProcessor {

    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String GROUP = "role";
    public static final String CATEGORY = "category";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ACTUATE = "actuate";
    public static final String OPACITY = "opacity";
    public static final String CONFIG = "config";
    public static final String EXPIRATION_AGE = "expirationAgeSeconds";
    public static final String REFRESH_INTERVAL = "refreshIntervalSeconds";
    public static final String INSTANCE_CLASS = "instanceClass";
    public static final String INSTANCE_CREATE = "instanceCreate";

    public static final String[] VALID_TYPES
            = {
                "Raster", "Vector", "Elevation", "Other", "Unknown"
            };
    public static final String[] VALID_GROUPS
            = {
                "Background", "Basemap", "Overlay", "Data", "Symbology", "Analytic", "Undefined", "Widget"
            };
    public static final String[] VALID_CATEGORIES
            = {
                "Satellite", "Aerial", "Street", "Topographic", "Thematic", "Hybrid", "Other", "Unknown"
            };
    public static final String[] VALID_ACTUATES
            = {
                "onLoad", "onRequest"
            };

    static {
        Arrays.sort(VALID_TYPES);
        Arrays.sort(VALID_GROUPS);
        Arrays.sort(VALID_CATEGORIES);
        Arrays.sort(VALID_ACTUATES);
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations,
                                    RoundEnvironment env) throws LayerGenerationException {
        for (Element e : env.getElementsAnnotatedWith(MapLayerRegistrations.class)) {
            MapLayerRegistrations refs = e.getAnnotation(MapLayerRegistrations.class);
            if (refs != null) {
                for (MapLayerRegistration layer : refs.value()) {
                    processReference(e, layer);
                }
            }
        }
        for (Element e : env.getElementsAnnotatedWith(MapLayerRegistration.class)) {
            MapLayerRegistration mapLayer = e.getAnnotation(MapLayerRegistration.class);
            if (mapLayer != null) {
                processReference(e, mapLayer);
            }
        }
        return true;
    }

    private void processReference(Element e, MapLayerRegistration ref) throws
            LayerGenerationException {
        String name = ref.name().replace(' ', '_').replace('.', '-');
        String group = ref.role().replace(' ', '_');
        File f = layer(e).file("WorldWind/Layers/" + group + "/" + name + ".instance");

        // Name
        f.bundlevalue(NAME, ref.name());

        // Position
        f.position(ref.position());

        // Type
        if (Arrays.binarySearch(VALID_TYPES, ref.type()) < 0) {
            throw new LayerGenerationException("type must be one of " + Arrays.toString(VALID_TYPES), e);
        }
        f.stringvalue(TYPE, ref.type());

        // Group
        if (Arrays.binarySearch(VALID_GROUPS, ref.role()) < 0) {
            throw new LayerGenerationException("role must be one of " + Arrays.toString(VALID_GROUPS), e);
        }
        f.stringvalue(GROUP, ref.role());

        // Category
        if (Arrays.binarySearch(VALID_CATEGORIES, ref.category()) < 0) {
            throw new LayerGenerationException("category must be one of " + Arrays.toString(VALID_CATEGORIES), e);
        }
        f.stringvalue(CATEGORY, ref.category());

        // Display Name
        if (!ref.displayName().isEmpty()) {
            f.bundlevalue(DISPLAY_NAME, ref.displayName());
        }

        // Opacity
        if (ref.opacity() < 0 || ref.opacity() > 1) {
            throw new LayerGenerationException("opacity must be between 0 and 1", e);
        }
        f.doublevalue(OPACITY, ref.opacity());

        // Expiration Age and Refresh Interval
        if (ref.refreshIntervalSeconds() > 0) {
            f.intvalue(REFRESH_INTERVAL, ref.refreshIntervalSeconds());
            f.intvalue(EXPIRATION_AGE, ref.expirationAgeSeconds());
        }

        // Configuration File
        if (!ref.config().isEmpty()) {
            f.urlvalue(CONFIG, ref.config());
        }

        // Actuate
        if (Arrays.binarySearch(VALID_ACTUATES, ref.actuate()) < 0) {
            throw new LayerGenerationException("actuate must be one of " + Arrays.toString(VALID_ACTUATES), e);
        }
        f.stringvalue(ACTUATE, ref.actuate());

        // Instance Class
        if (!ref.instanceClass().isEmpty()) {
            f.stringvalue(INSTANCE_CLASS, ref.instanceClass());
        }

        // Instance Create
        if (!ref.factoryClass().isEmpty() && !ref.factoryMethod().isEmpty()) {
            f.methodvalue(INSTANCE_CREATE, ref.factoryClass(), ref.factoryMethod());
        }

        f.write();

    }
//    @Override
//    public Set<String> getSupportedAnnotationTypes()
//    {
//        return new HashSet<String>();   // Using @SupportedAnnotationsTypes -- return empty set
//        
////        return new HashSet<String>(Arrays.asList(
////                MapLayerRegistration.class.getCanonicalName(),
////                MapLayerRegistrations.class.getCanonicalName()));
//    }
}
