/*
 * Copyright (c) 2009-2014, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.markers;

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.GeoCoord3D;
import com.emxsys.wmt.gis.api.marker.AbstractMarker;
import com.emxsys.wmt.gis.api.marker.Marker;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.util.Positions;
import com.terramenta.globe.GlobeTopComponent;
import com.terramenta.globe.dnd.Draggable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Component;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;
import org.openide.util.NotImplementedException;
import org.openide.windows.WindowManager;

/**
 * A BasicMarker implements a PointPlacemark on the WorldWind globe.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class BasicMarker extends AbstractMarker {

    public static final String PROP_MARKER_MOVABLE = "marker.movable";
    public static final String PROP_MARKER_ATTRIBUTES = "marker.attributes";
    private static final Logger logger = Logger.getLogger(BasicMarker.class.getName());
    private static MarkerSelectListener selectListener = null;
    private String markerID;
    private boolean movable = true;
    private PointPlacemarkAdapter impl;
    private Image image = null;
    private Node node = null;
    private final Object imageLock = new Object();

    static {
        logger.setLevel(Level.ALL);
    }

    /**
     * Implementation class overrides move operations to keep the BasicMarker position synchronized
     * with the implementation position.
     *
     * @author Bruce
     */
    private class PointPlacemarkAdapter extends PointPlacemark implements Draggable {

        GeoCoord3D syncPosition;
        BasicMarker owner;

        PointPlacemarkAdapter(Position position, BasicMarker owner) {
            super(position);
            this.owner = owner;
            this.syncPosition = Positions.toGeoCoord3D(position);
            initializeDefaultAttributes();
        }

        private void initializeDefaultAttributes() {
            Preferences pref = NbPreferences.forModule(getClass());
            double scale = pref.getDouble("basic_marker.scale", 1.0);
            double imageOffsetX = pref.getDouble("basic_marker.image_offset_x", 0.3);
            double imageOffsetY = pref.getDouble("basic_marker.image_offset_y", 0.0);
            double labelOffsetX = pref.getDouble("basic_marker.label_offset_x", 0.9);
            double labelOffsetY = pref.getDouble("basic_marker.label_offset_y", 0.6);

            PointPlacemarkAttributes attributes = new PointPlacemarkAttributes();
            attributes.setScale(scale);
            attributes.setImageOffset(new Offset(imageOffsetX, imageOffsetY, AVKey.FRACTION, AVKey.FRACTION));
            attributes.setLabelOffset(new Offset(labelOffsetX, labelOffsetY, AVKey.FRACTION, AVKey.FRACTION));
            attributes.setUsePointAsDefaultImage(false);

            this.setAttributes(attributes);
        }

        @Override
        public void move(Position delta) {
            if (owner.isMovable()) {
                super.move(delta);  // calls moveTo
            }
        }

        /**
         * Synchronizes the BasicMarker to the implementation when the marker is moved via a mouse.
         * @param position The new position to move to.
         */
        @Override
        public void moveTo(Position position) {
            if (owner.isMovable()) {
                syncPosition = Positions.toGeoCoord3D(position);
                super.moveTo(position);
                owner.setPosition(syncPosition);
            }
        }

        @Override
        public boolean isDraggable() {
            return owner.isMovable();
        }

        @Override
        public void setDraggable(boolean draggable) {
            owner.setMovable(draggable);
        }

        /**
         * Called by Terramenta DragController.
         * @param position New position.
         */
        @Override
        public void setPosition(Position position) {
                syncPosition = Positions.toGeoCoord3D(position);
                super.setPosition(position); 
                owner.setPosition(syncPosition);
        }
        
        
    }

    /**
     * Constructor with an invalid position.
     */
    public BasicMarker() {
        this(GeoCoord3D.INVALID_POSITION);        
    }

    /**
     * Constructor with a given coordinate.
     * @param coord
     */
    public BasicMarker(Coord3D coord) {
        super.setPosition(coord);
        this.impl = new PointPlacemarkAdapter(Positions.fromCoord3D(coord), this);
        this.markerID = UUID.randomUUID().toString();

        // Add the renderable to the lookup so the MarkerLayer can find it and render it.
        getInstanceContent().add(this.impl);

        // Inititialize the highlight/selection controller
        if (BasicMarker.selectListener == null) {
            BasicMarker.selectListener = new MarkerSelectListener();
            BasicMarker.selectListener.attachToGlobe();
        }
    }

    /**
     * Sets the Node that represents this marker, e.g., in the Project Manager and in GisViewer.
     *
     * @param node representative node
     */
    void setNode(Node node) {
        this.node = node;
    }

    /**
     * Sets the implementation rendering attributes
     *
     * @param attributes A PointPlacemarkAttributes instance.
     * @see PointPlacemarkAttributes
     */
    public void setAttributes(Object attributes) {
        if (attributes instanceof PointPlacemarkAttributes) {
            PointPlacemarkAttributes oldAttr = this.impl.getAttributes();
            this.impl.setAttributes((PointPlacemarkAttributes) attributes);
            pcs.firePropertyChange(PROP_MARKER_ATTRIBUTES, oldAttr, attributes);

            setImage(getImageFromAttributes());
        } else {
            throw new IllegalArgumentException("setAttributes: argument not compatible with PointPlacemarkAttributes");
        }
    }

    @Override
    public void setName(String name) {
        this.impl.setLabelText(name);
        super.setName(name);    // Fire's property change.
    }

    @Override
    public void setPosition(Coord3D coord) {
        if (getPosition().equals(coord)) {
            return;
        }
        if (this.impl.syncPosition != coord) {
            this.impl.setPosition(Positions.fromCoord3D(coord));
        }
        super.setPosition(coord); // fires property change
    }

    @Override
    public String getUniqueID() {
        return this.markerID;
    }

    @Override
    public void setUniqueID(String uniqueID) {
        this.markerID = uniqueID;
    }

    @Override
    public Image getImage() {
        if (this.image == null) {
            this.image = getImageFromAttributes();
        }
        return this.image;
    }

    private Image getImageFromAttributes() {
        Image attrImage = null;
        PointPlacemarkAttributes attrs = this.impl.getAttributes();
        if (attrs != null) {
            String imageAddress = attrs.getImageAddress();
            if (imageAddress != null) {
                try {
                    ImageIcon icon = new ImageIcon(new URL(imageAddress));
                    attrImage = ImageUtilities.icon2Image(icon);
                } catch (MalformedURLException exception) {
                    logger.log(Level.WARNING, "getImageFromAttributes failed to load [{0}]. Reason: {1}", new Object[]{
                        imageAddress, exception.toString()
                    });
                }
            }
        }
        return attrImage;
    }

    @Override
    public void setImage(Image image) {
        Image oldImage = this.image;
        synchronized (this.imageLock) {
            if (this.image != image) {
                this.image = image;
            }
        }
        super.pcs.firePropertyChange(PROP_MARKER_SYMBOL, oldImage, this.image);
    }

    /**
     * Launches the marker editor. Subclasses should override.
     */
    public void edit() {
        throw new NotImplementedException("BasicMarker.edit not implemented.");
    }

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean movable) {
        boolean oldMovable = isMovable();
        if (movable != oldMovable) {
            this.movable = movable;
            super.pcs.firePropertyChange(PROP_MARKER_MOVABLE, oldMovable, movable);
        }
    }

    /**
     * Activates the logical Node when the marker is selected.
     *
     * @param selected
     */
    @Override
    public void setSelected(boolean selected) {
        boolean oldSelected = isSelected();
        if (selected != oldSelected) {

            if (node != null) {
                // Update the selection in Viewer's explorer manager
                GlobeTopComponent tc = (GlobeTopComponent) Globe.getInstance().getGlobeTopComponent();
                ExplorerManager em = tc.getExplorerManager();

                if (selected) {
                    em.setRootContext(node);
                    em.setExploredContext(node, new Node[]{
                        node
                    });
                } // Deselect only if THIS node is currently selected
                else if (em.getRootContext() == node) {
                    em.setExploredContext(null);
                }
            }
            // Highlight the marker
            this.impl.setHighlighted(selected);
            super.setSelected(selected);  // Fires property change
        }
    }

    @Override
    public void setVisible(boolean visible) {
        boolean oldVisible = isVisible();
        this.impl.isVisible();
        if (visible != oldVisible || visible != this.impl.isVisible()) {
            this.impl.setVisible(visible);
            super.setVisible(visible);    // fires property change
        }

    }

    @Override
    public Class<? extends Marker.Builder> getFactoryClass() {
        return AbstractMarkerBuilder.class;
    }

    @Override
    public String toString() {
        return "BasicMarker{" + "name=" + getName() + ", position=" + getPosition() + ", markerID=" + markerID + '}';
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 43 * hash + (this.markerID != null ? this.markerID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BasicMarker other = (BasicMarker) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if ((this.markerID == null) ? (other.markerID != null) : !this.markerID.equals(other.markerID)) {
            return false;
        }
        return true;
    }

    /**
     * The MarkerSelectListener listens for selection events in the viewer and either highlights a
     * selected item or shows its context menu.
     *
     * (Based on gov.nasa.worldwind.examples.ContextMenusOnShapes)
     *
     * @author Bruce Schubert
     */
    protected static class MarkerSelectListener implements SelectListener {

        private PointPlacemarkAdapter lastPickedPlacemark = null;
        private PointPlacemarkAdapter lastSelectedPlacemark = null;

        /**
         * Defers attaching the MarkerSelectListener to the WorldWindow view until the UI is ready.
         * FYI: at startup, markers can be loaded from a project file before the top component is
         * loaded.
         */
        public void attachToGlobe() {
            WindowManager.getDefault().invokeWhenUIReady(() -> {
                WorldWindowGLJPanel wwd = Globe.getInstance().getWorldWindManager().getWorldWindow();
                if (wwd == null) {
                    logger.severe("attachToGlobe failed. The MarkerSelectListener is not attached to a WorldWindow. "
                            + "Marker highlighting and selection will be disabled.");
                    return;
                }
                wwd.addSelectListener(MarkerSelectListener.this);
            });
        }

        @Override
        public void selected(SelectEvent event) {

            try {
                switch (event.getEventAction()) {
                    case SelectEvent.ROLLOVER:
                        highlightMarker(event, event.getTopObject());
                        break;
                    case SelectEvent.RIGHT_PRESS:
                        // TODO: use context sensitive actions from the Node
                        updateNodeSelection(event, event.getTopObject());
                        showContextMenu(event);
                        break;
                    case SelectEvent.LEFT_PRESS:
                        updateNodeSelection(event, event.getTopObject());
                        break;
                    case SelectEvent.LEFT_DOUBLE_CLICK:
                        openMarker(event);
                        break;
                }
            } catch (Exception e) {
                logger.warning(e.getMessage() != null ? e.getMessage() : e.toString());
            }
        }

        protected void highlightMarker(SelectEvent event, Object obj) {
            // Do nothing if same thing selected.
            if (this.lastPickedPlacemark == obj) {
                return; // same thing selected
            }
            // Turn off currently highlighted pushpin
            if (this.lastPickedPlacemark != null) {
                this.lastPickedPlacemark.setHighlighted(false);
                this.lastPickedPlacemark = null;
            }
            // Turn on highlight for pushpins when they are selected.
            if (obj instanceof PointPlacemarkAdapter) {
                this.lastPickedPlacemark = (PointPlacemarkAdapter) obj;
                this.lastPickedPlacemark.setHighlighted(true);
            }
        }

        /**
         * Called by selected() to synchronize node selection with gis viewer selection.
         * @param event
         * @param obj
         */
        protected void updateNodeSelection(SelectEvent event, Object obj) {
            if (this.lastSelectedPlacemark == obj) {
                return; // same thing selected
            }
            // Turn off selection if on (muliple selection is not supported)
            if (this.lastSelectedPlacemark != null) {
                this.lastSelectedPlacemark.owner.setSelected(false);
                this.lastSelectedPlacemark = null;
            }

            if (obj instanceof PointPlacemarkAdapter) {
                this.lastSelectedPlacemark = (PointPlacemarkAdapter) obj;
                this.lastSelectedPlacemark.owner.setSelected(true);
            }
        }

        /**
         * Called on left double-click
         * @param event
         */
        protected void openMarker(SelectEvent event) {
            // Only one option, so invoke it instead of showing menu...
            if ((event.getTopObject() instanceof PointPlacemarkAdapter)) {
                PointPlacemarkAdapter placemark = (PointPlacemarkAdapter) event.getTopObject();
                placemark.owner.edit();
            }

        }

        /**
         * Called on right-click
         * @param event
         */
        protected void showContextMenu(SelectEvent event) {
            // Only one option, so invoke it instead of showing menu...
            if ((event.getTopObject() instanceof PointPlacemarkAdapter)) {
                PointPlacemarkAdapter placemark = (PointPlacemarkAdapter) event.getTopObject();
                Node node = placemark.owner.node;
                if (node != null) {
                    JPopupMenu contextMenu = node.getContextMenu();
                    if (contextMenu != null) {
                        contextMenu.show((Component) event.getSource(),
                                event.getMouseEvent().getX(),
                                event.getMouseEvent().getY());
                    }
                }
            }
        }
    }
}
