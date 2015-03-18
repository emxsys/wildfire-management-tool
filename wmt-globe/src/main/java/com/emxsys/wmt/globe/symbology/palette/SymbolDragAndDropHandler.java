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
package com.emxsys.wmt.globe.symbology.palette;

import com.emxsys.gis.api.GeoCoord3D;
import com.emxsys.gis.api.symbology.SymbolManager;
import com.emxsys.util.ProjectUtil;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.dnd.AbstractDragAndDropHandler;
import com.emxsys.wmt.globe.symbology.BasicSymbol;
import com.emxsys.wmt.globe.symbology.BasicSymbolWriter;
import com.emxsys.wmt.globe.util.Positions;
import gov.nasa.worldwind.geom.Position;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.w3c.dom.Document;

/**
 * This class creates BasicSymbol objects when they are dropped from the Palette.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class SymbolDragAndDropHandler extends AbstractDragAndDropHandler {

    private static final DataFlavor dataFlavor = SymbolPalette.SYMBOL_DATA_FLAVOR;
    private static final Logger logger = Logger.getLogger(SymbolDragAndDropHandler.class.getName());

    public SymbolDragAndDropHandler(Globe viewer) {
        super(viewer);

        // Specify the offset used to translate the symbol image from the center to the base.
        setImageOffset(new Point(0, -16));
    }

    /**
     * Get the type of data this handler supports.
     *
     * @return PaletteSupport.SYMBOL_DATA_FLAVOR
     */
    @Override
    public DataFlavor getDataFlavor() {
        return dataFlavor;
    }

    /**
     * Handle the start the drag and drop by establishing a drag-able image.
     *
     * @param dtde drag event
     */
    @Override
    public void doDragEnter(DropTargetDragEvent dtde) {
        SymbolPalette.ItemData data = extractPaletteData(dtde.getTransferable());
        if (data == null || !isDragSupported(dtde)) {
            dtde.rejectDrag();
            return;
        }

        setDragImage(new SymbolPalette.ItemDisplayer(data));
        getRenderableLayer().addRenderable(getDragImage());
        dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }

    /**
     * Reject drags that are not supported.
     *
     * @param dtde drag event
     */
    @Override
    public void doDragOver(DropTargetDragEvent dtde) {
        if (!isDragSupported(dtde)) {
            dtde.rejectDrag();
            return;
        }
        super.doDragOver(dtde);
    }

    /**
     * Handles the drop by creating a BasicSymbol at the screen location and then creating the
     * DataObject that will represent the symbol on the disk.
     *
     * @param dtde drop event
     */
    @Override
    public void doDrop(DropTargetDropEvent dtde) {
        SymbolPalette.ItemData data = extractPaletteData(dtde.getTransferable());
        if (data == null) {
            dtde.rejectDrop();
            return;
        }
        clearDragImage();

        // NOTE: For reference, the following method doesn't compute the correct latitude when the 
        // view is tilted and zoomed in.
        //  getViewer().getWwd().getView().computePositionFromScreenPoint(dtde.getLocation().x, dtde.getLocation().y);
        // Get the lat/lon of the drop point to use for the symbol
        Position position = Positions.fromScreenPoint(dtde.getLocation().x, dtde.getLocation().y);
        if (position == null) {
            // This can occur when the point is above the horizon. Just log the error and fail softly.
            logger.severe("The screen point could not be converted to a lat/lon. The symbol was not created.");
            return;
        }
        // Create a model Symbol object from the tactical symbol identifier stored in the 
        // TransferData and place it at the drop point. 
        // HACK: The altitude must be zero else WW dragger for moving the object will mess up.
        BasicSymbol model = new BasicSymbol(data.getIdentifier(),
                GeoCoord3D.fromDegrees(position.latitude.degrees, position.longitude.degrees)); // no altitude!
        model.setName(data.getDisplayName());
        model.setType(data.getType());
        model.setQuantity(data.getQuantity());
        

        // Create the Symbol file using the model Symbol object; create it in the current project.
        FileObject folder = null;
        Project currentProject = ProjectUtil.getCurrentProject();
        if (currentProject != null) {
            SymbolManager manager = currentProject.getLookup().lookup(SymbolManager.class);
            if (manager != null) {
                folder = manager.getFolder();
            }
        }        
        // After writing, the new symbol be read from the disk and displayed on the globe
        BasicSymbolWriter writer = new BasicSymbolWriter(folder, model);
        Document doc = writer.write();
        if (doc == null) {
            logger.severe("A BasicSymbol object was not found in the DataObject. The symbol was not created.");
            dtde.rejectDrop();
            return;
        }
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
    }

    /**
     *
     * @param dtde drag event
     * @return true if the current project accepts Symbols
     */
    private boolean isDragSupported(DropTargetDragEvent dtde) {
        SymbolPalette.ItemData data = extractPaletteData(dtde.getTransferable());
        Project project = Utilities.actionsGlobalContext().lookup(Project.class);
        if (data != null && project != null) {
            SymbolManager manager = project.getLookup().lookup(SymbolManager.class);
            if (manager != null) {
                // We can accept symbols!
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param transferable from the DropTargetDragEvent and DropTargetDropEvent events.
     * @return the SymbolPaletteData embedded in the Transferable
     */
    private SymbolPalette.ItemData extractPaletteData(Transferable transferable) {
        SymbolPalette.ItemData data = null;
        try {
            if (transferable.isDataFlavorSupported(getDataFlavor())) {
                data = (SymbolPalette.ItemData) transferable.getTransferData(getDataFlavor());
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return data;
    }
}
