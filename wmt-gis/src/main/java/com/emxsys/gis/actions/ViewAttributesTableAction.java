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
package com.emxsys.gis.actions;

import com.emxsys.gis.api.capabilities.ViewableAttributes;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Layer", id = "com.emxsys.gis.actions.ViewAttributesTable")
@ActionRegistration(iconBase = "com/emxsys/gis/images/attributes-display.png", displayName = "#CTL_ViewAttributesTable")
@ActionReferences({
    @ActionReference(path = "Ribbon/TaskPanes/LayerTools/View", position = 3333),
    @ActionReference(path = "Toolbars/Layer", position = 3333)
})
@Messages("CTL_ViewAttributesTable=View Attributes Table")
public final class ViewAttributesTableAction implements ActionListener {

    private final ViewableAttributes context;

    public ViewAttributesTableAction(ViewableAttributes context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (this.context != null) {
            this.context.viewAttributes();
        }
    }
}
