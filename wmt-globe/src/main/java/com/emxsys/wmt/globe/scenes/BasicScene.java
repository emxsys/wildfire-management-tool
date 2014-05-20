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

import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.scene.AbstractScene;
import com.emxsys.gis.api.scene.BasicSceneCatalog;
import com.emxsys.gis.api.scene.Scene;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.util.Positions;
import com.emxsys.util.DateUtil;
import com.emxsys.util.FilenameUtils;
import com.terramenta.globe.WorldWindManager;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.view.BasicView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class represents the contents of a Scene for the WorldWindViewer. It contains a SceneFactory
 * which implements the SceneProvider interface. The WorldWindViewer can place this factory in its
 * lookup to provide the capability for creating Scenes via the provider.
 *
 * @see Scene
 * @see SceneProvider
 * @see SceneFactory
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages({
    "# {0} - Scene Name",
    "err.could.not.restore.scene=Could not restore scene. {0}"
})
public class BasicScene extends AbstractScene {

    public static final String TAG_POSITION = "Position";
    public static final String TAG_EYEPOINT = "EyePoint";
    public static final String TAG_LAYERS = "Layers";
    public static final String TAG_LAYER = "Layer";
    public static final String ATTR_NAME = "Name";
    private Position eyePosition;
    private Position centerPosition;
    private List<String> layerNames = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(BasicScene.class.getName());

    /**
     * Hidden constructor.
     */
    private BasicScene() {
    }

    /**
     * Factory method to create a scene based on the current viewer content.
     *
     * @param globe provider of scene content
     * @return a new BasicScene
     */
    public static BasicScene fromViewer(Globe globe) {
        BasicView view = (BasicView) globe.getWorldWindManager().getWorldWindow().getView();

        BasicScene scene = new BasicScene();
        scene.name = DateUtil.getCurrentDateTimeString();
        scene.eyePosition = view.getEyePosition();
        scene.centerPosition = view.getCenterPosition();
        scene.layerNames = getEnabledLayerNames();

        return scene;
    }

    /**
     * Factory method that parses the {@code <Scene/>} elements into a BasicScene.
     *
     * @param sceneElement xml to be parsed
     * @return the Scene object defined by the XML elements; it will null if invalid.
     */
    public static BasicScene fromXmlElement(Element sceneElement) {
        BasicScene scene = new BasicScene();
        return loadXmlElement(sceneElement, scene);
    }

    /**
     * Initializes a BasicScene instance from an XML element.
     *
     * @param element to load
     * @param scene to initialize
     * @return an initialized scene
     */
    public static BasicScene loadXmlElement(final Element element, BasicScene scene) {
        return SceneXmlDecoder.importXml(element, scene);
    }

    /**
     * Creates a
     * <pre>{@code
     * <tagName Name="">
     *      <Position/>
     *      <Eyepoint/>
     *      <Layers/>
     * <tagName/>
     * >}</pre> node hierarchy.
     *
     * @param doc used to create the element
     * @param tagName name of the new element, e.g. "Scene"
     * @return a new node element ready to be appended to a parent.
     */
    public Element toXmlElement(Document doc, String tagName) {
        return SceneXmlEncoder.exportXml(doc, tagName, this);
    }

    /**
     * Sets the view from this scene.
     */
    @Override
    public void restore() {
        if (centerPosition == Position.ZERO || eyePosition == Position.ZERO) {
            throw new IllegalStateException("Null/ZERO positions are not allowed.");
        }
        Globe globe = Globe.getInstance();
        try {
            WorldWindManager wwm = globe.getWorldWindManager();
            wwm.getWorldWindow().getView().setOrientation(eyePosition, centerPosition);
        } catch (Exception exception) {
            logger.severe(Bundle.err_could_not_restore_scene(exception.toString()));
        }
        // Restore the layers...
        Collection<? extends GisLayer> allLayers = globe.getGisLayerList().getLayers();
        for (GisLayer gisLayer : allLayers) {
            gisLayer.setEnabled(this.layerNames.contains(gisLayer.getName()));
        }
        // The viewer does't refresh on its own, we have to poke it!
        globe.refreshView();
    }

