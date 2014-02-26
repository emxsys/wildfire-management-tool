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
package com.emxsys.wmt.util;

import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


/**
 *
 * @author Bruce Schubert
 * @version $Id: DataObjectUtil.java 470 2013-01-01 15:15:07Z bdschubert $
 */
public class DataObjectUtil
{

    private static final Logger logger = Logger.getLogger(DataObjectUtil.class.getName());


    /**
     * Locates the DataObject for the supplied file, and if the file is not found, creates the file
     * using the supplied template.
     *
     * @param parent folder containing the filename
     * @param filename file to load via DataObject
     * @param templatePath configFile path to template, e.g.,
     * "Templates/Symbology/SymbologyTemplate.xml"
     * @return the DataObject for the file
     */
    public static DataObject findDataObject(FileObject parent, String filename, String templatePath)
    {
        DataObject dob = null;
        try
        {
            FileObject fo = parent.getFileObject(filename);
            if (fo == null)
            {
                DataFolder folder = DataFolder.findFolder(parent);
                FileObject template = FileUtil.getConfigFile(templatePath);
                DataObject dTemplate = DataObject.find(template);
                String filenameWithoutExt = filename.replace(".xml", "");
                dob = dTemplate.createFromTemplate(folder, filenameWithoutExt);
            }
            else
            {
                dob = DataObject.find(fo);
            }
        }
        catch (Exception ex)
        {
            logger.severe(ex.getMessage());
        }
        return dob;
    }
}
