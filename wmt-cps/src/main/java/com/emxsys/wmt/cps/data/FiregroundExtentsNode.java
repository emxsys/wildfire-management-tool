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
package com.emxsys.wmt.cps.data;

import com.emxsys.wmt.cps.actions.DeleteSectorFromFiregroundAction;
import com.emxsys.wmt.cps.fireground.WildlandFireground;
import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.Coord2D;
import com.emxsys.wildfire.api.Fireground;
import com.emxsys.wildfire.api.FuelModelProvider;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * This class provides a {@link org.openide.nodes.Node} for the {@link Fireground}'s {@code extents}
 * member. This node represents the geographic extents of the fireground.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class FiregroundExtentsNode extends AbstractNode {

    public FiregroundExtentsNode(Fireground fireground) {
        super(Children.create(new ExtentsChildren(fireground), true),
                Lookups.singleton(fireground.getSectors()));
    }

    @Override
    public String getDisplayName() {
        return "Sectors";
    }

    /**
     * ChildFactory for the ExtentsNode
     */
    private static class ExtentsChildren extends ChildFactory<Box> implements PropertyChangeListener {

        private final Fireground fireground;

        @SuppressWarnings("LeakingThisInConstructor")
        ExtentsChildren(Fireground fireground) {
            this.fireground = fireground;
            fireground.addPropertyChangeListener(WeakListeners.propertyChange(this, fireground));
        }

        @Override
        protected boolean createKeys(List<Box> toPopulate) {
            for (Box box : this.fireground.getSectors()) {
                toPopulate.add(box);
            }
            return true;
        }

        @Override
        protected Node[] createNodesForKey(Box key) {
            return new Node[]{
                new SectorNode(key),
                new FuelModelProviderNode((fireground.getFuelModelProvider(key)))
            };
        }

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (WildlandFireground.SECTOR_ADDED_EVENT.equals(pce.getPropertyName())
                    || WildlandFireground.SECTOR_REMOVED_EVENT.equals(pce.getPropertyName())) {
                refresh(true);  // boolean = refresh immediately? 
            }

        }
    }

    /**
     * Child Node representing a sector.
     */
    public static class SectorNode extends AbstractNode {

        public SectorNode(Box sector) {
            super(Children.LEAF, Lookups.singleton(sector));
            setName(sector.toString());
        }

        public Box getSector() {
            return getLookup().lookup(Box.class);
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Image getOpenedIcon(int i) {
            return getIcon(i);
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage("com/emxsys/wmt/cps/images/region.png");
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                new DeleteSectorFromFiregroundAction(getSector())
            };
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set set = Sheet.createPropertiesSet();
            set.setName("southwest"); // use a unique name to prevent overwriting existing file-based property set
            set.setDisplayName("Southwest coordinates");
            set.put(new SectorNode.LatitudeProperty(getSector().getSouthwest()));
            set.put(new SectorNode.LongitudeProperty(getSector().getSouthwest()));
            sheet.put(set);

            set = Sheet.createPropertiesSet();
            set.setName("northeast"); // use a unique name to prevent overwriting existing file-based property set
            set.setDisplayName("Northeast coordinates");
            set.put(new SectorNode.LatitudeProperty(getSector().getNortheast()));
            set.put(new SectorNode.LongitudeProperty(getSector().getNortheast()));
            sheet.put(set);

            set = Sheet.createPropertiesSet();
            set.setName("dimensions"); // use a unique name to prevent overwriting existing file-based property set
            set.setDisplayName("Dimensions");
            set.put(new SectorNode.WidthProperty(getSector()));
            sheet.put(set);

            return sheet;
        }

        private class LatitudeProperty extends PropertySupport.ReadOnly<Double> {

            private Coord2D point;

            LatitudeProperty(Coord2D point) {
                super("latitude", Double.class, "Latitude", "Degrees of Latitude (North/South)");
                this.point = point;
            }

            @Override
            public Double getValue() throws IllegalAccessException, InvocationTargetException {
                return this.point.getLatitudeDegrees();
            }
        }

        private class LongitudeProperty extends PropertySupport.ReadOnly<Double> {

            private Coord2D point;

            LongitudeProperty(Coord2D point) {
                super("longitude", Double.class, "Longitude", "Degrees of Longitude (East/West)");
                this.point = point;
            }

            @Override
            public Double getValue() throws IllegalAccessException, InvocationTargetException {
                return this.point.getLongitudeDegrees();
            }
        }

        private class WidthProperty extends PropertySupport.ReadOnly<Double> {

            private Box sector;

            WidthProperty(Box sector) {
                super("width", Double.class, "Sector Width", "Sector width measured in degrees of longitude");
                this.sector = sector;
            }

            @Override
            public Double getValue() throws IllegalAccessException, InvocationTargetException {
                return this.sector.getWidth().getValue();
            }
        }

        private class HeightProperty extends PropertySupport.ReadOnly<Double> {

            private Box sector;

            HeightProperty(Box sector) {
                super("height", Double.class, "Sector Height", "Sector width measured in degrees of latitude");
                this.sector = sector;
            }

            @Override
            public Double getValue() throws IllegalAccessException, InvocationTargetException {
                return this.sector.getWidth().getValue();
            }
        }
    }

    /**
     * Child Node representing the FuelModelProvider.
     */
    public static class FuelModelProviderNode extends AbstractNode {

        public FuelModelProviderNode(FuelModelProvider provider) {
            super(Children.LEAF, provider == null ? null : Lookups.singleton(provider));
            setName(provider == null ? "Fuels Undefined" : provider.toString());
        }

        public Box getSector() {
            return getLookup().lookup(Box.class);
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Image getOpenedIcon(int i) {
            return getIcon(i);
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage("com/emxsys/wmt/cps/images/Fire_Location.png");
        }

    }
}
