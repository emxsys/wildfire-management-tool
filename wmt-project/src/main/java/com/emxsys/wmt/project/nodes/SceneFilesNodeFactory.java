/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.project.nodes;

import com.emxsys.wmt.project.BasicProject;
import com.emxsys.wmt.util.DragDropUtil;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 * A registered NodeFactory for the Scenes folder. Used by ProjectNode in its call to
 * NodeFactorySupport.createCompositeChildren(...).
 *
 * @author Bruce Schubert
 */
@NodeFactory.Registration(projectType = "com-emxsys-wmt-project", position = 100)
public class SceneFilesNodeFactory implements NodeFactory {

    private static final Logger logger = Logger.getLogger(SceneFilesNodeFactory.class.getName());

    @Override
    public NodeList createNodes(Project project) {

        try {
            SceneFilesNode nd = new SceneFilesNode(project);
            return NodeFactorySupport.fixedNodeList(nd);
        } catch (DataObjectNotFoundException ex) {
            logger.severe(ex.toString());
        }

        //If the above try/catch fails, e.g.,
        //then return an empty list of nodes:
        return NodeFactorySupport.fixedNodeList();

    }

    @NbBundle.Messages(
            {
                "CTL_Scenes=Scenes"
            })
    public class SceneFilesNode extends FilterNode {

        SceneFilesNode(Project project) throws DataObjectNotFoundException {
            super(DataObject.find(project.getProjectDirectory().getFileObject(BasicProject.SCENE_FOLDER_NAME)).getNodeDelegate());
        }

        @Override
        public String getDisplayName() {
            return Bundle.CTL_Scenes();
        }

        @Override
        public Image getIcon(int type) {
            Image folderIcon = super.getIcon(type);
            Image mergedImage = null;
            Image badge = ImageUtilities.loadImage("com/emxsys/wmt/project/images/image_badge.png");
            if (badge != null) {
                mergedImage = ImageUtilities.mergeImages(folderIcon, badge, 10, 3);
            }
            return mergedImage == null ? folderIcon : mergedImage;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
            if (isDataFlavorSupported(t)) {
                return super.getDropType(t, action, index);
            }

            return null;
        }

        @Override
        public PasteType[] getPasteTypes(Transferable t) {
            if (isDataFlavorSupported(t)) {
                return super.getPasteTypes(t);
            }
            return new PasteType[]{};
        }

        private boolean isDataFlavorSupported(Transferable trnsfr) {
            DataObject obj = DragDropUtil.findDataObject(trnsfr);
            if (obj != null) {
                return obj.getPrimaryFile().getMIMEType().equals("text/emxsys-wmt-basicscene+xml");
            }
            return false;
        }
    }
}