    /**
     * Updates the scene content from the scene provider.
     */
    @Override
    public void update() {
        // Get a new current scene and copy it to this object
        BasicScene scene = (BasicScene) SceneFactory.getInstance().createScene();

        this.centerPosition = scene.centerPosition;
        this.eyePosition = scene.eyePosition;
        this.layerNames = scene.layerNames;
        //this.name = scene.name; -- retain the orginal name

        // Fire property Change to update catalog
        this.pcs.firePropertyChange(PROP_SCENE_UPDATED, null, null);
    }

    /**
     * Returns the list of the viewer's currently enabled layers.
     *
     * @param layerList
     * @return
     */
    private static List<String> getEnabledLayerNames() {
        LayerList layerList = Globe.getInstance().getWorldWindManager().getWorldWindow().getModel().getLayers();
        List<String> layers = new ArrayList<>();
        for (int i = 0; i < layerList.size(); i++) {
            if (layerList.get(i).isEnabled()) {
                // TODO: could use our GisViewer.lookup(GisLayer.class)
                layers.add(layerList.get(i).getName());
            }
        }
        return layers;
    }

    /**
     * Utility class for encoding a BasicScene to XML.
     *
     * @author Bruce
     */
    private static class SceneXmlEncoder {

        /**
         * Encodes the scene to an XML Element, e.g.,
         * <pre>{@code
         *  <Scene>
         *      <Position/>
         *      <Eyepoint/>
         *      <Layers/>
         *      <Scenes/>
         *  </Scene>
         * }</pre>
         *
         * @param doc used to create the element
         * @param tagName name of the new element
         * @param scene the scene to encode
         * @return an Element representing a BasicScene that can be appended to a parent node.
         */
        public static Element exportXml(Document doc, String tagName, BasicScene scene) {
            Element element = doc.createElement(tagName);
            element.setAttribute(ATTR_NAME, scene.getName());
            element.appendChild(positionToElement(doc, TAG_POSITION, scene.centerPosition));
            element.appendChild(positionToElement(doc, TAG_EYEPOINT, scene.eyePosition));
            element.appendChild(layersToElement(doc, TAG_LAYERS, scene.layerNames));
            return element;
        }

        /**
         * Encodes a WorldWind Position via GeoCoord3D.toXmlElement().
         *
         * @param doc used to create the element
         * @param tagName name of the new element
         * @param position the position encode
         * @return a new element encoded by GeoCoord3D.toXmlElement().
         * @see GeoCoord3D
         */
        private static Element positionToElement(Document doc, String tagName, Position position) {
            return Positions.toGeoCoord3D(position).toXmlElement(doc, tagName);
        }

        /**
         * Creates the
         * <pre>{@code
         * <Layers>
         *      <Layer Name=""/>
         *      <Layer Name=""/>
         * </Layers>
         * }</pre> node hierarchy.
         *
         * @param doc used to create the element
         * @param tagName name of the new element
         * @param layerNames the layers to encode
         * @return a new element ready to be appended to its parent.
         */
        private static Element layersToElement(Document doc, String tagName, List<String> layerNames) {
            Element layersElement = doc.createElement(tagName);
            if (layerNames != null) {
                for (String name : layerNames) {
                    Element layer = doc.createElement(TAG_LAYER);
                    layer.setAttribute(ATTR_NAME, name);
                    layersElement.appendChild(layer);
                }
            }
            return layersElement;
        }
    }

    /**
     * Utility class for decoding BasicScene XML.
     *
     * @author Bruce
     */
    private static class SceneXmlDecoder {

        public static BasicScene importXml(Element element, BasicScene scene) {
            if (element == null) {
                // TODO: log/throw
                return null;
            }

            String sceneName = element.getAttribute(ATTR_NAME);
            if (sceneName == null || sceneName.isEmpty()) {
                // TODO: log/throw
                return null;
            }

            Position centerPosition = Position.ZERO;
            NodeList positionNodes = element.getElementsByTagName(TAG_POSITION);
            if (positionNodes != null && positionNodes.getLength() > 0) {
                centerPosition = parsePosition((Element) positionNodes.item(0));
            }

            Position eyePosition = Position.ZERO;
            NodeList eyepointNodes = element.getElementsByTagName(TAG_EYEPOINT);
            if (eyepointNodes != null && eyepointNodes.getLength() > 0) {
                eyePosition = parsePosition((Element) eyepointNodes.item(0));
            }

            List<String> layerNames = new ArrayList<String>();
            NodeList layersNodes = element.getElementsByTagName(TAG_LAYERS);
            if (layersNodes != null && layersNodes.getLength() > 0) {
                parseLayers((Element) layersNodes.item(0), layerNames);
            }

            scene.name = sceneName;
            scene.eyePosition = eyePosition;
            scene.centerPosition = centerPosition;
            scene.layerNames = layerNames;
            return scene;
        }

