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
package com.emxsys.wmt.globe.dnd;

import com.emxsys.wmt.globe.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.ScreenImage;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.logging.Logger;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class AbstractDragAndDropHandler implements DragAndDropHandler {

    protected Globe viewer;
    protected RenderableLayer layer;
    protected ScreenImage dragImage;
    protected Point imageOffset;
    private static final Logger logger = Logger.getLogger(AbstractDragAndDropHandler.class.getName());

    public AbstractDragAndDropHandler(Globe viewer) {
        this.viewer = viewer;
    }

    public Globe getViewer() {
        return viewer;
    }

    /**
     * Establishes the layer used to render drag-and-drop screen images.
     *
     * @param layer that renders drag-and-drop images
     */
    @Override
    public void setRenderableLayer(RenderableLayer layer) {
        this.layer = layer;
    }

    /**
     *
     * @return the layer that renders drag-and-drop screen images
     */
    @Override
    public RenderableLayer getRenderableLayer() {
        return layer;
    }

    public void setImageOffset(Point imageOffset) {
        this.imageOffset = imageOffset;
    }

    public Point getImageOffset() {
        return imageOffset;
    }

    public void setDragImage(ScreenImage dragImage) {
        clearDragImage();
        this.dragImage = dragImage;
    }

    public ScreenImage getDragImage() {
        return dragImage;
    }

    public void clearDragImage() {
        if (getDragImage() != null) {
            getRenderableLayer().removeRenderable(getDragImage());
        }
    }

    /**
     *
     * @param dtde drage event
     */
    @Override
    public void doDragOver(DropTargetDragEvent dtde) {
        ScreenImage image = getDragImage();
        if (image == null) {
            logger.warning("ScreenImage should not be null.");
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            return;
        }

        // Get the location to position the image, and optionally move the anchor point
        // from the center of the drag image (e.g., to the base of the image).
        Point location = dtde.getLocation();
        Point offset = getImageOffset();
        if (offset != null) {
            location.translate(offset.x, offset.y);
        }

        image.setScreenLocation(location);
        this.viewer.refreshView();
        dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }

    @Override
    public void doDragExit(DropTargetEvent dte) {
        ScreenImage image = getDragImage();
        if (image == null) {
            throw new IllegalStateException("ScreenImage should not be null.");
        }
        RenderableLayer renderableLayer = getRenderableLayer();
        if (renderableLayer == null) {
            throw new IllegalStateException("RenderableLayer should not be null.");
        }
        renderableLayer.removeRenderable(image);
    }

}
