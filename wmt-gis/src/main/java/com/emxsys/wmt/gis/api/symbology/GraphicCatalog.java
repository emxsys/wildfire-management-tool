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
package com.emxsys.wmt.gis.api.symbology;

import com.emxsys.wmt.gis.api.EntityCatalog;
import com.emxsys.wmt.gis.api.symbology.Graphic.Renderer;
import com.emxsys.wmt.gis.api.viewer.GisViewer;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;


@Messages(
{
    "err.graphic.null=Graphic argument cannot be null.",
    "err.graphics.null=Graphics collection cannot be null.",
    "# {0} - graphic type",
    "err.graphic.incompatible=The graphic type is incompatable [{0}]. The graphic was not added.",
    "# {0} - graphic id",
    "err.graphic.already.exists=The graphic ID ({0}) already exists.",
    "# {0} - graphic id",
    "err.graphic.renderer.not.found=A renderer for the graphic was not found. The graphic {0} will not be displayed.",
    "# {0} - graphic id",
    "info.adding.graphic.to.renderer=Adding the {0} graphic to a renderer.",
    "# {0} - graphic id",
    "info.removing.graphic.from.renderer=Removing the {0} graphic from the renderer."
})
/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: GraphicCatalog.java 441 2012-12-12 13:11:18Z bdschubert $
 */
public class GraphicCatalog extends EntityCatalog<Graphic>
{

    private static final Logger logger = Logger.getLogger(GraphicCatalog.class.getName());
    private FileObject folder;


    /**
     * Constructs a EntityCatalog who's contents are backed by a folder.
     *
     * @param folder the folder where Graphics are saved
     */
    public GraphicCatalog(FileObject folder)
    {
        setFolder(folder);
    }


    /**
     * @return the folder who's contents this catalog represents.
     */
    public FileObject getFolder()
    {
        return folder;
    }


    /**
     * Sets the folder that this catalog represents.
     *
     * @param folder the folder where Graphics are saved.
     */
    private void setFolder(FileObject folder)
    {
        if (folder != null && !folder.isFolder())
        {
            throw new IllegalArgumentException("setFolder: " + folder.getName() + " is not a folder.");
        }
        this.folder = folder;
    }


    @Override
    protected void doAddItem(Graphic item)
    {
        super.doAddItem(item);
        addGraphicToRenderer(item);
    }


    protected void addGraphicToRenderer(Graphic graphic)
    {
        // Get the renderer (map layer) associated with Graphics
        Graphic.Renderer renderer = getGraphicRenderer();
        if (renderer != null)
        {
            logger.fine(Bundle.info_adding_graphic_to_renderer(graphic.getName()));
            renderer.addGraphic(graphic);
        }
        else
        {
            logger.warning(Bundle.err_graphic_renderer_not_found(graphic.getName()));
        }
    }


    @Override
    protected void doRemoveItem(Graphic item)
    {
        super.doRemoveItem(item);

        // Remove from the renderer
        Graphic.Renderer renderer = getGraphicRenderer();
        if (renderer != null)
        {
            logger.fine(Bundle.info_removing_graphic_from_renderer(item.getName()));
            renderer.removeGraphic(item);
        }
    }


    /**
     * Dispose of this catalog. Release listeners, release renderables.
     */
    @Override
    public void dispose()
    {
        Renderer graphicRenderer = getGraphicRenderer();
        if (graphicRenderer != null)
        {
            // The renderer may force the removal of the item from the catalog, so copy
            // the graphics to array that won't be modified by a subsequent nested call 
            // to removeItem invoked the the Renderer
            Graphic[] array = this.getItems().toArray(new Graphic[0]);
            for (Graphic graphic : array)
            {
                graphicRenderer.removeGraphic(graphic);
            }
        }
        super.dispose();
    }


    private Graphic.Renderer getGraphicRenderer()
    {
        GisViewer viewer = Lookup.getDefault().lookup(GisViewer.class);
        if (viewer != null)
        {
            return viewer.getLookup().lookup(Graphic.Renderer.class);
        }
        return null;
    }
}
