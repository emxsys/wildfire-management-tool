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
package com.emxsys.wmt.cps.actions;

import com.emxsys.wmt.gis.api.Box;
import com.emxsys.wmt.wildfire.api.FuelModelProvider;
import com.emxsys.wmt.wildfire.spi.DefaultFuelModelProvider;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;

/**
 * This action displays a dialog for the user to choose a FuelModelProvider for the given sector
 * (Box context). The selected provider can be obtained via the getFuelModelProvider after the
 * action is performed
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(id = "com.emxsys.wmt.cps.actions.SelectFuelModelProvider", category = "Fire")
public class SelectFuelModelProviderAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(SelectFuelModelProviderAction.class.getName());
    private final Box context;
    private FuelModelProvider provider;

    public SelectFuelModelProviderAction(Box context) {
        this.context = context;
    }

    /**
     * Gets the selected FuelModelProvider.
     *
     * @return the FuelModelProvider chosen by the user; may be null.
     */
    public FuelModelProvider getFuelModelProvider() {
        return this.provider;
    }

    @Override
    public void actionPerformed(ActionEvent ignored) {
        // Collect all the available fuel model factories for this sector
        AbstractListModel<FuelModelProvider> listModel = getFuelModelFactories();

        // Display list
        JList jList = new JList(listModel);
        DialogDescriptor d = new DialogDescriptor(jList, "Select a Fuel Model Provider");
        Object result = DialogDisplayer.getDefault().notify(d);
        if (result == NotifyDescriptor.OK_OPTION) {
            FuelModelProvider factory = (FuelModelProvider) jList.getSelectedValue();
            if (factory == null) {
                throw new RuntimeException("No Fuel Model Provider selected");
            }
            // TODO: could collect all providers with the same source into groups
//            // Process < New > selections
//            if (factory instanceof FuelListModelProvider) {
//                FuelListModelProvider list = (FuelListModelProvider) factory;
//
//                // Get the list of individual fuel models that this factory represents
//                JList jListModels = new JList(list.getFuelModels());
//                DialogDescriptor dd2 = new DialogDescriptor(jListModels, "Select a Fuel Model");
//                Object result2 = DialogDisplayer.getDefault().notify(dd2);
//                if (result2 == NotifyDescriptor.OK_OPTION) {
//                    // Create a FuelModelProvider from the chosen fuel model
//                    FuelModel fuelModel = (FuelModel) jListModels.getSelectedValue();
//                    factory = FuelModelProviderFactory.newInstance(context, fuelModel);
//                } else {
//                    throw new RuntimeException("No Fuel Model selected");
//                }
//            }
            // All done
            this.provider = factory;
        }

    }

    /**
     *
     * @return A collection of FuelModelProviders that are valid for the sector
     */
    private AbstractListModel<FuelModelProvider> getFuelModelFactories() {
        DefaultListModel<FuelModelProvider> listModel = new DefaultListModel<>();

        // Get all the factories that may have data within the extents
        List<FuelModelProvider> instances = DefaultFuelModelProvider.getInstances(context);
        instances.stream().forEach((instance) -> {
            listModel.addElement(instance);
        });
        return listModel;
    }

}
