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
package com.emxsys.gis.api;

import com.emxsys.gis.api.Feature;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 * Abstract class that implements common methods for a concrete implementation of Feature.
 *
 * @author Bruce Schubert
 * @version $Id: AbstractFeature.java 528 2013-04-18 15:04:46Z bdschubert $
 */
public abstract class AbstractFeature implements Feature {

    private InstanceContent content = new InstanceContent();
    private AbstractLookup lookup;
    private ProxyLookup proxyLookup;

    /**
     * Gets this Feature's lookup merged with the Geometry lookup.
     *
     * @return a ProxyLookup object initialized from this object and the Geography lookups
     */
    @Override
    public Lookup getLookup() {
        if (this.lookup == null) {
            this.lookup = new AbstractLookup(content);
            this.proxyLookup = new ProxyLookup(this.lookup, getGeometry().getLookup());
        }
        return this.proxyLookup;
    }

    /**
     * Provides access to this Feature's lookup contents; allows sub-classes to add or remove
     * objects.
     *
     * @return the AbstractLookup's content.
     */
    protected InstanceContent getInstanceContent() {
        return this.content;
    }
}  // AbstractFeature
