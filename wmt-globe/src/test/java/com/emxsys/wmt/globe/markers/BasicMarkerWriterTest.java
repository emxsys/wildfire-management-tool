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
package com.emxsys.wmt.globe.markers;

import com.emxsys.wmt.gis.api.GeoCoord3D;
import static com.emxsys.wmt.globe.markers.BasicMarkerWriter.BASIC_MARKER_NS_URI;
import java.beans.PropertyVetoException;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Bruce Schubert
 */
public class BasicMarkerWriterTest {
    private static Schema schema;

    private Document doc;
    private BasicMarker marker;
    private FileObject folder;

    public BasicMarkerWriterTest() {
    }

    /**
     * Validates the Document to the Schema.
     * @param doc The document to validate.
     * @throws IOException
     * @throws SAXException
     */
    void validate(Document doc) throws IOException, SAXException {

        // Dump the XML to the output window
        XMLUtil.write(doc, System.out, "UTF-8");
        try {
            System.out.print(">>>Validating XML...");
            XMLUtil.validate(doc.getDocumentElement(), schema);
            //Validator validator = schema.newValidator();
            //validator.validate(new DOMSource(doc));
            System.out.println("Passed!");
        } catch (Exception ex) {
            System.out.println("FAILED! " + ex.getMessage());
            throw ex;
        }
    }

    @BeforeClass
    public static void setUpClass() {
        schema = MarkerSupport.getMarkerSchema("2.0");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Creates the Document and Marker for used for testing.
     */
    @Before
    public void setUp() throws PropertyVetoException, IOException, ParserConfigurationException {

        // write out data our tests to memory here        
        this.folder = FileUtil.createMemoryFileSystem().getRoot();
        assertTrue(this.folder.canWrite());
        // -- or --
//        clearWorkDir();
//        LocalFileSystem fs = new LocalFileSystem();
//        fs.setRootDirectory(new File("C:/temp"));
//        this.folder = fs.getRoot();        

        // create the document that we'll update
//        this.doc = XMLUtil.createDocument(null, null, null, null); // no root document element
        // -- or --
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setSchema(schema);
        this.doc = f.newDocumentBuilder().newDocument();

        // create the marker that we'll save
        this.marker = (BasicMarker) new BasicMarkerBuilder()
                .name("KOXR")
                .coordinate(GeoCoord3D.fromDegreesAndMeters(34.2, -119.2, 15))
                .build();

    }

    /**
     * Validates the Document schema after every test.
     */
    @After
    public void tearDown() throws IOException, SAXException {
    }

    /**
     * Test of document method, of class BasicMarkerWriter.
     */
    @Test
    public void testDocument() {
        System.out.println("document");
        BasicMarkerWriter instance = new BasicMarkerWriter();
        instance.document(doc);
        assertEquals(instance.getDocument(), doc);
    }

    /**
     * Test of folder method, of class BasicMarkerWriter.
     */
    @Test
    public void testFolder()  {
        System.out.println("folder");
        BasicMarkerWriter instance = new BasicMarkerWriter();
        instance.folder(folder);
        assertEquals(instance.getFolder(), folder);
    }

    /**
     * Test of marker method, of class BasicMarkerWriter.
     */
    @Test
    public void testMarker() {
        System.out.println("marker");
        BasicMarkerWriter instance = new BasicMarkerWriter();
        instance.marker(marker);
        assertEquals(instance.getMarker(), marker);
    }

    /**
     * Test of write method, of class BasicMarkerWriter.
     */
    @Test
    public void testWriteToADocument() throws IOException, SAXException {
        System.out.println("write to document");
        BasicMarkerWriter instance = new BasicMarkerWriter();
        instance.marker(marker);
        instance.document(doc);
        Document result = instance.write();
        assertNotNull(result);
        
        // Does not validate correctly.  However XML Document created by the BasicMarkerDataObject
        // works correctly.  I don't know why the Document created by the factory doesn't work. ARGH!
        //validate(result);
        //>>>Validating XML...FAILED! NAMESPACE_ERR: An attempt is made to create or change an object in a way which is incorrect with regard to namespaces.
    }
    /**
     * Test of write method, of class BasicMarkerWriter.
     */
    @Test
    public void testWrite() throws IOException, SAXException {
        // Create the document
        System.out.println("write to folder");
        BasicMarkerWriter instance = new BasicMarkerWriter();
        instance.marker(marker);
        instance.folder(folder);
        Document doc1 = instance.write();
        assertNotNull(doc1);
        validate(doc1);

        // Update the document
//        System.out.println("write to document");
//        instance = new BasicMarkerWriter();
//        marker.setName("Oxnard Airport");
//        instance.marker(marker);
//        instance.document(doc1);
//        Document doc2 = instance.write();
//        assertNotNull(doc2);
//        validate(doc1);
        //>>>Validating XML...FAILED! NAMESPACE_ERR: An attempt is made to create or change an object in a way which is incorrect with regard to namespaces.

    }

}
