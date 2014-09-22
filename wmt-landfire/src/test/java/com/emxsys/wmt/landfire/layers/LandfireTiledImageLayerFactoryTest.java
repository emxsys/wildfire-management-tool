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
package com.emxsys.wmt.landfire.layers;

import com.emxsys.util.HttpUtil;
import com.emxsys.wmt.landfire.options.LandfireOptionsPanel;
import static com.emxsys.wmt.landfire.options.LandfireOptionsPanel.SERVICE_GETCAPABILITIESURL;
import gov.nasa.worldwind.util.WWXML;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;


/**
 *
 * @author Bruce Schubert
 */
@Ignore
@RunWith(Parameterized.class)
public class LandfireTiledImageLayerFactoryTest {

    private final FileObject fo;
    private final File xmlCfgFile;

    /*
     * Constructor.
     * The JUnit test runner will instantiate this class once for every
     * element in the Collection returned by the method annotated with
     * @Parameters.
     */
    public LandfireTiledImageLayerFactoryTest(FileObject fo) {
        this.fo = fo;
        URL configUrl = (URL) fo.getAttribute("config");
        String path = configUrl.toString().replace("nbinst://com-emxsys-wmt-landfire", "release");
        this.xmlCfgFile = new File(path);
    }

    /*
     * Test data generator -- get a FileObject for each Landfire layer specified in the layer.xml file.
     * This method is called the the JUnit parameterized test runner and
     * returns a Collection of Arrays.  For each Array in the Collection,
     * each array element corresponds to a parameter in the constructor.
     */
    @Parameters
    public static Collection<Object[]> generateData() {
        List<Object[]> data = new ArrayList<>();

        // Read configs from the Layer.XML file
        FileObject[] children = FileUtil.getConfigFile(LandfireOptionsPanel.XML_LAYER_FOLDER).getChildren();
        for (FileObject fo : children) {
            // Filter out layers that aren't from LANDFIRE
            String source = (String) fo.getAttribute("source");
            if (source != null && source.equals("LANDFIRE")) {
                //System.out.println(fo.getAttribute("config"));
                data.add(new Object[]{fo});
            }
        }
        return data;
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testGetLayerDomElement_File() {
        System.out.println("getLayerDomElement - " + xmlCfgFile);
        Element result = LandfireTiledImageLayerFactory.getLayerDomElement(xmlCfgFile);
        assertNotNull(result);
    }

    @Test
    public void testGetCapabilities() throws MalformedURLException, IOException {
        System.out.println("testGetCapabilties - " + xmlCfgFile);
        Element dom = LandfireTiledImageLayerFactory.getLayerDomElement(xmlCfgFile);
        assertNotNull(dom);

        String capsUrl = WWXML.getText(dom, SERVICE_GETCAPABILITIESURL);
        capsUrl += "request=GetCapabilities&service=WMS";
        String result = HttpUtil.callWebService(new URL(capsUrl));

        assertNotNull(result);
        assertTrue(!result.isEmpty());
        //System.out.println(result);
    }

    @Test
    public void testIsTiledImageLayer() {
        System.out.println("isTiledImageLayer- " + xmlCfgFile);
        Element domElement = LandfireTiledImageLayerFactory.getLayerDomElement(xmlCfgFile);
        boolean expResult = true;
        boolean result = LandfireTiledImageLayerFactory.isTiledImageLayer(domElement);
        assertEquals(expResult, result);
    }

}
