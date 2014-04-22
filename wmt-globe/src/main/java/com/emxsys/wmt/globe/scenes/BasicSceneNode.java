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

import com.emxsys.wmt.gis.api.scene.Scene;
import com.emxsys.wmt.util.FilenameUtils;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 * A node that represents a Scene file.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: BasicSceneNode.java 442 2012-12-12 13:15:16Z bdschubert $
 */
public class BasicSceneNode extends DataNode implements PropertyChangeListener {

    private BasicScene scene;
    private InstanceContent content;
    private final PropertyChangeListener sceneListener = new ScenePropertyChangeListener();
    private final PropertyChangeListener dataListener = new DataObjectPropertyChangeListener();

    public BasicSceneNode(DataObject dataObject, BasicScene scene, Lookup cookieSet) {
        this(dataObject, scene, cookieSet, new InstanceContent());
    }

    private BasicSceneNode(DataObject dataObject, BasicScene scene, Lookup cookieSet, InstanceContent content) {
        super(dataObject, Children.LEAF, new ProxyLookup(cookieSet, new AbstractLookup(content)));
        this.scene = scene;
        this.content = content;
        // Listen for name changes, etc.
        scene.addPropertyChangeListener(WeakListeners.propertyChange(this.sceneListener, scene));
        dataObject.addPropertyChangeListener(WeakListeners.propertyChange(this.dataListener, dataObject));
    }

    @Override
    public String getDisplayName() {
        String displayName = super.getDisplayName();        // may include or hide ext based on base class settings
        return FilenameUtils.decodeFilename(displayName);    // decode URL to plain text
    }

    @Override
    public void setName(String name) {
        // The Scene fires a property change event that invokes updateName to update Node and DataObject
        this.scene.setName(name);
    }

    private void updateName(String name) {
        // Update the Node and the encapulated DataObject
        super.setName(name);
    }

    public BasicScene getScene() {
        return scene;
    }

    @Override
    public Action[] getActions(boolean context) {
        return super.getActions(context);
//        return new Action[]
//            {
//                new AbstractAction("Restore View")
//                {
//                    @Override
//                    public void actionPerformed(ActionEvent e)
//                    {
//                        scene.restore();
//                    }
//                },
//                null, // separator
//                new AbstractAction("Rename Scene...")
//            {
//                @Override
//                public void actionPerformed(ActionEvent e)
//                {
//                    NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(
//                        "Scene Name:",
//                        "Please enter a new name for the scene");
//                    d.setInputText(scene.getName());
//                    Object retval = DialogDisplayer.getDefault().notify(d);
//                    if (retval == NotifyDescriptor.OK_OPTION)
//                    {
//                        scene.setName(d.getInputText());
//                    }
//                }
//            },
//                new AbstractAction("Update Scene")
//                {
//                    @Override
//                    public void actionPerformed(ActionEvent e)
//                    {
//                        scene.update();
//                    }
//                },
//                new AbstractAction("Delete Scene")
//                {
//                    @Override
//                    public void actionPerformed(ActionEvent e)
//                    {
//                        try
//                        {
//                            getDataObject().delete();
//                        }
//                        catch (IOException ex)
//                        {
//                            Exceptions.printStackTrace(ex);
//                        }
//                    }
//                }
//            };
    }

    @Override
    public Action getPreferredAction() {
        return new AbstractAction("Restore") {
            @Override
            public void actionPerformed(ActionEvent e) {
                scene.restore();
            }
        };
    }

    /**
     * Monitors changes in the Scene model.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Scene.PROP_SCENE_NAME_CHANGED)) {
            setName(this.scene.getName());  // Fires the node name changed event
        }
    }

    /**
     * Listens to changes on the Scene.
     */
    private class ScenePropertyChangeListener implements PropertyChangeListener {

        ScenePropertyChangeListener() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals(Scene.PROP_SCENE_NAME_CHANGED)) {
                // Update Node and DataObject from scene (display name)
                updateName(FilenameUtils.encodeFilename(scene.getName()));
            }
        }
    }

    /**
     * Listens to changes on the DataObject.
     */
    private class DataObjectPropertyChangeListener implements PropertyChangeListener {

        DataObjectPropertyChangeListener() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals(DataObject.PROP_MODIFIED)) {
                // Fire event that triggers getHtmlDisplayName to that name reflects modified status
                fireDisplayNameChange(null, null);
            }
        }
    }

}
