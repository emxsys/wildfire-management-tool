/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.symbology;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.symbology.AbstractGraphic;
import com.emxsys.gis.api.symbology.Graphic;
import com.emxsys.gis.api.symbology.StandardIdentity;
import com.emxsys.gis.api.symbology.Status;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.symbology.editor.GraphicDesigner;
import com.emxsys.wmt.globe.util.Positions;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525GraphicFactory;
import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JPopupMenu;
import org.openide.loaders.DataNode;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * The Graphic interface manages the placement and control of surface-based graphics.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class BasicGraphic extends AbstractGraphic implements Movable {

    private static TacticalGraphicAttributes sharedNormalAttrs;
    private static TacticalGraphicAttributes sharedHiliteAttrs;
    private static TacticalGraphicAttributes sharedSelectedAttrs;
    private static TacticalGraphicAttributes sharedLockedAttrs;
    private String type;
    private boolean movable = false;
    private boolean savedMovable = false;
    private boolean editing = false;
    private DataNode node;
    private Lookup lookup;
    private InstanceContent content = new InstanceContent();
    private TacticalGraphic tacticalGraphic;
    private AVList modifiers = new AVListImpl();
    private GraphicDesigner editor;
    private static TacticalGraphicFactory factory = new MilStd2525GraphicFactory();
    private static SelectionController controller = null;
    private static final Logger logger = Logger.getLogger(BasicGraphic.class.getName());
    public static final String PROP_GRAPHIC_MOVABLE = "graphic.movable";
    public static final String PROP_GRAPHIC_ATTRIBUTES = "graphic.attributes";
    public static final String PROP_GRAPHIC_SHAPE = "graphic.shape";
    public static final int STATUS_INDEX = 3;
    public static final int BATTLE_DIM_INDEX = 2;
    public static final int STANDARD_ID_POSITION = 1;
    public static final int CODING_SCHEME_POSITION = 0;

    /**
     * Constructs a new BasicGraphic from a MILSTD-2525C symbol spec and a list of positions.
     *
     * @param symbolId defines specific symbology for graphic
     * @param coordinates used for graphic layout
     */
    public BasicGraphic(String symbolId, List<Coord3D> coordinates) {
        if (BasicGraphic.controller == null) {
            BasicGraphic.controller = GraphicSelectionController.getInstance();
        }

        // Create the Tactical Graphic!!
        makeTacticalGraphic(symbolId, coordinates);

    }

    /**
     * Implementation factory.
     *
     * @param identifier
     * @param coordinates
     */
    protected final void makeTacticalGraphic(String identifier, List<Coord3D> coordinates) {
        // Convert the Coords to WW Positions
        ArrayList<Position> positions = new ArrayList<>(coordinates.size());
        coordinates.stream().forEach((coord) -> {
            positions.add(Positions.fromCoord3D(coord));
        });
        makeTacticalGraphic(identifier, positions);
    }

    /**
     * Implementation factory.
     *
     * @param identifier
     * @param positions
     */
    protected final void makeTacticalGraphic(String identifier,
                                             Iterable<? extends Position> positions) {
        if (isEditing()) {
            stopEditor();
        }
        TacticalGraphic newGraphic = factory.createGraphic(identifier, positions, this.modifiers);
        if (newGraphic == null) {
            String msg = "Unable to create TacticalGraphic with identifier: " + identifier;
            logger.severe(msg);
            throw new IllegalArgumentException(msg);
        }
        newGraphic.setAttributes(getSharedNormalAttributes());
        newGraphic.setHighlightAttributes(getSharedHighlightAttributes());
        newGraphic.setShowGraphicModifiers(true);
        newGraphic.setShowTextModifiers(true);
        newGraphic.setDelegateOwner(this);

        // Release any existing implementation if we're replacing a graphic
        TacticalGraphic oldGraphic = this.tacticalGraphic;
        if (oldGraphic != null) {
            this.detachFromRenderer(getRenderer()); // operates on this.tacticalGraphic
            oldGraphic.setDelegateOwner(null);
            this.content.remove(oldGraphic);
        }

        // Update our member and the lookup
        this.tacticalGraphic = newGraphic;
        this.content.add(newGraphic);

        // Attach the new implementation to the renderable layer if we're replacing the current impl.
        // Normally, the creators of new BasicGraphic instances will perform this task.
        if (oldGraphic != null) {
            this.attachToRenderer(getRenderer());   // operates on this.tacticalGraphic
        }
        super.pcs.firePropertyChange(PROP_GRAPHIC_ATTRIBUTES, oldGraphic, newGraphic);
        Globe.getInstance().refreshView();
    }

    @Override
    public Lookup getLookup() {
        if (this.lookup == null) {
            this.lookup = new AbstractLookup(content);
        }
        return this.lookup;
    }

    @Override
    public void setName(String name) {
        modifiers.setValue(SymbologyConstants.UNIQUE_DESIGNATION, name); // Field T
        this.tacticalGraphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, name);
        super.setName(name);    // Fire's property change.
    }

    public DataNode getNode() {
        return node;
    }

    public void setNode(DataNode node) {
        this.node = node;
    }

    public String getIdentifier() {
        return this.tacticalGraphic.getIdentifier();
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void setSelected(boolean selected) {
        boolean oldSelected = isSelected();
        if (selected != oldSelected) {
            if (!selected) {
                // Turn off the editor
                this.tacticalGraphic.setHighlighted(false);
                stopEditor();
            }
            this.tacticalGraphic.setAttributes(selected ? getSharedSelectedAttributes() : getSharedNormalAttributes());

            // TODO: Update the selection in Viewer's explorer manager
//            WorldWindTopComponent tc = WorldWindTopComponent.findInstance();
//            if (selected)
//            {
//                WorldWindTopComponent.selectNode(node);
//            }
//            else
//            {
//                WorldWindTopComponent.deselectNode(node);
//            }
            super.setSelected(selected);    // fires property change event
        }
    }

    protected void startEditor() {
        if (this.editor == null) {
            this.editor = new GraphicDesigner(this);
        }
        this.savedMovable = isMovable();
        setMovable(false);
        this.editing = true;
        this.editor.startEditing();
    }

    protected void stopEditor() {
        if (this.editor == null) {
            return;
        }
        this.editor.stopEditing();
        this.editing = false;
        setMovable(this.savedMovable);
    }

    protected boolean isEditing() {
        return this.editing;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible != isVisible()) {
            this.tacticalGraphic.setVisible(visible);
            super.setVisible(visible);  // fires property change event
        }

    }

    public boolean isMovable() {
        return this.movable;
    }

    public void setMovable(boolean movable) {
        boolean oldMovable = isMovable();
        if (movable != oldMovable) {
            this.movable = movable;
            this.tacticalGraphic.setHighlightAttributes(movable
                    ? getSharedHighlightAttributes() : getSharedLockedHighlightAttributes());
            super.pcs.firePropertyChange(PROP_GRAPHIC_MOVABLE, oldMovable, movable);
        }
    }

    /**
     * A delegate.
     *
     * @return
     */
    @Override
    public Position getReferencePosition() {
        return this.tacticalGraphic.getReferencePosition();
    }

    /**
     * Move a specified distance and direction from the current reference point.
     *
     * @param delta vector
     */
    @Override
    public void move(Position delta) {
        if (isMovable()) {
            this.tacticalGraphic.move(delta);
            // Update owner and fire property change
            super.setPosition(Positions.toGeoCoord3D(this.tacticalGraphic.getReferencePosition()));
        }
    }

    /**
     * Moves the graphic to a specific reference position.
     *
     * @param position to move to
     */
    @Override
    public void moveTo(Position position) {
        if (isMovable()) {
            // Suspend editing
            if (isEditing()) {
                this.editor.stopEditing();
            }
            // Do the move of the implementtion
            this.tacticalGraphic.moveTo(position);

            // Resume editing
            if (isEditing()) {
                this.editor.startEditing();
            }
            // Update this object and fire property change
            super.setPosition(Positions.toGeoCoord3D(this.tacticalGraphic.getReferencePosition()));
        }
    }

    @Override
    public List<Coord3D> getPositions() {
        List<Coord3D> list = new ArrayList<>();
        Iterator<? extends Position> iterator = this.tacticalGraphic.getPositions().iterator();
        while (iterator.hasNext()) {
            list.add(Positions.toGeoCoord3D(iterator.next()));
        }
        return list;
    }

    @Override
    public void setPositions(List<Coord3D> coords) {
        List<Position> list = new ArrayList<>(coords.size());
        for (Coord3D coord : coords) {
            list.add(Positions.fromCoord3D(coord));
        }
        setPositionsImpl(list);
    }

    /**
     * Used by the GraphicDesigner.
     */
    public void setPositionsImpl(List<Position> positions) {
        this.tacticalGraphic.setPositions(positions);
        super.pcs.firePropertyChange(PROP_GRAPHIC_SHAPE, null, null);
    }

    @Override
    public Image getImage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean owns(Object impl) {
        return this.tacticalGraphic.equals(impl);
    }

    /**
     * Adds the Graphics's implementation to compatible renderers. Called by the Graphic.Renderer
     * (i.e., the GraphicLayer) when a Graphic is added to it.
     *
     * @param renderer A RenderableLayer.
     */
    @Override
    public void attachToRenderer(Graphic.Renderer renderer) {
        // Add the graphic implementation to compatible renderers.
        RenderableLayer layer = renderer.getLookup().lookup(RenderableLayer.class);
        if (layer != null) {
            layer.addRenderable(this.tacticalGraphic);
        }
    }

    /**
     * Removes the Graphic's implementation from the supplied Graphic.Renderer. Called by
     * Graphic.Renderer. Should not be called directly.
     *
     * @param renderer A RenderableLayer.
     */
    @Override
    public void detachFromRenderer(Graphic.Renderer renderer) {
        RenderableLayer layer = renderer.getLookup().lookup(RenderableLayer.class);
        if (layer != null) {
            layer.removeRenderable(this.tacticalGraphic);
        }
    }

    /**
     * Starts the attribute editor.
     */
    public void edit() {
        if (!isEditing()) {
            startEditor();
        }
    }

    /**
     * Starts the design editor
     */
    public void editShape() {
        if (!isEditing()) {
            startEditor();
        }
    }

    private static String newIdentifier(String identifier, String code, int position) {
        return identifier.substring(0, position) + code + identifier.substring(position + 1);
    }

    private static String getCharAt(String identifier, int position) {
        return identifier.substring(position, position + 1);
    }

    public StandardIdentity getStandardIdentity() {
        String identity = getCharAt(this.tacticalGraphic.getIdentifier(), STANDARD_ID_POSITION);
        return StandardIdentity.get(identity);
    }

    public void setStandardIdentity(StandardIdentity si) {
        String identifier = newIdentifier(this.tacticalGraphic.getIdentifier(), si.code, STANDARD_ID_POSITION);
        makeTacticalGraphic(identifier, this.tacticalGraphic.getPositions());
    }

    public Status getStatus() {
        String status = getCharAt(this.tacticalGraphic.getIdentifier(), STATUS_INDEX);
        return Status.get(status);
    }

    public void setStatus(Status status) {
        String identifier = newIdentifier(this.tacticalGraphic.getIdentifier(), status.code, STATUS_INDEX);
        makeTacticalGraphic(identifier, this.tacticalGraphic.getPositions());
    }

    protected static TacticalGraphicAttributes getSharedNormalAttributes() {
        // Create attributes for line and area graphics. Line and area graphics use the scale attribute to determine
        // the size of tactical symbols included in the graphic. Setting the scale to 1/4 prevents the symbol
        // overwhelming the rest of the graphic.
        if (sharedNormalAttrs == null) {
            sharedNormalAttrs = new BasicTacticalGraphicAttributes();
            sharedNormalAttrs.setOutlineWidth(3.0);
            sharedNormalAttrs.setScale(0.5);
        }
        return sharedNormalAttrs;
    }

    protected static TacticalGraphicAttributes getSharedSelectedAttributes() {
        if (sharedSelectedAttrs == null) {
            sharedSelectedAttrs = new BasicTacticalGraphicAttributes(getSharedNormalAttributes());
            sharedSelectedAttrs.setOutlineWidth(5.0);
        }
        return sharedSelectedAttrs;
    }

    protected static TacticalGraphicAttributes getSharedHighlightAttributes() {
        if (sharedHiliteAttrs == null) {
            sharedHiliteAttrs = new BasicTacticalGraphicAttributes(getSharedSelectedAttributes());
            sharedHiliteAttrs.setOutlineMaterial(Material.WHITE);
            sharedHiliteAttrs.setScale(0.7);
        }
        return sharedHiliteAttrs;
    }

    protected static TacticalGraphicAttributes getSharedLockedHighlightAttributes() {
        if (sharedLockedAttrs == null) {
            sharedLockedAttrs = new BasicTacticalGraphicAttributes(getSharedNormalAttributes());
            sharedLockedAttrs.setOutlineMaterial(Material.YELLOW);
            sharedLockedAttrs.setScale(0.7);
        }
        return sharedLockedAttrs;
    }

    /**
     * Supplies a class name that is stored in the XML files and used by the XML
     *
     * @return
     */
    @Override
    public Class<? extends Graphic.Builder> getFactoryClass() {
        return AbstractGraphicBuilder.class;
    }

    /**
     * The SelectionController listens for selection events in the viewer and either highlights a
     * selected item or shows its context menu.
     *
     * (Based on gov.nasa.worldwind.examples.ContextMenusOnShapes)
     *
     * @author Bruce
     */
    protected static class GraphicSelectionController extends SelectionController<BasicGraphic> {

        // A singleton.
        private static BasicGraphic.GraphicSelectionController INSTANCE = new BasicGraphic.GraphicSelectionController();

        public static BasicGraphic.GraphicSelectionController getInstance() {
            return INSTANCE;
        }

        private GraphicSelectionController() {
            attachToViewer();
        }

        @Override
        protected void doOpen(SelectEvent event, BasicGraphic graphic) {
            graphic.editShape();
        }

        @Override
        protected void doContextMenu(SelectEvent event, BasicGraphic graphic) {
            // Select the node in the ExplorerManager
            graphic.setSelected(true);

            JPopupMenu contextMenu = graphic.node.getContextMenu();
            if (contextMenu != null) {
                contextMenu.show((Component) event.getSource(),
                        event.getMouseEvent().getX(),
                        event.getMouseEvent().getY());
            }
        }

        @Override
        protected boolean doSetHighlight(SelectEvent event, BasicGraphic graphic, boolean value) {
            if (graphic.editing) {
                return false;
            }
            graphic.tacticalGraphic.setHighlighted(value);
            return true;
        }

        /**
         * @return false if the selection change was vetoed.
         */
        @Override
        protected boolean doSetSelected(SelectEvent event, BasicGraphic graphic, boolean value) {
            // If editing, veto the change to 'deselect' if the event is from the editor's
            // control points or lines.
            if (value == false && graphic.editing && graphic.editor.owns(event.getTopObject())) {
                return false;
            }
            graphic.setSelected(value);
            return true;
        }

        @Override
        protected BasicGraphic getInstance(Object obj) {
            return (obj instanceof BasicGraphic) ? (BasicGraphic) obj : null;
        }
    }
}
