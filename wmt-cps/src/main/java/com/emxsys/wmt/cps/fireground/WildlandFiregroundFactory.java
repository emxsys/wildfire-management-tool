/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.cps.fireground;

import com.emxsys.wmt.wildfire.api.FiregroundProvider;
import java.io.IOException;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;


/**
 * This class is a registered FiregroundProvider service provider that creates a DataObject
 * representing the "fireground" folder.
 *
 * @author Bruce Schubert
 */
@ServiceProvider(service = FiregroundProvider.class)
public class WildlandFiregroundFactory implements FiregroundProvider
{
    private static final String TEMPLATE_CONFIG_FILE = "Templates/Fire/FiregroundTemplate.xml";
    private static final String FIREGROUND_FILENAME = "fireground";
    private static final Logger logger = Logger.getLogger(WildlandFiregroundFactory.class.getName());

    /**
     * Creates a DataObject from the contents of the fireground folder.
     *
     * @param parent folder containing fireground folder
     * @param foldername for fireground folder
     * @return a new WildlandFiregroundDataObject
     */
    @Override
    public DataObject getFiregroundDataObject(FileObject parent, String foldername)
    {
        try
        {
            FileObject folder = parent.getFileObject(foldername);
            if (folder==null)
            {
                return null;
            }
            FileObject fg = folder.getFileObject(FIREGROUND_FILENAME, "xml");
            return fg == null ? null : DataObject.find(fg);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("getFiregoundDataObject failed.", ex);
        }
    }


    @Override
    public DataObject newFiregroundDataObject(FileObject parent, String foldername)
    {
        try
        {
            FileObject folder = FileUtil.createFolder(parent, foldername);
            DataFolder dataFolder = DataFolder.findFolder(folder);
            DataObject template = DataObject.find(FileUtil.getConfigFile(TEMPLATE_CONFIG_FILE));
            return template.createFromTemplate(dataFolder, FIREGROUND_FILENAME);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("newFiregoundDataObject failed.", ex);
        }
    }
}
