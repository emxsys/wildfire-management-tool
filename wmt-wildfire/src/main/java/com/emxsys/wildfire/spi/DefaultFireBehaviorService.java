/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wildfire.spi;

import com.emxsys.wildfire.api.FireBehaviorService;
import com.emxsys.wildfire.api.FiregroundProvider;
import com.emxsys.wildfire.surfacefire.SurfaceFireModel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * The DefaultFireBehaviorService provides the registered FireBehaviorService service provider found
 * on the global lookup, or, if not found, a SurfaceFireModel instance.
 *
 * @author Bruce Schubert
 */
public class DefaultFireBehaviorService {

    private static final Logger logger = Logger.getLogger(DefaultFireBehaviorService.class.getName());
    private static FireBehaviorService instance;

    /**
     * Hidden constructor.
     */
    private DefaultFireBehaviorService() {
    }

    /**
     * Gets the registered FiregroundProvider from the global lookup. If a service provider has not
     * been registered, then a SurfaceFireModel will be returned.
     *
     * @return The FireBehaviorService found on the global lookup, else a SurfaceFireModel.
     */
    public static FireBehaviorService getInstance() {
        {
            instance = Lookup.getDefault().lookup(FireBehaviorService.class);
            if (instance == null) {
                instance = new SurfaceFireModel();
            }
            logger.log(Level.CONFIG, "Providing a {0} instance.", instance.getClass().getName());
            return instance;
        }
    }

}