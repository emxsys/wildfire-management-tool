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

import com.emxsys.wmt.gis.capabilities.api.AddableGisLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


@ActionID(category = "Layer", id = "com.emxsys.gis.actions.AddMapLayerAction")
@ActionRegistration(iconBase = "com/emxsys/wmt/gis/images/layer-add.png", displayName = "#CTL_AddMapLayer")
@ActionReferences(
{
    @ActionReference(path = "Menu/Insert/Layer", position = 1111),
    @ActionReference(path = "Toolbars/Layer", position = 1111)
})
@Messages("CTL_AddMapLayer=Add Map Layer")
/**
 * This context sensitive action class based simply on an ActionListener.
 */
public final class AddMapLayerAction implements ActionListener
{

    private final AddableGisLayer context;


    public AddMapLayerAction(AddableGisLayer context)
    {
        this.context = context;
    }


    @Override
    public void actionPerformed(ActionEvent ev)
    {
        if (this.context != null)
        {
            this.context.addGisLayerToViewer();
        }
    }
}
