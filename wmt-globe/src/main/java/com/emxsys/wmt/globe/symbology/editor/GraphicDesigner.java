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
package com.emxsys.wmt.globe.symbology.editor;

import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.symbology.BasicGraphic;
import com.emxsys.wmt.globe.util.Positions;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.editor.AirspaceEditorUtil;
import gov.nasa.worldwind.symbology.TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.areas.BasicArea;
import gov.nasa.worldwind.symbology.milstd2525.graphics.lines.AbstractAxisArrow;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPopupMenu;
import org.openide.nodes.Node;

/**
 * An editor for Tactical Graphics.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: GraphicDesigner.java 392 2012-12-08 20:53:05Z bdschubert $
 */
public class GraphicDesigner {

    private final BasicGraphic graphic;
    private TacticalGraphic impl;
    private final RenderableLayer layer;
    private final WorldWindow wwd;
    private boolean armed = false;
    private List<ControlPoint> points = new ArrayList<ControlPoint>();
    private ControlPoint anchorPoint;
    private ControlPoint oldAnchorPoint;
    private ControlPoint rotationHandle;
    private DragLine dragline;
    private VertexController controller;
    private static final Logger logger = Logger.getLogger(GraphicDesigner.class.getName());

    public GraphicDesigner(BasicGraphic graphic) {
        this.graphic = graphic;
        this.impl = graphic.getLookup().lookup(TacticalGraphic.class);
        this.layer = new RenderableLayer();
        this.wwd = (WorldWindow) Globe.getInstance().getRendererComponent();

    }

    public boolean isArmed() {
        return armed;
    }

    public void startEditing() {
        // Get the current implementation
        this.impl = graphic.getLookup().lookup(TacticalGraphic.class);

        // Create the control points / lines
        updateControlPoints();

        // Activate a selection controller for the control points/lines
        registerController();

        this.wwd.getModel().getLayers().add(this.layer);
        this.armed = true;
    }

    public void stopEditing() {
        this.armed = false;

        // Release the controller to prevent unnecessary event handling
        unregisterController();

        // Release the resources 
        destroyControls();
        this.wwd.getModel().getLayers().remove(this.layer);
    }

    private void registerController() {
        this.controller = new VertexController();
        this.wwd.addSelectListener(this.controller);
    }

    private void unregisterController() {
        this.wwd.removeSelectListener(controller);
        this.controller = null;
    }

    private void destroyControls() {
        this.layer.removeAllRenderables();
        this.points.clear();
        this.dragline = null;
        this.anchorPoint = null;
        this.rotationHandle = null;
    }

    private List<Position> getPositionsWithZeroElevation() {
        ArrayList<Position> arrayList = new ArrayList<Position>();
        Iterator<? extends Position> iterator = this.impl.getPositions().iterator();
        while (iterator.hasNext()) {
            Position p = iterator.next();
            arrayList.add(Position.fromDegrees(p.latitude.degrees, p.longitude.degrees));
        }
        return arrayList;
    }

    /**
     * Update the control points whenever the implementation changes
     */
    private void updateControlPoints() {
        // Update the lines between control points
        updateDragLine();

        // Dispose of the old points
        for (ControlPoint cp : points) {
            layer.removeRenderable(cp);
        }
        points.clear();
        // Create a new set of points
        List<Position> positions = getPositionsWithZeroElevation();
        for (Position p : positions) {
            this.points.add(new ControlPoint(this, p));
        }
        this.layer.addRenderables(this.points);
        updateAnchorPoint();
        updateRotationHandle();
    }

    private void updateDragLine() {
        if (this.dragline == null) {
            this.dragline = new DragLine();
            this.layer.addRenderables(this.dragline.getRenderables());
        }
        List<Position> positions = getPositionsWithZeroElevation();
        if (this.impl instanceof AbstractAxisArrow) {
            this.dragline.setPositions(positions.subList(0, positions.size() - 1));
            this.dragline.setClosed(false);
        } else if (this.impl instanceof BasicArea) {
            this.dragline.setPositions(positions);
            this.dragline.setClosed(true);
        }
    }

    private void updateAnchorPoint() {
//        Position refPos = this.impl.getReferencePosition();
//        if (refPos == null)
//        {
//            return;
//        }
//        Position p = Position.fromDegrees(refPos.latitude.degrees, refPos.longitude.degrees);
//        this.anchorPoint = new ControlPoint(p, ControlPoint.ANCHOR_ATTRS, ControlPoint.ANCHOR_HILITE_ATTRS);
//        this.layer.addRenderable(this.anchorPoint);
        if (points.isEmpty()) {
            this.anchorPoint = null;
        } else {
            ControlPoint cp = points.get(0);
            cp.setAttributes(ControlPoint.ANCHOR_ATTRS);
            cp.setHighlightAttributes(ControlPoint.ANCHOR_HILITE_ATTRS);
            if (this.oldAnchorPoint != null) {
                cp.setHighlightAttributes(ControlPoint.VERTEX_HILITE_ATTRS);
                this.oldAnchorPoint = cp;
            } else {
                cp.setHighlightAttributes(ControlPoint.ANCHOR_HILITE_ATTRS);
                this.anchorPoint = cp;
            }
        }
    }

