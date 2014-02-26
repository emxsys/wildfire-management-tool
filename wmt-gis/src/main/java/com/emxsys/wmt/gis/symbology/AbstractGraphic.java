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
package com.emxsys.wmt.gis.symbology;

import com.emxsys.wmt.gis.GeoCoord3D;
import com.emxsys.wmt.gis.Viewers;
import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.symbology.api.Graphic;
import com.emxsys.wmt.gis.viewer.api.GisViewer;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Random;
import java.util.logging.Logger;
import org.openide.util.Lookup;


/**
 *
 * @see com.emxsys.worldwind.symbology.BasicGraphic
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: AbstractGraphic.java 543 2013-04-18 20:13:07Z bdschubert $
 */
public abstract class AbstractGraphic implements Graphic
{

    private long uniqueID;
    private String name;
    private boolean deleted = false;
    private boolean selected = false;
    private boolean visible = true;
    private Renderer renderer;
    protected GeoCoord3D referencePosition;
    protected PropertyChangeSupport pcs;
    private static Random random = new Random();
    private static final Logger LOG = Logger.getLogger(AbstractSymbol.class.getName());
    


    public AbstractGraphic()
    {
        this(random.nextLong());
    }


    public AbstractGraphic(long uniqueID)
    {
        this.uniqueID = uniqueID;
        this.pcs = new PropertyChangeSupport(this);
    }


    @Override
    abstract public Lookup getLookup();


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
        pcs.firePropertyChange(PROP_GRAPHIC_NAME, oldName, this.name);
    }


    @Override
    public long getUniqueID()
    {
        return uniqueID;
    }


    @Override
    public void setUniqueID(long uniqueID)
    {
        long oldUniqueID = getUniqueID();
        this.uniqueID = uniqueID;
        pcs.firePropertyChange(PROP_GRAPHIC_UNIQUE_ID, oldUniqueID, this.uniqueID);
    }


    @Override
    public Coord3D getPosition()
    {
        return this.referencePosition;
    }


    @Override
    public void setPosition(Coord3D location)
    {
        Coord3D oldLocation = getPosition();
        try
        {
            this.referencePosition = new GeoCoord3D(location);
        }
        catch (Exception ex)
        {
            this.referencePosition = GeoCoord3D.INVALID_POSITION;
        }
        pcs.firePropertyChange(PROP_GRAPHIC_POSITION, oldLocation, this.referencePosition);
    }


    @Override
    abstract public Image getImage();


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


    @Override
    public Graphic.Renderer getRenderer()
    {
        if (renderer == null)
        {
            GisViewer activeViewer = Viewers.getPrimaryViewer();
            if (activeViewer != null)
            {
                renderer = activeViewer.getLookup().lookup(Graphic.Renderer.class);
            }
        }
        return renderer;
    }


    @Override
    public void delete()
    {
        // As a matter of practice, prevent multiple deletes 
        if (!isDeleted())
        {
            boolean oldDeleted = this.deleted;
            this.deleted = true;
            pcs.firePropertyChange(PROP_GRAPHIC_DELETED, oldDeleted, this.deleted);
            setSelected(false);
        }
    }


    @Override
    public boolean isDeleted()
    {
        return this.deleted;
    }


    @Override
    public void setSelected(boolean selected)
    {
        boolean oldSelected = this.selected;
        this.selected = selected;
        pcs.firePropertyChange(PROP_GRAPHIC_SELECTED, oldSelected, this.selected);
    }


    @Override
    public boolean isSelected()
    {
        return this.selected;
    }


    @Override
    public void setVisible(boolean visible)
    {
        boolean oldVisible = this.visible;
        this.visible = true;
        pcs.firePropertyChange(PROP_GRAPHIC_VISIBLE, oldVisible, this.visible);
    }


    @Override
    public boolean isVisible()
    {
        return this.visible;
    }
}
