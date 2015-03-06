/*
 * Copyright (c) 2009-2015, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.layers;

import com.emxsys.gis.api.layer.BasicLayerCategory;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.BasicLayerType;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.layer.LayerCategory;
import com.emxsys.gis.api.layer.LayerGroup;
import com.emxsys.gis.api.layer.LayerType;
import com.emxsys.gis.api.symbology.Symbol;
import com.emxsys.wmt.globe.Globe;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * A general purpose RenderableLayer for displaying PointPlacemarks symbols. The existence of this
 * layer in the GisViewer's lookup will add the capability to add Symbols vis a vis the
 * Symbol.Renderer interface.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages({
    "# {0} - symbol name",
    "err.add.symbol.failed=Cannot add symbol. {0}",
    "# {0} - symbol name",
    "err.remove.symbol.failed=Cannot remove symbol. {0}",
    "err.symbol.catalog.not.found=A symbol catalog was not found. The symbol was not added to a catalog.",
    "err.symbol.not.compatible=The symbol does not contain a Renderable object."
})
public class SymbolLayer extends RenderableLayer implements GisLayer, Symbol.Renderer {

    private final Lookup lookup;
    private final InstanceContent content = new InstanceContent();
    private static final Logger logger = Logger.getLogger(SymbolLayer.class.getName());


    /**
     * Factory method used to create a SymbolLayer from an XML .instance file.
     *
     * @param instanceFile the .instance file object specified in the XML layer (layer.xml)
     * @return a SymbolLayer instance
     */
    public static SymbolLayer newInstance(FileObject instanceFile) {
//        // Psuedo singleton: look for an existing instance and if found, initialize it with the FileObject.
//        Symbol.Renderer symbolRenderer = Lookup.getDefault().lookup(Symbol.Renderer.class);
//        SymbolLayer renderableLayer = symbolRenderer instanceof SymbolLayer
//                ? (SymbolLayer) symbolRenderer : new SymbolLayer();
        SymbolLayer layer = new SymbolLayer();

        // Update GisLayer attributes from the XML config
        BasicLayerType type = BasicLayerType.fromString((String) instanceFile.getAttribute("type"));
        BasicLayerGroup role = BasicLayerGroup.fromString((String) instanceFile.getAttribute("role"));
        BasicLayerCategory category = BasicLayerCategory.fromString((String) instanceFile.getAttribute("category"));
        layer.updateLayerAttributes(type, role, category);

        // Update WW Layer implemenation from the XML config
        LayerFactory.updateLayerFromFileAttributes(layer, instanceFile);

        logger.log(Level.CONFIG, "Created new instance of {0}", layer.toString());
        return layer;
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public SymbolLayer() {
        this.lookup = new AbstractLookup(content);
        this.content.add(this);
        // Add support for active altitudes
        this.content.add(new BasicLayerActiveAltitude(this));

        // Create a Node to represent this object in the WorldWind Viewer's ExplorerManager
        //this.content.add(new PointSymbolLayerNode(this));
    }

    private void updateLayerAttributes(LayerType type, LayerGroup role, LayerCategory category) {
        this.content.add(type == null ? BasicLayerType.Other : type);
        this.content.add(role == null ? BasicLayerGroup.Symbology : role);
        this.content.add(category == null ? BasicLayerCategory.Unknown : category);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        super.firePropertyChange(AVKey.LAYER, null, this);
    }

    @Override
    public Lookup getLookup() {
        return this.lookup;
    }

    /**
     * Returns true if this layer contains the Symbol's Renderable object.
     *
     * @param symbol with a Renderable in its lookup
     * @return true if this layer contains the Symbol
     */
    @Override
    public boolean contains(Symbol symbol) {
        Renderable renderable = symbol.getLookup().lookup(Renderable.class);
        if (renderable != null) {
            Iterator<Renderable> iterator = super.getRenderables().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equals(renderable)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds all the symbols to the layer and the active project's catalog. Ensures all the new
     * symbols are associated with the project that was active when the method was invoked.
     *
     * @param symbols A collection of Symbols.
     */
    @Override
    public void addSymbols(Collection<? extends Symbol> symbols) {
        synchronized (this) {
            symbols.stream().forEach((symbol) -> {
                Collection<? extends Renderable> allRenderables = symbol.getLookup().lookupAll(Renderable.class);
                if (allRenderables.isEmpty()) {
                    logger.warning(Bundle.err_add_symbol_failed(Bundle.err_symbol_not_compatible()));
                } else {
                    allRenderables.stream().forEach((renderable) -> {
                        super.addRenderable(renderable);
                    });
                    super.firePropertyChange(Symbol.Renderer.PROP_SYMBOL_ADDED, null, symbol);
                }
            });
        }
        refreshLayer();
    }

    /**
     * Adds the symbol to the layer, and to the active project's symbol catalog.
     *
     * @param symbol A BasicSymbol instance.
     */
    @Override
    public void addSymbol(Symbol symbol) {
        Collection<? extends Renderable> allRenderables = symbol.getLookup().lookupAll(Renderable.class);
        if (allRenderables.isEmpty()) {
            logger.warning(Bundle.err_add_symbol_failed(Bundle.err_symbol_not_compatible()));
            return;
        }
        allRenderables.stream().forEach((renderable) -> {
            super.addRenderable(renderable);
        });
        super.firePropertyChange(Symbol.Renderer.PROP_SYMBOL_ADDED, null, symbol);
        refreshLayer();
    }

    @Override
    public void removeSymbol(Symbol symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol argument is null.");
        }
        Collection<? extends Renderable> allRenderables = symbol.getLookup().lookupAll(Renderable.class);
        if (allRenderables.isEmpty()) {
            logger.warning(Bundle.err_add_symbol_failed(Bundle.err_symbol_not_compatible()));
            return;
        }
        allRenderables.stream().forEach((renderable) -> {
            super.removeRenderable(renderable);
        });
        super.firePropertyChange(Symbol.Renderer.PROP_SYMBOL_REMOVED, symbol, null);
        refreshLayer();
    }

    private void refreshLayer() {
        Globe.getInstance().refreshView();
    }
}
