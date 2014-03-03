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
package com.emxsys.wmt.gis.api.shape;

import com.emxsys.wmt.gis.GeoCoord3D;
import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.shape.Shape;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;


/**
 * AbstractShape implements the Emxsys {@link Shape} GIS interface.
 * 
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class AbstractShape implements Shape
{

    protected String name;
    protected GeoCoord3D position;
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static final Logger LOG = Logger.getLogger(AbstractShape.class.getName());



    @Override
    public String getName()
    {
        return this.name;
    }



    @Override
    public void setName(String name)
    {
        String oldName = getName();
        this.name = name;
        pcs.firePropertyChange(PROP_SHAPE_NAME, oldName, this.name);
    }



    @Override
    public Coord3D getPosition()
    {
        return this.position;
    }



    @Override
    public void setPosition(Coord3D location)
    {
        Coord3D oldLocation = getPosition();
        try
        {
            this.position = new GeoCoord3D(location);
        }
        catch (Exception ex)
        {
            this.position = GeoCoord3D.INVALID_POSITION;
        }
        pcs.firePropertyChange(PROP_SHAPE_POSITION, oldLocation, this.position);
    }





    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.addPropertyChangeListener(listener);
    }



    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.removePropertyChangeListener(listener);
    }



    @Override
    public String toString()
    {
        return getName();
    }
}
