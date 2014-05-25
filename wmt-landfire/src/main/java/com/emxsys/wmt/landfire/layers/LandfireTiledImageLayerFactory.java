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
package com.emxsys.wmt.landfire.layers;

import com.emxsys.util.ModuleUtil;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class LandfireTiledImageLayerFactory {

    private static final Logger logger = Logger.getLogger(LandfireTiledImageLayerFactory.class.getName());

    /**
     * Factory method referenced by instanceCreate in layer.xml that creates a
     * LandfireTiledImageLayer object.
     *
     * The config xml files are typically found in or below the application's
     * modules/ext folder. Your module can supply config files by placing them
     * into the module's release/modules/ext folder hierarchy and referencing it
     * in the layer.xml file.
     *
     * @param fo the file specified in the layer.xml file
     * @return an object of the type specified in the instanceClass attribute
     */
    @SuppressWarnings("UseSpecificCatch")
    public static Layer newLayer(FileObject fo) {
        LandfireTiledImageLayer tiledImageLayer = null;
        try {
            // Get the instance cookie which defines the class to be created
            DataObject ob = DataObject.find(fo);
            InstanceCookie ck = ob.getLookup().lookup(InstanceCookie.class);

            // Get a constructor for the class defined in the instance cookie
            Class<?> layerCls = ck.instanceClass();
            Constructor<?> ctor = layerCls.getConstructor(new Class<?>[]{
                Element.class, AVList.class
            });

            // Create the landfire layer object
            if (ctor != null && LandfireTiledImageLayer.class.isAssignableFrom(layerCls)) {
                // First get the constructor arguments (worldwind config data)...
                Element domElement = getLayerDomElement(fo);
                if (domElement != null && isTiledImageLayer(domElement)) {
                    // ... and then invoke the constructor
                    tiledImageLayer = (LandfireTiledImageLayer) ctor.newInstance(new Object[]{
                        domElement, null
                    });
                }
            }

            // Perform initilization outside the scope of what's in the worldwind config.
            if (tiledImageLayer != null) {
                tiledImageLayer.updateLayerProperties(fo);
                tiledImageLayer.initColorMap((URL) fo.getAttribute("colorkey"));
                logger.log(Level.CONFIG, "Created new instance of {0}", tiledImageLayer.toString());
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, fo.toString(), ex);
        }
        return tiledImageLayer;
    }

    /**
     * Retrieves the Layer document object model element from a WorldWind tiled
     * image layer's xml config file. The FileObject must contain a a urlvalue
     * attribute named "config" that references the WW config file.
     *
     * @param fo FileObject from "layer.xml" that references the WW config file
     * @return Document Object Model element referring to the Layer element or
     * null if not found
     */
    public static Element getLayerDomElement(FileObject fo) {

        URL cfgUrl = (URL) fo.getAttribute("config");
        if (cfgUrl != null) {
            if (cfgUrl.getProtocol().contains("nbinst")) {
                File xmlCfgFile = ModuleUtil.createFileFromUrl(cfgUrl);
                return getLayerDomElement(xmlCfgFile);

            }
        }
        return null;
    }

    /**
     * Method that understands the NetBeans nbinst:// URL protocol.
     *
     * @param url
     * @return the file referenced in the URL or null if cannot be created.
     */
    /**
     * Retrieve the root Layer element from the supplied xml config file.
     *
     * @param xmlCfgFile WW xml configuration file for a TiledImageLayer
     * @return the root Layer dom element or null if not found
     */
    public static Element getLayerDomElement(File xmlCfgFile) {
        if (xmlCfgFile != null) {
            Document doc = WWXML.openDocument(xmlCfgFile);
            if (doc != null) {
                Element[] elements = WWXML.getElements(doc.getDocumentElement(), "./Layer", null);
                if (elements != null && elements.length == 1) {
                    return elements[0];
                }
                String localName = WWXML.getUnqualifiedName(doc.getDocumentElement());
                if (localName != null && localName.equals("Layer")) {
                    return doc.getDocumentElement();
                }
                logger.log(Level.WARNING, "No \"Layer\" element found in {0}. ", xmlCfgFile);
            } else {
                logger.log(Level.WARNING, "DOM Document for {0} is null. ", xmlCfgFile);
            }
        } else {
            logger.log(Level.WARNING, "xml config file should not be null");
        }
        return null;
    }

    protected static boolean isTiledImageLayer(Element domElement) {
        if (domElement != null) {
            String layerType = WWXML.getText(domElement, "@layerType");
            return layerType != null && layerType.equals("TiledImageLayer");
        } else {
            return false;
        }
    }
}
