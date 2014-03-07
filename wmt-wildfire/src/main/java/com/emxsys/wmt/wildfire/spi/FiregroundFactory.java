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
package com.emxsys.wmt.wildfire.spi;

import com.emxsys.wmt.wildfire.api.FiregroundProvider;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

/**
 * Factory class that supplies either a default FiregroundFactory or a factory class registered on
 * the global lookup.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class FiregroundFactory implements FiregroundProvider {

    private static final Logger LOG = Logger.getLogger(FiregroundFactory.class.getName());

    protected FiregroundFactory() {
    }

    /**
     * Gets the registered FiregroundFactory from the global lookup. If a factory has not been
     * registered, then a DefaultFactory will be used.
     *
     * @return
     * @see DefaultFactory
     */
    public static FiregroundFactory getInstance() {
        {
            FiregroundFactory factory = Lookup.getDefault().lookup(FiregroundFactory.class);
            if (factory == null) {
                factory = new DefaultFactory();
            }
            LOG.log(Level.INFO, "getInstance() returning a {0}", factory.getClass().getName());
            return factory;
        }
    }

    /**
     * Creates a Fireground DataObjects based on the contents of an XML file.
     */
    private static final class DefaultFactory extends FiregroundFactory {

        /**
         * Gets a Fireground DataObject from the registered loader of the suppled file.
         *
         * @param folder where fireground file resides
         * @param filename of the fireground file
         * @return DataObject representing the fireground
         */
        @Override
        public DataObject getFiregroundDataObject(FileObject folder, String filename) {
            // Loop through the folder's datafiles and look for the supplied filename
            Enumeration<? extends FileObject> datafiles = folder.getData(false); // false = don't recurse
            FileObject fgFile = null;
            while (datafiles.hasMoreElements()) {
                FileObject fo = datafiles.nextElement();
                if (fo.getNameExt().equalsIgnoreCase(filename)) {
                    try {
                        // We found the file, now load it.
                        return DataObject.find(fo);
                    } catch (DataObjectNotFoundException ex) {
                        LOG.warning(ex.toString());
                    }
                }
            }
            return null;
        }

        /**
         * Creates a fireground xml file on the disk and loads it into a DataObject.
         *
         * @param parent folder where file will be created
         * @param filename for the new xml file
         * @return DataObject loaded from a template
         */
        @Override
        public DataObject newFiregroundDataObject(FileObject parent, String filename) {
            try {
                DataFolder folder = DataFolder.findFolder(parent);
                FileObject template = FileUtil.getConfigFile("Templates/Fire/FiregroundTemplate.xml");
                DataObject dTemplate = DataObject.find(template);
                String filenameWithoutExt = filename.replace(".xml", "");
                DataObject dob = dTemplate.createFromTemplate(folder, filenameWithoutExt);

                return dob;
            } catch (DataObjectNotFoundException ex) {
                LOG.severe(ex.toString());
            } catch (IOException ex) {

                LOG.severe(ex.toString());
            }
            return null;
        }
    }
}
