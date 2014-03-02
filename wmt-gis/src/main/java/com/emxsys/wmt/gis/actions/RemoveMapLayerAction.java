/*
 * Copyright (C) 2010, 2011 Bruce Schubert <bruce@emxsys.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.emxsys.wmt.gis.actions;

import com.emxsys.wmt.gis.capabilities.api.RemovableGisLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


@ActionID(category = "Layer", id = "com.emxsys.gis.actions.RemoveMapLayerAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/gis/images/layer-remove.png", displayName = "#CTL_RemoveMapLayer")
@ActionReferences(
{
    @ActionReference(path = "Ribbon/TaskPanes/LayerTools/View", position = 2222),
    @ActionReference(path = "Toolbars/Layer", position = 2222)
})
@Messages("CTL_RemoveMapLayer=Remove Map Layer")
public final class RemoveMapLayerAction implements ActionListener
{

    private final RemovableGisLayer context;



    public RemoveMapLayerAction(RemovableGisLayer context)
    {
        this.context = context;
    }



    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (this.context != null)
        {
            this.context.removeGisLayerFromViewer();
        }
    }
}
