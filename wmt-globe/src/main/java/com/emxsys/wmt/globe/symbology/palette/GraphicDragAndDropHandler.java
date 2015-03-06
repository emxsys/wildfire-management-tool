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
package com.emxsys.wmt.globe.symbology.palette;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.symbology.GraphicManager;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.dnd.AbstractDragAndDropHandler;
import com.emxsys.wmt.globe.symbology.BasicGraphic;
import com.emxsys.wmt.globe.util.Positions;
import gov.nasa.worldwind.geom.Position;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * This class creates Graphic objects when they are dropped from the Palette.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: GraphicDragAndDropHandler.java 540 2013-04-18 15:48:26Z bdschubert $
 */
public class GraphicDragAndDropHandler extends AbstractDragAndDropHandler {

    private final static DataFlavor dataFlavor = PaletteSupport.GRAPHIC_DATA_FLAVOR;
    private static final Logger logger = Logger.getLogger(GraphicDragAndDropHandler.class.getName());

    public GraphicDragAndDropHandler(Globe viewer) {
        super(viewer);
    }

    /**
     * Gets the type of data this handler supports.
     *
     * @return PaletteSupport.GRAPHIC_DATA_FLAVOR
     * @see PaletteSupport
     */
    @Override
    public DataFlavor getDataFlavor() {
        return GraphicDragAndDropHandler.dataFlavor;
    }

    /**
     * Handle the start the drag and drop by establishing a drag-able image.
     *
     * @param dtde drag event
     */
    @Override
    public void doDragEnter(DropTargetDragEvent dtde) {
        if (!isDragSupported(dtde)) {
            dtde.rejectDrag();
            return;
        }
        GraphicPaletteData data = getPaletteDataFromTransferable(dtde.getTransferable());
        setDragImage(new PaletteItemDisplayer(data));
        getRenderableLayer().addRenderable(getDragImage());
        dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }

    @Override
    public void doDragOver(DropTargetDragEvent dtde) {
        if (!isDragSupported(dtde)) {
            dtde.rejectDrag();
            return;
        }
        super.doDragOver(dtde);
    }

    /**
     * Handles the drop by creating a Graphic at the screen location.
     *
     * @param dtde drop event
     */
    @Override
    public void doDrop(DropTargetDropEvent dtde) {
        // First, get our palette item payload from the dtde
        GraphicPaletteData data = getPaletteDataFromTransferable(dtde.getTransferable());
        if (data == null) {
            dtde.rejectDrop();
            return;
        }
        // Remove the drag image from the screen
        clearDragImage();

        // Now create a model TacticalGraphic from the symbol ID stored in the TransferData.
        ArrayList<Coord3D> positions = convertPointsToPositions(dtde.getLocation(), data.getPoints());
        BasicGraphic model = new BasicGraphic(data.getIdentifier(), positions);
        model.setName(data.getDisplayName());
        model.setType(data.getType());

dtde.rejectDrop();
//        Create the 'real' file-based tactical graphic in the current project (null folder)
//        DataObject dataObject = BasicGraphic.getFactory().createDataObject(model, null);
//        if (dataObject.getLookup().lookup(BasicGraphic.class) == null) {
//            logger.severe("A BasicGraphic was not found in the DataObject. The graphic was not created.");
//            try {
//                dataObject.delete();
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//            dtde.rejectDrop();
//            return;
//        }
//        dtde.acceptDrop(DnDConstants.ACTION_COPY);
    }

    private GraphicPaletteData getPaletteDataFromTransferable(Transferable transferable) {
        GraphicPaletteData data = null;
        try {
            if (transferable.isDataFlavorSupported(getDataFlavor())) {
                data = (GraphicPaletteData) transferable.getTransferData(getDataFlavor());
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return data;
    }

    private ArrayList<Coord3D> convertPointsToPositions(Point dropPoint, List<Point> points) {
        // Build the coordinates that will make up the model graphic: convert screen points to lat/lons
        ArrayList<Coord3D> positions = new ArrayList<Coord3D>();
        for (Point point : points) {
            point.translate(dropPoint.x, dropPoint.y);
            Position pos = Positions.fromScreenPoint(point.x, point.y);
            if (pos == null) {
                continue;
            }
            positions.add(Positions.toGeoCoord3D(pos));
        }
        return positions;
    }

    private boolean isDragSupported(DropTargetDragEvent dtde) {
        GraphicPaletteData data = getPaletteDataFromTransferable(dtde.getTransferable());
        Project project = Utilities.actionsGlobalContext().lookup(Project.class);
        if (data != null && project != null) {
            GraphicManager catalog = project.getLookup().lookup(GraphicManager.class);
            if (catalog != null) {
                // We can accept graphics!
                return true;
            }
        }
        return false;
    }
}
