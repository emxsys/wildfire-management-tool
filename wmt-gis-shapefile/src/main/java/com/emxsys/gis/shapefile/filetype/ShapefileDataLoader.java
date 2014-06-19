/*
 * Copyright (c) 2011-2014, Bruce Schubert. <bruce@emxsys.com> 
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
package com.emxsys.gis.shapefile.filetype;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.loaders.MultiFileLoader;

/**
 * This Loader tests for the existence of the mandatory files that comprise a shapefile. All the
 * files with the same name as the primary files are considered brothers in the context of file
 * operations (e.g., copy, move, delete, ...).
 *
 * @see <href
 * http://bits.netbeans.org/dev/javadoc/org-openide-loaders/org/openide/loaders/doc-files/api.html#write-loader/>
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class ShapefileDataLoader extends MultiFileLoader {

    public final String PRIMARY_EXTENSION = "shp";
    public final String[] MANDATORY_EXTENSIONS = new String[]{
        "shp", "dbf", "shx"
    };

    /**
     * Constructor that associates the this loader with the {@link SHPDataObject} class.
     */
    public ShapefileDataLoader() {
        super("com.emxsys.gis.shapefile.filetype.ShapefileDataObject");
    }

    /**
     *
     * @return the path to the actions applicable to a shapefile
     */
    @Override
    protected String actionsContext() {
        // Get the actions registered in the XML layer
        return "Loaders/application/x-shapefile/Actions/";  // NOI18N
    }

    /**
     * Finds the primary file within a shapefile. This is the file with the .shp extension. A
     * shapefile is not valid unless all the mandatory files that make up a shapefile are available.
     * @param fo
     * @return the file with the .shp extension or null if not found or not valid.
     */
    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        String fileExt = fo.getExt();

        // Ensure all the mandatory files that comprise a shapefile are avialable...
        for (String mandatoryExt : MANDATORY_EXTENSIONS) {
            FileObject brother = FileUtil.findBrother(fo, mandatoryExt);
            if (brother == null) {
                return null;
            }
        }
        // ... and then return the primary file object
        if (PRIMARY_EXTENSION.equalsIgnoreCase(fileExt)) {
            return fo;
        }
        else {
            return FileUtil.findBrother(fo, PRIMARY_EXTENSION);
        }
    }

    /**
     * Constructs a {@link SHPDataObject} for the primary file.
     *
     * @param primaryFile A file with a .shp extension
     * @return A {@link SHPDataObject}
     * @throws DataObjectExistsException
     * @throws IOException
     */
    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws
            DataObjectExistsException,
            IOException {
        // Creates new file entry initially attached to a given file object that can handle
        // copy, move, rename and delete it without any modification.
        return new ShapefileDataObject(primaryFile, this);
    }

    /**
     * Creates new {@link FileEntry} initially attached to the supplied file object that can handle
     * copy, move, rename and delete it without any modification.
     * @param obj {@link SHPDataObject} instance
     * @param primaryFile represents the .shp file
     * @return {@link FileEntry} object that works with plain files.
     */
    @Override
    protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return new FileEntry(obj, primaryFile);
    }

    /**
     * Creates new {@link FileEntry} initially attached to the supplied file object that can handle
     * copy, move, rename and delete it without any modification.
     * @param obj A {@link SHPDataObject} instance
     * @param secondaryFile represents a brother of the .shp file
     * @return {@link FileEntry} object that works with plain files.
     */
    @Override
    protected Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
        return new FileEntry(obj, secondaryFile);
    }
}