    /**
     * Toggles the existence of a movable anchor point.
     */
    public void toggleAnchorPoint() {
        // Toggle the existance of the anchor point
        if (this.anchorPoint != null) {
            // Disable of the anchor point
            this.anchorPoint.setHighlightAttributes(ControlPoint.VERTEX_HILITE_ATTRS);
            this.oldAnchorPoint = this.anchorPoint;
            this.anchorPoint = null;
        } else if (oldAnchorPoint != null) {
            // Enable the anchor point
            this.anchorPoint = this.oldAnchorPoint;
            this.anchorPoint.setHighlightAttributes(ControlPoint.ANCHOR_HILITE_ATTRS);
            this.oldAnchorPoint = null;
        }
    }

    public boolean isAnchorPoint(ControlPoint cp) {
        return (cp == this.anchorPoint) || (cp == this.oldAnchorPoint);
    }

    private void updateRotationHandle() {
        this.rotationHandle = null;
    }

    /**
     * Tests if the object is an editor component.
     *
     * @param object Object to test.
     * @return true if the object
     */
    public boolean owns(Object object) {
        if (object instanceof Renderable) {
            Iterator<Renderable> iterator = this.layer.getRenderables().iterator();
            while (iterator.hasNext()) {
                if (iterator.next() == object) {
                    return true;
                }
            }
            return false;
        } else if (object instanceof DragLine) {
            return object == this.dragline;
        } else {
            return object == this;
        }
    }

    boolean canDeleteControlPoint(ControlPoint point) {
        int first = Integer.MAX_VALUE;
        int last = -1;
        int min = 0;
        int indexOf = points.indexOf(point);
        if (impl instanceof AbstractAxisArrow) {
            // Cannot delete first point or last two points of an arrow.
            first = 1;
            last = points.size() - 2;
            min = 3;
        } else if (impl instanceof BasicArea) {
            first = 0;
            last = points.size() - 1;
            min = 3;
        }
        return indexOf >= first
                && indexOf <= last
                && points.size() > min;
    }

    /**
     * Move the shape when dragging.
     *
     * @param position new reference position.
     */
    private void moveShape(Position position) {
        this.impl.moveTo(position); // doesn't fire property change; can move if "locked"
        //this.graphic.moveTo(position);  // fires property change
    }

    /**
     * Update the shape geometry when a control point is moved.
     */
    private void updateShape() {

        // Update the graphic implementation 
        List<Position> positions = new ArrayList<Position>(points.size());
        for (ControlPoint p : points) {
            positions.add(p.getPosition());
        }
        this.graphic.setPositionsImpl(positions);   // Fires property change

        // Update the centerline to refect the current shape geometry
        updateDragLine();

        updateAnchorPoint();

        // Force a refresh
        Globe.getInstance().refreshView();
    }

    /**
     * Hide control points when dragging the shape,
     */
    private void hideControlPoints() {
        for (ControlPoint cp : points) {
            cp.setVisible(false);
        }
        dragline.setVisible(false);
    }

    private void showControlPoints() {
        for (ControlPoint cp : points) {
            cp.setVisible(true);
        }
        dragline.setVisible(true);
    }

    /**
     * Used to add a vertex along the centerline. Based largely on
     * gov.nasa.worldwindx.examples.shapebuilder.ExtrudedPolygonBuilder.addVertex().
     *
     * @param mousePoint
     * @return the index of the new position, or -1 if not added.
     */
    public ControlPoint addControlPoint(Point mousePoint) {
        // Try to find the edge that is closest to a ray passing through the screen point. 
        // We're trying to determine the user's intent as to which segment a new control point 
        // should be added to.
        Position pickPosition = Positions.fromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Vec4 pickPoint = wwd.getModel().getGlobe().computePointFromPosition(pickPosition);
        List<Position> positions = dragline.getPositions();
        // Loop through the control points and determine which segment is closest to the pick point
        double nearestDistance = Double.MAX_VALUE;
        int newVertexIndex = 0;
        int numSegments = positions.size() - (dragline.isClosed() ? 0 : 1);
        for (int i = 0; i < numSegments; i++) {
            Vec4 thisPt = wwd.getModel().getGlobe().computePointFromPosition(positions.get(i));
            Vec4 nextPt = wwd.getModel().getGlobe().computePointFromPosition(positions.get((i + 1) % positions.size()));
            Vec4 pointOnEdge = AirspaceEditorUtil.nearestPointOnSegment(thisPt, nextPt, pickPoint);
            {
                double d = pointOnEdge.distanceTo3(pickPoint);
                if (d < nearestDistance) {
                    newVertexIndex = i + 1;
                    nearestDistance = d;
                }
            }
        }
        // Create a ControlPoint at the new vertex
        Position newPosition = wwd.getModel().getGlobe().computePositionFromPoint(pickPoint);
        return addControlPointAt(newPosition, newVertexIndex);

    }

