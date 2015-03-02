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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import java.awt.Color;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class DragLine implements Highlightable
{

    private List<Position> positions = new ArrayList<>();
    private SurfacePolyline foreground;
    private SurfacePolyline background;
    private List<Renderable> renderables = new ArrayList<>();
    private static final ShapeAttributes foregroundAttrs;
    private static final ShapeAttributes backgroundAttrs;


    static
    {
        // Specify attributes for the foreground line.
        foregroundAttrs = new BasicShapeAttributes();
        foregroundAttrs.setOutlineMaterial(new Material(Color.WHITE));
        foregroundAttrs.setOutlineStipplePattern((short) 0xAAAA);
        foregroundAttrs.setOutlineStippleFactor(8);

        // Specify attributes for the background line.
        backgroundAttrs = new BasicShapeAttributes();
        backgroundAttrs.setOutlineMaterial(new Material(Color.BLACK));
        backgroundAttrs.setOutlineOpacity(0.1);
        backgroundAttrs.setOutlineWidth(foregroundAttrs.getOutlineWidth() + 2);
    }


    public DragLine()
    {
        this.foreground = new SurfacePolyline();
        this.foreground.setAttributes(foregroundAttrs);
        this.foreground.setHighlightAttributes(foregroundAttrs);
        this.foreground.setDelegateOwner(this);

        this.background = new SurfacePolyline();
        this.background.setAttributes(backgroundAttrs);
        this.background.setHighlightAttributes(backgroundAttrs);
        this.background.setDelegateOwner(this);

        this.renderables.add(background);
        this.renderables.add(foreground);


    }


    public DragLine(List<Position> positions, boolean closed)
    {
        this();
        this.positions.addAll(positions);
        setPositions(positions);
        setClosed(closed);
    }


    List<Renderable> getRenderables()
    {
        return renderables;
    }


    public final void setPositions(List<Position> positions)
    {
        background.setLocations(positions);
        foreground.setLocations(positions);
        this.positions.clear();
        this.positions.addAll(positions);
    }


    public List<Position> getPositions()
    {
        return positions;
    }


    public void setVisible(boolean visible)
    {
        background.setVisible(visible);
        foreground.setVisible(visible);
    }


    public final void setClosed(boolean closed)
    {
        background.setClosed(closed);
        foreground.setClosed(closed);
    }


    public boolean isClosed()
    {
        return background.isClosed();
    }


    @Override
    public boolean isHighlighted()
    {
        return background.isHighlighted();
    }


    @Override
    public void setHighlighted(boolean highlighted)
    {
        background.setHighlighted(highlighted);
        foreground.setHighlighted(highlighted);
        Globe.getInstance().getRendererComponent().setCursor(highlighted
            ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
            : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    }
}
