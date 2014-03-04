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
package com.emxsys.wmt.gis.api.scene;

import com.emxsys.wmt.gis.api.scene.Scene;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;

/**
 * This class manages a collection of Scenes. Typically, an instance of this class is placed in a
 * project's lookup, and its existence there indicates support for Scenes.
 *
 * @author Bruce Schubert
 * @version $Id: BasicSceneCatalog.java 441 2012-12-12 13:11:18Z bdschubert $
 */
public class BasicSceneCatalog extends AbstractSceneCatalog implements PropertyChangeListener {

    private Map<String, Scene> sceneMap = new TreeMap<String, Scene>();
    private static final Logger logger = Logger.getLogger(BasicSceneCatalog.class.getName());
    private FileObject folder;

    /**
     * Constructs a EntityCatalog who's contents are backed by a folder.
     *
     * @param folder the folder where Markers are saved
     */
    public BasicSceneCatalog(FileObject folder) {
        setFolder(folder);
    }

    /**
     * @return the folder who's contents this catalog represents.
     */
    public FileObject getFolder() {
        return folder;
    }

    /**
     * Sets the folder that this catalog represents.
     *
     * @param folder the folder where Markers are saved.
     */
    private void setFolder(FileObject folder) {
        if (folder != null && !folder.isFolder()) {
            throw new IllegalArgumentException("setFolder: " + folder.getName() + " is not a folder.");
        }
        this.folder = folder;
    }

    @Override
    public void addScene(Scene scene) {
        if (sceneMap.containsKey(scene.getName())) {
            String msg = "The scene name (" + scene.getName() + ") is already in use. The scene was not added.";
            logger.warning(msg);
            throw new IllegalArgumentException(msg);
        }

        scene.addPropertyChangeListener(this);
        this.sceneMap.put(scene.getName(), scene);
        logger.log(Level.FINE, "{0} added.", scene.getName());
        this.pcs.firePropertyChange(PROP_SCENE_ADDED, null, scene);
    }

    @Override
    public void removeScene(Scene scene) {
        scene.removePropertyChangeListener(this);
        this.sceneMap.remove(scene.getName());

        if (this.defaultScene == scene) {
            this.defaultScene = null;
        }
        logger.log(Level.FINE, "{0} removed.", scene.getName());
        pcs.firePropertyChange(PROP_SCENE_REMOVED, scene, null);
    }

    @Override
    public Collection<? extends Scene> getScenes() {
        return this.sceneMap.values();
    }

    @Override
    public void setScenes(Collection<? extends Scene> scenes) {
        logger.fine("Scene collection being added...");
        for (Scene scene : this.sceneMap.values()) {
            removeScene(scene);
        }
        this.sceneMap.clear();
        pcs.firePropertyChange(PROP_SCENES_CLEARED, null, null);
        this.defaultScene = null;

        if (scenes != null && !scenes.isEmpty()) {

            for (Scene scene : scenes) {
                addScene(scene);
            }
            pcs.firePropertyChange(PROP_SCENES_ADDED, null, scenes);
        }
    }

    /**
     * Responds to changes in individual scenes.
     *
     * @param evt event from a Scene.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Scene.PROP_SCENE_UPDATED)) {
            pcs.firePropertyChange(PROP_SCENE_CHANGED, evt.getOldValue(), evt.getNewValue());
        }
        else if (evt.getPropertyName().equals(Scene.PROP_SCENE_NAME_CHANGED)) {
            pcs.firePropertyChange(PROP_SCENE_CHANGED, evt.getOldValue(), evt.getNewValue());
        }
    }
}