    private ControlPoint addControlPointAt(Position newPosition, int newVertexIndex) {
        ControlPoint cp = new ControlPoint(this, Position.fromDegrees(
                newPosition.latitude.degrees, newPosition.longitude.degrees));
        points.add(newVertexIndex, cp);
        layer.addRenderable(cp);
        return cp;
    }

    /**
     * Called by ControlPointNode's DeleteAction
     *
     * @param cp
     */
    void deleteControlPoint(ControlPoint cp) {
        layer.removeRenderable(cp);
        points.remove(cp);
        updateShape();
    }

    /**
     * Inner class for handling control point and center line selections.
     *
     * @author Bruce
     */
    protected class VertexController implements SelectListener {

        protected Object lastTopObject = null;
        protected Highlightable lastHighlight = null;
        protected ControlPoint lastSelected = null;
        // HACK! Remember this point so that we can update the ControlPoint during a CenterLine drag.
        protected ControlPoint dragProxyPoint = null;

        @Override
        public void selected(SelectEvent event) {
            if (!isArmed()) {
                return;
            }
            Object topObject = event.getTopObject();
            try {
                if (event.isRollover()) {
                    if (lastHighlight == topObject) {
                        return;
                    }
                    // Turn off the last highlighted point whenever something else is rolled over
                    if (lastHighlight != null) {
                        lastHighlight.setHighlighted(false);
                        lastHighlight = null;
                    }
                    // Turn on the highlight for a control point
                    if (topObject instanceof Highlightable) {
                        lastHighlight = (Highlightable) topObject;
                        lastHighlight.setHighlighted(true);
                    }
                } else if (event.isRightClick()) {
                    if (topObject instanceof ControlPoint) {
                        Node node = ((ControlPoint) topObject).getNode();
                        if (node != null) {
                            JPopupMenu contextMenu = node.getContextMenu();
                            if (contextMenu != null) {
                                contextMenu.show((Component) event.getSource(),
                                        event.getMouseEvent().getX(),
                                        event.getMouseEvent().getY());
                            }
                        }
                    }

                } else if (event.isLeftPress()) {
                    logger.log(Level.FINE, "L-Press on {0}", topObject.getClass().getSimpleName());
                    if (topObject instanceof DragLine) {
                        // Subsequent drag should be on new ControlPoint (shouldn't it?)
                        logger.fine("*** Creating New ControlPoint and Proxy");
                        dragProxyPoint = addControlPoint(event.getPickPoint());
                        event.consume();
                    }
                } else if (event.isDrag()) {
                    logger.log(Level.FINE, "Dragging {0}", topObject.getClass().getSimpleName());
                    if (topObject instanceof DragLine) {
                        if (dragProxyPoint != null) {
                            logger.fine("****** Proxy Drag ControlPoint ");
                            dragProxyPoint.moveTo(Positions.fromScreenPoint(event.getPickPoint().x, event.getPickPoint().y));
                            updateShape();
                        }
                        event.consume();
                    }
                    if (topObject instanceof ControlPoint) {
                        // Move the entire shape when the anchor point is moved
                        if (topObject == anchorPoint) {
                            logger.fine("**Dragging Anchor ");
                            hideControlPoints();
                            moveShape(anchorPoint.getPosition());
                        } else if (topObject == rotationHandle) {
                            // TODO: implement rotate
                        } else {
                            logger.fine("Dragging ControlPoint ");
                            updateShape();
                        }
                        event.consume();
                    }
                } else if (event.isDragEnd()) {
                    if (topObject instanceof ControlPoint) {
                        // Move the entire shape when the anchor point is moved
                        if (topObject == anchorPoint) {
                            moveShape(anchorPoint.getPosition());
                            updateControlPoints();
                            showControlPoints();
                        } else if (topObject == rotationHandle) {
                            // TODO: implement rotate
                        } else {
                        }
                    }

                } else if (event.isLeftDoubleClick()) {
                    if (topObject instanceof ControlPoint) {
                        // Toggle the existance of the anchor point
                        if (topObject == anchorPoint) {
                            // Disable availability of the anchor point
                            anchorPoint.setHighlightAttributes(ControlPoint.VERTEX_HILITE_ATTRS);
                            oldAnchorPoint = anchorPoint;
                            anchorPoint = null;
                        } else if (topObject == oldAnchorPoint) {
                            anchorPoint = oldAnchorPoint;
                            anchorPoint.setHighlightAttributes(ControlPoint.ANCHOR_HILITE_ATTRS);
                            oldAnchorPoint = null;
                        }
                    }
                }
            } finally {
                lastTopObject = topObject;
            }
        }
    }
}
