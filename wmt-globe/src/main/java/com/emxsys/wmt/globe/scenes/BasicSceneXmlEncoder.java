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
package com.emxsys.wmt.globe.scenes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class is responsible for parsing and encoding the XML for a WorldWind scene.
 * 
 * Example:
 * <pre>
 * {@code
 * <root xmlns="http://emxsys.com/worldwind-basicscene">
 *      <Scene>
 *          <Position/>
 *          <Eyepoint/>
 *          <Layers/>
 *          <Scenes/>
 *      </Scene>
 * </root>
 * }
 * </pre>
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: BasicSceneXmlEncoder.java 442 2012-12-12 13:15:16Z bdschubert $
 */
@Messages(
{
    "err.cannot.import.scene=Cannot import scene. {0}",
    "err.cannot.export.scene=Cannot export scene. {0}",
    "err.document.is.null=Document argument cannot be null.",
    "err.document.missing.scene={0} is does not have a Scene element.",
    "err.document.has.extra.scenes={0} contains more than one Scene element. Only one Scene will be processed.",
})
public class BasicSceneXmlEncoder
{

    public static final String TAG_ROOT = "root";
    public static final String TAG_SCENE = "Scene";
    private static final Logger logger = Logger.getLogger(BasicSceneXmlEncoder.class.getName());


    /**
     * Reads a Scene from the supplied document.
     * @param doc containing a Scene
     * @return a new BasicScene object; null on error.
     */
    public static BasicScene readDocument(Document doc)
    {
        if (doc == null)
        {
            logger.severe(Bundle.err_cannot_import_scene(new IllegalArgumentException(
                Bundle.err_document_is_null()).toString()));
            return null;
        }
        // Get the scenes, should be only one, but check for [0-*]
        final NodeList sceneNodes = doc.getElementsByTagName(TAG_SCENE);
        if (sceneNodes.getLength() == 0)
        {
            logger.warning(Bundle.err_cannot_import_scene(new IllegalStateException(
                Bundle.err_document_missing_scene(doc.getDocumentURI())).toString()));
            return null;
        }
        else if (sceneNodes.getLength() > 1)
        {
            logger.warning(Bundle.err_cannot_import_scene(new IllegalStateException(
                Bundle.err_document_has_extra_scenes(doc.getDocumentURI())).toString()));
        }

        // Get the first (and only) scene
        Element sceneElement = (Element) sceneNodes.item(0);
        BasicScene scene = BasicScene.fromXmlElement(sceneElement);
        if (scene == null)
        {
            logger.severe(Bundle.err_cannot_import_scene(new IllegalStateException(
                "Could not decode XML.").toString()));
        }
        return scene;
    }


    /**
     * Performs a complete rewrite of a scene xml file.
     *
     * @param doc document to update
     * @param scene scene to export
     */
    public static synchronized void writeDocument(final Document doc, final BasicScene scene)
    {
        final Node root = doc.getElementsByTagName(TAG_ROOT).item(0);
        // Clear any existing element in prep for saving new scene data
        final NodeList children = root.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--)
        {
            root.removeChild(children.item(i));
        }
        // Write the data
        Element element = scene.toXmlElement(doc, TAG_SCENE);
        if (element != null)
        {
            root.appendChild(element);
        }
        else
        {
            logger.log(Level.WARNING, "writeDocument was unable to export {0}.", scene.toString());
        }
    }
}
