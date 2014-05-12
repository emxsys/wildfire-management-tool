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
package com.emxsys.wmt.globe;

import com.terramenta.globe.WorldWindManager;
import com.terramenta.globe.options.GlobeOptions;
import java.io.File;
import java.net.URL;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * A specialized WorldWindManager for the WMT.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ServiceProvider(service = WorldWindManager.class, position = 1, supersedes = "com.terramenta.globe.WorldWindManager")
public class EmxsysWorldWindManager extends WorldWindManager {

    public static final String WORLDWIND_CONFIG = "com/emxsys/wmt/globe/worldwind.xml";
    public static final String WORLDWIND_OVERRIDES = "com/emxsys/wmt/globe/worldwind.xml";
    private static final Preferences prefs = NbPreferences.forModule(GlobeOptions.class);
    private static final Logger logger = Logger.getLogger(EmxsysWorldWindManager.class.getName());

    /**
     * Override WorldWind Configuration Settings with our application defaults while preserving the
     * user option to set the configuration. Assumes this static block is called *after* the base
     * class static block.
     */
    static {
        // The default World Wind configuration document is config/worldwind.xml.
        // This can be changed by setting the Java property gov.nasa.worldwind.config.file
        // to a different file name or a valid URL prior to creating any World Wind object or
        // invoking any static methods of World Wind classes. If a file is specified its
        // location must be on the classpath. (The contents of application and WorldWind jar
        // files are typically on the classpath, in which case the  configuration file may 
        // be in the jar file.)
        //URL config = Thread.currentThread().getContextClassLoader().getResource(WORLDWIND_CONFIG);
        //System.setProperty("gov.nasa.worldwind.config.file", config.toString());

        // Additionally, an application may set another Java property, 
        // gov.nasa.worldwind.app.config.document, to a file name or URL whose contents contain 
        // configuration values to override those of the primary configuration document.
        // World Wind overrides only those values in this application document, it leaves all 
        // others to the value specified in the primary document. Applications usually specify 
        // an override document in order to specify the initial layers in the model.
        String config = prefs.get("options.globe.worldwindConfig", "");
        if (!config.isEmpty()) {
            System.setProperty("gov.nasa.worldwind.app.config.document", config);
        } else {
            URL overrides = Thread.currentThread().getContextClassLoader().getResource(WORLDWIND_OVERRIDES);
            if (overrides != null) {
                System.setProperty("gov.nasa.worldwind.app.config.document", overrides.toString());
            } else {
                logger.severe("Cannot locate resource: " + WORLDWIND_OVERRIDES);
            }
        }
    }

    /**
     * Constructor called by ServiceProvider.
     */
    public EmxsysWorldWindManager() {
        super();
    }

}
