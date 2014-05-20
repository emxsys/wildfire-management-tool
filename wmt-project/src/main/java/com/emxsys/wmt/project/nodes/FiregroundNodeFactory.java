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

import com.emxsys.util.DragDropUtil;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.datatransfer.PasteType;

/**
 * This class provides a node for the "fireground" folder which contains the geographic sectors and
 * fuel model overrides. The class is an extension point for the {@code com-emxsys-basic-project}
 * project type.
 *
 * @author Bruce Schubert
 * @version $Id: FiregroundNodeFactory.java 608 2013-05-07 18:46:01Z bdschubert $
 */
@NodeFactory.Registration(projectType = "com-emxsys-wmt-project", position = 400)
public class FiregroundNodeFactory implements NodeFactory {

    public static final String FIREGROUND_FOLDER = "fireground";
    public static final String WEATHER_DIR = "weather";
    public static final String BEHAVE_DIR = "analysis";
    private static final Logger logger = Logger.getLogger(FiregroundNodeFactory.class.getName());

    /**
     * Creates a Node that represents the fireground folder.
     *
     * @param project with a fireground
     * @return NodeList containing a FiregroundFilesNode
     */
    @Override
    public NodeList createNodes(Project project) {
        try {
            FiregroundFilesNode node = new FiregroundFilesNode(project);
            return NodeFactorySupport.fixedNodeList(node);
        } catch (DataObjectNotFoundException ex) {
            logger.severe(ex.toString());
        } catch (IOException ex) {
            logger.severe(ex.toString());
        }
        // return empty list if not a CpsProject
        return NodeFactorySupport.fixedNodeList();

    }

    /**
     * A FilterNode for the fireground folder. This node permits the display and drag-and-drop of
     * sector files and fuel model files.
     *
     * @author Bruce Schubert
     * @version $Id: FiregroundNodeFactory.java 608 2013-05-07 18:46:01Z bdschubert $
     */
    @Messages({
        "CTL_Fireground=Fireground"
    })
    public class FiregroundFilesNode extends FilterNode {

        private Image icon;

        /**
         * Constructs a DataNode representing the "fireground" folder. Creates the folder if it does
         * not exist in order to be compatible with old projects that are missing the folder.
         *
         * @param project where the "fireground" folder is found
         * @throws DataObjectNotFoundException
         */
        public FiregroundFilesNode(Project project) throws IOException {
            // Older projects may not have a "fireground" folder.
            this(FileUtil.createFolder(project.getProjectDirectory(), FIREGROUND_FOLDER));
        }

        private FiregroundFilesNode(FileObject folder) throws DataObjectNotFoundException {
            this(DataFolder.findFolder(folder));
        }

        private FiregroundFilesNode(DataFolder folder) {
            // Using a DataFilter implementation to prevent uninteresting file(s) from 
            // showing up in the child nole list.
            super(folder.getNodeDelegate(), folder.createNodeChildren(new FiregroundFilter()));
        }

        @Override
        public String getDisplayName() {
            return Bundle.CTL_Fireground();
        }

        @Override
        public Image getIcon(int type) {
            // Merge a folder icon with a sector badge
            Image folderIcon = super.getIcon(type);
            Image mergedImage = null;
            Image badge = ImageUtilities.loadImage("com/emxsys/wmt/project/images/sector_badge.png");
            if (badge != null) {
                mergedImage = ImageUtilities.mergeImages(folderIcon, badge, 8, 0);
            }
            return mergedImage == null ? folderIcon : mergedImage;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public boolean canRename() {
            return false;
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
            DataObject dob = DragDropUtil.findDataObject(trnsfr);
            if (dob != null) {
                return dob.getPrimaryFile().getMIMEType().equals("text/emxsys-worldwind-basicsector+xml");
            }
            return false;
        }
    }

    /**
     * Called by DataFolder.createNodeChildren, this class filters out DataObjects that do not
     * contain a fireground sectors or fuel models.
     */
    class FiregroundFilter implements DataFilter {

        @Override
        public boolean acceptDataObject(DataObject obj) {
//            String mimeType = obj.getPrimaryFile().getMIMEType();
//            switch (mimeType)
//            {
//                case "application/x-weather":
//            }
            return true; //return obj.getLookup().lookup(GeoSector.class) == null ? false : true;
        }
    }
}
