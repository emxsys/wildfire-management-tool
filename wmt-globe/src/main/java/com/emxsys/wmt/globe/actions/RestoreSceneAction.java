/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.actions;

import com.emxsys.wmt.gis.api.scene.Scene;
import com.emxsys.wmt.gis.api.scene.SceneCatalog;
import com.emxsys.wmt.globe.scenes.BasicScene;
import com.emxsys.wmt.globe.scenes.BasicSceneNode;
import com.emxsys.wmt.util.ProjectUtil;
import com.terramenta.ribbon.RibbonActionReference;
import com.terramenta.ribbon.RibbonActionReferences;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.JList;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 *
 * @author Bruce Schubert
 */
@ActionID(category = "Scenes", id = "com.emxsys.wmt.globe.actions.RestoreSceneAction")
@ActionRegistration(displayName = "#CTL_RestoreSceneAction",
        iconBase = "com/emxsys/wmt/core/images/image_accept.png",
        surviveFocusChange = false)
@ActionReference(path = "Toolbars/Find", position = 300)
@RibbonActionReferences({
    @RibbonActionReference(path = "Menu/Home/Manage",
            position = 600,
            priority = "top",
            description = "#CTL_RestoreSceneAction_Hint",
            tooltipTitle = "#CTL_RestoreSceneAction_TooltipTitle",
            tooltipBody = "#CTL_RestoreSceneAction_TooltipBody",
            tooltipIcon = "com/emxsys/wmt/core/images/image_accept32.png"),
//                       tooltipFooter = "com.emxsys.wmt.globe.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
    @RibbonActionReference(path = "Ribbon/TaskPanes/SceneTools/View",
            position = 300,
            priority = "top",
            description = "#CTL_RestoreSceneAction_Hint",
            tooltipTitle = "#CTL_RestoreSceneAction_TooltipTitle",
            tooltipBody = "#CTL_RestoreSceneAction_TooltipBody",
            tooltipIcon = "com/emxsys/wmt/core/images/image_accept32.png")
//                       tooltipFooter = "com.emxsys.wmt.globe.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/wmt/core/images/help.png")
})
@Messages({
    "CTL_RestoreSceneAction=Restore Scene",
    "CTL_RestoreSceneAction_Hint=Restores globe settings from a scene.",
    "CTL_RestoreSceneAction_TooltipTitle=Restore Scene",
    "CTL_RestoreSceneAction_TooltipBody=Restore the globe's maps, location and view perspective to previously stored scene.",
    "ERR_NoProjectForScene=A project is required to select a scene. Please open a project or create a new project.",
    "ERR_NoSceneCatalog=The main/active project does not have a scene catalog.",
    "ERR_NoScenesToRestore=There are no scenes to restore.",
    "ERR_NoSceneSelected=A scene was not selected.",
    "LBL_SelectScene=Select a Scene to Restore"

})
public final class RestoreSceneAction implements ActionListener {

    private static final Logger LOG = Logger.getLogger(RestoreSceneAction.class.getName());


    @Override
    public void actionPerformed(ActionEvent ev) {
        // Get the selected Scene
        Scene scene = null;
        BasicSceneNode node = Utilities.actionsGlobalContext().lookup(BasicSceneNode.class);
        if (node != null) {
            scene = node.getScene();
        }
        if (scene == null) {
            // No scene? Prompt the to user to select a scene from the current project
            scene = selectSceneFromProject();
        }
        // Activate the currenly selected Scene, if there is one.
        if (scene != null) {
            scene.restore();
        }
    }

    /**
     * Provides a dialog for the user to select a scene from a project's scene catalog.
     * @return
     */
    private Scene selectSceneFromProject() {
        try {
            Project mainProject = ProjectUtil.getCurrentProject();
            if (mainProject == null) {
                throw new IllegalStateException(Bundle.ERR_NoProjectForScene());
            }
            // Get reference to the catalog used to store scenes
            SceneCatalog catalog = mainProject.getLookup().lookup(SceneCatalog.class);
            if (catalog == null) {
                throw new RuntimeException(Bundle.ERR_NoSceneCatalog());
            }
            // Get the scenes from the project's catalog
            Collection<? extends Scene> scenes = catalog.getScenes();
            if (scenes.isEmpty()) {
                throw new RuntimeException(Bundle.ERR_NoScenesToRestore());
            } else if (scenes.size() == 1) {
                return scenes.iterator().next();
            }
            // Select a scene
            JList jList = new JList(scenes.toArray());
            DialogDescriptor d = new DialogDescriptor(jList, Bundle.LBL_SelectScene());
            Object result = DialogDisplayer.getDefault().notify(d);
            if (result == NotifyDescriptor.OK_OPTION) {
                Scene scene = (Scene) jList.getSelectedValue();
                if (scene == null) {
                    throw new RuntimeException(Bundle.ERR_NoSceneSelected());
                }
                return scene;
            }
        } catch (RuntimeException e) {
            LOG.warning(e.getMessage());
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(d);
        }
        return null;
    }
}
