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
package com.emxsys.wmt.gis.symbology;

import com.emxsys.wmt.gis.catalog.EntityCatalog;
import com.emxsys.wmt.gis.symbology.api.Symbol;
import com.emxsys.wmt.gis.symbology.api.Symbol.Renderer;
import com.emxsys.wmt.gis.viewer.api.GisViewer;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;


@Messages(
{
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
    "info.symbol.removed=The {0} symbol was removed."
})
/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: SymbolCatalog.java 441 2012-12-12 13:11:18Z bdschubert $
 */
public class SymbolCatalog extends EntityCatalog<Symbol>
{

    private FileObject folder;
    private static final Logger logger = Logger.getLogger(SymbolCatalog.class.getName());


    /**
     * Constructs a EntityCatalog for symbols stored in a folder.
     *
     * @param folder the folder where symbols are saved.
     */
    public SymbolCatalog(FileObject folder)
    {
        this.folder = folder;
    }


    /**
     * Gets the folder containing symbols associated with this catalog.
     *
     * @return the folder who's contents this catalog represents; may be null.
     */
    public FileObject getFolder()
    {
        return folder;
    }


    public void setFolder(FileObject folder)
    {
        this.folder = folder;
    }


    @Override
    protected void doAddItem(Symbol item)
    {
        super.doAddItem(item);
        addSymbolToRenderer(item);
    }


    protected void addSymbolToRenderer(Symbol symbol)
    {
        // Get the renderer (map layer) associated with Symbols
        Symbol.Renderer renderer = getSymbolRenderer();
        if (renderer != null)
        {
            renderer.addSymbol(symbol);
        }
        else
        {
            logger.warning(Bundle.err_symbol_renderer_not_found(symbol.getName()));
        }
    }


    @Override
    protected void doRemoveItem(Symbol item)
    {
        super.doRemoveItem(item);

        // Remove from the renderer
        Symbol.Renderer renderer = getSymbolRenderer();
        if (renderer != null)
        {
            renderer.removeSymbol(item);
        }
    }


    /**
     * Dispose of this catalog. Release listeners, release renderables.
     */
    @Override
    public void dispose()
    {
        synchronized (this)
        {
            Renderer symbolRenderer = getSymbolRenderer();
            if (symbolRenderer != null)
            {
                // The renderer may force the removal of an item from the catalog, so 
                // copy the symbols to array that won't be modified by a possible call 
                // to removeItem by the Renderer.
                Symbol[] array = getItems().toArray(new Symbol[0]);
                for (Symbol symbol : array)
                {
                    symbolRenderer.removeSymbol(symbol);
                }
            }
            // Now remove the catalog items, if present.
            if (!this.getItems().isEmpty())
            {
                this.getItems().clear();
            }
        }
    }


    private Symbol.Renderer getSymbolRenderer()
    {
        GisViewer viewer = Lookup.getDefault().lookup(GisViewer.class);
        if (viewer != null)
        {
            return viewer.getLookup().lookup(Symbol.Renderer.class);
        }
        return null;
    }
}
