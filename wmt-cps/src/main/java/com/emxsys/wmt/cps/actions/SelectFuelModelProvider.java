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

import com.emxsys.wmt.gis.api.GeoSector;
import com.emxsys.wmt.gis.api.Box;
import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.wildfire.api.StdFuelModel;
import com.emxsys.wmt.wildfire.api.FuelModel;
import com.emxsys.wmt.wildfire.api.FuelModelProvider;
import com.emxsys.wmt.wildfire.api.StdFuelModelParams13;
import com.emxsys.wmt.wildfire.api.StdFuelModelParams40;
import com.emxsys.wmt.wildfire.spi.FuelModelFactory;
import java.awt.event.ActionEvent;
import java.util.Collection;
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
import org.openide.util.Lookup;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(id = "com.emxsys.wmt.cps.actions.SelectFuelModelProvider", category = "Fire")
public class SelectFuelModelProvider extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(SelectFuelModelProvider.class.getName());
    private Box context;
    private FuelModelProvider provider;

    public SelectFuelModelProvider(Box context) {
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
        AbstractListModel<FuelModelFactory> factories = getFuelModelFactories();

        // Display list
        JList jList = new JList(factories);
        DialogDescriptor d = new DialogDescriptor(jList, "Select a Fuel Model Provider");
        Object result = DialogDisplayer.getDefault().notify(d);
        if (result == NotifyDescriptor.OK_OPTION) {
            FuelModelFactory factory = (FuelModelFactory) jList.getSelectedValue();
            if (factory == null) {
                throw new RuntimeException("No Fuel Model Provider selected");
            }

            // Process < New > selections
            if (factory instanceof DummyFactory) {
                DummyFactory dummy = (DummyFactory) factory;

                // Get the list of individual fuel models that this factory represents
                JList jListModels = new JList(dummy.getFuelModels());
                DialogDescriptor dd2 = new DialogDescriptor(jListModels, "Select a Fuel Model");
                Object result2 = DialogDisplayer.getDefault().notify(dd2);
                if (result2 == NotifyDescriptor.OK_OPTION) {
                    // Create a FuelModelProvider from the chosen fuel model
                    FuelModel fuelModel = (FuelModel) jListModels.getSelectedValue();
                    factory = FuelModelFactory.newInstance(context, fuelModel);
                } else {
                    throw new RuntimeException("No Fuel Model selected");
                }
            }
            // All done
            this.provider = factory;
        }

    }

    /**
     *
     * @return A collection of FuelModelProviders that are valid for the sector
     */
    private AbstractListModel<FuelModelFactory> getFuelModelFactories() {
        DefaultListModel<FuelModelFactory> listModel = new DefaultListModel<>();

        // Get all the factories that may have data within the extents
        List<FuelModelFactory> instances = FuelModelFactory.getInstances(context);

        // Add two <New> entries so the user can specify a given fuel model for the sector
        listModel.addElement(new Dummy13FuelModelFactory());
        listModel.addElement(new Dummy40FuelModelFactory());
        // Now add all the candidate factories
        for (FuelModelFactory fuelModelFactory : instances) {
            listModel.addElement(fuelModelFactory);
        }
        return listModel;
    }

    /**
     * A Dummy factory that can be placed in the AbstractListModel<FuelModelFactory> list model.
     */
    interface DummyFactory {

        AbstractListModel<FuelModel> getFuelModels();
    }

    private static class Dummy40FuelModelFactory extends FuelModelFactory implements DummyFactory {

        @Override
        public AbstractListModel<FuelModel> getFuelModels() {
            DefaultListModel<FuelModel> list = new DefaultListModel<>();
            int index = 0;
            for (StdFuelModelParams40 fbfm : StdFuelModelParams40.values()) {
                list.add(index++, StdFuelModel.getFuelModel(fbfm.getModelNo()));
            }
            return list;
        }

        @Override
        public String getName() {
            return "Standard 40 Fuel Model";
        }

        @Override
        public String getSource() {
            return "<New>";
        }

        @Override
        public Box getExtents() {
            return new GeoSector(-90, -180, 90, 180);
        }

        @Override
        public FuelModel getFuelModel(Coord2D location) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private static class Dummy13FuelModelFactory extends FuelModelFactory implements DummyFactory {

        public AbstractListModel<FuelModel> getFuelModels() {
            DefaultListModel<FuelModel> list = new DefaultListModel<>();
            int index = 0;
            for (StdFuelModelParams13 fbfm : StdFuelModelParams13.values()) {
                list.add(index++, StdFuelModel.getFuelModel(fbfm.getModelNo()));
            }
            return list;
        }

        @Override
        public String getName() {
            return "Original 13 Fuel Model";
        }

        @Override
        public String getSource() {
            return "< New >";
        }

        @Override
        public Box getExtents() {
            return new GeoSector(-90, -180, 90, 180);
        }

        @Override
        public FuelModel getFuelModel(Coord2D location) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
