/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.markers;

import com.emxsys.wmt.gis.api.marker.MarkerCatalog;
import com.emxsys.wmt.util.FilenameUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

/**
 *
 * @author Bruce Schubert
 * @version $Id: MarkerSupport.java 374 2012-12-05 21:11:11Z bdschubert $
 */
public class MarkerSupport {

    public static final String FILENAME_PREFIX = "Marker-";
    public static final String FILENAME_EXTENSION = "xml";
    private static final Logger logger = Logger.getLogger(MarkerSupport.class.getName());

    /**
     * Create a new BasicMarkerDataObject (file) from a model Marker object.
     *
     * @param marker The model marker who's values will be stored in the file.
     * @param folder The folder where the DataObject will be created. If null, trys to get it from
     * the current project.
     * @param template The file template.
     * @return a new BasicMarkerDataObject
     * @see MarkerCreateFromTemplateHandler
     */
    public static DataObject createBasicMarkerDataObject(BasicMarker marker, FileObject folder, DataObject template) {
        try {
            // Use the current project folder if not given
            if (folder == null) {
                Project currentProject = Utilities.actionsGlobalContext().lookup(Project.class);
                if (currentProject != null) {
                    MarkerCatalog catalog = currentProject.getLookup().lookup(MarkerCatalog.class);
                    if (catalog != null) {
                        folder = catalog.getFolder();
                    }
                }
            }
            // throws an IllegalArgumentException if not found
            DataFolder dataFolder = DataFolder.findFolder(folder);

            // Ensure the filename is unique -- appends a numeral if not
            String filename = FilenameUtils.getUniqueEncodedFilename(
                    folder, marker.getName(), template.getPrimaryFile().getExt());

            // Create the marker file from our template -- delegated to BasicMarkerTemplateHandler, 
            // which is a CreateFromTemplateHandler service provider
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("model", marker);
            DataObject dataObject = template.createFromTemplate(dataFolder, filename, parameters);

            return dataObject;
        } catch (IllegalArgumentException | IOException exception) {
            logger.log(Level.SEVERE, "createBasicMarkerDataObject() failed: {0}", exception.toString());
            return null;
        }
    }
}
