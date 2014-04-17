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

import com.emxsys.wmt.gis.api.marker.Marker;
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
 * BasicMarkerTemplateHandler is a CreateFromTemplateHandler service provider that creates
 * a new Marker XML file.
 *
 * @author Bruce Schubert
 */
@ServiceProvider(service = CreateFromTemplateHandler.class)
public class BasicMarkerTemplateHandler extends CreateFromTemplateHandler {

    private static final Logger logger = Logger.getLogger(BasicMarkerTemplateHandler.class.getName());

    @Override
    protected boolean accept(FileObject orig) {
        String mimeType = orig.getMIMEType();
        return mimeType.equals("text/emxsys-wmt-basicmarker+xml")
                || mimeType.equals("text/emxsys-worldwind-basicmarker+xml");
    }

    /**
     * Creates a Marker from a template.
     *
     * @param template FileObject to copy to the target file
     * @param folder Location for the target file
     * @param name Name for the target file
     * @param parameters Contains a "model" BasicMarker object that's copied into the target file
     * @return A FileObject representing the Marker.
     * @throws IOException
     */
    @Override
    protected FileObject createFromTemplate(FileObject template, FileObject folder, String name,
                                            Map<String, Object> parameters) throws IOException {
        // Get the marker object from the parameters.
        Object model = parameters.get("model");
        if (!(model instanceof BasicMarker)) {
            String msg = "createFromTemplate: 'model' parameter is null or not a BasicMarker.";
            logger.severe(msg);
            throw new IllegalArgumentException(msg);
        }
        BasicMarker marker = (BasicMarker) model;
        
        // ASSERT that our marker has a writer
        AbstractMarkerWriter writer = marker.getLookup().lookup(AbstractMarkerWriter.class);
        if (writer == null) {
            String msg = "createFromTemplate: marker must have an AbstractMarkerWriter instance in its lookup.";
            logger.severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Strip any extension from the name because we'll supply the extension
        String ext = FileUtil.getExtension(name);
        if (ext.length() != 0) {
            name = name.substring(0, name.length() - ext.length() - 1);
        }
        ext = template.getExt();

        // Create the target file from the template
        FileObject targetFile = template.copy(folder, name, ext);
        FileLock lock=targetFile.lock();
        try {
            // Create an XML document from the template file
            InputSource source = new InputSource(template.getInputStream());
            Document doc = XMLUtil.parse(source, false, false, null, null);
            
            // Write the marker data to the XML file
            writer.document(doc).write();
            
            // Write the XML to disk
            try (OutputStream output = targetFile.getOutputStream(lock)) {
                XMLUtil.write(doc, output, "UTF-8");
                output.flush();
            }
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, "createFromTemplate could not update file from model: {0}", ex.toString());
        } finally {
            lock.releaseLock();
        }
        
        return targetFile;
    }
}
