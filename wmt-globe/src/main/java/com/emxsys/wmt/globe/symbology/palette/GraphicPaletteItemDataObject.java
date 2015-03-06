/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.symbology.palette;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * This class manages the contents of a properties file that contains Palette Item Data for Tactical
 * Graphics and Symbols. It manifests a PaletteItemNode.
 *
 * Palette items are defined as .symbol and .graphic files in the layer.xml config. For example:
 * <pre>
 * {@code
 * <filesystem>
 *      <folder name="Globe Palette">
 *          <!--Subfolders are the palette categories-->
 *          <folder name="EMS Incidents">
 *              <attr name="position" intvalue="100"/>
 *
 *              <!--Files are palette items.  See the Globe's symbology PaletteItemDataObject class-->
 *              <file name="EMS.INCDNT.FIRE.symbol" url="symbology/symbols/EMS.INCDNT.FIRE.properties">
 *                  <attr name="position" intvalue="100"/>
 *              </file>
 *              <file name="EMS.INCDNT.CVDIS.symbol" url="symbology/symbols/EMS.INCDNT.CVDIS.properties">
 *                  <attr name="position" intvalue="1000"/>
 *              </file>
 *
 *          </folder>
 *      </folder>
 *  </filesystem>
 *
 *
 * }</pre>
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({
    "LBL_Graphic_LOADER=Tactical Graphic palette items",}
)
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Graphic_LOADER",
        mimeType = "text/x-graphic",
        extension = "graphic"
)
@DataObject.Registrations({
    @DataObject.Registration(
            mimeType = "text/x-graphic",
            iconBase = "com/emxsys/wmt/globe/images/favorite.png",
            displayName = "#LBL_Graphic_LOADER",
            position = 2001)
})
public class GraphicPaletteItemDataObject extends MultiDataObject {

    private Object data;

    public GraphicPaletteItemDataObject(FileObject pf, MultiFileLoader loader) throws
            DataObjectExistsException, IOException {
        super(pf, loader);

        // Read the file 
        InputStream inputStream = pf.getInputStream();
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();
        data = new GraphicPaletteData(properties);

        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }

    @Override
    protected Node createNodeDelegate() {
        //return new DataNode(this, Children.LEAF, getLookup());
        return new PaletteItemNode(this, data);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
}
