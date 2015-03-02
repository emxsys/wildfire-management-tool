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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;


/**
 * A ControlPoint represents a vertex that can be used to control a shape's geometry.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: ControlPoint.java 392 2012-12-08 20:53:05Z bdschubert $
 */
public class ControlPoint extends PointPlacemark
{
    // ABGR color strins: 0xaabbggrr

    public final static String NORMAL_POINT_COLOR = "0xffffffff";       // white
    public final static String HIGHLIGHTED_POINT_COLOR = "0xffffffe0";  // cyan
    public final static String SELECTED_POINT_COLOR = "0xffffff00";     // 
    public final static String SELECTED_AND_HIGHLIGHTED_POINT_COLOR = "0xffffff00";
    public final static double NORMAL_POINT_SIZE = 10.0;
    public final static double HILITE_POINT_SIZE = 13.0;
    public static final PointPlacemarkAttributes VERTEX_ATTRS;
    public static final PointPlacemarkAttributes ANCHOR_ATTRS;
    public static final PointPlacemarkAttributes GHOST_ATTRS;
    public static final PointPlacemarkAttributes HANDLE_ATTRS;
    public static final PointPlacemarkAttributes VERTEX_HILITE_ATTRS;
    public static final PointPlacemarkAttributes ANCHOR_HILITE_ATTRS;
    public static final PointPlacemarkAttributes GHOST_HILITE_ATTRS;
    public static final PointPlacemarkAttributes HANDLE_HILITE_ATTRS;
    private GraphicDesigner editor;
    private ControlPointNode node;
    private static final Logger logger = Logger.getLogger(ControlPoint.class.getName());


    static
    {
        VERTEX_ATTRS = new PointPlacemarkAttributes();
        VERTEX_ATTRS.setUsePointAsDefaultImage(true);
        VERTEX_ATTRS.setScale(NORMAL_POINT_SIZE);

        ANCHOR_ATTRS = new PointPlacemarkAttributes(VERTEX_ATTRS);
        GHOST_ATTRS = new PointPlacemarkAttributes(VERTEX_ATTRS);
        HANDLE_ATTRS = new PointPlacemarkAttributes(VERTEX_ATTRS);
        VERTEX_HILITE_ATTRS = new PointPlacemarkAttributes(VERTEX_ATTRS);
        ANCHOR_HILITE_ATTRS = new PointPlacemarkAttributes(VERTEX_ATTRS);
        GHOST_HILITE_ATTRS = new PointPlacemarkAttributes(VERTEX_ATTRS);
        HANDLE_HILITE_ATTRS = new PointPlacemarkAttributes(VERTEX_ATTRS);

        VERTEX_ATTRS.setLineMaterial(Material.CYAN);
        ANCHOR_ATTRS.setLineMaterial(Material.WHITE);
        HANDLE_ATTRS.setLineMaterial(Material.ORANGE);
        GHOST_ATTRS.setLineMaterial(Material.YELLOW);

        VERTEX_HILITE_ATTRS.setLineMaterial(Material.CYAN);
        ANCHOR_HILITE_ATTRS.setLineMaterial(Material.WHITE);
        HANDLE_HILITE_ATTRS.setLineMaterial(Material.ORANGE);
        GHOST_HILITE_ATTRS.setLineMaterial(Material.YELLOW);

        VERTEX_HILITE_ATTRS.setScale(HILITE_POINT_SIZE);
        ANCHOR_HILITE_ATTRS.setScale(HILITE_POINT_SIZE);
        HANDLE_HILITE_ATTRS.setScale(HILITE_POINT_SIZE);
        GHOST_HILITE_ATTRS.setScale(HILITE_POINT_SIZE);

    }


    public ControlPoint(GraphicDesigner parent, Position position)
    {
        this(parent, position, VERTEX_ATTRS, VERTEX_HILITE_ATTRS);
    }


    public ControlPoint(GraphicDesigner editor, Position position,
        PointPlacemarkAttributes normalAttrs,
        PointPlacemarkAttributes hiliteAttrs)
    {
        super(position);
        this.editor = editor;
        this.node = new ControlPointNode(this);

        setAttributes(normalAttrs);
        setHighlightAttributes(hiliteAttrs);

        // Increase line pick width to ease picking; default line pick width is 10
        setLinePickWidth(25);

    }


    public Node getNode()
    {
        return node;
    }


    /**
     * The ControlPointNode provides the ContextMenu for ControlPoints.
     *
     */
    public class ControlPointNode extends AbstractNode
    {

        private final ControlPoint point;


        public ControlPointNode(ControlPoint cp)
        {
            super(Children.LEAF, Lookups.fixed(cp));
            this.point = cp;
        }


        /**
         * Get the permissible actions for this control point.
         *
         * @param context true if called for context menu
         * @return permissible actions.
         */
        @Override
        public Action[] getActions(boolean context)
        {
            ArrayList<Action> actions = new ArrayList<Action>();

            if (editor.canDeleteControlPoint(this.point))
            {
                actions.add(new AbstractAction("Delete Point", ImageUtilities.loadImageIcon("com/emxsys/worldwind/resources/delete.png", true))
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        editor.deleteControlPoint(point);

                    }
                });
            }
            if (editor.isAnchorPoint(point))
            {
                actions.add(new AbstractAction("Toggle Move Point/Shape")
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        editor.toggleAnchorPoint();
                    }
                });
            }

            Action[] array = new Action[actions.size()];
            actions.toArray(array);
            return array;
        }
    }

}
