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
import com.emxsys.wmt.gis.api.marker.MarkerManager;
import static com.emxsys.wmt.globe.markers.BasicMarkerWriter.BASIC_MARKER_SCHEMA_FILE;
import com.emxsys.wmt.util.TimeUtil;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Bruce Schubert
 * @version $Id: MarkerSupport.java 374 2012-12-05 21:11:11Z bdschubert $
 */
public class MarkerSupport {

    private static final Logger logger = Logger.getLogger(MarkerSupport.class.getName());
    private static Schema schema;

    /**
     * Gets a factory object from the class embedded in the Marker element.
     * @param document The document containing the Marker element.
     * @return A new Marker.Builder instance; throws on error.
     */
    public static Marker.Builder getBuilder(Document document) {

        String clazz = null;
        NodeList list = document.getElementsByTagName("Marker");
        try {
            // Construct a factory object.
            // Use the Marker.Builder(Document document) interface.
            clazz = ((Element) list.item(0)).getAttribute("factory");
            return (Marker.Builder) Class.forName(clazz)
                    .getConstructor(Document.class)
                    .newInstance(document);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Cannot create Marker.Factory: \"{0}\". Reason: {1}",
                    new Object[]{clazz, ex.toString()});
            throw new RuntimeException(ex);
        }
    }

    /**
     * Called by Builder clients (e.g., actions) to get the folder for new Markers.
     * @return A FileObject for the "markers" folder; returns null if not found.
     */
    public static FileObject getFolderFromCurrentProject() {
        Project currentProject = Utilities.actionsGlobalContext().lookup(Project.class);
        if (currentProject != null) {
            MarkerManager manager = currentProject.getLookup().lookup(MarkerManager.class);
            if (manager != null) {
                return manager.getFolder();
            }
        }
        return null;
    }

    /**
     * @return A FileObject for the BASIC_MARKER_SCHEMA_FILE resource.
     * @see URLMapper#findFileObject(java.net.URL)
     */
    public static FileObject getLocalSchemaFile() {
        // Get a file object from a jar file entry (URL).
        URL resource = BasicMarkerWriter.class.getResource(BASIC_MARKER_SCHEMA_FILE);
        return URLMapper.findFileObject(resource);
    }

    /**
     * Loads the BasicMarkerSchema.xsd schema.
     * @return Schema representing {@code BasicMarkerSchema.xsd}.
     */
    static Schema getMarkerSchema() {
        if (schema == null) {
            SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL schemaUrl = BasicMarkerWriter.class.getResource(BASIC_MARKER_SCHEMA_FILE);
            try {
                logger.log(Level.CONFIG, "Loading Schema ({0}) ...", schemaUrl);
                long startMs = System.currentTimeMillis();
                schema = f.newSchema(schemaUrl);
                logger.log(Level.FINE, "Schema loaded: {0} secs", TimeUtil.msToSecs(System.currentTimeMillis() - startMs));
            } catch (SAXException exception) {
                logger.severe(exception.getMessage());
            }
        }
        return schema;
    }

}
