/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
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
package com.emxsys.wmt.globe.layers;

import com.emxsys.wmt.gis.api.layer.LayerGroup;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import java.awt.Point;
import java.util.logging.Logger;

/**
 * A DummyLayer is simply a non-renderable layer placed in the LayerList collection used for
 * grouping new Layers at the proper position within the list.
 * <p>
 * @author Bruce Schubert
 */
public class DummyLayer extends WWObjectImpl implements Layer
{

    private static final Logger logger = Logger.getLogger(DummyLayer.class.getName());
    private String name;
    private LayerGroup group;

    public DummyLayer(LayerGroup layerGroup)
    {
        this.group = layerGroup;
        this.name = layerGroup.getName();
    }

    public LayerGroup getLayerGroup()
    {
        return this.group;
    }

    @Override
    public boolean isEnabled()
    {
        return false;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        logger.warning("Unsupported Operation called: setEnabled()");
    }

    @Override
    public String getName()
    {
        return name.toUpperCase();
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public double getOpacity()
    {
        logger.warning("Unsupported Operation called: getOpacity()");
        return 0.0;
    }

    @Override
    public void setOpacity(double opacity)
    {
        logger.warning("Unsupported Operation called: setOpacity()");
    }

    @Override
    public boolean isPickEnabled()
    {
        return false;
    }

    @Override
    public void setPickEnabled(boolean isPickable)
    {
        logger.warning("Unsupported Operation called: setPickEnabled()");
    }

    @Override
    public void preRender(DrawContext dc)
    {
    }

    @Override
    public void render(DrawContext dc)
    {
    }

    @Override
    public void pick(DrawContext dc, Point pickPoint)
    {
    }

    @Override
    public boolean isAtMaxResolution()
    {
        return false;
    }

    @Override
    public boolean isMultiResolution()
    {
        return false;
    }

    @Override
    public double getScale()
    {
        return 1.0;
    }

    @Override
    public boolean isNetworkRetrievalEnabled()
    {
        return false;
    }

    @Override
    public void setNetworkRetrievalEnabled(boolean networkRetrievalEnabled)
    {
        logger.warning("Unsupported Operation called: setNetworkRetrievalEnabled()");
    }

    @Override
    public void setExpiryTime(long expiryTime)
    {
        logger.warning("Unsupported Operation called: setExpiryTime()");
    }

    @Override
    public long getExpiryTime()
    {
        return Long.MAX_VALUE;
    }

    @Override
    public double getMinActiveAltitude()
    {
        return Double.MIN_VALUE;
    }

    @Override
    public void setMinActiveAltitude(double minActiveAltitude)
    {
        logger.warning("Unsupported Operation called: setMinActiveAltitude()");
    }

    @Override
    public double getMaxActiveAltitude()
    {
        return Double.MAX_VALUE;
    }

    @Override
    public void setMaxActiveAltitude(double maxActiveAltitude)
    {
        logger.warning("Unsupported Operation called: setMaxActiveAltitude()");
    }

    @Override
    public boolean isLayerInView(DrawContext dc)
    {
        return false;
    }

    @Override
    public boolean isLayerActive(DrawContext dc)
    {
        return false;
    }

    @Override
    public Double getMaxEffectiveAltitude(Double radius)
    {
        return Double.MAX_VALUE;
    }

    @Override
    public Double getMinEffectiveAltitude(Double radius)
    {
        return Double.MIN_VALUE;
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public String getRestorableState()
    {
        return null;
    }

    @Override
    public void restoreState(String stateInXml)
    {
        throw new UnsupportedOperationException("restoreState() not supported.");
    }

    @Override
    public String toString()
    {
        return this.getName();
    }
}
