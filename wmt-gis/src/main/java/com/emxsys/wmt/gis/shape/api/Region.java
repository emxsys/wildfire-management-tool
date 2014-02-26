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
package com.emxsys.wmt.gis.shape.api;

import com.emxsys.wmt.gis.GeoSector;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.w3c.dom.Element;


/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: Region.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public interface Region extends Shape
{
    String PROP_REGION_SECTOR = "PROP_REGION_SECTOR";


    GeoSector getSector();


    void setSector(GeoSector sector);



    /**
     * 
     */
    public interface Renderer
    {
        public static final String PROP_REGION_ADDED = "PROP_REGION_ADDED";
        public static final String PROP_REGION_REMOVED = "PROP_REGION_REMOVED";


        void addRegion(Region region);


        void removeRegion(Region region);


        void addRegions(Collection<? extends Region> regions);


        boolean contains(Region region);


        void addPropertyChangeListener(PropertyChangeListener listener);


        void removePropertyChangeListener(PropertyChangeListener listener);

    }


    /**
     * 
     */
    public interface Provider
    {
        Region fromXmlElement(Element element);

    }
}
