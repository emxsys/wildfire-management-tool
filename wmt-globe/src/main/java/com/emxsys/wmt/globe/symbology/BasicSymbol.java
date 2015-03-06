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
import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.symbology.AbstractSymbol;
import com.emxsys.gis.api.symbology.Symbol;
import com.emxsys.gis.api.symbology.StandardIdentity;
import com.emxsys.gis.api.symbology.Status;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.symbology.editor.SymbolEditor;
import com.emxsys.wmt.globe.util.Positions;
import com.terramenta.globe.GlobeTopComponent;
import com.terramenta.globe.dnd.Draggable;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.symbology.BasicTacticalSymbolAttributes;
import gov.nasa.worldwind.symbology.SymbologyConstants;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525TacticalSymbol;
import java.awt.Component;
import java.awt.Image;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.media.opengl.GLContext;
import javax.swing.JPopupMenu;
import org.openide.explorer.ExplorerManager;
import org.openide.loaders.DataNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;

/**
 * The Symbol interface manages the placement of and control of bill-boarded symbols.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages({
    "# {0} - symbol name",
    "info_attaching_symbol_to_renderer=Attaching tactical symbol ({0}) to the supplied renderer.",
    "err_tac_symbol_impl_is_null=Tactical Symbol implementation object cannot be null.",
    "# {0} - milstd2525 id",
    "err_cannot_create_symbol_identifier=Cannot create a tactical system for {0}."
})
public class BasicSymbol extends AbstractSymbol implements Movable, Draggable {

    public static final String PROP_SYMBOL_MOVABLE = "symbol.movable";
    public static final String PROP_SYMBOL_ATTRIBUTES = "symbol.attributes";
    public static final String PROP_SYMBOL_IMAGE = "symbol.image";
    public static final int STATUS_INDEX = 3;
    public static final int BATTLE_DIM_INDEX = 2;
    public static final int STANDARD_ID_POSITION = 1;
    public static final int CODING_SCHEME_POSITION = 0;
    private String type;
    private boolean movable = true;
    private MilStd2525TacticalSymbol impl;
    private DataNode node;
    private Image image;
    private InstanceContent content = new InstanceContent();
    private Lookup lookup;
    private static BasicTacticalSymbolAttributes normalSharedAttr = null;
    private static BasicTacticalSymbolAttributes highlightedSharedAttr = null;
    private static BasicTacticalSymbolAttributes selectedSharedAttr = null;
    private static SymbolSelectionController selectController;

    private static final Logger logger = Logger.getLogger(BasicSymbol.class.getName());


    /**
     * Implementation class. The drag capability is provided by the Terramenta Globe via the
     * Draggable interface.
     */
    private static class TacticalSymbol extends MilStd2525TacticalSymbol {

        private GLContext lastContext;
        private BasicSymbol owner;

        TacticalSymbol(String symbolId, Position position, BasicSymbol owner) {
            super(symbolId, position);
        }

        @Override
        public void render(DrawContext dc) {
            GLContext currContext = dc.getGLContext();
            if (this.lastContext != currContext) {
                Preferences preferences = NbPreferences.forModule(BasicSymbol.class);

                // HACK: Toggle text modifiers on/off to force update of internal TextRenderer
                this.setShowTextModifiers(false);
                this.setShowTextModifiers(preferences.getBoolean("tactical.symbol.show.text.modifiers", true));
                this.lastContext = currContext;
            }
            super.render(dc); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected void drawTextModifiers(DrawContext dc) {
            super.drawTextModifiers(dc);
        }

    }

    /**
     * Factory method for creating the TacticalSymbol implementation object.
     *
     * @param symbolId
     * @param position
     * @param owner
     * @return a MilStd2525TacticalSymbol for the symbolId, or null on error.
     */
    static MilStd2525TacticalSymbol createTacticalSymbol(String symbolId, Position position,
                                                         BasicSymbol owner) {
        MilStd2525TacticalSymbol tacsym = new TacticalSymbol(symbolId, position, owner);
        // Create normal and highlight attribute bundles that are shared by all tactical symbols. 
        // Changes to these attribute bundles are reflected in all symbols. 
        // TODO: update these static vars when the preferences change
        Preferences preferences = NbPreferences.forModule(BasicSymbol.class);
        if (normalSharedAttr == null) {
            normalSharedAttr = new BasicTacticalSymbolAttributes();
            normalSharedAttr.setScale(preferences.getDouble("tactical.symbol.default.scale", 0.5));
            normalSharedAttr.setOpacity(preferences.getDouble("tactical.symbol.default.opacity", 0.8));
        }
        if (highlightedSharedAttr == null) {
            highlightedSharedAttr = new BasicTacticalSymbolAttributes();
            highlightedSharedAttr.setScale(preferences.getDouble("tactical.symbol.highlighed.scale", 0.6));
            highlightedSharedAttr.setOpacity(preferences.getDouble("tactical.symbol.highlighted.opacity", 0.8));
        }
        if (selectedSharedAttr == null) {
            selectedSharedAttr = new BasicTacticalSymbolAttributes();
            selectedSharedAttr.setScale(preferences.getDouble("tactical.symbol.selected.scale", 0.5));
            selectedSharedAttr.setOpacity(preferences.getDouble("tactical.symbol.selected.opacity", 1.0));
        }

        tacsym.setAttributes(normalSharedAttr);
        tacsym.setHighlightAttributes(highlightedSharedAttr);
        tacsym.setShowFrame(preferences.getBoolean("tactical.symbol.show.frame", true));
        tacsym.setShowFill(preferences.getBoolean("tactical.symbol.show.fill", true));
        tacsym.setShowIcon(preferences.getBoolean("tactical.symbol.show.icon", true));
        tacsym.setShowLocation(preferences.getBoolean("tactical.symbol.show.location", false));
        tacsym.setShowHostileIndicator(preferences.getBoolean("tactical.symbol.show.hostile.indicator", false));
        tacsym.setShowGraphicModifiers(preferences.getBoolean("tactical.symbol.show.graphic.modifiers", true));
        tacsym.setShowTextModifiers(preferences.getBoolean("tactical.symbol.show.text.modifiers", true));

        // Delegate the implementation's drag and selection/highlight operations to the BasicSymbol
        tacsym.setDelegateOwner(owner);

        return tacsym;
    }

    /**
     * Constructs a Symbol with an invalid position.
     */
    public BasicSymbol() {
        super.setCoordinates(GeoCoord3D.INVALID_COORD);
        // Add persistance capability
        this.content.add(new BasicSymbolWriter(this));
    }

    /**
     * Constructs a new Symbol from a MILSTD-2525C symbol ID and at the given position.
     *
     * @param symbolId A 15-character alphanumeric symbol identification code (SIDC).
     * @param coord The coordinate where the symbol is placed.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public BasicSymbol(String symbolId, Coord3D coord) {
        // Add persistance capability
        this.content.add(new BasicSymbolWriter(this));

        // Create the implementation
        assignTacticalSymbol(symbolId, coord);
    }

    /**
     * Assigns new tactical symbol implementation to an uninitialized object.
     *
     * @param symbolId A 15-character alphanumeric symbol identification code (SIDC).
     * @param coord used for symbol
     */
    final void assignTacticalSymbol(String symbolId, Coord3D coord) {
        //
        if (this.impl != null) {
            throw new IllegalStateException("this.impl != null.  Use replaceTacticalSymbol instead.");
        }
        super.setCoordinates(coord);

        // Force the SelectionController to be instanciated 
        if (selectController == null) {
            selectController = SymbolSelectionController.getInstance();
        }
        
        // Validate the standard identity - assign the 'friend' status if none is given
        if (symbolId.substring(1, 2).equals("-")) {
            symbolId = symbolId.charAt(0) + "F" + symbolId.substring(2);
        }

        // Create the implementation
        this.impl = createTacticalSymbol(symbolId, Positions.fromCoord3D(coord), this);

        // Update our lookup...this allows the renderer to access and modify the 
        // rendering attributes.
        this.content.add(this.impl);

        updateImage();
    }

    public Lookup getLookup() {
        if (this.lookup == null) {
            this.lookup = new AbstractLookup(this.content);
        }
        return this.lookup;
    }

    /**
     * Adds the Symbol's implementation to compatible renderers. Called by the Symbol.Renderer
     * (i.e., the SymbolLayer) when a Symbol is added to it.
     *
     * @param renderer A RenderableLayer.
     */
    @Override
    public void attachToRenderer(Symbol.Renderer renderer) {
        // Add the symbol implementation to compatible renderers.
        RenderableLayer layer = renderer.getLookup().lookup(RenderableLayer.class);
        if (layer != null) {
            logger.info(Bundle.info_attaching_symbol_to_renderer(this.getName()));
            layer.addRenderable(this.impl);
        }
    }

    /**
     * Removes the Symbol's implementation to the Symbol.Renderer. Called by Symbol.Renderer. Should
     * not be called directly.
     *
     * @param renderer A RenderableLayer.
     */
    @Override
    public void detachFromRenderer(Symbol.Renderer renderer) {
        RenderableLayer layer = renderer.getLookup().lookup(RenderableLayer.class);
        if (layer != null) {
            layer.removeRenderable(this.impl);
        }
    }

    /**
     * Replaces an existing tactical symbol. Called internally and by the SymbolEditor.
     *
     * @param tacticalSymbol
     */
    public void replaceTacticalSymbol(MilStd2525TacticalSymbol tacticalSymbol) {
        if (tacticalSymbol == null) {
            throw new IllegalArgumentException(Bundle.err_tac_symbol_impl_is_null());
        }
        if (this.impl == null) {
            throw new IllegalStateException(Bundle.err_tac_symbol_impl_is_null());
        }
        RenderableLayer layer = getRenderer().getLookup().lookup(RenderableLayer.class);
        if (layer != null) {
            layer.removeRenderable(this.impl);
            layer.addRenderable(tacticalSymbol);
            Globe.getInstance().refreshView();
        }
        MilStd2525TacticalSymbol oldImpl = this.impl;
        oldImpl.setDelegateOwner(null);
        this.impl = tacticalSymbol;
        this.impl.setShowFrame(oldImpl.isShowFrame());
        this.impl.setShowFill(oldImpl.isShowFill());
        this.impl.setShowIcon(oldImpl.isShowIcon());
        this.impl.setShowLocation(oldImpl.isShowLocation());
        this.impl.setShowHostileIndicator(oldImpl.isShowHostileIndicator());
        this.impl.setShowGraphicModifiers(oldImpl.isShowGraphicModifiers());
        this.impl.setShowTextModifiers(oldImpl.isShowTextModifiers());
        this.content.remove(oldImpl);
        this.content.add(this.impl);

        updateImage();
        super.pcs.firePropertyChange(PROP_SYMBOL_ATTRIBUTES, oldImpl, this.impl);
    }

    @Override
    public void setName(String name) {
        if (this.impl == null) {
            throw new IllegalStateException(Bundle.err_tac_symbol_impl_is_null());
        }
        this.impl.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, name);
        super.setName(name);
    }

    public void setNode(DataNode node) {
        this.node = node;
    }

    public String getIdentifier() {
        if (this.impl == null) {
            throw new IllegalStateException(Bundle.err_tac_symbol_impl_is_null());
        }
        return this.impl.getIdentifier();
    }

    private static String newIdentifier(String identifier, String code, int position) {
        return identifier.substring(0, position) + code + identifier.substring(position + 1);
    }

    private static String getCharAt(String identifier, int position) {
        return identifier.substring(position, position + 1);
    }

    public StandardIdentity getStandardIdentity() {
        String identity = getCharAt(this.impl.getIdentifier(), STANDARD_ID_POSITION);
        return StandardIdentity.get(identity);
    }

    public void setStandardIdentity(StandardIdentity si) {
        String identifier = newIdentifier(this.impl.getIdentifier(), si.code, STANDARD_ID_POSITION);
        MilStd2525TacticalSymbol symbol = createTacticalSymbol(identifier, this.getReferencePosition(), this);
        if (symbol != null) {
            replaceTacticalSymbol(symbol);
        }
    }

    public Status getStatus() {
        String status = getCharAt(this.impl.getIdentifier(), STATUS_INDEX);
        return Status.get(status);
    }

    public void setStatus(Status status) {
        String identifier = newIdentifier(this.impl.getIdentifier(), status.code, STATUS_INDEX);
        MilStd2525TacticalSymbol symbol = createTacticalSymbol(identifier, this.getReferencePosition(), this);
        if (symbol != null) {
            replaceTacticalSymbol(symbol);
        }
    }

    @Override
    public void setCoordinates(Coord3D coord) {
        if (getCoordinates().equals(coord)) {
            return;
        }
        this.impl.setPosition(Positions.fromCoord3D(coord));
        super.setCoordinates(coord); // fires property change
    }

    /**
     * Called by Terramenta DragController. Member of the Draggable interface.
     */
    @Override
    public boolean isDraggable() {
        return isMovable();
    }

    /**
     * Called by Terramenta DragController. Member of the Draggable interface.
     */
    @Override
    public void setDraggable(boolean draggable) {
        setMovable(draggable);
    }

    /**
     * Called by Terramenta DragController. Member of the Draggable interface
     * @param position New WorldWind position.
     */
    @Override
    public void setPosition(Position position) {
        setCoordinates(Positions.toGeoCoord3D(position));
    }

    /**
     * Called by Terramenta DragController. Member of the Draggable interface
     * @return 
     */
    @Override
    public Position getPosition() {
        return Positions.fromCoord3D(getCoordinates());
    }

    /**
     * Launches the symbol editor. Subclasses should override.
     */
    public void edit() {
        SymbolEditor editor = new SymbolEditor(this);
        editor.edit();
    }

    public boolean isMovable() {
        return this.movable;
    }

    public void setMovable(boolean movable) {
        boolean oldMovable = isMovable();
        if (movable != oldMovable) {
            this.movable = movable;
            this.pcs.firePropertyChange(PROP_SYMBOL_MOVABLE, oldMovable, movable);
        }
    }

    /**
     * Selects the Node associated with this Symbol.
     * @param selected
     */
    @Override
    public void setSelected(boolean selected) {
        boolean oldSelected = isSelected();
        if (selected != oldSelected) {
            if (this.impl == null) {
                throw new IllegalStateException(Bundle.err_tac_symbol_impl_is_null());
            }
            this.impl.setAttributes(selected ? selectedSharedAttr : normalSharedAttr);

            // Activate this node in the Viewier's explorer manager
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
            // fires property change
            super.setSelected(selected);
        }
    }

    /**
     * Gets an image suitable for an icon. If null, wait for a PROP_SYMBOL_IMAGE notification.
     *
     * @return current image; may be null.
     */
    @Override
    public Image getImage() {
        return this.image;
    }

    /**
     * Sets the image used for a Node icon. Fires a property change event.
     *
     * @param image used for icon
     */
    public void setImage(Image image) {
        Image oldImage = this.image;
        this.image = image;
        pcs.firePropertyChange(PROP_SYMBOL_IMAGE, oldImage, this.image);
    }

    protected final void updateImage() {
        SymbolIconRetriever.getDefault().postUpdateSymbolImage(this);
    }

    /**
     * A delegate.
     *
     * @return
     */
    @Override
    public Position getReferencePosition() {
        if (this.impl == null) {
            throw new IllegalStateException(Bundle.err_tac_symbol_impl_is_null());
        }
        return this.impl.getReferencePosition();
    }

    /**
     * Delegates to implementation
     *
     * @param delta
     */
    @Override
    public void move(Position delta) {
        if (isMovable()) {
            if (this.impl == null) {
                throw new IllegalStateException(Bundle.err_tac_symbol_impl_is_null());
            }
            this.impl.move(delta);
            // Fire property change
            super.setCoordinates(Positions.toGeoCoord3D(this.impl.getReferencePosition()));
        }
    }

    /**
     * Delegates to implementation
     *
     * @param position the new WorldWind Position.
     */
    @Override
    public void moveTo(Position position) {
        if (isMovable()) {
            if (this.impl == null) {
                throw new IllegalStateException(Bundle.err_tac_symbol_impl_is_null());
            }
            this.impl.moveTo(position);
            // Update owner and fire property change
            super.setCoordinates(Positions.toGeoCoord3D(this.impl.getReferencePosition()));
        }
    }

    /**
     * Supplies a class name that is stored in the XML files and used by the XML encoder.
     *
     * @return BasicSymbolBuilder.class
     */
    @Override
    public Class<? extends Symbol.Builder> getFactoryClass() {
        return BasicSymbolBuilder.class;
    }

    @Override
    public boolean owns(Object impl) {
        if (this.impl == null) {
            throw new IllegalStateException(Bundle.err_tac_symbol_impl_is_null());
        }
        return this.impl.equals(impl);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    /**
     * The SelectionController listens for selection events in the viewer and either highlights a
     * selected item or shows its context menu.
     *
     * (Based on gov.nasa.worldwind.examples.ContextMenusOnShapes)
     *
     * @author Bruce Schubert
     */
    protected static class SymbolSelectionController extends SelectionController<BasicSymbol> {

        // A singleton.
        private static SymbolSelectionController INSTANCE = new SymbolSelectionController();

        public static SymbolSelectionController getInstance() {
            return INSTANCE;
        }

        private SymbolSelectionController() {
            attachToViewer();
        }

        @Override
        protected void doOpen(SelectEvent event, BasicSymbol obj) {
            NodeOperation.getDefault().showProperties(obj.node);
        }

        @Override
        protected void doContextMenu(SelectEvent event, BasicSymbol obj) {
            if (obj.node == null) {
                throw new IllegalStateException(obj.getName() + " node has not been initialized.");
            }
            JPopupMenu contextMenu = obj.node.getContextMenu();
            if (contextMenu != null) {
                contextMenu.show((Component) event.getSource(),
                        event.getMouseEvent().getX(),
                        event.getMouseEvent().getY());
            }
        }

        @Override
        protected boolean doSetHighlight(SelectEvent event, BasicSymbol obj, boolean value) {
            if (obj.impl == null) {
                throw new IllegalStateException(obj.getName() + " implementation has not been initialized.");
            }
            obj.impl.setHighlighted(value);
            return true;
        }

        @Override
        protected boolean doSetSelected(SelectEvent event, BasicSymbol obj, boolean value) {
            obj.setSelected(value);
            return true;
        }

        @Override
        protected BasicSymbol getInstance(Object obj) {
            return (obj instanceof BasicSymbol) ? (BasicSymbol) obj : null;
        }
    }
}
