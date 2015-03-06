/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.gis.api.symbology;

import com.emxsys.gis.api.EntityCatalog;
import com.emxsys.gis.api.viewer.GisViewer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle.Messages;

@Messages({
    "err.symbol.null=Symbol argument cannot be null.",
    "err.symbols.null=Symbols collection cannot be null.",
    "# {0} - symbol type",
    "err.symbol.incompatible=The symbol type is incompatable [{0}]. The symbol was not added.",
    "# {0} - symbol id",
    "err.symbol.already.exists=The symbol ID ({0}) already exists.",
    "# {0} - symbol id",
    "err.symbol.renderer.not.found=A renderer for the symbol was not found. The symbol {0} may not be displayed.",
    "# {0} - symbol id",
    "info.symbol.added=The {0} symbol was added.",
    "# {0} - symbol id",
    "info.symbol.removed=The {0} symbol was removed.",
    "# {0} - symbol name",
    "info.adding.symbol.to.renderer=Adding the {0} symbol to a renderer.",
    "# {0} - symbol name",
    "info.removing.symbol.from.renderer=Removing the {0} symbol from the renderer."
})
/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SymbolManager extends EntityCatalog<Symbol> {

    private FileObject folder;
    /** The object (layer) that renders the symbols */
    private Symbol.Renderer symbolRenderer;
    /** Lookup result used to monitor the arrival of renderers (layers) */
    private Lookup.Result<Symbol.Renderer> rendererResults;
    /** Container that holds symbols awaiting a renderer */
    private final ArrayList<Symbol> pendingAdds = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(SymbolManager.class.getName());

    /**
     * Constructs a EntityCatalog for symbols stored in a folder.
     *
     * @param folder the folder where symbols are saved.
     */
    public SymbolManager(FileObject folder) {
        this.folder = folder;
    }

    /**
     * Gets the folder containing symbols associated with this catalog.
     *
     * @return the folder who's contents this catalog represents; may be null.
     */
    public FileObject getFolder() {
        return folder;
    }

    public void setFolder(FileObject folder) {
        this.folder = folder;
    }

    @Override
    protected void doAddItem(Symbol item) {
        super.doAddItem(item);
        addSymbolToRenderer(item);
    }

    protected void addSymbolToRenderer(Symbol symbol) {
        // Get the renderer (map layer) associated with Symbols
        Symbol.Renderer renderer = getSymbolRenderer();
        if (renderer != null) {
            logger.fine(Bundle.info_adding_symbol_to_renderer(symbol.getName()));
            renderer.addSymbol(symbol);
        }
        // Queue symbols while waiting for a renderer to show up.
        else {
            logger.warning(Bundle.err_symbol_renderer_not_found(symbol.getName()));
            pendingAdds.add(symbol);
        }
    }

    @Override
    protected void doRemoveItem(Symbol item) {
        super.doRemoveItem(item);

        // Remove from the renderer
        Symbol.Renderer renderer = getSymbolRenderer();
        if (renderer != null) {
            logger.fine(Bundle.info_removing_symbol_from_renderer(item.getName()));
            renderer.removeSymbol(item);
        }
        // Ensure item is not in the queue
        pendingAdds.remove(item);
    }

    /**
     * Dispose of this catalog. Release listeners, release renderables.
     */
    @Override
    public void dispose() {
        synchronized (this) {
            Symbol.Renderer renderer = getSymbolRenderer();
            if (renderer != null) {
                // The renderer may force the removal of an item from the catalog, so 
                // copy the symbols to array that won't be modified by a possible call 
                // to removeItem by the Renderer.
                Symbol[] array = getItems().toArray(new Symbol[0]);
                for (Symbol symbol : array) {
                    renderer.removeSymbol(symbol);
                }
            }
        }
        pendingAdds.clear();
        super.dispose();
    }

    private Symbol.Renderer getSymbolRenderer() {
        if (symbolRenderer == null) {
            GisViewer viewer = Lookup.getDefault().lookup(GisViewer.class);
            if (viewer != null && rendererResults == null) {
                
                // Create lookup listener to watch for the arrival or disposal of a Marker.Renderer
                this.rendererResults = viewer.getLookup().lookupResult(Symbol.Renderer.class);
                this.rendererResults.addLookupListener((LookupEvent ev) -> {
                    // Add any pending symbols to the renderer when it appears in the Lookup.Result
                    Collection<? extends Symbol.Renderer> allInstances = rendererResults.allInstances();
                    if (allInstances.isEmpty()) {
                        symbolRenderer = null;
                    }
                    else {
                        // Update the renderer and process any pending Symbols
                        symbolRenderer = allInstances.iterator().next();
                        pendingAdds.stream().forEach((symbol) -> {
                            addSymbolToRenderer(symbol);
                        });
                        pendingAdds.clear();
                    }
                });
                if (!rendererResults.allInstances().isEmpty()) {
                    symbolRenderer = rendererResults.allInstances().iterator().next();
                }
            }
        }
        return symbolRenderer;
    }
}
