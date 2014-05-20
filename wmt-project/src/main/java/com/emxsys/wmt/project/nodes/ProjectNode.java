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
package com.emxsys.wmt.project.nodes;

import com.emxsys.wmt.project.WmtProject;
import com.emxsys.util.ImageUtil;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * This class provides a {@link org.openide.nodes.Node} for the
 * {@link com.emxsys.basicproject.BasicProject} project directory. This is the root node used in the
 * the "Projects" window provided by NetBeans.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: ProjectNode.java 695 2013-05-28 11:19:15Z bdschubert $
 *
 * @see org.openide.nodes.FilterNode
 */
public class ProjectNode extends FilterNode {

    private final WmtProject project;
    private static final Logger logger = Logger.getLogger(ProjectNode.class.getName());

    /**
     * This constructor creates the root node for the supplied project with sub-nodes based on the
     * the {@code Projects/com-emxsys-basic-project/Nodes} path.
     *
     * @param original The original {@link Node} to be cloned in this {@link FilterNode}
     * @param project The current {@link WmtProject}.
     *
     * @see NodeFactorySupport
     */
    public ProjectNode(Node original, WmtProject project) {

        super(original,
                //Create the project's child nodes via registered NodeFactories
                NodeFactorySupport.createCompositeChildren(
                        project, "Projects/com-emxsys-wmt-project/Nodes"),
                //or: new ProjectNodeChildren(original, project), -- displays all children

                //The Project subsystem wants the project in the Node's lookup.
                //NewAction and friends want the original Node's lookup.
                //Make a merge of both ...
                new ProxyLookup(new Lookup[]{
                    Lookups.fixed(project, new EditSupport()), original.getLookup()
                }));

        this.project = project;
    }

    /**
     * Gets an icon for this node based on the default icon provided by
     * ProjectInformation.getIcon(). Note that a project extension can override the default icon by
     * placing an Icon object in the project's lookup.
     *
     * @param type a java.beans.BeanInfo constant
     * @return an icon sized according to the type parameter
     */
    @Override
    public Image getIcon(int type) {
        // Check to see if a project extension is supplying a custom icon,
        // otherwise use this project's default icon.
        Icon icon = this.project.getLookup().lookup(Icon.class);
        if (icon == null) {
            icon = ProjectUtils.getInformation(project).getIcon();
        }
        // Resize and convert to Image
        if (icon != null) {
            Image image = ImageUtilities.icon2Image(icon);
            int width = 16;
            int height = 16;
            switch (type) {
                case java.beans.BeanInfo.ICON_MONO_16x16:
                case java.beans.BeanInfo.ICON_COLOR_16x16:
                    break; // use defaults
                case java.beans.BeanInfo.ICON_MONO_32x32:
                case java.beans.BeanInfo.ICON_COLOR_32x32:
                    width = 32;
                    height = 32;
                    break;
            }
            return ImageUtil.resizeImage(image, width, height);
        }
        return super.getIcon(type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getDisplayName() {
        ProjectInformation info = ProjectUtils.getInformation(this.project);
        if (info == null) {
            logger.warning("A ProjectInfomation object should be placed in the project's lookup."); //NOI18N
            return this.project.getProjectDirectory().getName();
        }
        return info.getDisplayName();
    }

    @Override
    public Action[] getActions(boolean context) {
        // Note: null objects in the array act as separators.
        // This ordering mimics the native NetBeans menus
        return new Action[]{
            new AbstractAction("Update Startup Position") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    project.saveStartupPosition();
                }
            },
            new AbstractAction("Go to Startup Position") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    project.restoreStartupPosition();
                }
            },
            CommonProjectActions.customizeProjectAction(),
            null,// separator
            CommonProjectActions.renameProjectAction(),
            CommonProjectActions.moveProjectAction(),
            CommonProjectActions.copyProjectAction(),
            CommonProjectActions.deleteProjectAction(),
            null,// separator
            CommonProjectActions.closeProjectAction()
        };

    }

    private static class EditSupport implements Editable {

        @Override
        public void edit() {
            CommonProjectActions.customizeProjectAction().actionPerformed(null);
        }
    }
}
