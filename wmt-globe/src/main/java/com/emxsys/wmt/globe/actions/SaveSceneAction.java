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
import com.emxsys.wmt.gis.api.scene.Scene.Factory;
import com.emxsys.wmt.globe.scenes.BasicScene;
import com.emxsys.wmt.util.ProjectUtil;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * A context sensitive action based on a Scene.Factory that creates a Scene base on the current
 * viewer (a Scene.Factory) and saves it to disk.
 *
 * @author Bruce Schubert
 * @version $Id: SaveSceneAction.java 440 2012-12-12 13:09:59Z bdschubert $
 */
@ActionID(category = "Scenes", id = "com.emxsys.wmt.globe.actions.SaveSceneAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/core/images/image_add.png",
        displayName = "#CTL_SaveSceneAction", surviveFocusChange = true)
@ActionReference(path = "Toolbars/Create", position = 100)
@RibbonActionReference(path = "Menu/Home/Manage",
        position = 500,
        priority = "top",
        description = "#CTL_SaveSceneAction_Hint",
        tooltipTitle = "#CTL_SaveSceneAction_TooltipTitle",
        tooltipBody = "#CTL_SaveSceneAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/core/images/image_add32.png")
//                       tooltipFooter = "com.emxsys.basicui.Bundle#CTL_Default_TooltipFooter",
//                       tooltipFooterIcon = "com/emxsys/basicui/resources/help.png")
@Messages(
        {
            "CTL_SaveSceneAction=New Scene",
            "CTL_SaveSceneAction_Hint=Saves current map settings to a scene.",
            "CTL_SaveSceneAction_TooltipTitle=Create Scene",
            "CTL_SaveSceneAction_TooltipBody=Create a new scene to save the current map position and view settings.\n"
            + "You can restore a scene and return to the saved position and view perspective.",
            "ERR_ProjectRequired=A main/active project is required to save a scene. Please open a project or create a new project.",
            "ERR_SceneCatalogRequired=The main/active project does not have a scene catalog; the scene cannot be saved.",
            "ERR_NullScene=A scene could not be created (it's null); perhaps the viewer doesn't support this action.",
            "LBL_SceneName=Scene Name:",
            "LBL_SceneName_Prompt=Please enter a name for this scene"
        })
public final class SaveSceneAction implements ActionListener {

    private static final Logger LOG = Logger.getLogger(SaveSceneAction.class.getName());
    private final Project context;

    public SaveSceneAction(Project context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            Project project = ProjectUtil.getCurrentProject();
            if (project == null) {
                throw new IllegalStateException(Bundle.ERR_ProjectRequired());
            }
            // Record the viewer's contents in a scene
            BasicScene.SceneFactory factory = BasicScene.SceneFactory.getInstance();
            Scene scene = factory.createScene();
            if (scene == null) {
                throw new IllegalStateException(Bundle.ERR_NullScene());
            }
            // Let the user override the default name of the scene;
            // if OK is pressed, we'll save the scene with the new name;
            // if Cancel is pressed, we'll abort
            NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(
                    Bundle.LBL_SceneName(), Bundle.LBL_SceneName_Prompt());
            d.setInputText(scene.getName());
            Object retval = DialogDisplayer.getDefault().notify(d);
            if (retval == NotifyDescriptor.OK_OPTION) {
                scene.setName(d.getInputText());
                factory.createDataObject(scene, null); // null > create in current project
            }
        } catch (IllegalStateException e) {
            LOG.warning(e.getMessage());
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(d);
        }
    }
}
