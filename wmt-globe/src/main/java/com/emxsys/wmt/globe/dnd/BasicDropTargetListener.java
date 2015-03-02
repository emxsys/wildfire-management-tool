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
package com.emxsys.wmt.globe.dnd;

import com.emxsys.wmt.globe.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.ScreenImage;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Dispatches Drag-N-Drop events to the registered handlers.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class BasicDropTargetListener implements DropTargetListener {

    Globe viewer;
    RenderableLayer dragImageLayer;
    ScreenImage dragImage;
    Map<DataFlavor, DragAndDropHandler> dndHandlers = new HashMap<>();

    public BasicDropTargetListener(Globe viewer) {
        this.viewer = viewer;
        this.dragImageLayer = new RenderableLayer();
        this.dragImageLayer.setName("Screen Image");
        this.viewer.getWorldWindManager().getWorldWindow().getModel().getLayers().add(dragImageLayer);
    }

    public void addDndHandler(DragAndDropHandler handler) {
        this.dndHandlers.put(handler.getDataFlavor(), handler);
        handler.setRenderableLayer(dragImageLayer);
    }

    private DragAndDropHandler findHandler(DropTargetDragEvent dtde) {
        return findHandlerForFlavors(dtde.getCurrentDataFlavors());
    }

    private DragAndDropHandler findHandler(DropTargetDropEvent dtde) {
        return findHandlerForFlavors(dtde.getCurrentDataFlavors());
    }

    private DragAndDropHandler findHandlerForFlavors(DataFlavor[] flavors) {
        DragAndDropHandler handler = null;
        // Find the first handler for a DataFlavor
        for (DataFlavor dataFlavor : flavors) {
            handler = this.dndHandlers.get(dataFlavor);
            if (handler != null) {
                break;
            }
        }
        return handler;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        DragAndDropHandler handler = findHandler(dtde);
        if (handler != null) {
            handler.doDragEnter(dtde);
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        DragAndDropHandler handler = findHandler(dtde);
        if (handler != null) {
            handler.doDragOver(dtde);
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        DragAndDropHandler handler = findHandler(dtde);
        if (handler != null) {
            handler.doDrop(dtde);
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        this.dragImageLayer.removeAllRenderables();
    }

}
