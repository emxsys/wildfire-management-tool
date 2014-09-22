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
package com.emxsys.solar.spi;

import com.emxsys.solar.api.SunlightProvider;
import com.emxsys.solar.internal.SPASunlightProvider;
import org.openide.util.Lookup;

/**
 * SunlightProvider factory. The default instance can be overridden by creating SolarProvider
 * service provider.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class SunlightProviderFactory  {

    private static SunlightProvider instance = null;

    /**
     * Returns the singleton instance of a SunlightProvider. If a class has been registered as a
     * SunlightProvider service provider, then an instance of that class will be returned.
     * Otherwise, an instance of the RothermelDefaultSunlightProvider will be returned.
     *
     * @return A singleton instance of a SunlightProviderFactory.
     */
    public static SunlightProvider getInstance() {
        if (instance == null) {
            // Check the general Lookup for a service provider
            instance = Lookup.getDefault().lookup(SunlightProvider.class);

            // Use our default factory if no registered provider.
            if (instance == null) {
                instance = new SPASunlightProvider();
            }
        }
        return instance;
    }

}
