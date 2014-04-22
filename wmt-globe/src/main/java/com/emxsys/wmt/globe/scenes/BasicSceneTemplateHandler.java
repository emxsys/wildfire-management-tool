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
package com.emxsys.wmt.globe.scenes;

import com.emxsys.wmt.globe.scenes.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * This CreateFromTemplateHandler service provider populates a SceneTemplate.xml file with the
 * contents of Scene object passed in the "model" parameter.
 *
 * @author Bruce Schubert
 * @version $Id: BasicSceneTemplateHandler.java 442 2012-12-12 13:15:16Z bdschubert $
 */
@ServiceProvider(service = CreateFromTemplateHandler.class)
public class BasicSceneTemplateHandler extends CreateFromTemplateHandler
{

    private static final Logger logger = Logger.getLogger(BasicSceneTemplateHandler.class.getName());


    @Override
    protected boolean accept(FileObject orig)
    {
        String mimeType = orig.getMIMEType();
        return mimeType.equals("text/emxsys-worldwind-basicscene+xml");
    }


    /**
     *
     * @param template
     * @param folder
     * @param name
     * @param parameters
     * @return
     * @throws IOException
     */
    @Override
    protected FileObject createFromTemplate(FileObject template, FileObject folder, String name,
        Map<String, Object> parameters) throws IOException
    {
        // Update the target file contents from the model
        Object model = parameters.get("model");
        if (!(model instanceof BasicScene))
        {
            String msg = "createFromTemplate 'model' parameter is null or not a BasicScene";
            logger.severe(msg);
            throw new IllegalArgumentException(msg);
        }
        // Strip any extension from the name because we'll supply the extension
        String ext = FileUtil.getExtension(name);
        if (ext.length() != 0)
        {
            name = name.substring(0, name.length() - ext.length() - 1);
        }
        ext = template.getExt();

        // Create the target file from the template
        FileObject targetFile = template.copy(folder, name, ext);

        try
        {
            // Update the target file's contents from the model
            InputSource source = new InputSource(template.getInputStream());
            Document doc = XMLUtil.parse(source, false, false, null, null);
            BasicSceneXmlEncoder.writeDocument(doc, (BasicScene) model);
            OutputStream output = targetFile.getOutputStream(FileLock.NONE);
            XMLUtil.write(doc, output, "UTF-8");
            output.flush();
            output.close();
        }
        catch (SAXException ex)
        {
            logger.log(Level.SEVERE, "createFromTemplate could not update file from model: {0}", ex.toString());
        }
        return targetFile;
    }
}