        /**
         * Parse an XML element containing a position.
         *
         * @param position an Element encoded by GeoCoord3D.toXmlElement
         * @return a WorldWind Position
         * @see GeoCoord3D
         */
        private static Position parsePosition(final Element position) {
            GeoCoord3D coord = GeoCoord3D.fromXmlElement(position);
            return Positions.fromCoord3D(coord);
        }

        private static List<String> parseLayers(final Element layersElement, List<String> layerNames) {
            layerNames.clear();
            NodeList layerNodes = layersElement.getElementsByTagName(TAG_LAYER);
            for (int i = 0; i < layerNodes.getLength(); i++) {
                Element layerElement = (Element) layerNodes.item(i);
                String nameAttr = layerElement.getAttribute(ATTR_NAME);
                layerNames.add(nameAttr);
            }
            return layerNames;
        }
    }

    /**
     * This class is used to create scenes on the WorldWindViewer. An instance of this class should
     * be added to the WorldWindViewer's TopComponent lookup to provide Scene create and restore
     * capabilities. This class is a singleton.
     *
     * @see Scene
     * @see WorldWindViewer
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    public static class SceneFactory implements Scene.Factory {

        private static final String TEMPLATE_CONFIG_FILE = "Templates/Scene/SceneTemplate.xml";
        private static DataObject template;
        private static final Logger logger = Logger.getLogger(SceneFactory.class.getName());

        /**
         * Singleton implementation constructor.
         */
        private SceneFactory() {
            try {
                template = DataObject.find(FileUtil.getConfigFile(TEMPLATE_CONFIG_FILE));
            } catch (DataObjectNotFoundException ex) {
                logger.severe(ex.toString());
            }
        }

        /**
         * Gets the singleton SceneFactory.
         *
         * @return the singleton
         */
        public static SceneFactory getInstance() {
            return SceneFactoryHolder.INSTANCE;
        }

        /**
         * Creates a Scene from the WorldWindViewer.
         *
         * @return a BasicScene object.
         */
        @Override
        public Scene createScene() {
            return BasicScene.fromViewer(Globe.getInstance());
        }

        /**
         * Returns a new scene from an XML Element. Called by the BasicSceneDataObject.
         *
         * @param element an Element encoded by BasicScene.toXmlElement.
         * @return a Scene instance.
         */
        @Override
        public Scene fromXmlElement(final Element element) {
            BasicScene scene = new BasicScene();
            return BasicScene.loadXmlElement(element, scene);
        }

        /**
         * Creates a DataObject that represents the supplied Scene.
         *
         * @param scene to be assigned to the DataObject
         * @param folder where to create the DataObject, uses the current project if null
         * @return a BasicSceneDataObject
         */
        @Override
        public DataObject createDataObject(Scene scene, FileObject folder) {
            // Use the current project folder if not given
            if (folder == null) {
                Project currentProject = Utilities.actionsGlobalContext().lookup(Project.class);
                if (currentProject != null) {
                    BasicSceneCatalog catalog = currentProject.getLookup().lookup(BasicSceneCatalog.class);
                    if (catalog != null) {
                        folder = catalog.getFolder();
                    }
                }
            }
            DataFolder dataFolder = DataFolder.findFolder(folder);

            // Ensure the filename is unique -- appends a numeral if not
            String filename = FilenameUtils.getUniqueEncodedFilename(
                    folder, scene.getName(), template.getPrimaryFile().getExt());

            // Create the scene file from our template -- delegated to BasicSceneTemplateHandler
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("model", scene);
            DataObject dataObject = null;
            try {
                dataObject = template.createFromTemplate(dataFolder, filename, parameters);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return dataObject;
        }
    }

    /**
     * Singleton implementation for the SceneFactory
     */
    private static class SceneFactoryHolder {

        private static final SceneFactory INSTANCE = new SceneFactory();
    }
}
