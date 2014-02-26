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
package com.emxsys.wmt.gis.layer.api;

import com.emxsys.wmt.gis.api.Named;
import java.beans.PropertyChangeListener;
import org.openide.util.Lookup;


/**
 * This interface provides the common capabilites of a GIS raster or vector layer. Capabilities and
 * meta-data should be exposed through the lookup.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: GisLayer.java 769 2013-06-20 18:11:51Z bdschubert $
 */
public interface GisLayer extends Lookup.Provider, Named
{
    /**
     * The lookup should contain the capabilities of a layer. For example it could contain objects
     * representing the types of data that would be returned by getObjectAtLatLon.
     *
     * @return the layer capabilities, meta-data and possibly the implementation
     */
    @Override
    Lookup getLookup();


    /**
     * The name of the layer, typically used for displaying the name in a layer manager, but also
     * used to uniquely identify a layer in a collection.
     *
     * @return the layer name (display name)
     */
    @Override
    String getName();


    /**
     * The name of the layer, typically used for displaying the name in a layer manager, but also
     * used to uniquely identify a layer in a collection.
     *
     */
    @Override
    void setName(String name);


    /**
     * Returns the enabled state.
     *
     * @return true if this layer is enabled.
     */
    boolean isEnabled();


    /**
     * Sets the enabled state.
     *
     * @param enabled the new enabled state.
     */
    void setEnabled(boolean enabled);


    void addPropertyChangeListener(PropertyChangeListener listener);


    void removePropertyChangeListener(PropertyChangeListener listener);
}
