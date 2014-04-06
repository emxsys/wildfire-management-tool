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
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
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
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Bruce Schubert
 */
public class BasicMarkerWriterTest {

    private Document doc;
    private BasicMarker marker;
    private FileObject folder;

    public BasicMarkerWriterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Creates the Document and Marker for used for testing.
     */
    @Before
    public void setUp() throws PropertyVetoException, IOException {

        // write out data our tests to memory here        
        this.folder = FileUtil.createMemoryFileSystem().getRoot();
        assertTrue(this.folder.canWrite());
        // -- or --
//        clearWorkDir();
//        LocalFileSystem fs = new LocalFileSystem();
//        fs.setRootDirectory(new File("C:/temp"));
//        this.folder = fs.getRoot();        

        // create the document that we'll update
        this.doc = XMLUtil.createDocument(null, null, null, null); // no document element
//        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
//        f.setNamespaceAware(true);
//        this.doc = f.newDocumentBuilder().newDocument();

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

        // Validate XML after every test
        System.out.print("Validating XML...");
        Schema schema = MarkerSupport.getMarkerSchema("2.0");
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(this.doc));
        System.out.println("Passed!");
    }

    /**
     * Test of document method, of class BasicMarkerWriter.
     */
    @Test
    public void testDocument() throws IOException {
        System.out.println("document");
        BasicMarkerWriter instance = new BasicMarkerWriter();
        instance.marker(marker);
        instance.document(doc);
        assertEquals(instance.getDocument(), doc);

        instance.write();
        
        // Dump the XML to the output window
        XMLUtil.write(this.doc, System.out, "UTF-8");
    }

    /**
     * Test of folder method, of class BasicMarkerWriter.
     */
    @Test
    public void testFolder() throws IOException {
        System.out.println("folder");
        BasicMarkerWriter instance = new BasicMarkerWriter();
        instance.marker(marker);
        instance.folder(folder);
        assertEquals(instance.getFolder(), folder);

        // Should create KOXR.xml in the folder.
        this.doc = instance.write();
        
        FileObject result = folder.getFileObject("KOXR.xml");
        assertNotNull(result);
        
        System.out.println(result.getNameExt());
        System.out.println(result.asText());
        
    }

    /**
     * Test of marker method, of class BasicMarkerWriter.
     */
    @Test
    public void testMarker() {
        System.out.println("marker");
        BasicMarkerWriter instance = new BasicMarkerWriter();
        instance.marker(marker);
        instance.document(doc);
        assertEquals(instance.getMarker(), marker);

        instance.write();
    }

    /**
     * Test of write method, of class BasicMarkerWriter.
     */
    @Test
    public void testWrite() {
        System.out.println("write");
        BasicMarkerWriter instance = new BasicMarkerWriter();
        try {
            instance.write();
        } catch (Exception e) {
            return;
        }
        fail("The test case should have thrown an error.");
        
    }


}
