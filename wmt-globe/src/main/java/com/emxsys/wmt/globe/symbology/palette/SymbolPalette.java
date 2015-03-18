/*
 * Copyright (c) 2015, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.symbology.palette;

import com.emxsys.util.ImageUtil;
import gov.nasa.worldwind.render.ScreenImage;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;

/**
 * This class utility contains the entities that comprise the Symbol palette.
 *
 * @author Bruce Schubert
 */
public class SymbolPalette {

    public static final DataFlavor SYMBOL_DATA_FLAVOR = new DataFlavor(ItemData.class, "Symbol");

    private SymbolPalette() {
    }

    /**
     * Manages the properties of a symbol item in the palette. A symbol item persists as a
     * .properties file resource.
     *
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    public static class ItemData {

        private Properties props;
        private Image icon16;
        private Image icon32;
        private static final String PROP_TYPE = "type";
        private static final String PROP_IDENTIFIER = "identifier";
        private static final String PROP_NAME = "display_name";
        private static final String PROP_COMMENT = "comment";
        private static final String PROP_QUANTITY = "quantity";
        private static final String PROP_ICON16 = "icon_small";
        private static final String PROP_ICON32 = "icon_large";

        /**
         * PaletteData constructor from a properties url defined in the palette layer.xml config.
         * @param props Properties from url.
         */
        ItemData(Properties props) {
            this.props = props;
            loadIcons();
        }

        private void loadIcons() {
            String iconId = props.getProperty(PROP_ICON16);
            icon16 = ImageUtilities.loadImage(iconId);

            iconId = props.getProperty(PROP_ICON32);
            icon32 = ImageUtilities.loadImage(iconId);
        }

        public String getType() {
            return props.getProperty(PROP_TYPE);
        }

        public String getQuantity() {
            return props.getProperty(PROP_QUANTITY);
        }

        public String getIdentifier() {
            return props.getProperty(PROP_IDENTIFIER);
        }

        public String getDisplayName() {
            return props.getProperty(PROP_NAME);
        }

        public String getComment() {
            return props.getProperty(PROP_COMMENT);
        }

        public String getIconBase() {
            return props.getProperty(PROP_ICON16);
        }

        public String getSmallImagePath() {
            return props.getProperty(PROP_ICON16);
        }

        public String getLargeImagePath() {
            return props.getProperty(PROP_ICON32);
        }

        public Image getSmallImage() {
            return icon16;
        }

        public Image getLargeImage() {
            return icon32;
        }
    }

    /**
     * Node for displaying a SymbolPallete.ItemDataObject in the palette.
     *
     */
    public static class ItemNode extends DataNode {

        ItemData item;
        private Image smallImage;
        private Image largeImage;

        public ItemNode(DataObject obj, ItemData item) {
            super(obj, Children.LEAF);
            this.item = item;

            setDisplayName(item.getDisplayName());
            setShortDescription(item.getComment());
            this.smallImage = item.getSmallImage();
            this.largeImage = item.getLargeImage();

            if (this.smallImage == null) {
                this.smallImage = ImageUtilities.loadImage("com/emxsys/wmt/globe/images/favorite.png");
            }
            if (this.largeImage == null) {
                this.largeImage = ImageUtilities.loadImage("com/emxsys/wmt/globe/images/favorite24.png");
            }
        }

        @Override
        public Image getIcon(int type) {
            if (type == BeanInfo.ICON_COLOR_16x16
                    || type == BeanInfo.ICON_MONO_16x16) {
                return this.smallImage;
            } else {
                return this.largeImage;
            }
        }

        /**
         * Allows a symbol node to be dragged from the palette
         *
         * @return
         * @throws IOException
         */
        @Override
        public Transferable drag() throws IOException {
            ExTransferable retValue = ExTransferable.create(super.drag());
            // Add item to the Transferable:
            if (item instanceof ItemData) {
                retValue.put(new ExTransferable.Single(SYMBOL_DATA_FLAVOR) {

                    @Override
                    protected Object getData() throws IOException, UnsupportedFlavorException {
                        return ItemNode.this.item;
                    }
                });
            }
            return retValue;
        }
    }

    /**
     * Provides an icon and label when a component is dragged from a palette.
     *
     * @author Bruce Schubert <bruce@emxsys.com>
     */
    public static class ItemDisplayer extends ScreenImage {

        private SymbolPalette.ItemData symbolData;

        public ItemDisplayer(ItemData data) {
            this.symbolData = data;
            if (data.getLargeImage() != null) {
                this.setImageSource(ImageUtil.toBufferedImage(data.getLargeImage()));
            }
        }

    }

    /**
     * This class manages the contents of a properties file that contains Palette ItemData for
     * Tactical Symbols. It manifests a SymbolPalette.ItemNode.
     *
     * Palette items are defined as .symbol files in the layer.xml config. For example:
     * <pre>
     * {@code <filesystem>
     *      <folder name="Globe Palette">
     *
     *          <!--Subfolders are the palette categories-->
     *
     *          <folder name="EMS Incidents">
     *              <attr name="position" intvalue="100"/>
     *
     *              <!--Files are palette items.  See the Globe's symbology SymbolPaletteItemDataObject class-->
     *
     *              <file name="EMS.INCDNT.FIRE.symbol" url="symbology/symbols/EMS.INCDNT.FIRE.properties">
     *                  <attr name="position" intvalue="100"/>
     *              </file>
     *              <file name="EMS.INCDNT.CVDIS.symbol" url="symbology/symbols/EMS.INCDNT.CVDIS.properties">
     *                  <attr name="position" intvalue="1000"/>
     *              </file>
     *          </folder>
     *      </folder>
     * </filesystem>}</pre>
     *
     * @author Bruce Schubert
     */
    @NbBundle.Messages({
        "SymbolPaletteItem=Symbol palette item",
        "SymbolPaletteItem_Loader=Symbol palette items",}
    )
    @MIMEResolver.ExtensionRegistration(
            displayName = "#SymbolPaletteItem",
            mimeType = "text/x-symbol",
            extension = "symbol"
    )
    @DataObject.Registration(
            mimeType = "text/x-symbol",
            iconBase = "com/emxsys/wmt/globe/images/favorite.png",
            displayName = "#SymbolPaletteItem_Loader",
            position = 2000)
    public static class ItemDataObject extends MultiDataObject {

        private ItemData data;

        public ItemDataObject(FileObject primaryFile, MultiFileLoader loader) throws
                DataObjectExistsException, IOException {
            super(primaryFile, loader);

            Properties properties;
            try ( // Read the file
                    InputStream inputStream = primaryFile.getInputStream()) {
                properties = new Properties();
                properties.load(inputStream);
            }
            data = new ItemData(properties);

            CookieSet cookies = getCookieSet();
            cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        }

        @Override
        protected Node createNodeDelegate() {
            //return new DataNode(this, Children.LEAF, getLookup());
            return new ItemNode(this, data);
        }

        @Override
        public Lookup getLookup() {
            return getCookieSet().getLookup();
        }
    }
}
